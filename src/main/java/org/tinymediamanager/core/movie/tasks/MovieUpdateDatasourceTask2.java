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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieEdition;
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
import org.tinymediamanager.thirdparty.VSMeta;
import org.tinymediamanager.ui.UTF8Control;

import com.sun.jna.Platform;

/**
 * The Class UpdateDataSourcesTask.
 * 
 * @author Myron Boyle
 */
public class MovieUpdateDatasourceTask2 extends TmmThreadPool {
  private static final Logger         LOGGER         = LoggerFactory.getLogger(MovieUpdateDatasourceTask2.class);
  private static final ResourceBundle BUNDLE         = ResourceBundle.getBundle("messages", new UTF8Control());                                  //$NON-NLS-1$

  private static long                 preDir         = 0;
  private static long                 postDir        = 0;
  private static long                 visFile        = 0;
  private static long                 preDirAll      = 0;
  private static long                 postDirAll     = 0;
  private static long                 visFileAll     = 0;

  // skip well-known, but unneeded folders (UPPERCASE)
  private static final List<String>   skipFolders    = Arrays.asList(".", "..", "CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "SSIF", "AUXDATA",
      "AUDIO_TS", "JAR", "$RECYCLE.BIN", "RECYCLER", "SYSTEM VOLUME INFORMATION", "@EADIR");

  // skip folders starting with a SINGLE "." or "._"
  private static final String         skipRegex      = "^[.][\\w@]+.*";
  private static Pattern              video3DPattern = Pattern.compile("(?i)[ ._\\(\\[-]3D[ ._\\)\\]-]?");

  private List<String>                dataSources;
  private List<Movie>                 movieFolders   = new ArrayList<>();
  private MovieList                   movieList;
  private HashSet<Path>               filesFound     = new HashSet<>();

  public MovieUpdateDatasourceTask2() {
    super(BUNDLE.getString("update.datasource"));
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<>(MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource());
  }

  public MovieUpdateDatasourceTask2(String datasource) {
    super(BUNDLE.getString("update.datasource") + " (" + datasource + ")");
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<>(1);
    dataSources.add(datasource);
  }

  public MovieUpdateDatasourceTask2(List<Movie> movies) {
    super(BUNDLE.getString("update.datasource"));
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<>(0);
    movieFolders.addAll(movies);
  }

  @Override
  public void doInBackground() {
    // check if there is at least one DS to update
    Utils.removeEmptyStringsFromList(dataSources);
    if (dataSources.isEmpty() && movieFolders.isEmpty()) {
      LOGGER.info("no datasource to update");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.nonespecified"));
      return;
    }
    preDir = 0;
    postDir = 0;
    visFile = 0;
    preDirAll = 0;
    postDirAll = 0;
    visFileAll = 0;

    // get existing movie folders
    List<Path> existing = new ArrayList<>();
    for (Movie movie : movieList.getMovies()) {
      existing.add(movie.getPathNIO());
    }

    try {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      List<Path> imageFiles = new ArrayList<>();

      if (movieFolders.isEmpty()) {
        for (String ds : dataSources) {
          LOGGER.info("Start UDS on datasource: " + ds);
          initThreadPool(3, "update");
          setTaskName(BUNDLE.getString("update.datasource") + " '" + ds + "'");
          publishState();

          Path dsAsPath = Paths.get(ds);

          // first of all check if the DS is available; we can take the
          // Files.exist here:
          // if the DS exists (and we have access to read it): Files.exist = true
          if (!Files.exists(dsAsPath)) {
            // error - continue with next datasource
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable", new String[] { ds }));
            continue;
          }

          // just check datasource folder, parse NEW folders first
          List<Path> newMovieDirs = new ArrayList<>();
          List<Path> existingMovieDirs = new ArrayList<>();
          List<Path> rootList = listFilesAndDirs(dsAsPath);

          // when there is _nothing_ found in the ds root, it might be offline -
          // skip further processing;
          // not in Windows since that won't happen there
          if (rootList.isEmpty() && !Platform.isWindows()) {
            // error - continue with next datasource
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable", new String[] { ds }));
            continue;
          }

          List<Path> rootFiles = new ArrayList<>();
          for (Path path : rootList) {
            if (Files.isDirectory(path)) {
              if (existing.contains(path)) {
                existingMovieDirs.add(path);
              }
              else {
                newMovieDirs.add(path);
              }
            }
            else {
              rootFiles.add(path);
            }
          }
          rootList.clear();
          for (Path path : newMovieDirs) {
            searchAndParse(dsAsPath.toAbsolutePath(), path, Integer.MAX_VALUE);
          }
          for (Path path : existingMovieDirs) {
            searchAndParse(dsAsPath.toAbsolutePath(), path, Integer.MAX_VALUE);
          }
          if (rootFiles.size() > 0) {
            submitTask(new parseMultiMovieDirTask(dsAsPath.toAbsolutePath(), dsAsPath.toAbsolutePath(), rootFiles));
          }

          waitForCompletionOrCancel();
          newMovieDirs.clear();
          existingMovieDirs.clear();
          rootFiles.clear();

          if (cancel) {
            break;
          }

          // cleanup
          cleanup(ds);

          // mediainfo
          gatherMediainfo(ds);

          if (cancel) {
            break;
          }

          // build image cache on import
          if (MovieModuleManager.MOVIE_SETTINGS.isBuildImageCacheOnImport()) {
            for (Movie movie : movieList.getMovies()) {
              if (!dsAsPath.equals(Paths.get(movie.getDataSource()))) {
                // check only movies matching datasource
                continue;
              }
              imageFiles.addAll(movie.getImagesToCache());
            }
          }
        } // END datasource loop
      }
      else {
        LOGGER.info("Start UDS for selected movies");
        initThreadPool(3, "update");
        setTaskName(BUNDLE.getString("update.datasource"));
        publishState();

        // update per movie folder
        Map<Path, String> folder = new HashMap<Path, String>(movieFolders.size());
        // no dupes b/c of possible MMD movies with same path
        for (Movie m : movieFolders) {
          folder.put(m.getPathNIO(), m.getDataSource());
        }
        for (Map.Entry<Path, String> entry : folder.entrySet()) {
          Path dir = entry.getKey();
          String ds = entry.getValue();
          submitTask(new FindMovieTask(dir, Paths.get(ds)));
        }
        waitForCompletionOrCancel();

        // cleanup
        cleanup(movieFolders);

        // mediainfo
        gatherMediainfo(movieFolders);
      }

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

      LOGGER.debug("FilesFound " + filesFound.size());
      LOGGER.debug("moviesFound " + movieList.getMovieCount());
      LOGGER.debug("PreDir " + preDir);
      LOGGER.debug("PostDir " + postDir);
      LOGGER.debug("VisFile " + visFile);
      LOGGER.debug("PreDirAll " + preDirAll);
      LOGGER.debug("PostDirAll " + postDirAll);
      LOGGER.debug("VisFileAll " + visFileAll);
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

    private Path subdir     = null;
    private Path datasource = null;
    private long uniqueId;

    public FindMovieTask(Path subdir, Path datasource) {
      this.subdir = subdir;
      this.datasource = datasource;
      this.uniqueId = TmmTaskManager.getInstance().GLOB_THRD_CNT.incrementAndGet();
    }

    @Override
    public String call() {
      String name = Thread.currentThread().getName();
      if (!name.contains("-G")) {
        name = name + "-G0";
      }
      name = name.replaceAll("\\-G\\d+", "-G" + uniqueId);
      Thread.currentThread().setName(name);

      parseMovieDirectory(subdir, datasource);
      return subdir.toString();
    }
  }

  /**
   * ThreadpoolWorker just for spawning a MultiMovieDir parser directly
   * 
   * @author Myron Boyle
   * @version 1.0
   */
  private class parseMultiMovieDirTask implements Callable<Object> {

    private Path       movieDir   = null;
    private Path       datasource = null;
    private List<Path> allFiles   = null;

    public parseMultiMovieDirTask(Path dataSource, Path movieDir, List<Path> allFiles) {
      this.datasource = dataSource;
      this.movieDir = movieDir;
      this.allFiles = allFiles;
    }

    @Override
    public String call() {
      createMultiMovieFromDir(datasource, movieDir, allFiles);
      return movieDir.toString();
    }
  }

  private void parseMovieDirectory(Path movieDir, Path dataSource) {
    List<Path> movieDirList = listFilesAndDirs(movieDir);
    ArrayList<Path> files = new ArrayList<>();
    ArrayList<Path> dirs = new ArrayList<>(); // FIXME: what for....?
    HashSet<String> normalizedVideoFiles = new HashSet<>(); // just for
                                                            // identifying MMD

    boolean isDiscFolder = false;
    boolean isMultiMovieDir = false;
    boolean videoFileFound = false;
    Path movieRoot = movieDir; // root set to current dir - might be adjusted by
                               // disc folders

    for (Path path : movieDirList) {
      if (Utils.isRegularFile(path)) {
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
      // hmm... we never found a video file (but maybe others, trailers) so NO
      // need to parse THIS folder
      return;
    }

    if (isDiscFolder) {
      // if inside own DiscFolder, walk backwards till movieRoot folder
      Path relative = dataSource.relativize(movieDir);
      while (relative.toString().toUpperCase(Locale.ROOT).contains("VIDEO_TS") || relative.toString().toUpperCase(Locale.ROOT).contains("BDMV")) {
        movieDir = movieDir.getParent();
        relative = dataSource.relativize(movieDir);
      }
      movieRoot = movieDir;
    }
    else {
      // no VIDEO files in this dir - skip this folder
      if (normalizedVideoFiles.size() == 0) {
        return;
      }
      // more than one (unstacked) movie file in directory (or DS root) -> must
      // parsed as multiMovieDir
      if (normalizedVideoFiles.size() > 1 || movieDir.equals(dataSource)) {
        isMultiMovieDir = true;
      }
    }

    if (cancel) {
      return;
    }
    // ok, we're ready to parse :)
    if (isMultiMovieDir) {
      createMultiMovieFromDir(dataSource, movieRoot, files);
    }
    else {
      createSingleMovieFromDir(dataSource, movieRoot, isDiscFolder);
    }

  }

  /**
   * Parses ALL NFO MFs (merged together) and create a movie<br>
   * 
   * @param mfs
   * @return Movie or NULL
   */
  private Movie parseNFOs(List<MediaFile> mfs) {
    Movie movie = null;
    for (MediaFile mf : mfs) {

      if (mf.getType().equals(MediaFileType.NFO)) {
        LOGGER.info("| parsing NFO " + mf.getFileAsPath());
        Movie nfo = null;
        switch (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector()) {
          case XBMC:
            nfo = MovieToXbmcNfoConnector.getData(mf.getFileAsPath());
            if (nfo == null) {
              // try the other
              nfo = MovieToKodiNfoConnector.getData(mf.getFileAsPath());
            }
            if (nfo == null) {
              // try the other
              nfo = MovieToMpNfoConnector.getData(mf.getFileAsPath());
            }
            break;

          case KODI:
            nfo = MovieToKodiNfoConnector.getData(mf.getFileAsPath());
            // not needed at the moment since kodi is downwards compatible
            // if (nfo == null) {
            // // try the other
            // nfo = MovieToXbmcNfoConnector.getData(mf.getFileAsPath());
            // }
            if (nfo == null) {
              // try the other
              nfo = MovieToMpNfoConnector.getData(mf.getFileAsPath());
            }
            break;

          case MP:
            nfo = MovieToMpNfoConnector.getData(mf.getFileAsPath());
            if (nfo == null) {
              // try the other
              nfo = MovieToKodiNfoConnector.getData(mf.getFileAsPath());
            }
            // not needed at the moment since kodi is downwards compatible
            // if (nfo == null) {
            // // try the other
            // nfo = MovieToXbmcNfoConnector.getData(mf.getFileAsPath());
            // }
            break;
        }

        // take first nfo 1:1
        if (movie == null) {
          movie = nfo;
        }
        else {
          movie.merge(nfo);
        }

        // was NFO, but parsing exception. try to find at least imdb id within
        if (movie != null && movie.getImdbId().isEmpty()) {
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
    } // end MFs

    for (MediaFile mf : mfs) {
      if (mf.getType().equals(MediaFileType.VSMETA)) {
        if (movie == null) {
          movie = new Movie();
        }
        VSMeta vsmeta = new VSMeta();
        vsmeta.parseFile(mf.getFileAsPath());
        movie.merge(vsmeta.getMovie());
      }
    }

    return movie;
  }

  /**
   * for SingleMovie or DiscFolders
   *
   * @param dataSource
   *          the data source
   * @param movieDir
   *          the movie folder
   * @param isDiscFolder
   *          is the movie in a disc folder?
   */
  private void createSingleMovieFromDir(Path dataSource, Path movieDir, boolean isDiscFolder) {
    LOGGER.info("Parsing single movie directory: " + movieDir + " (are we a disc folder? " + isDiscFolder + ")");

    Path relative = dataSource.relativize(movieDir);
    // STACKED FOLDERS - go up ONE level (only when the stacked folder ==
    // stacking marker)
    // movie/CD1/ & /movie/CD2 -> go up
    // movie CD1/ & /movie CD2 -> NO - there could be other files/folders there

    // if (!Utils.getFolderStackingMarker(relative.toString()).isEmpty() &&
    // level > 1) {
    if (!Utils.getFolderStackingMarker(relative.toString()).isEmpty()
        && Utils.getFolderStackingMarker(relative.toString()).equals(movieDir.getFileName().toString())) {
      movieDir = movieDir.getParent();
    }

    Movie movie = movieList.getMovieByPath(movieDir);
    HashSet<Path> allFiles = getAllFilesRecursive(movieDir, 3); // need 3 (was
                                                                // 2) because
                                                                // extracted BD
    filesFound.add(movieDir.toAbsolutePath()); // our global cache
    filesFound.addAll(allFiles); // our global cache

    // convert to MFs (we need it anyways at the end)
    ArrayList<MediaFile> mfs = new ArrayList<>();
    for (Path file : allFiles) {
      mfs.add(new MediaFile(file));
    }
    allFiles.clear();

    // ***************************************************************
    // first round - try to parse NFO(s) first
    // ***************************************************************
    if (movie == null) {
      LOGGER.debug("| movie not found; looking for NFOs");
      movie = parseNFOs(mfs);
      if (movie == null) {
        movie = new Movie();
      }
      movie.setNewlyAdded(true);
      movie.setDateAdded(new Date());
    }

    // ***************************************************************
    // second round - try to parse additional files
    // ***************************************************************
    String bdinfoTitle = ""; // title parsed out of BDInfo
    String videoName = ""; // title from file
    for (MediaFile mf : mfs) {
      if (mf.getType().equals(MediaFileType.TEXT)) {
        try {
          String txtFile = Utils.readFileToString(mf.getFileAsPath());

          String bdinfo = StrgUtils.substr(txtFile, ".*Disc Title:\\s+(.*?)[\\n\\r]");
          if (!bdinfo.isEmpty()) {
            LOGGER.debug("| Found Disc Title in BDInfo.txt: " + bdinfo);
            bdinfoTitle = WordUtils.capitalizeFully(bdinfo);
          }

          String imdb = ParserUtils.detectImdbId(txtFile);
          if (movie.getImdbId().isEmpty() && !imdb.isEmpty()) {
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
    }

    if (movie.getTitle().isEmpty()) {
      // get the "cleaner" name/year combo
      // ParserUtils.ParserInfo video = ParserUtils.getCleanerString(new
      // String[] { videoName, movieDir.getName(), bdinfoTitle });
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
    // get edition from name if no edition has been set via NFO
    if (movie.getEdition() == MovieEdition.NONE) {
      movie.setEdition(MovieEdition.getMovieEditionFromString(movieDir.getFileName().toString()));
    }

    movie.setPath(movieDir.toAbsolutePath().toString());
    movie.setDataSource(dataSource.toString());

    // movie.findActorImages(); // TODO: find as MediaFiles
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
    // third round - now add all the other known files
    // ***************************************************************
    addMediafilesToMovie(movie, mfs);

    // ***************************************************************
    // fourth round - try to match unknown graphics like title.ext or
    // filename.ext as poster
    // ***************************************************************
    if (movie.getArtworkFilename(MediaFileType.POSTER).isEmpty()) {
      for (MediaFile mf : mfs) {
        if (mf.getType().equals(MediaFileType.GRAPHIC)) {
          LOGGER.debug("| parsing unknown graphic " + mf.getFilename());
          List<MediaFile> vid = movie.getMediaFiles(MediaFileType.VIDEO);
          if (vid != null && !vid.isEmpty()) {
            String vfilename = vid.get(0).getFilename();
            if (FilenameUtils.getBaseName(vfilename).equals(FilenameUtils.getBaseName(mf.getFilename())) // basename
                                                                                                         // match
                || FilenameUtils.getBaseName(Utils.cleanStackingMarkers(vfilename)).trim().equals(FilenameUtils.getBaseName(mf.getFilename())) // basename
                                                                                                                                               // w/o
                                                                                                                                               // stacking
                || movie.getTitle().equals(FilenameUtils.getBaseName(mf.getFilename()))) { // title
                                                                                           // match
              mf.setType(MediaFileType.POSTER);
              movie.addToMediaFiles(mf);
            }
          }
        }
      }
    }

    // ***************************************************************
    // check if that movie is an offline movie
    // ***************************************************************
    boolean isOffline = false;
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      if ("disc".equalsIgnoreCase(mf.getExtension())) {
        isOffline = true;
      }
    }
    movie.setOffline(isOffline);

    movie.reEvaluateStacking();
    movie.saveToDb();
  }

  /**
   * more than one movie in dir? Then use that!
   * 
   * @param dataSource
   *          the data source
   * @param movieDir
   *          the movie folder
   */
  private void createMultiMovieFromDir(Path dataSource, Path movieDir) {
    List<Path> allFiles = listFilesOnly(movieDir);
    createMultiMovieFromDir(dataSource, movieDir, allFiles);
  }

  /**
   * more than one movie in dir? Then use that with already known files
   * 
   * @param dataSource
   *          the data source
   * @param movieDir
   *          the movie folder
   * @param allFiles
   *          just use this files, do not list again
   */
  private void createMultiMovieFromDir(Path dataSource, Path movieDir, List<Path> allFiles) {
    LOGGER.info("Parsing multi  movie directory: " + movieDir); // double space is for log alignment ;)

    List<Movie> movies = movieList.getMoviesByPath(movieDir);

    filesFound.add(movieDir); // our global cache
    filesFound.addAll(allFiles); // our global cache

    // convert to MFs
    ArrayList<MediaFile> mfs = new ArrayList<>();
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

    for (MediaFile mf : getMediaFiles(mfs, MediaFileType.VIDEO)) {

      Movie movie = null;
      String basename = FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mf.getFilename()));

      // get all MFs with same basename
      List<MediaFile> sameName = new ArrayList<>();
      LOGGER.trace("UDS: basename: " + basename);
      for (MediaFile sm : mfs) {
        String smBasename = FilenameUtils.getBaseName(sm.getFilename());
        String smNameRegexp = Pattern.quote(basename) + "[\\s.,_-].*";
        if (smBasename.equals(basename) || smBasename.matches(smNameRegexp)) {
          if (sm.getType() == MediaFileType.GRAPHIC) {
            // same named graphics (unknown, not detected without postfix) treated as posters
            sm.setType(MediaFileType.POSTER);
          }
          sameName.add(sm);
          LOGGER.trace("UDS: found matching MF: " + sm);
        }
      }

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
        movie = parseNFOs(sameName);

        if (movie == null) {
          // still NULL, create new movie movie from file
          LOGGER.debug("| Create new movie from file: " + mf);
          movie = new Movie();
          String[] ty = ParserUtils.detectCleanMovienameAndYear(basename);
          movie.setTitle(ty[0]);
          if (!ty[1].isEmpty()) {
            movie.setYear(ty[1]);
          }
          // get edition from name
          movie.setEdition(MovieEdition.getMovieEditionFromString(basename));

          // if the String 3D is in the movie file name, assume it is a 3D movie
          Matcher matcher = video3DPattern.matcher(basename);
          if (matcher.find()) {
            movie.setVideoIn3D(true);
          }
          movie.setDateAdded(new Date());
        }
        movie.setDataSource(dataSource.toString());
        movie.setNewlyAdded(true);
        movie.setPath(mf.getPath());

        movieList.addMovie(movie);
        movies.add(movie); // add to our cached copy
      }

      if (!Utils.isValidImdbId(movie.getImdbId())) {
        movie.setImdbId(ParserUtils.detectImdbId(mf.getFileAsPath().toString()));
      }
      if (movie.getMediaSource() == MediaSource.UNKNOWN) {
        movie.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
      }
      LOGGER.debug("| parsing video file " + mf.getFilename());
      // movie.addToMediaFiles(mf);
      movie.setDateAddedFromMediaFile(mf);
      movie.setMultiMovieDir(true);

      // 3) find additional files, which start with videoFileName
      // List<MediaFile> existingMediaFiles = new ArrayList<>(movie.getMediaFiles());
      // List<MediaFile> foundMediaFiles = new ArrayList<>();
      // for (int i = allFiles.size() - 1; i >= 0; i--) {
      // Path fileInDir = allFiles.get(i);
      // if (fileInDir.getFileName().toString().startsWith(basename)) { // need
      // // toString
      // // b/c of
      // // possible
      // // spaces!!
      // MediaFile mediaFile = new MediaFile(fileInDir);
      // if (!existingMediaFiles.contains(mediaFile)) {
      // if (mediaFile.getType() == MediaFileType.GRAPHIC) {
      // // same named graphics (unknown, not detected without postfix)
      // // treated as posters
      // mediaFile.setType(MediaFileType.POSTER);
      // }
      // foundMediaFiles.add(mediaFile);
      // }
      // // started with basename, so remove it for others
      // allFiles.remove(i);
      // }
      // }
      // addMediafilesToMovie(movie, foundMediaFiles);
      addMediafilesToMovie(movie, sameName);
      mfs.removeAll(sameName);

      // check if that movie is an offline movie
      boolean isOffline = false;
      for (MediaFile mediaFiles : movie.getMediaFiles(MediaFileType.VIDEO)) {
        if ("disc".equalsIgnoreCase(mediaFiles.getExtension())) {
          isOffline = true;
        }
      }
      movie.setOffline(isOffline);

      if (movie.getMovieSet() != null) {
        LOGGER.debug("| movie is part of a movieset");
        // movie.getMovieSet().addMovie(movie);
        movie.getMovieSet().insertMovie(movie);
        movieList.sortMoviesInMovieSet(movie.getMovieSet());
        movie.getMovieSet().saveToDb();
      }

      movie.saveToDb();
    } // end foreach VIDEO MF

    // check stacking on all movie from this dir (it might have changed!)
    for (Movie m : movieList.getMoviesByPath(movieDir)) {
      m.reEvaluateStacking();
      m.saveToDb();
    }
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
          movie.setImdbId(ParserUtils.detectImdbId(mf.getFileAsPath().toString()));
        }

        LOGGER.debug("| parsing " + mf.getType().name() + " " + mf.getFileAsPath());
        switch (mf.getType()) {
          case VIDEO:
            movie.addToMediaFiles(mf);
            movie.setDateAddedFromMediaFile(mf);
            if (movie.getMediaSource() == MediaSource.UNKNOWN) {
              movie.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
            }
            break;

          case TRAILER:
            mf.gatherMediaInformation(); // do this exceptionally here, to set
                                         // quality in one rush
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
          case CLEARLOGO:
          case MEDIAINFO:
          case VSMETA:
            movie.addToMediaFiles(mf);
            break;

          case GRAPHIC:
          case UNKNOWN:
          default:
            LOGGER.debug("| NOT adding unknown media file type: " + mf.getFileAsPath());
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
    List<Movie> moviesToRemove = new ArrayList<>();
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
        if (!Files.exists(movieDir)) {
          LOGGER.debug("movie directory '" + movieDir + "' not found, removing from DB...");
          moviesToRemove.add(movie);
        }
        else {
          // can be; MMD and/or dir=DS root
          LOGGER.warn("dir " + movieDir + " not in hashset, but on hdd!");
        }
      }

      // have a look if that movie has just been added -> so we don't need any
      // cleanup
      if (!movie.isNewlyAdded()) {
        // check and delete all not found MediaFiles
        List<MediaFile> mediaFiles = new ArrayList<>(movie.getMediaFiles());
        for (MediaFile mf : mediaFiles) {
          if (!filesFound.contains(mf.getFileAsPath())) {
            if (!mf.exists()) {
              LOGGER.debug("removing orphaned file from DB: " + mf.getFileAsPath());
              movie.removeFromMediaFiles(mf);
            }
            else {
              // hmm...this should not happen
              LOGGER.warn("file " + mf.getFileAsPath() + " not in hashset, but on hdd!");
            }
          }
        }
        if (movie.getMediaFiles(MediaFileType.VIDEO).isEmpty()) {
          LOGGER.debug("Movie (" + movie.getTitle() + ") without VIDEO files detected, removing from DB...");
          moviesToRemove.add(movie);
        }
        else {
          movie.saveToDb();
        }
      }
      else {
        LOGGER.info("Movie (" + movie.getTitle() + ") is new - no need for cleanup");
      }
    }
    movieList.removeMovies(moviesToRemove);
  }

  private void cleanup(List<Movie> movies) {
    setTaskName(BUNDLE.getString("update.cleanup"));
    setTaskDescription(null);
    setProgressDone(0);
    setWorkUnits(0);
    publishState();

    LOGGER.info("removing orphaned movies/files...");
    List<Movie> moviesToRemove = new ArrayList<>();
    for (int i = movies.size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }

      Movie movie = movies.get(i);

      Path movieDir = movie.getPathNIO();
      if (!filesFound.contains(movieDir)) {
        // dir is not in hashset - check with exists to be sure it is not here
        if (!Files.exists(movieDir)) {
          LOGGER.debug("movie directory '" + movieDir + "' not found, removing from DB...");
          moviesToRemove.add(movie);
        }
        else {
          // can be; MMD and/or dir=DS root
          LOGGER.warn("dir " + movieDir + " not in hashset, but on hdd!");
        }
      }

      // have a look if that movie has just been added -> so we don't need any
      // cleanup
      if (!movie.isNewlyAdded()) {
        // check and delete all not found MediaFiles
        List<MediaFile> mediaFiles = new ArrayList<>(movie.getMediaFiles());
        for (MediaFile mf : mediaFiles) {
          if (!filesFound.contains(mf.getFileAsPath())) {
            if (!mf.exists()) {
              LOGGER.debug("removing orphaned file from DB: " + mf.getFileAsPath());
              movie.removeFromMediaFiles(mf);
            }
            else {
              // hmm...this should not happen
              LOGGER.warn("file " + mf.getFileAsPath() + " not in hashset, but on hdd!");
            }
          }
        }
        if (movie.getMediaFiles(MediaFileType.VIDEO).isEmpty()) {
          LOGGER.debug("Movie (" + movie.getTitle() + ") without VIDEO files detected, removing from DB...");
          moviesToRemove.add(movie);
        }
        else {
          movie.saveToDb();
        }
      }
      else {
        LOGGER.info("Movie (" + movie.getTitle() + ") is new - no need for cleanup");
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

      for (MediaFile mf : new ArrayList<>(movie.getMediaFiles())) {
        if (StringUtils.isBlank(mf.getContainerFormat())) {
          submitTask(new MediaFileInformationFetcherTask(mf, movie, false));
        }
      }
    }
    waitForCompletionOrCancel();
  }

  private void gatherMediainfo(List<Movie> movies) {
    // start MI
    setTaskName(BUNDLE.getString("update.mediainfo"));
    publishState();

    initThreadPool(1, "mediainfo");

    LOGGER.info("getting Mediainfo...");
    for (Movie movie : movies) {
      if (cancel) {
        break;
      }
      for (MediaFile mf : new ArrayList<>(movie.getMediaFiles())) {
        if (StringUtils.isBlank(mf.getContainerFormat())) {
          submitTask(new MediaFileInformationFetcherTask(mf, movie, false));
        }
      }
    }
    waitForCompletionOrCancel();
  }

  /**
   * gets mediaFile of specific type
   * 
   * @param mfs
   *          the MF list to search
   * @param types
   *          the MediaFileTypes
   * @return MF or NULL
   */
  private MediaFile getMediaFile(List<MediaFile> mfs, MediaFileType... types) {
    MediaFile mf = null;
    for (MediaFile mediaFile : mfs) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (match) {
        mf = new MediaFile(mediaFile);
      }
    }
    return mf;
  }

  /**
   * gets all mediaFiles of specific type
   * 
   * @param mfs
   *          the MF list to search
   * @param types
   *          the MediaFileTypes
   * @return list of matching MFs
   */
  private List<MediaFile> getMediaFiles(List<MediaFile> mfs, MediaFileType... types) {
    List<MediaFile> mf = new ArrayList<>();
    for (MediaFile mediaFile : mfs) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (match) {
        mf.add(new MediaFile(mediaFile));
      }
    }
    return mf;
  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the
    // text is never right
    publishState(progressDone);
  }

  /**
   * simple NIO File.listFiles() replacement<br>
   * returns ONLY regular files (NO folders, NO hidden) in specified dir, filtering against our badwords (NOT recursive)
   * 
   * @param directory
   *          the folder to list the files for
   * @return list of files&folders
   */
  public static List<Path> listFilesOnly(Path directory) {
    List<Path> fileNames = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
      for (Path path : directoryStream) {
        if (Utils.isRegularFile(path)) {
          String fn = path.getFileName().toString().toUpperCase(Locale.ROOT);
          if (!skipFolders.contains(fn) && !fn.matches(skipRegex)
              && !MovieModuleManager.MOVIE_SETTINGS.getMovieSkipFolders().contains(path.toFile().getAbsolutePath())) {
            fileNames.add(path.toAbsolutePath());
          }
          else {
            LOGGER.debug("Skipping: " + path);
          }
        }
      }
    }
    catch (IOException ex) {
    }
    return fileNames;
  }

  /**
   * simple NIO File.listFiles() replacement<br>
   * returns all files & folders in specified dir, filtering against our skip folders (NOT recursive)
   * 
   * @param directory
   *          the folder to list the items for
   * @return list of files&folders
   */
  public static List<Path> listFilesAndDirs(Path directory) {
    List<Path> fileNames = new ArrayList<>();
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
      for (Path path : directoryStream) {
        String fn = path.getFileName().toString().toUpperCase(Locale.ROOT);
        if (!skipFolders.contains(fn) && !fn.matches(skipRegex)
            && !MovieModuleManager.MOVIE_SETTINGS.getMovieSkipFolders().contains(path.toFile().getAbsolutePath())) {
          fileNames.add(path.toAbsolutePath());
        }
        else {
          LOGGER.debug("Skipping: " + path);
        }
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
    folder = folder.toAbsolutePath();
    AllFilesRecursive visitor = new AllFilesRecursive();
    try {
      Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), deep, visitor);
    }
    catch (IOException e) {
      // can not happen, since we overrided visitFileFailed, which throws no
      // exception ;)
    }
    return visitor.fFound;
  }

  private static class AllFilesRecursive extends SimpleFileVisitor<Path> {
    private HashSet<Path> fFound = new HashSet<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
      visFileAll++;
      if (Utils.isRegularFile(attr) && !file.getFileName().toString().matches(skipRegex)) {
        fFound.add(file.toAbsolutePath());
      }
      // System.out.println("(" + attr.size() + "bytes)");
      // System.out.println("(" + attr.creationTime() + " date)");
      return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      preDirAll++;
      // getFilename returns null on DS root!
      if (dir.getFileName() != null
          && (Files.exists(dir.resolve(".tmmignore")) || Files.exists(dir.resolve("tmmignore")) || Files.exists(dir.resolve(".nomedia"))
              || skipFolders.contains(dir.getFileName().toString().toUpperCase(Locale.ROOT)) || dir.getFileName().toString().matches(skipRegex))
          || MovieModuleManager.MOVIE_SETTINGS.getMovieSkipFolders().contains(dir.toFile().getAbsolutePath())) {
        LOGGER.debug("Skipping dir: " + dir);
        return SKIP_SUBTREE;
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      postDirAll++;
      return CONTINUE;
    }

    // If there is some error accessing the file, let the user know.
    // If you don't override this method and an error occurs, an IOException is
    // thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      LOGGER.error("" + exc);
      return CONTINUE;
    }
  }

  // **************************************
  // gets all files recursive,
  // detects movieRootDir (in case of stacked/disc folder)
  // and starts parsing directory immediately
  // **************************************
  public void searchAndParse(Path datasource, Path folder, int deep) {
    folder = folder.toAbsolutePath();
    SearchAndParseVisitor visitor = new SearchAndParseVisitor(datasource);
    try {
      Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), deep, visitor);
    }
    catch (IOException e) {
      // can not happen, since we override visitFileFailed, which throws no
      // exception ;)
    }
  }

  private class SearchAndParseVisitor implements FileVisitor<Path> {
    private Path              datasource;
    private ArrayList<String> unstackedRoot = new ArrayList<>(); // only for
                                                                 // folder
                                                                 // stacking
    private HashSet<Path>     videofolders  = new HashSet<>();   // all found
                                                                 // video
                                                                 // folders

    protected SearchAndParseVisitor(Path datasource) {
      this.datasource = datasource;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
      visFile++;
      if (Utils.isRegularFile(attr) && !file.getFileName().toString().matches(skipRegex)) {
        // check for video?
        if (Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.toString()).toLowerCase(Locale.ROOT))) {
          if (file.getParent().getFileName().toString().equals("STREAM")) {
            return CONTINUE; // BD folder has an additional parent video folder
                             // - ignore it here
          }
          videofolders.add(file.getParent());
        }
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      preDir++;
      String fn = dir.getFileName().toString().toUpperCase(Locale.ROOT);
      if (skipFolders.contains(fn) || fn.matches(skipRegex) || Files.exists(dir.resolve(".tmmignore")) || Files.exists(dir.resolve("tmmignore"))
          || Files.exists(dir.resolve(".nomedia"))
          || MovieModuleManager.MOVIE_SETTINGS.getMovieSkipFolders().contains(dir.toFile().getAbsolutePath())) {
        LOGGER.debug("Skipping dir: " + dir);
        return SKIP_SUBTREE;
      }
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      postDir++;
      if (cancel) {
        return TERMINATE;
      }

      if (this.videofolders.contains(dir)) {
        boolean update = true;
        // quick fix for folder stacking
        // name = stacking marker & parent has already been processed - skip
        Path relative = datasource.relativize(dir);
        if (!Utils.getFolderStackingMarker(relative.toString()).isEmpty()
            && Utils.getFolderStackingMarker(relative.toString()).equals(dir.getFileName().toString())) {
          if (unstackedRoot.contains(dir.getParent().toString())) {
            update = false;
          }
          else {
            unstackedRoot.add(dir.getParent().toString());
          }
        }
        if (update) {
          // this.videofolders.remove(dir);
          submitTask(new FindMovieTask(dir, datasource));
        }
      }
      return CONTINUE;
    }

    // If there is some error accessing the file, let the user know.
    // If you don't override this method and an error occurs, an IOException is
    // thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      LOGGER.error("" + exc);
      return CONTINUE;
    }
  }
}
