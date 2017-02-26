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
package org.tinymediamanager.scraper.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;

/**
 * The class MediaIdUtil is a helper class for managing ids
 *
 * @author Manuel Laggner
 */
public class MediaIdUtil {
  private final static Logger LOGGER = LoggerFactory.getLogger(MediaIdUtil.class);

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
      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setId(MediaMetadata.TVDB, tvdbId);
      MediaMetadata md = ((ITvShowMetadataProvider) scraper.getMediaProvider()).getMetadata(options);
      imdbId = (String) md.getId(MediaMetadata.IMDB);
    }
    catch (Exception e) {
      LOGGER.error("could not get imdb id from tvdb id: " + e.getMessage());
    }

    if (StringUtils.isBlank(imdbId)) {
      return ""; // do not pass null
    }

    return imdbId;
  }

}
