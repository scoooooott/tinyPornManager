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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a video format movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieVideoFormatFilter extends AbstractMovieUIFilter {
  private JComboBox<String> comboBox;

  @Override
  public boolean accept(Movie movie) {
    String videoFormat = (String) comboBox.getSelectedItem();
    if (videoFormat == MediaFile.VIDEO_FORMAT_HD || videoFormat == MediaFile.VIDEO_FORMAT_SD) {
      if (videoFormat == MediaFile.VIDEO_FORMAT_HD && isVideoHD(movie.getMediaInfoVideoFormat())) {
        return true;
      }
      if (videoFormat == MediaFile.VIDEO_FORMAT_SD && !isVideoHD(movie.getMediaInfoVideoFormat())) {
        return true;
      }
    }
    else {
      if (videoFormat == movie.getMediaInfoVideoFormat()) {
        return false;
      }
    }

    return true;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>(getVideoFormats());
    return comboBox;
  }

  private String[] getVideoFormats() {
    return new String[] { MediaFile.VIDEO_FORMAT_480P, MediaFile.VIDEO_FORMAT_540P, MediaFile.VIDEO_FORMAT_576P, MediaFile.VIDEO_FORMAT_720P,
        MediaFile.VIDEO_FORMAT_1080P, MediaFile.VIDEO_FORMAT_4K, MediaFile.VIDEO_FORMAT_SD, MediaFile.VIDEO_FORMAT_HD }; // MediaFile.VIDEO_FORMAT_8K,
  }

  private boolean isVideoHD(final String videoFormat) {
    if (MediaFile.VIDEO_FORMAT_720P.equals(videoFormat)) {
      return true;
    }
    if (MediaFile.VIDEO_FORMAT_1080P.equals(videoFormat)) {
      return true;
    }
    if (MediaFile.VIDEO_FORMAT_4K.equals(videoFormat)) {
      return true;
    }
    if (MediaFile.VIDEO_FORMAT_8K.equals(videoFormat)) {
      return true;
    }
    return false;
  }
}
