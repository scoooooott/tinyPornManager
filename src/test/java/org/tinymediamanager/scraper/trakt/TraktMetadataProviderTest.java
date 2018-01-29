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
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaType;

public class TraktMetadataProviderTest {

  @BeforeClass
  public static void setUp() {
    // ProxySettings.setProxySettings("localhost", 3128, "", "");
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
      assertThat(md.getTagline()).isNotEmpty();
      assertThat(md.getPlot()).isNotEmpty();
      assertThat(md.getYear()).isEqualTo(2001);
      assertThat(md.getReleaseDate()).isInSameDayAs("2001-11-16");
      assertThat(md.getRatings().size()).isEqualTo(1);
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVoteCount()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);
      assertThat(md.getRuntime()).isGreaterThan(0);
      assertThat(md.getCertifications()).containsOnly(Certification.US_PG);
      assertThat(md.getGenres()).containsOnly(MediaGenres.ADVENTURE, MediaGenres.FANTASY, MediaGenres.FAMILY);

      // ids
      assertThat(md.getId(md.getProviderId())).isEqualTo(545);
      assertThat(md.getId(MediaMetadata.IMDB)).isEqualTo("tt0241527");
      assertThat(md.getId(MediaMetadata.TMDB)).isEqualTo(671);
      // assertThat(md.getMediaArt(MediaArtwork.MediaArtworkType.POSTER)).isNotEmpty();

      // crew
      assertThat(md.getCastMembers()).isNotEmpty();
      MediaCastMember castMember = md.getCastMembers(MediaCastMember.CastType.ACTOR).get(0);
      assertThat(castMember.getName()).isNotEmpty();
      assertThat(castMember.getCharacter()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

  @Test
  public void testTvShowEpisodeList() {
    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
    TraktMetadataProvider mp = new TraktMetadataProvider();

    // Game of Thrones
    options.setId(mp.getProviderInfo().getId(), "1390");
    List<MediaMetadata> episodeList;
    MediaMetadata episode;

    try {
      episodeList = mp.getEpisodeList(options);

      assertThat(episodeList).isNotEmpty();
      assertThat(episodeList.get(0)).isNotNull();

      episode = episodeList.get(0);
      assertThat(episode.getTitle()).isNotEmpty();
      assertThat(episode.getPlot()).isNotNull(); // can be empty for some eps
      assertThat(episode.getIds()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowScrape() {
    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
    TraktMetadataProvider mp = new TraktMetadataProvider();

    // Game of Thrones
    options.setId(mp.getProviderInfo().getId(), "1390");

    try {
      MediaMetadata md = mp.getMetadata(options);

      assertThat(md).isNotNull();
      assertThat(md.getTitle()).isEqualTo("Game of Thrones");
      assertThat(md.getYear()).isEqualTo(2011);
      assertThat(md.getPlot()).isNotEmpty();
      // assertThat(md.getReleaseDate()).isInSameDayAs("2011-04-17");
      assertThat(md.getRuntime()).isGreaterThanOrEqualTo(55);
      assertThat(md.getProductionCompanies()).containsOnly("HBO");
      assertThat(md.getCertifications()).containsOnly(Certification.US_TVMA);
      assertThat(md.getCountries()).containsOnly("us");
      assertThat(md.getStatus()).isEqualTo("returning series");
      assertThat(md.getRatings().size()).isEqualTo(1);
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVoteCount()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);

      assertThat(md.getGenres()).containsOnly(MediaGenres.DRAMA, MediaGenres.FANTASY, MediaGenres.SCIENCE_FICTION, MediaGenres.ACTION,
          MediaGenres.ADVENTURE);

      // ids
      assertThat(md.getId(TraktMetadataProvider.providerInfo.getId())).isEqualTo(1390);
      assertThat(md.getId(MediaMetadata.TVDB)).isEqualTo(121361);
      assertThat(md.getId(MediaMetadata.IMDB)).isEqualTo("tt0944947");
      assertThat(md.getId(MediaMetadata.TMDB)).isEqualTo(1399);

      // crew
      assertThat(md.getCastMembers()).isNotEmpty();
      MediaCastMember castMember = md.getCastMembers(MediaCastMember.CastType.ACTOR).get(0);
      assertThat(castMember.getName()).isNotEmpty();
      assertThat(castMember.getCharacter()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowEpisodeScrape() {
    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
    TraktMetadataProvider mp = new TraktMetadataProvider();

    // Game of Thrones
    options.setId(mp.getProviderInfo().getId(), "1390");
    options.setId(MediaMetadata.SEASON_NR, "1");
    options.setId(MediaMetadata.EPISODE_NR, "1");

    try {
      MediaMetadata md = mp.getMetadata(options);

      assertThat(md).isNotNull();
      assertThat(md.getTitle()).isEqualTo("Winter Is Coming");
      assertThat(md.getPlot()).isNotEmpty();
      assertThat(md.getReleaseDate()).isInSameDayAs("2011-04-18");
      assertThat(md.getRatings().size()).isEqualTo(1);
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVoteCount()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);

      // ids
      assertThat(md.getId(TraktMetadataProvider.providerInfo.getId())).isEqualTo(73640);
      assertThat(md.getId(MediaMetadata.TVDB)).isEqualTo(3254641);
      assertThat(md.getId(MediaMetadata.IMDB)).isEqualTo("tt1480055");
      assertThat(md.getId(MediaMetadata.TMDB)).isEqualTo(63056);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
