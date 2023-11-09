package org.tinymediamanager.scraper.spisample;

import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.TrailerSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowTrailerProvider;

import java.util.Collections;
import java.util.List;

public class SampleTvShowTrailerProvider implements ITvShowTrailerProvider {

  private final MediaProviderInfo providerInfo;

  public SampleTvShowTrailerProvider() {
    providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    return new MediaProviderInfo("spi-sample", "tvshow_trailer", "SPI Sample", "A sample for a dynamic TV show trailer scraper");
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
  public List<MediaTrailer> getTrailers(TrailerSearchAndScrapeOptions trailerSearchAndScrapeOptions) throws ScrapeException, MissingIdException {
    return Collections.emptyList();
  }
}

