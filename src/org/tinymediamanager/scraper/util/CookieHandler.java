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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * The Class CookieHandler.
 */
public class CookieHandler {

  /** The Constant log. */
  private static final Logger log = Logger.getLogger(CookieHandler.class);

  /** The cookies. */
  private Map<String, String> cookies = new HashMap<String, String>();

  /** The cookie url. */
  private String cookieUrl = null;

  /** The looking for cookies. */
  private boolean lookingForCookies = true;

  /**
   * Instantiates a new cookie handler.
   * 
   * @param cookieUrl
   *          the cookie url
   */
  public CookieHandler(String cookieUrl) {
    this.cookieUrl = cookieUrl;
  }

  /**
   * Gets the cookies to send.
   * 
   * @param url
   *          the url
   * @return the cookies to send
   */
  public Map<String, String> getCookiesToSend(String url) {
    log.debug("getCookies called on url: " + url);
    if (cookies.size() == 0 && lookingForCookies) {
      log.debug("We don't have a cookie, so we'll try and get them from: " + cookieUrl);
      // this happens when we are fetching a document from a result that
      // is prev cached.
      // we need to connect to the site url, grab the cookie, and then
      // we'll be ok.

      lookingForCookies = false;
      Url u = new Url(cookieUrl);
      try {
        // this should call us, with the main cookie fetching url, so
        // that we get populated.
        u.getInputStream(this, false);
      } catch (Exception e) {
        // don't care
      }
    }
    return cookies;
  }

  /**
   * Handle set cookie.
   * 
   * @param url
   *          the url
   * @param cookie
   *          the cookie
   */
  public void handleSetCookie(String url, String cookie) {
    log.debug(String.format("Handlin Cookies: Url: %s; Cookie: %s\n", url, cookie));
    Pattern p = Pattern.compile("([^ =:]+)=([^;]+)");
    Matcher m = p.matcher(cookie);
    // Goup[0]: [ASP.NET_SessionId=v411dwiwwnb04ifq24avpeet]
    // Goup[1]: [ASP.NET_SessionId]
    // Goup[2]: [v411dwiwwnb04ifq24avpeet]
    if (m.find()) {
      cookies.put(m.group(1), m.group(2));
    }
  }

}
