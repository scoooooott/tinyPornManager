/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.WolDevice;
import org.tinymediamanager.jsonrpc.config.HostConfig;
import org.tinymediamanager.jsonrpc.io.ApiException;
import org.tinymediamanager.thirdparty.KodiRPC;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.WolDeviceDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The ExternalDevicesSettingsPanel - a panel to configure external devices
 * 
 * @author Manuel Laggner
 */
public class ExternalDevicesSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 8176824801347872222L;
  private static final Logger         LOGGER           = LoggerFactory.getLogger(ExternalDevicesSettingsPanel.class);

  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());    //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  private JTable                      tableWolDevices;
  private JTextField                  tfKodiHost;
  private JTextField                  tfKodiTcpPort;
  private JTextField                  tfKodiHttpPort;
  private JTextField                  tfKodiUsername;
  private JPasswordField              tfKodiPassword;
  private JButton                     btnRemoveWolDevice;
  private JButton                     btnAddWolDevice;
  private JButton                     btnEditWolDevice;
  private JCheckBox                   chckbxUpnpShareLibrary;
  private JCheckBox                   chckbxUpnpRemotePlay;

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
    setLayout(new MigLayout("", "[25lp][][150lp][]", "[][200lp][20lp][][][][][20lp][][][]"));
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

      btnAddWolDevice = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      add(btnAddWolDevice, "flowy,cell 3 1,growx,aligny top");

      btnEditWolDevice = new JButton(BUNDLE.getString("Button.edit")); //$NON-NLS-1$
      add(btnEditWolDevice, "cell 3 1,growx");

      btnRemoveWolDevice = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
      add(btnRemoveWolDevice, "cell 3 1,growx");
    }
    {
      final JLabel lblKodiT = new JLabel("Kodi / XBMC"); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblKodiT, 1.16667, Font.BOLD);
      add(lblKodiT, "cell 0 3 2 1");
    }
    {
      JLabel lblKodiHostT = new JLabel(BUNDLE.getString("Settings.kodi.host")); //$NON-NLS-1$
      add(lblKodiHostT, "cell 1 4");

      tfKodiHost = new JTextField();
      add(tfKodiHost, "cell 2 4");
      tfKodiHost.setColumns(20);

      JButton btnKodiConnect = new JButton(BUNDLE.getString("Settings.kodi.connect")); //$NON-NLS-1$
      btnKodiConnect.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          HostConfig c = new HostConfig(tfKodiHost.getText(), tfKodiHttpPort.getText(), tfKodiTcpPort.getText(), tfKodiUsername.getText(),
              new String(tfKodiPassword.getPassword()));
          try {
            KodiRPC.getInstance().connect(c);
          }
          catch (ApiException cex) {
            LOGGER.error("Error connecting to Kodi instance!", e);
          }
        }
      });
      add(btnKodiConnect, "cell 3 4,growx");

      JLabel lblKodiHttpPortT = new JLabel(BUNDLE.getString("Settings.kodi.httpport")); //$NON-NLS-1$
      add(lblKodiHttpPortT, "cell 1 5");

      tfKodiHttpPort = new JTextField();
      add(tfKodiHttpPort, "cell 2 5");
      tfKodiHttpPort.setColumns(20);

      JButton btnKodiDisconnect = new JButton(BUNDLE.getString("Settings.kodi.disconnect")); //$NON-NLS-1$
      btnKodiDisconnect.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          KodiRPC.getInstance().disconnect();
        }
      });
      add(btnKodiDisconnect, "cell 3 5,growx");

      JLabel lblKodiTcpPortT = new JLabel(BUNDLE.getString("Settings.kodi.tcpport")); //$NON-NLS-1$
      add(lblKodiTcpPortT, "cell 1 6");

      tfKodiTcpPort = new JTextField();
      add(tfKodiTcpPort, "cell 2 6");
      tfKodiTcpPort.setColumns(20);

      JLabel lblKodiUsernameT = new JLabel(BUNDLE.getString("Settings.kodi.user")); //$NON-NLS-1$
      add(lblKodiUsernameT, "cell 1 7");

      tfKodiUsername = new JTextField();
      add(tfKodiUsername, "cell 2 7");
      tfKodiUsername.setColumns(20);

      JLabel lblKodiPasswordT = new JLabel(BUNDLE.getString("Settings.kodi.pass")); //$NON-NLS-1$
      add(lblKodiPasswordT, "cell 1 8");

      tfKodiPassword = new JPasswordField();
      add(tfKodiPassword, "cell 2 8");
      tfKodiPassword.setColumns(20);
    }
    {
      final JLabel lblUpnpT = new JLabel("UPnP");
      TmmFontHelper.changeFont(lblUpnpT, 1.16667, Font.BOLD);
      add(lblUpnpT, "cell 0 10 3 1");
    }
    {
      chckbxUpnpShareLibrary = new JCheckBox(BUNDLE.getString("Settings.upnp.share")); //$NON-NLS-1$
      add(chckbxUpnpShareLibrary, "cell 1 11 2 1");

      chckbxUpnpRemotePlay = new JCheckBox(BUNDLE.getString("Settings.upnp.play")); //$NON-NLS-1$
      add(chckbxUpnpRemotePlay, "cell 1 12 2 1");
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
    BeanProperty<Settings, String> settingsBeanProperty_2 = BeanProperty.create("kodiUsername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, tfKodiUsername, jTextFieldBeanProperty_1);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_3 = BeanProperty.create("kodiPassword");
    BeanProperty<JPasswordField, String> jPasswordFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JPasswordField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, tfKodiPassword, jPasswordFieldBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_4 = BeanProperty.create("upnpRemotePlay");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxUpnpRemotePlay, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_5 = BeanProperty.create("upnpShareLibrary");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, chckbxUpnpShareLibrary, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_1 = BeanProperty.create("kodiHost");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, tfKodiHost, jTextFieldBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Integer> settingsBeanProperty_6 = BeanProperty.create("kodiHttpPort");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
    AutoBinding<Settings, Integer, JTextField, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, tfKodiHttpPort, jTextFieldBeanProperty_2);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Integer> settingsBeanProperty_7 = BeanProperty.create("kodiTcpPort");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, Integer, JTextField, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, tfKodiTcpPort, jTextFieldBeanProperty_3);
    autoBinding_6.bind();
  }
}
