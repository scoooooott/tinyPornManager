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
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.UIResource;

/**
 * The class ButtonBarUI. To draw the button bar nicely
 * 
 * @author Manuel Laggner
 */
public class ButtonBarUI extends PanelUI {
  public static ComponentUI createUI(JComponent c) {
    return new ButtonBarUI();
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);

    Border b = c.getBorder();
    if (b == null || b instanceof UIResource) {
      c.setBorder(new BorderUIResource(BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    Color color = c.getBackground();
    if (color == null || color instanceof ColorUIResource) {
      c.setOpaque(true);
      c.setBackground(new ColorUIResource(Color.white));
    }
  }

  @Override
  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    Dimension preferred = c.getLayout().preferredLayoutSize(c);
    return preferred;
  }
}
