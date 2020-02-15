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
package org.tinymediamanager.ui.tvshows.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.TvShowEpisodeScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.JHintCheckBox;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * The Class TvShowScraperMetadataPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowScraperMetadataPanel extends JPanel {
  private static final long                                  serialVersionUID = 2417066912659769559L;
  private static final int                                   COL_COUNT        = 5;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle                        BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private TvShowSettings                                     settings         = TvShowModuleManager.SETTINGS;
  private Map<TvShowScraperMetadataConfig, JCheckBox>        tvShowCheckboxes;
  private Map<TvShowEpisodeScraperMetadataConfig, JCheckBox> episodeCheckboxes;

  public TvShowScraperMetadataPanel() {
    this.tvShowCheckboxes = new LinkedHashMap<>();
    this.episodeCheckboxes = new LinkedHashMap<>();
    ItemListener checkBoxListener = e -> checkChanges();

    initComponents();

    for (TvShowScraperMetadataConfig value : settings.getTvShowScraperMetadataConfig()) {
      JCheckBox checkBox = tvShowCheckboxes.get(value);
      if (checkBox != null) {
        checkBox.setSelected(true);
      }
    }

    for (TvShowEpisodeScraperMetadataConfig value : settings.getEpisodeScraperMetadataConfig()) {
      JCheckBox checkBox = episodeCheckboxes.get(value);
      if (checkBox != null) {
        checkBox.setSelected(true);
      }
    }

    // set the checkbox listener at the end!
    for (JCheckBox checkBox : tvShowCheckboxes.values()) {
      checkBox.addItemListener(checkBoxListener);
    }
    for (JCheckBox checkBox : episodeCheckboxes.values()) {
      checkBox.addItemListener(checkBoxListener);
    }
  }

  private void initComponents() {
    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.ipadx = 10;

    /////////////////////////////////////////
    // TV SHOWS
    /////////////////////////////////////////
    JLabel label = new TmmLabel(BUNDLE.getString("metatag.tvshows"));
    add(label, gbc);

    // Metadata
    gbc.gridx = 0;
    gbc.gridy++;
    for (TvShowScraperMetadataConfig value : TvShowScraperMetadataConfig.values()) {
      if (value.isMetaData()) {
        addCheckbox(value, tvShowCheckboxes, gbc);
      }
    }

    // cast
    gbc.gridx = 0;
    gbc.gridy++;
    for (TvShowScraperMetadataConfig value : TvShowScraperMetadataConfig.values()) {
      if (value.isCast()) {
        addCheckbox(value, tvShowCheckboxes, gbc);
      }
    }

    // artwork
    gbc.gridx = 0;
    gbc.gridy++;
    for (TvShowScraperMetadataConfig value : TvShowScraperMetadataConfig.values()) {
      if (value.isArtwork()) {
        addCheckbox(value, tvShowCheckboxes, gbc);
      }
    }

    // add/remove all buttons
    JPanel panel = new JPanel();
    JButton btnSelectAll = new JButton(IconManager.CHECK_ALL);
    panel.add(btnSelectAll);
    btnSelectAll.setToolTipText(BUNDLE.getString("Button.select.all"));
    btnSelectAll.addActionListener(e -> setTvShowCheckBoxState(true));

    JButton btnDeSelectAll = new JButton(IconManager.CLEAR_ALL);
    panel.add(btnDeSelectAll);
    btnDeSelectAll.setToolTipText(BUNDLE.getString("Button.select.none"));
    btnDeSelectAll.addActionListener(e -> setTvShowCheckBoxState(false));

    gbc.gridx = 0;
    gbc.gridy++;
    add(panel, gbc);

    /////////////////////////////////////////
    // EPISODES
    /////////////////////////////////////////
    gbc.gridx = 0;
    gbc.gridy++;
    label = new TmmLabel(BUNDLE.getString("metatag.episodes"));
    add(label, gbc);

    // Metadata
    gbc.gridx = 0;
    gbc.gridy++;
    for (TvShowEpisodeScraperMetadataConfig value : TvShowEpisodeScraperMetadataConfig.values()) {
      if (value.isMetaData()) {
        addCheckbox(value, episodeCheckboxes, gbc);
      }
    }

    // cast
    gbc.gridx = 0;
    gbc.gridy++;
    for (TvShowEpisodeScraperMetadataConfig value : TvShowEpisodeScraperMetadataConfig.values()) {
      if (value.isCast()) {
        addCheckbox(value, episodeCheckboxes, gbc);
      }
    }

    // artwork
    gbc.gridx = 0;
    gbc.gridy++;
    for (TvShowEpisodeScraperMetadataConfig value : TvShowEpisodeScraperMetadataConfig.values()) {
      if (value.isArtwork()) {
        addCheckbox(value, episodeCheckboxes, gbc);
      }
    }

    // add/remove all buttons
    panel = new JPanel();
    btnSelectAll = new JButton(IconManager.CHECK_ALL);
    panel.add(btnSelectAll);
    btnSelectAll.setToolTipText(BUNDLE.getString("Button.select.all"));
    btnSelectAll.addActionListener(e -> setEpisodeCheckBoxState(true));

    btnDeSelectAll = new JButton(IconManager.CLEAR_ALL);
    panel.add(btnDeSelectAll);
    btnDeSelectAll.setToolTipText(BUNDLE.getString("Button.select.none"));
    btnDeSelectAll.addActionListener(e -> setEpisodeCheckBoxState(false));

    gbc.gridx = 0;
    gbc.gridy++;
    add(panel, gbc);
  }

  private <T extends ScraperMetadataConfig> void addCheckbox(T config, Map<T, JCheckBox> checkboxes, GridBagConstraints gbc) {
    JCheckBox checkBox;
    if (StringUtils.isNotBlank(config.getToolTip())) {
      checkBox = new JHintCheckBox(config.getDescription());
      checkBox.setToolTipText(config.getToolTip());
      ((JHintCheckBox) checkBox).setHintIcon(IconManager.HINT);
    }
    else {
      checkBox = new JCheckBox(config.getDescription());
    }
    checkboxes.put(config, checkBox);

    if (gbc.gridx >= COL_COUNT) {
      gbc.gridx = 0;
      gbc.gridy++;
    }
    add(checkBox, gbc);

    gbc.gridx++;
  }

  private void setTvShowCheckBoxState(boolean state) {
    setCheckBoxState(tvShowCheckboxes, state);
  }

  private void setEpisodeCheckBoxState(boolean state) {
    setCheckBoxState(episodeCheckboxes, state);
  }

  private void setCheckBoxState(Map<? extends ScraperMetadataConfig, JCheckBox> checkboxes, boolean state) {
    for (JCheckBox checkBox : checkboxes.values()) {
      checkBox.setSelected(state);
    }
  }

  private void checkChanges() {
    List<TvShowScraperMetadataConfig> tvShowConfig = new ArrayList<>();

    for (Map.Entry<TvShowScraperMetadataConfig, JCheckBox> entry : tvShowCheckboxes.entrySet()) {
      TvShowScraperMetadataConfig key = entry.getKey();
      JCheckBox value = entry.getValue();
      if (value.isSelected() && !tvShowConfig.contains(key)) {
        tvShowConfig.add(key);
      }
    }

    settings.setTvShowScraperMetadataConfig(tvShowConfig);

    List<TvShowEpisodeScraperMetadataConfig> tvShowEpisodeConfig = new ArrayList<>();

    for (Map.Entry<TvShowEpisodeScraperMetadataConfig, JCheckBox> entry : episodeCheckboxes.entrySet()) {
      TvShowEpisodeScraperMetadataConfig key = entry.getKey();
      JCheckBox value = entry.getValue();
      if (value.isSelected() && !tvShowEpisodeConfig.contains(key)) {
        tvShowEpisodeConfig.add(key);
      }
    }

    settings.setEpisodeScraperMetadataConfig(tvShowEpisodeConfig);
  }
}
