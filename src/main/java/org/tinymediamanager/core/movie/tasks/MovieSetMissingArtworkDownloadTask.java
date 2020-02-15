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
package org.tinymediamanager.core.movie.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSetArtworkHelper;
import org.tinymediamanager.core.movie.MovieSetSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieArtworkProvider;

/**
 * The Class MovieMissingArtworkDownloadTask. Used to find and download missing for the given movies
 *
 * @author Manuel Laggner
 */
public class MovieSetMissingArtworkDownloadTask extends TmmThreadPool {
  private static final Logger            LOGGER = LoggerFactory.getLogger(MovieSetMissingArtworkDownloadTask.class);
  private static final ResourceBundle    BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private List<MovieSet>                 moviesToScrape;
  private MovieSetSearchAndScrapeOptions scrapeOptions;

  public MovieSetMissingArtworkDownloadTask(List<MovieSet> moviesToScrape, MovieSetSearchAndScrapeOptions scrapeOptions) {
    super(BUNDLE.getString("task.missingartwork"));
    this.moviesToScrape = moviesToScrape;
    this.scrapeOptions = scrapeOptions;
  }

  @Override
  protected void doInBackground() {
    LOGGER.info("Getting missing movieset artwork");
    initThreadPool(3, "scrapeMissingMovieSetArtwork");
    start();

    for (MovieSet movieSet : moviesToScrape) {
      if (MovieSetArtworkHelper.hasMissingArtwork(movieSet)) {
        submitTask(new Worker(movieSet));
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
    private MovieSet  movieSet;

    public Worker(MovieSet movieSet) {
      this.movieSet = movieSet;
    }

    @Override
    public void run() {
      try {
        MovieList movieList = MovieList.getInstance();
        // set up scrapers
        List<MediaArtwork> artwork = new ArrayList<>();
        ArtworkSearchAndScrapeOptions options = new ArtworkSearchAndScrapeOptions(MediaType.MOVIE_SET);
        options.setDataFromOtherOptions(scrapeOptions);
        options.setArtworkType(MediaArtworkType.ALL);
        for (Map.Entry<String, Object> entry : movieSet.getIds().entrySet()) {
          options.setId(entry.getKey(), entry.getValue().toString());
        }
        options.setLanguage(MovieModuleManager.SETTINGS.getScraperLanguage());
        options.setFanartSize(MovieModuleManager.SETTINGS.getImageFanartSize());
        options.setPosterSize(MovieModuleManager.SETTINGS.getImagePosterSize());

        // scrape providers till one artwork has been found
        for (MediaScraper scraper : movieList.getDefaultArtworkScrapers()) {
          IMovieArtworkProvider artworkProvider = (IMovieArtworkProvider) scraper.getMediaProvider();
          try {
            artwork.addAll(artworkProvider.getArtwork(options));
          }
          catch (ScrapeException e) {
            LOGGER.error("getArtwork", e);
            MessageManager.instance.pushMessage(
                new Message(MessageLevel.ERROR, movieSet, "message.scrape.subtitlefailed", new String[] { ":", e.getLocalizedMessage() }));
          }
          catch (MissingIdException ignored) {
            // no need to log a missing ID here
          }
        }

        // now set & download the artwork
        if (!artwork.isEmpty()) {
          MovieSetArtworkHelper.getMissingArtwork(movieSet, artwork);
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
