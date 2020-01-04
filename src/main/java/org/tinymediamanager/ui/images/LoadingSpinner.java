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

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * The class LoadingSpinner is used to display a loading spinner.
 *
 * @author Manuel Laggner Created by laggner on 04.04.2016.
 */
public class LoadingSpinner implements Icon {
  private int        width;
  private int        height;
  private Area[]     ticker;
  private Area[]     shadow;
  private Timer      timer;
  private JComponent parent;
  private int        barsCount       = 14;

  private Color      baseColorLow    = new Color(80, 80, 80);
  private Color      baseColorHigh   = new Color(120, 120, 120);

  private Color      customColorLow  = null;
  private Color      customColorHigh = null;

  private boolean    active          = false;

  public LoadingSpinner(int size, JComponent parent) {
    this.width = this.height = size;
    this.parent = parent;
    ticker = buildTicker();
    shadow = buildShadow();

    int fps = 10;
    timer = new Timer(1000 / fps, new TimerTickActionListener());
  }

  public void setCustomColors(Color low, Color high) {
    customColorLow = low;
    customColorHigh = high;
    parent.repaint();
  }

  public void resetCustomColor() {
    customColorHigh = customColorLow = null;
    parent.repaint();
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // create offset to match the parents align
    g2.translate(x, y);

    g2.setColor(new Color(255, 255, 255, 0));
    g2.fillRect(0, 0, width - 1, height - 1);

    // active = rotating circle
    if (active) {
      // draw shadow
      for (int i = 0; i < shadow.length; i++) {
        int alpha = (int) (150 * (1 - i / (shadow.length * 2.0f / 3.0f)));
        if (alpha < 0) {
          alpha = 0;
        }
        Color shadowColor = new Color(0, 0, 0, alpha);
        g2.setColor(shadowColor);
        g2.fill(shadow[i]);
      }

      // draw spinner
      for (int i = 0; i < ticker.length; i++) {
        float factor = (1 - 1 / 2.0f * i / (float) (ticker.length - 1));

        int alpha = (int) (255 * (1 - i / (ticker.length * 2.0f / 3.0f)));
        if (alpha < 0) {
          alpha = 0;
        }
        Color[] colors = calculateColors(factor, alpha);
        g2.setPaint(new GradientPaint(width / 2, 0, colors[1], width / 2, height, colors[0]));
        // g2.setColor(new Color(color, color, color, alpha));
        g2.fill(ticker[i]);
      }
    }
    else {
      // draw shadow
      Color shadowColor = new Color(0, 0, 0, 150);
      g2.setColor(shadowColor);
      for (Area aShadow : shadow) {
        g2.fill(aShadow);
      }

      // draw spinner
      for (Area aTicker : ticker) {
        Color[] colors = calculateColors(1, 255);
        g2.setPaint(new GradientPaint(width / 2, 0, colors[1], width / 2, height, colors[0]));
        g2.fill(aTicker);
      }
    }
  }

  private Color[] calculateColors(float factor, int alpha) {
    Color[] colors = new Color[2];
    Color low;
    Color high;

    if (factor == 1.0f && alpha == 255) {
      // no need for calculating a color
      if (customColorLow != null && customColorHigh != null) {
        low = customColorLow;
        high = customColorHigh;
      }
      else {
        low = baseColorLow;
        high = baseColorHigh;
      }
    }
    else {
      // re-calculate the color
      if (customColorLow != null && customColorHigh != null) {
        low = new Color((int) (customColorLow.getRed() * factor), (int) (customColorLow.getGreen() * factor),
            (int) (customColorLow.getBlue() * factor), alpha);
        high = new Color((int) (customColorHigh.getRed() * factor), (int) (customColorHigh.getGreen() * factor),
            (int) (customColorHigh.getBlue() * factor), alpha);
      }
      else {
        low = new Color((int) (baseColorLow.getRed() * factor), (int) (baseColorLow.getGreen() * factor), (int) (baseColorLow.getBlue() * factor),
            alpha);
        high = new Color((int) (baseColorHigh.getRed() * factor), (int) (baseColorHigh.getGreen() * factor), (int) (baseColorHigh.getBlue() * factor),
            alpha);
      }
    }

    colors[0] = low;
    colors[1] = high;
    return colors;
  }

  @Override
  public int getIconWidth() {
    return width;
  }

  @Override
  public int getIconHeight() {
    return height;
  }

  public void start() {
    if (!active) {
      active = true;
      timer.start();
    }
  }

  public void stop() {
    if (active) {
      active = false;
      timer.stop();
    }
    parent.repaint();
  }

  private Area[] buildTicker() {
    Area[] ticker = new Area[barsCount];
    Point2D.Double center = new Point2D.Double((double) width / 2, (double) (height / 2));
    double fixedAngle = 2.0 * Math.PI / ((double) barsCount);

    for (double i = 0.0; i < (double) barsCount; i++) {
      Area primitive = new Area(new RoundRectangle2D.Double(0, 0, width / 4, height / 15, height / 15, height / 15));

      AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
      AffineTransform toBorder = AffineTransform.getTranslateInstance(width / 10, -height / 15);
      AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());

      AffineTransform toWheel = new AffineTransform();
      toWheel.concatenate(toCenter);
      toWheel.concatenate(toBorder);

      primitive.transform(toWheel);
      primitive.transform(toCircle);

      ticker[(int) i] = primitive;
    }

    return ticker;
  }

  private Area[] buildShadow() {
    Area[] shadow = new Area[barsCount];
    Point2D.Double center = new Point2D.Double((double) width / 2, (double) (height / 2));
    double fixedAngle = 2.0 * Math.PI / ((double) barsCount);

    for (double i = 0.0; i < (double) barsCount; i++) {
      Area primitive = new Area(new RoundRectangle2D.Double(0, 0, width / 4, height / 15, height / 15, height / 15));

      AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY() + 1);
      AffineTransform toBorder = AffineTransform.getTranslateInstance(width / 10, -height / 15);
      AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY() + 1);

      AffineTransform toWheel = new AffineTransform();
      toWheel.concatenate(toCenter);
      toWheel.concatenate(toBorder);

      primitive.transform(toWheel);
      primitive.transform(toCircle);

      shadow[(int) i] = primitive;
    }

    return shadow;
  }

  /*
   * this class is used to react on the timer ticks
   */
  private class TimerTickActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      // Rotate the spinner by one tick
      Point2D.Double center = new Point2D.Double((double) width / 2, (double) height / 2);
      double fixedIncrement = 2.0 * Math.PI / ((double) barsCount);
      AffineTransform toCircle = AffineTransform.getRotateInstance(fixedIncrement, center.getX(), center.getY());

      for (Area aTicker : ticker) {
        aTicker.transform(toCircle);
      }

      // rotate the shadow by one tick
      toCircle = AffineTransform.getRotateInstance(fixedIncrement, center.getX(), center.getY() + 1);
      for (Area aShadow : shadow) {
        aShadow.transform(toCircle);
      }

      parent.repaint();
    }
  }
}
