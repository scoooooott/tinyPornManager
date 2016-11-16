/*
 * Copyright 2012 - 2016 Manuel Laggner
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
import javax.swing.JOptionPane;

import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowScrapeMissingEpisodesAction. To update the information of missing episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeMissingEpisodesAction extends AbstractAction {
  private static final long           serialVersionUID = -389165862194237592L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowScrapeMissingEpisodesAction() {
    putValue(NAME, BUNDLE.getString("tvshow.scrape.missingepisodes")); //$NON-NLS-1$
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(SMALL_ICON, IconManager.SEARCH);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();

    if (selectedTvShows.size() > 0) {
      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.loadDefaults();

      TvShowScraperMetadataConfig scraperMetadataConfig = new TvShowScraperMetadataConfig(false);
      scraperMetadataConfig.setEpisodeList(true);
      options.setScraperMetadataConfig(scraperMetadataConfig);

      TmmThreadPool scrapeTask = new TvShowScrapeTask(selectedTvShows, true, options);
      if (TmmTaskManager.getInstance().addMainTask(scrapeTask)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }
}