/*
 * Copyright 2012 - 2014 Manuel Laggner
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
  private final static String TV_SHOW_DATA_SOURCE         = "tvShowDataSource";
  private final static String TV_SHOW_SCRAPER             = "tvShowScraper";
  private final static String PATH                        = "path";
  private final static String SCRAPE_BEST_IMAGE           = "scrapeBestImage";
  private final static String SCRAPER_LANGU               = "scraperLanguage";
  private final static String CERTIFICATION_COUNTRY       = "certificationCountry";
  private final static String RENAMER_ADD_SHOW            = "renamerAddShow";
  private final static String RENAMER_ADD_SEASON          = "renamerAddSeason";
  private final static String RENAMER_ADD_TITLE           = "renamerAddTitle";
  private final static String RENAMER_FORMAT              = "renamerFormat";
  private final static String RENAMER_SEPARATOR           = "renamerSeparator";
  private final static String RENAMER_SEASON_FOLDER       = "renamerSeasonFolder";
  private final static String BUILD_IMAGE_CACHE_ON_IMPORT = "buildImageCacheOnImport";
  private final static String IMAGE_SCRAPER_TVDB          = "imageScraperTvdb";
  private final static String IMAGE_SCRAPER_FANART_TV     = "imageScraperFanartTv";
  private final static String ASCII_REPLACEMENT           = "asciiReplacement";

  @XmlElementWrapper(name = TV_SHOW_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>  tvShowDataSources           = ObservableCollections.observableList(new ArrayList<String>());
  private TvShowScrapers      tvShowScraper               = TvShowScrapers.TVDB;
  private boolean             scrapeBestImage             = true;
  private MediaLanguages      scraperLanguage             = MediaLanguages.en;
  private CountryCode         certificationCountry        = CountryCode.US;
  private boolean             renamerTvShowFolder         = true;
  private boolean             renamerTvShowFolderYear     = false;
  private boolean             renamerAddShow              = true;
  private boolean             renamerAddSeason            = true;
  private boolean             renamerAddTitle             = true;
  private String              renamerSeparator            = "_";
  private String              renamerSeasonFolder         = "Season $1";
  private boolean             buildImageCacheOnImport     = false;
  private boolean             imageScraperTvdb            = true;
  private boolean             imageScraperFanartTv        = true;
  private boolean             asciiReplacement            = false;
  private boolean             renamerSpaceSubstitution    = false;
  private String              renamerSpaceReplacement     = "_";
  private boolean             syncTrakt                   = false;

  @Enumerated(EnumType.STRING)
  private TvShowEpisodeNaming renamerFormat               = TvShowEpisodeNaming.WITH_SE;

  public TvShowSettings() {
  }

  public void addTvShowDataSources(String path) {
    if (!tvShowDataSources.contains(path)) {
      tvShowDataSources.add(path);
      firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
    }
  }

  public void removeTvShowDataSources(String path) {
    TvShowList tvShowList = TvShowList.getInstance();
    tvShowList.removeDatasource(path);
    tvShowDataSources.remove(path);
    firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
  }

  public List<String> getTvShowDataSource() {
    return tvShowDataSources;
  }

  public TvShowScrapers getTvShowScraper() {
    return tvShowScraper;
  }

  public void setTvShowScraper(TvShowScrapers newValue) {
    TvShowScrapers oldValue = this.tvShowScraper;
    this.tvShowScraper = newValue;
    firePropertyChange(TV_SHOW_SCRAPER, oldValue, newValue);
  }

  public boolean isScrapeBestImage() {
    return scrapeBestImage;
  }

  public void setScrapeBestImage(boolean newValue) {
    boolean oldValue = this.scrapeBestImage;
    this.scrapeBestImage = newValue;
    firePropertyChange(SCRAPE_BEST_IMAGE, oldValue, newValue);
  }

  public MediaLanguages getScraperLanguage() {
    return scraperLanguage;
  }

  public void setScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.scraperLanguage;
    this.scraperLanguage = newValue;
    firePropertyChange(SCRAPER_LANGU, oldValue, newValue);
  }

  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  /** add TV show name to filename? */
  /** add season number to filename? */
  /** add title (if 1 EP) to filename? */
  public boolean getRenamerAddShow() {
    return renamerAddShow;
  }

  public void setRenamerAddShow(boolean newValue) {
    boolean oldValue = this.renamerAddShow;
    this.renamerAddShow = newValue;
    firePropertyChange(RENAMER_ADD_SHOW, oldValue, newValue);
  }

  public boolean getRenamerAddSeason() {
    return renamerAddSeason;
  }

  public void setRenamerAddSeason(boolean newValue) {
    boolean oldValue = this.renamerAddSeason;
    this.renamerAddSeason = newValue;
    firePropertyChange(RENAMER_ADD_SEASON, oldValue, newValue);
  }

  public boolean getRenamerAddTitle() {
    return renamerAddTitle;
  }

  public void setRenamerAddTitle(boolean newValue) {
    boolean oldValue = this.renamerAddTitle;
    this.renamerAddTitle = newValue;
    firePropertyChange(RENAMER_ADD_TITLE, oldValue, newValue);
  }

  public TvShowEpisodeNaming getRenamerFormat() {
    return renamerFormat;
  }

  public void setRenamerFormat(TvShowEpisodeNaming newValue) {
    TvShowEpisodeNaming oldValue = this.renamerFormat;
    this.renamerFormat = newValue;
    firePropertyChange(RENAMER_FORMAT, oldValue, newValue);
  }

  public String getRenamerSeparator() {
    return renamerSeparator;
  }

  public void setRenamerSeparator(String newValue) {
    String oldValue = this.renamerSeparator;
    this.renamerSeparator = newValue;
    firePropertyChange(RENAMER_SEPARATOR, oldValue, newValue);
  }

  public String getRenamerSeasonFolder() {
    return renamerSeasonFolder;
  }

  public void setRenamerSeasonFolder(String newValue) {
    String oldValue = this.renamerSeasonFolder;
    this.renamerSeasonFolder = newValue;
    firePropertyChange(RENAMER_SEASON_FOLDER, oldValue, newValue);
  }

  public boolean isBuildImageCacheOnImport() {
    return buildImageCacheOnImport;
  }

  public void setBuildImageCacheOnImport(boolean newValue) {
    boolean oldValue = this.buildImageCacheOnImport;
    this.buildImageCacheOnImport = newValue;
    firePropertyChange(BUILD_IMAGE_CACHE_ON_IMPORT, oldValue, newValue);
  }

  public boolean isImageScraperTvdb() {
    return imageScraperTvdb;
  }

  public boolean isImageScraperFanartTv() {
    return imageScraperFanartTv;
  }

  public void setImageScraperTvdb(boolean newValue) {
    boolean oldValue = this.imageScraperTvdb;
    this.imageScraperTvdb = newValue;
    firePropertyChange(IMAGE_SCRAPER_TVDB, oldValue, newValue);
  }

  public void setImageScraperFanartTv(boolean newValue) {
    boolean oldValue = this.imageScraperFanartTv;
    this.imageScraperFanartTv = newValue;
    firePropertyChange(IMAGE_SCRAPER_FANART_TV, oldValue, newValue);
  }

  public boolean isAsciiReplacement() {
    return asciiReplacement;
  }

  public void setAsciiReplacement(boolean newValue) {
    boolean oldValue = this.asciiReplacement;
    this.asciiReplacement = newValue;
    firePropertyChange(ASCII_REPLACEMENT, oldValue, newValue);
  }

  public boolean isRenamerTvShowFolder() {
    return renamerTvShowFolder;
  }

  public boolean isRenamerTvShowFolderYear() {
    return renamerTvShowFolderYear;
  }

  public void setRenamerTvShowFolder(boolean newValue) {
    boolean oldValue = this.renamerTvShowFolder;
    this.renamerTvShowFolder = newValue;
    firePropertyChange("renamerTvShowFolder", oldValue, newValue);
  }

  public void setRenamerTvShowFolderYear(boolean newValue) {
    boolean oldValue = this.renamerTvShowFolderYear;
    this.renamerTvShowFolderYear = newValue;
    firePropertyChange("renamerTvShowFolderYear", oldValue, newValue);
  }

  public String getRenamerSpaceReplacement() {
    return renamerSpaceReplacement;
  }

  public void setRenamerSpaceReplacement(String newValue) {
    String oldValue = this.renamerSpaceReplacement;
    this.renamerSpaceReplacement = newValue;
    firePropertyChange("renamerReplacement", oldValue, newValue);
  }

  public boolean isRenamerSpaceSubstitution() {
    return renamerSpaceSubstitution;
  }

  public void setRenamerSpaceSubstitution(boolean newValue) {
    boolean oldValue = this.renamerSpaceSubstitution;
    this.renamerSpaceSubstitution = newValue;
    firePropertyChange("renamerSpaceSubstitution", oldValue, newValue);
  }

  public void setSyncTrakt(boolean newValue) {
    boolean oldValue = this.syncTrakt;
    this.syncTrakt = newValue;
    firePropertyChange("syncTrakt", oldValue, newValue);
  }

  public boolean getSyncTrakt() {
    return syncTrakt;
  }
}