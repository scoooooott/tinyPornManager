/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.w3c.tidy.Tidy;

/**
 * The Class ParserUtils.
 * 
 * @author Manuel Laggner
 */
public class ParserUtils {

  /** The Constant LOGGER. */
  private static final Logger LOGGER    = LoggerFactory.getLogger(ParserUtils.class);

  public static String[]      stopwords = { "1080", "1080i", "1080p", "480i", "480p", "576i", "576p", "720", "720i", "720p", "ac3", "ac3ld", "ac3md",
      "aoe", "bd5", "bdrip", "bdrip", "blueray", "bluray", "brrip", "cam", "cd1", "cd2", "cd3", "cd4", "cd5", "cd6", "cd7", "cd8", "cd9", "complete",
      "custom", "dc", "disc1", "disc2", "disc3", "disc4", "disc5", "disc6", "disc7", "disc8", "disc9", "divx", "divx5", "dl", "docu", "dsr", "dsrip",
      "dts", "dtv", "dubbed", "dutch", "dvd", "dvd1", "dvd2", "dvd3", "dvd4", "dvd5", "dvd6", "dvd7", "dvd8", "dvd9", "dvdivx", "dvdrip", "dvdscr",
      "dvdscreener", "emule", "etm", "extended", "fragment", "fs", "german", "h264", "hddvd", "hdrip", "hdtv", "hdtvrip", "hrhd", "hrhdtv", "ind",
      "internal", "ld", "limited", "md", "multisubs", "nfo", "nfofix", "ntg", "ntsc", "ogg", "ogm", "pal", "pdtv", "proper", "pso", "r3", "r5",
      "read", "repack", "rerip", "retail", "roor", "rs", "rsvcd", "screener", "se", "subbed", "svcd", "swedish", "tc", "telecine", "telesync", "ts",
      "uncut", "unrated", "vcf", "webdl", "webrip", "workprint", "ws", "www", "x264", "xf", "xvid", "xvidvd", "xxx" };

  /**
   * Tries to get movie name from filename<br>
   * 1. splits string using common delimiters ".- ()"<br>
   * 2. searches for first occurrence of common stopwords<br>
   * 3. if last token is 4 digits, assume year and remove<br>
   * 4. everything before the first stopword must be the movie name :p<br>
   * <br>
   * Deprecated in favor of detectCleanMovienameAndYear (avoid possible dupes)
   * 
   * @param filename
   *          the filename to get the title from
   * @return the (hopefully) correct parsed movie name
   */
  @Deprecated
  public static String detectCleanMoviename(String filename) {
    return detectCleanMovienameAndYear(filename)[0];
  }

  /**
   * Tries to get movie name and year from filename<br>
   * 1. splits string using common delimiters ".- ()"<br>
   * 2. searches for first occurrence of common stopwords<br>
   * 3. if last token is 4 digits, assume year and set [1]<br>
   * 4. everything before the first stopword must be the movie name :p
   * 
   * @param filename
   *          the filename to get the title from
   * @return title/year string (year can be empty)
   */
  public static String[] detectCleanMovienameAndYear(String filename) {
    String[] ret = { "", "" };
    // use trace to not remove logging completely (function called way to often on multi movie dir parsing)
    LOGGER.trace("Parse filename for movie title: \"" + filename + "\"");

    if (filename == null || filename.isEmpty()) {
      LOGGER.warn("Filename empty?!");
      return ret;
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

    // scan backwards - if we have at least 1 token, and the last one is a 4 digit, assume year and remove
    String year = "";
    for (int i = s.length - 1; i > 0; i--) {
      if (!s[i].isEmpty() && s[i].matches("\\d{4}")) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int parsedYear = Integer.parseInt(s[i]);
        if (parsedYear > 1800 && parsedYear < currentYear + 5) {
          // well, limit the year a bit...
          LOGGER.trace("removed token '" + s[i] + "'- seems to be year");
          year = s[i];
          s[i] = "";
          break;
        }
      }
    }

    // rebuild string, respecting bad words
    String name = "";
    for (int i = 0; i < firstFoundStopwordPosition; i++) {
      if (!s[i].isEmpty()) {
        // check for bad words
        if (!MovieModuleManager.MOVIE_SETTINGS.getBadWords().contains(s[i].toLowerCase())) {
          name = name + s[i] + " ";
        }
      }
    }

    if (name.isEmpty()) {
      // started with a badword - return name unchanged
      ret[0] = fname;
    }
    else {
      ret[0] = name.trim();
    }
    ret[1] = year.trim();
    LOGGER.trace("Movie title should be: \"" + ret[0] + "\", from " + ret[1]);
    return ret;
  }

  /**
   * gets IMDB id out of filename
   * 
   * @param filename
   *          a string
   * @return imdbid or empty
   */
  public static String detectImdbId(String text) {
    String imdb = "";
    if (text != null && !text.isEmpty()) {
      imdb = StrgUtils.substr(text, ".*(tt\\d{7}).*");
      if (imdb.isEmpty()) {
        imdb = StrgUtils.substr(text, ".*imdb\\.com\\/Title\\?(\\d{7}).*");
        if (!imdb.isEmpty()) {
          imdb = "tt" + imdb;
        }
      }
    }
    return imdb;
  }

  /**
   * removes some weird number-stopwords like 1080, 720 etc.. to ease the regex parsing for season/episode
   * 
   * @param filename
   * @return the cleaned one
   */
  public static String removeStopwordsFromTvEpisodeName(String filename) {
    for (String s : stopwords) {
      filename = filename.replaceAll("(?i)\\W" + s, ""); // stopword must start with a non-word (else too global)
    }
    return filename;
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

  /**
   * Try to clean the NFO(XML) content with JTidy.
   * 
   * @param sourceNfoContent
   *          the XML content to be cleaned
   * @return the cleaned XML content (or the source, if any Exceptions occur)
   */
  public static String cleanNfo(String sourceNfoContent) {
    try {
      Tidy tidy = new Tidy();
      tidy.setInputEncoding("UTF-8");
      tidy.setOutputEncoding("UTF-8");
      tidy.setWraplen(Integer.MAX_VALUE);
      tidy.setXmlOut(true);
      tidy.setSmartIndent(true);
      tidy.setXmlTags(true);
      tidy.setMakeClean(true);
      tidy.setForceOutput(true);
      tidy.setQuiet(true);
      tidy.setShowWarnings(false);
      StringReader in = new StringReader(sourceNfoContent);
      StringWriter out = new StringWriter();
      tidy.parse(in, out);

      return out.toString();
    }
    catch (Exception e) {
    }
    return sourceNfoContent;
  }
}
