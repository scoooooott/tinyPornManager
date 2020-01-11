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
package org.tinymediamanager.scraper.trakt;

import static org.tinymediamanager.scraper.MediaMetadata.TMDB;
import static org.tinymediamanager.scraper.MediaMetadata.TVDB;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.interfaces.IMovieImdbMetadataProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.MetadataUtil;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.TraktV2Interceptor;
import com.uwetrottmann.trakt5.entities.SearchResult;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.IdType;
import com.uwetrottmann.trakt5.enums.Type;

import okhttp3.OkHttpClient;
import retrofit2.Response;

public class TraktMetadataProvider implements IMovieMetadataProvider, ITvShowMetadataProvider, IMovieImdbMetadataProvider {
  public static final String     ID           = "trakt";

  private static final Logger    LOGGER       = LoggerFactory.getLogger(TraktMetadataProvider.class);
  private static final String    CLIENT_ID    = ApiKey
      .decryptApikey("Xd0t1yRY+HaxMl3bqILuxIaokXxekrFNj0QszCUsG6aNSbrhOhC2h5PcxDhV7wUXmBdOt9cYlMGNJjLZvKcS3xTRx3zYH7EYb7Mv5hCsMQU=");

  static final MediaProviderInfo providerInfo = createMediaProviderInfo();

  private static TraktV2         api;

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "Trakt.tv",
        "<html><h3>Trakt.tv</h3><br />Trakt.tv is a platform that does many things, but primarily keeps track of TV shows and movies you watch. It also provides meta data for movies and TV shows<br /><br />Available languages: EN</html>",
        TraktMetadataProvider.class.getResource("/org/tinymediamanager/scraper/trakt_tv.png"));

    providerInfo.getConfig().addText("clientId", "", true);
    providerInfo.getConfig().load();

    return providerInfo;
  }

  // thread safe initialization of the API
  private static synchronized void initAPI() {
    String apiKey = CLIENT_ID;
    String userApiKey = providerInfo.getConfig().getValue("clientId");

    // check if the API should change from current key to user key
    if (StringUtils.isNotBlank(userApiKey) && api != null && !userApiKey.equals(api.apiKey())) {
      api = null;
      apiKey = userApiKey;
    }

    // check if the API should change from current key to tmm key
    if (StringUtils.isBlank(userApiKey) && api != null && !CLIENT_ID.equals(api.apiKey())) {
      api = null;
      apiKey = CLIENT_ID;
    }

    // create a new instance of the trakt api
    if (api == null) {
      api = new TraktV2(apiKey) {
        // tell the trakt api to use our OkHttp client

        @Override
        protected synchronized OkHttpClient okHttpClient() {
          OkHttpClient.Builder builder = TmmHttpClient.newBuilder(true);
          builder.addInterceptor(new TraktV2Interceptor(this));
          return builder.build();
        }
      };
    }
  }

  // ProviderInfo
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
  }

  /**
   * Looks up a Trakt entity and provides a list of search results
   *
   * @param options
   *          the options to lookup with the id
   * @return any media search result
   */
  public List<MediaSearchResult> lookupWithId(MediaSearchAndScrapeOptions options) {
    List<SearchResult> results = new ArrayList<>();

    // get known IDs
    String imdbId = options.getImdbId().isEmpty() ? null : options.getImdbId();
    if (MetadataUtil.isValidImdbId(options.getSearchQuery())) {
      imdbId = options.getSearchQuery();
    }
    String traktId = options.getIdAsString(providerInfo.getId());
    String tmdbId = options.getIdAsString(TMDB);
    String tvdbId = options.getIdAsString(TVDB);
    String tvrageId = options.getIdAsString("tvrage");

    // derive trakt type from ours
    Type type = null;
    switch (options.getMediaType()) {
      case MOVIE:
        type = Type.MOVIE;
        break;
      case TV_SHOW:
        type = Type.SHOW;
        break;
      case TV_EPISODE:
        type = Type.EPISODE;
        break;
      default:
    }

    // lookup until one has been found
    results = lookupWithId(results, IdType.TRAKT, traktId, type);
    results = lookupWithId(results, IdType.IMDB, imdbId, type);
    results = lookupWithId(results, IdType.TMDB, tmdbId, type);
    results = lookupWithId(results, IdType.TVDB, tvdbId, type);
    results = lookupWithId(results, IdType.TVRAGE, tvrageId, type);

    List<MediaSearchResult> msr = new ArrayList<>();
    for (SearchResult sr : results) {
      MediaSearchResult m = TraktUtils.morphTraktResultToTmmResult(options, sr);
      m.setScore(1.0f); // ID lookup
      msr.add(m);
    }
    return msr;
  }

  private List<SearchResult> lookupWithId(List<SearchResult> results, IdType id, String value, Type type) {
    // lazy initialization of the api
    initAPI();

    if (results.isEmpty() && value != null) {
      LOGGER.debug("found {} id {} - direct lookup", id, value);
      try {
        Response<List<SearchResult>> response = api.search().idLookup(id, value, type, Extended.FULL, 1, 25).execute();
        if (!response.isSuccessful()) {
          LOGGER.warn("request was NOT successful: HTTP/{} - {}", response.code(), response.message());
          return results;
        }
        results = response.body();
        LOGGER.debug("Found {} result with ID lookup", results.size());
      }
      catch (Exception e) {
        LOGGER.warn("request was NOT successful: {}", e.getMessage());
      }
    }
    return results;
  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("search() - {}", options);

    // lazy initialization of the api
    initAPI();
    return new TraktMovieMetadataProvider(api).search(options);
  }

  @Override
  public SortedSet<MediaSearchResult> search(TvShowSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("search() - {}", options);

    // lazy initialization of the api
    initAPI();
    return new TraktTVShowMetadataProvider(api).search(options);
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    LOGGER.debug("getMetadata(): {}", options);

    // lazy initialization of the api
    initAPI();
    return new TraktMovieMetadataProvider(api).scrape(options);
  }

  @Override
  public MediaMetadata getMetadata(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    LOGGER.debug("getMetadata(): {}", options);

    // lazy initialization of the api
    initAPI();
    return new TraktTVShowMetadataProvider(api).getTvShowMetadata(options);
  }

  @Override
  public MediaMetadata getMetadata(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    LOGGER.debug("getMetadata(): {}", options);

    // lazy initialization of the api
    initAPI();
    return new TraktTVShowMetadataProvider(api).getEpisodeMetadata(options);
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    LOGGER.debug("getEpisodeList(): {}", options);

    // lazy initialization of the api
    initAPI();
    return new TraktTVShowMetadataProvider(api).getEpisodeList(options);
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    LOGGER.debug("getEpisodeList(): {}", options);

    // lazy initialization of the api
    initAPI();
    return new TraktTVShowMetadataProvider(api).getEpisodeList(options);
  }
}
