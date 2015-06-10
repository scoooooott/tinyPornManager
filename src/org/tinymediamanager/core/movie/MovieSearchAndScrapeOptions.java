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
package org.tinymediamanager.core.movie;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;

/**
 * The Class MovieSearchAndScrapeOptions.
 * 
 * @author Manuel Laggner
 */
public class MovieSearchAndScrapeOptions {
  private MovieScraperMetadataConfig scraperMetadataConfig;
  private MediaScraper               metadataScraper;

  private List<MovieArtworkScrapers> artworkScrapers = new ArrayList<MovieArtworkScrapers>();

  private List<MovieTrailerScrapers> trailerScrapers = new ArrayList<MovieTrailerScrapers>();

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
    metadataScraper = MediaScraper.getMediaScraperById(MovieModuleManager.MOVIE_SETTINGS.getMovieScraper(), ScraperType.MOVIE);

    // artwork
    if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperTmdb()) {
      artworkScrapers.add(MovieArtworkScrapers.TMDB);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperFanartTv()) {
      artworkScrapers.add(MovieArtworkScrapers.FANART_TV);
    }

    // trailer
    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperTmdb()) {
      trailerScrapers.add(MovieTrailerScrapers.TMDB);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperHdTrailers()) {
      trailerScrapers.add(MovieTrailerScrapers.HDTRAILERS);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperOfdb()) {
      trailerScrapers.add(MovieTrailerScrapers.OFDB);
    }
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
  public List<MovieArtworkScrapers> getArtworkScrapers() {
    return artworkScrapers;
  }

  /**
   * Gets the trailer scrapers.
   * 
   * @return the trailer scrapers
   */
  public List<MovieTrailerScrapers> getTrailerScrapers() {
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
  public void addArtworkScraper(MovieArtworkScrapers artworkScraper) {
    this.artworkScrapers.add(artworkScraper);
  }

  /**
   * Adds the trailer scraper.
   * 
   * @param trailerScraper
   *          the trailer scraper
   */
  public void addTrailerScraper(MovieTrailerScrapers trailerScraper) {
    this.trailerScrapers.add(trailerScraper);
  }
}
