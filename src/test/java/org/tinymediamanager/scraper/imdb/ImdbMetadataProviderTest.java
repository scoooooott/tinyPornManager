package org.tinymediamanager.scraper.imdb;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaCastMember.CastType;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.util.ProxySettings;

public class ImdbMetadataProviderTest {
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
  public void testMovieSearch() {
    ImdbMetadataProvider mp = null;
    List<MediaSearchResult> results = null;

    /*
     * test on akas.imdb.com - "9"
     */
    try {
      mp = new ImdbMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, "9");
      options.set(MediaSearchOptions.SearchParam.LANGUAGE, "en");
      results = mp.search(options);

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 40, results.size());

      // check first result (9 - 2009 - tt0472033)
      MediaSearchResult result = results.get(0);
      checkSearchResult("9", "2009", "tt0472033", result);

      // check second result (9 - 2002 - tt0443424)
      result = results.get(1);
      checkSearchResult("9", "2002", "tt0342012", result);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * test on www.imdb.com - "Inglorious Basterds"
     */
    try {
      mp = new ImdbMetadataProvider();
      results = mp.search(new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, "Inglorious Basterds"));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 5, results.size());

      // check first result (Inglourious Basterds - 2009 - tt0361748)
      MediaSearchResult result = results.get(0);
      checkSearchResult("Inglourious Basterds", "2009", "tt0361748", result);

      // check second result (The Real Inglorious Bastards - 2012 - tt3320110)
      result = results.get(1);
      checkSearchResult("The Real Inglorious Bastards", "2012", "tt3320110", result);

      // check third result (Inglourious Basterds: Movie Special - 2009 -
      // tt1515156)
      result = results.get(2);
      checkSearchResult("Inglourious Basterds: Movie Special", "2009", "tt1515156", result);

    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * test on www.imdb.com - "Asterix der Gallier" in de
     */
    try {
      mp = new ImdbMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, "Asterix der Gallier");
      options.set(MediaSearchOptions.SearchParam.LANGUAGE, "de");
      results = mp.search(options);

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 1, results.size());

      // check first result (Asterix der Gallier - 1967 - tt0061369)
      MediaSearchResult result = results.get(0);
      checkSearchResult("Asterix der Gallier", "1967", "tt0061369", result);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testTvShowSearch() {
    ImdbMetadataProvider mp = null;
    List<MediaSearchResult> results = null;

    /*
     * test on akas.imdb.com - "Psych"
     */
    try {
      mp = new ImdbMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW, MediaSearchOptions.SearchParam.QUERY, "Psych");
      options.set(MediaSearchOptions.SearchParam.LANGUAGE, "en");
      results = mp.search(options);

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 40, results.size());

      // check first result (Psych - 2006 - tt0491738)
      MediaSearchResult result = results.get(0);
      checkSearchResult("Psych", "2006", "tt0491738", result);

    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testEpisodeListing() {
    ImdbMetadataProvider mp = null;
    List<MediaEpisode> episodes = null;

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    try {
      mp = new ImdbMetadataProvider();
      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setLanguage(MediaLanguages.en);
      options.setCountry(CountryCode.US);
      options.setId(mp.getProviderInfo().getId(), "tt0491738");

      episodes = mp.getEpisodeList(options);

      // did we get a result?
      assertNotNull("Episodes", episodes);

      // result count
      assertEquals("Episodes count", 121, episodes.size());

    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testTvShowScrape() {
    ImdbMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setImdbId("tt0491738");
      options.setCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Psych", md.getStringValue(MediaMetadata.TITLE));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * test on akas.imdb.com - Firefly (tt0303461)
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setImdbId("tt0303461");
      options.setCountry(CountryCode.DE);
      options.setLanguage(MediaLanguages.de);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Firefly - Der Aufbruch der Serenity", md.getStringValue(MediaMetadata.TITLE));
      assertEquals("Firefly", md.getStringValue(MediaMetadata.ORIGINAL_TITLE));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testEpisodeScrape() {
    ImdbMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;
    SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    ProxySettings.setProxySettings("localhost", 3128, "", "");

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    // S1E1
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setImdbId("tt0491738");
      options.setCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      options.setId(MediaMetadata.SEASON_NR, "1");
      options.setId(MediaMetadata.EPISODE_NR, "1");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Pilot", md.getStringValue(MediaMetadata.TITLE));
      assertEquals("The police department in Santa Barbara hires someone they think is a psychic detective.", md.getStringValue(MediaMetadata.PLOT));
      assertEquals("7 July 2006", sdf.format(md.getDateValue(MediaMetadata.RELEASE_DATE)));
      assertEquals(34, md.getCastMembers(CastType.ACTOR).size());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    // S3E12
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setImdbId("tt0491738");
      options.setCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      options.setId(MediaMetadata.SEASON_NR, "3");
      options.setId(MediaMetadata.EPISODE_NR, "12");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Earth, Wind and... Wait for It", md.getStringValue(MediaMetadata.TITLE));
      assertEquals("An arson inspector reluctantly teams up with Shawn and Gus to find the perpetrator of a string of fires.",
          md.getStringValue(MediaMetadata.PLOT));
      assertEquals("23 January 2009", sdf.format(md.getDateValue(MediaMetadata.RELEASE_DATE)));
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  private void checkSearchResult(String title, String year, String imdbId, MediaSearchResult result) {
    // title
    assertEquals("title", title, result.getTitle());
    // year
    assertEquals("year", year, result.getYear());
    // imdbId
    assertEquals("imdbId", imdbId, result.getIMDBId());
  }

  @Test
  public void testMovieScrape() {
    ImdbMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;

    /*
     * scrape www.imdb.com - 9 - tt0472033
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setImdbId("tt0472033");
      options.setCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("9", "2009", "9", 7.0, 63365, "(1) To Protect Us...", 79, "Shane Acker", "Pamela Pettler, Shane Acker", "PG-13", md);

      // check poster
      // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMTY2ODE1MTgxMV5BMl5BanBnXkFtZTcwNTM1NTM2Mg@@._V1._SX195_SY195_.jpg",
      // md);

      // check genres
      List<MediaGenres> genres = new ArrayList<MediaGenres>();
      genres.add(MediaGenres.ANIMATION);
      genres.add(MediaGenres.ACTION);
      genres.add(MediaGenres.ADVENTURE);
      genres.add(MediaGenres.FANTASY);
      genres.add(MediaGenres.MYSTERY);
      genres.add(MediaGenres.SCIENCE_FICTION);
      genres.add(MediaGenres.THRILLER);
      genres.add(MediaGenres.DRAMA);
      checkGenres(genres, md);

      // check plot
      checkPlot(
          "In a world destroyed in a war between man and machine, a hand-stitched doll with the number 9 written on its back comes to life. The world he has awakened in is frightening, but he quickly learns that he is not alone and that there are others like him, also with a single digit written on their back. The first one he encounters is 2 who tells him something of what happened to the world. 2 is also thrilled with the disk 9 is carrying, one with three unique symbols on the front. 9 soon learns that the disk and some of the other dolls who are prepared to die for the good of humankind may be the last hope for man's salvation.",
          md);

      // check cast
      List<MediaCastMember> castMembers = new ArrayList<MediaCastMember>();
      MediaCastMember cm = new MediaCastMember();
      cm.setName("Christopher Plummer");
      cm.setCharacter("#1");
      cm.setImageUrl("http://ia.media-imdb.com/images/M/MV5BMTU5MzQ5MDY3NF5BMl5BanBnXkFtZTcwNzMxOTU5Ng@@._V1._SX400_.jpg");
      cm.setType(CastType.ACTOR);
      castMembers.add(cm);

      cm = new MediaCastMember();
      cm.setName("Martin Landau");
      cm.setCharacter("#2");
      cm.setImageUrl("http://ia.media-imdb.com/images/M/MV5BMTI0MzkxNzg0OF5BMl5BanBnXkFtZTcwNDUzOTc5MQ@@._V1._SX400_.jpg");
      cm.setType(CastType.ACTOR);
      castMembers.add(cm);

      checkCastMembers(castMembers, 10, md);

      // check production company
      checkProductionCompany("Focus Features, Relativity Media, Arc Productions, Starz Animation, Tim Burton Productions", md);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * scrape akas.imdb.com - 12 Monkeys - tt0114746
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setImdbId("tt0114746");
      options.setCountry(CountryCode.DE);
      options.setLanguage(MediaLanguages.de);

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("12 Monkeys", "1995", "Twelve Monkeys", 8.1, 262821, "The future is history.", 129, "Terry Gilliam",
          "Chris Marker, David Webb Peoples, Janet Peoples", "16", md);

      // check poster
      // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMTQ4OTM3NzkyN15BMl5BanBnXkFtZTcwMzIwMzgyMQ@@._V1._SX195_SY195_.jpg",
      // md);

      // check genres
      List<MediaGenres> genres = new ArrayList<MediaGenres>();
      genres.add(MediaGenres.MYSTERY);
      genres.add(MediaGenres.SCIENCE_FICTION);
      genres.add(MediaGenres.THRILLER);
      checkGenres(genres, md);

      // check plot
      checkPlot(
          "An unknown and lethal virus has wiped out five billion people in 1996. Only 1% of the population has survived by the year 2035, and is forced to live underground. A convict (James Cole) reluctantly volunteers to be sent back in time to 1996 to gather information about the origin of the epidemic (who he's told was spread by a mysterious \"Army of the Twelve Monkeys\") and locate the virus before it mutates so that scientists can study it. Unfortunately Cole is mistakenly sent to 1990, six years earlier than expected, and is arrested and locked up in a mental institution, where he meets Dr. Kathryn Railly, a psychiatrist, and Jeffrey Goines, the insane son of a famous scientist and virus expert.",
          md);

      // check cast
      List<MediaCastMember> castMembers = new ArrayList<MediaCastMember>();
      MediaCastMember cm = new MediaCastMember();
      cm.setName("Joseph Melito");
      cm.setCharacter("Young Cole");
      cm.setImageUrl("");
      cm.setType(CastType.ACTOR);
      castMembers.add(cm);

      cm = new MediaCastMember();
      cm.setName("Bruce Willis");
      cm.setCharacter("James Cole");
      cm.setImageUrl("http://ia.media-imdb.com/images/M/MV5BMjA0MjMzMTE5OF5BMl5BanBnXkFtZTcwMzQ2ODE3Mw@@._V1._SX400_.jpg");
      cm.setType(CastType.ACTOR);
      castMembers.add(cm);

      checkCastMembers(castMembers, 85, md);

      // check production company
      checkProductionCompany("Universal Pictures, Atlas Entertainment, Classico", md);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * scrape akas.imdb.com - Brave - tt1217209
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setImdbId("tt1217209");
      options.setCountry(CountryCode.GB);
      options.setLanguage(MediaLanguages.en);

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("Brave", "2012", "Brave", 7.3, 52871, "Change your fate.", 93, "Mark Andrews, Brenda Chapman",
          "Brenda Chapman, Mark Andrews, Steve Purcell, Irene Mecchi, Michael Arndt", "PG", md);

      // check poster
      // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMzgwODk3ODA1NF5BMl5BanBnXkFtZTcwNjU3NjQ0Nw@@._V1._SX195_SY195_.jpg",
      // md);

      // check genres
      List<MediaGenres> genres = new ArrayList<MediaGenres>();
      genres.add(MediaGenres.ANIMATION);
      genres.add(MediaGenres.ADVENTURE);
      genres.add(MediaGenres.COMEDY);
      genres.add(MediaGenres.FAMILY);
      genres.add(MediaGenres.FANTASY);
      checkGenres(genres, md);

      // check plot
      checkPlot(
          "Set in Scotland in a rugged and mythical time, \"Brave\" features Merida, an aspiring archer and impetuous daughter of royalty. Merida makes a reckless choice that unleashes unintended peril and forces her to spring into action to set things right.",
          md);

      // check cast
      List<MediaCastMember> castMembers = new ArrayList<MediaCastMember>();
      MediaCastMember cm = new MediaCastMember();
      cm.setName("Kelly Macdonald");
      cm.setCharacter("Merida");
      cm.setImageUrl("http://ia.media-imdb.com/images/M/MV5BMjE0ODMzMjMyOV5BMl5BanBnXkFtZTcwMTYzNTA0NA@@._V1._SX400_.jpg");
      cm.setType(CastType.ACTOR);
      castMembers.add(cm);

      cm = new MediaCastMember();
      cm.setName("Billy Connolly");
      cm.setCharacter("Fergus");
      cm.setImageUrl("http://ia.media-imdb.com/images/M/MV5BMTQzMzM2MTA4Ml5BMl5BanBnXkFtZTYwMzIxNTM1._V1._SX400_.jpg");
      cm.setType(CastType.ACTOR);
      castMembers.add(cm);

      checkCastMembers(castMembers, 15, md);

      // check production company
      checkProductionCompany("Walt Disney Pictures, Pixar Animation Studios", md);

    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * scrape akas.imdb.com - Brave - tt1217209 - in DE
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setImdbId("tt1217209");
      options.setCountry(CountryCode.DE);
      options.setLanguage(MediaLanguages.de);

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("Merida - Legende der Highlands", "2012", "Brave", 7.3, 52871, "Change your fate.", 93, "Mark Andrews, Brenda Chapman",
          "Brenda Chapman, Mark Andrews, Steve Purcell, Irene Mecchi, Michael Arndt", "PG", md);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    //
    // /*
    // * scrape www.imdb.de - 9 - tt0472033
    // */
    // mp = new ImdbMetadataProvider(ImdbSiteDefinition.IMDB_DE);
    // options = new MediaScrapeOptions();
    // options.setImdbId("tt0472033");
    //
    // md = null;
    // try {
    // MovieModuleManager.MOVIE_SETTINGS.setCertificationCountry(CountryCode.US);
    // md = mp.getMetadata(options);
    // }
    // catch (Exception e) {
    // }
    //
    // // did we get metadata?
    // assertNotNull("MediaMetadata", md);
    //
    // // check moviedetails
    // checkMovieDetails("#9", "2009", "9", 7.0, 63365, "", 79, "Shane Acker",
    // "Pamela Pettler, Shane Acker", "PG-13", md);
    //
    // // check poster
    // //
    // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMTY2ODE1MTgxMV5BMl5BanBnXkFtZTcwNTM1NTM2Mg@@._V1._SX195_SY195_.jpg",
    // // md);
    //
    // // check genres
    // genres = new ArrayList<MediaGenres>();
    // genres.add(MediaGenres.ANIMATION);
    // genres.add(MediaGenres.ACTION);
    // genres.add(MediaGenres.ADVENTURE);
    // genres.add(MediaGenres.FANTASY);
    // genres.add(MediaGenres.MYSTERY);
    // genres.add(MediaGenres.SCIENCE_FICTION);
    // genres.add(MediaGenres.THRILLER);
    // checkGenres(genres, md);
    //
    // // check plot
    // checkPlot(
    // "Schauplatz Zukunft: Eine übergreifende Maschine, bekannt unter dem Namen
    // \"Die große Maschine\", hat sich zusammen mit allen anderen Maschinen der
    // Menschheit bemächtigt und diese restlos ausgelöscht. Doch unscheinbare
    // kleine Wesen aus Stoff, erfunden von einem Wissenschaftler in den letzten
    // Tage der menschlichen Existenz, haben sich zu einer Mission
    // zusammengeschlossen: in der Postapokalypse zu überleben. Nur eines von
    // Ihnen, Nummer 9, hat die notwendigen Führungsqualitäten, um alle
    // gemeinsam gegen die Maschinen aufzubringen.",
    // md);
    //
    // // check cast
    // castMembers = new ArrayList<MediaCastMember>();
    // cm = new MediaCastMember();
    // cm.setName("Christopher Plummer");
    // cm.setCharacter("#1 (voice)");
    // cm.setImageUrl("http://ia.media-imdb.com/images/M/MV5BMTU5MzQ5MDY3NF5BMl5BanBnXkFtZTcwNzMxOTU5Ng@@._V1._SY125_SX100_.jpg");
    // cm.setType(CastType.ACTOR);
    // castMembers.add(cm);
    //
    // cm = new MediaCastMember();
    // cm.setName("Martin Landau");
    // cm.setCharacter("#2 (voice)");
    // cm.setImageUrl("http://ia.media-imdb.com/images/M/MV5BMTI0MzkxNzg0OF5BMl5BanBnXkFtZTcwNDUzOTc5MQ@@._V1._SY125_SX100_.jpg");
    // cm.setType(CastType.ACTOR);
    // castMembers.add(cm);
    //
    // checkCastMembers(castMembers, 10, md);
    //
    // // check production company
    // checkProductionCompany("Focus Features, Relativity Media, Arc
    // Productions, Starz Animation, Tim Burton Productions", md);

  }

  private void checkMovieDetails(String title, String year, String originalTitle, double rating, int voteCount, String tagline, int runtime,
      String director, String writer, String certification, MediaMetadata md) {
    // title
    assertEquals("title ", title, md.getStringValue(MediaMetadata.TITLE));
    // year
    assertEquals("year", year, md.getStringValue(MediaMetadata.YEAR));
    // original title
    assertEquals("originalTitle", originalTitle, md.getStringValue(MediaMetadata.ORIGINAL_TITLE));
    // rating
    assertEquals("rating", rating, md.getDoubleValue(MediaMetadata.RATING), 0.5);
    // count (only check if parsed cout count is smaller than the given
    // votecount)
    if (voteCount > md.getIntegerValue(MediaMetadata.VOTE_COUNT)) {
      assertEquals("count", voteCount, (int) md.getIntegerValue(MediaMetadata.VOTE_COUNT));
    }
    // tagline
    assertEquals("tagline", tagline, md.getStringValue(MediaMetadata.TAGLINE));
    // runtime
    assertEquals("runtime", runtime, (int) md.getIntegerValue(MediaMetadata.RUNTIME));
    // director
    StringBuilder sb = new StringBuilder();
    for (MediaCastMember cm : md.getCastMembers(CastType.DIRECTOR)) {
      if (StringUtils.isNotEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(cm.getName());
    }
    assertEquals("director", director, sb.toString());
    // writer
    sb = new StringBuilder();
    for (MediaCastMember cm : md.getCastMembers(CastType.WRITER)) {
      if (StringUtils.isNotEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(cm.getName());
    }
    assertEquals("writer", writer, sb.toString());
    // certification
    // assertEquals("certification",
    // Certification.getCertification(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry(),
    // certification), md
    // .getCertifications().get(0));
  }

  private void checkMoviePoster(String url, MediaMetadata md) {
    // check poster
    List<MediaArtwork> mediaArt = md.getFanart();
    assertEquals("fanart count", 1, mediaArt.size());
    MediaArtwork art = mediaArt.get(0);
    assertEquals("poster", url, art.getDefaultUrl());
  }

  private void checkGenres(List<MediaGenres> genres, MediaMetadata md) {
    // cehck not null
    assertNotNull("genres", md.getGenres());

    // check the size
    assertEquals("genres count", genres.size(), md.getGenres().size());

    // check each genre if there is a counterpart in metadata
    for (MediaGenres genre : genres) {
      assertTrue("contains genre", md.getGenres().contains(genre));
    }
  }

  private void checkPlot(String plot, MediaMetadata md) {
    // plot
    assertEquals("plot", plot, md.getStringValue(MediaMetadata.PLOT));
  }

  private void checkCastMembers(List<MediaCastMember> castMembers, int count, MediaMetadata md) {
    // not null
    assertNotNull(md.getCastMembers(CastType.ACTOR));
    // count of castmembers
    assertEquals("castMember count", count, md.getCastMembers(CastType.ACTOR).size());
    // check all defined members
    for (int i = 0; i < castMembers.size(); i++) {
      MediaCastMember expected = castMembers.get(i);
      MediaCastMember actual = md.getCastMembers(CastType.ACTOR).get(i);

      // name
      assertEquals("name", expected.getName(), actual.getName());

      // character
      assertEquals("character", expected.getCharacter(), actual.getCharacter());

      // thumb
      assertEquals("thumb", expected.getImageUrl(), actual.getImageUrl());
    }
  }

  private void checkProductionCompany(String company, MediaMetadata md) {
    assertEquals("production company", company, md.getStringValue(MediaMetadata.PRODUCTION_COMPANY));
  }
}
