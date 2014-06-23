/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import javax.swing.JOptionPane;

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;

/**
 * The MovieDeleteAction - to remove all selected movies from the database and from the datasource
 * 
 * @author Manuel Laggner
 */
public class MovieDeleteAction extends AbstractAction {
  private static final long           serialVersionUID = -984567332370801730L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieDeleteAction() {
    putValue(SMALL_ICON, IconManager.CROSS);
    putValue(NAME, BUNDLE.getString("movie.delete")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.delete.hint")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    List<Movie> selectedMovies = new ArrayList<Movie>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    // display warning and ask the user again
    int answer = JOptionPane.showConfirmDialog(MainWindow.getActiveInstance(), BUNDLE.getString("movie.delete.hint") + "?",
        BUNDLE.getString("movie.delete"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
    if (answer != JOptionPane.OK_OPTION) {
      return;
    }

    // remove selected movies
    MovieList.getInstance().deleteMovies(selectedMovies);
  }
}
