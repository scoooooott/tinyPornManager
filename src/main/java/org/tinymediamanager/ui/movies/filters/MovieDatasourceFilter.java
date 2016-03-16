/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a video - 3D movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieDatasourceFilter extends AbstractMovieUIFilter {
  private MovieSettings     movieSettings = Settings.getInstance().getMovieSettings();

  private JComboBox<String> comboBox;

  public MovieDatasourceFilter() {
    super();
    buildAndInstallDatasourceArray();
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        buildAndInstallDatasourceArray();
      }
    };
    movieSettings.addPropertyChangeListener(Constants.DATA_SOURCE, propertyChangeListener);
  }

  @Override
  public boolean accept(Movie movie) {
    String datasource = (String) comboBox.getSelectedItem();
    if (datasource.equals(movie.getDataSource())) {
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>();
    return comboBox;
  }

  private void buildAndInstallDatasourceArray() {
    String oldValue = (String) comboBox.getSelectedItem();
    comboBox.removeAllItems();

    List<String> datasources = new ArrayList<>(movieSettings.getMovieDataSource());
    Collections.sort(datasources);
    for (String datasource : datasources) {
      comboBox.addItem(datasource);
    }

    if (oldValue != null) {
      comboBox.setSelectedItem(oldValue);
    }
  }
}
