package org.tinymediamanager.scraper.tmdb;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.providerInfo;

/**
 * @author Nikolas Mavropoylos
 */
public class TmdbMovieSetMetadataProviderTest extends TmdbMetadataProviderBaseTest {
  @Test
  public void testCollectionSearchDataIntegrity() throws Exception {
    searchOptions = new MediaSearchOptions(MediaType.MOVIE_SET, "F*ck You, Goethe Collection");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));

    searchResults = movieSetMetadataProvider.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("F*ck You, Goethe Collection");
    assertThat(searchResults.get(0).getId()).isEqualTo("344555");
  }

  @Test
  public void testCollectionSearchDataIntegrityInGerman() throws Exception {
    searchOptions = new MediaSearchOptions(MediaType.MOVIE_SET, "F*ck You, Goethe Collection");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.de.name()));

    searchResults = movieSetMetadataProvider.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("Fack ju Göhte Filmreihe");
  }

  @Test
  public void testCollectionSearchDataIntegrityInGreek() throws Exception {
    searchOptions = new MediaSearchOptions(MediaType.MOVIE_SET, "F*ck You, Goethe Collection");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));

    searchResults = movieSetMetadataProvider.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("Fack ju Göhte Filmreihe");
  }

  @Test
  public void testCollectionSearchDataIntegrityInGreekWithFallbackLanguageEnglish() throws Exception {

    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.en.toString());

    searchOptions = new MediaSearchOptions(MediaType.MOVIE_SET, "F*ck You, Goethe Collection");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));

    searchResults = movieSetMetadataProvider.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("F*ck You, Goethe Collection");

    providerInfo.getConfig().setValue("titleFallback",false);
  }

  @Test
  public void testCollectionScrapeDataIntegrityWithoutFallbackLanguageReturnMissingData() throws Exception {
    scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE_SET);
    scrapeOptions.setId(movieSetMetadataProvider.getProviderInfo().getId(), "257960");
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));

    md = movieSetMetadataProvider.getMetadata(scrapeOptions);

    assertThat(md).isNotNull();
    assertThat(md.getTitle()).isEqualTo("The Raid Collection");
    assertThat(md.getPlot()).isEmpty();

    assertThat(md.getSubItems()).hasSize(2);
    assertThat(md.getSubItems().get(0).getTitle()).isEqualTo("Επιχείρηση: Χάος");
    assertThat(md.getSubItems().get(1).getTitle()).isEqualTo("The Raid 2: Berandal");

  }

  @Test
  public void testCollectionScrapeDataIntegrityWithFallbackLanguageReturnCorrectData() throws Exception {

    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.en.toString());

    scrapeOptions = new MediaScrapeOptions(MediaType.MOVIE_SET);
    scrapeOptions.setId(movieSetMetadataProvider.getProviderInfo().getId(), "257960");
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));

    md = movieSetMetadataProvider.getMetadata(scrapeOptions);

    assertThat(md).isNotNull();
    assertThat(md.getTitle()).isEqualTo("The Raid Collection");
    assertThat(md.getPlot()).isEqualTo("A S.W.A.T. team becomes trapped in a tenement run by a ruthless mobster and his army of killers and thugs.");

    assertThat(md.getSubItems()).hasSize(2);
    assertThat(md.getSubItems().get(0).getTitle()).isEqualTo("Επιχείρηση: Χάος");
    assertThat(md.getSubItems().get(1).getTitle()).isEqualTo("The Raid 2");

    providerInfo.getConfig().setValue("titleFallback",false);

  }

}
