package org.tinymediamanager.core;

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
  public void testReplacement() {
    Assert.assertEquals("Test done", Utils.replacePlaceholders("Test {}", new String[] { "done" }));
    Assert.assertEquals("Test ", Utils.replacePlaceholders("Test {}", new String[] {}));
    Assert.assertEquals("Test one two three", Utils.replacePlaceholders("Test {} {} {}", new String[] { "one", "two", "three" }));
    Assert.assertEquals("Test with empty spaces", Utils.replacePlaceholders("Test {} with {}{}empty spaces", new String[] {}));
  }
}
