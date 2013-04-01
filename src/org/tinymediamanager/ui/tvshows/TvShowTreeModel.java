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
package org.tinymediamanager.ui.tvshows;

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * @author Manuel Laggner
 * 
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

  public TvShowTreeModel(List<TvShow> tvShows) {
    // create the listener
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // react on changes of tv shows
        if (TV_SHOWS.equals(evt.getPropertyName())) {
          // check changes of tv shows

        }
      }
    };

    // install a property change listener to the TvShowList
    tvShowList.addPropertyChangeListener(propertyChangeListener);

    // build initial tree
    for (TvShow tvShow : tvShows) {
      DefaultMutableTreeNode tvShowNode = new TvShowTreeNode(tvShow);
      nodeMap.put(tvShow, tvShowNode);
      for (TvShowSeason season : tvShow.getSeasons()) {
        // check if there is a node for its season
        TvShowSeasonTreeNode seasonNode = (TvShowSeasonTreeNode) nodeMap.get(season);
        if (seasonNode == null) {
          seasonNode = new TvShowSeasonTreeNode(season);
          tvShowNode.add(seasonNode);
          nodeMap.put(season, seasonNode);
        }

        for (TvShowEpisode episode : season.getEpisodes()) {
          TvShowEpisodeTreeNode episodeNode = new TvShowEpisodeTreeNode(episode);
          seasonNode.add(episodeNode);
          nodeMap.put(episode, episodeNode);
        }
      }

      root.add(tvShowNode);

      // implement change listener
      tvShow.addPropertyChangeListener(propertyChangeListener);
    }

    // sort the root and all children
    root.sort();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
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
  @Override
  public Object getChild(Object parent, int index) {
    return ((TreeNode) parent).getChildAt(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
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
  @Override
  public int getIndexOfChild(Object parent, Object child) {
    return ((TreeNode) parent).getIndex((TreeNode) child);
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
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
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
  @Override
  public void removeTreeModelListener(TreeModelListener listener) {
    listeners.remove(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  @Override
  public void valueForPathChanged(TreePath arg0, Object arg1) {
    // TODO Auto-generated method stub

  }

}
