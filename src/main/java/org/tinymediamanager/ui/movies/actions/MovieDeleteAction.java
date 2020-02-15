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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.movies.MovieUIModule;

/**
 * The MovieDeleteAction - to remove all selected movies from the database and from the datasource
 * 
 * @author Manuel Laggner
 */
public class MovieDeleteAction extends TmmAction {
  private static final long           serialVersionUID = -984567332370801730L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public MovieDeleteAction() {
    putValue(SMALL_ICON, IconManager.DELETE_FOREVER);
    putValue(NAME, BUNDLE.getString("movie.delete"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.delete.hint"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Movie> selectedMovies = new ArrayList<>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    if (selectedMovies.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    // display warning and ask the user again
    Object[] options = { BUNDLE.getString("Button.yes"), BUNDLE.getString("Button.no") };
    int answer = JOptionPane.showOptionDialog(MainWindow.getActiveInstance(), BUNDLE.getString("movie.delete.desc"), BUNDLE.getString("movie.delete"), //$NON-NLS-2$
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
    if (answer != JOptionPane.YES_OPTION) {
      return;
    }

    // remove selected movies
    MovieList.getInstance().deleteMovies(selectedMovies);
  }
}
