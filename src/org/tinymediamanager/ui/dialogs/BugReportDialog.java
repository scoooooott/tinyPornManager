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
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.License;
import org.tinymediamanager.scraper.util.TmmHttpClient;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
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

  private JTextField                  tfName;
  private JTextArea                   textArea;
  private JTextField                  tfEmail;
  private JCheckBox                   chckbxLogs;
  private JCheckBox                   chckbxConfigxml;

  /**
   * Instantiates a new feedback dialog.
   */
  public BugReportDialog() {
    super(BUNDLE.getString("BugReport"), "bugReport"); //$NON-NLS-1$
    setBounds(100, 100, 532, 453);

    getContentPane().setLayout(
        new FormLayout(
            new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(400px;min):grow"), FormFactory.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(250px;min):grow"), FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, "2, 2, fill, fill");
    panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.PARAGRAPH_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

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

    JLabel lblAttachments = new JLabel(BUNDLE.getString("BugReport.attachments")); //$NON-NLS-1$
    panelContent.add(lblAttachments, "2, 11");

    chckbxLogs = new JCheckBox(BUNDLE.getString("BugReport.logs")); //$NON-NLS-1$
    chckbxLogs.setSelected(true);
    panelContent.add(chckbxLogs, "4, 11");

    chckbxConfigxml = new JCheckBox("config.xml");
    panelContent.add(chckbxConfigxml, "4, 12");

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
        HttpClient client = TmmHttpClient.getHttpClient();
        HttpPost post = new HttpPost("https://script.google.com/macros/s/AKfycbzrhTmZiHJb1bdCqyeiVOqLup8zK4Dbx6kAtHYsgzBVqHTaNJqj/exec");
        try {
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

          MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.STRICT);
          mpEntity.addPart("message", new StringBody(message.toString(), Charset.forName("UTF-8")));
          mpEntity.addPart("sender", new StringBody(tfEmail.getText(), ContentType.TEXT_PLAIN));

          // attach files
          if (chckbxLogs.isSelected() || chckbxConfigxml.isSelected() /*
                                                                       * || chckbxDatabase . isSelected ()
                                                                       */) {
            try {
              // build zip with selected files in it
              ByteArrayOutputStream os = new ByteArrayOutputStream();
              ZipOutputStream zos = new ZipOutputStream(os);

              // attach logs
              if (chckbxLogs.isSelected()) {
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
                      ZipEntry ze = new ZipEntry(logFile.getName());
                      zos.putNextEntry(ze);
                      FileInputStream in = new FileInputStream(logFile);

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
                  ZipEntry ze = new ZipEntry("launcher.log");
                  zos.putNextEntry(ze);
                  FileInputStream in = new FileInputStream("launcher.log");

                  IOUtils.copy(in, zos);
                  in.close();
                  zos.closeEntry();
                }
                catch (Exception e) {
                  LOGGER.warn("unable to attach launcher.log: " + e.getMessage());
                }
              }

              // attach config file
              if (chckbxConfigxml.isSelected()) {
                try {
                  ZipEntry ze = new ZipEntry("config.xml");
                  zos.putNextEntry(ze);
                  FileInputStream in = new FileInputStream("config.xml");

                  IOUtils.copy(in, zos);
                  in.close();
                  zos.closeEntry();
                }
                catch (Exception e) {
                  LOGGER.warn("unable to attach config.xml: " + e.getMessage());
                }
              }

              zos.close();

              byte[] data = os.toByteArray();
              String data_string = Base64.encodeBase64String(data);
              mpEntity.addPart("logs", new StringBody(data_string));
            }

            catch (IOException ex) {
              LOGGER.warn("error adding attachments", ex);
            }
          }

          post.setEntity(mpEntity);
          HttpResponse response = client.execute(post);

          HttpEntity entity = response.getEntity();
          EntityUtils.consume(entity);

        }
        catch (IOException e) {
          LOGGER.error("failed sending bug report: " + e.getMessage());
          JOptionPane.showMessageDialog(null, BUNDLE.getObject("BugReport.send.error") + "\n" + e.getMessage()); //$NON-NLS-1$
          return;
        }
        finally {
          post.releaseConnection();
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
}
