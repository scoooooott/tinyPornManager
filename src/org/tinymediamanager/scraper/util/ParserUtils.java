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
import org.tinymediamanager.core.Utils;

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

    String[] stopwords = { "1080", "1080i", "1080p", "480i", "480p", "576i", "576p", "720", "720i", "720p", "ac3", "ac3ld", "ac3md", "aoe", "bd5",
        "bdrip", "bdrip", "blueray", "bluray", "brrip", "cam", "cd1", "cd2", "cd3", "cd4", "cd5", "cd6", "cd7", "cd8", "cd9", "custom", "dc",
        "disc1", "disc2", "disc3", "disc4", "disc5", "disc6", "disc7", "disc8", "disc9", "divx", "divx5", "dl", "docu", "dsr", "dsrip", "dts", "dtv",
        "dubbed", "dutch", "dvd", "dvd1", "dvd2", "dvd3", "dvd4", "dvd5", "dvd6", "dvd7", "dvd8", "dvd9", "dvdivx", "dvdrip", "dvdscr",
        "dvdscreener", "emule", "etm", "extended", "fragment", "fs", "german", "h264", "hddvd", "hdrip", "hdtv", "hdtvrip", "hrhd", "hrhdtv", "ind",
        "internal", "ld", "limited", "md", "multisubs", "nfo", "nfofix", "ntg", "ntsc", "ogg", "ogm", "pal", "pdtv", "proper", "pso", "r3", "r5",
        "read", "repack", "rerip", "retail", "roor", "rs", "screener", "se", "subbed", "svcd", "swedish", "tc", "telecine", "telesync", "ts",
        "uncut", "unrated", "vcf", "webdl", "webrip", "workprint", "ws", "www", "x264", "xf", "xvid", "xvidvd", "xxx" };

    if (filename == null || filename.isEmpty()) {
      LOGGER.warn("Filename empty?!");
      return "";
    }

    // remove extension (if found) and split
    String fname = filename.replaceFirst("\\.\\w{2,4}$", "");
    String[] s = fname.split("[\\[\\]() _.-]");
    int firstFoundStopwordPosition = s.length;

    // iterate over all splitted items
    for (int i = 0; i < s.length; i++) {
      // search for stopword position
      if (s[i] != null && !s[i].isEmpty()) {
        for (String stop : stopwords) {
          if (s[i].equalsIgnoreCase(stop)) {
            s[i] = ""; // delete stopword
            // remember lowest position, but not lower than 2!!!
            if (i < firstFoundStopwordPosition && i >= 2) {
              firstFoundStopwordPosition = i;
            }
          }
        }
        if (Utils.isValidImdbId(s[i])) {
          s[i] = ""; // delete imdbId from name
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
   * removes some weird number-stopwords like 1080, 720 etc.. to ease the regex parsing for season/episode
   * 
   * @param filename
   * @return the cleaned one
   */
  public static String removeStopwordsFromTvEpisodeName(String filename) {
    String[] stopwords = { "720p", "1080", "x264", "h264", "webdl", "hdtv", "dubbed", "xvid", "bdrip", "webrip", "hdrip" };
    for (String s : stopwords) {
      filename = filename.replaceAll(s, "");
    }
    return filename;
  }

  /**
   * returns cleaned tvepisode name
   * 
   * @param filename
   * @return the cleaned one
   */
  public static String cleanTvEpisodeName(String filename) {
    String[] stopwords = { "720p", "1080", "x264", "h264", "webdl", "hdtv", "dubbed", "xvid", "bdrip", "webrip", "hdrip" };

    if (filename == null || filename.isEmpty()) {
      LOGGER.warn("Filename empty?!");
      return "";
    }

    // remove extension (if found) and split
    String fname = filename.replaceFirst("\\.\\w{2,4}$", "");
    String[] s = fname.split("[()\\[\\] _.-]");

    // iterate over all splitted items and remove stopwords
    String ret = "";
    for (int i = 0; i < s.length; i++) {
      if (s[i] != null && !s[i].isEmpty()) {
        boolean stopword_found = false;
        for (String stop : stopwords) {
          if (s[i].toLowerCase().startsWith(stop)) {
            // we found a stopword - ignore
            stopword_found = true;
          }
        }
        if (!stopword_found) {
          ret = ret + s[i] + " ";
        }
      }
    }
    return ret.trim();
  }

  /**
   * returns the MediaSource if found in file name
   * 
   * @param filename
   *          the filename
   * @return Bluray Disc | HDDVD | TV | DVD | VHS
   */
  public static String getMediaSource(String filename) {
    String ms = "";
    String fn = filename.toLowerCase();
    // http://wiki.xbmc.org/index.php?title=Media_flags#Media_source
    if (fn.contains("bluray") || fn.contains("blueray") || fn.contains("bdrip") || fn.contains("bd25") || fn.contains("bd50")) {
      ms = "Bluray"; // yes!
    }
    else if (fn.contains("hddvd")) {
      ms = "HDDVD";
    }
    else if (fn.contains("dvd")) {
      ms = "DVD";
    }
    else if (fn.contains("hdtv") || fn.contains("pdtv") || fn.contains("dsr") || fn.contains("dtv")) {
      ms = "TV";
    }
    else if (fn.contains("vhs")) {
      ms = "VHS";
    }
    return ms;
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
