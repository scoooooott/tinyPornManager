/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;

import com.omertron.fanarttvapi.FanartTvApi;
import com.omertron.fanarttvapi.model.FTArtworkType;
import com.omertron.fanarttvapi.model.FanartTvArtwork;

/**
 * The Class FanartTvMetadataProvider.
 * 
 * @author Myron Boyle (myron0815@gmx.net)
 */
public class FanartTvMetadataProvider implements IMediaArtworkProvider {

  /** The Constant LOGGER. */
  private static final Logger      LOGGER       = LoggerFactory.getLogger(FanartTvMetadataProvider.class);

  /** The Constant BASE_URL. */
  private static final String      BASE_URL     = "http://www.fanart.tv";

  /** The provider info. */
  private static MediaProviderInfo providerInfo = new MediaProviderInfo("fanart", "fanart.tv", "Scraper for fanarts");

  /** The ftv. */
  private FanartTvApi              ftv          = null;

  /**
   * Instantiates a new FanartTv metadata provider.
   * 
   * @throws Exception
   *           the exception
   */
  public FanartTvMetadataProvider() throws Exception {
    if (ftv == null) {
      try {
        ftv = new FanartTvApi("9314fc8f4c7d4a8b80079da114794891 ");
      }
      catch (Exception e) {
        LOGGER.error("FanartTvMetadataProvider", e);
        throw e;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaArtworkProvider#getProviderInfo()
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaArtworkProvider#getArtwork(org.
   * tinymediamanager.scraper.MediaScrapeOptions)
   */
  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getArtwork() " + options.toString());
    MediaArtworkType artworkType = options.getArtworkType();
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
    Collections.sort(movieImages, new ArtworkComparator());

    for (FanartTvArtwork ftvaw : movieImages) {
      // http://fanart.tv/movie-fanart/

      FTArtworkType type = FTArtworkType.fromString(ftvaw.getType());

      if ((type == FTArtworkType.HDCLEARART || type == FTArtworkType.MOVIEBACKGROUND || type == FTArtworkType.MOVIETHUMB)
          && (artworkType == MediaArtworkType.BACKGROUND || artworkType == MediaArtworkType.ALL)) {
        MediaArtwork ma = new MediaArtwork();
        ma.setDefaultUrl(ftvaw.getUrl());
        ma.setPreviewUrl(ftvaw.getUrl() + "/preview");
        ma.setProviderId(getProviderInfo().getId());
        ma.setType(MediaArtworkType.BACKGROUND);
        ma.setLanguage(ftvaw.getLanguage());
        ma.setImdbId(imdbId);
        ma.setTmdbId(tmdbId);

        switch (type) {
          case HDCLEARART:
          case MOVIETHUMB:
            ma.addImageSize(1000, 562, ftvaw.getUrl());
            break;

          case MOVIEBACKGROUND:
            ma.addImageSize(1920, 1080, ftvaw.getUrl());
            break;
        }

        artwork.add(ma);
      }

      // if (type == FTArtworkType.MOVIEBANNER && (artworkType ==
      // MediaArtworkType.BANNER || artworkType == MediaArtworkType.ALL)) {
      // MediaArtwork ma = new MediaArtwork();
      // ma.setDefaultUrl(ftvaw.getUrl());
      // ma.setPreviewUrl(ftvaw.getUrl() + "/preview");
      // ma.setProviderId(getProviderInfo().getId());
      // ma.setType(MediaArtworkType.BANNER);
      // ma.setLanguage(ftvaw.getLanguage());
      // ma.setImdbId(imdbId);
      // ma.setTmdbId(tmdbId);
      //
      // artwork.add(ma);
      // }
    }

    // buffer the artwork
    MediaMetadata md = options.getMetadata();
    if (md != null) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

  /**
   * The Class ArtworkComparator.
   */
  private static class ArtworkComparator implements Comparator<FanartTvArtwork> {
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * 
     * sort artwork: primary by language: preferred lang (ie de), en, others;
     * then: score
     */
    @Override
    public int compare(FanartTvArtwork arg0, FanartTvArtwork arg1) {
      String preferredLangu = Globals.settings.getScraperLanguage().name();

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
      return arg0.getLikes() > arg1.getLikes() ? -1 : 1;
    }

  }
}
