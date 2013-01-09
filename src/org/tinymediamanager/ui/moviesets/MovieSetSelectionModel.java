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
package org.tinymediamanager.ui.moviesets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.MovieSet;

/**
 * The Class MovieSelectionModel.
 */
public class MovieSetSelectionModel extends AbstractModelObject {

  /** The Constant SELECTED_MOVIE_SET. */
  private static final String    SELECTED_MOVIE_SET = "selectedMovieSet";

  /** The selected movie. */
  private MovieSet               selectedMovieSet;

  /** The inital movie. */
  private MovieSet               initalMovieSet     = new MovieSet("");

  /** The property change listener. */
  private PropertyChangeListener propertyChangeListener;

  /**
   * Instantiates a new movie selection model. Usage in MovieSetPanel
   */
  public MovieSetSelectionModel() {
    selectedMovieSet = initalMovieSet;

    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
      }
    };
  }

  /**
   * Sets the selected movie set.
   * 
   * @param movieSet
   *          the new selected movie set
   */
  public void setSelectedMovieSet(MovieSet movieSet) {
    MovieSet oldValue = this.selectedMovieSet;
    this.selectedMovieSet = movieSet;

    if (oldValue != null) {
      oldValue.removePropertyChangeListener(propertyChangeListener);
    }

    if (selectedMovieSet != null) {
      selectedMovieSet.addPropertyChangeListener(propertyChangeListener);
    }

    firePropertyChange(SELECTED_MOVIE_SET, oldValue, this.selectedMovieSet);
  }

  /**
   * Gets the selected movie set.
   * 
   * @return the selected movie set
   */
  public MovieSet getSelectedMovieSet() {
    return selectedMovieSet;
  }

}
