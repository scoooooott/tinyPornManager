package org.tinymediamanager.core;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

  // own method to get some logging ;)
  public static void assertEqual(Object expected, Object actual) {
    try {
      Assert.assertEquals(expected, actual);
      System.out.println(expected + " - passed");
    }
    catch (AssertionError e) {
      System.err.println(expected + " - FAILED: " + e.getMessage());
      throw e;
    }
  }

  @Test
  public void map() {
    HashMap<String, Object> ids = new HashMap<String, Object>(0);
    ids.put("STR1", "str1");
    ids.put("STR2", "1000");
    ids.put("INT1", Integer.parseInt("1000"));
    ids.put("DUPE", "2000");
    ids.put("DUPE", 1000);
    for (String s : ids.keySet()) {
      Object o = ids.get(s);
      System.out.println(s + "  " + o + "  " + (o instanceof String ? "String" : "") + (o instanceof Integer ? "Integer" : ""));
    }
  }

  @Test
  public void getSortableName() {
    assertEqual("Dark Knight, The", Utils.getSortableName("The Dark Knight"));
    assertEqual("Dark Knight, The", Utils.getSortableName("tHE Dark Knight"));
    assertEqual("hard days night, A", Utils.getSortableName("a hard days night"));
    assertEqual("Die Hard", Utils.getSortableName("Die Hard")); // wohoo
    assertEqual("Die Hard 2", Utils.getSortableName("Die Hard 2")); // wohoo
    assertEqual("Die Hard: with a", Utils.getSortableName("Die Hard: with a")); // wohoo
    assertEqual("Good Day to Die Hard, A", Utils.getSortableName("A Good Day to Die Hard")); // wohoo
    assertEqual("Hardyboys, Die", Utils.getSortableName("Die Hardyboys"));
    assertEqual("Team, A", Utils.getSortableName("A Team"));
    assertEqual("A-Team", Utils.getSortableName("A-Team"));
    assertEqual("A!Team", Utils.getSortableName("A!Team"));
    assertEqual("Âge de Glace, L'", Utils.getSortableName("L' Âge de Glace"));
    assertEqual("Âge de Glace, L'", Utils.getSortableName("L'Âge de Glace"));
    assertEqual("'Âge de Glace, L'", Utils.getSortableName("L''Âge de Glace"));
    assertEqual("Âge de Glace, L´", Utils.getSortableName("L´ Âge de Glace"));
    assertEqual("Âge de Glace, L`", Utils.getSortableName("L` Âge de Glace"));
    assertEqual("Âge de Glace, L´", Utils.getSortableName("L´Âge de Glace"));
    assertEqual("Âge de Glace, L`", Utils.getSortableName("L`Âge de Glace"));
  }

  @Test
  public void removeSortableName() {
    assertEqual("The Dark Knight", Utils.removeSortableName("Dark Knight, The"));
    assertEqual("The Dark Knight", Utils.removeSortableName("Dark Knight, tHE"));
    assertEqual("A hard days night", Utils.removeSortableName("hard days night, a"));
    assertEqual("Die Hard", Utils.removeSortableName("Die Hard"));
    assertEqual("L'Âge de Glace", Utils.removeSortableName("Âge de Glace, L'"));
    assertEqual("L`Âge de Glace", Utils.removeSortableName("Âge de Glace, L`"));
    assertEqual("L´Âge de Glace", Utils.removeSortableName("Âge de Glace, L´"));
  }

  @Test
  public void detectStackingMarkers() {
    assertEqual("Easy A", FilenameUtils.getBaseName(Utils.cleanStackingMarkers("Easy A.avi"))); // not a stacking format!

    assertEqual("", Utils.getStackingMarker("2 Guns (2013) x264-720p DTS-6ch.mkv"));

    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd1.mkv"), "cd1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-PaRt1.mkv"), "PaRt1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013) DvD1.mkv"), "DvD1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013).disk3.mkv"), "disk3");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd 1.mkv"), "cd 1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-PaRt 1.mkv"), "PaRt 1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013) DvD 1.mkv"), "DvD 1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013).disk 3.mkv"), "disk 3");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd1.mkv"), "cd1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-1of2.mkv"), "1of2");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-1 of 2.mkv"), "1 of 2");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-(1 of 2).mkv"), "1 of 2");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-(1-2).mkv"), ""); // nah
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-1.mkv"), ""); // do not detect - could be prequel in MMD
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-2.mkv"), ""); // do not detect - could be sequel in MMD
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-a.mkv"), "a");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-b.mkv"), "b");
    assertEqual(Utils.getStackingMarker("Movie Name Part 4 (2013).mkv"), ""); // no inbetween matching
    assertEqual(Utils.getStackingMarker("Movie Name CD 1 (2013).mkv"), ""); // no inbetween matching
    assertEqual(Utils.getStackingMarker("Movie Name 3of4 (2013).mkv"), ""); // no inbetween matching
    assertEqual(Utils.getStackingMarker("Movie Name Part 4 (2013)-Part 1.mkv"), "Part 1"); // no inbetween matching, but ending matching

    assertEqual(Utils.getFolderStackingMarker("Movie Name (2013)-dvd1"), "dvd1"); // folder - check without extension

    assertEqual(Utils.getStackingNumber("Movie Name (2013)-cd1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-PaRt1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013) DvD1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013).disk3.mkv"), 3);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-cd 1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-PaRt 1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013) DvD 1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013).disk 3.mkv"), 3);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-cd1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-1of2.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-1 of 2.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-(1 of 2).mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-(1-2).mkv"), 0); // nah
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-1.mkv"), 0); // nah
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-2.mkv"), 0); // nah
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-a.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-b.mkv"), 2);

    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-cd1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-PaRt1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013) DvD1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013).disk3.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-cd 1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-PaRt 1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-part a.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013) DvD 1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013).disk 3.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-cd1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-1of2.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-1 of 2.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-(1 of 2).mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-(1-2).mkv"), "Movie Name (2013)-(1-2).mkv"); // nah
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-1.mkv"), "Movie Name (2013)-1.mkv"); // nah
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-2.mkv"), "Movie Name (2013)-2.mkv"); // nah
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-a.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-b.mkv"), "Movie Name (2013).mkv");

  }

  @Test
  public void backup() {
    Utils.createBackupFile(new File("movies.db"));
    Utils.deleteOldBackupFile(new File("movies.db"), 15);
  }

  @Test
  public void env() {
    Map<String, String> env = System.getenv();
    for (String envName : env.keySet()) {
      System.out.format("%s=%s%n", envName, env.get(envName));
    }
    Properties props = System.getProperties();
    Enumeration e = props.propertyNames();

    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      System.out.println(key + " -- " + props.getProperty(key));
    }
  }

  @Test
  public void locale() {
    for (String s : Locale.getISOLanguages()) {
      Locale l = new Locale(s);
      System.out.println(l.getISO3Language());
    }
    System.out.println();
    for (String s : Utils.KEY_TO_LOCALE_MAP.keySet()) {
      System.out.println(s + " - " + Utils.KEY_TO_LOCALE_MAP.get(s));
    }
  }

  @Test
  public void testReplacement() {
    assertEqual("Test done", Utils.replacePlaceholders("Test {}", new String[] { "done" }));
    assertEqual("Test ", Utils.replacePlaceholders("Test {}", new String[] {}));
    assertEqual("Test one two three", Utils.replacePlaceholders("Test {} {} {}", new String[] { "one", "two", "three" }));
    assertEqual("Test with empty spaces", Utils.replacePlaceholders("Test {} with {}{}empty spaces", new String[] {}));
  }
}
