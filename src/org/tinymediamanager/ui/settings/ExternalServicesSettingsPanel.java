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
package org.tinymediamanager.ui.settings;

import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class ExternalServicesSettingsPanel. Handle all settings for the external services
 * 
 * @author Manuel Laggner
 */
public class ExternalServicesSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 7266564870819511988L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  private JTextField                  tfTraktUsername;
  private JPasswordField              tfTraktPassword;
  private JTextField                  tfTraktAPIKey;

  public ExternalServicesSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));
    {
      JPanel panelTrakttv = new JPanel();
      panelTrakttv.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.trakttv"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
      add(panelTrakttv, "2, 2, fill, fill");
      panelTrakttv.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(125dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JLabel lblTraktUsername = new JLabel(BUNDLE.getString("Settings.proxyuser")); //$NON-NLS-1$
        panelTrakttv.add(lblTraktUsername, "2, 2, right, default");
      }
      {
        tfTraktUsername = new JTextField();
        panelTrakttv.add(tfTraktUsername, "4, 2, fill, default");
        tfTraktUsername.setColumns(10);
      }
      {
        JLabel lblTraktPassword = new JLabel(BUNDLE.getString("Settings.proxypass")); //$NON-NLS-1$
        panelTrakttv.add(lblTraktPassword, "2, 4, right, default");
      }
      {
        tfTraktPassword = new JPasswordField();
        panelTrakttv.add(tfTraktPassword, "4, 4, fill, default");
        tfTraktPassword.setColumns(10);
      }
      {
        JLabel lblTraktAPIKey = new JLabel(BUNDLE.getString("Settings.trakttv.apikey")); //$NON-NLS-1$
        panelTrakttv.add(lblTraktAPIKey, "2, 6, right, default");
      }
      {
        tfTraktAPIKey = new JTextField();
        panelTrakttv.add(tfTraktAPIKey, "4, 6, fill, default");
        tfTraktAPIKey.setColumns(10);
      }
    }
    initDataBindings();

  }

  protected void initDataBindings() {
    BeanProperty<Settings, String> settingsBeanProperty = BeanProperty.create("traktUsername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, tfTraktUsername, jTextFieldBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_1 = BeanProperty.create("traktPassword");
    BeanProperty<JPasswordField, String> jPasswordFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JPasswordField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, tfTraktPassword, jPasswordFieldBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_2 = BeanProperty.create("traktAPI");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, tfTraktAPIKey, jTextFieldBeanProperty_1);
    autoBinding_2.bind();
  }
}
