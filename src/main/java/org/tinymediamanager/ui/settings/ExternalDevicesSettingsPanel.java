/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.awt.Font;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.WolDevice;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.WolDeviceDialog;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The ExternalDevicesSettingsPanel - a panel to configure external devices
 * 
 * @author Manuel Laggner
 */
public class ExternalDevicesSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 8176824801347872222L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  private JTable                      tableWolDevices;
  private JTextField                  tfXbmcHost;
  private JTextField                  tfXbmcUsername;
  private JPasswordField              tfXbmcPassword;
  private JButton                     btnRemoveWolDevice;
  private JButton                     btnAddWolDevice;
  private JButton                     btnEditWolDevice;

  public ExternalDevicesSettingsPanel() {

    // UI init
    initComponents();
    initDataBindings();

    // button listeners
    btnAddWolDevice.addActionListener(arg0 -> {
      WolDeviceDialog dialog = new WolDeviceDialog();
      dialog.pack();
      dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
      dialog.setVisible(true);
    });
    btnRemoveWolDevice.addActionListener(e -> {
      int row = tableWolDevices.getSelectedRow();
      row = tableWolDevices.convertRowIndexToModel(row);
      if (row != -1) {
        WolDevice device = Globals.settings.getWolDevices().get(row);
        Globals.settings.removeWolDevice(device);
      }
    });
    btnEditWolDevice.addActionListener(e -> {
      int row = tableWolDevices.getSelectedRow();
      row = tableWolDevices.convertRowIndexToModel(row);
      if (row != -1) {
        WolDevice device = Globals.settings.getWolDevices().get(row);
        if (device != null) {
          WolDeviceDialog dialog = new WolDeviceDialog();
          dialog.setDevice(device);
          dialog.pack();
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      }

    });

    // set column titles
    tableWolDevices.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("Settings.devicename")); //$NON-NLS-1$
    tableWolDevices.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("Settings.macaddress")); //$NON-NLS-1$
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][][150lp][]", "[][200lp][20lp][][][][][]"));
    {
      final JLabel lblWolT = new JLabel(BUNDLE.getString("tmm.wakeonlan")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblWolT, 1.16667, Font.BOLD);
      add(lblWolT, "cell 0 0 2 1");
    }
    {
      JScrollPane spWolDevices = new JScrollPane();
      add(spWolDevices, "cell 1 1 2 1,grow");

      tableWolDevices = new JTable();
      spWolDevices.setViewportView(tableWolDevices);

      btnAddWolDevice = new JButton(BUNDLE.getString("Button.add"));
      add(btnAddWolDevice, "flowy,cell 3 1,growx,aligny top");

      btnEditWolDevice = new JButton(BUNDLE.getString("Button.edit"));
      add(btnEditWolDevice, "cell 3 1,growx");

      btnRemoveWolDevice = new JButton(BUNDLE.getString("Button.remove"));
      add(btnRemoveWolDevice, "cell 3 1,growx");
    }
    {
      final JLabel lblKodiT = new JLabel("Kodi / XBMC");
      TmmFontHelper.changeFont(lblKodiT, 1.16667, Font.BOLD);
      add(lblKodiT, "cell 0 3 2 1");
    }
    {
      JLabel lblXbmcHostT = new JLabel(BUNDLE.getString("Settings.proxyhost"));
      add(lblXbmcHostT, "cell 1 4");

      tfXbmcHost = new JTextField();
      add(tfXbmcHost, "cell 2 4");
      tfXbmcHost.setColumns(20);

      JLabel lblXbmcUsernameT = new JLabel(BUNDLE.getString("Settings.proxyuser"));
      add(lblXbmcUsernameT, "cell 1 5");

      tfXbmcUsername = new JTextField();
      add(tfXbmcUsername, "cell 2 5");
      tfXbmcUsername.setColumns(20);

      JLabel lblXbmcPasswordT = new JLabel(BUNDLE.getString("Settings.proxypass"));
      add(lblXbmcPasswordT, "cell 1 6");

      tfXbmcPassword = new JPasswordField();
      add(tfXbmcPassword, "cell 2 6");
      tfXbmcPassword.setColumns(20);
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, List<WolDevice>> settingsBeanProperty = BeanProperty.create("wolDevices");
    JTableBinding<WolDevice, Settings, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, tableWolDevices);
    //
    BeanProperty<WolDevice, String> wolBeanProperty_1 = BeanProperty.create("name");
    jTableBinding.addColumnBinding(wolBeanProperty_1);
    //
    BeanProperty<WolDevice, String> wolBeanProperty_2 = BeanProperty.create("macAddress");
    jTableBinding.addColumnBinding(wolBeanProperty_2);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_1 = BeanProperty.create("xbmcHost");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, tfXbmcHost, jTextFieldBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_2 = BeanProperty.create("xbmcUsername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, tfXbmcUsername, jTextFieldBeanProperty_1);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_3 = BeanProperty.create("xbmcPassword");
    BeanProperty<JPasswordField, String> jPasswordFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JPasswordField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, tfXbmcPassword, jPasswordFieldBeanProperty);
    autoBinding_2.bind();
  }
}
