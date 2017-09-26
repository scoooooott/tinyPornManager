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
package org.tinymediamanager.scraper.trakt;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.TraktV2Interceptor;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import okhttp3.OkHttpClient;

@PluginImplementation
public class TraktMetadataProvider implements IMovieMetadataProvider, ITvShowMetadataProvider {
  private static final String    CLIENT_ID    = "a8e7e30fd7fd3f397b6e079f9f023e790f9cbd80a2be57c104089174fa8c6d89";

  static final MediaProviderInfo providerInfo = createMediaProviderInfo();

  private static TraktV2         api;

  public TraktMetadataProvider() {
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("trakt", "Trakt.tv",
        "<html><h3>Trakt.tv</h3><br />Trakt.tv is a platform that does many things," + " but primarily keeps track of TV shows and movies you watch. "
            + "It also provides meta data for movies and TV shows<br /><br />Available languages: EN</html>",
        TraktMetadataProvider.class.getResource("/trakt_tv.png"));
    providerInfo.setVersion(TraktMetadataProvider.class);

    providerInfo.getConfig().addText("apiKey", "", true);

    return providerInfo;
  }

  // thread safe initialization of the API
  private static synchronized void initAPI() throws Exception {
    String apiKey = CLIENT_ID;
    String userApiKey = providerInfo.getConfig().getValue("apiKey");

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

    // create a new instance of the tmdb api
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

  // Searching
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    // lazy initialization of the api
    initAPI();

    switch (options.getMediaType()) {
      case MOVIE:
        return new TraktMovieMetadataProvider(api).search(options);

      case TV_SHOW:
        return new TraktTVShowMetadataProvider(api).search(options);

      default:
        throw new UnsupportedMediaTypeException(options.getMediaType());
    }
  }

  // Scraping Movie
  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    // lazy initialization of the api
    initAPI();

    switch (options.getType()) {
      case MOVIE:
        return new TraktMovieMetadataProvider(api).scrape(options);

      case TV_SHOW:
        return new TraktTVShowMetadataProvider(api).scrape(options);

      case TV_EPISODE:
        return new TraktTVShowMetadataProvider(api).scrape(options);

      default:
        throw new UnsupportedMediaTypeException(options.getType());
    }
  }

  @Override
  public List<MediaEpisode> getEpisodeList(MediaScrapeOptions mediaScrapeOptions) throws Exception {
    // lazy initialization of the api
    initAPI();

    switch (mediaScrapeOptions.getType()) {
      case TV_SHOW:
        return new TraktTVShowMetadataProvider(api).getEpisodeList(mediaScrapeOptions);

      default:
        throw new UnsupportedMediaTypeException(mediaScrapeOptions.getType());
    }
  }
}
