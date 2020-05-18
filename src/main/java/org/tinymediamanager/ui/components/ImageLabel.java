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
package org.tinymediamanager.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.scraper.http.InMemoryCachedUrl;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.plaf.TmmTheme;
import org.tinymediamanager.ui.thirdparty.ShadowRenderer;

import com.madgag.gif.fmsware.GifDecoder;

/**
 * The Class ImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ImageLabel extends JComponent {
  public enum Position {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
  }

  private static final long         serialVersionUID       = -2524445544386464158L;
  private static final char         ICON_ID                = '\uF03E';
  private static final Color        EMPTY_BACKGROUND_COLOR = new Color(141, 165, 179);
  private static final Dimension    EMPTY_SIZE             = new Dimension(0, 0);

  protected byte[]                  originalImageBytes;
  protected Dimension               originalImageSize      = EMPTY_SIZE;
  protected Image                   scaledImage;
  protected ImageIcon               animatedGif;

  protected String                  imageUrl;
  protected String                  imagePath;

  protected Position                position               = Position.TOP_LEFT;
  protected boolean                 drawBorder;
  protected boolean                 drawFullWidth;
  protected boolean                 drawShadow;

  protected boolean                 enabledLightbox        = false;
  protected boolean                 preferCache            = true;
  protected boolean                 isLightBox             = false;
  protected float                   desiredAspectRatio     = 0f;
  protected boolean                 cacheUrl               = false;

  protected ShadowRenderer          shadowRenderer;
  protected SwingWorker<Void, Void> worker                 = null;
  protected MouseListener           lightboxListener       = null;

  public ImageLabel() {
    this(true, false);
  }

  public ImageLabel(boolean drawBorder) {
    this(drawBorder, false);
  }

  public ImageLabel(boolean drawBorder, boolean drawFullWidth) {
    this(drawBorder, drawFullWidth, false);
  }

  public ImageLabel(boolean drawBorder, boolean drawFullWidth, boolean drawShadow) {
    super();
    this.drawBorder = drawBorder;
    this.drawFullWidth = drawFullWidth;
    this.drawShadow = drawShadow;
    if (drawShadow) {
      this.shadowRenderer = new ShadowRenderer(8, 0.3f, Color.BLACK);
    }
  }

  public void setOriginalImage(byte[] originalImageBytes) {
    setImageBytes(originalImageBytes);
    recreateScaledImageIfNeeded(0, 0, this.getSize().width, this.getSize().height);
    repaint();
  }

  protected void setImageBytes(byte[] bytes) {
    originalImageBytes = bytes;
  }

  protected void createScaledImage(byte[] originalImageBytes, int width, int height) throws Exception {
    // check if this file is a gif
    GifDecoder decoder = new GifDecoder();
    int status = decoder.read(new ByteArrayInputStream(originalImageBytes));
    if (status == GifDecoder.STATUS_OK && decoder.getFrameCount() > 1) {
      // this is an animated gif
      animatedGif = new ImageIcon(originalImageBytes);
      originalImageSize = new Dimension(decoder.getFrameSize().width, decoder.getFrameSize().height);
      scaledImage = animatedGif.getImage();
      // setIcon(animatedGif);
    }
    else {
      // this is just a normal pic
      BufferedImage originalImage = ImageUtils.createImage(originalImageBytes);
      originalImageSize = new Dimension(originalImage.getWidth(), originalImage.getHeight());
      scaledImage = Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, height, Scalr.OP_ANTIALIAS);
      animatedGif = null;
    }
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

    clearImageData();

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
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }
    clearImageData();
    this.repaint();
  }

  protected void clearImageData() {
    animatedGif = null;
    scaledImage = null;
    originalImageBytes = null;
    originalImageSize = EMPTY_SIZE;
    firePropertyChange("originalImageSize", null, 0);
    firePropertyChange("originalImageBytes", null, new byte[] {});
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

    clearImageData();

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

  /**
   * get a byte array of the original image.<br/>
   * WARNING: this array is only filled _after_ the image has been loaded!
   *
   * @return a byte array of the original (not rescaled) image
   */
  public byte[] getOriginalImageBytes() {
    return originalImageBytes;
  }

  /**
   * get the {@link Dimension} of the original image.<br/>
   * WARNING: the {@link Dimension} is only filled _after_ the image has been loaded!
   *
   * @return the {@link Dimension} original (not rescaled) image
   */
  public Dimension getOriginalImageSize() {
    return originalImageSize;
  }

  @Override
  public Dimension getPreferredSize() {
    if (desiredAspectRatio == 0) {
      // no desired aspect ratio; get the JLabel's preferred size
      return super.getPreferredSize();
    }
    if (originalImageSize != EMPTY_SIZE) {
      return new Dimension(getParent().getWidth(),
          (int) (getParent().getWidth() / (float) originalImageSize.width * (float) originalImageSize.height));
    }
    return new Dimension(getParent().getWidth(), (int) (getParent().getWidth() / desiredAspectRatio) + 1);
  }

  /**
   * This is overridden to return false if the current image is not equal to the passed in Image <code>img</code>.
   *
   * @see java.awt.image.ImageObserver
   * @see java.awt.Component#imageUpdate(java.awt.Image, int, int, int, int, int)
   */
  @Override
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
    if (!isShowing() || scaledImage != img) {
      return false;
    }

    return super.imageUpdate(img, infoflags, x, y, w, h);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (scaledImage != null) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int scaledImageWidth = scaledImage.getWidth(null);
      int scaledImageHeight = scaledImage.getHeight(null);

      // calculate new height/width
      int newWidth;
      int newHeight;

      int offsetX = 0;
      int offsetY = 0;

      if (drawBorder && !drawFullWidth && !drawShadow) {
        Point size = ImageUtils.calculateSize(this.getWidth() - 8, this.getHeight() - 8, originalImageSize.width, originalImageSize.height, true);

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
        recreateScaledImageIfNeeded(scaledImageWidth, scaledImageHeight, newWidth, newHeight);

        g.setColor(Color.BLACK);
        g.drawRect(offsetX, offsetY, size.x + 7, size.y + 7);
        g.setColor(Color.WHITE);
        g.fillRect(offsetX + 1, offsetY + 1, size.x + 6, size.y + 6);
        g.drawImage(scaledImage, offsetX + 4, offsetY + 4, newWidth, newHeight, this);
      }
      else if (drawShadow && !drawFullWidth) {
        Point size = ImageUtils.calculateSize(this.getWidth(), this.getHeight(), originalImageSize.width, originalImageSize.height, true);
        newWidth = size.x;
        newHeight = size.y;

        // when the image size differs too much - reload and rescale the original image
        recreateScaledImageIfNeeded(scaledImageWidth, scaledImageHeight, newWidth - 8, newHeight - 8);

        // did the image reset to null?
        if (scaledImage instanceof BufferedImage) {
          // draw shadow
          BufferedImage shadowImage = shadowRenderer.createShadow((BufferedImage) scaledImage);
          // draw shadow
          g.drawImage(shadowImage, 8, 8, newWidth - 8, newHeight - 8, this);
        }

        // draw image
        g.drawImage(scaledImage, 0, 0, newWidth - 8, newHeight - 8, this);
      }
      else {
        Point size = null;
        if (drawFullWidth) {
          size = new Point(this.getWidth(), this.getWidth() * originalImageSize.height / originalImageSize.width);
        }
        else {
          size = ImageUtils.calculateSize(this.getWidth(), this.getHeight(), originalImageSize.width, originalImageSize.height, true);
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
        recreateScaledImageIfNeeded(scaledImageWidth, scaledImageHeight, newWidth, newHeight);
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
      float fontSize = (float) (Math.min(newWidth, newHeight) * 0.5 / 0.75);

      // draw the _no image found_ icon
      Font font = TmmTheme.FONT_AWESOME.deriveFont(fontSize);
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
        BufferedImage shadowImage = shadowRenderer.createShadow(tmp);

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
    if (animatedGif != null) {
      scaledImage = animatedGif.getImage();
    }
    else if (originalWidth < 20 || originalHeight < 20 || (newWidth * 0.8f > originalWidth) || (originalWidth > newWidth * 1.2f)
        || (newHeight * 0.8f > originalHeight) || (originalHeight > newHeight * 1.2f)) {
      try {
        createScaledImage(originalImageBytes, newWidth, newHeight);
      }
      catch (Exception e) {
        scaledImage = null;
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
  protected class ImageFetcher extends SwingWorker<Void, Void> {
    private Dimension newSize;

    public ImageFetcher(Dimension newSize) {
      this.newSize = newSize;
    }

    @Override
    protected Void doInBackground() {
      try {
        Url url;
        if (cacheUrl) {
          url = new InMemoryCachedUrl(imageUrl);
        }
        else {
          url = new Url(imageUrl);
        }
        byte[] bytes = url.getBytesWithRetry(5);
        clearImageData();
        setImageBytes(bytes);
        recreateScaledImageIfNeeded(0, 0, newSize.width, newSize.height);
      }
      catch (Exception e) {
        imageUrl = "";
        clearImageData();
      }

      return null;
    }

    @Override
    protected void done() {
      if (isCancelled() || !ImageLabel.this.imageUrl.equals(imageUrl)) {
        ImageLabel.this.imageUrl = "";
        clearImageData();
      }
      else {
        // fire events
        ImageLabel.this.firePropertyChange("originalImageBytes", null, originalImageBytes);
        ImageLabel.this.firePropertyChange("originalImageSize", null, originalImageSize);
      }

      revalidate();
      repaint();
    }
  }

  /*
   * inner class for loading local images
   */
  protected class ImageLoader extends SwingWorker<Void, Void> {
    private String    imagePath;
    private Dimension newSize;

    public ImageLoader(String imagePath, Dimension newSize) {
      this.imagePath = imagePath;
      this.newSize = newSize;
    }

    @Override
    protected Void doInBackground() {
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
          byte[] bytes = Files.readAllBytes(file);
          clearImageData();
          setImageBytes(bytes);
          recreateScaledImageIfNeeded(0, 0, newSize.width, newSize.height);
        }
        catch (Exception e) {
          // okay, we got an exception here - set the image path to empty to avoid an endless try-to-reload
          ImageLabel.this.imagePath = "";
          clearImageData();
        }
      }

      return null;
    }

    @Override
    protected void done() {
      if (isCancelled() || !ImageLabel.this.imagePath.equals(imagePath)) {
        ImageLabel.this.imagePath = "";
        clearImageData();
      }
      else {
        // fire events
        ImageLabel.this.firePropertyChange("originalImageBytes", null, originalImageBytes);
        ImageLabel.this.firePropertyChange("originalImageSize", null, originalImageSize);
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
