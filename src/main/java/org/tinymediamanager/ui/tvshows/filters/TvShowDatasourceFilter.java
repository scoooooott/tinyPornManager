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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a data source filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowDatasourceFilter extends AbstractTvShowUIFilter {
  private TvShowSettings           tvShowSettings = TvShowModuleManager.SETTINGS;

  private TmmCheckComboBox<String> checkComboBox;

  public TvShowDatasourceFilter() {
    super();
    buildAndInstallDatasourceArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallDatasourceArray();
    tvShowSettings.addPropertyChangeListener(Constants.DATA_SOURCE, propertyChangeListener);
    tvShowSettings.addPropertyChangeListener("tvShowDataSource", propertyChangeListener);
  }

  @Override
  public String getId() {
    return "tvShowDatasource";
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
    List<String> dataSources = checkComboBox.getSelectedItems();
    return dataSources.contains(tvShow.getDataSource());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    checkComboBox = new TmmCheckComboBox<>();
    return checkComboBox;
  }

  private void buildAndInstallDatasourceArray() {
    // remove the listener to not firing unnecessary events
    checkComboBox.removeActionListener(actionListener);

    List<String> selectedItems = checkComboBox.getSelectedItems();

    List<String> datasources = new ArrayList<>(tvShowSettings.getTvShowDataSource());
    Collections.sort(datasources);

    checkComboBox.setItems(datasources);

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }

    // re-add the itemlistener
    checkComboBox.addActionListener(actionListener);
  }
}
