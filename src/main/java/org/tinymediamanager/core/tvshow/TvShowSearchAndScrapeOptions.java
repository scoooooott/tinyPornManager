/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.core.tvshow;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.MediaScraper;

/**
 * The Class TvShowSearchAndScrapeOptions.
 * 
 * @author Manuel Laggner
 */
public class TvShowSearchAndScrapeOptions {
  private TvShowScraperMetadataConfig scraperMetadataConfig;
  private MediaScraper                metadataScraper;
  private List<MediaScraper>          artworkScrapers = new ArrayList<>();

  /**
   * Instantiates a new movie search and scrape config.
   */
  public TvShowSearchAndScrapeOptions() {
  }

  /**
   * Load default Settings.
   */
  public void loadDefaults() {
    scraperMetadataConfig = Globals.settings.getTvShowScraperMetadataConfig();
    // metadata
    metadataScraper = TvShowList.getInstance().getDefaultMediaScraper();

    // artwork
    artworkScrapers.addAll(TvShowList.getInstance().getDefaultArtworkScrapers());
  }

  /**
   * Gets the scraper metadata config.
   * 
   * @return the scraper metadata config
   */
  public TvShowScraperMetadataConfig getScraperMetadataConfig() {
    return scraperMetadataConfig;
  }

  /**
   * Gets the metadata scraper.
   * 
   * @return the metadata scraper
   */
  public MediaScraper getMetadataScraper() {
    return metadataScraper;
  }

  /**
   * Gets the artwork scrapers.
   * 
   * @return the artwork scrapers
   */
  public List<MediaScraper> getArtworkScrapers() {
    return artworkScrapers;
  }

  /**
   * Sets the scraper metadata config.
   * 
   * @param scraperMetadataConfig
   *          the new scraper metadata config
   */
  public void setScraperMetadataConfig(TvShowScraperMetadataConfig scraperMetadataConfig) {
    this.scraperMetadataConfig = scraperMetadataConfig;
  }

  /**
   * Sets the metadata scraper.
   * 
   * @param metadataScraper
   *          the new metadata scraper
   */
  public void setMetadataScraper(MediaScraper metadataScraper) {
    this.metadataScraper = metadataScraper;
  }

  /**
   * Adds the artwork scraper.
   * 
   * @param artworkScraper
   *          the artwork scraper
   */
  public void addArtworkScraper(MediaScraper artworkScraper) {
    this.artworkScrapers.add(artworkScraper);
  }
}
