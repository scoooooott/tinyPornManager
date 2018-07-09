/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.moviesets.filters;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;
import org.tinymediamanager.ui.moviesets.AbstractMovieSetUIFilter;

/**
 * this class is used for a movie set data source filter
 * 
 * @author Manuel Laggner
 */
public class MovieSetDatasourceFilter extends AbstractMovieSetUIFilter {
  private MovieSettings            movieSettings = MovieModuleManager.SETTINGS;

  private TmmCheckComboBox<String> checkComboBox;

  public MovieSetDatasourceFilter() {
    super();
    buildAndInstallDatasourceArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallDatasourceArray();
    movieSettings.addPropertyChangeListener(Constants.DATA_SOURCE, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "movieDatasource";
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
  public boolean accept(MovieSet movieSet, List<Movie> movies) {
    List<String> datasources = checkComboBox.getSelectedItems();
    for (Movie movie : movies) {
      if (datasources.contains(movie.getDataSource())) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    checkComboBox = new TmmCheckComboBox<>();
    return checkComboBox;
  }

  private void buildAndInstallDatasourceArray() {
    // remove the listener to not firing unnecessary events
    checkComboBox.removeActionListener(actionListener);

    List<String> selectedItems = checkComboBox.getSelectedItems();

    List<String> datasources = new ArrayList<>(movieSettings.getMovieDataSource());
    Collections.sort(datasources);

    checkComboBox.setItems(datasources);

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }

    // re-add the itemlistener
    checkComboBox.addActionListener(actionListener);
  }
}
