/*
 * Copyright 2012 Manuel Laggner
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.log4j.Logger;

/**
 * The Class Url.
 */
public class Url {
  /** The log. */
  private static final Logger log = Logger.getLogger(Url.class);

  private static final CacheConfig cacheConfig = new CacheConfig();

  static {
    cacheConfig.setMaxCacheEntries(1000);
    cacheConfig.setMaxObjectSize(150000);
  }

  /** The url. */
  private String url = null;

  /** The moved url. */
  private String movedUrl = null;

  /** The Constant HTTP_USER_AGENT. */
  protected static final String HTTP_USER_AGENT = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.1) Gecko/2008072820 Firefox/9.0.1";

  /**
   * Instantiates a new url.
   * 
   * @param url
   *          the url
   */
  public Url(String url) {
    this.url = url;
  }

  /**
   * Gets the moved url.
   * 
   * @return the moved url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public URL getMovedUrl() throws IOException {
    return new URL(movedUrl);
  }

  /**
   * Gets the url.
   * 
   * @return the url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public URL getUrl() throws IOException {
    return new URL(url);
  }

  /**
   * Checks for moved.
   * 
   * @return true, if successful
   */
  public boolean hasMoved() {
    return movedUrl != null;
  }

  /**
   * Send cookies.
   * 
   * @param url
   *          the url
   * @param conn
   *          the conn
   * @param handler
   *          the handler
   */
  protected void sendCookies(URL url, URLConnection conn, CookieHandler handler) {
    if (handler != null) {
      Map<String, String> cookies = handler.getCookiesToSend(url.toExternalForm());
      if (cookies != null) {
        for (String key : cookies.keySet()) {
          log.debug("Sending Cookie: " + key + "=" + cookies.get(key) + " to " + url.toExternalForm());
          conn.setRequestProperty("Cookie", String.format("%s=%s", key, cookies.get(key)));
        }
      }
    }
  }

  /**
   * Gets the input stream.
   * 
   * @param handler
   *          the handler
   * @param followRedirects
   *          the follow redirects
   * @return the input stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public InputStream getInputStream(CookieHandler handler, boolean followRedirects) throws IOException {
    URL u = getUrl();

    URLConnection conn = u.openConnection();
    if (conn instanceof HttpURLConnection) {
      log.debug("Setting Follow Redirects: " + followRedirects);
      ((HttpURLConnection) conn).setInstanceFollowRedirects(followRedirects);
      conn.setRequestProperty("User-Agent", HTTP_USER_AGENT);
    }
    sendCookies(u, conn, handler);

    // get the stream
    InputStream is = conn.getInputStream();
    if (conn instanceof HttpURLConnection) {
      int rc = ((HttpURLConnection) conn).getResponseCode();
      if (rc == HttpURLConnection.HTTP_MOVED_PERM || rc == HttpURLConnection.HTTP_MOVED_TEMP) {
        movedUrl = conn.getHeaderField("Location");
        if (movedUrl != null) {
          int p = movedUrl.indexOf('?');
          if (p != -1) {
            movedUrl = movedUrl.substring(0, p);
          }
          log.debug("Found a Moved Url: " + u.toExternalForm() + "; Moved: " + movedUrl);
        }
      }
    }

    handleCookies(u, conn, handler);
    return is;
  }

  /**
   * Handle cookies.
   * 
   * @param u
   *          the u
   * @param conn
   *          the conn
   * @param handler
   *          the handler
   */
  protected void handleCookies(URL u, URLConnection conn, CookieHandler handler) {
    if (handler != null) {
      // process the response cookies
      String headerName = null;
      for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
        if (headerName.equals("Set-Cookie")) {
          String cookie = conn.getHeaderField(i);
          handler.handleSetCookie(u.toExternalForm(), cookie);
        }
      }
    }
  }

}
