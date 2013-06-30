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
import java.util.HashMap;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;

/**
 * The Class MovieSetTreeModel.
 * 
 * @author Manuel Laggner
 */
public class MovieSetTreeModel implements TreeModel {
  private MovieSetRootTreeNode      root      = new MovieSetRootTreeNode();
  private List<TreeModelListener>   listeners = new ArrayList<TreeModelListener>();
  private HashMap<Object, TreeNode> nodeMap   = new HashMap<Object, TreeNode>();
  private PropertyChangeListener    propertyChangeListener;
  private MovieList                 movieList = MovieList.getInstance();

  /**
   * Instantiates a new movie set tree model.
   * 
   * @param movieSets
   *          the movie sets
   */
  public MovieSetTreeModel(List<MovieSet> movieSets) {
    // create the listener
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // react on changes of movies attached to this set
        if ("addedMovie".equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getNewValue();
          MovieSet movieSet = (MovieSet) evt.getSource();
          addMovie(movieSet, movie);
        }
        if ("removedMovie".equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getNewValue();
          MovieSet movieSet = (MovieSet) evt.getSource();
          removeMovie(movieSet, movie);
        }
        if ("removedAllMovies".equals(evt.getPropertyName())) {
          @SuppressWarnings("unchecked")
          List<Movie> removedMovies = (List<Movie>) evt.getOldValue();
          MovieSet movieSet = (MovieSet) evt.getSource();
          for (Movie movie : removedMovies) {
            removeMovie(movieSet, movie);
          }
        }
        if ("addedMovieSet".equals(evt.getPropertyName())) {
          MovieSet movieSet = (MovieSet) evt.getNewValue();
          addMovieSet(movieSet);
        }
        if ("removedMovieSet".equals(evt.getPropertyName())) {
          MovieSet movieSet = (MovieSet) evt.getNewValue();
          removeMovieSet(movieSet);
        }
        if ("movies".equals(evt.getPropertyName()) && evt.getSource() instanceof MovieSet) {
          // sort order of movies inside the movieset changed
          MovieSet movieSet = (MovieSet) evt.getSource();
          sortMoviesInMovieSet(movieSet);
        }

        // update on changes of a movie
        if (evt.getSource() instanceof MovieSet || evt.getSource() instanceof Movie) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeMap.get(evt.getSource());
          if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            int index = parent.getIndex(node);
            TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { node });
            for (TreeModelListener listener : listeners) {
              listener.treeNodesChanged(event);
            }
          }
        }
      }
    };

    // build initial tree
    for (MovieSet movieSet : movieSets) {
      DefaultMutableTreeNode setNode = new MovieSetTreeNode(movieSet);
      nodeMap.put(movieSet, setNode);
      for (Movie movie : movieSet.getMovies()) {
        DefaultMutableTreeNode movieNode = new MovieTreeNode(movie);
        setNode.add(movieNode);
        nodeMap.put(movie, movieNode);
      }
      root.add(setNode);

      // implement change listener
      movieSet.addPropertyChangeListener(propertyChangeListener);
    }

    movieList.addPropertyChangeListener(propertyChangeListener);

    root.sort();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
   */
  @Override
  public Object getChild(Object parent, int index) {
    return ((TreeNode) parent).getChildAt(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  @Override
  public Object getRoot() {
    return root;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    return ((TreeNode) parent).getChildCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    // root is never a leaf
    if (node == root) {
      return false;
    }

    if (node instanceof MovieSetTreeNode) {
      MovieSetTreeNode mstnode = (MovieSetTreeNode) node;
      if (mstnode.getUserObject() instanceof MovieSet) {
        return false;
      }
    }

    return getChildCount(node) == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    return ((TreeNode) parent).getIndex((TreeNode) child);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event. TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener listener) {
    listeners.add(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event. TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener listener) {
    listeners.remove(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

  /**
   * Adds the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void addMovieSet(MovieSet movieSet) {
    MovieSetTreeNode child = new MovieSetTreeNode(movieSet);
    nodeMap.put(movieSet, child);
    // add the node
    root.add(child);
    root.sort();

    int index = root.getIndex(child);

    // inform listeners
    TreeModelEvent event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { child });

    for (TreeModelListener listener : listeners) {
      listener.treeNodesInserted(event);
    }

    movieSet.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Adds the movie.
   * 
   * @param movieSet
   *          the movie set
   * @param movie
   *          the movie
   */
  private void addMovie(MovieSet movieSet, Movie movie) {
    // get the movie set node
    MovieSetTreeNode parent = (MovieSetTreeNode) nodeMap.get(movieSet);
    MovieTreeNode child = new MovieTreeNode(movie);
    if (parent != null) {
      nodeMap.put(movie, child);
      parent.add(child);
      int index = parent.getIndex(child);

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesInserted(event);
      }

    }
  }

  /**
   * Removes the movie.
   * 
   * @param movieSet
   *          the movie set
   * @param movie
   *          the movie
   */
  private void removeMovie(MovieSet movieSet, Movie movie) {
    // get the movie set node
    MovieSetTreeNode parent = (MovieSetTreeNode) nodeMap.get(movieSet);
    MovieTreeNode child = (MovieTreeNode) nodeMap.get(movie);
    if (parent != null && child != null) {
      int index = parent.getIndex(child);
      parent.remove(child);
      nodeMap.remove(movie);

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesRemoved(event);
      }
    }
  }

  /**
   * Removes the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void removeMovieSet(MovieSet movieSet) {
    MovieSetTreeNode node = (MovieSetTreeNode) nodeMap.get(movieSet);
    int index = root.getIndex(node);

    movieSet.removePropertyChangeListener(propertyChangeListener);

    nodeMap.remove(movieSet);
    for (Movie movie : movieSet.getMovies()) {
      nodeMap.remove(movie);
      movie.removePropertyChangeListener(propertyChangeListener);
    }

    node.removeAllChildren();
    node.removeFromParent();

    // inform listeners
    TreeModelEvent event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { node });

    for (TreeModelListener listener : listeners) {
      listener.treeNodesRemoved(event);
    }
  }

  /**
   * Removes the.
   * 
   * @param path
   *          the path
   */
  public void remove(TreePath path) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getParentPath().getLastPathComponent();
    int index = parent.getIndex(node);

    // remove a movieset and all referenced movies
    if (node.getUserObject() instanceof MovieSet) {
      MovieSet movieSet = (MovieSet) node.getUserObject();
      for (Movie movie : movieSet.getMovies()) {
        movie.setMovieSet(null);
        movie.saveToDb();
        movie.writeNFO();
        nodeMap.remove(movie);
      }
      movieSet.removeAllMovies();
      movieSet.removePropertyChangeListener(propertyChangeListener);
      movieList.removeMovieSet(movieSet);
      nodeMap.remove(movieSet);

      node.removeAllChildren();
      node.removeFromParent();

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { node });

      for (TreeModelListener listener : listeners) {
        listener.treeNodesRemoved(event);
      }
    }

    // remove a movie
    if (node.getUserObject() instanceof Movie) {
      Movie movie = (Movie) node.getUserObject();
      MovieSet movieSet = movie.getMovieSet();
      if (movieSet != null) {
        movieSet.removeMovie(movie);
      }

      nodeMap.remove(movie);

      movie.setMovieSet(null);
      movie.saveToDb();
      movie.writeNFO();

      // here we do not need to inform listeners - is already done via
      // propertychangesupport (movieSet.removeMovie)
    }
  }

  /**
   * Sort movies in movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void sortMoviesInMovieSet(MovieSet movieSet) {
    MovieSetTreeNode node = (MovieSetTreeNode) nodeMap.get(movieSet);
    node.sort();

    // inform listeners
    TreeModelEvent event = new TreeModelEvent(this, node.getPath());

    for (TreeModelListener listener : listeners) {
      listener.treeStructureChanged(event);
    }
  }

  /**
   * Sort movie sets.
   */
  public void sortMovieSets() {
    root.sort();

    // inform listeners
    TreeModelEvent event = new TreeModelEvent(this, root.getPath());

    for (TreeModelListener listener : listeners) {
      listener.treeStructureChanged(event);
    }
  }
}
