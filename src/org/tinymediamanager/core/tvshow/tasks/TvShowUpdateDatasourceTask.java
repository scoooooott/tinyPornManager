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
package org.tinymediamanager.core.tvshow.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.tvshow.EpisodeMatching;
import org.tinymediamanager.core.tvshow.EpisodeMatching.EpisodeMatchingResult;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;

/**
 * The Class TvShowUpdateDataSourcesTask.
 * 
 * @author Manuel Laggner
 */

public class TvShowUpdateDatasourceTask extends TmmThreadPool {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowUpdateDatasourceTask.class);

  /** The data sources. */
  private List<String>        dataSources;

  /** The file types. */
  private List<String>        fileTypes;

  private TvShowList          tvShowList;

  /**
   * Instantiates a new scrape task.
   * 
   */
  public TvShowUpdateDatasourceTask() {
    tvShowList = TvShowList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getTvShowSettings().getTvShowDataSource());
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

        // check whether the path is accessible (eg disconnected shares)
        if (filePath.listFiles() == null) {
          return null;
        }

        for (File subdir : filePath.listFiles()) {
          if (subdir.isDirectory()) {
            submitTask(new FindTvShowTask(subdir, path));
          }
        }
      }

      waitForCompletionOrCancel();

      LOGGER.info("removing orphaned tv shows...");
      startProgressBar("cleanup...");
      for (int i = tvShowList.getTvShows().size() - 1; i >= 0; i--) {
        TvShow tvShow = tvShowList.getTvShows().get(i);
        File movieDir = new File(tvShow.getPath());
        if (!movieDir.exists()) {
          tvShowList.removeTvShow(tvShow);
        }
      }
      LOGGER.info("Done updating datasource :)");

      LOGGER.info("get MediaInfo...");
      // update MediaInfo
      startProgressBar("getting Mediainfo...");
      initThreadPool(1, "mediainfo");
      for (TvShow tvShow : tvShowList.getTvShows()) {
        // TODO
        // submitTask(new MediaFileInformationFetcherTask(tvShow));
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

  private class FindTvShowTask implements Callable<Object> {

    private File       subdir     = null;
    private String     datasource = "";
    private TvShowList tvShowList = TvShowList.getInstance();

    public FindTvShowTask(File subdir, String datasource) {
      this.subdir = subdir;
      this.datasource = datasource;
    }

    @Override
    public String call() throws Exception {
      // get the TV show from this subdir
      createTvShowFromDirectory(subdir);
      return subdir.getName();
    }
  }

  /**
   * Creates the tv show from directory.
   * 
   * @param dir
   *          the dir
   */
  private void createTvShowFromDirectory(File dir) {
    // search for this tvshow folder in database
    TvShow tvShow = tvShowList.getTvShowByPath(dir.getPath());
    if (tvShow == null) {
      // create new one
      tvShow = new TvShow();
      tvShow.setPath(dir.getPath());
      tvShow.setTitle(dir.getName());
      tvShow.saveToDb();
      tvShowList.addTvShow(tvShow);
    }

    // find episodes in this tv show folder
    if (tvShow != null) {
      findTvEpisodes(tvShow, dir);
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
    // crawl this folder and try to find every episode in it
    File[] content = dir.listFiles();
    for (File file : content) {
      if (file.isFile()) {
        // check filetype
        if (!Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()))) {
          continue;
        }

        TvShowEpisode episode = tvShowList.getTvEpisodeByFile(file);
        if (episode == null) {
          // try to check what episode//season
          EpisodeMatchingResult result = EpisodeMatching.detectEpisode(file);
          if (result.episodes.size() > 0) {
            // // episode(s) found; check if there was also a season found
            // int season = 0;
            // if (result.season == 0) {
            // // try to get the result from the parent folder
            // Pattern pattern = Pattern.compile("{1,2}[0-9]$");
            // Matcher matcher = pattern.matcher(dir.getPath());
            // if (matcher.find()) {
            // season = Integer.parseInt(matcher.group());
            // }
            // }

            // add it
            for (int ep : result.episodes) {
              episode = new TvShowEpisode();
              episode.setEpisode(ep);
              episode.setSeason(result.season);
              episode.setTvShow(tvShow);
              episode.addToMediaFiles(new MediaFile(file.getPath(), file.getName(), MediaFileType.TV_SHOW));
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
        }
      }
      if (file.isDirectory()) {
        // dig deeper
        findTvEpisodes(tvShow, file);
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
