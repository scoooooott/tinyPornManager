/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowClearImageCacheAction. To clear the image cache for all TV shows/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowClearImageCacheAction extends AbstractAction {
  private static final long           serialVersionUID = 3452373237085274937L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowClearImageCacheAction() {
    putValue(NAME, BUNDLE.getString("tvshow.clearimagecache")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();
    List<TvShowEpisode> selectedEpisodes = new ArrayList<TvShowEpisode>();

    // add all episodes which are not part of a selected tv show
    for (Object obj : TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects()) {
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        if (!selectedTvShows.contains(episode.getTvShow())) {
          selectedEpisodes.add(episode);
        }
      }
    }

    // clear the cache
    MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    for (TvShow tvShow : selectedTvShows) {
      ImageCache.clearImageCacheForMediaEntity(tvShow);
    }

    for (TvShowEpisode episode : selectedEpisodes) {
      ImageCache.clearImageCacheForMediaEntity(episode);
    }
    MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }
}