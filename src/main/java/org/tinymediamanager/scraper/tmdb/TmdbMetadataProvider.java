/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.scraper.tmdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaTrailer;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.mediaprovider.IMovieArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieSetMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieTrailerProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.ApiKey;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.TmdbInterceptor;
import com.uwetrottmann.tmdb2.entities.Configuration;
import com.uwetrottmann.tmdb2.entities.Genre;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import okhttp3.OkHttpClient;

/**
 * The Class TmdbMetadataProvider. A meta data, artwork and trailer provider for the site themoviedb.org
 *
 * @author Manuel Laggner
 */
@PluginImplementation
public class TmdbMetadataProvider implements IMovieMetadataProvider, IMovieSetMetadataProvider, ITvShowMetadataProvider, IMovieArtworkProvider,
    ITvShowArtworkProvider, IMovieTrailerProvider {
  static Tmdb              api;
  static MediaProviderInfo providerInfo = createMediaProviderInfo();
  static Configuration     configuration;

  public TmdbMetadataProvider() throws Exception {
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("tmdb", "themoviedb.org",
        "<html><h3>The Movie Database (TMDb)</h3><br />The largest free movie database maintained by the community. It provides metadata and artwork<br />in many different languages. Thus it is the first choice for non english users<br /><br />Available languages: multiple</html>",
        TmdbMetadataProvider.class.getResource("/themoviedb_org.png"));
    providerInfo.setVersion(TmdbMetadataProvider.class);

    providerInfo.getConfig().addBoolean("includeAdult", false);
    providerInfo.getConfig().addBoolean("scrapeLanguageNames", true);

    ArrayList<String> fallbackLanguages = new ArrayList<>();
    for (MediaLanguages mediaLanguages : MediaLanguages.values()) {
      fallbackLanguages.add(mediaLanguages.toString());
    }

    providerInfo.getConfig().addSelect("titleFallbackLanguage", fallbackLanguages.toArray(new String[0]), MediaLanguages.en.toString());
    providerInfo.getConfig().load();
    return providerInfo;
  }

  // thread safe initialization of the API
  private static synchronized void initAPI() throws Exception {
    // create a new instance of the tmdb api
    if (api == null) {
      api = new Tmdb(ApiKey.decryptApikey("dj5KmN0AO0eFDMF1tybX3H+zxGpfm4pUQAlEhM3iah/g2kuCzUQVZiiJ+ceCP2DO")) {
        // tell the tmdb api to use our OkHttp client

        @Override
        protected synchronized OkHttpClient okHttpClient() {
          OkHttpClient.Builder builder = TmmHttpClient.newBuilder(true);
          builder.addInterceptor(new TmdbInterceptor(this));
          return builder.build();
        }
      };

      configuration = api.configurationService().configuration().execute().body();
      if (configuration == null) {
        throw new Exception("Invalid TMDB API key");
      }
    }
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    // lazy initialization of the api
    initAPI();

    List<MediaSearchResult> searchResults;
    switch (query.getMediaType()) {
      case MOVIE:
        searchResults = new TmdbMovieMetadataProvider(api).search(query);
        break;

      case MOVIE_SET:
        searchResults = new TmdbMovieSetMetadataProvider(api).search(query);
        break;

      case TV_SHOW:
        searchResults = new TmdbTvShowMetadataProvider(api).search(query);
        break;

      default:
        throw new UnsupportedMediaTypeException(query.getMediaType());
    }

    Collections.sort(searchResults);
    Collections.reverse(searchResults);

    return searchResults;
  }

  @Override
  public List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    // lazy initialization of the api
    initAPI();

    switch (options.getType()) {
      case TV_SHOW:
      case TV_EPISODE:
        return new TmdbTvShowMetadataProvider(api).getEpisodeList(options);

      default:
        throw new Exception("unsupported media type");
    }
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    // lazy initialization of the api
    initAPI();

    switch (options.getType()) {
      case MOVIE:
        return new TmdbMovieMetadataProvider(api).getMetadata(options);

      case MOVIE_SET:
        return new TmdbMovieSetMetadataProvider(api).getMetadata(options);

      case TV_SHOW:
      case TV_EPISODE:
        return new TmdbTvShowMetadataProvider(api).getMetadata(options);

      default:
        throw new UnsupportedMediaTypeException(options.getType());
    }
  }

  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    // lazy initialization of the api
    initAPI();

    return new TmdbArtworkProvider(api).getArtwork(options);
  }

  @Override
  public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
    // lazy initialization of the api
    initAPI();

    switch (options.getType()) {
      case MOVIE:
        return new TmdbTrailerProvider(api).getTrailers(options);

      default:
        throw new Exception("unsupported media type");
    }
  }

  public int getTmdbIdFromImdbId(String imdbId) throws Exception {
    // lazy initialization of the api
    initAPI();

    return new TmdbMovieMetadataProvider(api).getTmdbIdFromImdbId(imdbId);
  }

  /*
   * Maps scraper Genres to internal TMM genres
   */
  static MediaGenres getTmmGenre(Genre genre) {
    MediaGenres g = null;
    switch (genre.id) {
      case 28:
        g = MediaGenres.ACTION;
        break;
      case 12:
        g = MediaGenres.ADVENTURE;
        break;
      case 16:
        g = MediaGenres.ANIMATION;
        break;
      case 35:
        g = MediaGenres.COMEDY;
        break;
      case 80:
        g = MediaGenres.CRIME;
        break;
      case 105:
        g = MediaGenres.DISASTER;
        break;
      case 99:
        g = MediaGenres.DOCUMENTARY;
        break;
      case 18:
        g = MediaGenres.DRAMA;
        break;
      case 82:
        g = MediaGenres.EASTERN;
        break;
      case 2916:
        g = MediaGenres.EROTIC;
        break;
      case 10751:
        g = MediaGenres.FAMILY;
        break;
      case 10750:
        g = MediaGenres.FAN_FILM;
        break;
      case 14:
        g = MediaGenres.FANTASY;
        break;
      case 10753:
        g = MediaGenres.FILM_NOIR;
        break;
      case 10769:
        g = MediaGenres.FOREIGN;
        break;
      case 36:
        g = MediaGenres.HISTORY;
        break;
      case 10595:
        g = MediaGenres.HOLIDAY;
        break;
      case 27:
        g = MediaGenres.HORROR;
        break;
      case 10756:
        g = MediaGenres.INDIE;
        break;
      case 10402:
        g = MediaGenres.MUSIC;
        break;
      case 22:
        g = MediaGenres.MUSICAL;
        break;
      case 9648:
        g = MediaGenres.MYSTERY;
        break;
      case 10754:
        g = MediaGenres.NEO_NOIR;
        break;
      case 1115:
        g = MediaGenres.ROAD_MOVIE;
        break;
      case 10749:
        g = MediaGenres.ROMANCE;
        break;
      case 878:
        g = MediaGenres.SCIENCE_FICTION;
        break;
      case 10755:
        g = MediaGenres.SHORT;
        break;
      case 9805:
        g = MediaGenres.SPORT;
        break;
      case 10758:
        g = MediaGenres.SPORTING_EVENT;
        break;
      case 10757:
        g = MediaGenres.SPORTS_FILM;
        break;
      case 10748:
        g = MediaGenres.SUSPENSE;
        break;
      case 10770:
        g = MediaGenres.TV_MOVIE;
        break;
      case 53:
        g = MediaGenres.THRILLER;
        break;
      case 10752:
        g = MediaGenres.WAR;
        break;
      case 37:
        g = MediaGenres.WESTERN;
        break;
    }
    if (g == null) {
      g = MediaGenres.getGenre(genre.name);
    }
    return g;
  }
}
