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

package com.omertron.themoviedbapi.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.Url;

import com.omertron.themoviedbapi.MovieDbException;

/**
 * The Class WebBrowser - For use with TheMovieDB API including support for HTTPClient.
 * 
 * @author Manuel Laggner
 */
public final class WebBrowser {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebBrowser.class);

  // Hide the constructor
  /**
   * Instantiates a new web browser.
   */
  protected WebBrowser() {
    // prevents calls from subclass
    throw new UnsupportedOperationException();
  }

  /**
   * Request.
   * 
   * @param url
   *          the url
   * @return the string
   * @throws MovieDbException
   *           the movie db exception
   */
  public static String request(String url) throws MovieDbException {
    try {
      return request(new URL(url));
    }
    catch (MalformedURLException ex) {
      throw new MovieDbException(MovieDbException.MovieDbExceptionType.INVALID_URL, null, ex);
    }
  }

  public static String request(URL url) throws MovieDbException {
    return request(url, null, Boolean.FALSE);
  }

  public static String request(URL url, String jsonBody) throws MovieDbException {
    return request(url, jsonBody, Boolean.FALSE);
  }

  /**
   * Request.
   * 
   * @param requestUrl
   *          the request url
   * @return the string
   * @throws MovieDbException
   *           the movie db exception
   */
  public static String request(URL requestUrl, String jsonBody, boolean isDeleteRequest) throws MovieDbException {
    StringWriter content = null;
    BufferedReader in = null;

    try {
      content = new StringWriter();

      Url url = null;
      url = new CachedUrl(requestUrl.toString());
      url.addHeader("Accept", "application/json");
      url.addHeader("Content-Type", "application/json");

      in = new BufferedReader(new InputStreamReader(url.getInputStream(), url.getCharset()));
      String line;
      while ((line = in.readLine()) != null) {
        content.write(line);
      }

      return content.toString();
    }
    catch (InterruptedException ex) {
      throw new MovieDbException(MovieDbException.MovieDbExceptionType.CONNECTION_ERROR, null, ex);
    }
    catch (IOException ex) {
      throw new MovieDbException(MovieDbException.MovieDbExceptionType.CONNECTION_ERROR, null, ex);
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException ex) {
          LOGGER.debug("Failed to close BufferedReader: " + ex.getMessage());
        }
      }
      if (content != null) {
        try {
          content.close();
        }
        catch (IOException ex) {
          LOGGER.debug("Failed to close connection: " + ex.getMessage());
        }
      }
    }
  }

  /**
   * Gets the proxy host.
   * 
   * @return the proxy host
   */
  public static String getProxyHost() {
    return Globals.settings.getProxyHost();
  }

  /**
   * Sets the proxy host.
   * 
   * @param myProxyHost
   *          the new proxy host
   */
  public static void setProxyHost(String myProxyHost) {
  }

  /**
   * Gets the proxy port.
   * 
   * @return the proxy port
   */
  public static int getProxyPort() {
    return Integer.parseInt(Globals.settings.getProxyPort());
  }

  /**
   * Sets the proxy port.
   * 
   * @param myProxyPort
   *          the new proxy port
   */
  public static void setProxyPort(int myProxyPort) {
  }

  /**
   * Gets the proxy username.
   * 
   * @return the proxy username
   */
  public static String getProxyUsername() {
    return Globals.settings.getProxyUsername();
  }

  /**
   * Sets the proxy username.
   * 
   * @param myProxyUsername
   *          the new proxy username
   */
  public static void setProxyUsername(String myProxyUsername) {
  }

  /**
   * Gets the proxy password.
   * 
   * @return the proxy password
   */
  public static String getProxyPassword() {
    return Globals.settings.getProxyPassword();
  }

  /**
   * Sets the proxy password.
   * 
   * @param myProxyPassword
   *          the new proxy password
   */
  public static void setProxyPassword(String myProxyPassword) {
  }

  /**
   * Gets the web timeout connect.
   * 
   * @return the web timeout connect
   */
  public static int getWebTimeoutConnect() {
    return 0;
  }

  /**
   * Gets the web timeout read.
   * 
   * @return the web timeout read
   */
  public static int getWebTimeoutRead() {
    return 0;
  }

  /**
   * Sets the web timeout connect.
   * 
   * @param webTimeoutConnect
   *          the new web timeout connect
   */
  public static void setWebTimeoutConnect(int webTimeoutConnect) {
  }

  /**
   * Sets the web timeout read.
   * 
   * @param webTimeoutRead
   *          the new web timeout read
   */
  public static void setWebTimeoutRead(int webTimeoutRead) {
  }
}
