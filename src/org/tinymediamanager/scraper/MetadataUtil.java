/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.scraper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.Similarity;

/**
 * The Class MetadataUtil.
 * 
 * @author Manuel Laggner
 */
public class MetadataUtil {

  /** The Constant log. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataUtil.class);

  /**
   * Return the best score for a title when compared to the search string. It uses 2 passes to find the best match. the first pass uses the matchTitle
   * as is, and the second pass uses the matchTitle will non search characters removed.
   * 
   * @param searchTitle
   *          the search title
   * @param matchTitle
   *          the match title
   * @return the best out of the 2 scored attempts
   */
  public static float calculateScore(String searchTitle, String matchTitle) {
    float score1 = Similarity.compareStrings(searchTitle, matchTitle);
    float score2 = Similarity.compareStrings(searchTitle, removeNonSearchCharacters(matchTitle));
    float score3 = 0;
    if (searchTitle != null && searchTitle.matches(".* \\d{4}$")) { // ends with space+year
      score3 = Similarity.compareStrings(searchTitle.replaceFirst(" \\d{4}$", ""), matchTitle);
    }
    return Math.max(score1, Math.max(score3, score2));
  }

  /**
   * Parses the running time.
   * 
   * @param in
   *          the in
   * @param regex
   *          the regex
   * @return the string
   */
  public static String parseRunningTime(String in, String regex) {
    if (in == null || regex == null)
      return null;
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(in);
    if (m.find()) {
      return m.group(1);
    }
    else {
      LOGGER.warn("Could not find Running Time in " + in + "; using Regex: " + regex);
      return null;
    }
  }

  /**
   * Copy search query to search result.
   * 
   * @param query
   *          the query
   * @param sr
   *          the sr
   */
  public static void copySearchQueryToSearchResult(MediaSearchOptions query, MediaSearchResult sr) {
    for (MediaSearchOptions.SearchParam f : MediaSearchOptions.SearchParam.values()) {
      if (f == MediaSearchOptions.SearchParam.QUERY)
        continue;
      String s = query.get(f);
      if (!StringUtils.isEmpty(s)) {
        sr.getExtra().put(f.name(), s);
      }
    }
  }

  /**
   * For the purposes of searching it will, keep only alpha numeric characters and '&.
   * 
   * @param s
   *          the s
   * @return the string
   */
  public static String removeNonSearchCharacters(String s) {
    if (s == null)
      return null;
    // return (s.replaceAll("[^A-Za-z0-9&']", " ")).replaceAll("[\\ ]+", " ");
    // return s.replaceAll("[\\\\[\\\\]-–_.:|]", " ");
    return s.replaceAll("[\\\\[\\\\]_.:|]", " ");
  }

  /**
   * Checks if is valid imdb id.
   * 
   * @param imdbId
   *          the imdb id
   * @return true, if is valid imdb id
   */
  public static boolean isValidImdbId(String imdbId) {
    if (StringUtils.isEmpty(imdbId)) {
      return false;
    }

    return imdbId.matches("tt\\d{7}");
  }

}
