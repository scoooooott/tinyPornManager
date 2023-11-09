package org.tinymediamanager.scraper.spisample;

import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.SubtitleSearchAndScrapeOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowSubtitleProvider;

import java.util.Collections;
import java.util.List;

public class SampleTvShowSubtitleProvider implements ITvShowSubtitleProvider {

  private final MediaProviderInfo providerInfo;

  public SampleTvShowSubtitleProvider() {
    providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    return new MediaProviderInfo("spi-sample", "tvshow_subtitle", "SPI Sample", "A sample for a dynamic TV show subtitle scraper");
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
  public List<SubtitleSearchResult> search(SubtitleSearchAndScrapeOptions subtitleSearchAndScrapeOptions) throws ScrapeException, MissingIdException {
    return Collections.emptyList();
  }
}

