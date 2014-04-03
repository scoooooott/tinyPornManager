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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.scraper.MediaGenres;

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.ActionResponse;
import com.jakewharton.trakt.entities.Movie;
import com.jakewharton.trakt.entities.TvShow;
import com.jakewharton.trakt.entities.TvShowEpisode;
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

  private static final Logger LOGGER   = LoggerFactory.getLogger(TraktTv.class);
  private static final Trakt  trakt    = new Trakt();
  private String              userName = "";
  private ActionResponse      response;

  /**
   * gets a new Trakt object with settings values (user / pass / apikey)
   */
  public TraktTv() {
    trakt.setApiKey(Globals.settings.getTraktAPI());
    trakt.setAuthentication(Globals.settings.getTraktUsername(), Globals.settings.getTraktPassword());
    userName = Globals.settings.getTraktUsername();
  }

  /**
   * gets a new Trakt object with custom values (user / passAsSHA1 / apikey)
   */
  public TraktTv(String username, String passwordSha1, String userApiKey) {
    trakt.setApiKey(userApiKey);
    trakt.setAuthentication(username, passwordSha1);
    userName = username;
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
   * gets the last trakt response (status, message, inserted, skipped, ...)
   * 
   * @return response object
   */
  public ActionResponse getResponse() {
    return response;
  }

  public String getUserName() {
    return userName;
  }

  /**
   * syncs complete database from/to Trakt<br>
   * Get first all watched flags from Trakt and submits then all movies/shows to Trakt collection
   */
  public void syncAll() {
    // FIXME: use task queue
    updatedWatchedMoviesFromTrakt();
    sendMyMoviesToTrakt();
    updatedWatchedTvShowsFromTrakt();
    // sendMyTvShowsToTrakt();
  }

  /**
   * gets ALL watched movies from Trakt, and sets the "watched" flag on TMM movies (if IMDB matches)
   */
  public void updatedWatchedMoviesFromTrakt() {
    MovieList movieList = MovieList.getInstance();
    List<org.tinymediamanager.core.movie.entities.Movie> tmmMovies = movieList.getMovies();

    // get all Trakt watched movies
    List<Movie> traktMovies = trakt.userService().libraryMoviesWatched(userName, Extended.MIN);

    // loop over all watched movies on trakt
    for (Movie watched : traktMovies) {

      // loop over TMM movies, and check if IMDBID match
      for (org.tinymediamanager.core.movie.entities.Movie tmmMovie : tmmMovies) {
        if (watched.imdb_id.equals(tmmMovie.getImdbId()) || watched.tmdbId == tmmMovie.getTmdbId()) {

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
   * gets ALL watched TvShows from Trakt, and sets the "watched" flag on TMM show/episodes (if TvDB matches)
   */
  public void updatedWatchedTvShowsFromTrakt() {
    TvShowList tvShowList = TvShowList.getInstance();
    List<org.tinymediamanager.core.tvshow.entities.TvShow> tmmShows = tvShowList.getTvShows();

    // get all Trakt watched TvShows
    List<TvShow> traktShows = trakt.userService().libraryShowsWatched(userName, Extended.MIN);
    // Extended.DEFAULT adds url, poster, fanart, banner, genres
    // Extended.MAX adds certs, runtime, and other stuff (useful for scraper!)

    // loop over all watched shows on trakt
    for (TvShow watched : traktShows) {

      // loop over TMM shows, and check if TvDB match
      for (org.tinymediamanager.core.tvshow.entities.TvShow tmmShow : tmmShows) {
        if (watched.tvdb_id.equals(tmmShow.getTvdbId())) {

          // update missing IDs (we get them for free :)
          if (tmmShow.getImdbId().isEmpty() && !StringUtils.isEmpty(watched.imdb_id)) {
            tmmShow.setImdbId(watched.imdb_id);
          }
          if (tmmShow.getTvdbId().isEmpty() && watched.tvdb_id != null && watched.tvdb_id != 0) {
            tmmShow.setTvdbId(String.valueOf(watched.tvdb_id));
          }
          if (((String) tmmShow.getId(Constants.TVRAGEID)).isEmpty() && watched.tvrage_id != null && watched.tvrage_id != 0) {
            tmmShow.setId(Constants.TVRAGEID, watched.tvrage_id);
          }

          // set show watched (only if COMPLETE watched?!)
          // if (!tmmShow.isWatched()) {
          // LOGGER.info("Marking TvShow '" + tmmShow.getTitle() + "' as watched");
          // tmmShow.setWatched(true);
          // }

          // set episodes watched
          for (TvShowEpisode ep : watched.episodes) {
            // loop over TMM episodes, and check if season/episode number match
            for (org.tinymediamanager.core.tvshow.entities.TvShowEpisode tmmEp : tmmShow.getEpisodes()) {
              if (ep.season == tmmEp.getSeason() && ep.number == tmmEp.getEpisode() && !tmmEp.isWatched()) {
                LOGGER.info("Marking '" + tmmShow.getTitle() + " S:" + tmmEp.getSeason() + " EP:" + tmmEp.getEpisode() + "' as watched");
                tmmEp.setWatched(true);
              }
            }
          }

          tmmShow.saveToDb();

        } // end tvdb_id matches
      }
    }
  }

  /**
   * adds ALL TMM movies to Trakt collection<br>
   * works only if we have a IMDB or TMDB id...
   */
  public void sendMyMoviesToTrakt() {
    MovieList movieList = MovieList.getInstance();
    List<org.tinymediamanager.core.movie.entities.Movie> tmmMovies = movieList.getMovies();
    sendMyMoviesToTrakt(tmmMovies);
  }

  /**
   * adds TMM movies to Trakt collection<br>
   * works only if we have a IMDB or TMDB id...
   * 
   * @param movies
   *          all the TMM movies to send
   */
  public void sendMyMoviesToTrakt(List<org.tinymediamanager.core.movie.entities.Movie> movies) {
    List<SeenMovie> libMovies = new ArrayList<SeenMovie>(); // array for ALL TMM movies
    List<SeenMovie> seenMovies = new ArrayList<SeenMovie>(); // array for "watched" TMM movies

    for (org.tinymediamanager.core.movie.entities.Movie tmmMovie : movies) {
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

    response = trakt.movieService().library(new Movies(libMovies)); // add all to collection
    System.out.println("Status: " + response.status);
    System.out.println("Message: " + response.message);
    System.out.println("Inserted: " + response.inserted);
    System.out.println("Already inserted: " + response.already_exist);
    System.out.println("Skipped: " + response.skipped);

    if (seenMovies.size() > 0) {
      response = trakt.movieService().seen(new Movies(seenMovies)); // and set seen/watched
      System.out.println("Status: " + response.status);
      System.out.println("Message: " + response.message);
      System.out.println("Inserted: " + response.inserted);
      System.out.println("Already inserted: " + response.already_exist);
      System.out.println("Skipped: " + response.skipped);
    }
  }

  /**
   * Maps scraper Genres to internal TMM genres
   */
  private MediaGenres getTmmGenre(String genre) {
    MediaGenres g = null;
    if (genre.isEmpty()) {
      return g;
    }
    // @formatter:off
    else if (genre.equals("Action"))           { g = MediaGenres.ACTION; }
    else if (genre.equals("Adventure"))        { g = MediaGenres.ADVENTURE; }
    else if (genre.equals("Animation"))        { g = MediaGenres.ANIMATION; }
    else if (genre.equals("Comedy"))           { g = MediaGenres.COMEDY; }
    else if (genre.equals("Children"))         { g = MediaGenres.FAMILY; }
    else if (genre.equals("Crime"))            { g = MediaGenres.CRIME; }
    else if (genre.equals("Documentary"))      { g = MediaGenres.DOCUMENTARY; }
    else if (genre.equals("Drama"))            { g = MediaGenres.DRAMA; }
    else if (genre.equals("Family"))           { g = MediaGenres.FAMILY; }
    else if (genre.equals("Fantasy"))          { g = MediaGenres.FANTASY; }
    else if (genre.equals("Film Noir"))        { g = MediaGenres.FILM_NOIR; }
    else if (genre.equals("History"))          { g = MediaGenres.HISTORY; }
    else if (genre.equals("Game Show"))        { g = MediaGenres.GAME_SHOW; }
    else if (genre.equals("Home and Garden"))  { g = MediaGenres.DOCUMENTARY; }
    else if (genre.equals("Horror"))           { g = MediaGenres.HORROR; }
    else if (genre.equals("Indie"))            { g = MediaGenres.INDIE; }
    else if (genre.equals("Music"))            { g = MediaGenres.MUSIC; }
    else if (genre.equals("Mini Series"))      { g = MediaGenres.SERIES; }
    else if (genre.equals("Musical"))          { g = MediaGenres.MUSICAL; }
    else if (genre.equals("Mystery"))          { g = MediaGenres.MYSTERY; }
    else if (genre.equals("News"))             { g = MediaGenres.NEWS; }
    else if (genre.equals("Reality"))          { g = MediaGenres.REALITY_TV; }
    else if (genre.equals("Romance"))          { g = MediaGenres.ROMANCE; }
    else if (genre.equals("Science Fiction"))  { g = MediaGenres.SCIENCE_FICTION; }
    else if (genre.equals("Sport"))            { g = MediaGenres.SPORT; }
    else if (genre.equals("Special Interest")) { g = MediaGenres.INDIE; }
    else if (genre.equals("Soap"))             { g = MediaGenres.SERIES; }
    else if (genre.equals("Suspense"))         { g = MediaGenres.SUSPENSE; }
    else if (genre.equals("Talk Show"))        { g = MediaGenres.TALK_SHOW; }
    else if (genre.equals("Thriller"))         { g = MediaGenres.THRILLER; }
    else if (genre.equals("War"))              { g = MediaGenres.WAR; }
    else if (genre.equals("Western"))          { g = MediaGenres.WESTERN; }
    else if (genre.equals("No Genre"))         { return null; }
    // @formatter:on
    if (g == null) {
      g = MediaGenres.getGenre(genre);
    }
    return g;
  }
}
