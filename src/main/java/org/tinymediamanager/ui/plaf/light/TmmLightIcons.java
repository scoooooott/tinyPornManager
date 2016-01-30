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
package org.tinymediamanager.ui.plaf.light;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseIcons;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * @author Manuel Laggner
 * 
 */
public class TmmLightIcons extends BaseIcons {

  public static Icon getCheckBoxIcon() {
    if (checkBoxIcon == null) {
      checkBoxIcon = new SquareCheckBoxIcon();
    }
    return checkBoxIcon;
  }

  public static Icon getCloseIcon() {
    if (closeIcon == null) {
      // closeIcon = new MacCloseIcon();
      closeIcon = new CloseSymbol(new Color(67, 67, 67), null, new Color(134, 134, 134));
    }
    return closeIcon;
  }

  public static Icon getIconIcon() {
    if (iconIcon == null) {
      // iconIcon = new MacIconIcon();
      iconIcon = new IconSymbol(new Color(67, 67, 67), null, new Color(134, 134, 134));
    }
    return iconIcon;
  }

  public static Icon getMaxIcon() {
    if (maxIcon == null) {
      // maxIcon = new MacMaxIcon();
      maxIcon = new MaxSymbol(new Color(67, 67, 67), null, new Color(134, 134, 134));
    }
    return maxIcon;
  }

  public static Icon getMinIcon() {
    if (minIcon == null) {
      // minIcon = new MacMinIcon();
      minIcon = new MaxSymbol(new Color(67, 67, 67), null, new Color(134, 134, 134));
    }
    return minIcon;
  }

  public static Icon getTreeExpandedIcon() {
    if (treeExpandedIcon == null) {
      treeExpandedIcon = new TreeExpandedIcon();
    }
    return treeExpandedIcon;
  }

  public static Icon getTreeCollapsedIcon() {
    if (treeCollapsedIcon == null) {
      treeCollapsedIcon = new TreeCollapsedIcon();
    }
    return treeCollapsedIcon;
  }

  /*
   * private static class RoundCheckBoxIcon implements Icon { private static final int RADIUS = 8; private static final int SELECTED_RADIUS = 4;
   * private static final Color SHADOW_COLOR = new Color(215, 215, 215); private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
   * private static final Color SELECTED_COLOR = new Color(141, 165, 179);
   * 
   * @Override public void paintIcon(Component c, Graphics g, int x, int y) { if (!JTattooUtilities.isLeftToRight(c)) { x += 3; }
   * 
   * AbstractButton b = (AbstractButton) c; ButtonModel model = b.getModel();
   * 
   * Graphics2D g2D = (Graphics2D) g; Object savedRenderingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
   * g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   * 
   * // paint background (and shadow) g.setColor(SHADOW_COLOR); g.fillOval(x, y + 1, 2 * RADIUS, 2 * RADIUS); g.setColor(BACKGROUND_COLOR);
   * g.fillOval(x, y, 2 * RADIUS, 2 * RADIUS);
   * 
   * if (model.isSelected()) { g.setColor(SELECTED_COLOR); g.fillOval(x + RADIUS - SELECTED_RADIUS, y + RADIUS - SELECTED_RADIUS, 2 * SELECTED_RADIUS,
   * 2 * SELECTED_RADIUS); }
   * 
   * g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRenderingHint); }
   * 
   * @Override public int getIconWidth() { return 2 * RADIUS + 2; }
   * 
   * @Override public int getIconHeight() { return 2 * RADIUS; } }
   */

  private static class SquareCheckBoxIcon implements Icon {
    private static final int   SIZE             = 16;
    private static final Color SHADOW_COLOR     = new Color(208, 208, 208);
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);

    private static final Icon  SMALL_CHECK_ICON = new ImageIcon(TmmLightIcons.class.getResource("icons/checkmark.png"));

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (!JTattooUtilities.isLeftToRight(c)) {
        x += 3;
      }

      AbstractButton b = (AbstractButton) c;
      ButtonModel model = b.getModel();

      Graphics2D g2D = (Graphics2D) g;
      Object savedRenderingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // paint background (and shadow)
      g.setColor(SHADOW_COLOR);
      g.fillRoundRect(x + 1, y + 1, SIZE, SIZE, SIZE / 2, SIZE / 2);
      g.setColor(BACKGROUND_COLOR);
      g.fillRoundRect(x, y, SIZE, SIZE, SIZE / 2, SIZE / 2);

      if (model.isSelected()) {
        SMALL_CHECK_ICON.paintIcon(c, g, x, y);
      }

      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRenderingHint);
    }

    @Override
    public int getIconWidth() {
      return SIZE + 2;
    }

    @Override
    public int getIconHeight() {
      return SIZE + 2;
    }
  }

  private static class CloseSymbol implements Icon {
    private Color  foregroundColor         = null;
    private Color  shadowColor             = null;
    private Color  rolloverColor           = null;
    private Color  inactiveForegroundColor = null;
    private Color  inactiveShadowColor     = null;
    private Insets insets                  = new Insets(0, 0, 0, 0);

    public CloseSymbol(Color foregroundColor, Color shadowColor, Color rolloverColor) {
      this.foregroundColor = foregroundColor;
      this.shadowColor = shadowColor;
      this.rolloverColor = rolloverColor;
      this.inactiveForegroundColor = foregroundColor;
      this.inactiveShadowColor = shadowColor;
    }

    @Override
    public int getIconHeight() {
      return 16;
    }

    @Override
    public int getIconWidth() {
      return 16;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2D = (Graphics2D) g;
      g2D.translate(insets.left, insets.top);
      int w = c.getWidth() - insets.left - insets.right;
      int h = c.getHeight() - insets.top - insets.bottom;
      boolean active = JTattooUtilities.isActive((JComponent) c);
      Color color = foregroundColor;
      if (!active) {
        color = inactiveForegroundColor;
      }
      if (c instanceof AbstractButton) {
        if (((AbstractButton) c).getModel().isRollover() && (rolloverColor != null)) {
          color = rolloverColor;
        }
      }
      int lw = (w / 12) + 1;
      int dx = (w / 5) + 1;
      int dy = dx;

      Stroke savedStroke = g2D.getStroke();
      Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2D.setStroke(new BasicStroke(lw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
      if (shadowColor != null) {
        if (!active) {
          g2D.setColor(inactiveShadowColor);
        }
        else {
          g2D.setColor(shadowColor);
        }
        g2D.drawLine(dx + 1, dy + 1, w - dx + 1, h - dy + 1);
        g2D.drawLine(w - dx + 1, dy + 1, dx + 1, h - dy + 1);
      }
      g2D.setColor(color);
      g2D.drawLine(dx, dy, w - dx, h - dy);
      g2D.drawLine(w - dx, dy, dx, h - dy);

      g2D.setStroke(savedStroke);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
      g2D.translate(-insets.left, -insets.top);
    }
  }

  private static class IconSymbol implements Icon {
    private Color  foregroundColor         = null;
    private Color  shadowColor             = null;
    private Color  inactiveForegroundColor = null;
    private Color  inactiveShadowColor     = null;
    private Color  rolloverColor           = null;
    private Insets insets                  = new Insets(0, 0, 0, 0);

    public IconSymbol(Color foregroundColor, Color shadowColor, Color rolloverColor) {
      this.foregroundColor = foregroundColor;
      this.shadowColor = shadowColor;
      this.rolloverColor = rolloverColor;
      this.inactiveForegroundColor = foregroundColor;
      this.inactiveShadowColor = shadowColor;
    }

    @Override
    public int getIconHeight() {
      return 16;
    }

    @Override
    public int getIconWidth() {
      return 16;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2D = (Graphics2D) g;
      g2D.translate(insets.left, insets.top);
      int w = c.getWidth() - insets.left - insets.right;
      int h = c.getHeight() - insets.top - insets.bottom;
      boolean active = JTattooUtilities.isActive((JComponent) c);
      Color color = foregroundColor;
      if (!active) {
        color = inactiveForegroundColor;
      }
      if (c instanceof AbstractButton) {
        if (((AbstractButton) c).getModel().isRollover() && (rolloverColor != null)) {
          color = rolloverColor;
        }
      }
      int lw = (w / 12) + 1;
      int dx = (w / 5) + 1;
      int dy = dx;

      Stroke savedStroke = g2D.getStroke();
      g2D.setStroke(new BasicStroke(lw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
      if (shadowColor != null) {
        if (!active) {
          g2D.setColor(inactiveShadowColor);
        }
        else {
          g2D.setColor(shadowColor);
        }
        g2D.drawLine(dx + 1, h - dy, w - dx + 1, h - dy);
      }
      g2D.setColor(color);
      g2D.drawLine(dx, h - dy - 1, w - dx, h - dy - 1);
      g2D.setStroke(savedStroke);
      g2D.translate(-insets.left, -insets.top);
    }
  }

  private static class MaxSymbol implements Icon {
    private Color  foregroundColor         = null;
    private Color  shadowColor             = null;
    private Color  rolloverColor           = null;
    private Color  inactiveForegroundColor = null;
    private Color  inactiveShadowColor     = null;
    private Insets insets                  = new Insets(0, 0, 0, 0);

    public MaxSymbol(Color foregroundColor, Color shadowColor, Color rolloverColor) {
      this.foregroundColor = foregroundColor;
      this.shadowColor = shadowColor;
      this.rolloverColor = rolloverColor;
      this.inactiveForegroundColor = foregroundColor;
      this.inactiveShadowColor = shadowColor;
    }

    @Override
    public int getIconHeight() {
      return 16;
    }

    @Override
    public int getIconWidth() {
      return 16;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2D = (Graphics2D) g;
      g2D.translate(insets.left, insets.top);
      int w = c.getWidth() - insets.left - insets.right;
      int h = c.getHeight() - insets.top - insets.bottom;
      boolean active = JTattooUtilities.isActive((JComponent) c);
      Color color = foregroundColor;
      if (!active) {
        color = inactiveForegroundColor;
      }
      if (c instanceof AbstractButton) {
        if (((AbstractButton) c).getModel().isRollover() && (rolloverColor != null)) {
          color = rolloverColor;
        }
      }
      int lw = (w / 12);
      int dx = (w / 5) + 1;
      int dy = (h / 5) + 1;

      Stroke savedStroke = g2D.getStroke();
      g2D.setStroke(new BasicStroke(lw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
      if (shadowColor != null) {
        if (!active) {
          g2D.setColor(inactiveShadowColor);
        }
        else {
          g2D.setColor(shadowColor);
        }
        g2D.drawRect(dx + 1, dy + 1, w - (2 * dx), h - (2 * dy));
        g2D.drawLine(dx + 1, dy + lw + 1, w - dx, dy + lw + 1);
      }
      g2D.setColor(color);
      g2D.drawRect(dx, dy, w - (2 * dx), h - (2 * dy));
      g2D.drawLine(dx + 1, dy + lw, w - dx, dy + lw);

      g2D.setStroke(savedStroke);
      g2D.translate(-insets.left, -insets.top);
    }
  }

  private static class TreeCollapsedIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2D = (Graphics2D) g;
      Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Polygon p = new Polygon();
      int size = getIconWidth();
      p.addPoint(x + size / 3, y + size / 6);
      p.addPoint(x + size / 3, y + size - size / 6);
      p.addPoint(x + size - size / 3, y + size / 2);

      g.fillPolygon(p);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
    }

    @Override
    public int getIconWidth() {
      if (AbstractLookAndFeel.getTheme().isSmallFontSize()) {
        return 14;
      }
      else if (AbstractLookAndFeel.getTheme().isMediumFontSize()) {
        return 15;
      }
      else {
        return 18;
      }
    }

    @Override
    public int getIconHeight() {
      if (AbstractLookAndFeel.getTheme().isSmallFontSize()) {
        return 14;
      }
      else if (AbstractLookAndFeel.getTheme().isMediumFontSize()) {
        return 15;
      }
      else {
        return 18;
      }
    }
  }

  private static class TreeExpandedIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2D = (Graphics2D) g;
      Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Polygon p = new Polygon();
      int size = getIconWidth();
      p.addPoint(x + size / 6, y + size / 3);
      p.addPoint(x + size - size / 6, y + size / 3);
      p.addPoint(x + size / 2, y + size - size / 3);

      g.fillPolygon(p);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
    }

    @Override
    public int getIconWidth() {
      if (AbstractLookAndFeel.getTheme().isSmallFontSize()) {
        return 14;
      }
      else if (AbstractLookAndFeel.getTheme().isMediumFontSize()) {
        return 15;
      }
      else {
        return 18;
      }
    }

    @Override
    public int getIconHeight() {
      if (AbstractLookAndFeel.getTheme().isSmallFontSize()) {
        return 14;
      }
      else if (AbstractLookAndFeel.getTheme().isMediumFontSize()) {
        return 15;
      }
      else {
        return 18;
      }
    }
  }
}
