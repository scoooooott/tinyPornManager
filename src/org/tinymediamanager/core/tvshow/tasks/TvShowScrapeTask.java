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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
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
import org.tinymediamanager.ui.TmmSwingWorker;

/**
 * The Class TvShowScrapeTask.
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeTask extends TmmSwingWorker {
  private final static Logger          LOGGER = LoggerFactory.getLogger(TvShowScrapeTask.class);

  private List<TvShow>                 tvShowsToScrape;
  private boolean                      doSearch;
  private TvShowSearchAndScrapeOptions options;
  private int                          tvShowCount;

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
    this.tvShowsToScrape = tvShowsToScrape;
    this.doSearch = doSearch;
    this.options = options;
    this.tvShowCount = tvShowsToScrape.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Void doInBackground() throws Exception {
    startProgressBar("scraping tv shows", 0);

    ExecutorService executor = Executors.newFixedThreadPool(3);

    // start 3 threads
    executor.execute(new Worker(this));
    // start second thread, if there are more than one movies to scrape
    if (tvShowCount > 1) {
      executor.execute(new Worker(this));
    }
    // start third thread, if there are more than two movies to scrape
    if (tvShowCount > 2) {
      executor.execute(new Worker(this));
    }

    executor.shutdown();

    // wait till scraping is finished
    while (true) {
      if (executor.isTerminated()) {
        break;
      }
      Thread.sleep(1000);
    }

    return null;
  }

  /**
   * Gets the next tv show.
   * 
   * @return the next tv show
   */
  private synchronized TvShow getNextTvShow() {
    // get next movie to scrape
    if (tvShowsToScrape.size() > 0) {
      TvShow tvShow = tvShowsToScrape.get(0);
      tvShowsToScrape.remove(tvShow);
      startProgressBar("scraping tv shows", 100 * (tvShowCount - tvShowsToScrape.size()) / tvShowCount);
      return tvShow;
    }

    return null;
  }

  /**
   * Cancel.
   */
  public void cancel() {
    cancel(false);
    tvShowsToScrape.clear();
  }

  /*
   * Executed in event dispatching thread
   */
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  public void done() {
    stopProgressBar();
  }

  private class Worker implements Runnable {
    private TvShowList       tvShowList;
    private TvShowScrapeTask scrapeTask;

    public Worker(TvShowScrapeTask scrapeTask) {
      this.scrapeTask = scrapeTask;
    }

    @Override
    public void run() {
      try {
        tvShowList = TvShowList.getInstance();
        // set up scrapers
        TvShowScraperMetadataConfig scraperMetadataConfig = options.getScraperMetadataConfig();
        ITvShowMetadataProvider mediaMetadataProvider = tvShowList.getMetadataProvider(options.getMetadataScraper());
        List<IMediaArtworkProvider> artworkProviders = tvShowList.getArtworkProviders(options.getArtworkScrapers());
        // List<IMediaTrailerProvider> trailerProviders = tvShowList.getTrailerProviders(options.getTrailerScrapers());

        // do work
        while (true) {
          TvShow tvShow = scrapeTask.getNextTvShow();
          if (tvShow == null) {
            break;
          }

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
                  continue;
                }
                // create a treshold of 0.75 - to minimize false positives
                if (result1.getScore() < 0.75) {
                  LOGGER.info("score is lower than 0.75 (" + result1.getScore() + ") - ignore result");
                  MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "tvshow.scrape.nomatchfound"));
                  continue;
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
              options.setLanguage(Globals.settings.getMovieSettings().getScraperLanguage());
              options.setCountry(Globals.settings.getMovieSettings().getCertificationCountry());

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
      List<MediaArtwork> artwork = null;

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
          artwork = artworkProvider.getArtwork(options);
        }
        catch (Exception e) {
          LOGGER.error("getArtwork", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "message.scrape.tvshowartworkfailed"));
          artwork = new ArrayList<MediaArtwork>();
        }
        // check if at least one artwork has been found
        if (artwork.size() > 0) {
          break;
        }
      }

      // initialize if null
      if (artwork == null) {
        artwork = new ArrayList<MediaArtwork>();
      }

      return artwork;
    }

    // /**
    // * Gets the trailers.
    // *
    // * @param metadata
    // * the metadata
    // * @return the trailers
    // */
    // private List<MediaTrailer> getTrailers(Movie movie, MediaMetadata metadata, List<IMediaTrailerProvider> trailerProviders) {
    // List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();
    //
    // MediaScrapeOptions options = new MediaScrapeOptions();
    // options.setMetadata(metadata);
    // options.setImdbId(movie.getImdbId());
    // options.setTmdbId(movie.getTmdbId());
    //
    // // scrape trailers
    // for (IMediaTrailerProvider trailerProvider : trailerProviders) {
    // try {
    // List<MediaTrailer> foundTrailers = trailerProvider.getTrailers(options);
    // trailers.addAll(foundTrailers);
    // }
    // catch (Exception e) {
    // LOGGER.error("getTrailers", e);
    // }
    // }
    //
    // return trailers;
    // }
  }
}
