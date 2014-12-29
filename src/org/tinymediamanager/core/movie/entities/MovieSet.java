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
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaFileComparator;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class MovieSet. This class is used to represent a movie set (which means a "collection" of n movies)
 * 
 * @author Manuel Laggner
 */
@Entity
public class MovieSet extends MediaEntity {
  private static final Logger            LOGGER                      = LoggerFactory.getLogger(MovieSet.class);
  private static final Comparator<Movie> MOVIE_SET_COMPARATOR        = new MovieInMovieSetComparator();
  private static final String[]          SUPPORTED_ARTWORK_FILETYPES = { "jpg", "png", "tbn" };

  @OneToMany(fetch = FetchType.EAGER)
  private List<Movie>                    movies                      = new ArrayList<Movie>(0);

  @Transient
  private String                         titleSortable               = "";

  static {
    mediaFileComparator = new MovieMediaFileComparator();
  }

  /**
   * Instantiates a new movieset. To initialize the propertychangesupport after loading
   */
  public MovieSet() {
  }

  @Override
  public void setTitle(String newValue) {
    super.setTitle(newValue);

    synchronized (movies) {
      for (Movie movie : movies) {
        movie.movieSetTitleChanged();
      }
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

  public MovieSet(String title) {
    setTitle(title);
  }

  public int getTmdbId() {
    int id = 0;
    try {
      id = (Integer) ids.get(TMDBID);
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  public void setTmdbId(int newValue) {
    int oldValue = getTmdbId();
    ids.put(TMDBID, newValue);
    firePropertyChange(TMDBID, oldValue, newValue);
  }

  @Override
  public void setArtworkUrl(String url, MediaFileType type) {
    super.setArtworkUrl(url, type);
    boolean written = false;

    // write new artwork
    writeImageToMovieFolder(movies, type);
    if (movies.size() > 0) {
      written = true;
    }

    // write to artwork folder
    if (MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(MovieModuleManager.MOVIE_SETTINGS.getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(type);
      written = true;
    }

    if (written) {
      firePropertyChange(type.name().toLowerCase(), false, true);
    }
    else {
      // at least cache it
      if (StringUtils.isNotEmpty(url) && movies.size() == 0) {
        ImageFetcher task = new ImageFetcher(type.name().toLowerCase(), url);
        TmmTaskManager.getInstance().addImageDownloadTask(task);
      }
    }
  }

  @Override
  @Deprecated
  public String getFanart() {
    return getArtworkFilename(MediaFileType.FANART);
  }

  @Override
  @Deprecated
  public String getPoster() {
    return getArtworkFilename(MediaFileType.POSTER);
  }

  @Override
  public String getArtworkFilename(final MediaFileType type) {
    // try to get from the artwork folder if enabled
    if (MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkFolder()) {
      File artworkDir = new File(MovieModuleManager.MOVIE_SETTINGS.getMovieSetArtworkFolder());

      // performance trick: look if the desired file is in the image cache
      for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
        String artworkFileName = artworkDir.getAbsolutePath() + File.separator + MovieRenamer.replaceInvalidCharacters(getTitle()) + "-"
            + type.name().toLowerCase() + "." + fileType;
        if (ImageCache.isImageCached(artworkFileName)) {
          return artworkFileName;
        }
      }

      // search the folder for the image
      if (artworkDir.exists()) {
        // File.listFiles is really slow on some bigger folders; stick to the old fashioned search
        for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
          String artworkFileName = MovieRenamer.replaceInvalidCharacters(getTitle()) + "-" + type.name().toLowerCase() + "." + fileType;
          File artworkFile = new File(artworkDir, artworkFileName);
          if (artworkFile.exists()) {
            return artworkFile.getPath();
          }
          else if (type == MediaFileType.POSTER) {
            // for posters there is also -folder possible
            artworkFileName = MovieRenamer.replaceInvalidCharacters(getTitle()) + "-folder." + fileType;
            artworkFile = new File(artworkDir, artworkFileName);
            if (artworkFile.exists()) {
              return artworkFile.getPath();
            }
          }
        }
      }
    }

    // try to get the artwork from one movie
    List<Movie> movies = new ArrayList<Movie>(this.movies);
    for (Movie movie : movies) {
      File movieDir = new File(movie.getPath());

      // performance trick: look if the desired file is in the image cache
      for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
        String artworkFileName = movieDir.getAbsolutePath() + File.separator + "movieset-" + type.name().toLowerCase() + "." + fileType;
        if (ImageCache.isImageCached(artworkFileName)) {
          return artworkFileName;
        }
      }

      // search the movie dirs
      if (movieDir.exists()) {
        // File.listFiles is really slow on some bigger folders; stick to the old fashioned search
        for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
          String artworkFileName = "movieset-" + type.name().toLowerCase() + "." + fileType;
          File artworkFile = new File(movieDir, artworkFileName);
          if (artworkFile.exists()) {
            return artworkFile.getPath();
          }
          else if (type == MediaFileType.POSTER) {
            // for posters there is also -folder possible
            artworkFileName = "movieset-folder." + fileType;
            artworkFile = new File(movieDir, artworkFileName);
            if (artworkFile.exists()) {
              return artworkFile.getPath();
            }
          }
        }
      }
    }

    // we did not find an image from any assigned movie - get the cached file from the url
    final String url = getArtworkUrl(type);
    if (StringUtils.isNotBlank(url)) {
      File cacheDir = ImageCache.getCacheDir();
      File artworkFile = new File(cacheDir, ImageCache.getCachedFileName(url));
      if (artworkFile.exists()) {
        return artworkFile.getPath();
      }
    }

    return "";
  }

  /**
   * Adds the movie to the end of the list
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    synchronized (movies) {
      if (movies.contains(movie)) {
        return;
      }
      movies.add(movie);
      saveToDb();
    }

    // write images
    List<Movie> movies = new ArrayList<Movie>(1);
    movies.add(movie);
    writeImagesToMovieFolder(movies);

    firePropertyChange("addedMovie", null, movie);
    firePropertyChange("movies", null, movies);
  }

  /**
   * Inserts the movie into the right position of the list
   * 
   * @param movie
   */
  public void insertMovie(Movie movie) {
    synchronized (movies) {
      if (movies.contains(movie)) {
        return;
      }

      int index = Collections.binarySearch(movies, movie, MOVIE_SET_COMPARATOR);
      if (index < 0) {
        movies.add(-index - 1, movie);
      }
      else if (index >= 0) {
        movies.add(index, movie);
      }

      saveToDb();
    }

    // write images
    List<Movie> movies = new ArrayList<Movie>(1);
    movies.add(movie);
    writeImagesToMovieFolder(movies);

    firePropertyChange("addedMovie", null, movie);
    firePropertyChange("movies", null, movies);
  }

  /**
   * Removes the movie from the list.
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

    synchronized (movies) {
      movies.remove(movie);
      saveToDb();
    }

    firePropertyChange("movies", null, movies);
    firePropertyChange("removedMovie", null, movie);
  }

  public List<Movie> getMovies() {
    return movies;
  }

  /**
   * Sort movies inside this movie set by using either the sort title, release date or year.
   */
  public void sortMovies() {
    synchronized (movies) {
      Collections.sort(movies, MOVIE_SET_COMPARATOR);
    }
    firePropertyChange("movies", null, movies);
  }

  /**
   * Removes the all movies from this movie set.
   */
  public void removeAllMovies() {
    // store all old movies to remove the nodes in the tree
    List<Movie> oldValue = new ArrayList<Movie>(movies);
    // remove images from movie folder
    synchronized (movies) {
      for (Movie movie : movies) {
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
      movies.clear();
      saveToDb();
    }

    firePropertyChange("movies", null, movies);
    firePropertyChange("removedAllMovies", oldValue, movies);
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

  public int getMovieIndex(Movie movie) {
    return movies.indexOf(movie);
  }

  private void writeImageToMovieFolder(List<Movie> movies, MediaFileType type) {
    String url = getArtworkUrl(type);
    String filename = "movieset-" + type.name().toLowerCase() + ".jpg";

    // check for empty strings or movies
    if (movies == null || movies.size() == 0 || StringUtils.isEmpty(filename) || StringUtils.isEmpty(url)) {
      return;
    }

    // write image for all movies
    for (Movie movie : movies) {
      try {
        if (!movie.isMultiMovieDir()) {
          writeImage(url, movie.getPath() + File.separator + filename);
        }
      }
      catch (InterruptedException e) {
        LOGGER.warn("interrupted image download");
      }
      catch (IOException e) {
        LOGGER.warn("could not write files", e);
      }
    }
  }

  public void rewriteAllImages() {
    writeImagesToMovieFolder(movies);

    // write to artwork folder
    if (MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkFolder()
        && StringUtils.isNotBlank(MovieModuleManager.MOVIE_SETTINGS.getMovieSetArtworkFolder())) {
      writeImagesToArtworkFolder(MediaFileType.POSTER);
      writeImagesToArtworkFolder(MediaFileType.FANART);
      writeImagesToArtworkFolder(MediaFileType.LOGO);
      writeImagesToArtworkFolder(MediaFileType.CLEARART);
      writeImagesToArtworkFolder(MediaFileType.BANNER);
    }
  }

  private void writeImagesToMovieFolder(List<Movie> movies) {
    List<MediaFileType> types = Arrays.asList(MediaFileType.POSTER, MediaFileType.FANART, MediaFileType.BANNER, MediaFileType.LOGO,
        MediaFileType.CLEARART);

    for (MediaFileType type : types) {
      writeImageToMovieFolder(movies, type);
    }

    // writeImagesToMovieFolder(movies);
  }

  private void writeImagesToArtworkFolder(MediaFileType type) {
    // write images to artwork folder
    if (StringUtils.isBlank(MovieModuleManager.MOVIE_SETTINGS.getMovieSetArtworkFolder())) {
      return;
    }

    File artworkFolder = new File(MovieModuleManager.MOVIE_SETTINGS.getMovieSetArtworkFolder());

    // check if folder exists
    if (!artworkFolder.exists()) {
      artworkFolder.mkdirs();
    }

    // write files
    try {
      String url = getArtworkUrl(type);
      if (StringUtils.isNotBlank(url)) {
        String providedFiletype = FilenameUtils.getExtension(url);
        if (type == MediaFileType.POSTER) {
          // poster should be written als "folder"
          writeImage(url, artworkFolder.getPath() + File.separator + MovieRenamer.replaceInvalidCharacters(getTitle()) + "-folder."
              + providedFiletype);
        }
        else {
          writeImage(url, artworkFolder.getPath() + File.separator + MovieRenamer.replaceInvalidCharacters(getTitle()) + "-"
              + type.name().toLowerCase() + "." + providedFiletype);
        }
      }
    }
    catch (InterruptedException e) {
      LOGGER.warn("interrupted image download");
    }
    catch (IOException e) {
      LOGGER.warn("could not write files", e);
    }
  }

  private void writeImage(String url, String pathAndFilename) throws IOException, InterruptedException {
    Url url1 = new Url(url);
    FileOutputStream outputStream = new FileOutputStream(pathAndFilename);
    InputStream is = url1.getInputStream();
    IOUtils.copy(is, outputStream);
    outputStream.flush();
    try {
      outputStream.getFD().sync(); // wait until file has been completely written
    }
    catch (Exception e) {
      // empty here -> just not let the thread crash
    }
    outputStream.close();
    is.close();

    ImageCache.invalidateCachedImage(pathAndFilename);
  }

  public Boolean getHasImages() {
    if (!StringUtils.isEmpty(getPoster()) && !StringUtils.isEmpty(getFanart())) {
      return true;
    }
    return false;
  }

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

  @Override
  public void saveToDb() {
    // update/insert this movie set to the database
    final EntityManager entityManager = getEntityManager();
    readWriteLock.readLock().lock();
    synchronized (entityManager) {
      if (!entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().begin();
        entityManager.persist(this);
        entityManager.getTransaction().commit();
      }
      else {
        entityManager.persist(this);
      }
    }
    readWriteLock.readLock().unlock();
  }

  @Override
  public void deleteFromDb() {
    // delete this movie set from the database
    final EntityManager entityManager = getEntityManager();
    synchronized (entityManager) {
      if (!entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().begin();
        entityManager.remove(this);
        entityManager.getTransaction().commit();
      }
      else {
        entityManager.remove(this);
      }
    }
  }

  /**
   * recalculate all movie sorttitles
   */
  public void updateMovieSorttitle() {
    for (Movie movie : new ArrayList<Movie>(movies)) {
      movie.setSortTitleFromMovieSet();
      movie.saveToDb();
      movie.writeNFO();
    }
  }

  /**
   * clean movies from this movieset if there are any inconsistances
   */
  public void cleanMovieSet() {
    MovieList movieList = MovieList.getInstance();
    boolean dirty = false;

    for (Movie movie : new ArrayList<Movie>(movies)) {
      if (!movieList.getMovies().contains(movie)) {
        movies.remove(movie);
        dirty = true;
      }
    }

    if (dirty) {
      saveToDb();
    }
  }

  @Override
  protected EntityManager getEntityManager() {
    return MovieModuleManager.getInstance().getEntityManager();
  }

  /*******************************************************************************
   * helper classses
   *******************************************************************************/
  private class ImageFetcher implements Runnable {
    private String propertyName = "";
    private String imageUrl     = "";

    public ImageFetcher(String propertyName, String url) {
      this.propertyName = propertyName;
      this.imageUrl = url;
    }

    @Override
    public void run() {
      String filename = ImageCache.getCachedFileName(imageUrl);
      File outputFile = new File(ImageCache.getCacheDir(), filename + ".jpg");

      try {
        Url url = new Url(imageUrl);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        InputStream is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync(); // wait until file has been completely
                                       // written
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        outputStream.close();
        is.close();

        firePropertyChange(propertyName, "", outputFile);
      }
      catch (InterruptedException e) {
        LOGGER.warn("interrupted image download");
      }
      catch (IOException e) {
        LOGGER.warn("error in image fetcher", e);
      }
    }
  }

  private static class MovieInMovieSetComparator implements Comparator<Movie> {
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
}
