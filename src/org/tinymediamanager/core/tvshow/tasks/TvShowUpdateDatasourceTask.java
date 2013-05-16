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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFile;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowUpdateDatasourceTask.class);

  /** The data sources. */
  private List<String>        dataSources;

  /** The file types. */
  private List<String>        fileTypes;

  /** The tv show list. */
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
        // FIXME
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

  /**
   * The Class FindTvShowTask.
   * 
   * @author Manuel Laggner
   */
  private class FindTvShowTask implements Callable<Object> {

    /** The subdir. */
    private File       subdir     = null;

    /** The datasource. */
    private String     datasource = "";

    /** The tv show list. */
    private TvShowList tvShowList = TvShowList.getInstance();

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
      // tvShow did not exist - try to parse a NFO file in parent folder
      tvShow = TvShow.parseNFO(dir);
      if (tvShow == null) {
        // create new one
        tvShow = new TvShow();
        tvShow.setPath(dir.getPath());
        tvShow.setTitle(ParserUtils.detectCleanMoviename(dir.getName()));
      }
      if (tvShow != null) {
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
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }

            // are there still episodes from NFO
            for (TvShowEpisode e : episodesInNfo) {
              e.setPath(dir.getPath());
              e.setTvShow(tvShow);
              e.addToMediaFiles(new MediaFile(file));
              e.saveToDb();
              tvShow.addEpisode(episode);
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
                e.saveToDb();
                tvShow.addEpisode(episode);
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
              episode.saveToDb();
              tvShow.addEpisode(episode);
            }
          }
        }
      }
      if (file.isDirectory()) {
        // dig deeper
        if (file.getName().equals("VIDEO_TS")) {
          findTvEpisodesAsDisc(tvShow, file);
        }
        else if (file.getName().equals("BDMV")) {
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
        if (!Globals.settings.getVideoFileType().contains("." + FilenameUtils.getExtension(file.getName()))) {
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
            episode.saveToDb();
            tvShow.addEpisode(episode);
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
