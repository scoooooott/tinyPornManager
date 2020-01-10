/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper.interfaces;

import java.util.List;
import java.util.SortedSet;

import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;

/**
 * The interface {@link ITvShowMetadataProvider}. To provide metadata for TV shows/episodes
 * 
 * @author Manuel Laggner
 * @since 3.0
 */
public interface ITvShowMetadataProvider extends IMediaProvider {

  /**
   * Gets the metadata for the given TV show
   * 
   * @param options
   *          the scrape options (containing the type TV_SHOW and the ID of the TV show)
   * @return the metadata
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   * @throws NothingFoundException
   *           indicated that nothing has been found
   */
  MediaMetadata getMetadata(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException;

  /**
   * Gets the metadata for the given episode
   *
   * @param options
   *          the scrape options (containing the type TV_EPISODE and the ID of the TV show/episode)
   * @return the metadata
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   * @throws NothingFoundException
   *           indicated that nothing has been found
   */
  MediaMetadata getMetadata(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException;

  /**
   * Search for a TV show
   * 
   * @param options
   *          the options
   * @return a {@link java.util.SortedSet} of all search result (ordered descending)
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   */
  SortedSet<MediaSearchResult> search(TvShowSearchAndScrapeOptions options) throws ScrapeException;

  /**
   * Gets an episode list for the given TV show
   * 
   * @param options
   *          scrape options (containing the ID of the TV show)
   * @return a list of episodes
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   */
  List<MediaMetadata> getEpisodeList(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException;

  /**
   * Gets an episode list through the given episode
   *
   * @param options
   *          scrape options (containing the ID of the TV show)
   * @return a list of episodes
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   */
  List<MediaMetadata> getEpisodeList(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException;
}
