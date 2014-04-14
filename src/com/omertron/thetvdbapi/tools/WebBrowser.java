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

package com.omertron.thetvdbapi.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class WebBrowser - For use with TheMovieDB API including support for HTTPClient and Caching.
 * 
 * @author Manuel Laggner
 */
public final class WebBrowser {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebBrowser.class);

  /**
   * Constructor for WebBrowser. Does instantiates the browser properties.
   */
  protected WebBrowser() {
    throw new UnsupportedOperationException("WebBrowser can not be instantiated!");
  }

  /**
   * Request the web page at the specified URL.
   * 
   * @param url
   *          the url
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws InterruptedException
   */
  public static String request(String url) throws IOException, InterruptedException {
    return request(new URL(url));
  }

  /**
   * Request the web page at the specified URL.
   * 
   * @param requestUrl
   *          the url
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws InterruptedException
   */
  public static String request(URL requestUrl) throws IOException, InterruptedException {
    StringBuilder content = new StringBuilder();

    InputStream is = null;
    BufferedReader in = null;
    InputStreamReader isr = null;
    GZIPInputStream zis = null;

    try {
      Url url = new CachedUrl(requestUrl.toString());
      is = url.getInputStream();

      // Check the content encoding of the connection. Null content encoding is standard HTTP
      if (url.getContentEncoding() == null) {
        isr = new InputStreamReader(is, url.getCharset());
      }
      else if (url.getContentEncoding().equalsIgnoreCase("gzip")) {
        zis = new GZIPInputStream(is);
        isr = new InputStreamReader(zis, "UTF-8");
      }
      else {
        LOGGER.warn("Unknown content encoding " + url.getContentEncoding() + ", aborting");
        return "";
      }

      in = new BufferedReader(isr);

      String line;
      while ((line = in.readLine()) != null) {
        content.append(line);
      }
    }
    finally {
      if (is != null) {
        is.close();
      }
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException ex) {
          LOGGER.debug("Failed to close BufferedReader: " + ex.getMessage());
        }
      }

      if (isr != null) {
        try {
          isr.close();
        }
        catch (IOException ex) {
          LOGGER.debug("Failed to close InputStreamReader: " + ex.getMessage());
        }
      }

      if (zis != null) {
        try {
          zis.close();
        }
        catch (IOException ex) {
          LOGGER.debug("Failed to close GZIPInputStream: " + ex.getMessage());
        }
      }
    }
    return content.toString();
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
  public static String getProxyPort() {
    return Globals.settings.getProxyPort();
  }

  /**
   * Sets the proxy port.
   * 
   * @param myProxyPort
   *          the new proxy port
   */
  public static void setProxyPort(String myProxyPort) {
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
