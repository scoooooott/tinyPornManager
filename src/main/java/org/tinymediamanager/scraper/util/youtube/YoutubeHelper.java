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
package org.tinymediamanager.scraper.util.youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Helper Class for getting the right Type of JSON return
 * 
 * @author Wolfgang Janes
 */
public class YoutubeHelper {
  private YoutubeHelper() {
    // hide constructors of helper classes
  }

  public static Integer getInt(JsonNode node, String name) {
    if (node.get(name) != null && node.get(name).isInt()) {
      return node.get(name).asInt();
    }
    else {
      return null;
    }
  }

  public static String getString(JsonNode node, String name) {
    if (node.get(name) != null && node.get(name).isTextual()) {
      return node.get(name).asText();
    }
    else {
      return null;
    }
  }

  /**
   * extracts the youtube id from the given url
   *
   * @param url
   *          to url to extract the youtube id
   * @return the youtube id (or an empty string if nothing found)
   */
  public static String extractId(String url) {

    Pattern pattern = Pattern.compile("youtube.com/watch?.*v=([^&]*)");
    Matcher matcher = pattern.matcher(url);
    if (matcher.find()) {
      return matcher.group(1);
    }

    pattern = Pattern.compile("youtube.com/v/([^&]*)");
    matcher = pattern.matcher(url);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return "";
  }
}
