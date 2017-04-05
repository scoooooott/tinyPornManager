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
package org.tinymediamanager.ui.settings;

import java.awt.Font;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.trakttv.TraktTv;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The class ExternalServicesSettingsPanel. Handle all settings for the external services
 * 
 * @author Manuel Laggner
 */
public class ExternalServicesSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 7266564870819511988L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private JButton                     btnGetTraktPin;
  private JButton                     btnTestTraktConnection;
  private JLabel                      lblTraktStatus;
  private JLabel                      lblTraktDonator;

  public ExternalServicesSettingsPanel() {
    // UI init
    initComponents();

    // data init
    if (StringUtils.isNoneBlank(Globals.settings.getTraktAccessToken(), Globals.settings.getTraktRefreshToken())) {
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.good")); //$NON-NLS-1$
    }
    else {
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.bad")); //$NON-NLS-1$
    }

    btnGetTraktPin.addActionListener(e -> getTraktPin());
    btnTestTraktConnection.addActionListener(e -> {
      try {
        TraktTv.refreshAccessToken();
        JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.testconnection.good"),
            BUNDLE.getString("Settings.trakt.testconnection"), JOptionPane.ERROR_MESSAGE);//$NON-NLS-1$
      }
      catch (Exception e1) {
        JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.testconnection.bad"),
            BUNDLE.getString("Settings.trakt.testconnection"), JOptionPane.ERROR_MESSAGE);//$NON-NLS-1$
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
      JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.getpin.fallback"), //$NON-NLS-1$
          BUNDLE.getString("Settings.trakt.getpin"), JOptionPane.INFORMATION_MESSAGE);
    }

    // let the user insert the pin
    String pin = JOptionPane.showInputDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.getpin.entercode")); //$NON-NLS-1$

    // try to get the tokens
    String accessToken = "";
    String refreshToken = "";
    try {
      Map<String, String> tokens = TraktTv.authenticateViaPin(pin);
      accessToken = tokens.get("accessToken") == null ? "" : tokens.get("accessToken"); //$NON-NLS-1$
      refreshToken = tokens.get("refreshToken") == null ? "" : tokens.get("refreshToken"); //$NON-NLS-1$
    }
    catch (Exception ignored) {
    }

    Globals.settings.setTraktAccessToken(accessToken);
    Globals.settings.setTraktRefreshToken(refreshToken);

    if (StringUtils.isNoneBlank(Globals.settings.getTraktAccessToken(), Globals.settings.getTraktRefreshToken())) {
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.good")); //$NON-NLS-1$
    }
    else {
      JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.getpin.problem"),
          BUNDLE.getString("Settings.trakt.getpin"), JOptionPane.ERROR_MESSAGE);//$NON-NLS-1$
      lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.bad")); //$NON-NLS-1$
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][]", "[][][][]"));
    {
      final JLabel lblTraktT = new JLabel(BUNDLE.getString("Settings.trakt"));
      TmmFontHelper.changeFont(lblTraktT, 1.16667, Font.BOLD);
      add(lblTraktT, "cell 0 0 2 1,growx");
    }
    {
      lblTraktStatus = new JLabel("");
      add(lblTraktStatus, "cell 1 1");
    }
    {
      btnGetTraktPin = new JButton(BUNDLE.getString("Settings.trakt.getpin"));
      add(btnGetTraktPin, "flowx,cell 1 2");

      btnTestTraktConnection = new JButton(BUNDLE.getString("Settings.trakt.testconnection"));
      add(btnTestTraktConnection, "cell 1 2");
    }
    {
      lblTraktDonator = new JLabel("");
      add(lblTraktDonator, "cell 1 3");
    }
  }
}
