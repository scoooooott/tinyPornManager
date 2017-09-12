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
package org.tinymediamanager.scraper.imdb;

import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.cleanString;
import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.getTmmGenre;
import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.processMediaArt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.PluginManager;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * The abstract class ImdbParser holds all relevant parsing logic which can be used either by the movie parser and TV show parser
 * 
 * @author Manuel Laggner
 */
public abstract class ImdbParser {
  protected static final Pattern IMDB_ID_PATTERN = Pattern.compile("/title/(tt[0-9]{7})/");
  protected final MediaType      type;

  protected ImdbParser(MediaType type) {
    this.type = type;
  }

  abstract protected Pattern getUnwantedSearchResultPattern();

  abstract protected Logger getLogger();

  abstract protected ImdbSiteDefinition getImdbSite();

  abstract protected MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception;

  abstract protected String getSearchCategory();

  /**
   * do the search according to the type
   * 
   * @param query
   *          the search params
   * @return the found results
   */
  protected List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    List<MediaSearchResult> result = new ArrayList<>();

    /*
     * IMDb matches seem to come in several "flavours".
     * 
     * Firstly, if there is one exact match it returns the matching IMDb page.
     * 
     * If that fails to produce a unique hit then a list of possible matches are returned categorised as: Popular Titles (Displaying ? Results) Titles
     * (Exact Matches) (Displaying ? Results) Titles (Partial Matches) (Displaying ? Results)
     * 
     * We should check the Exact match section first, then the poplar titles and finally the partial matches.
     * 
     * Note: That even with exact matches there can be more than 1 hit, for example "Star Trek"
     */
    String searchTerm = "";

    if (StringUtils.isNotEmpty(query.getImdbId())) {
      searchTerm = query.getImdbId();
    }

    if (StringUtils.isEmpty(searchTerm)) {
      searchTerm = query.getQuery();
    }

    if (StringUtils.isEmpty(searchTerm)) {
      return result;
    }

    // parse out language and coutry from the scraper query
    String language = query.getLanguage().getLanguage();
    int myear = query.getYear();
    String country = query.getCountry().getAlpha2(); // for passing the country to the scrape

    searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    StringBuilder sb = new StringBuilder(getImdbSite().getSite());
    sb.append("find?q=");
    try {
      // search site was everytime in UTF-8
      sb.append(URLEncoder.encode(searchTerm, "UTF-8"));
    }
    catch (UnsupportedEncodingException ex) {
      // Failed to encode the movie name for some reason!
      getLogger().debug("Failed to encode search term: " + searchTerm);
      sb.append(searchTerm);
    }

    // we need to search for all - otherwise we do not find TV movies
    sb.append(getSearchCategory());

    getLogger().debug("========= BEGIN IMDB Scraper Search for: " + sb.toString());
    Document doc;
    try {
      Url url = new Url(sb.toString());
      url.addHeader("Accept-Language", getAcceptLanguage(language, country));
      doc = Jsoup.parse(url.getInputStream(), "UTF-8", "");
    }
    catch (Exception e) {
      getLogger().debug("tried to fetch search response", e);
      return result;
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
        MediaScrapeOptions options = new MediaScrapeOptions(type);
        options.setImdbId(movieId);
        options.setLanguage(query.getLanguage());
        options.setCountry(CountryCode.valueOf(country));
        md = getMetadata(options);
        if (!StringUtils.isEmpty(md.getTitle())) {
          movieName = md.getTitle();
        }
      }

      // if a movie name/id was found - return it
      if (StringUtils.isNotEmpty(movieName) && StringUtils.isNotEmpty(movieId)) {
        MediaSearchResult sr = new MediaSearchResult(ImdbMetadataProvider.providerInfo.getId(), query.getMediaType());
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
            posterUrl = posterUrl.replaceAll("UX[0-9]{2,4}_", "UX200_");
            posterUrl = posterUrl.replaceAll("UY[0-9]{2,4}_", "UY200_");
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

    // parse results
    // elements = doc.getElementsByClass("result_text");
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

        // filter out unwanted results
        Pattern unwantedSearchResultPattern = getUnwantedSearchResultPattern();
        if (unwantedSearchResultPattern != null) {
          Matcher matcher = unwantedSearchResultPattern.matcher(element.text());
          if (matcher.find()) {
            continue;
          }
        }

        // is there a localized name? (aka)
        String localizedName = "";
        Elements italics = element.getElementsByTag("i");
        if (italics.size() > 0) {
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
            }
            else {
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
                }
                catch (Exception ignored) {
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
          posterUrl = posterUrl.replaceAll("UX[0-9]{2,4}_", "UX200_");
          posterUrl = posterUrl.replaceAll("UY[0-9]{2,4}_", "UY200_");
          posterUrl = posterUrl.replaceAll("CR[0-9]{1,3},[0-9]{1,3},[0-9]{1,3},[0-9]{1,3}_", "");
        }
      }

      // if no movie name/id was found - continue
      if (StringUtils.isEmpty(movieName) || StringUtils.isEmpty(movieId)) {
        continue;
      }

      MediaSearchResult sr = new MediaSearchResult(ImdbMetadataProvider.providerInfo.getId(), query.getMediaType());
      sr.setTitle(movieName);
      sr.setIMDBId(movieId);
      sr.setYear(year);
      sr.setPosterUrl(posterUrl);

      if (movieId.equals(query.getImdbId())) {
        // perfect match
        sr.setScore(1);
      }
      else {
        // compare score based on names
        float score = MetadataUtil.calculateScore(searchTerm, movieName);
        if (posterUrl.isEmpty() || posterUrl.contains("nopicture")) {
          getLogger().debug("no poster - downgrading score by 0.01");
          score = score - 0.01f;
        }
        if (yearDiffers(myear, year)) {
          float diff = (float) Math.abs(year - myear) / 100;
          getLogger().debug("parsed year does not match search result year - downgrading score by " + diff);
          score -= diff;
        }
        sr.setScore(score);
      }

      result.add(sr);

      // only get 40 results
      if (result.size() >= 40) {
        break;
      }
    }
    Collections.sort(result);
    Collections.reverse(result);

    return result;
  }

  protected/*
            * generates the accept-language http header for imdb
            */
  static String getAcceptLanguage(String language, String country) {
    List<String> languageString = new ArrayList<>();

    // first: take the preferred language from settings,
    // but validate whether it is legal or not
    if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(country)) {
      if (LocaleUtils.isAvailableLocale(new Locale(language, country))) {
        String combined = language + "-" + country;
        languageString.add(combined.toLowerCase(Locale.ROOT));
      }
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
    String combined = (jreLocale.getLanguage() + "-" + jreLocale.getCountry()).toLowerCase(Locale.ROOT);
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

  protected MediaMetadata parseCombinedPage(Document doc, MediaScrapeOptions options, MediaMetadata md) {
    /*
     * title and year have the following structure
     * 
     * <div id="tn15title"><h1>Merida - Legende der Highlands <span>(<a href="/year/2012/">2012</a>) <span class="pro-link">...</span> <span
     * class="title-extra">Brave <i>(original title)</i></span> </span></h1> </div>
     */

    // parse title and year
    Element title = doc.getElementById("tn15title");
    if (title != null) {
      Element element;
      // title
      Elements elements = title.getElementsByTag("h1");
      if (elements.size() > 0) {
        element = elements.first();
        String movieTitle = cleanString(element.ownText());
        md.setTitle(movieTitle);
      }

      // year
      elements = title.getElementsByTag("span");
      if (elements.size() > 0) {
        element = elements.first();
        String content = element.text();

        // search year
        Pattern yearPattern = Pattern.compile("\\(([0-9]{4})|/\\)");
        Matcher matcher = yearPattern.matcher(content);
        while (matcher.find()) {
          if (matcher.group(1) != null) {
            String movieYear = matcher.group(1);
            try {
              md.setYear(Integer.parseInt(movieYear));
              break;
            }
            catch (Exception ignored) {
            }
          }
        }
      }

      // original title
      elements = title.getElementsByAttributeValue("class", "title-extra");
      if (elements.size() > 0) {
        element = elements.first();
        String content = element.ownText().trim();
        content = cleanString(StringUtils.removeEnd(StringUtils.removeStart(content, ","), ","));
        md.setOriginalTitle(content);
      }
    }

    // poster
    Element poster = doc.getElementById("primary-poster");
    if (poster != null) {
      String posterUrl = poster.attr("src");
      posterUrl = posterUrl.replaceAll("UX[0-9]{2,4}_", "UX600_");
      posterUrl = posterUrl.replaceAll("UY[0-9]{2,4}_", "UY600_");
      processMediaArt(md, MediaArtwork.MediaArtworkType.POSTER, posterUrl);
    }

    /*
     * <div class="starbar-meta"> <b>7.4/10</b> &nbsp;&nbsp;<a href="ratings" class="tn15more">52,871 votes</a>&nbsp;&raquo; </div>
     */

    // rating and rating count
    Element ratingElement = doc.getElementById("tn15rating");
    if (ratingElement != null) {
      Elements elements = ratingElement.getElementsByClass("starbar-meta");
      if (elements.size() > 0) {
        Element div = elements.get(0);

        // rating comes in <b> tag
        Elements b = div.getElementsByTag("b");
        if (b.size() == 1) {
          String ratingAsString = b.text();
          Pattern ratingPattern = Pattern.compile("([0-9]\\.[0-9])/10");
          Matcher matcher = ratingPattern.matcher(ratingAsString);
          while (matcher.find()) {
            if (matcher.group(1) != null) {
              float rating = 0;
              try {
                rating = Float.valueOf(matcher.group(1));
              }
              catch (Exception ignored) {
              }
              md.setRating(rating);
              break;
            }
          }
        }

        // count
        Elements a = div.getElementsByAttributeValue("href", "ratings");
        if (a.size() == 1) {
          String countAsString = a.text().replaceAll("[.,]|votes", "").trim();
          int voteCount = 0;
          try {
            voteCount = Integer.parseInt(countAsString);
          }
          catch (Exception ignored) {
          }
          md.setVoteCount(voteCount);
        }
      }

      // top250
      elements = ratingElement.getElementsByClass("starbar-special");
      if (elements.size() > 0) {
        Elements a = elements.get(0).getElementsByTag("a");
        if (a.size() > 0) {
          Element anchor = a.get(0);
          Pattern topPattern = Pattern.compile("Top 250: #([0-9]{1,3})");
          Matcher matcher = topPattern.matcher(anchor.ownText());
          while (matcher.find()) {
            if (matcher.group(1) != null) {
              try {
                md.setTop250(Integer.parseInt(matcher.group(1)));
              }
              catch (Exception ignored) {
              }
            }
          }
        }
      }
    }

    // parse all items coming by <div class="info">
    Elements elements = doc.getElementsByClass("info");
    for (Element element : elements) {
      // only parse divs
      if (!"div".equals(element.tag().getName())) {
        continue;
      }

      // elements with h5 are the titles of the values
      Elements h5 = element.getElementsByTag("h5");
      if (h5.size() > 0) {
        Element firstH5 = h5.first();
        String h5Title = firstH5.text();

        // release date
        /*
         * <div class="info"><h5>Release Date:</h5><div class="info-content">5 January 1996 (USA)<a class= "tn15more inline"
         * href="/title/tt0114746/releaseinfo" onclick=
         * "(new Image()).src='/rg/title-tease/releasedates/images/b.gif?link=/title/tt0114746/releaseinfo';" > See more</a>&nbsp;</div></div>
         */
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getReleaseDate() + ".*")) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Element releaseDateElement = div.first();
            String releaseDate = cleanString(releaseDateElement.ownText().replaceAll("»", ""));
            Pattern pattern = Pattern.compile("(.*)\\(.*\\)");
            Matcher matcher = pattern.matcher(releaseDate);
            if (matcher.find()) {
              try {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
                Date parsedDate = sdf.parse(matcher.group(1));
                md.setReleaseDate(parsedDate);
              }
              catch (ParseException otherformat) {
                try {
                  SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
                  Date parsedDate = sdf.parse(matcher.group(1));
                  md.setReleaseDate(parsedDate);
                }
                catch (ParseException ignored) {
                }
              }
            }
          }
        }

        /*
         * <div class="info"><h5>Tagline:</h5><div class="info-content"> (7) To Defend Us... <a class="tn15more inline"
         * href="/title/tt0472033/taglines" onClick= "(new Image()).src='/rg/title-tease/taglines/images/b.gif?link=/title/tt0472033/taglines';" >See
         * more</a>&nbsp;&raquo; </div></div>
         */
        // tagline
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getTagline() + ".*")
            && !ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("useTmdb")) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Element taglineElement = div.first();
            String tagline = cleanString(taglineElement.ownText().replaceAll("»", ""));
            md.setTagline(tagline);
          }
        }

        /*
         * <div class="info-content"><a href="/Sections/Genres/Animation/">Animation</a> | <a href="/Sections/Genres/Action/">Action</a> | <a
         * href="/Sections/Genres/Adventure/">Adventure</a> | <a href="/Sections/Genres/Fantasy/">Fantasy</a> | <a
         * href="/Sections/Genres/Mystery/">Mystery</a> | <a href="/Sections/Genres/Sci-Fi/">Sci-Fi</a> | <a
         * href="/Sections/Genres/Thriller/">Thriller</a> <a class= "tn15more inline" href="/title/tt0472033/keywords" onClick=
         * "(new Image()).src='/rg/title-tease/keywords/images/b.gif?link=/title/tt0472033/keywords';" > See more</a>&nbsp;&raquo; </div>
         */
        // genres are only scraped from akas.imdb.com
        if (h5Title.matches("(?i)" + getImdbSite().getGenre() + "(.*)")) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Elements a = div.first().getElementsByTag("a");
            for (Element anchor : a) {
              if (anchor.attr("href").matches("/Sections/Genres/.*")) {
                md.addGenre(getTmmGenre(anchor.ownText()));
              }
            }
          }
        }
        // }

        /*
         * <div class="info"><h5>Runtime:</h5><div class="info-content">162 min | 171 min (special edition) | 178 min (extended cut)</div></div>
         */
        // runtime
        // if (h5Title.matches("(?i)" + imdbSite.getRuntime() + ".*")) {
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getRuntime() + ".*")) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Element taglineElement = div.first();
            String first = taglineElement.ownText().split("\\|")[0];
            String runtimeAsString = cleanString(first.replaceAll("min", ""));
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

        // <div class="info"><h5>Country:</h5><div class="info-content"><a href="/country/fr">France</a> | <a href="/country/es">Spain</a> | <a
        // href="/country/it">Italy</a> | <a href="/country/hu">Hungary</a></div></div>
        // <div class="info-content"><a href="/country/xwg">West Germany</a></div> (!!!)

        // country
        if (h5Title.matches("(?i)Country.*")) {
          Elements a = element.getElementsByTag("a");
          for (Element anchor : a) {
            Pattern pattern = Pattern.compile("/country/(.*)");
            Matcher matcher = pattern.matcher(anchor.attr("href"));
            if (matcher.matches()) {
              if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
                md.addCountry(LanguageUtils.getLocalizedCountryForLanguage(options.getLanguage().getLanguage(), anchor.text(), matcher.group(1)));
              }
              else {
                md.addCountry(matcher.group(1));
              }
            }
          }
        }

        /*
         * <div class="info"><h5>Language:</h5><div class="info-content"><a href="/language/en">English</a> | <a href="/language/de">German</a> | <a
         * href="/language/fr">French</a> | <a href="/language/it">Italian</a></div>
         */
        // Spoken languages
        if (h5Title.matches("(?i)Language.*")) {
          Elements a = element.getElementsByTag("a");
          for (Element anchor : a) {
            Pattern pattern = Pattern.compile("/language/(.*)");
            Matcher matcher = pattern.matcher(anchor.attr("href"));
            if (matcher.matches()) {
              if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
                md.addSpokenLanguage(
                    LanguageUtils.getLocalizedLanguageNameFromLocalizedString(options.getLanguage(), anchor.text(), matcher.group(1)));
              }
              else {
                md.addSpokenLanguage(matcher.group(1));
              }
            }
          }
        }

        /*
         * <div class="info"><h5>Certification:</h5><div class="info-content"><a href="/search/title?certificates=us:pg">USA:PG</a> <i>(certificate
         * #47489)</i> | <a href="/search/title?certificates=ca:pg">Canada:PG</a> <i>(Ontario)</i> | <a
         * href="/search/title?certificates=au:pg">Australia:PG</a> | <a href="/search/title?certificates=in:u">India:U</a> | <a
         * href="/search/title?certificates=ie:pg">Ireland:PG</a> ...</div></div>
         */
        // certification
        // if (h5Title.matches("(?i)" + imdbSite.getCertification() + ".*")) {
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getCertification() + ".*")) {
          Elements a = element.getElementsByTag("a");
          for (Element anchor : a) {
            // certification for the right country
            if (anchor.attr("href").matches("(?i)/search/title\\?certificates=" + options.getCountry().getAlpha2() + ".*")) {
              Pattern certificationPattern = Pattern.compile(".*:(.*)");
              Matcher matcher = certificationPattern.matcher(anchor.ownText());
              Certification certification = null;
              while (matcher.find()) {
                if (matcher.group(1) != null) {
                  certification = Certification.getCertification(options.getCountry(), matcher.group(1));
                }
              }

              if (certification != null) {
                md.addCertification(certification);
                break;
              }
            }
          }
        }
      }

      /*
       * <div id="director-info" class="info"> <h5>Director:</h5> <div class="info-content"><a href="/name/nm0000416/" onclick=
       * "(new Image()).src='/rg/directorlist/position-1/images/b.gif?link=name/nm0000416/';" >Terry Gilliam</a><br/> </div> </div>
       */
      // director
      if ("director-info".equals(element.id())) {
        Elements a = element.getElementsByTag("a");
        for (Element anchor : a) {
          if (anchor.attr("href").matches("/name/nm.*")) {
            MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
            cm.setName(anchor.ownText());
            md.addCastMember(cm);
          }
        }
      }
    }

    /*
     * <table class="cast"> <tr class="odd"><td class="hs"><a href="http://pro.imdb.com/widget/resume_redirect/" onClick=
     * "(new Image()).src='/rg/resume/prosystem/images/b.gif?link=http://pro.imdb.com/widget/resume_redirect/';" ><img src=
     * "http://i.media-imdb.com/images/SF9113d6f5b7cb1533c35313ccd181a6b1/tn15/no_photo.png" width="25" height="31" border="0"></td><td class="nm"><a
     * href="/name/nm0577828/" onclick= "(new Image()).src='/rg/castlist/position-1/images/b.gif?link=/name/nm0577828/';" >Joseph Melito</a></td><td
     * class="ddd"> ... </td><td class="char"><a href="/character/ch0003139/">Young Cole</a></td></tr> <tr class="even"><td class="hs"><a
     * href="/name/nm0000246/" onClick= "(new Image()).src='/rg/title-tease/tinyhead/images/b.gif?link=/name/nm0000246/';" ><img src=
     * "http://ia.media-imdb.com/images/M/MV5BMjA0MjMzMTE5OF5BMl5BanBnXkFtZTcwMzQ2ODE3Mw@@._V1._SY30_SX23_.jpg" width="23" height="32"
     * border="0"></a><br></td><td class="nm"><a href="/name/nm0000246/" onclick=
     * "(new Image()).src='/rg/castlist/position-2/images/b.gif?link=/name/nm0000246/';" >Bruce Willis</a></td><td class="ddd"> ... </td><td
     * class="char"><a href="/character/ch0003139/">James Cole</a></td></tr> <tr class="odd"><td class="hs"><a href="/name/nm0781218/" onClick=
     * "(new Image()).src='/rg/title-tease/tinyhead/images/b.gif?link=/name/nm0781218/';" ><img src=
     * "http://ia.media-imdb.com/images/M/MV5BODI1MTA2MjkxM15BMl5BanBnXkFtZTcwMTcwMDg2Nw@@._V1._SY30_SX23_.jpg" width="23" height="32"
     * border="0"></a><br></td><td class="nm"><a href="/name/nm0781218/" onclick=
     * "(new Image()).src='/rg/castlist/position-3/images/b.gif?link=/name/nm0781218/';" >Jon Seda</a></td><td class="ddd"> ... </td><td
     * class="char"><a href="/character/ch0003143/">Jose</a></td></tr>...</table>
     */
    // cast
    elements = doc.getElementsByClass("cast");
    if (elements.size() > 0) {
      Elements tr = elements.get(0).getElementsByTag("tr");
      for (Element row : tr) {
        MediaCastMember cm = parseCastMember(row);
        if (StringUtils.isNotEmpty(cm.getName()) && StringUtils.isNotEmpty(cm.getCharacter())) {
          cm.setType(MediaCastMember.CastType.ACTOR);
          md.addCastMember(cm);
        }
      }
    }

    Element content = doc.getElementById("tn15content");
    if (content != null) {
      elements = content.getElementsByTag("table");
      for (Element table : elements) {
        // writers
        if (table.text().contains(ImdbSiteDefinition.IMDB_COM.getWriter())) {
          Elements anchors = table.getElementsByTag("a");
          for (Element anchor : anchors) {
            if (anchor.attr("href").matches("/name/nm.*")) {
              MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.WRITER);
              cm.setName(anchor.ownText());
              md.addCastMember(cm);
            }
          }
        }

        // producers
        if (table.text().contains(ImdbSiteDefinition.IMDB_COM.getProducers())) {
          Elements rows = table.getElementsByTag("tr");
          for (Element row : rows) {
            if (row.text().contains(ImdbSiteDefinition.IMDB_COM.getProducers())) {
              continue;
            }
            Elements columns = row.children();
            if (columns.size() == 0) {
              continue;
            }
            MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.PRODUCER);
            String name = cleanString(columns.get(0).text());
            if (StringUtils.isBlank(name)) {
              continue;
            }
            cm.setName(name);
            if (columns.size() >= 3) {
              cm.setPart(cleanString(columns.get(2).text()));
            }
            md.addCastMember(cm);
          }
        }
      }
    }

    // Production companies
    elements = doc.getElementsByClass("blackcatheader");
    for (Element blackcatheader : elements) {
      if (blackcatheader.ownText().equals(ImdbSiteDefinition.IMDB_COM.getProductionCompanies())) {
        Elements a = blackcatheader.nextElementSibling().getElementsByTag("a");
        for (Element anchor : a) {
          md.addProductionCompany(anchor.ownText());
        }

        break;
      }
    }

    return md;
  }

  protected MediaMetadata parsePlotsummaryPage(Document doc, MediaScrapeOptions options, MediaMetadata md) {
    // imdb.com has another site structure
    if (getImdbSite() == ImdbSiteDefinition.IMDB_COM) {

      // first check synopsis content
      // Element zebraList = doc.getElementById("plot-synopsis-content");
      // if (zebraList != null) {
      // Elements p = zebraList.getElementsByClass("ipl-zebra-list__item");
      // if (!p.isEmpty()) {
      // Element em = p.get(0);
      // if (!"no-synopsis-content".equals(em.id())) {
      // String plot = cleanString(em.text());
      // md.setPlot(plot);
      // }
      // }
      // }
      // NOPE: synopsis contains spoilers

      // just take first summary
      Element zebraList = doc.getElementById("plot-summaries-content");
      if (zebraList != null) {
        Elements p = zebraList.getElementsByClass("ipl-zebra-list__item");
        if (!p.isEmpty()) {
          Element em = p.get(0);
          if (!"no-summary-content".equals(em.id())) {
            String plot = cleanString(em.text());
            md.setPlot(plot);
          }
        }
      }

    }
    else {
      Element wiki = doc.getElementById("swiki.2.1");
      if (wiki != null) {
        String plot = cleanString(wiki.ownText());
        md.setPlot(plot);
      }
    }

    return md;
  }

  protected MediaCastMember parseCastMember(Element row) {
    Elements td = row.getElementsByTag("td");
    MediaCastMember cm = new MediaCastMember();
    for (Element column : td) {
      // actor thumb
      if (column.hasClass("hs")) {
        Elements img = column.getElementsByTag("img");
        if (img.size() > 0) {
          String thumbUrl = img.get(0).attr("src");
          if (thumbUrl.contains("no_photo.png")) {
            cm.setImageUrl("");
          }
          else {
            thumbUrl = thumbUrl.replaceAll("SX[0-9]{2,4}_", "SX400_");
            thumbUrl = thumbUrl.replaceAll("SY[0-9]{2,4}_", "");
            cm.setImageUrl(thumbUrl);
          }
        }
      }
      // actor name
      if (column.hasClass("nm")) {
        cm.setName(cleanString(column.text()));
      }
      // character
      if (column.hasClass("char")) {
        try {
          String characterName = cleanString(column.text());
          // and now strip off trailing commentaries like - (120 episodes,
          // 2006-2014)
          characterName = characterName.replaceAll("\\(.*?\\)$", "").trim();
          cm.setCharacter(characterName);
        }
        catch (Exception ignored) {
        }
      }
    }

    return cm;
  }

  /**
   * Is i1 != i2 (when >0)
   */
  private boolean yearDiffers(Integer i1, Integer i2) {
    return i1 != null && i1 != 0 && i2 != null && i2 != 0 && i1 != i2;
  }

  /****************************************************************************
   * local helper classes
   ****************************************************************************/
  protected class ImdbWorker implements Callable<Document> {
    private String             url;
    private String             language;
    private String             country;
    private ImdbSiteDefinition imdbSite;
    private Document           doc = null;

    public ImdbWorker(String url, String language, String country, ImdbSiteDefinition imdbSite) {
      this.url = url;
      this.language = language;
      this.country = country;
      this.imdbSite = imdbSite;
    }

    @Override
    public Document call() throws Exception {
      doc = null;
      try {
        Url url = new Url(this.url);
        url.addHeader("Accept-Language", getAcceptLanguage(language, country));
        doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");
      }
      catch (Exception e) {
        getLogger().debug("tried to fetch imdb page " + url, e);
      }
      return doc;
    }
  }

  static class TmdbWorker implements Callable<MediaMetadata> {
    private String      imdbId;
    private Locale      language;
    private CountryCode certificationCountry;

    public TmdbWorker(String imdbId, Locale language, CountryCode certificationCountry) {
      this.imdbId = imdbId;
      this.language = language;
      this.certificationCountry = certificationCountry;
    }

    @Override
    public MediaMetadata call() throws Exception {
      try {
        IMovieMetadataProvider tmdb = null;
        List<IMovieMetadataProvider> providers = PluginManager.getInstance().getPluginsForInterface(IMovieMetadataProvider.class);
        for (IMovieMetadataProvider provider : providers) {
          if ("tmdb".equals(provider.getProviderInfo().getId())) {
            tmdb = provider;
            break;
          }
        }
        if (tmdb == null) {
          return null;
        }

        MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
        options.setLanguage(language);
        options.setCountry(certificationCountry);
        options.setImdbId(imdbId);
        return tmdb.getMetadata(options);
      }
      catch (Exception e) {
        return null;
      }
    }
  }
}
