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
package org.tinymediamanager.core.tvshow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class TvShowEpisodeAndSeasonParser.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeAndSeasonParser {
  private static final Logger LOGGER              = LoggerFactory.getLogger(TvShowEpisodeAndSeasonParser.class);

  // foo.yyyy.mm.dd.*
  private static Pattern      date1               = Pattern.compile("([0-9]{4})[.-]([0-9]{2})[.-]([0-9]{2})", Pattern.CASE_INSENSITIVE);

  // foo.mm.dd.yyyy.*
  private static Pattern      date2               = Pattern.compile("([0-9]{2})[.-]([0-9]{2})[.-]([0-9]{4})", Pattern.CASE_INSENSITIVE);

  // new parsing logic
  public static Pattern       SEASON_PATTERN      = Pattern.compile("(staffel|season|series)[\\s_.-]*(\\d{1,4})", Pattern.CASE_INSENSITIVE);
  private static Pattern      episodePattern      = Pattern.compile("[epx_-]+(\\d{1,3})", Pattern.CASE_INSENSITIVE);
  private static Pattern      episodePattern2     = Pattern.compile("(?:episode|ep)[\\. _-]*(\\d{1,3})", Pattern.CASE_INSENSITIVE);
  private static Pattern      romanPattern        = Pattern.compile("(part|pt)[\\._\\s]+([MDCLXVI]+)", Pattern.CASE_INSENSITIVE);
  private static Pattern      seasonMultiEP       = Pattern.compile("s(\\d{1,4})[ ]?((?:([epx_.-]+\\d{1,3})+))", Pattern.CASE_INSENSITIVE);
  private static Pattern      seasonMultiEP2      = Pattern.compile("(\\d{1,4})(?=x)((?:([epx]+\\d{1,3})+))", Pattern.CASE_INSENSITIVE);
  private static Pattern      numbers2Pattern     = Pattern.compile("([0-9]{2})", Pattern.CASE_INSENSITIVE);
  private static Pattern      numbers3Pattern     = Pattern.compile("([0-9])([0-9]{2})", Pattern.CASE_INSENSITIVE);
  private static Pattern      tvMultipartMatching = Pattern.compile("^[-_ex]+([0-9]+(?:(?:[a-i]|\\.[1-9])(?![0-9]))?)", Pattern.CASE_INSENSITIVE);

  public static String cleanEpisodeTitle(String titleToClean, String tvShowName) {
    String basename = FilenameUtils.getBaseName(ParserUtils.removeStopwordsAndBadwordsFromTvEpisodeName(titleToClean));

    // parse foldername
    Pattern regex = Pattern.compile("(.*[\\/\\\\])");
    Matcher m = regex.matcher(basename);
    if (m.find()) {
      basename = basename.replaceAll(regex.pattern(), "");
    }
    basename = basename + " ";

    // remove show name
    if (tvShowName != null && !tvShowName.isEmpty()) {
      // remove string like tvshow name (440, 24, ...)
      basename = basename.replaceAll("(?i)^" + Pattern.quote(tvShowName) + "", "");
      basename = basename.replaceAll("(?i) " + Pattern.quote(tvShowName) + " ", "");
    }
    basename = basename.replaceFirst("\\.\\w{1,4}$", ""); // remove extension if 1-4 chars
    basename = basename.replaceFirst("[\\(\\[]\\d{4}[\\)\\]]", ""); // remove (xxxx) or [xxxx] as year

    return removeEpisodeVariantsFromTitle(basename);
  }

  private static String removeEpisodeVariantsFromTitle(String title) {
    StringBuilder backup = new StringBuilder(title);
    StringBuilder ret = new StringBuilder();

    // quite same patters as above, minus the last ()
    title = title.replaceAll("[Ss]([0-9]+)[\\]\\[ _.-]*[Ee]([0-9]+)", "");
    title = title.replaceAll("[ _.-]()[Ee][Pp]?_?([0-9]+)", "");
    title = title.replaceAll("([0-9]{4})[.-]([0-9]{2})[.-]([0-9]{2})", "");
    title = title.replaceAll("([0-9]{2})[.-]([0-9]{2})[.-]([0-9]{4})", "");
    title = title.replaceAll("[\\\\/\\._ \\[\\(-]([0-9]+)x([0-9]+)", "");
    title = title.replaceAll("[\\/ _.-]p(?:ar)?t[ _.-]()([ivx]+)", "");
    title = title.replaceAll("[epx_-]+(\\d{1,3})", "");
    title = title.replaceAll("episode[\\. _-]*(\\d{1,3})", "");
    title = title.replaceAll("(part|pt)[\\._\\s]+([MDCLXVI]+)", "");
    title = title.replaceAll("(staffel|season|series)[\\s_.-]*(\\d{1,4})", "");
    title = title.replaceAll("s(\\d{1,4})[ ]?((?:([epx_.-]+\\d{1,3})+))", "");
    title = title.replaceAll("(\\d{1,4})(?=x)((?:([epx]+\\d{1,3})+))", "");

    // split and reassemble
    String[] splitted = StringUtils.split(title, "[\\[\\]() _,.-]");
    for (String s : splitted) {
      ret.append(" ").append(s);
    }
    ret = new StringBuilder(ret.toString().trim());

    // uh-oh - we removed too much
    // also split and reassemble backup
    if (StringUtils.isEmpty(ret.toString())) {
      String[] b = StringUtils.split(backup.toString(), "[\\[\\]() _,.-]");
      backup = new StringBuilder();
      for (String s : b) {
        backup.append(" ").append(s);
      }
      // System.out.println("****** empty string - setting back to " + backup);
      ret = new StringBuilder(backup.toString().trim());
    }
    return ret.toString();
  }

  /**
   * Does all the season/episode detection
   * 
   * @param name
   *          the RELATIVE filename (like /dir2/seas1/fname.ext) from the TvShowRoot
   * @param showname
   *          the show name
   * @return result the calculated result
   */
  public static EpisodeMatchingResult detectEpisodeFromFilenameAlternative(String name, String showname) {
    // first check ONLY filename!
    EpisodeMatchingResult result = detect(FilenameUtils.getName(name), showname);

    // only EPs found, but no season - parse whole string for season ONLY
    if (!result.episodes.isEmpty() && result.season == -1) {
      EpisodeMatchingResult result2 = detect(name, showname);
      result.season = result2.season;
    }
    else if (result.season == -1 && result.episodes.isEmpty()) {
      // nothing found - check whole string as such
      result = detect(name, showname);
    }

    return result;
  }

  /**
   * Does all the season/episode detection
   * 
   * @param name
   *          the RELATIVE filename (like /dir2/seas1/fname.ext) from the TvShowRoot
   * @param showname
   *          the show name
   * @return result the calculated result
   */
  public static EpisodeMatchingResult detect(String name, String showname) {
    LOGGER.debug("parsing '" + name + "'");
    EpisodeMatchingResult result = new EpisodeMatchingResult();
    Pattern regex;
    Matcher m;

    // remove problematic strings from name
    String filename = FilenameUtils.getName(name);
    String extension = FilenameUtils.getExtension(name);

    // check for disc files and remove!!
    if (filename.toLowerCase(Locale.ROOT).matches("(video_ts|vts_\\d\\d_\\d)\\.(vob|bup|ifo)") || // dvd
        filename.toLowerCase(Locale.ROOT).matches("(index\\.bdmv|movieobject\\.bdmv|\\d{5}\\.m2ts)")) { // bluray
      name = FilenameUtils.getPath(name);
    }

    String basename = ParserUtils.removeStopwordsAndBadwordsFromTvEpisodeName(name);
    String foldername = "";

    // parse foldername
    regex = Pattern.compile("(.*[\\/\\\\])");
    m = regex.matcher(basename);
    if (m.find()) {
      foldername = m.group(1);
      basename = basename.replaceAll(regex.pattern(), "");
    }

    // happens, when we only parse filename, but it completely gets stripped out.
    if (basename.isEmpty() && foldername.isEmpty()) {
      return result;
    }

    if (showname != null && !showname.isEmpty()) {
      // remove string like tvshow name (440, 24, ...)
      basename = basename.replaceAll("(?i)^" + Pattern.quote(showname) + "", "");
      basename = basename.replaceAll("(?i) " + Pattern.quote(showname) + " ", "");
    }
    basename = basename.replaceFirst("\\.\\w{1,4}$", ""); // remove extension if 1-4 chars
    basename = basename.replaceFirst("[\\(\\[]\\d{4}[\\)\\]]", ""); // remove (xxxx) or [xxxx] as year

    basename = basename + " ";

    result.stackingMarkerFound = !Utils.getStackingMarker(filename).isEmpty();
    result.name = basename.trim();

    // season detection
    if (result.season == -1) {
      regex = SEASON_PATTERN;
      m = regex.matcher(foldername + basename);
      if (m.find()) {
        int s = result.season;
        try {
          s = Integer.parseInt(m.group(2));
        }
        catch (NumberFormatException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        result.season = s;
        LOGGER.trace("add found season {}", s);
      }
    }

    // parse SxxEPyy 1-N
    regex = seasonMultiEP;
    m = regex.matcher(foldername + basename);
    int lastFoundEpisode = 0;
    while (m.find()) {
      int s = -1;
      try {
        s = Integer.parseInt(m.group(1));
        String eps = m.group(2); // name.s01"ep02-02-04".ext
        // now we have a string of 1-N episodes - parse them
        Pattern regex2 = episodePattern; // episode fixed to 1-2 chars
        Matcher m2 = regex2.matcher(eps);
        while (m2.find()) {
          int ep = 0;
          try {
            ep = Integer.parseInt(m2.group(1));
          }
          catch (NumberFormatException nfe) {
            // can not happen from regex since we only come here with max 2 numeric chars
          }
          // check if the found episode is greater zero, not already in the list and if multi episode
          // it has to be the next number than the previous found one
          if (ep > 0 && !result.episodes.contains(ep) && (lastFoundEpisode == 0 || lastFoundEpisode + 1 == ep)) {
            lastFoundEpisode = ep;
            result.episodes.add(ep);
            LOGGER.trace("add found EP " + ep);
          }
        }
      }
      catch (NumberFormatException nfe) {
        // can not happen from regex since we only come here with max 2 numeric chars
      }
      if (s >= 0) {
        result.season = s;
        LOGGER.trace("add found season " + s);
      }
    }

    // parse XYY or XX_YY 1-N
    regex = seasonMultiEP2;
    m = regex.matcher(foldername + basename);
    while (m.find()) {
      int s = -1;
      try {
        // for the case of name.1x02x03.ext
        if (m.group(2) != null && result.season == -1) {
          s = Integer.parseInt(m.group(1));
        }
        String eps = m.group(2); // name.s01"ep02-02-04".ext
        // now we have a string of 1-N episodes - parse them
        Pattern regex2 = episodePattern; // episode fixed to 1-2 chars
        Matcher m2 = regex2.matcher(eps);
        while (m2.find()) {
          int ep = 0;
          try {
            ep = Integer.parseInt(m2.group(1));
          }
          catch (NumberFormatException nfe) {
            // can not happen from regex since we only come here with max 2 numeric chars
          }
          if (ep > 0 && !result.episodes.contains(ep)) {
            result.episodes.add(ep);
            LOGGER.trace("add found EP " + ep);
          }
        }
      }
      catch (NumberFormatException nfe) {
        // can not happen from regex since we only come here with max 2 numeric chars
      }
      if (s >= 0) {
        result.season = s;
        LOGGER.trace("add found season " + s);
      }
    }

    // Episode-only parsing, when previous styles didn't find anything!
    if (result.episodes.isEmpty()) {
      regex = episodePattern2;
      m = regex.matcher(basename);
      while (m.find()) {
        int ep = 0;
        try {
          ep = Integer.parseInt(m.group(1));
        }
        catch (NumberFormatException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        if (ep > 0 && !result.episodes.contains(ep)) {
          result.episodes.add(ep);
          LOGGER.trace("add found EP " + ep);
        }
      }
    }

    // ======================================================================
    // After here are some generic detections
    // run them only, when we have NO result!!!
    // so we step out here...
    // ======================================================================
    if (!result.episodes.isEmpty()) {
      return postClean(result);
    }

    // parse Roman only when not found anything else!!
    if (result.episodes.isEmpty()) {
      regex = romanPattern;
      m = regex.matcher(basename);
      while (m.find()) {
        int ep = 0;
        ep = decodeRoman(m.group(2));
        if (ep > 0 && !result.episodes.contains(ep)) {
          result.episodes.add(ep);
          LOGGER.trace("add found EP " + ep);
        }
      }
    }

    if (result.season == -1) {
      // Date1 pattern yyyy-mm-dd
      m = date1.matcher(basename);
      if (m.find()) {
        int s = result.season;
        try {
          s = Integer.parseInt(m.group(1));
          result.date = new SimpleDateFormat("yyyy-MM-dd").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
        }
        catch (NumberFormatException | ParseException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        result.season = s;
        LOGGER.trace("add found year as season " + s + " date: " + result.date);
        return postClean(result); // since we have a matching year, we wont find episodes solely by number
      }
    }

    if (result.season == -1) {
      // Date2 pattern dd-mm-yyyy
      m = date2.matcher(basename);
      if (m.find()) {
        int s = result.season;
        try {
          s = Integer.parseInt(m.group(3));
          result.date = new SimpleDateFormat("dd-MM-yyyy").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
        }
        catch (NumberFormatException | ParseException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        result.season = s;
        LOGGER.trace("add found year as season " + s + " date: " + result.date);
        return postClean(result); // since we have a matching year, we wont find episodes solely by number
      }
    }

    // multiple numbers: get consecutive ones
    String delimitedNumbers = basename.replaceAll("\\|", "_"); // replace our delimiter
    delimitedNumbers = delimitedNumbers.replaceAll("(\\d+)", "$1|"); // add delimiter after numbers
    delimitedNumbers = delimitedNumbers.replaceAll("[^0-9\\|]", ""); // replace everything but numbers
    String[] numbersOnly = delimitedNumbers.split("\\|"); // split on our delimiters
    // now we have something like "8|804|2020"

    for (String num : numbersOnly) {
      if (num.length() == 3) {
        // Filename contains only 3 subsequent numbers; parse this as SEE
        int s = Integer.parseInt(num.substring(0, 1));
        int ep = Integer.parseInt(num.substring(1));
        if (ep > 0 && !result.episodes.contains(ep)) {
          result.episodes.add(ep);
          LOGGER.trace("add found EP " + ep);
        }
        LOGGER.trace("add found season " + s);
        result.season = s;
        // for 3 character numbers, we iterate multiple times!
        // do not stop on first one"
        // return result;
      }
    }
    // did we find 3 consecutive numbers? step out...
    if (!result.episodes.isEmpty()) {
      return postClean(result);
    }

    for (String num : numbersOnly) {
      if (num.length() == 2) {
        // Filename contains only 2 subsequent numbers; parse this as EE
        int ep = Integer.parseInt(num);
        if (ep > 0 && !result.episodes.contains(ep)) {
          result.episodes.add(ep);
          LOGGER.trace("add found EP " + ep);
        }
        return postClean(result);
      }
    }

    for (String num : numbersOnly) {
      if (num.length() == 1) {
        int ep = Integer.parseInt(num); // just one :P
        if (ep > 0 && !result.episodes.contains(ep)) {
          result.episodes.add(ep);
          LOGGER.trace("add found EP " + ep);
        }
        return postClean(result);
      }
    }

    return postClean(result);
  }

  private static EpisodeMatchingResult postClean(EpisodeMatchingResult emr) {
    // try to clean the filename
    emr.cleanedName = cleanFilename(emr.name, new Pattern[] { SEASON_PATTERN, seasonMultiEP, seasonMultiEP2, episodePattern, episodePattern2,
        numbers3Pattern, numbers2Pattern, romanPattern, date1, date2 });
    Collections.sort(emr.episodes);
    LOGGER.debug("returning result " + emr);
    return emr;
  }

  private static String cleanFilename(String name, Pattern[] patterns) {
    String result = name;
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(result);
      if (matcher.find()) {
        result = matcher.replaceFirst("");
      }
    }

    // last but not least clean all leading/trailing separators
    result = result.replaceAll("^[ \\.\\-_]+", "");
    result = result.replaceAll("[ \\.\\-_]+$", "");

    return result;
  }

  /**
   * Decode single roman.
   * 
   * @param letter
   *          the letter
   * @return the int
   */
  private static int decodeSingleRoman(char letter) {
    switch (letter) {
      case 'M':
        return 1000;
      case 'D':
        return 500;
      case 'C':
        return 100;
      case 'L':
        return 50;
      case 'X':
        return 10;
      case 'V':
        return 5;
      case 'I':
        return 1;
      default:
        return 0;
    }
  }

  /**
   * Decode roman.
   * 
   * @param roman
   *          the roman
   * @return the int
   */
  public static int decodeRoman(String roman) {
    int result = 0;
    String uRoman = roman.toUpperCase(Locale.ROOT); // case-insensitive
    for (int i = 0; i < uRoman.length() - 1; i++) {// loop over all but the last
                                                   // character
      // if this character has a lower value than the next character
      if (decodeSingleRoman(uRoman.charAt(i)) < decodeSingleRoman(uRoman.charAt(i + 1))) {
        // subtract it
        result -= decodeSingleRoman(uRoman.charAt(i));
      }
      else {
        // add it
        result += decodeSingleRoman(uRoman.charAt(i));
      }
    }
    // decode the last character, which is always added
    result += decodeSingleRoman(uRoman.charAt(uRoman.length() - 1));
    return result;
  }

  /******************************************************************************************
   * helper classes
   ******************************************************************************************/
  public static class EpisodeMatchingResult {

    public int           season              = -1;
    public List<Integer> episodes            = new ArrayList<>();
    public String        name                = "";
    public String        cleanedName         = "";
    public Date          date                = null;
    public boolean       stackingMarkerFound = false;

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }
}
