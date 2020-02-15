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
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * This class implements a media source filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowMediaSourceFilter extends AbstractCheckComboBoxTvShowUIFilter<MediaSource> {

  public TvShowMediaSourceFilter() {
    super();
    buildAndInstallMediaSources();
    MediaSource.addListener(evt -> SwingUtilities.invokeLater(this::buildAndInstallMediaSources));
  }

  @Override
  public String getId() {
    return "tvShowMediaSource";
  }

  @Override
  public boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<MediaSource> selectedItems = checkComboBox.getSelectedItems();

    // search for media source in episodes
    for (TvShowEpisode episode : episodes) {
      if (invert ^ selectedItems.contains(episode.getMediaSource())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.source"));
  }

  private void buildAndInstallMediaSources() {
    setValues(MediaSource.values());
  }

  @Override
  protected String parseTypeToString(MediaSource type) throws Exception {
    return type.name();
  }

  @Override
  protected MediaSource parseStringToType(String string) throws Exception {
    return MediaSource.getMediaSource(string);
  }
}
