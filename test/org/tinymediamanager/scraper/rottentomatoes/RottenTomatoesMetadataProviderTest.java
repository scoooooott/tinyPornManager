package org.tinymediamanager.scraper.rottentomatoes;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;

public class RottenTomatoesMetadataProviderTest {

  @Test
  public void testSearch() {
    try {
      IMediaMetadataProvider rt = new RottenTomatoesMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE);

      options.set(SearchParam.QUERY, "12 Monkeys");

      List<MediaSearchResult> results = rt.search(options);
      assertEquals(2, results.size());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testScrape() throws Exception {
    try {
      IMediaMetadataProvider rt = new RottenTomatoesMetadataProvider();

      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setId(Constants.ROTTENTOMATOESID, "15508");
      MediaMetadata md = rt.getMetadata(options);
      assertNotNull("MediaMetadata", md);
      assertEquals("title", "Twelve Monkeys (12 Monkeys)", md.getStringValue(MediaMetadata.TITLE));
      assertEquals("year", "1995", md.getStringValue(MediaMetadata.YEAR));
      assertEquals("rating", 8.8d, md.getDoubleValue(MediaMetadata.RATING), 0.2d);
      assertEquals("plot", "", md.getStringValue(MediaMetadata.PLOT));
      assertEquals("production company", "Universal Pictures", md.getStringValue(MediaMetadata.PRODUCTION_COMPANY));
      assertEquals("imdbid", "tt0114746", md.getId(MediaMetadata.IMDBID));
      assertEquals("runtime", 130, (int) md.getIntegerValue(MediaMetadata.RUNTIME));

      assertEquals("genres", 3, md.getGenres().size());
      assertEquals("Drama", true, md.getGenres().contains(MediaGenres.DRAMA));
      assertEquals("Fantasy", true, md.getGenres().contains(MediaGenres.FANTASY));
      assertEquals("Science Fiction", true, md.getGenres().contains(MediaGenres.SCIENCE_FICTION));

      assertEquals("genres", 6, md.getCastMembers().size());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
