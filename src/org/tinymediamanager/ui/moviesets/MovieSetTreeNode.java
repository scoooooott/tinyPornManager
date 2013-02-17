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

import javax.swing.tree.DefaultMutableTreeNode;

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieSet;

public class MovieSetTreeNode extends DefaultMutableTreeNode {

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

    // return movie name
    if (getUserObject() instanceof Movie) {
      Movie movie = (Movie) getUserObject();
      return movie.getNameSortable();
    }

    // fallback: call super
    return super.toString();
  }

}
