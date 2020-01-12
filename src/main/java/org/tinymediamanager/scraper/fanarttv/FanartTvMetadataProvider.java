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
package org.tinymediamanager.scraper.fanarttv;

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
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.fanarttv.entities.Image;
import org.tinymediamanager.scraper.fanarttv.entities.Images;
import org.tinymediamanager.scraper.interfaces.IMovieArtworkProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import retrofit2.Response;

/**
 * The Class FanartTvMetadataProvider. An artwork provider for the site fanart.tv
 *
 * @author Manuel Laggner
 */
public class FanartTvMetadataProvider implements IMovieArtworkProvider, ITvShowArtworkProvider {
  public static final String             ID           = "fanarttv";
  private static final Logger            LOGGER       = LoggerFactory.getLogger(FanartTvMetadataProvider.class);
  private static final String            TMM_API_KEY  = ApiKey.decryptApikey("2gkQtSYPIxfyThxPXveHiCGXEcqJJwClUDrB5JV60OnQeQ85Ft65kFIk1SBKoge3");
  private static final MediaProviderInfo providerInfo = createMediaProviderInfo();

  private static FanartTv                api          = null;

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "fanart.tv",
        "<html><h3>Fanart.tv</h3><br />Fanart.tv provides a huge library of artwork for movies, TV shows and music. This service can be consumed with the API key tinyMediaManager offers, but if you want to have faster access to the artwork, you should become a VIP at fanart.tv (https://fanart.tv/vip/).</html>",
        FanartTvMetadataProvider.class.getResource("/org/tinymediamanager/scraper/fanart_tv.png"));

    // configure/load settings
    providerInfo.getConfig().addText("clientKey", "", true);
    providerInfo.getConfig().load();

    return providerInfo;
  }

  public FanartTvMetadataProvider() {
  }

  // thread safe initialization of the API
  private static synchronized void initAPI() throws ScrapeException {
    if (api == null) {
      try {
        api = new FanartTv();
        api.setApiKey(TMM_API_KEY);
      }
      catch (Exception e) {
        LOGGER.error("FanartTvMetadataProvider", e);
        throw new ScrapeException(e);
      }
    }

    // check if we should set a client key
    String clientKey = providerInfo.getConfig().getValue("clientKey");
    if (!clientKey.equals(api.getClientKey())) {
      api.setClientKey(clientKey);
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
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    LOGGER.debug("getArtwork() - {}", options);

    // lazy initialization of the api
    initAPI();

    List<MediaArtwork> artwork;

    switch (options.getMediaType()) {
      case MOVIE:
      case MOVIE_SET:
        artwork = getMovieArtwork(options);
        break;

      case TV_SHOW:
        artwork = getTvShowArtwork(options);
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

  // http://webservice.fanart.tv/v3/movies/559
  private List<MediaArtwork> getMovieArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    MediaArtworkType artworkType = options.getArtworkType();
    String language = null;
    if (options.getLanguage() != null) {
      language = options.getLanguage().getLanguage();
      if (options.getLanguage().toLocale() != null && StringUtils.isNotBlank(options.getLanguage().toLocale().getCountry())) {
        language += "-" + options.getLanguage().toLocale().getCountry();
      }
    }

    List<MediaArtwork> returnArtwork = new ArrayList<>();

    Response<Images> images = null;
    String imdbId = options.getImdbId();
    int tmdbId = options.getTmdbId();

    // for movie sets we need another if
    if (options.getMediaType() == MediaType.MOVIE_SET && options.getIdAsInt(MediaMetadata.TMDB_SET) > 0) {
      tmdbId = options.getIdAsInt(MediaMetadata.TMDB_SET);
    }

    if (tmdbId == 0 && !MetadataUtil.isValidImdbId(imdbId)) {
      throw new MissingIdException(MediaMetadata.IMDB, MediaMetadata.TMDB);
    }

    Exception savedException = null;

    if (StringUtils.isNotBlank(imdbId)) {
      try {
        LOGGER.debug("getArtwork with IMDB id: {}", imdbId);
        images = api.getMovieService().getMovieImages(imdbId).execute();
      }
      catch (Exception e) {
        LOGGER.debug("failed to get artwork: {}", e.getMessage());
        savedException = e;
      }
    }

    if ((images == null || images.body() == null) && tmdbId != 0) {
      try {
        LOGGER.debug("getArtwork with TMDB id: {}", tmdbId);
        images = api.getMovieService().getMovieImages(Integer.toString(tmdbId)).execute();
      }
      catch (Exception e) {
        LOGGER.debug("failed to get artwork: {}", e.getMessage());
        savedException = e;
      }
    }

    // if there has been an exception and nothing has been found, throw this exception
    if ((images == null || !images.isSuccessful()) && savedException != null) {
      // if the thread has been interrupted, to no rethrow that exception
      if (savedException instanceof InterruptedException || savedException instanceof InterruptedIOException) {
        return returnArtwork;
      }
      throw new ScrapeException(savedException);
    }

    if (images == null) {
      LOGGER.info("got no result");
      return returnArtwork;
    }
    if (!images.isSuccessful()) {
      String message = "";
      try {
        message = images.errorBody().string();
      }
      catch (IOException e) {
        // ignore
      }
      LOGGER.warn("request was not successful: HTTP/{} - {}", images.code(), message);
      return returnArtwork;
    }

    returnArtwork = getArtwork(images.body(), artworkType);
    returnArtwork.sort(new MediaArtwork.MediaArtworkComparator(language));

    return returnArtwork;
  }

  // http://webservice.fanart.tv/v3/tv/79349
  private List<MediaArtwork> getTvShowArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    MediaArtworkType artworkType = options.getArtworkType();
    String language = null;
    if (options.getLanguage() != null) {
      language = options.getLanguage().getLanguage();
      if (options.getLanguage().toLocale() != null && StringUtils.isNotBlank(options.getLanguage().toLocale().getCountry())) {
        language += "-" + options.getLanguage().toLocale().getCountry();
      }
    }

    List<MediaArtwork> returnArtwork = new ArrayList<>();

    int tvdbId = options.getIdAsInt(MediaMetadata.TVDB);

    // no ID found? try the old one
    if (tvdbId == 0) {
      tvdbId = options.getIdAsInt("tvdb");
    }

    if (tvdbId == 0) {
      throw new MissingIdException(MediaMetadata.TVDB);
    }

    Response<Images> images = null;
    try {
      LOGGER.debug("getArtwork with TVDB id: {}", tvdbId);
      images = api.getTvShowService().getTvShowImages(tvdbId).execute();
    }
    catch (Exception e) {
      LOGGER.debug("failed to get artwork: {}", e.getMessage());
      // if the thread has been interrupted, to no rethrow that exception
      if (e instanceof InterruptedException || e instanceof InterruptedIOException) {
        return returnArtwork;
      }

      throw new ScrapeException(e);
    }

    if (images == null) {
      LOGGER.info("got no result");
      return returnArtwork;
    }
    if (!images.isSuccessful()) {
      String message = "";
      try {
        message = images.errorBody().string();
      }
      catch (IOException e) {
        // ignore
      }
      LOGGER.warn("request was not successful: HTTP/{} - {}", images.code(), message);
      return returnArtwork;
    }

    returnArtwork = getArtwork(images.body(), artworkType);
    returnArtwork.sort(new MediaArtwork.MediaArtworkComparator(language));

    return returnArtwork;
  }

  private List<MediaArtwork> getArtwork(Images images, MediaArtworkType artworkType) {
    List<MediaArtwork> artworks = new ArrayList<>();

    switch (artworkType) {
      case POSTER:
        artworks.addAll(prepareArtwork(images.movieposter, ImageType.MOVIEPOSTER));
        artworks.addAll(prepareArtwork(images.tvposter, ImageType.TVPOSTER));
        break;

      case BACKGROUND:
        artworks.addAll(prepareArtwork(images.moviebackground, ImageType.MOVIEBACKGROUND));
        artworks.addAll(prepareArtwork(images.showbackground, ImageType.SHOWBACKGROUND));
        break;

      case BANNER:
        artworks.addAll(prepareArtwork(images.moviebanner, ImageType.MOVIEBANNER));
        artworks.addAll(prepareArtwork(images.tvbanner, ImageType.TVBANNER));
        break;

      case CLEARART:
        artworks.addAll(prepareArtwork(images.hdmovieclearart, ImageType.HDMOVIECLEARART));
        artworks.addAll(prepareArtwork(images.movieart, ImageType.MOVIEART));
        artworks.addAll(prepareArtwork(images.hdclearart, ImageType.HDCLEARART));
        artworks.addAll(prepareArtwork(images.clearart, ImageType.CLEARART));
        break;

      case DISC:
        artworks.addAll(prepareArtwork(images.moviedisc, ImageType.MOVIEDISC));
        break;

      case LOGO:
      case CLEARLOGO:
        artworks.addAll(prepareArtwork(images.hdmovielogo, ImageType.HDMOVIELOGO));
        artworks.addAll(prepareArtwork(images.movielogo, ImageType.MOVIELOGO));
        artworks.addAll(prepareArtwork(images.hdtvlogo, ImageType.HDTVLOGO));
        artworks.addAll(prepareArtwork(images.clearlogo, ImageType.CLEARLOGO));
        break;

      case SEASON_POSTER:
        artworks.addAll(prepareArtwork(images.seasonposter, ImageType.SEASONPOSTER));
        break;

      case SEASON_BANNER:
        artworks.addAll(prepareArtwork(images.seasonbanner, ImageType.SEASONBANNER));
        break;

      case SEASON_THUMB:
        artworks.addAll(prepareArtwork(images.seasonthumb, ImageType.SEASONTHUMB));
        break;

      case THUMB:
        artworks.addAll(prepareArtwork(images.moviethumb, ImageType.MOVIETHUMB));
        artworks.addAll(prepareArtwork(images.tvthumb, ImageType.TVTHUMB));
        break;

      case CHARACTERART:
        artworks.addAll(prepareArtwork(images.characterart, ImageType.CHARACTERART));
        break;

      case KEYART:
        artworks.addAll(prepareArtwork(images.movieposter, ImageType.MOVIEKEYART));
        artworks.addAll(prepareArtwork(images.tvposter, ImageType.TVKEYART));
        break;

      case ALL:
        artworks.addAll(prepareArtwork(images.movieposter, ImageType.MOVIEPOSTER));
        artworks.addAll(prepareArtwork(images.tvposter, ImageType.TVPOSTER));

        artworks.addAll(prepareArtwork(images.moviebackground, ImageType.MOVIEBACKGROUND));
        artworks.addAll(prepareArtwork(images.showbackground, ImageType.SHOWBACKGROUND));

        artworks.addAll(prepareArtwork(images.moviebanner, ImageType.MOVIEBANNER));
        artworks.addAll(prepareArtwork(images.tvbanner, ImageType.TVBANNER));

        artworks.addAll(prepareArtwork(images.hdmovieclearart, ImageType.HDMOVIECLEARART));
        artworks.addAll(prepareArtwork(images.movieart, ImageType.MOVIEART));
        artworks.addAll(prepareArtwork(images.hdclearart, ImageType.HDCLEARART));
        artworks.addAll(prepareArtwork(images.clearart, ImageType.CLEARART));

        artworks.addAll(prepareArtwork(images.moviedisc, ImageType.MOVIEDISC));

        artworks.addAll(prepareArtwork(images.hdmovielogo, ImageType.HDMOVIELOGO));
        artworks.addAll(prepareArtwork(images.movielogo, ImageType.MOVIELOGO));
        artworks.addAll(prepareArtwork(images.hdtvlogo, ImageType.HDTVLOGO));
        artworks.addAll(prepareArtwork(images.clearlogo, ImageType.CLEARLOGO));

        artworks.addAll(prepareArtwork(images.seasonbanner, ImageType.SEASONBANNER));
        artworks.addAll(prepareArtwork(images.seasonposter, ImageType.SEASONPOSTER));
        artworks.addAll(prepareArtwork(images.seasonthumb, ImageType.SEASONTHUMB));

        artworks.addAll(prepareArtwork(images.moviethumb, ImageType.MOVIETHUMB));
        artworks.addAll(prepareArtwork(images.tvthumb, ImageType.TVTHUMB));

        artworks.addAll(prepareArtwork(images.characterart, ImageType.CHARACTERART));

        artworks.addAll(prepareArtwork(images.movieposter, ImageType.MOVIEKEYART));
        artworks.addAll(prepareArtwork(images.tvposter, ImageType.TVKEYART));
        break;

      default:
        break;
    }

    return artworks;
  }

  private List<MediaArtwork> prepareArtwork(List<Image> images, ImageType type) {
    List<MediaArtwork> artworks = new ArrayList<>();

    for (Image image : ListUtils.nullSafe(images)) {
      // -keyart is actually a poster with the language '00'
      if ((type == ImageType.MOVIEKEYART || type == ImageType.TVKEYART) && !"00".equals(image.lang)) {
        continue;
      }

      MediaArtwork ma = new MediaArtwork(providerInfo.getId(), type.type);
      ma.setDefaultUrl(image.url);
      ma.setPreviewUrl(image.url.replace("/fanart/", "/preview/"));
      ma.setLanguage(image.lang);
      ma.setLikes(image.likes);
      ma.addImageSize(type.width, type.height, image.url);
      ma.setSizeOrder(type.sizeOrder);

      if ("all".equals(image.season)) {
        ma.setSeason(0);
      }
      else if (StringUtils.isNotBlank(image.season)) {
        try {
          ma.setSeason(Integer.parseInt(image.season));
        }
        catch (Exception e) {
          LOGGER.trace("could not parse int: {}", e.getMessage());
        }
      }
      artworks.add(ma);

      // fanart.tv only knows clearlogo; copy that for the logo type
      if (type.type == MediaArtworkType.CLEARLOGO) {
        artworks.add(new MediaArtwork(ma, MediaArtworkType.LOGO));
      }
    }

    return artworks;
  }

  private enum ImageType {

    // @formatter:off
    HDMOVIECLEARART(1000, 562, MediaArtworkType.CLEARART, FanartSizes.MEDIUM.getOrder()),
    HDCLEARART(1000, 562, MediaArtworkType.CLEARART, FanartSizes.MEDIUM.getOrder()),
    MOVIETHUMB(1000, 562, MediaArtworkType.THUMB, FanartSizes.MEDIUM.getOrder()),
    SEASONTHUMB(1000, 562, MediaArtworkType.SEASON_THUMB, FanartSizes.MEDIUM.getOrder()),
    TVTHUMB(500, 281, MediaArtworkType.THUMB, FanartSizes.MEDIUM.getOrder()),
    MOVIEBACKGROUND(1920, 1080, MediaArtworkType.BACKGROUND, FanartSizes.LARGE.getOrder()),
    SHOWBACKGROUND(1920, 1080, MediaArtworkType.BACKGROUND, FanartSizes.LARGE.getOrder()),
    MOVIEPOSTER(1000, 1426, MediaArtworkType.POSTER, PosterSizes.LARGE.getOrder()),
    MOVIEKEYART(1000, 1426, MediaArtworkType.KEYART, PosterSizes.LARGE.getOrder()),
    TVPOSTER(1000, 1426, MediaArtworkType.POSTER, PosterSizes.LARGE.getOrder()),
    TVKEYART(1000, 1426, MediaArtworkType.KEYART, PosterSizes.LARGE.getOrder()),
    SEASONPOSTER(1000, 1426, MediaArtworkType.SEASON_POSTER, MediaArtwork.PosterSizes.LARGE.getOrder()),
    TVBANNER(1000, 185, MediaArtworkType.BANNER, FanartSizes.MEDIUM.getOrder()),
    MOVIEBANNER(1000, 185, MediaArtworkType.BANNER, FanartSizes.MEDIUM.getOrder()),
    SEASONBANNER(1000, 185, MediaArtworkType.SEASON_BANNER, FanartSizes.MEDIUM.getOrder()),
    HDMOVIELOGO(800, 310, MediaArtworkType.CLEARLOGO, FanartSizes.MEDIUM.getOrder()),
    HDTVLOGO(800, 310, MediaArtworkType.CLEARLOGO, FanartSizes.MEDIUM.getOrder()),
    CLEARLOGO(400, 155, MediaArtworkType.CLEARLOGO, FanartSizes.SMALL.getOrder()),
    MOVIELOGO(400, 155, MediaArtworkType.CLEARLOGO, FanartSizes.SMALL.getOrder()),
    CLEARART(500, 281, MediaArtworkType.CLEARART, FanartSizes.SMALL.getOrder()),
    MOVIEART(500, 281, MediaArtworkType.CLEARART, FanartSizes.SMALL.getOrder()),
    MOVIEDISC(1000, 1000, MediaArtworkType.DISC, FanartSizes.MEDIUM.getOrder()),
    CHARACTERART(512, 512, MediaArtworkType.CHARACTERART, FanartSizes.MEDIUM.getOrder());
    // @formatter:on

    ImageType(int width, int height, MediaArtworkType type, int sizeOrder) {
      this.width = width;
      this.height = height;
      this.type = type;
      this.sizeOrder = sizeOrder;
    }

    int              width;
    int              height;
    MediaArtworkType type;
    int              sizeOrder;
  }
}
