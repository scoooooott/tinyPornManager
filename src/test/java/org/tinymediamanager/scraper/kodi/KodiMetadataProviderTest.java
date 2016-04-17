package org.tinymediamanager.scraper.kodi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaCastMember.CastType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;

public class KodiMetadataProviderTest {
  private static final String CRLF = "\n";

  @Test
  public void xmlHeaders() {
    Assert.assertFalse(KodiUtil
        .fixXmlHeader("<?xml version=\"1.0\" test=\"false\" encoding=\"UTF-8\"?>\n<scraper framework=\"1.1\" date=\"2012-01-16\">").contains("test"));
    Assert.assertFalse(KodiUtil.fixXmlHeader("<?xml version=\'1.0\' encoding=\"UTF-8\" gzip=\"yes\" standalone=\"yes\" ?>").contains("gzip"));
    Assert.assertFalse(
        KodiUtil.fixXmlHeader("<?xml version=\"1.0\' encoding=\"UTF-8\" gzip=\"yes\"?><result key=\"true\">key=\"true\"</result>").contains("gzip"));
    Assert.assertFalse(KodiUtil.fixXmlHeader("<?xml version=\"1.0\' encoding=\"UTF-8\" asdf=yes?>").contains("asdf"));
    Assert.assertEquals("", KodiUtil.fixXmlHeader(""));
  }

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
      MediaSearchOptions searchOptions = new MediaSearchOptions(MediaType.MOVIE, "Harry Potter and the Philosopher's Stone");
      searchOptions.setYear(2001);
      searchOptions.setLanguage(Locale.ENGLISH);
      List<MediaSearchResult> results = tmdb.search(searchOptions);

      assertThat(results).isNotNull();
      assertThat(results.size()).isGreaterThan(0);

      // scrape
      MediaScrapeOptions scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE);
      scrapeOptions.setResult(results.get(0));
      MediaMetadata md = tmdb.getMetadata(scrapeOptions);

      assertEquals("Harry Potter and the Philosopher's Stone", md.getTitle());
      assertEquals("Harry Potter and the Philosopher's Stone", md.getOriginalTitle());
      assertEquals("2001", md.getYear());
      assertEquals(
          "Harry Potter has lived under the stairs at his aunt and uncle's house his whole life. But on his 11th birthday, he learns he's a powerful wizard -- with a place waiting for him at the Hogwarts School of Witchcraft and Wizardry. As he learns to harness his newfound powers with the help of the school's kindly headmaster, Harry uncovers the truth about his parents' deaths -- and about the villain who's to blame.",
          md.getPlot());
      assertEquals(152, md.getRuntime());
      assertEquals("Let the Magic Begin.", md.getTagline());
      assertEquals("Harry Potter Collection", md.getCollectionName());

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
