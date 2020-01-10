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
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseSplitPaneDivider;

/**
 * The class TmmSplitPaneDivider
 *
 * @author Manuel Laggner
 */
public class TmmSplitPaneDivider extends BaseSplitPaneDivider {

  public TmmSplitPaneDivider(BasicSplitPaneUI ui) {
    super(ui);
  }

  @Override
  public void paint(Graphics g) {
    if (!isFlatMode()) {
      Graphics2D g2D = (Graphics2D) g;
      Composite savedComposite = g2D.getComposite();
      int width = getSize().width;
      int height = getSize().height;
      int dx = 0;
      int dy = 0;
      if ((width % 2) == 1) {
        dx = 1;
      }
      if ((height % 2) == 1) {
        dy = 1;
      }

      if (UIManager.getLookAndFeel() instanceof AbstractLookAndFeel) {
        AbstractLookAndFeel laf = (AbstractLookAndFeel) UIManager.getLookAndFeel();
        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
          // JTattooUtilities.fillVerGradient(g, colors, 0, 0, width, height);
          Icon horBumps = laf.getIconFactory().getSplitterHorBumpIcon();
          if ((horBumps != null) && (width > horBumps.getIconWidth())) {
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
            g2D.setComposite(alpha);

            if (splitPane.isOneTouchExpandable() && centerOneTouchButtons) {
              int centerY = height / 2;
              int x = (width - horBumps.getIconWidth()) / 2 + dx;
              int y = centerY - horBumps.getIconHeight() - 40;
              horBumps.paintIcon(this, g, x, y);
              y = centerY + 40;
              horBumps.paintIcon(this, g, x, y);
            }
            else {
              int x = (width - horBumps.getIconWidth()) / 2 + dx;
              int y = (height - horBumps.getIconHeight()) / 2;
              horBumps.paintIcon(this, g, x, y);
            }
          }
        }
        else {
          // JTattooUtilities.fillHorGradient(g, colors, 0, 0, width, height);
          Icon verBumps = laf.getIconFactory().getSplitterVerBumpIcon();
          if ((verBumps != null) && (height > verBumps.getIconHeight())) {
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
            g2D.setComposite(alpha);
            if (splitPane.isOneTouchExpandable() && centerOneTouchButtons) {
              int centerX = width / 2;
              int x = centerX - verBumps.getIconWidth() - 40;
              int y = (height - verBumps.getIconHeight()) / 2 + dy;
              verBumps.paintIcon(this, g, x, y);
              x = centerX + 40;
              verBumps.paintIcon(this, g, x, y);
            }
            else {
              int x = (width - verBumps.getIconWidth()) / 2;
              int y = (height - verBumps.getIconHeight()) / 2 + dy;
              verBumps.paintIcon(this, g, x, y);
            }
          }
        }
      }
      g2D.setComposite(savedComposite);
    }
    paintComponents(g);
  }
}
