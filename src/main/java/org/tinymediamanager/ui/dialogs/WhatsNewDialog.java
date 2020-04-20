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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.LinkLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class WhatsNewDialog. Used to show the user a list of changelogs after each upgrade
 * 
 * @author Manuel Laggner
 */
public class WhatsNewDialog extends TmmDialog {
  private static final long   serialVersionUID = -4071143363981892283L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(WhatsNewDialog.class);

  public WhatsNewDialog(String changelog) {
    super(BUNDLE.getString("whatsnew.title"), "whatsnew");
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[600lp,grow]", "[400lp,grow][]"));

      JScrollPane scrollPane = new JScrollPane();
      panelContent.add(scrollPane, "cell 0 0,grow");

      JTextPane textPane = new JTextPane();
      textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Globals.settings.getFontSize() + 1));
      scrollPane.setViewportView(textPane);

      textPane.setContentType("text/html");
      textPane.setText(prepareTextAsHtml(changelog));
      textPane.setEditable(false);
      textPane.setCaretPosition(0);
      textPane.addHyperlinkListener(hle -> {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
          try {
            TmmUIHelper.browseUrl(hle.getURL().toString());
          }
          catch (Exception e) {
            LOGGER.error("error browsing to " + hle.getURL().toString() + " :" + e.getMessage());
          }
        }
      });

      JLabel lblHint = new JLabel(BUNDLE.getString("whatsnew.hint"));
      panelContent.add(lblHint, "flowx,cell 0 1");

      LinkLabel lblLink = new LinkLabel("https://www.tinymediamanager.org");
      lblLink.addActionListener(arg0 -> {
        try {
          TmmUIHelper.browseUrl("https://www.tinymediamanager.org/changelog/");
        }
        catch (Exception ignored) {
        }
      });
      panelContent.add(lblLink, "cell 0 1, growx");
    }
    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
      btnClose.addActionListener(arg0 -> setVisible(false));
      addDefaultButton(btnClose);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension superPref = super.getPreferredSize();
    return new Dimension((int) (700 > superPref.getWidth() ? superPref.getWidth() : 700),
        (int) (500 > superPref.getHeight() ? superPref.getHeight() : 500));
  }

  private String prepareTextAsHtml(String originalText) {
    Pattern pattern = Pattern.compile("(http[s]?://.*?)[\\n\\r\\s)]");
    Matcher matcher = pattern.matcher(originalText);
    while (matcher.find()) {
      originalText = originalText.replace(matcher.group(1), "<a href=\"" + matcher.group(1) + "\">" + matcher.group(1) + "</a>");
    }

    // set the foreground color of the content
    Color foreground = UIManager.getColor("TextPane.foreground");
    String color = Integer.toHexString(foreground.getRed()) + Integer.toHexString(foreground.getGreen()) + Integer.toHexString(foreground.getBlue());

    return "<html><pre style=\"color: #" + color + "\">" + originalText + "</pre><html>";
  }

  public static void showChangelog() {
    try {
      final String changelog = Utils.readFileToString(Paths.get("changelog.txt"));
      if (StringUtils.isNotBlank(changelog)) {
        EventQueue.invokeLater(() -> {
          WhatsNewDialog dialog = new WhatsNewDialog(changelog);
          dialog.setVisible(true);
        });
      }
    }
    catch (IOException e) {
      // no file found
      LOGGER.warn(e.getMessage());
    }
  }
}
