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
package org.tinymediamanager.ui.tvshows.settings;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.tinymediamanager.ui.UTF8Control;

/**
 * The class TvShowSettingsContainerPanel. For holding all sub panels for TV show settings
 * 
 * @author Manuel Laggner
 */
public class TvShowSettingsContainerPanel extends JPanel {
  private static final long           serialVersionUID = 6923587105310213302L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowSettingsContainerPanel() {
    setLayout(new BorderLayout(0, 0));
    {
      JTabbedPane tabbedPanePages = new JTabbedPane(JTabbedPane.TOP);
      add(tabbedPanePages, BorderLayout.CENTER);
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new TvShowSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.general"), null, scrollPane, null); //$NON-NLS-1$
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new TvShowScraperSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.scraper"), null, scrollPane, null); //$NON-NLS-1$
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new TvShowRenamerSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.renamer"), null, scrollPane, null); //$NON-NLS-1$
      }
    }
  }
}