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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.movie.MovieSearchOptions.MovieSearchOptionsAdapter;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.entities.MediaLanguages;

/**
 * The Class MovieSettings.
 */
@XmlRootElement(name = "MovieSettings")
public class MovieSettings extends AbstractModelObject {
  public final static String              DEFAULT_RENAMER_FOLDER_PATTERN           = "$T { - $U }($Y)";
  public final static String              DEFAULT_RENAMER_FILE_PATTERN             = "$T { - $U }($Y) $V $A";

  private final static String             PATH                                     = "path";
  private final static String             FILENAME                                 = "filename";
  private final static String             MOVIE_DATA_SOURCE                        = "movieDataSource";
  private final static String             IMAGE_POSTER_SIZE                        = "imagePosterSize";
  private final static String             IMAGE_FANART_SIZE                        = "imageFanartSize";
  private final static String             IMAGE_EXTRATHUMBS                        = "imageExtraThumbs";
  private final static String             IMAGE_EXTRATHUMBS_RESIZE                 = "imageExtraThumbsResize";
  private final static String             IMAGE_EXTRATHUMBS_SIZE                   = "imageExtraThumbsSize";
  private final static String             IMAGE_EXTRATHUMBS_COUNT                  = "imageExtraThumbsCount";
  private final static String             IMAGE_EXTRAFANART                        = "imageExtraFanart";
  private final static String             IMAGE_EXTRAFANART_COUNT                  = "imageExtraFanartCount";
  private final static String             ENABLE_MOVIESET_ARTWORK_MOVIE_FOLDER     = "enableMovieSetArtworkMovieFolder";
  private final static String             ENABLE_MOVIESET_ARTWORK_FOLDER           = "enableMovieSetArtworkFolder";
  private final static String             MOVIESET_ARTWORK_FOLDER                  = "movieSetArtworkFolder";
  private final static String             MOVIE_CONNECTOR                          = "movieConnector";
  private final static String             MOVIE_NFO_FILENAME                       = "movieNfoFilename";
  private final static String             MOVIE_POSTER_FILENAME                    = "moviePosterFilename";
  private final static String             MOVIE_FANART_FILENAME                    = "movieFanartFilename";
  private final static String             MOVIE_RENAMER_PATHNAME                   = "movieRenamerPathname";
  private final static String             MOVIE_RENAMER_FILENAME                   = "movieRenamerFilename";
  private final static String             MOVIE_RENAMER_SPACE_SUBSTITUTION         = "movieRenamerSpaceSubstitution";
  private final static String             MOVIE_RENAMER_SPACE_REPLACEMENT          = "movieRenamerSpaceReplacement";
  private final static String             MOVIE_RENAMER_NFO_CLEANUP                = "movieRenamerNfoCleanup";
  private final static String             MOVIE_RENAMER_MOVIESET_SINGLE_MOVIE      = "movieRenamerMoviesetSingleMovie";
  private final static String             MOVIE_SCRAPER                            = "movieScraper";
  private final static String             MOVIE_ARTWORK_SCRAPERS                   = "movieArtworkScrapers";
  private final static String             MOVIE_TRAILER_SCRAPERS                   = "movieTrailerScrapers";
  private final static String             MOVIE_SUBTITLE_SCRAPERS                  = "movieSubtitleScrapers";
  private final static String             SCRAPE_BEST_IMAGE                        = "scrapeBestImage";
  private final static String             WRITE_ACTOR_IMAGES                       = "writeActorImages";
  private final static String             SCRAPER_LANGU                            = "scraperLanguage";
  private final static String             SUBTITLE_SCRAPER_LANGU                   = "subtitleScraperLanguage";
  private final static String             CERTIFICATION_COUNTRY                    = "certificationCountry";
  private final static String             SCRAPER_THRESHOLD                        = "scraperThreshold";
  private final static String             DETECT_MOVIE_MULTI_DIR                   = "detectMovieMultiDir";
  private final static String             BUILD_IMAGE_CACHE_ON_IMPORT              = "buildImageCacheOnImport";
  private final static String             BAD_WORDS                                = "badWords";
  private final static String             ENTRY                                    = "entry";
  private final static String             RUNTIME_FROM_MI                          = "runtimeFromMediaInfo";
  private final static String             ASCII_REPLACEMENT                        = "asciiReplacement";
  private final static String             YEAR_COLUMN_VISIBLE                      = "yearColumnVisible";
  private final static String             NFO_COLUMN_VISIBLE                       = "nfoColumnVisible";
  private final static String             METADATA_COLUMN_VISIBLE                  = "metadataColumnVisible";
  private final static String             DATE_ADDED_COLUMN_VISIBLE                = "dateAddedColumnVisible";
  private final static String             IMAGE_COLUMN_VISIBLE                     = "imageColumnVisible";
  private final static String             TRAILER_COLUMN_VISIBLE                   = "trailerColumnVisible";
  private final static String             SUBTITLE_COLUMN_VISIBLE                  = "subtitleColumnVisible";
  private final static String             WATCHED_COLUMN_VISIBLE                   = "watchedColumnVisible";
  private final static String             SCRAPER_FALLBACK                         = "scraperFallback";
  private final static String             UI_FILTERS                               = "uiFilters";
  private final static String             STORE_UI_FILTERS                         = "storeUiFilters";
  private final static String             MOVIE_SKIP_FOLDERS                       = "movieSkipFolders";

  @XmlElementWrapper(name = MOVIE_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>              movieDataSources                         = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = MOVIE_NFO_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieNfoNaming>      movieNfoFilenames                        = new ArrayList<>();

  @XmlElementWrapper(name = MOVIE_POSTER_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MoviePosterNaming>   moviePosterFilenames                     = new ArrayList<>();

  @XmlElementWrapper(name = MOVIE_FANART_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieFanartNaming>   movieFanartFilenames                     = new ArrayList<>();

  @XmlElementWrapper(name = BAD_WORDS)
  @XmlElement(name = ENTRY)
  private final List<String>              badWords                                 = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = MOVIE_ARTWORK_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>              movieArtworkScrapers                     = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = MOVIE_TRAILER_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>              movieTrailerScrapers                     = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = MOVIE_SUBTITLE_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>              movieSubtitleScrapers                    = ObservableCollections.observableList(new ArrayList<String>());

  private Map<MovieSearchOptions, Object> uiFilters                                = new HashMap<>();

  @XmlElementWrapper(name = MOVIE_SKIP_FOLDERS)
  @XmlElement(name = ENTRY)
  private final List<String>              movieSkipFolders                         = ObservableCollections.observableList(new ArrayList<String>());

  // data sources / NFO settings
  private boolean                         detectMovieMultiDir                      = false;
  private boolean                         buildImageCacheOnImport                  = false;
  private MovieConnectors                 movieConnector                           = MovieConnectors.KODI;
  private CertificationStyle              movieCertificationStyle                  = CertificationStyle.LARGE;

  // renamer
  private boolean                         movieRenameAfterScrape                   = false;
  private String                          movieRenamerPathname                     = DEFAULT_RENAMER_FOLDER_PATTERN;
  private String                          movieRenamerFilename                     = DEFAULT_RENAMER_FILE_PATTERN;
  private boolean                         movieRenamerSpaceSubstitution            = false;
  private String                          movieRenamerSpaceReplacement             = "_";
  private boolean                         movieRenamerNfoCleanup                   = false;
  private boolean                         movieRenamerCreateMoviesetForSingleMovie = false;
  private boolean                         asciiReplacement                         = false;
  private LanguageStyle                   movieRenamerLanguageStyle                = LanguageStyle.ISO3T;

  // meta data scraper
  private String                          movieScraper                             = Constants.TMDB;
  private MediaLanguages                  scraperLanguage                          = MediaLanguages.en;
  private CountryCode                     certificationCountry                     = CountryCode.US;
  private double                          scraperThreshold                         = 0.75;
  private boolean                         scraperFallback                          = false;

  // artwork scraper
  private PosterSizes                     imagePosterSize                          = PosterSizes.BIG;
  private FanartSizes                     imageFanartSize                          = FanartSizes.LARGE;
  private boolean                         imageExtraThumbs                         = false;
  private boolean                         imageExtraThumbsResize                   = true;
  private int                             imageExtraThumbsSize                     = 300;
  private int                             imageExtraThumbsCount                    = 5;
  private boolean                         imageExtraFanart                         = false;
  private int                             imageExtraFanartCount                    = 5;
  private boolean                         enableMovieSetArtworkMovieFolder         = true;
  private boolean                         enableMovieSetArtworkFolder              = false;
  private String                          movieSetArtworkFolder                    = "MoviesetArtwork";
  private boolean                         scrapeBestImage                          = true;
  private boolean                         imageLanguagePriority                    = true;
  private boolean                         imageLogo                                = true;
  private boolean                         imageBanner                              = true;
  private boolean                         imageClearart                            = true;
  private boolean                         imageDiscart                             = true;
  private boolean                         imageThumb                               = true;
  private boolean                         writeActorImages                         = false;

  // trailer scraper
  private boolean                         useTrailerPreference                     = true;
  private boolean                         automaticTrailerDownload                 = false;
  private MovieTrailerQuality             trailerQuality                           = MovieTrailerQuality.HD_720;
  private MovieTrailerSources             trailerSource                            = MovieTrailerSources.YOUTUBE;

  // subtitle scraper
  private MediaLanguages                  subtitleScraperLanguage                  = MediaLanguages.en;

  // misc
  private boolean                         runtimeFromMediaInfo                     = false;
  private boolean                         syncTrakt                                = false;

  // UI settings
  private boolean                         yearColumnVisible                        = true;
  private boolean                         ratingColumnVisible                      = false;
  private boolean                         nfoColumnVisible                         = true;
  private boolean                         metadataColumnVisible                    = true;
  private boolean                         dateAddedColumnVisible                   = false;
  private boolean                         imageColumnVisible                       = true;
  private boolean                         trailerColumnVisible                     = true;
  private boolean                         subtitleColumnVisible                    = true;
  private boolean                         watchedColumnVisible                     = true;
  private boolean                         storeUiFilters                           = false;

  public MovieSettings() {
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

  public void addMovieNfoFilename(MovieNfoNaming filename) {
    if (!movieNfoFilenames.contains(filename)) {
      movieNfoFilenames.add(filename);
      firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
    }
  }

  public void removeMovieNfoFilename(MovieNfoNaming filename) {
    if (movieNfoFilenames.contains(filename)) {
      movieNfoFilenames.remove(filename);
      firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
    }
  }

  public void clearMovieNfoFilenames() {
    movieNfoFilenames.clear();
    firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
  }

  public List<MovieNfoNaming> getMovieNfoFilenames() {
    return new ArrayList<>(this.movieNfoFilenames);
  }

  public void addMoviePosterFilename(MoviePosterNaming filename) {
    if (!moviePosterFilenames.contains(filename)) {
      moviePosterFilenames.add(filename);
      firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
    }
  }

  public void removeMoviePosterFilename(MoviePosterNaming filename) {
    if (moviePosterFilenames.contains(filename)) {
      moviePosterFilenames.remove(filename);
      firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
    }
  }

  public void clearMoviePosterFilenames() {
    moviePosterFilenames.clear();
    firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
  }

  public List<MoviePosterNaming> getMoviePosterFilenames() {
    return new ArrayList<>(this.moviePosterFilenames);
  }

  public void addMovieFanartFilename(MovieFanartNaming filename) {
    if (!movieFanartFilenames.contains(filename)) {
      movieFanartFilenames.add(filename);
      firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
    }
  }

  public void removeMovieFanartFilename(MovieFanartNaming filename) {
    if (movieFanartFilenames.contains(filename)) {
      movieFanartFilenames.remove(filename);
      firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
    }
  }

  public void clearMovieFanartFilenames() {
    movieFanartFilenames.clear();
    firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
  }

  public List<MovieFanartNaming> getMovieFanartFilenames() {
    return new ArrayList<>(this.movieFanartFilenames);
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

  @XmlElement(name = MOVIE_RENAMER_PATHNAME)
  public String getMovieRenamerPathname() {
    return movieRenamerPathname;
  }

  public void setMovieRenamerPathname(String newValue) {
    String oldValue = this.movieRenamerPathname;
    this.movieRenamerPathname = newValue;
    firePropertyChange(MOVIE_RENAMER_PATHNAME, oldValue, newValue);
  }

  @XmlElement(name = MOVIE_RENAMER_FILENAME)
  public String getMovieRenamerFilename() {
    return movieRenamerFilename;
  }

  public void setMovieRenamerFilename(String newValue) {
    String oldValue = this.movieRenamerFilename;
    this.movieRenamerFilename = newValue;
    firePropertyChange(MOVIE_RENAMER_FILENAME, oldValue, newValue);
  }

  @XmlElement(name = MOVIE_RENAMER_SPACE_SUBSTITUTION)
  public boolean isMovieRenamerSpaceSubstitution() {
    return movieRenamerSpaceSubstitution;
  }

  public void setMovieRenamerSpaceSubstitution(boolean movieRenamerSpaceSubstitution) {
    this.movieRenamerSpaceSubstitution = movieRenamerSpaceSubstitution;
  }

  public void setMovieRenameAfterScrape(boolean newValue) {
    boolean oldValue = this.movieRenameAfterScrape;
    this.movieRenameAfterScrape = newValue;
    firePropertyChange("movieRenameAfterScrape", oldValue, newValue);
  }

  public boolean isMovieRenameAfterScrape() {
    return this.movieRenameAfterScrape;
  }

  @XmlElement(name = MOVIE_RENAMER_SPACE_REPLACEMENT)
  public String getMovieRenamerSpaceReplacement() {
    return movieRenamerSpaceReplacement;
  }

  public void setMovieRenamerSpaceReplacement(String movieRenamerSpaceReplacement) {
    this.movieRenamerSpaceReplacement = movieRenamerSpaceReplacement;
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
    if (!movieArtworkScrapers.contains(newValue)) {
      movieArtworkScrapers.add(newValue);
      firePropertyChange(MOVIE_ARTWORK_SCRAPERS, null, movieArtworkScrapers);
    }
  }

  public void removeMovieArtworkScraper(String newValue) {
    if (movieArtworkScrapers.contains(newValue)) {
      movieArtworkScrapers.remove(newValue);
      firePropertyChange(MOVIE_ARTWORK_SCRAPERS, null, movieArtworkScrapers);
    }
  }

  public List<String> getMovieArtworkScrapers() {
    return movieArtworkScrapers;
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
    if (!movieTrailerScrapers.contains(newValue)) {
      movieTrailerScrapers.add(newValue);
      firePropertyChange(MOVIE_TRAILER_SCRAPERS, null, movieTrailerScrapers);
    }
  }

  public void removeMovieTrailerScraper(String newValue) {
    if (movieTrailerScrapers.contains(newValue)) {
      movieTrailerScrapers.remove(newValue);
      firePropertyChange(MOVIE_TRAILER_SCRAPERS, null, movieTrailerScrapers);
    }
  }

  public List<String> getMovieTrailerScrapers() {
    return movieTrailerScrapers;
  }

  public void addMovieSubtitleScraper(String newValue) {
    if (!movieSubtitleScrapers.contains(newValue)) {
      movieSubtitleScrapers.add(newValue);
      firePropertyChange(MOVIE_SUBTITLE_SCRAPERS, null, movieSubtitleScrapers);
    }
  }

  public void removeMovieSubtitleScraper(String newValue) {
    if (movieSubtitleScrapers.contains(newValue)) {
      movieSubtitleScrapers.remove(newValue);
      firePropertyChange(MOVIE_SUBTITLE_SCRAPERS, null, movieSubtitleScrapers);
    }
  }

  public List<String> getMovieSubtitleScrapers() {
    return movieSubtitleScrapers;
  }

  public void addMovieSkipFolder(String newValue) {
    if (!movieSkipFolders.contains(newValue)) {
      movieSkipFolders.add(newValue);
      firePropertyChange(MOVIE_SKIP_FOLDERS, null, movieSkipFolders);
    }
  }

  public void removeMovieSkipFolder(String newValue) {
    if (movieSkipFolders.contains(newValue)) {
      movieSkipFolders.remove(newValue);
      firePropertyChange(MOVIE_SKIP_FOLDERS, null, movieSkipFolders);
    }
  }

  public List<String> getMovieSkipFolders() {
    return movieSkipFolders;
  }

  public void setUiFilters(Map<MovieSearchOptions, Object> filters) {
    uiFilters = filters;
    firePropertyChange(UI_FILTERS, null, uiFilters);
  }

  @XmlElement(name = UI_FILTERS)
  @XmlJavaTypeAdapter(MovieSearchOptionsAdapter.class)
  public Map<MovieSearchOptions, Object> getUiFilters() {
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

  public boolean isStoreUiFilters() {
    return storeUiFilters;
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

  @XmlElement(name = MOVIE_RENAMER_NFO_CLEANUP)
  public boolean isMovieRenamerNfoCleanup() {
    return movieRenamerNfoCleanup;
  }

  public void setMovieRenamerNfoCleanup(boolean newValue) {
    boolean oldValue = this.movieRenamerNfoCleanup;
    this.movieRenamerNfoCleanup = newValue;
    firePropertyChange(MOVIE_RENAMER_NFO_CLEANUP, oldValue, newValue);
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

  public boolean isMovieRenamerCreateMoviesetForSingleMovie() {
    return movieRenamerCreateMoviesetForSingleMovie;
  }

  public void setMovieRenamerCreateMoviesetForSingleMovie(boolean newValue) {
    boolean oldValue = this.movieRenamerCreateMoviesetForSingleMovie;
    this.movieRenamerCreateMoviesetForSingleMovie = newValue;
    firePropertyChange(MOVIE_RENAMER_MOVIESET_SINGLE_MOVIE, oldValue, newValue);
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

  public boolean isYearColumnVisible() {
    return yearColumnVisible;
  }

  public void setYearColumnVisible(boolean newValue) {
    boolean oldValue = this.yearColumnVisible;
    this.yearColumnVisible = newValue;
    firePropertyChange(YEAR_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isRatingColumnVisible() {
    return ratingColumnVisible;
  }

  public void setRatingColumnVisible(boolean newValue) {
    boolean oldValue = this.ratingColumnVisible;
    this.ratingColumnVisible = newValue;
    firePropertyChange("ratingColumnVisible", oldValue, newValue);
  }

  public boolean isNfoColumnVisible() {
    return nfoColumnVisible;
  }

  public void setNfoColumnVisible(boolean newValue) {
    boolean oldValue = this.nfoColumnVisible;
    this.nfoColumnVisible = newValue;
    firePropertyChange(NFO_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isMetadataColumnVisible() {
    return metadataColumnVisible;
  }

  public void setMetadataColumnVisible(boolean newValue) {
    boolean oldValue = this.metadataColumnVisible;
    this.metadataColumnVisible = newValue;
    firePropertyChange(METADATA_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isDateAddedColumnVisible() {
    return dateAddedColumnVisible;
  }

  public void setDateAddedColumnVisible(boolean newValue) {
    boolean oldValue = this.dateAddedColumnVisible;
    this.dateAddedColumnVisible = newValue;
    firePropertyChange(DATE_ADDED_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isImageColumnVisible() {
    return imageColumnVisible;
  }

  public void setImageColumnVisible(boolean newValue) {
    boolean oldValue = this.imageColumnVisible;
    this.imageColumnVisible = newValue;
    firePropertyChange(IMAGE_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isTrailerColumnVisible() {
    return trailerColumnVisible;
  }

  public void setTrailerColumnVisible(boolean newValue) {
    boolean oldValue = this.trailerColumnVisible;
    this.trailerColumnVisible = newValue;
    firePropertyChange(TRAILER_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isSubtitleColumnVisible() {
    return subtitleColumnVisible;
  }

  public void setSubtitleColumnVisible(boolean newValue) {
    boolean oldValue = this.subtitleColumnVisible;
    this.subtitleColumnVisible = newValue;
    firePropertyChange(SUBTITLE_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isWatchedColumnVisible() {
    return watchedColumnVisible;
  }

  public void setWatchedColumnVisible(boolean newValue) {
    boolean oldValue = this.watchedColumnVisible;
    this.watchedColumnVisible = newValue;
    firePropertyChange(WATCHED_COLUMN_VISIBLE, oldValue, newValue);
  }

  public boolean isScraperFallback() {
    return scraperFallback;
  }

  public void setScraperFallback(boolean newValue) {
    boolean oldValue = this.scraperFallback;
    this.scraperFallback = newValue;
    firePropertyChange(SCRAPER_FALLBACK, oldValue, newValue);
  }

  public boolean isImageLogo() {
    return imageLogo;
  }

  public boolean isImageBanner() {
    return imageBanner;
  }

  public boolean isImageClearart() {
    return imageClearart;
  }

  public boolean isImageDiscart() {
    return imageDiscart;
  }

  public boolean isImageThumb() {
    return imageThumb;
  }

  public void setImageLogo(boolean newValue) {
    boolean oldValue = this.imageLogo;
    this.imageLogo = newValue;
    firePropertyChange("imageLogo", oldValue, newValue);
  }

  public void setImageBanner(boolean newValue) {
    boolean oldValue = this.imageBanner;
    this.imageBanner = newValue;
    firePropertyChange("imageBanner", oldValue, newValue);
  }

  public void setImageClearart(boolean newValue) {
    boolean oldValue = this.imageClearart;
    this.imageClearart = newValue;
    firePropertyChange("imageClearart", oldValue, newValue);
  }

  public void setImageDiscart(boolean newValue) {
    boolean oldValue = this.imageDiscart;
    this.imageDiscart = newValue;
    firePropertyChange("imageDiscart", oldValue, newValue);
  }

  public void setImageThumb(boolean newValue) {
    boolean oldValue = this.imageThumb;
    this.imageThumb = newValue;
    firePropertyChange("imageThumb", oldValue, newValue);
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

  public CertificationStyle getMovieCertificationStyle() {
    return movieCertificationStyle;
  }

  public void setMovieCertificationStyle(CertificationStyle newValue) {
    CertificationStyle oldValue = this.movieCertificationStyle;
    this.movieCertificationStyle = newValue;
    firePropertyChange("movieCertificationStyle", oldValue, newValue);
  }

  public LanguageStyle getMovieRenamerLanguageStyle() {
    return movieRenamerLanguageStyle;
  }

  public void setMovieRenamerLanguageStyle(LanguageStyle newValue) {
    LanguageStyle oldValue = this.movieRenamerLanguageStyle;
    this.movieRenamerLanguageStyle = newValue;
    firePropertyChange("movieRenamerLanguageStyle", oldValue, newValue);
  }
}
