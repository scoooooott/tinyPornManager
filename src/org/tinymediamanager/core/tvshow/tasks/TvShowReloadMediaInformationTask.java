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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;

/**
 * The Class TvShowReloadMediaInformationTask, to explicit reload mediainformation.
 * 
 * @author Manuel Laggner
 */
public class TvShowReloadMediaInformationTask extends TmmThreadPool {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowReloadMediaInformationTask.class);

  private List<TvShow>        tvShows;
  private List<TvShowEpisode> episodes;

  public TvShowReloadMediaInformationTask(List<TvShow> tvShows, List<TvShowEpisode> episodes) {
    this.tvShows = new ArrayList<TvShow>(tvShows);
    this.episodes = new ArrayList<TvShowEpisode>(episodes);

    // add the episodes from the shows
    for (TvShow show : this.tvShows) {
      for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(show.getEpisodes())) {
        if (!this.episodes.contains(episode)) {
          this.episodes.add(episode);
        }
      }
    }

    initThreadPool(1, "reloadMI");
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      long start = System.currentTimeMillis();
      LOGGER.info("get MediaInfo...");
      // update MediaInfo
      startProgressBar("getting Mediainfo...");
      for (TvShow show : tvShows) {
        if (cancel) {
          break;
        }
        submitTask(new MediaFileInformationFetcherTask(show.getMediaFiles(), show, true));
      }

      for (TvShowEpisode episode : episodes) {
        if (cancel) {
          break;
        }
        submitTask(new MediaFileInformationFetcherTask(episode.getMediaFiles(), episode, true));
      }

      waitForCompletionOrCancel();
      long end = System.currentTimeMillis();
      LOGGER.info("Done getting MediaInfo - took " + Utils.MSECtoHHMMSS(end - start));
      if (cancel) {
        cancel(false);// swing cancel
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "MediaInfo", "message.mediainfo.threadcrashed"));
    }
    return null;
  }

  @Override
  public void callback(Object obj) {
    startProgressBar((String) obj, getTaskcount(), getTaskdone());
  }

  @Override
  public void cancel() {
    cancel = true;
  }

  @Override
  public void done() {
    stopProgressBar();
  }
}
