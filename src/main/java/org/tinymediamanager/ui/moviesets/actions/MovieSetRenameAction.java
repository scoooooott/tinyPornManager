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
package org.tinymediamanager.ui.moviesets.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.tasks.MovieRenameTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieSetRenameAction extends TmmAction {
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final long           serialVersionUID = 1677285197819210130L;

  public MovieSetRenameAction() {
    putValue(NAME, BUNDLE.getString("movie.rename")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.rename")); //$NON-NLS-1$
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Object> selectedObjects = MovieSetUIModule.getInstance().getSelectionModel().getSelectedObjects();
    Set<Movie> selectedMovies = new HashSet<>();

    for (Object obj : selectedObjects) {
      if (obj instanceof Movie) {
        selectedMovies.add((Movie) obj);
      }
      else if (obj instanceof MovieSet) {
        selectedMovies.addAll(((MovieSet) obj).getMovies());
      }
    }

    // rename
    TmmThreadPool renameTask = new MovieRenameTask(new ArrayList<>(selectedMovies));
    if (TmmTaskManager.getInstance().addMainTask(renameTask)) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
    }
  }
}
