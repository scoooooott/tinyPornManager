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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Utils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class FeedbackDialog.
 */
public class FeedbackDialog extends JDialog {
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The text field. */
  private JTextField                  tfName;

  /** The text area. */
  private JTextArea                   textArea;
  private JTextField                  tfEmail;

  /**
   * Instantiates a new feedback dialog.
   */
  public FeedbackDialog() {
    setTitle(BUNDLE.getString("Feedback")); //$NON-NLS-1$
    setName("feedback");
    setBounds(100, 100, 450, 303);
    TmmWindowSaver.loadSettings(this);

    setIconImage(Globals.logo);
    setModal(true);

    getContentPane().setLayout(
        new FormLayout(
            new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(400px;min):grow"), FormFactory.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(250px;min):grow"), FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, "2, 2, fill, fill");
    panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), }));

    JLabel lblName = new JLabel(BUNDLE.getString("Feedback.name")); //$NON-NLS-1$
    panelContent.add(lblName, "2, 2, right, default");

    tfName = new JTextField();
    panelContent.add(tfName, "4, 2, fill, default");
    tfName.setColumns(10);

    JLabel lblEmailoptional = new JLabel(BUNDLE.getString("Feedback.email")); //$NON-NLS-1$
    panelContent.add(lblEmailoptional, "2, 4, right, default");

    tfEmail = new JTextField();
    panelContent.add(tfEmail, "4, 4, fill, default");
    tfEmail.setColumns(10);

    JLabel lblFeedback = new JLabel(BUNDLE.getString("Feedback.message")); //$NON-NLS-1$
    panelContent.add(lblFeedback, "2, 6, right, top");

    JScrollPane scrollPane = new JScrollPane();
    panelContent.add(scrollPane, "4, 6, fill, fill");

    textArea = new JTextArea();
    scrollPane.setViewportView(textArea);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    getContentPane().add(panelButtons, "2, 4, fill, fill");

    JButton btnSend = new JButton(BUNDLE.getString("Feedback")); //$NON-NLS-1$
    btnSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // check if feedback is provided
        if (StringUtils.isEmpty(textArea.getText())) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("Feedback.message.empty")); //$NON-NLS-1$
          return;
        }

        // send feedback
        DefaultHttpClient client = Utils.getHttpClient();
        HttpPost post = new HttpPost("https://script.google.com/macros/s/AKfycbxTIhI58gwy0UJ0Z1CdmZDdHlwBDU_vugBmQxcKN9aug4nfgrgZ/exec");
        try {
          List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

          String message = new String("Feedback from " + tfName.getText() + "\nEmail: " + tfEmail.getText() + "\nUUID: "
              + System.getProperty("tmm.uuid") + "\n\n");
          message += textArea.getText();

          nameValuePairs.add(new BasicNameValuePair("message", message));
          post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

          HttpResponse response = client.execute(post);

          HttpEntity entity = response.getEntity();
          EntityUtils.consume(entity);

        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("Feedback.send.error")); //$NON-NLS-1$
          return;
        }

        JOptionPane.showMessageDialog(null, BUNDLE.getString("Feedback.send.ok")); //$NON-NLS-1$
        setVisible(false);
        dispose();
      }
    });
    panelButtons.add(btnSend);

    JButton btnCacnel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    btnCacnel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
      }
    });
    panelButtons.add(btnCacnel);
  }
}
