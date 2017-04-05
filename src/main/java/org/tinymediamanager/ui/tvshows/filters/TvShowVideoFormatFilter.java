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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a video format filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowVideoFormatFilter extends AbstractTvShowUIFilter {
  private JComboBox<String> comboBox;

  @Override
  public String getId() {
    return "tvShowVideoFormat";
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
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    String videoFormat = (String) comboBox.getSelectedItem();

    if (StringUtils.isBlank(videoFormat)) {
      return true;
    }

    for (TvShowEpisode episode : episodes) {
      if (videoFormat == MediaFile.VIDEO_FORMAT_HD || videoFormat == MediaFile.VIDEO_FORMAT_SD) {
        if (videoFormat == MediaFile.VIDEO_FORMAT_HD && isVideoHD(episode.getMediaInfoVideoFormat())) {
          return true;
        }
        if (videoFormat == MediaFile.VIDEO_FORMAT_SD && !isVideoHD(episode.getMediaInfoVideoFormat())) {
          return true;
        }
      }
      else {
        if (videoFormat == episode.getMediaInfoVideoFormat()) {
          return true;
        }
      }
    }
    return false;
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

  private boolean isVideoHD(String videoFormat) {
    if (videoFormat == MediaFile.VIDEO_FORMAT_720P) {
      return true;
    }
    if (videoFormat == MediaFile.VIDEO_FORMAT_1080P) {
      return true;
    }
    if (videoFormat == MediaFile.VIDEO_FORMAT_4K) {
      return true;
    }
    if (videoFormat == MediaFile.VIDEO_FORMAT_8K) {
      return true;
    }
    return false;
  }
}
