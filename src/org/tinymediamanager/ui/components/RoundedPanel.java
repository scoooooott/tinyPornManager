/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * The Class RoundedPanel - to draw a JPanel with rounded corners and optionally a drop shadow. Source:
 * http://www.codeproject.com/Articles/114959/Rounded-Border-JPanel-JPanel-graphics-improvements
 * 
 * @author b4rc0ll0
 */
public class RoundedPanel extends JPanel {
  private static final long serialVersionUID = -1225971440296084187L;
  /** Stroke size. it is recommended to set it to 1 for better view */
  protected int             strokeSize       = 1;
  /** Color of shadow */
  protected Color           shadowColor      = Color.black;
  /** Sets if it drops shadow */
  protected boolean         shady            = true;
  /** Sets if it has an High Quality view */
  protected boolean         highQuality      = true;
  /** Double values for Horizontal and Vertical radius of corner arcs */
  protected Dimension       arcs             = new Dimension(20, 20);
  /** Distance between shadow border and opaque panel border */
  protected int             shadowGap        = 5;
  /** The offset of shadow. */
  protected int             shadowOffset     = 4;
  /** The transparency value of shadow. ( 0 - 255) */
  protected int             shadowAlpha      = 150;

  public RoundedPanel() {
    super();
    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    int width = getWidth();
    int height = getHeight();
    int shadowGap = this.shadowGap;

    Color shadowColorA = new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), shadowAlpha);
    Graphics2D graphics = (Graphics2D) g;

    // save variables
    Object renderingHint = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

    // Sets antialiasing if HQ.
    if (highQuality) {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    // Draws shadow borders if any.
    if (shady) {
      graphics.setColor(shadowColorA);
      graphics.fillRoundRect(shadowOffset,// X position
          shadowOffset,// Y position
          width - strokeSize - shadowOffset, // width
          height - strokeSize - shadowOffset, // height
          arcs.width, arcs.height);// arc Dimension
    }
    else {
      shadowGap = 1;
    }

    // Draws the rounded opaque panel with borders.
    graphics.setColor(getBackground());
    graphics.fillRoundRect(0, 0, width - shadowGap, height - shadowGap, arcs.width, arcs.height);
    graphics.setColor(getForeground());
    graphics.setStroke(new BasicStroke(strokeSize));
    graphics.drawRoundRect(0, 0, width - shadowGap, height - shadowGap, arcs.width, arcs.height);

    // Sets strokes to default, is better.
    graphics.setStroke(new BasicStroke());

    if (highQuality) {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
    }
  }
}
