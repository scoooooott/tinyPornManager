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
package org.tinymediamanager.scraper.tmdb;

import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.providerInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.AppendToResponse;
import com.uwetrottmann.tmdb2.entities.BaseCompany;
import com.uwetrottmann.tmdb2.entities.BaseKeyword;
import com.uwetrottmann.tmdb2.entities.BaseMovie;
import com.uwetrottmann.tmdb2.entities.CastMember;
import com.uwetrottmann.tmdb2.entities.Country;
import com.uwetrottmann.tmdb2.entities.CrewMember;
import com.uwetrottmann.tmdb2.entities.FindResults;
import com.uwetrottmann.tmdb2.entities.Genre;
import com.uwetrottmann.tmdb2.entities.Keywords;
import com.uwetrottmann.tmdb2.entities.Movie;
import com.uwetrottmann.tmdb2.entities.MovieResultsPage;
import com.uwetrottmann.tmdb2.entities.ReleaseDate;
import com.uwetrottmann.tmdb2.entities.ReleaseDatesResult;
import com.uwetrottmann.tmdb2.entities.SpokenLanguage;
import com.uwetrottmann.tmdb2.enumerations.AppendToResponseItem;
import com.uwetrottmann.tmdb2.enumerations.ExternalSource;

/**
 * The class TmdbMovieMetadataProvider is used to provide metadata for movies from tmdb
 */
class TmdbMovieMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbMovieMetadataProvider.class);

  private final Tmdb          api;

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

    List<MediaSearchResult> resultList = new ArrayList<>();

    String searchString = "";
    Integer year = null;

    // check type
    if (query.getMediaType() != MediaType.MOVIE) {
      throw new Exception("wrong media type for this scraper");
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.getQuery())) {
      searchString = query.getQuery();
    }

    if (query.getYear() != 0) {
      year = query.getYear();
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("TMDB Scraper: empty searchString");
      return resultList;
    }

    boolean adult = providerInfo.getConfig().getValueAsBool("includeAdult");

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);
    String language = query.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(query.getLanguage().getCountry())) {
      language += "-" + query.getLanguage().getCountry();
    }
    String imdbId = "";
    int tmdbId = 0;

    // begin search
    LOGGER.info("========= BEGIN TMDB Scraper Search for: " + searchString);
    synchronized (api) {
      // 1. try with TMDBid
      if (query.getTmdbId() != 0) {
        // if we have already an ID, get this result and do not search
        tmdbId = query.getTmdbId();
        try {
          // /movie/{id}
          Movie movie = api.moviesService().summary(tmdbId, language).execute().body();
          verifyMovieTitleLanguage(query, movie);
          MediaSearchResult result = morphMovieToSearchResult(movie);
          resultList.add(result);
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with TMDB id");
      }

      // 2. try with IMDBid
      if (resultList.size() == 0 && StringUtils.isNotEmpty(query.getImdbId())) {
        imdbId = query.getImdbId();
        try {
          // /find/{id}
          FindResults findResults = api.findService().find(imdbId, null, language).execute().body();
          if (findResults != null && findResults.movie_results != null) {
            for (Movie movie : findResults.movie_results) {
              if (verifyMovieTitleLanguage(query, new ArrayList<BaseMovie>(findResults.movie_results), resultList, true)) {
                break;
              }
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with IMDB id");
      }

      // 3. try with search string and year
      if (resultList.size() == 0) {
        try {
          // /search/movie
          MovieResultsPage resultsPage = api.searchService().movie(searchString, 1, language, adult, year, year, "phrase").execute().body();
          if (resultsPage != null && resultsPage.results != null) {
            for (BaseMovie movie : resultsPage.results) {
              if (verifyMovieTitleLanguage(query, resultsPage.results, resultList, false)) {
                break;
              }
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with search string");
      }

      // 4. if the last token in search string seems to be a year, try without :)
      if (resultList.size() == 0) {
        searchString = searchString.replaceFirst("\\s\\d{4}$", "");
        try {
          // /search/movie
          MovieResultsPage resultsPage = api.searchService().movie(searchString, 1, language, adult, null, null, "phrase").execute().body();
          if (resultsPage != null && resultsPage.results != null) {
            for (BaseMovie movie : resultsPage.results) {
              if (verifyMovieTitleLanguage(query, resultsPage.results, resultList, false)) {
                break;
              }

              resultList.add(morphMovieToSearchResult(movie));
            }
          }
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with search string without year");
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
        LOGGER.debug("perfect match by ID - set score to 1");
        result.setScore(1);
      }
      else {
        float score = MetadataUtil.calculateScore(searchString, result.getTitle());

        if (year != null && yearDiffers(year.intValue(), result.getYear())) {
          float diff = (float) Math.abs(year.intValue() - result.getYear()) / 100;
          LOGGER.debug("parsed year does not match search result year - downgrading score by " + diff);
          score -= diff;
        }

        if (result.getPosterUrl() == null || result.getPosterUrl().isEmpty()) {
          // no poster?
          LOGGER.debug("no poster - downgrading score by 0.01");
          score -= 0.01f;
        }

        result.setScore(score);
      }
    }

    return resultList;
  }

  /**
   * Fallback Language Mechanism - For IMDB Id.
   *
   * @param query
   *          the query options
   * @param movie
   *          the already found movie
   */
  private void verifyMovieTitleLanguage(MediaSearchOptions query, BaseMovie movie) {
    if (providerInfo.getConfig().getValueAsBool("titleFallback")) {
      Locale fallbackLanguage = new Locale(MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).getLanguage());

      // tmdb provides title = originalTitle if no title in the requested language has been found,
      // so get the title in a alternative language
      if ((movie.title.equals(movie.original_title) && !movie.original_language.equals(query.getLanguage().getLanguage()))
          && !query.getLanguage().equals(fallbackLanguage)) {
        try {
          String lang = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name().replace("_", "-");

          Movie fallbackMovie = api.moviesService().summary(movie.id, lang).execute().body();

          if (fallbackMovie == null) {
            return;
          }

          if (!StringUtils.isBlank(fallbackMovie.title)) {
            movie.title = fallbackMovie.title;
          }
        }
        catch (Exception ignored) {
          return;
        }
      }
    }
  }

  /**
   * Fallback Language Mechanism - For Search.
   *
   * @param query
   *          the query options
   * @param original
   *          the movie list retrieved with primary language.
   * @param resultList
   *          the list that results will be added.
   * @param findService
   *          is it called by findService?
   */
  private Boolean verifyMovieTitleLanguage(MediaSearchOptions query, List<BaseMovie> original, List<MediaSearchResult> resultList,
      Boolean findService) {
    if (providerInfo.getConfig().getValueAsBool("titleFallback")) {
      Locale fallbackLanguage = new Locale(MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).getLanguage());

      for (BaseMovie movie : original) {
        // tmdb provides title = originalTitle if no title in the requested language has been found,
        // so get the title in a alternative language
        if ((movie.title.equals(movie.original_title) && !movie.original_language.equals(query.getLanguage().getLanguage()))
            && !query.getLanguage().equals(fallbackLanguage)) {

          LOGGER.debug("Fallback: Title Inconsistency Found. Bypassing default functionality and Initiating Fallback Mechanism.");

          try {
            String lang = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name().replace("_", "-");

            List<BaseMovie> fallback;
            if (findService) {
              FindResults findResults = api.findService().find(query.getImdbId(), null, lang).execute().body();

              if (findResults == null || findResults.movie_results == null) {
                return false;
              }

              fallback = new ArrayList<BaseMovie>(findResults.movie_results);
            }
            else {
              MovieResultsPage movieResultsPage = api.searchService()
                  .movie(query.getQuery(), 1, lang, providerInfo.getConfig().getValueAsBool("includeAdult"),
                      query.getYear() != 0 ? query.getYear() : null, query.getYear() != 0 ? query.getYear() : null, "phrase")
                  .execute().body();

              if (movieResultsPage == null || movieResultsPage.results == null) {
                return false;
              }

              fallback = movieResultsPage.results;
            }

            resultList.clear();

            for (int i = 0; i < original.size(); i++) {
              BaseMovie originalMovie = original.get(i);
              BaseMovie fallbackMovie = fallback.get(i);

              if (originalMovie.title.equals(originalMovie.original_title) && !originalMovie.title.equals(fallbackMovie.title)
                  && !originalMovie.original_language.equals(query.getLanguage().getLanguage()) && !StringUtils.isBlank(fallbackMovie.title)) {
                LOGGER.debug(String.format("Fallback: Movie Replaced          ([%-32.32s] -> [%-32.32s])", originalMovie.title, fallbackMovie.title));
                resultList.add(morphMovieToSearchResult(fallbackMovie));
              }
              else {
                LOGGER.debug(String.format("Fallback: Movie Remained the Same ([%-32.32s])", originalMovie.title));
                resultList.add(morphMovieToSearchResult(originalMovie));
              }
            }
            return true;
          }
          catch (Exception exc) {
            return false;
          }
        }
      }
    }
    return false;
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
    return getMetadata(options, false, null);
  }

  /**
   * Get the movie metadata for the given search options
   *
   * @param options
   *          the options for scraping
   * @param fallback
   *          whether this method called from fallback.
   * @param metadata
   *          the original metadata from the original result before callback.
   * @return the metadata (never null)
   * @throws Exception
   *           any exception which can be thrown while scraping
   */
  MediaMetadata getMetadata(MediaScrapeOptions options, boolean fallback, MediaMetadata metadata) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());

    Boolean titleFallback = providerInfo.getConfig().getValueAsBool("titleFallback");

    Locale fallbackLanguage = new Locale(MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).getLanguage());

    MediaMetadata md = new MediaMetadata(providerInfo.getId());
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

    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }

    // scrape
    LOGGER.debug("TMDB: getMetadata: tmdbId = " + tmdbId + "; imdbId = " + imdbId);
    Movie movie = null;
    synchronized (api) {
      if (tmdbId == 0 && MetadataUtil.isValidImdbId(imdbId)) {
        try {
          // get the tmdbId via the imdbId
          int tempTmdbId = getTmdbIdFromImdbId(imdbId);
          if (tempTmdbId > 0) {
            // and now get the full data
            movie = api.moviesService()
                .summary(tempTmdbId, language,
                    new AppendToResponse(AppendToResponseItem.CREDITS, AppendToResponseItem.RELEASE_DATES, AppendToResponseItem.TRANSLATIONS))
                .execute().body();
          }
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: " + e.getMessage());
        }
      }
      if (movie == null && tmdbId != 0) {
        try {
          movie = api.moviesService()
              .summary(tmdbId, language, new AppendToResponse(AppendToResponseItem.CREDITS, AppendToResponseItem.RELEASE_DATES)).execute().body();
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: " + e.getMessage());
        }
      }
    }
    if (movie == null) {
      LOGGER.warn("no result found");
      return md;
    }

    md = morphMovieToMediaMetadata(movie, options);

    // add some special keywords as tags
    // see http://forum.kodi.tv/showthread.php?tid=254004
    try {
      Keywords mk = api.moviesService().keywords(tmdbId).execute().body();
      for (BaseKeyword kw : ListUtils.nullSafe(mk.keywords)) {
        switch (kw.name) {
          case "aftercreditsstinger":
          case "duringcreditsstinger":
            md.addTag(kw.name);
            break;
          default:
            // ignore other tags?
            break;
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("Error getting keywords");
    }
    // check if we need to rescrape in the fallback language
    if (((movie.title.equals(movie.original_title) && !movie.original_language.equals(options.getLanguage().getLanguage()))
        || StringUtils.isBlank(movie.overview)) && (!options.getLanguage().equals(fallbackLanguage) || fallback) && titleFallback) {
      // title in original language or plot was empty - scrape in fallback language
      if (fallback) {
        LOGGER.debug("Movie data not found with fallback language. Returning original.");
        return metadata;
      }

      Locale oldLang = options.getLanguage();
      try {
        options.setLanguage(fallbackLanguage);
        LOGGER.debug("Re-scraping using fallback language " + MediaLanguages.valueOf(options.getLanguage().getLanguage()));

        MediaMetadata fallbackMd = getMetadata(options, true, md);

        if (StringUtils.isBlank(movie.overview) && !StringUtils.isBlank(fallbackMd.getPlot())) {
          md.setPlot(fallbackMd.getPlot());
        }
        if (movie.title.equals(movie.original_title) && !movie.original_language.equals(oldLang.getLanguage())
            && !StringUtils.isBlank(fallbackMd.getTitle())) {
          md.setTitle(fallbackMd.getTitle());
        }
        if (StringUtils.isBlank(movie.original_title) && !StringUtils.isBlank(fallbackMd.getOriginalTitle())) {
          md.setOriginalTitle(fallbackMd.getOriginalTitle());
        }
        if (StringUtils.isBlank(movie.tagline) && !StringUtils.isBlank(fallbackMd.getTagline())) {
          md.setTagline(fallbackMd.getTagline());
        }
      }
      catch (Exception ignored) {
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
    try {
      FindResults findResults = api.findService().find(imdbId, ExternalSource.IMDB_ID, null).execute().body();
      if (findResults != null && findResults.movie_results != null && !findResults.movie_results.isEmpty()) {
        // and now get the full data
        return findResults.movie_results.get(0).id;
      }
    }
    catch (Exception e) {
      LOGGER.debug("failed to get tmdb id: " + e.getMessage());
    }

    return 0;
  }

  private MediaSearchResult morphMovieToSearchResult(BaseMovie movie) {
    MediaSearchResult searchResult = new MediaSearchResult(providerInfo.getId(), MediaType.MOVIE);
    searchResult.setId(Integer.toString(movie.id));
    searchResult.setTitle(movie.title);
    searchResult.setOriginalTitle(movie.original_title);
    searchResult.setOriginalLanguage(movie.original_language);

    if (movie.poster_path != null && !movie.poster_path.isEmpty()) {
      searchResult.setPosterUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + movie.poster_path);
    }

    // parse release date to year
    if (movie.release_date != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(movie.release_date);
      searchResult.setYear(calendar.get(Calendar.YEAR));
    }

    return searchResult;
  }

  private MediaMetadata morphMovieToMediaMetadata(Movie movie, MediaScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    md.setId(providerInfo.getId(), movie.id);
    md.setTitle(movie.title);
    md.setOriginalTitle(movie.original_title);
    md.setPlot(movie.overview);
    md.setTagline(movie.tagline);
    md.setRuntime(movie.runtime);

    MediaRating rating = new MediaRating("tmdb");
    rating.setRating(movie.vote_average);
    rating.setVoteCount(movie.vote_count);
    rating.setMaxValue(10);
    md.addRating(rating);

    // Poster
    if (StringUtils.isNotBlank(movie.poster_path)) {
      MediaArtwork ma = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "w185" + movie.poster_path);
      ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + movie.poster_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setTmdbId(movie.id);
      md.addMediaArt(ma);
    }

    for (SpokenLanguage lang : ListUtils.nullSafe(movie.spoken_languages)) {
      if (providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
        md.addSpokenLanguage(LanguageUtils.getLocalizedLanguageNameFromLocalizedString(options.getLanguage(), lang.name, lang.iso_639_1));
      }
      else {
        md.addSpokenLanguage(lang.iso_639_1);
      }
    }

    for (Country country : ListUtils.nullSafe(movie.production_countries)) {
      if (providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
        md.addCountry(LanguageUtils.getLocalizedCountryForLanguage(options.getLanguage(), country.name, country.iso_3166_1));
      }
      else {
        md.addCountry(country.iso_3166_1);
      }
    }

    if (MetadataUtil.isValidImdbId(movie.imdb_id)) {
      md.setId(MediaMetadata.IMDB, movie.imdb_id);
    }

    // production companies
    for (BaseCompany company : ListUtils.nullSafe(movie.production_companies)) {
      md.addProductionCompany(company.name.trim());
    }

    // parse release date to year
    Date releaseDate = movie.release_date;
    if (releaseDate != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(releaseDate);
      md.setYear(calendar.get(Calendar.YEAR));
    }
    md.setReleaseDate(releaseDate);

    // releases & certification
    if (movie.release_dates != null) {
      for (ReleaseDatesResult countries : ListUtils.nullSafe(movie.release_dates.results)) {

        // only use the certification of the desired country (if any country has
        // been chosen)
        if (options.getCountry() == null || options.getCountry().getAlpha2().compareToIgnoreCase(countries.iso_3166_1) == 0) {
          // Any release from the desired country will do
          for (ReleaseDate countryReleaseDate : ListUtils.nullSafe(countries.release_dates)) {
            // do not use any empty certifications
            if (StringUtils.isEmpty(countryReleaseDate.certification)) {
              continue;
            }

            md.addCertification(Certification.getCertification(countries.iso_3166_1, countryReleaseDate.certification));

          }
        }
      }
    }

    // cast & crew
    if (movie.credits != null) {
      for (CastMember castMember : ListUtils.nullSafe(movie.credits.cast)) {
        MediaCastMember cm = new MediaCastMember();
        cm.setType(MediaCastMember.CastType.ACTOR);
        cm.setId(providerInfo.getId(), castMember.id);
        cm.setCharacter(castMember.character);
        cm.setName(castMember.name);

        if (!StringUtils.isEmpty(castMember.profile_path)) {
          cm.setImageUrl(TmdbMetadataProvider.configuration.images.base_url + "h632" + castMember.profile_path);
        }
        if (castMember.id != null) {
          cm.setProfileUrl("https://www.themoviedb.org/person/" + castMember.id);
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
        cm.setId(providerInfo.getId(), crewMember.id);
        cm.setName(crewMember.name);

        if (!StringUtils.isEmpty(crewMember.profile_path)) {
          cm.setImageUrl(TmdbMetadataProvider.configuration.images.base_url + "h632" + crewMember.profile_path);
        }
        if (crewMember.id != null) {
          cm.setProfileUrl("https://www.themoviedb.org/person/" + crewMember.id);
        }

        md.addCastMember(cm);
      }
    }

    // Genres
    for (Genre genre : ListUtils.nullSafe(movie.genres)) {
      md.addGenre(TmdbMetadataProvider.getTmmGenre(genre));
    }
    // "adult" on TMDB is always some pr0n stuff, and not just rated 18+ content
    if (movie.adult) {
      md.addGenre(MediaGenres.EROTIC);
    }

    if (movie.belongs_to_collection != null) {
      md.setId(MediaMetadata.TMDB_SET, movie.belongs_to_collection.id);
      md.setCollectionName(movie.belongs_to_collection.name);
    }

    return md;
  }

  /**
   * Is i1 != i2 (when >0)
   */
  private boolean yearDiffers(int i1, int i2) {
    return i1 > 0 && i2 > 0 && i1 != i2;
  }
}
