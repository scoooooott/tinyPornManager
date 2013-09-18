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
package org.tinymediamanager.core.movie;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.imdb.ImdbSiteDefinition;

/**
 * The Class MovieSettings.
 */
@XmlRootElement(name = "MovieSettings")
public class MovieSettings extends AbstractModelObject {
  private final static String           PATH                             = "path";
  private final static String           FILENAME                         = "filename";
  private final static String           MOVIE_DATA_SOURCE                = "movieDataSource";
  private final static String           IMAGE_POSTER_SIZE                = "imagePosterSize";
  private final static String           IMAGE_FANART_SIZE                = "imageFanartSize";
  private final static String           IMAGE_EXTRATHUMBS                = "imageExtraThumbs";
  private final static String           IMAGE_EXTRATHUMBS_RESIZE         = "imageExtraThumbsResize";
  private final static String           IMAGE_EXTRATHUMBS_SIZE           = "imageExtraThumbsSize";
  private final static String           IMAGE_EXTRATHUMBS_COUNT          = "imageExtraThumbsCount";
  private final static String           IMAGE_EXTRAFANART                = "imageExtraFanart";
  private final static String           IMAGE_EXTRAFANART_COUNT          = "imageExtraFanartCount";
  private final static String           ENABLE_MOVIESET_ARTWORK_FOLDER   = "enableMovieSetArtworkFolder";
  private final static String           MOVIESET_ARTWORK_FOLDER          = "movieSetArtworkFolder";
  private final static String           MOVIE_CONNECTOR                  = "movieConnector";
  private final static String           MOVIE_NFO_FILENAME               = "movieNfoFilename";
  private final static String           MOVIE_POSTER_FILENAME            = "moviePosterFilename";
  private final static String           MOVIE_FANART_FILENAME            = "movieFanartFilename";
  private final static String           MOVIE_RENAMER_PATHNAME           = "movieRenamerPathname";
  private final static String           MOVIE_RENAMER_FILENAME           = "movieRenamerFilename";
  private final static String           MOVIE_RENAMER_SPACE_SUBSTITUTION = "movieRenamerSpaceSubstitution";
  private final static String           MOVIE_RENAMER_SPACE_REPLACEMENT  = "movieRenamerSpaceReplacement";
  private final static String           MOVIE_RENAMER_NFO_CLEANUP        = "movieRenamerNfoCleanup";
  private final static String           MOVIE_SCRAPER                    = "movieScraper";
  private final static String           SCRAPE_BEST_IMAGE                = "scrapeBestImage";
  private final static String           IMAGE_SCRAPER_TMDB               = "imageScraperTmdb";
  private final static String           IMAGE_SCRAPER_FANART_TV          = "imageScraperFanartTv";
  private final static String           TRAILER_SCRAPER_TMDB             = "trailerScraperTmdb";
  private final static String           TRAILER_SCRAPER_HD_TRAILERS      = "trailerScraperHdTrailers";
  private final static String           TRAILER_SCRAPER_OFDB             = "trailerScraperOfdb";
  private final static String           WRITE_ACTOR_IMAGES               = "writeActorImages";
  private final static String           IMDB_SCRAPE_FOREIGN_LANGU        = "imdbScrapeForeignLanguage";
  private final static String           IMDB_SITE                        = "imdbSite";
  private final static String           SCRAPER_LANGU                    = "scraperLanguage";
  private final static String           CERTIFICATION_COUNTRY            = "certificationCountry";
  private final static String           DETECT_MOVIE_MULTI_DIR           = "detectMovieMultiDir";

  @XmlElementWrapper(name = MOVIE_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>            movieDataSources                 = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = MOVIE_NFO_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieNfoNaming>    movieNfoFilenames                = new ArrayList<MovieNfoNaming>();

  @XmlElementWrapper(name = MOVIE_POSTER_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MoviePosterNaming> moviePosterFilenames             = new ArrayList<MoviePosterNaming>();

  @XmlElementWrapper(name = MOVIE_FANART_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieFanartNaming> movieFanartFilenames             = new ArrayList<MovieFanartNaming>();

  private MovieConnectors               movieConnector                   = MovieConnectors.XBMC;
  private String                        movieRenamerPathname             = "$T ($Y)";
  private String                        movieRenamerFilename             = "$T ($Y) $V $A";
  private boolean                       movieRenamerSpaceSubstitution    = false;
  private String                        movieRenamerSpaceReplacement     = "_";
  private boolean                       movieRenamerNfoCleanup           = false;
  private boolean                       imdbScrapeForeignLanguage        = false;
  private MovieScrapers                 movieScraper                     = MovieScrapers.TMDB;
  private PosterSizes                   imagePosterSize                  = PosterSizes.BIG;
  private boolean                       imageScraperTmdb                 = true;
  private boolean                       imageScraperFanartTv             = true;
  private FanartSizes                   imageFanartSize                  = FanartSizes.LARGE;
  private boolean                       imageExtraThumbs                 = false;
  private boolean                       imageExtraThumbsResize           = true;
  private int                           imageExtraThumbsSize             = 300;
  private int                           imageExtraThumbsCount            = 5;
  private boolean                       imageExtraFanart                 = false;
  private int                           imageExtraFanartCount            = 5;
  private boolean                       enableMovieSetArtworkFolder      = false;
  private String                        movieSetArtworkFolder            = "MoviesetArtwork";
  private ImdbSiteDefinition            imdbSite                         = ImdbSiteDefinition.IMDB_COM;
  private boolean                       scrapeBestImage                  = true;
  private boolean                       trailerScraperTmdb               = true;
  private boolean                       trailerScraperHdTrailers         = true;
  private boolean                       trailerScraperOfdb               = true;
  private boolean                       writeActorImages                 = false;
  private MediaLanguages                scraperLanguage                  = MediaLanguages.en;
  private CountryCode                   certificationCountry             = CountryCode.US;
  private boolean                       detectMovieMultiDir              = false;

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
    return this.movieNfoFilenames;
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
    return this.moviePosterFilenames;
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
    return this.movieFanartFilenames;
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

  @XmlElement(name = MOVIE_RENAMER_SPACE_REPLACEMENT)
  public String getMovieRenamerSpaceReplacement() {
    return movieRenamerSpaceReplacement;
  }

  public void setMovieRenamerSpaceReplacement(String movieRenamerSpaceReplacement) {
    this.movieRenamerSpaceReplacement = movieRenamerSpaceReplacement;
  }

  public MovieScrapers getMovieScraper() {
    if (movieScraper == null) {
      return MovieScrapers.TMDB;
    }
    return movieScraper;
  }

  public void setMovieScraper(MovieScrapers newValue) {
    MovieScrapers oldValue = this.movieScraper;
    this.movieScraper = newValue;
    firePropertyChange(MOVIE_SCRAPER, oldValue, newValue);
  }

  public boolean isImdbScrapeForeignLanguage() {
    return imdbScrapeForeignLanguage;
  }

  public void setImdbScrapeForeignLanguage(boolean newValue) {
    boolean oldValue = this.imdbScrapeForeignLanguage;
    this.imdbScrapeForeignLanguage = newValue;
    firePropertyChange(IMDB_SCRAPE_FOREIGN_LANGU, oldValue, newValue);
  }

  public ImdbSiteDefinition getImdbSite() {
    return imdbSite;
  }

  public void setImdbSite(ImdbSiteDefinition newValue) {
    ImdbSiteDefinition oldValue = this.imdbSite;
    this.imdbSite = newValue;
    firePropertyChange(IMDB_SITE, oldValue, newValue);
  }

  public boolean isImageScraperTmdb() {
    return imageScraperTmdb;
  }

  public boolean isImageScraperFanartTv() {
    return imageScraperFanartTv;
  }

  public void setImageScraperTmdb(boolean newValue) {
    boolean oldValue = this.imageScraperTmdb;
    this.imageScraperTmdb = newValue;
    firePropertyChange(IMAGE_SCRAPER_TMDB, oldValue, newValue);
  }

  public void setImageScraperFanartTv(boolean newValue) {
    boolean oldValue = this.imageScraperFanartTv;
    this.imageScraperFanartTv = newValue;
    firePropertyChange(IMAGE_SCRAPER_FANART_TV, oldValue, newValue);
  }

  public boolean isScrapeBestImage() {
    return scrapeBestImage;
  }

  public void setScrapeBestImage(boolean newValue) {
    boolean oldValue = this.scrapeBestImage;
    this.scrapeBestImage = newValue;
    firePropertyChange(SCRAPE_BEST_IMAGE, oldValue, newValue);
  }

  public boolean isTrailerScraperTmdb() {
    return trailerScraperTmdb;
  }

  public boolean isTrailerScraperHdTrailers() {
    return trailerScraperHdTrailers;
  }

  public void setTrailerScraperTmdb(boolean newValue) {
    boolean oldValue = this.trailerScraperTmdb;
    this.trailerScraperTmdb = newValue;
    firePropertyChange(TRAILER_SCRAPER_TMDB, oldValue, newValue);
  }

  public void setTrailerScraperHdTrailers(boolean newValue) {
    boolean oldValue = this.trailerScraperHdTrailers;
    this.trailerScraperHdTrailers = newValue;
    firePropertyChange(TRAILER_SCRAPER_HD_TRAILERS, oldValue, newValue);
  }

  public boolean isTrailerScraperOfdb() {
    return trailerScraperOfdb;
  }

  public void setTrailerScraperOfdb(boolean newValue) {
    boolean oldValue = this.trailerScraperOfdb;
    this.trailerScraperOfdb = newValue;
    firePropertyChange(TRAILER_SCRAPER_OFDB, oldValue, newValue);
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

  @XmlElement(name = CERTIFICATION_COUNTRY)
  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  @XmlElement(name = MOVIE_RENAMER_NFO_CLEANUP)
  public boolean isMovieRenamerNfoCleanup() {
    return movieRenamerNfoCleanup;
  }

  public void setMovieRenamerNfoCleanup(boolean movieRenamerNfoCleanup) {
    this.movieRenamerNfoCleanup = movieRenamerNfoCleanup;
  }

  /**
   * Should we detect (and create) movies from directories containing more than one movie?
   * 
   * @return true/false
   */
  public boolean isDetectMovieMultiDir() {
    return detectMovieMultiDir;
  }

  /**
   * Should we detect (and create) movies from directories containing more than one movie?
   * 
   * @param newValue
   *          true/false
   */
  public void setDetectMovieMultiDir(boolean newValue) {
    boolean oldValue = this.detectMovieMultiDir;
    this.detectMovieMultiDir = newValue;
    firePropertyChange(DETECT_MOVIE_MULTI_DIR, oldValue, newValue);
  }
}
