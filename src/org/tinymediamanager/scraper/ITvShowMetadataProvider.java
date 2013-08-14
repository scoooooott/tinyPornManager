package org.tinymediamanager.scraper;

import java.util.List;

public interface ITvShowMetadataProvider {

  /**
   * Gets a general information about this metadata provider
   * 
   * @return the provider info containing metadata of the provider
   */
  public MediaProviderInfo getProviderInfo();

  /**
   * Gets the metadata for the given TV show
   * 
   * @param options
   *          the scrape options (containing the ID of the TV show)
   * @return the metadata
   * @throws Exception
   * 
   */
  public MediaMetadata getTvShowMetadata(MediaScrapeOptions options) throws Exception;

  /**
   * Gets the metadata for the given episode
   * 
   * @param options
   *          the scrape options (containing the ID of the TV show and season/episode number)
   * @return the metadata
   * @throws Exception
   * 
   */
  public MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws Exception;

  /**
   * Search for a TV show
   * 
   * @param options
   *          the options
   * @return the list
   * @throws Exception
   *           the exception
   */
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception;

  /**
   * Gets an episode list for the given TV show
   * 
   * @param options
   *          scrape options (containing the ID of the TV show)
   * @return a list of episodes
   * @throws Exception
   */
  public List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception;
}
