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

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowSeason;

/**
 * The Class TvShowSeasonTreeNode.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeasonTreeNode extends DefaultMutableTreeNode {

  /** The Constant serialVersionUID. */
  private static final long    serialVersionUID = -5734830011018805194L;

  /** The node comparator. */
  private Comparator<TreeNode> nodeComparator;

  public TvShowSeasonTreeNode(Object userObject) {
    super(userObject);
    nodeComparator = new Comparator<TreeNode>() {
      @Override
      public int compare(TreeNode o1, TreeNode o2) {
        if (o1 instanceof TvShowEpisodeTreeNode && o2 instanceof TvShowEpisodeTreeNode) {
          TvShowEpisodeTreeNode node1 = (TvShowEpisodeTreeNode) o1;
          TvShowEpisode tvShowEpisode1 = (TvShowEpisode) node1.getUserObject();
          TvShowEpisodeTreeNode node2 = (TvShowEpisodeTreeNode) o2;
          TvShowEpisode tvShowEpisode2 = (TvShowEpisode) node2.getUserObject();
          return tvShowEpisode1.getEpisode() - tvShowEpisode2.getEpisode();
        }
        return o1.toString().compareToIgnoreCase(o2.toString());
      }
    };
  }

  /**
   * provides the right name of the node for display
   */
  @Override
  public String toString() {
    // return movieSet name
    if (getUserObject() instanceof TvShowSeason) {
      TvShowSeason season = (TvShowSeason) getUserObject();
      if (season.getSeason() == -1) {
        return "Uncategorized";
      }

      return "Season " + season.getSeason();
    }

    // fallback: call super
    return super.toString();
  }

  // /**
  // * Sort.
  // */
  // @SuppressWarnings("unchecked")
  // public void sort() {
  // if (this.children != null) {
  // Collections.sort(this.children, nodeComparator);
  // }
  // }

  @SuppressWarnings("unchecked")
  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    if (this.children != null) {
      int index = Collections.binarySearch(this.children, newChild, nodeComparator);
      if (index < 0) {
        super.insert(newChild, -index - 1);
      }
    }
    else {
      super.insert(newChild, childIndex);
    }
  }
}
