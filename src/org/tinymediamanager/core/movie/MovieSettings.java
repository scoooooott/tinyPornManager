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
import org.tinymediamanager.scraper.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.imdb.ImdbSiteDefinition;

/**
 * The Class MovieSettings.
 */
@XmlRootElement(name = "MovieSettings")
public class MovieSettings extends AbstractModelObject {

  /** The Constant PATH. */
  private final static String           PATH                           = "path";

  /** The Constant FILENAME. */
  private final static String           FILENAME                       = "filename";

  /** The Constant MOVIE_DATA_SOURCE. */
  private final static String           MOVIE_DATA_SOURCE              = "movieDataSource";

  /** The Constant IMAGE_POSTER_SIZE. */
  private final static String           IMAGE_POSTER_SIZE              = "imagePosterSize";

  /** The Constant IMAGE_FANART_SIZE. */
  private final static String           IMAGE_FANART_SIZE              = "imageFanartSize";

  /** The Constant IMAGE_EXTRATHUMBS. */
  private final static String           IMAGE_EXTRATHUMBS              = "imageExtraThumbs";

  /** The Constant IMAGE_EXTRATHUMBS_RESIZE. */
  private final static String           IMAGE_EXTRATHUMBS_RESIZE       = "imageExtraThumbsResize";

  /** The Constant IMAGE_EXTRATHUMBS_SIZE. */
  private final static String           IMAGE_EXTRATHUMBS_SIZE         = "imageExtraThumbsSize";

  /** The Constant IMAGE_EXTRATHUMBS_COUNT. */
  private final static String           IMAGE_EXTRATHUMBS_COUNT        = "imageExtraThumbsCount";

  /** The Constant IMAGE_EXTRAFANART. */
  private final static String           IMAGE_EXTRAFANART              = "imageExtraFanart";

  /** The Constant IMAGE_EXTRAFANART_COUNT. */
  private final static String           IMAGE_EXTRAFANART_COUNT        = "imageExtraFanartCount";

  /** The Constant ENABLE_MOVIESET_ARTWORK_FOLDER. */
  private final static String           ENABLE_MOVIESET_ARTWORK_FOLDER = "enableMovieSetArtworkFolder";

  /** The Constant MOVIESET_ARTWORK_FOLDER. */
  private final static String           MOVIESET_ARTWORK_FOLDER        = "movieSetArtworkFolder";

  /** The Constant MOVIE_CONNECTOR. */
  private final static String           MOVIE_CONNECTOR                = "movieConnector";

  /** The Constant MOVIE_NFO_FILENAME. */
  private final static String           MOVIE_NFO_FILENAME             = "movieNfoFilename";

  /** The Constant MOVIE_POSTER_FILENAME. */
  private final static String           MOVIE_POSTER_FILENAME          = "moviePosterFilename";

  /** The Constant MOVIE_FANART_FILENAME. */
  private final static String           MOVIE_FANART_FILENAME          = "movieFanartFilename";

  /** The Constant MOVIE_RENAMER_PATHNAME. */
  private final static String           MOVIE_RENAMER_PATHNAME         = "movieRenamerPathname";

  /** The Constant MOVIE_RENAMER_FILENAME. */
  private final static String           MOVIE_RENAMER_FILENAME         = "movieRenamerFilename";

  /** The Constant MOVIE_SCRAPER. */
  private final static String           MOVIE_SCRAPER                  = "movieScraper";

  /** The Constant SCRAPE_BEST_IMAGE. */
  private final static String           SCRAPE_BEST_IMAGE              = "scrapeBestImage";

  /** The Constant IMAGE_SCRAPER_TMDB. */
  private final static String           IMAGE_SCRAPER_TMDB             = "imageScraperTmdb";

  /** The Constant IMAGE_SCRAPER_FANART_TV. */
  private final static String           IMAGE_SCRAPER_FANART_TV        = "imageScraperFanartTv";

  /** The Constant TRAILER_SCRAPER_TMDB. */
  private final static String           TRAILER_SCRAPER_TMDB           = "trailerScraperTmdb";

  /** The Constant TRAILER_SCRAPER_HD_TRAILERS. */
  private final static String           TRAILER_SCRAPER_HD_TRAILERS    = "trailerScraperHdTrailers";

  /** The Constant TRAILER_SCRAPER_OFDB. */
  private final static String           TRAILER_SCRAPER_OFDB           = "trailerScraperOfdb";

  /** The Constant WRITE_ACTOR_IMAGES. */
  private final static String           WRITE_ACTOR_IMAGES             = "writeActorImages";

  /** The Constant IMDB_SCRAPE_FOREIGN_LANGU. */
  private final static String           IMDB_SCRAPE_FOREIGN_LANGU      = "imdbScrapeForeignLanguage";

  /** The Constant IMDB_SITE. */
  private final static String           IMDB_SITE                      = "imdbSite";

  /** The movie data sources. */
  @XmlElementWrapper(name = MOVIE_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>            movieDataSources               = ObservableCollections.observableList(new ArrayList<String>());

  /** The movie nfo filenames. */
  @XmlElementWrapper(name = MOVIE_NFO_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieNfoNaming>    movieNfoFilenames              = new ArrayList<MovieNfoNaming>();

  /** The movie poster filenames. */
  @XmlElementWrapper(name = MOVIE_POSTER_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MoviePosterNaming> moviePosterFilenames           = new ArrayList<MoviePosterNaming>();

  /** The movie fanart filenames. */
  @XmlElementWrapper(name = MOVIE_FANART_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<MovieFanartNaming> movieFanartFilenames           = new ArrayList<MovieFanartNaming>();

  /** The movie connector. */
  private MovieConnectors               movieConnector                 = MovieConnectors.XBMC;

  /** The movie renamer pathname. */
  private String                        movieRenamerPathname           = "$T ($Y)";

  /** The movie renamer filename. */
  private String                        movieRenamerFilename           = "$T ($Y)";

  /** The imdb scrape foreign language. */
  private boolean                       imdbScrapeForeignLanguage      = false;

  /** The movie scraper. */
  private MovieScrapers                 movieScraper                   = MovieScrapers.TMDB;

  /** The image tmdb poster size. */
  private PosterSizes                   imagePosterSize                = PosterSizes.BIG;

  /** The image tmdb scraper. */
  private boolean                       imageScraperTmdb               = true;

  /** The image fanart tv scraper. */
  private boolean                       imageScraperFanartTv           = true;

  /** The image tmdb fanart size. */
  private FanartSizes                   imageFanartSize                = FanartSizes.LARGE;

  /** The image extra thumbs. */
  private boolean                       imageExtraThumbs               = false;

  /** The image extra thumbs resize. */
  private boolean                       imageExtraThumbsResize         = true;

  /** The image extra thumbs size. */
  private int                           imageExtraThumbsSize           = 300;

  /** The image extra thumbs count. */
  private int                           imageExtraThumbsCount          = 5;

  /** The image extra fanart. */
  private boolean                       imageExtraFanart               = false;

  /** The image extra fanart count. */
  private int                           imageExtraFanartCount          = 5;

  /** The enable movie set artwork folder. */
  private boolean                       enableMovieSetArtworkFolder    = false;

  /** The movie set artwork folder. */
  private String                        movieSetArtworkFolder          = "MoviesetArtwork";

  /** The imdb site. */
  private ImdbSiteDefinition            imdbSite                       = ImdbSiteDefinition.IMDB_COM;

  /** The scrape best image. */
  private boolean                       scrapeBestImage                = true;

  /** The trailer tmdb scraper. */
  private boolean                       trailerScraperTmdb             = true;

  /** The trailer hd-trailers.net scraper. */
  private boolean                       trailerScraperHdTrailers       = true;

  /** The trailer ofdb.de scraper. */
  private boolean                       trailerScraperOfdb             = true;

  /** The write actor images. */
  private boolean                       writeActorImages               = false;

  /**
   * Instantiates a new movie settings.
   */
  public MovieSettings() {
  }

  /**
   * Adds the movie data sources.
   * 
   * @param path
   *          the path
   */
  public void addMovieDataSources(String path) {
    movieDataSources.add(path);
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
  }

  /**
   * Removes the movie data sources.
   * 
   * @param path
   *          the path
   */
  public void removeMovieDataSources(String path) {
    MovieList movieList = MovieList.getInstance();
    movieList.removeDatasource(path);
    movieDataSources.remove(path);
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
  }

  /**
   * Gets the movie data source.
   * 
   * @return the movie data source
   */
  public List<String> getMovieDataSource() {
    return movieDataSources;
  }

  /**
   * Adds the movie nfo filename.
   * 
   * @param filename
   *          the filename
   */
  public void addMovieNfoFilename(MovieNfoNaming filename) {
    if (!movieNfoFilenames.contains(filename)) {
      movieNfoFilenames.add(filename);
      firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
    }
  }

  /**
   * Removes the movie nfo filename.
   * 
   * @param filename
   *          the filename
   */
  public void removeMovieNfoFilename(MovieNfoNaming filename) {
    if (movieNfoFilenames.contains(filename)) {
      movieNfoFilenames.remove(filename);
      firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
    }
  }

  /**
   * Clear movie nfo filenames.
   */
  public void clearMovieNfoFilenames() {
    movieNfoFilenames.clear();
    firePropertyChange(MOVIE_NFO_FILENAME, null, movieNfoFilenames);
  }

  /**
   * Gets the movie nfo filenames.
   * 
   * @return the movie nfo filenames
   */
  public List<MovieNfoNaming> getMovieNfoFilenames() {
    return this.movieNfoFilenames;
  }

  /**
   * Adds the movie poster filename.
   * 
   * @param filename
   *          the filename
   */
  public void addMoviePosterFilename(MoviePosterNaming filename) {
    if (!moviePosterFilenames.contains(filename)) {
      moviePosterFilenames.add(filename);
      firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
    }
  }

  /**
   * Removes the movie poster filename.
   * 
   * @param filename
   *          the filename
   */
  public void removeMoviePosterFilename(MoviePosterNaming filename) {
    if (moviePosterFilenames.contains(filename)) {
      moviePosterFilenames.remove(filename);
      firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
    }
  }

  /**
   * Clear movie poster filenames.
   */
  public void clearMoviePosterFilenames() {
    moviePosterFilenames.clear();
    firePropertyChange(MOVIE_POSTER_FILENAME, null, moviePosterFilenames);
  }

  /**
   * Gets the movie poster filenames.
   * 
   * @return the movie poster filenames
   */
  public List<MoviePosterNaming> getMoviePosterFilenames() {
    return this.moviePosterFilenames;
  }

  /**
   * Adds the movie fanart filename.
   * 
   * @param filename
   *          the filename
   */
  public void addMovieFanartFilename(MovieFanartNaming filename) {
    if (!movieFanartFilenames.contains(filename)) {
      movieFanartFilenames.add(filename);
      firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
    }
  }

  /**
   * Removes the movie fanart filename.
   * 
   * @param filename
   *          the filename
   */
  public void removeMovieFanartFilename(MovieFanartNaming filename) {
    if (movieFanartFilenames.contains(filename)) {
      movieFanartFilenames.remove(filename);
      firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
    }
  }

  /**
   * Clear movie fanart filenames.
   */
  public void clearMovieFanartFilenames() {
    movieFanartFilenames.clear();
    firePropertyChange(MOVIE_FANART_FILENAME, null, movieFanartFilenames);
  }

  /**
   * Gets the movie fanart filenames.
   * 
   * @return the movie fanart filenames
   */
  public List<MovieFanartNaming> getMovieFanartFilenames() {
    return this.movieFanartFilenames;
  }

  /**
   * Gets the image poster size.
   * 
   * @return the image poster size
   */
  @XmlElement(name = IMAGE_POSTER_SIZE)
  public PosterSizes getImagePosterSize() {
    return imagePosterSize;
  }

  /**
   * Sets the image poster size.
   * 
   * @param newValue
   *          the new image poster size
   */
  public void setImagePosterSize(PosterSizes newValue) {
    PosterSizes oldValue = this.imagePosterSize;
    this.imagePosterSize = newValue;
    firePropertyChange(IMAGE_POSTER_SIZE, oldValue, newValue);
  }

  /**
   * Gets the image fanart size.
   * 
   * @return the image fanart size
   */
  @XmlElement(name = IMAGE_FANART_SIZE)
  public FanartSizes getImageFanartSize() {
    return imageFanartSize;
  }

  /**
   * Sets the image fanart size.
   * 
   * @param newValue
   *          the new image fanart size
   */
  public void setImageFanartSize(FanartSizes newValue) {
    FanartSizes oldValue = this.imageFanartSize;
    this.imageFanartSize = newValue;
    firePropertyChange(IMAGE_FANART_SIZE, oldValue, newValue);
  }

  /**
   * Checks if is image extra thumbs.
   * 
   * @return true, if is image extra thumbs
   */
  public boolean isImageExtraThumbs() {
    return imageExtraThumbs;
  }

  /**
   * Checks if is image extra thumbs resize.
   * 
   * @return true, if is image extra thumbs resize
   */
  public boolean isImageExtraThumbsResize() {
    return imageExtraThumbsResize;
  }

  /**
   * Gets the image extra thumbs size.
   * 
   * @return the image extra thumbs size
   */
  public int getImageExtraThumbsSize() {
    return imageExtraThumbsSize;
  }

  /**
   * Sets the image extra thumbs resize.
   * 
   * @param newValue
   *          the new image extra thumbs resize
   */
  public void setImageExtraThumbsResize(boolean newValue) {
    boolean oldValue = this.imageExtraThumbsResize;
    this.imageExtraThumbsResize = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_RESIZE, oldValue, newValue);
  }

  /**
   * Sets the image extra thumbs size.
   * 
   * @param newValue
   *          the new image extra thumbs size
   */
  public void setImageExtraThumbsSize(int newValue) {
    int oldValue = this.imageExtraThumbsSize;
    this.imageExtraThumbsSize = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_SIZE, oldValue, newValue);
  }

  /**
   * Gets the image extra thumbs count.
   * 
   * @return the image extra thumbs count
   */
  public int getImageExtraThumbsCount() {
    return imageExtraThumbsCount;
  }

  /**
   * Sets the image extra thumbs count.
   * 
   * @param newValue
   *          the new image extra thumbs count
   */
  public void setImageExtraThumbsCount(int newValue) {
    int oldValue = this.imageExtraThumbsCount;
    this.imageExtraThumbsCount = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS_COUNT, oldValue, newValue);
  }

  /**
   * Gets the image extra fanart count.
   * 
   * @return the image extra fanart count
   */
  public int getImageExtraFanartCount() {
    return imageExtraFanartCount;
  }

  /**
   * Sets the image extra fanart count.
   * 
   * @param newValue
   *          the new image extra fanart count
   */
  public void setImageExtraFanartCount(int newValue) {
    int oldValue = this.imageExtraFanartCount;
    this.imageExtraFanartCount = newValue;
    firePropertyChange(IMAGE_EXTRAFANART_COUNT, oldValue, newValue);
  }

  /**
   * Checks if is image extra fanart.
   * 
   * @return true, if is image extra fanart
   */
  public boolean isImageExtraFanart() {
    return imageExtraFanart;
  }

  /**
   * Sets the image extra thumbs.
   * 
   * @param newValue
   *          the new image extra thumbs
   */
  public void setImageExtraThumbs(boolean newValue) {
    boolean oldValue = this.imageExtraThumbs;
    this.imageExtraThumbs = newValue;
    firePropertyChange(IMAGE_EXTRATHUMBS, oldValue, newValue);
  }

  /**
   * Sets the image extra fanart.
   * 
   * @param newValue
   *          the new image extra fanart
   */
  public void setImageExtraFanart(boolean newValue) {
    boolean oldValue = this.imageExtraFanart;
    this.imageExtraFanart = newValue;
    firePropertyChange(IMAGE_EXTRAFANART, oldValue, newValue);
  }

  /**
   * Checks if is enable movie set artwork folder.
   * 
   * @return true, if is enable movie set artwork folder
   */
  public boolean isEnableMovieSetArtworkFolder() {
    return enableMovieSetArtworkFolder;
  }

  /**
   * Sets the enable movie set artwork folder.
   * 
   * @param newValue
   *          the new enable movie set artwork folder
   */
  public void setEnableMovieSetArtworkFolder(boolean newValue) {
    boolean oldValue = this.enableMovieSetArtworkFolder;
    this.enableMovieSetArtworkFolder = newValue;
    firePropertyChange(ENABLE_MOVIESET_ARTWORK_FOLDER, oldValue, newValue);
  }

  /**
   * Gets the movie set artwork folder.
   * 
   * @return the movie set artwork folder
   */
  public String getMovieSetArtworkFolder() {
    return movieSetArtworkFolder;
  }

  /**
   * Sets the movie set artwork folder.
   * 
   * @param newValue
   *          the new movie set artwork folder
   */
  public void setMovieSetArtworkFolder(String newValue) {
    String oldValue = this.movieSetArtworkFolder;
    this.movieSetArtworkFolder = newValue;
    firePropertyChange(MOVIESET_ARTWORK_FOLDER, oldValue, newValue);
  }

  /**
   * Gets the movie connector.
   * 
   * @return the movie connector
   */
  @XmlElement(name = MOVIE_CONNECTOR)
  public MovieConnectors getMovieConnector() {
    return movieConnector;
  }

  /**
   * Sets the movie connector.
   * 
   * @param newValue
   *          the new movie connector
   */
  public void setMovieConnector(MovieConnectors newValue) {
    MovieConnectors oldValue = this.movieConnector;
    this.movieConnector = newValue;
    firePropertyChange(MOVIE_CONNECTOR, oldValue, newValue);
  }

  /**
   * Gets the movie renamer pathname.
   * 
   * @return the movie renamer pathname
   */
  @XmlElement(name = MOVIE_RENAMER_PATHNAME)
  public String getMovieRenamerPathname() {
    return movieRenamerPathname;
  }

  /**
   * Sets the movie renamer pathname.
   * 
   * @param newValue
   *          the new movie renamer pathname
   */
  public void setMovieRenamerPathname(String newValue) {
    String oldValue = this.movieRenamerPathname;
    this.movieRenamerPathname = newValue;
    firePropertyChange(MOVIE_RENAMER_PATHNAME, oldValue, newValue);
  }

  /**
   * Gets the movie renamer filename.
   * 
   * @return the movie renamer filename
   */
  @XmlElement(name = MOVIE_RENAMER_FILENAME)
  public String getMovieRenamerFilename() {
    return movieRenamerFilename;
  }

  /**
   * Sets the movie renamer filename.
   * 
   * @param newValue
   *          the new movie renamer filename
   */
  public void setMovieRenamerFilename(String newValue) {
    String oldValue = this.movieRenamerFilename;
    this.movieRenamerFilename = newValue;
    firePropertyChange(MOVIE_RENAMER_FILENAME, oldValue, newValue);
  }

  /**
   * Gets the movie scraper.
   * 
   * @return the movie scraper
   */
  public MovieScrapers getMovieScraper() {
    if (movieScraper == null) {
      return MovieScrapers.TMDB;
    }
    return movieScraper;
  }

  /**
   * Sets the movie scraper.
   * 
   * @param newValue
   *          the new movie scraper
   */
  public void setMovieScraper(MovieScrapers newValue) {
    MovieScrapers oldValue = this.movieScraper;
    this.movieScraper = newValue;
    firePropertyChange(MOVIE_SCRAPER, oldValue, newValue);
  }

  /**
   * Checks if is imdb scrape foreign language.
   * 
   * @return true, if is imdb scrape foreign language
   */
  public boolean isImdbScrapeForeignLanguage() {
    return imdbScrapeForeignLanguage;
  }

  /**
   * Sets the imdb scrape foreign language.
   * 
   * @param newValue
   *          the new imdb scrape foreign language
   */
  public void setImdbScrapeForeignLanguage(boolean newValue) {
    boolean oldValue = this.imdbScrapeForeignLanguage;
    this.imdbScrapeForeignLanguage = newValue;
    firePropertyChange(IMDB_SCRAPE_FOREIGN_LANGU, oldValue, newValue);
  }

  /**
   * Gets the imdb site.
   * 
   * @return the imdb site
   */
  public ImdbSiteDefinition getImdbSite() {
    return imdbSite;
  }

  /**
   * Sets the imdb site.
   * 
   * @param newValue
   *          the new imdb site
   */
  public void setImdbSite(ImdbSiteDefinition newValue) {
    ImdbSiteDefinition oldValue = this.imdbSite;
    this.imdbSite = newValue;
    firePropertyChange(IMDB_SITE, oldValue, newValue);
  }

  /**
   * Checks if is image scraper tmdb.
   * 
   * @return true, if is image scraper tmdb
   */
  public boolean isImageScraperTmdb() {
    return imageScraperTmdb;
  }

  /**
   * Checks if is image scraper fanart tv.
   * 
   * @return true, if is image scraper fanart tv
   */
  public boolean isImageScraperFanartTv() {
    return imageScraperFanartTv;
  }

  /**
   * Sets the image scraper tmdb.
   * 
   * @param newValue
   *          the new image scraper tmdb
   */
  public void setImageScraperTmdb(boolean newValue) {
    boolean oldValue = this.imageScraperTmdb;
    this.imageScraperTmdb = newValue;
    firePropertyChange(IMAGE_SCRAPER_TMDB, oldValue, newValue);
  }

  /**
   * Sets the image scraper fanart tv.
   * 
   * @param newValue
   *          the new image scraper fanart tv
   */
  public void setImageScraperFanartTv(boolean newValue) {
    boolean oldValue = this.imageScraperFanartTv;
    this.imageScraperFanartTv = newValue;
    firePropertyChange(IMAGE_SCRAPER_FANART_TV, oldValue, newValue);
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
   * Checks if is trailer scraper tmdb.
   * 
   * @return true, if is trailer scraper tmdb
   */
  public boolean isTrailerScraperTmdb() {
    return trailerScraperTmdb;
  }

  /**
   * Checks if is trailer scraper hd trailers.
   * 
   * @return true, if is trailer scraper hd trailers
   */
  public boolean isTrailerScraperHdTrailers() {
    return trailerScraperHdTrailers;
  }

  /**
   * Sets the trailer scraper tmdb.
   * 
   * @param newValue
   *          the new trailer scraper tmdb
   */
  public void setTrailerScraperTmdb(boolean newValue) {
    boolean oldValue = this.trailerScraperTmdb;
    this.trailerScraperTmdb = newValue;
    firePropertyChange(TRAILER_SCRAPER_TMDB, oldValue, newValue);
  }

  /**
   * Sets the trailer scraper hd trailers.
   * 
   * @param newValue
   *          the new trailer scraper hd trailers
   */
  public void setTrailerScraperHdTrailers(boolean newValue) {
    boolean oldValue = this.trailerScraperHdTrailers;
    this.trailerScraperHdTrailers = newValue;
    firePropertyChange(TRAILER_SCRAPER_HD_TRAILERS, oldValue, newValue);
  }

  /**
   * Checks if is trailer scraper ofdb.
   * 
   * @return true, if is trailer scraper ofdb
   */
  public boolean isTrailerScraperOfdb() {
    return trailerScraperOfdb;
  }

  /**
   * Sets the trailer scraper ofdb.
   * 
   * @param newValue
   *          the new trailer scraper ofdb
   */
  public void setTrailerScraperOfdb(boolean newValue) {
    boolean oldValue = this.trailerScraperOfdb;
    this.trailerScraperOfdb = newValue;
    firePropertyChange(TRAILER_SCRAPER_OFDB, oldValue, newValue);
  }

  /**
   * Checks if is write actor images.
   * 
   * @return true, if is write actor images
   */
  public boolean isWriteActorImages() {
    return writeActorImages;
  }

  /**
   * Sets the write actor images.
   * 
   * @param newValue
   *          the new write actor images
   */
  public void setWriteActorImages(boolean newValue) {
    boolean oldValue = this.writeActorImages;
    this.writeActorImages = newValue;
    firePropertyChange(WRITE_ACTOR_IMAGES, oldValue, newValue);
  }
}
