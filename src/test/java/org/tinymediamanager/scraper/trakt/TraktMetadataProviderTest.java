package org.tinymediamanager.scraper.trakt;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class TraktMetadataProviderTest {
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
  public void testMovieSearch() {
    TraktMetadataProvider mp = null;
    List<MediaSearchResult> results = null;

    // Harry Potter
    try {
      mp = new TraktMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY,
          "Harry Potter and the Philosopher's Stone");
      options.set(MediaSearchOptions.SearchParam.LANGUAGE, "en");
      results = mp.search(options);

      // did we get a result?
      assertThat(results).isNotNull().isNotEmpty();

      // are there all fields filled in the result?
      MediaSearchResult result = results.get(0);

      assertThat(result.getTitle()).isNotEmpty();
      assertThat(result.getYear()).isNotEmpty();
      assertThat(result.getPosterUrl()).isNotEmpty();
      assertThat(result.getId()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
