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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a data source filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowDatasourceFilter extends AbstractTvShowUIFilter {
  private TvShowSettings    tvShowSettings = Settings.getInstance().getTvShowSettings();

  private JComboBox<String> comboBox;

  public TvShowDatasourceFilter() {
    super();
    buildAndInstallDatasourceArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallDatasourceArray();
    tvShowSettings.addPropertyChangeListener(Constants.DATA_SOURCE, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "tvShowDatasource";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return (String) comboBox.getSelectedItem();
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    comboBox.setSelectedItem(value);
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    String datasource = (String) comboBox.getSelectedItem();
    if (new File(tvShow.getDataSource()).equals(new File(datasource))) {
      return true;
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>();
    return comboBox;
  }

  private void buildAndInstallDatasourceArray() {
    String oldValue = (String) comboBox.getSelectedItem();
    comboBox.removeAllItems();

    List<String> datasources = new ArrayList<>(tvShowSettings.getTvShowDataSource());
    Collections.sort(datasources);
    for (String datasource : datasources) {
      comboBox.addItem(datasource);
    }

    if (oldValue != null) {
      comboBox.setSelectedItem(oldValue);
    }
  }
}
