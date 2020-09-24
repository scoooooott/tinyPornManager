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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scott.pornhub.Pornhub;
import com.scott.pornhub.entities.BaseCompany;
import com.scott.pornhub.entities.BaseKeyword;
import com.scott.pornhub.entities.BaseMovie;
import com.scott.pornhub.entities.CastMember;
import com.scott.pornhub.entities.Country;
import com.scott.pornhub.entities.Credits;
import com.scott.pornhub.entities.CrewMember;
import com.scott.pornhub.entities.Genre;
import com.scott.pornhub.entities.Movie;
import com.scott.pornhub.entities.ReleaseDate;
import com.scott.pornhub.entities.ReleaseDatesResult;
import com.scott.pornhub.entities.SpokenLanguage;
import com.scott.pornhub.exceptions.PornhubNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
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
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.pornhub.v1.dto.LDJson;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * The class {@link PornhubMovieMetadataProvider} is used to provide metadata for movies from
 * pornhub
 *
 * @author Manuel Laggner
 */
class PornhubMovieMetadataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(PornhubMovieMetadataProvider.class);

  private final String VIDEO_LIST_ITEM = "#videoSearchResult .pcVideoListItem .title a[href]";

  private static final Pattern LD_JSON_DURATION = Pattern.compile("PT(\\d{2})H(\\d{2})M(\\d{2})S");

  private final String TITLE = ".title-container .title .inlineFree";
  private final String VIEWS = ".count";
  private final String UP_VOTES = ".votesUp";
  private final String DOWN_VOTES = ".votesDown";
  private final String PERCENT = ".percent";
  private final String AUTHOR = ".video-detailed-info .usernameBadgesWrapper a";
  private final String AUTHOR_SUBSCRIBER = ".video-detailed-info .subscribers-count";
  private final String PORN_STAR_LIST = ".pornstarsWrapper .pstar-list-btn";
  private final String CATEGORIES = ".categoriesWrapper a:not(.add-btn-small)";
  private final String TAGS = ".tagsWrapper a:not(.add-btn-small)";
  private final String PRODUCTION = ".productionWrapper";
  private final String DURATION = "meta[property=video:duration]";
  private final String NUMBER_OF_COMMENT = "#cmtWrapper h2 span";
  private final String SELECTOR_LD_JSON = "script[type=application/ld+json]";

  private final Pornhub api;

  private final ObjectMapper mapper = new ObjectMapper();

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
    /*
    searchString = MetadataUtil.removeNonSearchCharacters(searchString);

    Pattern videoNamePattern = Pattern.compile(".*?\\s?-\\s(.*)$");
    Matcher matcher = videoNamePattern.matcher(searchString);
    if (matcher.find()) {
        searchString = matcher.group(1);
    }*/

    String language = getRequestLanguage(options.getLanguage());

    // begin search
    LOGGER.info("========= BEGIN PORNHUB Scraper Search for: {}", searchString);
    synchronized (api) {
      // 1. try with PORNHUBid
      if (StringUtils.isNotEmpty(options.getPornhubId())) {
        LOGGER.debug("found PORNHUB ID {} - getting direct", options.getPornhubId());
        try {
          parseViewPage(options, results, language);
        } catch (Exception e) {
          LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
          savedException = e;
        }
      }

      // 2. try with search string and year
      if (results.isEmpty()) {
        try {
          searchWithKeywords(options, results, searchString, language);
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
          searchWithKeywords(options, results, searchString, language);
          LOGGER.debug("found {} results with search string without year", results.size());
        } catch (Exception e) {
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
   * 按照关键字查询
   *
   * @param options
   * @param results
   * @param searchString
   * @param language
   * @throws IOException
   */
  private void searchWithKeywords(MovieSearchAndScrapeOptions options,
      SortedSet<MediaSearchResult> results, String searchString, String language)
      throws IOException {
    int page = 1;
    int maxPage = 1;
    // get all result pages
    do {
      Document document = Jsoup.connect(String
          .format("https://www.pornhub.com/video/search?search=%s&page=%s",
              URLEncoder.encode(searchString, "utf-8"), page))
          .proxy("127.0.0.1", 10809)
          .get();
      // search result
      Elements searchResult = document.select(VIDEO_LIST_ITEM);
      for (Element e : searchResult) {
        Movie movie = searchResult2Movie(e);
        verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
        MediaSearchResult r = morphMovieToSearchResult(movie, options);
        if (r != null) {
          results.add(r);
        }
      }
      Element maxPageElement = document.selectFirst(".page_next_set");
      if (maxPageElement != null) {
        maxPage = Integer.parseInt(maxPageElement.text());
      }
      page++;
    }
    while (page <= maxPage);
  }

  /**
   * 直接尝试访问视频观看页面
   *
   * @param options
   * @param results
   * @param language
   * @throws IOException
   */
  private void parseViewPage(MovieSearchAndScrapeOptions options,
      SortedSet<MediaSearchResult> results, String language)
      throws IOException {
    Document document = Jsoup
        .connect(String
            .format("https://www.pornhub.com/view_video.php?viewkey=%s", options.getPornhubId()))
        .proxy("127.0.0.1", 10809)
        .get();
    Movie movie = parseDocument(document);
    verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
    MediaSearchResult result = morphMovieToSearchResult(movie, options);
    if (result != null) {
      results.add(result);
    }
    LOGGER.debug("found {} results with PORNHUB id", results.size());
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
    if (providerInfo.getConfig().getValueAsBool("titleFallback") ||
        StringUtils.isEmpty(movie.overview)) {
      Locale fallbackLanguage = Locale
          .forLanguageTag(providerInfo.getConfig().getValue("titleFallbackLanguage"));
      if ((movie.title.equals(movie.original_title) &&
          !movie.original_language.equals(language.getLanguage()))
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

    // pornhubId from option fixme: where is my pornhubId???
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
          Document document = Jsoup.connect(
              String.format("https://www.pornhub.com/view_video.php?viewkey=%s", pornhubId))
              .proxy("127.0.0.1", 10809)
              .get();
          movie = parseDocument(document);
          verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
        } catch (PornhubNotFoundException e) {
          LOGGER.info("nothing found");
        } catch (Exception e) {
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
    Pattern searchName = Pattern.compile(".*?\\s-\\s(.+)$");
    Matcher matcher = searchName.matcher(query.getSearchQuery());

    // calculate score
    if ((StringUtils.isNotBlank(query.getImdbId()) && query.getImdbId()
        .equals(searchResult.getIMDBId()))
        || String.valueOf(query.getPornhubId()).equals(searchResult.getId())) {
      LOGGER.debug("perfect match by ID - set score to 1");
      searchResult.setScore(1);
    } else if (query.getSearchQuery().equals(searchResult.getOriginalTitle())) {
      searchResult.setScore(1);
    } else if (matcher.find() &&
        (matcher.group(0).equalsIgnoreCase(searchResult.getOriginalTitle()) ||
            matcher.group(1).equalsIgnoreCase(searchResult.getOriginalTitle()))) {
      searchResult.setScore(1);
    } else {
      // calculate the score by comparing the search result with the search options
      searchResult.calculateScore(query);
    }

    // get threshold from settings (default 0.75) - to minimize false positives
    final double scraperTreshold = MovieModuleManager.SETTINGS.getScraperThreshold();
    LOGGER.info("using treshold from settings of {}", scraperTreshold);
    if (searchResult.getScore() < scraperTreshold) {
      LOGGER.info("score is lower than {} ({}) - ignore result", scraperTreshold,
          searchResult.getScore());
      MessageManager.instance.pushMessage(
          new Message(Message.MessageLevel.ERROR, movie, "movie.scrape.toolowscore",
              new String[]{String.format("%.2f", scraperTreshold)}));
      return null;
    }
    return searchResult;
  }

  private Movie parseDocument(Document document) {
    if (document == null) {
      return null;
    }
    LOGGER.debug(document.html());

    Movie movie = new Movie();
    LDJson json = null;
    try {
      json = mapper.readValue(document.selectFirst(SELECTOR_LD_JSON).html(), LDJson.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    if (json != null) {
      movie.homepage = json.getEmbedUrl();
      Pattern pattern1 = Pattern.compile(".*viewkey=(.*)$");
      Matcher matcher1 = pattern1.matcher(movie.homepage);
      if (matcher1.find()) {
        movie.id = matcher1.group(1);
      }
      movie.original_language = "en_US";
      movie.original_title = json.getName();
      movie.title = movie.original_title;
      movie.poster_path = json.getThumbnailUrl();
      movie.release_date = json.getUploadDate();
      movie.overview = json.getDescription();

      Matcher matcher2 = LD_JSON_DURATION.matcher(json.getDuration());
      if (matcher2.find()) {
        int hours = Integer.parseInt(matcher2.group(1));
        int minutes = Integer.parseInt(matcher2.group(2));
        int seconds = Integer.parseInt(matcher2.group(3));
        movie.runtime = hours * 60 * 60 + minutes * 60 + seconds;
      }
    }
    movie.vote_average = Double.parseDouble(document.selectFirst(PERCENT).text().replace("%", ""));
    movie.vote_count = Integer.parseInt(document.selectFirst(UP_VOTES).text()) + Integer
        .parseInt(document.selectFirst(DOWN_VOTES).text());

        /*Element originUrl = document.selectFirst("[property=og:url]");
        if (originUrl != null) {
            movie.homepage = originUrl.attr("content");
            Pattern pattern1 = Pattern.compile(".*viewkey=(.*)$");
            Matcher matcher1 = pattern1.matcher(movie.homepage);
            if (matcher1.find()) {
                movie.id = matcher1.group(1);
            }
            else {
                movie.id = null;
            }
        }
        movie.original_title = document.selectFirst("[property=og:title]").attr("content");
        movie.title = movie.original_title;
        movie.poster_path = document.selectFirst("#videoElementPoster").absUrl("src");
        movie.overview = document.selectFirst("[property=og:description]").attr("content");
        String runtime = document.selectFirst("[property=video:duration]").text();
        movie.runtime = StringUtils.isBlank(runtime) ? 0 : Integer.parseInt(runtime);
        */

    document.select(CATEGORIES).forEach(e -> {
      LOGGER.debug(e.html());
      if (movie.genres == null) {
        movie.genres = new ArrayList<>();
      }
      Genre genre = new Genre();
      genre.id = 0;
      genre.name = e.text();
      genre.href = e.absUrl("href");
      movie.genres.add(genre);
    });

    Element videoDetailInfo = document.selectFirst(".video-detailed-info");
    movie.credits = new Credits();
    CrewMember crewMember = new CrewMember();
    crewMember.id = 1;
    crewMember.credit_id = "";
    Element userAvatarElement = videoDetailInfo.selectFirst(".userAvatar img");
    if (userAvatarElement != null) {
      crewMember.profile_path = userAvatarElement.absUrl("src");
    }
    Element userNameBadge = videoDetailInfo.selectFirst(".usernameBadgesWrapper");
    if (userNameBadge != null) {
      crewMember.name = userNameBadge.text();
    }
    movie.credits.crew = Collections.singletonList(crewMember);
    document.select(PORN_STAR_LIST).forEach(e -> {
      if (movie.credits.cast == null) {
        movie.credits.cast = new ArrayList<>();
      }
      CastMember cast = new CastMember();
//            cast.id = 1;
//            cast.cast_id = 1;
      cast.name = e.text();
      cast.character = cast.name;
      cast.order = movie.credits.cast.size() + 1;
      cast.profile_path = e.selectFirst(".avatar").absUrl("href");
      movie.credits.cast.add(cast);
    });
    return movie;
  }

  private Movie searchResult2Movie(Element e) {
    if (e == null) {
      return null;
    }
    LOGGER.debug(e.outerHtml());
    Movie movie = new Movie();
    movie.homepage = e.absUrl("href");
    Pattern pattern1 = Pattern.compile(".*viewkey=(.*)$");
    Matcher matcher1 = pattern1.matcher(movie.homepage);
    if (matcher1.find()) {
      movie.id = matcher1.group(1);
    }
    movie.original_language = "en_US";
    movie.original_title = e.attr("title");
    movie.title = movie.original_title;

    Element posterElement = e.selectFirst("img");
    if (posterElement != null) {
      movie.poster_path = posterElement.absUrl("src");
    }
    Element durationElement = e.selectFirst(".duration");
    if (durationElement != null) {
      String duration = durationElement.text();
      Pattern pattern = Pattern.compile("^(\\d+):(\\d{2})$");
      Matcher matcher = pattern.matcher(duration);
      if (matcher.find()) {
        movie.runtime =
            Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
      }
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
    rating.setMaxValue(100);
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
          cm.setThumbUrl(castMember.profile_path);
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
