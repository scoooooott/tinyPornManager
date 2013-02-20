/*
 * Copyright 2013 Manuel Laggner
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

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieSet;

public class MovieSetTreeNode extends DefaultMutableTreeNode {

  protected static final Comparator nodeComparator = new Comparator() {
                                                     @Override
                                                     public int compare(Object o1, Object o2) {
                                                       if (o1 instanceof MovieTreeNode && o2 instanceof MovieTreeNode) {
                                                         MovieTreeNode node1 = (MovieTreeNode) o1;
                                                         Movie movie1 = (Movie) node1.getUserObject();
                                                         MovieTreeNode node2 = (MovieTreeNode) o2;
                                                         Movie movie2 = (Movie) node2.getUserObject();
                                                         return movie1.getSortTitle().compareTo(movie2.getSortTitle());
                                                       }
                                                       return o1.toString().compareToIgnoreCase(o2.toString());
                                                     }

                                                     @Override
                                                     @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
                                                     public boolean equals(Object obj) {
                                                       return false;
                                                     }

                                                     @Override
                                                     public int hashCode() {
                                                       int hash = 7;
                                                       return hash;
                                                     }
                                                   };

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
      return movieSet.getName();
    }

    // fallback: call super
    return super.toString();
  }

  public void sort() {
    Collections.sort(this.children, nodeComparator);
  }
}
