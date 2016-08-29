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
package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.License;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.images.Logo;

import net.miginfocom.swing.MigLayout;

/**
 * The Class AboutDialog.
 * 
 * @author Manuel Laggner
 */
public class AboutDialog extends TmmDialog {
  private static final long           serialVersionUID = 2298570526828925319L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());                                                     //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(AboutDialog.class);
  private static final String         TRANSLATORS      = "Joostzilla, Zagoslav, zbynek.fiala, roliverosc, roandr, Andrey Gorodnov, julienbloch, nerve, carlosmarchi, "
      + "espiman, beonex, otefenli, sxczmnb, piodio, peppe_sr, szobidani, kriss1981, mrj, xsintive, Gam, ppanhh, SeNmaN, Translador, Deleuze23, "
      + "ShevAbam, abrupt_neurosis, lynxstrike, Spegni, carfesh, vekheoqf, keleniki, htrex, namuit, stickell, Voltinus, Zwanzig, vipkoza"
      + "Amarante.pt_BR, TaniaC, maopequena, leandrofuscaldi, dukobpa3, bleuge";

  private final JPanel                contentPanel     = new JPanel();
  private final Action                action           = new SwingAction();

  public AboutDialog() {
    super(BUNDLE.getString("tmm.about"), "aboutDialog"); //$NON-NLS-1$
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new MigLayout("", "[][pref!][][pref!][grow]", "[][6px][][pref!][][6px][][][][][][][][]"));
    {
      JLabel lblLogo = new JLabel("");
      lblLogo.setIcon(new Logo(96));
      contentPanel.add(lblLogo, "cell 0 0 1 7,alignx left,aligny top");
    }
    {
      JLabel lblTinymediamanager = new JLabel("tinyMediaManager"); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblTinymediamanager, 1.5, Font.BOLD);
      contentPanel.add(lblTinymediamanager, "cell 2 0 3 1,alignx center");
    }
    {
      JLabel lblByManuel = new JLabel("\u00A9 2012 - 2015 by Manuel Laggner"); //$NON-NLS-1$
      contentPanel.add(lblByManuel, "cell 2 2 3 1,alignx center");
    }
    {
      if (Globals.isDonator()) {
        Properties p = License.decrypt();
        JLabel lblRegged = new JLabel(BUNDLE.getString("tmm.registeredto") + " " + p.getProperty("user")); //$NON-NLS-1$
        TmmFontHelper.changeFont(lblRegged, 1.166, Font.BOLD);
        contentPanel.add(lblRegged, "cell 4 4");
      }
    }
    {
      JLabel lblVersion = new JLabel(BUNDLE.getString("tmm.version") + ": " + ReleaseInfo.getRealVersion()); //$NON-NLS-1$
      contentPanel.add(lblVersion, "cell 2 4");
    }
    {
      JLabel lblBuild = new JLabel(BUNDLE.getString("tmm.builddate") + ": " + ReleaseInfo.getRealBuildDate());//$NON-NLS-1$
      contentPanel.add(lblBuild, "cell 2 6");
    }
    {
      JLabel lblHomepage = new JLabel(BUNDLE.getString("tmm.homepage")); //$NON-NLS-1$
      contentPanel.add(lblHomepage, "cell 0 7,alignx right");
    }
    {
      final LinkLabel lblHomepage = new LinkLabel("http://www.tinymediamanager.org/"); //$NON-NLS-1$
      lblHomepage.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          try {
            TmmUIHelper.browseUrl(lblHomepage.getNormalText());
          }
          catch (Exception e) {
            LOGGER.error(e.getMessage());
            MessageManager.instance.pushMessage(
                new Message(MessageLevel.ERROR, lblHomepage.getNormalText(), "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() })); //$NON-NLS-1$
          }
        }
      });
      contentPanel.add(lblHomepage, "cell 2 7 3 1");
    }
    {
      JLabel lblThanksTo = new JLabel(BUNDLE.getString("tmm.thanksto")); //$NON-NLS-1$
      contentPanel.add(lblThanksTo, "cell 0 8,alignx right");
    }
    {
      JLabel lblMyronForHelping = new JLabel("Myron for helping me with coding, scrapers, localization, setup, everything..."); //$NON-NLS-1$
      contentPanel.add(lblMyronForHelping, "cell 2 8 3 1");
    }
    {
      JLabel lblXysm = new JLabel("xysm for excessive testing and lots of feedback"); //$NON-NLS-1$
      contentPanel.add(lblXysm, "cell 2 9 3 1");
    }
    {
      JLabel lblMatthewSandersFor = new JLabel("Matthew Sanders for the cool export templates"); //$NON-NLS-1$
      contentPanel.add(lblMatthewSandersFor, "cell 2 10 3 1");
    }
    {
      JPanel panelTranslators = new JPanel();
      contentPanel.add(panelTranslators, "cell 2 11 3 1,grow");
      panelTranslators.setLayout(new MigLayout("insets 0", "[][300px:300px,grow]", "[]"));
      {
        JLabel lblTranslatorsT = new JLabel(BUNDLE.getString("tmm.translators")); //$NON-NLS-1$
        panelTranslators.add(lblTranslatorsT, "cell 0 0,alignx right,aligny top");
      }
      {
        JTextPane tpTranslators = new JTextPane();
        tpTranslators.setBorder(null);
        tpTranslators.setEditable(false);
        tpTranslators.setOpaque(false);
        tpTranslators.setText(TRANSLATORS);
        panelTranslators.add(tpTranslators, "cell 1 0,growx,aligny top");
      }
    }
    {
      JLabel lblLibs = new JLabel("The creators of all libs I've used"); //$NON-NLS-1$
      contentPanel.add(lblLibs, "cell 2 12 3 1");
    }
    {
      JLabel lblTester = new JLabel("Everyone who tested and provided feedback"); //$NON-NLS-1$
      contentPanel.add(lblTester, "cell 2 13 3 1");
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new MigLayout("", "[grow][]", "[]"));
      {
        JButton okButton = new JButton();
        okButton.setAction(action);
        buttonPane.add(okButton, "cell 1 0,growx");
        getRootPane().setDefaultButton(okButton);
      }
    }
  }

  private class SwingAction extends AbstractAction {
    private static final long serialVersionUID = 4652946848116365706L;

    public SwingAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
