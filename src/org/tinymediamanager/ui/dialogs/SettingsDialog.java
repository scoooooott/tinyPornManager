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
package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.settings.SettingsPanel;

/**
 * The class SettingsDialog. For displaying all settings in a dialog
 * 
 * @author Manuel Laggner
 */
public class SettingsDialog extends JDialog {
  private static final long           serialVersionUID = 2435834806519338339L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static JDialog              instance;

  /**
   * Get the single instance of the settings dialog
   * 
   * @return the settings dialog
   */
  public static JDialog getInstance() {
    if (instance == null) {
      instance = new SettingsDialog();
    }
    return instance;
  }

  private SettingsDialog() {
    super(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.settings"), true); //$NON-NLS-1$
    setIconImage(MainWindow.LOGO);
    Rectangle bounds = MainWindow.getActiveInstance().getBounds();
    setBounds(bounds.x + (bounds.width / 20), bounds.y + (bounds.height / 20), (int) (bounds.width * 0.90), (int) (bounds.height * 0.90));

    {
      JPanel panelSettings = new SettingsPanel();
      getContentPane().add(panelSettings, BorderLayout.CENTER);
    }
    {
      JPanel panelButtons = new JPanel();
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      panelButtons.setLayout(layout);
      panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
      getContentPane().add(panelButtons, BorderLayout.SOUTH);

      JButton okButton = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      panelButtons.add(okButton, "2, 1, fill, top");
      okButton.setAction(new CloseAction());
      getRootPane().setDefaultButton(okButton);
    }
  }

  private class CloseAction extends AbstractAction {
    private static final long serialVersionUID = 2386371884117941373L;

    public CloseAction() {
      putValue(NAME, BUNDLE.getString("Button.close")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY);
      putValue(LARGE_ICON_KEY, IconManager.APPLY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Globals.settings.saveSettings();
      setVisible(false);
    }
  }
}
