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
package org.tinymediamanager.ui.movies.settings;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.tinymediamanager.ui.UTF8Control;

/**
 * The class MovieSettingsContainerPanel. Holding all settings panes for the movie section
 * 
 * @author Manuel Laggner
 */
public class MovieSettingsContainerPanel extends JPanel {
  private static final long           serialVersionUID = -1191910643362891059L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieSettingsContainerPanel() {
    setLayout(new BorderLayout(0, 0));
    {
      JTabbedPane tabbedPanePages = new JTabbedPane(JTabbedPane.TOP);
      add(tabbedPanePages, BorderLayout.CENTER);
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new MovieSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.general"), null, scrollPane, null); //$NON-NLS-1$
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new MovieScraperSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.scraper"), null, scrollPane, null); //$NON-NLS-1$
      }

      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new MovieImageSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.images"), null, scrollPane, null); //$NON-NLS-1$
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new MovieRenamerSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.renamer"), null, scrollPane, null); //$NON-NLS-1$
      }
    }
  }
}
