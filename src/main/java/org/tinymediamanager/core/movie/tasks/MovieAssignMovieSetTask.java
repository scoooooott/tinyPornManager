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

import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieSetMetadataProvider;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class MovieAssignMovieSetTask. A task to assign the movie set to the given movies
 * 
 * @author Manuel Laggner
 */
public class MovieAssignMovieSetTask extends TmmThreadPool {
  private final static Logger         LOGGER = LoggerFactory.getLogger(MovieAssignMovieSetTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<Movie>                 moviesToScrape;

  public MovieAssignMovieSetTask(List<Movie> moviesToScrape) {
    super(BUNDLE.getString("movie.assignmovieset"));
    this.moviesToScrape = moviesToScrape;
  }

  @Override
  protected void doInBackground() {
    initThreadPool(1, "scrape");
    start();

    for (int i = 0; i < moviesToScrape.size(); i++) {
      Movie movie = moviesToScrape.get(i);
      submitTask(new Worker(movie));
    }
    waitForCompletionOrCancel();
    LOGGER.info("Done assigning movies to movie sets");
  }

  private class Worker implements Runnable {
    private MovieList movieList = MovieList.getInstance();
    private Movie     movie;

    public Worker(Movie movie) {
      this.movie = movie;
    }

    @Override
    public void run() {
      if (movie.getMovieSet() != null) {
        return;
      }
      try {
        List<MediaScraper> sets = MediaScraper.getMediaScrapers(ScraperType.MOVIE_SET);
        if (sets != null && sets.size() > 0) {
          MediaScraper first = sets.get(0); // just get first
          IMovieSetMetadataProvider mp = (IMovieSetMetadataProvider) first.getMediaProvider();
          MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
          options.setLanguage(LocaleUtils.toLocale(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().name()));
          options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());
          for (Entry<String, Object> entry : movie.getIds().entrySet()) {
            options.setId(entry.getKey(), entry.getValue().toString());
          }

          MediaMetadata md = mp.getMetadata(options);
          int collectionId = (int) md.getId(MediaMetadata.TMDB_SET);
          if (collectionId > 0) {
            String collectionName = md.getCollectionName();
            MovieSet movieSet = movieList.getMovieSet(collectionName, collectionId);
            if (movieSet != null && movieSet.getTmdbId() == 0) {
              movieSet.setTmdbId(collectionId);
              // get movieset metadata
              try {
                options = new MediaScrapeOptions(MediaType.MOVIE_SET);
                options.setTmdbId(collectionId);
                options.setLanguage(LocaleUtils.toLocale(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().name()));
                options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());

                MediaMetadata info = mp.getMetadata(options);
                if (info != null && StringUtils.isNotBlank(info.getTitle())) {
                  movieSet.setTitle(info.getTitle());
                  movieSet.setPlot(info.getPlot());
                  if (!info.getMediaArt(MediaArtworkType.POSTER).isEmpty()) {
                    movieSet.setArtworkUrl(info.getMediaArt(MediaArtworkType.POSTER).get(0).getDefaultUrl(), MediaFileType.POSTER);
                  }
                  if (!info.getMediaArt(MediaArtworkType.BACKGROUND).isEmpty()) {
                    movieSet.setArtworkUrl(info.getMediaArt(MediaArtworkType.BACKGROUND).get(0).getDefaultUrl(), MediaFileType.FANART);
                  }
                }
              }
              catch (Exception e) {
              }
            }

            // add movie to movieset
            if (movieSet != null) {
              // first remove from "old" movieset
              movie.setMovieSet(null);

              // add to new movieset
              movie.setMovieSet(movieSet);
              movieSet.insertMovie(movie);
              movie.writeNFO();
              movie.saveToDb();
              movieSet.saveToDb();
            }
          }
        }
      }
      catch (Exception e) {
        LOGGER.error("error getting metadata: " + e.getMessage());
      }
    }
  }

  @Override
  public void callback(Object obj) {
    publishState((String) obj, progressDone);
  }
}
