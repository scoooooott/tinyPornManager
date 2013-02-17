/*
 * Copyright 2013 Manuel Laggner
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * The Class MovieSet.
 */
@Entity
public class MovieSet extends AbstractModelObject {
  /** The static LOGGER. */
  private static final Logger LOGGER           = Logger.getLogger(MovieSet.class);

  /** The name. */
  private String              name             = "";

  /** The overview. */
  private String              overview         = "";

  /** The poster url. */
  private String              posterUrl        = "";

  /** The fanart url. */
  private String              fanartUrl        = "";

  /** The tmdb id. */
  private int                 tmdbId           = 0;

  /** The movies. */
  private List<Movie>         movies           = new ArrayList<Movie>();

  /** The movies observable. */
  @Transient
  private List<Movie>         moviesObservable = ObservableCollections.observableList(movies);

  /**
   * Instantiates a new movie set. Needed for JAXB
   */
  public MovieSet() {
  }

  /**
   * Instantiates a new movie set.
   * 
   * @param name
   *          the new value
   */
  public MovieSet(String name) {
    String oldValue = this.name;
    this.name = name;
    firePropertyChange("name", oldValue, name);
  }

  /**
   * Sets the observable cast list.
   */
  public void setObservables() {
    moviesObservable = ObservableCollections.observableList(movies);
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param newValue
   *          the new tmdb id
   */
  public void setTmdbId(int newValue) {
    int oldValue = this.tmdbId;
    this.tmdbId = newValue;
    firePropertyChange("tmdbId", oldValue, newValue);
  }

  /**
   * Gets the overview.
   * 
   * @return the overview
   */
  public String getOverview() {
    return overview;
  }

  /**
   * Sets the overview.
   * 
   * @param newValue
   *          the new overview
   */
  public void setOverview(String newValue) {
    String oldValue = this.overview;
    this.overview = newValue;
    firePropertyChange("overview", oldValue, newValue);
  }

  /**
   * Gets the poster url.
   * 
   * @return the posterUrl
   */
  public String getPosterUrl() {
    return posterUrl;
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  public void setPosterUrl(String newValue) {
    String oldValue = this.posterUrl;
    this.posterUrl = newValue;

    // write new poster
    writeImageToMovieFolder(moviesObservable, "movieset-poster.jpg", fanartUrl);

    firePropertyChange("posterUrl", oldValue, newValue);
    firePropertyChange("poster", oldValue, newValue);
  }

  /**
   * Gets the fanart url.
   * 
   * @return the fanart url
   */
  public String getFanartUrl() {
    return fanartUrl;
  }

  /**
   * Sets the fanart url.
   * 
   * @param newValue
   *          the new fanart url
   */
  public void setFanartUrl(String newValue) {
    String oldValue = this.fanartUrl;
    this.fanartUrl = newValue;

    // write new fanart
    writeImageToMovieFolder(moviesObservable, "movieset-fanart.jpg", fanartUrl);

    firePropertyChange("fanartUrl", oldValue, newValue);
    firePropertyChange("fanart", oldValue, newValue);
  }

  public String getFanart() {
    String fanart = "";

    // try to get a fanart from one movie
    for (Movie movie : moviesObservable) {
      String filename = movie.getPath() + File.separator + "movieset-fanart.jpg";
      File fanartFile = new File(filename);
      if (fanartFile.exists()) {
        return filename;
      }
    }

    // we did not find an image from a movie - get the cached file from the url
    File cachedFile = new File(ImageCache.getCacheDir() + File.separator + ImageCache.getCachedFileName(fanartUrl) + ".jpg");
    if (cachedFile.exists()) {
      return cachedFile.getPath();
    }

    // no cached file found - cache it via thread
    if (StringUtils.isNotEmpty(fanartUrl)) {
      ImageFetcher task = new ImageFetcher("fanart", fanartUrl);
      Globals.executor.execute(task);
    }

    return fanart;
  }

  public String getPoster() {
    String poster = "";

    // try to get a fanart from one movie
    for (Movie movie : moviesObservable) {
      String filename = movie.getPath() + File.separator + "movieset-poster.jpg";
      File posterFile = new File(filename);
      if (posterFile.exists()) {
        return filename;
      }
    }

    // we did not find an image from a movie - get the cached file from the url
    File cachedFile = new File(ImageCache.getCacheDir() + File.separator + ImageCache.getCachedFileName(posterUrl) + ".jpg");
    if (cachedFile.exists()) {
      return cachedFile.getPath();
    }

    // no cached file found - cache it via thread
    if (StringUtils.isNotEmpty(posterUrl)) {
      ImageFetcher task = new ImageFetcher("poster", posterUrl);
      Globals.executor.execute(task);
    }

    return poster;
  }

  /**
   * Sets the name.
   * 
   * @param newValue
   *          the new name
   */
  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange("name", oldValue, newValue);
  }

  /**
   * Adds the movie.
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    moviesObservable.add(movie);
    saveToDb();

    // // look for an tmdbid if no one available
    // if (tmdbId == 0) {
    // searchTmdbId();
    // }

    // write images
    List<Movie> movies = new ArrayList<Movie>(1);
    movies.add(movie);
    writeImageToMovieFolder(movies, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(movies, "movieset-poster.jpg", posterUrl);

    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("addedMovie", null, movie);
  }

  /**
   * Removes the movie.
   * 
   * @param movie
   *          the movie
   */
  public void removeMovie(Movie movie) {
    // remove images from movie folder
    File imageFile = new File(movie.getPath() + File.separator + "movieset-fanart.jpg");
    if (imageFile.exists()) {
      imageFile.delete();
    }
    imageFile = new File(movie.getPath() + File.separator + "movieset-poster.jpg");
    if (imageFile.exists()) {
      imageFile.delete();
    }

    moviesObservable.remove(movie);
    saveToDb();

    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("removedMovie", null, movie);
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public List<Movie> getMovies() {
    return moviesObservable;
  }

  /**
   * Sort movies.
   */
  public void sortMovies() {
    Collections.sort(moviesObservable, new MovieInMovieSetComparator());
  }

  /**
   * Removes the all movies.
   */
  public void removeAllMovies() {
    // remove images from movie folder
    for (Movie movie : moviesObservable) {
      File imageFile = new File(movie.getPath() + File.separator + "movieset-fanart.jpg");
      if (imageFile.exists()) {
        imageFile.delete();
      }
      imageFile = new File(movie.getPath() + File.separator + "movieset-poster.jpg");
      if (imageFile.exists()) {
        imageFile.delete();
      }
    }

    // store all old movies to remove the nodes in the tree
    List<Movie> oldValue = new ArrayList<Movie>(moviesObservable.size());
    oldValue.addAll(moviesObservable);
    moviesObservable.clear();
    saveToDb();

    firePropertyChange("movies", null, moviesObservable);
    firePropertyChange("removedAllMovies", oldValue, moviesObservable);
  }

  /**
   * Save to db.
   */
  public synchronized void saveToDb() {
    // update DB
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.persist(this);
    Globals.entityManager.getTransaction().commit();
  }

  /**
   * toString. used for JComboBox in movie editor
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return this.name;
    // return ToStringBuilder.reflectionToString(this,
    // ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * Gets the movie index.
   * 
   * @param movie
   *          the movie
   * @return the movie index
   */
  public int getMovieIndex(Movie movie) {
    return movies.indexOf(movie);
  }

  /**
   * Write image to movie folder.
   * 
   * @param movies
   *          the movies
   * @param filename
   *          the filename
   * @param url
   *          the url
   */
  private void writeImageToMovieFolder(List<Movie> movies, String filename, String url) {
    // check for empty strings or movies
    if (movies == null || movies.size() == 0 || StringUtils.isEmpty(filename) || StringUtils.isEmpty(url)) {
      return;
    }

    // write image for all movies
    try {
      for (Movie movie : movies) {
        CachedUrl cachedUrl = new CachedUrl(url);
        FileOutputStream outputStream = new FileOutputStream(movie.getPath() + File.separator + filename);
        InputStream is = cachedUrl.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.close();
        is.close();
      }
    }
    catch (IOException e) {
      LOGGER.warn(e);
    }
  }

  /**
   * Rewrite all images.
   */
  public void rewriteAllImages() {
    writeImageToMovieFolder(moviesObservable, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(moviesObservable, "movieset-poster.jpg", posterUrl);
  }

  // /**
  // * Search tmdb id for this movieset.
  // */
  // public void searchTmdbId() {
  // try {
  // TmdbMetadataProvider tmdb = new TmdbMetadataProvider();
  // for (Movie movie : moviesObservable) {
  // MediaScrapeOptions options = new MediaScrapeOptions();
  // if (Utils.isValidImdbId(movie.getImdbId()) || movie.getTmdbId() > 0) {
  // options.setTmdbId(movie.getTmdbId());
  // options.setImdbId(movie.getImdbId());
  // MediaMetadata md = tmdb.getMetadata(options);
  // if (md.getTmdbIdSet() > 0) {
  // setTmdbId(md.getTmdbIdSet());
  // saveToDb();
  // break;
  // }
  // }
  // }
  // }
  // catch (Exception e) {
  // LOGGER.warn(e);
  // }
  // }

  /**
   * The Class ImageFetcher.
   */
  private class ImageFetcher implements Runnable {

    private String propertyName = "";

    private String imageUrl     = "";

    public ImageFetcher(String propertyName, String url) {
      this.propertyName = propertyName;
      this.imageUrl = url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      String filename = ImageCache.getCachedFileName(imageUrl);
      File outputFile = new File(ImageCache.getCacheDir(), filename + ".jpg");

      try {
        CachedUrl url = new CachedUrl(imageUrl);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        InputStream is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.close();
        is.close();
        firePropertyChange(propertyName, "", filename);
      }
      catch (IOException e) {
        LOGGER.warn(e);
      }
    }
  }

  private class MovieInMovieSetComparator implements Comparator<Movie> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Movie o1, Movie o2) {
      return o1.getSortTitle().compareTo(o2.getSortTitle());
    }

  }
}
