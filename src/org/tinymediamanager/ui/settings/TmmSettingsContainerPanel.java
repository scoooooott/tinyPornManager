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
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.tinymediamanager.ui.UTF8Control;

/**
 * The class GeneralSettingsContainerPanel. For holding all general settings panels
 * 
 * @author Manuel Laggner
 */
public class TmmSettingsContainerPanel extends JPanel {
  private static final long           serialVersionUID = 2489431064154526834L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TmmSettingsContainerPanel() {
    setLayout(new BorderLayout(0, 0));
    {
      JTabbedPane tabbedPanePages = new JTabbedPane(JTabbedPane.TOP);
      add(tabbedPanePages, BorderLayout.CENTER);
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new GeneralSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.general"), null, scrollPane, null); //$NON-NLS-1$
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new FileTypesSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.filetypes"), null, scrollPane, null); //$NON-NLS-1$
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(new ExternalDevicesSettingsPanel());
        tabbedPanePages.addTab(BUNDLE.getString("Settings.externaldevices"), null, scrollPane, null); //$NON-NLS-1$
      }
    }
  }
}
