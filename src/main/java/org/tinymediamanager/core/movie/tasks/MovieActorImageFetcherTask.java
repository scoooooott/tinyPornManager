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

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tasks.MediaEntityActorImageFetcherTask;

/**
 * The Class MovieActorImageFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class MovieActorImageFetcherTask extends MediaEntityActorImageFetcherTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieActorImageFetcherTask.class);

  public MovieActorImageFetcherTask(Movie movie) {
    this.mediaEntity = movie;
    // do not do a cleanup if we're in a MMD
    if (movie.isMultiMovieDir()) {
      cleanup = false;
    }

    persons = new HashSet<>(movie.getActors());
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
}
