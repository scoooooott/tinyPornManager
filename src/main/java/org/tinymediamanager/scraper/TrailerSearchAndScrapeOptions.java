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

package org.tinymediamanager.scraper;

import org.tinymediamanager.scraper.entities.MediaType;

/**
 * the class {@link TrailerSearchAndScrapeOptions} is used to pass all needed parameters to the trailer scrapers
 *
 * @author Manuel Laggner
 * @since 3.1
 */
public class TrailerSearchAndScrapeOptions extends MediaSearchAndScrapeOptions {

  public TrailerSearchAndScrapeOptions(MediaType type) {
    super(type);
  }

  /**
   * copy constructor
   * 
   * @param original
   *          the original to copy
   */
  public TrailerSearchAndScrapeOptions(TrailerSearchAndScrapeOptions original) {
    super(original);
  }
}
