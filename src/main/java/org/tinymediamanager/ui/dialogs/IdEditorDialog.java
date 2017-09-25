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
import java.awt.Window;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MediaIdTable;

import net.miginfocom.swing.MigLayout;

/**
 * this dialog is used for editing a scraper id
 *
 * @author Manuel Laggner
 */
public class IdEditorDialog extends TmmDialog {
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final MediaIdTable.MediaId  idToEdit;

  private JTextField                  tfProviderId;
  private JTextField                  tfId;

  public IdEditorDialog(Window owner, String title, MediaIdTable.MediaId mediaId) {
    super(owner, title, "idEditor");
    idToEdit = mediaId;

    initComponents();

    tfProviderId.setText(idToEdit.key);
    tfId.setText(idToEdit.value);
  }

  private void initComponents() {
    getContentPane().setLayout(new BorderLayout(0, 0));
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent);
      panelContent.setLayout(new MigLayout("", "[][100lp:n,grow]", "[][]"));
      {
        JLabel lblProviderIdT = new JLabel(BUNDLE.getString("metatag.id.source"));
        panelContent.add(lblProviderIdT, "cell 0 0,alignx trailing");

        tfProviderId = new JTextField();
        panelContent.add(tfProviderId, "cell 1 0,growx");
        tfProviderId.setColumns(10);
      }
      {
        JLabel lblIdT = new JLabel(BUNDLE.getString("metatag.id"));
        panelContent.add(lblIdT, "cell 0 1,alignx trailing");

        tfId = new JTextField();
        panelContent.add(tfId, "cell 1 1,growx");
        tfId.setColumns(10);
      }
    }
    {
      JPanel panelButtons = new JPanel();
      getContentPane().add(panelButtons, BorderLayout.SOUTH);
      panelButtons.setLayout(new MigLayout("", "[grow]", "[]"));
      {
        JPanel panel = new JPanel();
        panel.setLayout(new EqualsLayout(5));
        panelButtons.add(panel, "cell 0 0,alignx right");

        JButton btnOk = new JButton(BUNDLE.getString("Button.save"));
        btnOk.addActionListener(e -> {
          if (StringUtils.isAnyBlank(tfId.getText(), tfProviderId.getText())) {
            JOptionPane.showMessageDialog(IdEditorDialog.this, BUNDLE.getString("id.empty"));
            return;
          }

          idToEdit.key = tfProviderId.getText();
          idToEdit.value = tfId.getText();
          setVisible(false);
        });
        panel.add(btnOk);
        getRootPane().setDefaultButton(btnOk);

        JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
        btnCancel.addActionListener(e -> setVisible(false));
        panel.add(btnCancel);
      }
    }
  }
}
