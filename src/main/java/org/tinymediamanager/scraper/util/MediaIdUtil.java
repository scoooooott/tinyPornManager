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
package org.tinymediamanager.scraper.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviders;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;

/**
 * The class MediaIdUtil is a helper class for managing ids
 *
 * @author Manuel Laggner
 */
public class MediaIdUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaIdUtil.class);

  private MediaIdUtil() {
    // empty constructor for utility classes
  }

  /**
   * get the imdb id from thetvdb by a given tvdb id
   * 
   * @param tvdbId
   *          the tvdb id
   * @return the imdb id or an empty string
   */
  public static String getImdbIdFromTvdbId(String tvdbId) {
    if (StringUtils.isBlank(tvdbId)) {
      return "";
    }

    String imdbId = "";
    try {
      MediaScraper scraper = MediaScraper.getMediaScraperById(MediaMetadata.TVDB, ScraperType.TV_SHOW);
      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.setId(MediaMetadata.TVDB, tvdbId);
      MediaMetadata md = ((ITvShowMetadataProvider) scraper.getMediaProvider()).getMetadata(options);
      imdbId = (String) md.getId(MediaMetadata.IMDB);
    }
    catch (Exception e) {
      LOGGER.error("could not get imdb id from tvdb id: {}", e.getMessage());
    }

    if (StringUtils.isBlank(imdbId)) {
      return ""; // do not pass null
    }

    return imdbId;
  }

  /**
   * gets the imdb id via tmdb id
   *
   * @param tmdbId
   *          the tmdb id
   * @return the imdb id or an empty String
   */
  public static String getImdbIdViaTmdbId(int tmdbId) {
    if (tmdbId == 0) {
      return "";
    }

    try {
      // call the tmdb metadata provider
      IMediaProvider tmdb = MediaProviders.getProviderById(MediaMetadata.TMDB);
      if (tmdb == null) {
        return "";
      }

      // we just need to "scrape" this movie
      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId(MediaMetadata.TMDB, Integer.toString(tmdbId));
      MediaMetadata md = ((IMovieMetadataProvider) tmdb).getMetadata(options);
      return md.getId(MediaMetadata.IMDB).toString();
    }
    catch (Exception ingored) {
      // nothing to be done here
    }

    return "";
  }
}
