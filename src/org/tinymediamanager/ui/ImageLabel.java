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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.bric.image.pixel.Scaling;

/**
 * The Class ImageLabel.
 */
public class ImageLabel extends JLabel {

  /** The Constant logger. */
  private static final Logger logger = Logger.getLogger(ImageLabel.class);

  /** The original image. */
  private BufferedImage originalImage;

  /** The image url. */
  private String imageUrl;

  /** The image path. */
  private String imagePath;

  /**
   * Instantiates a new image label.
   */
  public ImageLabel() {
    super("");
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
    this.imagePath = newValue;
    firePropertyChange("imagePath", oldValue, newValue);

    if (newValue == null) {
      originalImage = null;
      this.repaint();
      return;
    }

    File file = new File(imagePath);
    if (file.exists()) {
      this.originalImage = com.bric.image.ImageLoader.createImage(file);// ImageIO.read(file);
    } else {
      originalImage = null;
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

    if (newValue == null) {
      originalImage = null;
      this.repaint();
      return;
    }

    try {
      CachedUrl cachedUrl = new CachedUrl(imageUrl);
      this.originalImage = com.bric.image.ImageLoader.createImage(cachedUrl.getUrl());// ImageIO.read(cachedUrl.getInputStream(null,
                                                                                      // true));

      this.repaint();
    } catch (IOException e) {
      logger.error(e.getStackTrace());
    }
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
      Point size = calculateSize(this.getWidth() - 8, this.getHeight() - 8, originalWidth, originalHeight, true);
      int newWidth = size.x;
      int newHeight = size.y;

      g.setColor(Color.BLACK);
      g.drawRect(0, 0, size.x + 7, size.y + 7);
      g.setColor(Color.WHITE);
      g.fillRect(1, 1, size.x + 6, size.y + 6);
      g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), 4, 4, newWidth, newHeight, this);
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
    } else {
      size.x = maxWidth;
      size.y = maxHeight;
    }
    return size;
  }

}
