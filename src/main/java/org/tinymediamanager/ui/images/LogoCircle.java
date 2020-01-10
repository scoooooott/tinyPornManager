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

package org.tinymediamanager.ui.images;

import static java.awt.MultipleGradientPaint.ColorSpaceType.SRGB;
import static java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * This class has been automatically generated using <a href="https://flamingo.dev.java.net">Flamingo SVG transcoder</a>.
 */
public class LogoCircle implements javax.swing.Icon {
  // default values
  private static int    DEFAULT_WIDTH  = 50;
  private static int    DEFAULT_HEIGHT = 50;

  /** The width of this icon. */
  private int           width;

  /** The height of this icon. */
  private int           height;

  /** The rendered image. */
  private BufferedImage image;

  /**
   * Creates a new transcoded SVG image.
   */
  public LogoCircle() {
    width = DEFAULT_WIDTH;
    height = DEFAULT_HEIGHT;
  }

  /**
   * Creates a new transcoded SVG image.
   */
  public LogoCircle(int size) {
    this.width = size;
    this.height = size;
  }

  @Override
  public int getIconHeight() {
    return height;
  }

  @Override
  public int getIconWidth() {
    return width;
  }

  public Image getImage() {
    if (image == null) {
      image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      double coef = Math.min((double) width / (double) DEFAULT_WIDTH, (double) height / (double) DEFAULT_HEIGHT);

      Graphics2D g2d = image.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.scale(coef, coef);
      paint(g2d);
      g2d.dispose();
    }
    return image;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.drawImage(getImage(), x, y, null);
  }

  /**
   * Paints the transcoded SVG image on the specified graphics context.
   * 
   * @param g
   *          Graphics context.
   */
  private static void paint(Graphics2D g) {
    Shape shape = null;

    float origAlpha = 1.0f;

    java.util.LinkedList<AffineTransform> transformations = new java.util.LinkedList<>();

    //

    // _0

    // _0_0

    // _0_0_0
    shape = new RoundRectangle2D.Double(0, 0, 50, 50, 7.8125, 7.8125);
    g.setPaint(new RadialGradientPaint(new Point2D.Double(0.5, 0.45517972111701965), 0.6055503f, new Point2D.Double(0.5, 0.45517972111701965),
        new float[] { 0, 1 }, new Color[] { new Color(0x494949), new Color(0x303030) }, NO_CYCLE, SRGB, new AffineTransform(50, 0, 0, 50, 0, 0)));
    g.fill(shape);
    transformations.offer(g.getTransform());
    g.transform(new AffineTransform(1, 0, 0, 1, 7, 7));

    // _0_0_1

    // _0_0_1_0
    shape = new GeneralPath();
    ((GeneralPath) shape).moveTo(17.999876, 36.0);
    ((GeneralPath) shape).curveTo(8.074509, 36.0, 0.0, 27.925491, 0.0, 17.999876);
    ((GeneralPath) shape).curveTo(0.0, 8.074261, 8.074509, -3.5527137E-15, 17.999876, -3.5527137E-15);
    ((GeneralPath) shape).curveTo(27.925491, -3.5527137E-15, 36.0, 8.074261, 36.0, 17.999876);
    ((GeneralPath) shape).curveTo(36.0, 27.925491, 27.925491, 36.0, 17.999876, 36.0);
    ((GeneralPath) shape).lineTo(17.999876, 36.0);
    ((GeneralPath) shape).closePath();
    ((GeneralPath) shape).moveTo(21.754723, 4.454225);
    ((GeneralPath) shape).lineTo(19.095743, 16.930326);
    ((GeneralPath) shape).curveTo(18.938835, 17.596188, 18.88819, 18.116066, 18.88819, 18.58356);
    ((GeneralPath) shape).curveTo(18.88819, 20.027006, 19.559513, 20.490776, 21.002958, 20.490776);
    ((GeneralPath) shape).curveTo(23.016932, 20.490776, 24.873997, 18.527203, 25.649841, 15.946925);
    ((GeneralPath) shape).lineTo(27.816998, 15.946925);
    ((GeneralPath) shape).curveTo(24.873997, 24.361565, 19.76533, 25.498396, 16.978739, 25.498396);
    ((GeneralPath) shape).curveTo(13.878582, 25.498396, 11.455212, 23.63587, 11.455212, 19.30156);
    ((GeneralPath) shape).curveTo(11.455212, 18.321634, 11.608395, 17.184555, 11.866596, 15.946925);
    ((GeneralPath) shape).lineTo(14.323731, 4.431881);
    ((GeneralPath) shape).curveTo(8.343635, 6.0550747, 3.9266498, 11.515293, 3.9266498, 17.999876);
    ((GeneralPath) shape).curveTo(3.9266498, 25.75635, 10.239677, 32.06913, 17.999876, 32.06913);
    ((GeneralPath) shape).curveTo(25.760075, 32.06913, 32.07335, 25.75635, 32.07335, 17.999876);
    ((GeneralPath) shape).curveTo(32.07335, 11.541361, 27.695593, 6.1034875, 21.754723, 4.454225);
    ((GeneralPath) shape).lineTo(21.754723, 4.454225);
    ((GeneralPath) shape).closePath();

    g.setPaint(new Color(0xFF7D00));
    g.fill(shape);

    g.setTransform(transformations.poll()); // _0_0_1
  }
}
