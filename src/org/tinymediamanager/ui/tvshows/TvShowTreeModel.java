/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.tvshows.TvShowExtendedMatcher.SearchOptions;

/**
 * The Class TvShowTreeModel.
 * 
 * @author Manuel Laggner
 */
public class TvShowTreeModel implements TreeModel {
  private TvShowRootTreeNode      root       = new TvShowRootTreeNode();
  private List<TreeModelListener> listeners  = new ArrayList<TreeModelListener>();
  private Map<Object, TreeNode>   nodeMap    = Collections.synchronizedMap(new HashMap<Object, TreeNode>());
  private TvShowList              tvShowList = TvShowList.getInstance();
  private PropertyChangeListener  propertyChangeListener;
  private TvShowExtendedMatcher   matcher    = new TvShowExtendedMatcher();

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
          removeTvShowEpisode(episode);
        }

        // changed the season of an episode
        if (SEASON.equals(evt.getPropertyName()) && evt.getSource() instanceof TvShowEpisode) {
          // simply remove it from the tree and readd it
          TvShowEpisode episode = (TvShowEpisode) evt.getSource();
          removeTvShowEpisode(episode);
          addTvShowEpisode(episode, episode.getTvShow().getSeasonForEpisode(episode));
        }

        // update on changes of tv show
        if (evt.getSource() instanceof TvShow
            && (TITLE.equals(evt.getPropertyName()) || HAS_NFO_FILE.equals(evt.getPropertyName()) || HAS_IMAGES.equals(evt.getPropertyName()))) {
          // inform listeners (root - to update the sum)
          TreeModelEvent event = new TreeModelEvent(this, root.getPath(), null, null);
          for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(event);
          }
        }
        // update on changes of episode
        if (evt.getSource() instanceof TvShowEpisode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeMap.get(evt.getSource());
          if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            int index = parent.getIndex(node);
            if (index >= 0) {
              TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { node });
              for (TreeModelListener listener : listeners) {
                try {
                  listener.treeNodesChanged(event);
                }
                catch (Exception e) {
                }
              }
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
  }

  /**
   * Adds the tv show.
   * 
   * @param tvShow
   *          the tv show
   */
  private void addTvShow(TvShow tvShow) {
    synchronized (root) {
      DefaultMutableTreeNode tvShowNode = new TvShowTreeNode(tvShow);
      root.add(tvShowNode);
      nodeMap.put(tvShow, tvShowNode);

      for (TvShowSeason season : new ArrayList<TvShowSeason>(tvShow.getSeasons())) {
        // check if there is a node for its season
        TvShowSeasonTreeNode seasonNode = (TvShowSeasonTreeNode) nodeMap.get(season);
        if (seasonNode == null) {
          addTvShowSeason(season, tvShow);
        }

        for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(season.getEpisodes())) {
          addTvShowEpisode(episode, season);
        }
      }

      int index = getIndexOfChild(root, tvShowNode);

      // inform listeners
      if (index > -1) {
        TreeModelEvent event = new TreeModelEvent(this, root.getPath(), new int[] { index }, new Object[] { tvShow });
        for (TreeModelListener listener : listeners) {
          listener.treeNodesInserted(event);
        }
      }
    }
    tvShow.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Removes the tv show.
   * 
   * @param tvShow
   *          the tv show
   */
  private void removeTvShow(TvShow tvShow) {
    synchronized (root) {
      TvShowTreeNode child = (TvShowTreeNode) nodeMap.get(tvShow);
      DefaultMutableTreeNode parent = root;
      if (child != null) {
        int index = getIndexOfChild(parent, child);

        nodeMap.remove(tvShow);
        for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(tvShow.getEpisodes())) {
          nodeMap.remove(episode);
          episode.removePropertyChangeListener(propertyChangeListener);
        }

        tvShow.removePropertyChangeListener(propertyChangeListener);

        child.removeAllChildren();
        child.removeFromParent();

        // inform listeners
        if (index > -1) {
          TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
          for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(event);
          }
        }
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
  private void addTvShowSeason(TvShowSeason season, TvShow tvShow) {
    synchronized (root) {
      // get the tv show node
      TvShowTreeNode parent = (TvShowTreeNode) nodeMap.get(tvShow);
      TvShowSeasonTreeNode child = new TvShowSeasonTreeNode(season);
      if (parent != null) {
        parent.add(child);
        nodeMap.put(season, child);

        int index = getIndexOfChild(parent, child);

        // inform listeners (tv show)
        if (index > -1) {
          TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
          for (TreeModelListener listener : listeners) {
            listener.treeNodesInserted(event);
          }
        }

        // inform listeners (root - to update the sum)
        TreeModelEvent event = new TreeModelEvent(this, root.getPath(), null, null);
        for (TreeModelListener listener : listeners) {
          listener.treeNodesChanged(event);
        }
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
  private void addTvShowEpisode(TvShowEpisode episode, TvShowSeason season) {
    synchronized (root) {
      // get the tv show season node
      TvShowSeasonTreeNode parent = (TvShowSeasonTreeNode) nodeMap.get(season);
      TvShowEpisodeTreeNode child = new TvShowEpisodeTreeNode(episode);
      if (parent != null) {
        parent.add(child);
        nodeMap.put(episode, child);

        int index = getIndexOfChild(parent, child);

        // inform listeners
        if (index > -1) {
          TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
          for (TreeModelListener listener : listeners) {
            listener.treeNodesInserted(event);
          }
        }

        // inform listeners (root - to update the sum)
        TreeModelEvent event = new TreeModelEvent(this, root.getPath(), null, null);
        for (TreeModelListener listener : listeners) {
          listener.treeNodesChanged(event);
        }
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
  private void removeTvShowEpisode(TvShowEpisode episode) {
    synchronized (root) {
      // get the tv show season node
      TvShowEpisodeTreeNode child = (TvShowEpisodeTreeNode) nodeMap.get(episode);
      TvShowSeasonTreeNode parent = null;
      if (child != null) {
        parent = (TvShowSeasonTreeNode) child.getParent();
      }

      if (parent != null && child != null) {
        int index = getIndexOfChild(parent, child);
        parent.remove(child);
        nodeMap.remove(episode);
        episode.removePropertyChangeListener(propertyChangeListener);

        // inform listeners
        if (index > -1) {
          TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
          for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(event);
          }
        }

        // remove tv show if there is no more episode in it
        if (parent.getChildCount() == 0) {
          TvShowSeason season = null;
          for (Entry<Object, TreeNode> entry : nodeMap.entrySet()) {
            if (entry.getValue() == parent) {
              season = (TvShowSeason) entry.getKey();
            }
          }
          if (season != null) {
            removeTvShowSeason(season);
          }
        }
      }
    }
  }

  /**
   * Removes the tv show season.
   * 
   * @param season
   *          the season
   */
  private void removeTvShowSeason(TvShowSeason season) {
    synchronized (root) {
      TvShowSeasonTreeNode child = (TvShowSeasonTreeNode) nodeMap.get(season);
      TvShowTreeNode parent = null;
      if (child != null) {
        parent = (TvShowTreeNode) child.getParent();
      }

      if (parent != null && child != null) {
        int index = getIndexOfChild(parent, child);
        parent.remove(child);
        nodeMap.remove(season);

        // inform listeners
        if (index > -1) {
          TreeModelEvent event = new TreeModelEvent(this, parent.getPath(), new int[] { index }, new Object[] { child });
          for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(event);
          }
        }
      }
    }
  }

  @Override
  public void addTreeModelListener(TreeModelListener listener) {
    listeners.add(listener);
  }

  @Override
  public Object getChild(Object parent, int index) {
    int count = 0;
    int childCount = getChildCountInternal(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = getChildInternal(parent, i);
      if (matches(child)) {
        if (count == index) {
          return child;
        }
        count++;
      }
    }
    return null;
  }

  @Override
  public int getChildCount(Object parent) {
    int count = 0;
    int childCount = getChildCountInternal(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = getChildInternal(parent, i);
      if (matches(child)) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int getIndexOfChild(Object parent, Object childToFind) {
    int childCount = getChildCountInternal(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = getChildInternal(parent, i);
      if (matches(child)) {
        if (childToFind.equals(child)) {
          return i;
        }
      }
    }
    return -1;
  }

  private boolean matches(Object node) {
    Object bean = null;
    if (node instanceof TvShowTreeNode) {
      bean = (TvShow) ((TvShowTreeNode) node).getUserObject();
    }

    // if the node is a TvShowSeasonNode, we have to check the parent TV show and its episodes
    if (node instanceof TvShowSeasonTreeNode) {
      bean = (TvShowSeason) ((TvShowSeasonTreeNode) node).getUserObject();
    }

    // if the node is a TvShowEpisodeNode, we have to check the parent TV show and the episode
    if (node instanceof TvShowEpisodeTreeNode) {
      bean = (TvShowEpisode) ((TvShowEpisodeTreeNode) node).getUserObject();
    }

    if (bean == null) {
      return true;
    }

    return matcher.matches(bean);
  }

  @Override
  public Object getRoot() {
    return root;
  }

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

  @Override
  public void removeTreeModelListener(TreeModelListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void valueForPathChanged(TreePath arg0, Object arg1) {
  }

  private int getChildCountInternal(Object node) {
    return ((TreeNode) node).getChildCount();
  }

  private Object getChildInternal(Object parent, int index) {
    return ((TreeNode) parent).getChildAt(index);
  }

  public void setFilter(SearchOptions option, Object filterArg) {
    if (matcher.searchOptions.containsKey(option)) {
      matcher.searchOptions.remove(option);
    }
    matcher.searchOptions.put(option, filterArg);
  }

  public void removeFilter(SearchOptions option) {
    if (matcher.searchOptions.containsKey(option)) {
      matcher.searchOptions.remove(option);
    }
  }

  public void filter(JTree tree) {
    TreePath selection = tree.getSelectionPath();
    List<TreePath> currOpen = getCurrExpandedPaths(tree);
    reload();
    reExpandPaths(tree, currOpen);
    restoreSelection(selection, tree);
  }

  private List<TreePath> getCurrExpandedPaths(JTree tree) {
    List<TreePath> paths = new ArrayList<TreePath>();
    Enumeration<TreePath> expandEnum = tree.getExpandedDescendants(new TreePath(root.getPath()));
    if (expandEnum == null) {
      return null;
    }

    while (expandEnum.hasMoreElements()) {
      paths.add(expandEnum.nextElement());
    }

    return paths;
  }

  private void reExpandPaths(JTree tree, List<TreePath> expPaths) {
    if (expPaths == null) {
      return;
    }
    for (TreePath tp : expPaths) {
      tree.expandPath(tp);
    }
  }

  private void reload() {
    TreeModelEvent event = new TreeModelEvent(this, root.getPath(), null, null);
    for (TreeModelListener listener : listeners) {
      listener.treeStructureChanged(event);
    }
  }

  private void restoreSelection(TreePath path, JTree tree) {
    if (path != null) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) path.getLastPathComponent();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
      if (getIndexOfChild(parent, child) > -1) {
        tree.setSelectionPath(path);
        return;
      }
    }

    // search first valid node to select
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
    for (int i = 0; i < root.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
      if (getIndexOfChild(root, child) > -1) {
        tree.setSelectionPath(new TreePath(child.getPath()));
        break;
      }
    }
  }
}
