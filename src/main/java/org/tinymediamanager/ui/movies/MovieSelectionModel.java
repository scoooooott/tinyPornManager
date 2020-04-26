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

import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.AbstractSettings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.movies.filters.IMovieUIFilter;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;

/**
 * The Class MovieSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class MovieSelectionModel extends AbstractModelObject implements ListSelectionListener {
  private static final String               SELECTED_MOVIE = "selectedMovie";

  public final Movie                        initialMovie   = new Movie();

  private List<Movie>                       selectedMovies;
  private Movie                             selectedMovie;
  private DefaultEventSelectionModel<Movie> selectionModel;
  private MovieMatcherEditor                matcherEditor;
  private SortedList<Movie>                 sortedList;
  private PropertyChangeListener            propertyChangeListener;

  /**
   * Instantiates a new movie selection model. Usage in MoviePanel
   * 
   * @param sortedList
   *          the sorted list
   * @param source
   *          the source
   * @param matcher
   *          the matcher
   */
  public MovieSelectionModel(SortedList<Movie> sortedList, EventList<Movie> source, MovieMatcherEditor matcher) {
    constructInitialMovie();

    this.sortedList = sortedList;
    this.selectionModel = new DefaultEventSelectionModel<>(source);
    this.selectionModel.addListSelectionListener(this);
    this.matcherEditor = matcher;
    this.selectedMovies = selectionModel.getSelected();

    propertyChangeListener = evt -> {
      if (evt.getSource() == selectedMovie) {
        // wrap this event in a new event for listeners of the selection model
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
      }
      if (evt.getSource() instanceof IMovieUIFilter) {
        firePropertyChange("filterChanged", null, evt.getSource());
      }
    };
  }

  /**
   * Instantiates a new movie selection model. Usage in MovieSetPanel
   */
  public MovieSelectionModel() {
    constructInitialMovie();
  }

  /**
   * the initial movie needs some things set
   */
  private void constructInitialMovie() {
    MediaFile fakeVideo = new MediaFile();
    fakeVideo.setType(MediaFileType.VIDEO);
    initialMovie.addToMediaFiles(fakeVideo);
  }

  /**
   * Sets the selected movie.
   * 
   * @param movie
   *          the new selected movie
   */
  public void setSelectedMovie(Movie movie) {
    Movie oldValue = this.selectedMovie;
    if (movie == null) {
      this.selectedMovie = initialMovie;
    }
    else {
      this.selectedMovie = movie;
    }

    if (oldValue != null) {
      oldValue.removePropertyChangeListener(propertyChangeListener);
    }

    if (selectedMovie != null) {
      selectedMovie.addPropertyChangeListener(propertyChangeListener);
    }

    firePropertyChange(SELECTED_MOVIE, oldValue, selectedMovie);
  }

  /**
   * Gets the matcher editor.
   * 
   * @return the matcher editor
   */
  public MovieMatcherEditor getMatcherEditor() {
    return matcherEditor;
  }

  /**
   * Gets the selection model.
   * 
   * @return the selection model
   */
  public DefaultEventSelectionModel<Movie> getSelectionModel() {
    return selectionModel;
  }

  /**
   * Value changed.
   * 
   * @param e
   *          the e
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;
    }

    // display first selected movie
    if (!selectedMovies.isEmpty() && selectedMovie != selectedMovies.get(0)) {
      Movie oldValue = selectedMovie;
      selectedMovie = selectedMovies.get(0);

      // unregister propertychangelistener
      if (oldValue != null && oldValue != initialMovie) {
        oldValue.removePropertyChangeListener(propertyChangeListener);
      }
      if (selectedMovie != null && selectedMovie != initialMovie) {
        selectedMovie.addPropertyChangeListener(propertyChangeListener);
      }
      firePropertyChange(SELECTED_MOVIE, oldValue, selectedMovie);
    }

    // display empty movie (i.e. when all movies are removed from the list)
    if (selectedMovies.isEmpty()) {
      Movie oldValue = selectedMovie;
      selectedMovie = initialMovie;
      // unregister propertychangelistener
      if (oldValue != null && oldValue != initialMovie) {
        oldValue.removePropertyChangeListener(propertyChangeListener);
      }
      firePropertyChange(SELECTED_MOVIE, oldValue, selectedMovie);
    }
  }

  /**
   * Gets the selected movie.
   * 
   * @return the selected movie
   */
  public Movie getSelectedMovie() {
    if (selectedMovie == null) {
      return initialMovie;
    }
    return selectedMovie;
  }

  /**
   * Gets the selected movies.
   * 
   * @return the selected movies
   */
  public List<Movie> getSelectedMovies() {
    return selectedMovies;
  }

  /**
   * Sets the selected movies.
   * 
   * @param selectedMovies
   *          the new selected movies
   */
  public void setSelectedMovies(List<Movie> selectedMovies) {
    this.selectedMovies = selectedMovies;
  }

  /**
   * Sort movies.
   * 
   * @param column
   *          the column
   * @param ascending
   *          the ascending
   */
  public void sortMovies(MovieExtendedComparator.SortColumn column, boolean ascending) {
    Comparator<Movie> comparator = new MovieExtendedComparator(column, ascending);
    sortedList.setComparator(comparator);
  }

  /**
   * Add an UI filter
   * 
   * @param filter
   *          the new filter to be added
   */
  public void addFilter(IMovieUIFilter filter) {
    matcherEditor.addFilter(filter);
    filter.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * set any stored filter values
   * 
   * @param values
   *          the values to be set
   */
  void setFilterValues(List<AbstractSettings.UIFilters> values) {
    matcherEditor.setFilterValues(values);
    firePropertyChange("filterChanged", null, values);
  }
}
