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

import org.tinymediamanager.scraper.SubtitleSearchAndScrapeOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;

/**
 * The Interface {@link ISubtitleProvider}. All scrapers providing subtitles must implement this interface
 * 
 * @author Myron Boyle, Manuel Laggner
 * @since 3.0
 */
public interface ISubtitleProvider extends IMediaProvider {

  /**
   * searches for subtitles for MediaFile
   * 
   * @param options
   *          the options for searching the subtitles
   * @return the MediaSearchResults
   * @throws ScrapeException
   *           any exception which can be thrown while scraping
   * @throws MissingIdException
   *           indicates that there was no usable id to scrape
   */
  List<SubtitleSearchResult> search(SubtitleSearchAndScrapeOptions options) throws ScrapeException, MissingIdException;
}
