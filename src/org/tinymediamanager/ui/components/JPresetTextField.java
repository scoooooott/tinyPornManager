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
package org.tinymediamanager.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * The class JPresetTextField represents a textfield with a preset text when not focused
 * 
 * @author Manuel Laggner
 */
public class JPresetTextField extends JTextField {
  private static final long serialVersionUID = -7316054018368081957L;

  private String            textWhenNothingEntered;

  public JPresetTextField(String presetText) {
    this.textWhenNothingEntered = presetText;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (this.getText().equals("")) {
      int height = this.getHeight();
      Font prev = g.getFont();
      Font italic = prev.deriveFont(Font.ITALIC);
      Color prevColor = g.getColor();
      g.setFont(italic);
      g.setColor(UIManager.getColor("textInactiveText"));
      int h = g.getFontMetrics().getHeight();
      int textBottom = (height - h) / 2 + h - 4;
      int x = this.getInsets().left + 2;
      Graphics2D g2d = (Graphics2D) g;
      RenderingHints hints = g2d.getRenderingHints();
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.drawString(textWhenNothingEntered, x, textBottom);
      g2d.setRenderingHints(hints);
      g.setFont(prev);
      g.setColor(prevColor);
    }

  }
}
