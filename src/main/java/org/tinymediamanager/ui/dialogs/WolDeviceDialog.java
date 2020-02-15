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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.WolDevice;
import org.tinymediamanager.ui.IconManager;

import net.miginfocom.swing.MigLayout;

/**
 * The class WolDeviceDialog - to add/edit wol devices
 * 
 * @author Manuel Laggner
 */
public class WolDeviceDialog extends TmmDialog {
  private static final long serialVersionUID = -8293021735704401080L;

  private WolDevice         device           = null;

  private JTextField        tfName;
  private JTextField        tfMacAddress;

  /**
   * constructor for creating a device
   */
  public WolDeviceDialog() {
    super(BUNDLE.getString("tmm.wakeonlandevice"), "wolDialog");

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][]", "[][]"));

      JLabel lblDeviceName = new JLabel(BUNDLE.getString("Settings.devicename"));
      panelContent.add(lblDeviceName, "cell 0 0,alignx right");

      tfName = new JTextField();
      tfName.setColumns(20);
      panelContent.add(tfName, "cell 1 0");

      JLabel lblMacAddress = new JLabel(BUNDLE.getString("Settings.macaddress"));
      panelContent.add(lblMacAddress, "cell 0 1,alignx right");

      tfMacAddress = new JTextField();
      tfMacAddress.setColumns(20);
      panelContent.add(tfMacAddress, "cell 1 1");
    }
    {
      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
      btnCancel.setAction(new CancelAction());
      addButton(btnCancel);

      JButton btnSave = new JButton(BUNDLE.getString("Button.save"));
      btnSave.setAction(new SaveAction());
      addDefaultButton(btnSave);
    }
  }

  public void setDevice(WolDevice device) {
    this.device = device;
    this.tfName.setText(device.getName());
    this.tfMacAddress.setText(device.getMacAddress());
  }

  private class SaveAction extends AbstractAction {
    private static final long serialVersionUID = 1740130137146252281L;

    SaveAction() {
      putValue(NAME, BUNDLE.getString("Button.save"));
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // check whether both fields are filled
      if (StringUtils.isBlank(tfName.getText()) || StringUtils.isBlank(tfMacAddress.getText())) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("message.missingitems"));
        return;
      }

      // check MAC address with regexp
      Pattern pattern = Pattern.compile("^([0-9a-fA-F]{2}[:-]){5}([0-9a-fA-F]{2})$");
      Matcher matcher = pattern.matcher(tfMacAddress.getText());
      if (!matcher.matches()) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("message.invalidmac"));
        return;
      }

      // create a new WOL device
      if (device == null) {
        device = new WolDevice();
        Globals.settings.addWolDevice(device);
      }

      device.setName(tfName.getText());
      device.setMacAddress(tfMacAddress.getText());

      setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -8416641526799936831L;

    CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel"));
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
      putValue(LARGE_ICON_KEY, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
