package org.tinymediamanager.scraper.zelluloid;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.zelluloid.ZelluloidMetadataProvider;

public class ZelluloidMetadataProviderTest {

  @Test
  public void testZelluloidScraper() {
    ZelluloidMetadataProvider mp = null;

    mp = new ZelluloidMetadataProvider();

    try {
      // MediaSearchOptions op = new MediaSearchOptions();
      // op.set(MediaSearchOptions.SearchParam.IMDBID, "tt0258463");
      // op.set(MediaSearchOptions.SearchParam.TITLE, "bourne");
      // mp.search(op);

      MediaSearchResult msr = new MediaSearchResult("zelluloid");
      msr.setId("7614");
      msr.setUrl("http://www.zelluloid.de/filme/index.php3?id=7614");

      MediaScrapeOptions scop = new MediaScrapeOptions();
      scop.setResult(msr);
      MediaMetadata mmd = mp.getMetadata(scop);
      System.out.println(mmd.toString());

    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
