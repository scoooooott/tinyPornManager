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

package org.tinymediamanager.core.tvshow.tasks;

import static org.tinymediamanager.scraper.entities.MediaType.TV_SHOW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowArtworkHelper;
import org.tinymediamanager.core.tvshow.TvShowEpisodeScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowArtworkProvider;

/**
 * the class TvShowMissingArtworkDownloadTask is used to download missing artwork for TV shows
 */
public class TvShowMissingArtworkDownloadTask extends TmmThreadPool {
  private static final Logger                            LOGGER = LoggerFactory.getLogger(TvShowMissingArtworkDownloadTask.class);
  private static final ResourceBundle                    BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private final List<TvShow>                             tvShows;
  private final List<TvShowEpisode>                      episodes;
  private final TvShowSearchAndScrapeOptions             scrapeOptions;
  private final List<TvShowScraperMetadataConfig>        tvShowScraperMetadataConfig;
  private final List<TvShowEpisodeScraperMetadataConfig> episodeScraperMetadataConfig;

  public TvShowMissingArtworkDownloadTask(List<TvShow> tvShows, List<TvShowEpisode> episodes, TvShowSearchAndScrapeOptions scrapeOptions,
      List<TvShowScraperMetadataConfig> tvShowScraperMetadataConfig, List<TvShowEpisodeScraperMetadataConfig> episodeScraperMetadataConfig) {
    super(BUNDLE.getString("task.missingartwork"));
    this.tvShows = new ArrayList<>(tvShows);
    this.episodes = new ArrayList<>(episodes);
    this.scrapeOptions = scrapeOptions;
    this.tvShowScraperMetadataConfig = tvShowScraperMetadataConfig;
    this.episodeScraperMetadataConfig = episodeScraperMetadataConfig;

    // add the episodes from the shows
    for (TvShow show : this.tvShows) {
      for (TvShowEpisode episode : new ArrayList<>(show.getEpisodes())) {
        if (!this.episodes.contains(episode)) {
          this.episodes.add(episode);
        }
      }
    }
  }

  @Override
  protected void doInBackground() {
    LOGGER.info("Getting missing artwork");

    initThreadPool(3, "scrapeMissingMovieArtwork");
    start();

    for (TvShow show : tvShows) {
      if (cancel) {
        break;
      }
      if (TvShowArtworkHelper.hasMissingArtwork(show, tvShowScraperMetadataConfig)) {
        submitTask(new TvShowWorker(show, scrapeOptions));
      }
    }

    for (TvShowEpisode episode : episodes) {
      if (cancel) {
        break;
      }
      if (TvShowArtworkHelper.hasMissingArtwork(episode, episodeScraperMetadataConfig)) {
        submitTask(new TvShowEpisodeWorker(episode, scrapeOptions));
      }
    }

    waitForCompletionOrCancel();
    LOGGER.info("Done getting missing artwork");
  }

  @Override
  public void callback(Object obj) {
    publishState((String) obj, progressDone);
  }

  /****************************************************************************************
   * Helper classes
   ****************************************************************************************/
  private static class TvShowWorker implements Runnable {
    private TvShowList                  tvShowList = TvShowList.getInstance();
    private TvShow                      tvShow;
    private MediaSearchAndScrapeOptions scrapeOptions;

    private TvShowWorker(TvShow tvShow, MediaSearchAndScrapeOptions scrapeOptions) {
      this.tvShow = tvShow;
      this.scrapeOptions = scrapeOptions;
    }

    @Override
    public void run() {
      try {
        // set up scrapers
        List<MediaArtwork> artwork = new ArrayList<>();

        ArtworkSearchAndScrapeOptions options = new ArtworkSearchAndScrapeOptions(TV_SHOW);
        options.setDataFromOtherOptions(scrapeOptions);
        options.setArtworkType(MediaArtwork.MediaArtworkType.ALL);
        options.setLanguage(TvShowModuleManager.SETTINGS.getScraperLanguage());
        for (Map.Entry<String, Object> entry : tvShow.getIds().entrySet()) {
          options.setId(entry.getKey(), entry.getValue().toString());
        }

        // scrape providers till one artwork has been found
        for (MediaScraper artworkScraper : scrapeOptions.getArtworkScrapers()) {
          ITvShowArtworkProvider artworkProvider = (ITvShowArtworkProvider) artworkScraper.getMediaProvider();
          try {
            artwork.addAll(artworkProvider.getArtwork(options));
          }
          catch (ScrapeException e) {
            LOGGER.error("getArtwork", e);
            MessageManager.instance.pushMessage(
                new Message(Message.MessageLevel.ERROR, tvShow, "message.scrape.tvshowartworkfailed", new String[] { ":", e.getLocalizedMessage() }));
          }
          catch (MissingIdException ignored) {
            LOGGER.debug("no id found for scraper {}", artworkProvider.getProviderInfo());
          }
        }

        // now set & download the artwork
        if (!artwork.isEmpty()) {
          TvShowArtworkHelper.downloadMissingArtwork(tvShow, artwork);
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "TvShowMissingArtwork", "message.scrape.threadcrashed",
            new String[] { ":", e.getLocalizedMessage() }));
      }
    }
  }

  private static class TvShowEpisodeWorker implements Runnable {
    private TvShowList                  tvShowList = TvShowList.getInstance();
    private TvShowEpisode               episode;
    private MediaSearchAndScrapeOptions scrapeOptions;

    private TvShowEpisodeWorker(TvShowEpisode episode, MediaSearchAndScrapeOptions scrapeOptions) {
      this.episode = episode;
      this.scrapeOptions = scrapeOptions;
    }

    @Override
    public void run() {
      try {
        List<MediaArtwork> artwork = new ArrayList<>();

        // set up scrapers
        ArtworkSearchAndScrapeOptions options = new ArtworkSearchAndScrapeOptions(MediaType.TV_EPISODE);
        options.setDataFromOtherOptions(scrapeOptions);
        options.setArtworkType(MediaArtwork.MediaArtworkType.ALL);
        options.setLanguage(TvShowModuleManager.SETTINGS.getScraperLanguage());

        for (Map.Entry<String, Object> entry : episode.getTvShow().getIds().entrySet()) {
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
        options.setArtworkType(MediaArtwork.MediaArtworkType.THUMB);

        // scrape providers till one artwork has been found
        for (MediaScraper artworkScraper : scrapeOptions.getArtworkScrapers()) {
          ITvShowArtworkProvider artworkProvider = (ITvShowArtworkProvider) artworkScraper.getMediaProvider();
          try {
            artwork.addAll(artworkProvider.getArtwork(options));
            if (!artwork.isEmpty()) {
              break;
            }
          }
          catch (ScrapeException e) {
            LOGGER.error("getArtwork", e);
            MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, episode, "message.scrape.tvshowartworkfailed",
                new String[] { ":", e.getLocalizedMessage() }));
          }
          catch (MissingIdException ignored) {
            LOGGER.debug("no id found for scraper {}", artworkProvider.getProviderInfo());
          }
        }

        if (!artwork.isEmpty()) {
          episode.setArtworkUrl(artwork.get(0).getDefaultUrl(), MediaFileType.THUMB);
          episode.downloadArtwork(MediaFileType.THUMB);
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "TvShowMissingArtwork", "message.scrape.threadcrashed",
            new String[] { ":", e.getLocalizedMessage() }));
      }
    }
  }
}
