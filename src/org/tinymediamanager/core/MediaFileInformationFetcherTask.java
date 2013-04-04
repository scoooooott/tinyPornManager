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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The Class MediaFileInformationFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class MediaFileInformationFetcherTask implements Runnable {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = Logger.getLogger(MediaFileInformationFetcherTask.class);

  /** The movie. */
  private List<MediaFile>     mediaFiles;

  public MediaFileInformationFetcherTask(List<MediaFile> mediaFiles) {
    this.mediaFiles = new ArrayList<MediaFile>(mediaFiles);
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
      for (MediaFile mediaFile : mediaFiles) {
        mediaFile.gatherMediaInformation();
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
    }
  }
}
