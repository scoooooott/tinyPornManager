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
package org.tinymediamanager.ui.movies.filters;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a frame rate movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieFrameRateFilter extends AbstractMovieUIFilter {
  private MovieList         movieList = MovieList.getInstance();

  private JComboBox<Double> comboBox;

  public MovieFrameRateFilter() {
    super();
    buildAndInstallCodecArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallCodecArray();
    movieList.addPropertyChangeListener(Constants.FRAME_RATE, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "movieFrameRate";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return comboBox.getSelectedItem().toString();
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
    if (value instanceof Double) {
      comboBox.setSelectedItem(value);
    }
    else if (value instanceof String) {
      try {
        Double doubleValue = Double.valueOf((String) value);
        comboBox.setSelectedItem(doubleValue);
      }
      catch (Exception ignored) {
      }
    }
  }

  @Override
  public boolean accept(Movie movie) {
    Double frameRate = (Double) comboBox.getSelectedItem();
    if (frameRate == movie.getMediaInfoFrameRate()) {
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.framerate")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>();
    return comboBox;
  }

  private void buildAndInstallCodecArray() {
    // remove the listener to not firing unnecessary events
    comboBox.removeActionListener(actionListener);

    Double oldValue = (Double) comboBox.getSelectedItem();
    comboBox.removeAllItems();

    List<Double> frameRates = new ArrayList<>(movieList.getFrameRatesInMovies());
    Collections.sort(frameRates);
    for (Double frameRate : frameRates) {
      comboBox.addItem(frameRate);
    }

    if (oldValue != null) {
      comboBox.setSelectedItem(oldValue);
    }

    // re-add the itemlistener
    comboBox.addActionListener(actionListener);
  }
}
