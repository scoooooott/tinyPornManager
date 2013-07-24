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
package org.tinymediamanager.core.tvshow.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser.EpisodeMatchingResult;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class TvShowUpdateDataSourcesTask.
 * 
 * @author Manuel Laggner
 */

public class TvShowUpdateDatasourceTask extends TmmThreadPool {

  /** The Constant LOGGER. */
  private static final Logger LOGGER        = LoggerFactory.getLogger(TvShowUpdateDatasourceTask.class);

  private List<String>        dataSources;
  private List<File>          tvShowFolders = new ArrayList<File>();
  private TvShowList          tvShowList;

  /**
   * Instantiates a new scrape task - to update all datasources
   * 
   */
  public TvShowUpdateDatasourceTask() {
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getTvShowSettings().getTvShowDataSource());
  }

  /**
   * Instantiates a new scrape task - to update a single datasource
   * 
   * @param datasource
   */
  public TvShowUpdateDatasourceTask(String datasource) {
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
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(0);
    this.tvShowFolders.addAll(tvShowFolders);
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
      // here we have 2 ways of updateing:
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
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
    return null;
  }

  private void updateDatasource() {
    for (String path : dataSources) {
      File[] dirs = new File(path).listFiles();
      // check whether the path is accessible (eg disconnected shares)
      if (dirs == null) {
        // error - continue with next datasource
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable",
            new String[] { path }));
        continue;
      }

      initThreadPool(3, "update");

      for (File subdir : dirs) {
        if (subdir.isDirectory()) {
          submitTask(new FindTvShowTask(subdir, path));
        }
      }

      waitForCompletionOrCancel();

      // cleanup & mediainfo
      startProgressBar("getting Mediainfo & cleanup...");
      initThreadPool(1, "mediainfo");
      LOGGER.info("removing orphaned tv shows/files...");
      for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
        if (cancel) {
          break;
        }
        TvShow tvShow = tvShowList.getTvShows().get(i);
        if (!path.equals(tvShow.getDataSource())) {
          // check only Tv shows matching datasource
          continue;
        }

        File tvShowDir = new File(tvShow.getPath());
        if (!tvShowDir.exists()) {
          tvShowList.removeTvShow(tvShow);
        }
        else {
          // check and delete all not found MediaFiles
          List<MediaFile> mediaFiles = new ArrayList<MediaFile>(tvShow.getMediaFiles());
          for (MediaFile mf : mediaFiles) {
            if (!mf.getFile().exists()) {
              tvShow.removeFromMediaFiles(mf);
            }
          }
          tvShow.saveToDb();
          submitTask(new MediaFileInformationFetcherTask(tvShow.getMediaFiles(), tvShow));
        }
      }
      waitForCompletionOrCancel();
    }
    LOGGER.info("Done updating datasource :)");

    if (cancel) {
      cancel(false);// swing cancel
    }
  }

  private void updateTvShows() {
    initThreadPool(3, "update");

    for (File tvShowFolder : tvShowFolders) {
      // check if the tv show dir is accessible
      if (tvShowFolder.getParentFile().listFiles() == null) {
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "update.datasource.unavailable",
            new String[] { tvShowFolder.getParent() }));
        continue;
      }

      if (tvShowFolder.isDirectory()) {
        submitTask(new FindTvShowTask(tvShowFolder, tvShowFolder.getParent()));
      }
    }

    waitForCompletionOrCancel();

    // cleanup & mediainfo
    startProgressBar("getting Mediainfo & cleanup...");
    initThreadPool(1, "mediainfo");
    LOGGER.info("removing orphaned movies/files...");
    for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
      if (cancel) {
        break;
      }
      TvShow tvShow = tvShowList.getTvShows().get(i);
      if (!tvShowFolders.contains(tvShow.getPath())) {
        // check only Tv shows matching datasource
        continue;
      }

      // check and delete all not found MediaFiles
      List<MediaFile> mediaFiles = new ArrayList<MediaFile>(tvShow.getMediaFiles());
      for (MediaFile mf : mediaFiles) {
        if (!mf.getFile().exists()) {
          tvShow.removeFromMediaFiles(mf);
        }
      }
      tvShow.saveToDb();
      submitTask(new MediaFileInformationFetcherTask(tvShow.getMediaFiles(), tvShow));
    }
    waitForCompletionOrCancel();

    LOGGER.info("Done updating datasource :)");

    if (cancel) {
      cancel(false);// swing cancel
    }
  }

  /**
   * The Class FindTvShowTask.
   * 
   * @author Manuel Laggner
   */
  private class FindTvShowTask implements Callable<Object> {

    /** The subdir. */
    private File   subdir     = null;

    /** The datasource. */
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
    TvShow tvShow = tvShowList.getTvShowByPath(dir.getPath());
    if (tvShow == null) {
      // tvShow did not exist - try to parse a NFO file in parent folder
      tvShow = TvShow.parseNFO(dir);
      if (tvShow == null) {
        // create new one
        tvShow = new TvShow();
        tvShow.setPath(dir.getPath());
        tvShow.setTitle(ParserUtils.detectCleanMoviename(dir.getName()));
      }
      if (tvShow != null) {
        tvShow.setDataSource(datasource);
        findAdditionalTvShowFiles(tvShow, dir);
        tvShow.saveToDb();
        tvShowList.addTvShow(tvShow);
      }
    }

    // find episodes in this tv show folder
    if (tvShow != null) {
      findTvEpisodes(tvShow, dir);
      tvShow.saveToDb();
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
    tvShow.findImages();
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
    for (File file : content) {
      if (file.isFile()) {
        // check filetype - we only proceed here if it's a video file
        if (!Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()).toLowerCase())) {
          continue;
        }

        // is this file already assigned to another episode?
        TvShowEpisode episode = tvShowList.getTvEpisodeByFile(file);
        if (episode == null) {
          // try to check what episode//season
          EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilename(file);
          List<TvShowEpisode> episodesInNfo = TvShowEpisode.parseNFO(file);

          if (result.episodes.size() == 0) {
            // try to parse out episodes/season from parent directory
            result = TvShowEpisodeAndSeasonParser.detectEpisodeFromDirectory(dir, tvShow.getPath());
          }

          if (result.season == -1) {
            // did the search find a season?
            // no -> search for it in the folder name (relative path between tv show root and the current dir)
            result.season = TvShowEpisodeAndSeasonParser.detectSeason(new File(tvShow.getPath()).toURI().relativize(file.toURI()).getPath());
          }

          if (result.episodes.size() == 0) {
            // if episode STILL empty, try Myron's way of parsing - lol
            result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(file.getName(), tvShow.getTitle());
            LOGGER.debug(file.getName() + " - " + result.toString());
          }

          if (result.episodes.size() > 0) {
            // add it
            for (int ep : result.episodes) {
              episode = null;
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
                episode.setSeason(result.season);
                episode.setFirstAired(result.date);

                if (result.name.isEmpty()) {
                  result.name = FilenameUtils.getBaseName(file.getName());
                }
                episode.setTitle(result.name);
              }

              episode.setPath(dir.getPath());
              episode.setTvShow(tvShow);
              episode.addToMediaFiles(new MediaFile(file));
              // episode.findImages();
              findAdditionalEpisodeFiles(episode, file, content);
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }

            // are there still episodes from NFO
            for (TvShowEpisode e : episodesInNfo) {
              e.setPath(dir.getPath());
              e.setTvShow(tvShow);
              e.addToMediaFiles(new MediaFile(file));
              // e.findImages();
              findAdditionalEpisodeFiles(e, file, content);
              e.saveToDb();
              tvShow.addEpisode(e);
            }
          }
          else {
            // episode detection found nothing - simply add this file

            // search in the NFO list if an episode has been found
            if (episodesInNfo.size() > 0) {
              for (TvShowEpisode e : episodesInNfo) {
                e.setPath(dir.getPath());
                e.setTvShow(tvShow);
                e.addToMediaFiles(new MediaFile(file));
                // e.findImages();
                findAdditionalEpisodeFiles(e, file, content);
                e.saveToDb();
                tvShow.addEpisode(e);
              }
            }
            else {
              episode = new TvShowEpisode();
              episode.setEpisode(-1);
              episode.setSeason(-1);
              episode.setPath(dir.getPath());

              episode.setTitle(FilenameUtils.getBaseName(file.getName()));
              episode.setTvShow(tvShow);
              episode.setFirstAired(result.date);
              episode.addToMediaFiles(new MediaFile(file));
              // episode.findImages();
              findAdditionalEpisodeFiles(episode, file, content);
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
        }
      }
      if (file.isDirectory() && !"sample".equalsIgnoreCase(file.getName()) && !file.getName().startsWith(".")) {
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
    File[] content = dir.listFiles();
    for (File file : content) {
      if (file.isFile()) {
        // check filetype
        if (!Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()).toLowerCase())) {
          continue;
        }

        TvShowEpisode episode = tvShowList.getTvEpisodeByFile(file);
        if (episode == null) {
          // try to parse out episodes/season from parent directory
          EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromDirectory(dir.getParentFile(), tvShow.getPath());

          if (result.season == -1) {
            // did the search find a season?
            // no -> search for it in the folder name (relative path between tv show root and the current dir)
            result.season = TvShowEpisodeAndSeasonParser.detectSeason(new File(tvShow.getPath()).toURI().relativize(file.toURI()).getPath());
          }

          if (result.episodes.size() == 0) {
            // if episode STILL empty, try Myron's way of parsing - lol
            result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(new File(tvShow.getPath()).toURI().relativize(file.toURI())
                .getPath(), tvShow.getPath());
            LOGGER.debug(file.getName() + " - " + result.toString());
          }

          if (result.episodes.size() > 0) {
            // add it
            for (int ep : result.episodes) {
              episode = new TvShowEpisode();
              episode.setPath(dir.getPath());
              episode.setEpisode(ep);
              episode.setSeason(result.season);
              episode.setTvShow(tvShow);
              if (result.name.isEmpty()) {
                result.name = FilenameUtils.getBaseName(file.getName());
              }
              episode.setTitle(result.name);
              episode.setFirstAired(result.date);
              episode.setDisc(true);
              episode.addToMediaFiles(new MediaFile(file));
              findAdditionalEpisodeFiles(episode, file, content);
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
          else {
            // episode detection found nothing - simply add this file
            episode = new TvShowEpisode();
            episode.setPath(dir.getPath());
            episode.setEpisode(-1);
            episode.setSeason(-1);
            episode.setTitle(FilenameUtils.getBaseName(file.getName()));
            episode.setTvShow(tvShow);
            episode.setFirstAired(result.date);
            episode.setDisc(true);
            episode.addToMediaFiles(new MediaFile(file));
            findAdditionalEpisodeFiles(episode, file, content);
            episode.saveToDb();
            tvShow.addEpisode(episode);
          }
        }
      }
    }
  }

  /**
   * Find additional episode files.
   * 
   * @param episode
   *          the episode
   * @param videoFile
   *          the video file
   * @param directoryContents
   *          the directory contents
   */
  private void findAdditionalEpisodeFiles(TvShowEpisode episode, File videoFile, File[] directoryContents) {
    // there are much different ways the files could be stored; we only will try to find the files with the corresponding names (and sample)
    // 1st find all files/directories with videofilename*
    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(FilenameUtils.getBaseName(videoFile.getName())) + ".*");
    // 2nd find thumbs <episodename>-thumb.jpg/png
    Pattern thumbPattern = Pattern.compile("(?i)" + Pattern.quote(episode.getTitle()) + "-thumb\\..{2,4}");

    for (File file : directoryContents) {
      if (file == videoFile) {
        continue;
      }

      Matcher matcher = pattern.matcher(file.getName());
      Matcher thumbMatcher = thumbPattern.matcher(file.getName());
      if (matcher.matches() || thumbMatcher.matches()) {
        // add this file to the episode
        episode.addToMediaFiles(new MediaFile(file));
        continue;
      }

      // and last but not least we add a directory called sample/subs/subtitle
      if (file.isDirectory()
          && ("sample".equalsIgnoreCase(file.getName()) || "subs".equalsIgnoreCase(file.getName()) || "subtitle".equalsIgnoreCase(file.getName()))) {
        File[] subDirContent = file.listFiles();
        for (File subDirFile : subDirContent) {
          episode.addToMediaFiles(new MediaFile(subDirFile));
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
   * @param max
   *          the max
   * @param progress
   *          the progress
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

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.TmmThreadPool#callback(java.lang.Object)
   */
  @Override
  public void callback(Object obj) {
    startProgressBar((String) obj, getTaskcount(), getTaskdone());
  }
}
