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
package org.tinymediamanager.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseCheckBoxUI;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.JTattooUtilities;
import com.jtattoo.plaf.LazyImageIcon;

/**
 * The class SmallTextFieldBorder - for a smaller version of a JCheckBox
 * 
 * @author Manuel Laggner
 */
public class SmallCheckBoxUI extends BaseCheckBoxUI {
  private static SmallCheckBoxUI checkBoxUI = null;

  public static ComponentUI createUI(JComponent b) {
    if (checkBoxUI == null) {
      checkBoxUI = new SmallCheckBoxUI();
    }
    return checkBoxUI;
  }

  @Override
  public void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    icon = new CheckBoxIcon();
  }

  private static class CheckBoxIcon implements Icon {
    private static final Color MENU_ITEM_BACKGROUND = new Color(248, 248, 248);
    private static Icon        checkIcon            = new LazyImageIcon("icons/small/check_symbol_10x10.png");
    private static Icon        checkIconDisabled    = new LazyImageIcon("icons/small/check_symbol_disabled_10x10.png");
    private static final int   WIDTH                = 12;
    private static final int   HEIGHT               = 12;

    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (!JTattooUtilities.isLeftToRight(c)) {
        x += 3;
      }

      AbstractButton b = (AbstractButton) c;
      ButtonModel model = b.getModel();
      if (c instanceof JCheckBoxMenuItem) {
        g.setColor(MENU_ITEM_BACKGROUND);
        g.fillRect(x, y, WIDTH, HEIGHT);
        if (b.isEnabled()) {
          g.setColor(AbstractLookAndFeel.getFrameColor());
        }
        else {
          g.setColor(ColorHelper.brighter(AbstractLookAndFeel.getFrameColor(), 40));
        }
        g.drawRect(x, y, WIDTH, HEIGHT);
      }
      else {
        if (b.isEnabled()) {
          if (b.isRolloverEnabled() && model.isRollover()) {
            JTattooUtilities.fillHorGradient(g, AbstractLookAndFeel.getTheme().getRolloverColors(), x, y, WIDTH, HEIGHT);
          }
          else {
            if (AbstractLookAndFeel.getTheme().doShowFocusFrame() && b.hasFocus()) {
              JTattooUtilities.fillHorGradient(g, AbstractLookAndFeel.getTheme().getFocusColors(), x, y, WIDTH, HEIGHT);
            }
            else {
              JTattooUtilities.fillHorGradient(g, AbstractLookAndFeel.getTheme().getCheckBoxColors(), x, y, WIDTH, HEIGHT);
            }
            if (!model.isPressed()) {
              g.setColor(Color.white);
              g.drawLine(x + 1, y + 1, x + 1, y + HEIGHT - 2);
              g.drawLine(x + WIDTH - 1, y + 1, x + WIDTH - 1, y + HEIGHT - 2);
            }
          }
          if (AbstractLookAndFeel.getTheme().doShowFocusFrame() && b.hasFocus()) {
            Color hiColor = ColorHelper.brighter(AbstractLookAndFeel.getTheme().getFocusFrameColor(), 30);
            Color loColor = ColorHelper.darker(AbstractLookAndFeel.getTheme().getFocusFrameColor(), 20);
            g.setColor(hiColor);
            g.drawRect(x - 1, y - 1, WIDTH + 2, HEIGHT + 2);
            g.setColor(loColor);
            g.drawRect(x, y, WIDTH, HEIGHT);
          }
          else {
            g.setColor(AbstractLookAndFeel.getFrameColor());
            g.drawRect(x, y, WIDTH, HEIGHT);
          }
        }
        else {
          JTattooUtilities.fillHorGradient(g, AbstractLookAndFeel.getTheme().getDisabledColors(), x, y, WIDTH, HEIGHT);
          g.setColor(ColorHelper.brighter(AbstractLookAndFeel.getFrameColor(), 40));
          g.drawRect(x, y, WIDTH, HEIGHT);
        }
      }
      int xi = x + ((WIDTH - checkIcon.getIconWidth()) / 2) + 1;
      int yi = y + ((HEIGHT - checkIcon.getIconHeight()) / 2) + 1;
      if (model.isPressed() && model.isArmed()) {
        Color bc = AbstractLookAndFeel.getTheme().getSelectionBackgroundColor();
        Color fc = ColorHelper.darker(bc, 40);
        g.setColor(fc);
        g.drawRect(x + 2, y + 2, WIDTH - 4, HEIGHT - 4);
        g.setColor(bc);
        g.fillRect(x + 3, y + 3, WIDTH - 6, HEIGHT - 6);
      }
      else if (model.isSelected()) {
        if (b.isEnabled()) {
          checkIcon.paintIcon(c, g, xi, yi);
        }
        else {
          checkIconDisabled.paintIcon(c, g, xi, yi);
        }
      }
    }

    @Override
    public int getIconWidth() {
      return WIDTH + 4;
    }

    @Override
    public int getIconHeight() {
      return HEIGHT;
    }
  }
}
