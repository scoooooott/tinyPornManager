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
package org.tinymediamanager.scraper.kodi;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * This class emulates the Kodi addon processing
 *
 * @author Manuel Laggner, Myron Boyle
 */
class KodiAddonProcessor {
  private static final Logger  LOGGER                       = LoggerFactory.getLogger(KodiAddonProcessor.class);

  public static final String   FUNCTION_SETTINGS            = "GetSettings";
  public static final String   FUNCTION_NFO_URL             = "NfoUrl";
  public static final String   FUNCTION_CREATE_SEARCH_URL   = "CreateSearchUrl";
  public static final String   FUNCTION_GET_SEARCH_RESULTS  = "GetSearchResults";
  public static final String   FUNCTION_GET_DETAILS         = "GetDetails";
  private static final String  FUNCTION_GET_EPISODE_LIST    = "GetEpisodeList";
  private static final String  FUNCTION_GET_EPISODE_DETAILS = "GetEpisodeDetails";

  private KodiScraperProcessor scraperProcessor;

  public KodiAddonProcessor(KodiScraper scraper) {
    scraperProcessor = new KodiScraperProcessor(scraper);
  }

  /**
   * As per xbmc scraper, if the nfo data has the details url, then this url is the url of the details for the item.
   *
   * <pre>
   * $$1 is nfo contents
   * </pre>
   *
   * @param nfoContents
   * @return
   */
  public KodiUrl getNfoUrl(String nfoContents) throws Exception {
    String url = null;
    if (scraperProcessor.containsFunction(FUNCTION_NFO_URL)) {
      url = scraperProcessor.executeFunction(FUNCTION_NFO_URL, new String[] { "", nfoContents });
    }
    if (!StringUtils.isEmpty(url)) {
      return new KodiUrl(url);
    }
    else {
      return null;
    }
  }

  /**
   * Return search url.
   *
   * <pre>
   * $$1 is title
   * $$2 is date
   * </pre>
   *
   * @param title
   * @return
   */
  public KodiUrl getSearchUrl(String title, String date) throws Exception {
    if (date == null)
      date = "";
    String url = scraperProcessor.executeFunction(FUNCTION_CREATE_SEARCH_URL,
        new String[] { "", UrlUtil.encode(title), URLEncoder.encode(date, "UTF-8") });
    if (!StringUtils.isEmpty(url)) {
      return new KodiUrl(url);
    }
    else {
      return null;
    }
  }

  /**
   * Returns the Xml for the search results.
   *
   * <pre>
   * $$1 is the content
   * $$2 is the url
   * </pre>
   *
   * @param url
   * @return
   * @throws Exception
   */
  public String getSearchResults(KodiUrl url) throws Exception {
    String contents = url.getTextContent();
    // as per Kodi code
    // https://github.com/xbmc/xbmc/blob/master/xbmc/addons/Scraper.cpp
    // $$1 is content, $$2 is the url
    return scraperProcessor.executeFunction(FUNCTION_GET_SEARCH_RESULTS, new String[] { "", contents, url.toExternalForm() });
  }

  /**
   * Returns the default settings and settings/metadata xml for this provider
   * <p>
   * $$1 is empty
   *
   * @return
   */
  public String getDefaultSettings() {
    return scraperProcessor.executeFunction(FUNCTION_SETTINGS, null);
  }

  /**
   * returns details xml for the given url
   *
   * <pre>
   * $$1 is the url contents
   * $$2 is the movie id
   * $$3 is the movie url
   * </pre>
   *
   * @param url
   * @param id
   * @return
   * @throws Exception
   */
  public String getDetails(KodiUrl url, String id) throws Exception {
    String contents = url.getTextContent();
    String movieId = id;

    if (StringUtils.isEmpty(movieId)) {
      LOGGER.debug("getDetails() called with empty id.");
      movieId = parseIdFromUrl(url.toExternalForm());
    }

    LOGGER.debug("getDetails() called with id: " + movieId + " and url: " + url.toExternalForm());
    return scraperProcessor.executeFunction(FUNCTION_GET_DETAILS, new String[] { "", contents, movieId, url.toExternalForm() });
  }

  private String parseIdFromUrl(String url) {
    // HACK.... In the case that we don't get an id, then let's parse one.
    String movieId = null;
    try {
      Pattern p = Pattern.compile("/(tt[0-9]+)/");
      Matcher m = p.matcher(url);
      if (m.find()) {
        movieId = m.group(1);
        LOGGER.debug("Setting IMDB ID: " + movieId);
      }
    }
    catch (Exception e) {
    }

    if (StringUtils.isEmpty(movieId)) {
      try {
        Pattern p = Pattern.compile("http://www.themoviedb.org/movie/([0-9]+)\\-");
        Matcher m = p.matcher(url);
        if (m.find()) {
          movieId = m.group(1);
          LOGGER.debug("Setting TMDB ID: " + movieId);
        }
      }
      catch (Exception e) {
      }
    }

    if (StringUtils.isEmpty(movieId)) {
      try {
        Pattern p = Pattern.compile("http://www.thetvdb.com/api/(.*?)/series/([0-9]*)/all");
        Matcher m = p.matcher(url);
        if (m.find()) {
          movieId = m.group(2);
          LOGGER.debug("Setting TVDB ID: " + movieId);
        }
      }
      catch (Exception e) {
      }
    }

    return movieId;
  }

  public String getEpisodeList(KodiUrl url) throws Exception {
    String contents = url.getTextContent();
    return scraperProcessor.executeFunction(FUNCTION_GET_EPISODE_LIST, new String[] { "", contents, url.toExternalForm() });
  }

  public String getEpisodeDetails(KodiUrl url, String id) throws Exception {
    String contents = url.getTextContent();
    return scraperProcessor.executeFunction(FUNCTION_GET_EPISODE_DETAILS, new String[] { "", contents, id });
  }
}
