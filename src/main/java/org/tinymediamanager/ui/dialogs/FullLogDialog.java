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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.LoggerFactory;
import org.tinymediamanager.InMemoryAppender;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import net.miginfocom.swing.MigLayout;

public class FullLogDialog extends TmmDialog {
  private static final long serialVersionUID = -5054005564554148578L;

  public FullLogDialog() {
    super(BUNDLE.getString("logwindow.title"), "fullLog");
    setBounds(5, 5, 1000, 590);

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new MigLayout("", "[600lp,grow]", "[400lp,grow]"));

    JScrollPane scrollPane = new JScrollPane();
    panelContent.add(scrollPane, "cell 0 0,grow");

    JTextArea taLogs = new JTextArea();
    scrollPane.setViewportView(taLogs);
    taLogs.setEditable(false);
    taLogs.setWrapStyleWord(true);
    taLogs.setLineWrap(true);

    taLogs.setText(processInMemoryLogs());
    taLogs.setCaretPosition(0);
    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
      btnClose.addActionListener(arg0 -> setVisible(false));
      addDefaultButton(btnClose);
    }
  }

  @Override
  public void pack() {
    // do not let it pack - it looks weird
  }

  private String processInMemoryLogs() {
    // trace logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    Appender appender = lc.getLogger("ROOT").getAppender("INMEMORY");
    if (appender instanceof InMemoryAppender) {
      return ((InMemoryAppender) appender).getLog();
    }
    return "";
  }
}
