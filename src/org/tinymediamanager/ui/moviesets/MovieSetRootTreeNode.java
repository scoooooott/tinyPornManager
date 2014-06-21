/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.tinymediamanager.core.movie.entities.MovieSet;

/**
 * The Class MovieSetRootTreeNode.
 * 
 * @author Manuel Laggner
 */
public class MovieSetRootTreeNode extends DefaultMutableTreeNode {

  /** The Constant serialVersionUID. */
  private static final long    serialVersionUID = -1209627220507076339L;

  /** The node comparator. */
  private Comparator<TreeNode> nodeComparator;

  /**
   * Instantiates a new movie set tree node.
   * 
   */
  public MovieSetRootTreeNode() {
    super("MovieSets");

    nodeComparator = new Comparator<TreeNode>() {
      @Override
      public int compare(TreeNode o1, TreeNode o2) {
        if (o1 instanceof MovieTreeNode && o2 instanceof MovieTreeNode) {
          MovieSetTreeNode node1 = (MovieSetTreeNode) o1;
          MovieSet movieSet1 = (MovieSet) node1.getUserObject();
          MovieSetTreeNode node2 = (MovieSetTreeNode) o2;
          MovieSet movieSet2 = (MovieSet) node2.getUserObject();
          return movieSet1.getTitleSortable().compareToIgnoreCase(movieSet2.getTitleSortable());
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
    if (getUserObject() instanceof MovieSet) {
      MovieSet movieSet = (MovieSet) getUserObject();
      return movieSet.getTitle();
    }

    // fallback: call super
    return super.toString();
  }

  /**
   * Sort.
   */
  @SuppressWarnings("unchecked")
  public void sort() {
    if (this.children != null) {
      Collections.sort(this.children, nodeComparator);
    }
  }
}
