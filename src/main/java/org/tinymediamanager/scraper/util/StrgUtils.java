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

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class StrgUtils. This can be used for several String related tasks
 * 
 * @author Manuel Laggner, Myron Boyle
 * @since 1.0
 */
public class StrgUtils {
  private static final Map<Integer, Replacement> REPLACEMENTS          = new HashMap<>(20);
  private static final String[]                  COMMON_TITLE_PREFIXES = buildCommonTitlePrefixes();
  private static final char[]                    HEX_ARRAY             = "0123456789ABCDEF".toCharArray();
  private static final Map<String, String>       DATE_FORMAT_REGEXPS   = new HashMap<>(30);
  static {
    DATE_FORMAT_REGEXPS.put("^\\d{8}$", "yyyyMMdd");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
    DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
    DATE_FORMAT_REGEXPS.put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "dd.MM.yyyy");
    DATE_FORMAT_REGEXPS.put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}$", "yyyy.MM.dd");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
    DATE_FORMAT_REGEXPS.put("^\\d{12}$", "yyyyMMddHHmm");
    DATE_FORMAT_REGEXPS.put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
    DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
    DATE_FORMAT_REGEXPS.put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
    DATE_FORMAT_REGEXPS.put("^\\d{14}$", "yyyyMMddHHmmss");
    DATE_FORMAT_REGEXPS.put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
    DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
    DATE_FORMAT_REGEXPS.put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,3}$", "dd-MM-yyyy HH:mm:ss.S");
    DATE_FORMAT_REGEXPS.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,3}$", "yyyy-MM-dd HH:mm:ss.S");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,3}$", "MM/dd/yyyy HH:mm:ss.S");
    DATE_FORMAT_REGEXPS.put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,3}$", "yyyy/MM/dd HH:mm:ss.S");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
    DATE_FORMAT_REGEXPS.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    DATE_FORMAT_REGEXPS.put("^\\w{3} \\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "z yyyy-MM-dd HH:mm:ss"); // MediaInfo
    DATE_FORMAT_REGEXPS.put("^\\w{3} \\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{1,3}$", "z yyyy-MM-dd HH:mm:ss.S"); // MediaInfo

    REPLACEMENTS.put(0xc6, new Replacement("AE", "Ae"));
    REPLACEMENTS.put(0xe6, new Replacement("ae"));
    REPLACEMENTS.put(0xd0, new Replacement("D"));
    REPLACEMENTS.put(0x111, new Replacement("d"));
    REPLACEMENTS.put(0xd8, new Replacement("O"));
    REPLACEMENTS.put(0xf8, new Replacement("o"));
    REPLACEMENTS.put(0x152, new Replacement("OE", "Oe"));
    REPLACEMENTS.put(0x153, new Replacement("oe"));
    REPLACEMENTS.put(0x166, new Replacement("T"));
    REPLACEMENTS.put(0x167, new Replacement("t"));
    REPLACEMENTS.put(0x141, new Replacement("L"));
    REPLACEMENTS.put(0x142, new Replacement("l"));
  }

  private static String[] buildCommonTitlePrefixes() {
    // @formatter:off
    return new String[] { "A", "An", "The", // english
        "Der", "Die", "Das", "Ein", "Eine", "Eines", "Einer", "Einem", "Einen", // german
        "Le", "La", "Une", "Des", // french
        "El", "Los", "La", "Las", "Un", "Unos", "Una", "Unas" // spanish
    };
    // @formatter:on
  }

  private StrgUtils() {
  }

  /**
   * ByteArray to HEX String
   * 
   * @param bytes
   * @return
   */
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  /**
   * Removes the html.
   * 
   * @param html
   *          the html
   * @return the string
   */
  public static String removeHtml(String html) {
    if (html == null) {
      return null;
    }
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
    if (str == null) {
      return null;
    }
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
    if (map == null) {
      return "null";
    }
    if (map.isEmpty()) {
      return "empty";
    }

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
   * Determine SimpleDateFormat pattern matching with the given date string. Returns null if format is unknown. You can simply extend DateUtil with
   * more formats if needed.<br>
   * https://stackoverflow.com/a/3390252
   * 
   * @param dateString
   *          The date string to determine the SimpleDateFormat pattern for.
   * @return The matching SimpleDateFormat pattern, or null if format is unknown.
   * @see SimpleDateFormat
   */
  public static String determineDateFormat(String dateString) throws ParseException {
    for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
      if (dateString.toLowerCase(Locale.ROOT).matches(regexp)) {
        return DATE_FORMAT_REGEXPS.get(regexp);
      }
    }
    return null; // Unknown format.
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

    String format = determineDateFormat(dateAsString);
    if (format != null) {
      date = new SimpleDateFormat(format).parse(dateAsString);
    }
    else {
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
    StringBuilder result = new StringBuilder();
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
   * basic ascii equivalents. Originally written by Jens Hausherr (https://github.com/jabbrwcky), modified by Manuel Laggner
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
      // https://stackoverflow.com/questions/9376621/folding-normalizing-ligatures-e-g-%C3%86-to-ae-using-corefoundation

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
              else {
                result.append(c);
              }
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
      UPPER = ucReplacement;
      LOWER = lcReplacement;
    }

    Replacement(String caseInsensitiveReplacement) {
      this(caseInsensitiveReplacement, caseInsensitiveReplacement);
    }
  }

  /**
   * Returns the common name of title/originaltitle when it is named sortable <br>
   * eg "Bourne Legacy, The" -> "The Bourne Legacy".
   * 
   * @param title
   *          the title
   * @return the original title
   */
  public static String removeCommonSortableName(String title) {
    if (title == null || title.isEmpty()) {
      return "";
    }
    for (String prfx : COMMON_TITLE_PREFIXES) {
      String delim = " "; // one spaces as delim
      if (prfx.matches(".*['`´]$")) { // ends with hand-picked delim, so no
                                      // space between prefix and title
        delim = "";
      }
      title = title.replaceAll("(?i)(.*), " + prfx, prfx + delim + "$1");
    }
    return title.trim();
  }

  /**
   * compares the given version (v1) against another one (v2)<br>
   * Special case:<br>
   * if we have SNAPSHOT, SVN or GIT version, and both are the same, return -1
   * 
   * @param v1
   *          given version
   * @param v2
   *          other version
   * @return < 0 if v1 is lower<br>
   *         > 0 if v1 is higher<br>
   *         = 0 if equal
   */
  public static int compareVersion(String v1, String v2) {
    if (v1.contains("-SNAPSHOT") && v1.equals(v2) || v1.equals("SVN") || v1.equals("GIT")) {
      // we have the same snapshot version - consider as potential lower (for nightly)
      // same for GIT - always "lower" to trigger update scripts!
      return -1;
    }
    String s1 = normalisedVersion(v1);
    String s2 = normalisedVersion(v2);
    return s1.compareTo(s2);
  }

  private static String normalisedVersion(String version) {
    return normalisedVersion(version, ".", 4);
  }

  private static String normalisedVersion(String version, String sep, int maxWidth) {
    // SNAPSHOT should be considered as lower version
    // so just removing does not work
    // add micromicro version to other
    if (!version.contains("-SNAPSHOT")) {
      version += ".0.0.1";
    }
    else {
      version = version.replace("-SNAPSHOT", "");
    }

    String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
    StringBuilder sb = new StringBuilder();
    for (String s : split) {
      sb.append(String.format("%" + maxWidth + 's', s));
    }
    return sb.toString();
  }

  public static String getLongestString(String[] array) {
    int maxLength = 0;
    String longestString = null;
    for (String s : array) {
      if (s.length() > maxLength) {
        maxLength = s.length();
        longestString = s;
      }
    }
    return longestString;
  }

  /**
   * check the given String not to be null - returning always a not null String
   * 
   * @param originalString
   *          the string to be checked
   * @return the originalString or an empty String
   */
  public static String getNonNullString(String originalString) {
    if (originalString == null) {
      return "";
    }
    return originalString;
  }

  /**
   * normalizes the given {@link String}
   * 
   * @param original
   *          the {@link String} to normalize
   * @return the normalized {@link String}
   */
  public static String normalizeString(String original) {
    String nfdNormalizedString = Normalizer.normalize(original, Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    return pattern.matcher(nfdNormalizedString).replaceAll("");
  }
}
