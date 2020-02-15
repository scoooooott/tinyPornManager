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
package org.tinymediamanager.ui.moviesets.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieSetSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.tasks.MovieSetMissingArtworkDownloadTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;

/**
 * the class {@link MovieSetMissingArtworkAction} is used to scrape missing artwork for movie sets
 * 
 * @author Wolfgang Janes
 */
public class MovieSetMissingArtworkAction extends TmmAction {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  public MovieSetMissingArtworkAction() {
    putValue(NAME, BUNDLE.getString("movieset.downloadmissingartwork"));
    putValue(SMALL_ICON, IconManager.IMAGE);
    putValue(LARGE_ICON_KEY, IconManager.IMAGE);
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<MovieSet> selectedMovieSets = new ArrayList<>(MovieSetUIModule.getInstance().getSelectionModel().getSelectedMovieSets());

    if (selectedMovieSets.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    MovieSetSearchAndScrapeOptions options = new MovieSetSearchAndScrapeOptions();
    options.loadDefaults();

    MovieSetMissingArtworkDownloadTask task = new MovieSetMissingArtworkDownloadTask(selectedMovieSets, options);
    TmmTaskManager.getInstance().addDownloadTask(task);
  }
}
