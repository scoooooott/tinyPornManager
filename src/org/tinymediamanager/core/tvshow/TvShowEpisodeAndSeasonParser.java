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
package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class TvShowEpisodeAndSeasonParser.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeAndSeasonParser {
  /** The Constant LOGGER. */
  private final static Logger LOGGER   = LoggerFactory.getLogger(TvShowEpisodeAndSeasonParser.class);

  // foo.s01.e01, foo.s01_e01, S01E02 foo, S01 - E02
  /** The pattern1. */
  private static Pattern      pattern1 = Pattern.compile("[Ss]([0-9]+)[\\]\\[ ._-]*[Ee]([0-9]+)([^\\\\/]*)$", Pattern.CASE_INSENSITIVE);
  // foo.ep01, foo.EP_01
  /** The pattern2. */
  private static Pattern      pattern2 = Pattern.compile("[\\._ -]()[Ee][Pp]_?([0-9]+)([^\\\\/]*)$", Pattern.CASE_INSENSITIVE);
  // foo.yyyy.mm.dd.*
  /** The date1. */
  private static Pattern      date1    = Pattern.compile("([0-9]{4})[\\.-]([0-9]{2})[\\.-]([0-9]{2})", Pattern.CASE_INSENSITIVE);
  // foo.mm.dd.yyyy.*
  /** The date2. */
  private static Pattern      date2    = Pattern.compile("([0-9]{2})[\\.-]([0-9]{2})[\\.-]([0-9]{4})", Pattern.CASE_INSENSITIVE);
  // foo.1x09* or just /1x09*
  /** The pattern5. */
  private static Pattern      pattern5 = Pattern.compile("[\\\\/\\._ \\[\\(-]([0-9]+)x([0-9]+)([^\\\\/]*)$", Pattern.CASE_INSENSITIVE);
  // foo.103*, 103 foo
  /** The pattern6. */
  private static Pattern      pattern6 = Pattern.compile("[\\\\/\\._ -]([0-9]+)([0-9][0-9])([\\._ -][^\\\\/]*)$", Pattern.CASE_INSENSITIVE);
  // Part I, Pt.VI
  /** The pattern7. */
  private static Pattern      pattern7 = Pattern.compile("[\\/._ -]p(?:ar)?t[_. -]()([ivx]+)([._ -][^\\/]*)$", Pattern.CASE_INSENSITIVE);

  /**
   * The Class EpisodeMatchingResult.
   * 
   * @author Manuel Laggner
   */
  public static class EpisodeMatchingResult {
    /** The season. */
    public int           season   = -1;

    /** The episodes. */
    public List<Integer> episodes = new ArrayList<Integer>();

    /** The name. */
    public String        name     = "";

    /** the date */
    public Date          date     = null;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }

  /**
   * Detect episode from filename.
   * 
   * @param file
   *          the file
   * @return the episode matching result
   */
  public static EpisodeMatchingResult detectEpisodeFromFilename(File file) {
    LOGGER.info("Detect episodes/seasons from " + file.getName());
    EpisodeMatchingResult result = new EpisodeMatchingResult();
    String fileName = file.getName();

    result = parseString(fileName);
    Collections.sort(result.episodes);

    LOGGER.debug("returning result " + result);
    return result;
  }

  /**
   * old-style ;)
   * 
   * @param filename
   * @return
   */
  public static EpisodeMatchingResult detectEpisodeFromFilenameAlternative(File file, String showname) {
    EpisodeMatchingResult result = new EpisodeMatchingResult();

    // remove problematic strings from name
    String filename = ParserUtils.cleanTvEpisodeName(file.getName());
    if (showname != null && !showname.isEmpty()) {
      // remove string like tvshow name (440, 24, ...)
      filename = filename.replaceAll("(?i)^" + showname + "", "");
      filename = filename.replaceAll("(?i) " + showname + " ", "");
    }
    filename = FilenameUtils.getBaseName(filename); // w/o extension

    // FIXME: pattern quite fine, but second find should start AFTER complete first match, not inbetween
    Pattern regex = Pattern.compile("(?i)[epx_-]+(\\d{2})"); // episode fixed to 2 chars
    Matcher m = regex.matcher(filename);
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
      }
    }

    // parse Roman
    regex = Pattern.compile("(?i)(part|pt)[\\._\\s]+([MDCLXVI]+)");
    m = regex.matcher(filename);
    while (m.find()) {
      int ep = 0;
      ep = decodeRoman(m.group(2));
      if (ep > 0 && !result.episodes.contains(ep)) {
        result.episodes.add(ep);
      }
    }

    // season detection
    if (result.season == -1) {
      regex = Pattern.compile("(?i)(s|staffel|season)[\\s]*(\\d{1,4})");
      m = regex.matcher(filename);
      if (m.find()) {
        int s = result.season;
        try {
          s = Integer.parseInt(m.group(2));
        }
        catch (NumberFormatException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        result.season = s;
      }
    }

    if (result.season == -1) {
      // Date1 pattern yyyy-mm-dd
      m = date1.matcher(filename);
      if (m.find()) {
        int s = result.season;
        try {
          s = Integer.parseInt(m.group(1));
          result.date = new SimpleDateFormat("yyyy-MM-dd").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
        }
        catch (NumberFormatException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        catch (ParseException e) {
          // can not happen from regex since we only come here with correct pattern
        }
        result.season = s;
      }
    }

    if (result.season == -1) {
      // Date2 pattern dd-mm-yyyy
      m = date2.matcher(filename);
      if (m.find()) {
        int s = result.season;
        try {
          s = Integer.parseInt(m.group(3));
          result.date = new SimpleDateFormat("dd-MM-yyyy").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
        }
        catch (NumberFormatException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        catch (ParseException e) {
          // can not happen from regex since we only come here with correct pattern
        }
        result.season = s;
      }
    }

    // parse XYY
    if (result.episodes.isEmpty() || result.season == -1) {
      regex = Pattern.compile("[^\\d](\\d)+[x_-]?(\\d{2})[^\\d]");
      m = regex.matcher(filename);
      if (m.find()) {
        int ep = -1;
        int s = -1;
        try {
          s = Integer.parseInt(m.group(1));
          ep = Integer.parseInt(m.group(2));
        }
        catch (NumberFormatException nfe) {
          // can not happen from regex since we only come here with max 2 numeric chars
        }
        if (ep > 0 && !result.episodes.contains(ep)) {
          result.episodes.add(ep);
        }
        if (s >= 0) {
          result.season = s;
        }
      }
    }

    Collections.sort(result.episodes);
    return result;
  }

  /**
   * Detect episode from directory.
   * 
   * @param directory
   *          the directory
   * @param rootDirOfTvShow
   *          the root dir of tv show
   * @return the episode matching result
   */
  public static EpisodeMatchingResult detectEpisodeFromDirectory(File directory, String rootDirOfTvShow) {
    LOGGER.info("Detect episodes/seasons from " + directory.getAbsolutePath());
    EpisodeMatchingResult result = new EpisodeMatchingResult();

    // check if directory is the root of the tv show
    if (directory.toURI().equals(new File(rootDirOfTvShow).toURI())) {
      return result;
    }

    String directoryName = directory.getName();

    result = parseString(directoryName);

    if (result.episodes.size() == 0) {
      // look one directory above
      detectEpisodeFromDirectory(directory.getParentFile(), rootDirOfTvShow);
    }

    Collections.sort(result.episodes);
    return result;
  }

  /**
   * Parses the string.
   * 
   * @param stringToParse
   *          the string to parse
   * @return the episode matching result
   */
  private static EpisodeMatchingResult parseString(String stringToParse) {
    LOGGER.debug("parse String " + stringToParse);
    EpisodeMatchingResult result = new EpisodeMatchingResult();
    EpisodeMatchingResult resultFromParser = new EpisodeMatchingResult();

    resultFromParser = parse(stringToParse, pattern1);
    result = combineResults(result, resultFromParser);

    resultFromParser = parse(stringToParse, pattern2);
    result = combineResults(result, resultFromParser);

    resultFromParser = parse(stringToParse, date1);
    result = combineResults(result, resultFromParser);

    resultFromParser = parse(stringToParse, date2);
    result = combineResults(result, resultFromParser);

    resultFromParser = parse(stringToParse, pattern5);
    result = combineResults(result, resultFromParser);

    resultFromParser = parse(stringToParse, pattern6);
    result = combineResults(result, resultFromParser);

    resultFromParser = parse(stringToParse, pattern7);
    result = combineResults(result, resultFromParser);

    return result;
  }

  /**
   * Combine results.
   * 
   * @param result
   *          the result
   * @param resultFromParser
   *          the result from parser
   * @return the episode matching result
   */
  private static EpisodeMatchingResult combineResults(EpisodeMatchingResult result, EpisodeMatchingResult resultFromParser) {
    if (result.season < 0 && resultFromParser.season >= 0) {
      result.season = resultFromParser.season;
    }

    if (result.episodes.size() == 0 && resultFromParser.episodes.size() > 0) {
      result.episodes.addAll(resultFromParser.episodes);
    }

    if (StringUtils.isBlank(result.name) && StringUtils.isNotBlank(resultFromParser.name)) {
      result.name = resultFromParser.name;
    }

    return result;
  }

  /**
   * Parses the.
   * 
   * @param searchString
   *          the search string
   * @param pattern
   *          the pattern
   * @return the episode matching result
   */
  private static EpisodeMatchingResult parse(String searchString, Pattern pattern) {
    LOGGER.debug("parsing " + searchString + " with " + pattern.toString());
    EpisodeMatchingResult result = new EpisodeMatchingResult();
    Matcher m = pattern.matcher(searchString);

    while (m.find()) {
      int ep = 0;

      // match episode
      try {
        ep = Integer.parseInt(m.group(2));
      }
      catch (NumberFormatException nfe) {
        // maybe roman notation
        ep = decodeRoman(m.group(2));
      }

      if (ep > 0 && !result.episodes.contains(ep)) {
        result.episodes.add(ep);
      }

      // match season
      if (result.season < 0) {
        int season = -1;
        try {
          season = Integer.parseInt(m.group(1));
        }
        catch (NumberFormatException nfe) {
        }

        result.season = season;
      }

      // if episode found take the 3 matcher group again to the matcher
      if (StringUtils.isBlank(result.name)) {
        EpisodeMatchingResult newResult = parseString(m.group(3));
        if (newResult.episodes.size() > 0) {
          // we found episodes again
          result.episodes.addAll(newResult.episodes);
        }
        else {
          // get name an strip out file extension
          result.name = FilenameUtils.getBaseName(m.group(3));
        }
      }
    }

    LOGGER.debug("matching result " + result);

    return result;
  }

  /**
   * Detect season.
   * 
   * @param relativePath
   *          the relative path
   * @return the int
   */
  public static int detectSeason(String relativePath) {
    LOGGER.info("detect season from " + relativePath);
    int season = -1;

    // season detection
    Pattern regex = Pattern.compile("(?i)(?:s|season|staffel|)[\\s]*(\\d+)");
    Matcher m = regex.matcher(relativePath);
    if (m.find()) {
      try {
        season = Integer.parseInt(m.group(1));
      }
      catch (NumberFormatException nfe) {
        // can not happen from regex since we only come here with max 2 numeric chars
      }
    }

    return season;
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
    String uRoman = roman.toUpperCase(); // case-insensitive
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
}
