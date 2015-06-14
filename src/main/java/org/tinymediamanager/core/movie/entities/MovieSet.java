/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import org.tinymediamanager.core.movie.MovieSetArtworkHelper;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.tinymediamanager.core.Constants.TMDBID;

/**
 * The Class MovieSet. This class is used to represent a movie set (which means a "collection" of n movies)
 * 
 * @author Manuel Laggner
 */
@Entity
public class MovieSet extends MediaEntity {
  private static final Logger            LOGGER               = LoggerFactory.getLogger(MovieSet.class);
  private static final Comparator<Movie> MOVIE_SET_COMPARATOR = new MovieInMovieSetComparator();

  @OneToMany(fetch = FetchType.EAGER)
  private List<Movie>                    movies               = new ArrayList<Movie>(0);

  @Transient
  private String                         titleSortable        = "";

  static {
    mediaFileComparator = new MovieMediaFileComparator();
  }

  /**
   * Instantiates a new movieset. To initialize the propertychangesupport after loading
   */
  public MovieSet() {
    // register for dirty flag listener
    super();
  }

  public MovieSet(String title) {
    this();
    setTitle(title);

    // search for artwork in the artwork folder
    MovieSetArtworkHelper.findArtworkInArtworkFolder(this);
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

  public int getTmdbId() {
    int id;
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
    MovieSetArtworkHelper.downloadArtwork(this, type);
  }

  @Override
  public String getArtworkFilename(final MediaFileType type) {
    String artworkFilename = super.getArtworkFilename(type);

    // we did not find an image - get the cached file from the url
    if (StringUtils.isBlank(artworkFilename)) {
      final String artworkUrl = getArtworkUrl(type);
      if (StringUtils.isNotBlank(artworkUrl)) {
        File artworkFile = new File(ImageCache.getCacheDir(), ImageCache.getCachedFileName(artworkUrl));
        if (artworkFile.exists()) {
          artworkFilename = artworkFile.getPath();
        }
      }
    }

    return artworkFilename;
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
    if (MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkMovieFolder()) {
      MovieSetArtworkHelper.writeImagesToMovieFolder(this, movies);
    }

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
    if (MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkMovieFolder()) {
      MovieSetArtworkHelper.writeImagesToMovieFolder(this, movies);
    }

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

  public void rewriteAllImages() {
    List<MediaFileType> types = Arrays.asList(MediaFileType.POSTER, MediaFileType.FANART, MediaFileType.BANNER, MediaFileType.LOGO,
        MediaFileType.CLEARART);

    for (MediaFileType type : types) {
      MovieSetArtworkHelper.downloadArtwork(this, type);
    }
  }

  public Boolean getHasImages() {
    if (!StringUtils.isEmpty(getArtworkFilename(MediaFileType.POSTER)) && !StringUtils.isEmpty(getArtworkFilename(MediaFileType.FANART))) {
      return true;
    }
    return false;
  }

  public List<File> getImagesToCache() {
    // get files to cache
    List<File> filesToCache = new ArrayList<File>();

    if (StringUtils.isNotBlank(getArtworkFilename(MediaFileType.POSTER))) {
      filesToCache.add(new File(getArtworkFilename(MediaFileType.POSTER)));
    }

    if (StringUtils.isNotBlank(getArtworkFilename(MediaFileType.FANART))) {
      filesToCache.add(new File(getArtworkFilename(MediaFileType.FANART)));
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
    for (Movie movie : new ArrayList<>(movies)) {
      movie.setSortTitleFromMovieSet();
      movie.saveToDb();
    }
  }

  /**
   * clean movies from this movieset if there are any inconsistances
   */
  public void cleanMovieSet() {
    MovieList movieList = MovieList.getInstance();
    boolean dirty = false;

    for (Movie movie : new ArrayList<>(movies)) {
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
  private static class MovieInMovieSetComparator implements Comparator<Movie> {
    @Override
    public int compare(Movie o1, Movie o2) {
      if (o1 == null || o2 == null) {
        return 0;
      }

      // sort with sorttitle if available
      if (StringUtils.isNotBlank(o1.getSortTitle()) && StringUtils.isNotBlank(o2.getSortTitle())) {
        Collator collator = Collator.getInstance();
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
        catch (Exception ignored) {
        }
      }

      // fallback
      return 0;
    }
  }
}
