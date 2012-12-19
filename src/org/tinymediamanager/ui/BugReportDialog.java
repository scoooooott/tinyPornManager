/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Utils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class FeedbackDialog.
 */
public class BugReportDialog extends JDialog {

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID = 1L;

  /** The Constant LOGGER. */
  private static final Logger LOGGER           = Logger.getLogger(BugReportDialog.class);

  /** The text field. */
  private JTextField          textField;

  /** The text area. */
  private JTextArea           textArea;

  /** The chckbx logs. */
  private JCheckBox           chckbxLogs;

  /** The chckbx configxml. */
  private JCheckBox           chckbxConfigxml;

  /** The chckbx database. */
  private JCheckBox           chckbxDatabase;

  /**
   * Instantiates a new feedback dialog.
   */
  public BugReportDialog() {
    setTitle("Report a bug");
    setIconImage(Globals.logo);
    setModal(true);
    setBounds(100, 100, 532, 453);

    getContentPane().setLayout(
        new FormLayout(
            new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(400px;min):grow"), FormFactory.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(250px;min):grow"), FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, "2, 2, fill, fill");
    panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblName = new JLabel("Name (optional)");
    panelContent.add(lblName, "2, 2, right, default");

    textField = new JTextField();
    panelContent.add(textField, "4, 2, fill, default");
    textField.setColumns(10);

    JLabel lblFeedback = new JLabel("Description");
    panelContent.add(lblFeedback, "2, 4, right, top");

    JScrollPane scrollPane = new JScrollPane();
    panelContent.add(scrollPane, "4, 4, fill, fill");

    textArea = new JTextArea();
    scrollPane.setViewportView(textArea);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JLabel lblAttachments = new JLabel("Attachments");
    panelContent.add(lblAttachments, "2, 6");

    chckbxLogs = new JCheckBox("Logs");
    chckbxLogs.setSelected(true);
    panelContent.add(chckbxLogs, "4, 6");

    chckbxConfigxml = new JCheckBox("config.xml");
    panelContent.add(chckbxConfigxml, "4, 7");

    // chckbxDatabase = new JCheckBox("Database");
    // panelContent.add(chckbxDatabase, "4, 8");

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    getContentPane().add(panelButtons, "2, 4, fill, fill");

    JButton btnSend = new JButton("Send bug report");
    btnSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // check if feedback is provided
        if (StringUtils.isEmpty(textArea.getText())) {
          JOptionPane.showMessageDialog(null, "No description provided");
          return;
        }

        // send bug report
        DefaultHttpClient client = Utils.getHttpClient();
        HttpPost post = new HttpPost("https://script.google.com/macros/s/AKfycbzrhTmZiHJb1bdCqyeiVOqLup8zK4Dbx6kAtHYsgzBVqHTaNJqj/exec");
        try {
          StringBuilder message = new StringBuilder("Bug report from ");
          message.append(textField.getText());
          message.append("\n\nVersion: ");
          message.append(ReleaseInfo.getVersion());
          message.append("\nBuild: ");
          message.append(ReleaseInfo.getBuild());
          message.append("\nOS: ");
          message.append(System.getProperty("os.name"));
          message.append(" ");
          message.append(System.getProperty("os.version"));
          message.append("\n\n");
          message.append(textArea.getText());

          // String message = new String("Bug report from " +
          // textField.getText() + "\n\n");
          // message += textArea.getText();

          MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.STRICT);
          mpEntity.addPart("message", new StringBody(message.toString(), Charset.forName("UTF-8")));

          // attach files
          if (chckbxLogs.isSelected() || chckbxConfigxml.isSelected() /*
                                                                       * ||
                                                                       * chckbxDatabase
                                                                       * .
                                                                       * isSelected
                                                                       * ()
                                                                       */) {
            try {
              // byte[] buffer = new byte[1024];

              // build zip with selected files in it
              ByteArrayOutputStream os = new ByteArrayOutputStream();
              ZipOutputStream zos = new ZipOutputStream(os);

              // attach logs
              if (chckbxLogs.isSelected()) {
                ZipEntry ze = new ZipEntry("tmm.log");
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream("logs/tmm.log");

                IOUtils.copy(in, zos);
                in.close();
                zos.closeEntry();
              }

              // attach config file
              if (chckbxConfigxml.isSelected()) {
                ZipEntry ze = new ZipEntry("config.xml");
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream("config.xml");

                IOUtils.copy(in, zos);
                in.close();
                zos.closeEntry();
              }

              // // attach database
              // if (chckbxDatabase.isSelected()) {
              // ZipEntry ze = new ZipEntry("tmm.odb");
              // zos.putNextEntry(ze);
              // FileInputStream in = new FileInputStream("tmm.odb");
              //
              // IOUtils.copy(in, zos);
              // in.close();
              // zos.closeEntry();
              // }

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
          System.out.println(EntityUtils.toString(entity));
          EntityUtils.consume(entity);

        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(null, "Error sending bug report");
          return;
        }
        finally {
          post.releaseConnection();
        }

        JOptionPane.showMessageDialog(null, "Bug report sent");
        setVisible(false);
      }
    });
    panelButtons.add(btnSend);

    JButton btnCacnel = new JButton("Cancel");
    btnCacnel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    panelButtons.add(btnCacnel);
  }
}
