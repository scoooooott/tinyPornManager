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
package org.tinymediamanager.ui.movies.filters;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.ITmmUIFilter;

/**
 * The interface IMovieUIFilter is used for filtering movies in the JTable
 * 
 * @author Manuel Laggner
 */
public interface IMovieUIFilter extends ITmmUIFilter<Movie> {

  /**
   * is the given accepted by the filter
   * 
   * @param movie
   *          the movie to check
   * @return true or false
   */
  boolean accept(Movie movie);
}
