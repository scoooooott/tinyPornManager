package org.tinymediamanager.core;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class VersionComparatorTest {
  @Test
  public void testVersionOrder() {
    Assert.assertEquals(true, compare("2.5", "2.5.1") < 0);
    Assert.assertEquals(true, compare("2.5", "2.5.2") < 0);
    Assert.assertEquals(true, compare("2.5.1", "2.5.1") == 0);
    Assert.assertEquals(true, compare("2.5.2", "2.5") > 0);
    Assert.assertEquals(true, compare("2.5", "2.4.7") > 0);

    Assert.assertEquals(true, compare("2.1", "2.5") < 0);
    Assert.assertEquals(true, compare("2.4.7", "2.5.1") < 0);
    Assert.assertEquals(true, compare("2.5", "2.5.2") < 0);

    Assert.assertEquals(true, compare("2.9", "2.10") < 0);
    Assert.assertEquals(true, compare("2.5.9", "2.5.10") < 0);
  }

  private int compare(String v1, String v2) {
    String s1 = normalisedVersion(v1);
    String s2 = normalisedVersion(v2);
    return s1.compareTo(s2);
  }

  private String normalisedVersion(String version) {
    return normalisedVersion(version, ".", 4);
  }

  private String normalisedVersion(String version, String sep, int maxWidth) {
    String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
    StringBuilder sb = new StringBuilder();
    for (String s : split) {
      sb.append(String.format("%" + maxWidth + 's', s));
    }
    return sb.toString();
  }
}
