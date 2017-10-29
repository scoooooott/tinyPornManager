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

import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
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
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final Settings              settings         = Settings.getInstance();
  private JCheckBox                   chckbxAnalytics;

  public DisclaimerPanel() {
    initComponents();
  }

  /*
   * init UI components
   */
  private void initComponents() {
    setLayout(new MigLayout("", "[400lp:400lp,grow]", "[][150lp:200lp,grow][20lp:20lp][][]"));
    {
      JLabel lblDisclaimer = new JLabel(BUNDLE.getString("wizard.disclaimer"));//$NON-NLS-1$
      TmmFontHelper.changeFont(lblDisclaimer, 1.3333, Font.BOLD);
      add(lblDisclaimer, "cell 0 0,growx");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      add(scrollPane, "cell 0 1,grow");

      JTextArea taDisclaimer = new ReadOnlyTextArea(BUNDLE.getString("wizard.disclaimer.long"));//$NON-NLS-1$
      scrollPane.setViewportView(taDisclaimer);
    }
    {
      JTextArea taAnalytics = new JTextArea(BUNDLE.getString("Settings.analytics.desc"));//$NON-NLS-1$
      add(taAnalytics, "cell 0 3,grow");

      chckbxAnalytics = new JCheckBox(BUNDLE.getString("Settings.analytics"));//$NON-NLS-1$
      add(chckbxAnalytics, "cell 0 4");
    }
    initDataBindings();

    chckbxAnalytics.setSelected(true);
  }

  protected void initDataBindings() {
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("enableAnalytics");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAnalytics, jCheckBoxBeanProperty);
    autoBinding.bind();
  }
}
