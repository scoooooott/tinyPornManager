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
package org.tinymediamanager.ui.panels;

import static org.tinymediamanager.ui.thirdparty.GraphicsUtilities.createCompatibleImage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.tinymediamanager.ui.thirdparty.ShadowRenderer;

/**
 * The Class RoundedPanel - to draw a JPanel with rounded corners and optionally a drop shadow
 *
 * @author Manuel Laggner
 */
public class RoundedPanel extends JPanel {
  private static final long serialVersionUID = -1225971440296084187L;
  protected Dimension       arcs             = new Dimension(20, 20);

  protected boolean         drawShadow;
  protected Color           shadowColor;
  protected int             shadowSize;
  protected float           shadowOpacity;
  protected Insets          insets;

  protected ShadowRenderer  shadowRenderer;
  protected BufferedImage   shadowBuffer;

  public RoundedPanel() {
    this(true, Color.BLACK, 0.5f, 8);
  }

  public RoundedPanel(boolean drawShadow, Color shadowColor, float shadowOpacity, int shadowSize) {
    super();
    setOpaque(false);
    this.shadowOpacity = shadowOpacity;
    this.drawShadow = drawShadow;
    this.shadowColor = shadowColor;
    this.shadowSize = shadowSize;
    this.shadowRenderer = new ShadowRenderer(shadowSize, shadowOpacity, shadowColor);
    this.shadowRenderer.setSize(shadowSize);
    insets = new Insets(0, shadowSize, shadowSize, shadowSize);
  }

  @Override
  public java.awt.Insets getInsets() {
    Insets insets = super.getInsets();
    insets.top = this.insets.top + insets.top;
    insets.left = this.insets.left + insets.left;
    insets.bottom = this.insets.bottom + insets.bottom;
    insets.right = this.insets.right + insets.right;
    return insets;
  }

  @Override
  protected void paintComponent(Graphics g) {
    int width = getWidth() - 1;
    int height = getHeight() - 1;

    Graphics2D g2d = (Graphics2D) g.create();
    setRenderingHints(g2d);

    Insets insets = getInsets();
    Rectangle bounds = getBounds();

    bounds.x = insets.left;
    bounds.y = insets.top;
    bounds.width = width - (insets.left + insets.right);
    bounds.height = height - (insets.top + insets.bottom);
    RoundRectangle2D shape = new RoundRectangle2D.Float(bounds.x, bounds.y, bounds.width, bounds.height, arcs.width, arcs.height);

    if (drawShadow) {
      if (shadowBuffer == null || (bounds.width + 2 * shadowSize) != shadowBuffer.getWidth()
          || (bounds.getHeight() + shadowSize != shadowBuffer.getHeight())) {
        BufferedImage img = createCompatibleImage(bounds.width, bounds.height);
        RoundRectangle2D shadowShape = new RoundRectangle2D.Float(bounds.x, bounds.y, bounds.width + shadowSize, bounds.height - shadowSize,
            arcs.width, arcs.height);
        Graphics2D tg2d = img.createGraphics();
        setRenderingHints(g2d);
        tg2d.setColor(Color.BLACK);
        tg2d.translate(-2 * bounds.x, -bounds.y);
        tg2d.fill(shadowShape);
        tg2d.dispose();
        shadowBuffer = shadowRenderer.createShadow(img);
      }

      g2d.drawImage(shadowBuffer, 0, 0, this);
    }

    g2d.setColor(getBackground());
    g2d.fill(shape);

    getUI().paint(g2d, this);

    g2d.setColor(getForeground());
    g2d.draw(shape);
    g2d.dispose();
  }

  private void setRenderingHints(Graphics2D g2d) {
    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
  }
}
