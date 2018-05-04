package org.tinymediamanager.scraper.thetvdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaAiredStatus;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;

public class TheTvDbMetadataProviderTest {

  @Test
  public void testSearch() throws Exception {
    ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();

    searchShow(metadataProvider, "Un village français", "fr", "211941", 2009);
    searchShow(metadataProvider, "Der Mondbár", "de", "81049", 2007);
    searchShow(metadataProvider, "Psych", "en", "79335", 2006);
    searchShow(metadataProvider, "You're the Worst", "en", "281776", 2014);
    searchShow(metadataProvider, "America's Book of Secrets", "en", "256002", 2012);
    searchShow(metadataProvider, "Rich Man, Poor Man", "en", "77151", 1976);
    searchShow(metadataProvider, "Drugs, Inc", "en", "174501", 2010);
    searchShow(metadataProvider, "Yu-Gi-Oh!", "en", "113561", 1998);
    searchShow(metadataProvider, "What's the Big Idea?", "en", "268282", 2013);
    searchShow(metadataProvider, "Wallace & Gromit", "en", "78996", 1989);
    searchShow(metadataProvider, "SOKO Kitzbühel", "de", "101241", 2001);
  }

  @Test
  public void testSearchWithFallback() throws Exception {
    ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();
    metadataProvider.getProviderInfo().getConfig().setValue("fallbackLanguage", MediaLanguages.en.toString());
    searchShow(metadataProvider, "Wonderfalls", "de", "78845", 2004);

    metadataProvider.getProviderInfo().getConfig().setValue("fallbackLanguage", MediaLanguages.de.toString());
    searchShow(metadataProvider, "SOKO Kitzbühel", "en", "101241", 2001);
  }

  private void searchShow(ITvShowMetadataProvider metadataProvider, String title, String language, String id, int year) {
    try {
      MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW, title);
      options.setLanguage(Locale.forLanguageTag(language));

      List<MediaSearchResult> results = metadataProvider.search(options);
      if (results.isEmpty()) {
        Assert.fail("Result empty!");
      }

      MediaSearchResult result = results.get(0);
      assertThat(result.getTitle()).isNotEmpty();
      assertThat(result.getId()).isEqualTo(id);
      assertThat(result.getYear()).isEqualTo(year);
      assertThat(result.getPosterUrl()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowScrape() {
    /*
     * Psych (79335)
     */
    try {
      ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setId(metadataProvider.getProviderInfo().getId(), "79335");
      options.setCountry(CountryCode.US);
      options.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));
      MediaMetadata md = metadataProvider.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertThat(md.getIds().size()).isGreaterThanOrEqualTo(2); // at least tvdb and imdb
      assertEquals("Psych", md.getTitle());
      assertEquals(
          "Thanks to his police officer father's efforts, Shawn Spencer spent his childhood developing a keen eye for detail (and a lasting dislike of his dad).  Years later, Shawn's frequent tips to the police lead to him being falsely accused of a crime he solved.  Now, Shawn has no choice but to use his abilities to perpetuate his cover story: psychic crime-solving powers, all the while dragging his best friend, his dad, and the police along for the ride.",
          md.getPlot());
      assertEquals(2006, md.getYear());

      assertThat(md.getRatings()).isNotEmpty();
      MediaRating rating = md.getRatings().get(0);
      assertThat(rating.getId()).isEqualTo("tvdb");
      assertThat(rating.getMaxValue()).isEqualTo(10);
      assertThat(rating.getRating()).isGreaterThan(0);
      assertThat(rating.getVoteCount()).isGreaterThan(0);

      assertEquals(MediaAiredStatus.ENDED, md.getStatus());
      assertThat(md.getProductionCompanies()).isNotEmpty();
      assertEquals(Certification.US_TVPG, md.getCertifications().get(0));
      assertThat(md.getGenres()).containsExactly(MediaGenres.COMEDY, MediaGenres.CRIME, MediaGenres.DRAMA);
      assertThat(md.getRuntime()).isGreaterThan(0);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowScrapeWithFallback() {
    /*
     * Psych (79335)
     */
    try {
      ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();
      metadataProvider.getProviderInfo().getConfig().setValue("fallbackLanguage", MediaLanguages.de.toString());

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setId(metadataProvider.getProviderInfo().getId(), "79335");
      options.setCountry(CountryCode.US);
      options.setLanguage(LocaleUtils.toLocale(MediaLanguages.tr.name()));
      MediaMetadata md = metadataProvider.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertThat(md.getIds().size()).isGreaterThanOrEqualTo(2); // at least tvdb and imdb
      assertEquals("Psych", md.getTitle());
      assertEquals(
          "Shawn Spencer ist selbsternannter Detektiv. Von seinem Vater Henry, einem angesehenen Polizisten, wurde er trainiert, sich alle Dinge in seinem Umfeld genau einzuprägen, seien sie auch noch so klein oder unwichtig. Über seine Erziehung unzufrieden kehrte Shawn seinem Vater jedoch den Rücken. Nach einigen misslungenen Lebensabschnitten erkennt er seine Gabe, ungelöste Fälle der Polizei mithilfe seines fotografischen Gedächtnisses lösen zu können. Dabei gibt Shawn aber stets vor ein Hellseher zu sein. Nachdem er der Polizei in mehreren Fällen helfen konnte und diese ihn immer wieder als Unterstützung anfordert, gründet Shawn schließlich mit seinem Freund Burton Guster eine eigene Detektei.",
          md.getPlot());
      assertEquals(2006, md.getYear());

      assertThat(md.getRatings()).isNotEmpty();
      MediaRating rating = md.getRatings().get(0);
      assertThat(rating.getId()).isEqualTo("tvdb");
      assertThat(rating.getMaxValue()).isEqualTo(10);
      assertThat(rating.getRating()).isGreaterThan(0);
      assertThat(rating.getVoteCount()).isGreaterThan(0);

      assertEquals(MediaAiredStatus.ENDED, md.getStatus());
      assertThat(md.getProductionCompanies()).isNotEmpty();
      assertEquals(Certification.US_TVPG, md.getCertifications().get(0));
      assertThat(md.getGenres()).containsExactly(MediaGenres.COMEDY, MediaGenres.CRIME, MediaGenres.DRAMA);
      assertThat(md.getRuntime()).isGreaterThan(0);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeScrape() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    /*
     * Psych (79335)
     */
    try {
      ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setId(metadataProvider.getProviderInfo().getId(), "79335");
      options.setCountry(CountryCode.US);
      options.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));
      options.setId(MediaMetadata.SEASON_NR, "1");
      options.setId(MediaMetadata.EPISODE_NR, "2");
      options.setArtworkType(MediaArtwork.MediaArtworkType.ALL);
      MediaMetadata md = metadataProvider.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertThat(md.getIds().size()).isGreaterThanOrEqualTo(1); // at least tvdb
      assertThat(md.getEpisodeNumber()).isEqualTo(2);
      assertThat(md.getSeasonNumber()).isEqualTo(1);
      assertThat(md.getDvdEpisodeNumber()).isEqualTo(2);
      assertThat(md.getDvdSeasonNumber()).isEqualTo(1);
      assertThat(md.getTitle()).isEqualTo("The Spelling Bee");
      assertThat(md.getPlot()).startsWith("When what begins as a little competitive sabotage in a regional spelling");
      assertEquals("14-07-2006", sdf.format(md.getReleaseDate()));
      assertEquals(18, md.getCastMembers(MediaCastMember.CastType.ACTOR).size());
      assertThat(md.getCastMembers(MediaCastMember.CastType.DIRECTOR).size()).isGreaterThan(0);
      assertThat(md.getCastMembers(MediaCastMember.CastType.WRITER).size()).isGreaterThan(0);
      assertThat(md.getMediaArt(MediaArtwork.MediaArtworkType.THUMB)).isNotEmpty();

      assertThat(md.getRatings()).isNotEmpty();
      MediaRating rating = md.getRatings().get(0);
      assertThat(rating.getId()).isEqualTo("tvdb");
      assertThat(rating.getMaxValue()).isEqualTo(10);
      assertThat(rating.getRating()).isGreaterThan(0);
      assertThat(rating.getVoteCount()).isGreaterThan(0);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeScrapeWithFallback() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    /*
     * Psych (79335)
     */
    try {
      ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();
      metadataProvider.getProviderInfo().getConfig().setValue("fallbackLanguage", MediaLanguages.de.toString());

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setId(metadataProvider.getProviderInfo().getId(), "79335");
      options.setCountry(CountryCode.US);
      options.setLanguage(LocaleUtils.toLocale(MediaLanguages.tr.name()));
      options.setId(MediaMetadata.SEASON_NR, "1");
      options.setId(MediaMetadata.EPISODE_NR, "2");
      options.setArtworkType(MediaArtwork.MediaArtworkType.ALL);
      MediaMetadata md = metadataProvider.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertThat(md.getIds().size()).isGreaterThanOrEqualTo(1); // at least tvdb
      assertThat(md.getEpisodeNumber()).isEqualTo(2);
      assertThat(md.getSeasonNumber()).isEqualTo(1);
      assertThat(md.getDvdEpisodeNumber()).isEqualTo(2);
      assertThat(md.getDvdSeasonNumber()).isEqualTo(1);
      assertThat(md.getTitle()).isEqualTo("So spannend kann ein Buchstabierwettbewerb sein!");
      assertThat(md.getPlot()).startsWith(
          "In Santa Barbara findet der alljährliche Buchstabier-Wettbewerb statt. Gus, der als Knirps selbst einmal an dieser Veranstaltung teilgenommen hatte, aber ausgeschieden war, weil der im Publikum sitzende Shawn ihm falsch vorgesagt hatte, ist vor Begeisterung kaum zu halten und verfolgt die Veranstaltung mangels Tickets als Live-Übertragung im Fernsehen. Shawn dagegen hält die Veranstaltung für eine reine Freakshow und ist von Gus' Enthusiasmus mehr als genervt. Als Brendan Vu, der haushohe Favorit der Veranstaltung, auf Grund eines mysteriösen Ohnmachtsanfalles ausscheiden muss, werden Shawn und Gus mit der Untersuchung des Falles beauftragt. Kurz nachdem sie am Veranstaltungsort eintreffen, stürzt der altgediente Spielleiter des Buchstabierwettbewerbes, Elvin Cavanaugh, bewusstlos aus seiner Loge in das Publikum und ist auf der Stelle tot. Für die Polizei, insbesondere den zynischen Detective Carlton Lassiter scheint dieser Fall sonnenklar zu sein: Der stark übergewichtige Cavanaugh habe einen Herzinfarkt erlitten und sei deshalb in den Tod gestürzt. Lassiters neue Kollegin Juliet O'Hara, auf die Shawn sofort ein Auge wirft, zweifelt jedoch an dieser Theorie. Auch Shawn und Gus vermuten mehr dahinter, für sie deuten die Zeichen eindeutig auf ein Fremdverschulden hin.");
      assertEquals("14-07-2006", sdf.format(md.getReleaseDate()));
      assertEquals(18, md.getCastMembers(MediaCastMember.CastType.ACTOR).size());
      assertThat(md.getCastMembers(MediaCastMember.CastType.DIRECTOR).size()).isGreaterThan(0);
      assertThat(md.getCastMembers(MediaCastMember.CastType.WRITER).size()).isGreaterThan(0);
      assertThat(md.getMediaArt(MediaArtwork.MediaArtworkType.THUMB)).isNotEmpty();

      assertThat(md.getRatings()).isNotEmpty();
      MediaRating rating = md.getRatings().get(0);
      assertThat(rating.getId()).isEqualTo("tvdb");
      assertThat(rating.getMaxValue()).isEqualTo(10);
      assertThat(rating.getRating()).isGreaterThan(0);
      assertThat(rating.getVoteCount()).isGreaterThan(0);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testArtworkScrape() {
    /*
     * Psych (79335)
     */
    try {
      ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();
      ITvShowArtworkProvider artworkProvider = (ITvShowArtworkProvider) metadataProvider;

      // all scrape
      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setId(metadataProvider.getProviderInfo().getId(), "79335");
      options.setArtworkType(MediaArtwork.MediaArtworkType.ALL);

      List<MediaArtwork> artwork = artworkProvider.getArtwork(options);
      assertThat(artwork).isNotEmpty();

      MediaArtwork ma = artwork.get(0);
      assertThat(ma.getDefaultUrl()).isNotEmpty();
      assertThat(ma.getType()).isIn(MediaArtwork.MediaArtworkType.BANNER, MediaArtwork.MediaArtworkType.POSTER,
          MediaArtwork.MediaArtworkType.BACKGROUND, MediaArtwork.MediaArtworkType.SEASON_POSTER, MediaArtwork.MediaArtworkType.SEASON_BANNER);
      assertThat(ma.getImageSizes()).isNotEmpty();

      // season poster scrape
      options.setArtworkType(MediaArtwork.MediaArtworkType.SEASON_POSTER);

      artwork = artworkProvider.getArtwork(options);
      assertThat(artwork).isNotEmpty();

      ma = artwork.get(0);
      assertThat(ma.getDefaultUrl()).isNotEmpty();
      assertThat(ma.getType()).isEqualTo(MediaArtwork.MediaArtworkType.SEASON_POSTER);
      assertThat(ma.getSeason()).isGreaterThan(-1);

      // season banner scrape
      options.setArtworkType(MediaArtwork.MediaArtworkType.SEASON_BANNER);

      artwork = artworkProvider.getArtwork(options);
      assertThat(artwork).isNotEmpty();

      ma = artwork.get(0);
      assertThat(ma.getDefaultUrl()).isNotEmpty();
      assertThat(ma.getType()).isEqualTo(MediaArtwork.MediaArtworkType.SEASON_BANNER);
      assertThat(ma.getSeason()).isGreaterThan(-1);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeListScrape() {
    /*
     * Psych (79335)
     */
    try {
      ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setId(metadataProvider.getProviderInfo().getId(), "79335");
      options.setCountry(CountryCode.US);
      options.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));
      options.setArtworkType(MediaArtwork.MediaArtworkType.ALL);
      List<MediaMetadata> episodes = metadataProvider.getEpisodeList(options);

      // did we get metadata?
      assertNotNull("episodes", episodes);

      assertThat(episodes.size()).isEqualTo(126);

      MediaMetadata episode = episodes.get(9);
      assertThat(episode.getEpisodeNumber()).isEqualTo(2);
      assertThat(episode.getSeasonNumber()).isEqualTo(1);
      assertThat(episode.getDvdEpisodeNumber()).isEqualTo(2);
      assertThat(episode.getDvdSeasonNumber()).isEqualTo(1);
      assertThat(episode.getTitle()).isEqualTo("The Spelling Bee");
      assertThat(episode.getPlot()).startsWith("When what begins as a little competitive sabotage in a regional spelling");
      assertThat(episode.getReleaseDate()).isEqualTo("2006-07-14");

    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeListScrapeWithFallback() {
    /*
     * Psych (79335)
     */
    try {
      ITvShowMetadataProvider metadataProvider = new TheTvDbMetadataProvider();
      metadataProvider.getProviderInfo().getConfig().setValue("fallbackLanguage", MediaLanguages.de.toString());

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setId(metadataProvider.getProviderInfo().getId(), "79335");
      options.setCountry(CountryCode.US);
      options.setLanguage(LocaleUtils.toLocale(MediaLanguages.tr.name()));
      options.setArtworkType(MediaArtwork.MediaArtworkType.ALL);
      List<MediaMetadata> episodes = metadataProvider.getEpisodeList(options);

      // did we get metadata?
      assertNotNull("episodes", episodes);

      assertThat(episodes.size()).isEqualTo(126);

      MediaMetadata episode = episodes.get(9);
      assertThat(episode.getEpisodeNumber()).isEqualTo(2);
      assertThat(episode.getSeasonNumber()).isEqualTo(1);
      assertThat(episode.getDvdEpisodeNumber()).isEqualTo(2);
      assertThat(episode.getDvdSeasonNumber()).isEqualTo(1);
      assertThat(episode.getTitle()).isEqualTo("So spannend kann ein Buchstabierwettbewerb sein!");
      assertThat(episode.getPlot()).startsWith(
          "In Santa Barbara findet der alljährliche Buchstabier-Wettbewerb statt. Gus, der als Knirps selbst einmal an dieser Veranstaltung teilgenommen hatte, aber ausgeschieden war, weil der im Publikum sitzende Shawn ihm falsch vorgesagt hatte, ist vor Begeisterung kaum zu halten und verfolgt die Veranstaltung mangels Tickets als Live-Übertragung im Fernsehen. Shawn dagegen hält die Veranstaltung für eine reine Freakshow und ist von Gus' Enthusiasmus mehr als genervt. Als Brendan Vu, der haushohe Favorit der Veranstaltung, auf Grund eines mysteriösen Ohnmachtsanfalles ausscheiden muss, werden Shawn und Gus mit der Untersuchung des Falles beauftragt. Kurz nachdem sie am Veranstaltungsort eintreffen, stürzt der altgediente Spielleiter des Buchstabierwettbewerbes, Elvin Cavanaugh, bewusstlos aus seiner Loge in das Publikum und ist auf der Stelle tot. Für die Polizei, insbesondere den zynischen Detective Carlton Lassiter scheint dieser Fall sonnenklar zu sein: Der stark übergewichtige Cavanaugh habe einen Herzinfarkt erlitten und sei deshalb in den Tod gestürzt. Lassiters neue Kollegin Juliet O'Hara, auf die Shawn sofort ein Auge wirft, zweifelt jedoch an dieser Theorie. Auch Shawn und Gus vermuten mehr dahinter, für sie deuten die Zeichen eindeutig auf ein Fremdverschulden hin.");
      assertThat(episode.getReleaseDate()).isEqualTo("2006-07-14");
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}