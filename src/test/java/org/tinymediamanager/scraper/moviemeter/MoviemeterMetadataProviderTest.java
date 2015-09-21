package org.tinymediamanager.scraper.moviemeter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;

public class MoviemeterMetadataProviderTest {

  @Test
  public void testSearch() {
    try {
      IMovieMetadataProvider rt = new MovieMeterMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE);

      options.set(MediaSearchOptions.SearchParam.QUERY, "Avatar");

      List<MediaSearchResult> results = rt.search(options);
      assertEquals(3, results.size());
      for (MediaSearchResult result : results) {
        assertThat(result.getTitle()).isNotNull().isNotEmpty();
        assertThat(result.getId()).isNotNull().isNotEmpty();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testScrape() throws Exception {
    try {
      IMovieMetadataProvider rt = new MovieMeterMetadataProvider();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setId(rt.getProviderInfo().getId(), "17552");
      MediaMetadata md = rt.getMetadata(options);

      assertThat(md).isNotNull();

      assertThat(md.getStringValue(MediaMetadata.TITLE)).isEqualTo("Avatar");
      assertThat(md.getStringValue(MediaMetadata.YEAR)).isEqualTo("2009");
      assertThat(md.getDoubleValue(MediaMetadata.RATING)).isGreaterThan(0);
      assertThat(md.getStringValue(MediaMetadata.PLOT))
          .startsWith("Jake Sully (Sam Worthington) is een verlamde oorlogsveteraan in de toekomst, die met enkele");
      assertThat(md.getStringValue(MediaMetadata.PRODUCTION_COMPANY)).isEmpty();
      assertThat(md.getId(MediaMetadata.IMDB)).isEqualTo("tt0499549");
      assertThat(md.getIntegerValue(MediaMetadata.RUNTIME)).isEqualTo(162);
      assertThat(md.getGenres().size()).isEqualTo(2);
      assertThat(md.getCastMembers(MediaCastMember.CastType.ACTOR).size()).isEqualTo(3);
      assertThat(md.getCastMembers(MediaCastMember.CastType.DIRECTOR).size()).isEqualTo(1);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
