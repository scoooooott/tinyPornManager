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
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.TMDB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaFileComparator;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSetArtworkHelper;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class MovieSet. This class is used to represent a movie set (which means a "collection" of n movies)
 * 
 * @author Manuel Laggner
 */
public class MovieSet extends MediaEntity {
  private static final Comparator<Movie>     MOVIE_SET_COMPARATOR  = new MovieInMovieSetComparator();
  private static final Comparator<MediaFile> MEDIA_FILE_COMPARATOR = new MovieMediaFileComparator();

  @JsonProperty
  private List<UUID>                         movieIds              = new ArrayList<>(0);

  private List<Movie>                        movies                = new CopyOnWriteArrayList<>();
  private String                             titleSortable         = "";

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

    // search for artwork
    MovieSetArtworkHelper.updateArtwork(this);
  }

  @Override
  protected Comparator<MediaFile> getMediaFileComparator() {
    return MEDIA_FILE_COMPARATOR;
  }

  @Override
  public void initializeAfterLoading() {
    super.initializeAfterLoading();

    // link with movies
    for (UUID uuid : movieIds) {
      Movie movie = MovieList.getInstance().lookupMovie(uuid);
      if (movie != null) {
        movies.add(movie);
      }
    }
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
      id = (Integer) ids.get(TMDB);
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  public void setTmdbId(int newValue) {
    int oldValue = getTmdbId();
    ids.put(TMDB, newValue);
    firePropertyChange(TMDB, oldValue, newValue);
  }

  @Override
  public void setArtworkUrl(String url, MediaFileType type) {
    super.setArtworkUrl(url, type);
    MovieSetArtworkHelper.downloadArtwork(this, type);
  }

  /**
   * Sets the artwork.
   *
   * @param artwork
   *          the artwork
   * @param config
   *          the config
   */
  public void setArtwork(List<MediaArtwork> artwork, MovieScraperMetadataConfig config) {
    if (config.isArtwork()) {
      MovieSetArtworkHelper.setArtwork(this, artwork);
    }
  }

  @Override
  public String getArtworkFilename(final MediaFileType type) {
    String artworkFilename = super.getArtworkFilename(type);

    // we did not find an image - get the cached file from the url
    if (StringUtils.isBlank(artworkFilename)) {
      final String artworkUrl = getArtworkUrl(type);
      if (StringUtils.isNotBlank(artworkUrl)) {
        Path artworkFile = ImageCache.getCacheDir().resolve(ImageCache.getMD5(artworkUrl));
        if (Files.exists(artworkFile)) {
          artworkFilename = artworkFile.toAbsolutePath().toString();
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
      movieIds.add(movie.getDbId());

      // update artwork
      MovieSetArtworkHelper.updateArtwork(this);

      saveToDb();
    }

    // write images
    List<Movie> movies = new ArrayList<>(1);
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
   *          the movie to insert into the movie set
   */
  public void insertMovie(Movie movie) {
    synchronized (movies) {
      if (movies.contains(movie)) {
        return;
      }

      int index = Collections.binarySearch(movies, movie, MOVIE_SET_COMPARATOR);
      if (index < 0) {
        movies.add(-index - 1, movie);
        movieIds.add(-index - 1, movie.getDbId());
      }
      else if (index >= 0) {
        movies.add(index, movie);
        movieIds.add(index, movie.getDbId());
      }

      // update artwork
      MovieSetArtworkHelper.updateArtwork(this);

      saveToDb();
    }

    // write images
    List<Movie> movies = new ArrayList<>(1);
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
   * @param doCleanup
   *          do an artwork cleanup or not
   */
  public void removeMovie(Movie movie, boolean doCleanup) {
    // clean images files
    if (doCleanup) {
      MovieSetArtworkHelper.cleanMovieSetArtworkInMovieFolder(movie);
    }

    if (movie.getMovieSet() != null) {
      movie.setMovieSet(null);
      movie.saveToDb();
    }

    synchronized (movies) {
      movies.remove(movie);
      movieIds.remove(movie.getDbId());

      // update artwork
      if (doCleanup) {
        MovieSetArtworkHelper.updateArtwork(this);
      }

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
      Utils.sortList(movies, MOVIE_SET_COMPARATOR);
      // rebuild the ID table the same way
      movieIds.clear();
      for (Movie movie : movies) {
        movieIds.add(movie.getDbId());
      }
    }
    firePropertyChange("movies", null, movies);
  }

  /**
   * Removes the all movies from this movie set.
   */
  public void removeAllMovies() {
    // store all old movies to remove the nodes in the tree
    List<Movie> oldValue = new ArrayList<>(movies);
    // remove images from movie folder
    synchronized (movies) {
      for (Movie movie : movies) {
        // clean images files
        MovieSetArtworkHelper.cleanMovieSetArtworkInMovieFolder(movie);

        if (movie.getMovieSet() != null) {
          movie.setMovieSet(null);
          movie.writeNFO();
          movie.saveToDb();
        }
      }
      movies.clear();
      movieIds.clear();

      // update artwork
      MovieSetArtworkHelper.updateArtwork(this);

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
        MediaFileType.CLEARLOGO, MediaFileType.CLEARART);

    for (MediaFileType type : types) {
      MovieSetArtworkHelper.downloadArtwork(this, type);
    }
  }

  /**
   * Gets the check mark for images.<br>
   * Assumes true, but when PosterFilename is set and we do not have a poster, return false<br>
   * same for fanarts.
   * 
   * @return the checks for images
   */
  public Boolean getHasImages() {
    if (!MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().isEmpty() && StringUtils.isEmpty(getArtworkFilename(MediaFileType.POSTER))) {
      return false;
    }

    if (!MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames().isEmpty() && StringUtils.isEmpty(getArtworkFilename(MediaFileType.FANART))) {
      return false;
    }

    return true;
  }

  public List<Path> getImagesToCache() {
    // get files to cache
    List<Path> filesToCache = new ArrayList<>();

    if (StringUtils.isNotBlank(getArtworkFilename(MediaFileType.POSTER))) {
      filesToCache.add(Paths.get(getArtworkFilename(MediaFileType.POSTER)));
    }

    if (StringUtils.isNotBlank(getArtworkFilename(MediaFileType.FANART))) {
      filesToCache.add(Paths.get(getArtworkFilename(MediaFileType.FANART)));
    }

    return filesToCache;
  }

  @Override
  public synchronized void callbackForWrittenArtwork(MediaArtworkType type) {
  }

  @Override
  public void saveToDb() {
    MovieList.getInstance().persistMovieSet(this);
  }

  @Override
  public void deleteFromDb() {
    MovieList.getInstance().removeMovieSetFromDb(this);
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
        movieIds.remove(movie.getDbId());
        dirty = true;
      }
    }

    if (dirty) {
      saveToDb();
    }
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
