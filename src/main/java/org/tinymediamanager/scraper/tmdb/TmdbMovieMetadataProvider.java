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
package org.tinymediamanager.scraper.tmdb;

import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.PRODUCER;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;
import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.getRequestLanguage;
import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.providerInfo;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.HttpException;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
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
import com.uwetrottmann.tmdb2.entities.Movie;
import com.uwetrottmann.tmdb2.entities.MovieResultsPage;
import com.uwetrottmann.tmdb2.entities.ReleaseDate;
import com.uwetrottmann.tmdb2.entities.ReleaseDatesResult;
import com.uwetrottmann.tmdb2.entities.SpokenLanguage;
import com.uwetrottmann.tmdb2.enumerations.AppendToResponseItem;
import com.uwetrottmann.tmdb2.enumerations.ExternalSource;
import com.uwetrottmann.tmdb2.exceptions.TmdbNotFoundException;

import retrofit2.Response;

/**
 * The class {@link TmdbMovieMetadataProvider} is used to provide metadata for movies from tmdb
 *
 * @author Manuel Laggner
 */
class TmdbMovieMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbMovieMetadataProvider.class);

  private final Tmdb          api;

  TmdbMovieMetadataProvider(Tmdb api) {
    this.api = api;
  }

  /**
   * searches a movie with the given query parameters
   *
   * @param options
   *          the query parameters
   * @return a list of found movies
   * @throws ScrapeException
   *           any exception which can be thrown while searching
   */
  SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options) throws ScrapeException {
    Exception savedException = null;

    SortedSet<MediaSearchResult> results = new TreeSet<>();

    // detect the string to search
    String searchString = "";
    if (StringUtils.isNotEmpty(options.getSearchQuery())) {
      searchString = Utils.removeSortableName(options.getSearchQuery());
    }
    searchString = MetadataUtil.removeNonSearchCharacters(searchString);

    String imdbId = options.getImdbId();
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = "";
    }
    if (MetadataUtil.isValidImdbId(searchString)) {
      imdbId = searchString;
    }

    int tmdbId = options.getTmdbId();

    boolean adult = providerInfo.getConfig().getValueAsBool("includeAdult");

    String language = getRequestLanguage(options.getLanguage());

    // begin search
    LOGGER.info("========= BEGIN TMDB Scraper Search for: {}", searchString);
    synchronized (api) {
      // 1. try with TMDBid
      if (tmdbId != 0) {
        LOGGER.debug("found TMDB ID {} - getting direct", tmdbId);
        try {
          Response<Movie> httpResponse = api.moviesService().summary(tmdbId, language, new AppendToResponse(AppendToResponseItem.TRANSLATIONS))
              .execute();
          if (!httpResponse.isSuccessful()) {
            throw new HttpException(httpResponse.code(), httpResponse.message());
          }
          Movie movie = httpResponse.body();
          verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
          MediaSearchResult result = morphMovieToSearchResult(movie, options);
          results.add(result);
          LOGGER.debug("found {} results with TMDB id", results.size());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: {}", e.getMessage());
          savedException = e;
        }
      }

      // 2. try with IMDBid
      if (results.isEmpty() && StringUtils.isNotEmpty(imdbId)) {
        LOGGER.debug("found IMDB ID {} - getting direct", imdbId);
        try {
          Response<FindResults> httpResponse = api.findService().find(imdbId, ExternalSource.IMDB_ID, language).execute();
          if (!httpResponse.isSuccessful()) {
            throw new HttpException(httpResponse.code(), httpResponse.message());
          }
          for (BaseMovie movie : httpResponse.body().movie_results) { // should be only one
            verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
            results.add(morphMovieToSearchResult(movie, options));
          }
          LOGGER.debug("found {} results with IMDB id", results.size());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: {}", e.getMessage());
          savedException = e;
        }
      }

      // 3. try with search string and year
      if (results.isEmpty()) {
        try {
          int page = 1;
          int maxPage = 1;

          // get all result pages
          do {
            Response<MovieResultsPage> httpResponse = api.searchService().movie(searchString, page, language, null, adult, null, null).execute();
            if (!httpResponse.isSuccessful() || httpResponse.body() == null) {
              throw new HttpException(httpResponse.code(), httpResponse.message());
            }
            for (BaseMovie movie : ListUtils.nullSafe(httpResponse.body().results)) {
              verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
              results.add(morphMovieToSearchResult(movie, options));
            }

            maxPage = httpResponse.body().total_pages;
            page++;
          } while (page <= maxPage);

          LOGGER.debug("found {} results with search string", results.size());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: {}", e.getMessage());
          savedException = e;
        }
      }

      // 4. if the last token in search string seems to be a year, try without :)
      if (results.isEmpty()) {
        searchString = searchString.replaceFirst("\\s\\d{4}$", "");
        try {
          // /search/movie
          MovieResultsPage resultsPage = api.searchService().movie(searchString, 1, language, null, adult, null, null).execute().body();
          if (resultsPage != null && resultsPage.results != null) {
            for (BaseMovie movie : resultsPage.results) {
              verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
              results.add(morphMovieToSearchResult(movie, options));
            }
          }
          LOGGER.debug("found {} results with search string without year", results.size());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: {}", e.getMessage());
          savedException = e;
        }
      }
    }

    // if we have not found anything and there is a saved Exception, throw it to indicate a problem
    if (results.isEmpty() && savedException != null) {
      throw new ScrapeException(savedException);
    }

    return results;
  }

  /**
   * Fallback Language Mechanism - for direct TMDB lookup<br>
   * Title always gets returned in en-US, if translation has not been found.<br>
   * But overview IS EMPTY!<br>
   * So, when getting empty overview, we're doing another lookup...
   *
   * @throws IOException
   */
  private void verifyMovieTitleLanguage(Locale language, Movie movie) throws IOException {
    // always doing a fallback scrape when overview empty, regardless of setting!
    if (providerInfo.getConfig().getValueAsBool("titleFallback") || StringUtils.isEmpty(movie.overview)) {
      Locale fallbackLanguage = Locale.forLanguageTag(providerInfo.getConfig().getValue("titleFallbackLanguage"));
      if ((movie.title.equals(movie.original_title) && !movie.original_language.equals(language.getLanguage()))
          && !language.equals(fallbackLanguage)) {
        LOGGER.debug("checking for title fallback {} for movie {}", fallbackLanguage, movie.title);

        // get in desired localization
        String[] val = new String[] { "", "" };
        if (StringUtils.isNotBlank(movie.title)) {
          val[0] = movie.title;
        }
        if (StringUtils.isNotBlank(movie.overview)) {
          val[1] = movie.overview;
        }

        // merge empty ones with fallback
        String[] temp = TmdbMetadataProvider.getValuesFromTranslation(movie.translations, fallbackLanguage);
        if (StringUtils.isBlank(val[0])) {
          val[0] = temp[0];
        }
        if (StringUtils.isBlank(val[1])) {
          val[1] = temp[1];
        }

        // finally SET the values
        movie.title = val[0];
        movie.overview = val[1];
      }
    }
  }

  /**
   * Fallback Language Mechanism - For IMDB & searches
   *
   * @param language
   *          the language to verify
   * @param movie
   *          the already found movie
   * @throws IOException
   */
  private void verifyMovieTitleLanguage(Locale language, BaseMovie movie) throws IOException {
    // NOT doing a fallback scrape when overview empty, used only for SEARCH - unneeded!
    if (providerInfo.getConfig().getValueAsBool("titleFallback")) {
      Locale fallbackLanguage = Locale.forLanguageTag(providerInfo.getConfig().getValue("titleFallbackLanguage"));

      // tmdb provides title = originalTitle if no title in the requested language has been found,
      // so get the title in a alternative language
      if ((movie.title.equals(movie.original_title) && !movie.original_language.equals(language.getLanguage()))
          && !language.equals(fallbackLanguage)) {
        LOGGER.debug("checking for title fallback {} for movie {}", fallbackLanguage, movie.title);
        String lang = providerInfo.getConfig().getValue("titleFallbackLanguage").replace("_", "-");
        Response<Movie> httpResponse = api.moviesService().summary(movie.id, lang, new AppendToResponse(AppendToResponseItem.TRANSLATIONS)).execute();
        if (!httpResponse.isSuccessful()) {
          throw new HttpException(httpResponse.code(), httpResponse.message());
        }
        Movie m = httpResponse.body();

        // get in desired localization
        String[] val = new String[] { "", "" };
        if (StringUtils.isNotBlank(movie.title)) {
          val[0] = movie.title;
        }
        if (StringUtils.isNotBlank(movie.overview)) {
          val[1] = movie.overview;
        }

        // merge empty ones with fallback
        String[] temp = TmdbMetadataProvider.getValuesFromTranslation(m.translations, fallbackLanguage);
        if (StringUtils.isBlank(val[0])) {
          val[0] = temp[0];
        }
        if (StringUtils.isBlank(val[1])) {
          val[1] = temp[1];
        }

        // finally SET the values
        movie.title = val[0];
        movie.overview = val[1];
      }
    }
  }

  /**
   * Get the movie metadata for the given search options
   *
   * @param options
   *          the options for scraping
   * @return the metadata (never null)
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   * @throws NothingFoundException
   *           indicated that nothing has been found
   */
  MediaMetadata getMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    Exception savedException = null;

    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    // tmdbId from option
    int tmdbId = options.getTmdbId();

    // imdbId from option
    String imdbId = options.getImdbId();

    if (tmdbId == 0 && !MetadataUtil.isValidImdbId(imdbId)) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId/imdbId found");
      throw new MissingIdException(MediaMetadata.TMDB, MediaMetadata.IMDB);
    }

    String language = getRequestLanguage(options.getLanguage());

    // scrape
    Movie movie = null;
    synchronized (api) {

      // we do not have the tmdbId?!? hmm.. get it from imdb...
      if (tmdbId == 0 && MetadataUtil.isValidImdbId(imdbId)) {
        try {
          tmdbId = new TmdbMetadataProvider().getTmdbIdFromImdbId(imdbId, options.getMediaType());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting tmdbId from imdbId: {}", e.getMessage());
          savedException = e;
        }
      }

      if (movie == null && tmdbId > 0) {
        try {
          Response<Movie> httpResponse = api.moviesService().summary(tmdbId, language, new AppendToResponse(AppendToResponseItem.CREDITS,
              AppendToResponseItem.KEYWORDS, AppendToResponseItem.RELEASE_DATES, AppendToResponseItem.TRANSLATIONS)).execute();
          if (!httpResponse.isSuccessful()) {
            throw new HttpException(httpResponse.code(), httpResponse.message());
          }
          movie = httpResponse.body();
          verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
        }
        catch (TmdbNotFoundException e) {
          LOGGER.info("nothing found");
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data from tmdb: {}", e.getMessage());
          savedException = e;
        }
      }
    }

    // if there is no result, but a saved exception, propagate it
    if (movie == null && savedException != null) {
      throw new ScrapeException(savedException);
    }

    if (movie == null) {
      LOGGER.warn("no result found");
      throw new NothingFoundException();
    }

    md = morphMovieToMediaMetadata(movie, options);

    // add some special keywords as tags
    // see http://forum.kodi.tv/showthread.php?tid=254004
    if (movie.keywords != null && movie.keywords.keywords != null) {
      for (BaseKeyword kw : movie.keywords.keywords) {
        md.addTag(kw.name);
      }
    }

    return md;
  }

  private MediaSearchResult morphMovieToSearchResult(BaseMovie movie, MovieSearchAndScrapeOptions query) {
    MediaSearchResult searchResult = new MediaSearchResult(providerInfo.getId(), MediaType.MOVIE);
    searchResult.setId(Integer.toString(movie.id));
    searchResult.setTitle(movie.title);
    searchResult.setOverview(movie.overview); // empty overview tells us that we have no translation?
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

    // calculate score
    if ((StringUtils.isNotBlank(query.getImdbId()) && query.getImdbId().equals(searchResult.getIMDBId()))
        || String.valueOf(query.getTmdbId()).equals(searchResult.getId())) {
      LOGGER.debug("perfect match by ID - set score to 1");
      searchResult.setScore(1);
    }
    else {
      // calculate the score by comparing the search result with the search options
      searchResult.calculateScore(query);
    }

    return searchResult;
  }

  private MediaMetadata morphMovieToMediaMetadata(Movie movie, MovieSearchAndScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    md.setId(providerInfo.getId(), movie.id);
    md.setTitle(movie.title);
    md.setOriginalTitle(movie.original_title);
    md.setPlot(movie.overview);
    md.setTagline(movie.tagline);
    md.setRuntime(movie.runtime);

    MediaRating rating = new MediaRating("tmdb");
    rating.setRating(movie.vote_average.floatValue());
    rating.setVotes(movie.vote_count);
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
        md.addSpokenLanguage(LanguageUtils.getLocalizedLanguageNameFromLocalizedString(options.getLanguage().toLocale(), lang.name, lang.iso_639_1));
      }
      else {
        md.addSpokenLanguage(lang.iso_639_1);
      }
    }

    for (Country country : ListUtils.nullSafe(movie.production_countries)) {
      if (providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
        md.addCountry(LanguageUtils.getLocalizedCountryForLanguage(options.getLanguage().toLocale(), country.name, country.iso_3166_1));
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
      // only use the certification of the desired country (if any country has been chosen)
      CountryCode countryCode = MovieModuleManager.SETTINGS.getCertificationCountry();

      for (ReleaseDatesResult countries : ListUtils.nullSafe(movie.release_dates.results)) {
        if (countryCode == null || countryCode.getAlpha2().compareToIgnoreCase(countries.iso_3166_1) == 0) {
          // Any release from the desired country will do
          for (ReleaseDate countryReleaseDate : ListUtils.nullSafe(countries.release_dates)) {
            // do not use any empty certifications
            if (StringUtils.isEmpty(countryReleaseDate.certification)) {
              continue;
            }

            md.addCertification(MediaCertification.getCertification(countries.iso_3166_1, countryReleaseDate.certification));
          }
        }
      }
    }

    // cast & crew
    if (movie.credits != null) {
      for (CastMember castMember : ListUtils.nullSafe(movie.credits.cast)) {
        Person cm = new Person(Person.Type.ACTOR);
        ;
        cm.setId(providerInfo.getId(), castMember.id);
        cm.setName(castMember.name);
        cm.setRole(castMember.character);

        if (StringUtils.isNotBlank(castMember.profile_path)) {
          cm.setThumbUrl(TmdbMetadataProvider.configuration.images.base_url + "h632" + castMember.profile_path);
        }
        if (castMember.id != null) {
          cm.setProfileUrl("https://www.themoviedb.org/person/" + castMember.id);
        }
        md.addCastMember(cm);
      }

      // crew
      for (CrewMember crewMember : ListUtils.nullSafe(movie.credits.crew)) {
        Person cm = new Person();
        if ("Director".equals(crewMember.job)) {
          cm.setType(DIRECTOR);
          cm.setRole(crewMember.department);
        }
        else if ("Writing".equals(crewMember.department)) {
          cm.setType(WRITER);
          cm.setRole(crewMember.department);
        }
        else if ("Production".equals(crewMember.department)) {
          cm.setType(PRODUCER);
          cm.setRole(crewMember.job);
        }
        else {
          continue;
        }
        cm.setId(providerInfo.getId(), crewMember.id);
        cm.setName(crewMember.name);

        if (StringUtils.isNotBlank(crewMember.profile_path)) {
          cm.setThumbUrl(TmdbMetadataProvider.configuration.images.base_url + "h632" + crewMember.profile_path);
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
}
