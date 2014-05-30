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
package org.tinymediamanager.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * The Class VerticalTextIcon.
 * 
 * @author Santhosh Kumar
 */
public class VerticalTextIcon extends JComponent implements Icon, SwingConstants {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The fm. */
  private FontMetrics       fm;

  /** The text. */
  private String            text;

  /** The height. */
  private int               width, height;

  /** The clockwize. */
  private boolean           clockwize;

  /**
   * Instantiates a new vertical text icon.
   * 
   * @param text
   *          the text
   * @param clockwize
   *          the clockwize
   */
  public VerticalTextIcon(String text, boolean clockwize) {
    Font font = UIManager.getFont("Label.font");
    fm = getFontMetrics(font);

    this.text = text;
    width = SwingUtilities.computeStringWidth(fm, text);
    height = fm.getHeight();
    this.clockwize = clockwize;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g;
    Color oldColor = g.getColor();
    AffineTransform oldTransform = g2.getTransform();

    Object oldAAValue = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    g.setColor(Color.black);
    if (clockwize) {
      g2.translate(x + getIconWidth(), y);
      g2.rotate(Math.PI / 2);
    }
    else {
      g2.translate(x, y + getIconHeight());
      g2.rotate(-Math.PI / 2);
    }

    // try to paint the text via Swingutilities
    try {
      Class swingUtilities2Class = Class.forName("sun.swing.SwingUtilities2");
      Class classParams[] = { JComponent.class, Graphics.class, String.class, Integer.TYPE, Integer.TYPE };
      Method m = swingUtilities2Class.getMethod("drawString", classParams);
      Object methodParams[] = { c, g, text, Integer.valueOf(0), Integer.valueOf(fm.getLeading() + fm.getAscent()) };
      m.invoke(null, methodParams);
    }
    catch (Exception ex) {
      g.drawString(text, 0, fm.getLeading() + fm.getAscent());
    }
    // g.drawString(text, 0, fm.getLeading() + fm.getAscent());

    g.setColor(oldColor);
    g2.setTransform(oldTransform);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAAValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.Icon#getIconWidth()
   */
  public int getIconWidth() {
    return height;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.Icon#getIconHeight()
   */
  public int getIconHeight() {
    return width;
  }

  /**
   * Creates the tabbed pane.
   * 
   * @param tabPlacement
   *          the tab placement
   * @return the j tabbed pane
   */
  public static JTabbedPane createTabbedPane(int tabPlacement) {
    switch (tabPlacement) {
      case JTabbedPane.LEFT:
      case JTabbedPane.RIGHT:
        Object textIconGap = UIManager.get("TabbedPane.textIconGap");
        Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
        UIManager.put("TabbedPane.textIconGap", Integer.valueOf(1));
        UIManager.put("TabbedPane.tabInsets", new Insets(tabInsets.left, tabInsets.top, tabInsets.right, tabInsets.bottom));
        JTabbedPane tabPane = new JTabbedPane(tabPlacement);
        UIManager.put("TabbedPane.textIconGap", textIconGap);
        UIManager.put("TabbedPane.tabInsets", tabInsets);
        return tabPane;
      default:
        return new JTabbedPane(tabPlacement);
    }
  }

  /**
   * Adds the tab.
   * 
   * @param tabPane
   *          the tab pane
   * @param text
   *          the text
   * @param comp
   *          the comp
   */
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