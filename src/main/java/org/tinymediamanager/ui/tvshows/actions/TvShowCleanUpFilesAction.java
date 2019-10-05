package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.dialogs.CleanUpUnwantedFilesDialog;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

public class TvShowCleanUpFilesAction extends TmmAction {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowCleanUpFilesAction() {

    putValue(NAME, BUNDLE.getString("cleanupfiles")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("cleanupfiles.desc")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.DELETE);
    putValue(LARGE_ICON_KEY, IconManager.DELETE);

  }

  @Override
  protected void processAction(ActionEvent e) {

    List<MediaEntity> selectedTvShows = new ArrayList<>(TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows());

    if (selectedTvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected")); //$NON-NLS-1$
      return;
    }

    CleanUpUnwantedFilesDialog dialog = new CleanUpUnwantedFilesDialog(selectedTvShows);
    dialog.setVisible(true);

  }
}
