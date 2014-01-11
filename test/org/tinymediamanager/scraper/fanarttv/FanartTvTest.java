package org.tinymediamanager.scraper.fanarttv;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;

public class FanartTvTest {

  private static FanartTvMetadataProvider ftv = null;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ftv = new FanartTvMetadataProvider();
  }

  @Test
  public void test() throws Exception {
    MediaScrapeOptions o = new MediaScrapeOptions();
    o.setType(MediaType.TV_SHOW);
    o.setTmdbId(49040);
    o.setImdbId("tt1194173");

    List<MediaArtwork> ma = ftv.getArtwork(o);
    System.out.println(ma.get(0).getDefaultUrl());

  }
}
