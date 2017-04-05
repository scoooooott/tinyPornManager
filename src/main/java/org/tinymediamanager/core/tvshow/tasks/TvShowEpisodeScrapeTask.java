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
package org.tinymediamanager.core.tvshow.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class TvShowEpisodeScrapeTask.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeScrapeTask extends TmmTask {
  private static final ResourceBundle BUNDLE   = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER   = LoggerFactory.getLogger(TvShowEpisodeScrapeTask.class);

  private final List<TvShowEpisode>   episodes;
  private final MediaScraper          mediaScraper;

  private boolean                     scrapeThumb;
  private MediaLanguages              language = TvShowModuleManager.SETTINGS.getScraperLanguage();

  /**
   * Instantiates a new tv show episode scrape task.
   * 
   * @param episodes
   *          the episodes to scrape
   * @param mediaScraper
   *          the media scraper to use
   */
  public TvShowEpisodeScrapeTask(List<TvShowEpisode> episodes, MediaScraper mediaScraper) {
    super(BUNDLE.getString("tvshow.scraping"), episodes.size(), TaskType.BACKGROUND_TASK);
    this.episodes = episodes;
    this.mediaScraper = mediaScraper;
    this.scrapeThumb = true;
  }

  /**
   * Instantiates a new tv show episode scrape task.
   * 
   * @param episodes
   *          the episodes
   * @param mediaScraper
   *          the media scraper to use
   * @param scrapeThumb
   *          should we also scrape thumbs?
   */
  public TvShowEpisodeScrapeTask(List<TvShowEpisode> episodes, MediaScraper mediaScraper, boolean scrapeThumb) {
    super(BUNDLE.getString("tvshow.scraping"), episodes.size(), TaskType.BACKGROUND_TASK);
    this.episodes = episodes;
    this.mediaScraper = mediaScraper;
    this.scrapeThumb = scrapeThumb;
  }

  @Override
  public void doInBackground() {
    for (TvShowEpisode episode : episodes) {
      // only scrape if at least one ID is available
      if (episode.getTvShow().getIds().size() == 0) {
        LOGGER.info("we cannot scrape (no ID): " + episode.getTvShow().getTitle() + " - " + episode.getTitle());
        continue;
      }

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setLanguage(LocaleUtils.toLocale(language.name()));
      options.setCountry(TvShowModuleManager.SETTINGS.getCertificationCountry());

      MediaMetadata md = new MediaMetadata(mediaScraper.getMediaProvider().getProviderInfo().getId());
      md.setReleaseDate(episode.getFirstAired());
      options.setMetadata(md);

      for (Entry<String, Object> entry : episode.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      if (episode.isDvdOrder()) {
        options.setId(MediaMetadata.SEASON_NR_DVD, String.valueOf(episode.getDvdSeason()));
        options.setId(MediaMetadata.EPISODE_NR_DVD, String.valueOf(episode.getDvdEpisode()));
      }
      else {
        options.setId(MediaMetadata.SEASON_NR, String.valueOf(episode.getAiredSeason()));
        options.setId(MediaMetadata.EPISODE_NR, String.valueOf(episode.getAiredEpisode()));
      }
      if (scrapeThumb) {
        options.setArtworkType(MediaArtworkType.THUMB);
      }
      else {
        options.setArtworkType(null);
      }

      try {
        LOGGER.info("=====================================================");
        LOGGER.info("Scraper metadata with scraper: " + mediaScraper.getMediaProvider().getProviderInfo().getId() + ", "
            + mediaScraper.getMediaProvider().getProviderInfo().getVersion());
        LOGGER.info(options.toString());
        LOGGER.info("=====================================================");
        MediaMetadata metadata = ((ITvShowMetadataProvider) mediaScraper.getMediaProvider()).getMetadata(options);
        if (StringUtils.isNotBlank(metadata.getTitle())) {
          episode.setMetadata(metadata);
        }
      }
      catch (Exception e) {
        LOGGER.warn("Error getting metadata " + e.getMessage());
      }
    }

    if (TvShowModuleManager.SETTINGS.getSyncTrakt()) {
      Set<TvShow> tvShows = new HashSet<>();
      for (TvShowEpisode episode : episodes) {
        tvShows.add(episode.getTvShow());
      }
      TmmTask task = new SyncTraktTvTask(null, new ArrayList<>(tvShows));
      TmmTaskManager.getInstance().addUnnamedTask(task);
    }
  }

  public MediaLanguages getLanguage() {
    return language;
  }

  public void setLanguage(MediaLanguages language) {
    this.language = language;
  }
}
