package org.tinymediamanager.scraper.util;

import org.junit.Assert;
import org.junit.Test;

public class StrgUtilsTest {

  @Test
  public void testCompareVersion() {
    Assert.assertTrue(StrgUtils.compareVersion("2.7", "2.7-SNAPSHOT") > 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7-SNAPSHOT", "2.7") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7-SNAPSHOT", "2.7.1") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7.1", "2.7.2-SNAPSHOT") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.6.9", "2.7-SNAPSHOT") < 0);
    Assert.assertTrue(StrgUtils.compareVersion("2.7.1-SNAPSHOT", "2.7.2-SNAPSHOT") < 0);
  }

}
