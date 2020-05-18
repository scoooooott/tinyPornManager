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
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.TITLE_FOR_UI;
import static org.tinymediamanager.core.Constants.TITLE_SORTABLE;
import static org.tinymediamanager.core.Constants.TMDB;
import static org.tinymediamanager.core.Constants.TMDB_SET;

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
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaFileComparator;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSetArtworkHelper;
import org.tinymediamanager.core.movie.MovieSetScraperMetadataConfig;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class MovieSet. This class is used to represent a movie set (which means a "collection" of n movies)
 * 
 * @author Manuel Laggner
 */
public class MovieSet extends MediaEntity {
  private static final Logger                LOGGER                = LoggerFactory.getLogger(MovieSet.class);
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
    String oldValue = this.title;
    super.setTitle(newValue);

    firePropertyChange(TITLE_FOR_UI, oldValue, newValue);

    String oldValueTitleSortable = this.titleSortable;
    titleSortable = "";
    firePropertyChange(TITLE_SORTABLE, oldValueTitleSortable, titleSortable);

    if (!StringUtils.equals(oldValue, newValue)) {
      // update artwork
      MovieSetArtworkHelper.cleanupArtwork(this);

      synchronized (movies) {
        for (Movie movie : movies) {
          movie.movieSetTitleChanged();
        }
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
      id = (Integer) ids.get(TMDB_SET);
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  public void setTmdbId(int newValue) {
    int oldValue = getTmdbId();
    ids.put(TMDB_SET, newValue);
    firePropertyChange(TMDB, oldValue, newValue);
  }

  @Override
  public void setArtworkUrl(String url, MediaFileType type) {
    super.setArtworkUrl(url, type);
    MovieSetArtworkHelper.downloadArtwork(this, type);
  }

  /**
   * <b>PHYSICALLY</b> deletes all {@link MediaFile}s of the given type
   *
   * @param type
   *          the {@link MediaFileType} for all {@link MediaFile}s to delete
   */
  public void deleteMediaFiles(MediaFileType type) {
    getMediaFiles(type).forEach(mediaFile -> {
      Utils.deleteFileSafely(mediaFile.getFile());
      removeFromMediaFiles(mediaFile);
    });
  }

  /**
   * Sets the artwork.
   *
   * @param artwork
   *          the artwork
   * @param config
   *          the config
   */
  public void setArtwork(List<MediaArtwork> artwork, List<MovieSetScraperMetadataConfig> config) {
    MovieSetArtworkHelper.setArtwork(this, artwork, config);
  }

  @Override
  public String getArtworkFilename(final MediaFileType type) {
    String artworkFilename = super.getArtworkFilename(type);

    // we did not find an image - get the cached file from the url
    if (StringUtils.isBlank(artworkFilename)) {
      Path cachedFile = ImageCache.getCachedFile(getArtworkUrl(type));
      if (cachedFile != null && cachedFile.toFile().exists()) {
        return cachedFile.toAbsolutePath().toString();
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
    if (MovieModuleManager.SETTINGS.isEnableMovieSetArtworkMovieFolder()) {
      MovieSetArtworkHelper.writeImagesToMovieFolder(this, Collections.singletonList(movie));
    }

    firePropertyChange(Constants.ADDED_MOVIE, null, movie);
    firePropertyChange("movies", null, movies);
    firePropertyChange(Constants.WATCHED, null, movies);
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
      else {
        movies.add(index, movie);
        movieIds.add(index, movie.getDbId());
      }

      // update artwork
      MovieSetArtworkHelper.updateArtwork(this);

      saveToDb();
    }

    // write images
    if (MovieModuleManager.SETTINGS.isEnableMovieSetArtworkMovieFolder()) {
      MovieSetArtworkHelper.writeImagesToMovieFolder(this, Collections.singletonList(movie));
    }

    firePropertyChange(Constants.ADDED_MOVIE, null, movie);
    firePropertyChange("movies", null, movies);
    firePropertyChange(Constants.WATCHED, null, movies);
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

    firePropertyChange(Constants.REMOVED_MOVIE, null, movie);
    firePropertyChange("movies", null, movies);
    firePropertyChange(Constants.WATCHED, null, movies);
  }

  public List<Movie> getMovies() {
    return movies;
  }

  /**
   * Sort movies inside this movie set by using either the sort title, release date or year.
   */
  public void sortMovies() {
    synchronized (movies) {
      movies.sort(MOVIE_SET_COMPARATOR);
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

    firePropertyChange("removedAllMovies", oldValue, movies);
    firePropertyChange("movies", null, movies);
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
   * Gets the check mark for images. What to be checked is configurable
   * 
   * @return the checks for images
   */
  public Boolean getHasImages() {
    for (MediaArtworkType type : MovieModuleManager.SETTINGS.getCheckImagesMovie()) {
      if (StringUtils.isEmpty(getArtworkFilename(MediaFileType.getMediaFileType(type)))) {
        return false;
      }
    }
    return true;
  }

  public Boolean getHasMetadata() {
    return StringUtils.isNotBlank(plot) && StringUtils.isNotBlank(title);
  }

  public Boolean isWatched() {
    for (Movie movie : movies) {
      if (!movie.isWatched()) {
        return false;
      }
    }
    return true;
  }

  public List<MediaFile> getImagesToCache() {
    // get files to cache
    List<MediaFile> filesToCache = new ArrayList<>();

    if (StringUtils.isNotBlank(getArtworkFilename(MediaFileType.POSTER))) {
      filesToCache.add(new MediaFile(Paths.get(getArtworkFilename(MediaFileType.POSTER))));
    }

    if (StringUtils.isNotBlank(getArtworkFilename(MediaFileType.FANART))) {
      filesToCache.add(new MediaFile(Paths.get(getArtworkFilename(MediaFileType.FANART))));
    }

    return filesToCache;
  }

  @Override
  public MediaFile getMainFile() {
    return new MediaFile();
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

  /**
   * check if one of the movies is newly added
   *
   * @return true/false
   */
  public boolean hasNewlyAddedMovies() {
    for (Movie movie : movies) {
      if (movie.isNewlyAdded()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the metadata.
   *
   * @param metadata
   *          the new metadata
   * @param config
   *          the config
   */
  public void setMetadata(MediaMetadata metadata, List<MovieSetScraperMetadataConfig> config) {
    if (metadata == null) {
      LOGGER.error("metadata was null");
      return;
    }

    // check if metadata has at least an id (aka it is not empty)
    if (metadata.getIds().isEmpty()) {
      LOGGER.warn("wanted to save empty metadata for {}", getTitle());
      return;
    }

    // populate ids (and remove old ones)
    ids.clear();
    setIds(metadata.getIds());

    // set chosen metadata
    if (config.contains(MovieSetScraperMetadataConfig.TITLE)) {
      // Capitalize first letter of title if setting is set!
      if (MovieModuleManager.SETTINGS.getCapitalWordsInTitles()) {
        setTitle(WordUtils.capitalize(metadata.getTitle()));
      }
      else {
        setTitle(metadata.getTitle());
      }
    }

    if (config.contains(MovieSetScraperMetadataConfig.PLOT)) {
      setPlot(metadata.getPlot());
    }

    // set scraped
    setScraped(true);

    saveToDb();
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
      if (o1.getYear() > 0 && o2.getYear() > 0) {
        try {
          int year1 = o1.getYear();
          int year2 = o2.getYear();
          return year1 - year2;
        }
        catch (Exception ignored) {
        }
      }

      // fallback: sort via title
      return o2.getTitleForUi().compareTo(o1.getTitleForUi());
    }
  }
}
