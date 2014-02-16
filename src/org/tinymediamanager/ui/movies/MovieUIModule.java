/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.actions.MovieEditAction;
import org.tinymediamanager.ui.movies.actions.MovieSingleScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateDatasourceAction;
import org.tinymediamanager.ui.movies.settings.MovieSettingsContainerPanel;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieUIModule implements ITmmUIModule {
  private final static ResourceBundle BUNDLE   = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static String         ID       = "movies";
  private static MovieUIModule        instance = null;

  private MoviePanel                  listPanel;
  private JPanel                      detailPanel;
  private final JPanel                settingsPanel;

  private final MovieSelectionModel   selectionModel;

  private Action                      searchAction;
  private Action                      editAction;
  private Action                      updateAction;

  private JPopupMenu                  searchPopupMenu;
  private JPopupMenu                  editPopupMenu;

  private MovieUIModule() {
    // this will be used in v3
    // listPanel = new MoviePanel();
    // selectionModel = listPanel.movieSelectionModel;
    // detailPanel = new MovieInformationPanel(selectionModel);

    // createActions();
    // createPopupMenu();

    settingsPanel = new MovieSettingsContainerPanel();
    selectionModel = MainWindow.getActiveInstance().getMoviePanel().movieSelectionModel;
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
    // search popup menu
    searchPopupMenu = new JPopupMenu();
    searchPopupMenu.add(new MovieSingleScrapeAction(true));
    // popupMenu.add(actionScrapeSelected);
    // popupMenu.add(actionScrapeMetadataSelected);
    // popupMenu.addSeparator();
    // popupMenu.add(actionEditMovie2);
    // popupMenu.add(actionBatchEdit);
    // popupMenu.add(actionRename2);
    // popupMenu.add(actionMediaInformation2);
    // popupMenu.add(actionExport);
    // popupMenu.addSeparator();
    // popupMenu.add(actionRemove2);

    // edit popup menu
    editPopupMenu = new JPopupMenu();
    editPopupMenu.add(new MovieEditAction(true));

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
    return BUNDLE.getString("tmm.movies"); //$NON-NLS-1$)
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
    return null;
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
    return settingsPanel;
  }
}
