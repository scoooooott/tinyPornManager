package org.tinymediamanager.scraper.thesubdb;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.MediaSearchResult;

public class SubDBMetadataProviderTest {

  @Test
  public void testSubDBScraper() throws Exception {
    TheSubDbMetadataProvider mp = new TheSubDbMetadataProvider();
    // mp.getLanguages();
    List<MediaSearchResult> res = mp.search(new MediaFile(new File("C:\\Users\\Markus\\Downloads\\justified.mp4")));
  }
}
