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
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MovieComparator is used to (initial) sort the movies in the moviepanel.
 * 
 * @author Manuel Laggner
 */
public class MovieExtendedComparator implements Comparator<Movie> {
  private static final ResourceBundle BUNDLE         = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER         = LoggerFactory.getLogger(MovieExtendedComparator.class);

  private SortColumn                  sortColumn;
  private boolean                     sortAscending;
  private RuleBasedCollator           stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

  public enum SortColumn {
    TITLE(BUNDLE.getString("metatag.title")), //$NON-NLS-1$,
    YEAR(BUNDLE.getString("metatag.year")), //$NON-NLS-1$,
    DATE_ADDED(BUNDLE.getString("metatag.dateadded")), //$NON-NLS-1$,
    WATCHED(BUNDLE.getString("metatag.watched")), //$NON-NLS-1$,
    RATING(BUNDLE.getString("metatag.rating")), //$NON-NLS-1$,
    RUNTIME(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$,

    private String title;

    private SortColumn(String title) {
      this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return title;
    }
  }

  public enum WatchedFlag {
    WATCHED(BUNDLE.getString("metatag.watched")), //$NON-NLS-1$,
    NOT_WATCHED(BUNDLE.getString("metatag.notwatched")); //$NON-NLS-1$,

    private String title;

    private WatchedFlag(String title) {
      this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return title;
    }
  }

  public enum SortOrder {
    ASCENDING(BUNDLE.getString("sort.ascending")), //$NON-NLS-1$
    DESCENDING(BUNDLE.getString("sort.descending")); //$NON-NLS-1$

    private String title;

    private SortOrder(String title) {
      this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return title;
    }
  }

  public enum MovieInMovieSet {
    IN_MOVIESET(BUNDLE.getString("movie.inmovieset")), //$NON-NLS-1$
    NOT_IN_MOVIESET(BUNDLE.getString("movie.notinmovieset")); //$NON-NLS-1$

    private String title;

    private MovieInMovieSet(String title) {
      this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return title;
    }
  }

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
        case TITLE:
          sortOrder = stringCollator.compare(movie1.getTitleSortable().toLowerCase(), movie2.getTitleSortable().toLowerCase());
          break;

        case YEAR:
          sortOrder = stringCollator.compare(movie1.getYear(), movie2.getYear());
          break;

        case DATE_ADDED:
          sortOrder = movie1.getDateAdded().compareTo(movie2.getDateAdded());
          break;

        case WATCHED:
          Boolean watched1 = Boolean.valueOf(movie1.isWatched());
          Boolean watched2 = Boolean.valueOf(movie2.isWatched());
          sortOrder = watched1.compareTo(watched2);
          break;

        case RATING:
          sortOrder = Float.compare(movie1.getRating(), movie2.getRating());
          break;

        case RUNTIME:
          Integer runtime1 = Integer.valueOf(movie1.getRuntime());
          Integer runtime2 = Integer.valueOf(movie2.getRuntime());
          sortOrder = runtime1.compareTo(runtime2);
          break;
      }
    }
    catch (NullPointerException e) {
      // do nothing here. there could be
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
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
