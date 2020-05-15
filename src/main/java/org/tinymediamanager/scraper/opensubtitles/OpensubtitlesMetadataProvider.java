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

package org.tinymediamanager.scraper.opensubtitles;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.SubtitleSearchAndScrapeOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ISubtitleProvider;
import org.tinymediamanager.scraper.opensubtitles.model.Info;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.Similarity;

/**
 * OpensubtitlesMetadataProvider provides subtitle scraping from OpenSubtitles.org
 *
 * @author Myron Boyle, Manuel Laggner
 */
public class OpensubtitlesMetadataProvider implements ISubtitleProvider {
  public static final String       ID              = "opensubtitles";

  private static final Logger      LOGGER          = LoggerFactory.getLogger(OpensubtitlesMetadataProvider.class);
  private static final String      SERVICE         = "http://api.opensubtitles.org/xml-rpc";
  private static final String      USER_AGENT      = "tinyMediaManager v1";
  private static final int         HASH_CHUNK_SIZE = 64 * 1024;

  private static MediaProviderInfo providerInfo    = createMediaProviderInfo();
  private static TmmXmlRpcClient   client          = null;
  private static String            sessionToken    = "";
  private static String            username        = "";
  private static String            password        = "";

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "OpenSubtitles.org",
        "<html><h3>OpenSubtitles.org</h3><br />A subtitle scraper for OpenSubtitles.org</html>",
        OpensubtitlesMetadataProvider.class.getResource("/org/tinymediamanager/scraper/opensubtitles_org.png"));

    // configure/load settings
    providerInfo.getConfig().addText("username", "");
    providerInfo.getConfig().addText("password", "", true);

    providerInfo.getConfig().load();

    return providerInfo;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
  }

  private static synchronized void initAPI() {
    if (client == null) {
      try {
        client = new TmmXmlRpcClient(new URL(SERVICE), USER_AGENT);
      }
      catch (MalformedURLException e) {
        LOGGER.error("cannot create XmlRpcClient", e);
      }
    }
  }

  @Override
  public List<SubtitleSearchResult> search(SubtitleSearchAndScrapeOptions options) throws ScrapeException {
    // lazy initialization of the api
    initAPI();

    List<SubtitleSearchResult> results = new ArrayList<>();

    // first try: search with moviehash & filesize
    if (options.getFile() != null) {
      File file = options.getFile();
      if (file.exists() && file.length() > 0) {
        long fileSize = file.length();
        String hash = computeOpenSubtitlesHash(file);

        LOGGER.debug("searching subtitle for {}", file);
        LOGGER.debug("moviebytesize: {}; moviehash: {}", fileSize, hash);

        Map<String, Object> mapQuery = new HashMap<>();
        mapQuery.put("moviebytesize", fileSize);
        mapQuery.put("moviehash", hash);
        mapQuery.put("sublanguageid", getLanguageCode(options.getLanguage().toLocale()));
        try {
          Object[] arrayQuery = { mapQuery };
          Info info = new Info((Map<String, Object>) methodCall("SearchSubtitles", arrayQuery));

          for (Info.MovieInfo movieInfo : info.getMovieInfo()) {
            // hash search will give a 100% perfect match
            SubtitleSearchResult result = new SubtitleSearchResult(providerInfo.getId(), 1.0f);
            result.setId(movieInfo.id);
            result.setTitle(movieInfo.movieTitle);
            result.setReleaseName(movieInfo.movieReleaseName);
            result.setUrl(movieInfo.zipDownloadLink);
            result.setRating(movieInfo.subRating);

            results.add(result);
          }

          LOGGER.debug("found {} results", info.getMovieInfo().size());
        }
        catch (TmmXmlRpcException e) {
          switch (e.statusCode) {
            // forbidden/unauthorized
            case HttpURLConnection.HTTP_FORBIDDEN:
            case HttpURLConnection.HTTP_UNAUTHORIZED:
              throw new ScrapeException(new Exception("Access to Opensubtitles was not successfull (HTTP " + e.statusCode + ")"));

            // rate limit exceeded?
            case 429:
            case 407:
              throw new ScrapeException(new Exception("Rate limit exceeded (HTTP " + e.statusCode + ")"));

            // unspecified error:
            default:
              throw new ScrapeException(e.getCause());
          }
        }
        catch (Exception e) {
          LOGGER.error("Could not search subtitle - {}", e.getMessage());
        }
      }
      else {
        LOGGER.warn("file does not exist or is zero byte: {}", file);
      }
    }

    // second try: search by IMDB Id
    if (results.isEmpty() && StringUtils.isNotBlank(options.getImdbId())) {
      Map<String, Object> mapQuery = new HashMap<>();

      LOGGER.debug("searching subtitle for imdb id: {}", options.getImdbId());
      // use IMDB Id without leading tt
      mapQuery.put("imdbid", options.getImdbId().replace("tt", ""));
      mapQuery.put("sublanguageid", getLanguageCode(options.getLanguage().toLocale()));

      if (options.getEpisode() > -1) {
        mapQuery.put("episode", String.valueOf(options.getEpisode()));
      }
      if (options.getSeason() > -1) {
        mapQuery.put("season", String.valueOf(options.getSeason()));
      }

      try {
        Object[] arrayQuery = { mapQuery };
        Info info = new Info((Map<String, Object>) methodCall("SearchSubtitles", arrayQuery));

        for (Info.MovieInfo movieInfo : info.getMovieInfo()) {
          // degrade maximal search score of imdb search to 0.9
          SubtitleSearchResult result = new SubtitleSearchResult(providerInfo.getId(), 0.9f);
          result.setId(movieInfo.id);
          result.setTitle(movieInfo.movieTitle);
          result.setReleaseName(movieInfo.movieReleaseName);
          result.setUrl(movieInfo.zipDownloadLink);
          result.setRating(movieInfo.subRating);

          results.add(result);
        }

        LOGGER.debug("found {} results", info.getMovieInfo().size());
      }
      catch (TmmXmlRpcException e) {
        switch (e.statusCode) {
          // forbidden/unauthorized
          case HttpURLConnection.HTTP_FORBIDDEN:
          case HttpURLConnection.HTTP_UNAUTHORIZED:
            throw new ScrapeException(new Exception("Access to Opensubtitles was not successfull (HTTP " + e.statusCode + ")"));

          // rate limit exceeded?
          case 429:
          case 407:
            throw new ScrapeException(new Exception("Rate limit exceeded (HTTP " + e.statusCode + ")"));

          // unspecified error:
          default:
            throw new ScrapeException(e.getCause());
        }
      }
      catch (Exception e) {
        LOGGER.error("Could not search subtitle.", e);
      }
    }

    // third try: search by query
    if (results.isEmpty() && StringUtils.isNotBlank(options.getSearchQuery())) {
      Map<String, Object> mapQuery = new HashMap<>();

      LOGGER.debug("serching subtitle for query: {}", options.getSearchQuery());

      mapQuery.put("query", options.getSearchQuery());
      mapQuery.put("sublanguageid", getLanguageCode(options.getLanguage().toLocale()));
      try {
        Object[] arrayQuery = { mapQuery };
        Info info = new Info((Map<String, Object>) methodCall("SearchSubtitles", arrayQuery));
        for (Info.MovieInfo movieInfo : info.getMovieInfo()) {
          // degrade maximal search score of title search to 0.8
          float score = 0.8f * Similarity.compareStrings(options.getSearchQuery(), movieInfo.movieTitle);
          SubtitleSearchResult result = new SubtitleSearchResult(providerInfo.getId(), score);
          result.setId(movieInfo.id);
          result.setTitle(movieInfo.movieTitle);
          result.setReleaseName(movieInfo.movieReleaseName);
          result.setUrl(movieInfo.zipDownloadLink);
          result.setRating(movieInfo.subRating);

          results.add(result);
        }

        LOGGER.debug("found {} results", info.getMovieInfo().size());
      }
      catch (TmmXmlRpcException e) {
        switch (e.statusCode) {
          // forbidden/unauthorized
          case HttpURLConnection.HTTP_FORBIDDEN:
          case HttpURLConnection.HTTP_UNAUTHORIZED:
            throw new ScrapeException(new Exception("Access to Opensubtitles was not successfull (HTTP " + e.statusCode + ")"));

          // rate limit exceeded?
          case 429:
          case 407:
            throw new ScrapeException(new Exception("Rate limit exceeded (HTTP " + e.statusCode + ")"));

          // unspecified error:
          default:
            throw new ScrapeException(e);
        }
      }
      catch (Exception e) {
        LOGGER.error("Could not search subtitle.", e);
      }
    }

    Collections.sort(results);
    Collections.reverse(results);

    return results;
  }

  /**
   * calls the specific method with params...
   *
   * @param method
   *          the method
   * @param params
   *          the params
   * @return return value
   * @throws TmmXmlRpcException
   */
  private Object methodCall(String method, Object params) throws TmmXmlRpcException {
    startSession();
    Object response = null;
    if (StringUtils.isNotBlank(sessionToken)) {
      if (params != null) {
        response = client.call(method, new Object[] { sessionToken, params });
      }
      else {
        response = client.call(method, new Object[] { sessionToken, });
      }
    }
    else {
      LOGGER.warn("Have no session - seems the startSession() did not work successfully");
    }
    return response;
  }

  /**
   * opensubtitles need sometimes not ISO 639.2B - this method maps the exceptions
   *
   * @param locale
   *          the language top be converted
   * @return the string accepted by opensubtitles
   */
  private String getLanguageCode(Locale locale) {
    // default ISO 639.2B
    String languageCode = LanguageUtils.getISO3BLanguage(locale.getLanguage());

    // and now the exceptions
    // greek: gre -> ell
    if ("gre".equals(languageCode)) {
      languageCode = "ell";
    }

    return languageCode;
  }

  /**
   * This function should be always called when starting communication with OSDb server to identify user, specify application and start a new session
   * (either registered user or anonymous). If user has no account, blank username and password should be used.
   */
  @SuppressWarnings("unchecked")
  private static synchronized void startSession() throws TmmXmlRpcException {
    if ((providerInfo.getConfig().getValue("username") != null && !username.equals(providerInfo.getConfig().getValue("username")))
        || (providerInfo.getConfig().getValue("password") != null && !password.equals(providerInfo.getConfig().getValue("password")))) {
      username = providerInfo.getConfig().getValue("username");
      password = providerInfo.getConfig().getValue("password");
      sessionToken = "";
    }

    if (StringUtils.isBlank(sessionToken)) {
      Map<String, Object> response = (Map<String, Object>) client.call("LogIn", new Object[] { username, password, "", USER_AGENT });
      sessionToken = (String) response.get("token");
      LOGGER.debug("Login OK");
    }
  }

  /**
   * Returns OpenSubtitle hash or empty string if error
   *
   * @param file
   *          the file to compute the hash
   * @return hash
   */
  public static String computeOpenSubtitlesHash(File file) {
    long size = file.length();
    long chunkSizeForFile = Math.min(HASH_CHUNK_SIZE, size);

    try (FileInputStream is = new FileInputStream(file); FileChannel fileChannel = is.getChannel()) {
      // do not use FileChannel.map() here because it is not releasing resources
      ByteBuffer buf = ByteBuffer.allocate((int) chunkSizeForFile);
      fileChannel.read(buf);
      long head = computeOpenSubtitlesHashForChunk(buf);

      fileChannel.read(buf, Math.max(size - HASH_CHUNK_SIZE, 0));
      long tail = computeOpenSubtitlesHashForChunk(buf);

      return String.format("%016x", size + head + tail);
    }
    catch (Exception e) {
      LOGGER.error("Error computing OpenSubtitles hash", e);
    }
    return "";
  }

  private static long computeOpenSubtitlesHashForChunk(ByteBuffer buffer) {

    buffer.rewind();
    LongBuffer longBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
    long hash = 0;

    while (longBuffer.hasRemaining()) {
      hash += longBuffer.get();
    }

    return hash;
  }
}
