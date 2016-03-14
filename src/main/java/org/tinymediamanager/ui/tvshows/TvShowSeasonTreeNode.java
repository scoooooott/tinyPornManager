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

import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * The Class TvShowSeasonTreeNode.
 * 
 * @author Manuel Laggner
 */
@Deprecated
public class TvShowSeasonTreeNode extends TmmTreeNode {
  private static final long serialVersionUID = -5734830011018805194L;

  /**
   * Instantiates a new tv show season tree node.
   * 
   * @param userObject
   *          the user object
   */
  public TvShowSeasonTreeNode(Object userObject) {
    super(userObject);
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
}
