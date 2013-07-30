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
package org.tinymediamanager.core.movie.tasks;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.Movie;

/**
 * The Class MovieReloadMediaInformationTask, to explicit reload mediainformation.
 * 
 * @author Manuel Laggner
 */
public class MovieReloadMediaInformationTask extends TmmThreadPool {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieReloadMediaInformationTask.class);

  private List<Movie>         moviesToReload;

  public MovieReloadMediaInformationTask(List<Movie> movies) {
    moviesToReload = new ArrayList<Movie>(movies);
    initThreadPool(1, "reloadMI");
  }

  @Override
  protected Void doInBackground() throws Exception {
    try {
      LOGGER.info("get MediaInfo...");
      // update MediaInfo
      startProgressBar("getting Mediainfo...");
      for (Movie m : moviesToReload) {
        if (cancel) {
          break;
        }
        submitTask(new MediaFileInformationFetcherTask(m.getMediaFiles(), m));
      }

      waitForCompletionOrCancel();
      LOGGER.info("Done getting MediaInfo)");
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

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  private void startProgressBar(String description, int max, int progress) {
    if (!GraphicsEnvironment.isHeadless()) {
      if (!StringUtils.isEmpty(description)) {
        lblProgressAction.setText(description);
      }
      progressBar.setVisible(true);
      progressBar.setIndeterminate(false);
      progressBar.setMaximum(max);
      progressBar.setValue(progress);
      btnCancelTask.setVisible(true);
    }
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  private void startProgressBar(String description) {
    if (!GraphicsEnvironment.isHeadless()) {
      if (!StringUtils.isEmpty(description)) {
        lblProgressAction.setText(description);
      }
      progressBar.setVisible(true);
      progressBar.setIndeterminate(true);
      btnCancelTask.setVisible(true);
    }
  }

  /**
   * Stop progress bar.
   */
  private void stopProgressBar() {
    if (!GraphicsEnvironment.isHeadless()) {
      lblProgressAction.setText("");
      progressBar.setIndeterminate(false);
      progressBar.setVisible(false);
      btnCancelTask.setVisible(false);
    }
  }
}
