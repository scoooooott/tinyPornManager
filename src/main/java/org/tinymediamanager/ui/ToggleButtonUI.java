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
package org.tinymediamanager.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JMenuBar;
import javax.swing.plaf.ColorUIResource;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseToggleButtonUI;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * The Class ToggleButtonUI.
 * 
 * @author Manuel Laggner
 */
public class ToggleButtonUI extends BaseToggleButtonUI {

  /*
   * (non-Javadoc)
   * 
   * @see com.jtattoo.plaf.BaseToggleButtonUI#paintBackground(java.awt.Graphics, javax.swing.AbstractButton)
   */
  @Override
  protected void paintBackground(Graphics g, AbstractButton b) {
    if (!b.isContentAreaFilled() || (b.getParent() instanceof JMenuBar)) {
      return;
    }

    int width = b.getWidth();
    int height = b.getHeight();
    ButtonModel model = b.getModel();
    Color colors[] = null;
    Color pressed[] = AbstractLookAndFeel.getTheme().getThumbColors();

    if (b.isEnabled()) {
      if (b.getBackground() instanceof ColorUIResource) {
        if (model.isPressed() && model.isArmed()) {
          colors = pressed;
        }
        else if (b.isRolloverEnabled() && model.isRollover()) {
          if (model.isSelected()) {
            colors = pressed;
          }
          else {
            colors = AbstractLookAndFeel.getTheme().getRolloverColors();
          }
        }
        else if (model.isSelected()) {
          colors = pressed;
        }
        else {
          if (AbstractLookAndFeel.getTheme().doShowFocusFrame() && b.hasFocus()) {
            colors = AbstractLookAndFeel.getTheme().getFocusColors();
          }
          else {
            colors = AbstractLookAndFeel.getTheme().getButtonColors();
          }
        }
      }
      else {
        if (model.isPressed() && model.isArmed()) {
          colors = pressed;
        }
        else if ((b.isRolloverEnabled() && model.isRollover() && model.isSelected())) {
          colors = pressed;
        }
        else if (b.isRolloverEnabled() && model.isRollover()) {
          colors = ColorHelper.createColorArr(ColorHelper.brighter(b.getBackground(), 80), ColorHelper.brighter(b.getBackground(), 20), 20);
        }
        else if (model.isSelected()) {
          colors = pressed;
        }
        else {
          colors = ColorHelper.createColorArr(ColorHelper.brighter(b.getBackground(), 40), ColorHelper.darker(b.getBackground(), 20), 20);
        }
      }
    }
    else { // disabled
      colors = AbstractLookAndFeel.getTheme().getDisabledColors();
    }
    JTattooUtilities.fillHorGradient(g, colors, 1, 1, width - 2, height - 2);
  }

  /*
   * do not paint any focus border
   */
  /*
   * (non-Javadoc)
   * 
   * @see com.jtattoo.plaf.BaseToggleButtonUI#paintFocus(java.awt.Graphics, javax.swing.AbstractButton, java.awt.Rectangle, java.awt.Rectangle,
   * java.awt.Rectangle)
   */
  protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
  }
}
