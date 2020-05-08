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
package org.tinymediamanager.ui.movies;

import java.util.List;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.StrgUtils;

import ca.odell.glazedlists.TextFilterator;

/**
 * The Class MovieFilterator is used to search movies.
 * 
 * @author Manuel Laggner
 */
public class MovieFilterator implements TextFilterator<Movie> {

  @Override
  public void getFilterStrings(List<String> baseList, Movie movie) {
    baseList.add(movie.getTitle());
    baseList.add(movie.getOriginalTitle());
    baseList.add(StrgUtils.normalizeString(movie.getTitle()));
    baseList.add(StrgUtils.normalizeString(movie.getOriginalTitle()));
  }
}
