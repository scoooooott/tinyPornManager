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
package org.tinymediamanager.ui.tvshows.filters;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a video codec filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowVideoCodecFilter extends AbstractTvShowUIFilter {
  private TvShowList        tvShowList = TvShowList.getInstance();

  private JComboBox<String> comboBox;

  public TvShowVideoCodecFilter() {
    super();
    buildAndInstallCodecArray();
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        buildAndInstallCodecArray();
      }
    };
    tvShowList.addPropertyChangeListener(Constants.VIDEO_CODEC, propertyChangeListener);
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    String codec = (String) comboBox.getSelectedItem();
    if (StringUtils.isBlank(codec)) {
      return true;
    }

    // search codec in the episodes
    for (TvShowEpisode episode : episodes) {
      List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
      for (MediaFile mf : mfs) {
        if (mf.getVideoCodec().equalsIgnoreCase(codec)) {
          return true;
        }
      }
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
    comboBox.removeAllItems();
    List<String> codecs = new ArrayList<>(tvShowList.getVideoCodecsInEpisodes());
    Collections.sort(codecs);
    for (String codec : codecs) {
      comboBox.addItem(codec);
    }
  }
}
