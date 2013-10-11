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
package org.tinymediamanager.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.UTF8Control;

import com.l2fprod.common.swing.JButtonBar;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonBarUI;

/**
 * The Class SettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class SettingsPanel extends JPanel {
  private static final long            serialVersionUID = -3509434882626534578L;
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                     settings         = Settings.getInstance();

  /**
   * UI components
   */
  private Component                    currentComponent;
  private JButtonBar                   toolbar;
  private GeneralSettingsPanel         panelGeneralSettings;
  private MovieSettingsPanel           panelMovieSettings;
  private MovieScraperSettingsPanel    panelScraperMovieSettings;
  private MovieImageSettingsPanel      panelImageMovieSettings;
  private TvShowSettingsPanel          panelTvShowSettings;
  private TvShowScraperSettingsPanel   panelTvShowScraperSettings;
  private ExternalDevicesSettingsPanel panelExternalDevicesSettings;
  private JScrollPane                  scrollPane;

  /**
   * Create the panel.
   */
  public SettingsPanel() {
    toolbar = new JButtonBar(JButtonBar.VERTICAL);
    toolbar.setUI(new BlueishButtonBarUI());
    setLayout(new BorderLayout());

    add("West", toolbar);

    scrollPane = new JScrollPane();
    add("Center", scrollPane);

    ButtonGroup group = new ButtonGroup();
    panelMovieSettings = new MovieSettingsPanel();
    addButton(BUNDLE.getString("Settings.movies"), "/org/tinymediamanager/ui/images/show_reel.png", panelMovieSettings, toolbar, group); //$NON-NLS-1$
    panelScraperMovieSettings = new MovieScraperSettingsPanel();
    addButton(BUNDLE.getString("Settings.scraper"), "/org/tinymediamanager/ui/images/show_reel.png", panelScraperMovieSettings, toolbar, group); //$NON-NLS-1$
    panelImageMovieSettings = new MovieImageSettingsPanel();
    addButton(BUNDLE.getString("Settings.images"), "/org/tinymediamanager/ui/images/show_reel.png", panelImageMovieSettings, toolbar, group); //$NON-NLS-1$
    panelTvShowSettings = new TvShowSettingsPanel();
    addButton(BUNDLE.getString("Settings.tvshow"), "/org/tinymediamanager/ui/images/tv_show.png", panelTvShowSettings, toolbar, group); //$NON-NLS-1$
    panelTvShowScraperSettings = new TvShowScraperSettingsPanel();
    addButton(BUNDLE.getString("Settings.tvshowscraper"), "/org/tinymediamanager/ui/images/tv_show.png", panelTvShowScraperSettings, toolbar, group); //$NON-NLS-1$
    panelGeneralSettings = new GeneralSettingsPanel();
    addButton(BUNDLE.getString("Settings.general"), "/org/tinymediamanager/ui/images/Action-configure-icon.png", panelGeneralSettings, toolbar, group); //$NON-NLS-1$
    panelExternalDevicesSettings = new ExternalDevicesSettingsPanel();
    addButton(
        BUNDLE.getString("Settings.externaldevices"), "/org/tinymediamanager/ui/images/devices.png", panelExternalDevicesSettings, toolbar, group); //$NON-NLS-1$

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        settings.saveSettings();
      }
    });
  }

  /**
   * Show.
   * 
   * @param component
   *          the component
   */
  private void show(Component component) {
    if (currentComponent != null) {
      scrollPane.remove(currentComponent);
    }
    scrollPane.setViewportView(component);
  }

  /**
   * Adds the button.
   * 
   * @param title
   *          the title
   * @param iconUrl
   *          the icon url
   * @param component
   *          the component
   * @param bar
   *          the bar
   * @param group
   *          the group
   */
  private void addButton(String title, String iconUrl, final Component component, JButtonBar bar, ButtonGroup group) {
    Action action = new AbstractAction(title, new ImageIcon(SettingsPanel.class.getResource(iconUrl))) {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        show(component);
      }
    };

    JToggleButton button = new JToggleButton(action);
    button.setHorizontalTextPosition(JButton.CENTER);
    bar.add(button);

    group.add(button);

    if (group.getSelection() == null) {
      button.setSelected(true);
      show(component);
    }
  }
}
