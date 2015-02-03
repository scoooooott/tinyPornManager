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
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;

import retrofit.RetrofitError;
import retrofit.client.Response;

import com.uwetrottmann.trakt.v2.TraktV2;
import com.uwetrottmann.trakt.v2.entities.BaseEpisode;
import com.uwetrottmann.trakt.v2.entities.BaseMovie;
import com.uwetrottmann.trakt.v2.entities.BaseSeason;
import com.uwetrottmann.trakt.v2.entities.BaseShow;
import com.uwetrottmann.trakt.v2.entities.MovieIds;
import com.uwetrottmann.trakt.v2.entities.ShowIds;
import com.uwetrottmann.trakt.v2.entities.SyncEpisode;
import com.uwetrottmann.trakt.v2.entities.SyncErrors;
import com.uwetrottmann.trakt.v2.entities.SyncItems;
import com.uwetrottmann.trakt.v2.entities.SyncMovie;
import com.uwetrottmann.trakt.v2.entities.SyncResponse;
import com.uwetrottmann.trakt.v2.entities.SyncSeason;
import com.uwetrottmann.trakt.v2.entities.SyncShow;
import com.uwetrottmann.trakt.v2.entities.SyncStats;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.exceptions.OAuthUnauthorizedException;

/**
 * Sync your collection and watched status with Trakt.tv<br>
 * Using best practice 2-way-sync according to http://trakt.tv/api-docs/sync<br>
 * https://github.com/UweTrottmann/trakt-java
 * 
 * @author Myron Boyle
 * 
 */
public class TraktTv {
  private static final String  CLIENT_ID     = "a8e7e30fd7fd3f397b6e079f9f023e790f9cbd80a2be57c104089174fa8c6d89";
  private static final String  CLIENT_SECRET = "ab297a186a44a374c91ade21b9b76a7709c6411bf5bab8c9480ef4a3488426b1";
  private static final String  REDIRECT_URI  = "urn:ietf:wg:oauth:2.0:oob";

  private static final Logger  LOGGER        = LoggerFactory.getLogger(TraktTv.class);
  private static final TraktV2 TRAKT         = new TraktV2();

  private SyncResponse         response;
  private String               authToken;

  /**
   * gets a new Trakt object with custom values (user / passAsSHA1)
   */
  public TraktTv(String authToken) {
    this.authToken = authToken;

    TRAKT.setApiKey(CLIENT_ID);
    TRAKT.setAccessToken(authToken);

    if (LOGGER.isTraceEnabled()) {
      // when we are on TRACE, show some Trakt debug settings...
      TRAKT.setIsDebug(true);
    }
  }

  /**
   * retrieve the url for the authorization
   * 
   * @param username
   * @return
   * @throws Exception
   */
  public static String getAccessTokenRequestUrl(String username) throws Exception {
    String sampleState = new BigInteger(130, new SecureRandom()).toString(32);
    OAuthClientRequest request = TraktV2.getAuthorizationRequest(CLIENT_ID, REDIRECT_URI, sampleState, username);

    if (request == null || !request.getLocationUri().startsWith(TraktV2.OAUTH2_AUTHORIZATION_URL)) {
      return "";
    }

    LOGGER.debug("got authorization request url: " + request.getLocationUri());
    return request.getLocationUri();
  }

  /**
   * get the access token
   * 
   * @param authCode
   * @return
   * @throws Exception
   */
  public static String getAccessToken(String authCode) throws Exception {
    if (StringUtils.isBlank(authCode)) {
      return "";
    }

    OAuthAccessTokenResponse response = TraktV2.getAccessToken(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, authCode);
    LOGGER.debug("Retrieved access token: " + response.getAccessToken());
    return response.getAccessToken();
  }

  /**
   * do we have values for user/pass/api and are we a donator?!
   * 
   * @return true/false if trakt could be called
   */
  private boolean isEnabled() {
    if (StringUtils.isBlank(authToken)) {
      LOGGER.warn("Can't spawn TRAKT.TV - Settings empty.");
      return false;
    }
    if (!Globals.isDonator()) {
      LOGGER.warn("Won't spawn TRAKT.TV since you are not a donator!");
      return false;
    }
    return true;
  }

  /**
   * gets the underlying Trakt Java Object<br>
   * 
   * @return Trakt()
   */
  public final TraktV2 getManager() {
    return TRAKT;
  }

  /**
   * Syncs Trakt.tv collection (specified movies)<br>
   * Gets all Trakt movies from collection, matches them to ours, and sends ONLY the new ones back to Trakt
   */
  public void syncTraktMovieCollection(List<Movie> tmmMovies) {
    if (!isEnabled()) {
      return;
    }
    // *****************************************************************************
    // 1) get diff of TMM <-> Trakt collection
    // *****************************************************************************
    LOGGER.info("got " + tmmMovies.size() + " movies for Trakt.tv collection sync");

    // get ALL Trakt movies in collection
    List<BaseMovie> traktMovies = new ArrayList<BaseMovie>();

    try {
      // traktMovies = TRAKT.users().collectionMovies(userName, Extended.DEFAULT_MIN); // ???
      traktMovies = TRAKT.sync().collectionMovies(Extended.DEFAULT_MIN);
      LOGGER.info("You have " + traktMovies.size() + " movies in your Trakt.tv collection");
      // Extended.DEFAULT adds url, poster, fanart, banner, genres
      // Extended.MAX adds certs, runtime, and other stuff (useful for scraper!)
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }

    // loop over all movies on trakt
    for (BaseMovie traktMovie : traktMovies) {
      // loop over TMM movies, and check if IMDBID match
      for (int i = tmmMovies.size() - 1; i >= 0; i--) {
        Movie tmmMovie = tmmMovies.get(i);
        if ((StringUtils.isNotEmpty(traktMovie.movie.ids.imdb) && traktMovie.movie.ids.imdb.equals(tmmMovie.getImdbId()))
            || (traktMovie.movie.ids.tmdb != 0 && traktMovie.movie.ids.tmdb == tmmMovie.getTmdbId())
            || (traktMovie.movie.ids.trakt != 0 && traktMovie.movie.ids.trakt == tmmMovie.getTraktId())) {
          // we have a movie match

          boolean dirty = false;
          // update missing IDs (we get them for free :)
          if (tmmMovie.getImdbId().isEmpty() && !StringUtils.isEmpty(traktMovie.movie.ids.imdb)) {
            tmmMovie.setImdbId(traktMovie.movie.ids.imdb);
            dirty = true;
          }
          if (tmmMovie.getTmdbId() == 0 && traktMovie.movie.ids.tmdb != null && traktMovie.movie.ids.tmdb != 0) {
            tmmMovie.setTmdbId((int) traktMovie.movie.ids.tmdb);
            dirty = true;
          }
          if (tmmMovie.getTraktId() == 0 && traktMovie.movie.ids.trakt != null && traktMovie.movie.ids.trakt != 0) {
            tmmMovie.setTraktId((int) traktMovie.movie.ids.trakt);
            dirty = true;
          }
          if (dirty) {
            tmmMovie.saveToDb();
          }

          // remove it from our list (no need to add)
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
    LOGGER.debug("prepare " + tmmMovies.size() + " movies for Trakt.tv collection sync");

    List<SyncMovie> movies = new ArrayList<SyncMovie>();
    int nosync = 0;
    for (Movie tmmMovie : tmmMovies) {
      if (tmmMovie.getImdbId().isEmpty() && tmmMovie.getTmdbId() == 0) {
        // do not add to Trakt if we have no IDs
        nosync++;
        continue;
      }
      movies.add(toSyncMovie(tmmMovie));
    }
    if (nosync > 0) {
      LOGGER.debug("skipping " + nosync + " movies, because they have not been scraped yet!");
    }

    if (movies.size() == 0) {
      LOGGER.info("no new movies for Trakt collection sync found.");
      return;
    }

    try {
      LOGGER.info("Adding " + movies.size() + " movies to Trakt.tv collection");
      SyncItems items = new SyncItems().movies(movies);
      response = TRAKT.sync().addItemsToCollection(items);

      LOGGER.info("Trakt add-to-library status:");
      printStatus(response);
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Syncs Trakt.tv collection (all movies you have)<br>
   * Gets all Trakt movies from collection, matches them to ours, and sends ONLY the new ones back to Trakt
   */
  public void syncTraktMovieCollection() {
    if (!isEnabled()) {
      return;
    }

    syncTraktMovieCollection(new ArrayList<Movie>(MovieList.getInstance().getMovies()));
  }

  /**
   * clears the whole Trakt.tv movie collection. Gets all Trakt.tv movies from your collection and removes them from the collection and the watched
   * state; a little helper to initialize the collection
   */
  public void clearTraktMovies() {
    // *****************************************************************************
    // 1) get ALL Trakt movies in collection / watched
    // *****************************************************************************
    List<BaseMovie> traktCollection = new ArrayList<BaseMovie>();
    List<BaseMovie> traktWatched = new ArrayList<BaseMovie>();
    try {
      traktCollection = TRAKT.sync().collectionMovies(Extended.DEFAULT_MIN);
      LOGGER.info("You have " + traktCollection.size() + " movies in your Trakt.tv collection");
      traktCollection = TRAKT.sync().watchedMovies(Extended.DEFAULT_MIN);
      LOGGER.info("You have " + traktCollection.size() + " movies watched");
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }

    // *****************************************************************************
    // 2) remove every movie from the COLLECTION state
    // *****************************************************************************
    List<SyncMovie> movieToRemove = new ArrayList<SyncMovie>();
    for (BaseMovie traktMovie : traktCollection) {
      movieToRemove.add(toSyncMovie(traktMovie));
    }
    if (!movieToRemove.isEmpty()) {
      try {
        SyncItems items = new SyncItems().movies(movieToRemove);
        TRAKT.sync().deleteItemsFromCollection(items);
        LOGGER.info("removed " + movieToRemove.size() + " movies from your trakt.tv collection");
      }
      catch (RetrofitError e) {
        handleRetrofitError(e);
        return;
      }
      catch (OAuthUnauthorizedException e) {
        e.printStackTrace();
      }
    }

    // *****************************************************************************
    // 3) remove every movie from the WATCHED state
    // *****************************************************************************
    movieToRemove.clear();
    for (BaseMovie traktMovie : traktWatched) {
      movieToRemove.add(toSyncMovie(traktMovie));
    }
    if (!movieToRemove.isEmpty()) {
      try {
        SyncItems items = new SyncItems().movies(movieToRemove);
        TRAKT.sync().deleteItemsFromWatchedHistory(items);
        LOGGER.info("removed " + movieToRemove.size() + " movies from your trakt.tv watched");
      }
      catch (RetrofitError e) {
        handleRetrofitError(e);
        return;
      }
      catch (OAuthUnauthorizedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Syncs Trakt.tv "seen" flag (all gives you have already marked as watched)<br>
   * Gets all watched movies from Trakt, and sets the "watched" flag on TMM movies.<br>
   * Then update the remaining TMM movies on Trakt as 'seen'.
   */
  public void syncTraktMovieWatched(List<Movie> tmmMovies) {
    if (!isEnabled()) {
      return;
    }

    // *****************************************************************************
    // 1) get all Trakt watched movies and update our "watched" status
    // *****************************************************************************
    List<BaseMovie> traktMovies = new ArrayList<BaseMovie>();
    try {
      traktMovies = TRAKT.sync().watchedMovies(Extended.DEFAULT_MIN);
      LOGGER.info("You have " + traktMovies.size() + " movies marked as 'watched' in your Trakt.tv collection");
      // Extended.DEFAULT adds url, poster, fanart, banner, genres
      // Extended.MAX adds certs, runtime, and other stuff (useful for scraper!)
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }

    // loop over all watched movies on trakt
    for (BaseMovie traktWatched : traktMovies) {

      // loop over TMM movies, and check if IMDBID match
      for (Movie tmmMovie : tmmMovies) {
        boolean dirty = false;
        if ((StringUtils.isNotEmpty(traktWatched.movie.ids.imdb) && traktWatched.movie.ids.imdb.equals(tmmMovie.getImdbId()))
            || (traktWatched.movie.ids.tmdb != 0 && traktWatched.movie.ids.tmdb == tmmMovie.getTmdbId())) {

          // update missing IDs (we get them for free :)
          if (tmmMovie.getImdbId().isEmpty() && !StringUtils.isEmpty(traktWatched.movie.ids.imdb)) {
            tmmMovie.setImdbId(traktWatched.movie.ids.imdb);
            dirty = true;
          }
          if (tmmMovie.getTmdbId() == 0 && traktWatched.movie.ids.tmdb != null && traktWatched.movie.ids.tmdb != 0) {
            tmmMovie.setTmdbId((int) traktWatched.movie.ids.tmdb);
            dirty = true;
          }
          if (tmmMovie.getTraktId() == 0) { // TODO: TEST and get/set
            tmmMovie.setTraktId((int) traktWatched.movie.ids.trakt);
            dirty = true;
          }

          if (!tmmMovie.isWatched()) {
            // save Trakt watched status
            LOGGER.info("Marking movie '" + tmmMovie.getTitle() + "' as watched");
            tmmMovie.setWatched(true);
            dirty = true;
          }

          if (dirty) {
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
      for (BaseMovie traktWatched : traktMovies) {
        if ((StringUtils.isNotEmpty(traktWatched.movie.ids.imdb) && traktWatched.movie.ids.imdb.equals(tmmWatchedMovies.get(i).getImdbId()))
            || (traktWatched.movie.ids.tmdb != 0 && traktWatched.movie.ids.tmdb == tmmWatchedMovies.get(i).getTmdbId())) {
          tmmWatchedMovies.remove(i);
          break;
        }
      }
    }

    if (tmmWatchedMovies.size() == 0) {
      LOGGER.info("no new watched movies for Trakt sync found.");
      return;
    }

    LOGGER.debug("prepare " + tmmWatchedMovies.size() + " movies for Trakt.tv sync");
    List<SyncMovie> movies = new ArrayList<SyncMovie>();
    int nosync = 0;
    for (Movie tmmMovie : tmmWatchedMovies) {
      if (tmmMovie.getImdbId().isEmpty() && tmmMovie.getTmdbId() == 0) {
        // do not add to Trakt if we have no IDs
        nosync++;
        continue;
      }
      movies.add(toSyncMovie(tmmMovie)); // add to lib
    }
    if (nosync > 0) {
      LOGGER.debug("skipping " + nosync + " movies, because they have not been scraped yet!");
    }

    if (movies.size() == 0) {
      LOGGER.info("no new watched movies for Trakt sync found.");
      return;
    }

    try {
      LOGGER.info("Marking " + movies.size() + " movies as 'watched' to Trakt.tv collection");
      SyncItems items = new SyncItems().movies(movies);
      response = TRAKT.sync().addItemsToWatchedHistory(items);
      LOGGER.info("Trakt mark-as-watched status:");
      printStatus(response);
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
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
    syncTraktMovieWatched(MovieList.getInstance().getMovies());
  }

  // /**
  // * simple class to ease diff
  // *
  // * @author Myron Boyle
  // *
  // */
  // private class SimpleShow {
  // private String title = "";
  // private int year = 0;
  // private int tvdb = 0;
  // private int trakt = 0;
  // private List<Pair<Integer, Integer>> collection = new ArrayList<Pair<Integer, Integer>>(); // season/episode
  // private List<Pair<Integer, Integer>> watched = new ArrayList<Pair<Integer, Integer>>(); // season/episode
  //
  // public SimpleShow(TvShow show) {
  // this.title = show.getTitle();
  // if (!show.getYear().isEmpty()) {
  // try {
  // this.year = Integer.valueOf(show.getYear());
  // }
  // catch (Exception e) {
  // this.year = 0;
  // }
  // }
  // if (!show.getTvdbId().isEmpty()) {
  // try {
  // this.tvdb = Integer.valueOf(show.getTvdbId());
  // }
  // catch (Exception e) {
  // this.tvdb = 0;
  // }
  // }
  // if (show.getTraktId() != 0) {
  // try {
  // this.trakt = Integer.valueOf(show.getTraktId());
  // }
  // catch (Exception e) {
  // this.trakt = 0;
  // }
  // }
  // this.collection = buildSeasonEpArray(show, false);
  // this.watched = buildSeasonEpArray(show, true);
  // }
  // }
  //
  // /**
  // * Helper function to build simple list of all to-update seasons/episodes from tvshow
  // *
  // * @param show
  // * the tvshow
  // * @param watched
  // * only watched, or all?
  // * @return a list of tupels of season/episode pairs
  // */
  // private synchronized List<Pair<Integer, Integer>> buildSeasonEpArray(TvShow show, boolean watched) {
  // List<Pair<Integer, Integer>> tv = new ArrayList<Pair<Integer, Integer>>();
  // for (TvShowEpisode ep : show.getEpisodes()) {
  // if (watched) {
  // // add only watched
  // if (ep.isWatched()) {
  // tv.add(new Pair<Integer, Integer>(ep.getSeason(), ep.getEpisode()));
  // }
  // }
  // else {
  // // add all
  // tv.add(new Pair<Integer, Integer>(ep.getSeason(), ep.getEpisode()));
  // }
  // }
  // return tv;
  // }

  /**
   * Syncs Trakt.tv collection (add all TMM shows to Trakt)<br>
   * Syncs watched status from Trakt, and sends back the COMPLETE watched status<br>
   * Do not send diffs, since this is too complicated currently :|
   */
  public void syncTraktTvShows(List<TvShow> tvShows) {
    if (!isEnabled()) {
      return;
    }

    // *****************************************************************************
    // 1) add all our shows to Trakt collection (we have the physical file)
    // *****************************************************************************
    List<SyncShow> tmmShows = new ArrayList<SyncShow>();
    for (TvShow show : tvShows) {
      tmmShows.add(toSyncShow(show));
    }

    try {
      LOGGER.info("Adding " + tmmShows.size() + " TvShows to Trakt.tv collection");
      SyncItems items = new SyncItems().shows(tmmShows);
      response = TRAKT.sync().addItemsToCollection(items);

      LOGGER.info("Trakt add-to-library status:");
      printStatus(response);
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }

    // *****************************************************************************
    // 2) sync back all the missing show IDs (always good to have ;)
    // *****************************************************************************
    List<BaseShow> traktShows = new ArrayList<BaseShow>();
    try {
      traktShows = TRAKT.sync().collectionShows(Extended.DEFAULT_MIN);
      LOGGER.info("You have now " + traktShows.size() + " TvShows in your Trakt.tv collection");
      for (BaseShow traktShow : traktShows) {
        for (TvShow tmmShow : tvShows) {
          if ((traktShow.show.ids.tvdb != null && traktShow.show.ids.tvdb != 0 && traktShow.show.ids.tvdb.equals(tmmShow.getTvdbId()))
              || (traktShow.show.ids.trakt != null && traktShow.show.ids.trakt.equals(tmmShow.getTraktId()))
              || (traktShow.show.ids.tvrage != null && traktShow.show.ids.tvrage.equals(tmmShow.getTvRageId()))) {
            // ok, we have a show match
            boolean dirty = false;

            // update show IDs
            if (tmmShow.getImdbId().isEmpty() && !StringUtils.isEmpty(traktShow.show.ids.imdb)) {
              tmmShow.setImdbId(traktShow.show.ids.imdb);
              dirty = true;
            }
            if (tmmShow.getTvdbId().isEmpty() && traktShow.show.ids.tvdb != null && traktShow.show.ids.tvdb != 0) {
              tmmShow.setTvdbId(String.valueOf(traktShow.show.ids.tvdb));
              dirty = true;
            }
            if (tmmShow.getTvRageId() == 0 && traktShow.show.ids.tvrage != null && traktShow.show.ids.tvrage != 0) {
              tmmShow.setTvRageId(traktShow.show.ids.tvrage);
              dirty = true;
            }
            if (tmmShow.getTraktId() == 0 && traktShow.show.ids.trakt != null && traktShow.show.ids.trakt != 0) {
              tmmShow.setTraktId((int) traktShow.show.ids.trakt);
              dirty = true;
            }
            if (dirty) {
              tmmShow.saveToDb();
            }

            // NOT YET POSSIBLE IN LIB TO GET IDS FOR EPISODES
            // // update episode IDs (needed for sync!)
            // for (BaseSeason bs : traktShow.seasons) {
            // for (BaseEpisode be : bs.episodes) {
            // TvShowEpisode tmmEP = tmmShow.getEpisode(bs.number, be.number);
            //
            // if (tmmEP.getTraktId() == 0 && be.show.ids.trakt != null && be.show.ids.trakt != 0) {
            // tmmEP.setTraktId((int) be.show.ids.trakt);
            // dirty = true;
            // }
            //
            // }
            // }

          }
        }
      }
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }

    // *****************************************************************************
    // 3) get all the Trakt watched shows/seasons/episodes and update ours
    // *****************************************************************************

    // *****************************************************************************
    // 4) send all TMM watched shows/seasons/episodes back to Trakt
    // *****************************************************************************

  }

  /**
   * Syncs Trakt.tv collection (all given TvShows)<br>
   * Gets all Trakt shows/episodes from collection, matches them to ours, and sends ONLY the new ones back to Trakt
   */
  public void syncTraktTvShowCollectionOld(List<TvShow> tvShows) {
    if (!isEnabled()) {
      return;
    }

    // our simple list of TMM shows
    List<SyncShow> tmmShows = new ArrayList<SyncShow>();
    for (TvShow show : tvShows) {
      tmmShows.add(toSyncShow(show));
    }

    // *****************************************************************************
    // 1) get all Trakt TvShows/episodes in collection remove from our temp array
    // *****************************************************************************
    List<BaseShow> traktShows = new ArrayList<BaseShow>();
    try {
      traktShows = TRAKT.sync().collectionShows(Extended.DEFAULT_MIN);
      LOGGER.info("You have " + traktShows.size() + " TvShows in your Trakt.tv collection");
      // Extended.DEFAULT adds url, poster, fanart, banner, genres
      // Extended.MAX adds certs, runtime, and other stuff (useful for scraper!)
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }

    // // loop over all watched shows on trakt
    // for (BaseShow traktShow : traktShows) {
    //
    // // loop over TMM shows, and check if TvDB match
    // for (int i = tmmShows.size() - 1; i >= 0; i--) {
    // SimpleShow tmmShow = tmmShows.get(i);
    // // boolean dirty = false;
    // if ((traktShow.show.ids.tvdb != null && traktShow.show.ids.tvdb != 0 && traktShow.show.ids.tvdb.equals(tmmShow.tvdb))
    // || traktShow.show.ids.trakt != null && traktShow.show.ids.trakt.equals(tmmShow.trakt)) {
    //
    // // shows matches, so remove episodes already in tmm
    // for (BaseSeason traktSeason : traktShow.seasons) {
    // for (BaseEpisode traktEp : traktSeason.episodes) {
    //
    // Pair<Integer, Integer> p = new Pair<Integer, Integer>(traktSeason.number, traktEp.number);
    // if (tmmShow.collection.contains(p)) {
    // // FIXME: use size() - 1; i >= 0; i-- loop?
    // tmmShow.collection.remove(p);
    // }
    // }
    // }
    //
    // if (tmmShow.collection.size() == 0) {
    // LOGGER.debug("all Episodes already on Trakt - removing show '" + tmmShow.title + "' from update");
    // tmmShows.remove(i);
    // }
    //
    // } // end tvdb_id matches
    // } // end loop tmmShows
    // } // end loop traktShow
    //
    // if (tmmShows.size() == 0) {
    // LOGGER.info("no new TvShows for Trakt collection sync found.");
    // return;
    // }
    //
    // // *****************************************************************************
    // // 2) add additionally shows/episodes to your collection
    // // *****************************************************************************
    // LOGGER.debug("prepare " + tmmShows.size() + " TvShows for Trakt.tv collection sync");
    // for (SimpleShow tmmShow : tmmShows) {
    //
    // List<SyncShow> syncShows = new ArrayList<SyncShow>();
    // SyncShow ss = new SyncShow();
    //
    // // add episodes
    // List<com.jakewharton.trakt.services.ShowService.Episodes.Episode> traktEpList = new
    // ArrayList<com.jakewharton.trakt.services.ShowService.Episodes.Episode>();
    // for (Pair<Integer, Integer> p : tmmShow.collection) {
    // traktEpList.add(new com.jakewharton.trakt.services.ShowService.Episodes.Episode(p.first(), p.second()));
    // }
    //
    // ShowService.Episodes traktObj = new ShowService.Episodes(tmmShow.tvdb, traktEpList);
    // traktObj.title = tmmShow.title;
    // traktObj.year = tmmShow.year;
    //
    // // phew - we have now our not-yet-in-trakt array, lets do the update :)
    // try {
    // LOGGER.info("Adding " + traktEpList.size() + " episodes of show '" + tmmShow.title + "' to Trakt.tv collection");
    // com.jakewharton.trakt.entities.Response response = TRAKT.showService().episodeLibrary(traktObj);
    // printStatus(response);
    // }
    // catch (RetrofitError e) {
    // handleRetrofitError(e);
    // }
    // catch (OAuthUnauthorizedException e) {
    // e.printStackTrace();
    // }
    // } // end show loop
  }

  /**
   * Syncs Trakt.tv collection (all TvShows you have)<br>
   * Gets all Trakt shows/episodes from collection, matches them to ours, and sends ONLY the new ones back to Trakt
   */
  public void syncTraktTvShowCollection() {
    if (!isEnabled()) {
      return;
    }

    syncTraktTvShows(new ArrayList<TvShow>(TvShowList.getInstance().getTvShows()));
  }

  /**
   * clears the whole Trakt.tv movie collection. Gets all Trakt.tv movies from your collection and removes them from the collection and the watched
   * state; a little helper to initialize the collection
   */
  public void clearTraktTvShows() {
    // *****************************************************************************
    // 1) get ALL Trakt shows in collection / watched
    // *****************************************************************************
    List<BaseShow> traktCollection = new ArrayList<BaseShow>();
    List<BaseShow> traktWatched = new ArrayList<BaseShow>();
    try {
      traktCollection = TRAKT.sync().collectionShows(Extended.DEFAULT_MIN);
      LOGGER.info("You have " + traktCollection.size() + " shows in your Trakt.tv collection");
      traktCollection = TRAKT.sync().watchedShows(Extended.DEFAULT_MIN);
      LOGGER.info("You have " + traktCollection.size() + " shows watched");
    }
    catch (RetrofitError e) {
      handleRetrofitError(e);
      return;
    }
    catch (OAuthUnauthorizedException e) {
      e.printStackTrace();
    }

    // *****************************************************************************
    // 2) remove every shows from the COLLECTION state
    // *****************************************************************************
    List<SyncShow> showToRemove = new ArrayList<SyncShow>();
    for (BaseShow traktShow : traktCollection) {
      showToRemove.add(toSyncShow(traktShow));
    }
    if (!showToRemove.isEmpty()) {
      try {
        SyncItems items = new SyncItems().shows(showToRemove);
        TRAKT.sync().deleteItemsFromCollection(items);
        LOGGER.info("removed " + showToRemove.size() + " shows from your trakt.tv collection");
      }
      catch (RetrofitError e) {
        handleRetrofitError(e);
        return;
      }
      catch (OAuthUnauthorizedException e) {
        e.printStackTrace();
      }
    }

    // *****************************************************************************
    // 3) remove every shows from the WATCHED state
    // *****************************************************************************
    showToRemove.clear();
    for (BaseShow traktShow : traktWatched) {
      showToRemove.add(toSyncShow(traktShow));
    }
    if (!showToRemove.isEmpty()) {
      try {
        SyncItems items = new SyncItems().shows(showToRemove);
        TRAKT.sync().deleteItemsFromWatchedHistory(items);
        LOGGER.info("removed " + showToRemove.size() + " shows from your trakt.tv watched");
      }
      catch (RetrofitError e) {
        handleRetrofitError(e);
        return;
      }
      catch (OAuthUnauthorizedException e) {
        e.printStackTrace();
      }
    }
  }

  // *****************************
  // HELPER METHODS
  // *****************************

  private SyncMovie toSyncMovie(Movie tmmMovie) {
    // MovieIds
    MovieIds ids = new MovieIds();
    ids.imdb = tmmMovie.getImdbId();
    if (tmmMovie.getTmdbId() != 0) {
      ids.tmdb = tmmMovie.getTmdbId();
    }
    if (tmmMovie.getTraktId() != 0) {
      ids.trakt = tmmMovie.getTraktId();
    }

    SyncMovie movie = new SyncMovie().id(ids).collectedAt(new DateTime(tmmMovie.getDateAdded()));
    return movie;
  }

  private SyncMovie toSyncMovie(BaseMovie baseMovie) {
    SyncMovie movie = new SyncMovie().id(baseMovie.movie.ids).collectedAt(baseMovie.collected_at).watchedAt(baseMovie.last_watched_at);
    return movie;
  }

  private SyncShow toSyncShow(TvShow tmmShow) {
    // ShowIds
    ShowIds ids = new ShowIds();
    ids.imdb = tmmShow.getImdbId();
    try {
      ids.tvdb = Integer.valueOf(tmmShow.getTvdbId());
    }
    catch (Exception e) {
    }
    if (tmmShow.getTraktId() != 0) {
      ids.trakt = tmmShow.getTraktId();
    }
    if (tmmShow.getTvRageId() != 0) {
      ids.tvrage = tmmShow.getTvRageId();
    }

    ArrayList<SyncSeason> ss = new ArrayList<SyncSeason>();
    for (TvShowSeason tmmSeason : tmmShow.getSeasons()) {
      ArrayList<SyncEpisode> se = new ArrayList<SyncEpisode>();
      for (TvShowEpisode tmmEp : tmmSeason.getEpisodes()) {
        se.add(new SyncEpisode().number(tmmEp.getEpisode()).collectedAt(new DateTime(tmmEp.getDateAdded())));
      }
      ss.add(new SyncSeason().number(tmmSeason.getSeason()).episodes(se));
    }

    SyncShow show = new SyncShow().id(ids).collectedAt(new DateTime(tmmShow.getDateAdded())).seasons(ss);
    return show;
  }

  private SyncShow toSyncShow(BaseShow baseShow) {
    ArrayList<SyncSeason> ss = new ArrayList<SyncSeason>();
    for (BaseSeason baseSeason : baseShow.seasons) {
      ArrayList<SyncEpisode> se = new ArrayList<SyncEpisode>();
      for (BaseEpisode baseEp : baseSeason.episodes) {
        se.add(new SyncEpisode().number(baseEp.number));
      }
      ss.add(new SyncSeason().number(baseSeason.number).episodes(se));
    }
    SyncShow show = new SyncShow().id(baseShow.show.ids).collectedAt(new DateTime(baseShow.collected_at)).seasons(ss);
    return show;
  }

  /**
   * prints some trakt response status
   * 
   * @param reponse
   *          the reponse
   */
  private void printStatus(SyncResponse resp) {
    if (resp != null) {
      if (resp.added != null) {
        LOGGER.info("Added       : " + getStatusString(resp.added));
      }
      if (resp.existing != null) {
        LOGGER.info("Existing    : " + getStatusString(resp.existing));
      }
      if (resp.deleted != null) {
        LOGGER.info("Deleted     : " + getStatusString(resp.deleted));
      }
      if (resp.not_found != null) {
        LOGGER.error("Errors      : " + getStatusString(resp.not_found));
      }
    }
  }

  private String getStatusString(SyncStats ss) {
    StringBuilder sb = new StringBuilder(50);

    if (ss.movies != null && ss.movies > 0) {
      sb.append(ss.movies + " Movies ");
    }
    if (ss.shows != null && ss.shows > 0) {
      sb.append(ss.shows + " Shows ");
    }
    if (ss.seasons != null && ss.seasons > 0) {
      sb.append(ss.seasons + " Seasons ");
    }
    if (ss.episodes != null && ss.episodes > 0) {
      sb.append(ss.episodes + " Episodes");
    }

    return sb.toString();
  }

  private String getStatusString(SyncErrors ss) {
    StringBuilder sb = new StringBuilder(50);

    // TODO: iterate over error array and display which did not work
    if (ss.movies != null && ss.movies.size() > 0) {
      sb.append(ss.movies.size() + " Movies ");
    }
    if (ss.shows != null && ss.shows.size() > 0) {
      sb.append(ss.shows.size() + " Shows ");
    }
    if (ss.seasons != null && ss.seasons.size() > 0) {
      sb.append(ss.seasons.size() + " Seasons ");
    }
    if (ss.episodes != null && ss.episodes.size() > 0) {
      sb.append(ss.episodes.size() + " Episodes");
    }

    return sb.toString();
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
    // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, msg, "Settings.trakttv"));
  }

  // /**
  // * Maps scraper Genres to internal TMM genres
  // */
  // private MediaGenres getTmmGenre(String genre) {
  // MediaGenres g = null;
  // if (genre == null || genre.isEmpty()) {
  // return g;
  // }
//    // @formatter:off
//    else if (genre.equals("Action"))           { g = MediaGenres.ACTION; }
//    else if (genre.equals("Adventure"))        { g = MediaGenres.ADVENTURE; }
//    else if (genre.equals("Animation"))        { g = MediaGenres.ANIMATION; }
//    else if (genre.equals("Comedy"))           { g = MediaGenres.COMEDY; }
//    else if (genre.equals("Children"))         { g = MediaGenres.FAMILY; }
//    else if (genre.equals("Crime"))            { g = MediaGenres.CRIME; }
//    else if (genre.equals("Documentary"))      { g = MediaGenres.DOCUMENTARY; }
//    else if (genre.equals("Drama"))            { g = MediaGenres.DRAMA; }
//    else if (genre.equals("Family"))           { g = MediaGenres.FAMILY; }
//    else if (genre.equals("Fantasy"))          { g = MediaGenres.FANTASY; }
//    else if (genre.equals("Film Noir"))        { g = MediaGenres.FILM_NOIR; }
//    else if (genre.equals("History"))          { g = MediaGenres.HISTORY; }
//    else if (genre.equals("Game Show"))        { g = MediaGenres.GAME_SHOW; }
//    else if (genre.equals("Home and Garden"))  { g = MediaGenres.DOCUMENTARY; }
//    else if (genre.equals("Horror"))           { g = MediaGenres.HORROR; }
//    else if (genre.equals("Indie"))            { g = MediaGenres.INDIE; }
//    else if (genre.equals("Music"))            { g = MediaGenres.MUSIC; }
//    else if (genre.equals("Mini Series"))      { g = MediaGenres.SERIES; }
//    else if (genre.equals("Musical"))          { g = MediaGenres.MUSICAL; }
//    else if (genre.equals("Mystery"))          { g = MediaGenres.MYSTERY; }
//    else if (genre.equals("News"))             { g = MediaGenres.NEWS; }
//    else if (genre.equals("Reality"))          { g = MediaGenres.REALITY_TV; }
//    else if (genre.equals("Romance"))          { g = MediaGenres.ROMANCE; }
//    else if (genre.equals("Science Fiction"))  { g = MediaGenres.SCIENCE_FICTION; }
//    else if (genre.equals("Sport"))            { g = MediaGenres.SPORT; }
//    else if (genre.equals("Special Interest")) { g = MediaGenres.INDIE; }
//    else if (genre.equals("Soap"))             { g = MediaGenres.SERIES; }
//    else if (genre.equals("Suspense"))         { g = MediaGenres.SUSPENSE; }
//    else if (genre.equals("Talk Show"))        { g = MediaGenres.TALK_SHOW; }
//    else if (genre.equals("Thriller"))         { g = MediaGenres.THRILLER; }
//    else if (genre.equals("War"))              { g = MediaGenres.WAR; }
//    else if (genre.equals("Western"))          { g = MediaGenres.WESTERN; }
//    else if (genre.equals("No Genre"))         { return null; }
//    // @formatter:on
  // if (g == null) {
  // g = MediaGenres.getGenre(genre);
  // }
  // return g;
  // }
}
