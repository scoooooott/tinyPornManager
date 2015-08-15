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
package org.tinymediamanager.scraper.tmdb;

import com.uwetrottmann.tmdb.Tmdb;
import com.uwetrottmann.tmdb.entities.Collection;
import com.uwetrottmann.tmdb.entities.CollectionResultsPage;
import com.uwetrottmann.tmdb.entities.Part;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.*;
import org.tinymediamanager.scraper.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The class TmdbMovieSetMetadataProvider is used to provide metadata for moviesets from tmdb
 */
class TmdbMovieSetMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbMovieSetMetadataProvider.class);

  private Tmdb api;

  public TmdbMovieSetMetadataProvider(Tmdb api) {
    this.api = api;
  }

  /**
   * searches a movie set with the given query parameters
   *
   * @param query
   *          the query parameters
   * @return a list of found movie sets
   * @throws Exception
   *           any exception which can be thrown while searching
   */
  List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    LOGGER.debug("search() " + query.toString());

    List<MediaSearchResult> movieSetsFound = new ArrayList<MediaSearchResult>();

    String searchString = "";
    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.QUERY))) {
      searchString = query.get(MediaSearchOptions.SearchParam.QUERY);
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.TITLE))) {
      searchString = query.get(MediaSearchOptions.SearchParam.TITLE);
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("TMDB Scraper: empty searchString");
      return movieSetsFound;
    }

    CollectionResultsPage resultsPage = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      resultsPage = api.searchService().collection(searchString, 1, query.get(MediaSearchOptions.SearchParam.LANGUAGE));
    }

    if (resultsPage == null) {
      return movieSetsFound;
    }

    for (Collection collection : ListUtils.nullSafe(resultsPage.results)) {
      MediaSearchResult searchResult = new MediaSearchResult(TmdbMetadataProvider.providerInfo.getId());
      searchResult.setMediaType(MediaType.MOVIE_SET);
      searchResult.setId(Integer.toString(collection.id));
      searchResult.setTitle(collection.name);
      searchResult.setPosterUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + collection.poster_path);
      searchResult.setScore(MetadataUtil.calculateScore(searchString, collection.name));
      movieSetsFound.add(searchResult);
    }

    return movieSetsFound;
  }

  /**
   * Get the movie set metadata for the given search options
   *
   * @param options
   *          the options for scraping
   * @return the metadata (never null)
   * @throws Exception
   *           any exception which can be thrown while scraping
   */
  MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());

    MediaMetadata md = new MediaMetadata(TmdbMetadataProvider.providerInfo.getId());

    int tmdbId = 0;

    // tmdbId from option - own id
    try {
      tmdbId = Integer.parseInt(options.getId(TmdbMetadataProvider.providerInfo.getId()));
    }
    catch (NumberFormatException ignored) {
    }

    // tmdbId from option - legacy id
    if (tmdbId == 0) {
      tmdbId = options.getTmdbId();
    }

    if (tmdbId == 0) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId found");
      return md;
    }

    String language = options.getLanguage().name();

    Collection collection = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      collection = api.collectionService().summary(tmdbId, language, null);

      // if collection title/overview is not availbale, rescrape in en
      if (StringUtils.isBlank(collection.overview) || StringUtils.isBlank(collection.name)) {
        Collection collectionInEn = api.collectionService().summary(tmdbId, "en", null);

        if (StringUtils.isBlank(collection.name) && StringUtils.isNotBlank(collectionInEn.name)) {
          collection.name = collectionInEn.name;
        }

        if (StringUtils.isBlank(collection.overview) && StringUtils.isNotBlank(collectionInEn.overview)) {
          collection.overview = collectionInEn.overview;
        }
      }
    }

    md.setId(MediaMetadata.TMDB_SET, collection.id);
    md.storeMetadata(MediaMetadata.TITLE, collection.name);
    md.storeMetadata(MediaMetadata.PLOT, collection.overview);
    md.storeMetadata(MediaMetadata.POSTER_URL,
        TmdbMetadataProvider.configuration.images.base_url + "w342" + collection.poster_path);
    md.storeMetadata(MediaMetadata.BACKGROUND_URL,
        TmdbMetadataProvider.configuration.images.base_url + "w1280" + collection.backdrop_path);

    // add all movies belonging to this movie set
    for (Part part : ListUtils.nullSafe(collection.parts)) {
      MediaMetadata mdSubItem = new MediaMetadata(TmdbMetadataProvider.providerInfo.getId());
      mdSubItem.setId(TmdbMetadataProvider.providerInfo.getId(), part.id);
      mdSubItem.storeMetadata(MediaMetadata.TITLE, part.title);
      mdSubItem.storeMetadata(MediaMetadata.POSTER_URL,
          TmdbMetadataProvider.configuration.images.base_url + "w342" + part.poster_path);
      mdSubItem.storeMetadata(MediaMetadata.BACKGROUND_URL,
          TmdbMetadataProvider.configuration.images.base_url + "w1280" + part.backdrop_path);
      mdSubItem.storeMetadata(MediaMetadata.RELEASE_DATE, part.release_date);
      md.addSubItem(mdSubItem);
    }

    return md;
  }
}
