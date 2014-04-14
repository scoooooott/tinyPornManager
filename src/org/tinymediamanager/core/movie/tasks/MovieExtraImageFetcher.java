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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

/**
 * The Class MovieExtraImageFetcher.
 * 
 * @author Manuel Laggner
 */
public class MovieExtraImageFetcher implements Runnable {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieExtraImageFetcher.class);

  /** The movie. */
  private Movie               movie;

  /** The extrafanart. */
  private boolean             extrafanart;

  /** The extrathumbs. */
  private boolean             extrathumbs;

  /**
   * Instantiates a new movie extra image fetcher.
   * 
   * @param movie
   *          the movie
   * @param extrafanart
   *          the extrafanart
   * @param extrathumbs
   *          the extrathumbs
   */
  public MovieExtraImageFetcher(Movie movie, boolean extrafanart, boolean extrathumbs) {
    this.movie = movie;
    this.extrafanart = extrafanart;
    this.extrathumbs = extrathumbs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    // try/catch block in the root of the thread to log crashes
    try {
      if (!movie.isMultiMovieDir()) {
        // download extrathumbs
        if (extrathumbs) {
          movie.downloadExtraThumbs(new ArrayList<String>(movie.getExtraThumbs()));
        }

        // download extrafanart
        if (extrafanart) {
          movie.downloadExtraFanarts(new ArrayList<String>(movie.getExtraFanarts()));
        }

        // check if tmm has been shut down
        if (Thread.interrupted()) {
          return;
        }

        movie.saveToDb();
        movie.callbackForWrittenArtwork(MediaArtworkType.ALL);
      }
      else {
        LOGGER.info("Movie '" + movie.getTitle() + "' is within a multi-movie-directory - skip downloading of additional images.");
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.extraimage.threadcrashed"));
    }
  }
}
