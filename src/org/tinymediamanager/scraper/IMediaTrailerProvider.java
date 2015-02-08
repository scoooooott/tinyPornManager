/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.scraper;

import java.util.List;

/**
 * The Interface IMediaTrailerProvider.
 * 
 * @author Manuel Laggner
 */
public interface IMediaTrailerProvider extends IMediaProvider {

  /**
   * Gets the trailers.
   * 
   * @param options
   *          the options
   * @return the trailers
   * @throws Exception
   *           the exception
   */
  public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception;

}
