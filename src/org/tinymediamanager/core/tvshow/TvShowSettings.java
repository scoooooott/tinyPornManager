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
package org.tinymediamanager.core.tvshow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.MediaLanguages;

/**
 * The Class TvShowSettings.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "TvShowSettings")
public class TvShowSettings extends AbstractModelObject {

  /** The Constant TV_SHOW_DATA_SOURCE. */
  private final static String TV_SHOW_DATA_SOURCE   = "tvShowDataSource";

  /** The Constant TV_SHOW_SCRAPER. */
  private final static String TV_SHOW_SCRAPER       = "tvShowScraper";

  /** The Constant PATH. */
  private final static String PATH                  = "path";

  /** The Constant SCRAPE_BEST_IMAGE. */
  private final static String SCRAPE_BEST_IMAGE     = "scrapeBestImage";

  /** The Constant SCRAPER_LANGU. */
  private final static String SCRAPER_LANGU         = "scraperLanguage";

  /** The Constant CERTIFICATION_COUNTRY. */
  private final static String CERTIFICATION_COUNTRY = "certificationCountry";

  /** add TV show name to filename? */
  private final static String RENAMER_ADD_SHOW      = "renamerAddShow";

  /** add season number to filename? */
  private final static String RENAMER_ADD_SEASON    = "renamerAddSeason";

  /** add title (if 1 EP) to filename? */
  private final static String RENAMER_ADD_TITLE     = "renamerAddTitle";

  /** TvShowRenamer.Format enum */
  private final static String RENAMER_FORMAT        = "renamerFormat";

  /** Renamer separator character */
  private final static String RENAMER_SEPARATOR     = "renamerSeparator";

  /** The movie data sources. */
  @XmlElementWrapper(name = TV_SHOW_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>  tvShowDataSources     = ObservableCollections.observableList(new ArrayList<String>());

  /** The tv show scraper. */
  private TvShowScrapers      tvShowScraper         = TvShowScrapers.TVDB;

  /** The scrape best image. */
  private boolean             scrapeBestImage       = true;

  /** The scraper language. */
  private MediaLanguages      scraperLanguage       = MediaLanguages.en;

  /** The country for certification. */
  private CountryCode         certificationCountry  = CountryCode.US;

  /** add TV show name to filename? */
  private boolean             renamerAddShow        = true;

  /** add season number to filename? */
  private boolean             renamerAddSeason      = true;

  /** add title (if 1 EP) to filename? */
  private boolean             renamerAddTitle       = true;

  @Enumerated(EnumType.STRING)
  private TvShowEpisodeNaming renamerFormat         = TvShowEpisodeNaming.WITH_SE;

  /** Separator char */
  private String              renamerSeparator      = "_";

  /**
   * Instantiates a new tv show settings.
   */
  public TvShowSettings() {
  }

  /**
   * Adds the tv show data sources.
   * 
   * @param path
   *          the path
   */
  public void addTvShowDataSources(String path) {
    tvShowDataSources.add(path);
    firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
  }

  /**
   * Removes the tv show data sources.
   * 
   * @param path
   *          the path
   */
  public void removeTvShowDataSources(String path) {
    TvShowList tvShowList = TvShowList.getInstance();
    tvShowList.removeDatasource(path);
    tvShowDataSources.remove(path);
    firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
  }

  /**
   * Gets the tv show data source.
   * 
   * @return the tv show data source
   */
  public List<String> getTvShowDataSource() {
    return tvShowDataSources;
  }

  /**
   * Gets the tv show scraper.
   * 
   * @return the tv show scraper
   */
  public TvShowScrapers getTvShowScraper() {
    return tvShowScraper;
  }

  /**
   * Sets the tv show scraper.
   * 
   * @param newValue
   *          the new tv show scraper
   */
  public void setTvShowScraper(TvShowScrapers newValue) {
    TvShowScrapers oldValue = this.tvShowScraper;
    this.tvShowScraper = newValue;
    firePropertyChange(TV_SHOW_SCRAPER, oldValue, newValue);
  }

  /**
   * Checks if is scrape best image.
   * 
   * @return true, if is scrape best image
   */
  public boolean isScrapeBestImage() {
    return scrapeBestImage;
  }

  /**
   * Sets the scrape best image.
   * 
   * @param newValue
   *          the new scrape best image
   */
  public void setScrapeBestImage(boolean newValue) {
    boolean oldValue = this.scrapeBestImage;
    this.scrapeBestImage = newValue;
    firePropertyChange(SCRAPE_BEST_IMAGE, oldValue, newValue);
  }

  /**
   * Gets the scraper language.
   * 
   * @return the scraper language
   */
  @XmlElement(name = SCRAPER_LANGU)
  public MediaLanguages getScraperLanguage() {
    return scraperLanguage;
  }

  /**
   * Sets the scraper language.
   * 
   * @param newValue
   *          the new scraper language
   */
  public void setScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.scraperLanguage;
    this.scraperLanguage = newValue;
    firePropertyChange(SCRAPER_LANGU, oldValue, newValue);
  }

  /**
   * Gets the certification country.
   * 
   * @return the certification country
   */
  @XmlElement(name = CERTIFICATION_COUNTRY)
  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  /**
   * Sets the certification country.
   * 
   * @param newValue
   *          the new certification country
   */
  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  /** add TV show name to filename? */
  /** add season number to filename? */
  /** add title (if 1 EP) to filename? */

  /**
   * Should we add TV show name to filename?
   * 
   * @return true/false
   */
  public boolean getRenamerAddShow() {
    return renamerAddShow;
  }

  /**
   * Should we add TV show name to filename?
   * 
   * @param newValue
   *          true/false
   */
  public void setRenamerAddShow(boolean newValue) {
    boolean oldValue = this.renamerAddShow;
    this.renamerAddShow = newValue;
    firePropertyChange(RENAMER_ADD_SHOW, oldValue, newValue);
  }

  /**
   * Should we add season name to filename?
   * 
   * @return true/false
   */
  public boolean getRenamerAddSeason() {
    return renamerAddSeason;
  }

  /**
   * Should we add season name to filename?
   * 
   * @param newValue
   *          true/false
   */
  public void setRenamerAddSeason(boolean newValue) {
    boolean oldValue = this.renamerAddSeason;
    this.renamerAddSeason = newValue;
    firePropertyChange(RENAMER_ADD_SEASON, oldValue, newValue);
  }

  /**
   * Should we add title to filename?
   * 
   * @return true/false
   */
  public boolean getRenamerAddTitle() {
    return renamerAddTitle;
  }

  /**
   * Should we add title to filename?
   * 
   * @param newValue
   *          true/false
   */
  public void setRenamerAddTitle(boolean newValue) {
    boolean oldValue = this.renamerAddTitle;
    this.renamerAddTitle = newValue;
    firePropertyChange(RENAMER_ADD_TITLE, oldValue, newValue);
  }

  /**
   * gets the renamer format
   * 
   * @return TvShowRenamer.Format enum
   */
  public TvShowEpisodeNaming getRenamerFormat() {
    return renamerFormat;
  }

  /**
   * sets the renamer format
   * 
   * @param newValue
   *          TvShowRenamer.Format enum
   */
  public void setRenamerFormat(TvShowEpisodeNaming newValue) {
    TvShowEpisodeNaming oldValue = this.renamerFormat;
    this.renamerFormat = newValue;
    firePropertyChange(RENAMER_FORMAT, oldValue, newValue);
  }

  /**
   * gets the renamer separator char
   * 
   * @return separator
   */
  public String getRenamerSeparator() {
    return renamerSeparator;
  }

  /**
   * the renamer separator char to set
   * 
   * @param newValue
   *          a char (or string ;)
   */
  public void setRenamerSeparator(String newValue) {
    String oldValue = this.renamerSeparator;
    this.renamerSeparator = newValue;
    firePropertyChange(RENAMER_SEPARATOR, oldValue, newValue);
  }

}