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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a media source movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieMediaSourceFilter extends AbstractMovieUIFilter {
  private JComboBox<MediaSource> comboBox;

  @Override
  public String getId() {
    return "movieMediaSource";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return ((MediaSource) comboBox.getSelectedItem()).name();
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
    if (value instanceof MediaSource) {
      comboBox.setSelectedItem(value);
    }
    else if (value instanceof String) {
      MediaSource mediaSource = MediaSource.valueOf((String) value);
      if (mediaSource != null) {
        comboBox.setSelectedItem(mediaSource);
      }
    }
  }

  @Override
  public boolean accept(Movie movie) {
    if (movie.getMediaSource() == comboBox.getSelectedItem()) {
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>(MediaSource.values());
    return comboBox;
  }
}
