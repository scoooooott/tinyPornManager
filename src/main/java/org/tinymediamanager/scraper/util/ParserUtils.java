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
package org.tinymediamanager.scraper.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.w3c.tidy.Tidy;

/**
 * Various parses methods to get a clean and workable name out of weird filenames
 * 
 * @author Myron Boyle
 */
public class ParserUtils {

  private static final Logger  LOGGER     = LoggerFactory.getLogger(ParserUtils.class);
  private static final String  DELIMITER  = "[\\[\\](){} _,.-]";

  public static final String[] STOPWORDS  = { "1080", "1080i", "1080p", "2160p", "2160i", "3d", "480i", "480p", "576i", "576p", "720", "720i", "720p",
      "ac3", "ac3ld", "ac3md", "aoe", "atmos", "bd5", "bdrip", "bdrip", "blueray", "bluray", "brrip", "cam", "cd1", "cd2", "cd3", "cd4", "cd5", "cd6",
      "cd7", "cd8", "cd9", "complete", "custom", "dc", "disc1", "disc2", "disc3", "disc4", "disc5", "disc6", "disc7", "disc8", "disc9", "divx",
      "divx5", "dl", "docu", "dsr", "dsrip", "dts", "dtv", "dubbed", "dutch", "dvd", "dvd1", "dvd2", "dvd3", "dvd4", "dvd5", "dvd6", "dvd7", "dvd8",
      "dvd9", "dvdivx", "dvdrip", "dvdscr", "dvdscreener", "emule", "etm", "extended", "fragment", "fs", "fps", "german", "h264", "hd", "hddvd",
      "hdrip", "hdtv", "hdtvrip", "hevc", "hrhd", "hrhdtv", "ind", "internal", "ld", "limited", "ma", "md", "multi", "multisubs", "nfo", "nfofix", "ntg",
      "ntsc", "ogg", "ogm", "pal", "pdtv", "proper", "pso", "r3", "r5", "read", "repack", "rerip", "remux", "retail", "roor", "rs", "rsvcd",
      "screener", "se", "subbed", "svcd", "swedish", "tc", "telecine", "telesync", "ts", "truehd", "uhd", "uncut", "unrated", "vcf", "vhs", "vhsrip", 
	  "webdl", "webrip", "workprint", "ws", "www", "x264", "xf", "xvid", "xvidvd", "xxx" };

  // clean before splitting (needs delimiter in front!)
  public static final String[] CLEANWORDS = { "24\\.000", "23\\.976", "23\\.98", "24\\.00" };

  private ParserUtils() {
    // private constructor for utility classes
  }

  /**
   * Tries to get title and year from filename<br>
   * 1. splits string using common delimiters ".- ()"<br>
   * 2. searches for first occurrence of common stopwords<br>
   * 3. if last token is 4 digits, assume year and set [1]<br>
   * 4. everything before the first stopword must be the movie name :p
   * 
   * @param filename
   *          the filename to get the title from
   * @param badWords
   *          the bad words to filter
   * @return title/year string (year can be empty)
   */
  public static String[] detectCleanTitleAndYear(String filename, List<String> badWords) {
    String[] ret = { "", "" };
    // use trace to not remove logging completely (function called way to often on multi movie dir parsing)
    LOGGER.trace("Parse filename for title: \"{}\"", filename);

    if (filename == null || filename.isEmpty()) {
      LOGGER.warn("Filename empty?!");
      return ret;
    }

    // remove extension (if found) and split (keep var)
    String fname = filename.replaceFirst("\\.\\w{2,4}$", "");
    // replaces any resolution 1234x1234 (must start and end with a non-word (else too global)
    fname = fname.replaceFirst("(?i)\\W\\d{3,4}x\\d{3,4}", " ");
    // replace FPS specific words (must start with a non-word (else too global)
    for (String cw : CLEANWORDS) {
      fname = fname.replaceFirst("(?i)\\W" + cw, " ");
    }

    LOGGER.trace("--------------------");
    LOGGER.trace("IN: {} ", fname);

    // Get [optionals] delimited
    List<String> opt = new ArrayList<>();
    Pattern p = Pattern.compile("\\[(.*?)\\]");
    Matcher m = p.matcher(fname);
    while (m.find()) {
      LOGGER.trace("OPT: {}", m.group(1));
      String[] o = StringUtils.split(m.group(1), DELIMITER);
      opt.addAll(Arrays.asList(o));
      fname = fname.replace(m.group(), ""); // remove complete group from name
    }
    LOGGER.trace("ARR: {}", opt);

    // detect OTR recordings - at least with that special pattern
    p = Pattern.compile(".*?(_\\d{2}\\.\\d{2}\\.\\d{2}[_ ]+\\d{2}\\-\\d{2}\\_).*"); // like _12.11.17_20-15_
    m = p.matcher(fname);
    if (m.matches() && m.start(1) > 10) {
      // start at some later point, not that if pattern is first
      LOGGER.trace("OTR: {}", m.group(1));
      fname = fname.substring(0, m.start(1));
    }

    // parse good filename
    String[] s = StringUtils.split(fname, DELIMITER);
    if (s.length == 0) {
      s = opt.toArray(new String[opt.size()]);
    }
    int firstFoundStopwordPosition = s.length;

    // iterate over all splitted items
    for (int i = 0; i < s.length; i++) {
      // search for stopword position
      for (String stop : STOPWORDS) {
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

    // scan backwards - if we have at least 1 token, and the last one is a 4 digit, assume year and remove
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    String year = "";
    for (int i = s.length - 1; i > 0; i--) {
      if (s[i].matches("\\d{4}")) {
        int parsedYear = Integer.parseInt(s[i]);
        if (parsedYear > 1800 && parsedYear < currentYear + 5) {
          // well, limit the year a bit...
          LOGGER.trace("removed token '{}'- seems to be year", s[i]);
          year = s[i];
          s[i] = "";
          break;
        }
      }
    }
    if (year.isEmpty()) {
      // parse all optional tags for it
      for (String o : opt) {
        if (o.matches("\\d{4}")) {
          int parsedYear = Integer.parseInt(o);
          if (parsedYear > 1800 && parsedYear < currentYear + 5) {
            year = String.valueOf(parsedYear);
            LOGGER.trace("found possible year: {}", o);
          }
        }
      }
    }

    // rebuild string, respecting bad words
    StringBuilder name = new StringBuilder();
    for (int i = 0; i < firstFoundStopwordPosition; i++) {
      boolean badwordFound = false;
      if (!s[i].isEmpty()) {
        // check for bad words
        for (String badword : badWords) {
          if (s[i].toLowerCase(Locale.ROOT).matches(badword)) {
            badwordFound = true;
            break;
          }
        }
        if (!badwordFound) {
          String word = s[i];
          // roman characters such as "Part Iv" should not be camel-cased
          switch (word.toUpperCase(Locale.ROOT)) {
            case "I":
            case "II":
            case "III":
            case "IV":
            case "V":
            case "VI":
            case "VII":
            case "VIII":
            case "IX":
            case "X":
              name.append(word.toUpperCase(Locale.ROOT)).append(" ");
              break;

            default:
              name.append(WordUtils.capitalizeFully(word)).append(" "); // make CamelCase
              break;
          }
        }
      }
    }

    if (name.length() == 0) {
      // started with a badword - return name unchanged
      ret[0] = fname;
    }
    else {
      ret[0] = name.toString().trim();
    }
    ret[1] = year.trim();
    LOGGER.trace("Movie title should be: \"{}\", from {}", ret[0], ret[1]);

    return ret;
  }

  /**
   * gets IMDB id out of filename
   * 
   * @param text
   *          a string
   * @return imdbid or empty
   */
  public static String detectImdbId(String text) {
    String imdb = "";
    if (text != null && !text.isEmpty()) {
      imdb = StrgUtils.substr(text, ".*(tt\\d{6,}).*");
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
   *          the file name to remove the stop- and bad words for
   * @return the cleaned one
   */
  public static String removeStopwordsAndBadwordsFromTvEpisodeName(String filename) {
    String before = filename;

    // replaces any resolution 1234x1234 (must start with a non-word (else too global)
    filename = filename.replaceFirst("(?i)\\W\\d{3,4}x\\d{3,4}", " ");

    for (String s : STOPWORDS) {
      filename = filename.replaceAll("(?i)\\W" + s + "(\\W|$)", " "); // TV stop words must start AND END with a non-word (else too global) or line
                                                                      // end
      if (LOGGER.isTraceEnabled() && filename.length() != before.length()) {
        LOGGER.trace("Removed some TV stopword (" + s + "): " + before + " -> " + filename);
        before = filename;
      }
    }

    // also remove bad words
    for (String s : TvShowModuleManager.SETTINGS.getBadWord()) {
      filename = filename.replaceAll("(?i)\\W" + s + "(\\W|$)", " "); // TV bad words must start AND END with a non-word (else too global) or line end
      if (LOGGER.isTraceEnabled() && filename.length() != before.length()) {
        LOGGER.trace("Removed some TV bad word (" + s + "): " + before + " -> " + filename);
        before = filename;
      }
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
      return new Pair<>(null, null);

    Pattern p = Pattern.compile("(.*)\\s+\\(?([0-9]{4})\\)?", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(title);
    if (m.find()) {
      return new Pair<>(m.group(1), m.group(2));
    }

    return new Pair<>(title, null);
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
    catch (Exception ignored) {
    }
    return sourceNfoContent;
  }

  /**
   * for all strings, return the "cleanest" one detected by rateCleanness()
   * 
   * @param badWords
   *          the bad words to filter
   * @param names
   *          strings
   * @return cleanest one
   */
  public static ParserInfo getCleanerString(List<String> badWords, String... names) {
    ArrayList<ParserInfo> info = new ArrayList<>(1);
    ParserInfo ret = null;
    int rate = -10000;

    for (String s : names) {
      info.add(new ParserInfo(s, badWords));
    }
    for (ParserInfo i : info) {
      int tmp = ParserUtils.rateCleanness(i);
      if (tmp > rate) {
        ret = i;
        rate = tmp;
      }
    }

    return ret;
  }

  /**
   * returns a count how "clean" a string is<br>
   * CamelCase name with space as delimiter should get a higher value...<br>
   * 
   * @param info
   *          the info to rate
   * @return number, the higher, the better
   */
  public static int rateCleanness(ParserInfo info) {
    if (info.clean.isEmpty()) {
      return -1;
    }
    int rate = 0;

    int words = info.clean.split(" ").length; // count words
    int seps = info.clean.split("[_.-]").length - 1; // count other separators
    int uc = info.clean.replaceAll("[^A-Z]", "").length(); // count uppercase
    int lc = info.clean.replaceAll("[A-Z]", "").length(); // count lowercase
    double cleaned = 100 - info.clean.length() * 100 / info.name.length();

    int cc = 0; // count CamelCase
    Pattern pattern = Pattern.compile("[A-Z][a-z]");
    Matcher matcher = pattern.matcher(info.clean);
    while (matcher.find()) {
      cc++;
    }

    // boost CamesCase & cleaned words, rate non-space separators very worse, the lower words the better
    rate = cc * 20 + (10 - words * 2) * 2 + (seps * -20) - info.clean.length() * 2 + (int) cleaned;
    if (!info.year.isEmpty()) {
      // we found a year in string, so boost this specially
      rate += 20;
    }

    LOGGER.trace(info + " - Rate:" + rate + "    PERC:" + cleaned + " LEN:" + info.clean.length() + " WRD:" + words + " UC:" + uc + " LC:" + lc
        + " CC:" + cc + " SEP:" + seps);

    return rate;
  }

  public static class ParserInfo {
    public String name  = "";
    public String year  = "";
    public String clean = "";

    ParserInfo(String name, List<String> badWords) {
      this.name = name.trim();
      String[] ty = detectCleanTitleAndYear(this.name, badWords);
      this.clean = ty[0];
      this.year = ty[1];
    }

    @Override
    public String toString() {
      return clean + " (" + this.year + ")";
    }
  }
}
