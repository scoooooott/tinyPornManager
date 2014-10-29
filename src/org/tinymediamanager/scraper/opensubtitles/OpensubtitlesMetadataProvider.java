/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.IMediaSubtitleProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.opensubtitles.model.ApiStartSession;
import org.tinymediamanager.scraper.opensubtitles.model.Info;
import org.tinymediamanager.scraper.opensubtitles.model.Info.MovieInfo;
import org.tinymediamanager.scraper.util.SubtitleUtils;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcStruct;

/**
 * @author Myron Boyle
 */
public class OpensubtitlesMetadataProvider implements IMediaSubtitleProvider {

  private static final Logger      LOGGER       = LoggerFactory.getLogger(OpensubtitlesMetadataProvider.class);
  private static final String      SERVICE      = "http://api.opensubtitles.org/xml-rpc";
  private static final String      USER_AGENT   = "OS Test User Agent";                                                // TODO: register!!!
  private static MediaProviderInfo providerInfo = new MediaProviderInfo("opensubtitles", "opensubtitles.org",
                                                    "Scraper for opensubtitles.org which is able to scrape subtitles");
  private static ApiStartSession   session      = null;
  private static XmlRpcClient      client       = null;

  public OpensubtitlesMetadataProvider() {
    initAPI();
  }

  private static synchronized void initAPI() {
    if (client == null) {
      try {
        client = new XmlRpcClient(SERVICE, false);
        client.setRequestProperty("User-Agent", USER_AGENT);
      }
      catch (MalformedURLException e) {
        LOGGER.error("cannot create XmlRpcClient", e);
      }
    }
  }

  /**
   * calls the specific method with params...
   * 
   * @param method
   *          the method
   * @param params
   *          the params
   * @return return value
   * @throws XmlRpcFault
   * @throws XmlRpcException
   */
  public Object methodCall(String method, Object params) throws XmlRpcException, XmlRpcFault {
    startSession();
    Object token = null;
    if (session != null) {
      if (params != null) {
        token = client.invoke(method, new Object[] { session.getToken(), params });
      }
      else {
        token = client.invoke(method, new Object[] { session.getToken(), });
      }
    }
    else {
      LOGGER.warn("Have no session - seems the startSession() did not work successfully");
    }
    return token;
  }

  /**
   * Checks if given video file hashes hashes are already stored in the database.<br>
   * If found, the server will return basic movie information, including IMDb ID, movie title, release year.<br>
   * This information can be then used in client application to automatically fill (or verify) movie info.
   * 
   * @param hashes
   *          1-n Strings of file hashes
   */
  public ArrayList<MovieInfo> checkMovieHash(String... hashes) {
    ArrayList<MovieInfo> mia = new ArrayList<MovieInfo>();
    try {
      XmlRpcStruct token = (XmlRpcStruct) methodCall("CheckMovieHash", hashes);
      Info mi = new Info(token);
      mia = mi.getMovieInfo();
    }
    catch (Exception e) {
      LOGGER.error("Cannot fetch CheckMovieHash.", e);
    }
    return mia;
  }

  /**
   * Checks if given video file hashes hashes are already stored in the database.<br>
   * If found, the server will return basic movie information, including IMDb ID, movie title, release year.<br>
   * This information can be then used in client application to automatically fill (or verify) movie info.
   * 
   * @param hashes
   *          1-n Strings of file hashes
   * @return can return possible MULTIPLE movies for a single hash
   */
  public ArrayList<MovieInfo> checkMovieHash2(String... hashes) {
    ArrayList<MovieInfo> mia = new ArrayList<MovieInfo>();
    try {
      XmlRpcStruct token = (XmlRpcStruct) methodCall("CheckMovieHash2", hashes);
      Info mi = new Info(token);
      mia = mi.getMovieInfo();
    }
    catch (Exception e) {
      LOGGER.error("Cannot fetch CheckMovieHash.", e);
    }
    return mia;
  }

  public void getServerInfo() {
    try {
      XmlRpcStruct token = (XmlRpcStruct) methodCall("ServerInfo", null);
      System.out.println(token);
    }
    catch (Exception e) {
      LOGGER.error("Cannot fetch ServerInfo.", e);
    }
  }

  /**
   * Identifies a MediaFile via hash, to get some basic movie information (title, year, imdb, ...)
   * 
   * @param mf
   *          the mediafile
   * @return MediaSearchResult
   */
  public List<MediaSearchResult> identify(MediaFile mf) {
    LOGGER.info("trying to identify " + mf.getFile());
    List<MediaSearchResult> results = new ArrayList<MediaSearchResult>();

    try {
      String hash = SubtitleUtils.computeOpenSubtitlesHash(mf.getFile());
      LOGGER.info("identify - computed hash: " + hash);
      ArrayList<MovieInfo> mi = checkMovieHash2(hash);
      for (MovieInfo i : mi) {
        MediaSearchResult msr = new MediaSearchResult(this.getProviderInfo().getId());
        msr.setIMDBId(i.MovieImdbID);
        msr.setTitle(i.MovieName);
        msr.setYear(i.MovieYear);
        if (i.MovieKind.equals("movie")) {
          msr.setMediaType(MediaType.MOVIE);
        }
        else {
          msr.setMediaType(MediaType.TV_EPISODE); // what... else...?
        }

        MediaMetadata md = new MediaMetadata(this.getProviderInfo().getId());
        md.storeMetadata(MediaMetadata.EPISODE_NR, i.SeriesEpisode);
        md.storeMetadata(MediaMetadata.SEASON_NR, i.SeriesSeason);
        md.storeMetadata(MediaMetadata.IMDBID, i.MovieImdbID);
        md.storeMetadata(MediaMetadata.TITLE, i.MovieName);
        md.storeMetadata(MediaMetadata.YEAR, i.MovieYear);
        md.storeMetadata(MediaMetadata.VOTE_COUNT, i.SeenCount); // well...
        msr.setMetadata(md);

        results.add(msr);
      }
    }
    catch (Exception e) {
      LOGGER.error("Could not identify " + mf, e);
    }

    return results;
  }

  /**
   * search for subtitle files matching your mediafile using video file hash
   * 
   * @param mf
   *          the mediafile
   * @return MediaSearchResult
   */
  @Override
  public List<MediaSearchResult> search(MediaFile mf) {
    LOGGER.debug("searching subtitle for " + mf);
    List<MediaSearchResult> results = new ArrayList<MediaSearchResult>();

    Map<String, Object> mapQuery = new HashMap<String, Object>();
    mapQuery.put("sublanguageid", Globals.settings.getLanguage());
    mapQuery.put("moviehash", SubtitleUtils.computeOpenSubtitlesHash(mf.getFile()));
    // when MI is not run yet, MF always 0 (b/c of locking) - so get this direct
    mapQuery.put("moviebytesize", mf.getFilesize() == 0 ? mf.getFile().length() : mf.getFilesize());

    try {
      XmlRpcStruct token = (XmlRpcStruct) methodCall("SearchSubtitles", mapQuery);
      System.out.println(token);
    }
    catch (Exception e) {
      LOGGER.error("Could not search subtitle.", e);
    }

    return results;
  }

  /**
   * This function should be always called when starting communication with OSDb server to identify user, specify application and start a new session
   * (either registered user or anonymous). If user has no account, blank username and password should be used.
   */
  private static synchronized void startSession() {
    if (session == null) {
      Object token = null;
      try {
        token = client.invoke("LogIn", new Object[] { "", "", Globals.settings.getLanguage(), USER_AGENT });
        XmlRpcStruct response = (XmlRpcStruct) token;
        session = new ApiStartSession(response);
        LOGGER.debug("Login OK");
      }
      catch (Exception e) {
        LOGGER.error("Could not start session!", e);
      }
    }
    else {
      // System.out.println("session still valid till " + session.getValid_till());
    }
  }

  /**
   * This will logout user identified by token token. This function should be called just before exiting/closing client application.
   */
  public void closeSession() {
    try {
      methodCall("LogOut", null);
      LOGGER.debug("Session closed");
    }
    catch (Exception e) {
      LOGGER.error("Cannot close session.");
    }
    session = null;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public void download(String hash, String language) {
    // TODO Auto-generated method stub

  }

  /**
   * Searches for a subtitle
   * 
   * @param filVideoFile
   *          the video file which to download subtitles
   */
  /*
   * public static void searchAndDownloadSubtitles(File filVideoFile) throws Exception {
   * 
   * // Let's log in try {
   * 
   * logIn();
   * 
   * } catch (Exception e) { throw e; }
   * 
   * // Let's generate the hash of the movie file. This is a custom hashing // algorithm used by OpenSubtitles. String strHash =
   * SubtitleUtils.computeOpenSubtitlesHash(filVideoFile);
   * 
   * // Let's search for matching subtitles URL urlSubtitle = null;
   * 
   * try {
   * 
   * LOGGER.info(String.format("Searching for subtiles: %s, %s", strHash, filVideoFile.length()));
   * 
   * XmlRpcClientConfigImpl rpcConfig = new XmlRpcClientConfigImpl(); rpcConfig.setServerURL(new URL(OSDB_SERVER)); XmlRpcClient rpcClient = new
   * XmlRpcClient(); rpcClient.setConfig(rpcConfig);
   * 
   * Map<String, Object> mapQuery = new HashMap<String, Object>(); mapQuery.put("sublanguageid", Locale.getDefault().getISO3Language());
   * mapQuery.put("moviehash", new String("18379ac9af039390")); mapQuery.put("moviebytesize", new Double(366876694));
   * 
   * Object[] objParams = new Object[] { strToken, new Object[] { mapQuery } }; HashMap<?, ?> x = (HashMap<?, ?>) rpcClient.execute("SearchSubtitles",
   * objParams); Object[] lstData = (Object[]) x.get("data"); HashMap<?, ?> mapResult = (HashMap<?, ?>) lstData[0];
   * 
   * urlSubtitle = new URL((String) mapResult.get("SubDownloadLink"));
   * 
   * LOGGER.debug("Done.");
   * 
   * } catch (Exception e) { LOGGER.warn(String.format("Error: %s", e.toString())); }
   * 
   * // Now that we have the URL, we can download the file. The file is in // the GZIP format so we have to uncompress it. File filSubtitleFile = new
   * File(filVideoFile.getPath().substring(0, filVideoFile.getPath().length() - 4));
   * 
   * HttpURLConnection objConnection = null; FileOutputStream objOutputStream = null; GZIPInputStream objGzipInputStream = null;
   * 
   * try {
   * 
   * objConnection = (HttpURLConnection) ((urlSubtitle).openConnection()); objOutputStream = new FileOutputStream(filSubtitleFile); objGzipInputStream
   * = new GZIPInputStream(objConnection.getInputStream());
   * 
   * LOGGER.info(String.format("Downloading the subtitle: %s", urlSubtitle));
   * 
   * if (objConnection.getResponseCode() != 200) { LOGGER.debug("The server did not respond properly"); }
   * 
   * Integer intLength = 0; byte[] bytBuffer = new byte[1024];
   * 
   * objOutputStream.close(); filSubtitleFile.delete(); if (objConnection.getHeaderField("Content-Disposition").isEmpty() == false) { filSubtitleFile
   * = new File(filSubtitleFile.getPath() + "." + FileExtention.getExtention(objConnection)); }
   * 
   * objOutputStream.close(); objOutputStream = new FileOutputStream(filSubtitleFile); while ((intLength = objGzipInputStream.read(bytBuffer)) > 0) {
   * objOutputStream.write(bytBuffer, 0, intLength); } objConnection.disconnect();
   * 
   * LOGGER.debug("Downloaded.");
   * 
   * } catch (Exception e) { LOGGER.warn(String.format("Error: %s", e.toString())); } finally { objOutputStream.close(); objGzipInputStream.close(); }
   * 
   * // Let's log out try {
   * 
   * logOut();
   * 
   * } catch (Exception e) { return; }
   * 
   * }
   */
}