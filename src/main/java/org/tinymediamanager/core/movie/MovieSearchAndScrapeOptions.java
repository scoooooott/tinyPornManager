/*
 * Copyright 2012 - 2019 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaLanguages;

/**
 * The Class MovieSearchAndScrapeOptions.
 * 
 * @author Manuel Laggner
 */
public class MovieSearchAndScrapeOptions {
  private MediaLanguages                   language;
  private MediaScraper                     metadataScraper;
  private List<MediaScraper>               artworkScrapers = new ArrayList<>();
  private List<MediaScraper>               trailerScrapers = new ArrayList<>();
  private List<MovieScraperMetadataConfig> scraperMetadataConfig;

  /**
   * Load default Settings.
   */
  public void loadDefaults() {
    language = MovieModuleManager.SETTINGS.getScraperLanguage();

    // metadata
    metadataScraper = MovieList.getInstance().getDefaultMediaScraper();

    // artwork
    artworkScrapers.addAll(MovieList.getInstance().getDefaultArtworkScrapers());

    // trailer
    trailerScrapers.addAll(MovieList.getInstance().getDefaultTrailerScrapers());

    // scraper config
    scraperMetadataConfig = new ArrayList<>(MovieModuleManager.SETTINGS.getScraperMetadataConfig());
  }

  /**
   * Gets the scraper metadata config.
   * 
   * @return the scraper metadata config
   */
  public List<MovieScraperMetadataConfig> getScraperMetadataConfig() {
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
   * Gets the trailer scrapers.
   * 
   * @return the trailer scrapers
   */
  public List<MediaScraper> getTrailerScrapers() {
    return trailerScrapers;
  }

  /**
   * Sets the scraper metadata config.
   * 
   * @param scraperMetadataConfig
   *          the new scraper metadata config
   */
  public void setScraperMetadataConfig(List<MovieScraperMetadataConfig> scraperMetadataConfig) {
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
   * Set the artwork scrapers.
   * 
   * @param artworkScrapers
   *          the artwork scrapers
   */
  public void setArtworkScraper(List<MediaScraper> artworkScrapers) {
    this.artworkScrapers.clear();
    this.artworkScrapers.addAll(artworkScrapers);
  }

  /**
   * Set the trailer scrapers.
   * 
   * @param trailerScraper
   *          the trailer scrapers
   */
  public void setTrailerScraper(List<MediaScraper> trailerScraper) {
    this.trailerScrapers.clear();
    this.trailerScrapers.addAll(trailerScraper);
  }

  /**
   * get the language to scrape
   *
   * @return the language to scrape
   */
  public MediaLanguages getLanguage() {
    return language;
  }

  /**
   * set the language to scrape
   *
   * @param language
   *          the language to scrape
   */
  public void setLanguage(MediaLanguages language) {
    this.language = language;
  }
}
