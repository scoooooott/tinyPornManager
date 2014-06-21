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
package org.tinymediamanager.scraper.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.scraper.MediaLanguages;

import com.sun.jna.Platform;

/**
 * The Class UrlUtil.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class UrlUtil {

  /** The Constant log. */
  private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtil.class);

  /**
   * Casts url string to URI, and does the correct encoding (rfc2396) of query string ONLY (eg "|" character). URLEncoder encodes everything which
   * might break commons.http
   * 
   * @param url
   *          the url as string
   * @return URI object
   * @throws URISyntaxException
   *           if url could not be parsed / invalid
   */
  public static URI getURIEncoded(String url) throws URISyntaxException {
    String[] trArr = url.split("://");
    return new URI(trArr[0], "//" + trArr[1], null);
  }

  /**
   * Returns file extension from url.
   * 
   * @param url
   *          the url
   * @return file extension or empty string
   * @throws URISyntaxException
   *           if url is not valid
   */
  public static String getFileExtension(String url) throws URISyntaxException {
    String ext = getURIEncoded(url).getPath();
    if (ext == null || ext.isEmpty() || !ext.contains(".")) {
      LOGGER.warn("Url " + url + " has no extension!");
      return "";
    }
    else {
      ext = getFileNameArray(url)[1];
      return ext;
    }
  }

  /**
   * gets the BaseName (w/o extension) of an URL (better than commons-io)
   * 
   * @param url
   * @return BaseName
   * @throws URISyntaxException
   */
  public static String getBasename(String url) {
    return getFileNameArray(url)[0];
  }

  /**
   * gets the Extension of an URL (better than commons-io)
   * 
   * @param url
   * @return BaseName
   * @throws URISyntaxException
   */
  public static String getExtension(String url) {
    return getFileNameArray(url)[1];
  }

  /**
   * gets the FileName (with extension) of an URL (better than commons-io)
   * 
   * @param url
   * @return BaseName
   * @throws URISyntaxException
   */
  public static String getFilename(String url) {
    return getFileNameArray(url)[2];
  }

  /**
   * Returns the the entire Url Path except the filename, like doing a basedir on a filename.
   * 
   * @param url
   *          the url
   * @return the base url
   */
  public static String getBaseUrl(String url) {
    String path = getPathName(url);
    if (path != null && path.contains("/")) {
      path = path.substring(0, path.lastIndexOf('/'));
    }
    return getDomainUrl(url) + path;
  }

  /**
   * Gets the domain url.
   * 
   * @param url
   *          the url
   * @return the domain url
   */
  public static String getDomainUrl(String url) {
    URL u;
    try {
      u = new URL(url);
      return String.format("%s://%s/", u.getProtocol(), u.getHost());
    }
    catch (MalformedURLException e) {
      LOGGER.error("Failed to get domain url for: " + url);
    }
    return null;
  }

  /**
   * Join url path.
   * 
   * @param baseUrl
   *          the base url
   * @param path
   *          the path
   * @return the string
   */
  public static String joinUrlPath(String baseUrl, String path) {
    StringBuffer sb = new StringBuffer(baseUrl);
    if (baseUrl.endsWith("/") && path.startsWith("/")) {
      path = path.substring(1);
    }
    sb.append(path);

    return sb.toString();
  }

  /**
   * Gets the path name.
   * 
   * @param url
   *          the url
   * @return the path name
   */
  public static String getPathName(String url) {
    URL u;
    try {
      u = new URL(url);
      return u.getPath();
    }
    catch (MalformedURLException e) {
      LOGGER.error("getPathName() Failed! " + url, e);
    }
    return null;
  }

  /**
   * get the correct name/extension/filename of url (even with parameters! - commons-io CANNOT)
   * 
   * @param url
   *          the url
   * @return basename/ext/filename array
   */
  public static String[] getFileNameArray(String url) {
    String[] ret = new String[] { "", "", "" };

    // URL: "http://photosaaaaa.net/photos-ak-snc1/v315/224/13/659629384/s659629384_752969_4472.jpg?asdf=jklo"
    String filename = "";
    String path = "";
    // PATH: /photos-ak-snc1/v315/224/13/659629384/s659629384_752969_4472.jpg?asdf=jklo
    try {
      url = getURIEncoded(url).toString();
      path = new URL(url).getPath();
    }
    catch (Exception e) {
      return ret;
    }
    // Checks for both forward and/or backslash
    // NOTE:**While backslashes are not supported in URL's
    // most browsers will autoreplace them with forward slashes
    // So technically if you're parsing an html page you could run into
    // a backslash , so i'm accounting for them here;
    String[] pathContents = path.split("[\\\\/]");
    if (pathContents != null) {
      int pathContentsLength = pathContents.length;
      // System.out.println("Path Contents Length: " + pathContentsLength);
      // for (int i = 0; i < pathContents.length; i++) {
      // System.out.println("Path " + i + ": " + pathContents[i]);
      // }
      // // lastPart: s659629384_752969_4472.jpg
      String lastPart = pathContents[pathContentsLength - 1];
      String[] lastPartContents = lastPart.split("\\.");
      if (lastPartContents != null && lastPartContents.length > 1) {
        int lastPartContentLength = lastPartContents.length;
        // System.out.println("Last Part Length: " + lastPartContentLength);
        // filenames can contain . , so we assume everything before
        // the last . is the name, everything after the last . is the
        // extension
        String name = "";
        for (int i = 0; i < lastPartContentLength; i++) {
          // System.out.println("Last Part " + i + ": " + lastPartContents[i]);
          if (i < (lastPartContents.length - 1)) {
            name += lastPartContents[i];
            if (i < (lastPartContentLength - 2)) {
              name += ".";
            }
          }
        }
        String extension = lastPartContents[lastPartContentLength - 1];
        filename = name + "." + extension;
        // System.out.println("Name: " + name);
        // System.out.println("Extension: " + extension);
        // System.out.println("Filename: " + filename);
        ret = new String[] { name, extension, filename };
      }
      else {
        // no extension (eg youtube)
        String name = lastPartContents[0];
        ret = new String[] { name, "", name };
      }
    }
    return ret;
  }

  /**
   * Encode.
   * 
   * @param data
   *          the data
   * @return the string
   */
  @SuppressWarnings("deprecation")
  public static String encode(String data) {
    if (data == null)
      return "";
    try {
      return URLEncoder.encode(data, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      LOGGER.warn("Failed to url encode data: " + data + " as UTF-8; will try again using default encoding", e);
      return URLEncoder.encode(data);
    }
  }

  /**
   * generates a valid user-agent <br>
   * something like:<br>
   * Mozilla/5.0 (Windows; Windows NT 6.1; Windows 7 6.1; U; amd64; de-DE; rv:26.0) Gecko/20100101 Firefox/26.0<br>
   * but with correct OS and language values
   */
  public static String generateUA() {
    // this is due to the fact, that the OS is not correctly recognized (eg Mobile FirefoxOS, where it isn't)
    String hardcodeOS = "";
    if (Platform.isWindows()) {
      hardcodeOS = "Windows; Windows NT " + System.getProperty("os.version");
    }
    else if (Platform.isMac()) {
      hardcodeOS = "Macintosh";
    }
    else if (Platform.isLinux()) {
      hardcodeOS = "X11";
    }
    else {
      hardcodeOS = System.getProperty("os.name");
    }

    // set header according to movie scraper language (or default GUI language as fallback)
    Locale l = null;
    MediaLanguages ml = MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage();

    if (ml == null) {
      ml = Globals.settings.getTvShowSettings().getScraperLanguage();
    }
    if (ml != null) {
      l = Utils.getLocaleFromLanguage(ml.name());
    }
    else {
      l = Utils.getLocaleFromLanguage(Locale.getDefault().getLanguage());
    }

    // @formatter:off
    String ua = String.format("Mozilla/5.0 (%1$s; %2$s %3$s; U; %4$s; %5$s-%6$s; rv:26.0) Gecko/20100101 Firefox/26.0", 
        hardcodeOS,
        System.getProperty("os.name", ""),
        System.getProperty("os.version", ""),
        System.getProperty("os.arch", ""),
        l.getLanguage(),
        l.getCountry());
    // @formatter:on
    return ua;
  }

}
