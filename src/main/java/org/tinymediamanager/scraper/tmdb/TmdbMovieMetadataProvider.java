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
import com.uwetrottmann.tmdb.entities.AppendToResponse;
import com.uwetrottmann.tmdb.entities.CastMember;
import com.uwetrottmann.tmdb.entities.CountryRelease;
import com.uwetrottmann.tmdb.entities.CrewMember;
import com.uwetrottmann.tmdb.entities.FindResults;
import com.uwetrottmann.tmdb.entities.Genre;
import com.uwetrottmann.tmdb.entities.Movie;
import com.uwetrottmann.tmdb.entities.MovieResultsPage;
import com.uwetrottmann.tmdb.entities.ProductionCompany;
import com.uwetrottmann.tmdb.entities.ProductionCountry;
import com.uwetrottmann.tmdb.entities.SpokenLanguage;
import com.uwetrottmann.tmdb.entities.Videos;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.util.ListUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The class TmdbMovieMetadataProvider is used to provide metadata for movies from tmdb
 */
public class TmdbMovieMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbMovieMetadataProvider.class);

  private Tmdb                api;

  public TmdbMovieMetadataProvider(Tmdb api) {
    this.api = api;
  }

  /**
   * searches a movie with the given query parameters
   * 
   * @param query
   *          the query parameters
   * @return a list of found movies
   * @throws Exception
   *           any exception which can be thrown while searching
   */
  List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    LOGGER.debug("search() " + query.toString());

    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();

    String searchString = "";
    int year = 0;

    // check type
    if (query.getMediaType() != MediaType.MOVIE) {
      throw new Exception("wrong media type for this scraper");
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.QUERY))) {
      searchString = query.get(MediaSearchOptions.SearchParam.QUERY);
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.TITLE))) {
      searchString = query.get(MediaSearchOptions.SearchParam.TITLE);
    }

    if (StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.YEAR))) {
      try {
        year = Integer.parseInt(query.get(MediaSearchOptions.SearchParam.YEAR));
      }
      catch (Exception e) {
        year = 0;
      }
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("TMDB Scraper: empty searchString");
      return resultList;
    }

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);
    String language = query.get(MediaSearchOptions.SearchParam.LANGUAGE);
    String imdbId = "";
    int tmdbId = 0;

    // begin search
    LOGGER.info("========= BEGIN TMDB Scraper Search for: " + searchString);
    synchronized (api) {
      // 1. try with TMDBid
      if (StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.TMDBID))) {
        TmdbConnectionCounter.trackConnections();
        // if we have already an ID, get this result and do not search
        tmdbId = Integer.valueOf(query.get(MediaSearchOptions.SearchParam.TMDBID));
        try {
          // /movie/{id}
          Movie movie = api.moviesService().summary(tmdbId, language, null);
          MediaSearchResult result = morphMovieToSearchResult(movie);
          resultList.add(result);
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with TMDB id");
      }

      // 2. try with IMDBid
      if (resultList.size() == 0 && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.IMDBID))) {
        TmdbConnectionCounter.trackConnections();
        imdbId = query.get(MediaSearchOptions.SearchParam.IMDBID);
        try {
          // /find/{id}
          FindResults findResults = api.findService().find(imdbId, null, language);
          if (findResults != null && findResults.movie_results != null) {
            for (Movie movie : findResults.movie_results) {
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
          // moviesFound.add(tmdb.getMovieInfoImdb(imdbId, query.get(MediaSearchOptions.SearchParam.LANGUAGE)));
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with IMDB id");
      }

      // 3. try with search string and year
      if (resultList.size() == 0) {
        TmdbConnectionCounter.trackConnections();
        try {
          // /search/movie
          MovieResultsPage resultsPage = api.searchService().movie(searchString, 1, language, false, year, year, "phrase");
          if (resultsPage != null && resultsPage.results != null) {
            for (Movie movie : resultsPage.results) {
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
          // moviesFound = tmdb.searchMovie(searchString, year, query.get(MediaSearchOptions.SearchParam.LANGUAGE), false, 0).getResults();
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with search string");
      }

      // 4. if the last token in search string seems to be a year, try without :)
      if (searchString.matches(".*\\s\\d{4}$") && (resultList == null || resultList.size() == 0)) {
        // nada found & last part seems to be date; strip off and try again
        searchString = searchString.replaceFirst("\\s\\d{4}$", "");
        TmdbConnectionCounter.trackConnections();
        try {
          // /search/movie
          MovieResultsPage resultsPage = api.searchService().movie(searchString, 10, language, false, year, year, "phrase");
          if (resultsPage != null && resultsPage.results != null) {
            for (Movie movie : resultsPage.results) {
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
          // moviesFound = tmdb.searchMovie(searchString, year, query.get(MediaSearchOptions.SearchParam.LANGUAGE), false, 0).getResults();
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with search string removed year");
      }
    }

    LOGGER.info("found " + resultList.size() + " results");

    if (resultList.isEmpty()) {
      return resultList;
    }

    // final tasks for the search results
    for (MediaSearchResult result : resultList) {
      // calculate score for all found movies
      if (imdbId.equals(result.getIMDBId()) || String.valueOf(tmdbId).equals(result.getId())) {
        // perfect match
        result.setScore(1);
      }
      else {
        // compare score based on names
        result.setScore(MetadataUtil.calculateScore(searchString, result.getTitle()));
      }

      // populate extra args
      MetadataUtil.copySearchQueryToSearchResult(query, result);
    }

    return resultList;
  }

  /**
   * Get the movie metadata for the given search options
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

    // tmdbId from searchResult
    if (options.getResult() != null) {
      tmdbId = Integer.parseInt(options.getResult().getId());
    }

    // tmdbId from option
    if (tmdbId == 0) {
      tmdbId = options.getTmdbId();
    }

    // imdbId from option
    String imdbId = options.getImdbId();
    if (tmdbId == 0 && !MetadataUtil.isValidImdbId(imdbId)) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId/imdbId found");
      return md;
    }

    // scrape
    LOGGER.debug("TMDB: getMetadata: tmdbId = " + tmdbId + "; imdbId = " + imdbId);
    Movie movie = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      if (tmdbId == 0 && MetadataUtil.isValidImdbId(imdbId)) {
        try {
          // get the tmdbId via the imdbId
          int tempTmdbId = getTmdbIdFromImdbId(imdbId);
          if (tempTmdbId > 0) {
            // and now get the full data
            movie = api.moviesService().summary(tempTmdbId, options.getLanguage().name(),
                new AppendToResponse(AppendToResponseItem.CREDITS, AppendToResponseItem.RELEASES));
          }
          // movie = tmdb.getMovieInfoImdb(imdbId, options.getLanguage().name());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
      }
      if (movie == null && tmdbId != 0) {
        try {
          movie = api.moviesService().summary(tmdbId, options.getLanguage().name(),
              new AppendToResponse(AppendToResponseItem.CREDITS, AppendToResponseItem.RELEASES));
          // movie = tmdb.getMovieInfo(tmdbId, options.getLanguage().name());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
      }
    }
    if (movie == null) {
      LOGGER.warn("no result found");
      return md;
    }

    md = morphMovieToMediaMetadata(movie, options);

    // check if there was translateable content
    if (StringUtils.isBlank(movie.overview) && options.getLanguage() != MediaLanguages.en) {
      // plot was empty - scrape in english
      MediaLanguages oldLang = options.getLanguage();
      try {
        options.setLanguage(MediaLanguages.en);
        MediaMetadata englishMd = getMetadata(options);

        if (StringUtils.isBlank(movie.overview) && !StringUtils.isBlank(englishMd.getStringValue(MediaMetadata.PLOT))) {
          md.storeMetadata(MediaMetadata.PLOT, englishMd.getStringValue(MediaMetadata.PLOT));
        }
        if (StringUtils.isBlank(movie.title) && !StringUtils.isBlank(englishMd.getStringValue(MediaMetadata.TITLE))) {
          md.storeMetadata(MediaMetadata.TITLE, englishMd.getStringValue(MediaMetadata.TITLE));
        }
        if (StringUtils.isBlank(movie.original_title) && !StringUtils.isBlank(englishMd.getStringValue(MediaMetadata.ORIGINAL_TITLE))) {
          md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, englishMd.getStringValue(MediaMetadata.ORIGINAL_TITLE));
        }
        if (StringUtils.isBlank(movie.tagline) && !StringUtils.isBlank(englishMd.getStringValue(MediaMetadata.TAGLINE))) {
          md.storeMetadata(MediaMetadata.TAGLINE, englishMd.getStringValue(MediaMetadata.TAGLINE));
        }
      }
      catch (Exception e) {
      }
      finally {
        options.setLanguage(oldLang);
      }
    }

    return md;
  }

  /**
   * get the tmdbId via the imdbId
   * 
   * @param imdbId
   *          the imdbId
   * @return the tmdbId or 0 if nothing has been found
   * @throws Exception
   *           any exception which can be thrown while scraping
   */
  int getTmdbIdFromImdbId(String imdbId) throws Exception {
    FindResults findResults = api.findService().find(imdbId, null, null);
    if (findResults != null && findResults.movie_results != null && !findResults.movie_results.isEmpty()) {
      // and now get the full data
      return findResults.movie_results.get(0).id;
    }

    return 0;
  }

  /**
   * get movie trailers
   * 
   * @param options
   *          the scraping options
   * @return a list of found movie trailers
   * @throws Exception
   *           any exception which can be thrown while scraping
   */
  List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getTrailers() " + options.toString());

    List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();

    int tmdbId = options.getTmdbId();
    String imdbId = options.getImdbId();

    if (tmdbId == 0 && StringUtils.isNotEmpty(imdbId)) {
      // try to get tmdbId via imdbId
      tmdbId = getTmdbIdFromImdbId(imdbId);
    }

    if (tmdbId == 0) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId found");
      return trailers;
    }

    LOGGER.debug("TMDB: getTrailers(tmdbId): " + tmdbId);

    List<Videos.Video> tmdbTrailers = new ArrayList<Videos.Video>();

    synchronized (api) {
      TmdbConnectionCounter.trackConnections();

      // get trailers from tmdb (with specified langu and without)
      Videos tmdbVideos = api.moviesService().videos(tmdbId, options.getLanguage().name());
      if (tmdbVideos != null) {
        for (Videos.Video video : ListUtils.nullSafe(tmdbVideos.results)) {
          if ("Trailer".equals(video.type)) {
            tmdbTrailers.add(video);
          }
        }
      }

      Videos tmdbVideosWoLang = api.moviesService().videos(tmdbId, null);
      if (tmdbVideosWoLang != null) {
        for (Videos.Video video : ListUtils.nullSafe(tmdbVideos.results)) {
          if ("Trailer".equals(video.type)) {
            tmdbTrailers.add(video);
          }
        }
      }
    }

    for (Videos.Video tmdbTrailer : tmdbTrailers) {
      if (StringUtils.isBlank(tmdbTrailer.key)) {
        // no url somehow...?
        continue;
      }

      MediaTrailer trailer = new MediaTrailer();
      trailer.setName(tmdbTrailer.name);
      trailer.setQuality(Integer.toString(tmdbTrailer.size));
      trailer.setProvider(tmdbTrailer.site);
      trailer.setUrl(tmdbTrailer.key);

      // youtube support
      if ("youtube".equalsIgnoreCase(tmdbTrailer.site)) {
        // build url for youtube trailer
        StringBuilder sb = new StringBuilder();
        sb.append("http://www.youtube.com/watch?v=");
        sb.append(tmdbTrailer.key);
        if (tmdbTrailer.size >= 720 && !tmdbTrailer.key.contains("&hd=1")) {
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

  private MediaSearchResult morphMovieToSearchResult(Movie movie) {
    MediaSearchResult searchResult = new MediaSearchResult(TmdbMetadataProvider.providerInfo.getId());
    searchResult.setMediaType(MediaType.MOVIE);
    searchResult.setId(Integer.toString(movie.id));
    searchResult.setIMDBId(movie.imdb_id);
    searchResult.setTitle(movie.title);
    searchResult.setOriginalTitle(movie.original_title);
    searchResult.setPosterUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + movie.poster_path);

    // parse release date to year
    if (movie.release_date != null) {
      searchResult.setYear(new SimpleDateFormat("yyyy").format(movie.release_date));
    }

    return searchResult;
  }

  private MediaMetadata morphMovieToMediaMetadata(Movie movie, MediaScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(TmdbMetadataProvider.providerInfo.getId());

    md.setId(MediaMetadata.TMDBID, movie.id);
    md.storeMetadata(MediaMetadata.TITLE, movie.title);
    md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, movie.original_title);
    md.storeMetadata(MediaMetadata.PLOT, movie.overview);
    md.storeMetadata(MediaMetadata.TAGLINE, movie.tagline);

    md.storeMetadata(MediaMetadata.RATING, movie.vote_average);
    md.storeMetadata(MediaMetadata.RUNTIME, movie.runtime);
    md.storeMetadata(MediaMetadata.VOTE_COUNT, movie.vote_count);

    String spokenLanguages = "";
    for (SpokenLanguage lang : ListUtils.nullSafe(movie.spoken_languages)) {
      if (StringUtils.isNotEmpty(spokenLanguages)) {
        spokenLanguages += ", ";
      }

      spokenLanguages += lang.iso_639_1;
    }
    md.storeMetadata(MediaMetadata.SPOKEN_LANGUAGES, spokenLanguages);

    String countries = "";
    for (ProductionCountry country : ListUtils.nullSafe(movie.production_countries)) {
      if (StringUtils.isNotBlank(countries)) {
        countries += ", ";
      }
      countries += country.iso_3166_1;
    }
    md.storeMetadata(MediaMetadata.COUNTRY, countries);

    if (MetadataUtil.isValidImdbId(movie.imdb_id)) {
      md.setId(MediaMetadata.IMDBID, movie.imdb_id);
    }

    // production companies
    String productionCompanies = "";
    for (ProductionCompany company : ListUtils.nullSafe(movie.production_companies)) {
      if (!StringUtils.isEmpty(productionCompanies)) {

        productionCompanies += ", ";
      }
      productionCompanies += company.name.trim();
    }
    md.storeMetadata(MediaMetadata.PRODUCTION_COMPANY, productionCompanies.toString());

    // parse release date to year
    Date releaseDate = movie.release_date;
    if (releaseDate != null) {
      md.storeMetadata(MediaMetadata.YEAR, new SimpleDateFormat("yyyy").format(releaseDate));
    }
    md.storeMetadata(MediaMetadata.RELEASE_DATE, releaseDate);

    // releases & certification
    if (movie.releases != null) {
      for (CountryRelease info : ListUtils.nullSafe(movie.releases.countries)) {
        // do not use any empty certifications
        if (StringUtils.isEmpty(info.certification)) {
          continue;
        }

        // only use the certification of the desired country (if any country has
        // been chosen)
        if (options.getCountry() == null || options.getCountry().getAlpha2().compareToIgnoreCase(info.iso_3166_1) == 0) {
          md.addCertification(Certification.getCertification(info.iso_3166_1, info.certification));
        }
      }
    }

    // cast & crew
    if (movie.credits != null) {
      for (CastMember castMember : ListUtils.nullSafe(movie.credits.cast)) {
        MediaCastMember cm = new MediaCastMember();
        cm.setType(MediaCastMember.CastType.ACTOR);
        cm.setCharacter(castMember.character);
        cm.setName(castMember.name);

        if (!StringUtils.isEmpty(castMember.profile_path)) {
          cm.setImageUrl(TmdbMetadataProvider.configuration.images.base_url + "w185" + castMember.profile_path);
        }
        md.addCastMember(cm);
      }

      // crew
      for (CrewMember crewMember : ListUtils.nullSafe(movie.credits.crew)) {
        MediaCastMember cm = new MediaCastMember();
        if ("Director".equals(crewMember.job)) {
          cm.setType(MediaCastMember.CastType.DIRECTOR);
          cm.setPart(crewMember.department);
        }
        else if ("Writing".equals(crewMember.department)) {
          cm.setType(MediaCastMember.CastType.WRITER);
          cm.setPart(crewMember.department);
        }
        else if ("Production".equals(crewMember.department)) {
          cm.setType(MediaCastMember.CastType.PRODUCER);
          cm.setPart(crewMember.job);
        }
        else {
          continue;
        }
        cm.setName(crewMember.name);

        if (!StringUtils.isEmpty(crewMember.profile_path)) {
          cm.setImageUrl(TmdbMetadataProvider.configuration.images.base_url + "w185" + crewMember.profile_path);
        }
        md.addCastMember(cm);
      }
    }

    // Genres
    for (Genre genre : ListUtils.nullSafe(movie.genres)) {
      md.addGenre(TmdbMetadataProvider.getTmmGenre(genre));
    }

    if (movie.belongs_to_collection != null) {
      md.storeMetadata(MediaMetadata.TMDBID_SET, movie.belongs_to_collection.id);
      md.storeMetadata(MediaMetadata.COLLECTION_NAME, movie.belongs_to_collection.name);
    }

    return md;
  }
}
