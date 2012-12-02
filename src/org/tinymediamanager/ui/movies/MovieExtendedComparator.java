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

import java.util.Comparator;

import org.tinymediamanager.core.movie.Movie;

/**
 * The Class MovieComparator is used to (initial) sort the movies in the
 * moviepanel.
 */
public class MovieExtendedComparator implements Comparator<Movie> {

  public enum SortColumn {
    DATE_ADDED("Date added");

    private String title;

    private SortColumn(String title) {
      this.title = title;
    }

    public String toString() {
      return title;
    }
  }

  public enum SortOrder {
    ASCENDING("Ascending"), DESCENDING("Descending");

    private String title;

    private SortOrder(String title) {
      this.title = title;
    }

    public String toString() {
      return title;
    }
  }

  private SortColumn sortColumn;
  private boolean    sortAscending;

  public MovieExtendedComparator(SortColumn sortColumn, boolean sortAscending) {
    this.sortColumn = sortColumn;
    this.sortAscending = sortAscending;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Movie movie1, Movie movie2) {
    int sortOrder = 0;

    try {
      // try to sort the chosen column
      switch (sortColumn) {
        case DATE_ADDED:
          sortOrder = movie1.getDateAdded().compareTo(movie2.getDateAdded());
          break;

      }
    }
    catch (Exception e) {
    }

    // sort ascending or descending
    if (sortAscending) {
      return sortOrder;
    }
    else {
      return sortOrder * -1;
    }
  }

}
