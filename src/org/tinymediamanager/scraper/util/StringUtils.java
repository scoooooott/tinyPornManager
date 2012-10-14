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

import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class StringUtils.
 */
public class StringUtils {

  /**
   * Removes the html.
   * 
   * @param html
   *          the html
   * @return the string
   */
  public static String removeHtml(String html) {
    if (html == null)
      return null;
    return html.replaceAll("<[^>]+>", "");
  }

  /**
   * Unquote.
   * 
   * @param str
   *          the str
   * @return the string
   */
  public static String unquote(String str) {
    if (str == null)
      return null;
    return str.replaceFirst("^\\\"(.*)\\\"$", "$1");
  }

  /**
   * Map to string.
   * 
   * @param map
   *          the map
   * @return the string
   */
  public static String mapToString(Map map) {
    if (map == null)
      return "null";
    if (map.size() == 0)
      return "empty";

    StringBuilder sb = new StringBuilder();
    for (Object o : map.entrySet()) {
      Map.Entry me = (Entry) o;
      sb.append(me.getKey()).append(": ").append(me.getValue()).append(",");
    }
    return sb.toString();
  }

  /**
   * Zero pad.
   * 
   * @param encodeString
   *          the encode string
   * @param padding
   *          the padding
   * @return the string
   */
  public static String zeroPad(String encodeString, int padding) {
    try {
      int v = Integer.parseInt(encodeString);
      String format = "%0" + padding + "d";
      return String.format(format, v);
    }
    catch (Exception e) {
      return encodeString;
    }
  }

}