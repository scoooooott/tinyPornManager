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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.bric.image.pixel.Scaling;

/**
 * The Class ImageLabel.
 */
public class ImageLabel extends JLabel {

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID = 1L;

  /** The Constant logger. */
  private static final Logger LOGGER           = Logger.getLogger(ImageLabel.class);

  /** The original image. */
  private BufferedImage       originalImage;

  /** The image url. */
  private String              imageUrl;

  /** The image path. */
  private String              imagePath;

  /** The draw border. */
  private boolean             drawBorder;

  /** The draw full width. */
  private boolean             drawFullWidth;

  /** The worker. */
  private ImageFetcher        worker;

  /**
   * Instantiates a new image label.
   */
  public ImageLabel() {
    super("");
    this.drawBorder = true;
    this.drawFullWidth = false;
  }

  /**
   * Instantiates a new image label.
   * 
   * @param drawBorder
   *          the draw border
   */
  public ImageLabel(boolean drawBorder) {
    super("");
    this.drawBorder = drawBorder;
    this.drawFullWidth = false;
  }

  /**
   * Instantiates a new image label.
   * 
   * @param drawBorder
   *          the draw border
   * @param drawFullWidth
   *          the draw full width
   */
  public ImageLabel(boolean drawBorder, boolean drawFullWidth) {
    super("");
    this.drawBorder = drawBorder;
    this.drawFullWidth = drawFullWidth;
  }

  /**
   * Gets the image path.
   * 
   * @return the image path
   */
  public String getImagePath() {
    return imagePath;
  }

  /**
   * Sets the image path.
   * 
   * @param newValue
   *          the new image path
   */
  public void setImagePath(String newValue) {
    String oldValue = this.imagePath;

    if (!StringUtils.isEmpty(oldValue) && oldValue.equals(newValue)) {
      return;
    }

    if (newValue == null) {
      originalImage = null;
    }
    else {

      this.imagePath = newValue;
      firePropertyChange("imagePath", oldValue, newValue);

      if (StringUtils.isEmpty(newValue)) {
        originalImage = null;
        this.repaint();
        return;
      }

      File file = new File(imagePath);
      if (file.exists()) {
        try {
          this.originalImage = com.bric.image.ImageLoader.createImage(file);// ImageIO.read(file);
        }
        catch (Exception e) {
          // LOGGER.error("setImagePath", e);
          originalImage = null;
        }
      }
      else {
        originalImage = null;
      }
    }

    this.repaint();
  }

  /**
   * Gets the image url.
   * 
   * @return the image url
   */
  public String getImageUrl() {
    return imageUrl;
  }

  /**
   * Sets the image url.
   * 
   * @param newValue
   *          the new image url
   */
  public void setImageUrl(String newValue) {
    String oldValue = this.imageUrl;
    this.imageUrl = newValue;
    firePropertyChange("imageUrl", oldValue, newValue);

    if (StringUtils.isEmpty(newValue)) {
      originalImage = null;
      this.repaint();
      return;
    }

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    // fetch image in separate worker -> performance
    worker = new ImageFetcher();
    worker.execute();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (originalImage != null) {
      int originalWidth = originalImage.getWidth(null);
      int originalHeight = originalImage.getHeight(null);

      // calculate new height/width
      int newWidth = 0;
      int newHeight = 0;

      if (drawBorder && !drawFullWidth) {
        Point size = calculateSize(this.getWidth() - 8, this.getHeight() - 8, originalWidth, originalHeight, true);

        newWidth = size.x;
        newHeight = size.y;

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, size.x + 7, size.y + 7);
        g.setColor(Color.WHITE);
        g.fillRect(1, 1, size.x + 6, size.y + 6);
        g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), 4, 4, newWidth, newHeight, this);
      }
      else {
        Point size = null;
        if (drawFullWidth) {
          size = new Point(this.getWidth(), this.getWidth() * originalHeight / originalWidth);
        }
        else {
          size = calculateSize(this.getWidth(), this.getHeight(), originalWidth, originalHeight, true);
        }

        newWidth = size.x;
        newHeight = size.y;
        g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), 0, 0, newWidth, newHeight, this);
      }

    }
    else {
      // draw border and background
      g.setColor(Color.BLACK);
      g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
      g.setColor(getParent().getBackground());
      g.fillRect(1, 1, this.getWidth() - 2, this.getHeight() - 2);

      // calculate diagonal
      int diagonalSize = (int) Math.sqrt(this.getWidth() * this.getWidth() + this.getHeight() * this.getHeight());

      // draw text
      String text = "no image found";
      Graphics2D g2 = (Graphics2D) g;
      AffineTransform orig = g2.getTransform();
      AffineTransform at = new AffineTransform(orig);
      at.translate(0, this.getHeight());
      at.rotate(this.getWidth(), -this.getHeight());
      g2.setTransform(at);
      g2.setColor(Color.BLACK);
      Font font = new Font("Arial", Font.PLAIN, 18);
      g2.setFont(font);

      FontMetrics fm = g2.getFontMetrics();
      int x = (diagonalSize - fm.stringWidth(text)) / 2;
      int y = (fm.getAscent() - fm.getDescent()) / 2;

      g2.drawString(text, x, y);
      // g2.drawLine(0, 0, diagonalSize, 0);
      at.translate(0, -this.getHeight());
      g2.setTransform(orig);
    }
  }

  /**
   * Calculate size.
   * 
   * @param maxWidth
   *          the max width
   * @param maxHeight
   *          the max height
   * @param originalWidth
   *          the original width
   * @param originalHeight
   *          the original height
   * @param respectFactor
   *          the respect factor
   * @return the point
   */
  public static Point calculateSize(int maxWidth, int maxHeight, int originalWidth, int originalHeight, boolean respectFactor) {
    Point size = new Point();
    if (respectFactor) {
      // calculate on available height
      size.y = maxHeight;
      size.x = size.y * originalWidth / originalHeight;

      if (size.x > maxWidth) {
        // calculate on available height
        size.x = maxWidth;
        size.y = size.x * originalHeight / originalWidth;
      }
    }
    else {
      size.x = maxWidth;
      size.y = maxHeight;
    }
    return size;
  }

  /**
   * The Class ImageFetcher.
   */
  private class ImageFetcher extends SwingWorker<BufferedImage, Void> {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected BufferedImage doInBackground() throws Exception {
      try {
        CachedUrl cachedUrl = new CachedUrl(imageUrl);
        Image image = Toolkit.getDefaultToolkit().createImage(cachedUrl.getBytes());
        return com.bric.image.ImageLoader.createImage(image);

      }
      catch (IOException e) {
        return null;
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done() {
      try {
        // get fetched image
        originalImage = get();
      }
      catch (Exception e) {
        originalImage = null;
      }
      repaint();
    }
  }
}
