/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.tinymediamanager.ui.ButtonBarButtonUI;
import org.tinymediamanager.ui.ButtonBarUI;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The Class SettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class SettingsPanel extends JPanel {
  private static final long           serialVersionUID = -3509434882626534578L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /**
   * UI components
   */
  private JPanel                      buttonBar;
  private ButtonGroup                 buttonGroup;
  private Component                   currentComponent;
  private JPanel                      panelTmmSettings;
  private JPanel                      panelMovieSettings;
  private JPanel                      panelTvShowSettings;

  /**
   * Create the panel.
   */
  public SettingsPanel() {
    setLayout(new BorderLayout());

    buttonBar = new JPanel();
    buttonBar.setUI(new ButtonBarUI());
    EqualsLayout layout = new EqualsLayout(EqualsLayout.LEFT, 0);
    buttonBar.setLayout(layout);
    add("North", buttonBar);
    buttonGroup = new ButtonGroup();

    /*
     * General settings
     */
    panelTmmSettings = new TmmSettingsContainerPanel();
    addButton(
        BUNDLE.getString("Settings.general"), new ImageIcon(SettingsPanel.class.getResource("/org/tinymediamanager/ui/images/Action-configure-icon.png")), panelTmmSettings); //$NON-NLS-1$

    /*
     * Movie settings
     */
    panelMovieSettings = MovieUIModule.getInstance().getSettingsPanel();
    addButton(
        BUNDLE.getString("Settings.movies"), new ImageIcon(SettingsPanel.class.getResource("/org/tinymediamanager/ui/images/show_reel.png")), panelMovieSettings); //$NON-NLS-1$

    /*
     * TV show settings
     */
    panelTvShowSettings = TvShowUIModule.getInstance().getSettingsPanel();
    addButton(
        BUNDLE.getString("Settings.tvshow"), new ImageIcon(SettingsPanel.class.getResource("/org/tinymediamanager/ui/images/tv_show.png")), panelTvShowSettings); //$NON-NLS-1$

  }

  private void addButton(String title, ImageIcon icon, final Component component) {
    Action action = new AbstractAction(title, icon) {
      private static final long serialVersionUID = -5307503386163952433L;

      @Override
      public void actionPerformed(ActionEvent e) {
        show(component);
      }
    };

    JToggleButton button = new JToggleButton(action);
    button.setHorizontalTextPosition(JButton.CENTER);
    button.setVerticalTextPosition(JButton.BOTTOM);
    button.setUI(new ButtonBarButtonUI());
    buttonBar.add(button);
    buttonGroup.add(button);

    if (buttonGroup.getSelection() == null) {
      button.setSelected(true);
      show(component);
    }
  }

  private void show(Component component) {
    if (currentComponent != null) {
      remove(currentComponent);
    }
    add("Center", currentComponent = component);
    revalidate();
    repaint();
  }
}
