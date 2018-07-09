/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractSettings;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.tvshow.connector.TvShowConnectors;
import org.tinymediamanager.core.tvshow.filenaming.TvShowBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearlogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowFanartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowLogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowNfoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowThumbNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;

import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The Class TvShowSettings.
 * 
 * @author Manuel Laggner
 */
public class TvShowSettings extends AbstractSettings {
  private static final Logger                  LOGGER                         = LoggerFactory.getLogger(TvShowSettings.class);

  public final static String                   DEFAULT_RENAMER_FOLDER_PATTERN = "${showTitle} (${showYear})";
  public final static String                   DEFAULT_RENAMER_SEASON_PATTERN = "Season ${seasonNr}";
  public final static String                   DEFAULT_RENAMER_FILE_PATTERN   = "${showTitle} - S${seasonNr2}E${episodeNr2} - ${title}";

  private final static String                  CONFIG_FILE                    = "tvShows.json";

  private static TvShowSettings                instance;

  /**
   * Constants mainly for events
   */
  private final static String                  TV_SHOW_DATA_SOURCE            = "tvShowDataSource";
  private final static String                  ARTWORK_SCRAPERS               = "artworkScrapers";

  private final static String                  CERTIFICATION_COUNTRY          = "certificationCountry";
  private final static String                  RENAMER_SEASON_FOLDER          = "renamerSeasonFoldername";
  private final static String                  BAD_WORD                       = "badWord";
  private final static String                  SKIP_FOLDER                    = "skipFolder";
  private final static String                  SUBTITLE_SCRAPERS              = "subtitleScrapers";
  private final static String                  UI_FILTERS                     = "uiFilters";
  private final static String                  NFO_FILENAME                   = "nfoFilename";
  private final static String                  POSTER_FILENAME                = "posterFilename";
  private final static String                  FANART_FILENAME                = "fanartFilename";
  private final static String                  BANNER_FILENAME                = "bannerFilename";
  private final static String                  CLEARART_FILENAME              = "clearartFilename";
  private final static String                  THUMB_FILENAME                 = "thumbFilename";
  private final static String                  LOGO_FILENAME                  = "logoFilename";
  private final static String                  CLEARLOGO_FILENAME             = "clearlogoFilename";
  private final static String                  SEASON_POSTER_FILENAME         = "seasonPosterFilename";
  private final static String                  SEASON_BANNER_FILENAME         = "seasonBannerFilename";
  private final static String                  SEASON_THUMB_FILENAME          = "seasonThumbFilename";
  private final static String                  EPISODE_THUMB_FILENAME         = "episodeThumbFilename";

  private final List<String>                   tvShowDataSources              = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                   badWords                       = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                   artworkScrapers                = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                   skipFolders                    = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                   subtitleScrapers               = ObservableCollections.observableList(new ArrayList<>());
  private final List<TvShowNfoNaming>          nfoFilenames                   = new ArrayList<>();
  private final List<TvShowPosterNaming>       posterFilenames                = new ArrayList<>();
  private final List<TvShowFanartNaming>       fanartFilenames                = new ArrayList<>();
  private final List<TvShowBannerNaming>       bannerFilenames                = new ArrayList<>();
  private final List<TvShowClearartNaming>     clearartFilenames              = new ArrayList<>();
  private final List<TvShowThumbNaming>        thumbFilenames                 = new ArrayList<>();
  private final List<TvShowClearlogoNaming>    clearlogoFilenames             = new ArrayList<>();
  private final List<TvShowLogoNaming>         logoFilenames                  = new ArrayList<>();
  private final List<TvShowSeasonPosterNaming> seasonPosterFilenames          = new ArrayList<>();
  private final List<TvShowSeasonBannerNaming> seasonBannerFilenames          = new ArrayList<>();
  private final List<TvShowSeasonThumbNaming>  seasonThumbFilenames           = new ArrayList<>();
  private final List<TvShowEpisodeThumbNaming> episodeThumbFilenames          = new ArrayList<>();

  private List<UIFilters>                      uiFilters                      = new ArrayList<>();
  private final List<String>                   tvShowTableHiddenColumns       = ObservableCollections.observableList(new ArrayList<>());

  // data sources / NFO settings
  private boolean                              buildImageCacheOnImport        = false;
  private TvShowConnectors                     tvShowConnector                = TvShowConnectors.XBMC;
  private CertificationStyle                   certificationStyle             = CertificationStyle.LARGE;
  private boolean                              writeCleanNfo                  = false;
  private MediaLanguages                       nfoLanguage                    = MediaLanguages.en;

  // renamer
  private boolean                              renameAfterScrape              = false;
  private String                               renamerTvShowFoldername        = DEFAULT_RENAMER_FOLDER_PATTERN;
  private String                               renamerSeasonFoldername        = DEFAULT_RENAMER_SEASON_PATTERN;
  private String                               renamerFilename                = DEFAULT_RENAMER_FILE_PATTERN;
  private boolean                              renamerSpaceSubstitution       = false;
  private String                               renamerSpaceReplacement        = "_";
  private boolean                              asciiReplacement               = false;
  private boolean                              specialSeason                  = true;

  // meta data scraper
  private String                               scraper                        = Constants.TVDB;
  private MediaLanguages                       scraperLanguage                = MediaLanguages.en;
  private CountryCode                          certificationCountry           = CountryCode.US;
  private TvShowScraperMetadataConfig          scraperMetadataConfig          = null;

  // artwork scraper
  private boolean                              scrapeBestImage                = true;
  private boolean                              writeActorImages               = false;

  // subtitle scraper
  private MediaLanguages                       subtitleScraperLanguage        = MediaLanguages.en;
  private LanguageStyle                        subtitleLanguageStyle          = LanguageStyle.ISO3T;

  // misc
  private boolean                              syncTrakt                      = false;
  private boolean                              dvdOrder                       = false;
  private boolean                              preferPersonalRating           = true;
  private String                               preferredRating                = "tvdb";

  private boolean                              storeUiFilters                 = false;
  private boolean                              displayMissingEpisodes         = false;

  public TvShowSettings() {
    super();
    addPropertyChangeListener(evt -> setDirty());
    scraperMetadataConfig = new TvShowScraperMetadataConfig();
    scraperMetadataConfig.addPropertyChangeListener(ect -> setDirty());
  }

  @Override
  protected ObjectWriter createObjectWriter() {
    return objectMapper.writerFor(TvShowSettings.class);
  }

  /**
   * Gets the single instance of TvShowSettings.
   *
   * @return single instance of TvShowSettings
   */
  public synchronized static TvShowSettings getInstance() {
    return getInstance(Globals.settings.getSettingsFolder());
  }

  /**
   * Override our settings folder (defaults to "data")<br>
   * <b>Should only be used for unit testing et all!</b><br>
   *
   * @return single instance of TvShowSettings
   */
  public synchronized static TvShowSettings getInstance(String folder) {
    if (instance == null) {
      instance = (TvShowSettings) getInstance(folder, CONFIG_FILE, TvShowSettings.class);
    }
    return instance;
  }

  @Override
  public String getConfigFilename() {
    return CONFIG_FILE;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * the tmm defaults
   */
  @Override
  protected void writeDefaultSettings() {
    setTvShowTableHiddenColumns(Arrays.asList("format", "fileSize"));

    // activate default scrapers
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.SUBTITLE)) {
      addTvShowSubtitleScraper(ms.getId());
    }
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.TV_SHOW_ARTWORK)) {
      addTvShowArtworkScraper(ms.getId());
    }

    // set default languages based on java instance
    String defaultLang = Locale.getDefault().getLanguage();
    CountryCode cc = CountryCode.getByCode(defaultLang.toUpperCase());
    if (cc != null) {
      setCertificationCountry(cc);
    }
    for (MediaLanguages ml : MediaLanguages.values()) {
      if (ml.name().equals(defaultLang)) {
        setScraperLanguage(ml);
      }
    }

    posterFilenames.add(TvShowPosterNaming.POSTER);
    fanartFilenames.add(TvShowFanartNaming.FANART);
    bannerFilenames.add(TvShowBannerNaming.BANNER);
    clearartFilenames.add(TvShowClearartNaming.CLEARART);
    logoFilenames.add(TvShowLogoNaming.LOGO);
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);
    thumbFilenames.add(TvShowThumbNaming.THUMB);
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_POSTER);
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_BANNER);
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_THUMB);
    episodeThumbFilenames.add(TvShowEpisodeThumbNaming.FILENAME_THUMB);

    saveSettings();
  }

  public void addTvShowDataSources(String path) {
    if (!tvShowDataSources.contains(path)) {
      tvShowDataSources.add(path);
      firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
      firePropertyChange(Constants.DATA_SOURCE, null, tvShowDataSources);
    }
  }

  public void removeTvShowDataSources(String path) {
    TvShowList tvShowList = TvShowList.getInstance();
    tvShowList.removeDatasource(path);
    tvShowDataSources.remove(path);
    firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
    firePropertyChange(Constants.DATA_SOURCE, null, tvShowDataSources);
  }

  public List<String> getTvShowDataSource() {
    return tvShowDataSources;
  }

  public String getScraper() {
    if (StringUtils.isBlank(scraper)) {
      return Constants.TVDB;
    }
    return scraper;
  }

  public void setScraper(String newValue) {
    String oldValue = this.scraper;
    this.scraper = newValue;
    firePropertyChange("scraper", oldValue, newValue);
  }

  public void addTvShowArtworkScraper(String newValue) {
    if (!artworkScrapers.contains(newValue)) {
      artworkScrapers.add(newValue);
      firePropertyChange(ARTWORK_SCRAPERS, null, artworkScrapers);
    }
  }

  public void removeTvShowArtworkScraper(String newValue) {
    if (artworkScrapers.contains(newValue)) {
      artworkScrapers.remove(newValue);
      firePropertyChange(ARTWORK_SCRAPERS, null, artworkScrapers);
    }
  }

  public List<String> getArtworkScrapers() {
    return artworkScrapers;
  }

  public boolean isScrapeBestImage() {
    return scrapeBestImage;
  }

  public void setScrapeBestImage(boolean newValue) {
    boolean oldValue = this.scrapeBestImage;
    this.scrapeBestImage = newValue;
    firePropertyChange("scrapeBestImage", oldValue, newValue);
  }

  public MediaLanguages getScraperLanguage() {
    return scraperLanguage;
  }

  public void setScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.scraperLanguage;
    this.scraperLanguage = newValue;
    firePropertyChange("scraperLanguage", oldValue, newValue);
  }

  public MediaLanguages getSubtitleScraperLanguage() {
    return subtitleScraperLanguage;
  }

  public void setSubtitleScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.subtitleScraperLanguage;
    this.subtitleScraperLanguage = newValue;
    firePropertyChange("subtitleScraperLanguage", oldValue, newValue);
  }

  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  public String getRenamerSeasonFoldername() {
    return renamerSeasonFoldername;
  }

  public void setRenamerSeasonFoldername(String newValue) {
    String oldValue = this.renamerSeasonFoldername;
    this.renamerSeasonFoldername = newValue;
    firePropertyChange(RENAMER_SEASON_FOLDER, oldValue, newValue);
  }

  public String getRenamerTvShowFoldername() {
    return renamerTvShowFoldername;
  }

  public void setRenamerTvShowFoldername(String newValue) {
    String oldValue = this.renamerTvShowFoldername;
    this.renamerTvShowFoldername = newValue;
    firePropertyChange("renamerTvShowFoldername", oldValue, newValue);
  }

  public String getRenamerFilename() {
    return renamerFilename;
  }

  public void setRenamerFilename(String newValue) {
    String oldValue = this.renamerFilename;
    this.renamerFilename = newValue;
    firePropertyChange("renamerFilename", oldValue, newValue);
  }

  public boolean isBuildImageCacheOnImport() {
    return buildImageCacheOnImport;
  }

  public void setBuildImageCacheOnImport(boolean newValue) {
    boolean oldValue = this.buildImageCacheOnImport;
    this.buildImageCacheOnImport = newValue;
    firePropertyChange("buildImageCacheOnImport", oldValue, newValue);
  }

  public boolean isAsciiReplacement() {
    return asciiReplacement;
  }

  public void setAsciiReplacement(boolean newValue) {
    boolean oldValue = this.asciiReplacement;
    this.asciiReplacement = newValue;
    firePropertyChange("asciiReplacement", oldValue, newValue);
  }

  public boolean isSpecialSeason() {
    return specialSeason;
  }

  public void setSpecialSeason(boolean newValue) {
    boolean oldValue = this.specialSeason;
    this.specialSeason = newValue;
    firePropertyChange("specialSeason", oldValue, newValue);
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

  public boolean isDvdOrder() {
    return dvdOrder;
  }

  public void setDvdOrder(boolean newValue) {
    boolean oldValue = this.dvdOrder;
    this.dvdOrder = newValue;
    firePropertyChange("dvdOrder", oldValue, newValue);
  }

  public void addSkipFolder(String newValue) {
    if (!skipFolders.contains(newValue)) {
      skipFolders.add(newValue);
      firePropertyChange(SKIP_FOLDER, null, skipFolders);
    }
  }

  public void removeSkipFolder(String newValue) {
    if (skipFolders.contains(newValue)) {
      skipFolders.remove(newValue);
      firePropertyChange(SKIP_FOLDER, null, skipFolders);
    }
  }

  public List<String> getSkipFolder() {
    return skipFolders;
  }

  public void addBadWord(String badWord) {
    if (!badWords.contains(badWord.toLowerCase())) {
      badWords.add(badWord.toLowerCase());
      firePropertyChange(BAD_WORD, null, badWords);
    }
  }

  public void removeBadWord(String badWord) {
    badWords.remove(badWord.toLowerCase());
    firePropertyChange(BAD_WORD, null, badWords);
  }

  public List<String> getBadWord() {
    // convert to lowercase for easy contains checking
    ListIterator<String> iterator = badWords.listIterator();
    while (iterator.hasNext()) {
      iterator.set(iterator.next().toLowerCase());
    }
    return badWords;
  }

  public void addEpisodeThumbFilename(TvShowEpisodeThumbNaming filename) {
    if (!episodeThumbFilenames.contains(filename)) {
      episodeThumbFilenames.add(filename);
      firePropertyChange(EPISODE_THUMB_FILENAME, null, episodeThumbFilenames);
    }
  }

  public void clearEpisodeThumbFilenames() {
    episodeThumbFilenames.clear();
    firePropertyChange(EPISODE_THUMB_FILENAME, null, episodeThumbFilenames);
  }

  public List<TvShowEpisodeThumbNaming> getEpisodeThumbFilenames() {
    return new ArrayList<>(this.episodeThumbFilenames);
  }

  public void addTvShowSubtitleScraper(String newValue) {
    if (!subtitleScrapers.contains(newValue)) {
      subtitleScrapers.add(newValue);
      firePropertyChange(SUBTITLE_SCRAPERS, null, subtitleScrapers);
    }
  }

  public void removeTvShowSubtitleScraper(String newValue) {
    if (subtitleScrapers.contains(newValue)) {
      subtitleScrapers.remove(newValue);
      firePropertyChange(SUBTITLE_SCRAPERS, null, subtitleScrapers);
    }
  }

  public List<String> getSubtitleScrapers() {
    return subtitleScrapers;
  }

  public LanguageStyle getSubtitleLanguageStyle() {
    return subtitleLanguageStyle;
  }

  public void setSubtitleLanguageStyle(LanguageStyle newValue) {
    LanguageStyle oldValue = this.subtitleLanguageStyle;
    this.subtitleLanguageStyle = newValue;
    firePropertyChange("subtitleLanguageStyle", oldValue, newValue);
  }

  public void setTvShowTableHiddenColumns(List<String> hiddenColumns) {
    tvShowTableHiddenColumns.clear();
    tvShowTableHiddenColumns.addAll(hiddenColumns);
    firePropertyChange("tvShowTableHiddenColumns", null, tvShowTableHiddenColumns);
  }

  public List<String> getTvShowTableHiddenColumns() {
    return tvShowTableHiddenColumns;
  }

  public void setUiFilters(List<UIFilters> filters) {
    uiFilters = filters;
    firePropertyChange(UI_FILTERS, null, uiFilters);
  }

  public List<UIFilters> getUiFilters() {
    if (storeUiFilters) {
      return uiFilters;
    }
    return new ArrayList<>();
  }

  public void setStoreUiFilters(boolean newValue) {
    boolean oldValue = this.storeUiFilters;
    this.storeUiFilters = newValue;
    firePropertyChange("storeUiFilters", oldValue, newValue);
  }

  public boolean isStoreUiFilters() {
    return storeUiFilters;
  }

  public boolean isDisplayMissingEpisodes() {
    return displayMissingEpisodes;
  }

  public void setDisplayMissingEpisodes(boolean newValue) {
    boolean oldValue = this.displayMissingEpisodes;
    this.displayMissingEpisodes = newValue;
    firePropertyChange("displayMissingEpisodes", oldValue, newValue);
  }

  /**
   * Gets the tv show scraper metadata config.
   *
   * @return the tv show scraper metadata config
   */
  public TvShowScraperMetadataConfig getScraperMetadataConfig() {
    return scraperMetadataConfig;
  }

  /**
   * Sets the tv show scraper metadata config.
   *
   * @param scraperMetadataConfig
   *          the new tv show scraper metadata config
   */
  public void setScraperMetadataConfig(TvShowScraperMetadataConfig scraperMetadataConfig) {
    this.scraperMetadataConfig = scraperMetadataConfig;
    this.scraperMetadataConfig.addPropertyChangeListener(evt -> setDirty());
  }

  public void addNfoFilename(TvShowNfoNaming filename) {
    if (!nfoFilenames.contains(filename)) {
      nfoFilenames.add(filename);
      firePropertyChange(NFO_FILENAME, null, nfoFilenames);
    }
  }

  public void clearNfoFilenames() {
    nfoFilenames.clear();
    firePropertyChange(NFO_FILENAME, null, nfoFilenames);
  }

  public List<TvShowNfoNaming> getNfoFilenames() {
    return new ArrayList<>(this.nfoFilenames);
  }

  public void addPosterFilename(TvShowPosterNaming filename) {
    if (!posterFilenames.contains(filename)) {
      posterFilenames.add(filename);
      firePropertyChange(POSTER_FILENAME, null, posterFilenames);
    }
  }

  public void clearPosterFilenames() {
    posterFilenames.clear();
    firePropertyChange(POSTER_FILENAME, null, posterFilenames);
  }

  public List<TvShowPosterNaming> getPosterFilenames() {
    return new ArrayList<>(this.posterFilenames);
  }

  public void addFanartFilename(TvShowFanartNaming filename) {
    if (!fanartFilenames.contains(filename)) {
      fanartFilenames.add(filename);
      firePropertyChange(FANART_FILENAME, null, fanartFilenames);
    }
  }

  public void clearFanartFilenames() {
    fanartFilenames.clear();
    firePropertyChange(FANART_FILENAME, null, fanartFilenames);
  }

  public List<TvShowFanartNaming> getFanartFilenames() {
    return new ArrayList<>(this.fanartFilenames);
  }

  public void addBannerFilename(TvShowBannerNaming filename) {
    if (!bannerFilenames.contains(filename)) {
      bannerFilenames.add(filename);
      firePropertyChange(BANNER_FILENAME, null, bannerFilenames);
    }
  }

  public void clearBannerFilenames() {
    bannerFilenames.clear();
    firePropertyChange(BANNER_FILENAME, null, bannerFilenames);
  }

  public List<TvShowBannerNaming> getBannerFilenames() {
    return new ArrayList<>(this.bannerFilenames);
  }

  public void addClearartFilename(TvShowClearartNaming filename) {
    if (!clearartFilenames.contains(filename)) {
      clearartFilenames.add(filename);
      firePropertyChange(CLEARART_FILENAME, null, clearartFilenames);
    }
  }

  public void clearClearartFilenames() {
    clearartFilenames.clear();
    firePropertyChange(CLEARART_FILENAME, null, clearartFilenames);
  }

  public List<TvShowClearartNaming> getClearartFilenames() {
    return new ArrayList<>(this.clearartFilenames);
  }

  public void addThumbFilename(TvShowThumbNaming filename) {
    if (!thumbFilenames.contains(filename)) {
      thumbFilenames.add(filename);
      firePropertyChange(THUMB_FILENAME, null, thumbFilenames);
    }
  }

  public void clearThumbFilenames() {
    thumbFilenames.clear();
    firePropertyChange(THUMB_FILENAME, null, thumbFilenames);
  }

  public List<TvShowThumbNaming> getThumbFilenames() {
    return new ArrayList<>(this.thumbFilenames);
  }

  public void addLogoFilename(TvShowLogoNaming filename) {
    if (!logoFilenames.contains(filename)) {
      logoFilenames.add(filename);
      firePropertyChange(LOGO_FILENAME, null, logoFilenames);
    }
  }

  public void clearLogoFilenames() {
    logoFilenames.clear();
    firePropertyChange(LOGO_FILENAME, null, logoFilenames);
  }

  public List<TvShowLogoNaming> getLogoFilenames() {
    return new ArrayList<>(this.logoFilenames);
  }

  public void addClearlogoFilename(TvShowClearlogoNaming filename) {
    if (!clearlogoFilenames.contains(filename)) {
      clearlogoFilenames.add(filename);
      firePropertyChange(CLEARLOGO_FILENAME, null, clearlogoFilenames);
    }
  }

  public void clearClearlogoFilenames() {
    clearlogoFilenames.clear();
    firePropertyChange(CLEARLOGO_FILENAME, null, clearlogoFilenames);
  }

  public List<TvShowClearlogoNaming> getClearlogoFilenames() {
    return new ArrayList<>(this.clearlogoFilenames);
  }

  public void addSeasonPosterFilename(TvShowSeasonPosterNaming filename) {
    if (!seasonPosterFilenames.contains(filename)) {
      seasonPosterFilenames.add(filename);
      firePropertyChange(SEASON_POSTER_FILENAME, null, seasonPosterFilenames);
    }
  }

  public void clearSeasonPosterFilenames() {
    seasonPosterFilenames.clear();
    firePropertyChange(SEASON_POSTER_FILENAME, null, seasonPosterFilenames);
  }

  public List<TvShowSeasonPosterNaming> getSeasonPosterFilenames() {
    return new ArrayList<>(this.seasonPosterFilenames);
  }

  public void addSeasonBannerFilename(TvShowSeasonBannerNaming filename) {
    if (!seasonBannerFilenames.contains(filename)) {
      seasonBannerFilenames.add(filename);
      firePropertyChange(SEASON_BANNER_FILENAME, null, seasonBannerFilenames);
    }
  }

  public void clearSeasonBannerFilenames() {
    seasonBannerFilenames.clear();
    firePropertyChange(SEASON_BANNER_FILENAME, null, seasonBannerFilenames);
  }

  public List<TvShowSeasonBannerNaming> getSeasonBannerFilenames() {
    return new ArrayList<>(this.seasonBannerFilenames);
  }

  public void addSeasonThumbFilename(TvShowSeasonThumbNaming filename) {
    if (!seasonThumbFilenames.contains(filename)) {
      seasonThumbFilenames.add(filename);
      firePropertyChange(SEASON_THUMB_FILENAME, null, seasonThumbFilenames);
    }
  }

  public void clearSeasonThumbFilenames() {
    seasonThumbFilenames.clear();
    firePropertyChange(SEASON_THUMB_FILENAME, null, seasonThumbFilenames);
  }

  public List<TvShowSeasonThumbNaming> getSeasonThumbFilenames() {
    return new ArrayList<>(this.seasonThumbFilenames);
  }

  public CertificationStyle getCertificationStyle() {
    return certificationStyle;
  }

  public void setCertificationStyle(CertificationStyle newValue) {
    CertificationStyle oldValue = this.certificationStyle;
    this.certificationStyle = newValue;
    firePropertyChange("certificationStyle", oldValue, newValue);
  }

  public TvShowConnectors getTvShowConnector() {
    return tvShowConnector;
  }

  public void setTvShowConnector(TvShowConnectors newValue) {
    TvShowConnectors oldValue = this.tvShowConnector;
    this.tvShowConnector = newValue;
    firePropertyChange("tvShowConnector", oldValue, newValue);
  }

  public boolean isWriteCleanNfo() {
    return writeCleanNfo;
  }

  public void setWriteCleanNfo(boolean newValue) {
    boolean oldValue = this.writeCleanNfo;
    this.writeCleanNfo = newValue;
    firePropertyChange("writeCleanNfo", oldValue, newValue);
  }

  public MediaLanguages getNfoLanguage() {
    return nfoLanguage;
  }

  public void setNfoLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = nfoLanguage;
    this.nfoLanguage = newValue;
    firePropertyChange("nfoLanguage", oldValue, newValue);
  }

  public boolean getPreferPersonalRating() {
    return preferPersonalRating;
  }

  public void setPreferPersonalRating(boolean newValue) {
    boolean oldValue = this.preferPersonalRating;
    this.preferPersonalRating = newValue;
    firePropertyChange("preferPersonalRating", oldValue, newValue);
  }

  public String getPreferredRating() {
    return preferredRating;
  }

  public void setPreferredRating(String newValue) {
    String oldValue = this.preferredRating;
    this.preferredRating = newValue;
    firePropertyChange("preferredRating", oldValue, newValue);
  }

  public boolean isWriteActorImages() {
    return writeActorImages;
  }

  public void setWriteActorImages(boolean newValue) {
    boolean oldValue = this.writeActorImages;
    this.writeActorImages = newValue;
    firePropertyChange("writeActorImages", oldValue, newValue);
  }

  public void setRenameAfterScrape(boolean newValue) {
    boolean oldValue = this.renameAfterScrape;
    this.renameAfterScrape = newValue;
    firePropertyChange("renameAfterScrape", oldValue, newValue);
  }

  public boolean isRenameAfterScrape() {
    return this.renameAfterScrape;
  }

  /*****************************************************************
   * defaults
   *****************************************************************/

  /**
   * XBMC/Kodi <17 defaults
   */
  public void setDefaultSettingsForXbmc() {
    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.THUMB);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_POSTER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_BANNER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_THUMB);

    episodeThumbFilenames.clear();
    episodeThumbFilenames.add(TvShowEpisodeThumbNaming.FILENAME_THUMB);

    // other settings
    setTvShowConnector(TvShowConnectors.XBMC);
    setRenamerTvShowFoldername(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerSeasonFoldername(DEFAULT_RENAMER_SEASON_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.LARGE);

    firePropertyChange("preset", false, true);
  }

  /**
   * Kodi 17+ defaults
   */
  public void setDefaultSettingsForKodi() {
    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.THUMB);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_POSTER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_BANNER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_THUMB);

    episodeThumbFilenames.clear();
    episodeThumbFilenames.add(TvShowEpisodeThumbNaming.FILENAME_LANDSCAPE);

    // other settings
    setTvShowConnector(TvShowConnectors.KODI);
    setRenamerTvShowFoldername(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerSeasonFoldername(DEFAULT_RENAMER_SEASON_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.LARGE);

    firePropertyChange("preset", false, true);
  }

  /**
   * MediaPortal defaults
   */
  public void setDefaultSettingsForMediaPortal() {
    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.THUMB);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_POSTER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_BANNER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_THUMB);

    episodeThumbFilenames.clear();
    episodeThumbFilenames.add(TvShowEpisodeThumbNaming.FILENAME);

    // other settings
    setTvShowConnector(TvShowConnectors.XBMC);
    setRenamerTvShowFoldername(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerSeasonFoldername(DEFAULT_RENAMER_SEASON_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.TECHNICAL);

    firePropertyChange("preset", false, true);
  }

  /**
   * Plex defaults
   */
  public void setDefaultSettingsForPlex() {
    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.THUMB);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_FOLDER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_FOLDER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_FOLDER);

    episodeThumbFilenames.clear();
    episodeThumbFilenames.add(TvShowEpisodeThumbNaming.FILENAME);

    // other settings
    setTvShowConnector(TvShowConnectors.XBMC);
    setRenamerTvShowFoldername(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerSeasonFoldername(DEFAULT_RENAMER_SEASON_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.SHORT);

    firePropertyChange("preset", false, true);
  }
}
