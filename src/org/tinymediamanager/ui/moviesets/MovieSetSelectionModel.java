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
package org.tinymediamanager.ui.moviesets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieSet;

/**
 * The Class MovieSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class MovieSetSelectionModel extends AbstractModelObject {
  private static final String    SELECTED_MOVIE_SET = "selectedMovieSet";

  private MovieSet               selectedMovieSet;
  private MovieSet               initalMovieSet     = new MovieSet("");
  private PropertyChangeListener propertyChangeListener;
  private JTree                  tree;

  /**
   * Instantiates a new movie selection model. Usage in MovieSetPanel
   */
  public MovieSetSelectionModel(JTree tree) {
    selectedMovieSet = initalMovieSet;
    this.tree = tree;

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
    List<MovieSet> selectedMovieSets = new ArrayList<MovieSet>();
    TreePath[] paths = tree.getSelectionPaths();
    // tree.clearSelection();

    // filter out all movie sets from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof MovieSet) {
            MovieSet movieSet = (MovieSet) node.getUserObject();
            selectedMovieSets.add(movieSet);
          }
        }
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
    List<Movie> selectedMovies = new ArrayList<Movie>();
    TreePath[] paths = tree.getSelectionPaths();

    // filter out all movie sets from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof Movie) {
            Movie movie = (Movie) node.getUserObject();
            selectedMovies.add(movie);
          }
        }
      }
    }

    return selectedMovies;
  }

  /**
   * get all selected movies. selected movie sets will return all their movies
   * 
   * @return list of all selected movies
   */
  public List<Movie> getSelectedMoviesRecursive() {
    List<Movie> selectedMovies = new ArrayList<Movie>();

    TreePath[] paths = tree.getSelectionPaths();

    // filter out all movie sets from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof MovieSet) {
            MovieSet movieSet = (MovieSet) node.getUserObject();
            for (Movie movie : movieSet.getMovies()) {
              if (!selectedMovies.contains(movie)) {
                selectedMovies.add(movie);
              }
            }
          }
          if (node.getUserObject() instanceof Movie) {
            Movie movie = (Movie) node.getUserObject();
            if (!selectedMovies.contains(movie)) {
              selectedMovies.add(movie);
            }
          }
        }
      }
    }

    return selectedMovies;
  }
}
