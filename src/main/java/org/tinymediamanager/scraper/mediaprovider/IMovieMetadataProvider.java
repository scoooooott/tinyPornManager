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

/**
 * The Interface IMovieMetadataProvider. All scrapers providing movie meta data must implement this interface
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public interface IMovieMetadataProvider extends IMediaProvider {

  /**
   * Gets the meta data.
   * 
   * @param options
   *          the options
   * @return the meta data
   * @throws Exception
   *           the exception
   */
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception;

  /**
   * Search for media.
   * 
   * @param options
   *          the options
   * @return the list
   * @throws Exception
   *           the exception
   */
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception;
}
