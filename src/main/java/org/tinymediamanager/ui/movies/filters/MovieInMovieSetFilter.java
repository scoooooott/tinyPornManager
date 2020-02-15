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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a movie set movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieInMovieSetFilter extends AbstractMovieUIFilter {
  private enum MovieInMovieSet {
    IN_MOVIESET(BUNDLE.getString("movie.inmovieset")),
    NOT_IN_MOVIESET(BUNDLE.getString("movie.notinmovieset"));

    private String title;

    MovieInMovieSet(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  private JComboBox<MovieInMovieSet> combobox;

  @Override
  public String getId() {
    return "movieInMovieSet";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return ((MovieInMovieSet) combobox.getSelectedItem()).name();
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof MovieInMovieSet) {
      combobox.setSelectedItem(value);
    }
    else if (value instanceof String) {
      MovieInMovieSet movieInMovieSet = MovieInMovieSet.valueOf((String) value);
      if (movieInMovieSet != null) {
        combobox.setSelectedItem(movieInMovieSet);
      }
    }
  }

  @Override
  public boolean accept(Movie movie) {
    if (combobox.getSelectedItem() == MovieInMovieSet.IN_MOVIESET) {
      return movie.getMovieSet() != null;
    }
    else {
      return movie.getMovieSet() == null;
    }
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.movieset"));
  }

  @Override
  protected JComponent createFilterComponent() {
    combobox = new JComboBox<>(MovieInMovieSet.values());
    return combobox;
  }
}
