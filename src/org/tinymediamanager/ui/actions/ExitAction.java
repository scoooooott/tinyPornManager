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
package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;

public class ExitAction extends AbstractAction {
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final long           serialVersionUID = 6947937872224166477L;

  public ExitAction() {
    putValue(NAME, BUNDLE.getString("tmm.exit")); //$NON-NLS-1$
    // putValue(SHORT_DESCRIPTION, "Some short description");
    putValue(SMALL_ICON, IconManager.EXIT);
    putValue(LARGE_ICON_KEY, IconManager.EXIT);
  }

  public void actionPerformed(ActionEvent e) {
    TmmWindowSaver.getInstance().saveSettings(MainWindow.getActiveInstance());
    MainWindow.getActiveInstance().closeTmm();
  }
}