/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.scraper.trakt;

import com.uwetrottmann.trakt.v2.TraktV2;
import com.uwetrottmann.trakt.v2.entities.SearchResult;
import com.uwetrottmann.trakt.v2.entities.Show;
import com.uwetrottmann.trakt.v2.enums.Extended;
import com.uwetrottmann.trakt.v2.enums.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.*;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.MetadataUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The class TraktMovieMetadataProvider is used to provide metadata for movies from trakt.tv
 */

class TraktTVShowMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TraktTVShowMetadataProvider.class);

  final TraktV2               api;

  public TraktTVShowMetadataProvider(TraktV2 api) {
    this.api = api;
  }

  // Search
  List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());

    if (options.getMediaType() != MediaType.TV_SHOW) {
      throw new UnsupportedMediaTypeException(options.getMediaType());
    }

    String searchString = "";
    int year = 0;

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(options.getQuery())) {
      searchString = options.getQuery();
    }

    if (options.getYear() != 0) {
      try {
        year = options.getYear();
      }
      catch (Exception e) {
        year = 0;
      }
    }

    List<MediaSearchResult> results = new ArrayList<>();
    List<SearchResult> searchResults = null;

    try {
      if (year != 0) {
        searchResults = api.search().textQuery(searchString, Type.SHOW, year, 1, 25);
      }
      else {
        searchResults = api.search().textQuery(searchString, Type.SHOW, null, 1, 25);
      }
    }
    catch (Exception e) {
      LOGGER.error("Problem scraping for " + searchString + "; " + e.getMessage());
    }

    if (searchResults == null || searchResults.isEmpty()) {
      LOGGER.info("nothing found");
      return results;
    }

    // set SearchResult Data for every Entry of the result
    for (SearchResult result : searchResults) {
      MediaSearchResult mediaSearchResult = new MediaSearchResult(TraktMetadataProvider.providerInfo.getId());

      mediaSearchResult.setTitle(result.show.title);
      mediaSearchResult.setYear(result.show.year);
      mediaSearchResult.setId((result.show.ids.trakt).toString());
      mediaSearchResult.setIMDBId(result.show.ids.imdb);
      mediaSearchResult.setProviderId((result.show.ids.trakt).toString());
      mediaSearchResult.setPosterUrl(result.show.images.poster.full);

      mediaSearchResult.setScore(MetadataUtil.calculateScore(searchString, mediaSearchResult.getTitle()));

      results.add(mediaSearchResult);
    }

    return results;
  }

  // Episode List
  List<MediaEpisode> getEpisodeList(MediaScrapeOptions mediaScrapeOptions) throws Exception {

    List<MediaEpisode> results = new ArrayList<MediaEpisode>();
    MediaEpisode mediaEpisode = new MediaEpisode(TraktMetadataProvider.providerInfo.getId());
    Show traktResult;

    traktResult = api.shows().summary("353", Extended.FULL);

    mediaEpisode.title = traktResult.title;

    mediaEpisode.ids.put("trakt", traktResult.ids.trakt);
    mediaEpisode.ids.put("slug", traktResult.ids.slug);
    mediaEpisode.ids.put("tvdb", traktResult.ids.tvdb);
    mediaEpisode.ids.put("imdb", traktResult.ids.imdb);
    mediaEpisode.ids.put("tmdb", traktResult.ids.tmdb);
    mediaEpisode.ids.put("tvrage", traktResult.ids.tvrage);

    mediaEpisode.plot = traktResult.overview;

    results.add(mediaEpisode);

    return results;

  }
}
