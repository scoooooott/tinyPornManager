/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.SubtitleSearchOptions;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMediaSubtitleProvider;
import org.tinymediamanager.scraper.opensubtitles.model.Info;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.Similarity;

import de.timroes.axmlrpc.XMLRPCException;

/**
 * OpensubtitlesMetadataProvider provides subtitle scraping from OpenSubtitles.org
 * 
 * @author Myron Boyle, Manuel LAggner
 */
@PluginImplementation
public class OpensubtitlesMetadataProvider implements IMediaSubtitleProvider {
  private static final Logger      LOGGER          = LoggerFactory.getLogger(OpensubtitlesMetadataProvider.class);
  private static final String      SERVICE         = "http://api.opensubtitles.org/xml-rpc";
  private static final String      USER_AGENT      = "OSTestUserAgent";                                           // TODO: register!!!
  private static final int         HASH_CHUNK_SIZE = 64 * 1024;

  private static MediaProviderInfo providerInfo    = new MediaProviderInfo("opensubtitles", "opensubtitles.org",
      "Scraper for opensubtitles.org which is able to scrape subtitles");

  private static TmmXmlRpcClient   client          = null;
  private static String            sessionToken    = "";

  public OpensubtitlesMetadataProvider() {
    initAPI();
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
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
  @SuppressWarnings("unchecked")
  public List<MediaSearchResult> search(SubtitleSearchOptions options) throws Exception {
    List<MediaSearchResult> results = new ArrayList<>();

    boolean searchByQuery = false;

    if (options.getMediaType() != MediaType.SUBTITLE) {
      throw new UnsupportedMediaTypeException(options.getMediaType());
    }

    Info info = null;

    // first try: search with moviehash & filesize
    if (options.getFile() != null) {
      File file = options.getFile();
      if (file.exists()) {
        long fileSize = file.length();
        String hash = computeOpenSubtitlesHash(file);

        LOGGER.debug("searching subtitle for " + file);
        LOGGER.debug("moviebytesize: " + fileSize);
        LOGGER.debug("moviehash: " + hash);

        Map<String, Object> mapQuery = new HashMap<>();
        mapQuery.put("moviebytesize", fileSize);
        mapQuery.put("moviehash", hash);
        mapQuery.put("sublanguageid", LanguageUtils.getISO3BLanguage(options.getLanguage().getLanguage()));
        try {
          Object[] arrayQuery = { mapQuery };
          info = new Info((Map<String, Object>) methodCall("SearchSubtitles", arrayQuery));
        }
        catch (Exception e) {
          LOGGER.error("Could not search subtitle.", e);
        }
      }
      else {
        LOGGER.warn("file does not exist " + file);
      }

    }

    // second try: search by IMDB Id
    if (info != null && info.getMovieInfo().isEmpty() && StringUtils.isNotBlank(options.getImdbId())) {
      Map<String, Object> mapQuery = new HashMap<>();
      // use IMDB Id without leading tt
      mapQuery.put("imdbid", options.getImdbId().replace("tt", ""));
      mapQuery.put("sublanguageid", LanguageUtils.getISO3BLanguage(options.getLanguage().getLanguage()));
      searchByQuery = true;
      try {
        Object[] arrayQuery = { mapQuery };
        info = new Info((Map<String, Object>) methodCall("SearchSubtitles", arrayQuery));
      }
      catch (Exception e) {
        LOGGER.error("Could not search subtitle.", e);
      }
    }

    // third try: search by query
    if (info != null && info.getMovieInfo().isEmpty() && StringUtils.isNotBlank(options.getQuery())) {
      Map<String, Object> mapQuery = new HashMap<>();
      mapQuery.put("query", options.getQuery());
      mapQuery.put("sublanguageid", LanguageUtils.getISO3BLanguage(options.getLanguage().getLanguage()));
      searchByQuery = true;
      try {
        Object[] arrayQuery = { mapQuery };
        info = new Info((Map<String, Object>) methodCall("SearchSubtitles", arrayQuery));
      }
      catch (Exception e) {
        LOGGER.error("Could not search subtitle.", e);
      }
    }

    if (info != null) {
      for (Info.MovieInfo movieInfo : info.getMovieInfo()) {
        float score = 0;
        if (searchByQuery) {
          score = Similarity.compareStrings(options.getQuery(), movieInfo.movieTitle);
        }
        MediaSearchResult result = new MediaSearchResult(providerInfo.getId(), MediaType.SUBTITLE, score);
        result.setId(movieInfo.id);
        result.setTitle(movieInfo.movieTitle);
        result.setUrl(movieInfo.subDownloadLink);

        results.add(result);
      }
    }

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
   * @throws XMLRPCException
   */
  private Object methodCall(String method, Object params) throws XMLRPCException {
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
   * This function should be always called when starting communication with OSDb server to identify user, specify application and start a new session
   * (either registered user or anonymous). If user has no account, blank username and password should be used.
   */
  @SuppressWarnings("unchecked")
  private static synchronized void startSession() {
    if (StringUtils.isBlank(sessionToken)) {
      try {
        Map<String, Object> response = (Map<String, Object>) client.call("LogIn", new Object[] { "", "", "", USER_AGENT });
        sessionToken = (String) response.get("token");
        LOGGER.debug("Login OK");
      }
      catch (Exception e) {
        LOGGER.error("Could not start session!", e);
      }
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

    FileChannel fileChannel = null;
    try {
      fileChannel = new FileInputStream(file).getChannel();
      long head = computeOpenSubtitlesHashForChunk(fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, chunkSizeForFile));
      long tail = computeOpenSubtitlesHashForChunk(
          fileChannel.map(FileChannel.MapMode.READ_ONLY, Math.max(size - HASH_CHUNK_SIZE, 0), chunkSizeForFile));

      return String.format("%016x", size + head + tail);
    }
    catch (Exception e) {
      LOGGER.error("Error computing OpenSubtitles hash", e);
    }
    finally {
      try {
        if (fileChannel != null) {
          fileChannel.close();
        }
      }
      catch (IOException e) {
        LOGGER.error("Error closing file stream", e);
      }
    }
    return "";
  }

  private static long computeOpenSubtitlesHashForChunk(ByteBuffer buffer) {

    LongBuffer longBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
    long hash = 0;

    while (longBuffer.hasRemaining()) {
      hash += longBuffer.get();
    }

    return hash;
  }

  //
  // /**
  // * Checks if given video file hashes hashes are already stored in the database.<br>
  // * If found, the server will return basic movie information, including IMDb ID, movie title, release year.<br>
  // * This information can be then used in client application to automatically fill (or verify) movie info.
  // *
  // * @param hashes
  // * 1-n Strings of file hashes
  // */
  // public ArrayList<MovieInfo> checkMovieHash(String... hashes) {
  // ArrayList<MovieInfo> mia = new ArrayList<MovieInfo>();
  // try {
  // XmlRpcStruct token = (XmlRpcStruct) methodCall("CheckMovieHash", hashes);
  // Info mi = new Info(token);
  // mia = mi.getMovieInfo();
  // }
  // catch (Exception e) {
  // LOGGER.error("Cannot fetch CheckMovieHash.", e);
  // }
  // return mia;
  // }
  //
  // /**
  // * Checks if given video file hashes hashes are already stored in the database.<br>
  // * If found, the server will return basic movie information, including IMDb ID, movie title, release year.<br>
  // * This information can be then used in client application to automatically fill (or verify) movie info.
  // *
  // * @param hashes
  // * 1-n Strings of file hashes
  // * @return can return possible MULTIPLE movies for a single hash
  // */
  // public ArrayList<MovieInfo> checkMovieHash2(String... hashes) {
  // ArrayList<MovieInfo> mia = new ArrayList<MovieInfo>();
  // try {
  // XmlRpcStruct token = (XmlRpcStruct) methodCall("CheckMovieHash2", hashes);
  // Info mi = new Info(token);
  // mia = mi.getMovieInfo();
  // }
  // catch (Exception e) {
  // LOGGER.error("Cannot fetch CheckMovieHash.", e);
  // }
  // return mia;
  // }
  //
  // public void getServerInfo() {
  // try {
  // XmlRpcStruct token = (XmlRpcStruct) methodCall("ServerInfo", null);
  // System.out.println(token);
  // }
  // catch (Exception e) {
  // LOGGER.error("Cannot fetch ServerInfo.", e);
  // }
  // }
  //
  // /**
  // * Identifies a MediaFile via hash, to get some basic movie information (title, year, imdb, ...)
  // *
  // * @param mf
  // * the mediafile
  // * @return MediaSearchResult
  // */
  // public List<MediaSearchResult> identify(File file) {
  // LOGGER.info("trying to identify " + file);
  // List<MediaSearchResult> results = new ArrayList<MediaSearchResult>();
  //
  // try {
  // String hash = SubtitleUtils.computeOpenSubtitlesHash(file);
  // LOGGER.info("identify - computed hash: " + hash);
  // ArrayList<MovieInfo> mi = checkMovieHash2(hash);
  // for (MovieInfo i : mi) {
  // MediaSearchResult msr = new MediaSearchResult(this.getProviderInfo().getId());
  // msr.setIMDBId(i.MovieImdbID);
  // msr.setTitle(i.MovieName);
  //
  // if (i.MovieKind.equals("movie")) {
  // msr.setMediaType(MediaType.MOVIE);
  // }
  // else {
  // msr.setMediaType(MediaType.TV_EPISODE); // what... else...?
  // }
  //
  // MediaMetadata md = new MediaMetadata(this.getProviderInfo().getId());
  //
  // try {
  // md.setEpisodeNumber(Integer.parseInt(i.SeriesEpisode));
  // md.setSeasonNumber(Integer.parseInt(i.SeriesSeason));
  // }
  // catch (Exception ignored) {
  // }
  //
  // md.setId(MediaMetadata.IMDB, i.MovieImdbID);
  // md.setTitle(i.MovieName);
  //
  // try {
  // msr.setYear(Integer.parseInt(i.MovieYear));
  // md.setYear(Integer.parseInt(i.MovieYear));
  // }
  // catch (Exception ignored) {
  // }
  //
  // msr.setMetadata(md);
  // results.add(msr);
  // }
  // }
  // catch (Exception e) {
  // LOGGER.error("Could not identify " + file, e);
  // }
  //
  // return results;
  // }
  //

  //
  //
  //
  // /**
  // * This will logout user identified by token token. This function should be called just before exiting/closing client application.
  // */
  // public void closeSession() {
  // try {
  // methodCall("LogOut", null);
  // LOGGER.debug("Session closed");
  // }
  // catch (Exception e) {
  // LOGGER.error("Cannot close session.");
  // }
  // session = null;
  // }
  //

  //
  // @Override
  // public void download(MediaSubtitleDownloadOptions options) {
  // // TODO Auto-generated method stub
  //
  // }
  //
  // /**
  // * Searches for a subtitle
  // *
  // * @param filVideoFile
  // * the video file which to download subtitles
  // */
  // /*
  // * public static void searchAndDownloadSubtitles(File filVideoFile) throws Exception {
  // *
  // * // Let's log in try {
  // *
  // * logIn();
  // *
  // * } catch (Exception e) { throw e; }
  // *
  // * // Let's generate the hash of the movie file. This is a custom hashing // algorithm used by OpenSubtitles. String strHash =
  // * SubtitleUtils.computeOpenSubtitlesHash(filVideoFile);
  // *
  // * // Let's search for matching subtitles URL urlSubtitle = null;
  // *
  // * try {
  // *
  // * LOGGER.info(String.format("Searching for subtiles: %s, %s", strHash, filVideoFile.length()));
  // *
  // * XmlRpcClientConfigImpl rpcConfig = new XmlRpcClientConfigImpl(); rpcConfig.setServerURL(new URL(OSDB_SERVER)); XmlRpcClient rpcClient = new
  // * XmlRpcClient(); rpcClient.setConfig(rpcConfig);
  // *
  // * Map<String, Object> mapQuery = new HashMap<String, Object>(); mapQuery.put("sublanguageid", Locale.getDefault().getISO3Language());
  // * mapQuery.put("moviehash", new String("18379ac9af039390")); mapQuery.put("moviebytesize", new Double(366876694));
  // *
  // * Object[] objParams = new Object[] { strToken, new Object[] { mapQuery } }; HashMap<?, ?> x = (HashMap<?, ?>)
  // rpcClient.execute("SearchSubtitles",
  // * objParams); Object[] lstData = (Object[]) x.get("data"); HashMap<?, ?> mapResult = (HashMap<?, ?>) lstData[0];
  // *
  // * urlSubtitle = new URL((String) mapResult.get("SubDownloadLink"));
  // *
  // * LOGGER.debug("Done.");
  // *
  // * } catch (Exception e) { LOGGER.warn(String.format("Error: %s", e.toString())); }
  // *
  // * // Now that we have the URL, we can download the file. The file is in // the GZIP format so we have to uncompress it. File filSubtitleFile =
  // new
  // * File(filVideoFile.getPath().substring(0, filVideoFile.getPath().length() - 4));
  // *
  // * HttpURLConnection objConnection = null; FileOutputStream objOutputStream = null; GZIPInputStream objGzipInputStream = null;
  // *
  // * try {
  // *
  // * objConnection = (HttpURLConnection) ((urlSubtitle).openConnection()); objOutputStream = new FileOutputStream(filSubtitleFile);
  // objGzipInputStream
  // * = new GZIPInputStream(objConnection.getInputStream());
  // *
  // * LOGGER.info(String.format("Downloading the subtitle: %s", urlSubtitle));
  // *
  // * if (objConnection.getResponseCode() != 200) { LOGGER.debug("The server did not respond properly"); }
  // *
  // * Integer intLength = 0; byte[] bytBuffer = new byte[1024];
  // *
  // * objOutputStream.close(); filSubtitleFile.delete(); if (objConnection.getHeaderField("Content-Disposition").isEmpty() == false) {
  // filSubtitleFile
  // * = new File(filSubtitleFile.getPath() + "." + FileExtention.getExtention(objConnection)); }
  // *
  // * objOutputStream.close(); objOutputStream = new FileOutputStream(filSubtitleFile); while ((intLength = objGzipInputStream.read(bytBuffer)) > 0)
  // {
  // * objOutputStream.write(bytBuffer, 0, intLength); } objConnection.disconnect();
  // *
  // * LOGGER.debug("Downloaded.");
  // *
  // * } catch (Exception e) { LOGGER.warn(String.format("Error: %s", e.toString())); } finally { objOutputStream.close(); objGzipInputStream.close();
  // }
  // *
  // * // Let's log out try {
  // *
  // * logOut();
  // *
  // * } catch (Exception e) { return; }
  // *
  // * }
  // */
}