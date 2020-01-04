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
package org.tinymediamanager.ui.moviesets;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.components.treetable.TmmTreeTable;

/**
 * The Class MovieSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class MovieSetSelectionModel extends AbstractModelObject {
  public static final String     SELECTED_MOVIE_SET = "selectedMovieSet";

  private MovieSet               selectedMovieSet;
  private MovieSet               initalMovieSet     = new MovieSet("");
  private PropertyChangeListener propertyChangeListener;
  private TmmTreeTable           treeTable;

  /**
   * Instantiates a new movie selection model. Usage in MovieSetPanel
   */
  public MovieSetSelectionModel() {
    selectedMovieSet = initalMovieSet;
    propertyChangeListener = evt -> {
      if (evt.getSource() == selectedMovieSet) {
        // wrap this event in a new event for listeners of the selection model
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
      }
    };
  }

  public void setTreeTable(TmmTreeTable treeTable) {
    this.treeTable = treeTable;
  }

  /**
   * Sets the selected movie set.
   * 
   * @param movieSet
   *          the new selected movie set
   */
  public void setSelectedMovieSet(MovieSet movieSet) {
    MovieSet oldValue = this.selectedMovieSet;

    if (movieSet != null) {
      this.selectedMovieSet = movieSet;
    }
    else {
      this.selectedMovieSet = initalMovieSet;
    }

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

  /**
   * Gets the selected movie sets
   * 
   * @return the selected movie sets
   */
  public List<MovieSet> getSelectedMovieSets() {
    List<MovieSet> selectedMovieSets = new ArrayList<>();

    for (Object obj : getSelectedObjects()) {
      if (obj instanceof MovieSet) {
        selectedMovieSets.add((MovieSet) obj);
      }
    }

    return selectedMovieSets;
  }

  /**
   * get all selected movies. selected movie sets will NOT return all their movies
   * 
   * @return list of all selected movies
   */
  public List<Movie> getSelectedMovies() {
    List<Movie> selectedMovies = new ArrayList<>();

    for (Object obj : getSelectedObjects()) {
      if (obj instanceof Movie) {
        selectedMovies.add((Movie) obj);
      }
    }

    return selectedMovies;
  }

  /**
   * Get all selected objects from the treeTable
   *
   * @return the selected objects
   */
  public List<Object> getSelectedObjects() {
    List<Object> selectedObjects = new ArrayList<>();

    int rows[] = treeTable.getSelectedRows();
    for (int row : rows) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeTable.getValueAt(row, 0);
      if (node != null) {
        Object userObject = node.getUserObject();
        if (userObject instanceof MovieSet) {
          selectedObjects.add(userObject);
        }
        else if (userObject instanceof Movie) {
          selectedObjects.add(userObject);
        }
      }
    }
    return selectedObjects;
  }
}
