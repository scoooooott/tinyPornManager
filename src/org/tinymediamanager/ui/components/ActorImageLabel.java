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
package org.tinymediamanager.ui.components;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieActor;

/**
 * The Class ActorImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ActorImageLabel extends ImageLabel {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1768796209645569296L;

  /** The movie. */
  private Movie             movie;

  /**
   * Instantiates a new actor image label.
   */
  public ActorImageLabel() {
    super();
  }

  /**
   * Sets the movie.
   * 
   * @param movie
   *          the new movie
   */
  public void setMovie(Movie movie) {
    this.movie = movie;
  }

  /**
   * Sets the actor.
   * 
   * @param actor
   *          the new actor
   */
  public void setActor(MovieActor actor) {
    if (actor != null) {
      if (movie != null && StringUtils.isNotEmpty(actor.getThumbPath())) {
        File actorThumb = new File(movie.getPath() + File.separator + actor.getThumbPath());
        if (actorThumb.exists()) {
          setImagePath(actorThumb.getPath());
          return;
        }
      }

      setImageUrl(actor.getThumb());
    }
  }

}
