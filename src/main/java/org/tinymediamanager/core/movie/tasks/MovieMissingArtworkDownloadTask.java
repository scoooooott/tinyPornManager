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
package org.tinymediamanager.core.movie.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.MovieArtworkHelper;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieArtworkProvider;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MovieMissingArtworkDownloadTask. Used to find and download missing for the given movies
 * 
 * @author Manuel Laggner
 */
public class MovieMissingArtworkDownloadTask extends TmmThreadPool {
  private final static Logger         LOGGER = LoggerFactory.getLogger(MovieMissingArtworkDownloadTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());       //$NON-NLS-1$

  private List<Movie>                 moviesToScrape;

  public MovieMissingArtworkDownloadTask(List<Movie> moviesToScrape) {
    super(BUNDLE.getString("task.missingartwork"));
    this.moviesToScrape = moviesToScrape;
  }

  @Override
  protected void doInBackground() {
    LOGGER.info("Getting missing artwork");
    initThreadPool(3, "scrapeMissingMovieArtwork");
    start();

    for (int i = 0; i < moviesToScrape.size(); i++) {
      Movie movie = moviesToScrape.get(i);
      if (MovieArtworkHelper.hasMissingArtwork(movie)) {
        submitTask(new Worker(movie));
      }
    }
    waitForCompletionOrCancel();
    LOGGER.info("Done getting missing artwork");
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
        List<MediaArtwork> artwork = new ArrayList<>();
        MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
        options.setArtworkType(MediaArtworkType.ALL);
        for (Map.Entry<String, Object> entry : movie.getIds().entrySet()) {
          options.setId(entry.getKey(), entry.getValue().toString());
        }
        options.setLanguage(LocaleUtils.toLocale(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().name()));
        options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());
        options.setFanartSize(MovieModuleManager.MOVIE_SETTINGS.getImageFanartSize());
        options.setPosterSize(MovieModuleManager.MOVIE_SETTINGS.getImagePosterSize());

        // scrape providers till one artwork has been found
        for (MediaScraper scraper : movieList.getDefaultArtworkScrapers()) {
          IMovieArtworkProvider artworkProvider = (IMovieArtworkProvider) scraper.getMediaProvider();
          try {
            artwork.addAll(artworkProvider.getArtwork(options));
          }
          catch (Exception e) {
            LOGGER.error("getArtwork", e);
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.scrape.movieartworkfailed"));
          }
        }

        // now set & download the artwork
        if (!artwork.isEmpty()) {
          MovieArtworkHelper.downloadMissingArtwork(movie, artwork);
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, "MovieMissingArtwork", "message.scrape.threadcrashed", new String[] { ":", e.getLocalizedMessage() }));
      }
    }
  }
}
