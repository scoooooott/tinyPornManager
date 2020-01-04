/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.AbstractTmmUIFilter;
import org.tinymediamanager.ui.components.tree.ITmmTreeFilter;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * An abstract implementation for easier usage of the ITmmUIFilter and ITvShowUIFilter
 * 
 * @author Manuel Laggner
 */
abstract class AbstractTvShowUIFilter extends AbstractTmmUIFilter<TmmTreeNode> implements ITvShowUIFilter<TmmTreeNode> {
  @Override
  public boolean isActive() {
    switch (getFilterState()) {
      case ACTIVE:
      case ACTIVE_NEGATIVE:
        return true;

      default:
        return false;
    }
  }

  @Override
  public boolean accept(TmmTreeNode node) {
    // is this filter active?
    if (getFilterState() == FilterState.INACTIVE) {
      return true;
    }

    Object userObject = node.getUserObject();

    if (userObject instanceof TvShow) {
      TvShow tvShow = (TvShow) userObject;
      if (getFilterState() == FilterState.ACTIVE) {
        return accept(tvShow, new ArrayList<>(tvShow.getEpisodesForDisplay()));
      }
      else if (getFilterState() == FilterState.ACTIVE_NEGATIVE) {
        return accept(tvShow, new ArrayList<>(tvShow.getEpisodesForDisplay()), true);
      }
    }
    else if (userObject instanceof TvShowSeason) {
      TvShowSeason season = (TvShowSeason) userObject;
      if (getFilterState() == FilterState.ACTIVE) {
        return accept(season.getTvShow(), new ArrayList<>(season.getEpisodesForDisplay()));
      }
      else if (getFilterState() == FilterState.ACTIVE_NEGATIVE) {
        return accept(season.getTvShow(), new ArrayList<>(season.getEpisodesForDisplay()), true);
      }
    }
    else if (userObject instanceof TvShowEpisode) {
      TvShowEpisode episode = (TvShowEpisode) userObject;
      if (getFilterState() == FilterState.ACTIVE) {
        return accept(episode.getTvShow(), Collections.singletonList(episode));
      }
      else if (getFilterState() == FilterState.ACTIVE_NEGATIVE) {
        return accept(episode.getTvShow(), Collections.singletonList(episode), true);
      }
    }

    return true;
  }

  /**
   * utility method for easier calling
   */
  private boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    return accept(tvShow, episodes, false);
  }

  /**
   * should we accept the node providing this data?
   * 
   * @param tvShow
   *          the tvShow of this node
   * @param episodes
   *          all episodes of this node
   * @param invert
   *          invert the accept logic for negative searches
   * @return whether we should or not accept it.
   */
  protected abstract boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert);

  /**
   * delegate the filter changed event to the tree
   */
  @Override
  protected void filterChanged() {
    SwingUtilities.invokeLater(() -> firePropertyChange(ITmmTreeFilter.TREE_FILTER_CHANGED, checkBox.isSelected(), !checkBox.isSelected()));
  }
}
