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
 * This class implements a audio codec filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowAudioCodecFilter extends AbstractTvShowUIFilter {
  private TvShowList        tvShowList = TvShowList.getInstance();

  private JComboBox<String> comboBox;

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
    String codec = (String) comboBox.getSelectedItem();
    if (StringUtils.isBlank(codec)) {
      return true;
    }

    // search codec in the episodes
    for (TvShowEpisode episode : episodes) {
      List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
      for (MediaFile mf : mfs) {
        if (mf.getAudioCodec().equalsIgnoreCase(codec)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.audiocodec")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>();
    return comboBox;
  }

  private void buildAndInstallCodecArray() {
    // remove the listener to not firing unnecessary events
    comboBox.removeActionListener(actionListener);

    String oldValue = (String) comboBox.getSelectedItem();
    comboBox.removeAllItems();

    List<String> codecs = new ArrayList<>(tvShowList.getAudioCodecsInEpisodes());
    Collections.sort(codecs);
    for (String codec : codecs) {
      comboBox.addItem(codec);
    }

    if (oldValue != null) {
      comboBox.setSelectedItem(oldValue);
    }

    // re-add the itemlistener
    comboBox.addActionListener(actionListener);
  }
}
