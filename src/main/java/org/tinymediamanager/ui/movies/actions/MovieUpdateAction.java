/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask2;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;

/**
 * MovieUpdateSingleDatasourceAction - update all movies from a single data source
 * 
 * @author Manuel Laggner
 */
public class MovieUpdateAction extends AbstractAction {
  private static final long           serialVersionUID = 6885253964781733478L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieUpdateAction() {
    putValue(NAME, BUNDLE.getString("movie.update"));
    putValue(SMALL_ICON, IconManager.REFRESH);
    putValue(LARGE_ICON_KEY, IconManager.REFRESH);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<Movie> selectedMovies = MovieUIModule.getInstance().getSelectionModel().getSelectedMovies();
    if (selectedMovies.isEmpty()) {
      return;
    }

    TmmThreadPool task = new MovieUpdateDatasourceTask2(selectedMovies);
    if (TmmTaskManager.getInstance().addMainTask(task)) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
    }
  }
}
