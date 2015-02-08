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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class ClearDatabaseAction. Used to clear the whole database (to start with a new one)
 * 
 * @author Manuel Laggner
 */
public class ClearDatabaseAction extends AbstractAction {
  private static final long           serialVersionUID = 5840749350843921771L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(ClearDatabaseAction.class);

  public ClearDatabaseAction() {
    putValue(NAME, BUNDLE.getString("tmm.cleardatabase")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    // display warning popup
    int answer = JOptionPane.showConfirmDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.cleardatabase.hint"),
        BUNDLE.getString("tmm.cleardatabase"), JOptionPane.YES_NO_OPTION);
    if (answer != JOptionPane.OK_OPTION) {
      return;
    }

    MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    // delete the database
    try {
      TmmModuleManager.getInstance().shutDown();
      File db = new File(Constants.DB);
      if (db.exists()) {
        db.delete();
      }
      MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.info")); //$NON-NLS-1$
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.error")); //$NON-NLS-1$
      // open the tmm folder
      File path = new File(".");
      try {
        // check whether this location exists
        if (path.exists()) {
          TmmUIHelper.openFile(path);
        }
      }
      catch (Exception ex) {
        LOGGER.warn(ex.getMessage());
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":",
            ex.getLocalizedMessage() }));
      }
    }
    MainWindow.getActiveInstance().closeTmmAndStart(Utils.getPBforTMMrestart());
  }
}
