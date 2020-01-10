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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseButtonUI;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * @author Manuel Laggner
 */
public class TmmButtonUI extends BaseButtonUI {
  protected boolean isFlatButton = false;
  protected int     focusWidth   = 2;

  public static ComponentUI createUI(JComponent c) {
    return new TmmButtonUI();
  }

  @Override
  public void installDefaults(AbstractButton b) {
    super.installDefaults(b);

    Object prop = b.getClientProperty("flatButton");
    if (prop != null && prop instanceof Boolean) {
      isFlatButton = (Boolean) prop;

      if (isFlatButton) {
        b.setBorder(new EmptyBorder(3, 6, 3, 6));
      }
    }

    b.setOpaque(false);
    b.setFocusPainted(false);
  }

  @Override
  protected void paintBackground(Graphics g, AbstractButton b) {
    if (isFlatButton || !b.isContentAreaFilled() || (b.getParent() instanceof JMenuBar)) {
      return;
    }
    Graphics2D g2D = (Graphics2D) g;

    Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = b.getWidth();
    int height = b.getHeight();
    int borderRadius = (int) (b.getHeight() * 0.7 - 2 * focusWidth);

    int x = focusWidth;
    int y = focusWidth;
    int w = width - 2 * focusWidth;
    int h = height - 2 * focusWidth;

    ButtonModel model = b.getModel();

    // draw the focus ring
    if ((b.isRolloverEnabled()) && (model.isRollover())) {
      g2D.setColor(AbstractLookAndFeel.getFocusColor());
      // g2D.fillRoundRect(x - 1, y - 1, w + 1, h + 1, borderRadius, borderRadius);
      final Composite oldComposite = g2D.getComposite();
      for (int i = focusWidth; i > 0; i -= 1) {
        final float opacity = 1 - (2.f * i * i / 10);
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2D.fillRoundRect(x - i, y - i, w + 2 * i, h + 2 * i, borderRadius + i, borderRadius + i);
      }
      g2D.setComposite(oldComposite);
    }

    if ((model.isPressed()) && (model.isArmed())) {
      g2D.setColor(AbstractLookAndFeel.getTheme().getPressedBackgroundColor());
    }
    else {
      g2D.setColor(AbstractLookAndFeel.getButtonBackgroundColor());
    }

    g2D.fillRoundRect(x, y, w, h, borderRadius, borderRadius);
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
  }

  @Override
  protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
    ButtonModel model = b.getModel();

    FontMetrics fm = getFontMetrics(b, g, b.getFont());

    int mnemIndex = b.getDisplayedMnemonicIndex();

    if (model.isEnabled()) {
      Color foreground = b.getForeground();
      Color background = b.getBackground();
      int offs = 0;
      if (model.isArmed() && model.isPressed()) {
        offs = 0;
      }
      if (!(model.isPressed() && model.isArmed())) {
        Object sc = b.getClientProperty("shadowColor");
        if (sc instanceof Color) {
          g.setColor((Color) sc);
          JTattooUtilities.drawStringUnderlineCharAt(b, g, text, mnemIndex, textRect.x + 1, textRect.y + 1 + fm.getAscent());
        }
      }
      if (background instanceof ColorUIResource && !isFlatButton) {
        if (model.isPressed() && model.isArmed()) {
          g.setColor(AbstractLookAndFeel.getTheme().getPressedForegroundColor());
        }
        else if (model.isRollover()) {
          g.setColor(AbstractLookAndFeel.getTheme().getRolloverForegroundColor());
        }
        else {
          g.setColor(foreground);
        }
      }
      else {
        g.setColor(foreground);
      }
      JTattooUtilities.drawStringUnderlineCharAt(b, g, text, mnemIndex, textRect.x + offs, textRect.y + offs + fm.getAscent());
    }
    else {
      if (ColorHelper.getGrayValue(b.getForeground()) < 128) {
        g.setColor(Color.white);
        JTattooUtilities.drawStringUnderlineCharAt(b, g, text, mnemIndex, textRect.x + 1, textRect.y + 1 + fm.getAscent());
      }
      g.setColor(AbstractLookAndFeel.getDisabledForegroundColor());
      JTattooUtilities.drawStringUnderlineCharAt(b, g, text, mnemIndex, textRect.x, textRect.y + fm.getAscent());
    }
  }

  @Override
  protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    Graphics2D g2D = (Graphics2D) g;

    AbstractButton b = (AbstractButton) c;
    Font f = c.getFont();
    g.setFont(f);
    FontMetrics fm = getFontMetrics(b, g, b.getFont());
    Insets insets = c.getInsets();

    viewRect.x = insets.left;
    viewRect.y = insets.top;
    viewRect.width = b.getWidth() - (insets.right + viewRect.x);
    viewRect.height = b.getHeight() - (insets.bottom + viewRect.y);

    textRect.x = textRect.y = textRect.width = textRect.height = 0;
    iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

    int iconTextGap = b.getIconTextGap();
    String text = SwingUtilities.layoutCompoundLabel(c, fm, b.getText(), b.getIcon(), b.getVerticalAlignment(), b.getHorizontalAlignment(),
        b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect, textRect, b.getText() == null ? 0 : iconTextGap);

    paintBackground(g, b);

    if (b.getIcon() != null) {
      if (!b.isEnabled()) {
        Composite savedComposite = g2D.getComposite();
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        g2D.setComposite(alpha);
        paintIcon(g, c, iconRect);
        g2D.setComposite(savedComposite);
      }
      else {
        paintIcon(g, c, iconRect);
      }
    }

    if (text != null && !text.equals("")) {
      View v = (View) c.getClientProperty(BasicHTML.propertyKey);
      if (v != null) {
        Object savedRenderingHint = null;
        if (AbstractLookAndFeel.getTheme().isTextAntiAliasingOn()) {
          savedRenderingHint = g2D.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
          g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        v.paint(g, textRect);
        if (AbstractLookAndFeel.getTheme().isTextAntiAliasingOn()) {
          g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, savedRenderingHint);
        }
      }
      else {
        paintText(g, b, textRect, text);
      }
    }

    if (b.isFocusPainted() && b.hasFocus()) {
      paintFocus(g, b, viewRect, textRect, iconRect);
    }
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    AbstractButton b = (AbstractButton) c;
    return BasicGraphicsUtils.getPreferredButtonSize(b, b.getIconTextGap());
  }

  @SuppressWarnings("deprecation")
  private FontMetrics getFontMetrics(JComponent c, Graphics g, Font font) {
    if (c != null) {
      // Note: We assume that we're using the FontMetrics
      // from the widget to layout out text, otherwise we can get
      // mismatches when printing.
      return c.getFontMetrics(font);
    }
    return Toolkit.getDefaultToolkit().getFontMetrics(font);
  }
}
