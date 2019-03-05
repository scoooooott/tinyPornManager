/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;

/**
 * The class MetadataUtil. Here are some helper utils for managing meta data
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class MetadataUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataUtil.class);

  private MetadataUtil() {
    // hide the public constructor for utility classes
  }

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

    float score = Math.max(score1, Math.max(score3, score2));
    LOGGER.debug(String.format("Similarity Score: [%s][%s]=[%s]", searchTitle, matchTitle, score));

    return score;
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
    if (in == null || regex == null) {
      return null;
    }
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
   * For the purposes of searching it will, keep only alpha numeric characters and '&.
   * 
   * @param s
   *          the s
   * @return the string
   */
  public static String removeNonSearchCharacters(String s) {
    if (s == null) {
      return null;
    }
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
    if (StringUtils.isBlank(imdbId)) {
      return false;
    }

    return imdbId.matches("tt\\d{6,7}");
  }

  /**
   * parse a String for its integer value<br />
   * this method can parse normal integer values (e.g. 2001) as well as the style with digit separators (e.g. 2.001 or 2,001)
   *
   * @param intAsString
   *          the String to be parsed
   * @return the integer
   * @throws NumberFormatException
   *           an exception if none of the parsing methods worked
   */
  public static int parseInt(String intAsString) throws NumberFormatException {
    // first try to parse that with the interal parsing logic
    try {
      return Integer.parseInt(intAsString);
    }
    catch (NumberFormatException e) {
      // did not work; try to remove digit separators
      // since we do not know for which locale the separators has been written, remove . and ,
      return Integer.parseInt(intAsString.replaceAll("[,\\.]*", ""));
    }
  }

  /**
   * gets the imdb id via tmdb id
   *
   * @param tmdbId
   *          the tmdb id
   * @return the imdb id or an empty String
   */
  public static String getImdbIdViaTmdbId(int tmdbId) {
    if (tmdbId == 0) {
      return "";
    }

    try {
      // call the tmdb metadata provider
      IMovieMetadataProvider tmdb = null;
      for (IMovieMetadataProvider provider : PluginManager.getInstance().getPluginsForInterface(IMovieMetadataProvider.class)) {
        if (MediaMetadata.TMDB.equals(provider.getProviderInfo().getId())) {
          tmdb = provider;
          break;
        }
      }
      if (tmdb == null) {
        return "";
      }

      // we just need to "scrape" this movie
      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setId(MediaMetadata.TMDB, Integer.toString(tmdbId));
      MediaMetadata md = tmdb.getMetadata(options);
      return md.getId(MediaMetadata.IMDB).toString();
    }
    catch (Exception ingored) {
      // nothing to be done here
    }

    return "";
  }
}
