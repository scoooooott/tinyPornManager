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
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MissingMovieTask.
 * 
 * @author Myron Boyle
 */

public class MovieFindMissingTask extends TmmThreadPool {
  private static final Logger         LOGGER = LoggerFactory.getLogger(MovieFindMissingTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<String>                dataSources;
  private MovieList                   movieList;

  public MovieFindMissingTask() {
    super(BUNDLE.getString("movie.findmissing"));
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getMovieSettings().getMovieDataSource());
  }

  public MovieFindMissingTask(String datasource) {
    super(BUNDLE.getString("movie.findmissing") + " (" + datasource + ")");
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(1);
    dataSources.add(datasource);
  }

  @Override
  public void doInBackground() {
    try {
      long start = System.currentTimeMillis();

      // build MF list
      ArrayList<MediaFile> mfs = new ArrayList<MediaFile>();
      for (Movie movie : movieList.getMovies()) {
        mfs.addAll(movie.getMediaFiles());
        // mfs.addAll(movie.getMediaFiles(MediaFileType.VIDEO));
        // mfs.addAll(movie.getMediaFiles(MediaFileType.VIDEO_EXTRA));
        // mfs.addAll(movie.getMediaFiles(MediaFileType.TRAILER));
      }

      for (String ds : dataSources) {
        start();

        ArrayList<File> bigFiles = getBigFiles(new File(ds));
        if (cancel) {
          break;
        }

        for (File file : bigFiles) {
          if (cancel) {
            break;
          }

          MediaFile mf = new MediaFile(file);
          if (!mfs.contains(mf)) {
            LOGGER.info("found possible movie file " + file);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, "possible movie", "found possible movie " + file, new String[] { ds }));
          }
        }
        if (cancel) {
          break;
        }
      }

      long end = System.currentTimeMillis();
      LOGGER.info("Done updating datasource :) - took " + Utils.MSECtoHHMMSS(end - start));
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
  }

  /**
   * recursively gets all Files >100mb from a dir
   */
  private ArrayList<File> getBigFiles(File dir) {
    ArrayList<File> mv = new ArrayList<File>();

    File[] list = dir.listFiles();
    if (list == null || cancel) {
      return mv;
    }
    for (File file : list) {
      if (cancel) {
        break;
      }

      if (file.isFile()) {
        if (file.length() > 1024 * 1024 * 100) {
          mv.add(file);
        }
      }
      else {
        mv.addAll(getBigFiles(file));
      }
    }
    return mv;
  }

  @Override
  public void callback(Object obj) {
    publishState((String) obj, progressDone);
  }
}
