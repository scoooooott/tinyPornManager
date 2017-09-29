/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.scraper.tmdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.ListUtils;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.Image;
import com.uwetrottmann.tmdb2.entities.Images;

/**
 * The class TmdbArtworkProvider. For managing all artwork provided tasks with tmdb
 */
class TmdbArtworkProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbArtworkProvider.class);

  private Tmdb                api;

  public TmdbArtworkProvider(Tmdb api) {
    this.api = api;
  }

  /**
   * get the artwork for the given type/id
   * 
   * @param options
   *          the options for getting the artwork
   * @return a list of all found artworks
   * @throws Exception
   *           any exception which can be thrown while scraping
   */
  List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getArtwork() " + options.toString());
    MediaArtwork.MediaArtworkType artworkType = options.getArtworkType();

    int tmdbId = options.getTmdbId();
    String imdbId = options.getImdbId();

    if (tmdbId == 0 && StringUtils.isNotEmpty(imdbId)) {
      // try to get tmdbId via imdbId
      tmdbId = new TmdbMovieMetadataProvider(api).getTmdbIdFromImdbId(imdbId);
    }

    if (tmdbId == 0) {
      LOGGER.debug("Cannot get artwork - neither imdb/tmdb set");
      return new ArrayList<>(0);
    }

    Images images = null;
    synchronized (api) {
      // posters and fanart
      switch (options.getType()) {
        case MOVIE:
          images = api.moviesService().images(tmdbId, null).execute().body();
          break;

        case MOVIE_SET:
          images = api.collectionService().images(tmdbId, null).execute().body();
          break;

        case TV_SHOW:
          images = api.tvService().images(tmdbId, null).execute().body();
          break;

        case TV_EPISODE:
          int seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR, -1);
          int episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR, -1);

          if (seasonNr > -1 && episodeNr > -1) {
            images = api.tvEpisodesService().images(tmdbId, seasonNr, episodeNr).execute().body();
          }
          break;
      }
    }

    if (images == null) {
      return new ArrayList<>(0);
    }

    List<MediaArtwork> artwork = prepareArtwork(images, artworkType, tmdbId, options);

    // buffer the artwork
    MediaMetadata md = options.getMetadata();
    if (md != null) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

  private List<MediaArtwork> prepareArtwork(Images tmdbArtwork, MediaArtwork.MediaArtworkType artworkType, int tmdbId, MediaScrapeOptions options) {
    List<MediaArtwork> artwork = new ArrayList<>();
    String baseUrl = TmdbMetadataProvider.configuration.images.base_url;

    if (tmdbArtwork == null) {
      return artwork;
    }

    // first sort the artwork
    if (tmdbArtwork.posters != null) {
      Collections.sort(tmdbArtwork.posters, new ImageComparator(options.getLanguage().getLanguage()));
    }
    if (tmdbArtwork.backdrops != null) {
      Collections.sort(tmdbArtwork.backdrops, new ImageComparator(options.getLanguage().getLanguage()));
    }

    // prepare posters
    if (artworkType == MediaArtwork.MediaArtworkType.POSTER || artworkType == MediaArtwork.MediaArtworkType.ALL) {
      for (Image image : ListUtils.nullSafe(tmdbArtwork.posters)) {
        MediaArtwork ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtworkType.POSTER);
        ma.setPreviewUrl(baseUrl + "w185" + image.file_path);
        ma.setLanguage(image.iso_639_1);
        ma.setTmdbId(tmdbId);

        // add different sizes
        // original
        ma.addImageSize(image.width, image.height, baseUrl + "original" + image.file_path);
        // w500
        if (500 < image.width) {
          ma.addImageSize(500, image.height * 500 / image.width, baseUrl + "w500" + image.file_path);
        }
        // w342
        if (342 < image.width) {
          ma.addImageSize(342, image.height * 342 / image.width, baseUrl + "w342" + image.file_path);
        }
        // w185
        if (185 < image.width) {
          ma.addImageSize(185, image.height * 185 / image.width, baseUrl + "w185" + image.file_path);
        }

        // categorize image size and write default url
        prepareDefaultPoster(ma, options);

        artwork.add(ma);
      }
    }

    if (artworkType == MediaArtwork.MediaArtworkType.BACKGROUND || artworkType == MediaArtwork.MediaArtworkType.ALL) {
      for (Image image : ListUtils.nullSafe(tmdbArtwork.backdrops)) {
        MediaArtwork ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtworkType.BACKGROUND);
        ma.setPreviewUrl(baseUrl + "w300" + image.file_path);
        ma.setLanguage(image.iso_639_1);
        ma.setTmdbId(tmdbId);

        // add different sizes
        // original (most of the time 1920x1080)
        ma.addImageSize(image.width, image.height, baseUrl + "original" + image.file_path);
        // 1280x720
        if (1280 < image.width) {
          ma.addImageSize(1280, image.height * 1280 / image.width, baseUrl + "w1280" + image.file_path);
        }
        // w300
        if (300 < image.width) {
          ma.addImageSize(300, image.height * 300 / image.width, baseUrl + "w300" + image.file_path);
        }

        // categorize image size and write default url
        prepareDefaultFanart(ma, options);

        artwork.add(ma);
      }
    }

    return artwork;
  }

  private void prepareDefaultPoster(MediaArtwork ma, MediaScrapeOptions options) {
    for (MediaArtwork.ImageSizeAndUrl image : ma.getImageSizes()) {
      // LARGE
      if (image.getWidth() >= 1000) {
        if (options.getPosterSize().getOrder() >= MediaArtwork.PosterSizes.LARGE.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(MediaArtwork.PosterSizes.LARGE.getOrder());
          break;
        }
        continue;
      }
      // BIG
      if (image.getWidth() >= 500) {
        if (options.getPosterSize().getOrder() >= MediaArtwork.PosterSizes.BIG.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(MediaArtwork.PosterSizes.BIG.getOrder());
          break;
        }
        continue;
      }
      // MEDIUM
      if (image.getWidth() >= 342) {
        if (options.getPosterSize().getOrder() >= MediaArtwork.PosterSizes.MEDIUM.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(MediaArtwork.PosterSizes.MEDIUM.getOrder());
          break;
        }
        continue;
      }
      // SMALL
      if (image.getWidth() >= 185) {
        if (options.getPosterSize() == MediaArtwork.PosterSizes.SMALL) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(MediaArtwork.PosterSizes.SMALL.getOrder());
          break;
        }
        continue;
      }
    }
  }

  private void prepareDefaultFanart(MediaArtwork ma, MediaScrapeOptions options) {
    for (MediaArtwork.ImageSizeAndUrl image : ma.getImageSizes()) {
      // LARGE
      if (image.getWidth() >= 1920) {
        if (options.getFanartSize().getOrder() >= MediaArtwork.FanartSizes.LARGE.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(MediaArtwork.FanartSizes.LARGE.getOrder());
          break;
        }
        continue;
      }
      // MEDIUM
      if (image.getWidth() >= 1280) {
        if (options.getFanartSize().getOrder() >= MediaArtwork.FanartSizes.MEDIUM.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(MediaArtwork.FanartSizes.MEDIUM.getOrder());
          break;
        }
        continue;
      }
      // SMALL
      if (image.getWidth() >= 300) {
        if (options.getFanartSize().getOrder() >= MediaArtwork.FanartSizes.SMALL.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(MediaArtwork.FanartSizes.SMALL.getOrder());
          break;
        }
        continue;
      }
    }
  }

  /*****************************************************************************************
   * local helper classes
   *****************************************************************************************/
  private static class ImageComparator implements Comparator<Image> {
    private String preferredLangu;

    private ImageComparator(String language) {
      this.preferredLangu = language;
    }

    /*
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(Image arg0, Image arg1) {
      // check if first image is preferred langu
      if (preferredLangu.equals(arg0.iso_639_1) && !preferredLangu.equals(arg1.iso_639_1)) {
        return -1;
      }

      // check if second image is preferred langu
      if (!preferredLangu.equals(arg0.iso_639_1) && preferredLangu.equals(arg1.iso_639_1)) {
        return 1;
      }

      // check if the first image is en
      if ("en".equals(arg0.iso_639_1) && !"en".equals(arg1.iso_639_1)) {
        return -1;
      }

      // check if the second image is en
      if (!"en".equals(arg0.iso_639_1) && "en".equals(arg1.iso_639_1)) {
        return 1;
      }

      // if rating is the same, return 0
      if (arg0.vote_average.equals(arg1.vote_average)) {
        return 0;
      }

      // we did not sort until here; so lets sort with the rating
      return arg0.vote_average > arg1.vote_average ? -1 : 1;
    }
  }
}
