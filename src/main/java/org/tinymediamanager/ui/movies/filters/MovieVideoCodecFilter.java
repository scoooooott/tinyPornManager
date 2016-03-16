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
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a video codec movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieVideoCodecFilter extends AbstractMovieUIFilter {
  private MovieList         movieList = MovieList.getInstance();

  private JComboBox<String> comboBox;

  public MovieVideoCodecFilter() {
    super();
    buildAndInstallCodecArray();
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        buildAndInstallCodecArray();
      }
    };
    movieList.addPropertyChangeListener(Constants.VIDEO_CODEC, propertyChangeListener);
  }

  @Override
  public boolean accept(Movie movie) {
    String videoCodec = (String) comboBox.getSelectedItem();
    if (videoCodec.equals(movie.getMediaInfoVideoCodec())) {
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.videocodec")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>();
    return comboBox;
  }

  private void buildAndInstallCodecArray() {
    String oldValue = (String) comboBox.getSelectedItem();
    comboBox.removeAllItems();

    List<String> codecs = new ArrayList<>(movieList.getVideoCodecsInMovies());
    Collections.sort(codecs);
    for (String codec : codecs) {
      comboBox.addItem(codec);
    }

    if (oldValue != null) {
      comboBox.setSelectedItem(oldValue);
    }
  }
}
