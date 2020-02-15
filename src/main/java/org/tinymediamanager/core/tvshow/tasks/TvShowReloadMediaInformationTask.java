/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tasks.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

/**
 * The Class TvShowReloadMediaInformationTask, to explicit reload mediainformation.
 * 
 * @author Manuel Laggner
 */
public class TvShowReloadMediaInformationTask extends TmmThreadPool {
  private static final Logger         LOGGER = LoggerFactory.getLogger(TvShowReloadMediaInformationTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private List<TvShow>                tvShows;
  private List<TvShowEpisode>         episodes;

  public TvShowReloadMediaInformationTask(List<TvShow> tvShows, List<TvShowEpisode> episodes) {
    super(BUNDLE.getString("tvshow.updatemediainfo"));
    this.tvShows = new ArrayList<>(tvShows);
    this.episodes = new ArrayList<>(episodes);

    // add the episodes from the shows
    for (TvShow show : this.tvShows) {
      for (TvShowEpisode episode : new ArrayList<>(show.getEpisodes())) {
        if (!this.episodes.contains(episode)) {
          this.episodes.add(episode);
        }
      }
    }
  }

  @Override
  protected void doInBackground() {
    try {
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      LOGGER.info("get MediaInfo...");
      // update MediaInfo
      start();

      initThreadPool(1, "reloadMI");
      for (TvShow show : tvShows) {
        if (cancel) {
          break;
        }
        for (MediaFile mf : show.getMediaFiles()) {
          submitTask(new MediaFileInformationFetcherTask(mf, show, true));
        }
      }

      for (TvShowEpisode episode : episodes) {
        if (cancel) {
          break;
        }
        for (MediaFile mf : episode.getMediaFiles()) {
          submitTask(new MediaFileInformationFetcherTask(mf, episode, true));
        }
      }

      waitForCompletionOrCancel();
      stopWatch.stop();
      LOGGER.info("Done getting MediaInfo - took " + stopWatch);
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "MediaInfo", "message.mediainfo.threadcrashed"));
    }
  }

  @Override
  public void callback(Object obj) {
    publishState((String) obj, progressDone);
  }
}
