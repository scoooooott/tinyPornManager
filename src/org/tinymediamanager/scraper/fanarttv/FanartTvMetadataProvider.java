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
package org.tinymediamanager.scraper.fanarttv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;

import com.omertron.fanarttvapi.FanartTvApi;
import com.omertron.fanarttvapi.enumeration.FTArtworkType;
import com.omertron.fanarttvapi.model.FTArtwork;
import com.omertron.fanarttvapi.model.FTMovie;
import com.omertron.fanarttvapi.model.FTSeries;

/**
 * The Class FanartTvMetadataProvider. An artwork provider for the site fanart.tv
 * 
 * @author Myron Boyle, Manuel Laggner
 */
public class FanartTvMetadataProvider implements IMediaArtworkProvider {
  private static final Logger      LOGGER       = LoggerFactory.getLogger(FanartTvMetadataProvider.class);
  private static MediaProviderInfo providerInfo = new MediaProviderInfo(Constants.FANARTTVID, "fanart.tv", "Scraper for fanarts");

  private FanartTvApi              ftv          = null;

  public FanartTvMetadataProvider() throws Exception {
    if (ftv == null) {
      try {
        if (Globals.isDonator() && !StringUtils.isEmpty(Globals.settings.getFanartClientKey())) {
          ftv = new FanartTvApi("9314fc8f4c7d4a8b80079da114794891", Globals.settings.getFanartClientKey());
        }
        else {
          ftv = new FanartTvApi("9314fc8f4c7d4a8b80079da114794891");
        }
        // set our proxy
        if (StringUtils.isNotEmpty(Globals.settings.getProxyPort()) && StringUtils.isNotEmpty(Globals.settings.getProxyHost())) {
          try {
            ftv.setProxy(Globals.settings.getProxyHost(), Integer.valueOf(Globals.settings.getProxyPort()), Globals.settings.getProxyUsername(),
                Globals.settings.getProxyPassword());
          }
          catch (Exception e) {
            LOGGER.error("Failed to set proxy for FanartTvMetadataProvider - using NONE", e);
          }
        }

      }
      catch (Exception e) {
        LOGGER.error("FanartTvMetadataProvider", e);
        throw e;
      }
    }
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getArtwork() " + options.toString());

    List<MediaArtwork> artwork = null;

    switch (options.getType()) {
      case MOVIE:
        artwork = getMovieArtwork(options);
        break;

      case TV_SHOW:
        artwork = getTvShowArtwork(options);
        break;

      default:
        artwork = new ArrayList<MediaArtwork>(1);
    }

    // buffer the artwork
    MediaMetadata md = options.getMetadata();
    if (md != null && artwork.size() > 0) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

  // http://webservice.fanart.tv/v3/movies/559?api_key=<API_KEY>
  private List<MediaArtwork> getMovieArtwork(MediaScrapeOptions options) throws Exception {
    MediaArtworkType artworkType = options.getArtworkType();
    MediaLanguages language = options.getLanguage();
    List<MediaArtwork> allArtwork = new ArrayList<MediaArtwork>();
    List<MediaArtwork> returnArtwork = new ArrayList<MediaArtwork>();

    FTMovie movieImages = null;
    String imdbId = options.getImdbId();
    int tmdbId = options.getTmdbId();
    if (imdbId != null && !imdbId.isEmpty()) {
      LOGGER.debug("getArtwork with IMDB id: " + imdbId);
      try {
        movieImages = ftv.getMovieArtwork(imdbId);
      }
      catch (Exception e) {
      }
    }
    if (movieImages == null && tmdbId != 0) {
      LOGGER.debug("getArtwork with TMDB id: " + tmdbId);
      try {
        movieImages = ftv.getMovieArtwork(String.valueOf(tmdbId));
      }
      catch (Exception e) {
      }
    }

    if (movieImages == null) {
      LOGGER.warn("nothing found");
      return returnArtwork;
    }

    for (FTArtworkType type : movieImages.getArtwork().keySet()) {
      // iterate over all types, and create type specific MAs
      allArtwork.addAll(getMediaArtworkListFromFTArtworkList(movieImages.getArtwork(type), type));
      Collections.sort(allArtwork, new MediaArtwork.MediaArtworkComparator(language));
    }
    if (artworkType == MediaArtworkType.ALL) {
      return allArtwork;
    }
    else {
      // just copy ours into new array
      for (MediaArtwork ma : allArtwork) {
        if (ma.getType() == artworkType) {
          returnArtwork.add(ma);
        }
      }
    }
    return returnArtwork;
  }

  // http://webservice.fanart.tv/v3/tv/79349?api_key=<API_KEY>
  private List<MediaArtwork> getTvShowArtwork(MediaScrapeOptions options) throws Exception {
    MediaArtworkType artworkType = options.getArtworkType();
    MediaLanguages language = options.getLanguage();
    List<MediaArtwork> allArtwork = new ArrayList<MediaArtwork>();
    List<MediaArtwork> returnArtwork = new ArrayList<MediaArtwork>();

    FTSeries tvShowImages = null;
    int tvdbId = 0;
    try {
      tvdbId = Integer.parseInt(options.getId(Constants.TVDBID));
    }
    catch (Exception e) {
    }
    // no ID found? try the old one
    if (tvdbId == 0) {
      try {
        tvdbId = Integer.parseInt(options.getId("tvdb"));
      }
      catch (Exception e) {
      }
    }

    if (tvdbId > 0) {
      try {
        tvShowImages = ftv.getTvArtwork(String.valueOf(tvdbId));
      }
      catch (Exception e) {
      }
    }
    if (tvShowImages == null) {
      LOGGER.warn("nothing found");
      return returnArtwork;
    }

    for (FTArtworkType type : tvShowImages.getArtwork().keySet()) {
      // iterate over all types, and create type specific MAs
      allArtwork.addAll(getMediaArtworkListFromFTArtworkList(tvShowImages.getArtwork(type), type));
      Collections.sort(allArtwork, new MediaArtwork.MediaArtworkComparator(language));
    }
    if (artworkType == MediaArtworkType.ALL) {
      return allArtwork;
    }
    else {
      // just copy ours into new array
      for (MediaArtwork ma : allArtwork) {
        if (ma.getType() == artworkType) {
          returnArtwork.add(ma);
        }
      }
    }
    return returnArtwork;
  }

  /**
   * gets a list of Fanarts, and converts that to our type of MediaArtworks
   * 
   * @param awl
   * @param mat
   * @return
   */
  private static List<MediaArtwork> getMediaArtworkListFromFTArtworkList(List<FTArtwork> awl, FTArtworkType fat) {
    List<MediaArtwork> mal = new ArrayList<MediaArtwork>();

    for (FTArtwork aw : awl) {
      MediaArtwork ma = new MediaArtwork();
      ma.setProviderId(providerInfo.getId());
      ma.setType(mapFTArtworkTypeToMediaArtworkType(fat));
      ma.setDefaultUrl(aw.getUrl());
      ma.setPreviewUrl(aw.getUrl().replace("/fanart/", "/preview/"));
      ma.setLanguage(aw.getLanguage());
      ma.setLikes(aw.getLikes());
      try {
        ma.setSeason(Integer.valueOf(aw.getSeason()));
      }
      catch (Exception e) {
      }

      // set resolution
      switch (fat) {
        case HDMOVIECLEARART:
        case HDCLEARART:
        case MOVIETHUMB:
          ma.addImageSize(1000, 562, aw.getUrl());
          ma.setSizeOrder(FanartSizes.MEDIUM.getOrder());
          break;

        case SEASONTHUMB:
        case TVTHUMB:
          ma.addImageSize(500, 281, aw.getUrl());
          ma.setSizeOrder(FanartSizes.MEDIUM.getOrder());
          break;

        case MOVIEBACKGROUND:
        case SHOWBACKGROUND:
          ma.addImageSize(1920, 1080, aw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case MOVIEPOSTER:
        case TVPOSTER:
          ma.addImageSize(1000, 1426, aw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case TVBANNER:
        case MOVIEBANNER:
          ma.addImageSize(1000, 185, aw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case HDMOVIELOGO:
        case HDTVLOGO:
          ma.addImageSize(800, 310, aw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case CLEARLOGO:
        case MOVIELOGO:
          ma.addImageSize(400, 155, aw.getUrl());
          ma.setSizeOrder(FanartSizes.MEDIUM.getOrder());
          break;

        case CLEARART:
        case MOVIEART:
          ma.addImageSize(500, 281, aw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case CDART:
        case MOVIEDISC:
          ma.addImageSize(1000, 1000, aw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());

        default:
          break;
      }

      mal.add(ma);
    }

    return mal;
  }

  /**
   * Maps Fanart types to ours
   * 
   * @see FTArtworkType
   * @param fat
   *          Fanart type
   * @return MediaArtworkType
   */
  private static MediaArtworkType mapFTArtworkTypeToMediaArtworkType(FTArtworkType fat) {
    MediaArtworkType mat = null;
    // @formatter:off
    switch (fat) {
      // TV Artwork
      case  CLEARART:          mat = MediaArtworkType.CLEARART; break;
      case  CLEARLOGO:         mat = MediaArtworkType.LOGO; break;
      case  SEASONTHUMB:       mat = MediaArtworkType.SEASON; break;
      case  TVTHUMB:           mat = MediaArtworkType.THUMB; break;
      case  CHARACTERART:      mat = MediaArtworkType.ACTOR; break;
      case  SHOWBACKGROUND:    mat = MediaArtworkType.BACKGROUND; break;
      case  HDTVLOGO:          mat = MediaArtworkType.LOGO; break;
      case  HDCLEARART:        mat = MediaArtworkType.CLEARART; break;
      case  TVPOSTER:          mat = MediaArtworkType.POSTER; break;
      case  TVBANNER:          mat = MediaArtworkType.BANNER; break;
      case  SEASONPOSTER:      mat = MediaArtworkType.SEASON; break;
      case  SEASONBANNER:      mat = MediaArtworkType.BANNER; break;
      // Movie Artwork Types
      case  MOVIELOGO:         mat = MediaArtworkType.LOGO; break;
      case  MOVIEDISC:         mat = MediaArtworkType.DISC; break;
      case  MOVIEART:          mat = MediaArtworkType.CLEARART; break;
      case  MOVIEBACKGROUND:   mat = MediaArtworkType.BACKGROUND; break;
      case  MOVIETHUMB:        mat = MediaArtworkType.THUMB; break;
      case  MOVIEBANNER:       mat = MediaArtworkType.BANNER; break;
      case  HDMOVIELOGO:       mat = MediaArtworkType.LOGO; break;
      case  HDMOVIECLEARART:   mat = MediaArtworkType.CLEARART; break;
      case  MOVIEPOSTER:       mat = MediaArtworkType.POSTER; break;
      // Music Artwork Types
      case  CDART:             mat = MediaArtworkType.DISC; break;
      case  ARTISTBACKGROUND:  mat = MediaArtworkType.BACKGROUND; break;
      case  ALBUMCOVER:        mat = MediaArtworkType.POSTER; break;
      case  MUSICLOGO:         mat = MediaArtworkType.LOGO; break;
      case  ARTISTTHUMB:       mat = MediaArtworkType.ACTOR; break;
      case  HDMUSICLOGO:       mat = MediaArtworkType.LOGO; break;
      case  MUSICBANNER:       mat = MediaArtworkType.BANNER; break;
      default:
        break;
    }
    // @formatter:on
    return mat;
  }

  /**
   * maps our artwork types to Fanart artwork ones<br>
   * you get multiple ones, b/c our CLERART can map to CLEARART and <b>HD</b>CLEANART
   * 
   * @param mt
   *          our media type (like movie or tv)
   * @param mat
   *          our artwork type
   * @return FTArtworkType fat
   */
  private static List<FTArtworkType> mapMediaArtworkTypeToFTArtworkType(MediaType mt, MediaArtworkType mat) {
    List<FTArtworkType> fat = new ArrayList<FTArtworkType>(1); // usually only one
    // @formatter:off
    if (mt == MediaType.MOVIE || mt == MediaType.MOVIE_SET) {
      switch (mat) {
        case  LOGO:              fat.add(FTArtworkType.MOVIELOGO);
                                 fat.add(FTArtworkType.HDMOVIELOGO);       break;
        case  CLEARART:          fat.add(FTArtworkType.MOVIEART);
                                 fat.add(FTArtworkType.HDMOVIECLEARART);   break;
        case  DISC:              fat.add(FTArtworkType.MOVIEDISC);         break;
        case  BACKGROUND:        fat.add(FTArtworkType.MOVIEBACKGROUND);   break;
        case  THUMB:             fat.add(FTArtworkType.MOVIETHUMB);        break;
        case  BANNER:            fat.add(FTArtworkType.MOVIEBANNER);       break;
        case  POSTER:            fat.add(FTArtworkType.MOVIEPOSTER);       break;
        case  ACTOR:             fat.add(FTArtworkType.CHARACTERART);      break;
        default:
          break;
      }
    } else if (mt == MediaType.TV_SHOW || mt == MediaType.TV_EPISODE) {
      switch (mat) {
        case  CLEARART:          fat.add(FTArtworkType.CLEARART);
                                 fat.add(FTArtworkType.HDCLEARART);        break;
        case  LOGO:              fat.add(FTArtworkType.CLEARLOGO);
                                 fat.add(FTArtworkType.HDTVLOGO);          break;
        case  POSTER:            fat.add(FTArtworkType.TVPOSTER);
                                 fat.add(FTArtworkType.SEASONPOSTER);      break;
        case  BANNER:            fat.add(FTArtworkType.TVBANNER);
                                 fat.add(FTArtworkType.SEASONBANNER);      break;
        case  SEASON:            fat.add(FTArtworkType.SEASONTHUMB);       break;
        case  THUMB:             fat.add(FTArtworkType.TVTHUMB);           break;
        case  BACKGROUND:        fat.add(FTArtworkType.SHOWBACKGROUND);    break;
        case  ACTOR:             fat.add(FTArtworkType.CHARACTERART);      break;

        case  DISC:              break; // no DISC for TV on fanart
        default:
          break;
      }
    }
    // @formatter:on
    return fat;
  }
}
