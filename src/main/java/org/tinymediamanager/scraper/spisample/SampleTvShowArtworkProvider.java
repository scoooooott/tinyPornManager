package org.tinymediamanager.scraper.spisample;

import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowArtworkProvider;

import java.util.Collections;
import java.util.List;

public class SampleTvShowArtworkProvider implements ITvShowArtworkProvider {

  private final MediaProviderInfo providerInfo;

  public SampleTvShowArtworkProvider() {
    providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    return new MediaProviderInfo("spi-sample", "tvshow_artwork", "SPI Sample", "A sample for a dynamic TV show artwork scraper");
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

