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

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * The Class MovieExtraImageFetcher.
 * 
 * @author manuel
 */
public class MovieExtraImageFetcher implements Runnable {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = Logger.getLogger(MovieExtraImageFetcher.class);

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
      // download extrathumbs
      if (extrathumbs) {
        movie.downloadExtraThumbs(new ArrayList<String>(movie.getExtraThumbs()));
      }

      // download extrafanart
      if (extrafanart) {
        movie.downloadExtraFanarts(new ArrayList<String>(movie.getExtraFanarts()));
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
    }
  }
}
