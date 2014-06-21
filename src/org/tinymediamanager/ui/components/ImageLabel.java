/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.scraper.util.Url;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class ImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ImageLabel extends JLabel {
  public enum Position {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
  }

  private static final long                  serialVersionUID = -2524445544386464158L;
  protected static final ResourceBundle      BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static Font                        FONT;

  protected BufferedImage                    originalImage;
  protected BufferedImage                    scaledImage;
  protected Dimension                        size;
  protected String                           imageUrl;
  protected String                           imagePath;
  protected Position                         position         = Position.TOP_LEFT;
  protected String                           alternativeText  = null;
  protected boolean                          drawBorder;
  protected boolean                          drawFullWidth;
  protected boolean                          enabledLightbox  = false;
  protected boolean                          useCache         = true;

  protected SwingWorker<BufferedImage, Void> worker           = null;
  protected MouseListener                    lightboxListener = null;

  static {
    try {
      JLabel fontLabel = new JLabel("");
      TmmFontHelper.changeFont(fontLabel, 1.5);
      FONT = fontLabel.getFont();
    }
    catch (Exception e) {
      FONT = Font.getFont("Dialog").deriveFont(18f);
    }
  }

  public ImageLabel() {
    super("");
    this.drawBorder = true;
    this.drawFullWidth = false;
  }

  public ImageLabel(boolean drawBorder) {
    super("");
    this.drawBorder = drawBorder;
    this.drawFullWidth = false;
  }

  public ImageLabel(boolean drawBorder, boolean drawFullWidth) {
    super("");
    this.drawBorder = drawBorder;
    this.drawFullWidth = drawFullWidth;
  }

  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String newValue) {
    String oldValue = this.imagePath;

    if (StringUtils.isNotEmpty(oldValue) && oldValue.equals(newValue)) {
      return;
    }

    this.imagePath = newValue;
    firePropertyChange("imagePath", oldValue, newValue);

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    if (StringUtils.isBlank(newValue)) {
      originalImage = null;
      size = null;
      this.repaint();
      return;
    }

    // load image in separate worker -> performance
    worker = new ImageLoader(this.imagePath);
    worker.execute();
  }

  public void clearImage() {
    imagePath = "";
    imageUrl = "";
    originalImage = null;
    size = null;
    this.repaint();
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String newValue) {
    String oldValue = this.imageUrl;
    this.imageUrl = newValue;
    firePropertyChange("imageUrl", oldValue, newValue);

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    if (StringUtils.isEmpty(newValue)) {
      originalImage = null;
      size = null;
      this.repaint();
      return;
    }

    // fetch image in separate worker -> performance
    worker = new ImageFetcher();
    worker.execute();
  }

  private BufferedImage getScaledImage(Dimension size) {
    if (!size.equals(this.size)) {
      // rescale the image
      scaledImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, size.width, size.height, Scalr.OP_ANTIALIAS);
      this.size = size;
    }
    return this.scaledImage;
  }

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
        Point size = ImageCache.calculateSize(this.getWidth() - 8, this.getHeight() - 8, originalWidth, originalHeight, true);

        // calculate offsets
        if (position == Position.TOP_RIGHT || position == Position.BOTTOM_RIGHT) {
          offsetX = this.getWidth() - size.x - 8;
        }

        if (position == Position.BOTTOM_LEFT || position == Position.BOTTOM_RIGHT) {
          offsetY = this.getHeight() - size.y - 8;
        }

        if (position == Position.CENTER) {
          offsetX = (this.getWidth() - size.x - 8) / 2;
          offsetY = (this.getHeight() - size.y - 8) / 2;
        }

        newWidth = size.x;
        newHeight = size.y;

        g.setColor(Color.BLACK);
        g.drawRect(offsetX, offsetY, size.x + 7, size.y + 7);
        g.setColor(Color.WHITE);
        g.fillRect(offsetX + 1, offsetY + 1, size.x + 6, size.y + 6);
        // g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), offsetX + 4, offsetY + 4, newWidth, newHeight, this);
        g.drawImage(getScaledImage(new Dimension(newWidth, newHeight)), offsetX + 4, offsetY + 4, newWidth, newHeight, this);
      }
      else {
        Point size = null;
        if (drawFullWidth) {
          size = new Point(this.getWidth(), this.getWidth() * originalHeight / originalWidth);
        }
        else {
          size = ImageCache.calculateSize(this.getWidth(), this.getHeight(), originalWidth, originalHeight, true);
        }

        // calculate offsets
        if (position == Position.TOP_RIGHT || position == Position.BOTTOM_RIGHT) {
          offsetX = this.getWidth() - size.x;
        }

        if (position == Position.BOTTOM_LEFT || position == Position.BOTTOM_RIGHT) {
          offsetY = this.getHeight() - size.y;
        }

        if (position == Position.CENTER) {
          offsetX = (this.getWidth() - size.x) / 2;
          offsetY = (this.getHeight() - size.y) / 2;
        }

        newWidth = size.x;
        newHeight = size.y;
        // g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), offsetX, offsetY, newWidth, newHeight, this);
        g.drawImage(getScaledImage(new Dimension(newWidth, newHeight)), offsetX, offsetY, newWidth, newHeight, this);
      }
    }
    else {
      // draw border and background
      if (drawBorder) {
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        if (getParent().isOpaque()) {
          g.setColor(getParent().getBackground());
          g.fillRect(1, 1, this.getWidth() - 2, this.getHeight() - 2);
        }
      }

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
      if (!getParent().isOpaque()) {
        text = "";
      }
      Graphics2D g2 = (Graphics2D) g;
      AffineTransform orig = g2.getTransform();
      AffineTransform at = new AffineTransform(orig);
      at.translate(0, this.getHeight());
      at.rotate(this.getWidth(), -this.getHeight());
      g2.setTransform(at);
      g2.setColor(Color.BLACK);
      g2.setFont(FONT);

      FontMetrics fm = g2.getFontMetrics();
      int x = (diagonalSize - fm.stringWidth(text)) / 2;
      int y = (fm.getAscent() - fm.getDescent()) / 2;

      g2.drawString(text, x, y);
      // g2.drawLine(0, 0, diagonalSize, 0);
      at.translate(0, -this.getHeight());
      g2.setTransform(orig);
    }
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public void setAlternativeText(String text) {
    this.alternativeText = text;
  }

  public void enableLightbox() {
    this.enabledLightbox = true;
    if (lightboxListener == null) {
      lightboxListener = new ImageLabelClickListener();
      addMouseListener(lightboxListener);
    }
  }

  public void disableLightbox() {
    this.enabledLightbox = false;
    if (lightboxListener != null) {
      removeMouseListener(lightboxListener);
      lightboxListener = null;
    }
  }

  public void setUseCache(boolean useCache) {
    this.useCache = useCache;
  }

  /*
   * inner class for downloading online images
   */
  protected class ImageFetcher extends SwingWorker<BufferedImage, Void> {
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

    @Override
    protected void done() {
      try {
        // get fetched image
        originalImage = get();
      }
      catch (Exception e) {
        originalImage = null;
      }
      finally {
        size = null;
      }
      repaint();
    }
  }

  /*
   * inner class for loading local images
   */
  protected class ImageLoader extends SwingWorker<BufferedImage, Void> {
    private String imagePath;

    public ImageLoader(String imagePath) {
      this.imagePath = imagePath;
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
      File file = null;
      if (useCache) {
        file = ImageCache.getCachedFile(imagePath);
      }
      else {
        file = new File(imagePath);
      }

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

    @Override
    protected void done() {
      if (isCancelled()) {
        return;
      }
      try {
        // get fetched image
        originalImage = get();
      }
      catch (Exception e) {
        originalImage = null;
      }
      finally {
        size = null;
      }
      revalidate();
      repaint();
    }
  }

  /*
   * click listener for creating a lightbox effect
   */
  private class ImageLabelClickListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent arg0) {
      if (arg0.getClickCount() == 1 && originalImage != null) {
        MainWindow.getActiveInstance().createLightbox(getImagePath(), getImageUrl());
      }
    }
  }
}
