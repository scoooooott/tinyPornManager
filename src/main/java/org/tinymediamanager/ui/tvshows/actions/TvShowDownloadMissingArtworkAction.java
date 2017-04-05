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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.tasks.TvShowMissingArtworkDownloadTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * the class TvShowDownloadMissingArtworkAction is used to search/download missing artwork
 */
public class TvShowDownloadMissingArtworkAction extends AbstractAction {
  private static final long           serialVersionUID = 6102632119900792735L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowDownloadMissingArtworkAction() {
    putValue(NAME, BUNDLE.getString("tvshow.downloadmissingartwork")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.IMAGE);
    putValue(LARGE_ICON_KEY, IconManager.IMAGE);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();
    Set<TvShowEpisode> selectedEpisodes = new HashSet<>();

    // add all episodes which are not part of a selected tv show
    for (Object obj : TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects()) {
      if (obj instanceof TvShow) {
        TvShow show = (TvShow) obj;
        for (TvShowEpisode episode : show.getEpisodes()) {
          selectedEpisodes.add(episode);
        }
      }
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        selectedEpisodes.add(episode);
      }
      if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        for (TvShowEpisode episode : season.getEpisodes()) {
          selectedEpisodes.add(episode);
        }
      }
    }

    if (!selectedTvShows.isEmpty() || !selectedEpisodes.isEmpty()) {
      TvShowMissingArtworkDownloadTask task = new TvShowMissingArtworkDownloadTask(selectedTvShows, new ArrayList<>(selectedEpisodes));
      TmmTaskManager.getInstance().addDownloadTask(task);
    }
  }
}
