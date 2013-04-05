/*
 * Copyright 2012-2013 Manuel Laggner
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
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class UpdateDataSourcesTask.
 * 
 * @author Manuel Laggner
 */

public class MovieUpdateDatasourceTask extends TmmThreadPool {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(MovieUpdateDatasourceTask.class);

  /** The data sources. */
  private List<String>        dataSources;

  /** The file types. */
  private List<String>        fileTypes;

  /** The movie list. */
  private MovieList           movieList;

  /**
   * Instantiates a new scrape task.
   * 
   */
  public MovieUpdateDatasourceTask() {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getMovieSettings().getMovieDataSource());
    fileTypes = new ArrayList<String>(Globals.settings.getVideoFileType());
    initThreadPool(3, "update");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Void doInBackground() {
    try {
      startProgressBar("prepare scan...");
      for (String path : dataSources) {
        File filePath = new File(path);
        for (File subdir : filePath.listFiles()) {
          if (subdir.isDirectory()) {
            submitTask(new FindMovieTask(subdir, path));
          }
        }
      }

      waitForCompletionOrCancel();

      LOGGER.info("removing orphaned movies...");
      startProgressBar("cleanup...");
      for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
        Movie movie = movieList.getMovies().get(i);
        File movieDir = new File(movie.getPath());
        if (!movieDir.exists()) {
          movieList.removeMovie(movie);
        }
      }
      LOGGER.info("Done updating datasource :)");

      LOGGER.info("get MediaInfo...");
      // update MediaInfo
      startProgressBar("getting Mediainfo...");
      initThreadPool(1, "mediainfo");
      for (Movie m : movieList.getMovies()) {
        submitTask(new MediaFileInformationFetcherTask(m));
      }
      waitForCompletionOrCancel();
      if (cancel) {
        cancel(false);// swing cancel
      }
      LOGGER.info("Done getting MediaInfo)");

    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
    }
    return null;
  }

  /**
   * ThreadpoolWorker to work off ONE possible movie from root datasource directory
   * 
   * @author Myron Boyle
   * @version 1.0
   */
  private class FindMovieTask implements Callable<Object> {

    private File   subdir     = null;
    private String datasource = "";

    public FindMovieTask(File subdir, String datasource) {
      this.subdir = subdir;
      this.datasource = datasource;
    }

    @Override
    public String call() throws Exception {
      if (subdir.getName().equals("VIDEO_TS")) {
        findDiscInDirectory(subdir, datasource);
      }
      else if (subdir.getName().equals("BDMV")) {
        findDiscInDirectory(subdir, datasource);
      }
      else {
        findMovieInDirectory(subdir, datasource);
      }
      return subdir.getName();
    }
  }

  /**
   * Special handling for "Disc" folders.<br>
   * create meta files in parent directory.
   * 
   * @param dir
   *          the dir
   * @param dataSource
   *          the data source
   */
  // BD 2.3
  // http://www.blu-raydisc.com/assets/Downloadablefile/BD-ROM_Audio_Visual_Application_Format_Specifications-18780.pdf
  private void findDiscInDirectory(File dir, String dataSource) {
    String parentDir = dir.getParent();
    LOGGER.debug("find Disc in directory " + dir.getPath() + " parent: " + parentDir);

    // check if there are any videofiles in that subdir
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        boolean typeFound = false;

        // do not start with .
        if (name.toLowerCase().startsWith("."))
          return false;

        // check against sample.*
        Pattern pattern = Pattern.compile("(?i)^sample\\..{2,4}");
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches())
          return false;

        // check against *-trailer.*
        pattern = Pattern.compile("(?i).*-trailer\\..{2,4}");
        matcher = pattern.matcher(name);
        if (matcher.matches())
          return false;

        // check if filetype is in our settigns
        for (String type : fileTypes) {
          if (name.toLowerCase().endsWith(type.toLowerCase())) {
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
      try {
        LOGGER.debug("found video files in " + dir.getPath());
        // does the PARENT path exists for an other movie?
        Movie movie = movieList.getMovieByPath(parentDir);
        if (movie == null) {
          LOGGER.debug("movie not yet in our DB, so add " + parentDir);
          // movie did not exist - try to parse a NFO file in parent folder
          movie = Movie.parseNFO(parentDir, videoFiles);
          if (movie == null) {
            // movie did not exist - create new one
            movie = new Movie();
            movie.setTitle(ParserUtils.detectCleanMoviename(FilenameUtils.getBaseName(parentDir)));
            movie.setPath(parentDir);
            movie.addToFiles(videoFiles, MediaFileType.MAIN_MOVIE);
            movie.findImages();
            movie.addLocalTrailers();
            movie.addLocalSubtitles();
          }
          // persist movie
          if (movie != null) {
            movie.setDataSource(dataSource);
            movie.setDisc(true); // sets as Disc Folder
            movie.setDateAdded(new Date());
            LOGGER.info("store movie into DB " + parentDir);
            movie.saveToDb();
            if (movie.getMovieSet() != null) {
              LOGGER.debug("movie is part of a movieset");
              movie.getMovieSet().addMovie(movie);
              movieList.sortMoviesInMovieSet(movie.getMovieSet());
              movie.getMovieSet().saveToDb();
            }
            LOGGER.debug("add movie to GUI");
            movieList.addMovie(movie);
          }
        }
      }
      catch (Exception e) {
        LOGGER.error(e.getMessage());
      }
    }
    // already found Disc folder - DO NOT dig deeper
  }

  /**
   * Find movie in directory.
   * 
   * @param dir
   *          the dir
   * @param dataSource
   *          the data source
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

        // check against sample.*
        Pattern pattern = Pattern.compile("(?i)^sample\\..{2,4}");
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches())
          return false;

        // check against *-trailer.*
        pattern = Pattern.compile("(?i).*-trailer\\..{2,4}");
        matcher = pattern.matcher(name);
        if (matcher.matches())
          return false;

        // check if filetype is in our settigns
        for (String type : fileTypes) {
          if (name.toLowerCase().endsWith(type.toLowerCase())) {
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
      try {
        LOGGER.debug("found video files in " + dir.getPath());
        // does this path exists for an other movie?
        Movie movie = movieList.getMovieByPath(dir.getPath());
        if (movie == null) {
          LOGGER.debug("movie not yet in our DB, so add " + dir.getPath());
          // movie did not exist - try to parse a NFO file
          movie = Movie.parseNFO(dir.getPath(), videoFiles);
          if (movie == null) {
            // movie did not exist - create new one
            movie = new Movie();
            movie.setTitle(ParserUtils.detectCleanMoviename(dir.getName()));
            movie.setPath(dir.getPath());
            movie.addToFiles(videoFiles, MediaFileType.MAIN_MOVIE);
            movie.findImages();
            movie.addLocalTrailers();
            movie.addLocalSubtitles();
          }
          // persist movie
          if (movie != null) {
            movie.setDataSource(dataSource);
            movie.setDateAdded(new Date());
            LOGGER.info("store movie into DB " + dir.getPath());
            movie.saveToDb();
            if (movie.getMovieSet() != null) {
              LOGGER.debug("movie is part of a movieset");
              movie.getMovieSet().addMovie(movie);
              movieList.sortMoviesInMovieSet(movie.getMovieSet());
              // movie.getMovieSet().sortMovies();
              movie.getMovieSet().saveToDb();
            }
            LOGGER.debug("add movie to GUI");
            movieList.addMovie(movie);
          }
        }
      }
      catch (Exception e) {
        LOGGER.error(e.getMessage());
      }
    }
    else {
      // no - dig deeper
      for (File subdir : dir.listFiles()) {
        if (subdir.isDirectory()) {
          // cancel task
          if (cancel) {
            return;
          }

          if (subdir.getName().equals("VIDEO_TS")) {
            findDiscInDirectory(subdir, dataSource);
          }
          else if (subdir.getName().equals("BDMV")) {
            findDiscInDirectory(subdir, dataSource);
          }
          else {
            findMovieInDirectory(subdir, dataSource);
          }
        }
      }
    }
  }

  /*
   * Executed in event dispatching thread
   */
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  public void done() {
    stopProgressBar();
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  private void startProgressBar(String description, int max, int progress) {
    if (!StringUtils.isEmpty(description)) {
      lblProgressAction.setText(description);
    }
    progressBar.setVisible(true);
    progressBar.setIndeterminate(false);
    progressBar.setMaximum(max);
    progressBar.setValue(progress);
    btnCancelTask.setVisible(true);
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  private void startProgressBar(String description) {
    if (!StringUtils.isEmpty(description)) {
      lblProgressAction.setText(description);
    }
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);
    btnCancelTask.setVisible(true);
  }

  /**
   * Stop progress bar.
   */
  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setIndeterminate(false);
    progressBar.setVisible(false);
    btnCancelTask.setVisible(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.ui.TmmSwingWorker#cancel()
   */
  @Override
  public void cancel() {
    cancel = true;
    // cancel(false);
  }

  @Override
  public void callback(Object obj) {
    startProgressBar((String) obj, getTaskcount(), getTaskdone());
  }
}
