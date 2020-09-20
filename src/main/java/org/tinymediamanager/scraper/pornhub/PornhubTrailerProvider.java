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
package org.tinymediamanager.scraper.pornhub;

import com.scott.pornhub.Pornhub;
import com.scott.pornhub.entities.Videos;
import com.scott.pornhub.entities.Videos.Video;
import com.scott.pornhub.enumerations.VideoType;
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

import static org.tinymediamanager.scraper.pornhub.PornhubMetadataProvider.getRequestLanguage;

/**
 * The class PornhubTrailerProvider. For managing all trailer provided tasks with pornhub
 */
class PornhubTrailerProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(PornhubTrailerProvider.class);

  private final Pornhub          api;

  PornhubTrailerProvider(Pornhub api) {
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

    int pornhubId = options.getPornhubId();
    String imdbId = options.getImdbId();

    if (pornhubId == 0 && StringUtils.isNotEmpty(imdbId)) {
      // try to get pornhubId via imdbId
      pornhubId = new PornhubMetadataProvider().getPornhubIdFromImdbId(imdbId, options.getMediaType());
    }

    if (pornhubId == 0) {
      LOGGER.warn("not possible to scrape from PORNHUB - no pornhubId found");
      throw new MissingIdException(MediaMetadata.PORNHUB, MediaMetadata.IMDB);
    }

    String language = getRequestLanguage(options.getLanguage());

    LOGGER.debug("PORNHUB: getTrailers(pornhubId): {}", pornhubId);

    List<Video> videos = new ArrayList<>();
    synchronized (api) {
      // get trailers from pornhub (with specified langu and without)
      try {
        if (options.getMediaType() == MediaType.MOVIE) {
          Videos pornhubVideos = api.moviesService().videos(pornhubId, language).execute().body();
          Videos pornhubVideosWoLang = api.moviesService().videos(pornhubId, "").execute().body();

          videos.addAll(pornhubVideos.results);
          videos.addAll(pornhubVideosWoLang.results);

        } else if (options.getMediaType() == MediaType.TV_SHOW) {
          Videos pornhubVideos = api.tvService().videos(pornhubId, language).execute().body();
          Videos pornhubVideosWoLang = api.tvService().videos(pornhubId, "").execute().body();

          videos.addAll(pornhubVideos.results);
          videos.addAll(pornhubVideosWoLang.results);
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
