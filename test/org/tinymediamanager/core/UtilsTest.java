package org.tinymediamanager.core;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

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
  }

  @Test
  public void removeSortableName() {
    Assert.assertEquals("The Dark Knight", Utils.removeSortableName("Dark Knight, The"));
    Assert.assertEquals("The Dark Knight", Utils.removeSortableName("Dark Knight, tHE"));
    Assert.assertEquals("A hard days night", Utils.removeSortableName("hard days night, a"));
    Assert.assertEquals("Die Hard", Utils.removeSortableName("Die Hard"));
  }

  @Test
  public void testLoc() {
    Assert.assertEquals("German", Utils.getDisplayLanguage("deu"));
    Assert.assertEquals("German", Utils.getDisplayLanguage("AUT"));
    Assert.assertEquals("German", Utils.getDisplayLanguage("GER"));
    Assert.assertEquals("German", Utils.getDisplayLanguage("ger"));
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
    Set<String> langArray = Utils.KEY_TO_LOCALE_MAP.keySet();
    System.out.println(langArray);
  }

  @Test
  public void testReplacement() {
    Assert.assertEquals("Test done", Utils.replacePlaceholders("Test {}", new String[] { "done" }));
    Assert.assertEquals("Test ", Utils.replacePlaceholders("Test {}", new String[] {}));
    Assert.assertEquals("Test one two three", Utils.replacePlaceholders("Test {} {} {}", new String[] { "one", "two", "three" }));
    Assert.assertEquals("Test with empty spaces", Utils.replacePlaceholders("Test {} with {}{}empty spaces", new String[] {}));
  }
}
