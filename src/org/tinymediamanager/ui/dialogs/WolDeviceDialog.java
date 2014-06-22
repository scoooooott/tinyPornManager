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
package org.tinymediamanager.ui.dialogs;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.WolDevice;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class WolDeviceDialog - to add/edit wol devices
 * 
 * @author Manuel Laggner
 */
public class WolDeviceDialog extends TmmDialog {
  private static final long           serialVersionUID = -8293021735704401080L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private WolDevice                   device           = null;

  private JTextField                  tfName;
  private JTextField                  tfMacAddress;
  private final Action                actionSave       = new SaveAction();
  private final Action                actionCancel     = new CancelAction();

  /**
   * constructor for creating a device
   */
  public WolDeviceDialog() {
    super(BUNDLE.getString("tmm.wakeonlandevice"), "wolDialog"); //$NON-NLS-1$
    setResizable(false);

    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(100px;default)"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(100px;default)"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblDeviceName = new JLabel(BUNDLE.getString("Settings.devicename")); //$NON-NLS-1$
    getContentPane().add(lblDeviceName, "2, 2, right, default");

    tfName = new JTextField();
    getContentPane().add(tfName, "4, 2, 5, 1, fill, default");
    tfName.setColumns(10);

    JLabel lblMacAddress = new JLabel(BUNDLE.getString("Settings.macaddress")); //$NON-NLS-1$
    getContentPane().add(lblMacAddress, "2, 4, right, default");

    tfMacAddress = new JTextField();
    getContentPane().add(tfMacAddress, "4, 4, 5, 1, fill, default");
    tfMacAddress.setColumns(10);

    JButton btnSave = new JButton(BUNDLE.getString("Button.save")); //$NON-NLS-1$  
    btnSave.setAction(actionSave);
    getContentPane().add(btnSave, "6, 6");

    JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    btnCancel.setAction(actionCancel);
    getContentPane().add(btnCancel, "8, 6");
  }

  public void setDevice(WolDevice device) {
    this.device = device;
    this.tfName.setText(device.getName());
    this.tfMacAddress.setText(device.getMacAddress());
  }

  private class SaveAction extends AbstractAction {
    private static final long serialVersionUID = 1740130137146252281L;

    public SaveAction() {
      putValue(NAME, BUNDLE.getString("Button.save")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY);
      putValue(LARGE_ICON_KEY, IconManager.APPLY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // check whether both fields are filled
      if (StringUtils.isBlank(tfName.getText()) || StringUtils.isBlank(tfMacAddress.getText())) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("message.missingitems")); //$NON-NLS-1$
        return;
      }

      // check MAC address with regexp
      Pattern pattern = Pattern.compile("^([0-9a-fA-F]{2}[:-]){5}([0-9a-fA-F]{2})$");
      Matcher matcher = pattern.matcher(tfMacAddress.getText());
      if (!matcher.matches()) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("message.invalidmac")); //$NON-NLS-1$
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

    public CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.CANCEL);
      putValue(LARGE_ICON_KEY, IconManager.CANCEL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
