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
package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.RegisterDonatorVersionDialog;

/**
 * The class RegisterDonatorVersionAction. Used to register to the donator version
 * 
 * @author Manuel Laggner
 */
public class RegisterDonatorVersionAction extends AbstractAction {
  private static final long           serialVersionUID = -5959115195785207508L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public RegisterDonatorVersionAction() {
    putValue(NAME, BUNDLE.getString("tmm.registerdonator")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    RegisterDonatorVersionDialog dialog = new RegisterDonatorVersionDialog();
    dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
    dialog.pack();
    dialog.setVisible(true);
  }
}
