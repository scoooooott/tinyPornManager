/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieRenameTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.movies.MovieUIModule;

/**
 * MovieRenameAction - rename movies
 * 
 * @author Manuel Laggner
 */
public class MovieRenameAction extends TmmAction {
  private static final long           serialVersionUID = 4804592958868052533L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieRenameAction() {
    putValue(NAME, BUNDLE.getString("movie.rename")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.rename")); //$NON-NLS-1$
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Movie> selectedMovies = new ArrayList<>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    // rename
    TmmThreadPool renameTask = new MovieRenameTask(selectedMovies);
    if (TmmTaskManager.getInstance().addMainTask(renameTask)) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
    }
  }
}
