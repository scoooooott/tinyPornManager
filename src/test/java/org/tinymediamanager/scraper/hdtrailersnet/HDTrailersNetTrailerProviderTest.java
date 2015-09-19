package org.tinymediamanager.scraper.hdtrailersnet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieTrailerProvider;

public class HDTrailersNetTrailerProviderTest {
  private static final String CRLF = "\n";

  @BeforeClass
  public static void setUp() {
    StringBuilder config = new StringBuilder("handlers = java.util.logging.ConsoleHandler\n");
    config.append(".level = ALL").append(CRLF);
    config.append("java.util.logging.ConsoleHandler.level = ALL").append(CRLF);
    // Only works with Java 7 or later
    config.append("java.util.logging.SimpleFormatter.format = [%1$tH:%1$tM:%1$tS %4$6s] %2$s - %5$s %6$s%n")
        .append(CRLF);
    // Exclude http logging
    config.append("sun.net.www.protocol.http.HttpURLConnection.level = OFF").append(CRLF);
    InputStream ins = new ByteArrayInputStream(config.toString().getBytes());
    try {
      LogManager.getLogManager().readConfiguration(ins);
    }
    catch (IOException ignored) {
    }
  }

  @Test
  public void testScrapeTrailer() {
    IMovieTrailerProvider mp;
    try {
      mp = new HDTrailersNetTrailerProvider();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);

      MediaMetadata md = new MediaMetadata("foo");
      md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, "Iron Man 3");
      options.setMetadata(md);

      List<MediaTrailer> trailers = mp.getTrailers(options);
      assertThat(trailers).isNotNull().isNotEmpty();
      
      MediaTrailer trailer = trailers.get(0);
      assertThat(trailer.getName()).isNotEmpty();
      assertThat(trailer.getUrl()).isNotEmpty();
      assertThat(trailer.getProvider()).isNotEmpty();
      assertThat(trailer.getQuality()).isNotEmpty();
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
