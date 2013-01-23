/*
 * Copyright 2012 Manuel Laggner
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
import java.util.List;

import org.apache.log4j.Logger;
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
  private static final Logger      LOGGER       = Logger.getLogger(FanartTvMetadataProvider.class);

  private static final String      BASE_URL     = "http://www.fanart.tv";

  /** The provider info. */
  private static MediaProviderInfo providerInfo = new MediaProviderInfo("fanart", "fanart.tv", "Scraper for fanarts");

  private FanartTvApi              ftv          = null;

  /**
   * Instantiates a new FanartTv metadata provider.
   * 
   * @throws Exception
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

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

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

    for (FanartTvArtwork ftvaw : movieImages) {
      // http://fanart.tv/movie-fanart/
      FTArtworkType type = FTArtworkType.fromString(ftvaw.getType());

      if (type == FTArtworkType.HDCLEARART || type == FTArtworkType.MOVIEBACKGROUND || type == FTArtworkType.MOVIETHUMB
          && (artworkType == MediaArtworkType.BACKGROUND || artworkType == MediaArtworkType.ALL)) {
        MediaArtwork ma = new MediaArtwork();
        ma.setDownloadUrl(ftvaw.getUrl());
        ma.setLabel("Backdrop");
        ma.setProviderId(getProviderInfo().getId());
        ma.setType(MediaArtworkType.BACKGROUND);
        ma.setImdbId(imdbId);
        ma.setTmdbId(tmdbId);
        artwork.add(ma);
      }

      if (type == FTArtworkType.MOVIEBANNER && (artworkType == MediaArtworkType.BANNER || artworkType == MediaArtworkType.ALL)) {
        MediaArtwork ma = new MediaArtwork();
        ma.setDownloadUrl(ftvaw.getUrl());
        ma.setLabel("Banner");
        ma.setProviderId(getProviderInfo().getId());
        ma.setType(MediaArtworkType.BANNER);
        ma.setImdbId(imdbId);
        ma.setTmdbId(tmdbId);
        artwork.add(ma);
      }
    }

    // buffer the artwork
    MediaMetadata md = options.getMetadata();
    if (md != null) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

}