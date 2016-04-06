/*
 * Copyright 2012 - 2015 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.movies;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.movies.actions.*;
import org.tinymediamanager.ui.movies.panels.MovieArtworkPanel;
import org.tinymediamanager.ui.movies.panels.MovieCastPanel;
import org.tinymediamanager.ui.movies.panels.MovieInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieListPanel;
import org.tinymediamanager.ui.movies.panels.MovieTrailerPanel;
import org.tinymediamanager.ui.movies.settings.MovieSettingsContainerPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieUIModule implements ITmmUIModule {
  private final static ResourceBundle    BUNDLE   = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static String            ID       = "movies";
  private static MovieUIModule           instance = null;

  private MovieListPanel                 listPanel;
  private JPanel                         detailPanel;
  private JPanel                         settingsPanel;
  private final MovieExtendedSearchPanel filterPanel;

  private final MovieSelectionModel      selectionModel;

  private Map<Class, Action>              actionMap;

  private Action                         searchAction;
  private Action                         editAction;
  private Action                         updateAction;
  private Action                         exportAction;

  private JPopupMenu                     popupMenu;
  private JPopupMenu                     updatePopupMenu;
  private JPopupMenu                     searchPopupMenu;
  private JPopupMenu                     editPopupMenu;

  private MovieUIModule() {
    actionMap = new HashMap<>();

    listPanel = new MovieListPanel();
    selectionModel = listPanel.getSelectionModel();

    detailPanel = new JPanel();
    detailPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow") }, new RowSpec[] { RowSpec.decode("default:grow") }));

    // layeredpane for displaying the filter dialog at the top
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default"), ColumnSpec.decode("default:grow") },
        new RowSpec[] { RowSpec.decode("default"), RowSpec.decode("default:grow") }));
    detailPanel.add(layeredPane, "1, 1, fill, fill");

    // tabbed pane containing the movie data
    JTabbedPane tabbedPane = new MainTabbedPane();
    tabbedPane.addTab(BUNDLE.getString("metatag.details"), new MovieInformationPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.addTab(BUNDLE.getString("metatag.cast"), new MovieCastPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.addTab(BUNDLE.getString("metatag.mediafiles"), new MovieMediaInformationPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.addTab(BUNDLE.getString("metatag.artwork"), new MovieArtworkPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.addTab(BUNDLE.getString("metatag.trailer"), new MovieTrailerPanel(selectionModel)); //$NON-NLS-1$
    layeredPane.add(tabbedPane, "1, 1, 2, 2, fill, fill");
    layeredPane.setLayer(tabbedPane, 0);

    // glass pane for searching/filtering
    filterPanel = new MovieExtendedSearchPanel(selectionModel);
    filterPanel.setVisible(false);
    layeredPane.add(filterPanel, "1, 1, fill, fill");
    layeredPane.setLayer(filterPanel, 1);

    listPanel.setInitialSelection();
    settingsPanel = new MovieSettingsContainerPanel();

    createActions();
    createPopupMenu();
    registerAccelerators();

    listPanel.setPopupMenu(popupMenu);
  }

  public static MovieUIModule getInstance() {
    if (instance == null) {
      instance = new MovieUIModule();
    }
    return instance;
  }

  public MovieSelectionModel getSelectionModel() {
    return selectionModel;
  }

  private void createActions() {
    searchAction = createAndRegisterAction(MovieSingleScrapeAction.class);
    editAction = createAndRegisterAction(MovieEditAction.class);
    updateAction = createAndRegisterAction(MovieUpdateDatasourceAction.class);
    exportAction = createAndRegisterAction(MovieExportAction.class);
  }

  /**
   * this factory creates the action and registers the hotkeys for accelerator management
   * @param actionClass the class of the action
   * @return the constructed action
     */
  private Action createAndRegisterAction(Class actionClass){
    Action action = actionMap.get(actionClass);
    if (action == null) {
      try {
        action = (Action) actionClass.newInstance();
        actionMap.put(actionClass, action);
        //KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
      } catch (Exception ignored) {
      }
    }
    return action;
  }

  private void createPopupMenu() {
    popupMenu = new JPopupMenu();
    popupMenu.add(createAndRegisterAction(MovieSingleScrapeAction.class));
    popupMenu.add(createAndRegisterAction(MovieSelectedScrapeAction.class));
    popupMenu.add(createAndRegisterAction(MovieUnscrapedScrapeAction.class));
    popupMenu.add(createAndRegisterAction(MovieSelectedScrapeMetadataAction.class));
    popupMenu.add(createAndRegisterAction(MovieAssignMovieSetAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieEditAction.class));
    popupMenu.add(createAndRegisterAction(MovieBatchEditAction.class));
    popupMenu.add(createAndRegisterAction(MovieSetWatchedFlagAction.class));
    popupMenu.add(createAndRegisterAction(MovieRewriteNfoAction.class));
    popupMenu.add(createAndRegisterAction(MovieRenameAction.class));
    popupMenu.add(createAndRegisterAction(MovieRenamePreviewAction.class));
    popupMenu.add(createAndRegisterAction(MovieMediaInformationAction.class));
    popupMenu.add(createAndRegisterAction(MovieExportAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieSyncTraktTvAction.class));
    popupMenu.add(createAndRegisterAction(MovieSyncWatchedTraktTvAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieClearImageCacheAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieRemoveAction.class));
    popupMenu.add(createAndRegisterAction(MovieDeleteAction.class));

    // update popup menu
    updatePopupMenu = new JPopupMenu();
    updatePopupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        updatePopupMenu.removeAll();
        updatePopupMenu.add(createAndRegisterAction(MovieUpdateDatasourceAction.class));
        updatePopupMenu.addSeparator();
        for (String ds : Globals.settings.getMovieSettings().getMovieDataSource()) {
          updatePopupMenu.add(new MovieUpdateSingleDatasourceAction(ds));
        }
        updatePopupMenu.pack();
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
    });

    // search popup menu
    searchPopupMenu = new JPopupMenu();
    searchPopupMenu.add(createAndRegisterAction(MovieSingleScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(MovieSelectedScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(MovieUnscrapedScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(MovieSelectedScrapeMetadataAction.class));

    // edit popup menu
    editPopupMenu = new JPopupMenu();
    editPopupMenu.add(createAndRegisterAction(MovieEditAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieBatchEditAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieSetWatchedFlagAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRewriteNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRenameAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRenamePreviewAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieMediaInformationAction.class));
    editPopupMenu.addSeparator();
    editPopupMenu.add(createAndRegisterAction(MovieSyncTraktTvAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieSyncWatchedTraktTvAction.class));
  }

  /**
   * register accelerators
   */
  private void registerAccelerators(){
    for(Map.Entry<Class, Action> entry : actionMap.entrySet()){
      try {
        KeyStroke keyStroke = (KeyStroke)entry.getValue().getValue(Action.ACCELERATOR_KEY);
        if (keyStroke != null) {
          String actionMapKey = "action" + entry.getKey().getName();
          listPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionMapKey);
          listPanel.getActionMap().put(actionMapKey, entry.getValue());
        }
      } catch (Exception ignored) {
      }
    }
  }


  public void setFilterMenuVisible(boolean visible) {
    filterPanel.setVisible(visible);
  }

  @Override
  public String getModuleId() {
    return ID;
  }

  @Override
  public JPanel getTabPanel() {
    return listPanel;
  }

  @Override
  public String getTabTitle() {
    return BUNDLE.getString("tmm.movies"); //$NON-NLS-1$ )
  }

  @Override
  public JPanel getDetailPanel() {
    return detailPanel;
  }

  @Override
  public Action getSearchAction() {
    return searchAction;
  }

  @Override
  public JPopupMenu getSearchMenu() {
    return searchPopupMenu;
  }

  @Override
  public Action getEditAction() {
    return editAction;
  }

  @Override
  public JPopupMenu getEditMenu() {
    return editPopupMenu;
  }

  @Override
  public Action getUpdateAction() {
    return updateAction;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    return updatePopupMenu;
  }

  @Override
  public Action getExportAction() {
    return exportAction;
  }

  @Override
  public JPanel getSettingsPanel() {
    if (settingsPanel == null) {
      settingsPanel = new MovieSettingsContainerPanel();
    }
    return settingsPanel;
  }
}
