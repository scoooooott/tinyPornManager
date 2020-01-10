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
package org.tinymediamanager.core.tvshow.tasks;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.tasks.MediaEntityActorImageFetcherTask;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

/**
 * The class TvShowActorImageFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class TvShowActorImageFetcherTask extends MediaEntityActorImageFetcherTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowActorImageFetcherTask.class);

  public TvShowActorImageFetcherTask(TvShow tvShow) {
    this.mediaEntity = tvShow;

    // create a set of all actors and guests
    persons = new HashSet<>(tvShow.getActors());
    for (TvShowEpisode episode : tvShow.getEpisodes()) {
      persons.addAll(episode.getGuests());
    }
  }

  public TvShowActorImageFetcherTask(TvShowEpisode episode) {
    // use the show as entity to store the actor images in the TV show root
    this.mediaEntity = episode.getTvShow();
    cleanup = false;

    persons = new HashSet<>(episode.getGuests());
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
}
