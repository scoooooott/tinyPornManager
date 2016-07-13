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
package org.tinymediamanager.scraper.tmdb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.Collection;
import com.uwetrottmann.tmdb2.entities.CollectionResultsPage;
import com.uwetrottmann.tmdb2.entities.Part;

/**
 * The class TmdbMovieSetMetadataProvider is used to provide metadata for moviesets from tmdb
 */
class TmdbMovieSetMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbMovieSetMetadataProvider.class);

  private Tmdb                api;

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
    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.getQuery())) {
      searchString = query.getQuery();
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("TMDB Scraper: empty searchString");
      return movieSetsFound;
    }

    String language = query.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(query.getLanguage().getCountry())) {
      language += "-" + query.getLanguage().getCountry();
    }

    CollectionResultsPage resultsPage = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      resultsPage = api.searchService().collection(searchString, 1, language).execute().body();
    }

    if (resultsPage == null) {
      return movieSetsFound;
    }

    for (Collection collection : ListUtils.nullSafe(resultsPage.results)) {
      MediaSearchResult searchResult = new MediaSearchResult(TmdbMetadataProvider.providerInfo.getId(), MediaType.MOVIE_SET);
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

    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }

    Collection collection = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      collection = api.collectionService().summary(tmdbId, language, null).execute().body();

      // if collection title/overview is not availbale, rescrape in en
      if (StringUtils.isBlank(collection.overview) || StringUtils.isBlank(collection.name)) {
        TmdbConnectionCounter.trackConnections();
        Collection collectionInEn = api.collectionService().summary(tmdbId, "en", null).execute().body();

        if (StringUtils.isBlank(collection.name) && StringUtils.isNotBlank(collectionInEn.name)) {
          collection.name = collectionInEn.name;
        }

        if (StringUtils.isBlank(collection.overview) && StringUtils.isNotBlank(collectionInEn.overview)) {
          collection.overview = collectionInEn.overview;
        }
      }
    }

    md.setId(MediaMetadata.TMDB_SET, collection.id);
    md.setTitle(collection.name);
    md.setPlot(collection.overview);

    // Poster
    MediaArtwork ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
    ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "w185" + collection.poster_path);
    ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + collection.poster_path);
    ma.setLanguage(options.getLanguage().getLanguage());
    ma.setTmdbId(tmdbId);
    md.addMediaArt(ma);

    // Fanart
    ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.BACKGROUND);
    ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "w300" + collection.backdrop_path);
    ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "w1280" + collection.backdrop_path);
    ma.setLanguage(options.getLanguage().getLanguage());
    ma.setTmdbId(tmdbId);
    md.addMediaArt(ma);

    // add all movies belonging to this movie set
    for (Part part : ListUtils.nullSafe(collection.parts)) {
      MediaMetadata mdSubItem = new MediaMetadata(TmdbMetadataProvider.providerInfo.getId());
      mdSubItem.setId(TmdbMetadataProvider.providerInfo.getId(), part.id);
      mdSubItem.setTitle(part.title);

      // Poster
      ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "w185" + part.poster_path);
      ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + part.poster_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setTmdbId(part.id);
      mdSubItem.addMediaArt(ma);

      // Fanart
      ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.BACKGROUND);
      ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "w300" + part.backdrop_path);
      ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "w1280" + part.backdrop_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setTmdbId(part.id);
      mdSubItem.addMediaArt(ma);

      mdSubItem.setReleaseDate(part.release_date);
      md.addSubItem(mdSubItem);
    }

    return md;
  }
}
