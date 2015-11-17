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
package org.tinymediamanager.ui.dialogs;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.License;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

/**
 * The Class BugReportDialog, to send bug reports directly from inside tmm.
 * 
 * @author Manuel Laggner
 */
public class BugReportDialog extends TmmDialog {
  private static final long           serialVersionUID = 1992385114573899815L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(BugReportDialog.class);

  private JTextField                  tfName;
  private JTextArea                   textArea;
  private JTextField                  tfEmail;

  /**
   * Instantiates a new feedback dialog.
   */
  public BugReportDialog() {
    super(BUNDLE.getString("BugReport"), "bugReport"); //$NON-NLS-1$
    setBounds(100, 100, 550, 470);

    getContentPane().setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(400px;min):grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(250px;min):grow"), FormFactory.RELATED_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, "2, 2, fill, fill");
    panelContent.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.PARAGRAPH_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));

    JLabel lblName = new JLabel(BUNDLE.getString("BugReport.name")); //$NON-NLS-1$
    panelContent.add(lblName, "2, 2, right, default");

    tfName = new JTextField();
    panelContent.add(tfName, "4, 2, fill, default");
    tfName.setColumns(10);

    JLabel lblEmail = new JLabel(BUNDLE.getString("BugReport.email")); //$NON-NLS-1$
    panelContent.add(lblEmail, "2, 4, right, default");

    tfEmail = new JTextField();
    panelContent.add(tfEmail, "4, 4, fill, default");

    // pre-fill dialog
    if (Globals.isDonator()) {
      Properties p = License.decrypt();
      tfEmail.setText(p.getProperty("email"));
      tfName.setText(p.getProperty("user"));
    }

    JLabel lblEmaildesc = new JLabel(BUNDLE.getString("BugReport.email.description")); //$NON-NLS-1$
    panelContent.add(lblEmaildesc, "2, 5, 3, 1");

    JLabel lblFeedback = new JLabel(BUNDLE.getString("BugReport.description")); //$NON-NLS-1$
    panelContent.add(lblFeedback, "2, 7, 3, 1");

    JScrollPane scrollPane = new JScrollPane();
    panelContent.add(scrollPane, "2, 9, 3, 1, fill, fill");

    textArea = new JTextArea();
    scrollPane.setViewportView(textArea);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JTextPane textPane = new JTextPane();
    textPane.setText(BUNDLE.getString("BugReport.hint"));//$NON-NLS-1$
    textPane.setOpaque(false);
    panelContent.add(textPane, "2, 11, 3, 1, fill, fill");

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    getContentPane().add(panelButtons, "2, 4, fill, fill");

    JButton btnSend = new JButton(BUNDLE.getString("BugReport.send")); //$NON-NLS-1$
    btnSend.setIcon(IconManager.APPLY);
    btnSend.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        // check if feedback is provided
        if (StringUtils.isEmpty(textArea.getText())) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("BugReport.description.empty")); //$NON-NLS-1$
          return;
        }

        // send bug report
        try {
          sendBugReport();
        }
        catch (Exception e) {
          LOGGER.error("failed sending bug report: " + e.getMessage());
          JOptionPane.showMessageDialog(null, BUNDLE.getObject("BugReport.send.error") + "\n" + e.getMessage()); //$NON-NLS-1$
          return;
        }
        BugReportDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        JOptionPane.showMessageDialog(null, BUNDLE.getObject("BugReport.send.ok")); //$NON-NLS-1$
        setVisible(false);
      }
    });
    panelButtons.add(btnSend);

    JButton btnCacnel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    btnCacnel.setIcon(IconManager.CANCEL);
    btnCacnel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    panelButtons.add(btnCacnel);
  }

  private void sendBugReport() throws Exception {
    OkHttpClient client = TmmHttpClient.getHttpClient();
    String url = "https://script.google.com/macros/s/AKfycbzrhTmZiHJb1bdCqyeiVOqLup8zK4Dbx6kAtHYsgzBVqHTaNJqj/exec";

    StringBuilder message = new StringBuilder("Bug report from ");
    message.append(tfName.getText());
    message.append("\nEmail:");
    message.append(tfEmail.getText());
    message.append("\n");
    message.append("\nis Donator?: ");
    message.append(Globals.isDonator());
    message.append("\nVersion: ");
    message.append(ReleaseInfo.getRealVersion());
    message.append("\nBuild: ");
    message.append(ReleaseInfo.getRealBuildDate());
    message.append("\nOS: ");
    message.append(System.getProperty("os.name"));
    message.append(" ");
    message.append(System.getProperty("os.version"));
    message.append("\nJDK: ");
    message.append(System.getProperty("java.version"));
    message.append(" ");
    message.append(System.getProperty("java.vendor"));
    message.append("\nUUID: ");
    message.append(System.getProperty("tmm.uuid"));
    message.append("\n\n");
    message.append(textArea.getText());

    BugReportDialog.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    MultipartBuilder multipartBuilder = new MultipartBuilder();
    multipartBuilder.type(MultipartBuilder.FORM);

    multipartBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"message\""), RequestBody.create(null, message.toString()));
    multipartBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"sender\""), RequestBody.create(null, tfEmail.getText()));

    // attach files
    try {
      // build zip with selected files in it
      ByteArrayOutputStream os = new ByteArrayOutputStream();
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

      byte[] data = os.toByteArray();
      String data_string = Base64.encodeBase64String(data);
      multipartBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"logs\""), RequestBody.create(null, data_string));
    }
    catch (IOException ex) {
      LOGGER.warn("error adding attachments", ex);
    }
    Request request = new Request.Builder().url(url).post(multipartBuilder.build()).build();
    client.newCall(request).execute();
  }
}
