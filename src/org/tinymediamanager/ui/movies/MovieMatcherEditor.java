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
package org.tinymediamanager.ui.movies;

import java.util.HashMap;

import org.tinymediamanager.core.movie.entities.Movie;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * The Class MovieMatcherEditor.
 * 
 * @author Manuel Laggner
 */
public class MovieMatcherEditor extends AbstractMatcherEditor<Movie> {

  /**
   * Instantiates a new movie matcher editor.
   */
  public MovieMatcherEditor() {
  }

  /**
   * Filter movies.
   * 
   * @param filter
   *          the filter
   */
  public void filterMovies(HashMap<MoviesExtendedMatcher.SearchOptions, Object> filter) {
    Matcher<Movie> matcher = new MoviesExtendedMatcher(filter);
    fireChanged(matcher);
  }
}
