/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * The Class TvShowTreeNode.
 * 
 * @author Manuel Laggner
 */
@Deprecated
public class TvShowTreeNode extends TmmTreeNode {
  private static final long serialVersionUID = -1316609340104597133L;

  /**
   * Instantiates a new tv show tree node.
   * 
   * @param userObject
   *          the user object
   */
  public TvShowTreeNode(Object userObject) {
    super(userObject);
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
}
