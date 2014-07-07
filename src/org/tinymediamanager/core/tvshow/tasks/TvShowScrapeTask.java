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
package org.tinymediamanager.core.tvshow.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class TvShowScrapeTask. This starts scraping of TV shows
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeTask extends TmmThreadPool {
  private final static Logger          LOGGER = LoggerFactory.getLogger(TvShowScrapeTask.class);
  private static final ResourceBundle  BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<TvShow>                 tvShowsToScrape;
  private boolean                      doSearch;
  private TvShowSearchAndScrapeOptions options;

  /**
   * Instantiates a new tv show scrape task.
   * 
   * @param tvShowsToScrape
   *          the tv shows to scrape
   * @param doSearch
   *          the do search
   * @param options
   *          the options
   */
  public TvShowScrapeTask(List<TvShow> tvShowsToScrape, boolean doSearch, TvShowSearchAndScrapeOptions options) {
    super(BUNDLE.getString("tvshow.scraping"));
    this.tvShowsToScrape = tvShowsToScrape;
    this.doSearch = doSearch;
    this.options = options;
  }

  @Override
  protected void doInBackground() {
    LOGGER.debug("start scraping tv shows...");
    start();

    initThreadPool(3, "scrape");
    for (TvShow tvShow : tvShowsToScrape) {
      submitTask(new Worker(tvShow));
    }

    waitForCompletionOrCancel();

    if (Globals.settings.getTvShowSettings().getSyncTrakt()) {
      TmmTask task = new SyncTraktTvTask(null, tvShowsToScrape);
      TmmTaskManager.getInstance().addUnnamedTask(task);
    }

    LOGGER.debug("done scraping tv shows...");
  }

  private class Worker implements Runnable {
    private TvShowList tvShowList = TvShowList.getInstance();
    private TvShow     tvShow;

    public Worker(TvShow tvShow) {
      this.tvShow = tvShow;
    }

    @Override
    public void run() {
      try {
        // set up scrapers
        TvShowScraperMetadataConfig scraperMetadataConfig = options.getScraperMetadataConfig();
        ITvShowMetadataProvider mediaMetadataProvider = tvShowList.getMetadataProvider(options.getMetadataScraper());
        List<IMediaArtworkProvider> artworkProviders = tvShowList.getArtworkProviders(options.getArtworkScrapers());
        // List<IMediaTrailerProvider> trailerProviders = tvShowList.getTrailerProviders(options.getTrailerScrapers());

        // scrape tv show

        // search for tv show
        MediaSearchResult result1 = null;
        if (doSearch) {
          List<MediaSearchResult> results = tvShowList.searchTvShow(tvShow.getTitle(), mediaMetadataProvider);
          if (results != null && !results.isEmpty()) {
            result1 = results.get(0);
            // check if there is an other result with 100% score
            if (results.size() > 1) {
              MediaSearchResult result2 = results.get(1);
              // if both results have 100% score - do not take any result
              if (result1.getScore() == 1 && result2.getScore() == 1) {
                LOGGER.info("two 100% results, can't decide whitch to take - ignore result");
                MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "tvshow.scrape.nomatchfound"));
                return;
              }
              // create a treshold of 0.75 - to minimize false positives
              if (result1.getScore() < 0.75) {
                LOGGER.info("score is lower than 0.75 (" + result1.getScore() + ") - ignore result");
                MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "tvshow.scrape.nomatchfound"));
                return;
              }
            }
          }
          else {
            LOGGER.info("no result found for " + tvShow.getTitle());
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "tvshow.scrape.nomatchfound"));
          }
        }

        // get metadata and artwork
        if ((doSearch && result1 != null) || !doSearch) {
          try {
            MediaScrapeOptions options = new MediaScrapeOptions();
            options.setType(MediaType.TV_SHOW);
            options.setResult(result1);
            options.setLanguage(Globals.settings.getTvShowSettings().getScraperLanguage());
            options.setCountry(Globals.settings.getTvShowSettings().getCertificationCountry());

            // we didn't do a search - pass imdbid and tmdbid from movie
            // object
            if (!doSearch) {
              for (Entry<String, Object> entry : tvShow.getIds().entrySet()) {
                options.setId(entry.getKey(), entry.getValue().toString());
              }
            }

            // override scraper with one from search result
            mediaMetadataProvider = tvShowList.getMetadataProvider(result1.getProviderId());
            // scrape metadata if wanted
            MediaMetadata md = null;

            if (scraperMetadataConfig.isCast() || scraperMetadataConfig.isCertification() || scraperMetadataConfig.isGenres()
                || scraperMetadataConfig.isAired() || scraperMetadataConfig.isPlot() || scraperMetadataConfig.isRating()
                || scraperMetadataConfig.isRuntime() || scraperMetadataConfig.isStatus() || scraperMetadataConfig.isTitle()
                || scraperMetadataConfig.isYear()) {
              md = mediaMetadataProvider.getTvShowMetadata(options);
              tvShow.setMetadata(md, scraperMetadataConfig);
            }

            // scrape episodes
            if (scraperMetadataConfig.isEpisodes()) {
              tvShow.scrapeAllEpisodes();
            }

            // scrape artwork if wanted
            if (scraperMetadataConfig.isArtwork()) {
              tvShow.setArtwork(getArtwork(tvShow, md, artworkProviders), scraperMetadataConfig);
            }

            // // scrape trailer if wanted
            // if (scraperMetadataConfig.isTrailer()) {
            // tvShow.setTrailers(getTrailers(tvShow, md, trailerProviders));
            // }
            tvShow.writeNFO();
          }
          catch (Exception e) {
            LOGGER.error("tvShow.setMetadata", e);
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "message.scrape.metadatatvshowfailed"));
          }
        }
      }

      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "TvShowScraper", "message.scrape.threadcrashed"));
      }
    }

    /**
     * Gets the artwork.
     * 
     * @param metadata
     *          the metadata
     * @return the artwork
     */
    public List<MediaArtwork> getArtwork(TvShow tvShow, MediaMetadata metadata, List<IMediaArtworkProvider> artworkProviders) {
      List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setType(MediaType.TV_SHOW);
      options.setArtworkType(MediaArtworkType.ALL);
      options.setMetadata(metadata);
      options.setLanguage(Globals.settings.getTvShowSettings().getScraperLanguage());
      options.setCountry(Globals.settings.getTvShowSettings().getCertificationCountry());
      for (Entry<String, Object> entry : tvShow.getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      // scrape providers till one artwork has been found
      for (IMediaArtworkProvider artworkProvider : artworkProviders) {
        try {
          artwork.addAll(artworkProvider.getArtwork(options));
        }
        catch (Exception e) {
          LOGGER.error("getArtwork", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "message.scrape.tvshowartworkfailed"));
        }
      }
      return artwork;
    }
  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the text is never right
    publishState(progressDone);
  }
}
