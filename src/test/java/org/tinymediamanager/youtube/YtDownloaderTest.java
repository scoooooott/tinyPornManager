package org.tinymediamanager.youtube;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.util.youtube.model.MediaDetails;
import org.tinymediamanager.scraper.util.youtube.model.YoutubeMedia;

import java.io.IOException;


public class YtDownloaderTest {

  private static final String videoId = "8BeLUQRomPA"; // ANGEL HAS FALLEN Trailer German Deutsch
  private static YoutubeMedia media = new YoutubeMedia(videoId);



  @Test
  public void A_ParsingDataTest() throws IOException, InterruptedException {
    media.parseVideo();
    MediaDetails details = media.getDetails();

    //Video Details
    Assert.assertEquals(details.getVideoId(), "8BeLUQRomPA");
    Assert.assertEquals(details.getTitle(), "ANGEL HAS FALLEN Trailer German Deutsch (2019) Exklusiv");
    Assert.assertEquals(details.getAuthor(), "KinoCheck");
    Assert.assertEquals(details.getLengthSeconds(), 79);

  }
}
