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

import java.text.RuleBasedCollator;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.tinymediamanager.core.tvshow.entities.TvShow;

/**
 * The Class TvShowRootTreeNode.
 * 
 * @author Manuel Laggner
 */
public class TvShowRootTreeNode extends DefaultMutableTreeNode {
  private static final long    serialVersionUID = 6510900885675830369L;

  private RuleBasedCollator    stringCollator   = (RuleBasedCollator) RuleBasedCollator.getInstance();
  private Comparator<TreeNode> nodeComparator;

  /**
   * Instantiates a new movie set tree node.
   * 
   */
  public TvShowRootTreeNode() {
    super("TvShows");

    nodeComparator = new Comparator<TreeNode>() {
      @Override
      public int compare(TreeNode o1, TreeNode o2) {
        if (o1 instanceof TvShowTreeNode && o2 instanceof TvShowTreeNode) {
          TvShowTreeNode node1 = (TvShowTreeNode) o1;
          TvShow tvShow1 = (TvShow) node1.getUserObject();
          TvShowTreeNode node2 = (TvShowTreeNode) o2;
          TvShow tvShow2 = (TvShow) node2.getUserObject();
          if (stringCollator != null) {
            return stringCollator.compare(tvShow1.getTitleSortable().toLowerCase(), tvShow2.getTitleSortable().toLowerCase());
          }
          return tvShow1.getTitleSortable().compareToIgnoreCase(tvShow2.getTitleSortable());
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
  public synchronized void insert(MutableTreeNode newChild, int childIndex) {
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
