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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowEpisodeScrapeTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowScrapeEpisodesAction. To Scrape episode data
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeEpisodesAction extends AbstractAction {
  private static final long           serialVersionUID = -75916665265142730L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private boolean                     withArtwork;

  public TvShowScrapeEpisodesAction(boolean withArtwork) {
    this.withArtwork = withArtwork;
    if (this.withArtwork) {
      putValue(NAME, BUNDLE.getString("tvshowepisode.scrape")); //$NON-NLS-1$
    }
    else {
      putValue(NAME, BUNDLE.getString("tvshowepisode.scrape.withoutartwork")); //$NON-NLS-1$
    }
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(SMALL_ICON, IconManager.SEARCH);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    List<TvShowEpisode> episodes = TvShowUIModule.getInstance().getSelectionModel().getSelectedEpisodes();

    TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(episodes, withArtwork);
    TmmTaskManager.getInstance().addUnnamedTask(task);
  }
}
