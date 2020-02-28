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

import static org.tinymediamanager.core.MediaFileType.AUDIO;
import static org.tinymediamanager.core.MediaFileType.VIDEO;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a audio channel movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieAudioChannelFilter extends AbstractCheckComboBoxMovieUIFilter<String> {
  private static final String CHANNEL_1 = "1 (Mono)";
  private static final String CHANNEL_2 = "2 (Stereo)";
  private static final String CHANNEL_3 = "3 (2.1)";
  private static final String CHANNEL_6 = "6 (5.1)";
  private static final String CHANNEL_8 = "8 (7.1)";

  public MovieAudioChannelFilter() {
    super();
    setValues(CHANNEL_1, CHANNEL_2, CHANNEL_3, CHANNEL_6, CHANNEL_8);
  }

  @Override
  public String getId() {
    return "movieAudioChannel";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> selectedValues = checkComboBox.getSelectedItems();
    List<String> audioChannels = prepareSelectesAudioChannels(selectedValues);

    // check all audio channels of all VIDEO and AUDIO files
    List<MediaFile> mediaFiles = movie.getMediaFiles(VIDEO, AUDIO);
    for (MediaFile mf : mediaFiles) {
      for (String channels : mf.getAudioChannelsList()) {
        if (audioChannels.contains(channels)) {
          return true;
        }
      }
    }

    return false;
  }

  private List<String> prepareSelectesAudioChannels(List<String> selectedValues) {
    List<String> selectedAudioChannels = new ArrayList<>();

    for (String channel : selectedValues) {
      switch (channel) {
        case CHANNEL_1:
          selectedAudioChannels.add("1ch");
          break;

        case CHANNEL_2:
          selectedAudioChannels.add("2ch");
          break;

        case CHANNEL_3:
          selectedAudioChannels.add("3ch");
          break;

        case CHANNEL_6:
          selectedAudioChannels.add("6ch");
          break;

        case CHANNEL_8:
          selectedAudioChannels.add("8ch");
          break;

        default:
          break;
      }

    }

    return selectedAudioChannels;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.channels"));
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
