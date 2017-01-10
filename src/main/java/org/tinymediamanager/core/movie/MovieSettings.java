/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import static org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractSettings;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.filenaming.MovieBannerNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearlogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieDiscartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieFanartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieLogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;
import org.tinymediamanager.core.movie.filenaming.MoviePosterNaming;
import org.tinymediamanager.core.movie.filenaming.MovieThumbNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.entities.MediaLanguages;

/**
 * The Class MovieSettings.
 */
@XmlRootElement(name = "MovieSettings")
public class MovieSettings extends AbstractSettings {
  private static final Logger              LOGGER                               = LoggerFactory.getLogger(MovieSettings.class);

  public final static String               CONFIG_FILE                          = "movies.xml";
  public final static String               DEFAULT_RENAMER_FOLDER_PATTERN       = "$T { - $U }($Y)";
  public final static String               DEFAULT_RENAMER_FILE_PATTERN         = "$T { - $U }($Y) $V $A";

  private static MovieSettings             instance;

  /**
   * Constants mainly for events
   */
  private final static String              PATH                                 = "path";
  private final static String              FILENAME                             = "filename";
  private final static String              MOVIE_DATA_SOURCE                    = "movieDataSource";
  private final static String              IMAGE_POSTER_SIZE                    = "imagePosterSize";
  private final static String              IMAGE_FANART_SIZE                    = "imageFanartSize";
  private final static String              IMAGE_EXTRATHUMBS                    = "imageExtraThumbs";
  private final static String              IMAGE_EXTRATHUMBS_RESIZE             = "imageExtraThumbsResize";
  private final static String              IMAGE_EXTRATHUMBS_SIZE               = "imageExtraThumbsSize";
  private final static String              IMAGE_EXTRATHUMBS_COUNT              = "imageExtraThumbsCount";
  private final static String              IMAGE_EXTRAFANART                    = "imageExtraFanart";
  private final static String              IMAGE_EXTRAFANART_COUNT              = "imageExtraFanartCount";
  private final static String              ENABLE_MOVIESET_ARTWORK_MOVIE_FOLDER = "enableMovieSetArtworkMovieFolder";
  private final static String              ENABLE_MOVIESET_ARTWORK_FOLDER       = "enableMovieSetArtworkFolder";
  private final static String              MOVIESET_ARTWORK_FOLDER              = "movieSetArtworkFolder";
  private final static String              MOVIE_CONNECTOR                      = "movieConnector";
  private final static String              NFO_FILENAME                         = "nfoFilename";
  private final static String              POSTER_FILENAME                      = "posterFilename";
  private final static String              FANART_FILENAME                      = "fanartFilename";
  private final static String              BANNER_FILENAME                      = "bannerFilename";
  private final static String              CLEARART_FILENAME                    = "clearartFilename";
  private final static String              THUMB_FILENAME                       = "thumbFilename";
  private final static String              LOGO_FILENAME                        = "logoFilename";
  private final static String              CLEARLOGO_FILENAME                   = "clearlogoFilename";
  private final static String              DISCART_FILENAME                     = "discartFilename";
  private final static String              RENAMER_PATHNAME                     = "renamerPathname";
  private final static String              RENAMER_FILENAME                     = "renamerFilename";
  private final static String              RENAMER_SPACE_SUBSTITUTION           = "renamerSpaceSubstitution";
  private final static String              RENAMER_SPACE_REPLACEMENT            = "renamerSpaceReplacement";
  private final static String              RENAMER_NFO_CLEANUP                  = "renamerNfoCleanup";
  private final static String              RENAMER_MOVIESET_SINGLE_MOVIE        = "movieRenamerMoviesetSingleMovie";
  private final static String              MOVIE_SCRAPER                        = "movieScraper";
  private final static String              ARTWORK_SCRAPERS                     = "artworkScrapers";
  private final static String              TRAILER_SCRAPERS                     = "trailerScrapers";
  private final static String              SUBTITLE_SCRAPERS                    = "subtitleScrapers";
  private final static String              SCRAPE_BEST_IMAGE                    = "scrapeBestImage";
  private final static String              WRITE_ACTOR_IMAGES                   = "writeActorImages";
  private final static String              SCRAPER_LANGU                        = "scraperLanguage";
  private final static String              SUBTITLE_SCRAPER_LANGU               = "subtitleScraperLanguage";
  private final static String              CERTIFICATION_COUNTRY                = "certificationCountry";
  private final static String              SCRAPER_THRESHOLD                    = "scraperThreshold";
  private final static String              DETECT_MOVIE_MULTI_DIR               = "detectMovieMultiDir";
  private final static String              BUILD_IMAGE_CACHE_ON_IMPORT          = "buildImageCacheOnImport";
  private final static String              BAD_WORDS                            = "badWords";
  private final static String              ENTRY                                = "entry";
  private final static String              RUNTIME_FROM_MI                      = "runtimeFromMediaInfo";
  private final static String              ASCII_REPLACEMENT                    = "asciiReplacement";
  private final static String              SCRAPER_FALLBACK                     = "scraperFallback";
  private final static String              UI_FILTERS                           = "uiFilters";
  private final static String              STORE_UI_FILTERS                     = "storeUiFilters";
  private final static String              STORE_UI_SORTING                     = "storeUiSorting";
  private final static String              SORT_COLUMN                          = "sortColumn";
  private final static String              SORT_ASCENDING                       = "sortAscending";
  private final static String              SKIP_FOLDERS                         = "skipFolders";
  private final static String              MOVIE_TABLE_HIDDEN_COLUMNS           = "movieTableHiddenColumns";

  @XmlElementWrapper(name = MOVIE_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>               movieDataSources                     = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = NFO_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieNfoNaming>       nfoFilenames                         = new ArrayList<>();

  @XmlElementWrapper(name = POSTER_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MoviePosterNaming>    posterFilenames                      = new ArrayList<>();

  @XmlElementWrapper(name = FANART_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieFanartNaming>    fanartFilenames                      = new ArrayList<>();

  @XmlElementWrapper(name = BANNER_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieBannerNaming>    bannerFilenames                      = new ArrayList<>();

  @XmlElementWrapper(name = CLEARART_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieClearartNaming>  clearartFilenames                    = new ArrayList<>();

  @XmlElementWrapper(name = THUMB_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieThumbNaming>     thumbFilenames                       = new ArrayList<>();

  @XmlElementWrapper(name = CLEARLOGO_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieClearlogoNaming> clearlogoFilenames                   = new ArrayList<>();

  @XmlElementWrapper(name = LOGO_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieLogoNaming>      logoFilenames                        = new ArrayList<>();

  @XmlElementWrapper(name = DISCART_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieDiscartNaming>   discartFilenames                     = new ArrayList<>();

  @XmlElementWrapper(name = BAD_WORDS)
  @XmlElement(name = ENTRY)
  private final List<String>               badWords                             = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = ARTWORK_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>               artworkScrapers                      = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = TRAILER_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>               trailerScrapers                      = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = SUBTITLE_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>               subtitleScrapers                     = ObservableCollections.observableList(new ArrayList<String>());

  private Map<String, String>              uiFilters                            = new HashMap<>();

  @XmlElementWrapper(name = SKIP_FOLDERS)
  @XmlElement(name = ENTRY)
  private final List<String>               skipFolders                          = ObservableCollections.observableList(new ArrayList<String>());

  private final List<String>               movieTableHiddenColumns              = ObservableCollections.observableList(new ArrayList<String>());

  // data sources / NFO settings
  private boolean                          detectMovieMultiDir                  = false;
  private boolean                          buildImageCacheOnImport              = false;
  private MovieConnectors                  movieConnector                       = MovieConnectors.KODI;
  private CertificationStyle               certificationStyle                   = CertificationStyle.LARGE;

  // renamer
  private boolean                          renameAfterScrape                    = false;
  private String                           renamerPathname                      = DEFAULT_RENAMER_FOLDER_PATTERN;
  private String                           renamerFilename                      = DEFAULT_RENAMER_FILE_PATTERN;
  private boolean                          renamerSpaceSubstitution             = false;
  private String                           renamerSpaceReplacement              = "_";
  private boolean                          renamerNfoCleanup                    = false;
  private boolean                          renamerCreateMoviesetForSingleMovie  = false;
  private boolean                          asciiReplacement                     = false;

  // meta data scraper
  private String                           movieScraper                         = Constants.TMDB;
  private MediaLanguages                   scraperLanguage                      = MediaLanguages.en;
  private CountryCode                      certificationCountry                 = CountryCode.US;
  private double                           scraperThreshold                     = 0.75;
  private boolean                          scraperFallback                      = false;
  private MovieScraperMetadataConfig       movieScraperMetadataConfig           = null;

  // artwork scraper
  private PosterSizes                      imagePosterSize                      = PosterSizes.BIG;
  private FanartSizes                      imageFanartSize                      = FanartSizes.LARGE;
  private boolean                          imageExtraThumbs                     = false;
  private boolean                          imageExtraThumbsResize               = true;
  private int                              imageExtraThumbsSize                 = 300;
  private int                              imageExtraThumbsCount                = 5;
  private boolean                          imageExtraFanart                     = false;
  private int                              imageExtraFanartCount                = 5;
  private boolean                          enableMovieSetArtworkMovieFolder     = true;
  private boolean                          enableMovieSetArtworkFolder          = false;
  private String                           movieSetArtworkFolder                = "MoviesetArtwork";
  private boolean                          scrapeBestImage                      = true;
  private boolean                          imageLanguagePriority                = true;
  private boolean                          writeActorImages                     = false;

  // trailer scraper
  private boolean                          useTrailerPreference                 = true;
  private boolean                          automaticTrailerDownload             = false;
  private MovieTrailerQuality              trailerQuality                       = MovieTrailerQuality.HD_720;
  private MovieTrailerSources              trailerSource                        = MovieTrailerSources.YOUTUBE;

  // subtitle scraper
  private MediaLanguages                   subtitleScraperLanguage              = MediaLanguages.en;
  private LanguageStyle                    subtitleLanguageStyle                = LanguageStyle.ISO3T;

  // misc
  private boolean                          runtimeFromMediaInfo                 = false;
  private boolean                          syncTrakt                            = false;

  private boolean                          storeUiFilters                       = false;
  private boolean                          storeUiSorting                       = false;
  private SortColumn                       sortColumn                           = SortColumn.TITLE;
  private boolean                          sortAscending                        = true;

  public MovieSettings() {
    addPropertyChangeListener(evt -> setDirty());
    movieScraperMetadataConfig = new MovieScraperMetadataConfig();
    movieScraperMetadataConfig.addPropertyChangeListener(evt -> setDirty());
  }

  /**
   * Gets the single instance of MovieSettings.
   *
   * @return single instance of MovieSettings
   */
  public synchronized static MovieSettings getInstance() {
    return getInstance(Settings.getInstance().getSettingsFolder());
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
  protected String getConfigFilename() {
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
    setMovieTableHiddenColumns(Arrays.asList("filename", "path", "movieset"));

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

    saveSettings();
  }

  public void addMovieDataSources(String path) {
    if (!movieDataSources.contains(path)) {
      movieDataSources.add(path);
      firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
    }
  }

  public void removeMovieDataSources(String path) {
    MovieList movieList = MovieList.getInstance();
    movieList.removeDatasource(path);
    movieDataSources.remove(path);
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
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

  public List<MovieDiscartNaming> getDiscartFilenames() {
    return new ArrayList<>(this.discartFilenames);
  }

  @XmlElement(name = IMAGE_POSTER_SIZE)
  public PosterSizes getImagePosterSize() {
    return imagePosterSize;
  }

  public void setImagePosterSize(PosterSizes newValue) {
    PosterSizes oldValue = this.imagePosterSize;
    this.imagePosterSize = newValue;
    firePropertyChange(IMAGE_POSTER_SIZE, oldValue, newValue);
  }

  @XmlElement(name = IMAGE_FANART_SIZE)
  public FanartSizes getImageFanartSize() {
    return imageFanartSize;
  }

  public void setImageFanartSize(FanartSizes newValue) {
    FanartSizes oldValue = this.imageFanartSize;
    this.imageFanartSize = newValue;
    firePropertyChange(IMAGE_FANART_SIZE, oldValue, newValue);
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
    firePropertyChange(IMAGE_EXTRATHUMBS_RESIZE, oldValue, newValue);
  }

  public void setImageExtraThumbsSize(int newValue) {
    int oldValue = this.imageExtraThumbsSize;
    this.imageExtraThumbsSize = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_SIZE, oldValue, newValue);
  }

  public int getImageExtraThumbsCount() {
    return imageExtraThumbsCount;
  }

  public void setImageExtraThumbsCount(int newValue) {
    int oldValue = this.imageExtraThumbsCount;
    this.imageExtraThumbsCount = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_COUNT, oldValue, newValue);
  }

  public int getImageExtraFanartCount() {
    return imageExtraFanartCount;
  }

  public void setImageExtraFanartCount(int newValue) {
    int oldValue = this.imageExtraFanartCount;
    this.imageExtraFanartCount = newValue;
    firePropertyChange(IMAGE_EXTRAFANART_COUNT, oldValue, newValue);
  }

  public boolean isImageExtraFanart() {
    return imageExtraFanart;
  }

  public void setImageExtraThumbs(boolean newValue) {
    boolean oldValue = this.imageExtraThumbs;
    this.imageExtraThumbs = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS, oldValue, newValue);
  }

  public void setImageExtraFanart(boolean newValue) {
    boolean oldValue = this.imageExtraFanart;
    this.imageExtraFanart = newValue;
    firePropertyChange(IMAGE_EXTRAFANART, oldValue, newValue);
  }

  public boolean isEnableMovieSetArtworkMovieFolder() {
    return enableMovieSetArtworkMovieFolder;
  }

  public void setEnableMovieSetArtworkMovieFolder(boolean newValue) {
    boolean oldValue = this.enableMovieSetArtworkMovieFolder;
    this.enableMovieSetArtworkMovieFolder = newValue;
    firePropertyChange(ENABLE_MOVIESET_ARTWORK_MOVIE_FOLDER, oldValue, newValue);
  }

  public boolean isEnableMovieSetArtworkFolder() {
    return enableMovieSetArtworkFolder;
  }

  public void setEnableMovieSetArtworkFolder(boolean newValue) {
    boolean oldValue = this.enableMovieSetArtworkFolder;
    this.enableMovieSetArtworkFolder = newValue;
    firePropertyChange(ENABLE_MOVIESET_ARTWORK_FOLDER, oldValue, newValue);
  }

  public String getMovieSetArtworkFolder() {
    return movieSetArtworkFolder;
  }

  public void setMovieSetArtworkFolder(String newValue) {
    String oldValue = this.movieSetArtworkFolder;
    this.movieSetArtworkFolder = newValue;
    firePropertyChange(MOVIESET_ARTWORK_FOLDER, oldValue, newValue);
  }

  @XmlElement(name = MOVIE_CONNECTOR)
  public MovieConnectors getMovieConnector() {
    return movieConnector;
  }

  public void setMovieConnector(MovieConnectors newValue) {
    MovieConnectors oldValue = this.movieConnector;
    this.movieConnector = newValue;
    firePropertyChange(MOVIE_CONNECTOR, oldValue, newValue);
  }

  @XmlElement(name = RENAMER_PATHNAME)
  public String getRenamerPathname() {
    return renamerPathname;
  }

  public void setRenamerPathname(String newValue) {
    String oldValue = this.renamerPathname;
    this.renamerPathname = newValue;
    firePropertyChange(RENAMER_PATHNAME, oldValue, newValue);
  }

  @XmlElement(name = RENAMER_FILENAME)
  public String getRenamerFilename() {
    return renamerFilename;
  }

  public void setRenamerFilename(String newValue) {
    String oldValue = this.renamerFilename;
    this.renamerFilename = newValue;
    firePropertyChange(RENAMER_FILENAME, oldValue, newValue);
  }

  @XmlElement(name = RENAMER_SPACE_SUBSTITUTION)
  public boolean isRenamerSpaceSubstitution() {
    return renamerSpaceSubstitution;
  }

  public void setRenamerSpaceSubstitution(boolean renamerSpaceSubstitution) {
    this.renamerSpaceSubstitution = renamerSpaceSubstitution;
  }

  public void setRenameAfterScrape(boolean newValue) {
    boolean oldValue = this.renameAfterScrape;
    this.renameAfterScrape = newValue;
    firePropertyChange("renameAfterScrape", oldValue, newValue);
  }

  public boolean isRenameAfterScrape() {
    return this.renameAfterScrape;
  }

  @XmlElement(name = RENAMER_SPACE_REPLACEMENT)
  public String getRenamerSpaceReplacement() {
    return renamerSpaceReplacement;
  }

  public void setRenamerSpaceReplacement(String renamerSpaceReplacement) {
    this.renamerSpaceReplacement = renamerSpaceReplacement;
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
    firePropertyChange(MOVIE_SCRAPER, oldValue, newValue);
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
    firePropertyChange(SCRAPE_BEST_IMAGE, oldValue, newValue);
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

  public void addMovieSkipFolder(String newValue) {
    if (!skipFolders.contains(newValue)) {
      skipFolders.add(newValue);
      firePropertyChange(SKIP_FOLDERS, null, skipFolders);
    }
  }

  public void removeMovieSkipFolder(String newValue) {
    if (skipFolders.contains(newValue)) {
      skipFolders.remove(newValue);
      firePropertyChange(SKIP_FOLDERS, null, skipFolders);
    }
  }

  public List<String> getSkipFolders() {
    return skipFolders;
  }

  public void setMovieTableHiddenColumns(List<String> hiddenColumns) {
    movieTableHiddenColumns.clear();
    movieTableHiddenColumns.addAll(hiddenColumns);
    firePropertyChange(MOVIE_TABLE_HIDDEN_COLUMNS, null, movieTableHiddenColumns);
  }

  @XmlElementWrapper(name = MOVIE_TABLE_HIDDEN_COLUMNS)
  @XmlElement(name = ENTRY)
  public List<String> getMovieTableHiddenColumns() {
    return movieTableHiddenColumns;
  }

  public void setUiFilters(Map<String, String> filters) {
    uiFilters = filters;
    firePropertyChange(UI_FILTERS, null, uiFilters);
  }

  @XmlElement(name = UI_FILTERS)
  public Map<String, String> getUiFilters() {
    if (storeUiFilters) {
      return uiFilters;
    }
    return new HashMap<>();
  }

  public void setStoreUiFilters(boolean newValue) {
    boolean oldValue = this.storeUiFilters;
    this.storeUiFilters = newValue;
    firePropertyChange(STORE_UI_FILTERS, oldValue, newValue);
  }

  public boolean isStoreUiSorting() {
    return storeUiSorting;
  }

  public void setStoreUiSorting(boolean newValue) {
    boolean oldValue = this.storeUiSorting;
    this.storeUiSorting = newValue;
    firePropertyChange(STORE_UI_SORTING, oldValue, newValue);
  }

  public boolean isStoreUiFilters() {
    return storeUiFilters;
  }

  public SortColumn getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(SortColumn newValue) {
    SortColumn oldValue = this.sortColumn;
    this.sortColumn = newValue;
    firePropertyChange(SORT_COLUMN, oldValue, newValue);
  }

  public boolean isSortAscending() {
    return sortAscending;
  }

  public void setSortAscending(boolean newValue) {
    boolean oldValue = this.sortAscending;
    this.sortAscending = newValue;
    firePropertyChange(SORT_ASCENDING, oldValue, newValue);
  }

  public boolean isWriteActorImages() {
    return writeActorImages;
  }

  public void setWriteActorImages(boolean newValue) {
    boolean oldValue = this.writeActorImages;
    this.writeActorImages = newValue;
    firePropertyChange(WRITE_ACTOR_IMAGES, oldValue, newValue);
  }

  @XmlElement(name = SCRAPER_LANGU)
  public MediaLanguages getScraperLanguage() {
    return scraperLanguage;
  }

  public void setScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.scraperLanguage;
    this.scraperLanguage = newValue;
    firePropertyChange(SCRAPER_LANGU, oldValue, newValue);
  }

  @XmlElement(name = SUBTITLE_SCRAPER_LANGU)
  public MediaLanguages getSubtitleScraperLanguage() {
    return subtitleScraperLanguage;
  }

  public void setSubtitleScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.subtitleScraperLanguage;
    this.subtitleScraperLanguage = newValue;
    firePropertyChange(SUBTITLE_SCRAPER_LANGU, oldValue, newValue);
  }

  @XmlElement(name = CERTIFICATION_COUNTRY)
  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  @XmlElement(name = SCRAPER_THRESHOLD)
  public double getScraperThreshold() {
    return scraperThreshold;
  }

  public void setScraperThreshold(double newValue) {
    double oldValue = this.scraperThreshold;
    scraperThreshold = newValue;
    firePropertyChange(SCRAPER_THRESHOLD, oldValue, newValue);
  }

  @XmlElement(name = RENAMER_NFO_CLEANUP)
  public boolean isRenamerNfoCleanup() {
    return renamerNfoCleanup;
  }

  public void setRenamerNfoCleanup(boolean newValue) {
    boolean oldValue = this.renamerNfoCleanup;
    this.renamerNfoCleanup = newValue;
    firePropertyChange(RENAMER_NFO_CLEANUP, oldValue, newValue);
  }

  /**
   * Should we detect (and create) movies from directories containing more than one movie?
   *
   * @return true/false
   * @Deprecated obsolete with UDS2
   */
  @Deprecated
  public boolean isDetectMovieMultiDir() {
    return detectMovieMultiDir;
  }

  /**
   * Should we detect (and create) movies from directories containing more than one movie?
   *
   * @param newValue
   *          true/false
   * @Deprecated obsolete with UDS2
   */
  @Deprecated
  public void setDetectMovieMultiDir(boolean newValue) {
    boolean oldValue = this.detectMovieMultiDir;
    this.detectMovieMultiDir = newValue;
    firePropertyChange(DETECT_MOVIE_MULTI_DIR, oldValue, newValue);
  }

  public boolean isBuildImageCacheOnImport() {
    return buildImageCacheOnImport;
  }

  public void setBuildImageCacheOnImport(boolean newValue) {
    boolean oldValue = this.buildImageCacheOnImport;
    this.buildImageCacheOnImport = newValue;
    firePropertyChange(BUILD_IMAGE_CACHE_ON_IMPORT, oldValue, newValue);
  }

  public boolean isRenamerCreateMoviesetForSingleMovie() {
    return renamerCreateMoviesetForSingleMovie;
  }

  public void setRenamerCreateMoviesetForSingleMovie(boolean newValue) {
    boolean oldValue = this.renamerCreateMoviesetForSingleMovie;
    this.renamerCreateMoviesetForSingleMovie = newValue;
    firePropertyChange(RENAMER_MOVIESET_SINGLE_MOVIE, oldValue, newValue);
  }

  public boolean isRuntimeFromMediaInfo() {
    return runtimeFromMediaInfo;
  }

  public void setRuntimeFromMediaInfo(boolean newValue) {
    boolean oldValue = this.runtimeFromMediaInfo;
    this.runtimeFromMediaInfo = newValue;
    firePropertyChange(RUNTIME_FROM_MI, oldValue, newValue);
  }

  public boolean isAsciiReplacement() {
    return asciiReplacement;
  }

  public void setAsciiReplacement(boolean newValue) {
    boolean oldValue = this.asciiReplacement;
    this.asciiReplacement = newValue;
    firePropertyChange(ASCII_REPLACEMENT, oldValue, newValue);
  }

  public void addBadWord(String badWord) {
    if (!badWords.contains(badWord.toLowerCase(Locale.ROOT))) {
      badWords.add(badWord.toLowerCase(Locale.ROOT));
      firePropertyChange(BAD_WORDS, null, badWords);
    }
  }

  public void removeBadWord(String badWord) {
    badWords.remove(badWord.toLowerCase(Locale.ROOT));
    firePropertyChange(BAD_WORDS, null, badWords);
  }

  public List<String> getBadWords() {
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
    firePropertyChange(SCRAPER_FALLBACK, oldValue, newValue);
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

  public MovieTrailerQuality getTrailerQuality() {
    return trailerQuality;
  }

  public void setTrailerQuality(MovieTrailerQuality newValue) {
    MovieTrailerQuality oldValue = this.trailerQuality;
    this.trailerQuality = newValue;
    firePropertyChange("trailerQuality", oldValue, newValue);
  }

  public MovieTrailerSources getTrailerSource() {
    return trailerSource;
  }

  public void setTrailerSource(MovieTrailerSources newValue) {
    MovieTrailerSources oldValue = this.trailerSource;
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

  public MovieScraperMetadataConfig getMovieScraperMetadataConfig() {
    return movieScraperMetadataConfig;
  }

  public void setMovieScraperMetadataConfig(MovieScraperMetadataConfig scraperMetadataConfig) {
    this.movieScraperMetadataConfig = scraperMetadataConfig;
    this.movieScraperMetadataConfig.addPropertyChangeListener(evt -> setDirty());
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
    addPosterFilename(MoviePosterNaming.POSTER);

    fanartFilenames.clear();
    addFanartFilename(MovieFanartNaming.FANART);

    bannerFilenames.clear();
    addBannerFilename(MovieBannerNaming.BANNER);

    clearartFilenames.clear();
    addClearartFilename(MovieClearartNaming.CLEARART);

    thumbFilenames.clear();
    addThumbFilename(MovieThumbNaming.LANDSCAPE);

    logoFilenames.clear();
    addLogoFilename(MovieLogoNaming.LOGO);

    clearlogoFilenames.clear();
    addClearlogoFilename(MovieClearlogoNaming.CLEARLOGO);

    discartFilenames.clear();
    addDiscartFilename(MovieDiscartNaming.DISC);

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

    // other settings
    setMovieConnector(MovieConnectors.XBMC);
    setRenamerPathname(DEFAULT_RENAMER_FOLDER_PATTERN);
    setRenamerFilename(DEFAULT_RENAMER_FILE_PATTERN);
    setCertificationStyle(CertificationStyle.SHORT);

    firePropertyChange("preset", false, true);
  }
}
