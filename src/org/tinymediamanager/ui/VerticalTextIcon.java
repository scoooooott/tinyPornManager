/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author Santhosh Kumar
 * 
 */
public class VerticalTextIcon extends JComponent implements Icon, SwingConstants {
  private Font        font;
  private FontMetrics fm;

  private String      text;
  private int         width, height;
  private boolean     clockwize;

  public VerticalTextIcon(String text, boolean clockwize) {
    Font labelFont = UIManager.getFont("Label.font");
    Map<TextAttribute, Serializable> textAttributes = new HashMap<TextAttribute, Serializable>();
    textAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    textAttributes.put(TextAttribute.FONT, labelFont);
    font = Font.getFont(textAttributes);
    fm = getFontMetrics(font);

    this.text = text;
    width = SwingUtilities.computeStringWidth(fm, text);
    height = fm.getHeight();
    this.clockwize = clockwize;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g;
    Font oldFont = g.getFont();
    Color oldColor = g.getColor();
    AffineTransform oldTransform = g2.getTransform();

    // Object oldAAValue =
    // g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
    // RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
    // RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    g.setFont(font);
    g.setColor(Color.black);
    if (clockwize) {
      g2.translate(x + getIconWidth(), y);
      g2.rotate(Math.PI / 2);
    }
    else {
      g2.translate(x, y + getIconHeight());
      g2.rotate(-Math.PI / 2);
    }

    g.drawString(text, 0, fm.getLeading() + fm.getAscent());

    g.setFont(oldFont);
    g.setColor(oldColor);
    g2.setTransform(oldTransform);
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAAValue);
  }

  public int getIconWidth() {
    return height;
  }

  public int getIconHeight() {
    return width;
  }

  public static JTabbedPane createTabbedPane(int tabPlacement) {
    switch (tabPlacement) {
      case JTabbedPane.LEFT:
      case JTabbedPane.RIGHT:
        Object textIconGap = UIManager.get("TabbedPane.textIconGap");
        Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
        UIManager.put("TabbedPane.textIconGap", new Integer(1));
        UIManager.put("TabbedPane.tabInsets", new Insets(tabInsets.left, tabInsets.top, tabInsets.right, tabInsets.bottom));
        JTabbedPane tabPane = new JTabbedPane(tabPlacement);
        UIManager.put("TabbedPane.textIconGap", textIconGap);
        UIManager.put("TabbedPane.tabInsets", tabInsets);
        return tabPane;
      default:
        return new JTabbedPane(tabPlacement);
    }
  }

  public static void addTab(JTabbedPane tabPane, String text, Component comp) {
    int tabPlacement = tabPane.getTabPlacement();
    switch (tabPlacement) {
      case JTabbedPane.LEFT:
      case JTabbedPane.RIGHT:
        tabPane.addTab(null, new VerticalTextIcon(text, tabPlacement == JTabbedPane.RIGHT), comp);
        return;
      default:
        tabPane.addTab(text, null, comp);
    }
  }
}