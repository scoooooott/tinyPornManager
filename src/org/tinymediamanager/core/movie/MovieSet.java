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

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * The Class MovieSet.
 * 
 * @author Manuel Laggner
 */
@Entity
public class MovieSet extends MediaEntity {

  private static final Logger            LOGGER               = LoggerFactory.getLogger(MovieSet.class);
  private static final Comparator<Movie> MOVIE_SET_COMPARATOR = new MovieInMovieSetComparator();

  private List<Movie>                    movies               = new ArrayList<Movie>();
  @Transient
  private List<Movie>                    moviesObservable     = ObservableCollections.observableList(movies);
  @Transient
  private String                         titleSortable        = "";

  /**
   * Instantiates a new movieset. To initialize the propertychangesupport after loading
   */
  public MovieSet() {
  }

  @Override
  public void setTitle(String newValue) {
    super.setTitle(newValue);

    for (Movie movie : moviesObservable) {
      movie.movieSetTitleChanged();
    }
  }

  /**
   * Returns the sortable variant of title<br>
   * eg "The Terminator Collection" -> "Terminator Collection, The".
   * 
   * @return the title in its sortable format
   */
  public String getTitleSortable() {
    if (StringUtils.isEmpty(titleSortable)) {
      titleSortable = Utils.getSortableName(this.getTitle());
    }
    return titleSortable;
  }

  /**
   * Instantiates a new movie set.
   * 
   * @param title
   *          the title
   */
  public MovieSet(String title) {
    setTitle(title);
  }

  /**
   * Sets the observable cast list.
   */
  public void setObservables() {
    moviesObservable = ObservableCollections.observableList(movies);
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    int id = 0;
    try {
      id = (Integer) ids.get("tmdbId");
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param newValue
   *          the new tmdb id
   */
  public void setTmdbId(int newValue) {
    int oldValue = getTmdbId();
    ids.put("tmdbId", newValue);
    firePropertyChange(TMDBID, oldValue, newValue);
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  @Override
  public void setPosterUrl(String newValue) {
    super.setPosterUrl(newValue);
    boolean written = false;
    String posterFilename = "movieset-poster.jpg";

    // write new poster
    writeImageToMovieFolder(moviesObservable, posterFilename, posterUrl);
    if (moviesObservable.size() > 0) {
      written = true;
    }

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(true, false);
      written = true;
    }

    if (written) {
      firePropertyChange(POSTER, false, true);
    }
    else {
      // at least cache it
      if (StringUtils.isNotEmpty(posterUrl) && moviesObservable.size() == 0) {
        ImageFetcher task = new ImageFetcher("poster", posterUrl);
        Globals.executor.execute(task);
      }
    }

  }

  /**
   * Sets the fanart url.
   * 
   * @param newValue
   *          the new fanart url
   */
  @Override
  public void setFanartUrl(String newValue) {
    super.setFanartUrl(newValue);
    boolean written = false;
    String fanartFilename = "movieset-fanart.jpg";

    // write new fanart
    writeImageToMovieFolder(moviesObservable, fanartFilename, fanartUrl);
    if (moviesObservable.size() > 0) {
      written = true;
    }

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(false, true);
      written = true;
    }

    if (written) {
      firePropertyChange(FANART, false, true);
    }
    else {
      // at least cache it
      if (StringUtils.isNotEmpty(fanartUrl) && moviesObservable.size() == 0) {
        ImageFetcher task = new ImageFetcher("fanart", fanartUrl);
        Globals.executor.execute(task);
      }
    }
  }

  /**
   * Gets the fanart.
   * 
   * @return the fanart
   */
  @Override
  public String getFanart() {
    String fanart = "";

    // try to get from the artwork folder if enabled
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()) {
      String filename = Globals.settings.getMovieSettings().getMovieSetArtworkFolder() + File.separator + getTitle() + "-fanart.jpg";
      File fanartFile = new File(filename);
      if (fanartFile.exists()) {
        return filename;
      }
    }

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

    return fanart;
  }

  /**
   * Gets the poster.
   * 
   * @return the poster
   */
  @Override
  public String getPoster() {
    String poster = "";

    // try to get from the artwork folder if enabled
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()) {
      String filename = Globals.settings.getMovieSettings().getMovieSetArtworkFolder() + File.separator + getTitle() + "-poster.jpg";
      File fanartFile = new File(filename);
      if (fanartFile.exists()) {
        return filename;
      }
    }

    // try to get a fanart from one movie
    List<Movie> movies = new ArrayList<Movie>(moviesObservable);
    for (Movie movie : movies) {
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

    return poster;
  }

  /**
   * Adds the movie to the end of the list
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    if (moviesObservable.contains(movie)) {
      return;
    }
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

    firePropertyChange("addedMovie", null, movie);
    firePropertyChange("movies", null, moviesObservable);
  }

  /**
   * Inserts the movie into the right position of the list
   * 
   * @param movie
   */
  public void insertMovie(Movie movie) {
    if (moviesObservable.contains(movie)) {
      return;
    }

    int index = Collections.binarySearch(moviesObservable, movie, MOVIE_SET_COMPARATOR);
    if (index < 0) {
      moviesObservable.add(-index - 1, movie);
    }
    else if (index >= 0) {
      moviesObservable.add(index, movie);
    }

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

    firePropertyChange("addedMovie", null, movie);
    firePropertyChange("movies", null, moviesObservable);
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
    if (movie.getMovieSet() != null) {
      movie.setMovieSet(null);
      movie.saveToDb();
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
    Collections.sort(moviesObservable, MOVIE_SET_COMPARATOR);
    firePropertyChange("movies", null, moviesObservable);
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

      if (movie.getMovieSet() != null) {
        movie.setMovieSet(null);
        movie.saveToDb();
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
    synchronized (Globals.entityManager) {
      Globals.entityManager.getTransaction().begin();
      Globals.entityManager.persist(this);
      Globals.entityManager.getTransaction().commit();
    }
  }

  /**
   * toString. used for JComboBox in movie editor
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return getTitle();
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
    for (Movie movie : movies) {
      try {
        writeImage(url, movie.getPath() + File.separator + filename);
      }
      catch (IOException e) {
        LOGGER.warn("could not write files", e);
      }
    }
  }

  /**
   * Rewrite all images.
   */
  public void rewriteAllImages() {
    writeImageToMovieFolder(moviesObservable, "movieset-fanart.jpg", fanartUrl);
    writeImageToMovieFolder(moviesObservable, "movieset-poster.jpg", posterUrl);

    // write to artwork folder
    if (Globals.settings.getMovieSettings().isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(Globals.settings.getMovieSettings().getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(true, true);
    }
  }

  /**
   * Write images to artwork folder.
   * 
   * @param poster
   *          the poster
   * @param fanart
   *          the fanart
   */
  private void writeImagesToArtworkFolder(boolean poster, boolean fanart) {
    // write images to artwork folder
    File artworkFolder = new File(Globals.settings.getMovieSettings().getMovieSetArtworkFolder());

    // check if folder exists
    if (!artworkFolder.exists()) {
      artworkFolder.mkdirs();
    }

    // write files
    try {
      // poster
      if (poster && StringUtils.isNotBlank(posterUrl)) {
        String providedFiletype = FilenameUtils.getExtension(posterUrl);
        writeImage(posterUrl, artworkFolder.getPath() + File.separator + getTitle() + "-folder." + providedFiletype);
      }
    }
    catch (IOException e) {
      LOGGER.warn("could not write files", e);
    }

    try {
      // fanart
      if (fanart && StringUtils.isNotBlank(fanartUrl)) {
        String providedFiletype = FilenameUtils.getExtension(fanartUrl);
        writeImage(fanartUrl, artworkFolder.getPath() + File.separator + getTitle() + "-fanart." + providedFiletype);
      }
    }
    catch (IOException e) {
      LOGGER.warn("could not write files", e);
    }
  }

  /**
   * Write image.
   * 
   * @param url
   *          the url
   * @param pathAndFilename
   *          the path and filename
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void writeImage(String url, String pathAndFilename) throws IOException {
    CachedUrl cachedUrl = new CachedUrl(url);
    FileOutputStream outputStream = new FileOutputStream(pathAndFilename);
    InputStream is = cachedUrl.getInputStream();
    IOUtils.copy(is, outputStream);
    outputStream.close();
    is.close();

    ImageCache.invalidateCachedImage(pathAndFilename);
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
   * 
   * @author Manuel Laggner
   */
  private class ImageFetcher implements Runnable {

    /** The property name. */
    private String propertyName = "";

    /** The image url. */
    private String imageUrl     = "";

    /**
     * Instantiates a new image fetcher.
     * 
     * @param propertyName
     *          the property name
     * @param url
     *          the url
     */
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

        firePropertyChange(propertyName, "", outputFile);
      }
      catch (IOException e) {
        LOGGER.warn("error in image fetcher", e);
      }
    }
  }

  /**
   * The Class MovieInMovieSetComparator.
   * 
   * @author Manuel Laggner
   */
  private static class MovieInMovieSetComparator implements Comparator<Movie> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Movie o1, Movie o2) {
      Collator collator = null;

      if (o1 == null || o2 == null) {
        return 0;
      }

      // sort with sorttitle if available
      if (StringUtils.isNotBlank(o1.getSortTitle()) && StringUtils.isNotBlank(o2.getSortTitle())) {
        collator = Collator.getInstance();
        return collator.compare(o1.getSortTitle(), o2.getSortTitle());
      }

      // sort with release date if available
      if (o1.getReleaseDate() != null && o2.getReleaseDate() != null) {
        return o1.getReleaseDate().compareTo(o2.getReleaseDate());
      }

      // sort with year if available
      if (StringUtils.isNotBlank(o1.getYear()) && StringUtils.isNotBlank(o2.getYear())) {
        try {
          int year1 = Integer.parseInt(o1.getYear());
          int year2 = Integer.parseInt(o2.getYear());
          return year1 - year2;
        }
        catch (Exception e) {
        }
      }

      // fallback
      return 0;
    }

  }

  /**
   * Gets the checks for images.
   * 
   * @return the checks for images
   */
  public Boolean getHasImages() {
    if (!StringUtils.isEmpty(getPoster()) && !StringUtils.isEmpty(getFanart())) {
      return true;
    }
    return false;
  }

  /**
   * Gets the images to cache.
   * 
   * @return the images to cache
   */
  public List<File> getImagesToCache() {
    // get files to cache
    List<File> filesToCache = new ArrayList<File>();

    if (StringUtils.isNotBlank(getPoster())) {
      filesToCache.add(new File(getPoster()));
    }

    if (StringUtils.isNotBlank(getFanart())) {
      filesToCache.add(new File(getFanart()));
    }

    return filesToCache;
  }

  @Override
  public synchronized void callbackForWrittenArtwork(MediaArtworkType type) {
  }

  /**
   * recalculate all movie sorttitles
   */
  public void updateMovieSorttitle() {
    for (Movie movie : new ArrayList<Movie>(moviesObservable)) {
      movie.setSortTitleFromMovieSet();
      movie.saveToDb();
      movie.writeNFO();
    }
  }
}
