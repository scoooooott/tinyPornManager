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

import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieCast;
import org.tinymediamanager.scraper.MediaGenres;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * The Class MoviesExtendedMatcher.
 */
public class MoviesExtendedMatcher implements Matcher<Movie> {

  public enum SearchOptions {
    DUPLICATES, WATCHED, GENRE, CAST, TAG, MOVIESET;
  }

  /** The search options. */
  private HashMap<SearchOptions, Object> searchOptions;

  /**
   * Instantiates a new movies extended matcher.
   * 
   * @param searchOptions
   *          the search options
   */
  public MoviesExtendedMatcher(HashMap<SearchOptions, Object> searchOptions) {
    this.searchOptions = searchOptions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.matchers.Matcher#matches(java.lang.Object)
   */
  @Override
  public boolean matches(Movie movie) {
    // not null
    if (movie == null) {
      return false;
    }

    // check duplicates
    if (searchOptions.containsKey(SearchOptions.DUPLICATES)) {
      if (!movie.isDuplicate()) {
        return false;
      }
    }

    // check against watched flag
    if (searchOptions.containsKey(SearchOptions.WATCHED)) {
      boolean watched = (Boolean) searchOptions.get(SearchOptions.WATCHED);
      boolean result = !(movie.isWatched() ^ watched);
      if (result == false) {
        return false;
      }
    }

    // check against genre
    if (searchOptions.containsKey(SearchOptions.GENRE)) {
      MediaGenres genre = (MediaGenres) searchOptions.get(SearchOptions.GENRE);
      if (!movie.getGenres().contains(genre)) {
        return false;
      }
    }

    // check against cast member
    if (searchOptions.containsKey(SearchOptions.CAST)) {
      String castSearch = (String) searchOptions.get(SearchOptions.CAST);
      if (StringUtils.isNotEmpty(castSearch)) {
        Pattern pattern = Pattern.compile("(?i)" + castSearch);
        java.util.regex.Matcher matcher = null;

        // director
        if (StringUtils.isNotEmpty(movie.getDirector())) {
          matcher = pattern.matcher(movie.getDirector());
          if (matcher.find()) {
            return true;
          }
        }

        // writer
        if (StringUtils.isNotEmpty(movie.getWriter())) {
          matcher = pattern.matcher(movie.getWriter());
          if (matcher.find()) {
            return true;
          }
        }

        // actors
        for (MovieCast cast : movie.getCast()) {
          if (StringUtils.isNotEmpty(cast.getName())) {
            matcher = pattern.matcher(cast.getName());
            if (matcher.find()) {
              return true;
            }
          }
        }
      }

      return false;
    }

    // check against tag
    if (searchOptions.containsKey(SearchOptions.TAG)) {
      String tag = (String) searchOptions.get(SearchOptions.TAG);

      for (String tagInMovie : movie.getTags()) {
        if (tagInMovie.equals(tag)) {
          return true;
        }
      }

      return false;
    }

    // check against MOVIESET
    if (searchOptions.containsKey(SearchOptions.MOVIESET)) {
      Boolean isInSet = (Boolean) searchOptions.get(SearchOptions.MOVIESET);
      if ((movie.getMovieSet() != null) == isInSet) {
        return true;
      }

      return false;
    }

    return true;
  }
}
