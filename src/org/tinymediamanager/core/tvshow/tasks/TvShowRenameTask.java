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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowRenamer;

/**
 * The Class MovieRenameTask.
 * 
 * @author Manuel Laggner
 */
public class TvShowRenameTask extends TmmThreadPool {
  private final static Logger LOGGER           = LoggerFactory.getLogger(TvShowRenameTask.class);

  private List<TvShow>        tvShowsToRename  = new ArrayList<TvShow>();
  private List<TvShowEpisode> episodesToRename = new ArrayList<TvShowEpisode>();

  /**
   * Instantiates a new tv show rename task.
   * 
   * @param tvShowsToRename
   *          the tvshows to rename
   */
  public TvShowRenameTask(List<TvShow> tvShowsToRename, List<TvShowEpisode> episodesToRename) {
    this.tvShowsToRename.addAll(tvShowsToRename);
    this.episodesToRename.addAll(episodesToRename);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Void doInBackground() throws Exception {
    try {
      initThreadPool(1, "rename");
      startProgressBar("renaming TV shows...");
      // rename complete tv shows
      for (int i = 0; i < tvShowsToRename.size(); i++) {
        TvShow show = tvShowsToRename.get(i);
        for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(show.getEpisodes())) {
          submitTask(new RenameEpisodeTask(episode));
        }
      }
      // rename episodes
      for (int i = 0; i < episodesToRename.size(); i++) {
        TvShowEpisode episode = episodesToRename.get(i);
        submitTask(new RenameEpisodeTask(episode));
      }
      waitForCompletionOrCancel();
      if (cancel) {
        cancel(false);// swing cancel
      }
      LOGGER.info("Done renaming TV shows)");
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "Settings.renamer", "message.renamer.threadcrashed"));
    }
    return null;
  }

  /**
   * ThreadpoolWorker to work off ONE episode
   * 
   * @author Manuel Laggner
   * @version 1.0
   */
  private class RenameEpisodeTask implements Callable<Object> {

    private TvShowEpisode episode = null;

    public RenameEpisodeTask(TvShowEpisode episode) {
      this.episode = episode;
    }

    @Override
    public String call() throws Exception {
      TvShowRenamer.renameEpisode(episode);
      return episode.getTitle();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  public void done() {
    stopProgressBar();
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
