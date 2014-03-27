package org.tinymediamanager.scraper.thesubdb;

import org.junit.Test;

public class SubDBMetadataProviderTest {

  @Test
  public void testSubDBScraper() throws Exception {
    TheSubDbMetadataProvider mp = new TheSubDbMetadataProvider();
    mp.getLanguages();
    // mp.search(new File("C:\\Users\\Markus\\Downloads\\justified.mp4"));
  }
}
