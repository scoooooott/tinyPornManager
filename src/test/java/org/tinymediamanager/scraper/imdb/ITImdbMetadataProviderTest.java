package org.tinymediamanager.scraper.imdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.tinymediamanager.core.entities.Person.Type.ACTOR;
import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviders;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaLanguages;

public class ITImdbMetadataProviderTest {

  @Test
  public void testMovieSearch() {
    ImdbMetadataProvider mp = null;
    List<MediaSearchResult> results = null;

    /*
     * test on akas.imdb.com - "9"
     */
    try {
      mp = new ImdbMetadataProvider();
      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setSearchQuery("9");
      options.setSearchYear(2016);
      options.setLanguage(MediaLanguages.en);
      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 40, results.size());

      // check first result (9 - 2016 - tt5719388)
      MediaSearchResult result = results.get(0);
      checkSearchResult("9", 2016, "tt5719388", result);

      // check second result (9 - 2009 - tt0472033)
      result = results.get(1);
      checkSearchResult("9", 2009, "tt0472033", result);
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
      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setSearchQuery("Inglorious Basterds");
      options.setLanguage(MediaLanguages.en);
      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertThat(results.size()).isGreaterThanOrEqualTo(5);

      // check first result (Inglourious Basterds - 2009 - tt0361748)
      MediaSearchResult result = results.get(0);
      checkSearchResult("Inglourious Basterds", 2009, "tt0361748", result);

      // check second result (The Real Inglorious Bastards - 2012 - tt3320110)
      result = results.get(1);
      checkSearchResult("The Real Inglorious Bastards", 2012, "tt3320110", result);

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
      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setSearchQuery("Asterix der Gallier");
      options.setLanguage(MediaLanguages.de);
      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 1, results.size());

      // check first result (Asterix der Gallier - 1967 - tt0061369)
      MediaSearchResult result = results.get(0);
      checkSearchResult("Asterix der Gallier", 1967, "tt0061369", result);
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
      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.setSearchQuery("Psych");
      options.setLanguage(MediaLanguages.de);
      results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 40, results.size());

      // check first result (Psych - 2006 - tt0491738)
      MediaSearchResult result = results.get(0);
      checkSearchResult("Psych", 2006, "tt0491738", result);

    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testEpisodeListing() {
    ImdbMetadataProvider mp = null;
    List<MediaMetadata> episodes = null;

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    try {
      mp = new ImdbMetadataProvider();
      TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions();
      options.setLanguage(MediaLanguages.en);
      TvShowModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
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
    TvShowSearchAndScrapeOptions options = null;
    MediaMetadata md = null;

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new TvShowSearchAndScrapeOptions();
      options.setImdbId("tt0491738");
      TvShowModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Psych", md.getTitle());
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
      options = new TvShowSearchAndScrapeOptions();
      options.setImdbId("tt0303461");
      TvShowModuleManager.SETTINGS.setCertificationCountry(CountryCode.DE);
      options.setLanguage(MediaLanguages.de);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Firefly: Der Aufbruch der Serenity", md.getTitle());
      assertEquals("Firefly", md.getOriginalTitle());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testTvShowScrapeWithTmdb() {
    ImdbMetadataProvider mp = null;
    TvShowSearchAndScrapeOptions options = null;
    MediaMetadata md = null;

    MediaProviders.loadMediaProviders();

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    try {
      mp = new ImdbMetadataProvider();
      mp.getProviderInfo().getConfig().setValue(ImdbMetadataProvider.USE_TMDB_FOR_TV_SHOWS, Boolean.TRUE);
      options = new TvShowSearchAndScrapeOptions();
      options.setImdbId("tt0491738");
      TvShowModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.de);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Psych", md.getTitle());
      assertThat(md.getIds().size()).isGreaterThan(1);
      assertThat(md.getPlot()).startsWith("Shawn Spencer ist selbsternannter Detektiv");
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testEpisodeScrape() {
    ImdbMetadataProvider mp = null;
    TvShowEpisodeSearchAndScrapeOptions options = null;
    MediaMetadata md = null;
    SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    // S1E1
    try {
      mp = new ImdbMetadataProvider();
      options = new TvShowEpisodeSearchAndScrapeOptions();
      options.setImdbId("tt0491738");
      TvShowModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      options.setId(MediaMetadata.SEASON_NR, "1");
      options.setId(MediaMetadata.EPISODE_NR, "1");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Pilot", md.getTitle());
      assertEquals("The police department in Santa Barbara hires someone they think is a psychic detective.", md.getPlot());
      assertEquals("30 October 2007", sdf.format(md.getReleaseDate()));
      assertEquals(34, md.getCastMembers(ACTOR).size());
      assertEquals(1, md.getCastMembers(DIRECTOR).size());
      assertEquals("Michael Engler", md.getCastMembers(DIRECTOR).get(0).getName());
      assertEquals(1, md.getCastMembers(WRITER).size());
      assertEquals("Steve Franks", md.getCastMembers(WRITER).get(0).getName());

      assertThat(md.getRatings().size()).isEqualTo(1);
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getId()).isNotEmpty();
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVotes()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    // S3E12
    try {
      mp = new ImdbMetadataProvider();
      options = new TvShowEpisodeSearchAndScrapeOptions();
      options.setImdbId("tt0491738");
      TvShowModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      options.setId(MediaMetadata.SEASON_NR, "3");
      options.setId(MediaMetadata.EPISODE_NR, "12");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Earth, Wind and... Wait for It", md.getTitle());
      assertEquals("An arson inspector reluctantly teams up with Shawn and Gus to find the perpetrator of a string of fires.", md.getPlot());
      assertEquals("23 January 2009", sdf.format(md.getReleaseDate()));

      assertThat(md.getRatings().size()).isEqualTo(1);
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getId()).isNotEmpty();
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVotes()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testEpisodeScrapeWithTmdb() {
    ImdbMetadataProvider mp = null;
    TvShowEpisodeSearchAndScrapeOptions options = null;
    MediaMetadata md = null;
    SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    MediaProviders.loadMediaProviders();

    /*
     * test on akas.imdb.com - Psych (tt0491738)
     */
    // S1E1
    try {
      mp = new ImdbMetadataProvider();
      options = new TvShowEpisodeSearchAndScrapeOptions();
      mp.getProviderInfo().getConfig().setValue(ImdbMetadataProvider.USE_TMDB_FOR_TV_SHOWS, Boolean.TRUE);
      options.setImdbId("tt0491738");
      TvShowModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.de);
      options.setId(MediaMetadata.SEASON_NR, "1");
      options.setId(MediaMetadata.EPISODE_NR, "1");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Mit einer Ausrede fängt es an", md.getTitle());
      assertThat(md.getPlot()).startsWith("Als Shawn der Polizei eines Tages den entscheidenden");
      assertEquals("30 October 2007", sdf.format(md.getReleaseDate()));
      assertEquals(34, md.getCastMembers(ACTOR).size());
      assertEquals(1, md.getCastMembers(DIRECTOR).size());
      assertEquals("Michael Engler", md.getCastMembers(DIRECTOR).get(0).getName());
      assertEquals(1, md.getCastMembers(WRITER).size());
      assertEquals("Steve Franks", md.getCastMembers(WRITER).get(0).getName());
      assertThat(md.getIds().size()).isGreaterThan(1);
      assertThat(md.getRatings().size()).isEqualTo(1);
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getId()).isNotEmpty();
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVotes()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  private void checkSearchResult(String title, int year, String imdbId, MediaSearchResult result) {
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
    MovieSearchAndScrapeOptions options = null;
    MediaMetadata md = null;

    /*
     * scrape www.imdb.com - 9 - tt0472033
     */
    try {
      mp = new ImdbMetadataProvider();
      mp.getProviderInfo().getConfig().addBoolean("scrapeLanguageNames", false);
      mp.getProviderInfo().getConfig().addBoolean("scrapeKeywordsPage", true);
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt0472033");
      MovieModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("9", 2009, "9", 7.0, 63365, "(1) To Protect Us...", 79, "Shane Acker", "Pamela Pettler, Shane Acker",
          "PG-13", "09-09-2009", md);

      // check poster
      // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMTY2ODE1MTgxMV5BMl5BanBnXkFtZTcwNTM1NTM2Mg@@._V1._SX195_SY195_.jpg",
      // md);

      // check genres
      List<MediaGenres> genres = new ArrayList<>();
      genres.add(MediaGenres.ANIMATION);
      genres.add(MediaGenres.ACTION);
      genres.add(MediaGenres.ADVENTURE);
      genres.add(MediaGenres.SCIENCE_FICTION);
      checkGenres(genres, md);

      // check plot
      checkPlot(
          "A rag doll that awakens in a postapocalyptic future holds the key to humanity's salvation.",
          md);

      // check cast
      List<Person> castMembers = new ArrayList<>();
      Person cm = new Person();
      cm.setName("Christopher Plummer");
      cm.setRole("#1");
      cm.setThumbUrl("http://ia.media-imdb.com/images/M/MV5BMTU5MzQ5MDY3NF5BMl5BanBnXkFtZTcwNzMxOTU5Ng@@._V1._SX400_.jpg");
      cm.setProfileUrl("http://www.imdb.com/name/nm0001626/");
      cm.setType(ACTOR);
      castMembers.add(cm);

      cm = new Person();
      cm.setName("Martin Landau");
      cm.setRole("#2");
      cm.setThumbUrl("http://ia.media-imdb.com/images/M/MV5BMTI0MzkxNzg0OF5BMl5BanBnXkFtZTcwNDUzOTc5MQ@@._V1._SX400_.jpg");
      cm.setProfileUrl("http://www.imdb.com/name/nm0001445/");
      cm.setType(ACTOR);
      castMembers.add(cm);

      checkCastMembers(castMembers, 10, md);

      // check production company
      checkProductionCompany(
          Arrays.asList("Focus Features", "Relativity Media", "Arc Productions", "Starz Animation", "Tim Burton Productions"),
          md);

      // check localized values
      assertThat(md.getCountries()).containsOnly("US");
      assertThat(md.getSpokenLanguages()).containsOnly("en");

      assertThat(md.getTags()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * scrape www.imdb.com - 12 Monkeys - tt0114746
     */
    try {
      mp = new ImdbMetadataProvider();
      mp.getProviderInfo().getConfig().addBoolean("localReleaseDate", true);
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt0114746");
      MovieModuleManager.SETTINGS.setCertificationCountry(CountryCode.DE);
      options.setLanguage(MediaLanguages.de);

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("12 Monkeys", 1995, "Twelve Monkeys", 8.1, 262821, "The future is history.", 129, "Terry Gilliam",
          "Chris Marker, David Webb Peoples, Janet Peoples", "16", "01-02-1996", md);

      // check poster
      // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMTQ4OTM3NzkyN15BMl5BanBnXkFtZTcwMzIwMzgyMQ@@._V1._SX195_SY195_.jpg",
      // md);

      // check genres
      List<MediaGenres> genres = new ArrayList<>();
      genres.add(MediaGenres.MYSTERY);
      genres.add(MediaGenres.SCIENCE_FICTION);
      genres.add(MediaGenres.THRILLER);
      checkGenres(genres, md);

      // check plot
      checkPlot(
          "An unknown and lethal virus has wiped out five billion people in 1996. Only 1% of the population has survived by the year 2035, and is forced to live underground. A convict (James Cole) reluctantly volunteers to be sent back in time to 1996 to gather information about the origin of the epidemic (who he's told was spread by a mysterious \"Army of the Twelve Monkeys\") and locate the virus before it mutates so that scientists can study it. Unfortunately Cole is mistakenly sent to 1990, six years earlier than expected, and is arrested and locked up in a mental institution, where he meets Dr. Kathryn Railly, a psychiatrist, and Jeffrey Goines, the insane son of a famous scientist and virus expert.",
          md);

      // check cast
      List<Person> castMembers = new ArrayList<>();
      Person cm = new Person();
      cm.setName("Joseph Melito");
      cm.setRole("Young Cole");
      cm.setThumbUrl("");
      cm.setProfileUrl("http://www.imdb.com/name/nm0577828/");
      cm.setType(ACTOR);
      castMembers.add(cm);

      cm = new Person();
      cm.setName("Bruce Willis");
      cm.setRole("James Cole");
      cm.setThumbUrl("http://ia.media-imdb.com/images/M/MV5BMjA0MjMzMTE5OF5BMl5BanBnXkFtZTcwMzQ2ODE3Mw@@._V1._SX400_.jpg");
      cm.setProfileUrl("http://www.imdb.com/name/nm0000246/");
      cm.setType(ACTOR);
      castMembers.add(cm);

      checkCastMembers(castMembers, 86, md);

      // check production company
      checkProductionCompany(Arrays.asList("Universal Pictures", "Atlas Entertainment", "Classico"), md);

      // check localized values
      assertThat(md.getCountries()).containsOnly("Vereinigte Staaten von Amerika");
      assertThat(md.getSpokenLanguages()).containsOnly("Englisch", "Französisch");

      assertThat(md.getTags()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * scrape www.imdb.com - Brave - tt1217209
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt1217209");
      MovieModuleManager.SETTINGS.setCertificationCountry(CountryCode.GB);
      options.setLanguage(MediaLanguages.en);

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("Brave", 2012, "Brave", 7.3, 52871, "Change your fate.", 93, "Mark Andrews, Brenda Chapman, Steve Purcell",
          "Brenda Chapman, Mark Andrews, Steve Purcell, Irene Mecchi", "PG", "02-08-2012", md);

      // check poster
      // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMzgwODk3ODA1NF5BMl5BanBnXkFtZTcwNjU3NjQ0Nw@@._V1._SX195_SY195_.jpg",
      // md);

      // check genres
      List<MediaGenres> genres = new ArrayList<>();
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
      List<Person> castMembers = new ArrayList<>();
      Person cm = new Person();
      cm.setName("Kelly Macdonald");
      cm.setRole("Merida");
      cm.setThumbUrl("http://ia.media-imdb.com/images/M/MV5BMjE0ODMzMjMyOV5BMl5BanBnXkFtZTcwMTYzNTA0NA@@._V1._SX400_.jpg");
      cm.setType(ACTOR);
      castMembers.add(cm);

      cm = new Person();
      cm.setName("Billy Connolly");
      cm.setRole("Fergus");
      cm.setThumbUrl("http://ia.media-imdb.com/images/M/MV5BMTQzMzM2MTA4Ml5BMl5BanBnXkFtZTYwMzIxNTM1._V1._SX400_.jpg");
      cm.setType(ACTOR);
      castMembers.add(cm);

      checkCastMembers(castMembers, 16, md);

      // check production company
      checkProductionCompany(Arrays.asList("Walt Disney Pictures", "Pixar Animation Studios"), md);

      assertThat(md.getTags()).isNotEmpty();

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
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt1217209");
      MovieModuleManager.SETTINGS.setCertificationCountry(CountryCode.DE);
      options.setLanguage(MediaLanguages.de);

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("Merida - Legende der Highlands", 2012, "Brave", 7.3, 52871, "Change your fate.", 93,
          "Mark Andrews, Brenda Chapman, Steve Purcell", "Brenda Chapman, Mark Andrews, Steve Purcell, Irene Mecchi", "PG", "02-08-2012", md);

      assertThat(md.getTags()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    /*
     * scrape www.imdb.com - Winnebago Man - tt1396557
     */
    try {
      mp = new ImdbMetadataProvider();
      options = new MovieSearchAndScrapeOptions();
      options.setImdbId("tt1396557");
      MovieModuleManager.SETTINGS.setCertificationCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);

      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      // check moviedetails
      checkMovieDetails("Winnebago Man", 2009, "Winnebago Man", 7.2, 3890, "", 85, "Ben Steinbauer",
          "Malcolm Pullinger, Ben Steinbauer, Louisa Hall, Joel Heller, Berndt Mader, Natasha Rosow", "", "14-03-2009", md);

      // check poster
      // checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMzgwODk3ODA1NF5BMl5BanBnXkFtZTcwNjU3NjQ0Nw@@._V1._SX195_SY195_.jpg",
      // md);

      // check genres
      List<MediaGenres> genres = new ArrayList<>();
      genres.add(MediaGenres.DOCUMENTARY);
      genres.add(MediaGenres.BIOGRAPHY);
      genres.add(MediaGenres.COMEDY);

      checkGenres(genres, md);

      // check plot
      checkPlot(
          "Jack Rebney is the most famous man you've never heard of - after cursing his way through a Winnebago sales video, Rebney's outrageously funny outtakes became an underground sensation and made him an internet superstar. Filmmaker Ben Steinbauer journeys to the top of a mountain to find the recluse who unwittingly became the \"Winnebago Man.\"",
          md);

      // check cast
      List<Person> castMembers = new ArrayList<>();
      Person cm = new Person();
      cm.setName("Jack Rebney");
      cm.setRole("Himself");
      cm.setType(ACTOR);
      castMembers.add(cm);

      cm = new Person();
      cm.setName("Ben Steinbauer");
      cm.setRole("Himself");
      cm.setType(ACTOR);
      castMembers.add(cm);

      checkCastMembers(castMembers, 14, md);

      // check production company
      checkProductionCompany(Arrays.asList("Bear Media, The", "Field Guide Media"), md);

      assertThat(md.getTags()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  private void checkMovieDetails(String title, int year, String originalTitle, double rating, int voteCount, String tagline, int runtime,
      String director, String writer, String certification, String releaseDate, MediaMetadata md) {
    // title
    assertEquals("title ", title, md.getTitle());
    // year
    assertEquals("year", year, md.getYear());
    // original title
    assertEquals("originalTitle", originalTitle, md.getOriginalTitle());
    // rating
    assertThat(md.getRatings().size()).isEqualTo(1);
    MediaRating mediaRating = md.getRatings().get(0);

    assertEquals("rating", rating, mediaRating.getRating(), 0.5);
    // count (only check if parsed count count is smaller than the given votecount)
    if (voteCount > mediaRating.getVotes()) {
      assertEquals("count", voteCount, (int) mediaRating.getVotes());
    }
    // tagline
    assertEquals("tagline", tagline, md.getTagline());
    // runtime
    assertEquals("runtime", runtime, (int) md.getRuntime());
    // director
    StringBuilder sb = new StringBuilder();
    for (Person cm : md.getCastMembers(DIRECTOR)) {
      if (StringUtils.isNotEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(cm.getName());
    }
    assertEquals("director", director, sb.toString());
    // writer
    sb = new StringBuilder();
    for (Person cm : md.getCastMembers(WRITER)) {
      if (StringUtils.isNotEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(cm.getName());
    }
    assertEquals("writer", writer, sb.toString());

    // date can differ depending on the IP address
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    assertNotEquals("release date", "", sdf.format(md.getReleaseDate()));
    // certification
    // assertEquals("certification",
    // Certification.getCertification(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry(),
    // certification), md
    // .getCertifications().get(0));
  }

  private void checkMoviePoster(String url, MediaMetadata md) {
    // check poster
    List<MediaArtwork> mediaArt = md.getMediaArt();
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
    assertEquals("plot", plot, md.getPlot());
  }

  private void checkCastMembers(List<Person> castMembers, int count, MediaMetadata md) {
    // not null
    assertNotNull(md.getCastMembers(ACTOR));
    // count of castmembers
    assertThat(md.getCastMembers(ACTOR).size()).isGreaterThanOrEqualTo(count);
    // check all defined members
    for (int i = 0; i < castMembers.size(); i++) {
      Person expected = castMembers.get(i);
      Person actual = md.getCastMembers(ACTOR).get(i);

      // name
      assertEquals("name", expected.getName(), actual.getName());

      // character
      assertEquals("character", expected.getRole(), actual.getRole());

      // thumb
      if (StringUtils.isNotBlank(expected.getThumbUrl())) {
        // image may be hosted on a differnent CDN; just check if it is not empty
        assertNotEquals("thumb", "", actual.getThumbUrl());
      }

      // profile path
      if (StringUtils.isNotBlank(expected.getProfileUrl())) {
        assertEquals("profile", expected.getProfileUrl(), actual.getProfileUrl());
      }
    }
  }

  private void checkProductionCompany(List<String> companies, MediaMetadata md) {
    assertThat(md.getProductionCompanies()).containsAll(companies);
  }
}
