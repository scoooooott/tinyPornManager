package org.tinymediamanager.scraper.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.providerInfo;

import java.util.List;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * @author Nikolas Mavropoylos
 */
public class TmdbTvShowMetadataProviderTest extends TmdbMetadataProviderBaseTest {

  @Test
  public void testTvShowScrapeDataIntegrityInGerman() throws Exception {
    scrapeOptions = new MediaScrapeOptions(MediaType.TV_SHOW);
    scrapeOptions.setTmdbId(160);
    scrapeOptions.setCountry(CountryCode.US);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.de.name()));

    md = tvShowMetadataProvider.getMetadata(scrapeOptions);

    assertThat(md).isNotNull();
    assertThat(md.getTitle()).isEqualTo("Teenage Mutant Ninja Turtles");
    assertThat(md.getPlot()).isEqualTo("Wer weiß schon, was in der Kanalisation von New York so alles lebt... Warum nicht auch vier Schildkröten? Allerdings vier ganz besondere Schildkröten, denn Leonardo, Donatello, Raphael und Michelangelo sind die Teenage Mutant Ninja Turtles! Durch eine geheimnisvolle Substanz, das Ooze, sind sie einst mutiert und haben nicht nur sprechen gelernt. Auch ihre sonstigen Fähigkeiten sind durchaus beachtlich. Denn ihr Meister, die ebenfalls mutierte Ratte Splinter, hat sie in der Kunst des Ninja-Kampfes unterrichtet. Mit erstaunlichen Ergebnissen. Darüber hinaus sind die vier auch sehr niedliche Gesellen, das findet jedenfalls die Fernsehreporterin April O’Neil, die als einzige von der Existenz der Schildkröten weiß. Denn immer wieder gehen die vier auf Verbrecherjagd und geben ihr bestes, um die Einwohner von New York zu beschützen. Dafür stärken sie sich mit ihrer Leibspeise: Pizza! Als eines Tages der Fiesling Shredder ihren Meister entführt, sind die vier Schildkröten nicht mehr zu halten. Mit allen Mitteln wollen sie ihn befreien und Shredder endgültig dingfest machen. New York erlebt einen Kampf, wie es ihn noch nie gegeben hat.");

  }
  @Test
  public void testTvShowScrapeDataIntegrityInGermanWithFallBackLanguageReturnCorrectData() throws Exception {

    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.el.toString());

    scrapeOptions = new MediaScrapeOptions(MediaType.TV_SHOW);
    scrapeOptions.setTmdbId(160);
    scrapeOptions.setCountry(CountryCode.US);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.de.name()));

    md = tvShowMetadataProvider.getMetadata(scrapeOptions);

    assertThat(md).isNotNull();
    assertThat(md.getTitle()).isEqualTo("Χελωνονιντζάκια");
    assertThat(md.getPlot()).isEqualTo("Wer weiß schon, was in der Kanalisation von New York so alles lebt... Warum nicht auch vier Schildkröten? Allerdings vier ganz besondere Schildkröten, denn Leonardo, Donatello, Raphael und Michelangelo sind die Teenage Mutant Ninja Turtles! Durch eine geheimnisvolle Substanz, das Ooze, sind sie einst mutiert und haben nicht nur sprechen gelernt. Auch ihre sonstigen Fähigkeiten sind durchaus beachtlich. Denn ihr Meister, die ebenfalls mutierte Ratte Splinter, hat sie in der Kunst des Ninja-Kampfes unterrichtet. Mit erstaunlichen Ergebnissen. Darüber hinaus sind die vier auch sehr niedliche Gesellen, das findet jedenfalls die Fernsehreporterin April O’Neil, die als einzige von der Existenz der Schildkröten weiß. Denn immer wieder gehen die vier auf Verbrecherjagd und geben ihr bestes, um die Einwohner von New York zu beschützen. Dafür stärken sie sich mit ihrer Leibspeise: Pizza! Als eines Tages der Fiesling Shredder ihren Meister entführt, sind die vier Schildkröten nicht mehr zu halten. Mit allen Mitteln wollen sie ihn befreien und Shredder endgültig dingfest machen. New York erlebt einen Kampf, wie es ihn noch nie gegeben hat.");

    providerInfo.getConfig().setValue("titleFallback",false);

  }


  @Test
  public void testTvShowSearchDataIntegrityInEnglish() throws Exception {
    //1399
    searchOptions = new MediaSearchOptions(MediaType.TV_SHOW, "Game Of Thrones");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));
    searchResults = tvShowMetadataProvider.search(searchOptions);

    assertThat(searchResults).isNotNull();
    assertThat(searchResults).hasSize(1);
    assertThat(searchResults.get(0).getTitle()).isEqualTo("Game of Thrones");
    assertThat(searchResults.get(0).getId()).isEqualTo("1399");
  }

  @Test
  public void testTvShowSearchDataIntegrityInGreek() throws Exception {
    searchOptions = new MediaSearchOptions(MediaType.TV_SHOW, "2057");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));
    searchResults = tvShowMetadataProvider.search(searchOptions);

    assertThat(searchResults).isNotNull();
    assertThat(searchResults).hasSize(1);
    assertThat(searchResults.get(0).getTitle()).isEqualTo("2057:  Ο κόσμος σε 50 χρόνια");
    assertThat(searchResults.get(0).getId()).isEqualTo("4104");
  }

  @Test
  public void  testTvShowSearchDataWithoutFallBackLanguageAndReturnUnreadableData() throws Exception {

    searchOptions = new MediaSearchOptions(MediaType.TV_SHOW, "One piece");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.da.name()));
    searchResults = tvShowMetadataProvider.search(searchOptions);

    assertThat(searchResults).isNotNull();
    assertThat(searchResults.size()).isGreaterThanOrEqualTo(2);
    assertThat(searchResults.get(1).getTitle()).isEqualTo("ワンピース");
    assertThat(searchResults.get(1).getId()).isEqualTo("37854");
    assertThat(searchResults.get(1).getScore()).isLessThan(1);

  }

  @Test
  public void  testTvShowSearchDataWithFallBackLanguageShouldFallbackAndReturnCorrectData() throws Exception {

    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.en.toString());

    searchOptions = new MediaSearchOptions(MediaType.TV_SHOW, "One piece");
    searchOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.da.name()));
    searchResults = tvShowMetadataProvider.search(searchOptions);

    assertThat(searchResults).isNotNull();
    assertThat(searchResults.size()).isGreaterThanOrEqualTo(1);
    assertThat(searchResults.get(1).getTitle()).isEqualTo("One Piece Cut To Manga");
    assertThat(searchResults.get(1).getId()).isEqualTo("37854");
    assertThat(searchResults.get(1).getScore()).isEqualTo(1);

    providerInfo.getConfig().setValue("titleFallback",false);
  }

  @Test
  public void testTvEpisodeListDataIntegrityWithoutFallBackLanguageAndReturnIncorrectData() throws Exception {
    scrapeOptions = new MediaScrapeOptions(MediaType.TV_SHOW);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));
    scrapeOptions.setCountry(CountryCode.US);
    scrapeOptions.setId(tvShowMetadataProvider.getProviderInfo().getId(), "456");

    List<MediaMetadata> episodes = tvShowMetadataProvider.getEpisodeList(scrapeOptions);

    assertThat(episodes).isNotNull();
    assertThat(episodes.size()).isGreaterThanOrEqualTo(679);


    for (MediaMetadata episode : episodes) {
      if (episode.getEpisodeNumber() == 12 && episode.getSeasonNumber() == 2) {
        assertThat(episode.getTitle()).isEqualTo("Επεισόδιο 12");
        assertThat(episode.getPlot())
            .isEqualTo("Η φτηνή τηλεόραση των Σίμσονς χαλάει κι ο Χόμερ με την Μαρτζ διηγούνται στα παιδιά τους πώς γνωρίστηκαν.");
      }
    }

  }

  @Test
  public void testTvEpisodeListDataIntegrityWithFallBackLanguageShouldFallbackAndReturnCorrectData() throws Exception {
    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.en.toString());

    scrapeOptions = new MediaScrapeOptions(MediaType.TV_SHOW);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));
    scrapeOptions.setCountry(CountryCode.US);
    scrapeOptions.setId(tvShowMetadataProvider.getProviderInfo().getId(), "456");

    List<MediaMetadata> episodes = tvShowMetadataProvider.getEpisodeList(scrapeOptions);

    assertThat(episodes).isNotNull();
    assertThat(episodes.size()).isGreaterThanOrEqualTo(679);

    for (MediaMetadata episode : episodes) {
      if (episode.getEpisodeNumber() == 12 && episode.getSeasonNumber() == 2) {
        assertThat(episode.getTitle()).isEqualTo("The Way We Was");
        assertThat(episode.getPlot())
            .isEqualTo("Η φτηνή τηλεόραση των Σίμσονς χαλάει κι ο Χόμερ με την Μαρτζ διηγούνται στα παιδιά τους πώς γνωρίστηκαν.");
      }
    }

    providerInfo.getConfig().setValue("titleFallback",false);
  }

  @Test
  public void testScrapeTvEpisodeWithFallBackLanguageShouldFallbackAndReturnCorrectData() throws Exception {
    providerInfo.getConfig().setValue("titleFallback",true);
    providerInfo.getConfig().setValue("titleFallbackLanguage",MediaLanguages.en.toString());

    scrapeOptions = new MediaScrapeOptions(MediaType.TV_EPISODE);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));
    scrapeOptions.setCountry(CountryCode.US);
    scrapeOptions.setTmdbId(456);
    scrapeOptions.setId(MediaMetadata.SEASON_NR, "2");
    scrapeOptions.setId(MediaMetadata.EPISODE_NR, "12");


    MediaMetadata mediaMetadata = tvShowMetadataProvider.getMetadata(scrapeOptions);

    assertThat(mediaMetadata).isNotNull();
    assertThat(mediaMetadata.getEpisodeNumber()).isEqualTo(12);
    assertThat(mediaMetadata.getSeasonNumber()).isEqualTo(2);
    assertThat(mediaMetadata.getTitle()).isEqualTo("The Way We Was");
    assertThat(mediaMetadata.getPlot()).isEqualTo("Η φτηνή τηλεόραση των Σίμσονς χαλάει κι ο Χόμερ με την Μαρτζ διηγούνται στα παιδιά τους πώς γνωρίστηκαν.");

    providerInfo.getConfig().setValue("titleFallback",false);
  }

  @Test
  public void testScrapeTvEpisodeWithoutFallBackLanguageAndReturnIncorrectData() throws Exception {

    scrapeOptions = new MediaScrapeOptions(MediaType.TV_EPISODE);
    scrapeOptions.setLanguage(LocaleUtils.toLocale(MediaLanguages.el.name()));
    scrapeOptions.setCountry(CountryCode.US);
    scrapeOptions.setTmdbId(456);
    scrapeOptions.setId(MediaMetadata.SEASON_NR, "2");
    scrapeOptions.setId(MediaMetadata.EPISODE_NR, "12");

    MediaMetadata mediaMetadata = tvShowMetadataProvider.getMetadata(scrapeOptions);

    assertThat(mediaMetadata).isNotNull();
    assertThat(mediaMetadata.getEpisodeNumber()).isEqualTo(12);
    assertThat(mediaMetadata.getSeasonNumber()).isEqualTo(2);
    assertThat(mediaMetadata.getTitle()).isEqualTo("Επεισόδιο 12");
    assertThat(mediaMetadata.getPlot()).isEqualTo("Η φτηνή τηλεόραση των Σίμσονς χαλάει κι ο Χόμερ με την Μαρτζ διηγούνται στα παιδιά τους πώς γνωρίστηκαν.");

  }

}
