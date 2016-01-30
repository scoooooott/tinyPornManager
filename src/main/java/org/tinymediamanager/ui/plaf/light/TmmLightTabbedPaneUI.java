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

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseTabbedPaneUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * The Class TmmLightTabbedPaneUI.
 * 
 * @author Manuel Laggner
 */
public class TmmLightTabbedPaneUI extends BaseTabbedPaneUI {

  protected static int   BORDER_RADIUS  = 20;
  protected static int   TAB_GAP        = 1;

  protected static Color BORDER_COLOR   = new Color(203, 203, 203);
  protected static Color SELECTED_COLOR = new Color(141, 165, 179);

  public static ComponentUI createUI(JComponent c) {
    Object prop = c.getClientProperty("class");
    if (prop != null && prop instanceof String && "big".equals(prop.toString())) {
      return new TmmLightBigTabbedPaneUI();
    }
    return new TmmLightTabbedPaneUI();
  }

  @Override
  public void installDefaults() {
    super.installDefaults();
    tabInsets = new Insets(2, 10, 2, 10);
  }

  // @Override
  // protected Font getTabFont(boolean isSelected) {
  // return super.getTabFont(isSelected).deriveFont(16f);
  // }

  @Override
  protected FontMetrics getFontMetrics() {
    Font font = getTabFont(false);
    return Toolkit.getDefaultToolkit().getFontMetrics(font);
  }

  @Override
  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    Graphics2D g2D = (Graphics2D) g;
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // paint border; need to paint the border here, because the method is called after paintTabBackground in JTattoo
    g.setColor(BORDER_COLOR);
    if (tabPlacement == TOP) {
      g.fillRoundRect(x + TAB_GAP, y, w - 2 * TAB_GAP, h, BORDER_RADIUS, BORDER_RADIUS);
      g.fillRect(x + TAB_GAP, y + BORDER_RADIUS / 2, w - 2 * TAB_GAP, h - BORDER_RADIUS / 2);
    }

    // paint background
    if (isSelected) {
          g.setColor(SELECTED_COLOR);
    }
    else {
      g.setColor(AbstractLookAndFeel.getBackgroundColor());
    }

    if (tabPlacement == TOP) {
      g.fillRoundRect(x + TAB_GAP + 1, y + 1, w - 2 * TAB_GAP - 2, h - 2, BORDER_RADIUS, BORDER_RADIUS);
      g.fillRect(x + TAB_GAP + 1, y + BORDER_RADIUS / 2 + 1, w - 2 * TAB_GAP - 2, h - BORDER_RADIUS / 2 - 1);
    }
    else if (tabPlacement == LEFT) {
      g.fillRect(x + 1, y + 1, w + 2, h - 1);
    }
    else if (tabPlacement == BOTTOM) {
      g.fillRect(x + 1, y - 2, w - 1, h + 2);
    }
    else {
      g.fillRect(x - 2, y + 1, w + 2, h - 1);
    }

    g2D.setRenderingHints(savedRenderingHints);
  }

  @Override
  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    // when the tab is selected, it will be painted in the background method
//    if (isSelected){
//      return;
//    }
//    Graphics2D g2D = (Graphics2D) g;
//    Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
//    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//    g.setColor(BORDER_COLOR);
//
//    g.drawLine(x1 + TAB_GAP + BORDER_RADIUS / 2, y1, x2 - TAB_GAP - BORDER_RADIUS / 2, y1);
//    g.drawArc(x1 + TAB_GAP, y1, BORDER_RADIUS, BORDER_RADIUS, 90, 90);
//    g.drawArc(x2 - BORDER_RADIUS - TAB_GAP, y1, BORDER_RADIUS, BORDER_RADIUS, 0, 90);
//    g.drawLine(x1 + TAB_GAP, y1 + BORDER_RADIUS / 2, x1 + TAB_GAP, y2 - 1);
//    g.drawLine(x2 - TAB_GAP, y1 + BORDER_RADIUS / 2, x2 - TAB_GAP, y2 - 1);
//
//    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
  }

  @Override
  protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
    int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);

    Graphics2D g2D = (Graphics2D) g;
    Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(BORDER_COLOR);

    Insets bi = new Insets(0, 0, 0, 0);
    if (tabPane.getBorder() != null) {
      bi = tabPane.getBorder().getBorderInsets(tabPane);
    }
    int sepHeight = tabAreaInsets.bottom;

    g.drawLine(x, y + tabAreaHeight - sepHeight + bi.top, x + w, y + tabAreaHeight - sepHeight + bi.top);

    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
  }

  @Override
  protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect,
      boolean isSelected) {
  }
}
