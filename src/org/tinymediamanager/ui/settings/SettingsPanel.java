/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.tinymediamanager.core.Settings;

import com.l2fprod.common.swing.JButtonBar;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonBarUI;

/**
 * The Class SettingsPanel.
 */
public class SettingsPanel extends JPanel {
  private static final long         serialVersionUID = 1L;

  /** The settings. */
  private Settings                  settings         = Settings.getInstance();

  private Component                 currentComponent;
  private JButtonBar                toolbar;

  private GeneralSettingsPanel      panelGeneralSettings;
  private MovieSettingsPanel        panelMovieSettings;
  private MovieScraperSettingsPanel panelScraperMovieSettings;
  private MovieImageSettingsPanel   panelImageMovieSettings;

  /**
   * Create the panel.
   */
  public SettingsPanel() {
    toolbar = new JButtonBar(JButtonBar.VERTICAL);
    // toolbar.setUI(new IconPackagerButtonBarUI());
    toolbar.setUI(new BlueishButtonBarUI());
    setLayout(new BorderLayout());

    add("West", toolbar);

    ButtonGroup group = new ButtonGroup();
    panelGeneralSettings = new GeneralSettingsPanel();
    addButton("General", "/org/tinymediamanager/ui/images/Action-configure-icon.png", panelGeneralSettings, toolbar, group);
    panelMovieSettings = new MovieSettingsPanel();
    addButton("Movies", "/org/tinymediamanager/ui/images/show_reel.png", panelMovieSettings, toolbar, group);
    panelScraperMovieSettings = new MovieScraperSettingsPanel();
    addButton("<html>Movies -<br>Scraper</html>", "/org/tinymediamanager/ui/images/show_reel.png", panelScraperMovieSettings, toolbar, group);
    panelImageMovieSettings = new MovieImageSettingsPanel();
    addButton("<html>Movies -<br>Images</html>", "/org/tinymediamanager/ui/images/show_reel.png", panelImageMovieSettings, toolbar, group);

    // // button panel
    // JPanel buttonPanel = new JPanel();
    // // add(buttonPanel, "2, 4, fill, fill");
    // add("South", buttonPanel);
    // buttonPanel.setLayout(new FormLayout(new ColumnSpec[] {
    // FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
    // FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), }, new
    // RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));
    //
    // JButton btnSaveSettings = new JButton("Save");
    // buttonPanel.add(btnSaveSettings, "4, 1");
    // btnSaveSettings.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent arg0) {
    //
    // // save settings
    // settings.saveSettings();
    // }
    // });

    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
        settings.saveSettings();
      }
    });
  }

  private void show(Component component) {
    if (currentComponent != null) {
      remove(currentComponent);
    }
    add("Center", currentComponent = component);
    revalidate();
    repaint();
  }

  private void addButton(String title, String iconUrl, final Component component, JButtonBar bar, ButtonGroup group) {
    Action action = new AbstractAction(title, new ImageIcon(SettingsPanel.class.getResource(iconUrl))) {
      public void actionPerformed(ActionEvent e) {
        show(component);
      }
    };

    JToggleButton button = new JToggleButton(action);
    bar.add(button);

    group.add(button);

    if (group.getSelection() == null) {
      button.setSelected(true);
      show(component);
    }
  }
}
