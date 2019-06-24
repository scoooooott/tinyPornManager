/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import javax.swing.JLabel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * This class implements a video format filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowVideoFormatFilter extends AbstractCheckComboBoxTvShowUIFilter<String> {

  public TvShowVideoFormatFilter() {
    super();
    setValues(MediaFile.getVideoFormats());
  }

  @Override
  public String getId() {
    return "tvShowVideoFormat";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<String> selectedValues = checkComboBox.getSelectedItems();

    for (String videoFormat : selectedValues) {

      for (TvShowEpisode episode : episodes) {
        if (MediaFile.VIDEO_FORMAT_HD.equals(videoFormat) || MediaFile.VIDEO_FORMAT_SD.equals(videoFormat)) {
          if (invert ^ (MediaFile.VIDEO_FORMAT_HD.equals(videoFormat) && isVideoHD(episode.getMediaInfoVideoFormat()))) {
            return true;
          }
          if (invert ^ (MediaFile.VIDEO_FORMAT_SD.equals(videoFormat) && !isVideoHD(episode.getMediaInfoVideoFormat()))) {
            return true;
          }
        }
        else {
          if (invert ^ videoFormat.equals(episode.getMediaInfoVideoFormat())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
  }

  private String[] getVideoFormats() {
    return new String[] { MediaFile.VIDEO_FORMAT_480P, MediaFile.VIDEO_FORMAT_540P, MediaFile.VIDEO_FORMAT_576P, MediaFile.VIDEO_FORMAT_720P,
        MediaFile.VIDEO_FORMAT_1080P, MediaFile.VIDEO_FORMAT_2160P, MediaFile.VIDEO_FORMAT_4320P, MediaFile.VIDEO_FORMAT_SD,
        MediaFile.VIDEO_FORMAT_HD };
  }

  private boolean isVideoHD(String videoFormat) {
    if (videoFormat.equals(MediaFile.VIDEO_FORMAT_720P)) {
      return true;
    }
    if (videoFormat.equals(MediaFile.VIDEO_FORMAT_1080P)) {
      return true;
    }
    if (videoFormat.equals(MediaFile.VIDEO_FORMAT_2160P)) {
      return true;
    }
    if (videoFormat.equals(MediaFile.VIDEO_FORMAT_4320P)) {
      return true;
    }
    return false;
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
