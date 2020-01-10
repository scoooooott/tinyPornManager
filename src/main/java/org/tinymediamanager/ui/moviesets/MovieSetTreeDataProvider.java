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
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.components.tree.TmmTreeDataProvider;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * The class MovieSetTreeDataProvider is used for providing and managing the data for the movie set tree
 * 
 * @author Manuel Laggner
 */
public class MovieSetTreeDataProvider extends TmmTreeDataProvider<TmmTreeNode> {
  private TmmTreeNode                  root           = new TmmTreeNode(new Object(), this);
  private RuleBasedCollator            stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

  private final PropertyChangeListener movielistPropertyChangeListener;
  private final PropertyChangeListener movieSetPropertyChangeListener;
  private final PropertyChangeListener moviePropertyChangeListener;

  private final MovieList              movieList      = MovieList.getInstance();

  public MovieSetTreeDataProvider() {
    movielistPropertyChangeListener = evt -> {
      MovieSet movieSet;

      switch (evt.getPropertyName()) {
        case Constants.ADDED_MOVIE_SET:
          movieSet = (MovieSet) evt.getNewValue();
          addMovieSet(movieSet);
          break;

        case Constants.REMOVED_MOVIE_SET:
          movieSet = (MovieSet) evt.getNewValue();
          removeMovieSet(movieSet);
          break;

        default:
          nodeChanged(evt.getSource());
          break;
      }
    };
    movieList.addPropertyChangeListener(movielistPropertyChangeListener);

    movieSetPropertyChangeListener = evt -> {
      Movie movie;

      switch (evt.getPropertyName()) {
        case Constants.ADDED_MOVIE:
          movie = (Movie) evt.getNewValue();
          addMovie(movie);
          break;

        case Constants.REMOVED_MOVIE:
          movie = (Movie) evt.getNewValue();
          removeMovie(movie);
          break;

        default:
          nodeChanged(evt.getSource());
          break;
      }
    };

    moviePropertyChangeListener = evt -> {
      switch (evt.getPropertyName()) {
        default:
          nodeChanged(evt.getSource());
          break;
      }
    };

    setTreeComparator(new MovieSetComparator());
  }

  /**
   * trigger a node changed event for all other events
   * 
   * @param source
   */
  private void nodeChanged(Object source) {
    TmmTreeNode node = getNodeFromCache(source);
    if (node != null) {
      firePropertyChange(NODE_CHANGED, null, node);
    }
  }

  @Override
  public TmmTreeNode getRoot() {
    return root;
  }

  @Override
  public TmmTreeNode getParent(TmmTreeNode child) {
    if (child.getUserObject() instanceof MovieSet) {
      return root;
    }
    else if (child.getUserObject() instanceof Movie) {
      Movie movie = (Movie) child.getUserObject();
      TmmTreeNode node = getNodeFromCache(movie.getMovieSet());
      // parent movie set not yet added? add it
      if (node == null && movie.getMovieSet() != null) {
        node = addMovieSet(movie.getMovieSet());
      }
      return node;
    }
    return null;
  }

  @Override
  public List<TmmTreeNode> getChildren(TmmTreeNode parent) {
    if (parent == root) {
      ArrayList<TmmTreeNode> nodes = new ArrayList<>();
      for (MovieSet movieSet : new ArrayList<>(movieList.getMovieSetList())) {
        TmmTreeNode node = new MovieSetTreeNode(movieSet, this);
        putNodeToCache(movieSet, node);
        nodes.add(node);

        // add a propertychangelistener for this movie set
        movieSet.addPropertyChangeListener(movieSetPropertyChangeListener);
      }
      return nodes;
    }
    else if (parent.getUserObject() instanceof MovieSet) {
      MovieSet movieSet = (MovieSet) parent.getUserObject();
      ArrayList<TmmTreeNode> nodes = new ArrayList<>();
      for (Movie movie : movieSet.getMovies()) {
        TmmTreeNode node = new MovieTreeNode(movie, this);
        putNodeToCache(movie, node);
        nodes.add(node);
      }
      return nodes;
    }
    return null;
  }

  @Override
  public boolean isLeaf(TmmTreeNode node) {
    return node.getUserObject() instanceof Movie;
  }

  @Override
  public Comparator<TmmTreeNode> getTreeComparator() {
    return super.getTreeComparator();
  }

  private TmmTreeNode addMovieSet(MovieSet movieSet) {
    // check if this movie set has already been added
    TmmTreeNode cachedNode = getNodeFromCache(movieSet);
    if (cachedNode != null) {
      return cachedNode;
    }

    // add a new node
    TmmTreeNode node = new MovieSetTreeNode(movieSet, this);
    putNodeToCache(movieSet, node);
    firePropertyChange(NODE_INSERTED, null, node);

    // and also add a propertychangelistener to react on changes inside the movie set
    movieSet.addPropertyChangeListener(movieSetPropertyChangeListener);
    return node;
  }

  private void removeMovieSet(MovieSet movieSet) {
    TmmTreeNode cachedNode = removeNodeFromCache(movieSet);
    if (cachedNode == null) {
      return;
    }

    // remove all children from the map (the nodes will be removed by the treemodel)
    for (Movie movie : movieSet.getMovies()) {
      removeNodeFromCache(movie);
    }

    // remove the propertychangelistener from this movie set
    movieSet.removePropertyChangeListener(movieSetPropertyChangeListener);

    firePropertyChange(NODE_REMOVED, null, cachedNode);
  }

  private TmmTreeNode addMovie(Movie movie) {
    // check if this season has already been added
    TmmTreeNode cachedNode = getNodeFromCache(movie);
    if (cachedNode != null) {
      return cachedNode;
    }

    // add a new node
    TmmTreeNode node = new MovieTreeNode(movie, this);
    putNodeToCache(movie, node);
    firePropertyChange(NODE_INSERTED, null, node);

    // and also add a propertychangelistener to react on changes inside the movie
    movie.addPropertyChangeListener(moviePropertyChangeListener);

    return node;
  }

  private void removeMovie(Movie movie) {
    TmmTreeNode cachedNode = removeNodeFromCache(movie);
    if (cachedNode == null) {
      return;
    }

    // remove the propertychangelistener from this episode
    movie.removePropertyChangeListener(moviePropertyChangeListener);

    firePropertyChange(NODE_REMOVED, null, cachedNode);
  }

  /*
   * helper classes
   */
  class MovieSetComparator implements Comparator<TmmTreeNode> {
    @Override
    public int compare(TmmTreeNode o1, TmmTreeNode o2) {
      Object userObject1 = o1.getUserObject();
      Object userObject2 = o2.getUserObject();

      if (userObject1 instanceof MovieSet && userObject2 instanceof MovieSet) {
        MovieSet MovieSet1 = (MovieSet) userObject1;
        MovieSet MovieSet2 = (MovieSet) userObject2;
        if (stringCollator != null) {
          return stringCollator.compare(MovieSet1.getTitleSortable().toLowerCase(Locale.ROOT), MovieSet2.getTitleSortable().toLowerCase(Locale.ROOT));
        }
        return MovieSet1.getTitleSortable().compareToIgnoreCase(MovieSet2.getTitleSortable());
      }

      if (userObject1 instanceof Movie && userObject2 instanceof Movie) {
        if (((Movie) userObject1).getMovieSet() != null) {
          List<Movie> moviesInSet = ((Movie) userObject1).getMovieSet().getMovies();
          return moviesInSet.indexOf(userObject1) - moviesInSet.indexOf(userObject2);
        }
      }

      return o1.toString().compareToIgnoreCase(o2.toString());
    }
  }

  class MovieSetTreeNode extends TmmTreeNode {
    private static final long serialVersionUID = -1316609340104597133L;

    public MovieSetTreeNode(Object userObject, TmmTreeDataProvider dataProvider) {
      super(userObject, dataProvider);
    }

    /**
     * provides the right name of the node for display.
     * 
     * @return the string
     */
    @Override
    public String toString() {
      // return movieSet name
      if (getUserObject() instanceof MovieSet) {
        MovieSet MovieSet = (MovieSet) getUserObject();
        return MovieSet.getTitleSortable();
      }

      // fallback: call super
      return super.toString();
    }
  }

  class MovieTreeNode extends TmmTreeNode {
    private static final long serialVersionUID = -5734830011018805194L;

    public MovieTreeNode(Object userObject, TmmTreeDataProvider dataProvider) {
      super(userObject, dataProvider);
    }

    /**
     * provides the right name of the node for display
     */
    @Override
    public String toString() {
      // return movieSet name
      if (getUserObject() instanceof Movie) {
        Movie movie = (Movie) getUserObject();
        return movie.getTitleSortable();
      }

      // fallback: call super
      return super.toString();
    }
  }
}
