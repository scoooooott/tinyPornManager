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

import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.entities.Movie;

/**
 * The Class MovieComparator is used to (initial) sort the movies in the moviepanel.
 * 
 * @author Manuel Laggner
 */
public class MovieExtendedComparator implements Comparator<Movie> {
  private static final ResourceBundle BUNDLE         = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER         = LoggerFactory.getLogger(MovieExtendedComparator.class);

  private SortColumn                  sortColumn;
  private boolean                     sortAscending;
  private RuleBasedCollator           stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

  public enum SortColumn {
    TITLE(BUNDLE.getString("metatag.title")),
    SORT_TITLE(BUNDLE.getString("metatag.sorttitle")),
    YEAR(BUNDLE.getString("metatag.year")),
    DATE_ADDED(BUNDLE.getString("metatag.dateadded")),
    RELEASE_DATE(BUNDLE.getString("metatag.releasedate")),
    WATCHED(BUNDLE.getString("metatag.watched")),
    RATING(BUNDLE.getString("metatag.rating")),
    RUNTIME(BUNDLE.getString("metatag.runtime")),
    VIDEO_BITRATE(BUNDLE.getString("metatag.videobitrate")),
    FRAME_RATE(BUNDLE.getString("metatag.framerate"));

    private String title;

    SortColumn(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  public enum WatchedFlag {
    WATCHED(BUNDLE.getString("metatag.watched")),
    NOT_WATCHED(BUNDLE.getString("metatag.notwatched"));

    private String title;

    WatchedFlag(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  public enum SortOrder {
    ASCENDING(BUNDLE.getString("sort.ascending")),
    DESCENDING(BUNDLE.getString("sort.descending"));

    private String title;

    SortOrder(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  public enum MovieInMovieSet {
    IN_MOVIESET(BUNDLE.getString("movie.inmovieset")),
    NOT_IN_MOVIESET(BUNDLE.getString("movie.notinmovieset"));

    private String title;

    MovieInMovieSet(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  public enum OfflineMovie {
    OFFLINE(BUNDLE.getString("movie.offline")),
    NOT_OFFLINE(BUNDLE.getString("movie.online"));

    private String title;

    OfflineMovie(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  public MovieExtendedComparator(SortColumn sortColumn, boolean sortAscending) {
    this.sortColumn = sortColumn;
    this.sortAscending = sortAscending;
  }

  @Override
  public int compare(Movie movie1, Movie movie2) {
    Integer sortOrder = 0;

    try {
      // try to sort the chosen column
      switch (sortColumn) {
        case TITLE:
          sortOrder = stringCollator.compare(movie1.getTitleSortable().toLowerCase(Locale.ROOT), movie2.getTitleSortable().toLowerCase(Locale.ROOT));
          break;

        case SORT_TITLE:
          String title1 = StringUtils.isNotBlank(movie1.getSortTitle()) ? movie1.getSortTitle() : movie1.getTitleSortable();
          String title2 = StringUtils.isNotBlank(movie2.getSortTitle()) ? movie2.getSortTitle() : movie2.getTitleSortable();
          sortOrder = stringCollator.compare(title1.toLowerCase(Locale.ROOT), title2.toLowerCase(Locale.ROOT));
          break;

        case YEAR:
          sortOrder = compareNullFirst(movie1.getYear(), movie2.getYear());
          if (sortOrder == 0) {
            sortOrder = Integer.compare(movie1.getYear(), movie2.getYear());
          }
          break;

        case DATE_ADDED:
          sortOrder = compareNullFirst(movie1.getDateAdded(), movie2.getDateAdded());
          if (sortOrder != null && sortOrder == 0) {
            sortOrder = movie1.getDateAdded().compareTo(movie2.getDateAdded());
          }
          break;

        case WATCHED:
          sortOrder = compareNullFirst(movie1.isWatched(), movie2.isWatched());
          if (sortOrder != null && sortOrder == 0) {
            sortOrder = Boolean.compare(movie1.isWatched(), movie2.isWatched());
          }
          break;

        case RATING:
          sortOrder = compareNullFirst(movie1.getRating(), movie2.getRating());
          if (sortOrder != null && sortOrder == 0) {
            sortOrder = Float.compare(movie1.getRating().getRating(), movie2.getRating().getRating());
          }
          break;

        case RUNTIME:
          sortOrder = compareNullFirst(movie1.getRuntime(), movie2.getRuntime());
          if (sortOrder != null && sortOrder == 0) {
            sortOrder = Integer.compare(movie1.getRuntime(), movie2.getRuntime());
          }
          break;

        case VIDEO_BITRATE:
          sortOrder = compareNullFirst(movie1.getMediaInfoVideoBitrate(), movie2.getMediaInfoVideoBitrate());
          if (sortOrder != null && sortOrder == 0) {
            sortOrder = Integer.compare(movie1.getMediaInfoVideoBitrate(), movie2.getMediaInfoVideoBitrate());
          }
          break;

        case FRAME_RATE:
          sortOrder = compareNullFirst(movie1.getMediaInfoFrameRate(), movie2.getMediaInfoFrameRate());
          if (sortOrder != null && sortOrder == 0) {
            sortOrder = Double.compare(movie1.getMediaInfoFrameRate(), movie2.getMediaInfoFrameRate());
          }
          break;

        case RELEASE_DATE:
          sortOrder = compareNullFirst(movie1.getReleaseDate(), movie2.getReleaseDate());
          if (sortOrder != null && sortOrder == 0) {
            sortOrder = movie1.getReleaseDate().compareTo(movie2.getReleaseDate());
          }
          break;
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    if (sortOrder == null) {
      sortOrder = 0;
    }

    // if the sort order == 0 and the sorting column is not the title, sort on the title too
    if (sortOrder == 0 && sortColumn != SortColumn.TITLE && sortColumn != SortColumn.SORT_TITLE) {
      sortOrder = stringCollator.compare(movie1.getTitleSortable().toLowerCase(Locale.ROOT), movie2.getTitleSortable().toLowerCase(Locale.ROOT));
    }

    // sort ascending or descending
    if (sortAscending) {
      return sortOrder;
    }
    else {
      return sortOrder * -1;
    }
  }

  private Integer compareNullFirst(Object o1, Object o2) {
    Integer sort;
    if (o1 == null && o2 == null) {
      sort = null;
    }
    else if (o1 == null) {
      sort = -1;
    }
    else if (o2 == null) {
      sort = 1;
    }
    else {
      sort = 0;
    }
    return sort;
  }
}
