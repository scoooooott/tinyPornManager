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
package org.tinymediamanager.ui.movies.panels;

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
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.JHintCheckBox;

/**
 * The Class MovieScraperMetadataPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieScraperMetadataPanel extends JPanel {
  private static final long                          serialVersionUID = 1053348917399322570L;
  private static final int                           COL_COUNT        = 6;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle                BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private MovieSettings                              settings         = MovieModuleManager.SETTINGS;
  private Map<MovieScraperMetadataConfig, JCheckBox> checkboxes;

  public MovieScraperMetadataPanel() {
    this.checkboxes = new LinkedHashMap<>();
    ItemListener checkBoxListener = e -> checkChanges();
    initComponents();

    for (MovieScraperMetadataConfig value : settings.getScraperMetadataConfig()) {
      JCheckBox checkBox = checkboxes.get(value);
      if (checkBox != null) {
        checkBox.setSelected(true);
      }
    }

    // set the checkbox listener at the end!
    for (JCheckBox checkBox : checkboxes.values()) {
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

    // Metadata
    for (MovieScraperMetadataConfig value : MovieScraperMetadataConfig.values()) {
      if (value.isMetaData()) {
        addCheckbox(value, gbc);
      }
    }

    // cast
    gbc.gridx = 0;
    gbc.gridy++;
    for (MovieScraperMetadataConfig value : MovieScraperMetadataConfig.values()) {
      if (value.isCast()) {
        addCheckbox(value, gbc);
      }
    }

    // artwork
    gbc.gridx = 0;
    gbc.gridy++;
    for (MovieScraperMetadataConfig value : MovieScraperMetadataConfig.values()) {
      if (value.isArtwork()) {
        addCheckbox(value, gbc);
      }
    }

    // add/remove all buttons
    JPanel panel = new JPanel();
    JButton btnSelectAll = new JButton(IconManager.CHECK_ALL);
    panel.add(btnSelectAll);
    btnSelectAll.setToolTipText(BUNDLE.getString("Button.select.all"));
    btnSelectAll.addActionListener(e -> setCheckBoxState(true));

    JButton btnDeSelectAll = new JButton(IconManager.CLEAR_ALL);
    panel.add(btnDeSelectAll);
    btnDeSelectAll.setToolTipText(BUNDLE.getString("Button.select.none"));
    btnDeSelectAll.addActionListener(e -> setCheckBoxState(false));

    gbc.gridx = 0;
    gbc.gridy++;
    add(panel, gbc);
  }

  private void addCheckbox(MovieScraperMetadataConfig config, GridBagConstraints gbc) {
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

  private void setCheckBoxState(boolean state) {
    for (JCheckBox checkBox : checkboxes.values()) {
      checkBox.setSelected(state);
    }
  }

  private void checkChanges() {
    List<MovieScraperMetadataConfig> config = new ArrayList<>();

    for (Map.Entry<MovieScraperMetadataConfig, JCheckBox> entry : checkboxes.entrySet()) {
      MovieScraperMetadataConfig key = entry.getKey();
      JCheckBox value = entry.getValue();
      if (value.isSelected() && !config.contains(key)) {
        config.add(key);
      }
    }

    settings.setScraperMetadataConfig(config);
  }
}
