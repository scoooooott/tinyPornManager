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

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class StringUtils.
 * 
 * @author Manuel Laggner
 */
public class StrgUtils {
  private static Map<Integer, Replacement> REPLACEMENTS = buildReplacementMap();

  /*
   * build a replacement map of characters, which are not handled right by the normalizer method
   */
  private static Map<Integer, Replacement> buildReplacementMap() {
    Map<Integer, Replacement> replacements = new HashMap<Integer, Replacement>();
    replacements.put(0xc6, new Replacement("AE", "Ae"));
    replacements.put(0xe6, new Replacement("ae"));
    replacements.put(0xd0, new Replacement("D"));
    replacements.put(0x111, new Replacement("d"));
    replacements.put(0xd8, new Replacement("O"));
    replacements.put(0xf8, new Replacement("o"));
    replacements.put(0x152, new Replacement("OE", "Oe"));
    replacements.put(0x153, new Replacement("oe"));
    replacements.put(0x166, new Replacement("T"));
    replacements.put(0x167, new Replacement("t"));
    return replacements;
  }

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
  @SuppressWarnings("rawtypes")
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

  /**
   * gets regular expression based substring.
   * 
   * @param str
   *          the string to search
   * @param pattern
   *          the pattern to match; with ONE group bracket ()
   * @return the matched substring or empty string
   */
  public static String substr(String str, String pattern) {
    Pattern regex = Pattern.compile(pattern);
    Matcher m = regex.matcher(str);
    if (m.find()) {
      return m.group(1);
    }
    else {
      return "";
    }
  }

  /**
   * Parses the date.
   * 
   * @param dateAsString
   *          the date as string
   * @return the date
   * @throws ParseException
   *           the parse exception
   */
  public static Date parseDate(String dateAsString) throws ParseException {
    Date date = null;

    Pattern datePattern = Pattern.compile("([0-9]{2})[_\\.-]([0-9]{2})[_\\.-]([0-9]{4})");
    Matcher m = datePattern.matcher(dateAsString);
    if (m.find()) {
      date = new SimpleDateFormat("dd-MM-yyyy").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
    }
    else {
      datePattern = Pattern.compile("([0-9]{4})[_\\.-]([0-9]{2})[_\\.-]([0-9]{2})");
      m = datePattern.matcher(dateAsString);
      if (m.find()) {
        date = new SimpleDateFormat("yyyy-MM-dd").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
      }
    }

    if (date == null) {
      throw new ParseException("could not parse date from: \"" + dateAsString + "\"", 0);
    }

    return date;
  }

  /**
   * Remove all duplicate whitespace characters and line terminators are replaced with a single space.
   * 
   * @param s
   *          a not null String
   * @return a string with unique whitespace.
   */
  public static String removeDuplicateWhitespace(String s) {
    StringBuffer result = new StringBuffer();
    int length = s.length();
    boolean isPreviousWhiteSpace = false;
    for (int i = 0; i < length; i++) {
      char c = s.charAt(i);
      boolean thisCharWhiteSpace = Character.isWhitespace(c);
      if (!(isPreviousWhiteSpace && thisCharWhiteSpace)) {
        result.append(c);
      }
      isPreviousWhiteSpace = thisCharWhiteSpace;
    }
    return result.toString();
  }

  /**
   * This method takes an input String and replaces all special characters like umlauts, accented or other letter with diacritical marks with their
   * basic ascii eqivalents. Originally written by Jens Hausherr (https://github.com/jabbrwcky), modified by Manuel Laggner
   * 
   * @param input
   *          String to convert
   * @param replaceAllCapitalLetters
   *          <code>true</code> causes uppercase special chars that are replaced by more than one character to be replaced by all-uppercase
   *          replacements; <code>false</code> will cause only the initial character of the replacements to be in uppercase and all subsequent
   *          replacement characters will be in lowercase.
   * @return Input string reduced to ASCII-safe characters.
   */
  public static String convertToAscii(String input, boolean replaceAllCapitalLetters) {
    String result = null;
    if (null != input) {
      String normalized = Normalizer.normalize(input, Normalizer.Form.NFKD);

      int len = normalized.length();
      result = processSpecialChars(normalized.toCharArray(), 0, len, replaceAllCapitalLetters);
    }

    return result;
  }

  /*
   * replace special characters
   */
  private static String processSpecialChars(char[] target, int offset, int len, boolean uppercase) {
    StringBuilder result = new StringBuilder();
    boolean skip = false;

    for (int i = 0; i < len; i++) {
      if (skip) {
        skip = false;
      }
      else {
        char c = target[i];
        if ((c > 0x20 && c < 0x40) || (c > 0x7a && c < 0xc0) || (c > 0x5a && c < 0x61) || (c > 0x79 && c < 0xc0) || c == 0xd7 || c == 0xf7) {
          result.append(c);
        }
        else if (Character.isDigit(c) || Character.isISOControl(c)) {
          result.append(c);
        }
        else if (Character.isWhitespace(c) || Character.isLetter(c)) {
          boolean isUpper = false;

          switch (c) {
            case '\u00df':
              result.append("ss");
              break;
            /* Handling of capital and lowercase umlauts */
            case 'A':
            case 'O':
            case 'U':
              isUpper = true;
            case 'a':
            case 'o':
            case 'u':
              result.append(c);
              if (i + 1 < target.length && target[i + 1] == 0x308) {
                result.append(isUpper && uppercase ? 'E' : 'e');
                skip = true;
              }
              break;
            default:
              Replacement rep = REPLACEMENTS.get(Integer.valueOf(c));
              if (rep != null) {
                result.append(uppercase ? rep.UPPER : rep.LOWER);
              }
              else
                result.append(c);
          }
        }
      }
    }
    return result.toString();
  }

  /**
   * Combination of replacements for upper- and lowercase mode.
   */
  private static class Replacement {
    private final String UPPER;
    private final String LOWER;

    Replacement(String ucReplacement, String lcReplacement) {
      this.UPPER = ucReplacement;
      this.LOWER = lcReplacement;
    }

    Replacement(String caseInsensitiveReplacement) {
      this(caseInsensitiveReplacement, caseInsensitiveReplacement);
    }
  }
}