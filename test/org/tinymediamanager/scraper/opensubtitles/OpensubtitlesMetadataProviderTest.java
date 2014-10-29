package org.tinymediamanager.scraper.opensubtitles;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.util.SubtitleUtils;

public class OpensubtitlesMetadataProviderTest {

  @Test
  public void test() {
    OpensubtitlesMetadataProvider os = new OpensubtitlesMetadataProvider();
    try {
      // os.getServerInfo();
      File f = new File("D:\\_neu\\filme\\test.avi");
      String hash = SubtitleUtils.computeOpenSubtitlesHash(f);
      System.out.println(hash);

      List<MediaSearchResult> msr = os.identify(new MediaFile(f));
      for (MediaSearchResult mediaSearchResult : msr) {
        System.out.println("Found result: " + mediaSearchResult);
      }

      // List<MediaSearchResult> msr = os.search(new MediaFile(f));
      // os.checkMovieHash2(hash);
      // os.checkMovieHash("d7aa0275cace4410");
      os.closeSession();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}
