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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaGenres;

import retrofit.RetrofitError;
import retrofit.client.Response;

import com.jakewharton.trakt.Trakt;
import com.jakewharton.trakt.entities.ActionResponse;
import com.jakewharton.trakt.enumerations.Extended;
import com.jakewharton.trakt.enumerations.Status;
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
  private String              password = "";
  private String              apiKey   = "";
  private ActionResponse      response;

  /**
   * gets a new Trakt object with settings values (user / pass / apikey)
   */
  public TraktTv() {
    this(Globals.settings.getTraktUsername(), Globals.settings.getTraktPassword(), Globals.settings.getTraktAPI());
  }

  /**
   * gets a new Trakt object with custom values (user / passAsSHA1 / apikey)
   */
  public TraktTv(String username, String passwordSha1, String userApiKey) {
    userName = username;
    password = passwordSha1;
    apiKey = userApiKey;

    trakt.setApiKey(userApiKey);
    trakt.setAuthentication(username, passwordSha1);
  }

  /**
   * do we have values for user/pass/api ?!
   * 
   * @return true/false if trakt could be called
   */
  public boolean isEnabled() {
    return !userName.isEmpty() && !password.isEmpty() && !apiKey.isEmpty();
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
   * Syncs Trakt.tv collection (all movies you have)<br>
   * Gets all Trakt movies from collection, matches them to ours, and sends ONLY the new ones back to Trakt
   */
  public void syncTraktMovieCollection() {
    if (!isEnabled()) {
      return;
    }

    // *****************************************************************************
    // 1) get diff of TMM <-> Trakt collection
    // *****************************************************************************
    // our copied list of TMM movies
    List<Movie> tmmMovies = new ArrayList<Movie>(MovieList.getInstance().getMovies());
    LOGGER.info("You have " + tmmMovies.size() + " movies in your TMM database");

    // get ALL Trakt movies in collection
    List<com.jakewharton.trakt.entities.Movie> traktMovies;
    try {
      traktMovies = trakt.userService().libraryMoviesCollection(userName, Extended.MIN);
      LOGGER.info("You have " + traktMovies.size() + " movies in your Trakt.tv collection");
      // Extended.DEFAULT adds url, poster, fanart, banner, genres
      // Extended.MAX adds certs, runtime, and other stuff (useful for scraper!)
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }

    // loop over all movies on trakt
    for (com.jakewharton.trakt.entities.Movie traktMovie : traktMovies) {
      // loop over TMM movies, and check if IMDBID match
      for (int i = tmmMovies.size() - 1; i >= 0; i--) {
        Movie tmmMovie = tmmMovies.get(i);
        if (traktMovie.imdb_id.equals(tmmMovie.getImdbId()) || traktMovie.tmdbId == tmmMovie.getTmdbId()) {
          // we have a match; remove it from our list (no need to add)
          tmmMovies.remove(i);
        }
      }
    }

    if (tmmMovies.size() == 0) {
      LOGGER.info("Already up-to-date - no need to add anything :)");
      return;
    }

    // *****************************************************************************
    // 2) add remaining TMM movies to Trakt collection
    // *****************************************************************************
    LOGGER.debug("prepare " + tmmMovies.size() + " movies for Trakt.tv sync");

    List<SeenMovie> libMovies = new ArrayList<SeenMovie>(); // array for ALL TMM movies
    int nosync = 0;
    for (Movie tmmMovie : tmmMovies) {
      if (tmmMovie.getImdbId().isEmpty() && tmmMovie.getTmdbId() == 0) {
        // do not add to Trakt if we have no IDs
        nosync++;
        continue;
      }
      SeenMovie seen = new SeenMovie(tmmMovie.getImdbId());
      seen.title = tmmMovie.getTitle();
      seen.imdb_id = tmmMovie.getImdbId();
      seen.tmdb_id = tmmMovie.getTmdbId();
      seen.year = Integer.valueOf(tmmMovie.getYear());
      libMovies.add(seen); // add to lib
    }
    if (nosync > 0) {
      LOGGER.debug("skipping " + nosync + " movies, because they have not been scraped yet!");
    }

    if (libMovies.size() == 0) {
      LOGGER.info("no new movies for Trakt sync found.");
      return;
    }

    try {
      LOGGER.info("Adding " + tmmMovies.size() + " movies to Trakt.tv collection");
      response = trakt.movieService().library(new Movies(libMovies));
      LOGGER.info("Trakt add-to-library status:");
      printStatus(response);
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
    }

  }

  /**
   * Syncs Trakt.tv "seen" flag (all movies you have already marked as watched)<br>
   * Gets all watched movies from Trakt, and sets the "watched" flag on TMM movies.<br>
   * Then update the remaining TMM movies on Trakt as 'seen'.
   */
  public void syncTraktMovieWatched() {
    if (!isEnabled()) {
      return;
    }

    List<Movie> tmmMovies = MovieList.getInstance().getMovies();

    // *****************************************************************************
    // 1) get all Trakt watched movies and update our "watched" status
    // *****************************************************************************
    List<com.jakewharton.trakt.entities.Movie> traktMovies;
    try {
      traktMovies = trakt.userService().libraryMoviesWatched(userName, Extended.MIN);
      LOGGER.info("You have " + traktMovies.size() + " movies marked as 'seen' in your Trakt.tv collection");
      // Extended.DEFAULT adds url, poster, fanart, banner, genres
      // Extended.MAX adds certs, runtime, and other stuff (useful for scraper!)
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }

    // loop over all watched movies on trakt
    for (com.jakewharton.trakt.entities.Movie traktWatched : traktMovies) {

      // loop over TMM movies, and check if IMDBID match
      for (Movie tmmMovie : tmmMovies) {
        if (traktWatched.imdb_id.equals(tmmMovie.getImdbId()) || traktWatched.tmdbId == tmmMovie.getTmdbId()) {

          if (!tmmMovie.isWatched()) {
            // save Trakt watched status
            LOGGER.info("Marking movie '" + tmmMovie.getTitle() + "' as watched");
            tmmMovie.setWatched(true);
            tmmMovie.saveToDb();
          }

        }
      }
    }

    // *****************************************************************************
    // 2) mark additionally "watched" movies as 'seen' on Trakt
    // *****************************************************************************
    // Now get all TMM watched movies...
    List<Movie> tmmWatchedMovies = new ArrayList<Movie>();
    for (Movie movie : tmmMovies) {
      if (movie.isWatched()) {
        tmmWatchedMovies.add(movie);
      }
    }
    LOGGER.info("You have now " + tmmWatchedMovies.size() + " movies marked as 'watched' in your TMM database");

    // ...and subtract the already watched from Trakt
    for (int i = tmmWatchedMovies.size() - 1; i >= 0; i--) {
      for (com.jakewharton.trakt.entities.Movie traktWatched : traktMovies) {
        if (traktWatched.imdb_id.equals(tmmWatchedMovies.get(i).getImdbId()) || traktWatched.tmdbId == tmmWatchedMovies.get(i).getTmdbId()) {
          tmmWatchedMovies.remove(i);
        }
      }
    }

    if (tmmWatchedMovies.size() == 0) {
      LOGGER.info("no new watched movies for Trakt sync found.");
      return;
    }

    LOGGER.debug("prepare " + tmmWatchedMovies.size() + " movies for Trakt.tv sync");
    List<SeenMovie> seenMovies = new ArrayList<SeenMovie>(); // array for ALL TMM movies
    int nosync = 0;
    for (Movie tmmMovie : tmmWatchedMovies) {
      if (tmmMovie.getImdbId().isEmpty() && tmmMovie.getTmdbId() == 0) {
        // do not add to Trakt if we have no IDs
        nosync++;
        continue;
      }
      SeenMovie seen = new SeenMovie(tmmMovie.getImdbId());
      seen.title = tmmMovie.getTitle();
      seen.imdb_id = tmmMovie.getImdbId();
      seen.tmdb_id = tmmMovie.getTmdbId();
      seen.year = Integer.valueOf(tmmMovie.getYear());
      seenMovies.add(seen); // add to lib
    }
    if (nosync > 0) {
      LOGGER.debug("skipping " + nosync + " movies, because they have not been scraped yet!");
    }

    if (seenMovies.size() == 0) {
      LOGGER.info("no new watched movies for Trakt sync found.");
      return;
    }

    try {
      LOGGER.info("Marking " + seenMovies.size() + " movies as 'seen' to Trakt.tv collection");
      response = trakt.movieService().seen(new Movies(seenMovies));
      LOGGER.info("Trakt mark-as-watched status:");
      printStatus(response);
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
    }
  }

  /**
   * gets ALL watched TvShows from Trakt, and sets the "watched" flag on TMM show/episodes (if TvDB matches)
   */
  public void updatedWatchedTvShowsFromTrakt() {
    if (!isEnabled()) {
      return;
    }

    // Call user/library/shows/collection and make sure to send min for the extended parameter.
    // Using the data from step 1, compare this to the local collection. If nothing new, no action needed.
    // If new episodes are found, call show/episode/library for each show to tell trakt its in the collection.

    TvShowList tvShowList = TvShowList.getInstance();
    List<TvShow> tmmShows = tvShowList.getTvShows();

    // get all Trakt watched TvShows
    List<com.jakewharton.trakt.entities.TvShow> traktShows;
    try {
      traktShows = trakt.userService().libraryShowsWatched(userName, Extended.MIN);
      // Extended.DEFAULT adds url, poster, fanart, banner, genres
      // Extended.MAX adds certs, runtime, and other stuff (useful for scraper!)
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }

    // loop over all watched shows on trakt
    for (com.jakewharton.trakt.entities.TvShow watched : traktShows) {

      // loop over TMM shows, and check if TvDB match
      for (TvShow tmmShow : tmmShows) {
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
          for (com.jakewharton.trakt.entities.TvShowEpisode ep : watched.episodes) {
            // loop over TMM episodes, and check if season/episode number match
            for (TvShowEpisode tmmEp : tmmShow.getEpisodes()) {
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
   * prints some trakt status
   * 
   * @param reponse
   *          the reponse
   */
  private void printStatus(ActionResponse reponse) {
    if (response != null) {
      LOGGER.info("Status           : " + response.status);
      if (!response.status.equals(Status.SUCCESS)) {
        LOGGER.error("Error            : " + response.error);
        LOGGER.error("Message          : " + response.message);
      }
      LOGGER.info("Inserted         : " + response.inserted);
      LOGGER.info("Already inserted : " + response.already_exist);
      LOGGER.info("Skipped          : " + response.skipped);
    }
  }

  /**
   * handles the retrofit errors<br>
   * (which is always thrown when http status != 200)<br>
   * and print some error msg
   * 
   * @param re
   *          the Retrofit error
   */
  private void handleRetrofitError(RetrofitError re) {
    Response r = re.getResponse();
    String msg = "";
    if (r != null) {
      msg = r.getReason();
      if (r.getBody() != null) {
        try {
          InputStream in = r.getBody().in();
          msg += " - " + IOUtils.toString(in, "UTF-8");
          in.close();
        }
        catch (IOException e1) {
          LOGGER.warn("IOException on Trakt error", e1);
        }
      }
    }
    else {
      msg = re.getMessage();
    }
    LOGGER.error("Trakt error (wrong settings?) " + msg);
    MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, msg, "Settings.trakttv"));
  }

  /**
   * Maps scraper Genres to internal TMM genres
   */
  private MediaGenres getTmmGenre(String genre) {
    MediaGenres g = null;
    if (genre == null || genre.isEmpty()) {
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
