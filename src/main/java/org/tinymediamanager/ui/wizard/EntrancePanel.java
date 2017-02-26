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

import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.images.Logo;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class EntrancePanel is the first panel which is displayed in the wizard
 * 
 * @author Manuel Laggner
 */
class EntrancePanel extends JPanel {
  private static final long           serialVersionUID = -4743144534338715073L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public EntrancePanel() {
    initComponents();
  }

  /*
   * init UI components
   */
  private void initComponents() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50dlu"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50dlu"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, RowSpec.decode("50dlu"), FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("50dlu"),
            RowSpec.decode("default:grow"), FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));

    final JTextPane tpGreetingHeader = new JTextPane();
    tpGreetingHeader.setEditable(false);
    tpGreetingHeader.setOpaque(false);
    tpGreetingHeader.setEditorKit(new HTMLEditorKit());
    tpGreetingHeader.setText(BUNDLE.getString("wizard.greeting.header")); //$NON-NLS-1$
    add(tpGreetingHeader, "2, 2, 7, 1, center, bottom");

    JLabel lblLogo = new JLabel("");
    lblLogo.setIcon(new Logo(96));
    add(lblLogo, "4, 5, default, top");

    JTextPane tpGreetingText = new JTextPane();
    tpGreetingText.setEditable(false);
    tpGreetingText.setText(BUNDLE.getString("wizard.greeting.text")); //$NON-NLS-1$
    tpGreetingText.setOpaque(false);
    add(tpGreetingText, "6, 5, fill, fill");
  }
}
