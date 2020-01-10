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
import java.awt.Window;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.ui.components.MediaIdTable;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;

import net.miginfocom.swing.MigLayout;

/**
 * this dialog is used for editing a scraper id
 *
 * @author Manuel Laggner
 */
public class IdEditorDialog extends TmmDialog {
  private final MediaIdTable.MediaId idToEdit;

  private Set<String>                providerIds;

  private JComboBox                  cbProviderId;
  private JTextField                 tfId;

  public IdEditorDialog(Window owner, String title, MediaIdTable.MediaId mediaId, ScraperType type) {
    super(owner, title, "idEditor");
    idToEdit = mediaId;

    providerIds = new HashSet<>();
    for (MediaScraper scraper : MediaScraper.getMediaScrapers(type)) {
      providerIds.add(scraper.getId());
    }

    initComponents();

    cbProviderId.setSelectedItem(idToEdit.key);
    tfId.setText(idToEdit.value);
  }

  private void initComponents() {
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][100lp:n,grow]", "[][]"));
      {
        JLabel lblProviderIdT = new JLabel(BUNDLE.getString("metatag.id.source"));
        panelContent.add(lblProviderIdT, "cell 0 0,alignx trailing");

        cbProviderId = new AutocompleteComboBox(providerIds);
        panelContent.add(cbProviderId, "cell 1 0,growx");
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
      {
        JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
        btnCancel.addActionListener(e -> setVisible(false));
        addButton(btnCancel);

        JButton btnOk = new JButton(BUNDLE.getString("Button.save"));
        btnOk.addActionListener(e -> {
          if (StringUtils.isAnyBlank(tfId.getText(), (String) cbProviderId.getSelectedItem())) {
            JOptionPane.showMessageDialog(IdEditorDialog.this, BUNDLE.getString("id.empty"));
            return;
          }

          idToEdit.key = (String) cbProviderId.getSelectedItem();
          idToEdit.value = tfId.getText();
          setVisible(false);
        });
        addDefaultButton(btnOk);
      }
    }
  }
}
