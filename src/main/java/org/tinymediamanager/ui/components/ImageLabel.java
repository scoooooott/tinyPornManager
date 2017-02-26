/*
 * Copyright 2012 - 2017 Manuel Laggner
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
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.scraper.http.Url;
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

  private static final long                  serialVersionUID   = -2524445544386464158L;
  protected static final ResourceBundle      BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static Font                        FONT;

  protected BufferedImage                    scaledImage;
  protected String                           imageUrl;
  protected String                           imagePath;
  protected Position                         position           = Position.TOP_LEFT;
  protected String                           alternativeText    = null;
  protected boolean                          drawBorder;
  protected boolean                          drawFullWidth;
  protected boolean                          enabledLightbox    = false;
  protected boolean                          useCache           = true;
  protected float                            desiredAspectRatio = 0f;

  protected SwingWorker<BufferedImage, Void> worker             = null;
  protected MouseListener                    lightboxListener   = null;

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

    scaledImage = null;
    this.repaint();

    if (StringUtils.isBlank(newValue)) {
      return;
    }

    // load image in separate worker -> performance
    worker = new ImageLoader(this.imagePath, this.getSize());
    worker.execute();
  }

  public void clearImage() {
    imagePath = "";
    imageUrl = "";
    scaledImage = null;
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

    scaledImage = null;
    this.repaint();

    if (StringUtils.isEmpty(newValue)) {
      return;
    }

    // fetch image in separate worker -> performance
    worker = new ImageFetcher(this.getSize());
    worker.execute();
  }

  public void setDesiredAspectRatio(float desiredAspectRatio) {
    this.desiredAspectRatio = desiredAspectRatio;
  }

  public float getDesiredAspectRatio() {
    return desiredAspectRatio;
  }

  @Override
  public Dimension getPreferredSize() {
    if (desiredAspectRatio == 0) {
      // no desired aspect ratio; get the JLabel's preferred size
      return super.getPreferredSize();
    }
    if (scaledImage != null) {
      return new Dimension(getParent().getWidth(), (int) (getParent().getWidth() / (float) scaledImage.getWidth() * (float) scaledImage.getHeight()));
    }
    return new Dimension(getParent().getWidth(), (int) (getParent().getWidth() / desiredAspectRatio) + 1);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (scaledImage != null) {
      int originalWidth = scaledImage.getWidth(null);
      int originalHeight = scaledImage.getHeight(null);

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

        // when the image size differs too much - reload and rescale the original image
        recreateScaledImageIfNeeded(originalWidth, originalHeight, newWidth, newHeight);

        g.setColor(Color.BLACK);
        g.drawRect(offsetX, offsetY, size.x + 7, size.y + 7);
        g.setColor(Color.WHITE);
        g.fillRect(offsetX + 1, offsetY + 1, size.x + 6, size.y + 6);
        // g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), offsetX + 4, offsetY + 4, newWidth, newHeight, this);
        g.drawImage(scaledImage, offsetX + 4, offsetY + 4, newWidth, newHeight, this);
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

        // when the image size differs too much - reload and rescale the original image
        recreateScaledImageIfNeeded(originalWidth, originalHeight, newWidth, newHeight);

        // g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), offsetX, offsetY, newWidth, newHeight, this);
        g.drawImage(scaledImage, offsetX, offsetY, newWidth, newHeight, this);
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
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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

  private void recreateScaledImageIfNeeded(int originalWidth, int originalHeight, int newWidth, int newHeight) {
    if ((newWidth * 0.8f > originalWidth) || (originalWidth > newWidth * 1.2f) || (newHeight * 0.8f > originalHeight)
        || (originalHeight > newHeight * 1.2f) && newWidth > 10) {
      if (StringUtils.isNotBlank(imagePath)) {
        worker = new ImageLoader(imagePath, new Dimension(newWidth, newHeight));
        worker.execute();
      }
      else if (StringUtils.isNoneBlank(imageUrl)) {
        worker = new ImageFetcher(new Dimension(newWidth, newHeight));
        worker.execute();
      }
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
    private Dimension newSize;

    public ImageFetcher(Dimension newSize) {
      this.newSize = newSize;
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
      try {
        Url url = new Url(imageUrl);
        return Scalr.resize(ImageCache.createImage(url.getBytes()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, newSize.width, newSize.height,
            Scalr.OP_ANTIALIAS);
      }
      catch (Exception e) {
        imageUrl = "";
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
        scaledImage = get();
      }
      catch (Exception e) {
        scaledImage = null;
      }
      repaint();
    }
  }

  /*
   * inner class for loading local images
   */
  protected class ImageLoader extends SwingWorker<BufferedImage, Void> {
    private String    imagePath;
    private Dimension newSize;

    public ImageLoader(String imagePath, Dimension newSize) {
      this.imagePath = imagePath;
      this.newSize = newSize;
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
      Path file = null;
      if (useCache) {
        file = ImageCache.getCachedFile(Paths.get(imagePath));
      }
      else {
        file = Paths.get(imagePath);
      }

      if (file != null && Files.exists(file)) {
        try {
          return Scalr.resize(ImageCache.createImage(file), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, newSize.width, newSize.height,
              Scalr.OP_ANTIALIAS);
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
        scaledImage = get();
      }
      catch (Exception e) {
        scaledImage = null;
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
      if (arg0.getClickCount() == 1 && scaledImage != null) {
        MainWindow.getActiveInstance().createLightbox(getImagePath(), getImageUrl());
      }
    }
  }
}
