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
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The Class UpdateDataSourcesTask.
 * 
 * @author Myron Boyle
 */

public class MovieUpdateDatasourceTask extends TmmThreadPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(MovieUpdateDatasourceTask.class);

  // skip well-known, but unneeded BD & DVD folders
  private final List<String>  skip   = Arrays.asList("CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "AUXDATA", "AUDIO_TS");

  private List<String>        dataSources;
  private MovieList           movieList;

  /**
   * Instantiates a new scrape task.
   * 
   */
  public MovieUpdateDatasourceTask() {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getMovieSettings().getMovieDataSource());
  }

  public MovieUpdateDatasourceTask(String datasource) {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(1);
    dataSources.add(datasource);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Void doInBackground() {
    try {
      for (String ds : dataSources) {

        startProgressBar("prepare scan '" + ds + "'");
        initThreadPool(3, "update");
        File[] dirs = new File(ds).listFiles();
        if (dirs == null) {
          // error - continue with next datasource
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable",
              new String[] { ds }));
          continue;
        }
        for (File file : dirs) {
          if (file.isDirectory() && !cancel) {
            submitTask(new FindMovieTask(file, ds));
          }
        }
        waitForCompletionOrCancel();

        startProgressBar("getting Mediainfo & cleanup...");
        initThreadPool(1, "mediainfo");
        LOGGER.info("removing orphaned movies/files...");
        for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
          if (cancel) {
            break;
          }
          Movie movie = movieList.getMovies().get(i);
          if (!ds.equals(movie.getDataSource())) {
            // check only movies matching datasource
            continue;
          }

          File movieDir = new File(movie.getPath());
          if (!movieDir.exists()) {
            LOGGER.debug("movie directory '" + movieDir + "' not found, removing...");
            movieList.removeMovie(movie);
          }
          else {
            // check and delete all not found MediaFiles
            List<MediaFile> mediaFiles = new ArrayList<MediaFile>(movie.getMediaFiles());
            for (MediaFile mf : mediaFiles) {
              if (!mf.getFile().exists()) {
                movie.removeFromMediaFiles(mf);
              }
            }
            movie.saveToDb();
            submitTask(new MediaFileInformationFetcherTask(movie.getMediaFiles(), movie));
          }
        } // end movie loop
        waitForCompletionOrCancel();

      } // END datasource loop
      LOGGER.info("Done updating datasource :)");

      if (cancel) {
        cancel(false);// swing cancel
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
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
      // find all possible movie folders recursive
      ArrayList<File> mov = getRootMovieDirs(subdir, 1);
      // remove dupe movie dirs
      HashSet<File> h = new HashSet<File>(mov);
      mov.clear();
      mov.addAll(h);
      for (File movieDir : mov) {
        parseMovieDirectory(movieDir, datasource);
      }
      // return first level folder name... uhm. yeah
      return subdir.getName();
    }
  }

  /**
   * parses the complete movie directory, and adds a movie with all found MediaFiles
   * 
   * @param movieDir
   * @param dataSource
   */
  private void parseMovieDirectory(File movieDir, String dataSource) {
    try {
      Movie movie = movieList.getMovieByPath(movieDir.getPath());
      ArrayList<MediaFile> mfs = getAllMediaFilesRecursive(movieDir);

      if (movie == null) {
        LOGGER.info("parsing movie " + movieDir);
        movie = new Movie();

        // first round - try to parse NFO(s) first
        for (MediaFile mf : mfs) {
          if (mf.getType().equals(MediaFileType.NFO)) {
            LOGGER.debug("parsing NFO " + mf.getFilename());
            Movie nfo = null;
            switch (Globals.settings.getMovieSettings().getMovieConnector()) {
              case XBMC:
                nfo = MovieToXbmcNfoConnector.getData(mf.getPath() + File.separator + mf.getFilename());
                break;

              case MP:
                nfo = MovieToMpNfoConnector.getData(mf.getPath() + File.separator + mf.getFilename());
                break;
            }
            if (nfo != null) {
              movie = nfo;
              movie.addToMediaFiles(mf);
            }
            else {
              // is NFO, but parsing exception. try to find at least imdb id within
              try {
                String imdb = FileUtils.readFileToString(mf.getFile());
                imdb = StrgUtils.substr(imdb, ".*(tt\\d{7}).*");
                if (!imdb.isEmpty()) {
                  LOGGER.debug("Found IMDB id: " + imdb);
                  movie.setImdbId(imdb);
                }
              }
              catch (IOException e) {
                LOGGER.warn("couldn't read NFO " + mf.getFilename());
              }
            } // end NFO null
          }
        }

        if (movie.getTitle().isEmpty()) {
          movie.setTitle(ParserUtils.detectCleanMoviename(movieDir.getName()));
        }
        movie.setPath(movieDir.getPath());
        movie.setDataSource(dataSource);
        movie.setDateAdded(new Date());
        movie.setNewlyAdded(true);

        movie.findActorImages(); // TODO: find as MediaFIles
        LOGGER.debug("store movie into DB " + movieDir.getName());
        movie.saveToDb(); // savepoint

        if (movie.getMovieSet() != null) {
          LOGGER.debug("movie is part of a movieset");
          movie.getMovieSet().addMovie(movie);
          movieList.sortMoviesInMovieSet(movie.getMovieSet());
          movie.getMovieSet().saveToDb();
          movie.saveToDb();
        }

      } // end movie is null

      List<MediaFile> current = movie.getMediaFiles();

      // second round - now add all the other known files
      for (MediaFile mf : mfs) {

        if (!current.contains(mf)) { // a new mediafile was found!

          if (mf.getPath().toUpperCase().contains("BDMV") || mf.getPath().toUpperCase().contains("VIDEO_TS")) {
            movie.setDisc(true);
          }

          switch (mf.getType()) {
            case VIDEO:
              LOGGER.debug("parsing video file " + mf.getFilename());
              movie.addToMediaFiles(mf);
              break;

            case TRAILER:
              LOGGER.debug("parsing trailer " + mf.getFilename());
              MediaTrailer mt = new MediaTrailer();
              mt.setName(mf.getFilename());
              mt.setProvider("downloaded");
              mt.setQuality("unknown");
              mt.setInNfo(false);
              mt.setUrl(mf.getFile().toURI().toString());
              movie.addTrailer(mt);
              movie.addToMediaFiles(mf);
              break;

            case SUBTITLE:
              LOGGER.debug("parsing subtitle " + mf.getFilename());
              if (!mf.isPacked()) {
                movie.setSubtitles(true);
                movie.addToMediaFiles(mf);
              }
              break;

            case POSTER:
              LOGGER.debug("parsing poster " + mf.getFilename());
              movie.addToMediaFiles(mf);
              break;

            case FANART:
              if (mf.getPath().toLowerCase().contains("extrafanart")) {
                // there shouldn't be any files here
                LOGGER.warn("problem: detected media file type FANART in extrafanart folder: " + mf.getPath());
                continue;
              }
              LOGGER.debug("parsing fanart " + mf.getFilename());
              movie.addToMediaFiles(mf);
              break;

            case EXTRAFANART:
              LOGGER.debug("parsing extrafanart " + mf.getFilename());
              movie.addToMediaFiles(mf);
              break;

            case AUDIO:
              LOGGER.debug("parsing audio stream " + mf.getFilename());
              movie.addToMediaFiles(mf);
              break;

            case GRAPHIC:
            case UNKNOWN:
            default:
              LOGGER.debug("NOT adding unknown media file type: " + mf.getFilename());
              // movie.addToMediaFiles(mf); // DO NOT ADD UNKNOWN
              break;
          } // end switch type
        } // end new MF found
      } // end MF loop

      // third round - try to match unknown graphics like title.ext or filename.ext as poster
      if (movie.getPoster().isEmpty()) {
        for (MediaFile mf : mfs) {
          if (mf.getType().equals(MediaFileType.GRAPHIC)) {
            LOGGER.debug("parsing unknown graphic " + mf.getFilename());
            List<MediaFile> vid = movie.getMediaFiles(MediaFileType.VIDEO);
            if (vid != null && !vid.isEmpty()) {
              String vfilename = FilenameUtils.getBaseName(vid.get(0).getFilename());
              if (vfilename.equals(FilenameUtils.getBaseName(mf.getFilename())) // basename match
                  || Utils.cleanStackingMarkers(vfilename).trim().equals(FilenameUtils.getBaseName(mf.getFilename())) // basename w/o stacking
                  || movie.getTitle().equals(FilenameUtils.getBaseName(mf.getFilename()))) { // title match
                mf.setType(MediaFileType.POSTER);
                movie.addToMediaFiles(mf);
              }
            }
          }
        }
      }

      movie.saveToDb();
      movieList.addMovie(movie);
    }
    catch (NullPointerException e) {
      LOGGER.error("NPE:", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movieDir.getPath(), "message.update.errormoviedir"));
    }
    catch (Exception e) {
      LOGGER.error("error update Datasources", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movieDir.getPath(), "message.update.errormoviedir", new String[] { ":",
          e.getLocalizedMessage() }));
    }
  }

  /**
   * searches for file type VIDEO and tries to detect the root movie directory
   * 
   * @param directory
   *          start dir
   * @param level
   *          the level how deep we are (level 0 = datasource root)
   * @return arraylist of abolute movie dirs
   */
  public ArrayList<File> getRootMovieDirs(File directory, int level) {
    ArrayList<File> ar = new ArrayList<File>();

    // separate files & dirs
    ArrayList<File> files = new ArrayList<File>();
    ArrayList<File> dirs = new ArrayList<File>();
    File[] list = directory.listFiles();
    for (File file : list) {
      if (file.isFile()) {
        files.add(file);
      }
      else {
        // ignore .folders and others
        if (!skip.contains(file.getName().toUpperCase()) && !file.getName().startsWith(".")) {
          dirs.add(file);
        }
      }
    }
    list = null;

    for (File f : files) {
      boolean disc = false;
      MediaFile mf = new MediaFile(f);

      if (mf.getType().equals(MediaFileType.VIDEO)) {

        // get current folder
        File moviedir = f.getParentFile();

        // walk reverse till disc root (if found)
        while (moviedir.getPath().toUpperCase().contains("BDMV") || moviedir.getPath().toUpperCase().contains("VIDEO_TS")) {
          disc = true;
          moviedir = moviedir.getParentFile();
        }
        if (disc) {
          ar.add(moviedir);
          continue; // proceed with next file
        }

        // ok, regular structure
        if (dirs.isEmpty() && level > 1 && !Utils.getStackingMarker(moviedir.getName()).isEmpty()) {
          // no more dirs in that directory
          // and at least 2 levels deep
          // stacking found (either on file or parent dir)
          // -> assume parent as movie dir"
          moviedir = moviedir.getParentFile();
          ar.add(moviedir);
        }
        else {
          // -> assume current dir as movie dir"
          ar.add(moviedir);
        }
      }
    }

    for (File dir : dirs) {
      ar.addAll(getRootMovieDirs(dir, level + 1));
    }

    return ar;
  }

  /**
   * recursively gets all MediaFiles from a moviedir
   * 
   * @param dir
   *          the movie root dir
   * @return list of files
   */
  public ArrayList<MediaFile> getAllMediaFilesRecursive(File dir) {
    ArrayList<MediaFile> mv = new ArrayList<MediaFile>();

    File[] list = dir.listFiles();
    for (File file : list) {
      if (file.isFile()) {
        mv.add(new MediaFile(file));
      }
      else {
        // ignore .folders and others
        if (!skip.contains(file.getName().toUpperCase()) && !file.getName().startsWith(".")) {
          mv.addAll(getAllMediaFilesRecursive(file));
        }
      }
    }

    return mv;
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
