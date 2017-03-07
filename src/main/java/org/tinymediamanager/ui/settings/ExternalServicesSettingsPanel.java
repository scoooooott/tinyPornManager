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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.scraper.trakttv.TraktTv;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class ExternalServicesSettingsPanel. Handle all settings for the external services
 * 
 * @author Manuel Laggner
 */
public class ExternalServicesSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 7266564870819511988L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  public ExternalServicesSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));
    {
      JPanel panelTrakttv = new JPanel();
      panelTrakttv.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.trakttv"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
      add(panelTrakttv, "2, 2, fill, fill");
      panelTrakttv.setLayout(new FormLayout(
          new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("max(25dlu;default)"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
              FormSpecs.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

      final JLabel lblTraktStatus = new JLabel(""); //$NON-NLS-1$
      panelTrakttv.add(lblTraktStatus, "2, 2, 5, 1");

      JButton btnGetTraktPin = new JButton(BUNDLE.getString("Settings.trakt.getpin")); //$NON-NLS-1$
      panelTrakttv.add(btnGetTraktPin, "2, 4");

      JButton btnTestTraktConnection = new JButton(BUNDLE.getString("Settings.trakt.testconnection")); //$NON-NLS-1$
      panelTrakttv.add(btnTestTraktConnection, "4, 4");

      if (!Globals.isDonator()) {
        btnGetTraktPin.setEnabled(false);
        btnTestTraktConnection.setEnabled(false);

        String msg = "<html><body>" + BUNDLE.getString("tmm.donatorfunction.hint") + "</body></html>"; //$NON-NLS-1$
        JLabel lblTraktDonator = new JLabel(msg);
        lblTraktDonator.setForeground(Color.RED);
        panelTrakttv.add(lblTraktDonator, "2, 8, 3, 1, default, default");
      }
      else {
        if (StringUtils.isNoneBlank(Globals.settings.getTraktAccessToken(), Globals.settings.getTraktRefreshToken())) {
          lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.good")); //$NON-NLS-1$
        }
        else {
          lblTraktStatus.setText(BUNDLE.getString("Settings.trakt.status.bad")); //$NON-NLS-1$
        }
        btnGetTraktPin.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
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
              accessToken = tokens.get("accessToken") == null ? "" : tokens.get("accessToken");
              refreshToken = tokens.get("refreshToken") == null ? "" : tokens.get("refreshToken");
            }
            catch (Exception e1) {
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
        });
        btnTestTraktConnection.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            try {
              TraktTv.refreshAccessToken();
              JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.testconnection.good"),
                  BUNDLE.getString("Settings.trakt.testconnection"), JOptionPane.INFORMATION_MESSAGE);//$NON-NLS-1$
            }
            catch (Exception e1) {
              JOptionPane.showMessageDialog(MainWindow.getFrame(), BUNDLE.getString("Settings.trakt.testconnection.bad"),
                  BUNDLE.getString("Settings.trakt.testconnection"), JOptionPane.ERROR_MESSAGE);//$NON-NLS-1$
            }
          }
        });
      }
    }
    initDataBindings();

  }

  protected void initDataBindings() {
  }
}
