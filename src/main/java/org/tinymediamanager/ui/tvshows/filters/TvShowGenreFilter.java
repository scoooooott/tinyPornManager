/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a genres filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowGenreFilter extends AbstractTvShowUIFilter {
  private TmmCheckComboBox<MediaGenres> checkComboBox;

  public TvShowGenreFilter() {
    super();
    buildAndInstallMediaGenres();
    MediaGenres.addListener(evt -> SwingUtilities.invokeLater(this::buildAndInstallMediaGenres));
  }

  @Override
  public String getId() {
    return "tvShowGenre";
  }

  @Override
  public String getFilterValueAsString() {
    List<String> values = new ArrayList<>();
    for (MediaGenres genre : checkComboBox.getSelectedItems()) {
      values.add(genre.name());
    }
    try {
      return objectMapper.writeValueAsString(values);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    List<MediaGenres> selectedItems = new ArrayList<>();

    try {
      List<String> values = objectMapper.readValue((String) value, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

      for (String genre : values) {
        MediaGenres mediaGenres = MediaGenres.getGenre(genre);
        selectedItems.add(mediaGenres);
      }

    }
    catch (Exception ignored) {
    }

    checkComboBox.setSelectedItems(selectedItems);
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<MediaGenres> selectedItems = checkComboBox.getSelectedItems();
    return invert ^ tvShow.getGenres().containsAll(selectedItems);
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    checkComboBox = new TmmCheckComboBox<>();
    return checkComboBox;
  }

  private void buildAndInstallMediaGenres() {
    // remove the listener to not firing unnecessary events
    checkComboBox.removeActionListener(actionListener);

    List<MediaGenres> selectedItems = checkComboBox.getSelectedItems();

    checkComboBox.setItems(Arrays.asList(MediaGenres.values()));

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }

    // re-add the itemlistener
    checkComboBox.addActionListener(actionListener);
  }
}
