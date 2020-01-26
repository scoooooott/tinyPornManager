package org.tinymediamanager.youtube;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.util.youtube.model.MediaDetails;
import org.tinymediamanager.scraper.util.youtube.model.YoutubeMedia;


public class ITYtDownloaderTest {

  private static final String videoId = "I0iBBzyiRes"; // ANGEL HAS FALLEN Trailer German Deutsch
  private static YoutubeMedia media = new YoutubeMedia(videoId);



  @Test
  public void A_ParsingDataTest() throws Exception {
    String videoId = "8BeLUQRomPA";   // ANGEL HAS FALLEN Trailer German Deutsch
    YoutubeMedia media = new YoutubeMedia(videoId);
    media.parseVideo();
    MediaDetails details = media.getDetails();

    //Video Details
    Assert.assertEquals(details.getVideoId(), "8BeLUQRomPA");
    Assert.assertEquals(details.getTitle(), "ANGEL HAS FALLEN Trailer German Deutsch (2019) Exklusiv");
    Assert.assertEquals(details.getAuthor(), "KinoCheck");
    Assert.assertEquals(details.getLengthSeconds(), 79);

  }

  @Test
  public void A_ParsingDataTestSignature() throws Exception {
    String videoId = "kJQP7kiw5Fk";   // ANGEL HAS FALLEN Trailer German Deutsch
    YoutubeMedia media = new YoutubeMedia(videoId);
    media.parseVideo();
    MediaDetails details = media.getDetails();

    //Video Details
    Assert.assertEquals(details.getVideoId(), "kJQP7kiw5Fk");
    Assert.assertEquals(details.getTitle(), "Luis Fonsi - Despacito ft. Daddy Yankee");

  }
}
