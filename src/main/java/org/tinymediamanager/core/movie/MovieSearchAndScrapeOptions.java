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
package org.tinymediamanager.core.movie;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.MediaScraper;

/**
 * The Class MovieSearchAndScrapeOptions.
 * 
 * @author Manuel Laggner
 */
public class MovieSearchAndScrapeOptions {
  private MovieScraperMetadataConfig scraperMetadataConfig;
  private MediaScraper               metadataScraper;

  private List<MediaScraper>         artworkScrapers = new ArrayList<>();
  private List<MediaScraper>         trailerScrapers = new ArrayList<>();

  /**
   * Instantiates a new movie search and scrape config.
   */
  public MovieSearchAndScrapeOptions() {
  }

  /**
   * Load default Settings.
   */
  public void loadDefaults() {
    scraperMetadataConfig = Globals.settings.getMovieScraperMetadataConfig();
    // metadata
    metadataScraper = MovieList.getInstance().getDefaultMediaScraper();

    // artwork
    artworkScrapers.addAll(MovieList.getInstance().getDefaultArtworkScrapers());

    // trailer
    trailerScrapers.addAll(MovieList.getInstance().getDefaultTrailerScrapers());
  }

  /**
   * Gets the scraper metadata config.
   * 
   * @return the scraper metadata config
   */
  public MovieScraperMetadataConfig getScraperMetadataConfig() {
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
  public void setScraperMetadataConfig(MovieScraperMetadataConfig scraperMetadataConfig) {
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

  /**
   * Adds the trailer scraper.
   * 
   * @param trailerScraper
   *          the trailer scraper
   */
  public void addTrailerScraper(MediaScraper trailerScraper) {
    this.trailerScrapers.add(trailerScraper);
  }
}
