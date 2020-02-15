/*
 * Copyright 2012 - 2020 Manuel Laggner
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;

/**
 * The class ClearDatabaseAction. Used to clear the whole database (to start with a new one)
 * 
 * @author Manuel Laggner
 */
public class ClearDatabaseAction extends TmmAction {
  private static final long           serialVersionUID = 5840749350843921771L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER           = LoggerFactory.getLogger(ClearDatabaseAction.class);

  public ClearDatabaseAction() {
    putValue(NAME, BUNDLE.getString("tmm.cleardatabase"));
  }

  @Override
  protected void processAction(ActionEvent arg0) {
    // display warning popup
    Object[] options = { BUNDLE.getString("Button.yes"), BUNDLE.getString("Button.no") };
    int answer = JOptionPane.showOptionDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.cleardatabase.hint"),
        BUNDLE.getString("tmm.cleardatabase"), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, null);
    if (answer != JOptionPane.YES_OPTION) {
      return;
    }

    MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    // delete the database
    try {
      TmmModuleManager.getInstance().shutDown();
      TmmModuleManager.getInstance().initializeDatabase();
      MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.info"));
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.error"));
      // open the tmm folder
      Path path = Paths.get("");
      try {
        // check whether this location exists
        if (Files.exists(path)) {
          TmmUIHelper.openFile(path);
        }
      }
      catch (Exception ex) {
        LOGGER.warn(ex.getMessage());
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
      }
    }
    MainWindow.getActiveInstance().closeTmmAndStart(Utils.getPBforTMMrestart());
  }
}
