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

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a video format movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieVideoFormatFilter extends AbstractMovieUIFilter {
  private JComboBox<String> comboBox;

  @Override
  public String getId() {
    return "movieVideoFormat";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return (String) comboBox.getSelectedItem();
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    comboBox.setSelectedItem(value);
  }

  @Override
  public boolean accept(Movie movie) {
    String videoFormat = (String) comboBox.getSelectedItem();
    MediaFile mf = movie.getFirstVideoFile();
    if (mf == null) {
      return false;
    }

    if (videoFormat == MediaFile.VIDEO_FORMAT_HD && mf.isVideoDefinitionHD()) {
      return true;
    }
    else if (videoFormat == MediaFile.VIDEO_FORMAT_SD && mf.isVideoDefinitionSD()) {
      return true;
    }
    else if (videoFormat == MediaFile.VIDEO_FORMAT_LD && mf.isVideoDefinitionLD()) {
      return true;
    }
    else if (videoFormat.equals(movie.getMediaInfoVideoFormat())) {
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>(getVideoFormats());
    return comboBox;
  }

  private String[] getVideoFormats() {
    List<String> videoFormats = MediaFile.getVideoFormats();
    return videoFormats.toArray(new String[videoFormats.size()]);
  }
}
