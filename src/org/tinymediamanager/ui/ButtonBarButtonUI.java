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
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * The class ButtonBarButtonUI. To draw the buttons inside the button bar
 * 
 * @author Manuel Laggner
 */
public class ButtonBarButtonUI extends BasicButtonUI {
  private final static Color SELECTED_BACKGROUND = new Color(194, 208, 243);
  private final static Color HOVER_BACKGROUND    = new Color(215, 222, 240);

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);

    AbstractButton button = (AbstractButton) c;
    button.setOpaque(false);
    button.setRolloverEnabled(true);
    button.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    AbstractButton button = (AbstractButton) c;

    if (button.getModel().isSelected()) {
      Color oldColor = g.getColor();
      g.setColor(SELECTED_BACKGROUND);
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
      g.setColor(oldColor);
    }
    else if (button.getModel().isRollover()) {
      Color oldColor = g.getColor();
      g.setColor(HOVER_BACKGROUND);
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
      g.setColor(oldColor);
    }
    super.paint(g, c);
  }
}
