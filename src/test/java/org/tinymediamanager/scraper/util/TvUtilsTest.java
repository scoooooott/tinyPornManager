package org.tinymediamanager.scraper.util;

import org.junit.Assert;
import org.junit.Test;

public class TvUtilsTest {

  @Test
  public void testEpisodeNumber() {
    Assert.assertEquals(-1, TvUtils.getEpisodeNumber(0)); // 0 episode not possible
    Assert.assertEquals(-1, TvUtils.getEpisodeNumber((Object[]) null));

    Assert.assertEquals(2, TvUtils.getEpisodeNumber(new Double(2.2)));
    Assert.assertEquals(2, TvUtils.getEpisodeNumber(new Float(2.2)));
    Assert.assertEquals(2, TvUtils.getEpisodeNumber(new Integer(2)));
    Assert.assertEquals(2, TvUtils.getEpisodeNumber("2"));
    Assert.assertEquals(2, TvUtils.getEpisodeNumber(2));

    Assert.assertEquals(2, TvUtils.getEpisodeNumber("", null, -1, 0, new Float(-2.2), new Double(-2.2), 2));
  }

  @Test
  public void testSeasonNumber() {
    Assert.assertEquals(0, TvUtils.getSeasonNumber(0)); // 0 season IS possible
    Assert.assertEquals(-1, TvUtils.getSeasonNumber((Object[]) null));

    Assert.assertEquals(2, TvUtils.getSeasonNumber(new Double(2.2)));
    Assert.assertEquals(2, TvUtils.getSeasonNumber(new Float(2.2)));
    Assert.assertEquals(2, TvUtils.getSeasonNumber(new Integer(2)));
    Assert.assertEquals(2, TvUtils.getSeasonNumber("2"));
    Assert.assertEquals(2, TvUtils.getSeasonNumber(2));

    Assert.assertEquals(2, TvUtils.getSeasonNumber("", null, -1, 2, new Float(-2.2), new Double(-2.2), 0));
  }

}
