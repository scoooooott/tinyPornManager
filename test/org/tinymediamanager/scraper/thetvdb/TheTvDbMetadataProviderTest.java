package org.tinymediamanager.scraper.thetvdb;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;

public class TheTvDbMetadataProviderTest {

  TheTvDbMetadataProvider mp = null;

  @Test
  public void testSearch() {
    try {
      mp = new TheTvDbMetadataProvider();

      searchShow("Der Mondbar", "de", "81049");
      searchShow("Psych", "en", "79335");
      searchShow("You're the Worst", "en", "281776");
      searchShow("America's Book of Secrets", "en", "256002");
      searchShow("Rich Man, Poor Man", "en", "77151");
      searchShow("Drugs, Inc", "en", "174501");
      searchShow("Yu-Gi-Oh!", "en", "113561");
      searchShow("What's the Big Idea?", "en", "268282");

    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  private void searchShow(String title, String language, String id) throws Exception {
    MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW);

    options.set(SearchParam.TITLE, title);
    options.set(SearchParam.LANGUAGE, language);

    List<MediaSearchResult> results = mp.search(options);
    if (results.isEmpty() || !id.equals(results.get(0).getId())) {
      Assert.fail();
    }
  }
}
