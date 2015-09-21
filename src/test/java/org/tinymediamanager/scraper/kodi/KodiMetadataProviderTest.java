package org.tinymediamanager.scraper.kodi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaCastMember.CastType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;

public class KodiMetadataProviderTest {
  private static final String CRLF = "\n";

  @Test
  public void loadXbmcScrapers() {
    try {
      KodiMetadataProvider kodiMetadataProvider = new KodiMetadataProvider();
      List<IMediaProvider> movieScrapers = kodiMetadataProvider.getPluginsForType(MediaType.MOVIE);
      assertThat(movieScrapers).isNotNull().isNotEmpty();
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTmdbScraper() {
    try {
      KodiMetadataProvider kodiMetadataProvider = new KodiMetadataProvider();
      List<IMediaProvider> movieScrapers = kodiMetadataProvider.getPluginsForType(MediaType.MOVIE);
      assertThat(movieScrapers).isNotNull().isNotEmpty();

      IMovieMetadataProvider tmdb = null;
      for (IMediaProvider mp : movieScrapers) {
        if (mp.getProviderInfo().getId().equals("metadata.themoviedb.org")) {
          tmdb = (IMovieMetadataProvider) mp;
          break;
        }
      }

      assertThat(tmdb).isNotNull();

      // search
      MediaSearchOptions searchOptions = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY,
          "Harry Potter and the Philosopher's Stone");
      searchOptions.set(SearchParam.YEAR, "2001");
      searchOptions.set(SearchParam.LANGUAGE, "en");
      List<MediaSearchResult> results = tmdb.search(searchOptions);

      assertThat(results).isNotNull();
      assertThat(results.size()).isGreaterThan(0);

      // scrape
      MediaScrapeOptions scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE);
      scrapeOptions.setResult(results.get(0));
      MediaMetadata md = tmdb.getMetadata(scrapeOptions);

      assertEquals("Harry Potter and the Philosopher's Stone", md.getStringValue(MediaMetadata.TITLE));
      assertEquals("Harry Potter and the Philosopher's Stone", md.getStringValue(MediaMetadata.ORIGINAL_TITLE));
      assertEquals("2001", md.getStringValue(MediaMetadata.YEAR));
      assertEquals(
          "Harry Potter has lived under the stairs at his aunt and uncle's house his whole life. But on his 11th birthday, he learns he's a powerful wizard -- with a place waiting for him at the Hogwarts School of Witchcraft and Wizardry. As he learns to harness his newfound powers with the help of the school's kindly headmaster, Harry uncovers the truth about his parents' deaths -- and about the villain who's to blame.",
          md.getStringValue(MediaMetadata.PLOT));
      assertEquals(new Integer(152), md.getIntegerValue(MediaMetadata.RUNTIME));
      assertEquals("Let the Magic Begin.", md.getStringValue(MediaMetadata.TAGLINE));
      assertEquals("Harry Potter Collection", md.getStringValue(MediaMetadata.COLLECTION_NAME));

      assertNotNull(md.getCastMembers(CastType.ACTOR));
      assertThat(md.getCastMembers(CastType.ACTOR).size()).isGreaterThan(0);
      assertThat(md.getCastMembers(CastType.ACTOR).get(0).getName()).isNotEmpty();
      assertThat(md.getCastMembers(CastType.ACTOR).get(0).getCharacter()).isNotEmpty();

      assertNotNull(md.getCastMembers(CastType.DIRECTOR));
      assertThat(md.getCastMembers(CastType.DIRECTOR).size()).isGreaterThan(0);
      assertThat(md.getCastMembers(CastType.DIRECTOR).get(0).getName()).isNotEmpty();
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
