/*
 * Copyright 2012 - 2017 Manuel Laggner
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class BugReportDialog, to send bug reports directly from inside tmm.
 * 
 * @author Manuel Laggner
 */
public class BugReportDialog extends TmmDialog {
  private static final long           serialVersionUID = 1992385114573899815L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(BugReportDialog.class);

  private static final String         DIALOG_ID        = "bugReportdialog";

  /**
   * Instantiates a new feedback dialog.
   */
  public BugReportDialog() {
    super(BUNDLE.getString("BugReport"), DIALOG_ID);
    getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.UNRELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.UNRELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.UNRELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.UNRELATED_GAP_ROWSPEC, }));

    final JTextArea taDescription = new JTextArea();
    taDescription.setOpaque(false);
    taDescription.setWrapStyleWord(true);
    taDescription.setLineWrap(true);
    taDescription.setEditable(false);
    taDescription.setText(BUNDLE.getString("BugReport.description")); //$NON-NLS-1$
    panelContent.add(taDescription, "2, 2, 6, 1, fill, fill");

    final JButton btnSaveLogs = new JButton(BUNDLE.getString("BugReport.createlogs")); //$NON-NLS-1$
    btnSaveLogs.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // open the log download window
        try {
          String path = TmmProperties.getInstance().getProperty(DIALOG_ID + ".path");
          Path file = TmmUIHelper.saveFile(BUNDLE.getString("BugReport.savelogs"), path, "tmm_logs.zip", //$NON-NLS-1$
              new FileNameExtensionFilter("Zip files", ".zip"));
          if (Files.exists(file)) {
            writeLogsFile(file.toFile());
            TmmProperties.getInstance().putProperty(DIALOG_ID + ".path", file.toAbsolutePath().toString());
          }
        }
        catch (Exception ex) {
          LOGGER.error("Could not write logs.zip: " + ex.getMessage());
        }
      }
    });

    final JLabel lblStep1 = new JLabel(BUNDLE.getString("BugReport.step1")); //$NON-NLS-1$
    panelContent.add(lblStep1, "2, 4, default, top");

    final JTextArea taStep1 = new JTextArea();
    taStep1.setText(BUNDLE.getString("BugReport.step1.description")); //$NON-NLS-1$
    taStep1.setOpaque(false);
    taStep1.setEditable(false);
    panelContent.add(taStep1, "5, 4, fill, fill");
    panelContent.add(btnSaveLogs, "7, 4");

    final JButton btnCreateIssue = new JButton(BUNDLE.getString("BugReport.craeteissue")); //$NON-NLS-1$
    btnCreateIssue.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // create the url for github
        String baseUrl = "https://github.com/tinyMediaManager/tinyMediaManager/issues/new?body=";
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
      }
    });

    final JLabel lblStep2 = new JLabel(BUNDLE.getString("BugReport.step2")); //$NON-NLS-1$
    panelContent.add(lblStep2, "2, 6, default, top");

    final JTextArea taStep2 = new JTextArea();
    taStep2.setOpaque(false);
    taStep2.setEditable(false);
    taStep2.setText(BUNDLE.getString("BugReport.step2.description")); //$NON-NLS-1$
    panelContent.add(taStep2, "5, 6, fill, fill");
    panelContent.add(btnCreateIssue, "7, 6");

    final JLabel lblHintIcon = new JLabel(IconManager.HINT);
    panelContent.add(lblHintIcon, "3, 8");

    final JLabel lblHint = new JLabel(BUNDLE.getString("BugReport.languagehint")); //$NON-NLS-1$
    panelContent.add(lblHint, "5, 8");

    JPanel panelButtons = new JPanel();

    getContentPane().add(panelButtons, BorderLayout.SOUTH);

    JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
    btnClose.setIcon(IconManager.CANCEL);
    btnClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    panelButtons
        .setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("25px"), FormSpecs.RELATED_GAP_ROWSPEC, }));
    panelButtons.add(btnClose, "2, 2");
  }

  private void writeLogsFile(File file) throws Exception {
    FileOutputStream os = new FileOutputStream(file);
    ZipOutputStream zos = new ZipOutputStream(os);

    // attach logs
    File[] logs = new File("logs").listFiles(new FilenameFilter() {
      Pattern logPattern = Pattern.compile("tmm\\.log\\.*");

      @Override
      public boolean accept(File directory, String filename) {
        Matcher matcher = logPattern.matcher(filename);
        if (matcher.find()) {
          return true;
        }
        return false;
      }
    });
    if (logs != null) {
      for (File logFile : logs) {
        try {
          FileInputStream in = new FileInputStream(logFile);
          ZipEntry ze = new ZipEntry(logFile.getName());
          zos.putNextEntry(ze);

          IOUtils.copy(in, zos);
          in.close();
          zos.closeEntry();
        }
        catch (Exception e) {
          LOGGER.warn("unable to attach " + logFile.getName() + ": " + e.getMessage());
        }
      }
    }

    try {
      FileInputStream in = new FileInputStream("launcher.log");
      ZipEntry ze = new ZipEntry("launcher.log");
      zos.putNextEntry(ze);

      IOUtils.copy(in, zos);
      in.close();
      zos.closeEntry();
    }
    catch (Exception e) {
      LOGGER.warn("unable to attach launcher.log: " + e.getMessage());
    }

    // attach config file
    try {
      ZipEntry ze = new ZipEntry("config.xml");
      zos.putNextEntry(ze);
      FileInputStream in = new FileInputStream(new File(Settings.getInstance().getSettingsFolder(), "config.xml"));

      IOUtils.copy(in, zos);
      in.close();
      zos.closeEntry();
    }
    catch (Exception e) {
      LOGGER.warn("unable to attach config.xml: " + e.getMessage());
    }

    zos.close();
    os.close();
  }
}
