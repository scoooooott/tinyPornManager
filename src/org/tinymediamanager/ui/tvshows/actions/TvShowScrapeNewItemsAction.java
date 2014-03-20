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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowEpisodeScrapeTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowChooserDialog;

/**
 * The class TvShowScrapeNewItemsAction. Scrape all new items
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeNewItemsAction extends AbstractAction {
  private static final long           serialVersionUID = -3365542777082781952L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowScrapeNewItemsAction() {
    putValue(NAME, BUNDLE.getString("tvshow.scrape.newitems")); //$NON-NLS-1$
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(SMALL_ICON, IconManager.SEARCH);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<TvShow> newTvShows = new ArrayList<TvShow>();
    List<TvShowEpisode> newEpisodes = new ArrayList<TvShowEpisode>();

    for (TvShow tvShow : new ArrayList<TvShow>(TvShowList.getInstance().getTvShows())) {
      // if there is at least one new episode and no scraper id we assume the TV show is new
      if (tvShow.isNewlyAdded() && !tvShow.isScraped()) {
        newTvShows.add(tvShow);
        continue;
      }
      // else: check every episode if there is a new episode
      for (TvShowEpisode episode : tvShow.getEpisodes()) {
        if (episode.isNewlyAdded() && !episode.isScraped()) {
          newEpisodes.add(episode);
        }
      }
    }

    // now start the scrape tasks
    // epsiode scraping can run in background
    TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(newEpisodes);
    Globals.executor.execute(task);

    // whereas tv show scraping has to run in foreground
    for (TvShow tvShow : newTvShows) {
      TvShowChooserDialog chooser = new TvShowChooserDialog(tvShow, newTvShows.size() > 1 ? true : false);
      if (!chooser.showDialog()) {
        break;
      }
    }
  }
}
