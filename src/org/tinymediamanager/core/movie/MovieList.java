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
import org.tinymediamanager.scraper.IMediaMetadataProvider;

/**
 * The Class MovieList.
 */
public class MovieList extends AbstractModelObject {

  /** The Constant logger. */
  private static final Logger    logger         = Logger.getLogger(MovieList.class);

  /** The instance. */
  private static MovieList       instance;

  /** The settings. */
  private final Settings         settings       = Settings.getInstance();

  /** The movie list. */
  private final List<Movie>      movieList      = ObservableCollections.observableList(new ArrayList<Movie>());

  /** The movies to scrape. */
  private List<MovieJobConfig>   moviesToScrape = new ArrayList<MovieJobConfig>();

  /** The metadata provider. */
  private IMediaMetadataProvider metadataProvider;

  /**
   * Instantiates a new movie list.
   */
  private MovieList() {
    // load existing movies from database
    loadMoviesFromDatabase();

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
    movieList.add(movie);
    firePropertyChange("movies", null, movieList);
  }

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
    movieList.remove(movie);
    firePropertyChange("movies", null, movieList);
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public List<Movie> getMovies() {
    return movieList;
  }

  // load movielist from database
  /**
   * Load movies from database.
   */
  private void loadMoviesFromDatabase() {
    try {
      TypedQuery<Movie> query = Globals.entityManager.createQuery("SELECT movie FROM Movie movie", Movie.class);
      List<Movie> movies = query.getResultList();
      // List<Movie> movies = MovieJdbcDAO.getInstance().getAllMovies();
      for (Movie movie : movies) {
        movie.setObservableCastList();
        addMovie(movie);
      }
    }
    catch (PersistenceException e) {
      logger.error(e.getStackTrace());
    }
  }

  // Search for new media
  /**
   * Update data sources.
   */
  public void updateDataSources() {
    Globals.entityManager.getTransaction().begin();
    // each datasource
    for (String path : settings.getMovieDataSource()) {
      // each subdir
      for (File subdir : new File(path).listFiles()) {
        if (subdir.isDirectory()) {
          findMovieInDirectory(subdir);
        }
      }
    }
    Globals.entityManager.getTransaction().commit();
  }

  // check if there is a movie in this dir
  /**
   * Find movie in directory.
   * 
   * @param dir
   *          the dir
   */
  private void findMovieInDirectory(File dir) {
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
      // does this path exists for an other movie?
      Movie movie = getMovieByPath(dir.getPath());
      if (movie == null) {
        // movie did not exist - try to parse a NFO file
        movie = Movie.parseNFO(dir.getPath());
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
        }
        // persist movie
        if (movie != null) {
          Globals.entityManager.persist(movie);
          addMovie(movie);
        }
      }

      for (File file : videoFiles) {
        // check if that file exists for that movie
        if (!movie.hasFile(file.getName())) {
          // create new movie file
          movie.addToFiles(file.getName());
        }
      }

    }
    else {
      // no - dig deeper
      for (File subdir : dir.listFiles()) {
        if (subdir.isDirectory()) {
          findMovieInDirectory(subdir);
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
   * Adds the movie to scrape list.
   * 
   * @param movie
   *          the movie
   * @param scrapeSetting
   *          the scrape setting
   */
  public void addMovieToScrapeList(Movie movie, int scrapeSetting) {
    moviesToScrape.add(new MovieJobConfig(movie, scrapeSetting));
  }

  // // scrape all selected movies
  // public void searchMovies() {
  // if (movieScraperJob == null) {
  // movieScraperJob = new Job("Scraping movies") {
  // @Override
  // protected IStatus run(IProgressMonitor monitor) {
  // while (moviesToScrape.size() > 0) {
  // MovieJobConfig job = moviesToScrape.get(0);
  // searchMovie(job.getMovie(), job.getScrapeSetting());
  // moviesToScrape.remove(job);
  // }
  // return Status.OK_STATUS;
  // }
  // };
  // }
  // movieScraperJob.schedule();
  // }
  //
  // // show moviechooser window in UIJob
  // private void chooseMovie(Movie movieToScrape) {
  // if (movieChooserJob == null) {
  // movieChooserJob = new MovieChooserJob("Choose movie");
  // movieChooserJob.setRule(Globals.uiJob);
  // }
  // movieChooserJob.addMovie(movieToScrape);
  // }
  //
  // public IMediaMetadataProvider getMetadataProvider() {
  // return this.metadataProvider;
  // }
}
