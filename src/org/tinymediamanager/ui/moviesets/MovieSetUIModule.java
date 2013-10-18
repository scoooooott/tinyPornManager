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
}
