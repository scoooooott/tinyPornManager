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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.License;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class RegisterDonatorVersionDialog. The interface to register to the donator version
 * 
 * @author Manuel Laggner
 */
public class RegisterDonatorVersionDialog extends TmmDialog {
  private static final long           serialVersionUID = 9111695923659250520L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());    //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(RegisterDonatorVersionDialog.class);

  /** UI components */
  private JTextField                  tfName;
  private JTextField                  tfEmailAddress;

  public RegisterDonatorVersionDialog() {
    super(BUNDLE.getString("tmm.registerdonator"), "registerDonator"); //$NON-NLS-1$
    setBounds(166, 5, 400, 300);
    boolean isDonator = Globals.isDonator();
    Properties props = null;
    if (isDonator) {
      props = License.decrypt();
    }

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.PARAGRAPH_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, }));

      {
        JTextArea textArea = new JTextArea();
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        panelContent.add(textArea, "2, 2, 3, 1, default, center");
        if (isDonator) {
          textArea.setText(BUNDLE.getString("tmm.registerdonator.thanks")); //$NON-NLS-1$
        }
        else {
          textArea.setText(BUNDLE.getString("tmm.registerdonator.hint")); //$NON-NLS-1$
        }
      }
      {
        JLabel lblName = new JLabel(BUNDLE.getString("BugReport.name")); //$NON-NLS-1$
        panelContent.add(lblName, "2, 4, right, default");
        tfName = new JTextField("");
        lblName.setLabelFor(tfName);
        panelContent.add(tfName, "4, 4, fill, default");
        tfName.setColumns(10);
        if (isDonator) {
          tfName.setText(props.getProperty("user"));
          tfName.setEnabled(false);
        }
      }
      {
        JLabel lblEmailAddress = new JLabel(BUNDLE.getString("BugReport.email")); //$NON-NLS-1$
        panelContent.add(lblEmailAddress, "2, 6, right, default");
        tfEmailAddress = new JTextField("");
        lblEmailAddress.setLabelFor(tfEmailAddress);
        panelContent.add(tfEmailAddress, "4, 6, fill, default");
        tfEmailAddress.setColumns(10);
        if (isDonator) {
          tfEmailAddress.setText(props.getProperty("email"));
          tfEmailAddress.setEnabled(false);
        }
      }
    }
    {
      JPanel panelButtons = new JPanel();
      panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
      getContentPane().add(panelButtons, BorderLayout.SOUTH);
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      panelButtons.setLayout(layout);
      {
        JButton btnRegister = new JButton(BUNDLE.getString("Button.register")); //$NON-NLS-1$
        btnRegister.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
              Properties p = new Properties();
              p.setProperty("user", tfName.getText());
              p.setProperty("email", tfEmailAddress.getText());
              p.setProperty("generated", String.valueOf(new Date().getTime()));
              p.setProperty("uuid", FileUtils.readFileToString(new File("tmm.uuid")));

              // get encrypted string and write tmm.lic
              if (License.encrypt(p) && License.isValid()) {
                JOptionPane.showMessageDialog(RegisterDonatorVersionDialog.this, BUNDLE.getString("tmm.registerdonator.success")); //$NON-NLS-1$
                setVisible(false);
              }
              else {
                JOptionPane.showMessageDialog(RegisterDonatorVersionDialog.this, BUNDLE.getString("tmm.registerdonator.error")); //$NON-NLS-1$
              }
            }
            catch (Exception ex) {
              LOGGER.error("Error registering donator version: " + ex.getMessage());
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        });
        if (isDonator) {
          btnRegister.setEnabled(false);
        }
        panelButtons.add(btnRegister);
      }
      {
        JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$ 
        btnClose.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
          }
        });
        panelButtons.add(btnClose);
      }
    }
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        tfName.requestFocus();
      }
    });
  }

  @Override
  public void pack() {
    // do nothing
  }
}
