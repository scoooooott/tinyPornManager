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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.components.treetable.TmmTreeTable;

/**
 * The Class TvShowSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSelectionModel extends AbstractModelObject {
  private static final String    SELECTED_TV_SHOW = "selectedTvShow";

  private TvShow                 selectedTvShow;
  private TvShow                 initalTvShow     = new TvShow();
  private PropertyChangeListener propertyChangeListener;
  private TmmTreeTable           treeTable;

  /**
   * Instantiates a new tv show selection model. Usage in TvShowPanel
   */
  public TvShowSelectionModel() {
    selectedTvShow = initalTvShow;
    propertyChangeListener = evt -> firePropertyChange(evt);
  }

  public void setTreeTable(TmmTreeTable treeTable) {
    this.treeTable = treeTable;
  }

  /**
   * Sets the selected tv show.
   * 
   * @param tvShow
   *          the new selected tv show
   */
  public void setSelectedTvShow(TvShow tvShow) {
    TvShow oldValue = this.selectedTvShow;

    if (tvShow != null) {
      this.selectedTvShow = tvShow;
    }
    else {
      this.selectedTvShow = initalTvShow;
    }

    if (oldValue != null) {
      oldValue.removePropertyChangeListener(propertyChangeListener);
    }

    if (selectedTvShow != null) {
      selectedTvShow.addPropertyChangeListener(propertyChangeListener);
    }

    firePropertyChange(SELECTED_TV_SHOW, oldValue, this.selectedTvShow);
  }

  /**
   * Gets the selected tv show.
   * 
   * @return the selected tv show
   */
  public TvShow getSelectedTvShow() {
    return selectedTvShow;
  }

  /**
   * Gets the selected TV shows
   * 
   * @return the selected TV shows
   */
  public List<TvShow> getSelectedTvShows() {
    List<TvShow> selectedTvShows = new ArrayList<>();

    for (Object obj : getSelectedObjects()) {
      if (obj instanceof TvShow) {
        selectedTvShows.add((TvShow) obj);
      }
    }

    return selectedTvShows;
  }

  /**
   * Get the selected episodes
   * 
   * @return the selected episodes
   */
  public List<TvShowEpisode> getSelectedEpisodes() {
    List<TvShowEpisode> episodes = new ArrayList<>();

    for (Object obj : getSelectedObjects()) {
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        if (!episodes.contains(episode)) {
          episodes.add(episode);
        }
      }
      else if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        for (TvShowEpisode episode : season.getEpisodes()) {
          if (!episodes.contains(episode)) {
            episodes.add(episode);
          }
        }
      }
      else if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          if (!episodes.contains(episode)) {
            episodes.add(episode);
          }
        }
      }
    }

    return episodes;
  }

  /**
   * Get all selected objects from the treeTable
   * 
   * @return the selected objects
   */
  public List<Object> getSelectedObjects() {
    List<Object> selectedObjects = new ArrayList<>();

    int rows[] = treeTable.getSelectedRows();
    for (int row : rows) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeTable.getValueAt(row, 0);
      if (node != null) {
        selectedObjects.add(node.getUserObject());
      }
    }
    return selectedObjects;
  }
}
