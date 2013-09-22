/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.WolDevice;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.WolDeviceDialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The ExternalDevicesSettingsPanel - a panel to configure external devices
 * 
 * @author Manuel Laggner
 */
public class ExternalDevicesSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 8176824801347872222L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  private JTable                      tableWolDevices;

  public ExternalDevicesSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelWol = new JPanel();
    panelWol.setBorder(new TitledBorder(null, "Wake on LAN", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelWol, "2, 2, fill, fill");
    panelWol.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(50dlu;default):grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(100px;default)"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("max(30dlu;default)"), }));

    JScrollPane spWolDevices = new JScrollPane();
    panelWol.add(spWolDevices, "2, 2, 1, 5, fill, fill");

    tableWolDevices = new JTable();
    spWolDevices.setViewportView(tableWolDevices);

    JButton btnAddWolDevice = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddWolDevice.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        WolDeviceDialog dialog = new WolDeviceDialog();
        dialog.pack();
        dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        dialog.setVisible(true);
      }
    });
    panelWol.add(btnAddWolDevice, "4, 2, fill, default");

    JButton btnEditWolDevice = new JButton(BUNDLE.getString("Button.edit")); //$NON-NLS-1$
    btnEditWolDevice.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
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

      }
    });
    panelWol.add(btnEditWolDevice, "4, 4");

    JButton btnRemoveWolDevice = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveWolDevice.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int row = tableWolDevices.getSelectedRow();
        row = tableWolDevices.convertRowIndexToModel(row);
        if (row != -1) {
          WolDevice device = Globals.settings.getWolDevices().get(row);
          Globals.settings.removeWolDevice(device);
        }
      }
    });
    panelWol.add(btnRemoveWolDevice, "4, 6, fill, top");
    initDataBindings();

    // set column titles
    tableWolDevices.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("Settings.devicename")); //$NON-NLS-1$
    tableWolDevices.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("Settings.macaddress")); //$NON-NLS-1$
  }

  protected void initDataBindings() {
    BeanProperty<Settings, List<WolDevice>> settingsBeanProperty = BeanProperty.create("wolDevices");
    JTableBinding<WolDevice, Settings, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, tableWolDevices);
    jTableBinding.setEditable(false);
    //
    BeanProperty<WolDevice, String> wolBeanProperty_1 = BeanProperty.create("name");
    jTableBinding.addColumnBinding(wolBeanProperty_1);
    //
    BeanProperty<WolDevice, String> wolBeanProperty_2 = BeanProperty.create("macAddress");
    jTableBinding.addColumnBinding(wolBeanProperty_2);
    //
    jTableBinding.bind();
  }
}
