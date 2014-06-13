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
package org.tinymediamanager;

import java.awt.GraphicsEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ui.dialogs.MessageDialog;

/**
 * The Class Log4jBackstop.
 * 
 * @author Manuel Laggner
 */
class Log4jBackstop implements Thread.UncaughtExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(Log4jBackstop.class);

  public void uncaughtException(Thread t, Throwable ex) {
    LOGGER.error("Uncaught exception in thread: " + t.getName(), ex);
    if (!GraphicsEnvironment.isHeadless()) {
      // TaskDialog dlg = new TaskDialog(MainWindow.getActiveInstance(), "Exception");
      //
      // String msg = ex.getMessage();
      // String className = ex.getClass().getName();
      // boolean noMessage = Strings.isEmpty(msg);
      //
      // dlg.setInstruction("Whoops. Something unforeseen has happened.\nPlease restart TMM and try again.\nIf it happens again, we would kindly ask you to submit a bugreport.\n\n");
      // dlg.setText(noMessage ? "" : className);
      //
      // dlg.setIcon(TaskDialog.StandardIcon.ERROR);
      // dlg.setCommands(StandardCommand.CANCEL.derive(TaskDialog.makeKey("Close")));
      //
      // JTextArea text = new JTextArea();
      // text.setEditable(false);
      // text.setFont(UIManager.getFont("Label.font"));
      // text.setText(Strings.stackStraceAsString(ex));
      // text.setCaretPosition(0);
      //
      // JScrollPane scroller = new JScrollPane(text);
      // scroller.setPreferredSize(new Dimension(400, 200));
      // dlg.getDetails().setExpandableComponent(scroller);
      // dlg.getDetails().setExpanded(noMessage);
      //
      // dlg.setResizable(true);
      //
      // // Issue 22: Exception can be printed by user if required
      // // ex.printStackTrace();
      // dlg.setVisible(true);
      MessageDialog.showExceptionWindow(ex);
    }
  }
}
