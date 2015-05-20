package org.tinymediamanager.scraper.xbmc;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * This tries to emulate the XMBC Process. Much of what is done in this code was learned from looking at the original c++ code
 * http://xbmc.svn.sourceforge.net/viewvc/xbmc/trunk/XBMC/xbmc/utils/IMDB.cpp?view=markup
 * 
 * @author seans
 * 
 */
public class XbmcMovieProcessor {
  private static final Logger  LOGGER                       = LoggerFactory.getLogger(XbmcMovieProcessor.class);

  public static final String   FUNCTION_SETTINGS            = "GetSettings";
  public static final String   FUNCTION_NFO_URL             = "NfoUrl";
  public static final String   FUNCTION_CREATE_SEARCH_URL   = "CreateSearchUrl";
  public static final String   FUNCTION_GET_SEARCH_RESULTS  = "GetSearchResults";
  public static final String   FUNCTION_GET_DETAILS         = "GetDetails";
  private static final String  FUNCTION_GET_EPISODE_LIST    = "GetEpisodeList";
  private static final String  FUNCTION_GET_EPISODE_DETAILS = "GetEpisodeDetails";

  private XbmcScraperProcessor scraperProcessor;

  public XbmcMovieProcessor(XbmcScraper scraper) {
    scraperProcessor = new XbmcScraperProcessor(scraper);
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
  public XbmcUrl getNfoUrl(String nfoContents) throws Exception {
    String url = null;
    if (scraperProcessor.containsFunction(FUNCTION_NFO_URL)) {
      url = scraperProcessor.executeFunction(FUNCTION_NFO_URL, new String[] { "", nfoContents });
    }
    if (!StringUtils.isEmpty(url)) {
      return new XbmcUrl(url);
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
  public XbmcUrl getSearchUrl(String title, String date) throws Exception {
    if (date == null)
      date = "";
    String url = scraperProcessor.executeFunction(FUNCTION_CREATE_SEARCH_URL, new String[] { "", UrlUtil.encode(title), URLEncoder.encode(date) });
    if (!StringUtils.isEmpty(url)) {
      return new XbmcUrl(url);
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
  public String getSearchResults(XbmcUrl url) throws Exception {
    String contents = url.getTextContent();
    // as per xbmc code
    // http://xbmc.svn.sourceforge.net/viewvc/xbmc/trunk/XBMC/tools/Scrap/Scraper.cpp?revision=7949&view=markup
    // $$1 is content, $$2 is the url
    return scraperProcessor.executeFunction(FUNCTION_GET_SEARCH_RESULTS, new String[] { "", contents, url.toExternalForm() });
  }

  /**
   * Returns the default settings and settings/metadata xml for this provider
   * 
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
   * @param u2
   * @return
   * @throws Exception
   */
  public String getDetails(XbmcUrl url, String id) throws Exception {
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
        Pattern p = Pattern.compile("http://www.thetvdb.com/api/1A4971671264D790/series/([0-9]*)/all");
        Matcher m = p.matcher(url);
        if (m.find()) {
          movieId = m.group(1);
          LOGGER.debug("Setting TVDB ID: " + movieId);
        }
      }
      catch (Exception e) {
      }
    }

    return movieId;
  }

  public String getEpisodeList(XbmcUrl url) throws Exception {
    String contents = url.getTextContent();
    return scraperProcessor.executeFunction(FUNCTION_GET_EPISODE_LIST, new String[] { "", contents, url.toExternalForm() });
  }

  public String getEpisodeDetails(XbmcUrl url, String id) throws Exception {
    String contents = url.getTextContent();
    return scraperProcessor.executeFunction(FUNCTION_GET_EPISODE_DETAILS, new String[] { "", contents, id });
  }
}