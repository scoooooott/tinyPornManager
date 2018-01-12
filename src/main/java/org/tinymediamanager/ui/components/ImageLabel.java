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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.scraper.http.CachedUrl;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.thirdparty.ShadowRenderer;

/**
 * The Class ImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ImageLabel extends JLabel {
  public enum Position {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
  }

  private static final long                  serialVersionUID       = -2524445544386464158L;
  private static final char                  ICON_ID                = '\uF03E';
  private static final Color                 EMPTY_BACKGROUND_COLOR = new Color(141, 165, 179);

  protected BufferedImage                    scaledImage;
  protected String                           imageUrl;
  protected String                           imagePath;
  protected Position                         position               = Position.TOP_LEFT;
  protected boolean                          drawBorder;
  protected boolean                          drawFullWidth;
  protected boolean                          enabledLightbox        = false;
  protected boolean                          preferCache            = true;
  protected boolean                          isLightBox             = false;
  protected float                            desiredAspectRatio     = 0f;
  protected boolean                          drawShadow             = false;
  protected boolean                          cacheUrl               = false;

  protected SwingWorker<BufferedImage, Void> worker                 = null;
  protected MouseListener                    lightboxListener       = null;

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

  public ImageLabel(boolean drawBorder, boolean drawFullWidth, boolean drawShadow) {
    super("");
    this.drawBorder = drawBorder;
    this.drawFullWidth = drawFullWidth;
    this.drawShadow = drawShadow;
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

    if (StringUtils.isBlank(newValue)) {
      this.repaint();
      return;
    }

    // load image in separate worker -> performance
    worker = new ImageLoader(this.imagePath, this.getSize());
    worker.execute();
    this.repaint();
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

    if (StringUtils.isEmpty(newValue)) {
      this.repaint();
      return;
    }

    // fetch image in separate worker -> performance
    worker = new ImageFetcher(this.getSize());
    worker.execute();
    this.repaint();
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

      if (drawBorder && !drawFullWidth && !drawShadow) {
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
      else if (drawShadow && !drawFullWidth) {
        Point size = ImageCache.calculateSize(this.getWidth(), this.getHeight(), originalWidth, originalHeight, true);
        newWidth = size.x;
        newHeight = size.y;

        // when the image size differs too much - reload and rescale the original image
        recreateScaledImageIfNeeded(originalWidth, originalHeight, this.getWidth() - 8, this.getHeight() - 8);

        // draw shadow
        ShadowRenderer shadow = new ShadowRenderer(8, 0.3f, Color.BLACK);
        BufferedImage shadowImage = shadow.createShadow(scaledImage);

        // draw shadow
        g.drawImage(shadowImage, 8, 8, newWidth - 8, newHeight - 8, this);
        // draw image
        g.drawImage(scaledImage, 0, 0, newWidth - 8, newHeight - 8, this);
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
    // do not draw the "no image found" icon if the worker is loading or in lightbox usage
    else if (!isLoading() && !isLightBox) {
      // nothing to draw; draw the _no image found_ indicator
      int newWidth;
      int newHeight;

      if (drawShadow) {
        newWidth = this.getWidth() - 8;
        newHeight = this.getHeight() - 8;
      }
      else {
        newWidth = this.getWidth();
        newHeight = this.getHeight();
      }

      // calculate the optimal font size; the pt is about 0.75 * the needed px
      // we draw that icon at max 50% of the available space
      int fontSize = (int) ((newWidth < newHeight ? newWidth : newHeight) * 0.5 / 0.75);

      // draw the _no image found_ icon
      Font font = new Font("Font Awesome 5 Pro Regular", Font.PLAIN, fontSize);
      BufferedImage tmp = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(tmp);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setColor(EMPTY_BACKGROUND_COLOR);
      g2.fillRect(0, 0, newWidth, newHeight);

      g2.setFont(font);
      g2.setColor(UIManager.getColor("Panel.background"));
      Rectangle2D bounds = font.createGlyphVector(g2.getFontRenderContext(), String.valueOf(ICON_ID)).getVisualBounds();
      int iconWidth = (int) Math.ceil(bounds.getWidth()) + 2; // +2 to avoid clipping problems
      int iconHeight = (int) Math.ceil(bounds.getHeight()) + 2; // +2 to avoid clipping problems
      g2.drawString(String.valueOf(ICON_ID), (newWidth - iconWidth) / 2, (newHeight + iconHeight) / 2);

      g2.dispose();

      if (drawShadow) {
        ShadowRenderer shadow = new ShadowRenderer(8, 0.3f, Color.BLACK);
        BufferedImage shadowImage = shadow.createShadow(tmp);

        // draw shadow
        g.drawImage(shadowImage, 8, 8, newWidth, newHeight, this);
      }

      // draw image
      g.drawImage(tmp, 0, 0, newWidth, newHeight, this);
    }
  }

  protected boolean isLoading() {
    return worker != null && !worker.isDone();
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

  public void setPreferCache(boolean preferCache) {
    this.preferCache = preferCache;
  }

  public void setIsLightbox(boolean value) {
    this.isLightBox = value;
  }

  /**
   * should the url get cached for this session
   * 
   * @param cacheUrl
   *          true if the image behind this url should be cache in this session
   */
  public void setCacheUrl(boolean cacheUrl) {
    this.cacheUrl = cacheUrl;
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
    protected BufferedImage doInBackground() {
      try {
        Url url;
        if (cacheUrl) {
          url = new CachedUrl(imageUrl);
        }
        else {
          url = new Url(imageUrl);
        }
        return Scalr.resize(ImageCache.createImage(url.getBytes()), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, newSize.width, newSize.height,
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
    protected BufferedImage doInBackground() {
      Path file = null;

      // we prefer reading it from the cache
      if (preferCache) {
        file = ImageCache.getCachedFile(Paths.get(imagePath));
      }

      // not in the cache - read it from the path
      if (file == null) {
        file = Paths.get(imagePath);
      }

      // not available in the path and not preferred from the cache..
      // well just try to read it from the cache
      if ((file == null || !Files.exists(file)) && !preferCache) {
        file = ImageCache.getCachedFile(Paths.get(imagePath));
      }

      if (file != null && Files.exists(file)) {
        try {
          return Scalr.resize(ImageCache.createImage(file), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, newSize.width, newSize.height,
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
