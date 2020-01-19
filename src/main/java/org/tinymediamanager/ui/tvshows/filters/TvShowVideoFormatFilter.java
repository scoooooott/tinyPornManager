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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.MediaFileHelper;
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
    setValues(MediaFileHelper.getVideoFormats());
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
        MediaFile mf = episode.getMainVideoFile();
        if (mf == null) {
          return false;
        }

        if (MediaFileHelper.VIDEO_FORMAT_UHD.equals(videoFormat) || MediaFileHelper.VIDEO_FORMAT_HD.equals(videoFormat)
            || MediaFileHelper.VIDEO_FORMAT_SD.equals(videoFormat) || MediaFileHelper.VIDEO_FORMAT_LD.equals(videoFormat)) {
          if (invert ^ (MediaFileHelper.VIDEO_FORMAT_UHD.equals(videoFormat) && mf.isVideoDefinitionUHD())) {
            return true;
          }
          if (invert ^ (MediaFileHelper.VIDEO_FORMAT_HD.equals(videoFormat) && mf.isVideoDefinitionHD())) {
            return true;
          }
          if (invert ^ (MediaFileHelper.VIDEO_FORMAT_SD.equals(videoFormat) && mf.isVideoDefinitionSD())) {
            return true;
          }
          if (invert ^ (MediaFileHelper.VIDEO_FORMAT_LD.equals(videoFormat) && mf.isVideoDefinitionLD())) {
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

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }
}
