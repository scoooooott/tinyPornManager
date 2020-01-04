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

package org.tinymediamanager.ui.components;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * the class {@link SettingsPanelFactory} is a factory to create a bare bone settings panel with the right layout
 */
public class SettingsPanelFactory {

  /**
   * create a {@link JPanel} with the right layout for the settings dialog
   * 
   * @return the created {@link JPanel}
   */
  public static JPanel createSettingsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new MigLayout("hidemode 1, insets 0", "[20lp!][16lp][grow]", "")); // 16lp ~ width of the checkbox
    return panel;
  }
}
