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

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;

/**
 * The Class TvShowTreeNode.
 * 
 * @author Manuel Laggner
 */
public class TvShowTreeNode extends DefaultMutableTreeNode {

  /** The Constant serialVersionUID. */
  private static final long    serialVersionUID = -1316609340104597133L;

  /** The node comparator. */
  private Comparator<TreeNode> nodeComparator;

  /**
   * Instantiates a new tv show tree node.
   * 
   * @param userObject
   *          the user object
   */
  public TvShowTreeNode(Object userObject) {
    super(userObject);

    nodeComparator = new Comparator<TreeNode>() {
      @Override
      public int compare(TreeNode o1, TreeNode o2) {
        if (o1 instanceof TvShowSeasonTreeNode && o2 instanceof TvShowSeasonTreeNode) {
          TvShowSeasonTreeNode node1 = (TvShowSeasonTreeNode) o1;
          TvShowSeason tvShowSeason1 = (TvShowSeason) node1.getUserObject();
          TvShowSeasonTreeNode node2 = (TvShowSeasonTreeNode) o2;
          TvShowSeason tvShowSeason2 = (TvShowSeason) node2.getUserObject();
          return tvShowSeason1.getSeason() - tvShowSeason2.getSeason();
        }
        return o1.toString().compareToIgnoreCase(o2.toString());
      }
    };
  }

  /**
   * provides the right name of the node for display.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    // return movieSet name
    if (getUserObject() instanceof TvShow) {
      TvShow tvShow = (TvShow) getUserObject();
      return tvShow.getTitle();
    }

    // fallback: call super
    return super.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.DefaultMutableTreeNode#insert(javax.swing.tree.MutableTreeNode, int)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    if (this.children != null) {
      int index = Collections.binarySearch(this.children, newChild, nodeComparator);
      if (index < 0) {
        super.insert(newChild, -index - 1);
      }
      else if (index >= 0) {
        super.insert(newChild, index);
      }
    }
    else {
      super.insert(newChild, childIndex);
    }
  }
}
