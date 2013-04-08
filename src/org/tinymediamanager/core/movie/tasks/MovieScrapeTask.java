/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.core.movie.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.ui.TmmSwingWorker;

/**
 * The Class MovieScrapeTask.
 * 
 * @author Manuel Laggner
 */
public class MovieScrapeTask extends TmmSwingWorker {

  /** The Constant LOGGER. */
  private final static Logger         LOGGER = LoggerFactory.getLogger(MovieScrapeTask.class);

  /** The movies to scrape. */
  private List<Movie>                 moviesToScrape;

  /** The do search. */
  private boolean                     doSearch;

  private MovieSearchAndScrapeOptions options;

  /** The movie count. */
  private int                         movieCount;

  /**
   * Instantiates a new movie scrape task.
   * 
   * @param moviesToScrape
   *          the movies to scrape
   * @param doSearch
   *          the do search
   * @param scraperMetadataConfig
   *          the scraper metadata config
   * @param metadataProvider
   *          the metadata provider
   * @param artworkProviders
   *          the artwork providers
   * @param trailerProviders
   *          the trailer providers
   * @param label
   *          the label
   * @param progressBar
   *          the progress bar
   * @param button
   *          the button
   */
  public MovieScrapeTask(List<Movie> moviesToScrape, boolean doSearch, MovieSearchAndScrapeOptions options) {
    this.moviesToScrape = moviesToScrape;
    this.doSearch = doSearch;
    this.options = options;
    this.movieCount = moviesToScrape.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Void doInBackground() throws Exception {
    startProgressBar("scraping movies", 0);

    ExecutorService executor = Executors.newFixedThreadPool(3);

    // start 3 threads
    executor.execute(new Worker(this));
    // start second thread, if there are more than one movies to scrape
    if (movieCount > 1) {
      executor.execute(new Worker(this));
    }
    // start third thread, if there are more than two movies to scrape
    if (movieCount > 2) {
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
   * Gets the next movie.
   * 
   * @return the next movie
   */
  private synchronized Movie getNextMovie() {
    // get next movie to scrape
    if (moviesToScrape.size() > 0) {
      Movie movie = moviesToScrape.get(0);
      moviesToScrape.remove(movie);
      startProgressBar("scraping movies", 100 * (movieCount - moviesToScrape.size()) / movieCount);
      return movie;
    }

    return null;
  }

  /**
   * Cancel.
   */
  public void cancel() {
    cancel(false);
    moviesToScrape.clear();
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

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   * @param value
   *          the value
   */
  private void startProgressBar(String description, int value) {
    lblProgressAction.setText(description);
    progressBar.setVisible(true);
    progressBar.setValue(value);
    btnCancelTask.setVisible(true);
  }

  /**
   * Stop progress bar.
   */
  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setVisible(false);
    btnCancelTask.setVisible(false);
  }

  /**
   * The Class Worker.
   */
  private class Worker implements Runnable {

    /** The movie list. */
    private MovieList       movieList;

    /** The scrape task. */
    private MovieScrapeTask scrapeTask;

    /**
     * Instantiates a new worker.
     * 
     * @param scrapeTask
     *          the scrape task
     */
    public Worker(MovieScrapeTask scrapeTask) {
      this.scrapeTask = scrapeTask;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      try {
        movieList = MovieList.getInstance();
        // set up scrapers
        MovieScraperMetadataConfig scraperMetadataConfig = options.getScraperMetadataConfig();
        IMediaMetadataProvider mediaMetadataProvider = movieList.getMetadataProvider(options.getMetadataScraper());
        List<IMediaArtworkProvider> artworkProviders = movieList.getArtworkProviders(options.getArtworkScrapers());
        List<IMediaTrailerProvider> trailerProviders = movieList.getTrailerProviders(options.getTrailerScrapers());

        // do work
        while (true) {
          Movie movie = scrapeTask.getNextMovie();
          if (movie == null) {
            break;
          }

          // scrape movie

          // search movie
          MediaSearchResult result1 = null;
          if (doSearch) {
            List<MediaSearchResult> results = movieList.searchMovie(movie.getTitle(), movie.getImdbId(), mediaMetadataProvider);
            if (results != null && !results.isEmpty()) {
              result1 = results.get(0);
              // check if there is an other result with 100% score
              if (results.size() > 1) {
                MediaSearchResult result2 = results.get(1);
                // if both results have 100% score - do not take any result
                if (result1.getScore() == 1 && result2.getScore() == 1) {
                  continue;
                }
              }
            }
          }

          // get metadata, artwork and trailers
          if ((doSearch && result1 != null) || !doSearch) {
            try {
              MediaScrapeOptions options = new MediaScrapeOptions();
              options.setResult(result1);

              // we didn't do a search - pass imdbid and tmdbid from movie
              // object
              if (!doSearch) {
                options.setImdbId(movie.getImdbId());
                options.setTmdbId(movie.getTmdbId());
              }

              // scrape metadata if wanted
              MediaMetadata md = null;

              if (scraperMetadataConfig.isCast() || scraperMetadataConfig.isCertification() || scraperMetadataConfig.isGenres()
                  || scraperMetadataConfig.isOriginalTitle() || scraperMetadataConfig.isPlot() || scraperMetadataConfig.isRating()
                  || scraperMetadataConfig.isRuntime() || scraperMetadataConfig.isTagline() || scraperMetadataConfig.isTitle()
                  || scraperMetadataConfig.isYear()) {
                md = mediaMetadataProvider.getMetadata(options);
                movie.setMetadata(md);
              }

              // scrape artwork if wanted
              if (scraperMetadataConfig.isArtwork()) {
                movie.setArtwork(getArtwork(movie, md, artworkProviders));
              }

              // scrape trailer if wanted
              if (scraperMetadataConfig.isTrailer()) {
                movie.setTrailers(getTrailers(movie, md, trailerProviders));
              }
              movie.writeNFO();
            }
            catch (Exception e) {
              LOGGER.error("movie.setMetadata", e);
            }
          }
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
      }
    }

    /**
     * Gets the artwork.
     * 
     * @param metadata
     *          the metadata
     * @return the artwork
     */
    public List<MediaArtwork> getArtwork(Movie movie, MediaMetadata metadata, List<IMediaArtworkProvider> artworkProviders) {
      List<MediaArtwork> artwork = null;

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setArtworkType(MediaArtworkType.ALL);
      options.setMetadata(metadata);
      options.setImdbId(movie.getImdbId());
      options.setTmdbId(movie.getTmdbId());

      // scrape providers till one artwork has been found
      for (IMediaArtworkProvider artworkProvider : artworkProviders) {
        try {
          artwork = artworkProvider.getArtwork(options);
        }
        catch (Exception e) {
          LOGGER.error("getArtwork", e);
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

    /**
     * Gets the trailers.
     * 
     * @param metadata
     *          the metadata
     * @return the trailers
     */
    private List<MediaTrailer> getTrailers(Movie movie, MediaMetadata metadata, List<IMediaTrailerProvider> trailerProviders) {
      List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setMetadata(metadata);
      options.setImdbId(movie.getImdbId());
      options.setTmdbId(movie.getTmdbId());

      // scrape trailers
      for (IMediaTrailerProvider trailerProvider : trailerProviders) {
        try {
          List<MediaTrailer> foundTrailers = trailerProvider.getTrailers(options);
          trailers.addAll(foundTrailers);
        }
        catch (Exception e) {
          LOGGER.error("getTrailers", e);
        }
      }

      return trailers;
    }
  }
}
