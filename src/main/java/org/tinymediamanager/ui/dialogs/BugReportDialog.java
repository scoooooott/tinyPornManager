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
import java.net.URLEncoder;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.actions.ExportLogAction;

import net.miginfocom.swing.MigLayout;

/**
 * The Class BugReportDialog, to send bug reports directly from inside tmm.
 * 
 * @author Manuel Laggner
 */
public class BugReportDialog extends TmmDialog {
  private static final long   serialVersionUID = 1992385114573899815L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(BugReportDialog.class);

  private static final String DIALOG_ID        = "bugReportdialog";

  /**
   * Instantiates a new feedback dialog.
   */
  public BugReportDialog() {
    super(BUNDLE.getString("BugReport"), DIALOG_ID);

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new MigLayout("", "[][][450lp,grow]", "[][20lp][][][20lp][][][]"));

    final JTextArea taDescription = new JTextArea();
    taDescription.setOpaque(false);
    taDescription.setWrapStyleWord(true);
    taDescription.setLineWrap(true);
    taDescription.setEditable(false);
    taDescription.setText(BUNDLE.getString("BugReport.description"));
    panelContent.add(taDescription, "cell 0 0 3 1,growx");

    final JLabel lblStep1 = new JLabel(BUNDLE.getString("BugReport.step1"));
    panelContent.add(lblStep1, "cell 0 2");

    final JTextArea taStep1 = new JTextArea();
    taStep1.setWrapStyleWord(true);
    taStep1.setLineWrap(true);
    taStep1.setText(BUNDLE.getString("BugReport.step1.description"));
    taStep1.setOpaque(false);
    taStep1.setEditable(false);
    panelContent.add(taStep1, "cell 2 2,growx");

    final JButton btnSaveLogs = new JButton(BUNDLE.getString("BugReport.createlogs"));
    btnSaveLogs.addActionListener(new ExportLogAction());
    panelContent.add(btnSaveLogs, "cell 2 3");

    final JLabel lblStep2 = new JLabel(BUNDLE.getString("BugReport.step2"));
    panelContent.add(lblStep2, "cell 0 5,alignx left,aligny top");

    final JTextArea taStep2 = new JTextArea();
    taStep2.setLineWrap(true);
    taStep2.setWrapStyleWord(true);
    taStep2.setOpaque(false);
    taStep2.setEditable(false);
    taStep2.setText(BUNDLE.getString("BugReport.step2.description"));
    panelContent.add(taStep2, "cell 2 5,growx");

    final JButton btnCreateIssue = new JButton(BUNDLE.getString("BugReport.craeteissue"));
    btnCreateIssue.addActionListener(e -> {
      // create the url for github
      String baseUrl = "https://gitlab.com/tinyMediaManager/tinyMediaManager/issues/new?issue[description]=";
      String params = "Version: " + ReleaseInfo.getRealVersion();
      params += "\nBuild: " + ReleaseInfo.getRealBuildDate();
      params += "\nOS: " + System.getProperty("os.name") + " " + System.getProperty("os.version");
      params += "\nJDK: " + System.getProperty("java.version") + " " + System.getProperty("os.arch") + " " + System.getProperty("java.vendor");
      params += "\n\n__What is the actual behaviour?__\n\n";
      params += "\n\n__What is the expected behaviour?__\n\n";
      params += "\n\n__Steps to reproduce:__\n\n";
      params += "\n\n__Additional__\nHave you attached the logfile from the day it happened?";

      String url = "";
      try {
        url = baseUrl + URLEncoder.encode(params, "UTF-8");
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e1) {
        LOGGER.error("FAQ", e1);
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e1.getLocalizedMessage() }));
      }
    });
    panelContent.add(btnCreateIssue, "cell 2 6,alignx left,aligny center");

    final JLabel lblHintIcon = new JLabel(IconManager.HINT);
    panelContent.add(lblHintIcon, "cell 1 7,alignx left,aligny center");

    final JLabel lblHint = new JLabel(BUNDLE.getString("BugReport.languagehint"));
    panelContent.add(lblHint, "cell 2 7,growx,aligny top");

    JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
    btnClose.setIcon(IconManager.CANCEL_INV);
    btnClose.addActionListener(e -> setVisible(false));
    addDefaultButton(btnClose);
  }

}
