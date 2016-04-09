package org.tinymediamanager.scraper.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.entities.MediaCastMember.CastType;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;

public class TmdbMetadataProviderTest {

  @Test
  public void testMovieSearch() {
    IMovieMetadataProvider mp = null;
    List<MediaSearchResult> results = null;
    MediaSearchOptions options = null;

    /********************************************************
     * movie tests in EN
     ********************************************************/

    // Harry Potter
    results = null;
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, "Harry Potter");
      options.set(SearchParam.LANGUAGE, "en");
      results = mp.search(options);
      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertThat(results.size()).isGreaterThan(0);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // Lucky # Slevin
    results = null;
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, "Slevin");
      options.set(SearchParam.LANGUAGE, "en");
      results = mp.search(options);
      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 1, results.size());

      assertEquals("Lucky Number Slevin", results.get(0).getTitle());
      assertEquals("2006", results.get(0).getYear());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    /********************************************************
     * movie tests in DE
     ********************************************************/

    // Die Piefke Saga
    results = null;
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, "Die Piefke Saga");
      options.set(SearchParam.LANGUAGE, "de");
      results = mp.search(options);
      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 4, results.size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // Lucky # Slevin
    results = null;
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, "Slevin");
      options.set(SearchParam.LANGUAGE, "de");
      results = mp.search(options);
      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 1, results.size());

      assertEquals("Lucky # Slevin", results.get(0).getTitle());
      assertEquals("2006", results.get(0).getYear());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testMovieScrape() {
    IMovieMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;

    /********************************************************
     * movie tests in EN
     ********************************************************/

    // twelve monkeys
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setId(mp.getProviderInfo().getId(), "63");

      md = mp.getMetadata(options);

      assertEquals("Twelve Monkeys", md.getTitle());
      assertEquals("1995", md.getYear());
      assertEquals(
          "In the year 2035, convict James Cole (Bruce Willis) reluctantly volunteers to be sent back in time to discover the origin of a deadly virus that wiped out nearly all of the earth's population and forced the survivors into underground communities. But when Cole is mistakenly sent to 1990 instead of 1996, he's arrested and locked up in a mental hospital. There he meets psychiatrist Dr. Kathryn Railly (Madeleine Stowe), and patient Jeffrey Goines (Brad Pitt), the son of a famous virus expert, who may hold the key to the mysterious rogue group, the Army of the 12 Monkeys, thought to be responsible for unleashing the killer disease.",
          md.getPlot());
      assertEquals("The future is history", md.getTagline());

      assertNotNull(md.getCastMembers(CastType.ACTOR));
      assertEquals(14, md.getCastMembers(CastType.ACTOR).size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // Harry Potter #1
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setId(mp.getProviderInfo().getId(), "671");

      md = mp.getMetadata(options);

      assertEquals("Harry Potter and the Philosopher's Stone", md.getTitle());
      assertEquals("2001", md.getYear());
      assertEquals(
          "Harry Potter has lived under the stairs at his aunt and uncle's house his whole life. But on his 11th birthday, he learns he's a powerful wizard -- with a place waiting for him at the Hogwarts School of Witchcraft and Wizardry. As he learns to harness his newfound powers with the help of the school's kindly headmaster, Harry uncovers the truth about his parents' deaths -- and about the villain who's to blame.",
          md.getPlot());
      assertEquals("Let the Magic Begin.", md.getTagline());
      assertEquals(1241, (int) md.getId(MediaMetadata.TMDB_SET));
      assertEquals("Harry Potter Collection", md.getCollectionName());

      assertNotNull(md.getCastMembers(CastType.ACTOR));
      assertThat(md.getCastMembers(CastType.ACTOR).size()).isGreaterThan(0);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    /********************************************************
     * movie tests in DE
     ********************************************************/

    // Merida
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setLanguage(MediaLanguages.de);
      options.setId(mp.getProviderInfo().getId(), "62177");

      md = mp.getMetadata(options);

      assertEquals("Merida - Legende der Highlands", md.getTitle());
      assertEquals("2012", md.getYear());
      assertEquals(
          "Merida – Legende der Highlands spielt im Schottland des 10. Jahrhunderts. König Fergus und Königin Elinor haben es nicht leicht. Ihre Tochter Merida, ein Ass im Bogenschießen, ist ein echter Wildfang und Sturkopf. In ihrem Ungestüm verletzt die Prinzessin alte Traditionen, indem sie bei einem Turnier mit ihrer Schussfertigkeit auftrumpft, die offiziellen Teilnehmer brüskiert und damit den Zorn der schottischen Lords auf sich zieht. Als sie dadurch das Königreich in ein Chaos stürzt, bittet sie eine weise alte Frau um Hilfe, die ihr einen verhängnisvollen Wunsch gewährt. Um ihre Fehler wieder gut zu machen, muss Merida lernen, was wahrer Mut bedeutet und so den Fluch aufheben, bevor es zu spät ist.",
          md.getPlot());
      assertEquals("", md.getTagline());

      assertNotNull(md.getCastMembers(CastType.ACTOR));
      assertThat(md.getCastMembers(CastType.ACTOR).size()).isGreaterThan(0);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCollectionSearch() {
    IMovieMetadataProvider mp = null;
    List<MediaSearchResult> results = null;
    MediaSearchOptions options = null;

    /********************************************************
     * movie set tests in EN
     ********************************************************/

    // Harry Potter
    results = null;
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.MOVIE_SET, MediaSearchOptions.SearchParam.QUERY, "Harry Potter");
      options.set(SearchParam.LANGUAGE, "en");
      results = mp.search(options);
      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 1, results.size());

      assertEquals("1st result title", "Harry Potter Collection", results.get(0).getTitle());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    /********************************************************
     * movie set tests in DE
     ********************************************************/

    // 101 Dalmatiner
    results = null;
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.MOVIE_SET, MediaSearchOptions.SearchParam.QUERY, "101 Dalmatiner");
      options.set(SearchParam.LANGUAGE, "de");
      results = mp.search(options);
      // did we get a result?
      assertNotNull("Result", results);

      // result count
      assertEquals("Result count", 2, results.size());

      assertEquals("1st result title", "101 Dalmatiner Filmreihe", results.get(0).getTitle());
      assertEquals("2nd result title", "101 Dalmatiner (Animiert) Filmreihe", results.get(1).getTitle());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCollectionScrape() {
    IMovieMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;

    /********************************************************
     * movie set tests in EN
     ********************************************************/

    // Harry Potter collection
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE_SET);
      options.setId(mp.getProviderInfo().getId(), "1241");

      md = mp.getMetadata(options);

      assertEquals("Harry Potter Collection", md.getTitle());
      assertEquals("The Harry Potter films are a fantasy series based on the series of seven Harry Potter novels by British writer J. K. Rowling.",
          md.getPlot());
      assertNotNull(md.getSubItems());
      assertEquals(8, md.getSubItems().size());
      assertEquals("Harry Potter and the Philosopher's Stone", md.getSubItems().get(0).getTitle());
      assertEquals(671, md.getSubItems().get(0).getId(MediaMetadata.TMDB));
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowSearch() {
    ITvShowMetadataProvider metadataProvider = null;
    MediaSearchOptions options = null;
    List<MediaSearchResult> results = null;

    /********************************************************
     * TV show tests in EN
     ********************************************************/
    try {
      metadataProvider = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.TV_SHOW, MediaSearchOptions.SearchParam.QUERY, "Psych");
      options.set(SearchParam.LANGUAGE, "en");
      results = metadataProvider.search(options);

      assertNotNull(results);
      assertEquals(20, results.size());
      assertEquals("Psych", results.get(0).getTitle());
      assertEquals("1447", results.get(0).getId());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    /********************************************************
     * TV show tests in DE
     ********************************************************/
    results = null;
    try {
      metadataProvider = new TmdbMetadataProvider();
      options = new MediaSearchOptions(MediaType.TV_SHOW, MediaSearchOptions.SearchParam.QUERY, "Die Simpsons");
      options.set(SearchParam.LANGUAGE, "de");
      results = metadataProvider.search(options);

      assertNotNull(results);
      assertEquals(1, results.size());
      assertEquals("Die Simpsons", results.get(0).getTitle());
      assertEquals("The Simpsons", results.get(0).getOriginalTitle());
      assertEquals("456", results.get(0).getId());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeListing() {
    ITvShowMetadataProvider mp = null;
    List<MediaEpisode> episodes = null;

    /*
     * Psych (1447)
     */
    try {
      mp = new TmdbMetadataProvider();
      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setLanguage(MediaLanguages.en);
      options.setCountry(CountryCode.US);
      options.setId(mp.getProviderInfo().getId(), "1447");

      episodes = mp.getEpisodeList(options);

      // did we get a result?
      assertNotNull("Episodes", episodes);

      // result count
      assertEquals("Episodes count", 144, episodes.size());

    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowScrape() {
    ITvShowMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;

    /*
     * Psych (1447)
     */
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setTmdbId(1447);
      options.setCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Psych", md.getTitle());
      assertEquals(
          "Psych is an American detective comedy-drama television series created by Steve Franks and broadcast on USA Network. The series stars James Roday as Shawn Spencer, a young crime consultant for the Santa Barbara Police Department whose \"heightened observational skills\" and impressive detective instincts allow him to convince people that he solves cases with psychic abilities. The program also stars Dulé Hill as Shawn's best friend and reluctant partner Burton \"Gus\" Guster, as well as Corbin Bernsen as Shawn's father, Henry, a former officer of the Santa Barbara Police Department.",
          md.getPlot());
      assertEquals("2006", md.getYear());
      assertNotEquals(0d, md.getRating());
      assertNotEquals(0, (int) md.getVoteCount());
      assertEquals("Ended", md.getStatus());
      assertThat(md.getProductionCompanies()).isEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodeScrape() {
    ITvShowMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    /*
     * Psych (1447)
     */
    try {
      mp = new TmdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setTmdbId(1447);
      options.setCountry(CountryCode.US);
      options.setLanguage(MediaLanguages.en);
      options.setId(MediaMetadata.SEASON_NR, "1");
      options.setId(MediaMetadata.EPISODE_NR, "1");
      md = mp.getMetadata(options);

      // did we get metadata?
      assertNotNull("MediaMetadata", md);

      assertEquals("Pilot", md.getTitle());
      assertEquals(
          "When Shawn Spencer is arrested for calling in an accurate tip to the police because only the perpetrator would know the details, his only way out is pretending to be a psychic. It turns out Santa Barbara PD isn't done with him. They ask him to consult on a kidnapping case, and a business is born.",
          md.getPlot());
      assertEquals("07-07-2006", sdf.format(md.getReleaseDate()));
      assertEquals(7, md.getCastMembers(CastType.ACTOR).size());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
