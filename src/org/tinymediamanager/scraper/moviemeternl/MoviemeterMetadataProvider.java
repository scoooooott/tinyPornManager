/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.scraper.moviemeternl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;

/**
 * The Class OfdbMetadataProvider.
 * 
 * @author Myron Boyle (myron0815@gmx.net)
 */
public class MoviemeterMetadataProvider implements IMediaMetadataProvider {

  /** The Constant LOGGER. */
  private static final Logger               LOGGER       = LoggerFactory.getLogger(MoviemeterMetadataProvider.class);

  private static final String               BASE_URL     = "http://www.moviemeter.nl";

  /** The Constant instance. */
  private static MoviemeterMetadataProvider instance;

  /** The provider info. */
  private static MediaProviderInfo          providerInfo = new MediaProviderInfo("moviemeter", "moviemeter.nl",
                                                             "Scraper for moviemeter.nl which is able to scrape movie metadata");

  /**
   * Gets the single instance of OfdbMetadataProvider.
   * 
   * @return single instance of OfdbMetadataProvider
   */
  public static synchronized MoviemeterMetadataProvider getInstance() {
    if (instance == null) {
      instance = new MoviemeterMetadataProvider();
    }
    return instance;
  }

  /**
   * Instantiates a new ofdb metadata provider.
   */
  public MoviemeterMetadataProvider() {
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /**
   * Removes all weird characters from search as well some "stopwords" as der|die|das|the|a
   * 
   * @param q
   *          the query string to clean
   * @return
   */
  private String cleanSearch(String q) {
    q = " " + q + " "; // easier regex
    // TODO: doppelte hintereinander funzen so nicht
    q = q.replaceAll("(?i)( a | the | der | die | das |\\(\\d+\\))", " ");
    q = q.replaceAll("[^A-Za-z0-9äöüÄÖÜ ]", " ");
    q = q.replaceAll("  ", "");
    return q.trim();
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());
    // TODO Auto-generated method stub
    return null;
  }

}
