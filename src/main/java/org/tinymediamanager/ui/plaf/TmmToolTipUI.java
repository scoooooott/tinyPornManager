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
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolTipUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.ColorHelper;
import com.jtattoo.plaf.DecorationHelper;

public class TmmToolTipUI extends MetalToolTipUI {
  private boolean           fancyLayout         = false;
  private ComponentListener popupWindowListener = null;

  public static ComponentUI createUI(JComponent c) {
    return new TmmToolTipUI();
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);
    int borderSize = AbstractLookAndFeel.getTheme().getTooltipBorderSize();
    int shadowSize = AbstractLookAndFeel.getTheme().getTooltipShadowSize();
    fancyLayout = DecorationHelper.isTranslucentWindowSupported() && ToolTipManager.sharedInstance().isLightWeightPopupEnabled();
    if (fancyLayout) {
      c.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize + shadowSize, borderSize + shadowSize, borderSize + shadowSize));
      c.setOpaque(false);
      Container parent = c.getParent();
      if (parent instanceof JPanel) {
        ((JPanel) c.getParent()).setOpaque(false);
      }
    }
    else {
      c.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
    }
  }

  @Override
  protected void installListeners(JComponent c) {
    super.installListeners(c);

    // We must set the popup window to opaque because it is cached and reused within the PopupFactory
    popupWindowListener = new ComponentAdapter() {

      @Override
      public void componentHidden(ComponentEvent e) {
        Window window = (Window) e.getComponent();
        DecorationHelper.setTranslucentWindow(window, false);
        window.removeComponentListener(popupWindowListener);
      }
    };
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    Graphics2D g2D = (Graphics2D) g;
    Composite savedComposit = g2D.getComposite();
    Object savedRederingHint = g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int borderSize = AbstractLookAndFeel.getTheme().getTooltipBorderSize();
    int shadowSize = AbstractLookAndFeel.getTheme().getTooltipShadowSize();

    int w = c.getWidth();
    int h = c.getHeight();
    Color backColor = AbstractLookAndFeel.getTheme().getTooltipBackgroundColor();

    // We can't draw the fancyLayout if popup is medium weight
    boolean mediumWeight = false;
    Container parent = c.getParent();
    while (parent != null) {
      if ((parent.getClass().getName().indexOf("MediumWeight") > 0)) {
        mediumWeight = true;
        break;
      }
      parent = parent.getParent();
    }

    // Paint the tooltip with a shadow border
    if (!mediumWeight && fancyLayout && shadowSize > 0) {
      parent = c.getParent();
      while (parent != null) {
        if ((parent.getClass().getName().indexOf("HeavyWeightWindow") > 0) && (parent instanceof Window)) {
          // Make the popup transparent
          Window window = (Window) parent;
          // Add a component listener to revert this operation if popup is closed
          window.addComponentListener(popupWindowListener);
          DecorationHelper.setTranslucentWindow(window, true);
          break;
        }
        parent = parent.getParent();
      }
      // draw the shadow
      g2D.setColor(AbstractLookAndFeel.getTheme().getShadowColor());
      float[] composites = { 0.01f, 0.02f, 0.04f, 0.06f, 0.08f, 0.12f };
      int shadowOffset = AbstractLookAndFeel.getTheme().isTooltipCastShadow() ? shadowSize : 0;
      for (int i = 0; i < shadowSize; i++) {
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, composites[i >= composites.length ? composites.length - 1 : i]));
        g2D.fillRoundRect(i + shadowOffset, borderSize + i, w - (2 * i) - shadowOffset, h - borderSize - (2 * i), 12 - i, 12 - i);

      }
      g2D.setComposite(savedComposit);

      // Draw background with borders
      if (ColorHelper.getGrayValue(backColor) < 128) {
        g2D.setColor(ColorHelper.brighter(AbstractLookAndFeel.getTheme().getBackgroundColor(), 20));
      }
      else {
        g2D.setColor(Color.white);

      }
      // g2D.fillRoundRect(shadowSize, 0, w - (2 * shadowSize) - 1, h - shadowSize - 1, 6, 6);
      g2D.fillRoundRect(shadowSize, 0, w - (2 * shadowSize) - 1, h - shadowSize - 1, shadowSize, shadowSize);
      g2D.setColor(ColorHelper.darker(backColor, 40));
      // g2D.drawRoundRect(shadowSize, 0, w - (2 * shadowSize) - 1, h - shadowSize - 1, 6, 6);
      g2D.drawRoundRect(shadowSize, 0, w - (2 * shadowSize) - 1, h - shadowSize - 1, shadowSize, shadowSize);
      g2D.setColor(ColorHelper.darker(backColor, 10));

      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);
      // Draw the text. This must be done within an offscreen image because of a bug
      // in the jdk, wich causes ugly antialiased font rendering when background is
      // transparent and popup is heavy weight.
      BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      Graphics2D big = bi.createGraphics();
      big.setClip(0, 0, w, h);
      big.fillRect(borderSize + shadowSize, borderSize, w - (2 * borderSize) - (2 * shadowSize), h - (2 * borderSize) - shadowSize);

      if (c instanceof JToolTip) {
        c.setForeground(AbstractLookAndFeel.getTheme().getTooltipForegroundColor());
      }
      super.paint(big, c);
      g2D.setClip(borderSize + shadowSize, borderSize, w - (2 * borderSize) - (2 * shadowSize), h - (2 * borderSize) - shadowSize);
      g2D.drawImage(bi, 0, 0, null);

    }
    else {
      // Draw background with borders
      if (ColorHelper.getGrayValue(backColor) < 128) {
        g2D.setColor(ColorHelper.brighter(AbstractLookAndFeel.getTheme().getBackgroundColor(), 20));
      }
      else {
        g2D.setColor(Color.white);
      }
      g2D.fillRect(0, 0, w, h);
      g2D.setColor(ColorHelper.darker(backColor, 40));
      g2D.drawRect(0, 0, w - 1, h - 1);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedRederingHint);

      super.paint(g, c);
    }
  }

} // end of class BaseToolTipUI
