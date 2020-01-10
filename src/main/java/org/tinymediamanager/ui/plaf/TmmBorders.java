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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseBorders;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * @author Manuel Laggner
 */
public class TmmBorders extends BaseBorders {
  protected static Border titledBorder   = null;
  protected static Border treeNodeBorder = null;

  public static void initDefaults() {
    BaseBorders.initDefaults();
    titledBorder = null;
    treeNodeBorder = null;
  }

  // ------------------------------------------------------------------------------------
  // Lazy access methods
  // ------------------------------------------------------------------------------------
  public static Border getTextBorder() {
    if (textFieldBorder == null) {
      textFieldBorder = new TextFieldBorder();
    }
    return textFieldBorder;
  }

  public static Border getTextFieldBorder() {
    return getTextBorder();
  }

  public static Border getComboBoxBorder() {
    if (comboBoxBorder == null) {
      comboBoxBorder = new TextFieldBorder();
    }
    return comboBoxBorder;
  }

  public static Border getScrollPaneBorder() {
    if (scrollPaneBorder == null) {
      scrollPaneBorder = new ScrollPaneBorder(false);
    }
    return scrollPaneBorder;
  }

  public static Border getTableScrollPaneBorder() {
    if (tableScrollPaneBorder == null) {
      tableScrollPaneBorder = new ScrollPaneBorder(true);
    }
    return tableScrollPaneBorder;
  }

  public static Border getButtonBorder() {
    if (buttonBorder == null) {
      buttonBorder = new EmptyBorder(4, 15, 4, 15);
    }
    return buttonBorder;
  }

  public static Border getToggleButtonBorder() {
    return getButtonBorder();
  }

  public static Border getRolloverToolButtonBorder() {
    if (rolloverToolButtonBorder == null) {
      rolloverToolButtonBorder = new RolloverToolButtonBorder();
    }
    return rolloverToolButtonBorder;
  }

  public static Border getInternalFrameBorder() {
    if (internalFrameBorder == null) {
      internalFrameBorder = new InternalFrameBorder();
    }
    return internalFrameBorder;
  }

  public static Border getTableHeaderBorder() {
    if (tableHeaderBorder == null) {
      tableHeaderBorder = new TableHeaderBorder();
    }
    return tableHeaderBorder;
  }

  public static Border getPopupMenuBorder() {
    if (popupMenuBorder == null) {
      popupMenuBorder = new PopupMenuBorder();
    }
    return popupMenuBorder;
  }

  public static Border getSpinnerBorder() {
    if (spinnerBorder == null) {
      spinnerBorder = new TextFieldBorder();
    }
    return spinnerBorder;
  }

  public static Border getTitledBorder() {
    if (titledBorder == null) {
      titledBorder = new RoundLineBorder(ColorHelper.brighter(AbstractLookAndFeel.getForegroundColor(), 30), 1, 16);
    }
    return titledBorder;
  }

  public static Border getTreeNodeBorder() {
    if (treeNodeBorder == null) {
      treeNodeBorder = new CompoundBorder(new BottomBorderBorder(), new EmptyBorder(5, 0, 5, 0));
    }
    return treeNodeBorder;
  }

  // ------------------------------------------------------------------------------------
  // Implementation of border classes
  // ------------------------------------------------------------------------------------
  public static class TextFieldBorder extends AbstractBorder implements UIResource {
    private static final long   serialVersionUID = -1476629322366320255L;
    private static final Insets insets           = new Insets(4, 6, 5, 7);

    private static int          focusWidth       = 2;

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      int r = 10;
      Container parent = c.getParent();
      if (parent != null && !(parent.getParent() instanceof JScrollPane)) {
        RoundRectangle2D round = new RoundRectangle2D.Float(x + focusWidth, y + focusWidth, width - 2 * focusWidth, height - 2 * focusWidth, r, r);
        RoundRectangle2D shadow = new RoundRectangle2D.Float(x + focusWidth + 1, y + focusWidth + 1, width - 2 * focusWidth, height - 2 * focusWidth,
            r, r);
        GraphicsConfiguration gc = ((Graphics2D) g).getDeviceConfiguration();
        BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, width, height);

        Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
        g2.setComposite(AlphaComposite.Src);
        if (c instanceof JComboBox) {
          int i = 1;
        }
        if (parent.isOpaque()) {
          g2.setColor(parent.getBackground());
        }
        else if (parent.getParent() != null) {
          // dirty but was not able to solve it via transparency
          g2.setColor(parent.getParent().getBackground());
        }
        corner.subtract(new Area(round));
        g2.fill(corner);

        g2.setColor(AbstractLookAndFeel.getTheme().getBackgroundColorDark());
        corner.intersect(new Area(shadow));

        // drop shadow only when the component is opaque
        if (c.isOpaque()) {
          g2.fill(corner);
        }

        boolean focus = c.hasFocus();
        if (c instanceof JComboBox) {
          focus |= ((JComboBox) c).getEditor().getEditorComponent().hasFocus();
        }

        if (c instanceof JSpinner) {
          Component[] comps = ((JSpinner) c).getEditor().getComponents();
          for (Component component : comps) {
            focus |= component.hasFocus();
          }
        }

        // do not draw a focus indicator to non editable text components
        if (focus && !(c instanceof JTextComponent && !((JTextComponent) c).isEditable())) {
          x = focusWidth;
          y = focusWidth;
          int w = width - 2 * focusWidth;
          int h = height - 2 * focusWidth;
          g2.setColor(AbstractLookAndFeel.getFocusColor());
          for (int i = focusWidth; i > 0; i -= 1) {
            final float opacity = (float) (1 - (2.f * i * i / 10));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
            g2.fillRoundRect(x - i, y - i, w + 2 * i, h + 2 * i, r, r);
          }
        }

        g2.dispose();
        g.drawImage(img, 0, 0, null);
      }
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
      return TextFieldBorder.insets;
    }
  } // class TextFieldBorder

  public static class ScrollPaneBorder extends AbstractBorder implements UIResource {
    private static final long   serialVersionUID = -7118022577788519656L;
    private static final Color  fieldBorderColor = new Color(127, 157, 185);
    private static final Insets insets           = new Insets(0, 0, 0, 0);
    private static final Insets tableInsets      = new Insets(0, 0, 0, 0);
    private boolean             tableBorder      = false;

    public ScrollPaneBorder(boolean tableBorder) {
      this.tableBorder = tableBorder;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      if (tableBorder) {
        g.setColor(fieldBorderColor);
        g.drawRect(x, y, w - 1, h - 1);
        g.setColor(ColorHelper.brighter(AbstractLookAndFeel.getTheme().getBackgroundColor(), 50));
        g.drawRect(x + 1, y + 1, w - 3, h - 3);
      }
      else {
        Container parent = c.getParent();
        if (parent != null) {
          int r = 16;
          RoundRectangle2D round = new RoundRectangle2D.Float(x, y, w, h, r, r);
          GraphicsConfiguration gc = ((Graphics2D) g).getDeviceConfiguration();
          BufferedImage img = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
          Graphics2D g2 = img.createGraphics();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setComposite(AlphaComposite.Clear);
          g2.fillRect(0, 0, w, h);

          Area corner = new Area(new Rectangle2D.Float(x, y, w, h));
          g2.setComposite(AlphaComposite.Src);
          g2.setColor(parent.getBackground());
          corner.subtract(new Area(round));
          g2.fill(corner);
          g2.dispose();
          g.drawImage(img, 0, 0, null);
        }
      }
    }

    @Override
    public Insets getBorderInsets(Component c) {
      if (tableBorder) {
        return new Insets(tableInsets.top, tableInsets.left, tableInsets.bottom, tableInsets.right);
      }
      else {
        return new Insets(insets.top, insets.left, insets.bottom, insets.right);
      }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      Insets ins = getBorderInsets(c);
      borderInsets.left = ins.left;
      borderInsets.top = ins.top;
      borderInsets.right = ins.right;
      borderInsets.bottom = ins.bottom;
      return borderInsets;
    }
  } // class ScrollPaneBorder

  public static class InternalFrameBorder extends BaseInternalFrameBorder {
    private static final long serialVersionUID = 1227394113801329301L;

    public InternalFrameBorder() {
      INSETS.top = 3;
      INSETS.left = 2;
      INSETS.right = 2;
      INSETS.bottom = 2;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      g.setColor(Color.BLACK);
      g.fillRect(x, y, w, h);
      g.setColor(AbstractLookAndFeel.getWindowBorderColor());
      g.fillRect(x + 1, y + 1, w - 2, h - 2);
    }
  } // class InternalFrameBorder

  public static class TableHeaderBorder extends AbstractBorder implements UIResource {
    private static final long   serialVersionUID = -2182436739429673033L;
    private static final Insets insets           = new Insets(0, 1, 1, 1);

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      g.setColor(ColorHelper.brighter(AbstractLookAndFeel.getControlBackgroundColor(), 40));
      g.drawLine(0, 0, 0, h - 1);
      g.setColor(ColorHelper.darker(AbstractLookAndFeel.getControlBackgroundColor(), 20));
      g.drawLine(w - 1, 0, w - 1, h - 1);
      g.setColor(ColorHelper.darker(AbstractLookAndFeel.getControlBackgroundColor(), 10));
      g.drawLine(0, h - 1, w - 1, h - 1);
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      borderInsets.left = insets.left;
      borderInsets.top = insets.top;
      borderInsets.right = insets.right;
      borderInsets.bottom = insets.bottom;
      return borderInsets;
    }
  } // class TableHeaderBorder

  public static class PopupMenuBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -2851747427345778378L;
    protected static Insets   insets;

    public PopupMenuBorder() {
      insets = new Insets(1, 1, 1, 1);

    }

    public boolean isMenuBarPopup(Component c) {
      boolean menuBarPopup = false;
      if (c instanceof JPopupMenu) {
        JPopupMenu pm = (JPopupMenu) c;
        if (pm.getInvoker() != null) {
          menuBarPopup = (pm.getInvoker().getParent() instanceof JMenuBar);
        }
      }
      return menuBarPopup;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      Color borderColorLo = AbstractLookAndFeel.getGridColor();

      Graphics2D g2D = (Graphics2D) g;
      Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // - outer frame
      g.setColor(borderColorLo);
      if (isMenuBarPopup(c)) {
        // top
        g.drawLine(x - 1, y, x + w, y);
        // left
        g.drawLine(x, y, x, y + h - 1);
        // bottom
        g.drawLine(x, y + h - 1, x + w, y + h - 1);
        // right
        g.drawLine(x + w - 1, y + 1, x + w - 1, y + h - 1);
      }
      else {
        g.drawRect(x, y, w - 1, h - 1);
      }

      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      Insets ins = getBorderInsets(c);
      borderInsets.left = ins.left;
      borderInsets.top = ins.top;
      borderInsets.right = ins.right;
      borderInsets.bottom = ins.bottom;
      return borderInsets;
    }

  } // class PopupMenuBorder

  public static class RoundLineBorder extends LineBorder {
    protected int radius;

    public RoundLineBorder(Color color, int thickness, int radius) {
      super(color, thickness, true);
      this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      if ((this.thickness > 0) && (g instanceof Graphics2D)) {
        Graphics2D g2d = (Graphics2D) g;

        Object savedRederingHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color oldColor = g2d.getColor();
        Stroke oldKeyStroke = g2d.getStroke();
        g2d.setColor(this.lineColor);

        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawRoundRect(x, y, width - thickness, height - thickness, radius, radius);

        g2d.setColor(oldColor);
        g2d.setStroke(oldKeyStroke);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
      }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets borderInsets) {
      Insets ins = getBorderInsets(c);
      borderInsets.left = ins.left;
      borderInsets.top = ins.top;
      borderInsets.right = ins.right;
      borderInsets.bottom = ins.bottom;
      return borderInsets;
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(0, radius / 2, radius / 2, radius / 2);
    }
  } // class RoundLineBorder

  public static class RolloverToolButtonBorder implements Border, UIResource {
    private static final Insets insets = new Insets(2, 2, 2, 2);

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      AbstractButton button = (AbstractButton) c;
      ButtonModel model = button.getModel();
      if (model.isEnabled()) {
        if ((model.isPressed() && model.isArmed()) || model.isSelected()) {
          Color frameColor = ColorHelper.darker(AbstractLookAndFeel.getToolbarBackgroundColor(), 20);
          g.setColor(frameColor);
          g.drawRect(x, y, w - 1, h - 1);

          Graphics2D g2D = (Graphics2D) g;
          Composite composite = g2D.getComposite();
          AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);
          g2D.setComposite(alpha);
          g.setColor(Color.black);
          g.fillRect(x + 1, y + 1, w - 2, h - 2);
          g2D.setComposite(composite);
        }
        else if (model.isRollover()) {
          Color frameColor = AbstractLookAndFeel.getToolbarBackgroundColor();
          Color frameHiColor = ColorHelper.darker(frameColor, 5);
          Color frameLoColor = ColorHelper.darker(frameColor, 30);
          JTattooUtilities.draw3DBorder(g, frameHiColor, frameLoColor, x, y, w, h);
          frameHiColor = Color.white;
          frameLoColor = ColorHelper.brighter(frameLoColor, 60);
          JTattooUtilities.draw3DBorder(g, frameHiColor, frameLoColor, x + 1, y + 1, w - 2, h - 2);

          Graphics2D g2D = (Graphics2D) g;
          Composite composite = g2D.getComposite();
          AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
          g2D.setComposite(alpha);
          g.setColor(Color.white);
          g.fillRect(x + 2, y + 2, w - 4, h - 4);
          g2D.setComposite(composite);
        }
        else if (model.isSelected()) {
          Color frameColor = AbstractLookAndFeel.getToolbarBackgroundColor();
          Color frameHiColor = Color.white;
          Color frameLoColor = ColorHelper.darker(frameColor, 30);
          JTattooUtilities.draw3DBorder(g, frameLoColor, frameHiColor, x, y, w, h);
        }
      }
    }

    public Insets getBorderInsets(Component c) {
      return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    public Insets getBorderInsets(Component c, Insets borderInsets) {
      borderInsets.left = insets.left;
      borderInsets.top = insets.top;
      borderInsets.right = insets.right;
      borderInsets.bottom = insets.bottom;
      return borderInsets;
    }

    public boolean isBorderOpaque() {
      return true;
    }
  } // class RolloverToolButtonBorder

  public static class BottomBorderBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -1431631265848685069L;
    private final Color       color1           = AbstractLookAndFeel.getTheme().getGridColors()[0];
    private final Color       color2           = AbstractLookAndFeel.getTheme().getGridColors()[1];

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2d = (Graphics2D) g;

      g.setColor(color1);
      g.drawLine(g.getClipBounds().x, height - 2, g.getClipBounds().width, height - 2);
      g.setColor(color2);

      Composite savedComposite = g2d.getComposite();
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g.drawLine(g.getClipBounds().x, height - 1, g.getClipBounds().width, height - 1);

      g2d.setComposite(savedComposite);
    }
  } // class BottomBorderBorder

} // class TmmBorders
