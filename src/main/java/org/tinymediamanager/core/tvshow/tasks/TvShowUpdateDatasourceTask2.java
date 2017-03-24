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
package org.tinymediamanager.core.tvshow.tasks;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser.EpisodeMatchingResult;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.connector.TvShowToXbmcNfoConnector;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.thirdparty.VSMeta;
import org.tinymediamanager.ui.UTF8Control;

import com.sun.jna.Platform;

/**
 * The Class TvShowUpdateDataSourcesTask.
 * 
 * @author Manuel Laggner
 */

public class TvShowUpdateDatasourceTask2 extends TmmThreadPool {
  private static final Logger         LOGGER        = LoggerFactory.getLogger(TvShowUpdateDatasourceTask2.class);
  private static final ResourceBundle BUNDLE        = ResourceBundle.getBundle("messages", new UTF8Control());                                  //$NON-NLS-1$

  // skip well-known, but unneeded folders (UPPERCASE)
  private static final List<String>   skipFolders   = Arrays.asList(".", "..", "CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "SSIF", "AUXDATA",
      "AUDIO_TS", "$RECYCLE.BIN", "RECYCLER", "SYSTEM VOLUME INFORMATION", "@EADIR");

  // skip folders starting with a SINGLE "." or "._"
  private static final String         skipRegex     = "^[.][\\w@]+.*";

  private static final Pattern        seasonPattern = Pattern.compile("(?i)season([0-9]{0,2}|-specials)-poster\\..{2,4}");

  private static long                 preDir        = 0;
  private static long                 postDir       = 0;
  private static long                 visFile       = 0;

  private List<String>                dataSources;
  private List<Path>                  tvShowFolders = new ArrayList<>();
  private TvShowList                  tvShowList;
  private HashSet<Path>               filesFound    = new HashSet<>();

  /**
   * Instantiates a new scrape task - to update all datasources
   * 
   */
  public TvShowUpdateDatasourceTask2() {
    super(BUNDLE.getString("update.datasource"));
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<>(TvShowModuleManager.SETTINGS.getTvShowDataSource());
  }

  /**
   * Instantiates a new scrape task - to update a single datasource
   * 
   * @param datasource
   *          the data source to start the task for
   */
  public TvShowUpdateDatasourceTask2(String datasource) {
    super(BUNDLE.getString("update.datasource") + " (" + datasource + ")");
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<>(1);
    dataSources.add(datasource);
  }

  /**
   * Instantiates a new scrape task - to update given tv shows
   * 
   * @param tvShowFolders
   *          a list of TV show folders to start the task for
   */
  public TvShowUpdateDatasourceTask2(List<Path> tvShowFolders) {
    super(BUNDLE.getString("update.datasource"));
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<>(0);
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
    preDir = 0;
    postDir = 0;
    visFile = 0;

    try {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      start();

      // get existing show folders
      List<Path> existing = new ArrayList<>();
      for (TvShow show : tvShowList.getTvShows()) {
        existing.add(show.getPathNIO());
      }

      // here we have 2 ways of updating:
      // - per datasource -> update ds / remove orphaned / update MFs
      // - per TV show -> udpate TV show / update MFs
      if (tvShowFolders.isEmpty()) {
        // update selected data sources
        for (String ds : dataSources) {
          LOGGER.info("Start UDS on datasource: " + ds);
          Path dsAsPath = Paths.get(ds);

          // first of all check if the DS is available; we can take the
          // Files.exist here:
          // if the DS exists (and we have access to read it): Files.exist =
          // true
          if (!Files.exists(dsAsPath)) {
            // error - continue with next datasource
            LOGGER.warn("Datasource not available/empty " + ds);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable", new String[] { ds }));
            continue;
          }

          initThreadPool(3, "update"); // FIXME: more threads result in
                                       // duplicate tree entries :/
          List<Path> newTvShowDirs = new ArrayList<>();
          List<Path> existingTvShowDirs = new ArrayList<>();
          List<Path> rootList = listFilesAndDirs(dsAsPath);

          // when there is _nothing_ found in the ds root, it might be offline -
          // skip further processing
          // not in Windows since that won't happen there
          if (rootList.isEmpty() && !Platform.isWindows()) {
            // error - continue with next datasource
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable", new String[] { ds }));
            continue;
          }

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
              // File in root folder - not possible for TV datasource (at least, for videos ;)
              String ext = FilenameUtils.getExtension(path.getFileName().toString());
              if (Globals.settings.getVideoFileType().contains("." + ext)) {
                MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.episodeinroot",
                    new String[] { path.getFileName().toString() }));
              }
            }
          }

          for (Path subdir : newTvShowDirs) {
            submitTask(new FindTvShowTask(subdir, dsAsPath.toAbsolutePath()));
          }
          for (Path subdir : existingTvShowDirs) {
            submitTask(new FindTvShowTask(subdir, dsAsPath.toAbsolutePath()));
          }
          waitForCompletionOrCancel();
          if (cancel) {
            break;
          }

          cleanupDatasource(ds);
          waitForCompletionOrCancel();
          if (cancel) {
            break;
          }
        } // end forech datasource
      }
      else {
        initThreadPool(3, "update");
        // update selected TV shows
        for (Path path : tvShowFolders) {
          // first of all check if the DS is available; we can take the
          // Files.exist here:
          // if the DS exists (and we have access to read it): Files.exist =
          // true
          if (!Files.exists(path)) {
            // error - continue with next datasource
            LOGGER.warn("Datasource not available/empty " + path.toAbsolutePath().toString());
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable",
                new String[] { path.toAbsolutePath().toString() }));
            continue;
          }
          submitTask(new FindTvShowTask(path, path.getParent().toAbsolutePath()));
        }
        waitForCompletionOrCancel();

        if (!cancel) {
          cleanupShows();
          waitForCompletionOrCancel();
        }
      }

      LOGGER.info("getting Mediainfo...");
      initThreadPool(1, "mediainfo");
      setTaskName(BUNDLE.getString("update.mediainfo"));
      setTaskDescription(null);
      setProgressDone(0);
      // gather MediaInformation for ALL shows - TBD
      if (!cancel) {
        if (tvShowFolders.isEmpty()) {
          // get MI for selected DS
          for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
            if (cancel) {
              break;
            }
            TvShow tvShow = tvShowList.getTvShows().get(i);
            if (dataSources.contains(tvShow.getDataSource())) {
              gatherMediaInformationForUngatheredMediaFiles(tvShow);
            }
          }
        }
        else {
          // get MI for selected TV shows
          for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
            if (cancel) {
              break;
            }
            TvShow tvShow = tvShowList.getTvShows().get(i);
            if (tvShowFolders.contains(tvShow.getPathNIO())) {
              gatherMediaInformationForUngatheredMediaFiles(tvShow);
            }
          }
        }
        waitForCompletionOrCancel();
      }

      stopWatch.stop();
      LOGGER.info("Done updating datasource :) - took " + stopWatch);

      LOGGER.debug("FilesFound " + filesFound.size());
      LOGGER.debug("tvShowsFound " + tvShowList.getTvShowCount());
      LOGGER.debug("episodesFound " + tvShowList.getEpisodeCount());
      LOGGER.debug("PreDir " + preDir);
      LOGGER.debug("PostDir " + postDir);
      LOGGER.debug("VisFile " + visFile);
      preDir = 0;
      postDir = 0;
      visFile = 0;
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
  }

  private void cleanupShows() {
    setTaskName(BUNDLE.getString("update.cleanup"));
    setTaskDescription(null);
    setProgressDone(0);
    setWorkUnits(0);
    publishState();

    LOGGER.info("removing orphaned movies/files...");
    for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      TvShow tvShow = tvShowList.getTvShows().get(i);

      // check only Tv shows matching datasource
      if (!tvShowFolders.contains(tvShow.getPathNIO())) {
        continue;
      }

      if (!Files.exists(tvShow.getPathNIO())) {
        tvShowList.removeTvShow(tvShow);
      }
      else {
        cleanup(tvShow);
      }
    }
  }

  private void cleanupDatasource(String datasource) {
    setTaskName(BUNDLE.getString("update.cleanup"));
    setTaskDescription(null);
    setProgressDone(0);
    setWorkUnits(0);
    publishState();
    LOGGER.info("removing orphaned tv shows/files...");

    for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      TvShow tvShow = tvShowList.getTvShows().get(i);

      // check only Tv shows matching datasource
      if (!Paths.get(datasource).toAbsolutePath().equals(Paths.get(tvShow.getDataSource()).toAbsolutePath())) {
        continue;
      }

      if (!Files.exists(tvShow.getPathNIO())) {
        tvShowList.removeTvShow(tvShow);
      }
      else {
        cleanup(tvShow);
      }
    }
  }

  private void cleanup(TvShow tvShow) {
    boolean dirty = false;
    if (!tvShow.isNewlyAdded() || tvShow.hasNewlyAddedEpisodes()) {
      // check and delete all not found MediaFiles
      List<MediaFile> mediaFiles = new ArrayList<>(tvShow.getMediaFiles());
      for (MediaFile mf : mediaFiles) {
        if (!filesFound.contains(mf.getFileAsPath())) {
          if (!mf.exists()) {
            LOGGER.debug("removing orphaned file: " + mf.getFileAsPath());
            tvShow.removeFromMediaFiles(mf);
            dirty = true;
          }
          else {
            LOGGER.warn("file " + mf.getFileAsPath() + " not in hashset, but on hdd!");
          }
        }
      }
      List<TvShowEpisode> episodes = new ArrayList<>(tvShow.getEpisodes());
      for (TvShowEpisode episode : episodes) {
        mediaFiles = new ArrayList<>(episode.getMediaFiles());
        for (MediaFile mf : mediaFiles) {
          if (!filesFound.contains(mf.getFileAsPath())) {
            if (!mf.exists()) {
              LOGGER.debug("removing orphaned file: " + mf.getFileAsPath());
              episode.removeFromMediaFiles(mf);
              dirty = true;
            }
            else {
              LOGGER.warn("file " + mf.getFileAsPath() + " not in hashset, but on hdd!");
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
    for (MediaFile mf : tvShow.getMediaFiles()) {
      if (StringUtils.isBlank(mf.getContainerFormat())) {
        submitTask(new MediaFileInformationFetcherTask(mf, tvShow, false));
      }
    }

    // get mediainfo for all episodes within this tv show
    for (TvShowEpisode episode : new ArrayList<>(tvShow.getEpisodes())) {
      for (MediaFile mf : episode.getMediaFiles()) {
        if (StringUtils.isBlank(mf.getContainerFormat())) {
          submitTask(new MediaFileInformationFetcherTask(mf, episode, false));
        }
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
    private long uniqueId;

    /**
     * Instantiates a new find tv show task.
     * 
     * @param showDir
     *          the subdir
     * @param datasource
     *          the datasource
     */
    public FindTvShowTask(Path showDir, Path datasource) {
      this.showDir = showDir;
      this.datasource = datasource;
      this.uniqueId = TmmTaskManager.getInstance().GLOB_THRD_CNT.incrementAndGet();
    }

    @Override
    public String call() throws Exception {
      String name = Thread.currentThread().getName();
      if (!name.contains("-G")) {
        name = name + "-G0";
      }
      name = name.replaceAll("\\-G\\d+", "-G" + uniqueId);
      Thread.currentThread().setName(name);

      LOGGER.info("start parsing " + showDir);
      if (showDir.getFileName().toString().matches(skipRegex)) {
        LOGGER.debug("Skipping dir: " + showDir);
        return "";
      }

      HashSet<Path> allFiles = getAllFilesRecursive(showDir, Integer.MAX_VALUE);
      if (allFiles != null && allFiles.isEmpty()) {
        LOGGER.info("skip empty directory " + showDir);
        return "";
      }
      filesFound.add(showDir.toAbsolutePath()); // our global cache
      filesFound.addAll(allFiles); // our global cache

      // convert to MFs (we need it anyways at the end)
      ArrayList<MediaFile> mfs = new ArrayList<>();
      for (Path file : allFiles) {
        if (!file.getFileName().toString().matches(skipRegex)) {
          mfs.add(new MediaFile(file));
        }
      }
      allFiles.clear();

      if (getMediaFiles(mfs, MediaFileType.VIDEO).size() == 0) {
        LOGGER.info("no video file found in directory " + showDir);
        return "";
      }

      // ******************************
      // STEP 1 - get (or create) TvShow object
      // ******************************
      TvShow tvShow = tvShowList.getTvShowByPath(showDir);
      // FIXME: create a method to get a MF solely by constant name like
      // SHOW_NFO or SEASON_BANNER
      MediaFile showNFO = new MediaFile(showDir.resolve("tvshow.nfo"), MediaFileType.NFO); // fixate
      if (tvShow == null) {
        // tvShow did not exist - try to parse a NFO file in parent folder
        if (Files.exists(showNFO.getFileAsPath())) {
          tvShow = TvShowToXbmcNfoConnector.getData(showNFO.getFileAsPath().toFile());
        }
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
      // STEP 2 - get all video MFs and get or create episodes
      // ******************************
      HashSet<Path> discFolders = new HashSet<>();
      for (MediaFile mf : getMediaFiles(mfs, MediaFileType.VIDEO)) {

        // build an array of MFs, which might be in same episode
        List<MediaFile> epFiles = new ArrayList<>();

        if (mf.isDiscFile()) {
          // find EP root folder, and do not walk lower than showDir!
          Path discRoot = mf.getFileAsPath().getParent().toAbsolutePath(); // folder
          String folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // relative
          while (folder.contains("BDMV") || folder.contains("VIDEO_TS")) {
            discRoot = discRoot.getParent();
            folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // reevaluate
          }
          if (discFolders.contains(discRoot)) {
            // we already parsed one disc file (which adds all other videos), so
            // break here already
            continue;
          }
          discFolders.add(discRoot);
          // add all known files starting with same discRootDir
          for (MediaFile em : mfs) {
            if (em.getFileAsPath().startsWith(discRoot)) {
              if (em.getType() != MediaFileType.UNKNOWN) {
                epFiles.add(em);
              }
            }
          }
        }
        else {
          // normal episode file - get all same named files
          String basename = FilenameUtils.getBaseName(mf.getFilenameWithoutStacking());
          LOGGER.trace("UDS: basename: " + basename);
          for (MediaFile em : mfs) {
            String emBasename = FilenameUtils.getBaseName(em.getFilename());
            String epNameRegexp = Pattern.quote(basename) + "[\\s.,_-].*";
            // same named files or thumb files
            if (emBasename.equals(basename) || emBasename.matches(epNameRegexp)) {
              // we found some graphics named like the episode - define them as thumb here
              if (em.getType() == MediaFileType.GRAPHIC) {
                em.setType(MediaFileType.THUMB);
              }
              epFiles.add(em);
              LOGGER.trace("UDS: found matching MF: " + em);
            }
          }
        }

        // ******************************
        // STEP 2.1 - is this file already assigned to another episode?
        // ******************************
        List<TvShowEpisode> episodes = tvShowList.getTvEpisodesByFile(tvShow, mf.getFile());
        if (episodes.size() == 0) {

          // ******************************
          // STEP 2.1.1 - parse EP NFO (has precedence over files)
          // ******************************
          MediaFile meta = getMediaFile(epFiles, MediaFileType.VSMETA);
          TvShowEpisode vsMetaEP = null;
          if (meta != null) {
            VSMeta vsmeta = new VSMeta();
            vsmeta.parseFile(meta.getFileAsPath());
            vsMetaEP = vsmeta.getTvShowEpisode();
          }

          MediaFile epNfo = getMediaFile(epFiles, MediaFileType.NFO);
          if (epNfo != null) {
            LOGGER.info("found episode NFO - try to parse '" + showDir.relativize(epNfo.getFileAsPath()) + "'");
            List<TvShowEpisode> episodesInNfo = TvShowEpisode.parseNFO(epNfo);
            // did we find any episodes in the NFO?
            if (episodesInNfo.size() > 0) {
              // these have priority!
              for (TvShowEpisode episode : episodesInNfo) {
                episode.setPath(mf.getPath());
                episode.setTvShow(tvShow);
                episode.setDateAddedFromMediaFile(mf);
                if (episode.getMediaSource() == MediaSource.UNKNOWN) {
                  episode.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
                }
                episode.setNewlyAdded(true);
                episode.addToMediaFiles(epFiles); // all found EP MFs

                if (mf.isDiscFile()) {
                  episode.setDisc(true);

                  // set correct EP path in case of disc files
                  Path discRoot = mf.getFileAsPath().getParent().toAbsolutePath(); // folder
                  String folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // relative
                  while (folder.contains("BDMV") || folder.contains("VIDEO_TS")) {
                    discRoot = discRoot.getParent();
                    folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // reevaluate
                  }
                  episode.setPath(discRoot.toAbsolutePath().toString());
                }

                if (episodesInNfo.size() > 1) {
                  episode.setMultiEpisode(true);
                }
                else {
                  episode.setMultiEpisode(false);
                }
                episode.merge(vsMetaEP); // merge VSmeta infos

                episode.saveToDb();
                tvShow.addEpisode(episode);
              }
              continue; // with next video MF
            }
          } // end parse NFO

          // ******************************
          // STEP 2.1.2 - no NFO? try to parse episode/season
          // ******************************
          String relativePath = showDir.relativize(mf.getFileAsPath()).toString();
          EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(relativePath, tvShow.getTitle());

          // second check: is the detected episode (>-1; season >-1) already in
          // tmm and any valid stacking markers
          // found?
          // FIXME: uhm.. for what is that?!?
          if (result.episodes.size() == 1 && result.season > -1 && result.stackingMarkerFound) {
            // get any assigned episode
            TvShowEpisode ep = tvShow.getEpisode(result.season, result.episodes.get(0));
            if (ep != null) {
              ep.setNewlyAdded(true);
              ep.addToMediaFiles(mf);
              continue;
            }
          }
          if (result.episodes.size() == 0 && result.date == null) {
            // try to parse out episodes/season from parent directory (but only if we haven't detected an ared date!)
            result = TvShowEpisodeAndSeasonParser.detectEpisodeFromDirectory(showDir.toFile(), tvShow.getPath());
          }
          if (result.season == -1) {
            // did the search find a season?
            // no -> search for it in the folder name (relative path between tv
            // show root and the current dir)
            result.season = TvShowEpisodeAndSeasonParser.detectSeason(relativePath);
          }
          if (result.episodes.size() > 0) {
            // something found with the season detection?
            for (int ep : result.episodes) {
              TvShowEpisode episode = new TvShowEpisode();
              episode.setDvdOrder(TvShowModuleManager.SETTINGS.isDvdOrder());
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

              if (mf.isDiscFile()) {
                episode.setDisc(true);

                // set correct EP path in case of disc files
                Path discRoot = mf.getFileAsPath().getParent().toAbsolutePath(); // folder
                String folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // relative
                while (folder.contains("BDMV") || folder.contains("VIDEO_TS")) {
                  discRoot = discRoot.getParent();
                  folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // reevaluate
                }
                episode.setPath(discRoot.toAbsolutePath().toString());
              }

              if (result.episodes.size() > 1) {
                episode.setMultiEpisode(true);
              }
              else {
                episode.setMultiEpisode(false);
              }
              episode.merge(vsMetaEP); // merge VSmeta infos
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
          else {
            // ******************************
            // STEP 2.1.3 - episode detection found nothing - simply add this
            // video as -1/-1
            // ******************************
            TvShowEpisode episode = new TvShowEpisode();
            episode.setDvdOrder(TvShowModuleManager.SETTINGS.isDvdOrder());
            episode.setEpisode(-1);
            episode.setSeason(-1);
            episode.setPath(mf.getPath());

            if (mf.isDiscFile()) {
              episode.setDisc(true);

              // set correct EP path in case of disc files
              Path discRoot = mf.getFileAsPath().getParent().toAbsolutePath(); // folder
              String folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // relative
              while (folder.contains("BDMV") || folder.contains("VIDEO_TS")) {
                discRoot = discRoot.getParent();
                folder = showDir.relativize(discRoot).toString().toUpperCase(Locale.ROOT); // reevaluate
              }
              episode.setPath(discRoot.toAbsolutePath().toString());
            }

            episode.setTitle(FilenameUtils.getBaseName(mf.getFilename()));
            episode.setTvShow(tvShow);
            episode.setFirstAired(result.date); // maybe found
            episode.addToMediaFiles(epFiles); // all found EP MFs
            episode.setDateAddedFromMediaFile(mf);
            if (episode.getMediaSource() == MediaSource.UNKNOWN) {
              episode.setMediaSource(MediaSource.parseMediaSource(mf.getFile().getAbsolutePath()));
            }
            episode.setNewlyAdded(true);
            episode.merge(vsMetaEP); // merge VSmeta infos
            episode.saveToDb();
            tvShow.addEpisode(episode);
          }
        } // end creation of new episodes
        else {
          // ******************************
          // STEP 2.2 - video MF was already found in DB - just add all
          // non-video MFs
          // ******************************
          for (TvShowEpisode episode : episodes) {
            episode.addToMediaFiles(epFiles); // add all (dupes will be
                                              // filtered)
            episode.setDisc(mf.isDiscFile());
            if (episodes.size() > 1) {
              episode.setMultiEpisode(true);
            }
            else {
              episode.setMultiEpisode(false);
            }
            episode.saveToDb();
          }
        }
      } // end for all video MFs loop

      // ******************************
      // STEP 3 - now we have a working show/episode object
      // remove all used episode MFs, rest must be show MFs ;)
      // ******************************
      mfs.removeAll(tvShow.getEpisodesMediaFiles()); // remove EP files

      // tvShow.addToMediaFiles(mfs); // add remaining
      // not so fast - try to parse S/E from remaining first!
      for (MediaFile mf : mfs) {
        String relativePath = showDir.relativize(mf.getFileAsPath()).toString();
        EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(relativePath, tvShow.getTitle());
        if (result.season > 0 && !result.episodes.isEmpty()) {
          for (int epnr : result.episodes) {
            TvShowEpisode ep = tvShow.getEpisode(result.season, epnr);
            if (ep != null && mf.getType() != MediaFileType.SEASON_POSTER) {
              ep.addToMediaFiles(mf);
            }
          }
        }
      }
      mfs.removeAll(tvShow.getEpisodesMediaFiles()); // remove EP files
      tvShow.addToMediaFiles(mfs); // now add remaining

      // fill season posters map
      for (MediaFile mf : getMediaFiles(mfs, MediaFileType.SEASON_POSTER)) {
        Matcher matcher = seasonPattern.matcher(mf.getFilename());
        if (matcher.matches()) {
          try {
            int season = Integer.parseInt(matcher.group(1));
            LOGGER.debug("found season poster " + mf.getFileAsPath());
            tvShow.setSeasonPoster(season, mf);
          }
          catch (Exception e) {
            if (mf.getFilename().startsWith("season-specials-poster")) {
              LOGGER.debug("found season specials poster " + mf.getFileAsPath());
              tvShow.setSeasonPoster(-1, mf);
            }
          }
        }
      }

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
    List<MediaFile> mf = new ArrayList<>();
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
    // do not publish task description here, because with different workers the
    // text is never right
    publishState(progressDone);
  }

  /**
   * simple NIO File.listFiles() replacement<br>
   * returns ONLY regular files (NO folders, NO hidden) in specified dir (NOT recursive)
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
              && !TvShowModuleManager.SETTINGS.getTvShowSkipFolders().contains(path.toFile().getAbsolutePath())) {
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
   * returns all files & folders in specified dir (NOT recursive)
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
            && !TvShowModuleManager.SETTINGS.getTvShowSkipFolders().contains(path.toFile().getAbsolutePath())) {
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
      visFile++;
      if (Utils.isRegularFile(attr) && !file.getFileName().toString().matches(skipRegex)) {
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
      if (dir.getFileName() != null
          && (Files.exists(dir.resolve(".tmmignore")) || Files.exists(dir.resolve("tmmignore")) || Files.exists(dir.resolve(".nomedia"))
              || skipFolders.contains(dir.getFileName().toString().toUpperCase(Locale.ROOT)) || dir.getFileName().toString().matches(skipRegex))
          || TvShowModuleManager.SETTINGS.getTvShowSkipFolders().contains(dir.toFile().getAbsolutePath())) {
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
    // If you don't override this method and an error occurs, an IOException is
    // thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      LOGGER.error("" + exc);
      return CONTINUE;
    }
  }
}
