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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;

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
  private JTree                  tree;

  /**
   * Instantiates a new tv show selection model. Usage in TvShowPanel
   */
  public TvShowSelectionModel(JTree tree) {
    selectedTvShow = initalTvShow;
    this.tree = tree;

    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
      }
    };
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
    List<TvShow> selectedTvShows = new ArrayList<TvShow>();

    TreePath[] paths = tree.getSelectionPaths();

    // filter out all tv shows from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof TvShow) {
            TvShow tvShow = (TvShow) node.getUserObject();
            selectedTvShows.add(tvShow);
          }
        }
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
    List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>();

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
   * Get all selected objects from the tree
   * 
   * @return the selected objects
   */
  public List<Object> getSelectedObjects() {
    List<Object> selectedObjects = new ArrayList<Object>();

    TreePath[] paths = tree.getSelectionPaths();

    // filter out all objects from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          selectedObjects.add(node.getUserObject());
        }
      }
    }

    return selectedObjects;
  }
}
