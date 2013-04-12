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

import javax.swing.tree.DefaultMutableTreeNode;

import org.tinymediamanager.core.tvshow.TvShowEpisode;

/**
 * The Class TvShowEpisodeTreeNode.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeTreeNode extends DefaultMutableTreeNode {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -7108614568808831980L;

  /**
   * Instantiates a new tv show episode tree node.
   * 
   * @param userObject
   *          the user object
   */
  public TvShowEpisodeTreeNode(Object userObject) {
    super(userObject);
  }

  /**
   * provides the right name of the node for display.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    // return episode name and number
    if (getUserObject() instanceof TvShowEpisode) {
      TvShowEpisode episode = (TvShowEpisode) getUserObject();
      if (episode.getEpisode() >= 0) {
        return episode.getEpisode() + ". " + episode.getTitle();
      }
      else {
        return episode.getTitle();
      }
    }

    // fallback: call super
    return super.toString();
  }

}
