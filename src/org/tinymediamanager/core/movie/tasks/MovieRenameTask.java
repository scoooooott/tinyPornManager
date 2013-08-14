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

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieRenamer;

/**
 * The Class MovieRenameTask.
 * 
 * @author Manuel Laggner
 */
public class MovieRenameTask extends TmmThreadPool {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieRenameTask.class);

  /** The movies to rename. */
  private List<Movie>         moviesToRename;

  /**
   * Instantiates a new movie rename task.
   * 
   * @param moviesToRename
   *          the movies to rename
   */
  public MovieRenameTask(List<Movie> moviesToRename) {
    this.moviesToRename = moviesToRename;
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
      startProgressBar("renaming movies...");
      // rename movies
      for (int i = 0; i < moviesToRename.size(); i++) {
        Movie movie = moviesToRename.get(i);
        submitTask(new RenameMovieTask(movie));
      }
      waitForCompletionOrCancel();
      if (cancel) {
        cancel(false);// swing cancel
      }
      LOGGER.info("Done renaming movies)");
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
  private class RenameMovieTask implements Callable<Object> {

    private Movie movie = null;

    public RenameMovieTask(Movie movie) {
      this.movie = movie;
    }

    @Override
    public String call() throws Exception {
      MovieRenamer.renameMovie(movie);
      return movie.getTitle();
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
