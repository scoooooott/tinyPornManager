package org.tinymediamanager.scraper.spisample;

import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

public class SampleTvShowMetadataProvider implements ITvShowMetadataProvider {

  private final MediaProviderInfo providerInfo;

  public SampleTvShowMetadataProvider() {
    this.providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    return new MediaProviderInfo("spi-sample", "tvShow", "SPI Sample", "A sample for a dynamic TV show metadata scraper");
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
  public MediaMetadata getMetadata(TvShowSearchAndScrapeOptions tvShowSearchAndScrapeOptions) throws ScrapeException {
    return new MediaMetadata(getId());
  }

  @Override
  public MediaMetadata getMetadata(TvShowEpisodeSearchAndScrapeOptions tvShowEpisodeSearchAndScrapeOptions) throws ScrapeException {
    return new MediaMetadata(getId());
  }

  @Override
  public SortedSet<MediaSearchResult> search(TvShowSearchAndScrapeOptions tvShowSearchAndScrapeOptions) throws ScrapeException {
    return Collections.emptySortedSet();
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowSearchAndScrapeOptions tvShowSearchAndScrapeOptions) throws ScrapeException {
    return Collections.emptyList();
  }
}

