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
package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ui.TmmUILogAppender.LogOutput;
import org.tinymediamanager.ui.TmmUILogCollector;

import net.miginfocom.swing.MigLayout;

public class LogDialog extends TmmDialog implements ActionListener {
  private static final long   serialVersionUID = -5054005564554148578L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(LogDialog.class);
  private static final int    REFRESH_PERIOD   = 1000;

  private JTextArea           taLogs;

  private int                 logByteCount     = 0;
  private final Timer         timerRefresh;

  public LogDialog() {
    super(BUNDLE.getString("logwindow.title"), "log");
    setBounds(5, 5, 1000, 590);

    timerRefresh = new Timer(REFRESH_PERIOD, this);
    timerRefresh.setInitialDelay(0);

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new MigLayout("", "[600lp,grow]", "[400lp,grow]"));

    JScrollPane scrollPane = new JScrollPane();
    panelContent.add(scrollPane, "cell 0 0,grow");

    taLogs = new JTextArea();
    scrollPane.setViewportView(taLogs);
    taLogs.setEditable(false);
    taLogs.setWrapStyleWord(true);
    taLogs.setLineWrap(true);

    taLogs.setText(TmmUILogCollector.instance.getLogOutput().getContent());
    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
      btnClose.addActionListener(arg0 -> setVisible(false));
      addDefaultButton(btnClose);
    }
    timerRefresh.start();
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == timerRefresh) {
      updateApplicationLog();
    }
  }

  @Override
  public void pack() {
    // do not let it pack - it looks weird
  }

  private void updateApplicationLog() {
    final boolean append = logByteCount > 0;
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
