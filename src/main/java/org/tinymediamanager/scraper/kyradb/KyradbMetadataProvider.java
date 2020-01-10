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
package org.tinymediamanager.scraper.kyradb;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieArtworkProvider;
import org.tinymediamanager.scraper.kyradb.entities.Image;
import org.tinymediamanager.scraper.kyradb.entities.KyraEntity;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import retrofit2.Response;

/**
 * The Class KyradbMetadataProvider. An artwork provider for the site kyradb.com
 *
 * @author Myron Boyle
 */
public class KyradbMetadataProvider implements IMovieArtworkProvider {
  public static final String       ID           = "animated";

  private static final Logger      LOGGER       = LoggerFactory.getLogger(KyradbMetadataProvider.class);
  private static final String      TMM_API_KEY  = ApiKey.decryptApikey("ZCj2SXQCu+iVTt7RYUqlds0UoCJWuWTZpDIcAIZnvV3CoCeyu2srJQCcZVz5RFAT");
  private static final String      TMM_USER_KEY = ApiKey.decryptApikey("shv369dt1GcJH0bL7Dab3LseS1H0UyEBRKC361coeSM=");

  private static MediaProviderInfo providerInfo = createMediaProviderInfo();
  private static KyraApi           api          = null;

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "KyraAnimated",
        "<html><h3>KyraDB Animated Posters</h3><br />as seen on https://forum.kodi.tv/showthread.php?tid=343391 :)</html>",
        KyradbMetadataProvider.class.getResource("/org/tinymediamanager/scraper/kyradb_logo.png"));

    // configure/load settings
    providerInfo.getConfig().addText("apiKey", "", true);
    providerInfo.getConfig().addText("userKey", "", true);
    providerInfo.getConfig().load();

    return providerInfo;
  }

  // thread safe initialization of the API
  private static synchronized void initAPI() throws ScrapeException {
    if (api == null) {
      try {
        api = new KyraApi();
      }
      catch (Exception e) {
        LOGGER.error("Error initializing KyraApi!", e);
        throw new ScrapeException(e);
      }
    }

    // set user keys, or ours...
    String apiKey = providerInfo.getConfig().getValue("apiKey");
    if (StringUtils.isNotBlank(apiKey) && !apiKey.equals(api.getApiKey())) {
      api.setApiKey(apiKey);
    }
    else {
      api.setApiKey(TMM_API_KEY);
    }

    String userKey = providerInfo.getConfig().getValue("userKey");
    if (StringUtils.isNotBlank(userKey) && !userKey.equals(api.getUserKey())) {
      api.setUserKey(userKey);
    }
    else {
      api.setUserKey(TMM_USER_KEY);
    }

  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions options) throws MissingIdException, ScrapeException {
    LOGGER.debug("getArtwork() - {}", options);

    // lazy initialization of the api
    initAPI();

    List<MediaArtwork> artwork;

    switch (options.getMediaType()) {
      case MOVIE:
        artwork = getMovieArtwork(options);
        break;

      default:
        artwork = new ArrayList<>(1);
    }

    // buffer the artwork
    MediaMetadata md = options.getMetadata();
    if (md != null && !artwork.isEmpty()) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

  private List<MediaArtwork> getMovieArtwork(ArtworkSearchAndScrapeOptions options) throws MissingIdException, ScrapeException {
    MediaArtworkType artworkType = options.getArtworkType();

    String imdbId = options.getImdbId();
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = "";
    }
    int tmdbId = options.getTmdbId();
    if (StringUtils.isBlank(imdbId) && tmdbId == 0) {
      LOGGER.info("neither IMDB nor TMDB id set - returning");
      throw new MissingIdException(MediaMetadata.IMDB, MediaMetadata.TMDB);
    }

    String language = "en";
    if (options.getLanguage() != null) {
      language = options.getLanguage().getLanguage();
    }

    List<MediaArtwork> returnArtwork = new ArrayList<>();
    Exception savedException = null;

    Response<KyraEntity> httpResponse = null;
    if (tmdbId != 0) {
      try {
        LOGGER.debug("getArtwork with TMDB id: {}", tmdbId);
        switch (artworkType) {
          case LOGO:
            httpResponse = api.getMovieService().getLogo(tmdbId).execute();
            break;

          case CHARACTERART:
            httpResponse = api.getMovieService().getCharacterArt(tmdbId).execute();
            break;

          default:
            httpResponse = api.getMovieService().getAnimatedImages(tmdbId).execute();
            break;
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to get artwork: {}", e.getMessage());
        savedException = e;
      }
    }

    if ((httpResponse == null || !httpResponse.isSuccessful()) && StringUtils.isNotBlank(imdbId)) {
      try {
        LOGGER.debug("getArtwork with IMDB id: {}", imdbId);
        switch (artworkType) {
          case LOGO:
            httpResponse = api.getMovieService().getLogo(imdbId).execute();
            break;

          case CHARACTERART:
            httpResponse = api.getMovieService().getCharacterArt(imdbId).execute();
            break;

          default:
            httpResponse = api.getMovieService().getAnimatedImages(imdbId).execute();
            break;
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to get artwork: {}", e.getMessage());
        savedException = e;
      }
    }

    // if there has been an exception and nothing has been found, throw this exception
    if ((httpResponse == null || !httpResponse.isSuccessful()) && savedException != null) {
      // if the thread has been interrupted, to no rethrow that exception
      if (savedException instanceof InterruptedException) {
        return returnArtwork;
      }
      if (savedException instanceof InterruptedIOException) { // got this for some reasons
        return returnArtwork;
      }
      throw new ScrapeException(savedException);
    }

    if (httpResponse == null) {
      LOGGER.info("got no result");
      return returnArtwork;
    }
    if (!httpResponse.isSuccessful()) {
      String message = "";
      try {
        message = httpResponse.errorBody().string();
      }
      catch (IOException e) {
        // ignore
      }
      LOGGER.warn("request was not successful: HTTP/{} - {}", httpResponse.code(), message);
      return returnArtwork;
    }

    KyraEntity kyra = httpResponse.body();
    returnArtwork = getArtwork(kyra, artworkType);
    returnArtwork.sort(new MediaArtwork.MediaArtworkComparator(language));
    return returnArtwork;
  }

  private List<MediaArtwork> getArtwork(KyraEntity kyra, MediaArtworkType artworkType) {
    List<MediaArtwork> artworks = new ArrayList<>();

    String baseUrl = getBaseUrl(kyra, artworkType);
    switch (artworkType) {
      case POSTER:
        artworks.addAll(prepareArtwork(kyra.getPosters(), baseUrl, artworkType));
        break;

      case BACKGROUND:
        artworks.addAll(prepareArtwork(kyra.getBackgrounds(), baseUrl, artworkType));
        break;

      case LOGO:
        artworks.addAll(prepareArtwork(kyra.getLogos(), baseUrl, artworkType));
        break;

      case CHARACTERART:
        artworks.addAll(prepareArtwork(kyra.getCharacters(), baseUrl, artworkType));
        break;

      case ACTOR:
        artworks.addAll(prepareArtwork(kyra.getActors(), baseUrl, artworkType));
        break;

      case ALL:
        artworks.addAll(prepareArtwork(kyra.getPosters(), kyra.getBasePosters(), MediaArtworkType.POSTER));
        artworks.addAll(prepareArtwork(kyra.getBackgrounds(), kyra.getBaseBackground(), MediaArtworkType.BACKGROUND));
        artworks.addAll(prepareArtwork(kyra.getLogos(), kyra.getBaseLogos(), MediaArtworkType.LOGO));
        artworks.addAll(prepareArtwork(kyra.getCharacters(), kyra.getBaseCharacter(), MediaArtworkType.CHARACTERART));
        artworks.addAll(prepareArtwork(kyra.getActors(), kyra.getBaseActor(), MediaArtworkType.ACTOR));
        break;

      default:
        break;
    }

    return artworks;
  }

  private List<MediaArtwork> prepareArtwork(List<Image> images, String baseUrl, MediaArtworkType type) {
    List<MediaArtwork> artworks = new ArrayList<>();

    for (Image image : ListUtils.nullSafe(images)) {
      MediaArtwork ma = new MediaArtwork(providerInfo.getId(), type);
      String url = baseUrl + "/" + image.getName();
      ma.setDefaultUrl(url);
      if (type == MediaArtworkType.POSTER || type == MediaArtworkType.BACKGROUND) {
        ma.setAnimated(true);
      }
      ma.addImageSize(image.getWidth(), image.getHeight(), url);
      artworks.add(ma);
    }

    return artworks;
  }

  /**
   * the images base url is always sent with complete entity....
   *
   * @param kyra
   *          the kyra entity
   * @param type
   *          the artwork type
   * @return
   */
  private String getBaseUrl(KyraEntity kyra, MediaArtworkType type) {
    String ret = "";
    switch (type) {
      case POSTER:
        ret = kyra.getBasePosters();
        break;

      case BACKGROUND:
        ret = kyra.getBaseBackground();
        break;

      case LOGO:
      case CLEARLOGO:
        ret = kyra.getBaseLogos();
        break;

      case CHARACTERART:
        ret = kyra.getBaseCharacter();
        break;

      case ACTOR:
        ret = kyra.getBaseActor();
        break;

      default:
        break;
    }
    return ret;
  }

}
