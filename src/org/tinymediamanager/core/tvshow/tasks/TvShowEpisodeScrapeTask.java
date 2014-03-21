/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.core.tvshow.tasks;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;

/**
 * The Class TvShowEpisodeScrapeTask.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeScrapeTask implements Runnable {
  private static final Logger           LOGGER           = LoggerFactory.getLogger(TvShowEpisodeScrapeTask.class);

  private final List<TvShowEpisode>     episodes;
  private final ITvShowMetadataProvider metadataProvider = TvShowList.getInstance().getMetadataProvider();

  /**
   * Instantiates a new tv show episode scrape task.
   * 
   * @param episodes
   *          the episodes
   */
  public TvShowEpisodeScrapeTask(List<TvShowEpisode> episodes) {
    this.episodes = episodes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    for (TvShowEpisode episode : episodes) {
      // only scrape if at least one ID is available
      if (episode.getTvShow().getIds().size() == 0) {
        LOGGER.info("we cannot scrape (no ID): " + episode.getTvShow().getTitle() + " - " + episode.getTitle());
        continue;
      }

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setLanguage(Globals.settings.getMovieSettings().getScraperLanguage());
      options.setCountry(Globals.settings.getMovieSettings().getCertificationCountry());

      for (Entry<String, Object> entry : episode.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      options.setType(MediaType.TV_EPISODE);
      options.setId(MediaMetadata.SEASON_NR, String.valueOf(episode.getSeason()));
      options.setId(MediaMetadata.EPISODE_NR, String.valueOf(episode.getEpisode()));

      try {
        MediaMetadata metadata = metadataProvider.getEpisodeMetadata(options);
        if (StringUtils.isNotBlank(metadata.getStringValue(MediaMetadata.TITLE))) {
          episode.setMetadata(metadata);
        }
      }
      catch (Exception e) {
        LOGGER.warn("Error getting metadata " + e.getMessage());
      }
    }
  }

}
