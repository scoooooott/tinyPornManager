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

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.DownloadTask;
import org.tinymediamanager.scraper.MediaTrailer;

/**
 * A task for downloading movie trailers
 * 
 * @author Manuel Laggner
 */
public class MovieTrailerDownloadTask extends DownloadTask {

  public MovieTrailerDownloadTask(MediaTrailer trailer, Movie movie) {
    super(trailer.getDownloadUrl(), new File(movie.getPath(), movie.getTrailerBasename() + "-trailer"), movie, MediaFileType.TRAILER);
    if ("apple".equalsIgnoreCase(trailer.getProvider())) {
      setSpecialUserAgent("QuickTime");
    }
  }
}
