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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TmmThreadPool;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;

/**
 * The Class MissingMovieTask.
 * 
 * @author Myron Boyle
 */

public class MissingMovieTask extends TmmThreadPool {
  private static final Logger LOGGER = LoggerFactory.getLogger(MissingMovieTask.class);

  private List<String>        dataSources;
  private MovieList           movieList;

  public MissingMovieTask() {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(Globals.settings.getMovieSettings().getMovieDataSource());
  }

  public MissingMovieTask(String datasource) {
    movieList = MovieList.getInstance();
    dataSources = new ArrayList<String>(1);
    dataSources.add(datasource);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Void doInBackground() {
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
        startProgressBar("searching '" + ds + "'");

        ArrayList<File> bigFiles = getBigFiles(new File(ds));
        for (File file : bigFiles) {
          MediaFile mf = new MediaFile(file);
          if (!mfs.contains(mf)) {
            LOGGER.info("found possible movie file " + file);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, "possible movie", "found possible movie " + file, new String[] { ds }));
          }
        }
      }

      long end = System.currentTimeMillis();
      LOGGER.info("Done updating datasource :) - took " + Utils.MSECtoHHMMSS(end - start));

      if (cancel) {
        cancel(false);// swing cancel
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "update.datasource", "message.update.threadcrashed"));
    }
    return null;
  }

  /**
   * recursively gets all Files >100mb from a dir
   */
  private ArrayList<File> getBigFiles(File dir) {
    ArrayList<File> mv = new ArrayList<File>();

    File[] list = dir.listFiles();
    if (list == null) {
      return mv;
    }
    for (File file : list) {
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

  /*
   * Executed in event dispatching thread
   */
  @Override
  public void done() {
    stopProgressBar();
  }

  @Override
  public void cancel() {
    cancel = true;
  }

  @Override
  public void callback(Object obj) {
    startProgressBar((String) obj, getTaskcount(), getTaskdone());
  }

}
