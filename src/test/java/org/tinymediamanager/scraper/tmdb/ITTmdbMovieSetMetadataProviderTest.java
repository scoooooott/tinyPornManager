package org.tinymediamanager.scraper.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.providerInfo;

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.core.movie.MovieSetSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.interfaces.IMovieSetMetadataProvider;

/**
 * @author Nikolas Mavropoylos
 */
public class ITTmdbMovieSetMetadataProviderTest {
  @Test
  public void testCollectionSearchDataIntegrity() throws Exception {
    IMovieSetMetadataProvider mp = new TmdbMetadataProvider();

    MovieSetSearchAndScrapeOptions searchOptions = new MovieSetSearchAndScrapeOptions();
    searchOptions.setSearchQuery("F*ck You, Goethe Collection");
    searchOptions.setLanguage(MediaLanguages.en);

    List<MediaSearchResult> searchResults = mp.search(searchOptions);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("F*ck You, Goethe Collection");
    assertThat(searchResults.get(0).getId()).isEqualTo("344555");
  }

  @Test
  public void testCollectionSearchDataIntegrityInGerman() throws Exception {
    IMovieSetMetadataProvider mp = new TmdbMetadataProvider();

    MovieSetSearchAndScrapeOptions options = new MovieSetSearchAndScrapeOptions();
    options.setSearchQuery("F*ck You, Goethe Collection");
    options.setLanguage(MediaLanguages.de);

    List<MediaSearchResult> searchResults = mp.search(options);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("Fack ju Göhte Filmreihe");
  }

  @Test
  public void testCollectionSearchDataIntegrityInGreek() throws Exception {
    IMovieSetMetadataProvider mp = new TmdbMetadataProvider();

    MovieSetSearchAndScrapeOptions options = new MovieSetSearchAndScrapeOptions();
    options.setSearchQuery("F*ck You, Goethe Collection");
    options.setLanguage(MediaLanguages.el);

    List<MediaSearchResult> searchResults = mp.search(options);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("Fack ju Göhte Filmreihe");
  }

  @Test
  public void testCollectionSearchDataIntegrityInGreekWithFallbackLanguageEnglish() throws Exception {
    providerInfo.getConfig().setValue("titleFallback", true);
    providerInfo.getConfig().setValue("titleFallbackLanguage", MediaLanguages.en.toString());

    IMovieSetMetadataProvider mp = new TmdbMetadataProvider();

    MovieSetSearchAndScrapeOptions options = new MovieSetSearchAndScrapeOptions();
    options.setSearchQuery("F*ck You, Goethe Collection");
    options.setLanguage(MediaLanguages.el);

    List<MediaSearchResult> searchResults = mp.search(options);
    // did we get a result?
    assertNotNull("Result", searchResults);

    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);

    assertThat(searchResults.get(0).getTitle()).isEqualTo("F*ck You, Goethe Collection");

    providerInfo.getConfig().setValue("titleFallback", false);
  }

  @Test
  public void testCollectionScrapeDataIntegrityWithoutFallbackLanguageReturnMissingData() throws Exception {
    IMovieSetMetadataProvider mp = new TmdbMetadataProvider();

    MovieSetSearchAndScrapeOptions options = new MovieSetSearchAndScrapeOptions();
    options.setId(mp.getId(), "257960");
    options.setLanguage(MediaLanguages.el);

    MediaMetadata md = mp.getMetadata(options);

    assertThat(md).isNotNull();
    assertThat(md.getTitle()).isEqualTo("The Raid Collection");
    assertThat(md.getPlot()).isEmpty();

    assertThat(md.getSubItems()).hasSize(2);
    assertThat(md.getSubItems().get(0).getTitle()).isEqualTo("Επιχείρηση: Χάος");
    assertThat(md.getSubItems().get(1).getTitle()).isEqualTo("The Raid 2: Berandal");

  }

  @Test
  public void testCollectionScrapeDataIntegrityWithFallbackLanguageReturnCorrectData() throws Exception {
    providerInfo.getConfig().setValue("titleFallback", true);
    providerInfo.getConfig().setValue("titleFallbackLanguage", MediaLanguages.en.toString());

    IMovieSetMetadataProvider mp = new TmdbMetadataProvider();

    MovieSetSearchAndScrapeOptions options = new MovieSetSearchAndScrapeOptions();
    options.setId(mp.getId(), "257960");
    options.setLanguage(MediaLanguages.el);

    MediaMetadata md = mp.getMetadata(options);

    assertThat(md).isNotNull();
    assertThat(md.getTitle()).isEqualTo("The Raid Collection");
    assertThat(md.getPlot()).isEqualTo("A S.W.A.T. team becomes trapped in a tenement run by a ruthless mobster and his army of killers and thugs.");

    assertThat(md.getSubItems()).hasSize(2);
    assertThat(md.getSubItems().get(0).getTitle()).isEqualTo("Επιχείρηση: Χάος");
    assertThat(md.getSubItems().get(1).getTitle()).isEqualTo("The Raid 2");

    providerInfo.getConfig().setValue("titleFallback", false);

  }

}
