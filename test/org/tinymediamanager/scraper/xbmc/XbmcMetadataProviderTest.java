package org.tinymediamanager.scraper.xbmc;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;

public class XbmcMetadataProviderTest {

  @Test
  public void testXbmcScraper() {

    // ********************************************************************************
    // detect XBMC folder or use local folder
    File addons = new File(Utils.detectXbmcUserdataFolder(), "addons");
    if (addons == null || !addons.exists()) {
      addons = new File("xbmc_scraper");
      if (!addons.exists()) {
        System.out.println("Meh - could not find any scrapers...");
        System.exit(0);
      }
    }
    System.out.println("Loading scrapers from: " + addons);

    // ********************************************************************************
    // find scraper XMLs
    File[] files = addons.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return new File(dir, name).isDirectory() && name.toLowerCase().startsWith("metadata") && !name.toLowerCase().contains("common");
      }
    });
    if (files.length == 0) {
      System.out.println("Meh - could not find any scrapers...");
      System.exit(0);
    }
    for (File file : files) {
      System.out.println("found scraper: " + file);
    }

    // ********************************************************************************
    // parse XML and all common ones
    String providerFolder = "metadata.themoviedb.org";
    // String providerFolder = "metadata.imdb.com";
    XbmcMetadataProvider mp = new XbmcMetadataProvider(new File(addons, providerFolder));

    try {
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
      MediaMetadata mm = mp.getMetaData(msr);

      // TODO: XML quite fine (see ***BEGIN/END XML ***) - todo output parsing; remove "double details"?
      System.out.println(mm);
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to Load XBMC Scraper: " + providerFolder, e);
    }

  }
}
