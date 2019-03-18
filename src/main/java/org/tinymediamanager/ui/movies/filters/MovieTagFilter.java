/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * This class implements a tag filter for movies
 * 
 * @author Manuel Laggner
 */
public class MovieTagFilter extends AbstractCheckComboBoxMovieUIFilter<String> {
  private MovieList movieList = MovieList.getInstance();

  public MovieTagFilter() {
    super();
    buildAndInstallTagsArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallTagsArray();
    movieList.addPropertyChangeListener(Constants.TAG, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "movieTag";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> tags = checkComboBox.getSelectedItems();
    for (String tag : movie.getTags()) {
      if (tags.contains(tag)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
  }

  private void buildAndInstallTagsArray() {
    List<String> tags = new ArrayList<>(movieList.getTagsInMovies());
    Collections.sort(tags);

    setValues(tags);
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }
}
