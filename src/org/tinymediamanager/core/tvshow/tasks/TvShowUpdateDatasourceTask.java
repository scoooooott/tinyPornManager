/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser.EpisodeMatchingResult;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class TvShowUpdateDataSourcesTask.
 * 
 * @author Manuel Laggner
 */

public class TvShowUpdateDatasourceTask extends TmmThreadPool {
  private static final Logger         LOGGER                = LoggerFactory.getLogger(TvShowUpdateDatasourceTask.class);
  private static final ResourceBundle BUNDLE                = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  // skip well-known, but unneeded folders (UPPERCASE)
  private static final List<String>   skipFolders           = Arrays.asList(".", "..", "CERTIFICATE", "BACKUP", "PLAYLIST", "CLPINF", "SSIF",
                                                                "AUXDATA", "AUDIO_TS", "$RECYCLE.BIN", "RECYCLER", "SYSTEM VOLUME INFORMATION",
                                                                "@EADIR");

  // skip folders starting with a SINGLE "." or "._"
  private static final String         skipFoldersRegex      = "^[.][\\w]+.*";

  // MacOS ignore
  private static final String         skipFilesStartingWith = "._";

  // regexp patterns for artwork search
  private static final Pattern        posterPattern1        = Pattern.compile("(?i)(poster|folder)\\..{2,4}");
  private static final Pattern        posterPattern2        = Pattern.compile("(?i).*-poster\\..{2,4}");
  private static final Pattern        fanartPattern1        = Pattern.compile("(?i)fanart\\..{2,4}");
  private static final Pattern        fanartPattern2        = Pattern.compile("(?i).*(-|.)fanart\\..{2,4}");
  private static final Pattern        bannerPattern1        = Pattern.compile("(?i)banner\\..{2,4}");
  private static final Pattern        bannerPattern2        = Pattern.compile("(?i).*(-|.)banner\\..{2,4}");
  private static final Pattern        clearartPattern1      = Pattern.compile("(?i)clearart\\..{2,4}");
  private static final Pattern        clearartPattern2      = Pattern.compile("(?i).*(-|.)clearart\\..{2,4}");
  private static final Pattern        logoPattern1          = Pattern.compile("(?i)logo\\..{2,4}");
  private static final Pattern        logoPattern2          = Pattern.compile("(?i).*(-|.)logo\\..{2,4}");
  private static final Pattern        thumbPattern1         = Pattern.compile("(?i)thumb\\..{2,4}");
  private static final Pattern        thumbPattern2         = Pattern.compile("(?i).*(-|.)thumb\\..{2,4}");
  private static final Pattern        seasonPattern         = Pattern.compile("(?i)season([0-9]{0,2}|-specials)-poster\\..{2,4}");

  private List<String>                dataSources;
  private List<File>                  tvShowFolders         = new ArrayList<File>();
  private TvShowList                  tvShowList;

  /**
   * Instantiates a new scrape task - to update all datasources
   * 
   */
  public TvShowUpdateDatasourceTask() {
    super(BUNDLE.getString("update.datasource"));
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getTvShowSettings().getTvShowDataSource());
  }

  /**
   * Instantiates a new scrape task - to update a single datasource
   * 
   * @param datasource
   */
  public TvShowUpdateDatasourceTask(String datasource) {
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
  public TvShowUpdateDatasourceTask(List<File> tvShowFolders) {
    super(BUNDLE.getString("update.datasource"));
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(0);
    this.tvShowFolders.addAll(tvShowFolders);
  }

  @Override
  public void doInBackground() {
    try {
      long start = System.currentTimeMillis();
      start();

      // cleanup just added for a new UDS run
      for (TvShow tvShow : tvShowList.getTvShows()) {
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          episode.justAdded = false;
        }
        tvShow.justAdded = false;
      }

      // here we have 2 ways of updating:
      // - per datasource -> update ds / remove orphaned / update MFs
      // - per TV show -> udpate TV show / update MFs
      if (tvShowFolders.size() == 0) {
        // update ds
        updateDatasource();
      }
      else {
        // update TV show
        updateTvShows();
      }
      long end = System.currentTimeMillis();
      LOGGER.info("Done updating datasource :) - took " + Utils.MSECtoHHMMSS(end - start));
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
  }

  /*
   * update one or more datasources
   */
  private void updateDatasource() {
    List<File> imageFiles = new ArrayList<File>();

    for (String path : dataSources) {
      File[] dirs = new File(path).listFiles();
      // check whether the path is accessible (eg disconnected shares)
      if (dirs == null || dirs.length == 0) {
        // error - continue with next datasource
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable",
            new String[] { path }));
        continue;
      }

      // one thread here - more threads killed the UI
      initThreadPool(1, "update");

      for (File subdir : dirs) {
        if (cancel) {
          break;
        }

        String directoryName = subdir.getName();
        // check against unwanted dirs
        if (skipFolders.contains(directoryName.toUpperCase()) || directoryName.matches(skipFoldersRegex)) {
          LOGGER.info("ignoring directory " + directoryName);
          continue;
        }

        // check this dir as TV show dir
        if (subdir.isDirectory()) {
          submitTask(new FindTvShowTask(subdir, path));
        }

        // video FILE in DS root - not supported!
        if (subdir.isFile() && Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(subdir.getName()))) {
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.episodeinroot",
              new String[] { subdir.getName() }));
        }
      }

      waitForCompletionOrCancel();
      if (cancel) {
        break;
      }

      // cleanup
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
        if (!new File(path).equals(new File(tvShow.getDataSource()))) {
          // check only Tv shows matching datasource
          continue;
        }

        File tvShowDir = new File(tvShow.getPath());
        if (!tvShowDir.exists()) {
          tvShowList.removeTvShow(tvShow);
        }
        else {
          // do a cleanup
          cleanup(tvShow);
        }
      }

      // mediainfo
      setTaskName(BUNDLE.getString("update.mediainfo"));
      publishState();

      initThreadPool(1, "mediainfo");
      LOGGER.info("getting Mediainfo...");
      for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
        if (cancel) {
          break;
        }
        TvShow tvShow = tvShowList.getTvShows().get(i);
        if (!new File(path).equals(new File(tvShow.getDataSource()))) {
          // check only Tv shows matching datasource
          continue;
        }

        gatherMediaInformationForUngatheredMediaFiles(tvShow);
      }

      waitForCompletionOrCancel();
      if (cancel) {
        break;
      }

      // build image cache on import
      if (Globals.settings.getTvShowSettings().isBuildImageCacheOnImport()) {
        for (TvShow tvShow : new ArrayList<TvShow>(tvShowList.getTvShows())) {
          if (!new File(path).equals(new File(tvShow.getDataSource()))) {
            continue;
          }
          for (MediaFile mf : new ArrayList<MediaFile>(tvShow.getMediaFiles())) {
            if (mf.isGraphic()) {
              imageFiles.add(mf.getFile());
            }
          }
          for (TvShowEpisode episode : tvShow.getEpisodes()) {
            for (MediaFile mf : new ArrayList<MediaFile>(episode.getMediaFiles())) {
              if (mf.isGraphic()) {
                imageFiles.add(mf.getFile());
              }
            }
          }
        }
      }
    }

    if (cancel) {
      return;
    }

    if (imageFiles.size() > 0) {
      ImageCacheTask task = new ImageCacheTask(imageFiles);
      TmmTaskManager.getInstance().addUnnamedTask(task);
    }

    if (Globals.settings.getTvShowSettings().getSyncTrakt()) {
      TmmTask task = new SyncTraktTvTask(false, false, true, true);
      TmmTaskManager.getInstance().addUnnamedTask(task);
    }
  }

  /*
   * update a single TV show
   */
  private void updateTvShows() {
    // one thread here - more threads killed the UI
    initThreadPool(1, "update");

    for (File tvShowFolder : tvShowFolders) {
      // check if the tv show dir is accessible
      File[] filesInDatasourceRoot = tvShowFolder.getParentFile().listFiles();
      if (filesInDatasourceRoot == null || filesInDatasourceRoot.length == 0) {
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable",
            new String[] { tvShowFolder.getParent() }));
        continue;
      }

      if (tvShowFolder.isDirectory()) {
        submitTask(new FindTvShowTask(tvShowFolder, tvShowFolder.getParent()));
      }
    }

    waitForCompletionOrCancel();

    // cleanup
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
      if (!tvShowFolders.contains(new File(tvShow.getPath()))) {
        continue;
      }

      // check and delete all not found MediaFiles
      cleanup(tvShow);
    }

    // start MI
    setTaskName(BUNDLE.getString("update.mediainfo"));
    publishState();

    initThreadPool(1, "mediainfo");
    LOGGER.info("getting Mediainfo...");
    for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      TvShow tvShow = tvShowList.getTvShows().get(i);

      // check only Tv shows matching datasource
      if (!tvShowFolders.contains(new File(tvShow.getPath()))) {
        continue;
      }

      gatherMediaInformationForUngatheredMediaFiles(tvShow);
    }

    waitForCompletionOrCancel();

    if (cancel) {
      return;
    }

    // build up the image cache
    if (Globals.settings.getTvShowSettings().isBuildImageCacheOnImport()) {
      List<File> imageFiles = new ArrayList<File>();
      for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
        if (cancel) {
          break;
        }
        TvShow tvShow = tvShowList.getTvShows().get(i);

        // check only Tv shows matching datasource
        if (!tvShowFolders.contains(new File(tvShow.getPath()))) {
          continue;
        }

        for (MediaFile mf : new ArrayList<MediaFile>(tvShow.getMediaFiles())) {
          if (mf.isGraphic()) {
            imageFiles.add(mf.getFile());
          }
        }
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          for (MediaFile mf : new ArrayList<MediaFile>(episode.getMediaFiles())) {
            if (mf.isGraphic()) {
              imageFiles.add(mf.getFile());
            }
          }
        }
      }

      ImageCacheTask task = new ImageCacheTask(imageFiles);
      TmmTaskManager.getInstance().addUnnamedTask(task);
    }
  }

  private void cleanup(TvShow tvShow) {
    boolean dirty = false;
    if (!tvShow.justAdded) {
      // check and delete all not found MediaFiles
      List<MediaFile> mediaFiles = new ArrayList<MediaFile>(tvShow.getMediaFiles());
      for (MediaFile mf : mediaFiles) {
        if (!mf.getFile().exists()) {
          tvShow.removeFromMediaFiles(mf);
          dirty = true;
        }
      }
      List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>(tvShow.getEpisodes());
      for (TvShowEpisode episode : episodes) {
        mediaFiles = new ArrayList<MediaFile>(episode.getMediaFiles());
        for (MediaFile mf : mediaFiles) {
          if (!mf.getFile().exists()) {
            episode.removeFromMediaFiles(mf);
            dirty = true;
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
    private File   subdir     = null;
    private String datasource = "";

    /**
     * Instantiates a new find tv show task.
     * 
     * @param subdir
     *          the subdir
     * @param datasource
     *          the datasource
     */
    public FindTvShowTask(File subdir, String datasource) {
      this.subdir = subdir;
      this.datasource = datasource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public String call() throws Exception {
      // get the TV show from this subdir
      createTvShowFromDirectory(subdir, datasource);
      return subdir.getName();
    }
  }

  /**
   * Creates the tv show from directory.
   * 
   * @param dir
   *          the dir
   */
  private void createTvShowFromDirectory(File dir, String datasource) {
    // search for this tvshow folder in database
    TvShow tvShow = tvShowList.getTvShowByPath(dir);
    if (tvShow == null) {
      // tvShow did not exist - try to parse a NFO file in parent folder
      tvShow = TvShow.parseNFO(dir);
      if (tvShow == null) {
        // create new one
        tvShow = new TvShow();
        tvShow.setPath(dir.getPath());
        String[] ty = ParserUtils.detectCleanMovienameAndYear(dir.getName());
        tvShow.setTitle(ty[0]);
        if (!ty[1].isEmpty()) {
          tvShow.setYear(ty[1]);
        }
      }
      if (tvShow != null) {
        tvShow.setDataSource(datasource);
        findAdditionalTvShowFiles(tvShow, dir);
        // tvShow.saveToDb();
        tvShow.justAdded = true;
        tvShowList.addTvShow(tvShow);
      }
    }

    // find episodes in this tv show folder
    if (tvShow != null) {
      findTvEpisodes(tvShow, dir);
      if (tvShow.isNewlyAdded()) {
        tvShow.saveToDb();
      }
    }
  }

  /**
   * Find additional tv show files.
   * 
   * @param tvShow
   *          the tv show
   * @param directory
   *          the directory
   */
  private void findAdditionalTvShowFiles(TvShow tvShow, File directory) {
    // find tv show images for this TV show; NOTE: the NFO has been found in TvShow.parseNFO()
    List<File> completeDirContents = new ArrayList<File>(Arrays.asList(directory.listFiles()));

    // search for poster or download
    findArtwork(tvShow, completeDirContents, posterPattern1, MediaFileType.POSTER);
    findArtwork(tvShow, completeDirContents, posterPattern2, MediaFileType.POSTER);
    downloadArtwork(tvShow, MediaFileType.POSTER);

    // search fanart or download
    findArtwork(tvShow, completeDirContents, fanartPattern1, MediaFileType.FANART);
    findArtwork(tvShow, completeDirContents, fanartPattern2, MediaFileType.FANART);
    downloadArtwork(tvShow, MediaFileType.FANART);

    // search banner or download
    findArtwork(tvShow, completeDirContents, bannerPattern1, MediaFileType.BANNER);
    findArtwork(tvShow, completeDirContents, bannerPattern2, MediaFileType.BANNER);
    downloadArtwork(tvShow, MediaFileType.BANNER);

    // search logo or download
    findArtwork(tvShow, completeDirContents, logoPattern1, MediaFileType.LOGO);
    findArtwork(tvShow, completeDirContents, logoPattern2, MediaFileType.LOGO);
    downloadArtwork(tvShow, MediaFileType.LOGO);

    // search clearart or download
    findArtwork(tvShow, completeDirContents, clearartPattern1, MediaFileType.CLEARART);
    findArtwork(tvShow, completeDirContents, clearartPattern2, MediaFileType.CLEARART);
    downloadArtwork(tvShow, MediaFileType.CLEARART);

    // search thumb or download
    findArtwork(tvShow, completeDirContents, thumbPattern1, MediaFileType.THUMB);
    findArtwork(tvShow, completeDirContents, thumbPattern2, MediaFileType.THUMB);
    downloadArtwork(tvShow, MediaFileType.THUMB);

    // search season posters
    for (File file : completeDirContents) {
      Matcher matcher = seasonPattern.matcher(file.getName());
      if (matcher.matches() && !file.getName().startsWith("._")) { // MacOS ignore
        LOGGER.debug("found season poster " + file.getPath());
        try {
          int season = Integer.parseInt(matcher.group(1));
          tvShow.setSeasonPoster(season, file);
        }
        catch (Exception e) {
        }
      }
      else if (file.getName().startsWith("season-specials-poster")) {
        LOGGER.debug("found season specials poster " + file.getPath());
        tvShow.setSeasonPoster(-1, file);
      }
    }
  }

  private void findArtwork(TvShow show, List<File> directoryContents, Pattern searchPattern, MediaFileType type) {
    for (File file : directoryContents) {
      Matcher matcher = searchPattern.matcher(file.getName());
      if (matcher.matches() && !file.getName().startsWith("._")) { // MacOS ignore
        MediaFile mf = new MediaFile(file, type);
        show.addToMediaFiles(mf);
        LOGGER.debug("found " + mf.getType().name().toLowerCase() + ": " + file.getPath());
        break;
      }
    }
  }

  private void downloadArtwork(TvShow tvShow, MediaFileType type) {
    if (StringUtils.isBlank(tvShow.getArtworkFilename(type)) && StringUtils.isNotBlank(tvShow.getArtworkUrl(type))) {
      tvShow.downloadArtwork(type);
      LOGGER.debug("got " + type.name().toLowerCase() + " url: " + tvShow.getArtworkUrl(type) + " ; try to download this");
    }
  }

  /**
   * Find tv episodes.
   * 
   * @param tvShow
   *          the tv show
   * @param dir
   *          the dir
   */
  private void findTvEpisodes(TvShow tvShow, File dir) {
    LOGGER.debug("parsing " + dir.getPath());
    // crawl this folder and try to find every episode and its corresponding files in it
    File[] content = dir.listFiles();
    Arrays.sort(content);
    for (File file : content) {
      if (file.isFile()) {
        if (!file.getName().startsWith(skipFilesStartingWith)) {
          MediaFile mf = new MediaFile(file);
          // check filetype - we only proceed here if it's a video file
          if (!mf.getType().equals(MediaFileType.VIDEO)) {
            continue;
          }

          // is this file already assigned to another episode?
          List<TvShowEpisode> episodes = tvShowList.getTvEpisodesByFile(tvShow, file);
          if (episodes.size() == 0) {
            // try to check what episode//season
            // EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilename(file);
            String relativePath = new File(tvShow.getPath()).toURI().relativize(file.toURI()).getPath();
            EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(relativePath, tvShow.getTitle());

            // second check: is the detected episode (>-1; season >-1) already in tmm and any valid stacking markers found?
            if (result.episodes.size() == 1 && result.season > -1 && result.stackingMarkerFound) {
              // get any assigned episode
              TvShowEpisode ep = tvShow.getEpisode(result.season, result.episodes.get(0));
              if (ep != null) {
                ep.setNewlyAdded(true);
                ep.addToMediaFiles(new MediaFile(file));
                continue;
              }
            }

            if (result.episodes.size() == 0) {
              // try to parse out episodes/season from parent directory
              result = TvShowEpisodeAndSeasonParser.detectEpisodeFromDirectory(dir, tvShow.getPath());
            }

            if (result.season == -1) {
              // did the search find a season?
              // no -> search for it in the folder name (relative path between tv show root and the current dir)
              result.season = TvShowEpisodeAndSeasonParser.detectSeason(relativePath);
            }

            List<TvShowEpisode> episodesInNfo = TvShowEpisode.parseNFO(file);

            // did we find any episodes in the NFO?
            if (episodesInNfo.size() > 0) {
              // these have priority!
              for (TvShowEpisode e : episodesInNfo) {
                e.setPath(dir.getPath());
                e.setTvShow(tvShow);
                e.addToMediaFiles(new MediaFile(file));
                findAdditionalEpisodeFiles(e, file, content);
                e.setNewlyAdded(true);
                // e.saveToDb();
                tvShow.addEpisode(e);
              }
            }
            else if (result.episodes.size() > 0) {
              // something found with the season detection?
              for (int ep : result.episodes) {
                TvShowEpisode episode = new TvShowEpisode();
                episode.setEpisode(ep);
                episode.setSeason(result.season);
                episode.setFirstAired(result.date);

                if (result.name.isEmpty()) {
                  result.name = FilenameUtils.getBaseName(file.getName());
                }
                episode.setTitle(result.name);

                episode.setPath(dir.getPath());
                episode.setTvShow(tvShow);
                episode.addToMediaFiles(new MediaFile(file));
                findAdditionalEpisodeFiles(episode, file, content);
                episode.setNewlyAdded(true);
                // episode.saveToDb();
                tvShow.addEpisode(episode);
              }
            }
            else {
              // episode detection found nothing - simply add this file
              TvShowEpisode episode = new TvShowEpisode();
              episode.setEpisode(-1);
              episode.setSeason(-1);
              episode.setPath(dir.getPath());

              episode.setTitle(FilenameUtils.getBaseName(file.getName()));
              episode.setTvShow(tvShow);
              episode.setFirstAired(result.date);
              episode.addToMediaFiles(new MediaFile(file));
              findAdditionalEpisodeFiles(episode, file, content);
              episode.setNewlyAdded(true);
              // episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
        }// end skipFilesStartingWith
      } // end isFile

      if (file.isDirectory() && !skipFolders.contains(file.getName().toUpperCase()) && !file.getName().matches(skipFoldersRegex)) {
        // dig deeper
        if (file.getName().toUpperCase().equals("VIDEO_TS")) {
          findTvEpisodesAsDisc(tvShow, file);
        }
        else if (file.getName().toUpperCase().equals("BDMV")) {
          findTvEpisodesAsDisc(tvShow, file);
        }
        else {
          findTvEpisodes(tvShow, file);
        }
      }
    }
  }

  /**
   * Find tv episodes.
   * 
   * @param tvShow
   *          the tv show
   * @param dir
   *          the dir
   */
  private void findTvEpisodesAsDisc(TvShow tvShow, File dir) {
    String parentDir = dir.getParent();
    LOGGER.debug("parsing disc structure in " + dir.getPath() + " parent: " + parentDir);
    // crawl this folder and try to find every episode in it

    List<MediaFile> videoFiles = new ArrayList<MediaFile>();
    File firstVideoFile = null;

    File[] content = dir.listFiles();
    for (File file : content) {
      if (file.isFile()) {
        // check filetype
        if (!Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()).toLowerCase())
            || file.getName().startsWith(skipFilesStartingWith)) { // MacOS ignore
          continue;
        }

        videoFiles.add(new MediaFile(file));
        if (firstVideoFile == null) {
          firstVideoFile = file;
        }
      }
    }

    List<TvShowEpisode> episodes = tvShowList.getTvEpisodesByFile(tvShow, firstVideoFile);
    if (episodes.size() == 0) {
      String relativePath = new File(tvShow.getPath()).toURI().relativize(firstVideoFile.toURI()).getPath();
      EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(relativePath, tvShow.getTitle());

      if (result.season == -1) {
        // did the search find a season?
        // no -> search for it in the folder name (relative path between tv show root and the current dir)
        result.season = TvShowEpisodeAndSeasonParser.detectSeason(relativePath);
      }

      if (result.episodes.size() == 0) {
        // try to parse out episodes/season from parent directory
        result = TvShowEpisodeAndSeasonParser.detectEpisodeFromDirectory(dir.getParentFile(), tvShow.getPath());
      }

      List<TvShowEpisode> episodesInNfo = TvShowEpisode.parseNFO(firstVideoFile);

      // FIXME: Episode root is outside of disc folders ?!
      while (dir.getPath().toUpperCase().contains("BDMV") || dir.getPath().toUpperCase().contains("VIDEO_TS")) {
        dir = dir.getParentFile();
      }

      if (result.episodes.size() > 0) {
        // add it
        for (int ep : result.episodes) {
          TvShowEpisode episode = null;
          // search in the NFO list if an episode has been found
          for (int i = episodesInNfo.size() - 1; i >= 0; i--) {
            TvShowEpisode e = episodesInNfo.get(i);
            if (e.getSeason() == result.season && e.getEpisode() == ep) {
              episode = e;
              episodesInNfo.remove(i);
              break;
            }
          }
          if (episode == null) {
            episode = new TvShowEpisode();
            episode.setEpisode(ep);
            episode.setNewlyAdded(true);
            episode.setSeason(result.season);
            episode.setFirstAired(result.date);
          }

          episode.setPath(dir.getPath());
          episode.setTvShow(tvShow);
          episode.setDisc(true);
          episode.setNewlyAdded(true);
          episode.addToMediaFiles(videoFiles);
          findAdditionalEpisodeFiles(episode, firstVideoFile, content);
          // episode.saveToDb();
          tvShow.addEpisode(episode);
        }
      }
      else {
        // episode detection found nothing - simply add this file
        if (episodesInNfo.size() > 0) {
          for (TvShowEpisode e : episodesInNfo) {
            e.setPath(dir.getPath());
            e.setTvShow(tvShow);
            e.addToMediaFiles(videoFiles);
            e.setNewlyAdded(true);
            // e.findImages();
            findAdditionalEpisodeFiles(e, firstVideoFile, content);
            // e.saveToDb();
            tvShow.addEpisode(e);
          }
        }
        else {
          TvShowEpisode episode = new TvShowEpisode();
          episode.setPath(dir.getPath());
          episode.setEpisode(-1);
          episode.setSeason(-1);
          episode.setTvShow(tvShow);
          episode.setFirstAired(result.date);
          episode.setDisc(true);
          episode.setNewlyAdded(true);
          episode.addToMediaFiles(videoFiles);
          findAdditionalEpisodeFiles(episode, firstVideoFile, content);
          // episode.saveToDb();
          tvShow.addEpisode(episode);
        }
      }
    }

  }

  /**
   * Find additional episode files.<br>
   * adds everything which starts with "videoFile name"<br>
   * scans subs/sample/subtitle directories aswell
   * 
   * @param episode
   *          the episode
   * @param videoFile
   *          the video file
   * @param directoryContents
   *          the directory contents
   */
  private void findAdditionalEpisodeFiles(TvShowEpisode episode, File videoFile, File[] directoryContents) {
    for (File file : directoryContents) {
      if (file.isFile()) {
        MediaFile mf = new MediaFile(file);
        if (mf.getType().equals(MediaFileType.VIDEO) || !mf.getBasename().startsWith(FilenameUtils.getBaseName(videoFile.getName()))
            || file.getName().startsWith(skipFilesStartingWith)) { // MacOS ignore)
          continue;
        }
        if (mf.getType() == MediaFileType.SUBTITLE) {
          episode.setSubtitles(true);
        }
        // check if it is a poster
        if (mf.getType() == MediaFileType.GRAPHIC) {
          LOGGER.debug("parsing unknown graphic " + mf.getFilename());
          String vfilename = FilenameUtils.getBaseName(videoFile.getName());
          if (vfilename.equals(FilenameUtils.getBaseName(mf.getFilename())) // basename match
              || Utils.cleanStackingMarkers(vfilename).trim().equals(FilenameUtils.getBaseName(mf.getFilename())) // basename w/o stacking
              || episode.getTitle().equals(FilenameUtils.getBaseName(mf.getFilename()))) { // title match
            mf.setType(MediaFileType.POSTER);
          }
        }
        episode.addToMediaFiles(mf);
      }
      else {
        if (file.getName().equalsIgnoreCase("sample") || file.getName().equalsIgnoreCase("subs") || file.getName().equalsIgnoreCase("subtitle")) {
          File[] subDirContent = file.listFiles();
          for (File subDirFile : subDirContent) {
            if (FilenameUtils.getBaseName(subDirFile.getName()).startsWith(FilenameUtils.getBaseName(videoFile.getName()))) {
              MediaFile mf = new MediaFile(subDirFile);
              if (mf.getType() == MediaFileType.SUBTITLE) {
                episode.setSubtitles(true);
              }
              episode.addToMediaFiles(mf);
            }
          }
        }
      }
    }
  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the text is never right
    publishState(progressDone);
  }
}
