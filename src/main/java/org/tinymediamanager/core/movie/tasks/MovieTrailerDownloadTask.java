/*
 * Copyright 2012 - 2019 Manuel Laggner
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
import java.util.List;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.movie.filenaming.MovieTrailerNaming;
import org.tinymediamanager.core.tasks.DownloadTask;

/**
 * A task for downloading movie trailers
 * 
 * @author Manuel Laggner
 */
public class MovieTrailerDownloadTask extends DownloadTask {

  public MovieTrailerDownloadTask(MovieTrailer trailer, Movie movie) throws Exception {
    super(trailer.getUrl(), movie.getPathNIO().resolve(movie.getTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER)), movie, MediaFileType.TRAILER);

    List<MovieTrailerNaming> trailernames = new ArrayList<>();
    if (movie.isMultiMovieDir()) {
      trailernames.add(MovieTrailerNaming.FILENAME_TRAILER);
    }
    else {
      trailernames = MovieModuleManager.SETTINGS.getTrailerFilenames();
    }

    // hmm.. we can only download ONE trailer, so both patterns won't work
    for (MovieTrailerNaming name : trailernames) {
      file = movie.getPathNIO().resolve(movie.getTrailerFilename(name));
    }

    if ("apple".equalsIgnoreCase(trailer.getProvider())) {
      setSpecialUserAgent("QuickTime");
    }
  }
}
