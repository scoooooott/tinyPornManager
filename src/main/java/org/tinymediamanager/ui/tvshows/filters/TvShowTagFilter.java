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
package org.tinymediamanager.ui.tvshows.filters;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a tag filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowTagFilter extends AbstractTvShowUIFilter {
  private TvShowList               tvShowList = TvShowList.getInstance();

  private TmmCheckComboBox<String> checkComboBox;

  public TvShowTagFilter() {
    super();
    buildAndInstallTagsArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallTagsArray();
    tvShowList.addPropertyChangeListener(Constants.TAG, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "tvShowTag";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return objectMapper.writeValueAsString(checkComboBox.getSelectedItems());
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    try {
      List<String> selectedItems = objectMapper.readValue((String) value,
          objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
      checkComboBox.setSelectedItems(selectedItems);
    }
    catch (Exception ignored) {
    }
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    List<String> tags = checkComboBox.getSelectedItems();
    if (tvShow.getTags().containsAll(tags)) {
      return true;
    }

    for (TvShowEpisode episode : episodes) {
      if (episode.getTags().containsAll(tags)) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    checkComboBox = new TmmCheckComboBox<>();
    return checkComboBox;
  }

  private void buildAndInstallTagsArray() {
    List<String> selectedItems = checkComboBox.getSelectedItems();

    List<String> tags = new ArrayList<>(tvShowList.getTagsInTvShows());
    tags.addAll(tvShowList.getTagsInEpisodes());
    Collections.sort(tags);

    checkComboBox.setItems(tags);

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }
  }
}
