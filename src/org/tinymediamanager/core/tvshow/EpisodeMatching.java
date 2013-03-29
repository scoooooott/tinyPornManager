package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpisodeMatching {

  public static class EpisodeMatchingResult {
    public int           season   = -1;
    public List<Integer> episodes = new ArrayList<Integer>();
  }

  public static EpisodeMatchingResult detectEpisode(File file) {
    EpisodeMatchingResult result = new EpisodeMatchingResult();

    // season detection
    Pattern regex = Pattern.compile("(?i)(s|season)[\\s]*(\\d{1,2})");
    Matcher m = regex.matcher(file.getAbsolutePath());
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

    // FIXME: pattern quite fine, but second find should start AFTER complete first match, not inbetween
    regex = Pattern.compile("(?i)[epx_-]+(\\d{1,2})"); // episode fixed to 2 chars
    m = regex.matcher(file.getName());
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
    // parse XYY

    // parse Roman
    regex = Pattern.compile("(?i)(part|pt)[\\._]+([MDCLXVI]+)");
    m = regex.matcher(file.getName());
    while (m.find()) {
      int ep = 0;
      ep = decodeRoman(m.group(2));
      if (ep > 0 && !result.episodes.contains(ep)) {
        result.episodes.add(ep);
      }
    }

    Collections.sort(result.episodes);
    return result;
  }

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
