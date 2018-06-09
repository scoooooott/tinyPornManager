/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.scraper.mediaprovider;

import java.util.List;

import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.exceptions.UnsupportedMediaTypeException;

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
   *          the scrape options (containing the type (TV_SHOW or TV_EPISODE) and the ID of the TV show)
   * @return the metadata
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   * @throws NothingFoundException
   *           indicated that nothing has been found
   * @throws UnsupportedMediaTypeException
   *           indicates that the requested media type is not supported
   */
  MediaMetadata getMetadata(MediaScrapeOptions options)
      throws ScrapeException, MissingIdException, NothingFoundException, UnsupportedMediaTypeException;

  /**
   * Search for a TV show
   * 
   * @param options
   *          the options
   * @return the list
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws UnsupportedMediaTypeException
   *           indicates that the requested media type is not supported
   */
  List<MediaSearchResult> search(MediaSearchOptions options) throws ScrapeException, UnsupportedMediaTypeException;

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
   * @throws UnsupportedMediaTypeException
   *           indicates that the requested media type is not supported
   */
  List<MediaMetadata> getEpisodeList(MediaScrapeOptions options) throws ScrapeException, MissingIdException, UnsupportedMediaTypeException;
}
