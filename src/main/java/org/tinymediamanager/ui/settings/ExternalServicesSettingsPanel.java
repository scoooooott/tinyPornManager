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
package org.tinymediamanager.ui.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.thirdparty.trakttv.TraktTv;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class ExternalServicesSettingsPanel. Handle all settings for the external services
 * 
 * @author Manuel Laggner
 */
class ExternalServicesSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 7266564870819511988L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private JButton                     btnGetTraktPin;
  private JButton                     btnTestTraktConnection;
  private JLabel                      lblTraktStatus;

  ExternalServicesSettingsPanel() {
    // UI init
    initComponents();

    // data init
    if (StringUtils.isNoneBlank(Globals.settings.getTraktAccessToken(), Globals.settings.getTraktRefreshToken())) {
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.good"));
    }
    else {
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.bad"));
    }

    btnGetTraktPin.addActionListener(e -> getTraktPin());
    btnTestTraktConnection.addActionListener(e -> {
      try {
        TraktTv.refreshAccessToken();
        JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.testconnection.good"),
            BUNDLE.getString("Settings.trakt.testconnection"), JOptionPane.INFORMATION_MESSAGE);
      }
      catch (Exception e1) {
        JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.testconnection.bad"),
            BUNDLE.getString("Settings.trakt.testconnection"), JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  private void getTraktPin() {
    // open the pin url in a browser
    try {
      TmmUIHelper.browseUrl("https://trakt.tv/pin/799");
    }
    catch (Exception e1) {
      // browser could not be opened, show a dialog box
      JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.getpin.fallback"),
          BUNDLE.getString("Settings.trakt.getpin"), JOptionPane.INFORMATION_MESSAGE);
    }

    // let the user insert the pin
    String pin = JOptionPane.showInputDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.getpin.entercode"));

    // try to get the tokens
    String accessToken = "";
    String refreshToken = "";
    try {
      Map<String, String> tokens = TraktTv.authenticateViaPin(pin);
      accessToken = tokens.get("accessToken") == null ? "" : tokens.get("accessToken");
      refreshToken = tokens.get("refreshToken") == null ? "" : tokens.get("refreshToken");
    }
    catch (Exception ignored) {
    }

    Globals.settings.setTraktAccessToken(accessToken);
    Globals.settings.setTraktRefreshToken(refreshToken);

    if (StringUtils.isNoneBlank(Globals.settings.getTraktAccessToken(), Globals.settings.getTraktRefreshToken())) {
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.good"));
    }
    else {
      JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.getpin.problem"),
          BUNDLE.getString("Settings.trakt.getpin"), JOptionPane.ERROR_MESSAGE);
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.bad"));
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[]"));
    {
      JPanel panelTrakt = SettingsPanelFactory.createSettingsPanel();

      JLabel lblTraktT = new TmmLabel(BUNDLE.getString("Settings.trakt"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelTrakt, lblTraktT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        lblTraktStatus = new JLabel("");
        panelTrakt.add(lblTraktStatus, "cell 1 0 2 1");
      }
      {
        btnGetTraktPin = new JButton(BUNDLE.getString("Settings.trakt.getpin"));
        panelTrakt.add(btnGetTraktPin, "cell 1 1 2 1");

        btnTestTraktConnection = new JButton(BUNDLE.getString("Settings.trakt.testconnection"));
        panelTrakt.add(btnTestTraktConnection, "cell 1 1");
      }
    }
  }
}
