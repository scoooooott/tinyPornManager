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
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;

/**
 * The Class TvShowSeasonSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeasonSelectionModel extends AbstractModelObject {
  private static final String    SELECTED_TV_SHOW_SEASON = "selectedTvShowSeason";

  private TvShowSeason           selectedTvShowSeason;
  private PropertyChangeListener propertyChangeListener;

  /**
   * Instantiates a new tv show season selection model. Usage in TvShowPanel
   */
  public TvShowSeasonSelectionModel() {
    propertyChangeListener = evt -> {
      if (evt.getSource() == selectedTvShowSeason) {
        // wrap this event in a new event for listeners of the selection model
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
      }
    };
  }

  /**
   * Sets the selected tv show season.
   * 
   * @param tvShowSeason
   *          the new selected tv show season
   */
  public void setSelectedTvShowSeason(TvShowSeason tvShowSeason) {
    TvShowSeason oldValue = this.selectedTvShowSeason;

    if (tvShowSeason != null) {
      this.selectedTvShowSeason = tvShowSeason;
    }
    else {
      this.selectedTvShowSeason = null;
    }

    if (oldValue != null) {
      oldValue.removePropertyChangeListener(propertyChangeListener);
    }

    if (this.selectedTvShowSeason != null) {
      this.selectedTvShowSeason.addPropertyChangeListener(propertyChangeListener);
    }

    firePropertyChange(SELECTED_TV_SHOW_SEASON, oldValue, this.selectedTvShowSeason);
  }

  /**
   * Gets the selected tv show season.
   * 
   * @return the selected tv show season
   */
  public TvShowSeason getSelectedTvShowSeason() {
    return selectedTvShowSeason;
  }
}
