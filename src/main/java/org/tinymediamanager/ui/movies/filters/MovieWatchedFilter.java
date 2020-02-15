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
package org.tinymediamanager.ui.movies.filters;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a watched movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieWatchedFilter extends AbstractMovieUIFilter {
  private enum WatchedFlag {
    WATCHED(BUNDLE.getString("metatag.watched")),
    NOT_WATCHED(BUNDLE.getString("metatag.notwatched"));

    private String title;

    WatchedFlag(String title) {
      this.title = title;
    }

    @Override
    public String toString() {
      return title;
    }
  }

  private JComboBox<WatchedFlag> combobox;

  @Override
  public String getId() {
    return "movieWatched";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return ((WatchedFlag) combobox.getSelectedItem()).name();
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof WatchedFlag) {
      combobox.setSelectedItem(value);
    }
    else if (value instanceof String) {
      WatchedFlag watchedFlag = WatchedFlag.valueOf((String) value);
      if (watchedFlag != null) {
        combobox.setSelectedItem(watchedFlag);
      }
    }
  }

  @Override
  public boolean accept(Movie movie) {
    return !(movie.isWatched() ^ combobox.getSelectedItem() == WatchedFlag.WATCHED);
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.watched"));
  }

  @Override
  protected JComponent createFilterComponent() {
    combobox = new JComboBox<>(WatchedFlag.values());
    return combobox;
  }
}
