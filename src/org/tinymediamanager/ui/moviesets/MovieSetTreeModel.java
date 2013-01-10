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
 */
public class MovieSetTreeModel implements TreeModel {

  /** The root. */
  private DefaultMutableTreeNode    root      = new DefaultMutableTreeNode("MovieSets");

  /** The listeners. */
  private List<TreeModelListener>   listeners = new ArrayList<TreeModelListener>();

  /** The node map to store the node for Objects. */
  private HashMap<Object, TreeNode> nodeMap   = new HashMap<Object, TreeNode>();

  private PropertyChangeListener    propertyChangeListener;

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
      }
    };

    // build initial tree
    for (MovieSet movieSet : movieSets) {
      DefaultMutableTreeNode setNode = new MovieSetTreeNode(movieSet);
      nodeMap.put(movieSet, setNode);
      for (Movie movie : movieSet.getMovies()) {
        DefaultMutableTreeNode movieNode = new MovieSetTreeNode(movie);
        setNode.add(movieNode);
        nodeMap.put(movie, movieNode);
      }
      root.add(setNode);

      // implement change listener
      movieSet.addPropertyChangeListener(propertyChangeListener);
    }
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
    return getChildCount(node) == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
   * java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    return ((TreeNode) parent).getIndex((TreeNode) child);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
   * TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener listener) {
    listeners.add(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
   * TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener listener) {
    listeners.remove(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
   * java.lang.Object)
   */
  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // foo
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
    int index = root.getIndex(child);

    // inform listeners
    TreeModelEvent event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { child });

    for (TreeModelListener listener : listeners)
      listener.treeNodesInserted(event);
  }

  private void addMovie(MovieSet movieSet, Movie movie) {
    // get the movie set node
    MovieSetTreeNode parent = (MovieSetTreeNode) nodeMap.get(movieSet);
    if (parent != null) {
      MovieSetTreeNode child = new MovieSetTreeNode(movie);
      nodeMap.put(movie, child);
      parent.add(child);

      int index = parent.getIndex(child);

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners)
        listener.treeNodesInserted(event);
    }
  }

  private void removeMovie(MovieSet movieSet, Movie movie) {
    // get the movie set node
    MovieSetTreeNode parent = (MovieSetTreeNode) nodeMap.get(movieSet);
    if (parent != null) {
      MovieSetTreeNode child = (MovieSetTreeNode) nodeMap.get(movie);
      nodeMap.remove(movie);
      int index = parent.getIndex(child);

      parent.remove(child);

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners)
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
    int index = getIndexOfChild(parent, node);

    // remove a movieset and all referenced movies
    if (node.getUserObject() instanceof MovieSet) {
      MovieSet movieSet = (MovieSet) node.getUserObject();
      for (Movie movie : movieSet.getMovies()) {
        movie.setMovieSet(null);
        movie.saveToDb();
      }
      movieSet.removeAllMovies();
      MovieList.getInstance().removeMovieSet(movieSet);

      node.removeAllChildren();
      node.removeFromParent();
    }

    // remove a movie
    if (node.getUserObject() instanceof Movie) {
      Movie movie = (Movie) node.getUserObject();
      MovieSet movieSet = movie.getMovieSet();
      if (movieSet != null) {
        movieSet.removeMovie(movie);
      }

      movie.setMovieSet(null);
      movie.saveToDb();

      node.removeFromParent();
    }

    // inform listeners
    TreeModelEvent event = new TreeModelEvent(this, path.getParentPath(), new int[] { index }, new Object[] { node });

    for (TreeModelListener listener : listeners)
      listener.treeNodesRemoved(event);
  }

}
