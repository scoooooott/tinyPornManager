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
package org.tinymediamanager.core.movie.tasks;

import static java.nio.file.FileVisitResult.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCacheTask;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaSource;
import org.tinymediamanager.core.movie.MovieModuleManager;
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

public class MovieUpdateDatasourceTask2 extends TmmThreadPool {
  private static final Logger         LOGGER           = LoggerFactory.getLogger(MovieUpdateDatasourceTask2.class);
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());                                  //$NON-NLS-1$

  private static long                 preDir           = 0;
  private static long                 postDir          = 0;
  private static long                 visFile          = 0;
  private static long                 preDir2          = 0;
  private static long                 postDir2         = 0;
  private static long                 visFile2         = 0;

  // skip well-known, but unneeded folders (UPPERCASE)
  private static final List<String>   skipFolders      = Arrays.asList(".", "..", "CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "SSIF", "AUXDATA",
      "AUDIO_TS", "$RECYCLE.BIN", "RECYCLER", "SYSTEM VOLUME INFORMATION", "@EADIR");

  // skip folders starting with a SINGLE "." or "._"
  private static final String         skipFoldersRegex = "^[.][\\w@]+.*";
  private static Pattern              video3DPattern   = Pattern.compile("(?i)[ ._\\(\\[-]3D[ ._\\)\\]-]?");

  private List<String>                dataSources;
  private MovieList                   movieList;
  private HashSet<Path>               filesFound       = new HashSet<Path>();

  public MovieUpdateDatasourceTask2() {
    super(BUNDLE.getString("update.datasource"));
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource());
  }

  public MovieUpdateDatasourceTask2(String datasource) {
    super(BUNDLE.getString("update.datasource") + " (" + datasource + ")");
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(1);
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

    try {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      List<Path> imageFiles = new ArrayList<Path>();

      // cleanup newlyadded for a new UDS run
      for (Movie movie : movieList.getMovies()) {
        movie.setNewlyAdded(false);
      }

      for (String ds : dataSources) {
        initThreadPool(3, "update");
        setTaskName(BUNDLE.getString("update.datasource") + " '" + ds + "'");
        publishState();

        // ***************************************************************

        getAllFilesRecursiveAndParse(Paths.get(ds), Integer.MAX_VALUE);
        // filesFound.addAll(allFiles); // global cache

        // System.out.println("Files found: " + allFiles.size());
        // System.out.println("took " + stopWatch);

        // ***************************************************************

        // Quick filtering
        // all VIDEO files (inkl. sample, extras)
        // but no unneeded MF creation - quite fast
        // just to get the "interesting" dirs, where we have some files :)
        // HashSet<Path> movieDirs = new HashSet<Path>();
        // for (Path path : allFiles) {
        // if (Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(path.toString()).toLowerCase())) {
        // if (!movieDirs.contains(path.getParent())) {
        // movieDirs.add(path.getParent()); // just the folder
        // submitTask(new FindMovieTask(path.getParent(), ds));
        // }
        // // System.out.println(path.getNameCount() + " - " + path);
        // }
        // }
        // System.out.println();
        // allFiles.clear(); // not needed anylonger

        // FIXME: what if, if a single movie in a dir, contains 2 level deeper another movie
        // evaluate every dir, if some others are in a subfolder

        // FIXME: folder stacking? movie/name CD1/VIDEO_TS
        // FIXME: folder stacking? movie/name CD2/VIDEO_TS
        // FIXME: folder stacking? movie CD1/VIDEO_TS
        // FIXME: folder stacking? movie CD2/VIDEO_TS

        // now we have all DIRECTORIES containing movie files - lets parse them
        // for (Path path : movieDirs) {
        // submitTask(new FindMovieTask(path, ds));
        // }
        waitForCompletionOrCancel();

        if (cancel) {
          break;
        }

        // cleanup
        cleanup(ds);

        // mediainfo
        gatherMediainfo(ds);

        waitForCompletionOrCancel();
        if (cancel) {
          break;
        }

        // build image cache on import
        if (MovieModuleManager.MOVIE_SETTINGS.isBuildImageCacheOnImport()) {
          for (Movie movie : movieList.getMovies()) {
            if (!Paths.get(ds).equals(Paths.get(movie.getDataSource()))) {
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
      System.out.println("FilesFound " + filesFound.size());
      System.out.println("moviesFound " + movieList.getMovieCount());
      System.out.println("PreDir " + preDir);
      System.out.println("PostDir " + postDir);
      System.out.println("VisFile " + visFile);
      System.out.println("PreDir2 " + preDir2);
      System.out.println("PostDir2 " + postDir2);
      System.out.println("VisFile2 " + visFile2);
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
  }

  /**
   * ThreadpoolWorker to work off ONE possible movie from root datasource directory
   * 
   * @author Myron Boyle
   * @version 1.0
   */
  private class FindMovieTask implements Callable<Object> {

    private Path   subdir     = null;
    private String datasource = "";

    public FindMovieTask(Path subdir, String datasource) {
      this.subdir = subdir;
      this.datasource = datasource;
    }

    @Override
    public String call() throws Exception {
      parseMovieDirectory(subdir, datasource);
      return subdir.toFile().toString();
    }
  }

  private void parseMovieDirectory(Path movieDir, String dataSource) {
    List<Path> movieDirList = listFilesAndDirs(movieDir);
    HashSet<Path> files = new HashSet<Path>();
    HashSet<Path> dirs = new HashSet<Path>(); // FIXME: what for....?
    HashSet<String> normalizedVideoFiles = new HashSet<String>(); // just for identifying MMD

    boolean isDiscFolder = false;
    boolean isMultiMovieDir = false;
    boolean videoFileFound = false;
    Path movieRoot = movieDir; // root set to current dir - might be adjusted by disc folders

    for (Path path : movieDirList) {
      if (Files.isRegularFile(path)) {
        files.add(path.toAbsolutePath());

        // do not construct a fully MF yet
        // just minimal to get the type out of filename
        MediaFile mf = new MediaFile();
        mf.setPath(path.getParent().toString());
        mf.setFilename(path.getFileName().toString());
        mf.setType(mf.parseType());

        // System.out.println("************ " + mf);
        if (mf.getType() == MediaFileType.VIDEO) {
          videoFileFound = true;
          if (mf.isDiscFile()) {
            isDiscFolder = true;
            break; // step out - this is all we need to know
          }
          else {
            // detect unique basename, without stacking etc
            String[] ty = ParserUtils.detectCleanMovienameAndYear(FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mf.getFilename())));
            normalizedVideoFiles.add(ty[0] + ty[1]);
          }
        }
      }
      else if (Files.isDirectory(path)) {
        dirs.add(path.toAbsolutePath());
      }
    }

    if (!videoFileFound) {
      // hmm... we never found a video file (but maybe others, trailers) so no need to parse THIS folder
      return;
    }

    if (isDiscFolder) {
      // if inside own DiscFolder, set movieRoot to parent, else use movieDir as movieRoot
      String parent = movieDir.getFileName().toString();
      if (parent.equalsIgnoreCase("BDMV") || parent.equalsIgnoreCase("VIDEO_TS")) {
        movieRoot = movieDir.getParent();
      }
    }
    else {
      // no VIDEO files in this dir - skip this folder
      if (normalizedVideoFiles.size() == 0) {
        return;
      }
      // more than one (unstacked) movie file in directory (or DS root) -> must parsed as multiMovieDir
      if (normalizedVideoFiles.size() > 1 || movieDir.equals(Paths.get(dataSource))) {
        isMultiMovieDir = true;
      }
    }

    // ok, we're ready to parse :)
    if (isMultiMovieDir) {
      createMultiMovieFromDir(dataSource, movieRoot);
    }
    else {
      createSingleMovieFromDir(dataSource, movieRoot, isDiscFolder);
    }

  }

  /**
   * for SingleMovie or DiscFolders
   * 
   * @param movieDir
   */
  private void createSingleMovieFromDir(String dataSource, Path movieDir, boolean isDiscFolder) {
    LOGGER.info("Parsing single movie directory: " + movieDir + " (are we a disc folder? " + isDiscFolder + ")");

    Movie movie = movieList.getMovieByPath(movieDir);
    HashSet<Path> allFiles = getAllFilesRecursive(movieDir, 2); // TODO: just this and next level? TBD!
    filesFound.add(movieDir.toAbsolutePath()); // our global cache
    filesFound.addAll(allFiles); // our global cache

    // convert to MFs (we need it anyways at the end)
    ArrayList<MediaFile> mfs = new ArrayList<MediaFile>();
    for (Path file : allFiles) {
      mfs.add(new MediaFile(file));
    }
    allFiles.clear();

    if (movie == null) {
      LOGGER.debug("| movie not found; looking for NFOs");
      movie = new Movie();
      String bdinfoTitle = ""; // title parsed out of BDInfo
      String videoName = ""; // title from file

      // ***************************************************************
      // first round - try to parse NFO(s) first
      // ***************************************************************
      // TODO: add movie.addMissingMetaData(otherMovie) to get merged movie from multiple NFOs ;)
      for (MediaFile mf : mfs) {

        if (mf.getType().equals(MediaFileType.NFO)) {
          // PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.[nN][fF][oO]");
          LOGGER.info("| parsing NFO " + mf.getFileAsPath());
          Movie nfo = null;
          switch (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector()) {
            case XBMC:
              nfo = MovieToXbmcNfoConnector.getData(mf.getFileAsPath());
              if (nfo == null) {
                // try the other
                nfo = MovieToMpNfoConnector.getData(mf.getFileAsPath());
              }
              break;
            case MP:
              nfo = MovieToMpNfoConnector.getData(mf.getFileAsPath());
              if (nfo == null) {
                // try the other
                nfo = MovieToXbmcNfoConnector.getData(mf.getFileAsPath());
              }
              break;
          }
          if (nfo != null) {
            movie = nfo;
          }
          // was NFO, but parsing exception. try to find at least imdb id within
          if (movie.getImdbId().isEmpty()) {
            try {
              String imdb = Utils.readFileToString(mf.getFileAsPath());
              imdb = ParserUtils.detectImdbId(imdb);
              if (!imdb.isEmpty()) {
                LOGGER.debug("| Found IMDB id: " + imdb);
                movie.setImdbId(imdb);
              }
            }
            catch (IOException e) {
              LOGGER.warn("| couldn't read NFO " + mf);
            }
          }

        } // end NFO
        else if (mf.getType().equals(MediaFileType.TEXT)) {
          try {
            String txtFile = Utils.readFileToString(mf.getFileAsPath());

            String bdinfo = StrgUtils.substr(txtFile, ".*Disc Title:\\s+(.*?)[\\n\\r]");
            if (!bdinfo.isEmpty()) {
              LOGGER.debug("| Found Disc Title in BDInfo.txt: " + bdinfo);
              bdinfoTitle = WordUtils.capitalizeFully(bdinfo);
            }

            String imdb = ParserUtils.detectImdbId(txtFile);
            if (!imdb.isEmpty()) {
              LOGGER.debug("| Found IMDB id: " + imdb);
              movie.setImdbId(imdb);
            }
          }
          catch (Exception e) {
            LOGGER.warn("| couldn't read TXT " + mf.getFilename());
          }
        }
        else if (mf.getType().equals(MediaFileType.VIDEO)) {
          videoName = mf.getBasename();
        }
      } // end NFO MF loop
    } // end first round - we might have a filled movie

    if (movie.getTitle().isEmpty()) {
      // get the "cleaner" name/year combo
      // ParserUtils.ParserInfo video = ParserUtils.getCleanerString(new String[] { videoName, movieDir.getName(), bdinfoTitle });
      // does not work reliable yet - user folder name
      String[] video = ParserUtils.detectCleanMovienameAndYear(movieDir.getFileName().toString());
      movie.setTitle(video[0]);
      if (!video[1].isEmpty()) {
        movie.setYear(video[1]);
      }
    }

    // if the String 3D is in the movie dir, assume it is a 3D movie
    Matcher matcher = video3DPattern.matcher(movieDir.getFileName().toString());
    if (matcher.find()) {
      movie.setVideoIn3D(true);
    }
    movie.setPath(movieDir.toAbsolutePath().toString());
    movie.setDataSource(dataSource);
    movie.setDateAdded(new Date());
    movie.setNewlyAdded(true);

    movie.findActorImages(); // TODO: find as MediaFiles
    LOGGER.debug("| store movie into DB as: " + movie.getTitle());

    movieList.addMovie(movie);

    if (movie.getMovieSet() != null) {
      LOGGER.debug("| movie is part of a movieset");
      // movie.getMovieSet().addMovie(movie);
      movie.getMovieSet().insertMovie(movie);
      movieList.sortMoviesInMovieSet(movie.getMovieSet());
      movie.getMovieSet().saveToDb();
    }

    // ***************************************************************
    // second round - now add all the other known files
    // ***************************************************************
    addMediafilesToMovie(movie, mfs);

    // ***************************************************************
    // third round - try to match unknown graphics like title.ext or filename.ext as poster
    // ***************************************************************
    if (movie.getArtworkFilename(MediaFileType.POSTER).isEmpty()) {
      for (MediaFile mf : mfs) {
        if (mf.getType().equals(MediaFileType.GRAPHIC)) {
          LOGGER.debug("| parsing unknown graphic " + mf.getFilename());
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

  /**
   * more than one movie in dir? Then use that!
   * 
   * @param movieDir
   */
  private void createMultiMovieFromDir(String dataSource, Path movieDir) {
    LOGGER.info("Parsing multi  movie directory: " + movieDir); // double space is for log alignment ;)

    List<Movie> movies = movieList.getMoviesByPath(movieDir);

    List<Path> allFiles = listFilesOnly(movieDir);
    filesFound.add(movieDir); // our global cache
    filesFound.addAll(allFiles); // our global cache

    // convert to MFs
    ArrayList<MediaFile> mfs = new ArrayList<MediaFile>();
    for (Path file : allFiles) {
      mfs.add(new MediaFile(file));
    }
    // allFiles.clear(); // might come handy

    // just compare filename length, start with longest b/c of overlapping names
    Collections.sort(mfs, new Comparator<MediaFile>() {
      @Override
      public int compare(MediaFile file1, MediaFile file2) {
        return file2.getFileAsPath().getFileName().toString().length() - file1.getFileAsPath().getFileName().toString().length();
      }
    });

    for (MediaFile mf : mfs) {

      Movie movie = null;
      String basename = FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mf.getFilename()));

      // 1) check if MF is already assigned to a movie within path
      for (Movie m : movies) {
        if (m.getMediaFiles(MediaFileType.VIDEO).contains(mf)) {
          // ok, our MF is already in an movie
          LOGGER.debug("| found movie '" + m.getTitle() + "' from MediaFile " + mf);
          movie = m;
          break;
        }
        for (MediaFile mfile : m.getMediaFiles(MediaFileType.VIDEO)) {
          // try to match like if we would create a new movie
          String[] mfileTY = ParserUtils.detectCleanMovienameAndYear(FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mfile.getFilename())));
          String[] mfTY = ParserUtils.detectCleanMovienameAndYear(FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mf.getFilename())));
          if (mfileTY[0].equals(mfTY[0]) && mfileTY[1].equals(mfTY[1])) { // title AND year (even empty) match
            LOGGER.debug("| found possible movie '" + m.getTitle() + "' from filename " + mf);
            movie = m;
            break;
          }
        }
      }

      if (movie == null) {
        // 2) create if not found
        // check for NFO
        Path nfoFile = movieDir.resolve(basename + ".nfo");
        if (allFiles.contains(nfoFile)) {
          MediaFile nfo = new MediaFile(nfoFile, MediaFileType.NFO);
          // from NFO?
          LOGGER.debug("| found NFO '" + nfo + "' - try to parse");
          switch (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector()) {
            case XBMC:
              movie = MovieToXbmcNfoConnector.getData(nfo.getFileAsPath());
              if (movie == null) {
                // try the other
                movie = MovieToMpNfoConnector.getData(nfo.getFileAsPath());
              }
              break;
            case MP:
              movie = MovieToMpNfoConnector.getData(nfo.getFileAsPath());
              if (movie == null) {
                // try the other
                movie = MovieToXbmcNfoConnector.getData(nfo.getFileAsPath());
              }
              break;
          }
          if (movie != null) {
            // valid NFO found, so add itself as MF
            LOGGER.debug("| NFO valid - add it");
            movie.addToMediaFiles(nfo);
          }
        }
        if (movie == null) {
          // still NULL, create new movie movie from file
          LOGGER.debug("| Create new movie from file: " + mf);
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
        movie.setDataSource(dataSource);
        movie.setNewlyAdded(true);
        movie.setPath(mf.getPath());

        movieList.addMovie(movie);
        movies.add(movie); // add to our cached copy
      }

      if (!Utils.isValidImdbId(movie.getImdbId())) {
        movie.setImdbId(ParserUtils.detectImdbId(mf.getFileAsPath().toString()));
      }
      if (movie.getMediaSource() == MovieMediaSource.UNKNOWN) {
        movie.setMediaSource(MovieMediaSource.parseMediaSource(mf.getFileAsPath().toString()));
      }
      LOGGER.debug("| parsing video file " + mf.getFilename());
      movie.addToMediaFiles(mf);
      movie.setDateAddedFromMediaFile(mf);
      movie.setMultiMovieDir(true);

      // 3) find additional files, which start with videoFileName
      List<MediaFile> existingMediaFiles = new ArrayList<MediaFile>(movie.getMediaFiles());
      List<MediaFile> foundMediaFiles = new ArrayList<MediaFile>();
      for (int i = allFiles.size() - 1; i >= 0; i--) {
        Path fileInDir = allFiles.get(i);
        if (fileInDir.getFileName().toString().startsWith(basename)) { // need toString b/c of possible spcaes!!
          MediaFile mediaFile = new MediaFile(fileInDir);
          if (!existingMediaFiles.contains(mediaFile)) {
            foundMediaFiles.add(mediaFile);
          }
          // started with basename, so remove it for others
          allFiles.remove(i);
        }
      }
      addMediafilesToMovie(movie, foundMediaFiles);

      if (movie.getMovieSet() != null) {
        LOGGER.debug("| movie is part of a movieset");
        // movie.getMovieSet().addMovie(movie);
        movie.getMovieSet().insertMovie(movie);
        movieList.sortMoviesInMovieSet(movie.getMovieSet());
        movie.getMovieSet().saveToDb();
      }

      movie.saveToDb();
    } // end foreach MF

    // check stacking on all movie from this dir (it might have changed!)
    for (Movie m : movieList.getMoviesByPath(movieDir)) {
      m.reEvaluateStacking();
      m.saveToDb();
    }
  }

  private void addMediafilesToMovie(Movie movie, List<MediaFile> mediaFiles) {
    List<MediaFile> current = new ArrayList<MediaFile>(movie.getMediaFiles());

    for (MediaFile mf : mediaFiles) {
      if (!current.contains(mf)) { // a new mediafile was found!
        if (mf.getPath().toUpperCase().contains("BDMV") || mf.getPath().toUpperCase().contains("VIDEO_TS") || mf.isDiscFile()) {
          movie.setDisc(true);
          if (movie.getMediaSource() == MovieMediaSource.UNKNOWN) {
            movie.setMediaSource(MovieMediaSource.parseMediaSource(mf.getPath()));
          }
        }

        if (!Utils.isValidImdbId(movie.getImdbId())) {
          movie.setImdbId(ParserUtils.detectImdbId(mf.getFileAsPath().toString()));
        }

        LOGGER.debug("| parsing " + mf.getType().name() + " " + mf.getFilename());
        switch (mf.getType()) {
          case VIDEO:
            movie.addToMediaFiles(mf);
            movie.setDateAddedFromMediaFile(mf);
            if (movie.getMediaSource() == MovieMediaSource.UNKNOWN) {
              movie.setMediaSource(MovieMediaSource.parseMediaSource(mf.getFileAsPath().toString()));
            }
            break;

          case TRAILER:
            mf.gatherMediaInformation(); // do this exceptionally here, to set quality in one rush
            MovieTrailer mt = new MovieTrailer();
            mt.setName(mf.getFilename());
            mt.setProvider("downloaded");
            mt.setQuality(mf.getVideoFormat());
            mt.setInNfo(false);
            mt.setUrl(mf.getFileAsPath().toUri().toString());
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
            if (mf.getPath().toLowerCase().contains("extrafanart")) {
              // there shouldn't be any files here
              LOGGER.warn("problem: detected media file type FANART in extrafanart folder: " + mf.getPath());
              continue;
            }
            movie.addToMediaFiles(mf);
            break;

          case THUMB:
            if (mf.getPath().toLowerCase().contains("extrathumbs")) { //
              // there shouldn't be any files here
              LOGGER.warn("| problem: detected media file type THUMB in extrathumbs folder: " + mf.getPath());
              continue;
            }
            movie.addToMediaFiles(mf);
            break;

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
            movie.addToMediaFiles(mf);
            break;

          case GRAPHIC:
          case UNKNOWN:
          default:
            LOGGER.debug("| NOT adding unknown media file type: " + mf.getFilename());
            // movie.addToMediaFiles(mf); // DO NOT ADD UNKNOWN
            break;
        } // end switch type

        // debug
        if (mf.getType() != MediaFileType.GRAPHIC && mf.getType() != MediaFileType.UNKNOWN && mf.getType() != MediaFileType.NFO
            && !movie.getMediaFiles().contains(mf)) {
          LOGGER.error("| Movie not added mf: " + mf.getFileAsPath());
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
    List<Movie> moviesToRemove = new ArrayList<Movie>();
    for (int i = movieList.getMovies().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      Movie movie = movieList.getMovies().get(i);

      // check only movies matching datasource
      if (!Paths.get(datasource).equals(Paths.get(movie.getDataSource()))) {
        continue;
      }

      Path movieDir = movie.getPathNIO();
      if (!filesFound.contains(movieDir)) {
        // dir is not in hashset - check with exists to be sure it is not here
        if (Files.notExists(movieDir)) {
          LOGGER.debug("movie directory '" + movieDir + "' not found, removing...");
          moviesToRemove.add(movie);
        }
        else {
          LOGGER.warn("dir " + movieDir + " not in hashset, but on hdd!");
        }
      }
      else {
        // have a look if that movie has just been added -> so we don't need any cleanup
        if (!movie.isNewlyAdded()) {
          // check and delete all not found MediaFiles
          List<MediaFile> mediaFiles = new ArrayList<MediaFile>(movie.getMediaFiles());
          for (MediaFile mf : mediaFiles) {
            if (!filesFound.contains(mf.getFileAsPath())) {
              if (!mf.exists()) {
                LOGGER.debug("removing orphaned file: " + mf.getFileAsPath());
                movie.removeFromMediaFiles(mf);
              }
              else {
                LOGGER.warn("file " + mf.getFileAsPath() + " not in hashset, but on hdd!");
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
      if (!Paths.get(datasource).equals(Paths.get(movie.getDataSource()))) {
        continue;
      }

      ArrayList<MediaFile> ungatheredMediaFiles = new ArrayList<MediaFile>();
      for (MediaFile mf : new ArrayList<MediaFile>(movie.getMediaFiles())) {
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
   * simple NIO File.listFiles() replacement<br>
   * returns ONLY regular files (NO folders, NO hidden) in specified dir (NOT recursive)
   * 
   * @param directory
   * @return list of files&folders
   */
  public static List<Path> listFilesOnly(Path directory) {
    List<Path> fileNames = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
      for (Path path : directoryStream) {
        if (Files.isRegularFile(path)) {
          fileNames.add(path);
        }
      }
    }
    catch (IOException ex) {
    }
    return fileNames;
  }

  /**
   * simple NIO File.listFiles() replacement<br>
   * returns all files & folders in specified dir (NOT recursive)
   * 
   * @param directory
   * @return list of files&folders
   */
  public static List<Path> listFilesAndDirs(Path directory) {
    List<Path> fileNames = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
      for (Path path : directoryStream) {
        fileNames.add(path);
      }
    }
    catch (IOException ex) {
    }
    return fileNames;
  }

  // **************************************
  // gets all files recursive,
  // **************************************
  public static HashSet<Path> getAllFilesRecursive(Path folder, int deep) {
    AllFilesRecursive visitor = new AllFilesRecursive();
    try {
      Files.walkFileTree(folder, EnumSet.noneOf(FileVisitOption.class), deep, visitor);
    }
    catch (IOException e) {
      // can not happen, since we overrided visitFileFailed, which throws no exception ;)
    }
    return visitor.fFound;
  }

  private static class AllFilesRecursive extends SimpleFileVisitor<Path> {
    private HashSet<Path> fFound = new HashSet<Path>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
      visFile2++;
      if (attr.isRegularFile()) {
        fFound.add(file.toAbsolutePath());
      }
      // System.out.println("(" + attr.size() + "bytes)");
      // System.out.println("(" + attr.creationTime() + " date)");
      return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      preDir2++;
      // getFilename returns null on DS root!
      if (dir.getFileName() != null && (Files.exists(dir.resolve(".tmmignore")) || Files.exists(dir.resolve("tmmignore"))
          || skipFolders.contains(dir.getFileName().toString().toUpperCase()) || dir.getFileName().toString().matches(skipFoldersRegex))) {
        LOGGER.debug("Skipping dir: " + dir);
        return SKIP_SUBTREE;
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      postDir2++;
      return CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      System.err.println(exc);
      return CONTINUE;
    }
  }

  // **************************************
  // gets all files recursive,
  // and starts parsing directory immediately
  // **************************************
  public void getAllFilesRecursiveAndParse(Path folder, int deep) {
    AllFilesRecursiveWithParse visitor = new AllFilesRecursiveWithParse(folder);
    try {
      Files.walkFileTree(folder, EnumSet.noneOf(FileVisitOption.class), deep, visitor);
    }
    catch (IOException e) {
      // can not happen, since we overrided visitFileFailed, which throws no exception ;)
    }
  }

  private class AllFilesRecursiveWithParse extends SimpleFileVisitor<Path> {
    private String  dataSource = "";
    private boolean filesFound = false;

    public AllFilesRecursiveWithParse(Path dataSource) {
      this.dataSource = dataSource.toAbsolutePath().toString();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
      visFile++;
      if (attr.isRegularFile()) {
        // check for video?
        if (Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.toString()).toLowerCase())) {
          this.filesFound = true;
        }
      }
      // System.out.println("(" + attr.size() + "bytes)");
      // System.out.println("(" + attr.creationTime() + " date)");
      return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      preDir++;
      // getFilename returns null on DS root!
      if (dir.getFileName() != null && (Files.exists(dir.resolve(".tmmignore")) || Files.exists(dir.resolve("tmmignore"))
          || skipFolders.contains(dir.getFileName().toString().toUpperCase()) || dir.getFileName().toString().matches(skipFoldersRegex))) {
        LOGGER.debug("Skipping dir: " + dir);
        return SKIP_SUBTREE;
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      postDir++;
      if (this.filesFound == true) {
        submitTask(new FindMovieTask(dir, dataSource));
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      System.err.println(exc);
      return CONTINUE;
    }
  }
}
