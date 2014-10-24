package org.tinymediamanager.scraper.xbmc;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;

public class XbmcMetadataProviderTest {

  @Test
  public void loadXbmcScrapers() {
    for (XbmcScraper sc : XbmcUtil.getAllScrapers()) {
      try {
        XbmcMetadataProvider mp = new XbmcMetadataProvider(sc);
        System.out.println("SCRAPER: " + mp.scraper.getId());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void testXbmcScraper() {
    try {
      // ********************************************************************************
      // parse XML and all common ones
      String providerFolder = "metadata.themoviedb.org";
      // String providerFolder = "metadata.imdb.com";
      XbmcScraper scr = new XbmcScraper(new File(providerFolder));
      XbmcMetadataProvider mp = new XbmcMetadataProvider(scr);

      System.out.println("Pause 5 sec");
      Thread.sleep(5000);

      // create our searchresult
      MediaSearchResult msr = new MediaSearchResult("Spider Man 3");
      msr.setIMDBId("tt0413300");
      msr.setMediaType(MediaType.MOVIE);
      msr.setUrl("http://api.themoviedb.org/3/movie/559?api_key=6247670ec93f4495a36297ff88f7cd15&language=en");
      // msr.setUrl("http://www.imdb.com/title/tt0413300/");

      // ********************************************************************************
      // do it :)
      // MediaMetadata mm = mp.getMetaData(msr);

      // TODO: XML quite fine (see ***BEGIN/END XML ***) - todo output parsing; remove "double details"?
      // System.out.println(mm);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }
}
