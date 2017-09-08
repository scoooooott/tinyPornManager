/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.ui.moviesets;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.movies.panels.MovieArtworkPanel;
import org.tinymediamanager.ui.movies.panels.MovieCastPanel;
import org.tinymediamanager.ui.movies.panels.MovieInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieMediaInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieTrailerPanel;
import org.tinymediamanager.ui.moviesets.actions.MovieEditAction;
import org.tinymediamanager.ui.moviesets.actions.MovieSetAddAction;
import org.tinymediamanager.ui.moviesets.actions.MovieSetEditAction;
import org.tinymediamanager.ui.moviesets.actions.MovieSetRemoveAction;
import org.tinymediamanager.ui.moviesets.actions.MovieSetRenameAction;
import org.tinymediamanager.ui.moviesets.actions.MovieSetSearchAction;
import org.tinymediamanager.ui.moviesets.panels.MovieSetExtendedSearchPanel;
import org.tinymediamanager.ui.moviesets.panels.MovieSetInformationPanel;
import org.tinymediamanager.ui.moviesets.panels.MovieSetTreePanel;
import org.tinymediamanager.ui.settings.TmmSettingsNode;

import net.miginfocom.swing.MigLayout;

public class MovieSetUIModule implements ITmmUIModule {
  private final static ResourceBundle       BUNDLE   = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static String               ID       = "movieSets";

  private static MovieSetUIModule           instance = null;

  private final MovieSetSelectionModel      selectionModel;
  private final MovieSelectionModel         movieSelectionModel;

  private Map<Class, Action>                actionMap;

  private final MovieSetTreePanel           listPanel;
  private final JPanel                      detailPanel;
  private final JPanel                      dataPanel;
  private final MovieSetExtendedSearchPanel filterPanel;

  private JPopupMenu                        popupMenu;
  private Action                            searchAction;
  private Action                            editAction;

  private MovieSetUIModule() {
    actionMap = new HashMap<>();

    selectionModel = new MovieSetSelectionModel();
    movieSelectionModel = new MovieSelectionModel();

    listPanel = new MovieSetTreePanel(selectionModel);

    detailPanel = new JPanel();
    detailPanel.setLayout(new CardLayout());

    // layeredpane for displaying the filter dialog at the top
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));
    detailPanel.add(layeredPane, "cell 0 0, grow");

    dataPanel = new JPanel();
    dataPanel.setOpaque(false);
    dataPanel.setLayout(new CardLayout());

    // panel for movie sets
    JTabbedPane movieSetDetailPanel = new MainTabbedPane();
    movieSetDetailPanel.addTab(BUNDLE.getString("metatag.details"), new MovieSetInformationPanel(selectionModel));//$NON-NLS-1$
    dataPanel.add(movieSetDetailPanel, "movieSet");

    // panel for movies
    JTabbedPane movieDetailPanel = new MainTabbedPane();
    movieDetailPanel.addTab("Details", new MovieInformationPanel(movieSelectionModel));
    movieDetailPanel.addTab("Cast", new MovieCastPanel(movieSelectionModel));
    movieDetailPanel.addTab("Media files", new MovieMediaInformationPanel(movieSelectionModel));
    movieDetailPanel.addTab("Artwork", new MovieArtworkPanel(movieSelectionModel));
    movieDetailPanel.addTab("Trailer", new MovieTrailerPanel(movieSelectionModel));
    dataPanel.add(movieDetailPanel, "movie");

    layeredPane.add(dataPanel, "cell 0 0, grow");
    layeredPane.setLayer(dataPanel, 0);

    // glass pane for searching/filtering
    filterPanel = new MovieSetExtendedSearchPanel(listPanel.getTreeTable());
    filterPanel.setVisible(false);
    layeredPane.add(filterPanel, "pos 0 0");
    layeredPane.setLayer(filterPanel, 1);

    // create actions and menus
    createActions();
    createPopupMenu();
    registerAccelerators();

    // further initializations
    init();
  }

  public static MovieSetUIModule getInstance() {
    if (instance == null) {
      instance = new MovieSetUIModule();
    }
    return instance;
  }

  private void init() {
    // re-set filters
    if (MovieModuleManager.SETTINGS.isStoreUiFilters()) {
      SwingUtilities.invokeLater(() -> listPanel.getTreeTable().setFilterValues(MovieModuleManager.SETTINGS.getMovieSetUiFilters()));
    }
  }

  public void setFilterMenuVisible(boolean visible) {
    filterPanel.setVisible(visible);
  }

  private void createActions() {
    searchAction = createAndRegisterAction(MovieSetSearchAction.class);
    editAction = createAndRegisterAction(MovieSetEditAction.class);
  }

  private void createPopupMenu() {
    // popup menu
    popupMenu = new JPopupMenu();

    // movieset actions
    popupMenu.add(createAndRegisterAction(MovieSetAddAction.class));
    popupMenu.add(createAndRegisterAction(MovieSetRemoveAction.class));
    popupMenu.add(createAndRegisterAction(MovieSetEditAction.class));
    popupMenu.add(createAndRegisterAction(MovieSetSearchAction.class));

    // movie actions
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieEditAction.class));

    // actions for both of them
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieSetRenameAction.class));
  }

  /**
   * this factory creates the action and registers the hotkeys for accelerator management
   *
   * @param actionClass
   *          the class of the action
   * @return the constructed action
   */
  private Action createAndRegisterAction(Class<? extends Action> actionClass) {
    Action action = actionMap.get(actionClass);
    if (action == null) {
      try {
        action = (Action) actionClass.newInstance();
        actionMap.put(actionClass, action);
        // KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
      }
      catch (Exception ignored) {
      }
    }
    return action;
  }

  /**
   * register accelerators
   */
  private void registerAccelerators() {
    for (Map.Entry<Class, Action> entry : actionMap.entrySet()) {
      try {
        KeyStroke keyStroke = (KeyStroke) entry.getValue().getValue(Action.ACCELERATOR_KEY);
        if (keyStroke != null) {
          String actionMapKey = "action" + entry.getKey().getName();
          listPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionMapKey);
          listPanel.getActionMap().put(actionMapKey, entry.getValue());
        }
      }
      catch (Exception ignored) {
      }
    }
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
    return BUNDLE.getString("tmm.moviesets"); //$NON-NLS-1$
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
    return null;
  }

  @Override
  public Action getEditAction() {
    return editAction;
  }

  @Override
  public JPopupMenu getEditMenu() {
    return null;
  }

  @Override
  public Action getUpdateAction() {
    return null;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    return null;
  }

  @Override
  public Action getRenameAction() {
    return null;
  }

  @Override
  public JPopupMenu getRenameMenu() {
    return null;
  }

  @Override
  public Action getExportAction() {
    return null;
  }

  public MovieSetSelectionModel getSelectionModel() {
    return selectionModel;
  }

  @Override
  public TmmSettingsNode getSettingsNode() {
    return null;
  }

  public void setSelectedMovieSet(MovieSet movieSet) {
    selectionModel.setSelectedMovieSet(movieSet);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "movieSet");
  }

  public void setSelectedMovie(Movie movie) {
    movieSelectionModel.setSelectedMovie(movie);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "movie");
  }
}
