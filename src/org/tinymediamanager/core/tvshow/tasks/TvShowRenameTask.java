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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowRenamer;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class MovieRenameTask. rename all chosen movies
 * 
 * @author Manuel Laggner
 */
public class TvShowRenameTask extends TmmThreadPool {
  private final static Logger         LOGGER           = LoggerFactory.getLogger(TvShowRenameTask.class);
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<TvShow>                tvShowsToRename  = new ArrayList<TvShow>();
  private List<TvShowEpisode>         episodesToRename = new ArrayList<TvShowEpisode>();
  private boolean                     renameRoot       = true;

  /**
   * Instantiates a new tv show rename task.
   * 
   * @param tvShowsToRename
   *          the tvshows to rename
   */
  public TvShowRenameTask(List<TvShow> tvShowsToRename, List<TvShowEpisode> episodesToRename, boolean renameRootFolder) {
    super(BUNDLE.getString("tvshow.rename"));
    if (tvShowsToRename != null) {
      this.tvShowsToRename.addAll(tvShowsToRename);
    }
    if (episodesToRename != null) {
      this.episodesToRename.addAll(episodesToRename);
    }
    this.renameRoot = renameRootFolder;
  }

  @Override
  protected void doInBackground() {
    try {
      start();
      initThreadPool(1, "rename");

      // rename complete tv shows
      for (int i = 0; i < tvShowsToRename.size(); i++) {
        if (cancel) {
          break;
        }
        TvShow show = tvShowsToRename.get(i);
        for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(show.getEpisodes())) {
          submitTask(new RenameEpisodeTask(episode));
        }
      }
      // rename single episodes
      for (int i = 0; i < episodesToRename.size(); i++) {
        if (cancel) {
          break;
        }
        TvShowEpisode episode = episodesToRename.get(i);
        submitTask(new RenameEpisodeTask(episode));
      }

      waitForCompletionOrCancel();
      if (cancel) {
        return;
      }

      // rename TvShowRoot and update all MFs in DB to new path
      if (renameRoot) {
        for (int i = 0; i < episodesToRename.size(); i++) {
          if (cancel) {
            break;
          }
          // fill TvShowsToRename if we just rename an episodes list
          TvShow show = episodesToRename.get(i).getTvShow();
          if (!tvShowsToRename.contains(show)) {
            tvShowsToRename.add(show);
          }
        }
        for (int i = 0; i < tvShowsToRename.size(); i++) {
          if (cancel) {
            break;
          }
          TvShowRenamer.renameTvShowRoot(tvShowsToRename.get(i)); // rename root and update ShowMFs
        }
      }

      LOGGER.info("Done renaming TV shows)");
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "Settings.renamer", "message.renamer.threadcrashed"));
    }
    return;
  }

  /**
   * ThreadpoolWorker to work off ONE episode
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

  @Override
  public void callback(Object obj) {
    publishState((String) obj, progressDone);
  }
}
