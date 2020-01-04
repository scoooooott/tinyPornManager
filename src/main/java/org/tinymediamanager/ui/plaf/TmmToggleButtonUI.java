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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseToggleButtonUI;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.JTattooUtilities;

public class TmmToggleButtonUI extends BaseToggleButtonUI {
  protected boolean isFlatButton = false;
  protected int     focusWidth   = 2;

  public static ComponentUI createUI(JComponent c) {
    return new TmmToggleButtonUI();
  }

  @Override
  public void installDefaults(AbstractButton b) {
    super.installDefaults(b);

    Object prop = b.getClientProperty("flatButton");
    if (prop != null && prop instanceof Boolean) {
      isFlatButton = (Boolean) prop;
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
    int borderRadius = (int) (b.getHeight() * 0.9 - 2 * focusWidth);

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

    if ((model.isPressed()) && (model.isArmed()) || model.isSelected()) {
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
      if ((model.isArmed() && model.isPressed()) || model.isSelected()) {
        offs = 0;
      }
      if (!(model.isPressed() && model.isArmed())) {
        Object sc = b.getClientProperty("shadowColor");
        if (sc instanceof Color) {
          g.setColor((Color) sc);
          JTattooUtilities.drawStringUnderlineCharAt(b, g, text, mnemIndex, textRect.x + 1, textRect.y + 1 + fm.getAscent());
        }
      }
      if (background instanceof ColorUIResource) {
        if ((model.isPressed() && model.isArmed()) || model.isSelected()) {
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
