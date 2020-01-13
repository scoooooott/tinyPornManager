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
package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.anidb.AniDBMetadataProvider;
import org.tinymediamanager.scraper.fanarttv.FanartTvMetadataProvider;
import org.tinymediamanager.scraper.hdtrailersnet.HDTrailersNetTrailerProvider;
import org.tinymediamanager.scraper.imdb.ImdbMetadataProvider;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.kodi.KodiMetadataProvider;
import org.tinymediamanager.scraper.kyradb.KyradbMetadataProvider;
import org.tinymediamanager.scraper.moviemeter.MovieMeterMetadataProvider;
import org.tinymediamanager.scraper.mpdbtv.MpdbMetadataProvider;
import org.tinymediamanager.scraper.ofdb.OfdbMetadataProvider;
import org.tinymediamanager.scraper.omdb.OmdbMetadataProvider;
import org.tinymediamanager.scraper.opensubtitles.OpensubtitlesMetadataProvider;
import org.tinymediamanager.scraper.thetvdb.TheTvDbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.trakt.TraktMetadataProvider;
import org.tinymediamanager.scraper.universal_movie.UniversalMovieMetadataProvider;

/**
 * the class {@link MediaProviders} is used to manage all loaded {@link IMediaProvider}s.
 * 
 * @author Manuel Laggner
 */
public class MediaProviders {
  private static final Logger                          LOGGER          = LoggerFactory.getLogger(MediaProviders.class);
  private static final HashMap<String, IMediaProvider> MEDIA_PROVIDERS = new HashMap<>();

  private MediaProviders() {
    // private constructor for utility classes
  }

  /**
   * load all media providers
   */
  public static void loadMediaProviders() {
    // just call it once -> if the array has been filled before then exit
    if (!MEDIA_PROVIDERS.isEmpty()) {
      return;
    }

    /////////////////////////////////////////////
    // MOVIE
    /////////////////////////////////////////////
    loadProvider(TmdbMetadataProvider.class);
    loadProvider(ImdbMetadataProvider.class);
    loadProvider(MovieMeterMetadataProvider.class);
    loadProvider(OfdbMetadataProvider.class);
    loadProvider(OmdbMetadataProvider.class);
    loadProvider(MpdbMetadataProvider.class);
    loadProvider(KodiMetadataProvider.class);
    loadProvider(TraktMetadataProvider.class);
    loadProvider(UniversalMovieMetadataProvider.class);

    // register all compatible scrapers in the universal scraper
    MEDIA_PROVIDERS.forEach((key, value) -> UniversalMovieMetadataProvider.addProvider(value));
    UniversalMovieMetadataProvider.afterInitialization();

    /////////////////////////////////////////////
    // TV SHOWS
    /////////////////////////////////////////////
    loadProvider(TheTvDbMetadataProvider.class);
    loadProvider(AniDBMetadataProvider.class);
    // tmdb, imdb and trakt are already loaded in the movie block

    /////////////////////////////////////////////
    // ARTWORK
    /////////////////////////////////////////////
    loadProvider(FanartTvMetadataProvider.class);
    loadProvider(KyradbMetadataProvider.class);
    // tmdb is already loaded in the movie block
    // tvdb is alrey loaded in the TV show block

    /////////////////////////////////////////////
    // TRAILER
    /////////////////////////////////////////////
    loadProvider(HDTrailersNetTrailerProvider.class);
    // tmdb is already loaded in the movie block

    /////////////////////////////////////////////
    // SUBTITLES
    /////////////////////////////////////////////
    loadProvider(OpensubtitlesMetadataProvider.class);
  }

  private static void loadProvider(Class<? extends IMediaProvider> clazz) {
    try {
      IMediaProvider provider = clazz.getDeclaredConstructor().newInstance();

      // add the provider to our list of supported providers
      MEDIA_PROVIDERS.putIfAbsent(provider.getProviderInfo().getId(), provider);
    }
    catch (Exception e) {
      LOGGER.error("could not load media provider {} - {}", clazz.getName(), e.getMessage());
    }
  }

  /**
   * get a list of all available media providers for the given interface
   * 
   * @param clazz
   *          the interface which needs to be implemented
   * @param <T>
   *          the type of the interface
   * @return a list of all media providers which implements the given interface
   */
  public static <T extends IMediaProvider> List<T> getProvidersForInterface(Class<T> clazz) {
    List<T> providers = new ArrayList<>();

    MEDIA_PROVIDERS.forEach((key, value) -> {
      if (clazz.isAssignableFrom(value.getClass())) {
        providers.add((T) value);
      }
    });

    return providers;
  }

  /**
   * get the media provider by the given id
   * 
   * @param id
   *          the id of the media provider
   * @return the {@link IMediaProvider} or null
   */
  public static IMediaProvider getProviderById(String id) {
    if (StringUtils.isBlank(id)) {
      return null;
    }

    return MEDIA_PROVIDERS.get(id);
  }
}
