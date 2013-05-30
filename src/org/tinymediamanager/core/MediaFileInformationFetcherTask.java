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

import static org.tinymediamanager.core.Constants.*;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.Movie;

/**
 * The Class MediaFileInformationFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class MediaFileInformationFetcherTask implements Callable<Object> {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(MediaFileInformationFetcherTask.class);

  /** The media files. */
  private List<MediaFile>     mediaFiles;

  /** The media entity. */
  private MediaEntity         mediaEntity;

  /**
   * Instantiates a new media file information fetcher task.
   * 
   * @param mediaFiles
   *          the media files
   * @param mediaEntity
   *          the media entity
   */
  public MediaFileInformationFetcherTask(List<MediaFile> mediaFiles, MediaEntity mediaEntity) {
    this.mediaFiles = mediaFiles;
    this.mediaEntity = mediaEntity;
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
      for (MediaFile mediaFile : mediaFiles) {
        mediaFile.gatherMediaInformation();
        if (mediaEntity != null && mediaEntity instanceof Movie && mediaFile.hasSubtitles()) {
          Movie movie = (Movie) mediaEntity;
          movie.setSubtitles(true);
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
    }

    if (mediaEntity != null) {
      mediaEntity.firePropertyChange(MEDIA_INFORMATION, false, true);
      mediaEntity.saveToDb();
    }

    return "getting MediaInfo from " + mediaEntity.getTitle();
  }
}
