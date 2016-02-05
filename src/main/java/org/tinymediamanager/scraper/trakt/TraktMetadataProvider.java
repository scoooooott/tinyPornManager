/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;

import com.uwetrottmann.trakt.v2.TraktV2;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class TraktMetadataProvider implements IMovieMetadataProvider {
  private static final Logger    LOGGER       = LoggerFactory.getLogger(TraktMetadataProvider.class);
  private static final String    CLIENT_ID    = "a8e7e30fd7fd3f397b6e079f9f023e790f9cbd80a2be57c104089174fa8c6d89";

  static final MediaProviderInfo providerInfo = createMediaProviderInfo();

  static final TraktV2           api          = createTraktApi();

  public TraktMetadataProvider() {
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("trakt", "Trakt.tv",
        "<html><h3>Trakt.tv</h3><br />Trakt.tv is a platform that does many things, but primarily keeps track of TV shows and movies you watch. It also provides meta data for movies and TV shows<br /><br />Available languages: EN</html>",
        TraktMetadataProvider.class.getResource("/trakt_tv.png"));

    return providerInfo;
  }

  private static TraktV2 createTraktApi() {
    TraktV2 api = new TraktV2();
    api.setApiKey(CLIENT_ID);
    // if (LOGGER.isTraceEnabled()) {
    api.setIsDebug(true);
    // }

    return api;
  }

  // ProviderInfo
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  // Scraping
  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    switch (options.getType()) {
      case MOVIE:
        return new TraktMovieMetadataProvider(api).scrape(options);

      default:
        throw new UnsupportedMediaTypeException(options.getType());
    }
  }

  // Searching
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    switch (options.getMediaType()) {
      case MOVIE:
        return new TraktMovieMetadataProvider(api).search(options);

      default:
        throw new UnsupportedMediaTypeException(options.getMediaType());
    }
  }
}
