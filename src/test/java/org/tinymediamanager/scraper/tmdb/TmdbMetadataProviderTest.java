package org.tinymediamanager.scraper.tmdb;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.IMovieMetadataProvider;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MediaCastMember.CastType;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;

public class TmdbMetadataProviderTest {

  @Test
  public void testSearch() {
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
      assertEquals("Result count", 12, results.size());
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
  public void testScrape() {
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
      
      assertEquals("Twelve Monkeys", md.getStringValue(MediaMetadata.TITLE));
      assertEquals("1995", md.getStringValue(MediaMetadata.YEAR));
      assertEquals("In the year 2035, convict James Cole (Bruce Willis) reluctantly volunteers to be sent back in time to discover the origin of a deadly virus that wiped out nearly all of the earth's population and forced the survivors into underground communities. But when Cole is mistakenly sent to 1990 instead of 1996, he's arrested and locked up in a mental hospital. There he meets psychiatrist Dr. Kathryn Railly (Madeleine Stowe), and patient Jeffrey Goines (Brad Pitt), the son of a famous virus expert, who may hold the key to the mysterious rogue group, the Army of the 12 Monkeys, thought to be responsible for unleashing the killer disease.", md.getStringValue(MediaMetadata.PLOT));
      assertEquals("The future is history", md.getStringValue(MediaMetadata.TAGLINE));
      
      assertNotNull(md.getCastMembers(CastType.ACTOR));
      assertEquals(14, md.getCastMembers(CastType.ACTOR).size());
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
      
      assertEquals("Merida - Legende der Highlands", md.getStringValue(MediaMetadata.TITLE));
      assertEquals("2012", md.getStringValue(MediaMetadata.YEAR));
      assertEquals("Merida – Legende der Highlands spielt im Schottland des 10. Jahrhunderts. König Fergus und Königin Elinor haben es nicht leicht. Ihre Tochter Merida, ein Ass im Bogenschießen, ist ein echter Wildfang und Sturkopf. In ihrem Ungestüm verletzt die Prinzessin alte Traditionen, indem sie bei einem Turnier mit ihrer Schussfertigkeit auftrumpft, die offiziellen Teilnehmer brüskiert und damit den Zorn der schottischen Lords auf sich zieht. Als sie dadurch das Königreich in ein Chaos stürzt, bittet sie eine weise alte Frau um Hilfe, die ihr einen verhängnisvollen Wunsch gewährt. Um ihre Fehler wieder gut zu machen, muss Merida lernen, was wahrer Mut bedeutet und so den Fluch aufheben, bevor es zu spät ist.", md.getStringValue(MediaMetadata.PLOT));
      assertEquals("", md.getStringValue(MediaMetadata.TAGLINE));
      
      assertNotNull(md.getCastMembers(CastType.ACTOR));
      assertEquals(13, md.getCastMembers(CastType.ACTOR).size());
    }
    catch (Exception e) {
      fail(e.getMessage());
    }    
  }
}
