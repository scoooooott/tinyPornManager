package org.tinymediamanager.scraper.opensubtitles;

import org.junit.Test;

public class OpensubtitlesMetadataProviderTest {

  @Test
  public void test() {
    OpensubtitlesMetadataProvider os = new OpensubtitlesMetadataProvider();
    try {
      os.getServerInfo();
      // os.searchSubtitles(new MediaFile());
      os.checkMovieHash("d7aa0275cace4410");
      os.closeSession();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
