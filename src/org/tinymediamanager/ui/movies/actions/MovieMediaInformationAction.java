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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieReloadMediaInformationTask;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmSwingWorker;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;

/**
 * MovieMediaInformationAction - gather MediaInformation for movies
 * 
 * @author Manuel Laggner
 */
public class MovieMediaInformationAction extends AbstractAction {
  private static final long           serialVersionUID = 4927466975489852998L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieMediaInformationAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("movie.updatemediainfo")); //$NON-NLS-1$
    }
    putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/mediainfo.png")));
    putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/mediainfo.png")));
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
      TmmSwingWorker task = new MovieReloadMediaInformationTask(selectedMovies);
      if (!MainWindow.executeMainTask(task)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }
}
