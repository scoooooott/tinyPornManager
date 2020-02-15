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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a audio codec movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieAudioCodecFilter extends AbstractCheckComboBoxMovieUIFilter<String> {
  private MovieList movieList = MovieList.getInstance();

  public MovieAudioCodecFilter() {
    super();
    buildAndInstallCodecArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallCodecArray();
    movieList.addPropertyChangeListener(Constants.AUDIO_CODEC, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "movieAudioCodec";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> audioCodecs = checkComboBox.getSelectedItems();

    // check all audio codecs of all VIDEO and AUDIO files
    for (MediaFile mf : movie.getMediaFiles(VIDEO, AUDIO)) {
      for (String audioCodec : mf.getAudioCodecList()) {
        if (audioCodecs.contains(audioCodec)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.audiocodec"));
  }

  private void buildAndInstallCodecArray() {
    List<String> audioCodecs = new ArrayList<>(movieList.getAudioCodecsInMovies());
    Collections.sort(audioCodecs);

    setValues(audioCodecs);
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
