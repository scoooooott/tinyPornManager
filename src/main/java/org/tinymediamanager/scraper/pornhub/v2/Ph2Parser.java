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
package org.tinymediamanager.scraper.pornhub.v2;

import com.scott.pornhub.entities.BaseMovie;
import com.scott.pornhub.entities.Movie;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.HttpException;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.http.InMemoryCachedUrl;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.pornhub.v1.PornhubMetadataProvider;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.UrlUtil;
import retrofit2.Response;

import static org.tinymediamanager.core.entities.Person.Type.ACTOR;
import static org.tinymediamanager.core.entities.Person.Type.PRODUCER;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;
import static org.tinymediamanager.scraper.pornhub.v2.Ph2MovieMetadataProvider.PROVIDER_INFO;
import static org.tinymediamanager.scraper.pornhub.v2.Ph2MovieMetadataProvider.USE_TMDB_FOR_MOVIES;
import static org.tinymediamanager.scraper.pornhub.v2.Ph2MovieMetadataProvider.cleanString;
import static org.tinymediamanager.scraper.pornhub.v2.Ph2MovieMetadataProvider.getTmmGenre;
import static org.tinymediamanager.scraper.pornhub.v2.Ph2MovieMetadataProvider.processMediaArt;

/**
 * The abstract class ImdbParser holds all relevant parsing logic which can be used either by the movie parser and TV
 * show parser
 *
 * @author Manuel Laggner
 */
public abstract class Ph2Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ph2Parser.class);

    protected static final Pattern IMDB_ID_PATTERN = Pattern.compile("/title/(tt[0-9]{6,})/");
    protected static final Pattern PERSON_ID_PATTERN = Pattern.compile("/name/(nm[0-9]{6,})/");
    private final String searchResultCSSSelector = "#videoSearchResult .pcVideoListItem a[href]";

    public static PornhubApi api = new PornhubApi();

    protected final MediaType type;

    protected SimpleDateFormat sdf1 = new SimpleDateFormat("d MMMM yyyy", Locale.US);
    protected SimpleDateFormat sdf2 = new SimpleDateFormat("MMMM yyyy", Locale.US);
    protected SimpleDateFormat sdf3 = new SimpleDateFormat("d MMM. yyyy", Locale.US);
    protected SimpleDateFormat sdf4 = new SimpleDateFormat("d MMM yyyy",
        Locale.US); // no dot like "May"

    protected Ph2Parser(MediaType type) {
        this.type = type;
    }

    /**
     * thread safe initialization of the API todo: 懒汉模式
     *
     * @throws ScrapeException
     */
    private static synchronized void initAPI() throws ScrapeException {
        api = new PornhubApi();
    }

    protected abstract Pattern getUnwantedSearchResultPattern();

    protected abstract Logger getLogger();

    protected abstract MediaMetadata getMetadata(MediaSearchAndScrapeOptions options)
        throws ScrapeException, MissingIdException, NothingFoundException;

    protected abstract CountryCode getCountry();

    /**
     * scrape tmdb for movies too?
     *
     * @return true/false
     */
    protected boolean isUseTmdbForMovies() {
        return Ph2MovieMetadataProvider.PROVIDER_INFO.getConfig().getValueAsBool(USE_TMDB_FOR_MOVIES);
    }

    /**
     * should we scrape also the collection info
     *
     * @return true/false
     */
    protected boolean isScrapeCollectionInfo() {
        return Ph2MovieMetadataProvider.PROVIDER_INFO.getConfig()
            .getValueAsBool("scrapeCollectionInfo");
    }

    /**
     * should we scrape the keywords page too
     *
     * @return true/false
     */
    protected boolean isScrapeKeywordsPage() {
        return Ph2MovieMetadataProvider.PROVIDER_INFO.getConfig().getValueAsBool("scrapeKeywordsPage");
    }

    /**
     * @param options
     * @return
     * @throws ScrapeException
     * @see com.scott.pornhub.services.SearchService#videos(String, Integer, Integer, Integer, Integer, String, String,
     * String)
     */
    protected SortedSet<MediaSearchResult> search(MediaSearchAndScrapeOptions options)
        throws ScrapeException {
        getLogger().debug("search(): {}", options);
        Exception savedException = null;
        SortedSet<MediaSearchResult> result = new TreeSet<>();

        /*
         * IMDb matches seem to come in several "flavours".
         *
         * Firstly, if there is one exact match it returns the matching IMDb page.
         *
         * If that fails to produce a unique hit then a list of possible matches are returned categorised as: Popular Titles (Displaying ? result) Titles
         * (Exact Matches) (Displaying ? result) Titles (Partial Matches) (Displaying ? result)
         *
         * We should check the Exact match section first, then the poplar titles and finally the partial matches.
         *
         * Note: That even with exact matches there can be more than 1 hit, for example "Star Trek"
         */
        String searchTerm = "";

    /*if (StringUtils.isNotEmpty(options.getImdbId())) {
      searchTerm = options.getImdbId();
    }*/

        if (StringUtils.isEmpty(searchTerm)) {
            searchTerm = options.getSearchQuery();
        }

        if (StringUtils.isEmpty(searchTerm)) {
            return result;
        }

        // parse out language and country from the scraper query
        String language = options.getLanguage().getLanguage();
        String country = getCountry().getAlpha2(); // for passing the country to the scrape

        searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

        String pornhubId = options.getPornhubId();

        // begin search
        LOGGER.info("========= BEGIN PORNHUB Scraper Search for: {}", searchTerm);
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
                    MediaSearchResult searchResult = morphMovieToSearchResult(movie, options);
                    result.add(searchResult);
                    LOGGER.debug("found {} result with PORNHUB id", result.size());
                }
                catch (Exception e) {
                    LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
                    savedException = e;
                }
            }

            // 2. try with search string and year
            if (result.isEmpty()) {
                try {
                    int page = 1;
                    int maxPage;

                    // get all result pages
                    do {
                        Response<Document> httpResponse = api.searchService()
                            .videos(searchTerm, page, null, null, null, null, null, null).execute();
                        if (!httpResponse.isSuccessful() || httpResponse.body() == null) {
                            throw new HttpException(httpResponse.code(), httpResponse.message());
                        }
                        /*for (BaseMovie movie : ListUtils.nullSafe(httpResponse.body().result)) {
                            verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
                            result.add(morphMovieToSearchResult(movie, options));
                        }

                        maxPage = httpResponse.body().total_pages;*/

                        Document html = httpResponse.body();
                        // search result
                        Elements searchResult = html.select(searchResultCSSSelector);
                        for (Element e : searchResult) {
                            Movie movie = searchResult2Movie(e);
                            verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
                            result.add(morphMovieToSearchResult(movie, options));
                        }

                        maxPage = Integer.parseInt(html.selectFirst(".page_next_set").text());

                        page++;
                    }
                    while (page <= maxPage);

                    LOGGER.debug("found {} result with search string", result.size());
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
                    savedException = e;
                }
            }

            // 3. if the last token in search string seems to be a year, try without :)
            if (result.isEmpty()) {
                searchTerm = searchTerm.replaceFirst("\\s\\d{4}$", "");
                try {
                    // /search/movie
                    Document resultPage = api.searchService()
                        .videos(searchTerm, 1, null, null, null, null, null, null).execute().body();
                    /*if (resultPage != null && resultPage.result != null) {
                        for (BaseMovie movie : resultPage.result) {
                            verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
                            result.add(morphMovieToSearchResult(movie, options));
                        }
                    }*/
                    assert resultPage != null;
                    Elements searchResult = resultPage.select(searchResultCSSSelector);
                    if (searchResult != null) {
                        for (Element e : searchResult) {
                            Movie movie = searchResult2Movie(e);
                            verifyMovieTitleLanguage(Locale.forLanguageTag(language), movie);
                            result.add(morphMovieToSearchResult(movie, options));
                        }
                    }
                    LOGGER.debug("found {} result with search string without year", result.size());
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.warn("problem getting data from pornhub: {}", e.getMessage());
                    savedException = e;
                }
            }
        }

        // if we have not found anything and there is a saved Exception, throw it to indicate a problem
        if (result.isEmpty() && savedException != null) {
            throw new ScrapeException(savedException);
        }

        return result;
/*
    StringBuilder sb = new StringBuilder(PORNHUB_URL);
    sb.append("video/search");
    sb.append("?search=");
    try {
      // search site was everytime in UTF-8
      sb.append(URLEncoder.encode(searchTerm, UrlUtil.UTF_8));
    } catch (UnsupportedEncodingException ex) {
      // Failed to encode the movie name for some reason!
      getLogger().debug("Failed to encode search term: {}", searchTerm);
      sb.append(searchTerm);
    }

    getLogger().debug("========= BEGIN IMDB Scraper Search for: {}", sb);
    Document doc = null;

    Url url;
    try {
      url = new Url(sb.toString());
      url.addHeader("Accept-Language", getAcceptLanguage(language, country));
    } catch (Exception e) {
      getLogger().debug("tried to fetch search response", e);
      throw new ScrapeException(e);
    }

    try (InputStream is = url.getInputStream()) {
      doc = Jsoup.parse(is, UrlUtil.UTF_8, "");
    } catch (InterruptedException | InterruptedIOException e) {
      // do not swallow these Exceptions
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      getLogger().debug("tried to fetch search response", e);
      throw new ScrapeException(e);
    }

    // check if it was directly redirected to the site
    Elements elements = doc.getElementsByAttributeValue("rel", "canonical");
    for (Element element : elements) {
      MediaMetadata md = null;
      // we have been redirected to the movie site
      String movieName = null;
      String movieId = null;

      String href = element.attr("href");
      Matcher matcher = IMDB_ID_PATTERN.matcher(href);
      while (matcher.find()) {
        if (matcher.group(1) != null) {
          movieId = matcher.group(1);
        }
      }

      // get full information
      if (!StringUtils.isEmpty(movieId)) {
        try {
          md = getMetadata(options);
          if (!StringUtils.isEmpty(md.getTitle())) {
            movieName = md.getTitle();
          }
        } catch (Exception e) {
          getLogger().trace("could not get (sub)metadata: {}", e.getMessage());
        }
      }

      // if a movie name/id was found - return it
      if (StringUtils.isNotEmpty(movieName) && StringUtils.isNotEmpty(movieId)) {
        MediaSearchResult sr = new MediaSearchResult(Ph2MovieMetadataProvider.PROVIDER_INFO.getId(),
            options.getMediaType());
        sr.setTitle(movieName);
        sr.setIMDBId(movieId);
        sr.setYear(md.getYear());
        sr.setMetadata(md);
        sr.setScore(1);

        // and parse out the poster
        String posterUrl = "";
        Elements posters = doc.getElementsByClass("poster");
        if (posters != null && !posters.isEmpty()) {
          Elements imgs = posters.get(0).getElementsByTag("img");
          for (Element img : imgs) {
            posterUrl = img.attr("src");
            posterUrl = posterUrl.replaceAll("UX[0-9]{2,4}_", "");
            posterUrl = posterUrl.replaceAll("UY[0-9]{2,4}_", "");
            posterUrl = posterUrl.replaceAll("CR[0-9]{1,3},[0-9]{1,3},[0-9]{1,3},[0-9]{1,3}_", "");
          }
        }
        if (StringUtils.isNotBlank(posterUrl)) {
          sr.setPosterUrl(posterUrl);
        }

        result.add(sr);
        return result;
      }
    }

    Pattern unwantedSearchResultPattern = getUnwantedSearchResultPattern();

    // parse result
    elements = doc.getElementsByClass("findResult");
    for (Element tr : elements) {
      // we only want the tr's
      if (!"tr".equalsIgnoreCase(tr.tagName())) {
        continue;
      }

      // find the id / name
      String movieName = "";
      String movieId = "";
      int year = 0;
      Elements tds = tr.getElementsByClass("result_text");
      for (Element element : tds) {
        // we only want the td's
        if (!"td".equalsIgnoreCase(element.tagName())) {
          continue;
        }

        // filter out unwanted result
        if (unwantedSearchResultPattern != null) {
          Matcher matcher = unwantedSearchResultPattern.matcher(element.text());
          if (matcher.find()) {
            continue;
          }
        }

        // is there a localized name? (aka)
        String localizedName = "";
        Elements italics = element.getElementsByTag("i");
        if (!italics.isEmpty()) {
          localizedName = italics.text().replace("\"", "");
        }

        // get the name inside the link
        Elements anchors = element.getElementsByTag("a");
        for (Element a : anchors) {
          if (StringUtils.isNotEmpty(a.text())) {
            // movie name
            if (StringUtils.isNotBlank(localizedName) && !language.equals("en")) {
              // take AKA as title, but only if not EN
              movieName = localizedName;
            } else {
              movieName = a.text();
            }

            // parse id
            String href = a.attr("href");
            Matcher matcher = IMDB_ID_PATTERN.matcher(href);
            while (matcher.find()) {
              if (matcher.group(1) != null) {
                movieId = matcher.group(1);
              }
            }

            // try to parse out the year
            Pattern yearPattern = Pattern.compile("\\(([0-9]{4})|/\\)");
            matcher = yearPattern.matcher(element.text());
            while (matcher.find()) {
              if (matcher.group(1) != null) {
                try {
                  year = Integer.parseInt(matcher.group(1));
                  break;
                } catch (Exception ignored) {
                  // nothing to do here
                }
              }
            }
            break;
          }
        }
      }

      // if an id/name was found - parse the poster image
      String posterUrl = "";
      tds = tr.getElementsByClass("primary_photo");
      for (Element element : tds) {
        Elements imgs = element.getElementsByTag("img");
        for (Element img : imgs) {
          posterUrl = img.attr("src");
          posterUrl = posterUrl.replaceAll("UX[0-9]{2,4}_", "");
          posterUrl = posterUrl.replaceAll("UY[0-9]{2,4}_", "");
          posterUrl = posterUrl.replaceAll("CR[0-9]{1,3},[0-9]{1,3},[0-9]{1,3},[0-9]{1,3}_", "");
        }
      }

      // if no movie name/id was found - continue
      if (StringUtils.isEmpty(movieName) || StringUtils.isEmpty(movieId)) {
        continue;
      }

      MediaSearchResult sr = new MediaSearchResult(Ph2MovieMetadataProvider.PROVIDER_INFO.getId(),
          options.getMediaType());
      sr.setTitle(movieName);
      sr.setIMDBId(movieId);
      sr.setYear(year);
      sr.setPosterUrl(posterUrl);

      if (movieId.equals(options.getImdbId())) {
        // perfect match
        sr.setScore(1);
      } else {
        // calculate the score by comparing the search result with the search options
        sr.calculateScore(options);
      }

      result.add(sr);

      // only get 80 result
      if (result.size() >= 80) {
        break;
      }
    }

    return result;*/
    }

    private Movie document2Movie(Document document) {
        LOGGER.debug(document.html());
        Movie movie = new Movie();
        movie.homepage = document.selectFirst("[property=PornhubMovieMetadataProviderurl]")
            .attr("content");
        movie.title = document.selectFirst("[property=og:title]").attr("content");
        movie.original_title = movie.title;
        movie.overview = document.selectFirst("[property=og:description]").attr("content");

        Pattern pattern1 = Pattern.compile(".*viewkey=(.*)$");
        assert movie.homepage != null;
        Matcher matcher1 = pattern1.matcher(movie.homepage);
        if (matcher1.find()) {
            movie.id = matcher1.group(1);
        }
        else {
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
        }
        else {
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

    /**
     * Fallback Language Mechanism - for direct PORNHUB lookup<br> Title always gets returned in en-US, if translation
     * has not been found.<br> But overview IS EMPTY!<br> So, when getting empty overview, we're doing another
     * lookup...
     *
     * @throws IOException
     */
    private void verifyMovieTitleLanguage(Locale language, Movie movie) throws IOException {
        // always doing a fallback scrape when overview empty, regardless of setting!
        if (PROVIDER_INFO.getConfig().getValueAsBool("titleFallback") || StringUtils
            .isEmpty(movie.overview)) {
            Locale fallbackLanguage = Locale
                .forLanguageTag(PROVIDER_INFO.getConfig().getValue("titleFallbackLanguage"));
            if ((movie.title.equals(movie.original_title) && !movie.original_language
                .equals(language.getLanguage()))
                && !language.equals(fallbackLanguage)) {
                LOGGER.debug("checking for title fallback {} for movie {}", fallbackLanguage, movie.title);

                // get in desired localization
                String[] val = new String[] {"", ""};
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

    private MediaSearchResult morphMovieToSearchResult(
        BaseMovie movie, MediaSearchAndScrapeOptions query) {
        MediaSearchResult searchResult = new MediaSearchResult(PROVIDER_INFO.getId(), MediaType.MOVIE);
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
        }
        else {
            // calculate the score by comparing the search result with the search options
            searchResult.calculateScore(query);
        }

        return searchResult;
    }

    /**
     * generates the accept-language http header for pornhub
     *
     * @param language the language code to be used
     * @param country  the country to be used
     * @return the Accept-Language string
     */
    protected static String getAcceptLanguage(String language, String country) {
        List<String> languageString = new ArrayList<>();

        // first: take the preferred language from settings,
        // but validate whether it is legal or not
        if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(country) && LocaleUtils
            .isAvailableLocale(new Locale(language, country))) {
            String combined = language + "-" + country;
            languageString.add(combined.toLowerCase(Locale.ROOT));
        }

        // also build langu & default country
        Locale localeFromLanguage = UrlUtil.getLocaleFromLanguage(language);
        if (localeFromLanguage != null) {
            String combined = language + "-" + localeFromLanguage.getCountry().toLowerCase(Locale.ROOT);
            if (!languageString.contains(combined)) {
                languageString.add(combined);
            }
        }

        if (StringUtils.isNotBlank(language)) {
            languageString.add(language.toLowerCase(Locale.ROOT));
        }

        // second: the JRE language
        Locale jreLocale = Locale.getDefault();
        String combined = (jreLocale.getLanguage() + "-" + jreLocale.getCountry())
            .toLowerCase(Locale.ROOT);
        if (!languageString.contains(combined)) {
            languageString.add(combined);
        }

        if (!languageString.contains(jreLocale.getLanguage().toLowerCase(Locale.ROOT))) {
            languageString.add(jreLocale.getLanguage().toLowerCase(Locale.ROOT));
        }

        // third: fallback to en
        if (!languageString.contains("en-us")) {
            languageString.add("en-us");
        }
        if (!languageString.contains("en")) {
            languageString.add("en");
        }

        // build a http header for the preferred language
        StringBuilder languages = new StringBuilder();
        float qualifier = 1f;

        for (String line : languageString) {
            if (languages.length() > 0) {
                languages.append(",");
            }
            languages.append(line);
            if (qualifier < 1) {
                languages.append(String.format(Locale.US, ";q=%1.1f", qualifier));
            }
            qualifier -= 0.1;
        }

        return languages.toString().toLowerCase(Locale.ROOT);
    }

    protected void parseReferencePage(Document doc, MediaSearchAndScrapeOptions options,
        MediaMetadata md) {
        /*
         * title and year have the following structure
         *
         * <div id="tn15title"><h1>Merida - Legende der Highlands <span>(<a href="/year/2012/">2012</a>) <span class="pro-link">...</span> <span
         * class="title-extra">Brave <i>(original title)</i></span> </span></h1> </div>
         */

        // title
        Element title = doc.getElementsByAttributeValue("name", "title").first();
        if (title != null) {
            String movieTitle = cleanString(title.attr("content"));
            int yearStart = movieTitle.lastIndexOf('(');
            if (yearStart > 0) {
                movieTitle = movieTitle.substring(0, yearStart - 1).trim();
                md.setTitle(movieTitle);
            }
        }

        // original title and year
        Element originalTitleYear = doc.getElementsByAttributeValue("property", "og:title").first();
        if (originalTitleYear != null) {
            String content = originalTitleYear.attr("content");
            int startOfYear = content.lastIndexOf('(');
            if (startOfYear > 0) {
                // noo - this is NOT the original title!!! (seems always english?) parse from AKAs page...
                String originalTitle = content.substring(0, startOfYear - 1).trim();
                md.setOriginalTitle(originalTitle);

                String yearText = content.substring(startOfYear);

                // search year
                Pattern yearPattern = Pattern.compile("[1-2][0-9]{3}");
                Matcher matcher = yearPattern.matcher(yearText);
                while (matcher.find()) {
                    if (matcher.group(0) != null) {
                        String movieYear = matcher.group(0);
                        try {
                            md.setYear(Integer.parseInt(movieYear));
                            break;
                        }
                        catch (Exception ignored) {
                            // nothing to do here
                        }
                    }
                }
            }
        }

        // poster
        Element poster = doc.getElementsByAttributeValue("property", "og:image").first();
        if (poster != null) {
            String posterUrl = poster.attr("content");

            int fileStart = posterUrl.lastIndexOf('/');
            if (fileStart > 0) {
                int parameterStart = posterUrl.indexOf('_', fileStart);
                if (parameterStart > 0) {
                    int startOfExtension = posterUrl.lastIndexOf('.');
                    if (startOfExtension > parameterStart) {
                        posterUrl =
                            posterUrl.substring(0, parameterStart) + posterUrl.substring(startOfExtension);
                    }
                }
            }
            processMediaArt(md, MediaArtwork.MediaArtworkType.POSTER, posterUrl);
        }

        /*
         * <div class="starbar-meta"> <b>7.4/10</b> &nbsp;&nbsp;<a href="ratings" class="tn15more">52,871 votes</a>&nbsp;&raquo; </div>
         */

        // rating and rating count
        Element ratingElement = doc.getElementsByClass("ipl-rating-star__rating").first();
        if (ratingElement != null) {
            String ratingAsString = ratingElement.ownText().replace(",", ".");
            Element votesElement = doc.getElementsByClass("ipl-rating-star__total-votes").first();
            if (votesElement != null) {
                String countAsString = votesElement.ownText().replaceAll("[.,()]", "").trim();
                try {
                    MediaRating rating = new MediaRating("pornhub");
                    rating.setRating(Float.parseFloat(ratingAsString));
                    rating.setVotes(MetadataUtil.parseInt(countAsString));
                    md.addRating(rating);
                }
                catch (Exception e) {
                    getLogger().trace("could not parse rating/vote count: {}", e.getMessage());
                }
            }
        }
        // top250
        Element topRatedElement = doc.getElementsByAttributeValue("href", "/chart/top").first();
        if (topRatedElement != null) {
            Pattern topPattern = Pattern.compile("Top Rated Movies: #([0-9]{1,3})");
            Matcher matcher = topPattern.matcher(topRatedElement.ownText());
            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    try {
                        String top250Text = matcher.group(1);
                        md.setTop250(Integer.parseInt(top250Text));
                    }
                    catch (Exception e) {
                        getLogger().trace("could not parse top250: {}", e.getMessage());
                    }
                }
            }
        }

        // releasedate
        Element releaseDateElement = doc.getElementsByAttributeValue("href",
            "/title/" + options.getImdbId().toLowerCase(Locale.ROOT) + "/releaseinfo")
            .first();
        if (releaseDateElement != null) {
            String releaseDateText = releaseDateElement.ownText();
            int startOfCountry = releaseDateText.indexOf('(');
            if (startOfCountry > 0) {
                releaseDateText = releaseDateText.substring(0, startOfCountry - 1).trim();
            }
            md.setReleaseDate(parseDate(releaseDateText));
        }

        Elements elements = doc.getElementsByClass("ipl-zebra-list__label");
        for (Element element : elements) {
            // only parse tds
            if (!"td".equals(element.tag().getName())) {
                continue;
            }

            String elementText = element.ownText();

            if ("Plot Keywords".equals(elementText)) {
                parseKeywords(element, md);
            }

            if ("Taglines".equals(elementText) && !isUseTmdbForMovies()) {
                Element taglineElement = element.nextElementSibling();
                if (taglineElement != null) {
                    String tagline = cleanString(taglineElement.ownText().replace("»", ""));
                    md.setTagline(tagline);
                }
            }

            if ("Genres".equals(elementText)) {
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    Elements genreElements = nextElement
                        .getElementsByAttributeValueStarting("href", "/genre/");

                    for (Element genreElement : genreElements) {
                        String genreText = genreElement.ownText();
                        md.addGenre(getTmmGenre(genreText));
                    }
                }
            }

            /*
             * Old HTML, but maybe the same content formart <div class="info"><h5>Runtime:</h5><div class="info-content">162 min | 171 min (special edition)
             * | 178 min (extended cut)</div></div>
             */
            if ("Runtime".equals(elementText)) {
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    Element runtimeElement = nextElement.getElementsByClass("ipl-inline-list__item").first();
                    if (runtimeElement != null) {
                        String first = runtimeElement.ownText().split("\\|")[0];
                        String runtimeAsString = cleanString(first.replace("min", ""));
                        int runtime = 0;
                        try {
                            runtime = Integer.parseInt(runtimeAsString);
                        }
                        catch (Exception e) {
                            // try to filter out the first number we find
                            Pattern runtimePattern = Pattern.compile("([0-9]{2,3})");
                            Matcher matcher = runtimePattern.matcher(runtimeAsString);
                            if (matcher.find()) {
                                runtime = Integer.parseInt(matcher.group(0));
                            }
                        }
                        md.setRuntime(runtime);
                    }
                }
            }

            if ("Country".equals(elementText)) {
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    Elements countryElements = nextElement
                        .getElementsByAttributeValueStarting("href", "/country/");
                    Pattern pattern = Pattern.compile("/country/(.*)");

                    for (Element countryElement : countryElements) {
                        Matcher matcher = pattern.matcher(countryElement.attr("href"));
                        if (matcher.matches()) {
                            if (Ph2MovieMetadataProvider.PROVIDER_INFO.getConfig()
                                .getValueAsBool("scrapeLanguageNames")) {
                                md.addCountry(
                                    LanguageUtils
                                        .getLocalizedCountryForLanguage(options.getLanguage().getLanguage(),
                                            countryElement.text(), matcher.group(1)));
                            }
                            else {
                                md.addCountry(matcher.group(1));
                            }
                        }
                    }
                }
            }

            if ("Language".equals(elementText)) {
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    Elements languageElements = nextElement
                        .getElementsByAttributeValueStarting("href", "/language/");
                    Pattern pattern = Pattern.compile("/language/(.*)");

                    for (Element languageElement : languageElements) {
                        Matcher matcher = pattern.matcher(languageElement.attr("href"));
                        if (matcher.matches()) {
                            if (Ph2MovieMetadataProvider.PROVIDER_INFO.getConfig()
                                .getValueAsBool("scrapeLanguageNames")) {
                                md.addSpokenLanguage(LanguageUtils
                                    .getLocalizedLanguageNameFromLocalizedString(options.getLanguage().toLocale(),
                                        languageElement.text(), matcher.group(1)));
                            }
                            else {
                                md.addSpokenLanguage(matcher.group(1));
                            }
                        }
                    }
                }
            }

            if ("Certification".equals(elementText)) {
                Element nextElement = element.nextElementSibling();
                if (nextElement != null) {
                    String languageCode = getCountry().getAlpha2();
                    Elements certificationElements = nextElement.getElementsByAttributeValueStarting("href",
                        "/search/title?certificates=" + languageCode);
                    boolean done = false;
                    for (Element certificationElement : certificationElements) {
                        String certText = certificationElement.ownText();
                        int startOfCert = certText.indexOf(':');
                        if (startOfCert > 0 && certText.length() > startOfCert + 1) {
                            certText = certText.substring(startOfCert + 1);
                        }

                        MediaCertification certification = MediaCertification
                            .getCertification(getCountry(), certText);
                        if (certification != null) {
                            md.addCertification(certification);
                            done = true;
                            break;
                        }
                    }

                    if (!done && "DE".equals(languageCode)) {
                        certificationElements = nextElement
                            .getElementsByAttributeValueStarting("href", "/search/title?certificates=XWG");
                        for (Element certificationElement : certificationElements) {
                            String certText = certificationElement.ownText();
                            int startOfCert = certText.indexOf(':');
                            if (startOfCert > 0 && certText.length() > startOfCert + 1) {
                                certText = certText.substring(startOfCert + 1);
                            }

                            MediaCertification certification = MediaCertification
                                .getCertification(getCountry(), certText);
                            if (certification != null) {
                                md.addCertification(certification);
                                break;
                            }
                        }
                    }

                }
            }
        }

        // director
        Element directorsElement = doc.getElementById("directors");
        while (directorsElement != null && !"header".equals(directorsElement.tag().getName())) {
            directorsElement = directorsElement.parent();
        }
        if (directorsElement != null) {
            directorsElement = directorsElement.nextElementSibling();
        }
        if (directorsElement != null) {
            for (Element directorElement : directorsElement.getElementsByClass("name")) {
                String director = directorElement.text().trim();

                Person cm = new Person(Person.Type.DIRECTOR, director);
                // profile path
                Element anchor = directorElement.getElementsByAttributeValueStarting("href", "/name/")
                    .first();
                if (anchor != null) {
                    Matcher matcher = PERSON_ID_PATTERN.matcher(anchor.attr("href"));
                    if (matcher.find()) {
                        if (matcher.group(0) != null) {
                            cm.setProfileUrl("http://www.pornhub.com" + matcher.group(0));
                        }
                        if (matcher.group(1) != null) {
                            cm.setId(PROVIDER_INFO.getId(), matcher.group(1));
                        }
                    }
                }
                md.addCastMember(cm);
            }
        }

        // actors
        Element castTableElement = doc.getElementsByClass("cast_list").first();
        if (castTableElement != null) {
            Elements tr = castTableElement.getElementsByTag("tr");
            for (Element row : tr) {
                Person cm = parseCastMember(row);
                if (cm != null && StringUtils.isNotEmpty(cm.getName()) && StringUtils
                    .isNotEmpty(cm.getRole())) {
                    cm.setType(ACTOR);
                    md.addCastMember(cm);
                }
            }
        }

        // writers
        Element writersElement = doc.getElementById("writers");
        while (writersElement != null && !"header".equals(writersElement.tag().getName())) {
            writersElement = writersElement.parent();
        }
        if (writersElement != null) {
            writersElement = writersElement.nextElementSibling();
        }
        if (writersElement != null) {
            Elements writersElements = writersElement
                .getElementsByAttributeValueStarting("href", "/name/");

            for (Element writerElement : writersElements) {
                String writer = cleanString(writerElement.ownText());
                Person cm = new Person(WRITER, writer);
                // profile path
                Element anchor = writerElement.getElementsByAttributeValueStarting("href", "/name/")
                    .first();
                if (anchor != null) {
                    Matcher matcher = PERSON_ID_PATTERN.matcher(anchor.attr("href"));
                    if (matcher.find()) {
                        if (matcher.group(0) != null) {
                            cm.setProfileUrl("http://www.pornhub.com" + matcher.group(0));
                        }
                        if (matcher.group(1) != null) {
                            cm.setId(PROVIDER_INFO.getId(), matcher.group(1));
                        }
                    }
                }
                md.addCastMember(cm);
            }
        }

        // producers
        Element producersElement = doc.getElementById("producers");
        while (producersElement != null && !"header".equals(producersElement.tag().getName())) {
            producersElement = producersElement.parent();
        }
        if (producersElement != null) {
            producersElement = producersElement.nextElementSibling();
        }
        if (producersElement != null) {
            Elements producersElements = producersElement
                .getElementsByAttributeValueStarting("href", "/name/");

            for (Element producerElement : producersElements) {
                String producer = cleanString(producerElement.ownText());
                Person cm = new Person(PRODUCER, producer);
                md.addCastMember(cm);
            }
        }

        // producers
        Elements prodCompHeaderElements = doc.getElementsByClass("ipl-list-title");
        Element prodCompHeaderElement = null;

        for (Element possibleProdCompHeaderEl : prodCompHeaderElements) {
            if ("Production Companies".equals(possibleProdCompHeaderEl.ownText())) {
                prodCompHeaderElement = possibleProdCompHeaderEl;
                break;
            }
        }

        while (prodCompHeaderElement != null && !"header"
            .equals(prodCompHeaderElement.tag().getName())) {
            prodCompHeaderElement = prodCompHeaderElement.parent();
        }
        if (prodCompHeaderElement != null) {
            prodCompHeaderElement = prodCompHeaderElement.nextElementSibling();
        }
        if (prodCompHeaderElement != null) {
            Elements prodCompElements = prodCompHeaderElement
                .getElementsByAttributeValueStarting("href", "/company/");

            for (Element prodCompElement : prodCompElements) {
                String prodComp = prodCompElement.ownText();
                md.addProductionCompany(prodComp);
            }
        }
    }

    private void parseKeywords(Element element, MediaMetadata md) {
        // <td>
        // <ul class="ipl-inline-list">
        // <li class="ipl-inline-list__item"><a href="/keyword/male-alien">male-alien</a></li>
        // <li class="ipl-inline-list__item"><a href="/keyword/planetary-romance">planetary-romance</a></li>
        // <li class="ipl-inline-list__item"><a href="/keyword/female-archer">female-archer</a></li>
        // <li class="ipl-inline-list__item"><a href="/keyword/warrioress">warrioress</a></li>
        // <li class="ipl-inline-list__item"><a href="/keyword/original-story">original-story</a></li>
        // <li class="ipl-inline-list__item"><a href="/title/tt0499549/keywords">See All (379) »</a></li>
        // </ul>
        // </td>

        Element parent = element.nextElementSibling();
        Elements keywords = parent.getElementsByClass("ipl-inline-list__item");
        for (Element keyword : keywords) {
            Element a = keyword.getElementsByTag("a").first();
            if (a != null && !a.attr("href").contains("/keywords")) {
                md.addTag(a.ownText());
            }
        }
    }

    protected void parseKeywordsPage(Document doc, MediaSearchAndScrapeOptions options,
        MediaMetadata md) {
        Element div = doc.getElementById("keywords_content");
        if (div == null) {
            return;
        }

        Elements keywords = div.getElementsByClass("sodatext");
        for (Element keyword : keywords) {
            if (StringUtils.isNotBlank(keyword.text())) {
                md.addTag(keyword.text());
            }
        }
    }

    protected void parsePlotsummaryPage(Document doc, MediaSearchAndScrapeOptions options,
        MediaMetadata md) {
        // just take first summary
        // <li class="ipl-zebra-list__item" id="summary-ps21700000">
        // <p>text text text text </p>
        // <div class="author-container">
        // <em>&mdash;<a href="/search/title?plot_author=author">Author Name</a></em>
        // </div>
        // </li>
        Element zebraList = doc.getElementById("plot-summaries-content");
        if (zebraList != null) {
            Elements p = zebraList.getElementsByClass("ipl-zebra-list__item");
            if (!p.isEmpty()) {
                Element em = p.get(0);

                // remove author
                Elements authors = em.getElementsByClass("author-container");
                if (!authors.isEmpty()) {
                    authors.get(0).remove();
                }

                if (!"no-summary-content".equals(em.id())) {
                    String plot = cleanString(em.text());
                    md.setPlot(plot);
                }
            }
        }
    }

    protected void parseReleaseInfoPage(Document doc, MediaSearchAndScrapeOptions options,
        MediaMetadata md) {
        Date releaseDate = null;
        Pattern pattern = Pattern.compile("/calendar/\\?region=(.{2})");

        Element tableReleaseDates = doc.getElementById("release_dates");
        if (tableReleaseDates != null) {
            Elements rows = tableReleaseDates.getElementsByTag("tr");
            // first round: check the release date for the first one with the requested country
            for (Element row : rows) {
                // get the anchor
                Element anchor = row.getElementsByAttributeValueStarting("href", "/calendar/").first();
                if (anchor != null) {
                    Matcher matcher = pattern.matcher(anchor.attr("href"));
                    if (matcher.find()) {
                        String country = matcher.group(1);

                        Element column = row.getElementsByClass("release_date").first();
                        if (column != null) {
                            Date parsedDate = parseDate(column.text());
                            // do not overwrite any parsed date with a null value!
                            if (parsedDate != null && (releaseDate == null || getCountry().getAlpha2()
                                .equalsIgnoreCase(country))) {
                                releaseDate = parsedDate;

                                // abort the loop if we have found a valid date in our desired language
                                if (getCountry().getAlpha2().equalsIgnoreCase(country)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // new way; iterating over class name items
        if (releaseDate == null) {
            Elements rows = doc.getElementsByClass("release-date-item");
            for (Element row : rows) {
                Element anchor = row.getElementsByAttributeValueStarting("href", "/calendar/").first();
                if (anchor != null) {
                    Matcher matcher = pattern.matcher(anchor.attr("href"));
                    // continue if we either do not have found any date yet or the country matches
                    if (matcher.find()) {
                        String country = matcher.group(1);

                        Element column = row.getElementsByClass("release-date-item__date").first();
                        if (column != null) {
                            Date parsedDate = parseDate(column.text());
                            // do not overwrite any parsed date with a null value!
                            if (parsedDate != null && (releaseDate == null || getCountry().getAlpha2()
                                .equalsIgnoreCase(country))) {
                                releaseDate = parsedDate;

                                // abort the loop if we have found a valid date in our desired language
                                if (getCountry().getAlpha2().equalsIgnoreCase(country)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // no matching local release date found; take the first one
        if (releaseDate == null && tableReleaseDates != null) {
            Element column = tableReleaseDates.getElementsByClass("release_date").first();
            if (column != null) {
                releaseDate = parseDate(column.text());
            }
        }

        if (releaseDate != null) {
            md.setReleaseDate(releaseDate);
        }
    }

    protected Person parseCastMember(Element row) {

        Element nameElement = row.getElementsByAttributeValueStarting("itemprop", "name").first();
        if (nameElement == null) {
            return null;
        }
        String name = cleanString(nameElement.ownText());
        String characterName = "";

        Element characterElement = row.getElementsByClass("character").first();
        if (characterElement != null) {
            characterName = cleanString(characterElement.text());
            // and now strip off trailing commentaries like - (120 episodes, 2006-2014)
            characterName = characterName.replaceAll("\\(.*?\\)$", "").trim();
        }

        String image = "";
        Element imageElement = row.getElementsByTag("img").first();
        if (imageElement != null) {
            String imageSrc = imageElement.attr("loadlate");

            if (!StringUtils.isEmpty(imageSrc)) {
                int fileStart = imageSrc.lastIndexOf('/');
                if (fileStart > 0) {
                    // parse out the rescale/crop params
                    int parameterStart = imageSrc.indexOf("._", fileStart);
                    if (parameterStart > 0) {
                        int startOfExtension = imageSrc.lastIndexOf('.');
                        if (startOfExtension > parameterStart) {
                            // rebuild the path - scaled to 632 px height as in tmdb scraper
                            imageSrc = imageSrc.substring(0, parameterStart) + "._UY632" + imageSrc
                                .substring(startOfExtension);
                        }
                    }
                }
                image = imageSrc;
            }
        }

        // profile path
        String profilePath = "";
        String id = "";
        Element anchor = row.getElementsByAttributeValueStarting("href", "/name/").first();
        if (anchor != null) {
            Matcher matcher = PERSON_ID_PATTERN.matcher(anchor.attr("href"));
            if (matcher.find()) {
                if (matcher.group(0) != null) {
                    profilePath = "http://www.pornhub.com" + matcher.group(0);
                }
                if (matcher.group(1) != null) {
                    id = matcher.group(1);
                }
            }
        }

        Person cm = new Person();
        cm.setId(PROVIDER_INFO.getId(), id);
        cm.setName(name);
        cm.setRole(characterName);
        cm.setThumbUrl(image);
        cm.setProfileUrl(profilePath);
        return cm;
    }

    protected Date parseDate(String dateAsSting) {
        try {
            return sdf1.parse(dateAsSting);
        }
        catch (ParseException e) {
            getLogger().trace("could not parse date: {}", e.getMessage());
        }
        try {
            return sdf2.parse(dateAsSting);
        }
        catch (ParseException e) {
            getLogger().trace("could not parse date: {}", e.getMessage());
        }
        try {
            return sdf3.parse(dateAsSting);
        }
        catch (ParseException e) {
            getLogger().trace("could not parse date: {}", e.getMessage());
        }
        try {
            return sdf4.parse(dateAsSting);
        }
        catch (ParseException e) {
            getLogger().trace("could not parse date: {}", e.getMessage());
        }
        return null;
    }

    /****************************************************************************
     * local helper classes
     ****************************************************************************/
    protected class PornhubWorker implements Callable<Document> {

        private final String pageUrl;
        private final String language;
        private final String country;
        private final boolean useCachedUrl;

        PornhubWorker(String url, String language, String country) {
            this(url, language, country, false);
        }

        PornhubWorker(String url, String language, String country, boolean useCachedUrl) {
            this.pageUrl = url;
            this.language = language;
            this.country = country;
            this.useCachedUrl = useCachedUrl;
        }

        @Override
        public Document call() throws Exception {
            Document doc = null;

            Url url;

            try {
                if (useCachedUrl) {
                    url = new InMemoryCachedUrl(this.pageUrl);
                }
                else {
                    url = new Url(this.pageUrl);
                }
                url.addHeader("Accept-Language", getAcceptLanguage(language, country));
            }
            catch (Exception e) {
                getLogger().debug("tried to fetch pornhub page {} - {}", this.pageUrl, e);
                throw new ScrapeException(e);
            }

            try (InputStream is = url.getInputStream()) {
                doc = Jsoup.parse(is, "UTF-8", "");
            }
            catch (InterruptedException | InterruptedIOException e) {
                // do not swallow these Exceptions
                Thread.currentThread().interrupt();
            }
            catch (Exception e) {
                getLogger().debug("tried to fetch pornhub page {} - {}", this.pageUrl, e);
                throw e;
            }

            return doc;
        }
    }
}
