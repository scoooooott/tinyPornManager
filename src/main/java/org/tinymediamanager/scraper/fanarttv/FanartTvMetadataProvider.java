/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.fanarttv.entities.Image;
import org.tinymediamanager.scraper.fanarttv.entities.Images;
import org.tinymediamanager.scraper.mediaprovider.IMovieArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.ListUtils;

import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * The Class FanartTvMetadataProvider. An artwork provider for the site fanart.tv
 *
 * @author Manuel Laggner
 */
@PluginImplementation
public class FanartTvMetadataProvider implements IMovieArtworkProvider, ITvShowArtworkProvider {
  private static final Logger      LOGGER       = LoggerFactory.getLogger(FanartTvMetadataProvider.class);
  private static final String      TMM_API_KEY  = ApiKey.decryptApikey("2gkQtSYPIxfyThxPXveHiCGXEcqJJwClUDrB5JV60OnQeQ85Ft65kFIk1SBKoge3");

  private static MediaProviderInfo providerInfo = createMediaProviderInfo();
  private static FanartTv          api          = null;

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("fanarttv", "fanart.tv",
        "<html><h3>Fanart.tv</h3><br />Fanart.tv provides a huge library of artwork for movies, TV shows and music.<br />Does not provide movie poster</html>",
        FanartTvMetadataProvider.class.getResource("/fanart_tv.png"));
    providerInfo.setVersion(FanartTvMetadataProvider.class);

    // configure/load settings
    providerInfo.getConfig().addText("apiKey", "", true);
    providerInfo.getConfig().addText("clientKey", "", true);
    providerInfo.getConfig().load();

    return providerInfo;
  }

  public FanartTvMetadataProvider() throws Exception {
  }

  // thread safe initialization of the API
  private static synchronized void initAPI() throws Exception {
    if (api == null) {
      try {
        api = new FanartTv();
      }
      catch (Exception e) {
        LOGGER.error("FanartTvMetadataProvider", e);
        throw e;
      }
    }

    String userApiKey = providerInfo.getConfig().getValue("apiKey");

    // check if the API should change from current key to user key
    if (StringUtils.isNotBlank(userApiKey) && !userApiKey.equals(api.getApiKey())) {
      api.setApiKey(userApiKey);
    }

    // check if the API should change from current key to tmm key
    if (StringUtils.isBlank(userApiKey) && !TMM_API_KEY.equals(api.getApiKey())) {
      api.setApiKey(TMM_API_KEY);
    }

    // check if we should set a client key
    String clientKey = providerInfo.getConfig().getValue("clientKey");
    if (clientKey.equals(api.getClientKey())) {
      api.setClientKey(clientKey);
    }
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    // lazy initialization of the api
    initAPI();

    LOGGER.debug("getArtwork() " + options.toString());

    List<MediaArtwork> artwork;

    switch (options.getType()) {
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
    if (md != null && artwork.size() > 0) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

  // http://webservice.fanart.tv/v3/movies/559
  private List<MediaArtwork> getMovieArtwork(MediaScrapeOptions options) throws Exception {
    MediaArtworkType artworkType = options.getArtworkType();
    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }

    List<MediaArtwork> returnArtwork = new ArrayList<>();

    Images images = null;
    String imdbId = options.getImdbId();
    int tmdbId = options.getTmdbId();

    if (StringUtils.isNotBlank(imdbId)) {
      try {
        LOGGER.debug("getArtwork with IMDB id: " + imdbId);
        images = api.getMovieService().getMovieImages(imdbId);
      }
      catch (Exception e) {
        LOGGER.debug("failed to get artwork: " + e.getMessage());
      }
    }

    if (images == null && tmdbId != 0) {
      try {
        LOGGER.debug("getArtwork with TMDB id: " + tmdbId);
        images = api.getMovieService().getMovieImages(Integer.toString(tmdbId));
      }
      catch (Exception e) {
        LOGGER.debug("failed to get artwork: " + e.getMessage());
      }
    }

    if (images == null) {
      LOGGER.info("got no result");
      return returnArtwork;
    }

    returnArtwork = getArtwork(images, artworkType);
    Collections.sort(returnArtwork, new MediaArtwork.MediaArtworkComparator(language));

    return returnArtwork;
  }

  // http://webservice.fanart.tv/v3/tv/79349
  private List<MediaArtwork> getTvShowArtwork(MediaScrapeOptions options) throws Exception {
    MediaArtworkType artworkType = options.getArtworkType();
    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }

    List<MediaArtwork> returnArtwork = new ArrayList<>();

    Images images = null;
    int tvdbId = options.getIdAsInt(MediaMetadata.TVDB);

    // no ID found? try the old one
    if (tvdbId == 0) {
      tvdbId = options.getIdAsInt("tvdb");
    }

    if (tvdbId > 0) {
      try {
        LOGGER.debug("getArtwork with TVDB id: " + tvdbId);
        images = api.getTvShowService().getTvShowImages(tvdbId);
      }
      catch (Exception e) {
        LOGGER.debug("failed to get artwork: " + e.getMessage());
      }
    }
    else {
      LOGGER.warn("not tvdbId set");
      return returnArtwork;
    }

    if (images == null) {
      LOGGER.info("got no result");
      return returnArtwork;
    }

    returnArtwork = getArtwork(images, artworkType);
    Collections.sort(returnArtwork, new MediaArtwork.MediaArtworkComparator(language));

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

      case SEASON:
        artworks.addAll(prepareArtwork(images.seasonbanner, ImageType.SEASONBANNER));
        artworks.addAll(prepareArtwork(images.seasonposter, ImageType.SEASONPOSTER));
        artworks.addAll(prepareArtwork(images.seasonthumb, ImageType.SEASONTHUMB));
        break;

      case THUMB:
        artworks.addAll(prepareArtwork(images.moviethumb, ImageType.MOVIETHUMB));
        artworks.addAll(prepareArtwork(images.tvthumb, ImageType.TVTHUMB));
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
        break;

      default:
        break;
    }

    return artworks;
  }

  private List<MediaArtwork> prepareArtwork(List<Image> images, ImageType type) {
    List<MediaArtwork> artworks = new ArrayList<>();

    for (Image image : ListUtils.nullSafe(images)) {
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
      else {
        try {
          ma.setSeason(Integer.valueOf(image.season));
        }
        catch (Exception ignored) {
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
    SEASONTHUMB(500, 281, MediaArtworkType.SEASON, FanartSizes.SMALL.getOrder()),
    TVTHUMB(500, 281, MediaArtworkType.THUMB, FanartSizes.MEDIUM.getOrder()),
    MOVIEBACKGROUND(1920, 1080, MediaArtworkType.BACKGROUND, FanartSizes.LARGE.getOrder()),
    SHOWBACKGROUND(1920, 1080, MediaArtworkType.BACKGROUND, FanartSizes.LARGE.getOrder()),
    MOVIEPOSTER(1000, 1426, MediaArtworkType.POSTER, PosterSizes.LARGE.getOrder()),
    TVPOSTER(1000, 1426, MediaArtworkType.POSTER, PosterSizes.LARGE.getOrder()),
    SEASONPOSTER(1000, 1426, MediaArtworkType.SEASON, MediaArtwork.PosterSizes.LARGE.getOrder()),
    TVBANNER(1000, 185, MediaArtworkType.BANNER, FanartSizes.MEDIUM.getOrder()),
    MOVIEBANNER(1000, 185, MediaArtworkType.BANNER, FanartSizes.MEDIUM.getOrder()),
    SEASONBANNER(1000, 185, MediaArtworkType.SEASON, FanartSizes.MEDIUM.getOrder()),
    HDMOVIELOGO(800, 310, MediaArtworkType.CLEARLOGO, FanartSizes.MEDIUM.getOrder()),
    HDTVLOGO(800, 310, MediaArtworkType.CLEARLOGO, FanartSizes.MEDIUM.getOrder()),
    CLEARLOGO(400, 155, MediaArtworkType.CLEARLOGO, FanartSizes.SMALL.getOrder()),
    MOVIELOGO(400, 155, MediaArtworkType.CLEARLOGO, FanartSizes.SMALL.getOrder()),
    CLEARART(500, 281, MediaArtworkType.CLEARART, FanartSizes.SMALL.getOrder()),
    MOVIEART(500, 281, MediaArtworkType.CLEARART, FanartSizes.SMALL.getOrder()),
    MOVIEDISC(1000, 1000, MediaArtworkType.DISC, FanartSizes.MEDIUM.getOrder());
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
