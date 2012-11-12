package org.tinymediamanager.scraper.imdb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImdbUtils {
  public static final String IMDB_TITLE_URL = "http://%s/title/%s/";

  public static String parseIMDBID(String url) {
    if (url == null)
      return null;
    Pattern p = Pattern.compile("/(tt[0-9]+)/");
    Matcher m = p.matcher(url);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  public static String parseUserRating(String ur) {
    if (ur == null)
      return null;
    Pattern p = Pattern.compile("([0-9\\.]+)\\s*/\\s*10");
    Matcher m = p.matcher(ur);
    if (m.find()) {
      ur = m.group(1);
    }

    try {
      float f = Float.parseFloat(ur);
      ur = String.valueOf((int) f);
    } catch (Exception e) {
      // not a number, just return it
    }

    return ur;
  }

  public static String createDetailUrl(String imdbid) {
    // IMDBConfiguration cfg = new IMDBConfiguration();
    return String.format(IMDB_TITLE_URL, "akas.imdb.com", imdbid);
  }
}
