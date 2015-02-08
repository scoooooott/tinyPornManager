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
package org.tinymediamanager.ui.moviesets;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.MainWindow;

public class MovieSetUIModule implements ITmmUIModule {
  private final static String          ID       = "movieSets";
  private static MovieSetUIModule      instance = null;

  private final MovieSetSelectionModel selectionModel;

  private MovieSetUIModule() {
    // this will be used in v3
    // listPanel = new MoviePanel();
    // selectionModel = listPanel.movieSelectionModel;
    // detailPanel = new MovieInformationPanel(selectionModel);

    // createActions();
    // createPopupMenu();

    selectionModel = MainWindow.getActiveInstance().getMovieSetPanel().movieSetSelectionModel;
  }

  public static MovieSetUIModule getInstance() {
    if (instance == null) {
      instance = new MovieSetUIModule();
    }
    return instance;
  }

  @Override
  public String getModuleId() {
    return ID;
  }

  @Override
  public JPanel getTabPanel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTabTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPanel getDetailPanel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getSearchAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getSearchMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getEditAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getEditMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getUpdateAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getExportAction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPopupMenu getExportMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  public MovieSetSelectionModel getSelectionModel() {
    return selectionModel;
  }

  @Override
  public JPanel getSettingsPanel() {
    return null;
  }
}
