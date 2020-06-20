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
package org.tinymediamanager.ui.movies.filters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a genre movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieGenreFilter extends AbstractCheckComboBoxMovieUIFilter<MediaGenres> {
  private final Comparator<MediaGenres> comparator;
  private final MovieList               movieList;

  public MovieGenreFilter() {
    super();
    movieList = MovieList.getInstance();
    comparator = new MediaGenres.MediaGenresComparator();

    buildAndInstallMediaGenres();
    movieList.addPropertyChangeListener(Constants.GENRE, evt -> SwingUtilities.invokeLater(this::buildAndInstallMediaGenres));
  }

  @Override
  public String getId() {
    return "movieGenre";
  }

  @Override
  public boolean accept(Movie movie) {
    List<MediaGenres> selectedItems = checkComboBox.getSelectedItems();

    // check for explicit empty search
    if (selectedItems.isEmpty() && movie.getGenres().isEmpty()) {
      return true;
    }

    // check for all values
    for (MediaGenres genre : movie.getGenres()) {
      if (selectedItems.contains(genre)) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.genre"));
  }

  private void buildAndInstallMediaGenres() {
    ArrayList<MediaGenres> usedGenres = new ArrayList<>(movieList.getUsedGenres());
    usedGenres.sort(comparator);
    setValues(usedGenres);
  }

  @Override
  protected String parseTypeToString(MediaGenres type) throws Exception {
    return type.name();
  }

  @Override
  protected MediaGenres parseStringToType(String string) throws Exception {
    return MediaGenres.getGenre(string);
  }
}
