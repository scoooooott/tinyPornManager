package org.tinymediamanager.scraper.ofdb;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;

public class OfdbMetadataProviderTest {

  @Test
  public void testOfdbScraper() {
    OfdbMetadataProvider mp = null;

    mp = new OfdbMetadataProvider();

    try {
      // MediaSearchOptions op = new MediaSearchOptions();
      // op.set(MediaSearchOptions.SearchParam.IMDBID, "tt0258463");
      // op.set(MediaSearchOptions.SearchParam.TITLE, "bourne");
      // mp.search(op);

      MediaSearchResult msr = new MediaSearchResult("ofdb");
      msr.setIMDBId("tt1194173");
      msr.setUrl("http://www.ofdb.de/film/226745,Das-Bourne-Verm&auml;chtnis");

      MediaScrapeOptions scop = new MediaScrapeOptions();
      scop.setResult(msr);
      mp.getMetadata(scop);

    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }
}
