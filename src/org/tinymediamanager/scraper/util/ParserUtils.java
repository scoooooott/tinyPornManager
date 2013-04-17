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
package org.tinymediamanager.scraper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ParserUtils.
 * 
 * @author Manuel Laggner
 */
public class ParserUtils {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ParserUtils.class);

  /**
   * Tries to get movie name from filename<br>
   * 1. splits string using common delimiters ".- ()"<br>
   * 2. searches for first occurrence of common stopwords<br>
   * 3. if last token is 4 digits, assume year and remove (only if we have at least 3 tokens)<br>
   * 4. everything before the first stopword must be the movie name :p
   * 
   * @param filename
   *          the filename to get the title from
   * @return the (hopefully) correct parsed movie name
   * @author Myron Boyle
   */
  public static String detectCleanMoviename(String filename) {
    LOGGER.debug("Parse filename for movie title: \"" + filename + "\"");

    String[] stopwords = { "ac3", "dts", "custom", "docu", "dc", "dtv", "divx", "divx5", "dsr", "dsrip", "dutch", "dvd", "dvdrip", "dvdscr",
        "dvdscreener", "screener", "dvdivx", "cam", "fragment", "fs", "german", "hdtv", "hdrip", "hdtvrip", "internal", "limited", "multisubs",
        "ntsc", "ogg", "ogm", "pal", "pdtv", "proper", "repack", "rerip", "retail", "r3", "r5", "bd5", "se", "svcd", "swedish", "german", "read.nfo",
        "nfofix", "unrated", "ws", "telesync", "ts", "telecine", "tc", "brrip", "bdrip", "480p", "480i", "576p", "576i", "720p", "720i", "1080p",
        "1080i", "hrhd", "hrhdtv", "hddvd", "bluray", "blueray", "x264", "h264", "xvid", "xvidvd", "xxx", "www.www", "cd1", "cd2", "cd3", "cd4",
        "cd5", "cd6", "cd7", "cd8", "cd9", "dvd1", "dvd2", "dvd3", "dvd4", "dvd5", "dvd6", "dvd7", "dvd8", "dvd9", "disc1", "disc2", "disc3",
        "disc4", "disc5", "disc6", "disc7", "disc8", "disc9" };

    if (filename == null || filename.isEmpty()) {
      LOGGER.warn("Filename empty?!");
      return "";
    }

    // remove extension (if found) and split
    String fname = filename.replaceFirst("\\.\\w{2,4}$", "");
    String[] s = fname.split("[()_ -.]");
    int firstFoundStopwordPosition = s.length;

    // iterate over all splitted items
    for (int i = 0; i < s.length; i++) {
      // search for stopword position
      if (s[i] != null && !s[i].isEmpty()) {
        for (String stop : stopwords) {
          if (s[i].equalsIgnoreCase(stop)) {
            // remember lowest position
            if (i < firstFoundStopwordPosition) {
              firstFoundStopwordPosition = i;
            }
          }
        }
      }
    }

    // if we have at least 3 tokens, and the last one is a 4 digit, assume year
    // and remove
    if (firstFoundStopwordPosition > 3 && s[firstFoundStopwordPosition - 1].matches("\\d{4}")) {
      LOGGER.debug("removed last token - seems to be year");
      firstFoundStopwordPosition--;
    }

    // rebuild string
    String ret = "";
    for (int i = 0; i < firstFoundStopwordPosition; i++) {
      if (!s[i].isEmpty()) {
        ret = ret + s[i] + " ";
      }
    }
    LOGGER.debug("Movie title should be: \"" + ret.trim() + "\"");
    return ret.trim();
  }

  /**
   * return a 2 element array. 0 = title; 1=date
   * 
   * parses the title in the format Title YEAR or Title (YEAR)
   * 
   * @param title
   *          the title
   * @return the string[]
   */
  public static String[] parseTitle(String title) {
    String v[] = { "", "" };
    if (title == null)
      return v;

    Pattern p = Pattern.compile("(.*)\\s+\\(?([0-9]{4})\\)?", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(title);
    if (m.find()) {
      v[0] = m.group(1);
      v[1] = m.group(2);
    }
    else {
      v[0] = title;
    }
    return v;
  }

  /**
   * Parses titles if they are in the form Title (Year). The first element is the title, and the second element is the date, both can be null. If the
   * matcher fails to find the pattern, then the passed in title is set as the first element, which is the title.
   * 
   * @param title
   *          the title
   * @return the pair
   */
  public static Pair<String, String> parseTitleAndDateInBrackets(String title) {
    if (title == null)
      return new Pair<String, String>(null, null);

    Pattern p = Pattern.compile("(.*)\\s+\\(?([0-9]{4})\\)?", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(title);
    if (m.find()) {
      return new Pair<String, String>(m.group(1), m.group(2));
    }

    return new Pair<String, String>(title, null);
  }
}
