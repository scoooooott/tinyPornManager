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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class WhatsNewDialog. Used to show the user a list of changelogs after each upgrade
 * 
 * @author Manuel Laggner
 */
public class WhatsNewDialog extends JDialog {
  private static final long           serialVersionUID = -4071143363981892283L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public WhatsNewDialog(String changelog) {
    setSize(500, 250);
    setIconImage(Globals.logo);
    setTitle(BUNDLE.getString("whatsnew.title")); //$NON-NLS-1$
    {
      JScrollPane scrollPane = new JScrollPane();
      getContentPane().add(scrollPane, BorderLayout.CENTER);
      JTextPane textPane = new JTextPane();
      textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
      scrollPane.setViewportView(textPane);

      textPane.setContentType("text/html");
      textPane.setText(buildHTMLFromChangelog(changelog));
      // textPane.setText(changelog);
      textPane.setEditable(false);
      textPane.setCaretPosition(0);
    }
    {
      JPanel panel = new JPanel();
      getContentPane().add(panel, BorderLayout.SOUTH);
      panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

      JLabel lblHint = new JLabel(BUNDLE.getString("whatsnew.hint")); //$NON-NLS-1$
      panel.add(lblHint, "2, 2");

      LinkLabel lblLink = new LinkLabel("http://www.tinymediamanager.org");
      lblLink.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          try {
            TmmUIHelper.browseUrl("http://www.tinymediamanager.org/index.php/changelog/");
          }
          catch (Exception e) {
          }
        }
      });
      panel.add(lblLink, "4, 2");

      JButton btnClose = new JButton("Close");
      btnClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          WhatsNewDialog.this.setVisible(false);
          WhatsNewDialog.this.dispose();
        }
      });
      panel.add(btnClose, "8, 2");
    }
  }

  private String buildHTMLFromChangelog(String changelog) {
    StringBuilder changelogInHTML = new StringBuilder(
        "<html><head><style type=\"text/css\">p { text-indent: -10px; padding-left: 10px; margin: 0px; }</style></head><body>");

    for (String line : changelog.split("\\r|\\n|\\r\\n")) {
      changelogInHTML.append("<p>");
      changelogInHTML.append(line);
      changelogInHTML.append("</p>");
    }

    changelogInHTML.append("</body></html>");
    return changelogInHTML.toString();
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension superPref = super.getPreferredSize();
    return new Dimension((int) (700 > superPref.getWidth() ? superPref.getWidth() : 700), (int) (500 > superPref.getHeight() ? superPref.getHeight()
        : 500));
  }

}
