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

package org.tinymediamanager.ui.plaf;

import static org.tinymediamanager.ui.plaf.TmmTheme.FONT_AWESOME;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseIcons;
import com.jtattoo.plaf.JTattooUtilities;

public class TmmIcons extends BaseIcons {

  public static Color           COLOR       = Color.GRAY;
  public static Color           COLOR_HOVER = Color.WHITE;

  public static final ImageIcon EMPTY_IMAGE = new ImageIcon(TmmIcons.class.getResource("empty.png"));

  public static Icon getCloseIcon() {
    if (closeIcon == null) {
      closeIcon = new CloseSymbol(COLOR, null, COLOR_HOVER);
    }
    return closeIcon;
  }

  public static Icon getIconIcon() {
    if (iconIcon == null) {
      iconIcon = new IconSymbol(COLOR, null, COLOR_HOVER);
    }
    return iconIcon;
  }

  public static Icon getMaxIcon() {
    if (maxIcon == null) {
      maxIcon = new MaxSymbol(COLOR, null, COLOR_HOVER);
    }
    return maxIcon;
  }

  public static Icon getMinIcon() {
    if (minIcon == null) {
      minIcon = new MaxSymbol(COLOR, null, COLOR_HOVER);
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

  public static Icon getCheckBoxIcon() {
    if (checkBoxIcon == null) {
      checkBoxIcon = new SquareCheckBoxIcon();
    }
    return checkBoxIcon;
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

  private static class SquareCheckBoxIcon implements Icon {
    private static final int       SIZE             = AbstractLookAndFeel.getDefaultFontSize() + 4;
    private static final ImageIcon SMALL_CHECK_ICON = createFontAwesomeIcon('\uF00C', AbstractLookAndFeel.getTheme().getFocusColor());
    private static final ImageIcon TRI_STATE_ICON   = createFontAwesomeIcon('\uF068', AbstractLookAndFeel.getTheme().getFocusColor());

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
      g.setColor(AbstractLookAndFeel.getTheme().getBackgroundColorDark());
      g.fillRoundRect(x + 1, y + 2, SIZE, SIZE, SIZE / 2, SIZE / 2);
      g.setColor(AbstractLookAndFeel.getTheme().getInputBackgroundColor());
      g.fillRoundRect(x, y + 1, SIZE, SIZE, SIZE / 2, SIZE / 2);

      Icon icon = null;
      int offsetX = 0;
      if (isTriStateButtonModelStatusMixed(model)) {
        icon = TRI_STATE_ICON;
        offsetX = 2;
      }
      else if (model.isSelected()) {
        icon = SMALL_CHECK_ICON;
      }

      if (icon != null) {
        if (!model.isEnabled()) {
          icon = new ImageIcon(GrayFilter.createDisabledImage(SMALL_CHECK_ICON.getImage()));
        }

        icon.paintIcon(c, g, x + offsetX, y + 1);
      }

      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRenderingHint);
    }

    private boolean isTriStateButtonModelStatusMixed(ButtonModel model) {
      if ("TriStateButtonModel".equals(model.getClass().getSimpleName())) {
        // check the model state via reflection
        try {
          Method method = model.getClass().getMethod("isMixed");
          if ((boolean) method.invoke(model)) {
            return true;
          }
        }
        catch (Exception ignored) {
        }
      }
      return false;
    }

    @Override
    public int getIconWidth() {
      return Math.max(SMALL_CHECK_ICON.getIconWidth(), TRI_STATE_ICON.getIconWidth());
    }

    @Override
    public int getIconHeight() {
      return Math.max(SMALL_CHECK_ICON.getIconHeight(), TRI_STATE_ICON.getIconHeight());
    }
  }

  /**
   * create a image off the font awesome icon font in the default size 14px for 12pt base font size.
   *
   * @param iconId
   *          the icon id
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId) {
    return createFontAwesomeIcon(iconId, calculateFontIconSize(1.1667f), UIManager.getColor("Label.foreground"));
  }

  private static int calculateFontIconSize(float scaleFactor) {
    try {
      return (int) Math.floor(AbstractLookAndFeel.getDefaultFontSize() * scaleFactor);
    }
    catch (Exception e) {
      return 12;
    }

  }

  /**
   * create a image off the font awesome icon font in given size (scaling to the base font size of 12pt applied!)
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, int size) {
    return createFontAwesomeIcon(iconId, calculateFontIconSize(size / 12.0f), UIManager.getColor("Label.foreground"));
  }

  /**
   * create a image off the awesome icon font with the given scaling factor
   *
   * @param iconId
   *          the icon id
   * @param scaleFactor
   *          the scale factor to apply
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, float scaleFactor) {
    return createFontAwesomeIcon(iconId, calculateFontIconSize(scaleFactor), UIManager.getColor("Label.foreground"));
  }

  /**
   * create a image off the awesome icon font size 14pt for 12pt base font size.
   *
   * @param iconId
   *          the icon id
   * @param color
   *          the color to create the icon in
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, Color color) {
    return createFontAwesomeIcon(iconId, calculateFontIconSize(1.1667f), color);
  }

  /**
   * create a image off the awesome icon font
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @param color
   *          the color to create the icon in
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, int size, Color color) {
    if (FONT_AWESOME == null) {
      return EMPTY_IMAGE;
    }
    Font font = FONT_AWESOME.deriveFont((float) size);
    return createFontIcon(font, String.valueOf(iconId), color);
  }

  /**
   * create a text icon in the default Label.foreground color
   *
   * @param text
   *          the text to be painted
   * @param size
   *          the text size
   * @return an icon containing the text
   */
  public static ImageIcon createTextIcon(String text, int size) {
    return createTextIcon(text, size, UIManager.getColor("Label.foreground"));
  }

  /**
   * create a text icon in the given color
   *
   * @param text
   *          the text to be painted
   * @param size
   *          the text size
   * @param color
   *          the color to draw in
   * @return an icon containing the text
   */
  public static ImageIcon createTextIcon(String text, int size, Color color) {
    Font defaultfont = (Font) UIManager.get("Label.font");
    if (defaultfont == null) {
      return null;
    }
    Font font = defaultfont.deriveFont(Font.BOLD, (float) size);
    return createFontIcon(font, text, color);
  }

  /**
   * create a font icon - draw an icon off a font with the given text/character
   *
   * @param font
   *          the font to be used
   * @param text
   *          the text to be painted
   * @param color
   *          the color to draw in
   * @return an icon containing the text
   */
  public static ImageIcon createFontIcon(Font font, String text, Color color) {
    try {
      // calculate icon size
      BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(tmp);
      g2.setFont(font);

      // get the visual bounds of the string (this is more realiable than the string bounds)
      Rectangle2D defaultBounds = g2.getFontMetrics().getStringBounds("M", g2);
      Rectangle2D bounds = font.createGlyphVector(g2.getFontRenderContext(), text).getVisualBounds();
      int iconWidth = (int) Math.ceil(bounds.getWidth()) + 2; // +2 to avoid clipping problems
      int iconHeight = (int) Math.ceil(bounds.getHeight()) + 2; // +2 to avoid clipping problems

      if (iconHeight < defaultBounds.getHeight()) {
        iconHeight = (int) Math.ceil(defaultBounds.getHeight());
      }

      g2.dispose();

      // if width is less than height, increase the width to be at least a square
      if (iconWidth < iconHeight) {
        iconWidth = iconHeight;
      }

      // and draw it
      BufferedImage buffer = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB);
      g2 = (Graphics2D) buffer.getGraphics();
      // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      // g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
      if (desktopHints != null) {
        g2.setRenderingHints(desktopHints);
      }

      g2.setFont(font);
      g2.setColor(color);

      // draw the glyhps centered
      int y = (int) Math.floor(bounds.getY() - (defaultBounds.getHeight() - bounds.getHeight()) / 2);
      g2.drawString(text, (int) ((iconWidth - Math.ceil(bounds.getWidth())) / 2), -y);
      g2.dispose();
      return new ImageIcon(buffer);
    }
    catch (Exception ignored) {
    }

    return EMPTY_IMAGE;
  }
}
