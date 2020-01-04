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

package org.tinymediamanager.ui.components.toolbar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.apache.commons.lang3.StringUtils;

public class ToolbarMenu extends ToolbarLabel {
  public static Color        COLOR       = Color.GRAY;
  public static Color        COLOR_HOVER = Color.WHITE;

  private static int         arrowSize   = 10;

  protected static ImageIcon menuImage;
  protected static ImageIcon menuImageHover;

  protected JPopupMenu       popupMenu   = null;

  public ToolbarMenu(String text) {
    super(text);
  }

  public ToolbarMenu(String text, JPopupMenu popupMenu) {
    this(text);

    if (popupMenu != null) {
      setPopupMenu(popupMenu);
    }
  }

  @Override
  protected void setMouseListener() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent arg0) {
        setForeground(COLOR);
        if (popupMenu != null) {
          setIcon(getMenuIndicatorImage());
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }

      @Override
      public void mouseEntered(MouseEvent arg0) {
        setForeground(COLOR_HOVER);
        if (popupMenu != null) {
          setIcon(getMenuIndicatorHoverImage());
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
      }

      @Override
      public void mouseClicked(MouseEvent arg0) {
        if (popupMenu != null) {
          int x = ToolbarMenu.this.getWidth() - (int) popupMenu.getPreferredSize().getWidth();
          // prevent the popupmenu from being displayed on another screen if x < 0
          if (x < 0) {
            x = 0;
          }
          int y = ToolbarMenu.this.getHeight();
          popupMenu.show(ToolbarMenu.this, x, y);
        }
      }
    });
  }

  public void setPopupMenu(JPopupMenu popupMenu) {
    this.popupMenu = popupMenu;

    if (popupMenu == null || StringUtils.isBlank(popupMenu.getLabel())) {
      setText(defaultText);
    }
    else {
      setText(popupMenu.getLabel());
    }

    if (popupMenu != null) {
      setIcon(getMenuIndicatorImage());
    }
    else {
      setIcon(null);
    }
  }

  protected ImageIcon getMenuIndicatorHoverImage() {
    if (menuImageHover != null) {
      return menuImageHover;
    }

    menuImageHover = new ImageIcon(paintMenuImage(true));
    return menuImageHover;
  }

  protected ImageIcon getMenuIndicatorImage() {
    if (menuImage != null) {
      return menuImage;
    }

    menuImage = new ImageIcon(paintMenuImage(false));
    return menuImage;
  }

  protected Image paintMenuImage(boolean hover) {
    BufferedImage img = new BufferedImage(arrowSize, arrowSize, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    g.setColor(hover ? COLOR : COLOR_HOVER);
    g.fillRect(0, 0, img.getWidth(), img.getHeight());
    g.setColor(hover ? COLOR_HOVER : COLOR);
    // this creates a triangle facing right >
    g.fillPolygon(new int[] { 0, 0, arrowSize / 2 }, new int[] { 0, arrowSize, arrowSize / 2 }, 3);
    g.dispose();
    // rotate it to face downwards
    img = rotate(img, 90);

    BufferedImage dimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
    g = dimg.createGraphics();
    g.setComposite(AlphaComposite.Src);
    g.drawImage(img, null, 0, 0);
    g.dispose();

    // paint transparent background
    for (int i = 0; i < dimg.getHeight(); i++) {
      for (int j = 0; j < dimg.getWidth(); j++) {
        if (dimg.getRGB(j, i) == (hover ? COLOR.getRGB() : COLOR_HOVER.getRGB())) {
          dimg.setRGB(j, i, 0x8F1C1C);
        }
      }
    }

    return Toolkit.getDefaultToolkit().createImage(dimg.getSource());
  }

  protected BufferedImage rotate(BufferedImage img, int angle) {
    int w = img.getWidth();
    int h = img.getHeight();
    BufferedImage dimg = new BufferedImage(w, h, img.getType());
    Graphics2D g = dimg.createGraphics();
    g.rotate(Math.toRadians(angle), w / 2, h / 2);
    g.drawImage(img, null, 0, 0);
    return dimg;
  }
}
