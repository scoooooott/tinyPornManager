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
package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class AboutDialog.
 * 
 * @author Manuel Laggner
 */
public class AboutDialog extends JDialog {
  private static final long           serialVersionUID = 2298570526828925319L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(AboutDialog.class);

  private final JPanel                contentPanel     = new JPanel();
  private final Action                action           = new SwingAction();

  /**
   * Create the dialog.
   */
  public AboutDialog() {
    setTitle(BUNDLE.getString("tmm.about")); //$NON-NLS-1$
    setName("aboutDialog");
    setResizable(false);
    setModal(true);
    setBounds(100, 100, 643, 431);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("center:89px"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(25px;min)"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    {
      JLabel lblLogo = new JLabel("");
      lblLogo.setIcon(new ImageIcon(AboutDialog.class.getResource("/org/tinymediamanager/ui/images/tmm96.png")));
      contentPanel.add(lblLogo, "2, 2, 1, 9, default, top");
    }
    {
      JLabel lblTinymediamanager = new JLabel("tinyMediaManager");
      lblTinymediamanager.setFont(new Font("Dialog", Font.BOLD, 18));
      contentPanel.add(lblTinymediamanager, "4, 2, 3, 1, center, default");
    }
    {
      JLabel lblByManuel = new JLabel("\u00A9 2012 - 2013 by Manuel Laggner");
      contentPanel.add(lblByManuel, "4, 4, 3, 1, center, default");
    }
    {
      JLabel lblVersion = new JLabel("Version: " + ReleaseInfo.getRealVersion());
      contentPanel.add(lblVersion, "6, 8, left, top");
    }
    {
      JLabel lblBuild = new JLabel("Build date: " + ReleaseInfo.getRealBuildDate());
      contentPanel.add(lblBuild, "6, 10, left, top");
    }
    {
      JLabel lblHomepage = new JLabel("Homepage");
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
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, lblHomepage.getNormalText(), "message.erroropenurl", new String[] {
                ":", e.getLocalizedMessage() }));
          }
        }
      });
      contentPanel.add(lblHomepage, "6, 12");
    }
    {
      JLabel lblThanksTo = new JLabel("Thanks to");
      contentPanel.add(lblThanksTo, "2, 16, right, default");
    }
    {
      JLabel lblMyronForHelping = new JLabel("Myron for helping me with coding, scrapers, localization, setup, builds...");
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
      JLabel lblXzener = new JLabel("Our translators: xsintive, kriss1981, Joostzilla, zbynek.fiala, carlosmarchi");
      contentPanel.add(lblXzener, "6, 24");
    }
    {
      JLabel lblLibs = new JLabel("The creators of all libs I've used");
      contentPanel.add(lblLibs, "6, 26");
    }
    {
      JLabel lblTester = new JLabel("Everyone who tested and provided feedback");
      contentPanel.add(lblTester, "6, 28");
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.BUTTON_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("23px"), FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JButton okButton = new JButton();
        okButton.setAction(action);
        buttonPane.add(okButton, "2, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
    }

    pack();
  }

  /**
   * The Class SwingAction.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action.
     */
    public SwingAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
