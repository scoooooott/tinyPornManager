package org.tinymediamanager.scraper.trakt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.http.ProxySettings;

public class TraktMetadataProviderTest {

  @BeforeClass
  public static void setUp() {
    ProxySettings.setProxySettings("localhost", 3128, "", "");
  }

  @Test
  public void testMovieSearch() {
    TraktMetadataProvider mp;
    List<MediaSearchResult> results;

    // Harry Potter and the Philosopher's Stone
    try {
      mp = new TraktMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE, "Harry Potter and the Philosopher's Stone");
      options.setLanguage(Locale.ENGLISH);
      results = mp.search(options);

      // did we get a result?
      assertThat(results).isNotNull().isNotEmpty();
      // are there all fields filled in the result?
      MediaSearchResult result = results.get(0);
      assertThat(result.getTitle()).isNotEmpty();
      assertThat(result.getYear()).isEqualTo(2001);
      assertThat(result.getId()).isEqualTo("545");
      assertThat(result.getScore()).isGreaterThan(0);
      assertThat(result.getIMDBId()).isEqualTo("tt0241527");
      assertThat(result.getProviderId()).isNotEmpty();
      assertThat(result.getPosterUrl()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testMovieScrape() {

    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
    TraktMetadataProvider mp = new TraktMetadataProvider();
    options.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));
    options.setId(mp.getProviderInfo().getId(), "545"); // Harry Potter and the Philosopher's Stone

    try {
      /**
       * Harry Potter and the Philosopher's Stone
       */
      MediaMetadata md = mp.getMetadata(options);
      assertNotNull(md);
      assertThat(md.getTitle()).isEqualTo("Harry Potter and the Philosopher's Stone");
      assertThat(md.getOriginalTitle()).isEqualTo("Harry Potter and the Philosopher's Stone");
      assertThat(md.getPlot()).isNotEmpty();
      assertThat(md.getYear()).isEqualTo(2001);
      assertThat(md.getRating()).isGreaterThan(0);
      assertThat(md.getVoteCount()).isGreaterThan(0);
      assertThat(md.getRuntime()).isGreaterThan(0);
      assertThat(md.getId(md.getProviderId())).isEqualTo(545);
      assertThat(md.getId(MediaMetadata.IMDB)).isEqualTo("tt0241527");
      assertThat(md.getId(MediaMetadata.TMDB)).isEqualTo(671);
      // assertThat(md.getMediaArt(MediaArtwork.MediaArtworkType.POSTER)).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

  @Test
  // Game of Thrones
  public void testTVShowEpisodeList() {
    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
    TraktMetadataProvider mp = new TraktMetadataProvider();
    options.setId(mp.getProviderInfo().getId(), "1390");
    List<MediaEpisode> episodeList;
    MediaEpisode episode;

    try {
      episodeList = mp.getEpisodeList(options);

      assertThat(episodeList).isNotEmpty();
      assertThat(episodeList.get(0)).isNotNull();

      episode = episodeList.get(0);
      assertThat(episode.title).isNotEmpty();
      assertThat(episode.plot).isNotNull(); // can be empty for some eps
      assertThat(episode.ids).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
