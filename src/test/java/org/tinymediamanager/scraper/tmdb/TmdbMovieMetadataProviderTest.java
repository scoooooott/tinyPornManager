package org.tinymediamanager.scraper.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.providerInfo;

import java.util.List;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * @author Nikolas Mavropoylos
 */
public class TmdbMovieMetadataProviderTest extends TmdbMetadataProviderBaseTest {

  @Test
  public void testMovieScrapeDataIntegrityInEnglish() throws Exception {

    scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE);
    scrapeOptions.setId(movieMetadataProvider.getProviderInfo().getId(), "63");
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));

    md = movieMetadataProvider.getMetadata(scrapeOptions);

    assertEquals("Twelve Monkeys", md.getTitle());
    assertEquals(1995, md.getYear());
    assertEquals(
        "In the year 2035, convict James Cole reluctantly volunteers to be sent back in time to discover the origin of a deadly virus that wiped out nearly all of the earth's population and forced the survivors into underground communities. But when Cole is mistakenly sent to 1990 instead of 1996, he's arrested and locked up in a mental hospital. There he meets psychiatrist Dr. Kathryn Railly, and patient Jeffrey Goines, the son of a famous virus expert, who may hold the key to the mysterious rogue group, the Army of the 12 Monkeys, thought to be responsible for unleashing the killer disease.",
        md.getPlot());
    assertEquals("The future is history.", md.getTagline());

    assertNotNull(md.getCastMembers(MediaCastMember.CastType.ACTOR));
    assertEquals(65, md.getCastMembers(MediaCastMember.CastType.ACTOR).size());
  }

  @Test
  public void testMovieScrapeDataIntegrityInGerman() throws Exception {

    scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.de.name()));
    scrapeOptions.setId(movieMetadataProvider.getProviderInfo().getId(), "62177");

    md = movieMetadataProvider.getMetadata(scrapeOptions);

    assertEquals("Merida - Legende der Highlands", md.getTitle());
    assertEquals(2012, md.getYear());
    assertEquals(
        "Merida – Legende der Highlands spielt im Schottland des 10. Jahrhunderts. König Fergus und Königin Elinor haben es nicht leicht. Ihre Tochter Merida, ein Ass im Bogenschießen, ist ein echter Wildfang und Sturkopf. In ihrem Ungestüm verletzt die Prinzessin alte Traditionen, indem sie bei einem Turnier mit ihrer Schussfertigkeit auftrumpft, die offiziellen Teilnehmer brüskiert und damit den Zorn der schottischen Lords auf sich zieht. Als sie dadurch das Königreich in ein Chaos stürzt, bittet sie eine weise alte Frau um Hilfe, die ihr einen verhängnisvollen Wunsch gewährt. Um ihre Fehler wieder gut zu machen, muss Merida lernen, was wahrer Mut bedeutet und so den Fluch aufheben, bevor es zu spät ist.",
        md.getPlot());
    assertEquals("", md.getTagline());

    assertNotNull(md.getCastMembers(MediaCastMember.CastType.ACTOR));
    assertThat(md.getCastMembers(MediaCastMember.CastType.ACTOR).size()).isGreaterThan(0);
  }

  @Test
  public void testMovieScrapeDataWithFallBackLanguageShouldFallbackAndReturnCorrectData() throws Exception {

    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.en.toString());

    scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));
    scrapeOptions.setId(movieMetadataProvider.getProviderInfo().getId(), "79553");

    md = movieMetadataProvider.getMetadata(scrapeOptions);

    assertThat(md.getTitle()).isEqualTo("The Front Line");

    providerInfo.getConfig().setValue("titleFallback",false);
  }

  // @Test
  // public void testMovieScrapeDataWithFallBackLanguageShouldFallbackAndReturnOriginalData() throws Exception {
  //
  // providerInfo.getConfig().setValue("titleFallback",true);
  // providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.ar.toString());
  //
  // scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE);
  // scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.ar.name()));
  // scrapeOptions.setId(movieMetadataProvider.getProviderInfo().getId(), "79553");
  //
  // md = movieMetadataProvider.getMetadata(scrapeOptions);
  //
  // assertThat(md.getTitle()).isEqualTo("The Front Line");
  // assertThat(md.getTitle()).isEqualTo(md.getOriginalTitle());
  //
  // providerInfo.getConfig().setValue("titleFallback",false);
  // }
  //
  // @Test
  // public void testMovieScrapeDataWithFallBackLanguageSameAsQueryLanguageShouldNotFallBack() throws Exception {
  //
  // LOGGER.info("Test Case:");
  // LOGGER.info("\tWe query for a Movie in Greek with Fallback Language Greek and we verify that callback is not Initiated.");
  //
  // providerInfo.getConfig().setValue("titleFallback",true);
  // providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.el.toString());
  //
  // scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE);
  // scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));
  // scrapeOptions.setId(movieMetadataProvider.getProviderInfo().getId(), "79553");
  //
  // md = movieMetadataProvider.getMetadata(scrapeOptions);
  //
  // assertThat(md.getTitle()).isEqualTo("고지전");
  // assertThat(md.getTitle()).isEqualTo(md.getOriginalTitle());
  //
  // providerInfo.getConfig().setValue("titleFallback",false);
  // }

  @Test
  public void testMovieSearchWithFallBackLanguageEnglishVerifyFallbackInitiatedAndChangedTitlesNumberIntegrity() throws Exception {

    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.en.toString());

    searchOptions = new MediaSearchOptions(MediaType.MOVIE, "The Front Line");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));

    List<MediaSearchResult> results = movieMetadataProvider.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", results);

    // result count
    assertThat(results.size()).isGreaterThan(0);

    assertThat(results.get(1).getTitle()).isEqualTo("The Front Line");

    providerInfo.getConfig().setValue("titleFallback",false);
  }

  @Test
  public void testMovieSearchDataIntegrity() throws Exception {

    searchOptions = new MediaSearchOptions(MediaType.MOVIE, "Harry Potter");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));

    List<MediaSearchResult> results = movieMetadataProvider.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", results);

    // result count
    assertThat(results.size()).isGreaterThanOrEqualTo(20);

  }

  @Test
  public void testMovieSearchDataIntegrityInGerman() throws Exception {

    searchOptions = new MediaSearchOptions(MediaType.MOVIE, "Harry Potter");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.de.name()));

    List<MediaSearchResult> results = movieMetadataProvider.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", results);

    // result count
    assertThat(results.size()).isGreaterThanOrEqualTo(20);

    MediaSearchResult object = null;
    for (MediaSearchResult result : results) {
      if (result.getOriginalTitle().equals("Harry Potter and the Philosopher's Stone")) {
        object = result;
      }
    }

    assertThat(object).isNotNull();

    assertThat(object.getTitle()).isEqualTo("Harry Potter und der Stein der Weisen");
    assertThat(object.getOriginalTitle()).isEqualTo("Harry Potter and the Philosopher's Stone");

  }

}
