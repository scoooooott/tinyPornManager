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
import com.scott.pornhub.entities.BaseCollection;
import com.scott.pornhub.entities.BaseMovie;
import com.scott.pornhub.entities.Collection;
import com.scott.pornhub.entities.CollectionResultsPage;
import com.scott.pornhub.exceptions.PornhubNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieSetSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import static org.tinymediamanager.scraper.pornhub.PornhubMetadataProvider.getRequestLanguage;
import static org.tinymediamanager.scraper.pornhub.PornhubMetadataProvider.providerInfo;

/**
 * The class PornhubMovieSetMetadataProvider is used to provide metadata for moviesets from pornhub
 */
class PornhubMovieSetMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(PornhubMovieSetMetadataProvider.class);

  private final Pornhub          api;

  PornhubMovieSetMetadataProvider(Pornhub api) {
    this.api = api;
  }

  /**
   * searches a movie set with the given query parameters
   *
   * @param query
   *          the query parameters
   * @return a list of found movie sets
   * @throws ScrapeException
   *           any exception which can be thrown while searching
   */
  List<MediaSearchResult> search(MovieSetSearchAndScrapeOptions query) throws ScrapeException {
    LOGGER.debug("search() - {}", query.toString());

    List<MediaSearchResult> movieSetsFound = new ArrayList<>();

    String searchString = "";
    if (StringUtils.isNotEmpty(query.getSearchQuery())) {
      searchString = Utils.removeSortableName(query.getSearchQuery());
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("PORNHUB Scraper: empty searchString");
      return movieSetsFound;
    }

    String language = getRequestLanguage(query.getLanguage());

    synchronized (api) {
      try {
        CollectionResultsPage resultsPage = api.searchService().collection(searchString, 1, language).execute().body();
        if (resultsPage != null) {
          for (BaseCollection collection : ListUtils.nullSafe(resultsPage.results)) {
            MediaSearchResult searchResult = new MediaSearchResult(PornhubMetadataProvider.providerInfo.getId(), MediaType.MOVIE_SET);
            searchResult.setId(Integer.toString(collection.id));
            searchResult.setTitle(collection.name);
            searchResult.setPosterUrl(PornhubMetadataProvider.configuration.images.base_url + "w342" + collection.poster_path);
            searchResult.setScore(MetadataUtil.calculateScore(searchString, collection.name));
            if (searchResult.getScore() < 0.5 && providerInfo.getConfig().getValueAsBool("titleFallback")) {
              if (verifyMovieSetTitleLanguage(movieSetsFound, resultsPage, query)) {
                break;
              }
            }
            movieSetsFound.add(searchResult);
          }
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to search: {}", e.getMessage());
        throw new ScrapeException(e);
      }
    }

    LOGGER.info("found {} results ", movieSetsFound.size());
    return movieSetsFound;
  }

  /**
   * Language Fallback Mechanism - For Search Results
   *
   * @param query
   *          the query options
   * @param original
   *          the original movie set list
   * @param movieSetsFound
   *          the list that movie sets will be added.
   */
  private boolean verifyMovieSetTitleLanguage(List<MediaSearchResult> movieSetsFound, CollectionResultsPage original,
      MovieSetSearchAndScrapeOptions query) throws Exception {

    String lang = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name().replace("_", "-");
    CollectionResultsPage fallBackResultsPage = api.searchService().collection(query.getSearchQuery(), 1, lang).execute().body();

    if (fallBackResultsPage != null && original.results != null && fallBackResultsPage.results != null) {

      movieSetsFound.clear();

      for (int i = 0; i < original.results.size(); i++) {
        BaseCollection originalCollection = original.results.get(i);
        BaseCollection fallbackCollection = fallBackResultsPage.results.get(i);

        MediaSearchResult searchResult = new MediaSearchResult(PornhubMetadataProvider.providerInfo.getId(), MediaType.MOVIE_SET);

        searchResult.setId(Integer.toString(originalCollection.id));

        if (MetadataUtil.calculateScore(query.getSearchQuery(), originalCollection.name) >= MetadataUtil.calculateScore(query.getSearchQuery(),
            fallbackCollection.name)) {
          searchResult.setTitle(originalCollection.name);
          searchResult.setPosterUrl(PornhubMetadataProvider.configuration.images.base_url + "w342" + originalCollection.poster_path);
          searchResult.setScore(MetadataUtil.calculateScore(query.getSearchQuery(), originalCollection.name));
        }
        else {
          searchResult.setTitle(fallbackCollection.name);
          searchResult.setPosterUrl(PornhubMetadataProvider.configuration.images.base_url + "w342" + fallbackCollection.poster_path);
          searchResult.setScore(MetadataUtil.calculateScore(query.getSearchQuery(), fallbackCollection.name));
        }
        movieSetsFound.add(searchResult);
      }
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Get the movie set metadata for the given search options
   *
   * @param options
   *          the options for scraping
   * @return the metadata (never null)
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   */
  MediaMetadata getMetadata(MovieSetSearchAndScrapeOptions options) throws MissingIdException, ScrapeException, NothingFoundException {
    MediaMetadata md = new MediaMetadata(PornhubMetadataProvider.providerInfo.getId());

    // pornhubId from option
    int pornhubId = options.getPornhubId();

    if (pornhubId == 0) {
      LOGGER.warn("not possible to scrape from PORNHUB - no pornhubId found");
      throw new MissingIdException(MediaMetadata.PORNHUB_SET);
    }

    String language = getRequestLanguage(options.getLanguage());

    Collection collection = null;
    synchronized (api) {
      try {
        collection = api.collectionService().summary(pornhubId, language).execute().body();
        // if collection title/overview is not availbale, rescrape in the fallback language
        if (collection != null && (StringUtils.isBlank(collection.overview) || StringUtils.isBlank(collection.name))
            && providerInfo.getConfig().getValueAsBool("titleFallback")) {

          String fallbackLang = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name().replace("_", "-");
          Collection collectionInFallbackLanguage = api.collectionService().summary(pornhubId, fallbackLang).execute().body();

          if (collectionInFallbackLanguage != null) {
            Collection collectionInDefaultLanguage = null;
            if (StringUtils.isBlank(collectionInFallbackLanguage.name) || StringUtils.isBlank(collectionInFallbackLanguage.overview)) {
              collectionInDefaultLanguage = api.collectionService().summary(pornhubId, null).execute().body();

            }

            if (StringUtils.isBlank(collection.name) && StringUtils.isNotBlank(collectionInFallbackLanguage.name)) {
              collection.name = collectionInFallbackLanguage.name;
            }
            else if (StringUtils.isBlank(collection.name) && collectionInDefaultLanguage != null
                && StringUtils.isNotBlank(collectionInDefaultLanguage.name)) {
              collection.name = collectionInDefaultLanguage.name;
            }

            if (StringUtils.isBlank(collection.overview) && StringUtils.isNotBlank(collectionInFallbackLanguage.overview)) {
              collection.overview = collectionInFallbackLanguage.overview;
            }
            else if (StringUtils.isBlank(collection.overview) && collectionInDefaultLanguage != null
                && StringUtils.isNotBlank(collectionInDefaultLanguage.overview)) {
              collection.overview = collectionInDefaultLanguage.overview;
            }

            for (BaseMovie movie : collection.parts) {
              for (BaseMovie fallbackMovie : collectionInFallbackLanguage.parts) {
                if (movie.id.equals(fallbackMovie.id)) {
                  if (StringUtils.isBlank(movie.overview) && !StringUtils.isBlank(fallbackMovie.overview)) {
                    movie.overview = fallbackMovie.overview;
                  }
                  if (movie.title.equals(movie.original_title) && !movie.original_language.equals(options.getLanguage().getLanguage())
                      && !StringUtils.isBlank(fallbackMovie.title)) {
                    movie.title = fallbackMovie.title;
                  }
                  break;
                }
              }
            }
          }
        }
      }
      catch (PornhubNotFoundException e) {
        LOGGER.info("nothing found");
      }
      catch (Exception e) {
        LOGGER.debug("failed to get meta data: {}", e.getMessage());
        throw new ScrapeException(e);
      }
    }

    if (collection == null) {
      throw new NothingFoundException();
    }

    md.setId(MediaMetadata.PORNHUB_SET, collection.id);
    md.setTitle(collection.name);
    md.setPlot(collection.overview);

    // Poster
    if (StringUtils.isNotBlank(collection.poster_path)) {
      MediaArtwork ma = new MediaArtwork(PornhubMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      ma.setPreviewUrl(PornhubMetadataProvider.configuration.images.base_url + "w185" + collection.poster_path);
      ma.setDefaultUrl(PornhubMetadataProvider.configuration.images.base_url + "original" + collection.poster_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setPornhubId(pornhubId);
      md.addMediaArt(ma);
    }

    // Fanart
    if (StringUtils.isNotBlank(collection.backdrop_path)) {
      MediaArtwork ma = new MediaArtwork(PornhubMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.BACKGROUND);
      ma.setPreviewUrl(PornhubMetadataProvider.configuration.images.base_url + "w300" + collection.backdrop_path);
      ma.setDefaultUrl(PornhubMetadataProvider.configuration.images.base_url + "original" + collection.backdrop_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setPornhubId(pornhubId);
      md.addMediaArt(ma);
    }

    // add all movies belonging to this movie set
    for (BaseMovie part : ListUtils.nullSafe(collection.parts)) {
      MediaMetadata mdSubItem = new MediaMetadata(PornhubMetadataProvider.providerInfo.getId());
      mdSubItem.setId(PornhubMetadataProvider.providerInfo.getId(), part.id);
      mdSubItem.setTitle(part.title);

      // Poster
      if (StringUtils.isNotBlank(part.poster_path)) {
        MediaArtwork ma = new MediaArtwork(PornhubMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
        ma.setPreviewUrl(PornhubMetadataProvider.configuration.images.base_url + "w185" + part.poster_path);
        ma.setDefaultUrl(PornhubMetadataProvider.configuration.images.base_url + "original" + part.poster_path);
        ma.setLanguage(options.getLanguage().getLanguage());
        ma.setPornhubId(part.id);
        mdSubItem.addMediaArt(ma);
      }

      // Fanart
      if (StringUtils.isNotBlank(part.backdrop_path)) {
        MediaArtwork ma = new MediaArtwork(PornhubMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.BACKGROUND);
        ma.setPreviewUrl(PornhubMetadataProvider.configuration.images.base_url + "w300" + part.backdrop_path);
        ma.setDefaultUrl(PornhubMetadataProvider.configuration.images.base_url + "original" + part.backdrop_path);
        ma.setLanguage(options.getLanguage().getLanguage());
        ma.setPornhubId(part.id);
        mdSubItem.addMediaArt(ma);
      }

      mdSubItem.setReleaseDate(part.release_date);
      md.addSubItem(mdSubItem);
    }

    return md;
  }
}
