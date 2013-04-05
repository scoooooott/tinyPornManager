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
package org.tinymediamanager.core;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.tinymediamanager.core.movie.Movie;

/**
 * The Class MediaFileInformationFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class MediaFileInformationFetcherTask implements Callable<Object> {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = Logger.getLogger(MediaFileInformationFetcherTask.class);

  /** The movie. */
  private Movie               m;

  public MediaFileInformationFetcherTask(Movie movie) {
    this.m = movie;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Callable#call()
   */
  @Override
  public String call() {
    // try/catch block in the root of the thread to log crashes
    try {
      for (MediaFile mediaFile : m.getMediaFiles()) {
        mediaFile.gatherMediaInformation();
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
    }
    m.saveToDb();
    return "getting MediaInfo from " + m.getTitle();
  }
}
