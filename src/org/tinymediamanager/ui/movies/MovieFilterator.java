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
package org.tinymediamanager.ui.movies;

import java.util.List;

import org.tinymediamanager.core.movie.Movie;

import ca.odell.glazedlists.TextFilterator;

/**
 * The Class MovieFilterator is used to search movies.
 * 
 * @author Manuel Laggner
 */
public class MovieFilterator implements TextFilterator<Movie> {

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.TextFilterator#getFilterStrings(java.util.List, java.lang.Object)
   */
  @Override
  public void getFilterStrings(List<String> baseList, Movie movie) {
    baseList.add(movie.getTitle());
    baseList.add(movie.getOriginalTitle());
  }

}
