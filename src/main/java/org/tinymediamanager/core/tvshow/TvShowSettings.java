/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.DateField;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractSettings;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.TrailerQuality;
import org.tinymediamanager.core.TrailerSources;
import org.tinymediamanager.core.tvshow.connector.TvShowConnectors;
import org.tinymediamanager.core.tvshow.filenaming.TvShowBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowCharacterartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearlogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowDiscartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeNfoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowFanartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowKeyartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowLogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowNfoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowTrailerNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaLanguages;

import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The Class TvShowSettings.
 *
 * @author Manuel Laggner
 */
public class TvShowSettings extends AbstractSettings {
  private static final Logger                      LOGGER                                 = LoggerFactory.getLogger(TvShowSettings.class);

  public static final String                       DEFAULT_RENAMER_FOLDER_PATTERN         = "${showTitle} (${showYear})";
  public static final String                       DEFAULT_RENAMER_SEASON_PATTERN         = "Season ${seasonNr}";
  public static final String                       DEFAULT_RENAMER_FILE_PATTERN           = "${showTitle} - S${seasonNr2}E${episodeNr2} - ${title}";

  private static final String                      CONFIG_FILE                            = "tvShows.json";

  private static TvShowSettings                    instance;

  /**
   * Constants mainly for events
   */
  private static final String                      TV_SHOW_DATA_SOURCE                    = "tvShowDataSource";
  private static final String                      ARTWORK_SCRAPERS                       = "artworkScrapers";
  private static final String                      TRAILER_SCRAPERS                       = "trailerScrapers";
  private static final String                      TRAILER_FILENAME                       = "trailerFilename";

  private static final String                      CERTIFICATION_COUNTRY                  = "certificationCountry";
  private static final String                      RENAMER_SEASON_FOLDER                  = "renamerSeasonFoldername";
  private static final String                      BAD_WORD                               = "badWord";
  private static final String                      SKIP_FOLDER                            = "skipFolder";
  private static final String                      SUBTITLE_SCRAPERS                      = "subtitleScrapers";
  private static final String                      UI_FILTERS                             = "uiFilters";
  private static final String                      NFO_FILENAME                           = "nfoFilename";
  private static final String                      POSTER_FILENAME                        = "posterFilename";
  private static final String                      FANART_FILENAME                        = "fanartFilename";
  private static final String                      BANNER_FILENAME                        = "bannerFilename";
  private static final String                      DISCART_FILENAME                       = "discartFilename";
  private static final String                      CLEARART_FILENAME                      = "clearartFilename";
  private static final String                      THUMB_FILENAME                         = "thumbFilename";
  private static final String                      LOGO_FILENAME                          = "logoFilename";
  private static final String                      CLEARLOGO_FILENAME                     = "clearlogoFilename";
  private static final String                      CHARACTERART_FILENAME                  = "characterartFilename";
  private static final String                      KEYART_FILENAME                        = "keyartFilename";
  private static final String                      SEASON_POSTER_FILENAME                 = "seasonPosterFilename";
  private static final String                      SEASON_BANNER_FILENAME                 = "seasonBannerFilename";
  private static final String                      SEASON_THUMB_FILENAME                  = "seasonThumbFilename";
  private static final String                      EPISODE_NFO_FILENAME                   = "episodeNfoFilename";
  private static final String                      EPISODE_THUMB_FILENAME                 = "episodeThumbFilename";
  private static final String                      EPISODE_CHECK_IMAGES                   = "episodeCheckImages";
  private static final String                      SEASON_CHECK_IMAGES                    = "seasonCheckImages";
  private static final String                      TVSHOW_CHECK_IMAGES                    = "TvShowCheckImages";

  private final List<String>                       tvShowDataSources                      = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                       badWords                               = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                       artworkScrapers                        = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                       trailerScrapers                        = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                       skipFolders                            = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>                       subtitleScrapers                       = ObservableCollections.observableList(new ArrayList<>());
  private final List<TvShowNfoNaming>              nfoFilenames                           = new ArrayList<>();
  private final List<TvShowPosterNaming>           posterFilenames                        = new ArrayList<>();
  private final List<TvShowFanartNaming>           fanartFilenames                        = new ArrayList<>();
  private final List<TvShowBannerNaming>           bannerFilenames                        = new ArrayList<>();
  private final List<TvShowDiscartNaming>          discartFilenames                       = new ArrayList<>();
  private final List<TvShowClearartNaming>         clearartFilenames                      = new ArrayList<>();
  private final List<TvShowThumbNaming>            thumbFilenames                         = new ArrayList<>();
  private final List<TvShowClearlogoNaming>        clearlogoFilenames                     = new ArrayList<>();
  private final List<TvShowLogoNaming>             logoFilenames                          = new ArrayList<>();
  private final List<TvShowCharacterartNaming>     characterartFilenames                  = new ArrayList<>();
  private final List<TvShowKeyartNaming>           keyartFilenames                        = new ArrayList<>();
  private final List<TvShowSeasonPosterNaming>     seasonPosterFilenames                  = new ArrayList<>();
  private final List<TvShowSeasonBannerNaming>     seasonBannerFilenames                  = new ArrayList<>();
  private final List<TvShowSeasonThumbNaming>      seasonThumbFilenames                   = new ArrayList<>();
  private final List<TvShowEpisodeNfoNaming>       episodeNfoFilenames                    = new ArrayList<>();
  private final List<TvShowEpisodeThumbNaming>     episodeThumbFilenames                  = new ArrayList<>();
  private final List<MediaArtworkType>             episodeCheckImages                     = new ArrayList<>();
  private final List<MediaArtworkType>             seasonCheckImages                      = new ArrayList<>();
  private final List<MediaArtworkType>             tvShowCheckImages                      = new ArrayList<>();
  private final List<TvShowTrailerNaming>          trailerFilenames                       = new ArrayList<>();

  private List<UIFilters>                          uiFilters                              = new ArrayList<>();
  private final List<String>                       tvShowTableHiddenColumns               = ObservableCollections.observableList(new ArrayList<>());

  // data sources / NFO settings
  private TvShowConnectors                         tvShowConnector                        = TvShowConnectors.XBMC;
  private CertificationStyle                       certificationStyle                     = CertificationStyle.LARGE;
  private boolean                                  writeCleanNfo                          = false;
  private DateField                                nfoDateAddedField                      = DateField.DATE_ADDED;
  private MediaLanguages                           nfoLanguage                            = MediaLanguages.en;

  // renamer
  private boolean                                  renameAfterScrape                      = false;
  private String                                   renamerTvShowFoldername                = DEFAULT_RENAMER_FOLDER_PATTERN;
  private String                                   renamerSeasonFoldername                = DEFAULT_RENAMER_SEASON_PATTERN;
  private String                                   renamerFilename                        = DEFAULT_RENAMER_FILE_PATTERN;
  private boolean                                  renamerShowPathnameSpaceSubstitution   = false;
  private String                                   renamerShowPathnameSpaceReplacement    = "_";
  private boolean                                  renamerSeasonPathnameSpaceSubstitution = false;
  private String                                   renamerSeasonPathnameSpaceReplacement  = "_";
  private boolean                                  renamerFilenameSpaceSubstitution       = false;
  private String                                   renamerFilenameSpaceReplacement        = "_";
  private String                                   renamerColonReplacement                = "";
  private boolean                                  asciiReplacement                       = false;
  private boolean                                  specialSeason                          = true;

  // meta data scraper
  private String                                   scraper                                = Constants.TVDB;
  private MediaLanguages                           scraperLanguage                        = MediaLanguages.en;
  private CountryCode                              certificationCountry                   = CountryCode.US;
  private List<TvShowScraperMetadataConfig>        tvShowScraperMetadataConfig            = new ArrayList<>();
  private List<TvShowEpisodeScraperMetadataConfig> episodeScraperMetadataConfig           = new ArrayList<>();

  // artwork scraper
  private MediaLanguages                           imageScraperLanguage                   = MediaLanguages.en;
  private MediaArtwork.PosterSizes                 imagePosterSize                        = MediaArtwork.PosterSizes.LARGE;
  private MediaArtwork.FanartSizes                 imageFanartSize                        = MediaArtwork.FanartSizes.LARGE;
  private boolean                                  scrapeBestImage                        = true;
  private boolean                                  writeActorImages                       = false;
  private boolean                                  imageExtraFanart                       = false;
  private int                                      imageExtraFanartCount                  = 5;

  // trailer scraper
  private boolean                                  useTrailerPreference                   = true;
  private boolean                                  automaticTrailerDownload               = false;
  private TrailerQuality                           trailerQuality                         = TrailerQuality.HD_720;
  private TrailerSources                           trailerSource                          = TrailerSources.YOUTUBE;

  // subtitle scraper
  private MediaLanguages                           subtitleScraperLanguage                = MediaLanguages.en;
  private LanguageStyle                            subtitleLanguageStyle                  = LanguageStyle.ISO3T;

  // misc
  private boolean                                  buildImageCacheOnImport                = false;
  private boolean                                  syncTrakt                              = false;
  private boolean                                  dvdOrder                               = false;
  private boolean                                  preferPersonalRating                   = true;
  private String                                   preferredRating                        = "tvdb";
  private boolean                                  extractArtworkFromVsmeta               = false;

  // ui
  private boolean                                  storeUiFilters                         = false;
  private boolean                                  displayMissingEpisodes                 = false;
  private boolean                                  displayMissingSpecials                 = false;
  private boolean                                  capitalWordsinTitles                   = false;
  private boolean                                  showLogosPanel                         = true;

  public TvShowSettings() {
    super();

    // add default entries to the lists - they will be overwritten by jackson later
    addDefaultEntries();

    addPropertyChangeListener(evt -> setDirty());
  }

  private void addDefaultEntries() {
    nfoFilenames.clear();
    addNfoFilename(TvShowNfoNaming.TV_SHOW);

    posterFilenames.clear();
    addPosterFilename(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    addFanartFilename(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    addBannerFilename(TvShowBannerNaming.BANNER);

    discartFilenames.clear();
    addDiscartFilename(TvShowDiscartNaming.DISCART);

    clearartFilenames.clear();
    addClearartFilename(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    addLogoFilename(TvShowLogoNaming.LOGO);

    characterartFilenames.clear();
    addCharacterartFilename(TvShowCharacterartNaming.CHARACTERART);

    clearlogoFilenames.clear();
    addClearlogoFilename(TvShowClearlogoNaming.CLEARLOGO);

    thumbFilenames.clear();
    addThumbFilename(TvShowThumbNaming.THUMB);

    keyartFilenames.clear();
    addKeyartFilename(TvShowKeyartNaming.KEYART);

    seasonPosterFilenames.clear();
    addSeasonPosterFilename(TvShowSeasonPosterNaming.SEASON_POSTER);

    seasonBannerFilenames.clear();
    addSeasonBannerFilename(TvShowSeasonBannerNaming.SEASON_BANNER);

    seasonThumbFilenames.clear();
    addSeasonThumbFilename(TvShowSeasonThumbNaming.SEASON_THUMB);

    episodeNfoFilenames.clear();
    addEpisodeNfoFilename(TvShowEpisodeNfoNaming.FILENAME);

    episodeThumbFilenames.clear();
    addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_THUMB);

    episodeCheckImages.clear();
    addEpisodeCheckImages(MediaArtworkType.THUMB);

    seasonCheckImages.clear();
    addSeasonCheckImages(MediaArtworkType.SEASON_POSTER);
    addSeasonCheckImages(MediaArtworkType.SEASON_BANNER);
    addSeasonCheckImages(MediaArtworkType.SEASON_THUMB);

    tvShowCheckImages.clear();
    addTvShowCheckImages(MediaArtworkType.POSTER);
    addTvShowCheckImages(MediaArtworkType.BACKGROUND);
    addTvShowCheckImages(MediaArtworkType.BANNER);

    trailerFilenames.clear();
    addTrailerFilename(TvShowTrailerNaming.TVSHOW_TRAILER);

    tvShowScraperMetadataConfig.addAll(Arrays.asList(TvShowScraperMetadataConfig.values()));
    episodeScraperMetadataConfig.addAll(Arrays.asList(TvShowEpisodeScraperMetadataConfig.values()));
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
  public static synchronized TvShowSettings getInstance() {
    return getInstance(Globals.settings.getSettingsFolder());
  }

  /**
   * Override our settings folder (defaults to "data")<br>
   * <b>Should only be used for unit testing et all!</b><br>
   *
   * @return single instance of TvShowSettings
   */
  public static synchronized TvShowSettings getInstance(String folder) {
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
    setTvShowTableHiddenColumns(Arrays.asList("format", "fileSize", "aired"));

    // activate default scrapers
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.SUBTITLE)) {
      addTvShowSubtitleScraper(ms.getId());
    }
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.TV_SHOW_ARTWORK)) {
      addTvShowArtworkScraper(ms.getId());
    }

    // set default languages based on java instance
    String defaultLang = Locale.getDefault().getLanguage();
    CountryCode cc = CountryCode.getByCode(defaultLang.toUpperCase(Locale.ROOT));
    if (cc != null) {
      setCertificationCountry(cc);
    }
    for (MediaLanguages ml : MediaLanguages.values()) {
      if (ml.name().equals(defaultLang)) {
        setScraperLanguage(ml);
      }
    }

    addDefaultEntries();
    saveSettings();

    // V2-to-V3 datasource migration
    Path mig = Paths.get("cache", "migv3shows.ds");
    if (mig.toFile().exists()) {
      try {
        List<String> datasources = Files.readAllLines(mig);
        for (String ds : datasources) {
          addTvShowDataSources(ds);
        }
        Files.delete(mig);
        saveSettings();
      }
      catch (IOException e) {
        LOGGER.warn("Could not migrate TV show datasources!", e);
      }
    }

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

  public void addTrailerFilename(TvShowTrailerNaming filename) {
    if (!trailerFilenames.contains(filename)) {
      trailerFilenames.add(filename);
      firePropertyChange(TRAILER_FILENAME, null, trailerFilenames);
    }
  }

  public void clearTrailerFilenames() {
    trailerFilenames.clear();
    firePropertyChange(TRAILER_FILENAME, null, trailerFilenames);
  }

  public List<TvShowTrailerNaming> getTrailerFilenames() {
    return new ArrayList<>(this.trailerFilenames);
  }

  public boolean isUseTrailerPreference() {
    return useTrailerPreference;
  }

  public void setUseTrailerPreference(boolean newValue) {
    boolean oldValue = this.useTrailerPreference;
    this.useTrailerPreference = newValue;
    firePropertyChange("useTrailerPreference", oldValue, newValue);
  }

  public boolean isAutomaticTrailerDownload() {
    return automaticTrailerDownload;
  }

  public void setAutomaticTrailerDownload(boolean newValue) {
    boolean oldValue = this.automaticTrailerDownload;
    this.automaticTrailerDownload = newValue;
    firePropertyChange("automaticTrailerDownload", oldValue, newValue);
  }

  public TrailerQuality getTrailerQuality() {
    return trailerQuality;
  }

  public void setTrailerQuality(TrailerQuality newValue) {
    TrailerQuality oldValue = this.trailerQuality;
    this.trailerQuality = newValue;
    firePropertyChange("trailerQuality", oldValue, newValue);
  }

  public TrailerSources getTrailerSource() {
    return trailerSource;
  }

  public void setTrailerSource(TrailerSources newValue) {
    TrailerSources oldValue = this.trailerSource;
    this.trailerSource = newValue;
    firePropertyChange("trailerSource", oldValue, newValue);
  }

  public void addTvShowTrailerScraper(String newValue) {
    if (!trailerScrapers.contains(newValue)) {
      trailerScrapers.add(newValue);
      firePropertyChange(TRAILER_SCRAPERS, null, trailerScrapers);
    }
  }

  public void removeTvShowTrailerScraper(String newValue) {
    if (trailerScrapers.contains(newValue)) {
      trailerScrapers.remove(newValue);
      firePropertyChange(TRAILER_SCRAPERS, null, trailerScrapers);
    }
  }

  public List<String> getTrailerScrapers() {
    return trailerScrapers;
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

  public boolean isExtractArtworkFromVsmeta() {
    return extractArtworkFromVsmeta;
  }

  public void setExtractArtworkFromVsmeta(boolean newValue) {
    boolean oldValue = this.extractArtworkFromVsmeta;
    this.extractArtworkFromVsmeta = newValue;
    firePropertyChange("extractArtworkFromVsmeta", oldValue, newValue);
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

  public String getRenamerShowPathnameSpaceReplacement() {
    return renamerShowPathnameSpaceReplacement;
  }

  public void setRenamerShowPathnameSpaceReplacement(String newValue) {
    String oldValue = this.renamerShowPathnameSpaceReplacement;
    this.renamerShowPathnameSpaceReplacement = newValue;
    firePropertyChange("renamerShowPathnameSpaceReplacement", oldValue, newValue);
  }

  public String getRenamerSeasonPathnameSpaceReplacement() {
    return renamerSeasonPathnameSpaceReplacement;
  }

  public void setRenamerSeasonPathnameSpaceReplacement(String newValue) {
    String oldValue = this.renamerSeasonPathnameSpaceReplacement;
    this.renamerSeasonPathnameSpaceReplacement = newValue;
    firePropertyChange("renamerSeasonPathnameSpaceReplacement", oldValue, newValue);
  }

  public String getRenamerFilenameSpaceReplacement() {
    return renamerFilenameSpaceReplacement;
  }

  public void setRenamerFilenameSpaceReplacement(String newValue) {
    String oldValue = this.renamerFilenameSpaceReplacement;
    this.renamerFilenameSpaceReplacement = newValue;
    firePropertyChange("renamerFilenameSpaceReplacement", oldValue, newValue);
  }

  public String getRenamerColonReplacement() {
    return renamerColonReplacement;
  }

  public void setRenamerColonReplacement(String newValue) {
    String oldValue = this.renamerColonReplacement;
    this.renamerColonReplacement = newValue;
    firePropertyChange("renamerColonReplacement", oldValue, newValue);
  }

  public boolean isRenamerShowPathnameSpaceSubstitution() {
    return renamerShowPathnameSpaceSubstitution;
  }

  public void setRenamerShowPathnameSpaceSubstitution(boolean newValue) {
    boolean oldValue = this.renamerShowPathnameSpaceSubstitution;
    this.renamerShowPathnameSpaceSubstitution = newValue;
    firePropertyChange("renamerShowPathnameSpaceSubstitution", oldValue, newValue);
  }

  public boolean isRenamerSeasonPathnameSpaceSubstitution() {
    return renamerSeasonPathnameSpaceSubstitution;
  }

  public void setRenamerSeasonPathnameSpaceSubstitution(boolean newValue) {
    boolean oldValue = this.renamerSeasonPathnameSpaceSubstitution;
    this.renamerSeasonPathnameSpaceSubstitution = newValue;
    firePropertyChange("renamereasonPathnameSpaceSubstitution", oldValue, newValue);
  }

  public boolean isRenamerFilenameSpaceSubstitution() {
    return renamerFilenameSpaceSubstitution;
  }

  public void setRenamerFilenameSpaceSubstitution(boolean newValue) {
    boolean oldValue = this.renamerFilenameSpaceSubstitution;
    this.renamerFilenameSpaceSubstitution = newValue;
    firePropertyChange("renamerFilenameSpaceSubstitution", oldValue, newValue);
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
    if (!badWords.contains(badWord.toLowerCase(Locale.ROOT))) {
      badWords.add(badWord.toLowerCase(Locale.ROOT));
      firePropertyChange(BAD_WORD, null, badWords);
    }
  }

  public void removeBadWord(String badWord) {
    badWords.remove(badWord.toLowerCase(Locale.ROOT));
    firePropertyChange(BAD_WORD, null, badWords);
  }

  public List<String> getBadWord() {
    // convert to lowercase for easy contains checking
    ListIterator<String> iterator = badWords.listIterator();
    while (iterator.hasNext()) {
      iterator.set(iterator.next().toLowerCase(Locale.ROOT));
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
    uiFilters.clear();
    uiFilters.addAll(filters);
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

  public boolean isDisplayMissingSpecials() {
    return displayMissingSpecials;
  }

  public void setDisplayMissingSpecials(boolean newValue) {
    boolean oldValue = this.displayMissingSpecials;
    this.displayMissingSpecials = newValue;
    firePropertyChange("displayMissingSpecials", oldValue, newValue);
  }

  /**
   * Gets the tv show scraper metadata config.
   *
   * @return the tv show scraper metadata config
   */
  public List<TvShowScraperMetadataConfig> getTvShowScraperMetadataConfig() {
    return tvShowScraperMetadataConfig;
  }

  /**
   * Sets the tv show scraper metadata config.
   *
   * @param tvShowScraperMetadataConfig
   *          the new tv show scraper metadata config
   */
  public void setTvShowScraperMetadataConfig(List<TvShowScraperMetadataConfig> tvShowScraperMetadataConfig) {
    this.tvShowScraperMetadataConfig.clear();
    this.tvShowScraperMetadataConfig.addAll(tvShowScraperMetadataConfig);
    firePropertyChange("scraperMetadataConfig", null, tvShowScraperMetadataConfig);
  }

  /**
   * Gets the episode scraper metadata config.
   *
   * @return the episode scraper metadata config
   */
  public List<TvShowEpisodeScraperMetadataConfig> getEpisodeScraperMetadataConfig() {
    return episodeScraperMetadataConfig;
  }

  /**
   * Sets the episode scraper metadata config.
   *
   * @param scraperMetadataConfig
   *          the new episode scraper metadata config
   */
  public void setEpisodeScraperMetadataConfig(List<TvShowEpisodeScraperMetadataConfig> scraperMetadataConfig) {
    this.episodeScraperMetadataConfig.clear();
    this.episodeScraperMetadataConfig.addAll(scraperMetadataConfig);
    firePropertyChange("episodeScraperMetadataConfig", null, episodeScraperMetadataConfig);
  }

  public MediaLanguages getImageScraperLanguage() {
    return imageScraperLanguage;
  }

  public void setImageScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.imageScraperLanguage;
    this.imageScraperLanguage = newValue;
    firePropertyChange("imageScraperLanguage", oldValue, newValue);
  }

  public MediaArtwork.PosterSizes getImagePosterSize() {
    return imagePosterSize;
  }

  public void setImagePosterSize(MediaArtwork.PosterSizes newValue) {
    MediaArtwork.PosterSizes oldValue = this.imagePosterSize;
    this.imagePosterSize = newValue;
    firePropertyChange("imagePosterSize", oldValue, newValue);
  }

  public MediaArtwork.FanartSizes getImageFanartSize() {
    return imageFanartSize;
  }

  public void setImageFanartSize(MediaArtwork.FanartSizes newValue) {
    MediaArtwork.FanartSizes oldValue = this.imageFanartSize;
    this.imageFanartSize = newValue;
    firePropertyChange("imageFanartSize", oldValue, newValue);
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

  public void addDiscartFilename(TvShowDiscartNaming filename) {
    if (!discartFilenames.contains(filename)) {
      discartFilenames.add(filename);
      firePropertyChange(DISCART_FILENAME, null, discartFilenames);
    }
  }

  public void clearDiscartFilenames() {
    discartFilenames.clear();
    firePropertyChange(DISCART_FILENAME, null, discartFilenames);
  }

  public List<TvShowDiscartNaming> getDiscartFilenames() {
    return new ArrayList<>(this.discartFilenames);
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

  public void addCharacterartFilename(TvShowCharacterartNaming filename) {
    if (!characterartFilenames.contains(filename)) {
      characterartFilenames.add(filename);
      firePropertyChange(CHARACTERART_FILENAME, null, characterartFilenames);
    }
  }

  public void clearCharacterartFilenames() {
    characterartFilenames.clear();
  }

  public List<TvShowCharacterartNaming> getCharacterartFilenames() {
    return characterartFilenames;
  }

  public void addKeyartFilename(TvShowKeyartNaming filename) {
    if (!keyartFilenames.contains(filename)) {
      keyartFilenames.add(filename);
      firePropertyChange(KEYART_FILENAME, null, keyartFilenames);
    }
  }

  public void clearKeyartFilenames() {
    keyartFilenames.clear();
    firePropertyChange(KEYART_FILENAME, null, keyartFilenames);
  }

  public List<TvShowKeyartNaming> getKeyartFilenames() {
    return keyartFilenames;
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

  public void addEpisodeNfoFilename(TvShowEpisodeNfoNaming filename) {
    if (!episodeNfoFilenames.contains(filename)) {
      episodeNfoFilenames.add(filename);
      firePropertyChange(EPISODE_NFO_FILENAME, null, episodeNfoFilenames);
    }
  }

  public void clearEpisodeNfoFilenames() {
    episodeNfoFilenames.clear();
    firePropertyChange(EPISODE_NFO_FILENAME, null, episodeNfoFilenames);
  }

  public List<TvShowEpisodeNfoNaming> getEpisodeNfoFilenames() {
    return new ArrayList<>(this.episodeNfoFilenames);
  }

  public void addEpisodeCheckImages(MediaArtworkType type) {
    if (!episodeCheckImages.contains(type)) {
      episodeCheckImages.add(type);
      firePropertyChange(EPISODE_CHECK_IMAGES, null, episodeCheckImages);
    }
  }

  public void clearEpisodeCheckImages() {
    episodeCheckImages.clear();
    firePropertyChange(EPISODE_CHECK_IMAGES, null, episodeCheckImages);
  }

  public List<MediaArtworkType> getEpisodeCheckImages() {
    return new ArrayList<>(this.episodeCheckImages);
  }

  public void addSeasonCheckImages(MediaArtworkType type) {
    if (!seasonCheckImages.contains(type)) {
      seasonCheckImages.add(type);
      firePropertyChange(SEASON_CHECK_IMAGES, null, seasonCheckImages);
    }
  }

  public void clearSeasonCheckImages() {
    seasonCheckImages.clear();
    firePropertyChange(SEASON_CHECK_IMAGES, null, seasonCheckImages);
  }

  public List<MediaArtworkType> getSeasonCheckImages() {
    return new ArrayList<>(this.seasonCheckImages);
  }

  public void addTvShowCheckImages(MediaArtworkType type) {
    if (!tvShowCheckImages.contains(type)) {
      tvShowCheckImages.add(type);
      firePropertyChange(TVSHOW_CHECK_IMAGES, null, tvShowCheckImages);
    }
  }

  public void clearTvShowCheckImages() {
    tvShowCheckImages.clear();
    firePropertyChange(TVSHOW_CHECK_IMAGES, null, tvShowCheckImages);
  }

  public List<MediaArtworkType> getTvShowCheckImages() {
    return new ArrayList<>(this.tvShowCheckImages);
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

  public DateField getNfoDateAddedField() {
    return nfoDateAddedField;
  }

  public void setNfoDateAddedField(DateField newValue) {
    DateField oldValue = nfoDateAddedField;
    this.nfoDateAddedField = newValue;
    firePropertyChange("nfoDateAddedField", oldValue, newValue);
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

  public int getImageExtraFanartCount() {
    return imageExtraFanartCount;
  }

  public void setImageExtraFanartCount(int newValue) {
    int oldValue = this.imageExtraFanartCount;
    this.imageExtraFanartCount = newValue;
    firePropertyChange("imageExtraFanartCount", oldValue, newValue);
  }

  public boolean isImageExtraFanart() {
    return imageExtraFanart;
  }

  public void setImageExtraFanart(boolean newValue) {
    boolean oldValue = this.imageExtraFanart;
    this.imageExtraFanart = newValue;
    firePropertyChange("imageExtraFanart", oldValue, newValue);
  }

  public boolean getCapitalWordsInTitles() {
    return capitalWordsinTitles;
  }

  public void setCapitalWordsInTitles(boolean newValue) {
    boolean oldValue = this.capitalWordsinTitles;
    this.capitalWordsinTitles = newValue;
    firePropertyChange("capitalWordsInTitles", oldValue, newValue);
  }

  public boolean isShowLogosPanel() {
    return showLogosPanel;
  }

  public void setShowLogosPanel(boolean newValue) {
    boolean oldValue = showLogosPanel;
    this.showLogosPanel = newValue;
    firePropertyChange("showLogosPanel", oldValue, newValue);
  }

  /*****************************************************************
   * defaults
   *****************************************************************/

  /**
   * XBMC/Kodi <17 defaults
   */
  public void setDefaultSettingsForXbmc() {
    nfoFilenames.clear();
    nfoFilenames.add(TvShowNfoNaming.TV_SHOW);

    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    discartFilenames.clear();
    discartFilenames.add(TvShowDiscartNaming.DISCART);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    characterartFilenames.clear();
    characterartFilenames.add(TvShowCharacterartNaming.CHARACTERART);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.THUMB);

    keyartFilenames.clear();
    keyartFilenames.add(TvShowKeyartNaming.KEYART);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_POSTER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_BANNER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_THUMB);

    episodeNfoFilenames.clear();
    episodeNfoFilenames.add(TvShowEpisodeNfoNaming.FILENAME);

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
    nfoFilenames.clear();
    nfoFilenames.add(TvShowNfoNaming.TV_SHOW);

    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    discartFilenames.clear();
    discartFilenames.add(TvShowDiscartNaming.DISCART);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    characterartFilenames.clear();
    characterartFilenames.add(TvShowCharacterartNaming.CHARACTERART);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.LANDSCAPE);

    keyartFilenames.clear();
    keyartFilenames.add(TvShowKeyartNaming.KEYART);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_POSTER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_BANNER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_THUMB);

    episodeNfoFilenames.clear();
    episodeNfoFilenames.add(TvShowEpisodeNfoNaming.FILENAME);

    episodeThumbFilenames.clear();
    episodeThumbFilenames.add(TvShowEpisodeThumbNaming.FILENAME_THUMB);

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
    nfoFilenames.clear();
    nfoFilenames.add(TvShowNfoNaming.TV_SHOW);

    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    discartFilenames.clear();
    discartFilenames.add(TvShowDiscartNaming.DISCART);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    characterartFilenames.clear();
    characterartFilenames.add(TvShowCharacterartNaming.CHARACTERART);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.THUMB);

    keyartFilenames.clear();
    keyartFilenames.add(TvShowKeyartNaming.KEYART);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_POSTER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_BANNER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_THUMB);

    episodeNfoFilenames.clear();
    episodeNfoFilenames.add(TvShowEpisodeNfoNaming.FILENAME);

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
    nfoFilenames.clear();
    nfoFilenames.add(TvShowNfoNaming.TV_SHOW);

    posterFilenames.clear();
    posterFilenames.add(TvShowPosterNaming.POSTER);

    fanartFilenames.clear();
    fanartFilenames.add(TvShowFanartNaming.FANART);

    bannerFilenames.clear();
    bannerFilenames.add(TvShowBannerNaming.BANNER);

    discartFilenames.clear();
    discartFilenames.add(TvShowDiscartNaming.DISCART);

    clearartFilenames.clear();
    clearartFilenames.add(TvShowClearartNaming.CLEARART);

    logoFilenames.clear();
    logoFilenames.add(TvShowLogoNaming.LOGO);

    clearlogoFilenames.clear();
    clearlogoFilenames.add(TvShowClearlogoNaming.CLEARLOGO);

    characterartFilenames.clear();
    characterartFilenames.add(TvShowCharacterartNaming.CHARACTERART);

    thumbFilenames.clear();
    thumbFilenames.add(TvShowThumbNaming.THUMB);

    keyartFilenames.clear();
    keyartFilenames.add(TvShowKeyartNaming.KEYART);

    seasonPosterFilenames.clear();
    seasonPosterFilenames.add(TvShowSeasonPosterNaming.SEASON_FOLDER);

    seasonBannerFilenames.clear();
    seasonBannerFilenames.add(TvShowSeasonBannerNaming.SEASON_FOLDER);

    seasonThumbFilenames.clear();
    seasonThumbFilenames.add(TvShowSeasonThumbNaming.SEASON_FOLDER);

    episodeNfoFilenames.clear();
    episodeNfoFilenames.add(TvShowEpisodeNfoNaming.FILENAME);

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

  /**
   * set the default scrapers for the movie module
   */
  public void setDefaultScrapers() {
    // activate default scrapers
    artworkScrapers.clear();
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.TV_SHOW_ARTWORK)) {
      addTvShowArtworkScraper(ms.getId());
    }

    trailerScrapers.clear();
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.TVSHOW_TRAILER)) {
      addTvShowTrailerScraper(ms.getId());
    }

    subtitleScrapers.clear();
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.SUBTITLE)) {
      addTvShowSubtitleScraper(ms.getId());
    }
  }
}
