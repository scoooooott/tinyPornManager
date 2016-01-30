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

import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.movies.actions.MovieAssignMovieSetAction;
import org.tinymediamanager.ui.movies.actions.MovieBatchEditAction;
import org.tinymediamanager.ui.movies.actions.MovieClearImageCacheAction;
import org.tinymediamanager.ui.movies.actions.MovieDeleteAction;
import org.tinymediamanager.ui.movies.actions.MovieEditAction;
import org.tinymediamanager.ui.movies.actions.MovieExportAction;
import org.tinymediamanager.ui.movies.actions.MovieMediaInformationAction;
import org.tinymediamanager.ui.movies.actions.MovieRemoveAction;
import org.tinymediamanager.ui.movies.actions.MovieRenameAction;
import org.tinymediamanager.ui.movies.actions.MovieRenamePreviewAction;
import org.tinymediamanager.ui.movies.actions.MovieRewriteNfoAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeMetadataAction;
import org.tinymediamanager.ui.movies.actions.MovieSetWatchedFlagAction;
import org.tinymediamanager.ui.movies.actions.MovieSingleScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSyncTraktTvAction;
import org.tinymediamanager.ui.movies.actions.MovieSyncWatchedTraktTvAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateDatasourceAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateSingleDatasourceAction;
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

  private Action                         searchAction;
  private Action                         editAction;
  private Action                         updateAction;

  private JPopupMenu                     popupMenu;
  private JPopupMenu                     updatePopupMenu;
  private JPopupMenu                     searchPopupMenu;
  private JPopupMenu                     editPopupMenu;

  private MovieUIModule() {
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
    searchAction = new MovieSingleScrapeAction(false);
    editAction = new MovieEditAction(false);
    updateAction = new MovieUpdateDatasourceAction(false);
  }

  private void createPopupMenu() {
    popupMenu = new JPopupMenu();
    popupMenu.add(new MovieSingleScrapeAction(true));
    popupMenu.add(new MovieSelectedScrapeAction());
    popupMenu.add(new MovieSelectedScrapeMetadataAction());
    popupMenu.add(new MovieAssignMovieSetAction());
    popupMenu.addSeparator();
    popupMenu.add(new MovieEditAction(true));
    popupMenu.add(new MovieBatchEditAction());
    popupMenu.add(new MovieSetWatchedFlagAction());
    popupMenu.add(new MovieRewriteNfoAction());
    popupMenu.add(new MovieRenameAction(true));
    popupMenu.add(new MovieRenamePreviewAction());
    popupMenu.add(new MovieMediaInformationAction(true));
    popupMenu.add(new MovieExportAction());
    popupMenu.addSeparator();
    popupMenu.add(new MovieSyncTraktTvAction());
    popupMenu.add(new MovieSyncWatchedTraktTvAction());
    popupMenu.addSeparator();
    popupMenu.add(new MovieClearImageCacheAction());
    popupMenu.addSeparator();
    popupMenu.add(new MovieRemoveAction());
    popupMenu.add(new MovieDeleteAction());

    // update popup menu
    updatePopupMenu = new JPopupMenu();
    updatePopupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        updatePopupMenu.removeAll();
        updatePopupMenu.add(new MovieUpdateDatasourceAction(true));
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
    searchPopupMenu.add(new MovieSingleScrapeAction(true));
    searchPopupMenu.add(new MovieSelectedScrapeAction());
    searchPopupMenu.add(new MovieSelectedScrapeMetadataAction());

    // edit popup menu
    editPopupMenu = new JPopupMenu();
    editPopupMenu.add(new MovieEditAction(true));
    editPopupMenu.add(new MovieBatchEditAction());
    editPopupMenu.add(new MovieSetWatchedFlagAction());
    editPopupMenu.add(new MovieRewriteNfoAction());
    editPopupMenu.add(new MovieRenameAction(true));
    editPopupMenu.add(new MovieRenamePreviewAction());
    editPopupMenu.add(new MovieMediaInformationAction(true));

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
    return null;
  }

  @Override
  public JPopupMenu getExportMenu() {
    return null;
  }

  @Override
  public JPanel getSettingsPanel() {
    if (settingsPanel == null) {
      settingsPanel = new MovieSettingsContainerPanel();
    }
    return settingsPanel;
  }
}
