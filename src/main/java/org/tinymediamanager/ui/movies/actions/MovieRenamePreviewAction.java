/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.ui.movies.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.movies.dialogs.MovieRenamerPreviewDialog;

/**
 * The class MovieRenamePreviewAction. This action is for creating a preview of the renamer
 * 
 * @author Manuel Laggner
 */
public class MovieRenamePreviewAction extends TmmAction {
  private static final long           serialVersionUID = 5158514686702295145L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public MovieRenamePreviewAction() {
    putValue(NAME, BUNDLE.getString("movie.renamepreview"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.renamepreview.hint"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Movie> selectedMovies = MovieUIModule.getInstance().getSelectionModel().getSelectedMovies();

    if (selectedMovies.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    MovieRenamerPreviewDialog dialog = new MovieRenamerPreviewDialog(selectedMovies);
    dialog.setVisible(true);
  }
}
