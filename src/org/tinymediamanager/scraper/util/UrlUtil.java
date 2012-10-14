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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

/**
 * The Class UrlUtil.
 */
public class UrlUtil {

  /** The Constant log. */
  private static final Logger LOGGER = Logger.getLogger(UrlUtil.class);

  /**
   * Returns the the entire Url Path except the filename, like doing a basedir
   * on a filename.
   * 
   * @param url
   *          the url
   * @return the base url
   */
  public static String getBaseUrl(String url) {
    String path = getPathName(url);
    if (path != null && path.contains("/")) {
      path = path.substring(0, path.lastIndexOf("/"));
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
   * Encode.
   * 
   * @param data
   *          the data
   * @return the string
   */
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

}
