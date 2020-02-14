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
 * this class is used for a year movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieYearFilter extends AbstractCheckComboBoxMovieUIFilter<Integer> {
  private MovieList movieList = MovieList.getInstance();

  public MovieYearFilter() {
    super();
    buildAndInstallYears();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallYears();
    movieList.addPropertyChangeListener(Constants.YEAR, propertyChangeListener);
  }

  private void buildAndInstallYears() {
    List<Integer> years = new ArrayList<>(movieList.getYearsInMovies());
    Collections.sort(years);
    setValues(years);
  }

  @Override
  public String getId() {
    return "movieYear";
  }

  @Override
  public boolean accept(Movie movie) {
    List<Integer> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getYear());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.year"));
  }

  @Override
  protected String parseTypeToString(Integer type) throws Exception {
    return type.toString();
  }

  @Override
  protected Integer parseStringToType(String string) throws Exception {
    return Integer.parseInt(string);
  }
}
