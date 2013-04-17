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
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.bric.image.pixel.Scaling;

/**
 * The Class ImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ImageLabel extends JLabel {

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The Constant logger. */
  private static final Logger         LOGGER           = LoggerFactory.getLogger(ImageLabel.class);

  /** The Constant CACHE_DIR. */
  private static final String         CACHE_DIR        = "cache/image";

  /** The original image. */
  private BufferedImage               originalImage;

  /** The image url. */
  private String                      imageUrl;

  /** The image path. */
  private String                      imagePath;

  /** The draw border. */
  private boolean                     drawBorder;

  /** The draw full width. */
  private boolean                     drawFullWidth;

  /** The url cache dir. */
  private File                        imageCacheDir    = null;

  /** The worker. */
  private ImageFetcher                worker;

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

      // File file = new File(imagePath);
      File file = getCachedFile(imagePath);
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
  @Override
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
      String text = BUNDLE.getString("image.nonefound"); //$NON-NLS-1$
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
   * Gets the cached file name.
   * 
   * @param path
   *          the url
   * @return the cached file name
   */
  private String getCachedFileName(String path) {
    try {
      if (path == null)
        return null;
      // now uses a simple md5 hash, which should have a fairly low collision
      // rate, especially for our
      // limited use
      byte[] key = DigestUtils.md5(path);
      return new String(Hex.encodeHex(key));
    }
    catch (Exception e) {
      LOGGER.error("Failed to create cached filename for image: " + path, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the cache dir.
   * 
   * @return the cache dir
   */
  private File getCacheDir() {
    if (imageCacheDir == null) {
      imageCacheDir = new File(CACHE_DIR);
      if (!imageCacheDir.exists())
        imageCacheDir.mkdirs();
    }
    return imageCacheDir;
  }

  /**
   * Gets the cached file.
   * 
   * @param path
   *          the path
   * @return the cached file
   */
  private File getCachedFile(String path) {
    // try {
    // File originalFile = new File(path);
    // String cacheFilename = getCachedFileName(path);
    // File cachedFile = new File(getCacheDir(), cacheFilename + ".tbn");
    // if (!cachedFile.exists()) {
    // // rescale & cache
    // BufferedImage originalImage =
    // com.bric.image.ImageLoader.createImage(originalFile);
    // Point size = calculateSize((int) (originalImage.getWidth() / 1.5), (int)
    // (originalImage.getHeight() / 1.5), originalImage.getWidth(),
    // originalImage.getHeight(), true);
    // BufferedImage scaledImage = Scaling.scale(originalImage, size.x, size.y);
    //
    // // convert to rgb
    // BufferedImage rgb = new BufferedImage(scaledImage.getWidth(),
    // scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    //
    // ColorConvertOp xformOp = new ColorConvertOp(null);
    // xformOp.filter(scaledImage, rgb);
    //
    // ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
    // ImageWriteParam jpgWrtPrm = imgWrtr.getDefaultWriteParam();
    // jpgWrtPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
    // jpgWrtPrm.setCompressionQuality(0.7f);
    //
    // FileImageOutputStream output = new FileImageOutputStream(cachedFile);
    // imgWrtr.setOutput(output);
    // IIOImage image = new IIOImage(rgb, null, null);
    // imgWrtr.write(null, image, jpgWrtPrm);
    // imgWrtr.dispose();
    //
    // // ImageIO.write(rgb, "jpg", cachedFile);
    // }
    // return cachedFile;
    // }
    // catch (Exception e) {
    // LOGGER.error("cache", e);
    // }

    // fallback - openjdk does not store jpg :(
    return new File(path);
  }

  /**
   * The Class ImageFetcher.
   * 
   * @author Manuel Laggner
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
