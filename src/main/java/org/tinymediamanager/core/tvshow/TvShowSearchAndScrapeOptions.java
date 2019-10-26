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
package org.tinymediamanager.core.tvshow;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaLanguages;

/**
 * The Class TvShowSearchAndScrapeOptions.
 * 
 * @author Manuel Laggner
 */
public class TvShowSearchAndScrapeOptions {
  private MediaLanguages                           language;
  private MediaScraper                             metadataScraper;
  private List<MediaScraper>                       artworkScrapers = new ArrayList<>();
  private List<TvShowScraperMetadataConfig>        tvShowScraperMetadataConfig;
  private List<TvShowEpisodeScraperMetadataConfig> tvShowEpisodeScraperMetadataConfig;
  private boolean                                  episodeList;

  /**
   * Load default Settings.
   */
  public void loadDefaults() {
    // language
    language = TvShowModuleManager.SETTINGS.getScraperLanguage();

    // metadata
    metadataScraper = TvShowList.getInstance().getDefaultMediaScraper();

    // artwork
    artworkScrapers.addAll(TvShowList.getInstance().getDefaultArtworkScrapers());

    // scraper config
    tvShowScraperMetadataConfig = new ArrayList<>(TvShowModuleManager.SETTINGS.getTvShowScraperMetadataConfig());
    tvShowEpisodeScraperMetadataConfig = new ArrayList<>(TvShowModuleManager.SETTINGS.getEpisodeScraperMetadataConfig());

    // scrape episodelist?
    episodeList = TvShowModuleManager.SETTINGS.isDisplayMissingEpisodes();
  }

  /**
   * get the scraper config for TV shows
   * 
   * @return the scraper config
   */
  public List<TvShowScraperMetadataConfig> getTvShowScraperMetadataConfig() {
    return tvShowScraperMetadataConfig;
  }

  /**
   * get the scraper config for episodes
   *
   * @return the scraper config
   */
  public List<TvShowEpisodeScraperMetadataConfig> getTvShowEpisodeScraperMetadataConfig() {
    return tvShowEpisodeScraperMetadataConfig;
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
   * set the scraper config for TV shows
   * 
   * @param tvShowScraperMetadataConfig
   *          the new scraper config
   */
  public void setTvShowScraperMetadataConfig(List<TvShowScraperMetadataConfig> tvShowScraperMetadataConfig) {
    this.tvShowScraperMetadataConfig = tvShowScraperMetadataConfig;
  }

  /**
   * set the scraper config for episodes
   * 
   * @param tvShowEpisodeScraperMetadataConfig
   *          the new scraper config
   */
  public void setTvShowEpisodeScraperMetadataConfig(List<TvShowEpisodeScraperMetadataConfig> tvShowEpisodeScraperMetadataConfig) {
    this.tvShowEpisodeScraperMetadataConfig = tvShowEpisodeScraperMetadataConfig;
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

  /**
   * scrape the episode list
   * 
   * @return true/false
   */
  public boolean isEpisodeList() {
    return episodeList;
  }

  /**
   * scrape the episode list too
   * 
   * @param episodeList
   *          true/false
   */
  public void setEpisodeList(boolean episodeList) {
    this.episodeList = episodeList;
  }
}
