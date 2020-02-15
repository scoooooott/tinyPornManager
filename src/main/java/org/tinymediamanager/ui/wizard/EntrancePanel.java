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

import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;
import org.tinymediamanager.ui.images.Logo;

import net.miginfocom.swing.MigLayout;

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
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public EntrancePanel() {
    initComponents();
  }

  /*
   * init UI components
   */
  private void initComponents() {
    setLayout(new MigLayout("", "[50lp:50lp,grow][][10lp][][50lp:50lp,grow]", "[20lp:20lp,grow][][20lp:20lp][][50lp:50lp,grow]"));

    final JTextPane tpGreetingHeader = new ReadOnlyTextPane(BUNDLE.getString("wizard.greeting.header"));
    tpGreetingHeader.setEditorKit(new HTMLEditorKit());
    add(tpGreetingHeader, "cell 0 1 5 1,alignx center");

    JLabel lblLogo = new JLabel("");
    lblLogo.setIcon(new Logo(96));
    add(lblLogo, "cell 1 3,alignx right,aligny top");

    JTextPane tpGreetingText = new ReadOnlyTextPane(BUNDLE.getString("wizard.greeting.text"));
    add(tpGreetingText, "cell 3 3,grow");
  }
}
