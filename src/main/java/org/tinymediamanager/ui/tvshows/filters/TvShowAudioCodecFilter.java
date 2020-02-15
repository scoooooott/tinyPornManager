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

import static org.tinymediamanager.core.MediaFileType.AUDIO;
import static org.tinymediamanager.core.MediaFileType.VIDEO;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * This class implements a audio codec filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowAudioCodecFilter extends AbstractCheckComboBoxTvShowUIFilter<String> {
  private TvShowList tvShowList = TvShowList.getInstance();

  public TvShowAudioCodecFilter() {
    super();
    buildAndInstallCodecArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallCodecArray();
    tvShowList.addPropertyChangeListener(Constants.AUDIO_CODEC, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "tvShowAudioCodec";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<String> codecs = checkComboBox.getSelectedItems();

    // search codec in the episodes
    for (TvShowEpisode episode : episodes) {
      List<MediaFile> mfs = episode.getMediaFiles(VIDEO, AUDIO);
      for (MediaFile mf : mfs) {
        for (String audioCodec : mf.getAudioCodecList()) {
          if (invert ^ codecs.contains(audioCodec)) {
            return true;
          }
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
    List<String> codecs = new ArrayList<>(tvShowList.getAudioCodecsInEpisodes());
    Collections.sort(codecs);

    setValues(codecs);
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
