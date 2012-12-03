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

  /**
   * The Enum SortColumn.
   */
  public enum SortColumn {

    /** The date added. */
    DATE_ADDED("Date added"),
    /** The watched. */
    WATCHED("Watched"),
    /** The rating. */
    RATING("Rating"),
    /** The runtime. */
    RUNTIME("Runtime");

    /** The title. */
    private String title;

    /**
     * Instantiates a new sort column.
     * 
     * @param title
     *          the title
     */
    private SortColumn(String title) {
      this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    public String toString() {
      return title;
    }
  }

  /**
   * The Enum SortOrder.
   */
  public enum SortOrder {

    /** The ascending. */
    ASCENDING("Ascending"),
    /** The descending. */
    DESCENDING("Descending");

    /** The title. */
    private String title;

    /**
     * Instantiates a new sort order.
     * 
     * @param title
     *          the title
     */
    private SortOrder(String title) {
      this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    public String toString() {
      return title;
    }
  }

  /** The sort column. */
  private SortColumn sortColumn;

  /** The sort ascending. */
  private boolean    sortAscending;

  /**
   * Instantiates a new movie extended comparator.
   * 
   * @param sortColumn
   *          the sort column
   * @param sortAscending
   *          the sort ascending
   */
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

        case WATCHED:
          sortOrder = Boolean.compare(movie1.isWatched(), movie2.isWatched());
          break;

        case RATING:
          sortOrder = Float.compare(movie1.getRating(), movie2.getRating());
          break;

        case RUNTIME:
          sortOrder = Integer.compare(movie1.getRuntime(), movie2.getRuntime());
          break;

      }
    } catch (Exception e) {
    }

    // sort ascending or descending
    if (sortAscending) {
      return sortOrder;
    } else {
      return sortOrder * -1;
    }
  }
}
