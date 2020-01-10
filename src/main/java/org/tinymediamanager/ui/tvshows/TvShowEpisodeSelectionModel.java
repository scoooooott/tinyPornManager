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
package org.tinymediamanager.ui.tvshows;

import java.beans.PropertyChangeListener;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

/**
 * The Class TvShowEpisodeSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeSelectionModel extends AbstractModelObject {
  private static final String    SELECTED_TV_SHOW_EPISODE = "selectedTvShowEpisode";

  private TvShowEpisode          selectedTvShowEpisode;
  private TvShowEpisode          initalTvShowEpisode      = new TvShowEpisode();
  private PropertyChangeListener propertyChangeListener;

  /**
   * Instantiates a new tv show episode selection model. Usage in TvShowPanel
   */
  public TvShowEpisodeSelectionModel() {
    selectedTvShowEpisode = initalTvShowEpisode;

    propertyChangeListener = evt -> {
      if (evt.getSource() == selectedTvShowEpisode) {
        // wrap this event in a new event for listeners of the selection model
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
      }
    };
  }

  /**
   * Sets the selected tv show episode.
   * 
   * @param tvShowEpisode
   *          the new selected tv show episode
   */
  public void setSelectedTvShowEpisode(TvShowEpisode tvShowEpisode) {
    TvShowEpisode oldValue = this.selectedTvShowEpisode;

    if (tvShowEpisode != null) {
      this.selectedTvShowEpisode = tvShowEpisode;
    }
    else {
      this.selectedTvShowEpisode = initalTvShowEpisode;
    }

    if (oldValue != null) {
      oldValue.removePropertyChangeListener(propertyChangeListener);
    }

    if (selectedTvShowEpisode != null) {
      selectedTvShowEpisode.addPropertyChangeListener(propertyChangeListener);
    }

    firePropertyChange(SELECTED_TV_SHOW_EPISODE, oldValue, this.selectedTvShowEpisode);
  }

  /**
   * Gets the selected tv show episode.
   * 
   * @return the selected tv show episode
   */
  public TvShowEpisode getSelectedTvShowEpisode() {
    return selectedTvShowEpisode;
  }
}
