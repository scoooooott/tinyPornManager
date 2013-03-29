package org.tinymediamanager.core.tvshow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class EpisodeMatching {

  private String detectEpisode(String name) {
    // FIXME: patters quite fine, but second find should start AFTER complete first match, not inbetween
    Pattern regex = Pattern.compile("(?i)[epx_-]+(\\d{1,2})");
    // episode fixed to 2 chars
    Matcher m = regex.matcher(name);
    System.out.print(padRight(name + ": ", 40));
    String ret = "";
    while (m.find()) {
      int ep = 0;
      try {
        ep = Integer.parseInt(m.group(1));
      }
      catch (NumberFormatException nfe) {
        // can not happen from regex since we only come here with max 2 numeric chars
      }
      ret += " E:" + ep;
    }
    // parse XYY

    // parse Roman
    regex = Pattern.compile("(?i)(part|pt)[\\._]+([MDCLXVI]+)");
    m = regex.matcher(name);
    while (m.find()) {
      ret += " E:" + decodeRoman(m.group(2));
    }

    ret = ret.trim();
    System.out.println(ret);
    return ret;
  }

  @Test
  public void test() {
    // http://wiki.xbmc.org/index.php?title=Video_library/Naming_files/TV_shows
    // with season
    Assert.assertEquals("E:2", detectEpisode("name.s01e02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.s01.e02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.s1e2.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.s01_e02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.1x02.ext"));
    // Assert.assertEquals("E:2", detectEpisode("name.102.ext")); // TODO

    // without season
    Assert.assertEquals("E:2", detectEpisode("name.ep02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.ep_02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.part.II.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.pt.II.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.pt_II.ext"));

    // multi episode
    Assert.assertEquals("E:1 E:2", detectEpisode("name.s01e01.s01e02.ext"));
    // Assert.assertEquals("E:1 E:2",detectEpisode("name.s01e01.episode1.title.s01e02.episode2.title.ext"));
    Assert.assertEquals("E:1 E:2 E:3", detectEpisode("name.s01e01.s01e02.s01e03.ext"));
    // Assert.assertEquals("E:1 E:2", detectEpisode("name.1x01_1x02.ext"));

    Assert.assertEquals("E:1 E:2", detectEpisode("name.s01e01 1x02.ext"));

    Assert.assertEquals("E:1 E:2", detectEpisode("name.ep01.ep02.ext"));
    // multi episode short
    Assert.assertEquals("E:1 E:2", detectEpisode("name.s01e01e02.ext"));
    Assert.assertEquals("E:1 E:2 E:3", detectEpisode("name.s01e01-02-03.ext"));
    Assert.assertEquals("E:1 E:2", detectEpisode("name.1x01x02.ext"));
    Assert.assertEquals("E:1 E:2", detectEpisode("name.ep01_02.ext"));
    // multi episode mixed; weird, but valid :p
    Assert.assertEquals("E:1 E:2 E:3 E:4", detectEpisode("name.1x01e02_03-x-04.ext"));

    // split episode
    // TODO: detect split?
    detectEpisode("name.s01e01.1.ext");
    detectEpisode("name.s01e01a.ext");
    detectEpisode("name.1x01.1.ext");
    detectEpisode("name.1x01a.ext");
    detectEpisode("name.ep01.1.ext");
    detectEpisode("name.101.1.ext");
    detectEpisode("name.ep01a_01b.ext");
    detectEpisode("name.s01e01.1.s01e01.2.ext");
    detectEpisode("name.1x01.1x01.2.ext"); // (note this is (1x01.1)x(01.2) not (1x01).(1x01.2))

    // parseInt testing
    Assert.assertEquals("E:2", detectEpisode("name.s01e02435454715743435435554.ext"));

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
    for (int i = 0; i < uRoman.length() - 1; i++) { // loop over all but the last character
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

  public static String padRight(String s, int n) {
    return String.format("%1$-" + n + "s", s);
  }

  public static String padLeft(String s, int n) {
    return String.format("%1$" + n + "s", s);
  }
}
