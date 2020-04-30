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
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.images.Logo;

import net.miginfocom.swing.MigLayout;

/**
 * The Class AboutDialog.
 * 
 * @author Manuel Laggner
 */
public class AboutDialog extends TmmDialog {
  private static final long   serialVersionUID = 2298570526828925319L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(AboutDialog.class);

  public AboutDialog() {
    super(BUNDLE.getString("tmm.about"), "aboutDialog");

    JPanel contentPanel = new JPanel();
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new MigLayout("", "[][20lp:n][300lp,grow]", "[][10lp:n][][20lp:n][][10lp:n][][][10lp:n][][][][][]"));
    {
      JLabel lblLogo = new JLabel("");
      lblLogo.setIcon(new Logo(96));
      contentPanel.add(lblLogo, "cell 0 0 1 7,alignx left,aligny top");
    }
    {
      JLabel lblTinymediamanager = new JLabel("tinyMediaManager");
      TmmFontHelper.changeFont(lblTinymediamanager, 1.5, Font.BOLD);
      contentPanel.add(lblTinymediamanager, "cell 2 0,alignx center");
    }
    {
      JLabel lblByManuel = new JLabel("\u00A9 2012 - 2020 by Manuel Laggner");
      contentPanel.add(lblByManuel, "cell 2 2,alignx center");
    }
    {
      JLabel lblVersion = new JLabel(BUNDLE.getString("tmm.version") + ": " + ReleaseInfo.getRealVersion());
      contentPanel.add(lblVersion, "cell 2 4");
    }
    {
      JLabel lblBuild = new JLabel(BUNDLE.getString("tmm.builddate") + ": " + ReleaseInfo.getRealBuildDate());
      contentPanel.add(lblBuild, "cell 2 6");
    }
    {
      JLabel lblHomepage = new JLabel(BUNDLE.getString("tmm.homepage"));
      contentPanel.add(lblHomepage, "cell 0 7,alignx right");
    }
    {
      final LinkLabel lblHomepage = new LinkLabel("https://www.tinymediamanager.org/");
      lblHomepage.setLineWrap(false);
      lblHomepage.addActionListener(arg0 -> {
        try {
          TmmUIHelper.browseUrl(lblHomepage.getText());
        }
        catch (Exception e) {
          LOGGER.error(e.getMessage());
          MessageManager.instance.pushMessage(
              new Message(MessageLevel.ERROR, lblHomepage.getText(), "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
        }
      });
      contentPanel.add(lblHomepage, "cell 2 7");
    }
    {
      JLabel lblThanksTo = new JLabel(BUNDLE.getString("tmm.thanksto"));
      contentPanel.add(lblThanksTo, "cell 0 9,alignx right");
    }
    {
      JLabel lblMyronForHelping = new JLabel("Myron for helping me with coding, scrapers, localization, setup, everything...");
      contentPanel.add(lblMyronForHelping, "cell 2 9");
    }
    {
      JLabel lblJoostzilla = new JLabel("Joostzilla for the UI design");
      contentPanel.add(lblJoostzilla, "cell 2 10");
    }
    {
      JLabel lblTranslators = new JLabel("All our translators");
      contentPanel.add(lblTranslators, "cell 2 11");
    }
    {
      JLabel lblLibs = new JLabel("The creators of all libs we've used");
      contentPanel.add(lblLibs, "cell 2 12");
    }
    {
      JLabel lblTester = new JLabel("Everyone who tested and provided feedback");
      contentPanel.add(lblTester, "cell 2 13");
    }
    {
      JButton okButton = new JButton();
      Action action = new CloseAction();
      okButton.setAction(action);
      addDefaultButton(okButton);
    }
  }

  private class CloseAction extends AbstractAction {
    private static final long serialVersionUID = 4652946848116365706L;

    CloseAction() {
      putValue(NAME, BUNDLE.getString("Button.ok"));
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
