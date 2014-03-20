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
package org.tinymediamanager.ui.movies.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.movies.dialogs.MovieBatchEditorDialog;

/**
 * The MovieBatchEditAction - to start a bulk edit of movies
 * 
 * @author Manuel Laggner
 */
public class MovieBatchEditAction extends AbstractAction {
  private static final long           serialVersionUID = -3974602352019088416L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieBatchEditAction() {
    putValue(NAME, BUNDLE.getString("movie.bulkedit")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.bulkedit.desc")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.EDIT);
    putValue(LARGE_ICON_KEY, IconManager.EDIT);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    List<Movie> selectedMovies = new ArrayList<Movie>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    // get data of all files within all selected movies
    if (selectedMovies.size() > 0) {
      MovieBatchEditorDialog editor = new MovieBatchEditorDialog(selectedMovies);
      editor.setLocationRelativeTo(MainWindow.getActiveInstance());
      editor.pack();
      editor.setVisible(true);
    }
  }
}
