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
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.images.Logo;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class AboutDialog.
 * 
 * @author Manuel Laggner
 */
public class AboutDialog extends TmmDialog {
  private static final long           serialVersionUID = 2298570526828925319L;
  /**
   * @wbp.nls.resourceBundle messages
   */
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
    setBounds(100, 100, 655, 500);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("300dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("max(25px;min)"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            RowSpec.decode("default:grow"), }));
    {
      JLabel lblLogo = new JLabel("");
      lblLogo.setIcon(new Logo(96));
      contentPanel.add(lblLogo, "2, 2, 1, 9, default, top");
    }
    {
      JLabel lblTinymediamanager = new JLabel("tinyMediaManager"); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblTinymediamanager, 1.5, Font.BOLD);
      contentPanel.add(lblTinymediamanager, "4, 2, 3, 1, center, default");
    }
    {
      JLabel lblByManuel = new JLabel("\u00A9 2012 - 2016 by Manuel Laggner"); //$NON-NLS-1$
      contentPanel.add(lblByManuel, "4, 4, 3, 1, center, default");
    }
    {
      if (Globals.isDonator()) {
        Properties p = License.decrypt();
        // p.list(System.out);
        JLabel lblRegged = new JLabel(BUNDLE.getString("tmm.registeredto") + " " + p.getProperty("user")); //$NON-NLS-1$
        TmmFontHelper.changeFont(lblRegged, 1.166, Font.BOLD);
        contentPanel.add(lblRegged, "4, 6, 3, 1, center, default");
      }
    }
    {
      JLabel lblVersion = new JLabel(BUNDLE.getString("tmm.version") + ": " + ReleaseInfo.getRealVersion()); //$NON-NLS-1$
      contentPanel.add(lblVersion, "6, 8, left, top");
    }
    {
      JLabel lblBuild = new JLabel(BUNDLE.getString("tmm.builddate") + ": " + ReleaseInfo.getRealBuildDate());//$NON-NLS-1$
      contentPanel.add(lblBuild, "6, 10, left, top");
    }
    {
      JLabel lblHomepage = new JLabel(BUNDLE.getString("tmm.homepage")); //$NON-NLS-1$
      contentPanel.add(lblHomepage, "2, 12, right, default");
    }
    {
      final LinkLabel lblHomepage = new LinkLabel("http://www.tinymediamanager.org/");
      lblHomepage.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          try {
            TmmUIHelper.browseUrl(lblHomepage.getNormalText());
          }
          catch (Exception e) {
            LOGGER.error(e.getMessage());
            MessageManager.instance.pushMessage(
                new Message(MessageLevel.ERROR, lblHomepage.getNormalText(), "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
          }
        }
      });
      contentPanel.add(lblHomepage, "6, 12");
    }
    {
      JLabel lblThanksTo = new JLabel(BUNDLE.getString("tmm.thanksto")); //$NON-NLS-1$
      contentPanel.add(lblThanksTo, "2, 16, right, default");
    }
    {
      JLabel lblMyronForHelping = new JLabel("Myron for helping me with coding, scrapers, localization, setup, everything...");
      contentPanel.add(lblMyronForHelping, "6, 16");
    }
    {
      JLabel lblXysm = new JLabel("xysm for excessive testing and lots of feedback");
      contentPanel.add(lblXysm, "6, 18");
    }
    {
      JLabel lblXzener = new JLabel("Xzener for genre images");
      contentPanel.add(lblXzener, "6, 20");
    }
    {
      JLabel lblMatthewSandersFor = new JLabel("Matthew Sanders for the cool export templates");
      contentPanel.add(lblMatthewSandersFor, "6, 22");
    }
    {
      JLabel lblLblakya = new JLabel("Akya for the reworked logo");
      contentPanel.add(lblLblakya, "6, 24");
    }
    {
      JPanel panelTranslators = new JPanel();
      contentPanel.add(panelTranslators, "6, 26, fill, fill");
      panelTranslators
          .setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"), },
              new RowSpec[] { RowSpec.decode("top:max(50dlu;pref)"), }));
      {
        JLabel lblTranslatorsT = new JLabel(BUNDLE.getString("tmm.translators")); //$NON-NLS-1$
        panelTranslators.add(lblTranslatorsT, "1, 1, right, top");
      }
      {
        JTextPane tpTranslators = new JTextPane();
        tpTranslators.setBorder(null);
        tpTranslators.setEditable(false);
        tpTranslators.setOpaque(false);
        tpTranslators.setText(TRANSLATORS);
        panelTranslators.add(tpTranslators, "3, 1, fill, fill");
      }
    }
    {
      JLabel lblLibs = new JLabel("The creators of all libs I've used");
      contentPanel.add(lblLibs, "6, 28");
    }
    {
      JLabel lblTester = new JLabel("Everyone who tested and provided feedback");
      contentPanel.add(lblTester, "6, 30");
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(
          new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
              new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("23px"), FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JButton okButton = new JButton();
        okButton.setAction(action);
        buttonPane.add(okButton, "2, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
    }
  }

  @Override
  public void pack() {

  }

  private class SwingAction extends AbstractAction {
    private static final long serialVersionUID = 4652946848116365706L;

    public SwingAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY);
      putValue(LARGE_ICON_KEY, IconManager.APPLY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
