/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.util.List;

import org.apache.log4j.Logger;
import org.tinymediamanager.ui.TmmSwingWorker;

/**
 * The Class MovieRenameTask.
 * 
 * @author manuel
 */
public class MovieRenameTask extends TmmSwingWorker {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = Logger.getLogger(MovieRenameTask.class);

  /** The movies to rename. */
  private List<Movie>         moviesToRename;

  /** The movie count. */
  private int                 movieCount;

  private boolean             cancel = false;

  /**
   * Instantiates a new movie rename task.
   * 
   * @param moviesToRename
   *          the movies to rename
   * @param label
   *          the label
   * @param progressBar
   *          the progress bar
   * @param button
   *          the button
   */
  public MovieRenameTask(List<Movie> moviesToRename) {
    this.moviesToRename = moviesToRename;
    this.movieCount = moviesToRename.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Void doInBackground() throws Exception {
    // rename movies
    for (int i = 0; i < moviesToRename.size(); i++) {
      if (cancel) {
        break;
      }

      Movie movie = moviesToRename.get(i);
      startProgressBar("renaming movies", 100 * i / movieCount);
      MovieRenamer.renameMovie(movie);
    }

    return null;
  }

  /**
   * Cancel.
   */
  public void cancel() {
    cancel = true;
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
   * @param value
   *          the value
   */
  private void startProgressBar(String description, int value) {
    lblProgressAction.setText(description);
    progressBar.setVisible(true);
    progressBar.setValue(value);
    btnCancelTask.setVisible(true);
  }

  /**
   * Stop progress bar.
   */
  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setVisible(false);
    btnCancelTask.setVisible(false);
  }
}
