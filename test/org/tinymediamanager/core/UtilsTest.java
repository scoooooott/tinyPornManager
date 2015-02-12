package org.tinymediamanager.core;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

  @Test
  public void map() {
    HashMap<String, Object> ids = new HashMap<String, Object>(0);
    ids.put("STR1", "str1");
    ids.put("STR2", "1000");
    ids.put("INT1", Integer.valueOf("1000"));
    ids.put("DUPE", "2000");
    ids.put("DUPE", Integer.valueOf("1000").intValue());
    for (String s : ids.keySet()) {
      Object o = ids.get(s);
      System.out.println(s + "  " + o + "  " + (o instanceof String ? "String" : "") + (o instanceof Integer ? "Integer" : ""));
    }
  }

  @Test
  public void getSortableName() {
    Assert.assertEquals("Dark Knight, The", Utils.getSortableName("The Dark Knight"));
    Assert.assertEquals("Dark Knight, The", Utils.getSortableName("tHE Dark Knight"));
    Assert.assertEquals("hard days night, A", Utils.getSortableName("a hard days night"));
    Assert.assertEquals("Die Hard", Utils.getSortableName("Die Hard")); // wohoo
    Assert.assertEquals("Die Hard 2", Utils.getSortableName("Die Hard 2")); // wohoo
    Assert.assertEquals("Die Hard: with a", Utils.getSortableName("Die Hard: with a")); // wohoo
    Assert.assertEquals("Good Day to Die Hard, A", Utils.getSortableName("A Good Day to Die Hard")); // wohoo
    Assert.assertEquals("Hardyboys, Die", Utils.getSortableName("Die Hardyboys"));
    Assert.assertEquals("Team, A", Utils.getSortableName("A Team"));
    Assert.assertEquals("A-Team", Utils.getSortableName("A-Team"));
    Assert.assertEquals("A!Team", Utils.getSortableName("A!Team"));
    Assert.assertEquals("Âge de Glace, L'", Utils.getSortableName("L' Âge de Glace"));
    Assert.assertEquals("Âge de Glace, L'", Utils.getSortableName("L'Âge de Glace"));
    Assert.assertEquals("'Âge de Glace, L'", Utils.getSortableName("L''Âge de Glace"));
    Assert.assertEquals("Âge de Glace, L´", Utils.getSortableName("L´ Âge de Glace"));
    Assert.assertEquals("Âge de Glace, L`", Utils.getSortableName("L` Âge de Glace"));
    Assert.assertEquals("Âge de Glace, L´", Utils.getSortableName("L´Âge de Glace"));
    Assert.assertEquals("Âge de Glace, L`", Utils.getSortableName("L`Âge de Glace"));
  }

  @Test
  public void removeSortableName() {
    Assert.assertEquals("The Dark Knight", Utils.removeSortableName("Dark Knight, The"));
    Assert.assertEquals("The Dark Knight", Utils.removeSortableName("Dark Knight, tHE"));
    Assert.assertEquals("A hard days night", Utils.removeSortableName("hard days night, a"));
    Assert.assertEquals("Die Hard", Utils.removeSortableName("Die Hard"));
    Assert.assertEquals("L'Âge de Glace", Utils.removeSortableName("Âge de Glace, L'"));
    Assert.assertEquals("L`Âge de Glace", Utils.removeSortableName("Âge de Glace, L`"));
    Assert.assertEquals("L´Âge de Glace", Utils.removeSortableName("Âge de Glace, L´"));
  }

  @Test
  public void detectStackingMarkers() {
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-cd1.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-PaRt1.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013) DvD1.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013).disk3.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-cd 1.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-PaRt 1.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013) DvD 1.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013).disk 3.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-cd1.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-1of2.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-1 of 2.mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-(1 of 2).mkv"));
    System.out.println(Utils.getStackingMarker("Movie Name (2013)-(1-2).mkv"));
    System.out.println("end");
  }

  @Test
  public void backup() {
    Utils.createBackupFile(new File("tmm.odb"));
    Utils.deleteOldBackupFile(new File("tmm.odb"), 15);
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
    Assert.assertEquals("Test done", Utils.replacePlaceholders("Test {}", new String[] { "done" }));
    Assert.assertEquals("Test ", Utils.replacePlaceholders("Test {}", new String[] {}));
    Assert.assertEquals("Test one two three", Utils.replacePlaceholders("Test {} {} {}", new String[] { "one", "two", "three" }));
    Assert.assertEquals("Test with empty spaces", Utils.replacePlaceholders("Test {} with {}{}empty spaces", new String[] {}));
  }
}
