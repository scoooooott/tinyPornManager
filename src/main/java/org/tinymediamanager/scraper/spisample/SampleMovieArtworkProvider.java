package org.tinymediamanager.scraper.spisample;

import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieArtworkProvider;

import java.util.Collections;
import java.util.List;

public class SampleMovieArtworkProvider implements IMovieArtworkProvider {

  private final MediaProviderInfo providerInfo;

  public SampleMovieArtworkProvider() {
    providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    return new MediaProviderInfo("spi-sample", "movie", "SPI Sample", "A sample for a dynamic movie artwork scraper");
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions artworkSearchAndScrapeOptions) throws ScrapeException, MissingIdException {
    return Collections.emptyList();
  }
}

