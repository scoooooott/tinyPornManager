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
package org.tinymediamanager.scraper.pornhub.v1;

import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.PRODUCER;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;
import static org.tinymediamanager.scraper.pornhub.v1.PornhubMetadataProvider.getRequestLanguage;
import static org.tinymediamanager.scraper.pornhub.v1.PornhubMetadataProvider.providerInfo;

import com.scott.pornhub.Pornhub;
import com.scott.pornhub.entities.BaseCompany;
import com.scott.pornhub.entities.BaseKeyword;
import com.scott.pornhub.entities.BaseMovie;
import com.scott.pornhub.entities.CastMember;
import com.scott.pornhub.entities.Country;
import com.scott.pornhub.entities.CrewMember;
import com.scott.pornhub.entities.Genre;
import com.scott.pornhub.entities.Movie;
import com.scott.pornhub.entities.ReleaseDate;
import com.scott.pornhub.entities.ReleaseDatesResult;
import com.scott.pornhub.entities.SpokenLanguage;
import com.scott.pornhub.exceptions.PornhubNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import retrofit2.Response;

/**
 * The class {@link PornhubMovieMetadataProvider} is used to provide metadata for movies from
 * pornhub
 *
 * @author Manuel Laggner
 */
class PornhubMovieMetadataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(PornhubMovieMetadataProvider.class);

  private final String searchResultCSSSelector = "#videoSearchResult .pcVideoListItem a[href]";

  private final Pornhub api;

  PornhubMovieMetadataProvider(Pornhub api) {
    this.api = api;
  }

  /**
   * searches a movie with the given query parameters
   *
   * @param options the query parameters
   * @return a list of found movies
   * @throws ScrapeException any exception which can be thrown while searching
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

        /*Pattern videoNamePattern = Pattern.compile(".*?\\s?-\\s(.*)$");
        Matcher matcher = videoNamePattern.matcher(searchString);
        if (matcher.find()) {
            searchString = matcher.group(1);
        }*/

    String imdbId = options.getImdbId();
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = "";
    }
    if (MetadataUtil.isValidImdbId(searchString)) {
      imdbId = searchString;
    }

    String pornhubId = options.getPornhubId();

    boolean adult = providerInfo.getConfig().getValueAsBool("includeAdult");

    String language = getRequestLanguage(options.getLanguage());

    // begin search
    LOGGER.info("========= BEGIN PORNHUB Scraper Search for: {}", searchString);
    synchronized (api) {
      // 1. try with PORNHUBid
      if (StringUtils.isNotEmpty(pornhubId)) {
        LOGGER.debug("found PORNHUB ID {} - getting direct", pornhubId);
        try {
          Response<Document> httpResponse = api.moviesService().summary(pornhubId, null, null)
              .execute();
          if (!httpResponse.isSuccessful()) {
            throw new HttpException(httpResponse.code(), httpResponse.message());
          }
          Movie movie = document2Movie(httpResponse.body());
          verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
          MediaSearchResult result = morphMovieToSearchResult(movie, options);
          results.add(result);
          LOGGER.debug("found {} results with PORNHUB id", results.size());
        } catch (Exception e) {
          LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
          savedException = e;
        }
      }

      // 2. try with search string and year
      if (results.isEmpty()) {
        try {
          int page = 1;
          int maxPage;

          // get all result pages
          do {
            Response<Document> httpResponse = api.searchService()
                .videos(searchString, page, null, null, null, null, null, null).execute();
            if (!httpResponse.isSuccessful() || httpResponse.body() == null) {
              throw new HttpException(httpResponse.code(), httpResponse.message());
            }
                        /*for (BaseMovie movie : ListUtils.nullSafe(httpResponse.body().results)) {
                            verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
                            results.add(morphMovieToSearchResult(movie, options));
                        }

                        maxPage = httpResponse.body().total_pages;*/

            Document html = httpResponse.body();
            // search result
            Elements searchResult = html.select(searchResultCSSSelector);
            for (Element e : searchResult) {
              Movie movie = searchResult2Movie(e);
              verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
              results.add(morphMovieToSearchResult(movie, options));
            }

            maxPage = Integer.parseInt(html.selectFirst(".page_next_set").text());

            page++;
          }
          while (page <= maxPage);

          LOGGER.debug("found {} results with search string", results.size());
        } catch (Exception e) {
          LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
          savedException = e;
        }
      }

      // 3. if the last token in search string seems to be a year, try without :)
      if (results.isEmpty()) {
        searchString = searchString.replaceFirst("\\s\\d{4}$", "");
        try {
          // /search/movie
          Document resultsPage = api.searchService()
              .videos(searchString, 1, null, null, null, null, null, null).execute().body();
                    /*if (resultsPage != null && resultsPage.results != null) {
                        for (BaseMovie movie : resultsPage.results) {
                            verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
                            results.add(morphMovieToSearchResult(movie, options));
                        }
                    }*/
          assert resultsPage != null;
          Elements searchResult = resultsPage.select(searchResultCSSSelector);
          if (searchResult != null) {
            for (Element e : searchResult) {
              Movie movie = searchResult2Movie(e);
              verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
              results.add(morphMovieToSearchResult(movie, options));
            }
          }
          LOGGER.debug("found {} results with search string without year", results.size());
        } catch (Exception e) {
          e.printStackTrace();
          LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
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
   * Fallback Language Mechanism - for direct PORNHUB lookup<br> Title always gets returned in
   * en-US, if translation has not been found.<br> But overview IS EMPTY!<br> So, when getting empty
   * overview, we're doing another lookup...
   *
   * @throws IOException
   */
  private void verifyMovieTitleLanguage(Locale language, Movie movie) throws IOException {
    // always doing a fallback scrape when overview empty, regardless of setting!
    if (providerInfo.getConfig().getValueAsBool("titleFallback") || StringUtils
        .isEmpty(movie.overview)) {
      Locale fallbackLanguage = Locale
          .forLanguageTag(providerInfo.getConfig().getValue("titleFallbackLanguage"));
      if ((movie.title.equals(movie.original_title) && !movie.original_language
          .equals(language.getLanguage()))
          && !language.equals(fallbackLanguage)) {
        LOGGER.debug("checking for title fallback {} for movie {}", fallbackLanguage, movie.title);

        // get in desired localization
        String[] val = new String[]{"", ""};
        if (StringUtils.isNotBlank(movie.title)) {
          val[0] = movie.title;
        }
        if (StringUtils.isNotBlank(movie.overview)) {
          val[1] = movie.overview;
        }

        // merge empty ones with fallback
        String[] temp = PornhubMetadataProvider
            .getValuesFromTranslation(movie.translations, fallbackLanguage);
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
   * @param language the language to verify
   * @param movie    the already found movie
   * @throws IOException
   */
  private void verifyMovieTitleLanguage(Locale language, BaseMovie movie) throws IOException {
    // NOT doing a fallback scrape when overview empty, used only for SEARCH - unneeded!
    if (providerInfo.getConfig().getValueAsBool("titleFallback")) {
      Locale fallbackLanguage = Locale
          .forLanguageTag(providerInfo.getConfig().getValue("titleFallbackLanguage"));

      // pornhub provides title = originalTitle if no title in the requested language has been found,
      // so get the title in a alternative language
      if ((movie.title.equals(movie.original_title) && !movie.original_language
          .equals(language.getLanguage()))
          && !language.equals(fallbackLanguage)) {
        LOGGER.debug("checking for title fallback {} for movie {}", fallbackLanguage, movie.title);
                /*String lang = providerInfo.getConfig().getValue("titleFallbackLanguage").replace("_", "-");
                Response<Movie> httpResponse = api.moviesService().summary(movie.id, lang, new AppendToResponse(AppendToResponseItem.TRANSLATIONS)).execute();
                if (!httpResponse.isSuccessful()) {
                    throw new HttpException(httpResponse.code(), httpResponse.message());
                }
                Movie m = httpResponse.body();*/

        // get in desired localization
        String[] val = new String[]{"", ""};
        if (StringUtils.isNotBlank(movie.title)) {
          val[0] = movie.title;
        }
        if (StringUtils.isNotBlank(movie.overview)) {
          val[1] = movie.overview;
        }

        // merge empty ones with fallback
                /*String[] temp = PornhubMetadataProvider.getValuesFromTranslation(m.translations, fallbackLanguage);
                if (StringUtils.isBlank(val[0])) {
                    val[0] = temp[0];
                }
                if (StringUtils.isBlank(val[1])) {
                    val[1] = temp[1];
                }*/

        // finally SET the values
        movie.title = val[0];
        movie.overview = val[1];
      }
    }
  }

  /**
   * Get the movie metadata for the given search options
   *
   * @param options the options for scraping
   * @return the metadata (never null)
   * @throws ScrapeException       any exception which can be thrown while scraping
   * @throws MissingIdException    indicates that there was no usable id to scrape
   * @throws NothingFoundException indicated that nothing has been found
   */
  MediaMetadata getMetadata(
      MovieSearchAndScrapeOptions options)
      throws ScrapeException, MissingIdException, NothingFoundException {
    Exception savedException = null;

    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    // pornhubId from option
    String pornhubId = options.getPornhubId();

    if (pornhubId == null || Objects.equals(pornhubId, "")) {
      LOGGER.warn("not possible to scrape from PORNHUB - no pornhubId/imdbId found");
      throw new MissingIdException(MediaMetadata.PORNHUB);
    }

    String language = getRequestLanguage(options.getLanguage());

    // scrape
    Movie movie = null;
    synchronized (api) {

      if (StringUtils.isNotEmpty(pornhubId)) {
        try {
          Response<Document> httpResponse = api.moviesService().summary(pornhubId, null, null)
              .execute();
          if (!httpResponse.isSuccessful()) {
            throw new HttpException(httpResponse.code(), httpResponse.message());
          }
          movie = document2Movie(httpResponse.body());
          verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
        } catch (PornhubNotFoundException e) {
          LOGGER.info("nothing found");
        } catch (Exception e) {
          e.printStackTrace();
          LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
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

  private MediaSearchResult morphMovieToSearchResult(BaseMovie movie,
      MovieSearchAndScrapeOptions query) {
    MediaSearchResult searchResult = new MediaSearchResult(providerInfo.getId(), MediaType.MOVIE);
    searchResult.setId(movie.id);
    searchResult.setTitle(movie.title);
    searchResult
        .setOverview(movie.overview); // empty overview tells us that we have no translation?
    searchResult.setOriginalTitle(movie.original_title);
    searchResult.setOriginalLanguage(movie.original_language);

    if (movie.poster_path != null && !movie.poster_path.isEmpty()) {
      searchResult.setPosterUrl(movie.poster_path);
    }

    // parse release date to year
    if (movie.release_date != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(movie.release_date);
      searchResult.setYear(calendar.get(Calendar.YEAR));
    }

    // calculate score
    if ((StringUtils.isNotBlank(query.getImdbId()) && query.getImdbId()
        .equals(searchResult.getIMDBId()))
        || String.valueOf(query.getPornhubId()).equals(searchResult.getId())) {
      LOGGER.debug("perfect match by ID - set score to 1");
      searchResult.setScore(1);
    } else {
      // calculate the score by comparing the search result with the search options
      searchResult.calculateScore(query);
    }

    return searchResult;
  }

  private Movie document2Movie(Document document) {
    LOGGER.debug(document.html());
    Movie movie = new Movie();
    movie.homepage = document.selectFirst("[property=og:url]").attr("content");
    movie.title = document.selectFirst("[property=og:title]").attr("content");
    movie.original_title = movie.title;
    movie.overview = document.selectFirst("[property=og:description]").attr("content");

    Pattern pattern1 = Pattern.compile(".*viewkey=(.*)$");
    assert movie.homepage != null;
    Matcher matcher1 = pattern1.matcher(movie.homepage);
    if (matcher1.find()) {
      movie.id = matcher1.group(1);
    } else {
      movie.id = null;
    }

    movie.original_language = "en_US";
    movie.poster_path = document.selectFirst("#videoElementPoster").absUrl("src");
        /*String duration = e.selectFirst(".duration").text();
        Pattern pattern = Pattern.compile("^(\\d+):(\\d{2})$");
        Matcher matcher = pattern.matcher(duration);
        if (matcher.find())
            movie.runtime = Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));*/
    movie.vote_average = 0.0;
    movie.vote_count = Integer.valueOf(document.selectFirst(".votesUp").text());
    return movie;
  }

  private Movie searchResult2Movie(Element e) {
    Movie movie = new Movie();
    movie.homepage = e.absUrl("href");

    Pattern pattern1 = Pattern.compile(".*viewkey=(.*)$");
    assert movie.homepage != null;
    Matcher matcher1 = pattern1.matcher(movie.homepage);
    if (matcher1.find()) {
      movie.id = matcher1.group(1);
    } else {
      movie.id = null;
    }
    movie.title = e.attr("title");
    movie.original_title = e.attr("title");
    movie.original_language = "en_US";
    movie.poster_path = e.selectFirst("img").absUrl("src");
    String duration = e.selectFirst(".duration").text();
    Pattern pattern = Pattern.compile("^(\\d+):(\\d{2})$");
    Matcher matcher = pattern.matcher(duration);
    if (matcher.find()) {
      movie.runtime = Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
    }
    movie.vote_average = 0.0;
    movie.vote_count = 0;
    return movie;
  }

  private MediaMetadata morphMovieToMediaMetadata(Movie movie,
      MovieSearchAndScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    md.setId(providerInfo.getId(), movie.id);
    md.setTitle(movie.title);
    md.setOriginalTitle(movie.original_title);
    md.setPlot(movie.overview);
    md.setTagline(movie.tagline);
    md.setRuntime(movie.runtime);

    MediaRating rating = new MediaRating("pornhub");
    rating.setRating(movie.vote_average.floatValue());
    rating.setVotes(movie.vote_count);
    rating.setMaxValue(10);
    md.addRating(rating);

    // Poster
    if (StringUtils.isNotBlank(movie.poster_path)) {
      MediaArtwork ma = new MediaArtwork(providerInfo.getId(),
          MediaArtwork.MediaArtworkType.POSTER);
      ma.setPreviewUrl(movie.poster_path);
      ma.setDefaultUrl(movie.poster_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setPornhubId(movie.id);
      md.addMediaArt(ma);
    }

    for (SpokenLanguage lang : ListUtils.nullSafe(movie.spoken_languages)) {
      if (providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
        md.addSpokenLanguage(LanguageUtils
            .getLocalizedLanguageNameFromLocalizedString(options.getLanguage().toLocale(),
                lang.name, lang.iso_639_1));
      } else {
        md.addSpokenLanguage(lang.iso_639_1);
      }
    }

    for (Country country : ListUtils.nullSafe(movie.production_countries)) {
      if (providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
        md.addCountry(LanguageUtils
            .getLocalizedCountryForLanguage(options.getLanguage().toLocale(), country.name,
                country.iso_3166_1));
      } else {
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
        if (countryCode == null
            || countryCode.getAlpha2().compareToIgnoreCase(countries.iso_3166_1) == 0) {
          // Any release from the desired country will do
          for (ReleaseDate countryReleaseDate : ListUtils.nullSafe(countries.release_dates)) {
            // do not use any empty certifications
            if (StringUtils.isEmpty(countryReleaseDate.certification)) {
              continue;
            }

            md.addCertification(MediaCertification
                .getCertification(countries.iso_3166_1, countryReleaseDate.certification));
          }
        }
      }
    }

    // cast & crew
    if (movie.credits != null) {
      for (CastMember castMember : ListUtils.nullSafe(movie.credits.cast)) {
        Person cm = new Person(Person.Type.ACTOR);
          cm.setId(providerInfo.getId(), castMember.id);
        cm.setName(castMember.name);
        cm.setRole(castMember.character);

        if (StringUtils.isNotBlank(castMember.profile_path)) {
          cm.setThumbUrl(PornhubMetadataProvider.configuration.images.base_url + "h632"
              + castMember.profile_path);
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
        } else if ("Writing".equals(crewMember.department)) {
          cm.setType(WRITER);
          cm.setRole(crewMember.department);
        } else if ("Production".equals(crewMember.department)) {
          cm.setType(PRODUCER);
          cm.setRole(crewMember.job);
        } else {
          continue;
        }
        cm.setId(providerInfo.getId(), crewMember.id);
        cm.setName(crewMember.name);

        if (StringUtils.isNotBlank(crewMember.profile_path)) {
          cm.setThumbUrl(PornhubMetadataProvider.configuration.images.base_url + "h632"
              + crewMember.profile_path);
        }
        if (crewMember.id != null) {
          cm.setProfileUrl("https://www.themoviedb.org/person/" + crewMember.id);
        }

        md.addCastMember(cm);
      }
    }

    // Genres
    for (Genre genre : ListUtils.nullSafe(movie.genres)) {
      md.addGenre(PornhubMetadataProvider.getTmmGenre(genre));
    }
    // "adult" on PORNHUB is always some pr0n stuff, and not just rated 18+ content
    if (movie.adult) {
      md.addGenre(MediaGenres.EROTIC);
    }

    if (movie.belongs_to_collection != null) {
      md.setId(MediaMetadata.PORNHUB_SET, movie.belongs_to_collection.id);
      md.setCollectionName(movie.belongs_to_collection.name);
    }

    return md;
  }

}
