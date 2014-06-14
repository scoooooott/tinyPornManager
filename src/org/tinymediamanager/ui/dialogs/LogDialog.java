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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUILogAppender.LogOutput;
import org.tinymediamanager.ui.TmmUILogCollector;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class LogDialog extends JDialog implements ActionListener {
  private static final long           serialVersionUID = -5054005564554148578L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(LogDialog.class);
  private static final int            REFRESH_PERIOD   = 1000;

  private JTextArea                   taLogs;

  private int                         logByteCount     = 0;
  private final Timer                 timerRefresh;

  public LogDialog() {
    setIconImage(MainWindow.LOGO);
    setTitle(BUNDLE.getString("logwindow.title")); //$NON-NLS-1$
    setSize(600, 250);

    timerRefresh = new Timer(REFRESH_PERIOD, this);
    timerRefresh.setInitialDelay(0);

    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JScrollPane scrollPane = new JScrollPane();
    getContentPane().add(scrollPane, "2, 2, fill, fill");

    taLogs = new JTextArea();
    scrollPane.setViewportView(taLogs);
    taLogs.setEditable(false);
    taLogs.setWrapStyleWord(true);
    taLogs.setLineWrap(true);

    taLogs.setText(TmmUILogCollector.instance.getLogOutput().getContent());
    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          setVisible(false);
          dispose();
        }
      });
      getContentPane().add(btnClose, "2, 4, right, default");
    }
    timerRefresh.start();
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == timerRefresh) {
      updateApplicationLog();
    }
  }

  private void updateApplicationLog() {
    final boolean append = (logByteCount > 0) ? true : false;
    final LogOutput logOutput = TmmUILogCollector.instance.getLogOutput(this.logByteCount);
    logByteCount = logOutput.getByteCount();
    final String content = logOutput.getContent();

    if (content.length() > 0) {
      if (append) {
        final Document doc = taLogs.getDocument();
        try {
          doc.insertString(doc.getLength(), content, null);
        }
        catch (BadLocationException ble) {
          LOGGER.error("bad location: ", ble);
        }
      }
      else {
        taLogs.setText(content);
      }
      // scroll to the end of the textarea
      taLogs.setCaretPosition(taLogs.getText().length());
    }
  }
}
