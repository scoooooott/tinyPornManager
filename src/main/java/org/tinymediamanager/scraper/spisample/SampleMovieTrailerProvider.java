package org.tinymediamanager.scraper.spisample;

import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.TrailerSearchAndScrapeOptions;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieTrailerProvider;

import java.util.Collections;
import java.util.List;

public class SampleMovieTrailerProvider implements IMovieTrailerProvider {

  private final MediaProviderInfo providerInfo;

  public SampleMovieTrailerProvider() {
    providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    return new MediaProviderInfo("spi-sample", "movie_trailer", "SPI Sample", "A sample for a dynamic movie trailer scraper");
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

