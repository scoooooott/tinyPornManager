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
import org.tinymediamanager.scraper.entities.MediaTrailer;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.AppendToResponse;
import com.uwetrottmann.tmdb2.entities.CastMember;
import com.uwetrottmann.tmdb2.entities.CrewMember;
import com.uwetrottmann.tmdb2.entities.FindResults;
import com.uwetrottmann.tmdb2.entities.Genre;
import com.uwetrottmann.tmdb2.entities.Keyword;
import com.uwetrottmann.tmdb2.entities.Movie;
import com.uwetrottmann.tmdb2.entities.MovieKeywords;
import com.uwetrottmann.tmdb2.entities.MovieResultsPage;
import com.uwetrottmann.tmdb2.entities.ProductionCompany;
import com.uwetrottmann.tmdb2.entities.ProductionCountry;
import com.uwetrottmann.tmdb2.entities.ReleaseDate;
import com.uwetrottmann.tmdb2.entities.ReleaseDatesResult;
import com.uwetrottmann.tmdb2.entities.SpokenLanguage;
import com.uwetrottmann.tmdb2.entities.Videos;
import com.uwetrottmann.tmdb2.enumerations.AppendToResponseItem;
import com.uwetrottmann.tmdb2.enumerations.ExternalSource;

/**
 * The class TmdbMovieMetadataProvider is used to provide metadata for movies from tmdb
 */
class TmdbMovieMetadataProvider {
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

    boolean adult = TmdbMetadataProvider.providerInfo.getConfig().getValueAsBool("includeAdult");

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
        TmdbConnectionCounter.trackConnections();
        // if we have already an ID, get this result and do not search
        tmdbId = query.getTmdbId();
        try {
          // /movie/{id}
          Movie movie = api.moviesService().summary(tmdbId, language, null).execute().body();
          MediaSearchResult result = morphMovieToSearchResult(movie);
          resultList.add(result);
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with TMDB id");
      }

      // 2. try with IMDBid
      if (resultList.size() == 0 && StringUtils.isNotEmpty(query.getImdbId())) {
        TmdbConnectionCounter.trackConnections();
        imdbId = query.getImdbId();
        try {
          // /find/{id}
          FindResults findResults = api.findService().find(imdbId, null, language).execute().body();
          if (findResults != null && findResults.movie_results != null) {
            for (Movie movie : findResults.movie_results) {
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
          // moviesFound.add(tmdb.getMovieInfoImdb(imdbId,
          // query.getLanguage().getLanguage()));
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
          MovieResultsPage resultsPage = api.searchService().movie(searchString, 1, language, adult, year, year, "phrase").execute().body();
          if (resultsPage != null && resultsPage.results != null) {
            for (Movie movie : resultsPage.results) {
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
          // moviesFound = tmdb.searchMovie(searchString, year,
          // query.getLanguage().getLanguage(),
          // false, 0).getResults();
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + resultList.size() + " results with search string");
      }

      // 4. if the last token in search string seems to be a year, try without :)
      if (resultList.size() == 0) {
        searchString = searchString.replaceFirst("\\s\\d{4}$", "");
        TmdbConnectionCounter.trackConnections();
        try {
          // /search/movie
          MovieResultsPage resultsPage = api.searchService().movie(searchString, 1, language, adult, null, null, "phrase").execute().body();
          if (resultsPage != null && resultsPage.results != null) {
            for (Movie movie : resultsPage.results) {
              resultList.add(morphMovieToSearchResult(movie));
            }
          }
          // moviesFound = tmdb.searchMovie(searchString, year,
          // query.getLanguage().getLanguage(),
          // false, 0).getResults();
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
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
        // perfect match
        result.setScore(1);
      }
      else {
        float score = MetadataUtil.calculateScore(searchString, result.getTitle());
        if (yearDiffers(year, result.getYear())) {
          float diff = (float) Math.abs(year - result.getYear()) / 100;
          LOGGER.debug("parsed year does not match search result year - downgrading score by " + diff);
          score -= diff;
        }
        result.setScore(score);
      }
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

    // tmdbId from option - own id
    if (tmdbId == 0) {
      try {
        tmdbId = Integer.parseInt(options.getId(TmdbMetadataProvider.providerInfo.getId()));
      }
      catch (NumberFormatException ignored) {
      }
    }

    // tmdbId from option - legacy id
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
            TmdbConnectionCounter.trackConnections();
            movie = api.moviesService()
                .summary(tempTmdbId, language, new AppendToResponse(AppendToResponseItem.CREDITS, AppendToResponseItem.RELEASE_DATES)).execute()
                .body();
          }
          // movie = tmdb.getMovieInfoImdb(imdbId,
          // options.getLanguage().name());
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
      }
      if (movie == null && tmdbId != 0) {
        try {
          TmdbConnectionCounter.trackConnections();
          movie = api.moviesService()
              .summary(tmdbId, language, new AppendToResponse(AppendToResponseItem.CREDITS, AppendToResponseItem.RELEASE_DATES)).execute().body();
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

    // add some special keywords as tags
    // see http://forum.kodi.tv/showthread.php?tid=254004
    try {
      TmdbConnectionCounter.trackConnections();
      MovieKeywords mk = api.moviesService().keywords(tmdbId).execute().body();
      for (Keyword kw : mk.keywords) {
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

    // check if there was translatable content
    if (StringUtils.isBlank(movie.overview) && !"en".equalsIgnoreCase(options.getLanguage().getLanguage())) {
      // plot was empty - scrape in english
      Locale oldLang = options.getLanguage();
      try {
        options.setLanguage(new Locale("en"));
        MediaMetadata englishMd = getMetadata(options);

        if (StringUtils.isBlank(movie.overview) && !StringUtils.isBlank(englishMd.getPlot())) {
          md.setPlot(englishMd.getPlot());
        }
        if (StringUtils.isBlank(movie.title) && !StringUtils.isBlank(englishMd.getTitle())) {
          md.setTitle(englishMd.getTitle());
        }
        if (StringUtils.isBlank(movie.original_title) && !StringUtils.isBlank(englishMd.getOriginalTitle())) {
          md.setOriginalTitle(englishMd.getOriginalTitle());
        }
        if (StringUtils.isBlank(movie.tagline) && !StringUtils.isBlank(englishMd.getTagline())) {
          md.setTagline(englishMd.getTagline());
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
    TmdbConnectionCounter.trackConnections();
    FindResults findResults = api.findService().find(imdbId, ExternalSource.IMDB_ID, null).execute().body();
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

    List<MediaTrailer> trailers = new ArrayList<>();

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

    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }

    LOGGER.debug("TMDB: getTrailers(tmdbId): " + tmdbId);

    List<Videos.Video> tmdbTrailers = new ArrayList<>();

    synchronized (api) {
      // get trailers from tmdb (with specified langu and without)
      TmdbConnectionCounter.trackConnections();
      Videos tmdbVideos = api.moviesService().videos(tmdbId, language).execute().body();
      if (tmdbVideos != null) {
        for (Videos.Video video : ListUtils.nullSafe(tmdbVideos.results)) {
          if ("Trailer".equals(video.type)) {
            tmdbTrailers.add(video);
          }
        }
      }

      TmdbConnectionCounter.trackConnections();
      Videos tmdbVideosWoLang = api.moviesService().videos(tmdbId, null).execute().body();
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
    MediaSearchResult searchResult = new MediaSearchResult(TmdbMetadataProvider.providerInfo.getId(), MediaType.MOVIE);
    searchResult.setId(Integer.toString(movie.id));
    searchResult.setIMDBId(movie.imdb_id);
    searchResult.setTitle(movie.title);
    searchResult.setOriginalTitle(movie.original_title);
    searchResult.setPosterUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + movie.poster_path);

    // parse release date to year
    if (movie.release_date != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(movie.release_date);
      searchResult.setYear(calendar.get(Calendar.YEAR));
    }

    return searchResult;
  }

  private MediaMetadata morphMovieToMediaMetadata(Movie movie, MediaScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(TmdbMetadataProvider.providerInfo.getId());

    md.setId(TmdbMetadataProvider.providerInfo.getId(), movie.id);
    md.setTitle(movie.title);
    md.setOriginalTitle(movie.original_title);
    md.setPlot(movie.overview);
    md.setTagline(movie.tagline);
    md.setRuntime(movie.runtime);
    md.setRating(movie.vote_average);
    md.setVoteCount(movie.vote_count);

    // Poster
    if (StringUtils.isNotBlank(movie.poster_path)) {
      MediaArtwork ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "w185" + movie.poster_path);
      ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + movie.poster_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setTmdbId(movie.id);
      md.addMediaArt(ma);
    }

    for (SpokenLanguage lang : ListUtils.nullSafe(movie.spoken_languages)) {
      if (TmdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
        md.addSpokenLanguage(LanguageUtils.getLocalizedLanguageNameFromLocalizedString(options.getLanguage(), lang.name, lang.iso_639_1));
      }
      else {
        md.addSpokenLanguage(lang.iso_639_1);
      }
    }

    for (ProductionCountry country : ListUtils.nullSafe(movie.production_countries)) {
      if (TmdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
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
    for (ProductionCompany company : ListUtils.nullSafe(movie.production_companies)) {
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
      md.setId(MediaMetadata.TMDB_SET, movie.belongs_to_collection.id);
      md.setCollectionName(movie.belongs_to_collection.name);
    }

    return md;
  }

  /**
   * Is i1 != i2 (when >0)
   */
  private boolean yearDiffers(Integer i1, Integer i2) {
    return i1 != null && i1 != 0 && i2 != null && i2 != 0 && i1 != i2;
  }
}
