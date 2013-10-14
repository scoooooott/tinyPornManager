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

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.movies.dialogs.MovieChooserDialog;

/**
 * MovieSingleScrapeAction - does a single scrape for a movie including moviechooser popup
 * 
 * @author Manuel Laggner
 */
public class MovieSingleScrapeAction extends AbstractAction {
  private static final long           serialVersionUID = 3066746719177708420L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /**
   * Instantiates a new SingleScrapeAction.
   * 
   * @param withTitle
   *          the with title
   */
  public MovieSingleScrapeAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("movie.scrape.selected")); //$NON-NLS-1$
    }
    putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.scrape.selected")); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    List<Movie> selectedMovies = new ArrayList<Movie>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    for (Movie movie : selectedMovies) {
      MovieChooserDialog dialogMovieChooser = new MovieChooserDialog(movie, selectedMovies.size() > 1 ? true : false);
      if (!dialogMovieChooser.showDialog()) {
        break;
      }
    }
  }
}
