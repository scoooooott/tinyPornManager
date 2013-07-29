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

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowRenamer;

/**
 * The Class MovieRenameTask.
 * 
 * @author Manuel Laggner
 */
public class TvShowRenameTask extends TmmThreadPool {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(TvShowRenameTask.class);

  /** The movies to rename. */
  private List<TvShow>        tvShowToRename;

  /**
   * Instantiates a new movie rename task.
   * 
   * @param tvShowToRename
   *          the tvshow to rename
   * @param label
   *          the label
   * @param progressBar
   *          the progress bar
   * @param button
   *          the button
   */
  public TvShowRenameTask(List<TvShow> tvShowToRename) {
    this.tvShowToRename = tvShowToRename;
    initThreadPool(1, "rename");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Void doInBackground() throws Exception {
    try {
      startProgressBar("renaming TV shows...");
      // rename movies
      for (int i = 0; i < tvShowToRename.size(); i++) {
        TvShow show = tvShowToRename.get(i);
        submitTask(new RenameTvShowTask(show));
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
   * ThreadpoolWorker to work off ONE possible movie from root datasource directory
   * 
   * @author Myron Boyle
   * @version 1.0
   */
  private class RenameTvShowTask implements Callable<Object> {

    private TvShow show = null;

    public RenameTvShowTask(TvShow show) {
      this.show = show;
    }

    @Override
    public String call() throws Exception {
      TvShowRenamer.renameTvShow(show);
      return show.getTitle();
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
