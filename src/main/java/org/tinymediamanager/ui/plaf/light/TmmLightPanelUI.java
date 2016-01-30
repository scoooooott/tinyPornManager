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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.BasePanelUI;

/**
 * The Class TmmLightPanelUI.
 * 
 * @author Manuel Laggner
 */
public class TmmLightPanelUI extends BasePanelUI {

  private static String          CLASS               = "class";
  private static String          ROUNDED_PANEL       = "roundedPanel";
  private static String          BORDER_RADIUS       = "borderRadius";
  private static String          TOOLBAR_PANEL       = "toolbarPanel";
  private static String          ROOT_PANEL          = "rootPanel";

  private static TmmLightPanelUI panelUI             = null;

  private static Color           TOOLBAR_PANEL_COLOR = new Color(46, 46, 46);

  public static ComponentUI createUI(JComponent c) {
    if (panelUI == null) {
      panelUI = new TmmLightPanelUI();
    }
    return panelUI;
  }

  @Override
  public void update(Graphics g, JComponent c) {
    if (c.isOpaque()) {
      Object panelClass = c.getClientProperty(CLASS);
      if (panelClass != null && panelClass instanceof String && ROUNDED_PANEL.equals(panelClass.toString())) {
        // draw a rounded panel
        updateRoundedPanel(g, c);
      }
      else if (panelClass != null && panelClass instanceof String && TOOLBAR_PANEL.equals(panelClass.toString())) {
        // draw the toolbar panel
        c.setBackground(TOOLBAR_PANEL_COLOR);
        super.update(g, c);
      }
      else if (panelClass != null && panelClass instanceof String && ROOT_PANEL.equals(panelClass.toString())) {
        // draw the root panel
        // c.setBackground(AbstractLookAndFeel.getTheme().getBackgroundColorDark());
        c.setBackground(new Color(213, 213, 213));
        super.update(g, c);
      }
      else {
        // default drawing
        super.update(g, c);
      }
    }
  }

  private void updateRoundedPanel(Graphics g, JComponent c) {
    int radius = 15;
    Object borderRadius = c.getClientProperty(BORDER_RADIUS);
    if (borderRadius != null && borderRadius instanceof Integer) {
      radius = (Integer) borderRadius;
    }

    Graphics2D g2D = (Graphics2D) g;
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(c.getBackground());
    g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), radius, radius);

    g2D.setRenderingHints(savedRenderingHints);
  }
}
