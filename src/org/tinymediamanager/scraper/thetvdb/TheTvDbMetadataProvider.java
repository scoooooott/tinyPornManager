/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.scraper.thetvdb;

import java.util.List;

import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;

import com.moviejukebox.thetvdb.TheTVDB;

/**
 * The Class TheTvDbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
public class TheTvDbMetadataProvider implements IMediaMetadataProvider {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(TmdbMetadataProvider.class);

  /** The Constant instance. */
  private static TheTVDB      tvdb;

  /**
   * Instantiates a new the tv db metadata provider.
   */
  private TheTvDbMetadataProvider() {
    if (tvdb == null) {
      tvdb = new TheTVDB("1A4971671264D790");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getProviderInfo()
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getMetaData(org.
   * tinymediamanager.scraper.MediaScrapeOptions)
   */
  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager
   * .scraper.MediaSearchOptions)
   */
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());
    // TODO Auto-generated method stub
    return null;
  }

}
