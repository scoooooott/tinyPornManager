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
package org.tinymediamanager.ui.moviesets.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.AbstractTmmUIFilter;
import org.tinymediamanager.ui.components.tree.ITmmTreeFilter;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * An abstract implementation for easier usage of the ITmmUIFilter and IMovieSetUIFilter
 * 
 * @author Manuel Laggner
 */
abstract class AbstractMovieSetUIFilter extends AbstractTmmUIFilter<TmmTreeNode> implements IMovieSetUIFilter<TmmTreeNode> {
  @Override
  public boolean isActive() {
    switch (getFilterState()) {
      case ACTIVE:
      case ACTIVE_NEGATIVE:
        return true;

      default:
        return false;
    }
  }

  @Override
  public boolean accept(TmmTreeNode node) {
    // is this filter active?
    if (getFilterState() == FilterState.INACTIVE) {
      return true;
    }

    Object userObject = node.getUserObject();

    if (userObject instanceof MovieSet) {
      MovieSet movieSet = (MovieSet) userObject;
      if (getFilterState() == FilterState.ACTIVE) {
        return accept(movieSet, new ArrayList<>(movieSet.getMovies()));
      }
      else if (getFilterState() == FilterState.ACTIVE_NEGATIVE) {
        return !accept(movieSet, new ArrayList<>(movieSet.getMovies()));
      }
    }
    else if (userObject instanceof Movie) {
      Movie movie = (Movie) userObject;

      // just in case we're getting an event just when we change the assigned movie set
      if (movie.getMovieSet() == null) {
        return false;
      }

      if (getFilterState() == FilterState.ACTIVE) {
        return accept(movie.getMovieSet(), Collections.singletonList(movie));
      }
      else if (getFilterState() == FilterState.ACTIVE_NEGATIVE) {
        return !accept(movie.getMovieSet(), Collections.singletonList(movie));
      }
    }

    return true;
  }

  /**
   * should we accept the node providing this data?
   * 
   * @param movieSet
   *          the movie set of this node
   * @param movies
   *          all movies of this node
   * @return whether we should or not accept it.
   */
  protected abstract boolean accept(MovieSet movieSet, List<Movie> movies);

  /**
   * delegate the filter changed event to the tree
   */
  @Override
  protected void filterChanged() {
    SwingUtilities.invokeLater(() -> firePropertyChange(ITmmTreeFilter.TREE_FILTER_CHANGED, checkBox.isSelected(), !checkBox.isSelected()));
  }
}
