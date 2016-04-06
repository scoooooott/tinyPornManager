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
package org.tinymediamanager.core.tvshow.tasks;

import static java.nio.file.FileVisitResult.*;

import java.io.File;
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser.EpisodeMatchingResult;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.connector.TvShowToXbmcNfoConnector;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class TvShowUpdateDataSourcesTask.
 * 
 * @author Manuel Laggner
 */

public class TvShowUpdateDatasourceTask2 extends TmmThreadPool {
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowUpdateDatasourceTask2.class);
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());                                  //$NON-NLS-1$

  // skip well-known, but unneeded folders (UPPERCASE)
  private static final List<String>   skipFolders      = Arrays.asList(".", "..", "CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "SSIF", "AUXDATA",
      "AUDIO_TS", "$RECYCLE.BIN", "RECYCLER", "SYSTEM VOLUME INFORMATION", "@EADIR");

  // skip folders starting with a SINGLE "." or "._"
  private static final String         skipFoldersRegex = "^[.][\\w@]+.*";

  private static long                 preDir           = 0;
  private static long                 postDir          = 0;
  private static long                 visFile          = 0;

  private List<String>                dataSources;
  private List<Path>                  tvShowFolders    = new ArrayList<Path>();
  private TvShowList                  tvShowList;
  private HashSet<Path>               filesFound       = new HashSet<Path>();

  /**
   * Instantiates a new scrape task - to update all datasources
   * 
   */
  public TvShowUpdateDatasourceTask2() {
    super(BUNDLE.getString("update.datasource"));
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getTvShowSettings().getTvShowDataSource());
  }

  /**
   * Instantiates a new scrape task - to update a single datasource
   * 
   * @param datasource
   */
  public TvShowUpdateDatasourceTask2(String datasource) {
    super(BUNDLE.getString("update.datasource") + " (" + datasource + ")");
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(1);
    dataSources.add(datasource);
  }

  /**
   * Instantiates a new scrape task - to update given tv shows
   * 
   * @param tvShowFolders
   */
  public TvShowUpdateDatasourceTask2(List<Path> tvShowFolders) {
    super(BUNDLE.getString("update.datasource"));
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(0);
    this.tvShowFolders.addAll(tvShowFolders);
  }

  @Override
  public void doInBackground() {
    // check if there is at least one DS to update
    Utils.removeEmptyStringsFromList(dataSources);
    if (dataSources.isEmpty() && tvShowFolders.isEmpty()) {
      LOGGER.info("no datasource to update");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.nonespecified"));
      return;
    }

    try {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      start();

      // get existing show folders
      List<Path> existing = new ArrayList<Path>();
      for (TvShow show : tvShowList.getTvShows()) {
        existing.add(show.getPathNIO());
      }

      // here we have 2 ways of updating:
      // - per datasource -> update ds / remove orphaned / update MFs
      // - per TV show -> udpate TV show / update MFs
      if (tvShowFolders.size() == 0) {

        for (String ds : dataSources) {
          initThreadPool(3, "update"); // FIXME: one thread here? - more threads killed the UI
          List<Path> newTvShowDirs = new ArrayList<Path>();
          List<Path> existingTvShowDirs = new ArrayList<Path>();
          List<Path> rootList = listFilesAndDirs(Paths.get(ds));
          for (Path path : rootList) {
            if (Files.isDirectory(path)) {
              if (existing.contains(path)) {
                existingTvShowDirs.add(path);
              }
              else {
                newTvShowDirs.add(path);
              }
            }
            else {
              // File in root folder - not possible for TV datasource
            }
          }

          for (Path subdir : newTvShowDirs) {
            submitTask(new FindTvShowTask(subdir, Paths.get(ds).toAbsolutePath()));
          }
          for (Path subdir : existingTvShowDirs) {
            submitTask(new FindTvShowTask(subdir, Paths.get(ds).toAbsolutePath()));
          }
          waitForCompletionOrCancel();
          if (cancel) {
            break;
          }

          // cleanup DS
          // gatherMI

        } // end forech datasource
      }
      else {
        // update TV show
        for (Path path : tvShowFolders) {
          submitTask(new FindTvShowTask(path, path.getParent().toAbsolutePath()));
        }
        // cleanup DS
        // gatherMI
      }

      stopWatch.stop();
      LOGGER.info("Done updating datasource :) - took " + stopWatch);

      System.out.println("FilesFound " + filesFound.size());
      System.out.println("tvShowsFound " + tvShowList.getTvShowCount());
      System.out.println("episodesFound " + tvShowList.getEpisodeCount());
      System.out.println("PreDir " + preDir);
      System.out.println("PostDir " + postDir);
      System.out.println("VisFile " + visFile);
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
  }

  private void cleanup(TvShow tvShow) {
    boolean dirty = false;
    if (!tvShow.isNewlyAdded() || tvShow.hasNewlyAddedEpisodes()) {
      // check and delete all not found MediaFiles
      List<MediaFile> mediaFiles = new ArrayList<MediaFile>(tvShow.getMediaFiles());
      for (MediaFile mf : mediaFiles) {
        if (!filesFound.contains(mf.getFile())) {
          if (!mf.exists()) {
            LOGGER.debug("removing orphaned file: " + mf.getPath() + File.separator + mf.getFilename());
            tvShow.removeFromMediaFiles(mf);
            dirty = true;
          }
          else {
            LOGGER.warn("file " + mf.getFile().getAbsolutePath() + " not in hashset, but on hdd!");
          }
        }
      }
      List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>(tvShow.getEpisodes());
      for (TvShowEpisode episode : episodes) {
        mediaFiles = new ArrayList<MediaFile>(episode.getMediaFiles());
        for (MediaFile mf : mediaFiles) {
          if (!filesFound.contains(mf.getFile())) {
            if (!mf.exists()) {
              LOGGER.debug("removing orphaned file: " + mf.getPath() + File.separator + mf.getFilename());
              episode.removeFromMediaFiles(mf);
              dirty = true;
            }
            else {
              LOGGER.warn("file " + mf.getFile().getAbsolutePath() + " not in hashset, but on hdd!");
            }
          }
        }
        // lets have a look if there is at least one video file for this episode
        List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
        if (mfs.size() == 0) {
          tvShow.removeEpisode(episode);
          dirty = true;
        }
      }
    }

    if (dirty) {
      tvShow.saveToDb();
    }
  }

  /*
   * detect which mediafiles has to be parsed and start a thread to do that
   */
  private void gatherMediaInformationForUngatheredMediaFiles(TvShow tvShow) {
    // get mediainfo for tv show (fanart/poster..)
    ArrayList<MediaFile> ungatheredMediaFiles = new ArrayList<MediaFile>();
    for (MediaFile mf : tvShow.getMediaFiles()) {
      if (StringUtils.isBlank(mf.getContainerFormat())) {
        ungatheredMediaFiles.add(mf);
      }
    }

    if (ungatheredMediaFiles.size() > 0) {
      submitTask(new MediaFileInformationFetcherTask(ungatheredMediaFiles, tvShow, false));
    }

    // get mediainfo for all episodes within this tv show
    for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(tvShow.getEpisodes())) {
      ungatheredMediaFiles = new ArrayList<MediaFile>();
      for (MediaFile mf : episode.getMediaFiles()) {
        if (StringUtils.isBlank(mf.getContainerFormat())) {
          ungatheredMediaFiles.add(mf);
        }
      }

      if (ungatheredMediaFiles.size() > 0) {
        submitTask(new MediaFileInformationFetcherTask(ungatheredMediaFiles, episode, false));
      }
    }
  }

  /**
   * The Class FindTvShowTask.
   * 
   * @author Manuel Laggner
   */
  private class FindTvShowTask implements Callable<Object> {
    private Path showDir    = null;
    private Path datasource = null;

    /**
     * Instantiates a new find tv show task.
     * 
     * @param subdir
     *          the subdir
     * @param datasource
     *          the datasource
     */
    public FindTvShowTask(Path showDir, Path datasource) {
      this.showDir = showDir;
      this.datasource = datasource;
    }

    @Override
    public String call() throws Exception {
      LOGGER.info("start parsing " + showDir);

      HashSet<Path> allFiles = getAllFilesRecursive(showDir, Integer.MAX_VALUE);
      filesFound.add(showDir.toAbsolutePath()); // our global cache
      filesFound.addAll(allFiles); // our global cache

      // convert to MFs (we need it anyways at the end)
      ArrayList<MediaFile> mfs = new ArrayList<MediaFile>();
      for (Path file : allFiles) {
        mfs.add(new MediaFile(file));
      }
      allFiles.clear();

      // search for this tvshow folder in database
      TvShow tvShow = tvShowList.getTvShowByPath(showDir);
      // FIXME: create a method to get a MF solely by constant name like SHOW_NFO or SEASON_BANNER
      MediaFile showNFO = new MediaFile(showDir.resolve("tvshow.nfo"), MediaFileType.NFO); // fixate
      if (tvShow == null) {
        // tvShow did not exist - try to parse a NFO file in parent folder
        tvShow = TvShowToXbmcNfoConnector.getData(showNFO.getFile()); // FIXME: get at end from tmp.mf.list
        if (tvShow == null) {
          // create new one
          tvShow = new TvShow();
          String[] ty = ParserUtils.detectCleanMovienameAndYear(showDir.getFileName().toString());
          tvShow.setTitle(ty[0]);
          if (!ty[1].isEmpty()) {
            tvShow.setYear(ty[1]);
          }
        }

        if (tvShow != null) {
          tvShow.setPath(showDir.toAbsolutePath().toString());
          tvShow.setDataSource(datasource.toString());
          // tvShow.saveToDb();
          tvShow.setNewlyAdded(true);
          tvShowList.addTvShow(tvShow);
        }
      }

      // ******************************
      // STEP 1 - get all video MFs and create episodes, then match same named MFs (nfo, thumb)
      // ******************************
      for (MediaFile mf : getMediaFiles(mfs, MediaFileType.VIDEO)) {
        if (mf.isDiscFile()) {
          LOGGER.error("********************** DISC FILE: not yet! " + mf);
          continue;
        }

        // build an array of MFs, which start with the same name as video MF (aka episode MFs)
        String basename = FilenameUtils.getBaseName(mf.getFilenameWithoutStacking());
        List<MediaFile> epFiles = new ArrayList<MediaFile>();
        for (MediaFile em : mfs) {
          if (em.getFilename().startsWith(basename)) {
            epFiles.add(em);
          }
        }

        // is this file already assigned to another episode?
        List<TvShowEpisode> episodes = tvShowList.getTvEpisodesByFile(tvShow, mf.getFile());
        if (episodes.size() == 0) {

          // ******************************
          // STEP 1.1 - parse EP NFO
          // ******************************
          MediaFile epNfo = getMediaFile(epFiles, MediaFileType.NFO);
          if (epNfo != null) {
            LOGGER.info("found episode NFO - try to parse '" + epNfo.getFilename() + "'");
            List<TvShowEpisode> episodesInNfo = TvShowEpisode.parseNFO(epNfo);
            // did we find any episodes in the NFO?
            if (episodesInNfo.size() > 0) {
              // these have priority!
              for (TvShowEpisode e : episodesInNfo) {
                e.setPath(mf.getPath());
                e.setTvShow(tvShow);
                e.setDateAddedFromMediaFile(mf);
                if (e.getMediaSource() == MediaSource.UNKNOWN) {
                  e.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
                }
                e.setNewlyAdded(true);
                e.addToMediaFiles(epFiles); // all found EP MFs
                e.saveToDb();
                tvShow.addEpisode(e);
              }
              continue; // with next video MF
            }
          }

          // ******************************
          // STEP 1.2 - no NFO? try to parse episode/season
          // ******************************
          String relativePath = showDir.relativize(mf.getFileAsPath()).toString();
          EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(relativePath, tvShow.getTitle());

          // second check: is the detected episode (>-1; season >-1) already in tmm and any valid stacking markers found?
          if (result.episodes.size() == 1 && result.season > -1 && result.stackingMarkerFound) {
            // get any assigned episode
            TvShowEpisode ep = tvShow.getEpisode(result.season, result.episodes.get(0));
            if (ep != null) {
              ep.setNewlyAdded(true);
              ep.addToMediaFiles(mf);
              continue;
            }
          }
          if (result.episodes.size() == 0) {
            // try to parse out episodes/season from parent directory
            result = TvShowEpisodeAndSeasonParser.detectEpisodeFromDirectory(showDir.toFile(), tvShow.getPath());
          }
          if (result.season == -1) {
            // did the search find a season?
            // no -> search for it in the folder name (relative path between tv show root and the current dir)
            result.season = TvShowEpisodeAndSeasonParser.detectSeason(relativePath);
          }
          if (result.episodes.size() > 0) {
            // something found with the season detection?
            for (int ep : result.episodes) {
              TvShowEpisode episode = new TvShowEpisode();
              episode.setDvdOrder(Globals.settings.getTvShowSettings().isDvdOrder());
              episode.setEpisode(ep);
              episode.setSeason(result.season);
              episode.setFirstAired(result.date);
              if (result.name.isEmpty()) {
                result.name = FilenameUtils.getBaseName(mf.getFilename());
              }
              episode.setTitle(result.name);
              episode.setPath(mf.getPath());
              episode.setTvShow(tvShow);
              episode.addToMediaFiles(epFiles); // all found EP MFs
              episode.setDateAddedFromMediaFile(mf);
              if (episode.getMediaSource() == MediaSource.UNKNOWN) {
                episode.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
              }
              episode.setNewlyAdded(true);
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
          else {
            // ******************************
            // STEP 1.3 - episode detection found nothing - simply add this file
            // ******************************
            TvShowEpisode episode = new TvShowEpisode();
            episode.setDvdOrder(Globals.settings.getTvShowSettings().isDvdOrder());
            episode.setEpisode(-1);
            episode.setSeason(-1);
            episode.setPath(mf.getPath());
            episode.setTitle(FilenameUtils.getBaseName(mf.getFilename()));
            episode.setTvShow(tvShow);
            episode.setFirstAired(result.date); // maybe found
            episode.addToMediaFiles(epFiles); // all found EP MFs
            episode.setDateAddedFromMediaFile(mf);
            if (episode.getMediaSource() == MediaSource.UNKNOWN) {
              episode.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
            }
            episode.setNewlyAdded(true);
            episode.saveToDb();
            tvShow.addEpisode(episode);
          }
        } // end creation of new episodes
        else {
          // ******************************
          // STEP 1.4 - video MF was already found in DB - just add all non-video MFs
          // ******************************
          for (TvShowEpisode episode : episodes) {
            for (MediaFile additional : getMediaFilesExceptType(epFiles, MediaFileType.VIDEO)) {
              episode.addToMediaFiles(additional); // add one by one, to only add NON-existing
            }
            episode.saveToDb();
          }
        }
      } // end STEP 1 - for all video MFs

      // ******************************
      // STEP2 - now we have a working show/episode object
      // remove all used episode MFs, rest must be show MFs ;)
      // ******************************
      mfs.removeAll(tvShow.getEpisodesMediaFiles()); // remove EP files
      tvShow.addToMediaFiles(mfs); // add remaining
      tvShow.saveToDb();

      return showDir.getFileName().toString();
    }

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
    List<MediaFile> mf = new ArrayList<MediaFile>();
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

  /**
   * returns all MFs NOT matching specified type
   * 
   * @param mfs
   *          array to search
   * @param types
   *          MF types to exclude
   * @return list of matching MFs
   */
  private List<MediaFile> getMediaFilesExceptType(List<MediaFile> mfs, MediaFileType... types) {
    List<MediaFile> mf = new ArrayList<MediaFile>();
    for (MediaFile mediaFile : mfs) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (!match) {
        mf.add(new MediaFile(mediaFile));
      }
    }
    return mf;
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
          fileNames.add(path.toAbsolutePath());
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
        fileNames.add(path.toAbsolutePath());
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
      visFile++;
      if (attr.isRegularFile()) {
        fFound.add(file.toAbsolutePath());
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
      return CONTINUE;
    }

    // If there is some error accessing the file, let the user know.
    // If you don't override this method and an error occurs, an IOException is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      LOGGER.error("" + exc);
      return CONTINUE;
    }
  }
}
