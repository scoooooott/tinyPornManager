/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.MetadataUtil;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.Movie;
import com.uwetrottmann.trakt5.entities.SearchResult;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.Type;

import retrofit2.Response;

/**
 * The class TraktMovieMetadataProvider is used to provide metadata for movies from trakt.tv
 */

class TraktMovieMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TraktMovieMetadataProvider.class);

  final TraktV2               api;

  public TraktMovieMetadataProvider(TraktV2 api) {
    this.api = api;
  }

  List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());

    if (options.getMediaType() != MediaType.MOVIE) {
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
      Response<List<SearchResult>> response;
      if (year != 0) {
        response = api.search().textQuery(searchString, Type.MOVIE, year, 1, 25).execute();
      }
      else {
        response = api.search().textQuery(searchString, Type.MOVIE, null, 1, 25).execute();
      }
      searchResults = response.body();
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
      MediaSearchResult mediaSearchResult = new MediaSearchResult(TraktMetadataProvider.providerInfo.getId(), MediaType.MOVIE);

      mediaSearchResult.setTitle(result.movie.title);
      mediaSearchResult.setYear((result.movie.year));
      mediaSearchResult.setId((result.movie.ids.trakt).toString());
      mediaSearchResult.setIMDBId(result.movie.ids.imdb);
      mediaSearchResult.setPosterUrl(result.movie.images.poster.full);

      mediaSearchResult.setScore(MetadataUtil.calculateScore(searchString, mediaSearchResult.getTitle()));

      results.add(mediaSearchResult);
    }

    return results;
  }

  MediaMetadata scrape(MediaScrapeOptions options) throws Exception {

    Movie result;
    MediaMetadata metadata = new MediaMetadata(TraktMetadataProvider.providerInfo.getId());

    if (options.getType() != MediaType.MOVIE)
      throw new UnsupportedMediaTypeException(options.getType());

    LOGGER.debug("Scraping... Trakt ID");

    Response<Movie> response = api.movies().summary(options.getId(TraktMetadataProvider.providerInfo.getId()), Extended.FULL).execute();
    result = response.body();

    metadata.setId(TraktMetadataProvider.providerInfo.getId(), result.ids.trakt);
    metadata.setId(MediaMetadata.IMDB, result.ids.imdb);

    if (result.rating != null) {
      metadata.setRating(result.rating.floatValue());
    }

    metadata.setRuntime(result.runtime);
    metadata.setOriginalTitle(result.title);
    metadata.setPlot(result.overview);
    metadata.setVoteCount(result.votes);
    metadata.setId(MediaMetadata.TMDB, result.ids.tmdb);
    metadata.setYear(result.year);
    metadata.setTitle(result.title);

    return metadata;
  }
}
