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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.Movie;

/**
 * The Class MovieSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class MovieSelectionModel extends AbstractModelObject implements ListSelectionListener {

  /** The Constant SELECTED_MOVIE. */
  private static final String    SELECTED_MOVIE = "selectedMovie";

  /** The selected movies. */
  private List<Movie>            selectedMovies = new ArrayList<Movie>();

  /** The selected movie. */
  private Movie                  selectedMovie;

  /** The inital movie. */
  private Movie                  initalMovie    = new Movie();

  /** The table. */
  private JTable                 table;

  /** The filtered movies. */
  private final List<Movie>      filteredMovies;

  /** The property change listener. */
  private PropertyChangeListener propertyChangeListener;

  /**
   * Instantiates a new movie selection model. Usage in MoviePanel
   * 
   * @param table
   *          the table
   */
  public MovieSelectionModel(JTable table) {
    this.table = table;
    table.getSelectionModel().addListSelectionListener(this);

    MovieTableModel model = (MovieTableModel) table.getModel();
    this.filteredMovies = model.getFilteredMovies();

    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == selectedMovie) {
          firePropertyChange(evt);
        }
      }
    };
  }

  /**
   * Instantiates a new movie selection model.
   */
  public MovieSelectionModel() {
    filteredMovies = new ArrayList<Movie>();
  }

  /**
   * Sets the selected movie.
   * 
   * @param movie
   *          the new selected movie
   */
  public void setSelectedMovie(Movie movie) {
    Movie oldValue = this.selectedMovie;
    this.selectedMovie = movie;

    if (oldValue != null) {
      oldValue.removePropertyChangeListener(propertyChangeListener);
    }

    if (selectedMovie != null) {
      selectedMovie.addPropertyChangeListener(propertyChangeListener);
    }

    firePropertyChange(SELECTED_MOVIE, oldValue, movie);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event. ListSelectionEvent)
   */
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

    System.out.println("ping");

    selectedMovies.clear();
    for (int i : table.getSelectedRows()) {
      selectedMovies.add(filteredMovies.get(table.convertRowIndexToModel(i)));
    }

    // display first selected movie
    if (selectedMovies.size() > 0 && selectedMovie != selectedMovies.get(0)) {
      Movie oldValue = selectedMovie;
      selectedMovie = selectedMovies.get(0);

      // unregister propertychangelistener
      if (oldValue != null) {
        oldValue.removePropertyChangeListener(propertyChangeListener);
      }
      if (selectedMovie != null) {
        selectedMovie.addPropertyChangeListener(propertyChangeListener);
      }
      firePropertyChange(SELECTED_MOVIE, oldValue, selectedMovie);
    }

    // display empty movie (i.e. when all movies are removed from the list)
    if (selectedMovies.size() == 0) {
      Movie oldValue = selectedMovie;
      selectedMovie = initalMovie;
      // unregister propertychangelistener
      if (oldValue != null) {
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
}
