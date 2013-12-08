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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.tvshow.TvShow;

/**
 * The Class TvShowSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSelectionModel extends AbstractModelObject {
  private static final String    SELECTED_TV_SHOW = "selectedTvShow";

  private TvShow                 selectedTvShow;
  private TvShow                 initalTvShow     = new TvShow();

  /** The property change listener. */
  private PropertyChangeListener propertyChangeListener;

  /**
   * Instantiates a new tv show selection model. Usage in TvShowPanel
   */
  public TvShowSelectionModel() {
    selectedTvShow = initalTvShow;

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
}
