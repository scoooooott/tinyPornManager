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
package org.tinymediamanager.ui.tvshows;

import static org.tinymediamanager.core.Constants.*;

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

import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowSeason;

/**
 * The Class TvShowTreeModel.
 * 
 * @author Manuel Laggner
 */
public class TvShowTreeModel implements TreeModel {
  /** The root. */
  private TvShowRootTreeNode        root       = new TvShowRootTreeNode();

  /** The listeners. */
  private List<TreeModelListener>   listeners  = new ArrayList<TreeModelListener>();

  /** The node map to store the node for Objects. */
  private HashMap<Object, TreeNode> nodeMap    = new HashMap<Object, TreeNode>();

  /** The property change listener. */
  private PropertyChangeListener    propertyChangeListener;

  /** The movie list. */
  private TvShowList                tvShowList = TvShowList.getInstance();

  /**
   * Instantiates a new tv show tree model.
   * 
   * @param tvShows
   *          the tv shows
   */
  public TvShowTreeModel(List<TvShow> tvShows) {
    // create the listener
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // added a tv show
        if (ADDED_TV_SHOW.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShow) {
          TvShow tvShow = (TvShow) evt.getNewValue();
          addTvShow(tvShow);
        }

        // removed a tv show
        if (REMOVED_TV_SHOW.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShow) {
          TvShow tvShow = (TvShow) evt.getNewValue();
          removeTvShow(tvShow);
        }

        // added a season
        if (ADDED_SEASON.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShowSeason) {
          TvShowSeason season = (TvShowSeason) evt.getNewValue();
          addTvShowSeason(season, season.getTvShow());
        }

        // added an episode
        if (ADDED_EPISODE.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) evt.getNewValue();
          addTvShowEpisode(episode, episode.getTvShow().getSeasonForEpisode(episode));
        }

        // removed an episode
        if (REMOVED_EPISODE.equals(evt.getPropertyName()) && evt.getNewValue() instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) evt.getNewValue();
          removeTvShowEpisode(episode, episode.getTvShow().getSeasonForEpisode(episode));
        }

        // update on changes of tv show or episode
        if (evt.getSource() instanceof TvShow || evt.getSource() instanceof TvShowEpisode) {
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

    // install a property change listener to the TvShowList
    tvShowList.addPropertyChangeListener(propertyChangeListener);

    // build initial tree
    for (TvShow tvShow : tvShows) {
      addTvShow(tvShow);
    }

    // sort the root and all children
    // root.sort();
  }

  /**
   * Adds the tv show.
   * 
   * @param tvShow
   *          the tv show
   */
  private synchronized void addTvShow(TvShow tvShow) {
    DefaultMutableTreeNode tvShowNode = new TvShowTreeNode(tvShow);
    nodeMap.put(tvShow, tvShowNode);
    root.add(tvShowNode);

    for (TvShowSeason season : tvShow.getSeasons()) {
      // check if there is a node for its season
      TvShowSeasonTreeNode seasonNode = (TvShowSeasonTreeNode) nodeMap.get(season);
      if (seasonNode == null) {
        addTvShowSeason(season, tvShow);
      }

      for (TvShowEpisode episode : season.getEpisodes()) {
        addTvShowEpisode(episode, season);
      }
    }

    int index = root.getIndex(tvShowNode);

    // inform listeners
    TreeModelEvent event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { tvShow });

    for (TreeModelListener listener : listeners) {
      listener.treeNodesInserted(event);
    }

    tvShow.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Removes the tv show.
   * 
   * @param tvShow
   *          the tv show
   */
  private synchronized void removeTvShow(TvShow tvShow) {
    TvShowTreeNode child = (TvShowTreeNode) nodeMap.get(tvShow);
    DefaultMutableTreeNode parent = root;
    if (parent != null && child != null) {
      int index = parent.getIndex(child);

      nodeMap.remove(tvShow);
      for (TvShowEpisode episode : tvShow.getEpisodes()) {
        nodeMap.remove(episode);
        episode.removePropertyChangeListener(propertyChangeListener);
      }

      tvShow.removePropertyChangeListener(propertyChangeListener);

      child.removeAllChildren();
      child.removeFromParent();

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesRemoved(event);
      }
    }
  }

  /**
   * Adds the tv show season.
   * 
   * @param season
   *          the season
   * @param tvShow
   *          the tv show
   */
  private synchronized void addTvShowSeason(TvShowSeason season, TvShow tvShow) {
    // get the tv show node
    TvShowTreeNode parent = (TvShowTreeNode) nodeMap.get(tvShow);
    TvShowSeasonTreeNode child = new TvShowSeasonTreeNode(season);
    if (parent != null) {
      nodeMap.put(season, child);
      parent.add(child);

      int index = parent.getIndex(child);

      // inform listeners (tv show)
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesInserted(event);
      }

      // inform listeners (root - to update the sum)
      index = root.getIndex(parent);
      event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { parent });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesChanged(event);
      }
    }
  }

  /**
   * Adds the tv show episode.
   * 
   * @param episode
   *          the episode
   * @param season
   *          the season
   */
  private synchronized void addTvShowEpisode(TvShowEpisode episode, TvShowSeason season) {
    // get the tv show season node
    TvShowSeasonTreeNode parent = (TvShowSeasonTreeNode) nodeMap.get(season);
    TvShowEpisodeTreeNode child = new TvShowEpisodeTreeNode(episode);
    if (parent != null) {
      nodeMap.put(episode, child);
      parent.add(child);
      int index = parent.getIndex(child);

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesInserted(event);
      }

      // inform listeners (root - to update the sum)
      index = root.getIndex(parent.getParent());
      event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { parent.getParent() });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesChanged(event);
      }
    }

    episode.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Removes the tv show episode.
   * 
   * @param episode
   *          the episode
   * @param season
   *          the season
   */
  private synchronized void removeTvShowEpisode(TvShowEpisode episode, TvShowSeason season) {
    // get the tv show season node
    TvShowSeasonTreeNode parent = (TvShowSeasonTreeNode) nodeMap.get(season);
    TvShowEpisodeTreeNode child = (TvShowEpisodeTreeNode) nodeMap.get(episode);
    if (parent != null && child != null) {
      int index = parent.getIndex(child);
      parent.remove(child);
      nodeMap.remove(episode);
      episode.removePropertyChangeListener(propertyChangeListener);

      // inform listeners
      TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
      for (TreeModelListener listener : listeners) {
        listener.treeNodesRemoved(event);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
   */
  /**
   * Adds the tree model listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void addTreeModelListener(TreeModelListener listener) {
    listeners.add(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
   */
  /**
   * Gets the child.
   * 
   * @param parent
   *          the parent
   * @param index
   *          the index
   * @return the child
   */
  @Override
  public Object getChild(Object parent, int index) {
    return ((TreeNode) parent).getChildAt(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  /**
   * Gets the child count.
   * 
   * @param parent
   *          the parent
   * @return the child count
   */
  @Override
  public int getChildCount(Object parent) {
    return ((TreeNode) parent).getChildCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  /**
   * Gets the index of child.
   * 
   * @param parent
   *          the parent
   * @param child
   *          the child
   * @return the index of child
   */
  @Override
  public int getIndexOfChild(Object parent, Object child) {
    TreeNode childNode = null;
    if (child instanceof TreeNode) {
      childNode = (TreeNode) child;
    }
    else {
      childNode = nodeMap.get(child);
    }
    return ((TreeNode) parent).getIndex(childNode);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  /**
   * Gets the root.
   * 
   * @return the root
   */
  @Override
  public Object getRoot() {
    return root;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  /**
   * Checks if is leaf.
   * 
   * @param node
   *          the node
   * @return true, if is leaf
   */
  @Override
  public boolean isLeaf(Object node) {
    // root is never a leaf
    if (node == root) {
      return false;
    }

    if (node instanceof TvShowTreeNode || node instanceof TvShowSeasonTreeNode) {
      return false;
    }

    if (node instanceof TvShowEpisodeTreeNode) {
      return true;
    }

    return getChildCount(node) == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
   */
  /**
   * Removes the tree model listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void removeTreeModelListener(TreeModelListener listener) {
    listeners.remove(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  /**
   * Value for path changed.
   * 
   * @param arg0
   *          the arg0
   * @param arg1
   *          the arg1
   */
  @Override
  public void valueForPathChanged(TreePath arg0, Object arg1) {
  }
}
