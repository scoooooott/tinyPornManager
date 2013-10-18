/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.scraper.util.Url;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class ImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ImageLabel extends JLabel {
  public enum Position {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
  }

  private static final long                  serialVersionUID = -2524445544386464158L;
  protected static final ResourceBundle      BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger                LOGGER           = LoggerFactory.getLogger(ImageLabel.class);

  protected BufferedImage                    originalImage;
  protected String                           imageUrl;
  protected String                           imagePath;
  protected Position                         position         = Position.TOP_LEFT;
  protected String                           alternativeText  = null;
  protected boolean                          drawBorder;
  protected boolean                          drawFullWidth;

  protected SwingWorker<BufferedImage, Void> worker           = null;

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

    if (StringUtils.isNotEmpty(oldValue) && oldValue.equals(newValue)) {
      return;
    }

    this.imagePath = newValue;
    firePropertyChange("imagePath", oldValue, newValue);

    if (StringUtils.isBlank(newValue)) {
      originalImage = null;
      this.repaint();
      return;
    }

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    // load image in separate worker -> performance
    worker = new ImageLoader(this.imagePath);
    worker.execute();
  }

  public void clearImage() {
    imagePath = "";
    imageUrl = "";
    originalImage = null;
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
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (originalImage != null) {
      int originalWidth = originalImage.getWidth(null);
      int originalHeight = originalImage.getHeight(null);

      // calculate new height/width
      int newWidth = 0;
      int newHeight = 0;

      int offsetX = 0;
      int offsetY = 0;

      if (drawBorder && !drawFullWidth) {
        Point size = calculateSize(this.getWidth() - 8, this.getHeight() - 8, originalWidth, originalHeight, true);

        // calculate offsets
        if (position == Position.TOP_RIGHT || position == Position.BOTTOM_RIGHT) {
          offsetX = this.getWidth() - size.x - 8;
        }

        if (position == Position.BOTTOM_LEFT || position == Position.BOTTOM_RIGHT) {
          offsetY = this.getHeight() - size.y - 8;
        }

        newWidth = size.x;
        newHeight = size.y;

        g.setColor(Color.BLACK);
        g.drawRect(offsetX, offsetY, size.x + 7, size.y + 7);
        g.setColor(Color.WHITE);
        g.fillRect(offsetX + 1, offsetY + 1, size.x + 6, size.y + 6);
        // g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), offsetX + 4, offsetY + 4, newWidth, newHeight, this);
        g.drawImage(Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, newWidth, newHeight, Scalr.OP_ANTIALIAS), offsetX + 4,
            offsetY + 4, newWidth, newHeight, this);
      }
      else {
        Point size = null;
        if (drawFullWidth) {
          size = new Point(this.getWidth(), this.getWidth() * originalHeight / originalWidth);
        }
        else {
          size = calculateSize(this.getWidth(), this.getHeight(), originalWidth, originalHeight, true);
        }

        // calculate offsets
        if (position == Position.TOP_RIGHT || position == Position.BOTTOM_RIGHT) {
          offsetX = this.getWidth() - size.x;
        }

        if (position == Position.BOTTOM_LEFT || position == Position.BOTTOM_RIGHT) {
          offsetY = this.getHeight() - size.y;
        }

        newWidth = size.x;
        newHeight = size.y;
        // g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), offsetX, offsetY, newWidth, newHeight, this);
        g.drawImage(Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, newWidth, newHeight, Scalr.OP_ANTIALIAS), offsetX,
            offsetY, newWidth, newHeight, this);
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
      String text = "";
      if (alternativeText != null) {
        text = alternativeText;
      }
      else {
        text = BUNDLE.getString("image.nonefound"); //$NON-NLS-1$
      }
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
   * Sets the position.
   * 
   * @param position
   *          the new position
   */
  public void setPosition(Position position) {
    this.position = position;
  }

  /**
   * Sets the alternative text.
   * 
   * @param text
   *          the new alternative text
   */
  public void setAlternativeText(String text) {
    this.alternativeText = text;
  }

  /**
   * The Class ImageFetcher.
   * 
   * @author Manuel Laggner
   */
  protected class ImageFetcher extends SwingWorker<BufferedImage, Void> {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected BufferedImage doInBackground() throws Exception {
      try {
        Url url = new Url(imageUrl);
        Image image = Toolkit.getDefaultToolkit().createImage(url.getBytes());
        return com.bric.image.ImageLoader.createImage(image);

      }
      catch (Exception e) {
        imageUrl = "";
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

  /**
   * The Class ImageLoader.
   */
  protected class ImageLoader extends SwingWorker<BufferedImage, Void> {

    /** The image path. */
    private String imagePath;

    /**
     * Instantiates a new image loader.
     * 
     * @param imagePath
     *          the image path
     */
    public ImageLoader(String imagePath) {
      this.imagePath = imagePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected BufferedImage doInBackground() throws Exception {
      File file = ImageCache.getCachedFile(imagePath);
      if (file != null && file.exists()) {
        try {
          return com.bric.image.ImageLoader.createImage(file);
        }
        catch (Exception e) {
          return null;
        }
      }
      else {
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
