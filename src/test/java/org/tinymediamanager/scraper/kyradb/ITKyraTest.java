package org.tinymediamanager.scraper.kyradb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;

public class ITKyraTest {

  @Test
  public void getMovie() throws IOException, MissingIdException, ScrapeException {
    KyradbMetadataProvider mp = new KyradbMetadataProvider();

    ArtworkSearchAndScrapeOptions options = new ArtworkSearchAndScrapeOptions(MediaType.MOVIE);
    options.setTmdbId(245891);
    options.setLanguage(MediaLanguages.en);
    options.setArtworkType(MediaArtwork.MediaArtworkType.POSTER);

    List<MediaArtwork> images = mp.getArtwork(options);
    assertThat(images).isNotNull().isNotEmpty();
    assertThat(images.size()).isGreaterThan(0);
  }

}
