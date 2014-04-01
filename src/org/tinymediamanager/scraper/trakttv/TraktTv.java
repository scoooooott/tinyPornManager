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
package org.tinymediamanager.scraper.trakttv;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieList;

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.ActionResponse;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.enumerations.Extended;
import com.jakewharton.trakt.services.MovieService.Movies;
import com.jakewharton.trakt.services.MovieService.SeenMovie;

/**
 * Sync your watched status with Trakt.tv
 * 
 * @author Myron Boyle
 * 
 */
public class TraktTv {

  private static final Logger LOGGER       = LoggerFactory.getLogger(TraktTv.class);
  private static final String DEV_API_KEY  = "";
  private static String       USER_API_KEY = "";
  private static String       USER_NAME    = "";
  private static final Trakt  trakt        = new Trakt();

  public TraktTv(String username, String passwordSha1, String userApiKey) {
    trakt.setApiKey(userApiKey);
    USER_API_KEY = userApiKey;
    trakt.setAuthentication(username, passwordSha1);
    USER_NAME = username;
    trakt.setIsDebug(true); // testing
  }

  /**
   * gets the underlying Trakt Java Object<br>
   * https://github.com/UweTrottmann/trakt-java
   * 
   * @return Trakt()
   */
  public final Trakt getManager() {
    return trakt;
  }

  /**
   * gets all watched movies from Trakt, and sets the "watched" flag on TMM movies (if IMDB matches)
   */
  public void updatedWatchedMoviesFromTrakt() {
    MovieList movieList = MovieList.getInstance();
    List<org.tinymediamanager.core.movie.entities.Movie> tmmMovies = movieList.getMovies();

    List<Movie> traktMovies = trakt.userService().libraryMoviesWatched(USER_NAME, Extended.DEFAULT);

    // loop over all watched movies on trakt
    for (Movie watched : traktMovies) {

      // loop over TMM movies, and check if IMDBID match
      for (org.tinymediamanager.core.movie.entities.Movie tmmMovie : tmmMovies) {
        if (watched.imdb_id.equals(tmmMovie.getImdbId())) {

          if (!tmmMovie.isWatched()) {
            LOGGER.info("Marking movie '" + tmmMovie.getTitle() + "' as watched");
            tmmMovie.setWatched(true);
            tmmMovie.saveToDb();
          }

        }
      }
    }
  }

  /**
   * adds all TMM movies to Trakt collection<br>
   * optionally set them as watched, if they are watched in TMM<br>
   * works only if we have a IMDB id...
   * 
   * @param setWatched
   *          true/false if we want to set all watched TMM movies as "watched" on Trakt
   */
  public void sendMyMoviesToTrakt(boolean setWatched) {
    MovieList movieList = MovieList.getInstance();
    List<org.tinymediamanager.core.movie.entities.Movie> tmmMovies = movieList.getMovies();

    List<SeenMovie> libMovies = new ArrayList<SeenMovie>(); // array for ALL TMM movies
    List<SeenMovie> seenMovies = new ArrayList<SeenMovie>(); // array for "watched" TMM movies
    for (org.tinymediamanager.core.movie.entities.Movie tmmMovie : tmmMovies) {
      if (tmmMovie.getImdbId().isEmpty() && tmmMovie.getTmdbId() == 0) {
        // do not add to Trakt if we have no IDs
        continue;
      }
      SeenMovie seen = new SeenMovie(tmmMovie.getImdbId());
      seen.title = tmmMovie.getTitle();
      seen.tmdb_id = tmmMovie.getTmdbId();
      seen.year = Integer.valueOf(tmmMovie.getYear());
      libMovies.add(seen); // add to lib
      if (tmmMovie.isWatched()) {
        seenMovies.add(seen); // add to seen
      }
    }
    ActionResponse response;
    response = trakt.movieService().library(new Movies(libMovies)); // add all to collection
    System.out.println("Status: " + response.status);
    System.out.println("Message: " + response.message);
    System.out.println("Inserted: " + response.inserted);
    System.out.println("Already inserted: " + response.already_exist);
    System.out.println("Skipped: " + response.skipped);

    if (setWatched) {
      response = trakt.movieService().seen(new Movies(seenMovies)); // and set seen/watched
      System.out.println("Status: " + response.status);
      System.out.println("Message: " + response.message);
      System.out.println("Inserted: " + response.inserted);
      System.out.println("Already inserted: " + response.already_exist);
      System.out.println("Skipped: " + response.skipped);
    }
  }
}
