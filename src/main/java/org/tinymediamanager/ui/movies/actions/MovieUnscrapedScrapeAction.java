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
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieScrapeTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.movies.dialogs.MovieScrapeMetadataDialog;

/**
 * The MovieUnscrapedScrapeAction - to scrape all unscraped movies
 * 
 * @author Manuel Laggner
 */
public class MovieUnscrapedScrapeAction extends TmmAction {
  private static final long           serialVersionUID = -5330113139288186736L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public MovieUnscrapedScrapeAction() {
    putValue(NAME, BUNDLE.getString("movie.scrape.unscraped"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.scrape.unscraped.desc"));
    putValue(SMALL_ICON, IconManager.SEARCH);
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Movie> unscrapedMovies = MovieList.getInstance().getUnscrapedMovies();
    if (!unscrapedMovies.isEmpty()) {
      MovieScrapeMetadataDialog dialog = new MovieScrapeMetadataDialog(BUNDLE.getString("movie.scrape.unscraped"));
      dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
      dialog.setVisible(true);

      // get options from dialog
      MovieSearchAndScrapeOptions options = dialog.getMovieSearchAndScrapeOptions();
      List<MovieScraperMetadataConfig> config = dialog.getMovieScraperMetadataConfig();

      // do we want to scrape?
      if (dialog.shouldStartScrape()) {
        // scrape
        TmmThreadPool scrapeTask = new MovieScrapeTask(unscrapedMovies, true, options, config);
        TmmTaskManager.getInstance().addMainTask(scrapeTask);
      }
    }
  }
}
