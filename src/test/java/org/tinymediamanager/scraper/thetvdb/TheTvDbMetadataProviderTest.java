package org.tinymediamanager.scraper.thetvdb;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;

public class TheTvDbMetadataProviderTest {

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
    searchShow("Wallace & Gromit", "en", "78996");
    searchShow("SOKO Kitzbühel", "de", "101241");
  }

  private void searchShow(String title, String language, String id) {
    ITvShowMetadataProvider mp;

    try {
      mp = new TheTvDbMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW);

      options.set(SearchParam.QUERY, title);
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
