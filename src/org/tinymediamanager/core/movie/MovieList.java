/*
 * Copyright 2012 Manuel Laggner
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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.scraper.IHasFindByIMDBID;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.SearchQuery;
import org.tinymediamanager.scraper.xbmc.XbmcMetadataProvider;
import org.tinymediamanager.scraper.xbmc.XbmcScraperParser;

/**
 * The Class MovieList.
 */
public class MovieList extends AbstractModelObject {

  /** The Constant logger. */
  private static final Logger    LOGGER   = Logger.getLogger(MovieList.class);

  /** The instance. */
  private static MovieList       instance;

  /** The settings. */
  private final Settings         settings = Settings.getInstance();

  /** The movie list. */
  private List<Movie>            movieList;

  /** The metadata provider. */
  private IMediaMetadataProvider metadataProvider;

  /**
   * Instantiates a new movie list.
   */
  private MovieList() {

  }

  /**
   * Gets the single instance of MovieList.
   * 
   * @return single instance of MovieList
   */
  public static MovieList getInstance() {
    if (MovieList.instance == null) {
      MovieList.instance = new MovieList();
    }
    return MovieList.instance;
  }

  /**
   * Adds the movie.
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    int oldValue = movieList.size();
    movieList.add(movie);
    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * Gets the unscraped movies.
   * 
   * @return the unscraped movies
   */
  public List<Movie> getUnscrapedMovies() {
    List<Movie> unscrapedMovies = new ArrayList<Movie>();

    for (Movie movie : movieList) {
      if (!movie.isScraped()) {
        unscrapedMovies.add(movie);
      }
    }

    return unscrapedMovies;
  }

  /**
   * Removes the movie.
   * 
   * @param movie
   *          the movie
   */
  public void removeMovie(Movie movie) {
    int oldValue = movieList.size();
    movieList.remove(movie);
    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public List<Movie> getMovies() {
    if (movieList == null) {
      movieList = ObservableCollections.observableList(new ArrayList<Movie>());
    }
    return movieList;
  }

  // load movielist from database
  /**
   * Load movies from database.
   */
  public void loadMoviesFromDatabase() {
    try {
      TypedQuery<Movie> query = Globals.entityManager.createQuery("SELECT movie FROM Movie movie", Movie.class);
      List<Movie> movies = query.getResultList();
      if (movies != null) {
        LOGGER.debug("found " + movies.size() + " movies in database");
        movieList = ObservableCollections.observableList(new ArrayList<Movie>(movies.size()));
      } else {
        LOGGER.debug("found nothing in database");
      }
      // LOGGER.debug(movies);
      for (Object obj : movies)
        if (obj instanceof Movie) {
          Movie movie = (Movie) obj;
          // LOGGER.debug(movie);
          movie.setObservables();
          addMovie(movie);
        } else {
          LOGGER.error("retrieved no movie: " + obj);
        }
    } catch (PersistenceException e) {
      LOGGER.error("loadMoviesFromDatabase", e);
    } catch (Exception e) {
      LOGGER.error("loadMoviesFromDatabase", e);
    }
  }

  /**
   * find movies in path.
   * 
   * @param path
   *          the path
   */
  public void findMoviesInPath(String path) {
    LOGGER.debug("find movies in path " + path);
    for (File subdir : new File(path).listFiles()) {
      if (subdir.isDirectory()) {
        findMovieInDirectory(subdir, path);
      }
    }
  }

  // check if there is a movie in this dir
  /**
   * Find movie in directory.
   * 
   * @param dir
   *          the dir
   */
  private void findMovieInDirectory(File dir, String dataSource) {
    LOGGER.debug("find movies in directory " + dir.getPath());
    // check if there are any videofiles in that subdir
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        boolean typeFound = false;

        // do not start with .
        if (name.toLowerCase().startsWith("."))
          return false;

        // check if filetype is in our settigns
        for (String type : settings.getVideoFileType()) {
          if (name.toLowerCase().endsWith(type)) {
            typeFound = true;
            break;
          }
        }

        return typeFound;
      }
    };

    File[] videoFiles = dir.listFiles(filter);
    // movie files found in directory?
    if (videoFiles.length > 0) {
      LOGGER.debug("found video files in " + dir.getPath());
      // does this path exists for an other movie?
      Movie movie = getMovieByPath(dir.getPath());
      if (movie == null) {
        LOGGER.debug("no movie exists in path " + dir.getPath());
        // movie did not exist - try to parse a NFO file
        movie = Movie.parseNFO(dir.getPath(), videoFiles);
        if (movie == null) {
          // movie did not exist - create new one
          movie = new Movie();
          String name = dir.getName().replaceAll("\\[.*[0-9].\\]", ""); // cut
          // year
          // information
          name = name.replaceAll("[._]", " "); // replace ._ in folder
          // name
          movie.setName(name);
          movie.setPath(dir.getPath());
          movie.addToFiles(videoFiles);
          movie.findImages();
        }
        // persist movie
        if (movie != null) {
          movie.setDataSource(dataSource);
          LOGGER.debug("store movie " + dir.getPath());
          movie.saveToDb();
          addMovie(movie);
        }
      }

      // for (File file : videoFiles) {
      // // check if that file exists for that movie
      // if (!movie.hasFile(file.getName())) {
      // // create new movie file
      // movie.addToFiles(file.getName());
      // }
      // }

    } else {
      // no - dig deeper
      for (File subdir : dir.listFiles()) {
        if (subdir.isDirectory()) {
          findMovieInDirectory(subdir, dataSource);
        }
      }
    }
  }

  /**
   * Gets the movie by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   */
  private Movie getMovieByPath(String path) {

    for (Movie movie : movieList) {
      if (movie.getPath().compareTo(path) == 0) {
        return movie;
      }
    }

    return null;
  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
   * @param ImdbId
   *          the imdb id
   * @return the list
   */
  public List<MediaSearchResult> searchMovie(String searchTerm, String ImdbId) {
    List<MediaSearchResult> sr = searchMovieByImdbId(ImdbId);
    if (sr == null || sr.size() == 0) {
      sr = searchMovie(searchTerm);
    }

    return sr;
  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
   * @return the list
   */
  private List<MediaSearchResult> searchMovie(String searchTerm) {
    // format searchstring
    searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    List<MediaSearchResult> searchResult = null;
    try {
      searchResult = getMetadataProvider().search(new SearchQuery(MediaType.MOVIE, SearchQuery.Field.QUERY, searchTerm));
    } catch (Exception e) {
      LOGGER.error("searchMovie", e);
    }

    return searchResult;
  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
   * @return the list
   */
  private List<MediaSearchResult> searchMovieByImdbId(String imdbId) {

    List<MediaSearchResult> searchResult = null;
    try {
      if (getMetadataProvider() instanceof IHasFindByIMDBID) {
        IHasFindByIMDBID provider = (IHasFindByIMDBID) getMetadataProvider();
        MediaSearchResult result = provider.searchByImdbId(imdbId);
        if (result != null) {
          searchResult = new ArrayList<MediaSearchResult>(1);
          searchResult.add(result);
        }
      }
    } catch (Exception e) {
      LOGGER.error("searchMovie", e);
    }

    return searchResult;
  }

  /**
   * Gets the metadata provider.
   * 
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider() {
    if (metadataProvider == null) {
      // LOGGER.debug("get instance of TmdbMetadataProvider");
      // metadataProvider = TmdbMetadataProvider.getInstance();
      LOGGER.debug("get instance of XbmcMetadataProvider");
      try {
        metadataProvider = new XbmcMetadataProvider(new XbmcScraperParser().parseScraper(new File("xbmc_scraper/metadata.imdb.com/imdb.xml")));
        // metadataProvider = new XbmcMetadataProvider(new
        // XbmcScraperParser().parseScraper(new
        // File("xbmc_scraper/metadata.imdb.de/imdb_de.xml")));
      } catch (Exception e) {
        LOGGER.error("tried to get xmbc scraper", e);
      }
    }
    return metadataProvider;
  }

  public int getMovieCount() {
    int size = movieList.size();
    return size;
  }
}
