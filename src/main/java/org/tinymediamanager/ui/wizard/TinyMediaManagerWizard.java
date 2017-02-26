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
package org.tinymediamanager.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class TinyMediaManagerWizard provides a wizard for easy first time setup of tinyMediaManager
 * 
 * @author Manuel Laggner
 */
public class TinyMediaManagerWizard extends TmmDialog {
  private static final long           serialVersionUID = 1112053710541745443L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<JPanel>                panels;
  private int                         activePanelIndex = 0;

  private JButton                     btnBack;
  private JButton                     btnNext;
  private JButton                     btnCancel;
  private JPanel                      panelContent;

  public TinyMediaManagerWizard() {
    super("tinyMediaManager Setup Wizard", "wizard");
    setBounds(100, 100, 577, 71);

    initComponents();

    // data init
    panels = new ArrayList<>();
    panels.add(new EntrancePanel());
    panels.add(new MovieSourcePanel());
    panels.add(new MovieScraperPanel());
    panels.add(new TvShowSourcePanel());
    panels.add(new TvShowScraperPanel());

    for (int i = 0; i < panels.size(); i++) {
      JPanel panel = panels.get(i);
      panelContent.add(panel, "" + i);
    }

    btnBack.setEnabled(false);
  }

  private void initComponents() {
    JPanel panelSizing = new JPanel();
    panelSizing.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("450dlu:grow") }, new RowSpec[] { RowSpec.decode("300dlu:grow") }));
    getContentPane().add(panelSizing, BorderLayout.CENTER);

    panelContent = new JPanel();
    panelContent.setLayout(new CardLayout());
    panelSizing.add(panelContent, "1, 1, fill, fill");

    final JPanel panelSouth = new JPanel();
    panelSouth
        .setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LINE_GAP_ROWSPEC, }));

    final JPanel panelButtons = new JPanel();
    EqualsLayout layout = new EqualsLayout(5);
    layout.setMinWidth(75);
    layout.setAlignment(EqualsLayout.RIGHT);
    panelButtons.setLayout(layout);

    panelSouth.add(panelButtons, "2, 2, fill, fill");
    getContentPane().add(panelSouth, BorderLayout.SOUTH);

    btnBack = new JButton();
    btnBack.setAction(new BackAction());
    panelButtons.add(btnBack);

    btnNext = new JButton();
    btnNext.setAction(new NextAction());
    panelButtons.add(btnNext);

    btnCancel = new JButton();
    btnCancel.setAction(new FinishAction());
    panelButtons.add(btnCancel);
  }

  @Override
  public void setVisible(boolean visible) {
    if (!visible) {
      Globals.settings.saveSettings();
    }
    super.setVisible(visible);
  }

  private class BackAction extends AbstractAction {
    private static final long serialVersionUID = -510135441507847318L;

    public BackAction() {
      putValue(NAME, BUNDLE.getString("wizard.back")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      activePanelIndex--;
      if (activePanelIndex == 0) {
        btnBack.setEnabled(false);
      }
      btnNext.setEnabled(true);
      CardLayout cl = (CardLayout) (panelContent.getLayout());
      cl.show(panelContent, "" + activePanelIndex);
    }
  }

  private class NextAction extends AbstractAction {
    private static final long serialVersionUID = -7813935881525980050L;

    public NextAction() {
      putValue(NAME, BUNDLE.getString("wizard.next")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      activePanelIndex++;
      if (panels.size() == activePanelIndex + 1) {
        btnNext.setEnabled(false);
      }
      btnBack.setEnabled(true);

      CardLayout cl = (CardLayout) (panelContent.getLayout());
      cl.show(panelContent, "" + activePanelIndex);
    }
  }

  private class FinishAction extends AbstractAction {
    private static final long serialVersionUID = 8047070989186510289L;

    public FinishAction() {
      putValue(NAME, BUNDLE.getString("wizard.finish")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TinyMediaManagerWizard.this.setVisible(false);
    }
  }
}
