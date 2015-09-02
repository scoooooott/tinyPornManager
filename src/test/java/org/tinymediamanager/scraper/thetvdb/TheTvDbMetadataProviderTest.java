package org.tinymediamanager.scraper.thetvdb;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.LogManager;

public class TheTvDbMetadataProviderTest {
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
  public void testSearch() {
    searchShow("Un village français", "fr", "211941");
    searchShow("Der Mondbár", "de", "81049");
    searchShow("Psych", "en", "79335");
    searchShow("You're the Worst", "en", "281776");
    searchShow("America's Book of Secrets", "en", "256002");
    searchShow("Rich Man, Poor Man", "en", "77151");
    searchShow("Drugs, Inc", "en", "174501");
    searchShow("Yu-Gi-Oh!", "en", "113561");
    searchShow("What's the Big Idea?", "en", "268282");
  }

  private void searchShow(String title, String language, String id) {
    ITvShowMetadataProvider mp;

    try {
      mp = new TheTvDbMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW);

      options.set(SearchParam.TITLE, title);
      options.set(SearchParam.LANGUAGE, language);

      List<MediaSearchResult> results = mp.search(options);
      if (results.isEmpty()) {
        Assert.fail("Result empty!");
      }
      if (!id.equals(results.get(0).getId())) {
        Assert.fail("ID not as expected! expected: " + id + " was: " + results.get(0).getId());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }
}
