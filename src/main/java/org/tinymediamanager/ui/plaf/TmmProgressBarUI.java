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

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.BaseProgressBarUI;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * @author Manuel Laggner
 */
public class TmmProgressBarUI extends BaseProgressBarUI {

  private static final int PROGRESS_BAR_WIDTH = 6;

  public static ComponentUI createUI(JComponent c) {
    return new TmmProgressBarUI();
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);
    progressBar.setOpaque(false);
  }

  @Override
  protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
    // no string to be painted here
  }

  @Override
  protected void paintDeterminate(Graphics g, JComponent c) {
    if (!(g instanceof Graphics2D)) {
      return;
    }
    Graphics2D g2D = (Graphics2D) g;
    Composite savedComposite = g2D.getComposite();
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets b = progressBar.getInsets(); // area for border
    int w = progressBar.getWidth() - (b.right + b.left);
    int h = progressBar.getHeight() - (b.top + b.bottom);

    // amount of progress to draw
    int amountFull = getAmountFull(b, w, h);

    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      // calculate the origin for the progress bar
      int y = b.top + (h - PROGRESS_BAR_WIDTH) / 2;

      // draw background
      g2D.setColor(progressBar.getBackground());
      g2D.fillRoundRect(b.left, y, w, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH);

      g2D.setColor(progressBar.getForeground());
      if (JTattooUtilities.isLeftToRight(progressBar)) {
        g2D.fillRoundRect(b.left, y, amountFull, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH);
      }
      else {
        g2D.fillRoundRect(progressBar.getWidth() - amountFull - b.right, y, b.right, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH);
      }
    }
    else { // VERTICAL
      // calculate the origin for the progress bar
      int x = b.left + (w - PROGRESS_BAR_WIDTH) / 2;

      // draw background
      g2D.setColor(progressBar.getBackground());
      g2D.fillRoundRect(x, b.top, w, h, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH);

      g2D.setColor(progressBar.getForeground());
      g2D.fillRoundRect(x, b.top, w, h - amountFull, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH);
    }

    g2D.setComposite(savedComposite);
    g2D.setRenderingHints(savedRenderingHints);
  }

  @Override
  protected void paintIndeterminate(Graphics g, JComponent c) {
    if (!(g instanceof Graphics2D)) {
      return;
    }
    Graphics2D g2D = (Graphics2D) g;
    Composite savedComposite = g2D.getComposite();
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets b = progressBar.getInsets(); // area for border
    int w = progressBar.getWidth() - (b.right + b.left);
    int h = progressBar.getHeight() - (b.top + b.bottom);

    if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
      // calculate the origin for the progress bar
      int y = b.top + (h - PROGRESS_BAR_WIDTH) / 2;

      // draw background
      g2D.setColor(progressBar.getForeground());
      Area background = new Area(new RoundRectangle2D.Float(b.left, y, w, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH));
      g2D.fill(background);

      // Paint the striped box.
      boxRect = getBox(boxRect);
      if (boxRect != null) {
        w = 20;
        int x = getAnimationIndex();
        GeneralPath p = new GeneralPath();

        p.moveTo(boxRect.x, boxRect.y + boxRect.height);
        p.lineTo(boxRect.x + w * .5f, boxRect.y + boxRect.height);
        p.lineTo(boxRect.x + w, boxRect.y);
        p.lineTo(boxRect.x + w * .5f, boxRect.y);

        p.closePath();
        g2D.setColor(progressBar.getBackground());

        for (int i = boxRect.width + x; i > -w; i -= w) {
          Area bar = new Area(AffineTransform.getTranslateInstance(i, 0).createTransformedShape(p));
          bar.intersect(background);
          g2D.fill(bar);
        }
      }
    }
    else { // VERTICAL
      // calculate the origin for the progress bar
      int x = b.left + (w - PROGRESS_BAR_WIDTH) / 2;

      // not implemented

      // draw background
      g2D.setColor(progressBar.getForeground());
      g2D.fillRoundRect(x, b.top, w, h, PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH);
    }

    g2D.setComposite(savedComposite);
    g2D.setRenderingHints(savedRenderingHints);
  }

  @Override
  protected int getBoxLength(int availableLength, int otherDimension) {
    return availableLength; // (int) Math.round(availableLength / 6d);
  }
}
