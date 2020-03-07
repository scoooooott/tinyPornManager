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
package org.tinymediamanager.core.movie;

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
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.filenaming.MovieBannerNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearlogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieDiscartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieFanartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieKeyartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieLogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;
import org.tinymediamanager.core.movie.filenaming.MoviePosterNaming;
import org.tinymediamanager.core.movie.filenaming.MovieThumbNaming;
import org.tinymediamanager.core.movie.filenaming.MovieTrailerNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.entities.MediaLanguages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * The Class MovieSettings.
 */
public class MovieSettings extends AbstractSettings {
  private static final Logger              LOGGER                              = LoggerFactory.getLogger(MovieSettings.class);

  public static final String               DEFAULT_RENAMER_FOLDER_PATTERN      = "${title} ${- ,edition,} (${year})";
  public static final String               DEFAULT_RENAMER_FILE_PATTERN        = "${title} ${- ,edition,} (${year}) ${videoFormat} ${audioCodec}";

  private static final String              CONFIG_FILE                         = "movies.json";

  private static MovieSettings             instance;

  /**
   * Constants mainly for events
   */
  private static final String              MOVIE_DATA_SOURCE                   = "movieDataSource";
  private static final String              NFO_FILENAME                        = "nfoFilename";
  private static final String              POSTER_FILENAME                     = "posterFilename";
  private static final String              FANART_FILENAME                     = "fanartFilename";
  private static final String              BANNER_FILENAME                     = "bannerFilename";
  private static final String              CLEARART_FILENAME                   = "clearartFilename";
  private static final String              THUMB_FILENAME                      = "thumbFilename";
  private static final String              LOGO_FILENAME                       = "logoFilename";
  private static final String              CLEARLOGO_FILENAME                  = "clearlogoFilename";
  private static final String              DISCART_FILENAME                    = "discartFilename";
  private static final String              KEYART_FILENAME                     = "keyartFilename";
  private static final String              TRAILER_FILENAME                    = "trailerFilename";
  private static final String              ARTWORK_SCRAPERS                    = "artworkScrapers";
  private static final String              TRAILER_SCRAPERS                    = "trailerScrapers";
  private static final String              SUBTITLE_SCRAPERS                   = "subtitleScrapers";
  private static final String              BAD_WORD                            = "badWord";
  private static final String              UI_FILTERS                          = "uiFilters";
  private static final String              MOVIE_SET_UI_FILTERS                = "movieSetUiFilters";
  private static final String              SKIP_FOLDER                         = "skipFolder";
  private static final String              CHECK_IMAGES_MOVIE                  = "checkImagesMovie";

  private final List<String>               movieDataSources                    = ObservableCollections.observableList(new ArrayList<>());
  private final List<MovieNfoNaming>       nfoFilenames                        = new ArrayList<>();
  private final List<MoviePosterNaming>    posterFilenames                     = new ArrayList<>();
  private final List<MovieFanartNaming>    fanartFilenames                     = new ArrayList<>();
  private final List<MovieBannerNaming>    bannerFilenames                     = new ArrayList<>();
  private final List<MovieClearartNaming>  clearartFilenames                   = new ArrayList<>();
  private final List<MovieThumbNaming>     thumbFilenames                      = new ArrayList<>();
  private final List<MovieClearlogoNaming> clearlogoFilenames                  = new ArrayList<>();
  private final List<MovieLogoNaming>      logoFilenames                       = new ArrayList<>();
  private final List<MovieDiscartNaming>   discartFilenames                    = new ArrayList<>();
  private final List<MovieKeyartNaming>    keyartFilenames                     = new ArrayList<>();
  private final List<MovieTrailerNaming>   trailerFilenames                    = new ArrayList<>();
  private final List<MediaArtworkType>     checkImagesMovie                    = new ArrayList<>();
  private final List<String>               badWords                            = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>               artworkScrapers                     = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>               trailerScrapers                     = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>               subtitleScrapers                    = ObservableCollections.observableList(new ArrayList<>());
  private final List<String>               skipFolders                         = ObservableCollections.observableList(new ArrayList<>());

  private final List<UIFilters>            uiFilters                           = new ArrayList<>();
  private final List<String>               movieTableHiddenColumns             = ObservableCollections.observableList(new ArrayList<>());
  private final List<UIFilters>            movieSetUiFilters                   = new ArrayList<>();
  private final List<String>               movieSetTableHiddenColumns          = ObservableCollections.observableList(new ArrayList<>());

  // data sources / NFO settings
  private boolean                          buildImageCacheOnImport             = false;
  private MovieConnectors                  movieConnector                      = MovieConnectors.KODI;
  private CertificationStyle               certificationStyle                  = CertificationStyle.LARGE;
  private boolean                          writeCleanNfo                       = false;
  private DateField                        nfoDateAddedField                   = DateField.DATE_ADDED;
  private MediaLanguages                   nfoLanguage                         = MediaLanguages.en;
  private boolean                          createOutline                       = true;
  private boolean                          outlineFirstSentence                = false;

  // renamer
  private boolean                          renameAfterScrape                   = false;
  private String                           renamerPathname                     = DEFAULT_RENAMER_FOLDER_PATTERN;
  private String                           renamerFilename                     = DEFAULT_RENAMER_FILE_PATTERN;
  private boolean                          renamerPathnameSpaceSubstitution    = false;
  private String                           renamerPathnameSpaceReplacement     = "_";
  private boolean                          renamerFilenameSpaceSubstitution    = false;
  private String                           renamerFilenameSpaceReplacement     = "_";
  private String                           renamerColonReplacement             = "-";
  private boolean                          renamerNfoCleanup                   = false;
  private boolean                          renamerCreateMoviesetForSingleMovie = false;
  private boolean                          asciiReplacement                    = false;

  // meta data scraper
  private String                           movieScraper                        = Constants.TMDB;
  private MediaLanguages                   scraperLanguage                     = MediaLanguages.en;
  private CountryCode                      certificationCountry                = CountryCode.US;
  private double                           scraperThreshold                    = 0.75;
  private boolean                          scraperFallback                     = false;
  private List<MovieScraperMetadataConfig> scraperMetadataConfig               = new ArrayList<>();
  private boolean                          capitalWordsInTitles                = false;

  // artwork scraper
  private PosterSizes                      imagePosterSize                     = PosterSizes.LARGE;
  private FanartSizes                      imageFanartSize                     = FanartSizes.LARGE;
  private boolean                          imageExtraThumbs                    = false;
  private boolean                          imageExtraThumbsResize              = true;
  private int                              imageExtraThumbsSize                = 300;
  private int                              imageExtraThumbsCount               = 5;
  private boolean                          imageExtraFanart                    = false;
  private int                              imageExtraFanartCount               = 5;
  private boolean                          enableMovieSetArtworkMovieFolder    = true;
  private boolean                          enableMovieSetArtworkFolder         = false;
  private String                           movieSetArtworkFolder               = "MoviesetArtwork";
  private boolean                          movieSetArtworkFolderStyleKodi      = true;
  private boolean                          movieSetArtworkFolderStyleAutomator = false;
  private boolean                          scrapeBestImage                     = true;
  private MediaLanguages                   imageScraperLanguage                = MediaLanguages.en;
  private boolean                          imageLanguagePriority               = true;
  private boolean                          writeActorImages                    = false;

  // trailer scraper
  private boolean                          useTrailerPreference                = true;
  private boolean                          automaticTrailerDownload            = false;
  private TrailerQuality                   trailerQuality                      = TrailerQuality.HD_720;
  private TrailerSources                   trailerSource                       = TrailerSources.YOUTUBE;

  // subtitle scraper
  private MediaLanguages                   subtitleScraperLanguage             = MediaLanguages.en;
  private LanguageStyle                    subtitleLanguageStyle               = LanguageStyle.ISO3T;
  private boolean                          subtitleWithoutLanguageTag          = false;

  // misc
  private boolean                          runtimeFromMediaInfo                = false;
  private boolean                          includeExternalAudioStreams         = false;
  private boolean                          syncTrakt                           = false;
  private boolean                          preferPersonalRating                = true;
  private String                           preferredRating                     = "imdb";
  private boolean                          extractArtworkFromVsmeta            = false;

  // ui
  private boolean                          storeUiFilters                      = false;
  private boolean                          showLogosPanel                      = true;

  public MovieSettings() {
    super();

    // add default entries to the lists - they will be overwritten by jackson later
    addDefaultEntries();

    addPropertyChangeListener(evt -> setDirty());
  }

  private void addDefaultEntries() {
    nfoFilenames.clear();
    addNfoFilename(MovieNfoNaming.MOVIE_NFO);

    posterFilenames.clear();
    addPosterFilename(MoviePosterNaming.POSTER);

    fanartFilenames.clear();
    addFanartFilename(MovieFanartNaming.FANART);

    bannerFilenames.clear();
    addBannerFilename(MovieBannerNaming.BANNER);

    clearartFilenames.clear();
    addClearartFilename(MovieClearartNaming.CLEARART);

    thumbFilenames.clear();
    addThumbFilename(MovieThumbNaming.THUMB);

    logoFilenames.clear();
    addLogoFilename(MovieLogoNaming.LOGO);

    clearlogoFilenames.clear();
    addClearlogoFilename(MovieClearlogoNaming.CLEARLOGO);

    discartFilenames.clear();
    addDiscartFilename(MovieDiscartNaming.DISC);

    keyartFilenames.clear();
    addKeyartFilename(MovieKeyartNaming.KEYART);

    trailerFilenames.clear();
    addTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER);

    checkImagesMovie.clear();
    addCheckImagesMovie(MediaArtworkType.POSTER);
    addCheckImagesMovie(MediaArtworkType.BACKGROUND);

    scraperMetadataConfig.addAll(Arrays.asList(MovieScraperMetadataConfig.values()));
  }

  @Override
  protected ObjectWriter createObjectWriter() {
    return objectMapper.writerFor(MovieSettings.class);
  }

  /**
   * Gets the single instance of MovieSettings.
   *
   * @return single instance of MovieSettings
   */
  public synchronized static MovieSettings getInstance() {
    return getInstance(Globals.settings.getSettingsFolder());
  }

  /**
   * Override our settings folder (defaults to "data")<br>
   * <b>Should only be used for unit testing et all!</b><br>
   *
   * @return single instance of MovieSettings
   */
  public synchronized static MovieSettings getInstance(String folder) {
    if (instance == null) {
      instance = (MovieSettings) getInstance(folder, CONFIG_FILE, MovieSettings.class);
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
    // hidden columns
    setMovieTableHiddenColumns(Arrays.asList("originalTitle", "sortTitle", "dateAdded", "filename", "path", "movieset", "fileSize", "audio",
        "video3d",
        "videoFormat", "votes", "edition", "mediaSource", "certification"));

    addDefaultEntries();

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
    saveSettings();

    // V2-to-V3 datasource migration
    Path mig = Paths.get("cache", "migv3movies.ds");
    if (mig.toFile().exists()) {
      try {
        List<String> datasources = Files.readAllLines(mig);
        for (String ds : datasources) {
          addMovieDataSources(ds);
        }
        Files.delete(mig);
        saveSettings();
      }
      catch (IOException e) {
        LOGGER.warn("Could not migrate movie datasources! {}", e);
      }
    }

  }

  public void addMovieDataSources(String path) {
    if (!movieDataSources.contains(path)) {
      movieDataSources.add(path);
      firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
      firePropertyChange(Constants.DATA_SOURCE, null, movieDataSources);
    }
  }

  public void removeMovieDataSources(String path) {
    MovieList movieList = MovieList.getInstance();
    movieList.removeDatasource(path);
    movieDataSources.remove(path);
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
    firePropertyChange(Constants.DATA_SOURCE, null, movieDataSources);
  }

  public List<String> getMovieDataSource() {
    return movieDataSources;
  }

  public void addNfoFilename(MovieNfoNaming filename) {
    if (!nfoFilenames.contains(filename)) {
      nfoFilenames.add(filename);
      firePropertyChange(NFO_FILENAME, null, nfoFilenames);
    }
  }

  public void clearNfoFilenames() {
    nfoFilenames.clear();
    firePropertyChange(NFO_FILENAME, null, nfoFilenames);
  }

  public List<MovieNfoNaming> getNfoFilenames() {
    return new ArrayList<>(this.nfoFilenames);
  }

  public void addTrailerFilename(MovieTrailerNaming filename) {
    if (!trailerFilenames.contains(filename)) {
      trailerFilenames.add(filename);
      firePropertyChange(TRAILER_FILENAME, null, trailerFilenames);
    }
  }

  public void clearTrailerFilenames() {
    trailerFilenames.clear();
    firePropertyChange(TRAILER_FILENAME, null, trailerFilenames);
  }

  public List<MovieTrailerNaming> getTrailerFilenames() {
    return new ArrayList<>(this.trailerFilenames);
  }

  public void addPosterFilename(MoviePosterNaming filename) {
    if (!posterFilenames.contains(filename)) {
      posterFilenames.add(filename);
      firePropertyChange(POSTER_FILENAME, null, posterFilenames);
    }
  }

  public void clearPosterFilenames() {
    posterFilenames.clear();
    firePropertyChange(POSTER_FILENAME, null, posterFilenames);
  }

  public List<MoviePosterNaming> getPosterFilenames() {
    return new ArrayList<>(this.posterFilenames);
  }

  public void addFanartFilename(MovieFanartNaming filename) {
    if (!fanartFilenames.contains(filename)) {
      fanartFilenames.add(filename);
      firePropertyChange(FANART_FILENAME, null, fanartFilenames);
    }
  }

  public void clearFanartFilenames() {
    fanartFilenames.clear();
    firePropertyChange(FANART_FILENAME, null, fanartFilenames);
  }

  public List<MovieFanartNaming> getFanartFilenames() {
    return new ArrayList<>(this.fanartFilenames);
  }

  public void addBannerFilename(MovieBannerNaming filename) {
    if (!bannerFilenames.contains(filename)) {
      bannerFilenames.add(filename);
      firePropertyChange(BANNER_FILENAME, null, bannerFilenames);
    }
  }

  public void clearBannerFilenames() {
    bannerFilenames.clear();
    firePropertyChange(BANNER_FILENAME, null, bannerFilenames);
  }

  public List<MovieBannerNaming> getBannerFilenames() {
    return new ArrayList<>(this.bannerFilenames);
  }

  public void addClearartFilename(MovieClearartNaming filename) {
    if (!clearartFilenames.contains(filename)) {
      clearartFilenames.add(filename);
      firePropertyChange(CLEARART_FILENAME, null, clearartFilenames);
    }
  }

  public void clearClearartFilenames() {
    clearartFilenames.clear();
    firePropertyChange(CLEARART_FILENAME, null, clearartFilenames);
  }

  public List<MovieClearartNaming> getClearartFilenames() {
    return new ArrayList<>(this.clearartFilenames);
  }

  public void addThumbFilename(MovieThumbNaming filename) {
    if (!thumbFilenames.contains(filename)) {
      thumbFilenames.add(filename);
      firePropertyChange(THUMB_FILENAME, null, thumbFilenames);
    }
  }

  public void clearThumbFilenames() {
    thumbFilenames.clear();
    firePropertyChange(THUMB_FILENAME, null, thumbFilenames);
  }

  public List<MovieThumbNaming> getThumbFilenames() {
    return new ArrayList<>(this.thumbFilenames);
  }

  public void addLogoFilename(MovieLogoNaming filename) {
    if (!logoFilenames.contains(filename)) {
      logoFilenames.add(filename);
      firePropertyChange(LOGO_FILENAME, null, logoFilenames);
    }
  }

  public void clearLogoFilenames() {
    logoFilenames.clear();
    firePropertyChange(LOGO_FILENAME, null, logoFilenames);
  }

  public List<MovieLogoNaming> getLogoFilenames() {
    return new ArrayList<>(this.logoFilenames);
  }

  public void addClearlogoFilename(MovieClearlogoNaming filename) {
    if (!clearlogoFilenames.contains(filename)) {
      clearlogoFilenames.add(filename);
      firePropertyChange(CLEARLOGO_FILENAME, null, clearlogoFilenames);
    }
  }

  public void clearClearlogoFilenames() {
    clearlogoFilenames.clear();
    firePropertyChange(CLEARLOGO_FILENAME, null, clearlogoFilenames);
  }

  public List<MovieClearlogoNaming> getClearlogoFilenames() {
    return new ArrayList<>(this.clearlogoFilenames);
  }

  public void addDiscartFilename(MovieDiscartNaming filename) {
    if (!discartFilenames.contains(filename)) {
      discartFilenames.add(filename);
      firePropertyChange(DISCART_FILENAME, null, discartFilenames);
    }
  }

  public void clearDiscartFilenames() {
    discartFilenames.clear();
    firePropertyChange(DISCART_FILENAME, null, discartFilenames);
  }

  public void clearCheckImagesMovie() {
    checkImagesMovie.clear();
    firePropertyChange(CHECK_IMAGES_MOVIE, null, checkImagesMovie);
  }

  public List<MediaArtworkType> getCheckImagesMovie() {
    return new ArrayList<>(this.checkImagesMovie);
  }

  public void addCheckImagesMovie(MediaArtworkType type) {
    if (!checkImagesMovie.contains(type)) {
      checkImagesMovie.add(type);
      firePropertyChange(CHECK_IMAGES_MOVIE, null, checkImagesMovie);
    }
  }

  public List<MovieDiscartNaming> getDiscartFilenames() {
    return new ArrayList<>(this.discartFilenames);
  }

  public void addKeyartFilename(MovieKeyartNaming filename) {
    if (!keyartFilenames.contains(filename)) {
      keyartFilenames.add(filename);
      firePropertyChange(KEYART_FILENAME, null, keyartFilenames);
    }
  }

  public void clearKeyartFilenames() {
    keyartFilenames.clear();
    firePropertyChange(KEYART_FILENAME, null, keyartFilenames);
  }

  public List<MovieKeyartNaming> getKeyartFilenames() {
    return keyartFilenames;
  }

  public PosterSizes getImagePosterSize() {
    return imagePosterSize;
  }

  public void setImagePosterSize(PosterSizes newValue) {
    PosterSizes oldValue = this.imagePosterSize;
    this.imagePosterSize = newValue;
    firePropertyChange("imagePosterSize", oldValue, newValue);
  }

  public FanartSizes getImageFanartSize() {
    return imageFanartSize;
  }

  public void setImageFanartSize(FanartSizes newValue) {
    FanartSizes oldValue = this.imageFanartSize;
    this.imageFanartSize = newValue;
    firePropertyChange("imageFanartSize", oldValue, newValue);
  }

  public boolean isImageExtraThumbs() {
    return imageExtraThumbs;
  }

  public boolean isImageExtraThumbsResize() {
    return imageExtraThumbsResize;
  }

  public int getImageExtraThumbsSize() {
    return imageExtraThumbsSize;
  }

  public void setImageExtraThumbsResize(boolean newValue) {
    boolean oldValue = this.imageExtraThumbsResize;
    this.imageExtraThumbsResize = newValue;
    firePropertyChange("imageExtraThumbsResize", oldValue, newValue);
  }

  public void setImageExtraThumbsSize(int newValue) {
    int oldValue = this.imageExtraThumbsSize;
    this.imageExtraThumbsSize = newValue;
    firePropertyChange("imageExtraThumbsSize", oldValue, newValue);
  }

  public int getImageExtraThumbsCount() {
    return imageExtraThumbsCount;
  }

  public void setImageExtraThumbsCount(int newValue) {
    int oldValue = this.imageExtraThumbsCount;
    this.imageExtraThumbsCount = newValue;
    firePropertyChange("imageExtraThumbsCount", oldValue, newValue);
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

  public void setImageExtraThumbs(boolean newValue) {
    boolean oldValue = this.imageExtraThumbs;
    this.imageExtraThumbs = newValue;
    firePropertyChange("imageExtraThumbs", oldValue, newValue);
  }

  public void setImageExtraFanart(boolean newValue) {
    boolean oldValue = this.imageExtraFanart;
    this.imageExtraFanart = newValue;
    firePropertyChange("imageExtraFanart", oldValue, newValue);
  }

  public boolean isEnableMovieSetArtworkMovieFolder() {
    return enableMovieSetArtworkMovieFolder;
  }

  public void setEnableMovieSetArtworkMovieFolder(boolean newValue) {
    boolean oldValue = this.enableMovieSetArtworkMovieFolder;
    this.enableMovieSetArtworkMovieFolder = newValue;
    firePropertyChange("enableMovieSetArtworkMovieFolder", oldValue, newValue);
  }

  public boolean isEnableMovieSetArtworkFolder() {
    return enableMovieSetArtworkFolder;
  }

  public void setEnableMovieSetArtworkFolder(boolean newValue) {
    boolean oldValue = this.enableMovieSetArtworkFolder;
    this.enableMovieSetArtworkFolder = newValue;
    firePropertyChange("enableMovieSetArtworkFolder", oldValue, newValue);
  }

  public String getMovieSetArtworkFolder() {
    return movieSetArtworkFolder;
  }

  public void setMovieSetArtworkFolder(String newValue) {
    String oldValue = this.movieSetArtworkFolder;
    this.movieSetArtworkFolder = newValue;
    firePropertyChange("movieSetArtworkFolder", oldValue, newValue);
  }

  public boolean isMovieSetArtworkFolderStyleKodi() {
    return movieSetArtworkFolderStyleKodi;
  }

  public void setMovieSetArtworkFolderStyleKodi(boolean newValue) {
    boolean oldValue = this.movieSetArtworkFolderStyleKodi;
    this.movieSetArtworkFolderStyleKodi = newValue;
    firePropertyChange("movieSetArtworkFolderStyleKodi", oldValue, newValue);
  }

  public boolean isMovieSetArtworkFolderStyleAutomator() {
    return movieSetArtworkFolderStyleAutomator;
  }

  public void setMovieSetArtworkFolderStyleAutomator(boolean newValue) {
    boolean oldValue = this.movieSetArtworkFolderStyleAutomator;
    this.movieSetArtworkFolderStyleAutomator = newValue;
    firePropertyChange("movieSetArtworkFolderStyleAutomator", oldValue, newValue);
  }

  public MovieConnectors getMovieConnector() {
    return movieConnector;
  }

  public void setMovieConnector(MovieConnectors newValue) {
    MovieConnectors oldValue = this.movieConnector;
    this.movieConnector = newValue;
    firePropertyChange("movieConnector", oldValue, newValue);
  }

  public String getRenamerPathname() {
    return renamerPathname;
  }

  public void setRenamerPathname(String newValue) {
    String oldValue = this.renamerPathname;
    this.renamerPathname = newValue;
    firePropertyChange("renamerPathname", oldValue, newValue);
  }

  public String getRenamerFilename() {
    return renamerFilename;
  }

  public void setRenamerFilename(String newValue) {
    String oldValue = this.renamerFilename;
    this.renamerFilename = newValue;
    firePropertyChange("renamerFilename", oldValue, newValue);
  }

  public boolean isRenamerPathnameSpaceSubstitution() {
    return renamerPathnameSpaceSubstitution;
  }

  public void setRenamerPathnameSpaceSubstitution(boolean newValue) {
    boolean oldValue = this.renamerPathnameSpaceSubstitution;
    this.renamerPathnameSpaceSubstitution = newValue;
    firePropertyChange("renamerPathnameSpaceSubstitution", oldValue, newValue);
  }

  public boolean isRenamerFilenameSpaceSubstitution() {
    return renamerFilenameSpaceSubstitution;
  }

  @JsonProperty(value = "renamerSpaceSubstitution")
  public void setRenamerFilenameSpaceSubstitution(boolean newValue) {
    boolean oldValue = this.renamerFilenameSpaceSubstitution;
    this.renamerFilenameSpaceSubstitution = newValue;
    firePropertyChange("renamerFilenameSpaceSubstitution", oldValue, newValue);
  }

  public void setRenameAfterScrape(boolean newValue) {
    boolean oldValue = this.renameAfterScrape;
    this.renameAfterScrape = newValue;
    firePropertyChange("renameAfterScrape", oldValue, newValue);
  }

  public boolean isRenameAfterScrape() {
    return this.renameAfterScrape;
  }

  public String getRenamerPathnameSpaceReplacement() {
    return renamerPathnameSpaceReplacement;
  }

  public void setRenamerPathnameSpaceReplacement(String newValue) {
    String oldValue = this.renamerPathnameSpaceReplacement;
    this.renamerPathnameSpaceReplacement = newValue;
    firePropertyChange("renamerPathnameSpaceReplacement", oldValue, newValue);
  }

  @JsonProperty(value = "renamerSpaceReplacement")
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

  public String getMovieScraper() {
    if (StringUtils.isBlank(movieScraper)) {
      return Constants.TMDB;
    }
    return movieScraper;
  }

  public void setMovieScraper(String newValue) {
    String oldValue = this.movieScraper;
    this.movieScraper = newValue;
    firePropertyChange("movieScraper", oldValue, newValue);
  }

  public void addMovieArtworkScraper(String newValue) {
    if (!artworkScrapers.contains(newValue)) {
      artworkScrapers.add(newValue);
      firePropertyChange(ARTWORK_SCRAPERS, null, artworkScrapers);
    }
  }

  public void removeMovieArtworkScraper(String newValue) {
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

  public void addMovieTrailerScraper(String newValue) {
    if (!trailerScrapers.contains(newValue)) {
      trailerScrapers.add(newValue);
      firePropertyChange(TRAILER_SCRAPERS, null, trailerScrapers);
    }
  }

  public void removeMovieTrailerScraper(String newValue) {
    if (trailerScrapers.contains(newValue)) {
      trailerScrapers.remove(newValue);
      firePropertyChange(TRAILER_SCRAPERS, null, trailerScrapers);
    }
  }

  public List<String> getTrailerScrapers() {
    return trailerScrapers;
  }

  public void addMovieSubtitleScraper(String newValue) {
    if (!subtitleScrapers.contains(newValue)) {
      subtitleScrapers.add(newValue);
      firePropertyChange(SUBTITLE_SCRAPERS, null, subtitleScrapers);
    }
  }

  public void removeMovieSubtitleScraper(String newValue) {
    if (subtitleScrapers.contains(newValue)) {
      subtitleScrapers.remove(newValue);
      firePropertyChange(SUBTITLE_SCRAPERS, null, subtitleScrapers);
    }
  }

  public List<String> getSubtitleScrapers() {
    return subtitleScrapers;
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

  public void setMovieTableHiddenColumns(List<String> hiddenColumns) {
    movieTableHiddenColumns.clear();
    movieTableHiddenColumns.addAll(hiddenColumns);
    firePropertyChange("movieTableHiddenColumns", null, movieTableHiddenColumns);
  }

  public List<String> getMovieTableHiddenColumns() {
    return movieTableHiddenColumns;
  }

  public void setMovieSetTableHiddenColumns(List<String> hiddenColumns) {
    movieSetTableHiddenColumns.clear();
    movieSetTableHiddenColumns.addAll(hiddenColumns);
    firePropertyChange("movieSetTableHiddenColumns", null, movieSetTableHiddenColumns);
  }

  public List<String> getMovieSetTableHiddenColumns() {
    return movieSetTableHiddenColumns;
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

  public void setMovieSetUiFilters(List<UIFilters> filters) {
    movieSetUiFilters.clear();
    movieSetUiFilters.addAll(filters);
    firePropertyChange(MOVIE_SET_UI_FILTERS, null, movieSetUiFilters);
  }

  public List<UIFilters> getMovieSetUiFilters() {
    if (storeUiFilters) {
      return movieSetUiFilters;
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

  public boolean isWriteActorImages() {
    return writeActorImages;
  }

  public void setWriteActorImages(boolean newValue) {
    boolean oldValue = this.writeActorImages;
    this.writeActorImages = newValue;
    firePropertyChange("writeActorImages", oldValue, newValue);
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
    firePropertyChange("certificationCountry", oldValue, newValue);
  }

  public double getScraperThreshold() {
    return scraperThreshold;
  }

  public void setScraperThreshold(double newValue) {
    double oldValue = this.scraperThreshold;
    scraperThreshold = newValue;
    firePropertyChange("scraperThreshold", oldValue, newValue);
  }

  public boolean isRenamerNfoCleanup() {
    return renamerNfoCleanup;
  }

  public void setRenamerNfoCleanup(boolean newValue) {
    boolean oldValue = this.renamerNfoCleanup;
    this.renamerNfoCleanup = newValue;
    firePropertyChange("renamerNfoCleanup", oldValue, newValue);
  }

  public boolean isBuildImageCacheOnImport() {
    return buildImageCacheOnImport;
  }

  public void setBuildImageCacheOnImport(boolean newValue) {
    boolean oldValue = this.buildImageCacheOnImport;
    this.buildImageCacheOnImport = newValue;
    firePropertyChange("buildImageCacheOnImport", oldValue, newValue);
  }

  public boolean isRenamerCreateMoviesetForSingleMovie() {
    return renamerCreateMoviesetForSingleMovie;
  }

  public void setRenamerCreateMoviesetForSingleMovie(boolean newValue) {
    boolean oldValue = this.renamerCreateMoviesetForSingleMovie;
    this.renamerCreateMoviesetForSingleMovie = newValue;
    firePropertyChange("renamerCreateMoviesetForSingleMovie", oldValue, newValue);
  }

  public boolean isRuntimeFromMediaInfo() {
    return runtimeFromMediaInfo;
  }

  public void setRuntimeFromMediaInfo(boolean newValue) {
    boolean oldValue = this.runtimeFromMediaInfo;
    this.runtimeFromMediaInfo = newValue;
    firePropertyChange("runtimeFromMediaInfo", oldValue, newValue);
  }

  public boolean isExtractArtworkFromVsmeta() {
    return extractArtworkFromVsmeta;
  }

  public void setExtractArtworkFromVsmeta(boolean newValue) {
    boolean oldValue = this.extractArtworkFromVsmeta;
    this.extractArtworkFromVsmeta = newValue;
    firePropertyChange("extractArtworkFromVsmeta", oldValue, newValue);
  }

  public boolean isIncludeExternalAudioStreams() {
    return includeExternalAudioStreams;
  }

  public void setIncludeExternalAudioStreams(boolean newValue) {
    boolean oldValue = this.includeExternalAudioStreams;
    this.includeExternalAudioStreams = newValue;
    firePropertyChange("includeExternalAudioStreams", oldValue, newValue);
  }

  public boolean isAsciiReplacement() {
    return asciiReplacement;
  }

  public void setAsciiReplacement(boolean newValue) {
    boolean oldValue = this.asciiReplacement;
    this.asciiReplacement = newValue;
    firePropertyChange("asciiReplacement", oldValue, newValue);
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

  public boolean isScraperFallback() {
    return scraperFallback;
  }

  public void setScraperFallback(boolean newValue) {
    boolean oldValue = this.scraperFallback;
    this.scraperFallback = newValue;
    firePropertyChange("scraperFallback", oldValue, newValue);
  }

  public boolean isUseTrailerPreference() {
    return useTrailerPreference;
  }

  public void setUseTrailerPreference(boolean newValue) {
    boolean oldValue = this.useTrailerPreference;
    this.useTrailerPreference = newValue;
    firePropertyChange("useTrailerPreference", oldValue, newValue);
    // also influences the automatic trailer download
    firePropertyChange("automaticTrailerDownload", oldValue, newValue);
  }

  public boolean isAutomaticTrailerDownload() {
    // only available if the trailer preference is set
    return useTrailerPreference && automaticTrailerDownload;
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

  public void setSyncTrakt(boolean newValue) {
    boolean oldValue = this.syncTrakt;
    this.syncTrakt = newValue;
    firePropertyChange("syncTrakt", oldValue, newValue);
  }

  public boolean getSyncTrakt() {
    return syncTrakt;
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

  public MediaLanguages getImageScraperLanguage() {
    return imageScraperLanguage;
  }

  public void setImageScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.imageScraperLanguage;
    this.imageScraperLanguage = newValue;
    firePropertyChange("imageScraperLanguage", oldValue, newValue);
  }

  public boolean isImageLanguagePriority() {
    return imageLanguagePriority;
  }

  public void setImageLanguagePriority(boolean newValue) {
    boolean oldValue = this.imageLanguagePriority;
    this.imageLanguagePriority = newValue;
    firePropertyChange("imageLanguagePriority", oldValue, newValue);
  }

  public CertificationStyle getCertificationStyle() {
    return certificationStyle;
  }

  public void setCertificationStyle(CertificationStyle newValue) {
    CertificationStyle oldValue = this.certificationStyle;
    this.certificationStyle = newValue;
    firePropertyChange("certificationStyle", oldValue, newValue);
  }

  public LanguageStyle getSubtitleLanguageStyle() {
    return subtitleLanguageStyle;
  }

  public void setSubtitleLanguageStyle(LanguageStyle newValue) {
    LanguageStyle oldValue = this.subtitleLanguageStyle;
    this.subtitleLanguageStyle = newValue;
    firePropertyChange("subtitleLanguageStyle", oldValue, newValue);
  }

  public boolean isSubtitleWithoutLanguageTag() {
    return subtitleWithoutLanguageTag;
  }

  public void setSubtitleWithoutLanguageTag(boolean newValue) {
    boolean oldValue = this.subtitleWithoutLanguageTag;
    this.subtitleWithoutLanguageTag = newValue;
    firePropertyChange("subtitleWithoutLanguageTag", oldValue, newValue);
  }

  public List<MovieScraperMetadataConfig> getScraperMetadataConfig() {
    return scraperMetadataConfig;
  }

  public void setScraperMetadataConfig(List<MovieScraperMetadataConfig> newValues) {
    scraperMetadataConfig.clear();
    scraperMetadataConfig.addAll(newValues);
    firePropertyChange("scraperMetadataConfig", null, scraperMetadataConfig);
  }

  public boolean isWriteCleanNfo() {
    return writeCleanNfo;
  }

  public void setWriteCleanNfo(boolean newValue) {
    boolean oldValue = writeCleanNfo;
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

  public boolean isCreateOutline() {
    return createOutline;
  }

  public void setCreateOutline(boolean newValue) {
    boolean oldValue = this.createOutline;
    this.createOutline = newValue;
    firePropertyChange("createOutline", oldValue, newValue);
  }

  public boolean isOutlineFirstSentence() {
    return outlineFirstSentence;
  }

  public void setOutlineFirstSentence(boolean newValue) {
    boolean oldValue = this.outlineFirstSentence;
    this.outlineFirstSentence = newValue;
    firePropertyChange("outlineFirstSentence", oldValue, newValue);
  }

  public boolean getCapitalWordsInTitles() {
    return capitalWordsInTitles;
  }

  public void setCapitalWordsInTitles(boolean newValue) {
    boolean oldValue = this.capitalWordsInTitles;
    this.capitalWordsInTitles = newValue;
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
    // file names
    nfoFilenames.clear();
    addNfoFilename(MovieNfoNaming.FILENAME_NFO);

    posterFilenames.clear();
    addPosterFilename(MoviePosterNaming.FILENAME_POSTER);

    fanartFilenames.clear();
    addFanartFilename(MovieFanartNaming.FILENAME_FANART);

    bannerFilenames.clear();
    addBannerFilename(MovieBannerNaming.FILENAME_BANNER);

    clearartFilenames.clear();
    addClearartFilename(MovieClearartNaming.FILENAME_CLEARART);

    thumbFilenames.clear();
    addThumbFilename(MovieThumbNaming.FILENAME_LANDSCAPE);

    logoFilenames.clear();
    addLogoFilename(MovieLogoNaming.FILENAME_LOGO);

    clearlogoFilenames.clear();
    addClearlogoFilename(MovieClearlogoNaming.FILENAME_CLEARLOGO);

    discartFilenames.clear();
    addDiscartFilename(MovieDiscartNaming.FILENAME_DISC);

    keyartFilenames.clear();
    addKeyartFilename(MovieKeyartNaming.FILENAME_KEYART);

    trailerFilenames.clear();
    addTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER);

    // other settings
    setMovieConnector(MovieConnectors.XBMC);
    setRenamerPathname(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.LARGE);

    firePropertyChange("preset", false, true);
  }

  /**
   * Kodi 17+ defaults
   */
  public void setDefaultSettingsForKodi() {
    // file names
    nfoFilenames.clear();
    addNfoFilename(MovieNfoNaming.FILENAME_NFO);

    posterFilenames.clear();
    addPosterFilename(MoviePosterNaming.FILENAME_POSTER);

    fanartFilenames.clear();
    addFanartFilename(MovieFanartNaming.FILENAME_FANART);

    bannerFilenames.clear();
    addBannerFilename(MovieBannerNaming.FILENAME_BANNER);

    clearartFilenames.clear();
    addClearartFilename(MovieClearartNaming.FILENAME_CLEARART);

    thumbFilenames.clear();
    addThumbFilename(MovieThumbNaming.FILENAME_LANDSCAPE);

    logoFilenames.clear();
    addLogoFilename(MovieLogoNaming.FILENAME_LOGO);

    clearlogoFilenames.clear();
    addClearlogoFilename(MovieClearlogoNaming.FILENAME_CLEARLOGO);

    discartFilenames.clear();
    addDiscartFilename(MovieDiscartNaming.FILENAME_DISCART);

    keyartFilenames.clear();
    addKeyartFilename(MovieKeyartNaming.FILENAME_KEYART);

    trailerFilenames.clear();
    addTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER);

    // other settings
    setMovieConnector(MovieConnectors.KODI);
    setRenamerPathname(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.LARGE);

    firePropertyChange("preset", false, true);
  }

  /**
   * MediaPortal 1 defaults
   */
  public void setDefaultSettingsForMediaPortal1() {
    // file names
    nfoFilenames.clear();
    addNfoFilename(MovieNfoNaming.FILENAME_NFO);

    posterFilenames.clear();
    addPosterFilename(MoviePosterNaming.POSTER);

    fanartFilenames.clear();
    addFanartFilename(MovieFanartNaming.FANART);

    bannerFilenames.clear();
    addBannerFilename(MovieBannerNaming.BANNER);

    clearartFilenames.clear();
    addClearartFilename(MovieClearartNaming.CLEARART);

    thumbFilenames.clear();
    addThumbFilename(MovieThumbNaming.THUMB);

    logoFilenames.clear();
    addLogoFilename(MovieLogoNaming.LOGO);

    clearlogoFilenames.clear();
    addClearlogoFilename(MovieClearlogoNaming.CLEARLOGO);

    discartFilenames.clear();
    addDiscartFilename(MovieDiscartNaming.DISC);

    keyartFilenames.clear();
    addKeyartFilename(MovieKeyartNaming.KEYART);

    trailerFilenames.clear();
    addTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER);

    // other settings
    setMovieConnector(MovieConnectors.MP);
    setRenamerPathname(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.TECHNICAL);

    firePropertyChange("preset", false, true);
  }

  /**
   * MediaPortal 2 defaults
   */
  public void setDefaultSettingsForMediaPortal2() {
    // file names
    nfoFilenames.clear();
    addNfoFilename(MovieNfoNaming.FILENAME_NFO);

    posterFilenames.clear();
    addPosterFilename(MoviePosterNaming.POSTER);

    fanartFilenames.clear();
    addFanartFilename(MovieFanartNaming.FANART);

    bannerFilenames.clear();
    addBannerFilename(MovieBannerNaming.BANNER);

    clearartFilenames.clear();
    addClearartFilename(MovieClearartNaming.CLEARART);

    thumbFilenames.clear();
    addThumbFilename(MovieThumbNaming.THUMB);

    logoFilenames.clear();
    addLogoFilename(MovieLogoNaming.LOGO);

    clearlogoFilenames.clear();
    addClearlogoFilename(MovieClearlogoNaming.CLEARLOGO);

    discartFilenames.clear();
    addDiscartFilename(MovieDiscartNaming.DISC);

    keyartFilenames.clear();
    addKeyartFilename(MovieKeyartNaming.KEYART);

    trailerFilenames.clear();
    addTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER);

    // other settings
    setMovieConnector(MovieConnectors.KODI);
    setRenamerPathname(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.TECHNICAL);

    firePropertyChange("preset", false, true);
  }

  /**
   * Plex defaults
   */
  public void setDefaultSettingsForPlex() {
    // file names
    nfoFilenames.clear();
    addNfoFilename(MovieNfoNaming.FILENAME_NFO);

    posterFilenames.clear();
    addPosterFilename(MoviePosterNaming.POSTER);

    fanartFilenames.clear();
    addFanartFilename(MovieFanartNaming.FANART);

    bannerFilenames.clear();
    addBannerFilename(MovieBannerNaming.BANNER);

    clearartFilenames.clear();
    addClearartFilename(MovieClearartNaming.CLEARART);

    thumbFilenames.clear();
    addThumbFilename(MovieThumbNaming.THUMB);

    logoFilenames.clear();
    addLogoFilename(MovieLogoNaming.LOGO);

    clearlogoFilenames.clear();
    addClearlogoFilename(MovieClearlogoNaming.CLEARLOGO);

    discartFilenames.clear();
    addDiscartFilename(MovieDiscartNaming.DISC);

    keyartFilenames.clear();
    addKeyartFilename(MovieKeyartNaming.KEYART);

    trailerFilenames.clear();
    addTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER);

    // other settings
    setMovieConnector(MovieConnectors.XBMC);
    setRenamerPathname(DEFAULT_RENAMER_FOLDER_PATTERN);
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
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.MOVIE_ARTWORK)) {
      addMovieArtworkScraper(ms.getId());
    }

    trailerScrapers.clear();
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.MOVIE_TRAILER)) {
      addMovieTrailerScraper(ms.getId());
    }

    subtitleScrapers.clear();
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.SUBTITLE)) {
      addMovieSubtitleScraper(ms.getId());
    }
  }
}
