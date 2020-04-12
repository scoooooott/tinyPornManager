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

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.ui.components.MediaRatingTable;

import net.miginfocom.swing.MigLayout;

/**
 * this dialog is used for editing a rating
 *
 * @author Manuel Laggner
 */
public class RatingEditorDialog extends TmmDialog {
  private static final long                  serialVersionUID = 535315882962742572L;
  private static final String                DIALOG_ID        = "ratingEditor";

  private final MediaRatingTable.MediaRating ratingToEdit;

  private JTextField                         tfProviderId;
  private JSpinner                           spRating;
  private JSpinner                           spMaxValue;
  private JSpinner                           spVotes;

  public RatingEditorDialog(Window owner, String title, MediaRatingTable.MediaRating mediaRating) {
    super(owner, title, DIALOG_ID);
    ratingToEdit = mediaRating;

    initComponents();

    tfProviderId.setText(ratingToEdit.key);
    spRating.setValue(ratingToEdit.value);
    spMaxValue.setValue(ratingToEdit.maxValue);
    spVotes.setValue(ratingToEdit.votes);

    // if there is not rating set (new one) enter the last remembered one
    if (StringUtils.isBlank(ratingToEdit.key) && ratingToEdit.value == 0) {
      tfProviderId.setText(TmmProperties.getInstance().getProperty(DIALOG_ID + ".ratingid"));
    }
  }

  private void initComponents() {
    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent);
    panelContent.setLayout(new MigLayout("", "[][50lp][20lp:n][][50lp]", "[][][]"));
    {
      JLabel lblProviderIdT = new JLabel(BUNDLE.getString("metatag.rating.source"));
      panelContent.add(lblProviderIdT, "cell 0 0,alignx trailing");

      tfProviderId = new JTextField();
      panelContent.add(tfProviderId, "cell 1 0 4 1,growx");
      tfProviderId.setColumns(10);
    }
    {
      JLabel lblRatingT = new JLabel(BUNDLE.getString("metatag.rating"));
      panelContent.add(lblRatingT, "cell 0 1,alignx trailing");

      spRating = new JSpinner(new SpinnerNumberModel(0, 0.0, 1000.0, 0.1));
      panelContent.add(spRating, "cell 1 1,growx");
    }
    {
      JLabel lblMaxValue = new JLabel(BUNDLE.getString("metatag.rating.maxvalue"));
      panelContent.add(lblMaxValue, "cell 3 1");

      spMaxValue = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
      panelContent.add(spMaxValue, "cell 4 1,growx");
    }
    {
      JLabel lblVotes = new JLabel(BUNDLE.getString("metatag.rating.votes"));
      panelContent.add(lblVotes, "cell 0 2");

      spVotes = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
      panelContent.add(spVotes, "cell 1 2,growx");
    }
    {
      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
      btnCancel.addActionListener(e -> setVisible(false));
      addButton(btnCancel);

      JButton btnOk = new JButton(BUNDLE.getString("Button.save"));
      btnOk.addActionListener(e -> {
        float rating = ((Double) spRating.getValue()).floatValue();
        int maxValue = (int) spMaxValue.getValue();

        if (StringUtils.isBlank(tfProviderId.getText())) {
          JOptionPane.showMessageDialog(RatingEditorDialog.this, BUNDLE.getString("id.empty"));
          return;
        }

        if (rating > maxValue) {
          JOptionPane.showMessageDialog(RatingEditorDialog.this, BUNDLE.getString("rating.rating.higher.maxvalue"));
          return;
        }

        ratingToEdit.key = tfProviderId.getText();
        ratingToEdit.value = rating;
        ratingToEdit.maxValue = maxValue;
        ratingToEdit.votes = (int) spVotes.getValue();

        // store the ID for the next usage
        TmmProperties.getInstance().putProperty(DIALOG_ID + ".ratingid", ratingToEdit.key);

        setVisible(false);
      });
      addDefaultButton(btnOk);
    }
  }
}
