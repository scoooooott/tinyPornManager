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

package org.tinymediamanager.core.movie.tasks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.TmmThreadPool;

/**
 * the class {@link MovieChangeDatasourceTask} is used to change a Data source of a movie including the move/copy of all files
 * 
 * @author Manuel Laggner
 */
public class MovieChangeDatasourceTask extends TmmThreadPool {
  private static final Logger         LOGGER         = LoggerFactory.getLogger(MovieChangeDatasourceTask.class);
  private static final ResourceBundle BUNDLE         = ResourceBundle.getBundle("messages", new UTF8Control());

  private final String                datasource;
  private final List<Movie>           moviesToChange = new ArrayList<>();

  public MovieChangeDatasourceTask(List<Movie> moviesToChange, String datasource) {
    super(BUNDLE.getString("movie.changedatasource"));
    this.moviesToChange.addAll(moviesToChange);
    this.datasource = datasource;
  }

  @Override
  protected void doInBackground() {
    initThreadPool(1, "changeDataSource");
    start();

    for (Movie movie : moviesToChange) {
      submitTask(new Worker(movie));
    }
    waitForCompletionOrCancel();
    LOGGER.info("Done changing data sources");
  }

  @Override
  public void callback(Object obj) {
    publishState((String) obj, progressDone);
  }

  private class Worker implements Runnable {
    private final Movie movie;

    private Worker(Movie movie) {
      this.movie = movie;
    }

    @Override
    public void run() {
      LOGGER.info("changing data source of movie [" + movie.getTitle() + "] to " + datasource);

      if (movie.getDataSource().equals(datasource)) {
        LOGGER.warn("old and new data source is the same");
        return;
      }

      // if we are in a MMD, we will create the same parent dir in the new datasource and move all files
      if (movie.isMultiMovieDir()) {
        moveMovieFromMMD();
      }
      else {
        moveMovie();
      }
    }

    private void moveMovie() {
      Path srcDir = movie.getPathNIO();
      Path destDir = Paths.get(datasource, Paths.get(movie.getDataSource()).relativize(movie.getPathNIO()).toString());

      LOGGER.debug("moving movie dir " + srcDir.toString() + " to " + destDir.toString());

      boolean ok = false;
      try {
        ok = Utils.moveDirectorySafe(srcDir, destDir);
        if (ok) {
          movie.setDataSource(datasource);
          movie.setPath(destDir.toAbsolutePath().toString());
          movie.updateMediaFilePath(srcDir, destDir);
          movie.saveToDb(); // since we moved already, save it
        }
      }
      catch (Exception e) {
        LOGGER.error("error moving folder: ", e);
        MessageManager.instance.pushMessage(
            new Message(Message.MessageLevel.ERROR, srcDir, "message.changedatasource.failedmove", new String[] { ":", e.getLocalizedMessage() }));
      }
      if (!ok) {
        // FIXME: when we were not able to rename folder, display error msg and abort!!!
        LOGGER.error("Could not move to destination '" + destDir + "' - NOT changing datasource");
        return;
      }
    }

    private void moveMovieFromMMD() {
      Path srcDir = movie.getPathNIO();
      Path destDir = Paths.get(datasource, Paths.get(movie.getDataSource()).relativize(movie.getPathNIO()).toString());

      LOGGER.debug("moving multi movie dir " + srcDir.toString() + " to " + destDir.toString());

      boolean ok = false;
      try {
        if (!Files.exists(destDir)) {
          Files.createDirectories(destDir);
        }
        else {
          LOGGER.error("Directory already exists! '" + destDir + "' - NOT renaming folder ('upgrade' movie)");
          // well, better not to move
          return;
        }

        for (MediaFile mf : movie.getMediaFiles()) {
          Path srcFile = mf.getFileAsPath();
          Path destFile = destDir.resolve(srcDir.relativize(srcFile));
          Utils.moveFileSafe(srcFile, destFile);
        }

        movie.setDataSource(datasource);
        movie.setPath(destDir.toAbsolutePath().toString());
        movie.updateMediaFilePath(srcDir, destDir);
        movie.saveToDb(); // since we moved already, save it
        ok = true;
      }
      catch (Exception e) {
        LOGGER.error("error moving movie files: ", e);
        MessageManager.instance.pushMessage(
            new Message(Message.MessageLevel.ERROR, srcDir, "message.changedatasource.failedmove", new String[] { ":", e.getLocalizedMessage() }));
      }
      if (!ok) {
        // FIXME: when we were not able to rename folder, display error msg and abort!!!
        LOGGER.error("Could not move to destination '" + destDir + "' - NOT changing datasource");
        return;
      }
    }
  }
}
