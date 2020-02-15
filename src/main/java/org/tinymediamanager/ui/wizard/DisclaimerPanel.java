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
package org.tinymediamanager.ui.wizard;

import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * The class DisclaimerPanel is used to display a disclaimer to the users
 * 
 * @author Manuel Laggner
 */
class DisclaimerPanel extends JPanel {
  private static final long           serialVersionUID = -4743134514329815273L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public DisclaimerPanel() {
    initComponents();
  }

  /*
   * init UI components
   */
  private void initComponents() {
    setLayout(new MigLayout("", "[400lp:400lp,grow]", "[][150lp:200lp,grow]"));
    {
      JLabel lblDisclaimer = new JLabel(BUNDLE.getString("wizard.disclaimer"));
      TmmFontHelper.changeFont(lblDisclaimer, 1.3333, Font.BOLD);
      add(lblDisclaimer, "cell 0 0,growx");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      add(scrollPane, "cell 0 1,grow");

      JTextArea taDisclaimer = new ReadOnlyTextArea(BUNDLE.getString("wizard.disclaimer.long"));
      scrollPane.setViewportView(taDisclaimer);
    }
  }
}
