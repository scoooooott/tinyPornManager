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
package org.tinymediamanager.ui.movies.filters;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * This class implements a tag filter for movies
 * 
 * @author Manuel Laggner
 */
public class MovieTagFilter extends AbstractMovieUIFilter {
  private MovieList                movieList = MovieList.getInstance();

  private TmmCheckComboBox<String> checkComboBox;

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
  public String getFilterValueAsString() {
    try {
      return objectMapper.writeValueAsString(checkComboBox.getSelectedItems());
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    try {
      List<String> selectedItems = objectMapper.readValue((String) value,
          objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
      checkComboBox.setSelectedItems(selectedItems);
    }
    catch (Exception ignored) {
    }
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> tags = checkComboBox.getSelectedItems();
    if (movie.getTags().containsAll(tags)) {
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    checkComboBox = new TmmCheckComboBox<>();
    return checkComboBox;
  }

  private void buildAndInstallTagsArray() {
    // remove the listener to not firing unnecessary events
    checkComboBox.removeActionListener(actionListener);

    List<String> selectedItems = checkComboBox.getSelectedItems();

    List<String> tags = new ArrayList<>(movieList.getTagsInMovies());
    Collections.sort(tags);

    checkComboBox.setItems(tags);

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }

    // re-add the itemlistener
    checkBox.addActionListener(actionListener);
  }
}
