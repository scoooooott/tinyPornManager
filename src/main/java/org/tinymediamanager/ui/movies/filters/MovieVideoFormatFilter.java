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

import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a video format movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieVideoFormatFilter extends AbstractCheckComboBoxMovieUIFilter<String> {

  public MovieVideoFormatFilter() {
    super();
    setValues(MediaFileHelper.getVideoFormats());
  }

  @Override
  public String getId() {
    return "movieVideoFormat";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> selectedValues = checkComboBox.getSelectedItems();

    MediaFile mf = movie.getMainVideoFile();
    if (mf == null) {
      return false;
    }

    for (String videoFormat : selectedValues) {
      if (MediaFileHelper.VIDEO_FORMAT_UHD.equals(videoFormat) && mf.isVideoDefinitionUHD()) {
        return true;
      }
      else if (MediaFileHelper.VIDEO_FORMAT_HD.equals(videoFormat) && mf.isVideoDefinitionHD()) {
        return true;
      }
      else if (MediaFileHelper.VIDEO_FORMAT_SD.equals(videoFormat) && mf.isVideoDefinitionSD()) {
        return true;
      }
      else if (MediaFileHelper.VIDEO_FORMAT_LD.equals(videoFormat) && mf.isVideoDefinitionLD()) {
        return true;
      }
      else if (videoFormat.equals(movie.getMediaInfoVideoFormat())) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.resolution"));
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }
}
