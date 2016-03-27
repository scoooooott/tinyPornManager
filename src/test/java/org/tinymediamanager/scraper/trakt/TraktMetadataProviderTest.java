package org.tinymediamanager.scraper.trakt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;

public class TraktMetadataProviderTest {
  private static final String CRLF = "\n";

  @BeforeClass
  public static void setUp() {
    StringBuilder config = new StringBuilder("handlers = java.util.logging.ConsoleHandler\n");
    config.append(".level = ALL").append(CRLF);
    config.append("java.util.logging.ConsoleHandler.level = ALL").append(CRLF);
    // Only works with Java 7 or later
    config.append("java.util.logging.SimpleFormatter.format = [%1$tH:%1$tM:%1$tS %4$6s] %2$s - %5$s %6$s%n").append(CRLF);
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
    // Movie Search
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
      assertThat(result.getId()).isNotEmpty();
      assertThat(result.getScore()).isNotNull();
      assertThat(result.getIMDBId()).isNotNull();
      assertThat(result.getProviderId()).isNotNull();
      assertThat(result.getPosterUrl()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testMovieScrape() {

    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
    TraktMetadataProvider mp = new TraktMetadataProvider();
    options.setId(mp.getProviderInfo().getId(), "521");

    try {
      MediaMetadata md = mp.getMetadata(options);
      assertNotNull(md);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

  }

  @Test
  // Game of Thrones
  public void testTVShowScrape() {
    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
    TraktMetadataProvider mp = new TraktMetadataProvider();
    options.setId(mp.getProviderInfo().getId(), "353");
    List<MediaEpisode> episodeList = new ArrayList<MediaEpisode>();
    MediaEpisode test = null;

    try {

      episodeList = mp.getEpisodeList(options);

      assertThat(episodeList.get(0)).isNotNull();

      test = episodeList.get(0);

      assertThat(test.title).isNotNull();
      assertThat(test.plot).isNotNull();
      assertThat(test.ids).isNotNull();

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
