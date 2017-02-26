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
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCacheTask;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.connector.MovieToKodiNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class UpdateDataSourcesTask.
 * 
 * @author Myron Boyle
 */
@Deprecated
public class MovieUpdateDatasourceTask extends TmmThreadPool {
  private static final Logger         LOGGER           = LoggerFactory.getLogger(MovieUpdateDatasourceTask.class);
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());                                  //$NON-NLS-1$

  // skip well-known, but unneeded folders (UPPERCASE)
  private static final List<String>   skipFolders      = Arrays.asList(".", "..", "CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "SSIF", "AUXDATA",
      "AUDIO_TS", "$RECYCLE.BIN", "RECYCLER", "SYSTEM VOLUME INFORMATION", "@EADIR");

  // skip folders starting with a SINGLE "." or "._"
  private static final String         skipFoldersRegex = "^[.][\\w@]+.*";
  private static Pattern              video3DPattern   = Pattern.compile("(?i)[ ._\\(\\[-]3D[ ._\\)\\]-]?");

  private List<String>                dataSources;
  private MovieList                   movieList;
  private HashSet<File>               filesFound       = new HashSet<>();

  @Deprecated
  public MovieUpdateDatasourceTask() {
    super(BUNDLE.getString("update.datasource"));
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<>(MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource());
  }

  @Deprecated
  public MovieUpdateDatasourceTask(String datasource) {
    super(BUNDLE.getString("update.datasource") + " (" + datasource + ")");
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<>(1);
    dataSources.add(datasource);
  }

  @Override
  public void doInBackground() {
    // check if there is at least one DS to update
    Utils.removeEmptyStringsFromList(dataSources);
    if (dataSources.isEmpty()) {
      LOGGER.info("no datasource to update");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.nonespecified"));
      return;
    }

    // get existing movie folders
    List<File> existing = new ArrayList<>();
    for (Movie movie : movieList.getMovies()) {
      existing.add(new File(movie.getPath()));
    }

    try {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      List<Path> imageFiles = new ArrayList<>();

      for (String ds : dataSources) {
        setTaskName(BUNDLE.getString("update.datasource") + " '" + ds + "'");
        publishState();

        // just check main/root datasource folder
        List<File> newMovieInDsRoot = new ArrayList<>();
        List<File> existingMovieInDsRoot = new ArrayList<>();

        if (MovieModuleManager.MOVIE_SETTINGS.isDetectMovieMultiDir()) {
          initThreadPool(1, "update"); // use only one, since the multiDir detection relies on accurate values...
        }
        else {
          initThreadPool(3, "update");
        }
        File[] dirs = new File(ds).listFiles();
        if (dirs == null || dirs.length == 0) {
          // error - continue with next datasource
          MessageManager.instance
              .pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable", new String[] { ds }));
          continue;
        }

        boolean parseDsRoot = false;
        for (File file : dirs) {
          if (!cancel) {
            if (file.isDirectory()) {
              String directoryName = file.getName();
              // check against unwanted dirs
              if (skipFolders.contains(directoryName.toUpperCase(Locale.ROOT)) || directoryName.matches(skipFoldersRegex)
                  || MovieModuleManager.MOVIE_SETTINGS.getMovieSkipFolders().contains(file.getAbsolutePath())) {
                LOGGER.info("ignoring directory " + directoryName);
                continue;
              }

              // dig deeper in this dir
              if (existing.contains(file)) {
                existingMovieInDsRoot.add(file);
              }
              else {
                newMovieInDsRoot.add(file);
              }
            }
            else {
              if (Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()))) {
                if (MovieModuleManager.MOVIE_SETTINGS.isDetectMovieMultiDir()) {
                  parseDsRoot = true; // at least on movie found in DS root
                }
                else {
                  MessageManager.instance.pushMessage(
                      new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.movieinroot", new String[] { file.getName() }));
                }
              }
            }
          }
        }

        // parse new directories first
        for (File subdir : newMovieInDsRoot) {
          submitTask(new FindMovieTask(subdir, ds));
        }
        for (File subdir : existingMovieInDsRoot) {
          submitTask(new FindMovieTask(subdir, ds));
        }

        waitForCompletionOrCancel();

        if (parseDsRoot) {
          LOGGER.debug("parsing datasource root for movies...");
          initThreadPool(1, "update");
          submitTask(new FindMovieTask(new File(ds), ds));
          waitForCompletionOrCancel();
        }

        if (cancel) {
          break;
        }

        // cleanup
        cleanup(ds);

        if (cancel) {
          break;
        }

        // mediainfo
        gatherMediainfo(ds);

        waitForCompletionOrCancel();
        if (cancel) {
          break;
        }

        // build image cache on import
        if (MovieModuleManager.MOVIE_SETTINGS.isBuildImageCacheOnImport()) {
          for (Movie movie : movieList.getMovies()) {
            if (!new File(ds).equals(new File(movie.getDataSource()))) {
              // check only movies matching datasource
              continue;
            }
            imageFiles.addAll(movie.getImagesToCache());
          }
        }

      } // END datasource loop

      if (imageFiles.size() > 0) {
        ImageCacheTask task = new ImageCacheTask(imageFiles);
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }

      if (MovieModuleManager.MOVIE_SETTINGS.getSyncTrakt()) {
        TmmTask task = new SyncTraktTvTask(true, true, false, false);
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }

      stopWatch.stop();
      LOGGER.info("Done updating datasource :) - took " + stopWatch);
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
  }

  /**
   * parses a list of VIDEO files in a dir and creates movies out of it
   */
  private void parseMultiMovieDir(List<File> files, File parentDir, String datasource) {
    if (files == null || files.isEmpty()) {
      return;
    }
    File[] contents = parentDir.listFiles();
    if (contents == null) {
      return;
    }

    List<File> completeDirContents = new ArrayList<>(Arrays.asList(contents));

    // just compare filename length, start with longest b/c of overlapping names
    Collections.sort(files, new Comparator<File>() {
      @Override
      public int compare(File file1, File file2) {
        return file2.getName().length() - file1.getName().length();
      }
    });
    for (File file : files) {
      Movie movie = null;
      MediaFile mf = new MediaFile(file);
      String basename = FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mf.getFilename()));

      // 1) check if MF is already assigned to a movie within path
      for (Movie m : movieList.getMoviesByPath(mf.getFile().getParentFile())) {
        if (m.getMediaFiles(MediaFileType.VIDEO).contains(mf)) {
          // ok, our MF is already in an movie
          LOGGER.debug("found movie '" + m.getTitle() + "' from MediaFile " + file);
          movie = m;
          break;
        }
        for (MediaFile mfile : m.getMediaFiles(MediaFileType.VIDEO)) {
          // try to match like if we would create a new movie
          String[] mfileTY = ParserUtils.detectCleanMovienameAndYear(FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mfile.getFilename())));
          String[] mfTY = ParserUtils.detectCleanMovienameAndYear(FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mf.getFilename())));
          if (mfileTY[0].equals(mfTY[0]) && mfileTY[1].equals(mfTY[1])) { // title AND year (even empty) match
            LOGGER.debug("found possible movie '" + m.getTitle() + "' from filename " + file);
            movie = m;
            break;
          }
        }
      }

      if (movie == null) {
        // 2) create if not found
        // check for NFO
        File nfoFile = new File(parentDir, basename + ".nfo");
        if (completeDirContents.contains(nfoFile)) {
          MediaFile nfo = new MediaFile(nfoFile, MediaFileType.NFO);
          // from NFO?
          LOGGER.debug("found NFO '" + nfo.getFile() + "' - try to parse");
          switch (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector()) {
            case XBMC:
              movie = MovieToXbmcNfoConnector.getData(nfo.getFileAsPath());
              break;

            case KODI:
              movie = MovieToKodiNfoConnector.getData(nfo.getFileAsPath());
              break;

            case MP:
              movie = MovieToMpNfoConnector.getData(nfo.getFileAsPath());
              break;
          }
          if (movie != null) {
            // valid NFO found, so add itself as MF
            LOGGER.debug("NFO valid - add it");
            movie.addToMediaFiles(nfo);
          }
        }
        if (movie == null) {
          // still NULL, create new movie movie from file
          LOGGER.debug("Create new movie from file: " + file);
          movie = new Movie();
          String[] ty = ParserUtils.detectCleanMovienameAndYear(basename);
          movie.setTitle(ty[0]);
          if (!ty[1].isEmpty()) {
            movie.setYear(ty[1]);
          }
          // if the String 3D is in the movie file name, assume it is a 3D movie
          Matcher matcher = video3DPattern.matcher(basename);
          if (matcher.find()) {
            movie.setVideoIn3D(true);
          }
          movie.setDateAdded(new Date());
        }
        movie.setDataSource(datasource);
        movie.setNewlyAdded(true);
        movie.setPath(mf.getPath());

        movieList.addMovie(movie);
      }

      if (!Utils.isValidImdbId(movie.getImdbId())) {
        movie.setImdbId(ParserUtils.detectImdbId(mf.getFile().getAbsolutePath()));
      }
      if (movie.getMediaSource() == MediaSource.UNKNOWN) {
        movie.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
      }
      LOGGER.debug("parsing video file " + mf.getFilename());
      movie.addToMediaFiles(mf);
      movie.setDateAddedFromMediaFile(mf);
      movie.setMultiMovieDir(true);

      // 3) find additional files, which start with videoFileName
      List<MediaFile> existingMediaFiles = new ArrayList<>(movie.getMediaFiles());
      List<MediaFile> foundMediaFiles = new ArrayList<>();
      for (int i = completeDirContents.size() - 1; i >= 0; i--) {
        File fileInDir = completeDirContents.get(i);
        if (fileInDir.getName().startsWith(basename)) {
          MediaFile mediaFile = new MediaFile(fileInDir);
          if (!existingMediaFiles.contains(mediaFile)) {
            // store file for faster cleanup
            synchronized (filesFound) {
              filesFound.add(fileInDir);
            }
            foundMediaFiles.add(mediaFile);
          }
          // started with basename, so remove it for others
          completeDirContents.remove(i);
        }
      }
      addMediafilesToMovie(movie, foundMediaFiles);

      if (movie.getMovieSet() != null) {
        LOGGER.debug("movie is part of a movieset");
        // movie.getMovieSet().addMovie(movie);
        movie.getMovieSet().insertMovie(movie);
        movieList.sortMoviesInMovieSet(movie.getMovieSet());
        movie.getMovieSet().saveToDb();
      }

      movie.saveToDb();
    } // end for every file

    // check stacking on all movie from this dir (it might have changed!)
    for (Movie m : movieList.getMoviesByPath(parentDir)) {
      m.reEvaluateStacking();
      m.saveToDb();
    }
  }

  /**
   * parses the complete movie directory, and adds a movie with all found MediaFiles
   */
  private void parseMovieDirectory(File movieDir, String dataSource) {
    try {
      // list all type VIDEO files
      File[] fileArray = movieDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File file) {
          if (file.getName().equals(".tmmignore") || file.getName().equals("tmmignore")) {
            return true;
          }
          if (file.isDirectory() || file.getName().startsWith("._")) { // MacOS ignore
            return false;
          }
          return new MediaFile(file).getType().equals(MediaFileType.VIDEO); // no trailer or extra vids!
        }
      });

      if (fileArray == null) {
        LOGGER.error("Whops. Cannot access directory: " + movieDir.getName());
        return;
      }

      List<File> files = new ArrayList<>(Arrays.asList(fileArray));

      if (files.contains(new File(movieDir, ".tmmignore")) || files.contains(new File(movieDir, "tmmignore"))) {
        return;
      }

      // store dir for faster cleanup
      synchronized (filesFound) {
        filesFound.add(movieDir);
      }

      // check if we have more than one movie in dir
      HashSet<String> h = new HashSet<>();
      LOGGER.debug("Checking for multi-movie dir; parsing all video files in " + movieDir);

      // no need to check files == null; NPE is caught
      for (File file : files) {
        MediaFile mf = new MediaFile(file);
        if (mf.isDiscFile()) { // MacOS ignore
          // ignore disc files when trying to detect multi movie dir!
          continue;
        }
        String[] ty = ParserUtils.detectCleanMovienameAndYear(FilenameUtils.getBaseName(Utils.cleanStackingMarkers(file.getName())));
        h.add(ty[0] + ty[1]); // title+year, just temp
      }
      // more than 1, or if DS=dir then assume a multi dir (only second level is a normal movie dir)
      if (h.size() > 1 || movieDir.equals(new File(dataSource))) {
        LOGGER.debug("WOOT - we have a multi movie directory: " + movieDir);
        if (MovieModuleManager.MOVIE_SETTINGS.isDetectMovieMultiDir()) {
          parseMultiMovieDir(files, movieDir, dataSource);
        }
        else {
          MessageManager.instance.pushMessage(
              new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.movieinroot", new String[] { movieDir.getName() }));
        }
      }
      else {
        LOGGER.debug("PAH - normal movie directory: " + movieDir);

        Movie movie = movieList.getMovieByPath(movieDir);
        ArrayList<MediaFile> mfs = getAllMediaFilesRecursive(movieDir);

        if (movie == null) {
          LOGGER.info("Movie not found; parsing directory" + movieDir);
          movie = new Movie();
          String bdinfoTitle = ""; // title parsed out of BDInfo
          String videoName = ""; // title from file

          // first round - try to parse NFO(s) first
          for (MediaFile mf : mfs) {
            if (mf.getType().equals(MediaFileType.NFO)) {
              LOGGER.debug("parsing NFO " + mf.getFilename());
              Movie nfo = null;
              switch (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector()) {
                case XBMC:
                  nfo = MovieToXbmcNfoConnector.getData(mf.getFileAsPath());
                  break;

                case KODI:
                  nfo = MovieToKodiNfoConnector.getData(mf.getFileAsPath());
                  break;

                case MP:
                  nfo = MovieToMpNfoConnector.getData(mf.getFileAsPath());
                  break;
              }
              if (nfo != null) {
                movie = nfo;
              }
              else {
                // is NFO, but parsing exception. try to find at least imdb id within
                try {
                  String imdb = FileUtils.readFileToString(mf.getFile());
                  imdb = ParserUtils.detectImdbId(imdb);
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
            else if (mf.getType().equals(MediaFileType.TEXT)) {
              try {
                String txtFile = FileUtils.readFileToString(mf.getFile());

                String bdinfo = StrgUtils.substr(txtFile, ".*Disc Title:\\s+(.*?)[\\n\\r]");
                if (!bdinfo.isEmpty()) {
                  LOGGER.debug("Found Disc Title in BDInfo.txt: " + bdinfo);
                  bdinfoTitle = bdinfo;
                }

                String imdb = ParserUtils.detectImdbId(txtFile);
                if (!imdb.isEmpty()) {
                  LOGGER.debug("Found IMDB id: " + imdb);
                  movie.setImdbId(imdb);
                }
              }
              catch (Exception e) {
                LOGGER.warn("couldn't read TXT " + mf.getFilename());
              }
            }
            else if (mf.getType().equals(MediaFileType.VIDEO)) {
              videoName = mf.getBasename();
            }
          }

          if (movie.getTitle().isEmpty()) {
            // get the "cleaner" name/year combo
            // ParserUtils.ParserInfo video = ParserUtils.getCleanerString(new String[] { videoName, movieDir.getName(), bdinfoTitle });
            // does not work reliable yet - user folder name
            String[] video = ParserUtils.detectCleanMovienameAndYear(movieDir.getName());
            movie.setTitle(video[0]);
            if (!video[1].isEmpty()) {
              movie.setYear(video[1]);
            }
          }

          // if the String 3D is in the movie dir, assume it is a 3D movie
          Matcher matcher = video3DPattern.matcher(movieDir.getName());
          if (matcher.find()) {
            movie.setVideoIn3D(true);
          }
          movie.setPath(movieDir.getPath());
          movie.setDataSource(dataSource);
          movie.setDateAdded(new Date());
          movie.setNewlyAdded(true);

          movie.findActorImages(); // TODO: find as MediaFiles
          LOGGER.debug("store movie into DB " + movie.getTitle());

          movieList.addMovie(movie);

          if (movie.getMovieSet() != null) {
            LOGGER.debug("movie is part of a movieset");
            // movie.getMovieSet().addMovie(movie);
            movie.getMovieSet().insertMovie(movie);
            movieList.sortMoviesInMovieSet(movie.getMovieSet());
            movie.getMovieSet().saveToDb();
          }
        } // end movie is null

        // second round - now add all the other known files
        addMediafilesToMovie(movie, mfs);

        // third round - try to match unknown graphics like title.ext or filename.ext as poster
        if (movie.getArtworkFilename(MediaFileType.POSTER).isEmpty()) {
          for (MediaFile mf : mfs) {
            if (mf.getType().equals(MediaFileType.GRAPHIC)) {
              LOGGER.debug("parsing unknown graphic " + mf.getFilename());
              List<MediaFile> vid = movie.getMediaFiles(MediaFileType.VIDEO);
              if (vid != null && !vid.isEmpty()) {
                String vfilename = vid.get(0).getFilename();
                if (FilenameUtils.getBaseName(vfilename).equals(FilenameUtils.getBaseName(mf.getFilename())) // basename match
                    || FilenameUtils.getBaseName(Utils.cleanStackingMarkers(vfilename)).trim().equals(FilenameUtils.getBaseName(mf.getFilename())) // basename
                                                                                                                                                   // w/o
                                                                                                                                                   // stacking
                    || movie.getTitle().equals(FilenameUtils.getBaseName(mf.getFilename()))) { // title match
                  mf.setType(MediaFileType.POSTER);
                  movie.addToMediaFiles(mf);
                }
              }
            }
          }
        }

        movie.reEvaluateStacking();
        movie.saveToDb();
      }
    }
    catch (NullPointerException e) {
      LOGGER.error("NPE:", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movieDir.getPath(), "message.update.errormoviedir"));
    }
    catch (Exception e) {
      LOGGER.error("error update Datasources", e);
      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, movieDir.getPath(), "message.update.errormoviedir", new String[] { ":", e.getLocalizedMessage() }));
    }
  }

  /**
   * searches for file type VIDEO and tries to detect the root movie directory
   * 
   * @param directory
   *          start dir
   * @param level
   *          the level how deep we are (level 0 = datasource root)
   * @return arraylist of absolute movie dirs
   */
  private ArrayList<File> getRootMovieDirs(File directory, int level) {
    ArrayList<File> ar = new ArrayList<>();

    // separate files & dirs
    ArrayList<File> files = new ArrayList<>();
    ArrayList<File> dirs = new ArrayList<>();
    File[] list = directory.listFiles();
    if (list == null) {
      LOGGER.error("Whops. Cannot access directory: " + directory);
      return ar;
    }
    for (File file : list) {
      if (file.isFile()) {
        files.add(file);
      }
      else {
        // ignore .folders, well known unwanted folders and configured skip folders
        if (!skipFolders.contains(file.getName().toUpperCase(Locale.ROOT)) && !file.getName().matches(skipFoldersRegex)
            && !MovieModuleManager.MOVIE_SETTINGS.getMovieSkipFolders().contains(file.getAbsolutePath())) {
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
        while (moviedir.getPath().toUpperCase(Locale.ROOT).contains("BDMV") || moviedir.getPath().toUpperCase(Locale.ROOT).contains("VIDEO_TS")) {
          disc = true;
          moviedir = moviedir.getParentFile();
        }
        if (disc) {
          ar.add(moviedir);
          continue; // proceed with next file
        }

        // ok, regular structure
        if (dirs.isEmpty() && level > 1 && Utils.cleanFolderStackingMarkers(moviedir.getName()).isEmpty()) {
          // no more dirs in that directory and at least 2 levels deep
          // stacking folder found (solely with stacking name!!!)
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

  /*
   * recursively gets all MediaFiles from a moviedir
   */
  private ArrayList<MediaFile> getAllMediaFilesRecursive(File dir) {
    ArrayList<MediaFile> mv = new ArrayList<>();

    File[] list = dir.listFiles();
    if (list == null) {
      LOGGER.error("Whops. Cannot access directory: " + dir.getName());
      return mv;
    }

    for (File file : list) {
      if (file.isFile()) {
        if (!file.getName().startsWith("._")) { // MacOS ignore
          mv.add(new MediaFile(file));
          // store dir for faster cleanup
          synchronized (filesFound) {
            filesFound.add(file);
          }
        }
      }
      else {
        // ignore .folders, well known unwanted folders and configured skip folders
        if (!skipFolders.contains(file.getName().toUpperCase(Locale.ROOT)) && !file.getName().matches(skipFoldersRegex)
            && !MovieModuleManager.MOVIE_SETTINGS.getMovieSkipFolders().contains(file.getAbsolutePath())) {
          mv.addAll(getAllMediaFilesRecursive(file));
        }
      }
    }

    return mv;
  }

  private void addMediafilesToMovie(Movie movie, List<MediaFile> mediaFiles) {
    List<MediaFile> current = new ArrayList<>(movie.getMediaFiles());

    for (MediaFile mf : mediaFiles) {
      if (!current.contains(mf)) { // a new mediafile was found!
        if (mf.getPath().toUpperCase(Locale.ROOT).contains("BDMV") || mf.getPath().toUpperCase(Locale.ROOT).contains("VIDEO_TS") || mf.isDiscFile()) {
          movie.setDisc(true);
          if (movie.getMediaSource() == MediaSource.UNKNOWN) {
            movie.setMediaSource(MediaSource.parseMediaSource(mf.getPath()));
          }
        }

        if (!Utils.isValidImdbId(movie.getImdbId())) {
          movie.setImdbId(ParserUtils.detectImdbId(mf.getFile().getAbsolutePath()));
        }

        LOGGER.debug("parsing " + mf.getType().name() + " " + mf.getFilename());
        switch (mf.getType()) {
          case VIDEO:
            movie.addToMediaFiles(mf);
            movie.setDateAddedFromMediaFile(mf);
            if (movie.getMediaSource() == MediaSource.UNKNOWN) {
              movie.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
            }
            break;

          case TRAILER:
            mf.gatherMediaInformation(); // do this exceptionally here, to set quality in one rush
            MovieTrailer mt = new MovieTrailer();
            mt.setName(mf.getFilename());
            mt.setProvider("downloaded");
            mt.setQuality(mf.getVideoFormat());
            mt.setInNfo(false);
            mt.setUrl(mf.getFile().toURI().toString());
            movie.addTrailer(mt);
            movie.addToMediaFiles(mf);
            break;

          case SUBTITLE:
            if (!mf.isPacked()) {
              movie.setSubtitles(true);
              movie.addToMediaFiles(mf);
            }
            break;

          case FANART:
            if (mf.getPath().toLowerCase(Locale.ROOT).contains("extrafanart")) {
              // there shouldn't be any files here
              LOGGER.warn("problem: detected media file type FANART in extrafanart folder: " + mf.getPath());
              continue;
            }
            movie.addToMediaFiles(mf);
            break;

          case THUMB:
            if (mf.getPath().toLowerCase(Locale.ROOT).contains("extrathumbs")) { //
              // there shouldn't be any files here
              LOGGER.warn("problem: detected media file type THUMB in extrathumbs folder: " + mf.getPath());
              continue;
            }
            movie.addToMediaFiles(mf);
            break;

          // just add them 1:1, without special handling
          case VIDEO_EXTRA:
          case SAMPLE:
          case NFO:
          case TEXT:
          case POSTER:
          case SEASON_POSTER:
          case EXTRAFANART:
          case EXTRATHUMB:
          case AUDIO:
          case DISCART:
          case BANNER:
          case CLEARART:
          case LOGO:
          case CLEARLOGO:
            movie.addToMediaFiles(mf);
            break;

          case GRAPHIC:
          case UNKNOWN:
          default:
            LOGGER.debug("NOT adding unknown media file type: " + mf.getFilename());
            // movie.addToMediaFiles(mf); // DO NOT ADD UNKNOWN
            break;
        } // end switch type

        // debug
        if (mf.getType() != MediaFileType.GRAPHIC && mf.getType() != MediaFileType.UNKNOWN && mf.getType() != MediaFileType.NFO
            && !movie.getMediaFiles().contains(mf)) {
          LOGGER.error("Movie not added mf: " + mf.getFile().getPath());
        }

      } // end new MF found
    } // end MF loop
  }

  /*
   * cleanup database - remove orphaned movies/files
   */
  private void cleanup(String datasource) {
    setTaskName(BUNDLE.getString("update.cleanup"));
    setTaskDescription(null);
    setProgressDone(0);
    setWorkUnits(0);
    publishState();

    LOGGER.info("removing orphaned movies/files...");
    List<Movie> moviesToRemove = new ArrayList<>();
    for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      Movie movie = movieList.getMovies().get(i);

      // check only movies matching datasource
      if (!new File(datasource).equals(new File(movie.getDataSource()))) {
        continue;
      }

      File movieDir = new File(movie.getPath());
      if (!filesFound.contains(movieDir)) {
        // dir is not in hashset - check with exists to be sure it is not here
        if (!movieDir.exists()) {
          LOGGER.debug("movie directory '" + movieDir + "' not found, removing...");
          moviesToRemove.add(movie);
        }
        else {
          LOGGER.warn("dir " + movie.getPath() + " not in hashset, but on hdd!");
        }
      }
      else {
        // have a look if that movie has just been added -> so we don't need any cleanup
        if (!movie.isNewlyAdded()) {
          // check and delete all not found MediaFiles
          List<MediaFile> mediaFiles = new ArrayList<>(movie.getMediaFiles());
          for (MediaFile mf : mediaFiles) {
            if (!filesFound.contains(mf.getFile())) {
              if (!mf.exists()) {
                LOGGER.debug("removing orphaned file: " + mf.getPath() + File.separator + mf.getFilename());
                movie.removeFromMediaFiles(mf);
              }
              else {
                LOGGER.warn("file " + mf.getFile().getAbsolutePath() + " not in hashset, but on hdd!");
              }
            }
          }
          movie.saveToDb();
        }
      }
    }
    movieList.removeMovies(moviesToRemove);
  }

  /*
   * gather mediainfo for ungathered movies
   */
  private void gatherMediainfo(String datasource) {
    // start MI
    setTaskName(BUNDLE.getString("update.mediainfo"));
    publishState();

    initThreadPool(1, "mediainfo");

    LOGGER.info("getting Mediainfo...");
    for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      Movie movie = movieList.getMovies().get(i);

      // check only movies matching datasource
      if (!new File(datasource).equals(new File(movie.getDataSource()))) {
        continue;
      }

      ArrayList<MediaFile> ungatheredMediaFiles = new ArrayList<>();
      for (MediaFile mf : new ArrayList<>(movie.getMediaFiles())) {
        if (StringUtils.isBlank(mf.getContainerFormat())) {
          ungatheredMediaFiles.add(mf);
        }
      }

      if (ungatheredMediaFiles.size() > 0) {
        submitTask(new MediaFileInformationFetcherTask(ungatheredMediaFiles, movie, false));
      }
    }
  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the text is never right
    publishState(progressDone);
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
      // are we parsing the DS root?
      if (subdir.equals(new File(datasource))) {
        // just parse, no recursive scanning!
        LOGGER.debug("Parsing dataSource root folder: " + subdir);
        parseMovieDirectory(subdir, datasource);
      }
      else {
        // find all possible movie folders recursive
        ArrayList<File> mov = getRootMovieDirs(subdir, 1);

        // remove dupe movie dirs
        HashSet<File> h = new HashSet<>(mov);
        mov.clear();
        mov.addAll(h);
        for (File movieDir : mov) {
          // check if multiple movies or a single one
          parseMovieDirectory(movieDir, datasource);
        }
      }

      // return first level folder name... uhm. yeah
      return subdir.getName();
    }
  }
}
