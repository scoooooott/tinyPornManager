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

    // title
    Element title = doc.getElementsByAttributeValue("name", "title").first();
    if (title != null) {
      String movieTitle = cleanString(title.attr("content"));
      int yearStart = movieTitle.lastIndexOf("(");
      if (yearStart > 0) {
        movieTitle = movieTitle.substring(0, yearStart - 1).trim();
        md.setTitle(movieTitle);
      }
    }

    // original title and year
    Element originalTitleYear = doc.getElementsByAttributeValue("property", "og:title").first();
    if (originalTitleYear != null) {
      String content = originalTitleYear.attr("content");
      int startOfYear = content.lastIndexOf("(");
      if (startOfYear > 0) {
        String originalTitle = content.substring(0, startOfYear - 1).trim();
        md.setOriginalTitle(originalTitle);

        String yearText = content.substring(startOfYear);

        // search year
        Pattern yearPattern = Pattern.compile("\\(([0-9]{4})|/\\)");
        Matcher matcher = yearPattern.matcher(yearText);
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
    }

    // poster
    Element poster = doc.getElementsByAttributeValue("property", "og:image").first();
    if (poster != null) {
      String posterUrl = poster.attr("content");

      int fileStart = posterUrl.lastIndexOf("/");
      if (fileStart > 0) {
        int parameterStart = posterUrl.indexOf("_", fileStart);
        if (parameterStart > 0) {
          int startOfExtension = posterUrl.lastIndexOf(".");
          if (startOfExtension > parameterStart) {
            posterUrl = posterUrl.substring(0, parameterStart) + posterUrl.substring(startOfExtension);

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
      try {
        md.setRating(Float.valueOf(ratingAsString));
      }
      catch (Exception ignored) {
      }

      Element votesElement = doc.getElementsByClass("ipl-rating-star__total-votes").first();
      if (votesElement != null) {
        String countAsString = votesElement.ownText().replaceAll("[.,()]", "").trim();
        try {
          md.setVoteCount(Integer.parseInt(countAsString));
        }
        catch (Exception ignored) {
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
          catch (Exception ignored) {
          }
        }
      }
    }

    // releasedate
    Element releaseDateElement = doc.getElementsByAttributeValue("href", "/title/" + options.getImdbId().toLowerCase() + "/releaseinfo").first();
    if (releaseDateElement != null) {
      String releaseDateText = releaseDateElement.ownText();
      int startOfCountry = releaseDateText.indexOf("(");
      if (startOfCountry > 0) {
        releaseDateText = releaseDateText.substring(0, startOfCountry - 1).trim();
      }
      try {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
        Date parsedDate = sdf.parse(releaseDateText);
        md.setReleaseDate(parsedDate);
      }
      catch (ParseException otherformat) {
        try {
          SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
          Date parsedDate = sdf.parse(releaseDateText);
          md.setReleaseDate(parsedDate);
        }
        catch (ParseException ignored) {
        }
      }
    }

    Elements elements = doc.getElementsByClass("ipl-zebra-list__label");
    for (Element element : elements) {
      // only parse tds
      if (!"td".equals(element.tag().getName())) {
        continue;
      }

      String elementText = element.ownText();

      if (elementText.equals("Taglines")) {
        if (!!ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("useTmdb")) {
          Element taglineElement = element.nextElementSibling();
          if (taglineElement != null) {
            String tagline = cleanString(taglineElement.ownText().replaceAll("»", ""));
            md.setTagline(tagline);
          }
        }
      }

      if (elementText.equals("Genres")) {
        Element nextElement = element.nextElementSibling();
        if (nextElement != null) {
          Elements genreElements = nextElement.getElementsByAttributeValueStarting("href", "/genre/");

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
      if (elementText.equals("Runtime")) {
        Element nextElement = element.nextElementSibling();
        if (nextElement != null) {
          Element runtimeElement = nextElement.getElementsByClass("ipl-inline-list__item").first();
          if (runtimeElement != null) {
            String first = runtimeElement.ownText().split("\\|")[0];
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
      }

      if (elementText.equals("Country")) {
        Element nextElement = element.nextElementSibling();
        if (nextElement != null) {
          Elements countryElements = nextElement.getElementsByAttributeValueStarting("href", "/country/");

          for (Element countryElement : countryElements) {
            String countryText = countryElement.ownText();
            if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
              md.addCountry(LanguageUtils.getLocalizedCountryForLanguage(options.getLanguage().getLanguage(), countryText));
            }
            else {
              md.addCountry(countryText);
            }
          }
        }
      }

      if (elementText.equals("Language")) {
        Element nextElement = element.nextElementSibling();
        if (nextElement != null) {
          Elements languageElements = nextElement.getElementsByAttributeValueStarting("href", "/language/");

          for (Element languageElement : languageElements) {
            String languageText = languageElement.ownText();
            if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
              md.addSpokenLanguage(LanguageUtils.getLocalizedLanguageNameFromLocalizedString(options.getLanguage(), languageText));
            }
            else {
              md.addSpokenLanguage(languageText);
            }
          }
        }
      }

      if (elementText.equals("Certification")) {
        Element nextElement = element.nextElementSibling();
        if (nextElement != null) {
          String languageCode = options.getCountry().getAlpha2();
          Elements certificationElements = nextElement.getElementsByAttributeValueStarting("href", "/search/title?certificates=" + languageCode);
          boolean done = false;
          for (Element certificationElement : certificationElements) {
            String certText = certificationElement.ownText();
            int startOfCert = certText.indexOf(":");
            if (startOfCert > 0 && certText.length() > startOfCert + 1) {
              certText = certText.substring(startOfCert + 1);
            }

            Certification certification = Certification.getCertification(options.getCountry(), certText);
            if (certification != null) {
              md.addCertification(certification);
              done = true;
              break;
            }
          }

          if (!done && languageCode.equals("DE")) {
            certificationElements = nextElement.getElementsByAttributeValueStarting("href", "/search/title?certificates=XWG");
            for (Element certificationElement : certificationElements) {
              String certText = certificationElement.ownText();
              int startOfCert = certText.indexOf(":");
              if (startOfCert > 0 && certText.length() > startOfCert + 1) {
                certText = certText.substring(startOfCert + 1);
              }

              Certification certification = Certification.getCertification(options.getCountry(), certText);
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
    while (directorsElement != null && directorsElement.tag().getName() != "header") {
      directorsElement = directorsElement.parent();
    }
    if (directorsElement != null) {
      directorsElement = directorsElement.nextElementSibling();
    }
    if (directorsElement != null) {
      directorsElement = directorsElement.getElementsByClass("name").first();
      String director = directorsElement.text().trim();

      MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
      cm.setName(director);
      md.addCastMember(cm);
    }

    // actors
    Element castTableElement = doc.getElementsByClass("cast_list").first();
    if (castTableElement != null) {
      Elements tr = castTableElement.getElementsByTag("tr");
      for (Element row : tr) {
        MediaCastMember cm = parseCastMember(row);
        if (cm != null && StringUtils.isNotEmpty(cm.getName()) && StringUtils.isNotEmpty(cm.getCharacter())) {
          cm.setType(MediaCastMember.CastType.ACTOR);
          md.addCastMember(cm);
        }
      }
    }

    // writers
    Element writersElement = doc.getElementById("writers");
    while (writersElement != null && writersElement.tag().getName() != "header") {
      writersElement = writersElement.parent();
    }
    if (writersElement != null) {
      writersElement = writersElement.nextElementSibling();
    }
    if (writersElement != null) {
      Elements writersElements = writersElement.getElementsByAttributeValueStarting("href", "/name/");

      for (Element writerElement : writersElements) {
        String writer = cleanString(writerElement.ownText());
        MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.WRITER);
        cm.setName(writer);
        md.addCastMember(cm);
      }
    }

    // producers
    Element producersElement = doc.getElementById("producers");
    while (producersElement != null && producersElement.tag().getName() != "header") {
      producersElement = producersElement.parent();
    }
    if (producersElement != null) {
      producersElement = producersElement.nextElementSibling();
    }
    if (producersElement != null) {
      Elements producersElements = producersElement.getElementsByAttributeValueStarting("href", "/name/");

      for (Element producerElement : producersElements) {
        String producer = cleanString(producerElement.ownText());
        MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.PRODUCER);
        cm.setName(producer);
        md.addCastMember(cm);
      }
    }

    // producers
    Elements prodCompHeaderElements = doc.getElementsByClass("ipl-list-title");
    Element prodCompHeaderElement = null;

    for (Element possibleProdCompHeaderEl : prodCompHeaderElements) {
      if (possibleProdCompHeaderEl.ownText().equals("Production Companies")) {
        prodCompHeaderElement = possibleProdCompHeaderEl;
        break;
      }
    }

    while (prodCompHeaderElement != null && prodCompHeaderElement.tag().getName() != "header") {
      prodCompHeaderElement = prodCompHeaderElement.parent();
    }
    if (prodCompHeaderElement != null) {
      prodCompHeaderElement = prodCompHeaderElement.nextElementSibling();
    }
    if (prodCompHeaderElement != null) {
      Elements prodCompElements = prodCompHeaderElement.getElementsByAttributeValueStarting("href", "/company/");

      for (Element prodCompElement : prodCompElements) {
        String prodComp = prodCompElement.ownText();
        md.addProductionCompany(prodComp);
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

    Element nameElement = row.getElementsByAttributeValueStarting("itemprop", "name").first();
    if (nameElement == null) {
      return null;
    }
    String name = cleanString(nameElement.ownText());
    String characterName = "";

    Element characterElement = row.getElementsByClass("character").first();
    if (characterElement != null) {
      characterName = cleanString(characterElement.text());
      // and now strip off trailing commentaries like - (120 episodes,
      // 2006-2014)
      characterName = characterName.replaceAll("\\(.*?\\)$", "").trim();
    }

    String image = "";
    Element imageElement = row.getElementsByTag("img").first();
    if (imageElement != null) {
      String imageSrc = imageElement.attr("loadlate");

      if (!StringUtils.isEmpty(imageSrc)) {
        int fileStart = imageSrc.lastIndexOf("/");
        if (fileStart > 0) {
          int parameterStart = imageSrc.indexOf("_", fileStart);
          if (parameterStart > 0) {
            int startOfExtension = imageSrc.lastIndexOf(".");
            if (startOfExtension > parameterStart) {
              imageSrc = imageSrc.substring(0, parameterStart) + imageSrc.substring(startOfExtension);
            }
          }
        }
        image = imageSrc;
      }
    }

    MediaCastMember cm = new MediaCastMember();
    cm.setCharacter(characterName);
    cm.setName(name);
    cm.setImageUrl(image);
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
