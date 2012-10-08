/*
 * Copyright 2012 Manuel Laggner
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
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * The Class TmmTabbedPaneUI.
 */
public class TmmTabbedPaneUI extends BasicTabbedPaneUI {

  /** The select color. */
  private Color selectColor;

  /** The incl tab. */
  private int inclTab = 12;

  /** The ancho foco v. */
  private int anchoFocoV = inclTab;

  /** The ancho foco h. */
  private int anchoFocoH = 4;

  /** The ancho carpetas. */
  private int anchoCarpetas = 18;

  /**
   * En este poligono se guarda la forma de la pesta�a. Es muy importante.
   */
  private Polygon shape;

  /**
   * Creates the ui.
   * 
   * @param c
   *          the c
   * @return the component ui
   */
  public static ComponentUI createUI(JComponent c) {
    return new TmmTabbedPaneUI();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicTabbedPaneUI#installDefaults()
   */
  protected void installDefaults() {
    super.installDefaults();

    selectColor = new Color(255, 192, 192);
    tabAreaInsets.right = anchoCarpetas;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicTabbedPaneUI#layoutLabel(int,
   * java.awt.FontMetrics, int, java.lang.String, javax.swing.Icon,
   * java.awt.Rectangle, java.awt.Rectangle, java.awt.Rectangle, boolean)
   */
  protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect,
      boolean isSelected) {
    Rectangle tabRectPeq = new Rectangle(tabRect);
    // tabRectPeq.width -= inclTab;
    super.layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRectPeq, iconRect, textRect, isSelected);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.plaf.basic.BasicTabbedPaneUI#paintTabArea(java.awt.Graphics,
   * int, int)
   */
  protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
    if (runCount > 1) {
      int lines[] = new int[runCount];
      for (int i = 0; i < runCount; i++) {
        lines[i] = rects[tabRuns[i]].y + (tabPlacement == TOP ? maxTabHeight : 0);
      }

      Arrays.sort(lines);

      if (tabPlacement == TOP) {
        int fila = runCount;
        for (int i = 0; i < lines.length - 1; i++, fila--) {
          Polygon carp = new Polygon();
          carp.addPoint(0, lines[i]);
          carp.addPoint(tabPane.getWidth() - 2 * fila - 2, lines[i]);
          carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i] + 3);

          if (i < lines.length - 2) {
            carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i + 1]);
            carp.addPoint(0, lines[i + 1]);
          } else {
            carp.addPoint(tabPane.getWidth() - 2 * fila, lines[i] + rects[selectedIndex].height);
            carp.addPoint(0, lines[i] + rects[selectedIndex].height);
          }

          carp.addPoint(0, lines[i]);

          g.setColor(hazAlfa(fila));
          g.fillPolygon(carp);

          g.setColor(darkShadow.darker());
          g.drawPolygon(carp);
        }
      } else {
        int fila = 0;
        for (int i = 0; i < lines.length - 1; i++, fila++) {
          Polygon carp = new Polygon();
          carp.addPoint(0, lines[i]);
          carp.addPoint(tabPane.getWidth() - 2 * fila - 1, lines[i]);

          carp.addPoint(tabPane.getWidth() - 2 * fila - 1, lines[i + 1] - 3);
          carp.addPoint(tabPane.getWidth() - 2 * fila - 3, lines[i + 1]);
          carp.addPoint(0, lines[i + 1]);

          carp.addPoint(0, lines[i]);

          g.setColor(hazAlfa(fila + 2));
          g.fillPolygon(carp);

          g.setColor(darkShadow.darker());
          g.drawPolygon(carp);
        }
      }
    }

    super.paintTabArea(g, tabPlacement, selectedIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.plaf.basic.BasicTabbedPaneUI#paintTabBackground(java.awt.Graphics
   * , int, int, int, int, int, int, boolean)
   */
  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    // Este es el primer metodo al que se llama, asi que aqui preparamos el
    // shape que dibujara despues todo...
    Graphics2D g2D = (Graphics2D) g;
    GradientPaint gradientShadow;

    int xp[] = null; // Para la forma
    int yp[] = null;
    switch (tabPlacement) {
      case LEFT:
        xp = new int[] { x, x, x + w, x + w, x };
        yp = new int[] { y, y + h - 3, y + h - 3, y, y };
        gradientShadow = new GradientPaint(x, y, new Color(0, 0, 255), x, y + h, new Color(153, 186, 243)); // Color.ORANGE);
        break;
      case RIGHT:
        xp = new int[] { x, x, x + w - 2, x + w - 2, x };
        yp = new int[] { y, y + h - 3, y + h - 3, y, y };
        gradientShadow = new GradientPaint(x, y, new Color(0, 0, 255), x, y + h, new Color(153, 186, 243));
        break;
      case BOTTOM:
        xp = new int[] { x, x, x + 3, x + w - inclTab - 6, x + w - inclTab - 2, x + w - inclTab, x + w - 3, x };
        yp = new int[] { y, y + h - 3, y + h, y + h, y + h - 1, y + h - 3, y, y };
        gradientShadow = new GradientPaint(x, y, new Color(0, 0, 255), x, y + h, Color.BLUE);
        break;
      case TOP:
      default:
        xp = new int[] { x, x, x + 3, x + w - inclTab - 6, x + w - inclTab - 2, x + w - inclTab, x + w, x };
        yp = new int[] { y + h, y + 3, y, y, y + 1, y + 3, y + h, y + h };
        gradientShadow = new GradientPaint(0, 0, new Color(255, 255, 255), 0, y + h / 4, new Color(153, 186, 243));
        // gradientShadow = new GradientPaint( x, y,Color.WHITE,
        // x, y+h,new Color(0,128,255));
        break;
    }
    ;

    shape = new Polygon(xp, yp, xp.length);

    // Despues ponemos el color que toque
    if (isSelected) {
      // System.out.println("Tab is Selected");
      g2D.setColor(tabPane.getBackground());
      // g2D.setPaint(gradientShadow);
    } else {
      // g2D.setPaint( gradientShadow);
      g2D.setColor(tabPane.getBackgroundAt(tabIndex));
      // g2D.setColor(selectColor);
    }

    // Encima, pintamos la pesta�a con el color que sea
    g2D.fill(shape);

    // Encima, pintamos la pesta�a con el color que le corresponde por
    // profundidad
    if (runCount > 1) {
      g2D.setColor(hazAlfa(getRunForTab(tabPane.getTabCount(), tabIndex) - 1));
      g2D.fill(shape);
    }

    // Y despues, le damos un sombreado que hace que parezca curbada (�A que
    // duele ver algunas faltas de ortografia?)

    g2D.fill(shape);
  }

  /**
   * Este metodo devuelve un tama�o mas grande de lo necesario, haciendoer
   * hueco para la decoracion.
   * 
   * @param tabPlacement
   *          the tab placement
   * @param tabIndex
   *          the tab index
   * @param metrics
   *          the metrics
   * @return the int
   */
  protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    return 8 + inclTab + super.calculateTabWidth(tabPlacement, tabIndex, metrics);
  }

  /**
   * Este metodo devuelve un tama�o mas grande de lo necesario, haciendo el
   * hueco para la decoracion.
   * 
   * @param tabPlacement
   *          the tab placement
   * @param tabIndex
   *          the tab index
   * @param fontHeight
   *          the font height
   * @return the int
   */
  protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
    } else {
      return anchoFocoH + super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
    }
  }

  /**
   * Este metodo dibuja el borde.
   * 
   * @param g
   *          the g
   * @param tabPlacement
   *          the tab placement
   * @param tabIndex
   *          the tab index
   * @param x
   *          the x
   * @param y
   *          the y
   * @param w
   *          the w
   * @param h
   *          the h
   * @param isSelected
   *          the is selected
   */
  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
  }

  /**
   * Este metodo dibuja una se�al amarilla en la solapa que tiene el foco.
   * 
   * @param g
   *          the g
   * @param tabPlacement
   *          the tab placement
   * @param rects
   *          the rects
   * @param tabIndex
   *          the tab index
   * @param iconRect
   *          the icon rect
   * @param textRect
   *          the text rect
   * @param isSelected
   *          the is selected
   */
  protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    if (/* tabPane.hasFocus() && */isSelected) {
      g.setColor(UIManager.getColor("ScrollBar.thumbShadow"));
      g.drawPolygon(shape);
    }
  }

  /**
   * Esta funcion devuelve una sombra mas opaca cuanto mas arriba este la fila.
   * A partir de valores de fila superiores a 7 siempre devuelve el mismo color
   * 
   * @param fila
   *          int la fila a pintar
   * @return the color
   */
  protected Color hazAlfa(int fila) {
    int alfa = 0;
    if (fila >= 0) {
      alfa = 50 + (fila > 7 ? 70 : 10 * fila);
    }

    return new Color(0, 0, 0, alfa);
  }

}
