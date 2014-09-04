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
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;

import com.omertron.fanarttvapi.FanartTvApi;
import com.omertron.fanarttvapi.model.FTArtworkType;
import com.omertron.fanarttvapi.model.FanartTvArtwork;

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
        ftv = new FanartTvApi("9314fc8f4c7d4a8b80079da114794891");
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

  private List<MediaArtwork> getMovieArtwork(MediaScrapeOptions options) throws Exception {
    MediaArtworkType artworkType = options.getArtworkType();
    MediaLanguages language = options.getLanguage();
    List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();

    List<FanartTvArtwork> movieImages = null;
    String imdbId = options.getImdbId();
    int tmdbId = options.getTmdbId();
    if (imdbId != null && !imdbId.isEmpty()) {
      LOGGER.debug("getArtwork with IMDB id: " + imdbId);
      movieImages = ftv.getMovieArtwork(imdbId);
    }
    else if (tmdbId != 0) {
      LOGGER.debug("getArtwork with TMDB id: " + tmdbId);
      movieImages = ftv.getMovieArtwork(tmdbId);
    }
    else {
      LOGGER.warn("neither imdb/tmdb set");
      return artwork;
    }

    // sort
    Collections.sort(movieImages, new ArtworkComparator(language));

    for (FanartTvArtwork ftvaw : movieImages) {
      // http://fanart.tv/movie-fanart/

      MediaArtwork ma = extractArtwork(artworkType, ftvaw);

      if (ma != null) {
        ma.setImdbId(imdbId);
        ma.setTmdbId(tmdbId);
        artwork.add(ma);
      }
    }
    return artwork;
  }

  private List<MediaArtwork> getTvShowArtwork(MediaScrapeOptions options) throws Exception {
    MediaArtworkType artworkType = options.getArtworkType();
    MediaLanguages language = options.getLanguage();
    List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();

    List<FanartTvArtwork> tvShowImages = null;
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
      tvShowImages = ftv.getTvArtwork(tvdbId);
    }
    else {
      LOGGER.warn("not tvdbId set");
      return artwork;
    }

    // sort
    Collections.sort(tvShowImages, new ArtworkComparator(language));

    for (FanartTvArtwork ftvaw : tvShowImages) {
      MediaArtwork ma = extractArtwork(artworkType, ftvaw);

      if (ma != null) {
        artwork.add(ma);
      }
    }

    return artwork;
  }

  private MediaArtwork extractArtwork(MediaArtworkType artworkType, FanartTvArtwork ftvaw) {
    MediaArtwork ma = null;
    FTArtworkType type = FTArtworkType.fromString(ftvaw.getType());

    // select desired types
    switch (type) {
      case MOVIEBACKGROUND:
      case MOVIETHUMB:
      case SHOWBACKGROUND:
      case TVTHUMB:
        if (artworkType == MediaArtworkType.BACKGROUND || artworkType == MediaArtworkType.ALL) {
          ma = new MediaArtwork();
          ma.setType(MediaArtworkType.BACKGROUND);
        }
        break;

      case MOVIEPOSTER:
      case TVPOSTER:
        if (artworkType == MediaArtworkType.POSTER || artworkType == MediaArtworkType.ALL) {
          ma = new MediaArtwork();
          ma.setType(MediaArtworkType.POSTER);
        }
        break;

      case SEASONTHUMB:
        if (artworkType == MediaArtworkType.SEASON || artworkType == MediaArtworkType.ALL) {
          ma = new MediaArtwork();
          ma.setType(MediaArtworkType.SEASON);
        }
        break;

      case TVBANNER:
      case MOVIEBANNER:
        if (artworkType == MediaArtworkType.BANNER || artworkType == MediaArtworkType.ALL) {
          ma = new MediaArtwork();
          ma.setType(MediaArtworkType.BANNER);
        }
        break;

      case HDMOVIELOGO:
      case HDTVLOGO:
      case CLEARLOGO:
      case MOVIELOGO:
        if (artworkType == MediaArtworkType.LOGO || artworkType == MediaArtworkType.ALL) {
          ma = new MediaArtwork();
          ma.setType(MediaArtworkType.LOGO);
        }
        break;

      case HDMOVIECLEARART:
      case CLEARART:
      case MOVIEART:
      case HDCLEARART:
        if (artworkType == MediaArtworkType.CLEARART || artworkType == MediaArtworkType.ALL) {
          ma = new MediaArtwork();
          ma.setType(MediaArtworkType.CLEARART);
        }
        break;

      case CDART:
      case MOVIEDISC:
        if (artworkType == MediaArtworkType.DISC || artworkType == MediaArtworkType.ALL) {
          ma = new MediaArtwork();
          ma.setType(MediaArtworkType.DISC);
        }
        break;

      default:
        return null;
    }

    if (ma != null) {
      ma.setDefaultUrl(ftvaw.getUrl());
      ma.setPreviewUrl(ftvaw.getUrl().replace("/fanart/", "/preview/"));
      // System.out.println("***" + ma.getDefaultUrl() + " -> " + ma.getPreviewUrl());
      ma.setProviderId(getProviderInfo().getId());
      ma.setLanguage(ftvaw.getLanguage());

      // resolution
      switch (type) {
        case HDMOVIECLEARART:
        case HDCLEARART:
        case MOVIETHUMB:
          ma.addImageSize(1000, 562, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.MEDIUM.getOrder());
          break;

        case SEASONTHUMB:
        case TVTHUMB:
          ma.addImageSize(500, 281, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.MEDIUM.getOrder());
          break;

        case MOVIEBACKGROUND:
        case SHOWBACKGROUND:
          ma.addImageSize(1920, 1080, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case MOVIEPOSTER:
        case TVPOSTER:
          ma.addImageSize(1000, 1426, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case TVBANNER:
        case MOVIEBANNER:
          ma.addImageSize(1000, 185, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case HDMOVIELOGO:
        case HDTVLOGO:
          ma.addImageSize(800, 310, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case CLEARLOGO:
        case MOVIELOGO:
          ma.addImageSize(400, 155, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.MEDIUM.getOrder());
          break;

        case CLEARART:
        case MOVIEART:
          ma.addImageSize(500, 281, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;

        case CDART:
        case MOVIEDISC:
          ma.addImageSize(1000, 1000, ftvaw.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());

        default:
          break;
      }
    }

    return ma;
  }

  private static class ArtworkComparator implements Comparator<FanartTvArtwork> {
    private MediaLanguages preferredLangu = MediaLanguages.en;

    public ArtworkComparator(MediaLanguages language) {
      this.preferredLangu = language;
    }

    /*
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(FanartTvArtwork arg0, FanartTvArtwork arg1) {
      String preferredLangu = this.preferredLangu.name();

      // check if first image is preferred langu
      if (preferredLangu.equals(arg0.getLanguage()) && !preferredLangu.equals(arg1.getLanguage())) {
        return -1;
      }

      // check if second image is preferred langu
      if (!preferredLangu.equals(arg0.getLanguage()) && preferredLangu.equals(arg1.getLanguage())) {
        return 1;
      }

      // check if the first image is en
      if ("en".equals(arg0.getLanguage()) && !"en".equals(arg1.getLanguage())) {
        return -1;
      }

      // check if the second image is en
      if (!"en".equals(arg0.getLanguage()) && "en".equals(arg1.getLanguage())) {
        return 1;
      }

      // we did not sort until here; so lets sort with the rating
      if (arg0.getLikes() == arg1.getLikes()) {
        return 0;
      }
      else {
        return arg0.getLikes() > arg1.getLikes() ? -1 : 1;
      }
    }
  }
}
