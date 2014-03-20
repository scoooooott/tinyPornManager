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
package org.tinymediamanager.ui.movies;

import java.text.RuleBasedCollator;
import java.util.Comparator;

import org.tinymediamanager.core.movie.entities.Movie;

/**
 * The Class MovieComparator is used to (initial) sort the movies in the moviepanel.
 * 
 * @author Manuel Laggner
 */
public class MovieComparator implements Comparator<Movie> {
  private RuleBasedCollator stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Movie movie1, Movie movie2) {
    if (stringCollator != null) {
      return stringCollator.compare(movie1.getTitleSortable().toLowerCase(), movie2.getTitleSortable().toLowerCase());
    }
    return movie1.getTitleSortable().toLowerCase().compareTo(movie2.getTitleSortable().toLowerCase());
  }

}
