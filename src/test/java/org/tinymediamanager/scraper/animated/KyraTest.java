package org.tinymediamanager.scraper.animated;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KyraTest {

  @Test
  public void getMovie() throws IOException, MissingIdException, ScrapeException {
    AnimatedMetadataProvider mp = new AnimatedMetadataProvider();

    MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
    options.setTmdbId(245891);
    options.setLanguage(MediaLanguages.en.toLocale());
    options.setArtworkType(MediaArtwork.MediaArtworkType.POSTER);

    List<MediaArtwork> images = mp.getArtwork(options);
    assertThat(images).isNotNull().isNotEmpty();
    assertThat(images.size()).isGreaterThan(0);
  }

}
