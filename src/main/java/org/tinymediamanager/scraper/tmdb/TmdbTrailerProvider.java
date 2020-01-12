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
package org.tinymediamanager.scraper.tmdb;

import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.getRequestLanguage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.TrailerSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.util.ListUtils;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.Videos;
import com.uwetrottmann.tmdb2.entities.Videos.Video;
import com.uwetrottmann.tmdb2.enumerations.VideoType;

/**
 * The class TmdbTrailerProvider. For managing all trailer provided tasks with tmdb
 */
class TmdbTrailerProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbTrailerProvider.class);

  private final Tmdb          api;

  TmdbTrailerProvider(Tmdb api) {
    this.api = api;
  }

  /**
   * get the trailer for the given type/id
   *
   * @param options
   *          the options for getting the trailers
   * @return a list of all found trailers
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   */
  List<MediaTrailer> getTrailers(TrailerSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    LOGGER.debug("getTrailers(): {}", options);
    List<MediaTrailer> trailers = new ArrayList<>();

    int tmdbId = options.getTmdbId();
    String imdbId = options.getImdbId();

    if (tmdbId == 0 && StringUtils.isNotEmpty(imdbId)) {
      // try to get tmdbId via imdbId
      tmdbId = new TmdbMetadataProvider().getTmdbIdFromImdbId(imdbId, options.getMediaType());
    }

    if (tmdbId == 0) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId found");
      throw new MissingIdException(MediaMetadata.TMDB, MediaMetadata.IMDB);
    }

    String language = getRequestLanguage(options.getLanguage());

    LOGGER.debug("TMDB: getTrailers(tmdbId): {}", tmdbId);

    List<Video> videos = new ArrayList<>();
    synchronized (api) {
      // get trailers from tmdb (with specified langu and without)
      try {
        if (options.getMediaType() == MediaType.MOVIE) {
          Videos tmdbVideos = api.moviesService().videos(tmdbId, language).execute().body();
          Videos tmdbVideosWoLang = api.moviesService().videos(tmdbId, "").execute().body();

          videos.addAll(tmdbVideos.results);
          videos.addAll(tmdbVideosWoLang.results);

        } else if (options.getMediaType() == MediaType.TV_SHOW) {
          Videos tmdbVideos = api.tvService().videos(tmdbId, language).execute().body();
          Videos tmdbVideosWoLang = api.tvService().videos(tmdbId, "").execute().body();

          videos.addAll(tmdbVideos.results);
          videos.addAll(tmdbVideosWoLang.results);
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to get trailer: {}", e.getMessage());
        throw new ScrapeException(e);
      }
    }

    for (Video video : ListUtils.nullSafe(videos)) {
      if (VideoType.TRAILER != video.type) {
        continue;
      }
      MediaTrailer trailer = new MediaTrailer();
      trailer.setName(video.name);
      trailer.setQuality(String.valueOf(video.size) + "p");
      trailer.setProvider(video.site);
      trailer.setUrl(video.key);

      // youtube support
      if ("youtube".equalsIgnoreCase(video.site)) {
        // build url for youtube trailer
        StringBuilder sb = new StringBuilder();
        sb.append("http://www.youtube.com/watch?v=");
        sb.append(video.key);
        if (video.size >= 720) {
          sb.append("&hd=1");
        }
        trailer.setUrl(sb.toString());
      }

      if (!trailers.contains(trailer)) {
        trailers.add(trailer);
      }
    }

    return trailers;
  }
}
