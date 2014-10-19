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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class UpdateDialog. Used to show the user that an update is available
 * 
 * @author Manuel Laggner
 */
public class UpdateDialog extends TmmDialog {
  private static final long           serialVersionUID = 535315282932742179L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(UpdateDialog.class);

  public UpdateDialog(String changelog) {
    super(BUNDLE.getString("tmm.update.title"), "update"); //$NON-NLS-1$
    setSize(500, 250);
    {
      JPanel panel = new JPanel();
      getContentPane().add(panel, BorderLayout.CENTER);
      panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.PARAGRAPH_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_ROWSPEC, }));

      JTextPane lblUpdateInfo = new JTextPane();
      lblUpdateInfo.setOpaque(false);
      lblUpdateInfo.setText(BUNDLE.getString("tmm.update.message")); //$NON-NLS-1$
      panel.add(lblUpdateInfo, "2, 2, fill, default");

      JLabel lblChangelog = new JLabel(BUNDLE.getString("whatsnew.title")); //$NON-NLS-1$
      panel.add(lblChangelog, "2, 4, fill, default");

      JScrollPane scrollPane = new JScrollPane();
      panel.add(scrollPane, "2, 6, fill, fill");
      JTextPane textPane = new JTextPane();
      textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Globals.settings.getFontSize() + 1));
      scrollPane.setViewportView(textPane);

      textPane.setContentType("text/html");
      textPane.setText(prepareTextAsHtml(changelog));
      textPane.setEditable(false);
      textPane.setCaretPosition(0);
      textPane.addHyperlinkListener(new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent hle) {
          if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
            try {
              TmmUIHelper.browseUrl(hle.getURL().toString());
            }
            catch (Exception e) {
              LOGGER.error("error browsing to " + hle.getURL().toString() + " :" + e.getMessage());
            }
          }
        }
      });
    }
    {
      JPanel panel = new JPanel();
      getContentPane().add(panel, BorderLayout.SOUTH);
      panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

      JPanel buttonPanel = new JPanel();
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      buttonPanel.setLayout(layout);
      panel.add(buttonPanel, "8, 2");

      JButton btnUpdate = new JButton(BUNDLE.getString("Button.update")); //$NON-NLS-1$
      btnUpdate.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          setVisible(false);
          LOGGER.info("Updating...");

          // spawn getdown and exit TMM
          MainWindow.getActiveInstance().closeTmmAndStart(Utils.getPBforTMMupdate());
        }
      });
      buttonPanel.add(btnUpdate);

      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          setVisible(false);
        }
      });
      buttonPanel.add(btnClose);
    }

  }

  @Override
  public Dimension getPreferredSize() {
    Dimension superPref = super.getPreferredSize();
    return new Dimension((int) (700 > superPref.getWidth() ? superPref.getWidth() : 700), (int) (500 > superPref.getHeight() ? superPref.getHeight()
        : 500));
  }

  private String prepareTextAsHtml(String originalText) {
    Pattern pattern = Pattern.compile("(http[s]?://.*?)[ )]");
    Matcher matcher = pattern.matcher(originalText);
    while (matcher.find()) {
      originalText = originalText.replace(matcher.group(1), "<a href=\"" + matcher.group(1) + "\">" + matcher.group(1) + "</a>");
    }

    return "<html><pre>" + originalText + "</pre><html>";
  }
}
