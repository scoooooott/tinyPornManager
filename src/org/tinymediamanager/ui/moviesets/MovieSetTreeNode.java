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

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;

/**
 * The Class MovieSetTreeNode.
 * 
 * @author Manuel Laggner
 */
public class MovieSetTreeNode extends DefaultMutableTreeNode {

  /** The Constant serialVersionUID. */
  private static final long                   serialVersionUID = 1095499645850717752L;

  /** The Constant nodeComparator. */
  protected static final Comparator<TreeNode> nodeComparator;

  static {
    nodeComparator = new Comparator<TreeNode>() {
      @Override
      public int compare(TreeNode o1, TreeNode o2) {
        if (o1 instanceof MovieTreeNode && o2 instanceof MovieTreeNode) {
          MovieTreeNode node1 = (MovieTreeNode) o1;
          Movie movie1 = (Movie) node1.getUserObject();
          MovieTreeNode node2 = (MovieTreeNode) o2;
          Movie movie2 = (Movie) node2.getUserObject();

          if (movie1 == null || movie2 == null || movie1.getMovieSet() == null || movie2.getMovieSet() == null) {
            return 0;
          }
          int index1 = movie1.getMovieSet().getMovieIndex(movie1);
          int index2 = movie2.getMovieSet().getMovieIndex(movie2);
          return index1 - index2;
        }
        return o1.toString().compareToIgnoreCase(o2.toString());
      }
    };
  }

  /**
   * Instantiates a new movie set tree node.
   * 
   * @param userObject
   *          the user object
   */
  public MovieSetTreeNode(Object userObject) {
    super(userObject);
  }

  /**
   * provides the right name of the node for display
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
   * Sort the nodes
   */
  @SuppressWarnings("unchecked")
  public void sort() {
    if (this.children != null) {
      Collections.sort(this.children, nodeComparator);
    }
  }
}
