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
package org.tinymediamanager.core.movie.tasks;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.dialogs.MovieChooserDialog;

/**
 * The Class MovieScrapeTask.
 * 
 * @author Manuel Laggner
 */
public class MovieScrapeTask extends TmmThreadPool {
  private final static Logger         LOGGER = LoggerFactory.getLogger(MovieScrapeTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<Movie>                 moviesToScrape;
  private boolean                     doSearch;
  private MovieSearchAndScrapeOptions options;
  private List<Movie>                 smartScrapeList;

  public MovieScrapeTask(List<Movie> moviesToScrape, boolean doSearch, MovieSearchAndScrapeOptions options) {
    super(BUNDLE.getString("movie.scraping"));
    this.moviesToScrape = moviesToScrape;
    this.doSearch = doSearch;
    this.options = options;
  }

  @Override
  protected void doInBackground() {
    initThreadPool(3, "scrape");
    start();

    smartScrapeList = new ArrayList<Movie>(0);

    for (int i = 0; i < moviesToScrape.size(); i++) {
      Movie movie = moviesToScrape.get(i);
      submitTask(new Worker(movie));
    }
    waitForCompletionOrCancel();

    // initiate smart scrape
    if (!smartScrapeList.isEmpty() && !GraphicsEnvironment.isHeadless()) {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            for (Movie movie : smartScrapeList) {
              MovieChooserDialog dialogMovieChooser = new MovieChooserDialog(movie, smartScrapeList.size() > 1 ? true : false);
              if (!dialogMovieChooser.showDialog()) {
                break;
              }
            }
          }
        });
      }
      catch (Exception e) {
        LOGGER.error("SmartScrape crashed " + e.getMessage());
      }
    }

    if (MovieModuleManager.MOVIE_SETTINGS.getSyncTrakt()) {
      TmmTask task = new SyncTraktTvTask(moviesToScrape, null);
      TmmTaskManager.getInstance().addUnnamedTask(task);
    }

    LOGGER.info("Done scraping movies)");
  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the text is never right
    publishState(progressDone);
  }

  /****************************************************************************************
   * Helper classes
   ****************************************************************************************/
  private class Worker implements Runnable {
    private MovieList movieList;
    private Movie     movie;

    public Worker(Movie movie) {
      this.movie = movie;
    }

    @Override
    public void run() {
      try {
        movieList = MovieList.getInstance();
        // set up scrapers
        MovieScraperMetadataConfig scraperMetadataConfig = options.getScraperMetadataConfig();
        IMediaMetadataProvider mediaMetadataProvider = movieList.getMetadataProvider(options.getMetadataScraper());
        List<IMediaArtworkProvider> artworkProviders = movieList.getArtworkProviders(options.getArtworkScrapers());
        List<IMediaTrailerProvider> trailerProviders = movieList.getTrailerProviders(options.getTrailerScrapers());

        // search movie
        MediaSearchResult result1 = null;
        if (doSearch) {
          result1 = searchForMovie(mediaMetadataProvider);
          if (result1 == null) {
            // append this search request to the UI with search & scrape dialog
            synchronized (smartScrapeList) {
              smartScrapeList.add(movie);
              return;
            }
          }
        }

        // get metadata, artwork and trailers
        if ((doSearch && result1 != null) || !doSearch) {
          try {
            MediaScrapeOptions options = new MediaScrapeOptions();
            options.setResult(result1);
            options.setLanguage(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
            options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());
            options.setScrapeImdbForeignLanguage(MovieModuleManager.MOVIE_SETTINGS.isImdbScrapeForeignLanguage());
            options.setScrapeCollectionInfo(scraperMetadataConfig.isCollection());

            // we didn't do a search - pass imdbid and tmdbid from movie object
            if (!doSearch) {
              for (Entry<String, Object> entry : movie.getIds().entrySet()) {
                options.setId(entry.getKey(), entry.getValue().toString());
              }
            }
            else {
              // override scraper with one from search result
              mediaMetadataProvider = movieList.getMetadataProvider(result1.getProviderId());
            }

            // scrape metadata if wanted
            MediaMetadata md = null;

            md = mediaMetadataProvider.getMetadata(options);

            if (scraperMetadataConfig.isMetadata()) {
              movie.setMetadata(md, scraperMetadataConfig);
            }

            // scrape artwork if wanted
            if (scraperMetadataConfig.isArtwork()) {
              movie.setArtwork(getArtwork(movie, md, artworkProviders), scraperMetadataConfig);
            }

            // scrape trailer if wanted
            if (scraperMetadataConfig.isTrailer()) {
              movie.setTrailers(getTrailers(movie, md, trailerProviders));
            }
            movie.writeNFO();
          }
          catch (Exception e) {
            LOGGER.error("movie.setMetadata", e);
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.scrape.metadatamoviefailed"));
          }
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "MovieScraper", "message.scrape.threadcrashed"));
      }
    }

    private MediaSearchResult searchForMovie(IMediaMetadataProvider mediaMetadataProvider) {
      List<MediaSearchResult> results = movieList.searchMovie(movie.getTitle(), movie, mediaMetadataProvider);
      MediaSearchResult result = null;

      if (results != null && !results.isEmpty()) {
        result = results.get(0);
        // check if there is an other result with 100% score
        if (results.size() > 1) {
          MediaSearchResult result2 = results.get(1);
          // if both results have 100% score - do not take any result
          if (result.getScore() == 1 && result2.getScore() == 1) {
            LOGGER.info("two 100% results, can't decide whitch to take - ignore result");
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "movie.scrape.toosimilar"));
            return null;
          }
        }

        // get threshold from settings (default 0.75) - to minimize false positives
        final double scraperTreshold = MovieModuleManager.MOVIE_SETTINGS.getScraperThreshold();
        LOGGER.info("using treshold from settings of {}", scraperTreshold);
        if (result.getScore() < scraperTreshold) {
          LOGGER.info("score is lower than " + scraperTreshold + " (" + result.getScore() + ") - ignore result");
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "movie.scrape.toolowscore", new String[] { String.format("%.2f",
              scraperTreshold) }));
          return null;
        }
      }
      else {
        LOGGER.info("no result found for " + movie.getTitle());
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "movie.scrape.nomatchfound"));
      }

      return result;
    }

    private List<MediaArtwork> getArtwork(Movie movie, MediaMetadata metadata, List<IMediaArtworkProvider> artworkProviders) {
      List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setType(MediaType.MOVIE);
      options.setArtworkType(MediaArtworkType.ALL);
      options.setMetadata(metadata);
      options.setImdbId(movie.getImdbId());
      options.setTmdbId(movie.getTmdbId());
      options.setLanguage(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
      options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());
      options.setScrapeImdbForeignLanguage(MovieModuleManager.MOVIE_SETTINGS.isImdbScrapeForeignLanguage());

      // scrape providers till one artwork has been found
      for (IMediaArtworkProvider artworkProvider : artworkProviders) {
        try {
          artwork.addAll(artworkProvider.getArtwork(options));
        }
        catch (Exception e) {
          LOGGER.error("getArtwork", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.scrape.movieartworkfailed"));
        }
      }

      return artwork;
    }

    private List<MovieTrailer> getTrailers(Movie movie, MediaMetadata metadata, List<IMediaTrailerProvider> trailerProviders) {
      List<MovieTrailer> trailers = new ArrayList<MovieTrailer>();

      // add local trailers!
      for (MediaFile mf : movie.getMediaFiles(MediaFileType.TRAILER)) {
        LOGGER.debug("adding local trailer " + mf.getFilename());
        MovieTrailer mt = new MovieTrailer();
        mt.setName(mf.getFilename());
        mt.setProvider("downloaded");
        mt.setQuality(mf.getVideoFormat());
        mt.setInNfo(false);
        mt.setUrl(mf.getFile().toURI().toString());
        trailers.add(mt);
      }

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setMetadata(metadata);
      options.setImdbId(movie.getImdbId());
      options.setTmdbId(movie.getTmdbId());
      options.setLanguage(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
      options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());
      options.setScrapeImdbForeignLanguage(MovieModuleManager.MOVIE_SETTINGS.isImdbScrapeForeignLanguage());

      // scrape trailers
      for (IMediaTrailerProvider trailerProvider : trailerProviders) {
        try {
          List<MediaTrailer> foundTrailers = trailerProvider.getTrailers(options);
          for (MediaTrailer mediaTrailer : foundTrailers) {
            MovieTrailer movieTrailer = new MovieTrailer(mediaTrailer);
            trailers.add(movieTrailer);
          }
        }
        catch (Exception e) {
          LOGGER.error("getTrailers", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.scrape.movietrailerfailed"));
        }
      }

      return trailers;
    }
  }
}
