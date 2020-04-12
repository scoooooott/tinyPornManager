package org.tinymediamanager.thirdparty;

/*
 * @(#)ImageLoader.java
 *
 * $Date$
 *
 * Copyright (c) 2011 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can convert an abstract <code>Image</code> into a ARGB <code>BufferedImage</code>.
 * <P>
 * It was written to replace the <code>MediaTracker</code>; on my Macs this class is faster and more efficient. It has come to my attention that on
 * Linux this may not be the case (see my <A HREF="http://javagraphics.blogspot.com/2007/04/images-studying-mediatracker.html">blog</A> for more
 * discussion.)
 * <P>
 * Also this class has the added advantage of always returning an RGB image. Using other methods (such as ImageIO) may return an arbitrary image type.
 * (And this class doesn't require a <code>java.awt.Component</code> to initialize; why does the MediaTracker do that? It's just a strange animal.)
 * 
 */
public class ImageLoader {
  private static final Logger           LOGGER    = LoggerFactory.getLogger(ImageLoader.class);

  private static final DirectColorModel ARGBModel = (DirectColorModel) ColorModel.getRGBdefault();
  private static final DirectColorModel RGBModel  = new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0);

  public static BufferedImage createImage(URL url) {
    if (url == null) {
      throw new NullPointerException();
    }
    try {
      return createImage(Toolkit.getDefaultToolkit().createImage(url), url.toString());
    }
    catch (RuntimeException e) {
      LOGGER.debug("could not create image: {}", e.getMessage());
      throw e;
    }
  }

  public static BufferedImage createImage(File file) {
    try {
      return createImage(Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath()), file.getAbsolutePath());
    }
    catch (RuntimeException e) {
      LOGGER.debug("could not create image: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * This returns an ARGB BufferedImage depicting the argument <code>i</code>.
   * <P>
   * Note that if <code>i</code> is already an ARGB BufferedImage, then it is immediately returned and this method does NOT duplicate it.
   * 
   * @param i
   *          the source image to create the buffered image of
   * @return an ARGB BufferedImage identical to the argument.
   */
  public static BufferedImage createImage(Image i) {
    if (i instanceof BufferedImage) {
      BufferedImage bi = (BufferedImage) i;
      int type = bi.getType();
      if (type == BufferedImage.TYPE_INT_ARGB) {
        return bi;
      }
      BufferedImage newImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = newImage.createGraphics();
      g.drawImage(bi, 0, 0, null);
      g.dispose();
      return newImage;
    }
    try {
      return createImage(i, null);
    }
    catch (RuntimeException e) {
      LOGGER.debug("could not create image: {}", e.getMessage());
      throw e;
    }
  }

  protected static BufferedImage createImage(Image i, String description) {
    ImageLoader l = new ImageLoader(i.getSource(), null, description);
    return l.getImage();
  }

  /**
   * This checks to see if two DirectColorModels are identical. Apparently the "equals" method in DirectColorModel doesn't really work.
   */
  private static boolean equals(DirectColorModel d1, DirectColorModel d2) {
    if (d1.getAlphaMask() != d2.getAlphaMask()) {
      return false;
    }
    if (d1.getGreenMask() != d2.getGreenMask()) {
      return false;
    }
    if (d1.getRedMask() != d2.getRedMask()) {
      return false;
    }
    if (d1.getBlueMask() != d2.getBlueMask()) {
      return false;
    }
    if (d1.getColorSpace() != d2.getColorSpace()) {
      return false;
    }
    if (d1.isAlphaPremultiplied() != d2.isAlphaPremultiplied()) {
      return false;
    }
    if (d1.getTransferType() != d2.getTransferType()) {
      return false;
    }
    if (d1.getTransparency() != d2.getTransparency()) {
      return false;
    }
    return true;
  }

  InnerImageConsumer   consumer;
  List<ChangeListener> listeners;
  ImageProducer        producer;
  float                progress = 0;
  String               description;

  /**
   * This constructs an ImageLoader. As soon as an ImageLoader is constructed the <code>ImageProducer</code> is asked to start producing data.
   * (However constructing this object will not block waiting on the image data.)
   * 
   * @param p
   *          the source of this image
   * @param changeListener
   *          an optional <code>ChangeListener</code>. This will be notified when a change occurs. It can be added here in the constructor because
   *          loading the image begins immediately; depending on how <code>ImageProducer.startProduction</code> is implemented this <i>may</i> be a
   *          blocking call so the <code>ChangeListener</code> needs to be added before the loading begins
   * @param description
   *          an optional description that may be useful for debugging
   */
  public ImageLoader(ImageProducer p, ChangeListener changeListener, String description) {
    producer = p;
    addChangeListener(changeListener);
    consumer = new InnerImageConsumer();
    p.startProduction(consumer);
    this.description = description;
  }

  /**
   * Adds a ChangeListener to this loader. This listener will be notified either when the image is fully loaded/created, or when
   * <code>getProgress()</code> changes value.
   */
  public void addChangeListener(ChangeListener l) {
    if (l == null) {
      return;
    }

    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    if (listeners.contains(l)) {
      return;
    }
    listeners.add(l);
  }

  /**
   * Removes a ChangeListener from this loader.
   */
  public void removeChangeListener(ChangeListener l) {
    if (listeners == null) {
      return;
    }
    listeners.remove(l);
  }

  /**
   * Returns a float from [0,1] <i>approximating</i> how much of the image is loaded. If your image were a text document, this is basically telling
   * you where the text cursor was last placed. However the <code>ImageProducer</code> may make several different iterations over the image to deliver
   * the complete image, so it is completely possible that this value will range from 0 to 1 a few different times.
   * <P>
   * I wish this could be more precise, but I don't see how more precision is possible given the way the <code>ImageProducer</code>/
   * <code>ImageConsumer</code> model works.
   * <P>
   * For the most part, this will be a straight-forward 1-pass system. If you limit yourself to certain types of images (like PNGs) this is probably
   * the case.
   */
  public float getProgress() {
    return progress;
  }

  /**
   * This indicates whether this <code>ImageLoader</code> is finished.
   * <P>
   * Unlike <code>getProgress()</code>, this is guaranteed to be 100% accurate.
   */
  public boolean isDone() {
    return finished;
  }

  /**
   * Returns the dimension of this image, or null if the dimensions are not yet known.
   */
  public Dimension getSize() {
    if (size == null) {
      return null;
    }
    return new Dimension(size.width, size.height);
  }

  /** Fires all change listeners */
  protected void fireChangeListeners() {
    if (listeners == null) {
      return;
    }

    for (ChangeListener l : listeners) {
      try {
        l.stateChanged(new ChangeEvent(this));
      }
      catch (Exception e) {
        LOGGER.warn("failed to inform listener: {}", e.getMessage());
      }
    }
  }

  /**
   * This blocks until the image has finished loading, an error occurs, or the operation has been cancelled.
   * 
   * @return the ARGB image represented in the original ImageProducer, or <code>null</code> if the operation was cancelled.
   * @throws RuntimeException
   *           if an error occurred while loading this image
   */
  public BufferedImage getImage() {

    block();

    if (status == ImageConsumer.IMAGEERROR)
      throw new RuntimeException("An error occurred.");
    if (status == ImageConsumer.IMAGEABORTED)
      throw new RuntimeException("The operation was aborted.");

    return dest;
  }

  private Thread       waitingThread;
  private List<Thread> waitingThreads;

  /**
   * Blocks this thread until the image is finished
   * 
   */
  private void block() {
    Thread t = Thread.currentThread();
    int i = 0;
    synchronized (this) {
      if (waitingThreads == null) {
        // there is no list defined
        if (waitingThread == null) {
          waitingThread = t;
        }
        else {
          waitingThreads = new ArrayList<>();
          waitingThreads.add(waitingThread);
          waitingThreads.add(t);
          i = 1;
          waitingThread = null;
        }
      }
      else {
        i = waitingThreads.size();
        waitingThreads.add(t);
      }
    }
    while (!finished) {
      try {
        Thread.sleep(500);
      }
      catch (Exception e) {
        Thread.yield();
      }
    }
    synchronized (this) {
      Thread.interrupted();
      if (waitingThread == t) {
        waitingThread = null;
      }
      if (waitingThreads != null) {
        waitingThreads.remove(i);
      }
    }
  }

  private void unblock() {
    synchronized (this) {
      if (waitingThread != null) {
        waitingThread.interrupt();
      }
      if (waitingThreads != null) {
        for (Thread t : waitingThreads) {
          t.interrupt();
        }
      }
    }
  }

  boolean       finished = false;
  int           status;
  Dimension     size;
  Properties    properties;
  BufferedImage dest;

  class InnerImageConsumer implements ImageConsumer {
    public InnerImageConsumer() {
    }

    public void imageComplete(int completionStatus) {
      producer.removeConsumer(this);

      status = completionStatus;
      finished = true;
      unblock();
      fireChangeListeners();
    }

    int[] indexed;

    public void setColorModel(ColorModel cm) {
      try {
        lastCM = cm;
        indexed = null;
        if (cm instanceof IndexColorModel) {
          IndexColorModel i = (IndexColorModel) cm;
          if (i.getMapSize() <= 256) {
            indexed = new int[i.getMapSize()];
            for (int a = 0; a < indexed.length; a++) {
              int r = i.getRed(a);
              int g = i.getGreen(a);
              int b = i.getBlue(a);
              indexed[a] = (255 << 24) + (r << 16) + (g << 8) + b;
            }
            int t = i.getTransparentPixel();
            if (i.hasAlpha() && t >= 0 && t < indexed.length) {
              indexed[t] = 0;
            }
          }
        }
      }
      catch (RuntimeException | Error e) {
        LOGGER.debug("could not set color model: {}", e.getMessage());
        throw e;
      }
    }

    private transient int[] row;

    public void setDimensions(int w, int h) {
      // try to cache the image file; we have up to 5 retries here if we hit the memory cap since we are
      // hitting the machine hard due to multi CPU image caching
      int retries = 5;
      do {
        try {
          if (w <= 0)
            throw new IllegalArgumentException("Width must be greater than zero.  (" + w + ")");
          if (h <= 0)
            throw new IllegalArgumentException("Height must be greater than zero.  (" + h + ")");
          if (size != null) {
            // eh? already exists?
            if (size.width == w && size.height == h)
              return;
            if (dest != null) {
              throw new RuntimeException("An image of " + (size.getWidth()) + "x" + size.getHeight()
                  + " was already created.  Illegal attempt to call setDimensions(" + w + "," + h + ")");
            }
          }
          size = new Dimension(w, h);
          dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
          row = new int[w];
          fireChangeListeners();
          break;
        }
        catch (OutOfMemoryError e) {
          // memory limit hit; give it another 500ms time to recover
          LOGGER.warn("hit memory cap: {}", e.getMessage());
          size = null;
          dest = null;
          try {
            Thread.sleep(200);
          }
          catch (InterruptedException ignored) {
            Thread.interrupted();
          }
        }
        catch (RuntimeException | Error e) {
          LOGGER.debug("could not set dimensions: {}", e.getMessage());
          throw e;
        }
        retries--;
      } while (retries > 0);
    }

    public void setHints(int hints) {
    }

    public void setPixels(int x, int y, int w, int h, ColorModel cm, byte[] data, int offset, int scanSize) {
      try {
        if (size == null)
          throw new RuntimeException(
              "The dimensions of this image are not yet defined.  Cannot write image data until the dimensions of the image are known.");

        if (cm == lastCM && indexed != null) {
          int argb;
          byte k = 0;
          int k2 = 0;
          for (int n = y; n < y + h; n++) {
            for (int m = x; m < x + w; m++) {
              k = data[(n - y) * scanSize + (m - x) + offset];
              if (k >= 0) {
                k2 = k;
              }
              else {
                k2 = k + 256;
              }
              argb = indexed[k2];
              row[m - x] = argb;
            }
            dest.getRaster().setDataElements(x, n, w, 1, row);
          }
        }
        else {
          int transIndex = (cm instanceof IndexColorModel) ? ((IndexColorModel) cm).getTransparentPixel() : -1;

          int argb;
          for (int n = y; n < y + h; n++) {
            for (int m = x; m < x + w; m++) {
              byte k = data[(n - y) * scanSize + (m - x) + offset];
              int k2 = k & 0xff;
              if (k2 == transIndex) {
                argb = 0;
              }
              else {
                argb = cm.getRGB(k2);
              }
              row[m - x] = argb;
            }
            dest.getRaster().setDataElements(x, n, w, 1, row);
          }
        }
        setProgress(x + w, y + h);
      }
      catch (RuntimeException | Error e) {
        LOGGER.debug("could not set pixels: {}", e.getMessage());
        throw e;
      }
    }

    private boolean lastCMwasRGB  = false;
    private boolean lastCMwasARGB = false;
    ColorModel      lastCM        = null;

    public void setPixels(int x, int y, int w, int h, ColorModel cm, int[] data, int offset, int scanSize) {
      try {
        if (size == null)
          throw new RuntimeException(
              "The dimensions of this image are not yet defined.  Cannot write image data until the dimensions of the image are known.");
        if (cm instanceof DirectColorModel) {
          // don't call "equals" all the time; that's just a waste
          // this is a little uglier, but it will save hundreds of method calls:
          boolean quickRGB = (cm == lastCM && lastCMwasRGB);
          boolean quickARGB = (cm == lastCM && lastCMwasARGB);

          DirectColorModel d = (DirectColorModel) cm;
          if (quickARGB || ((!quickRGB) && ImageLoader.equals(d, ARGBModel))) {
            for (int n = y; n < y + h; n++) {
              int k = (n - y) * scanSize - x + offset;
              System.arraycopy(data, k, row, 0, w);
              dest.getRaster().setDataElements(x, n, w, 1, row);
            }
            lastCMwasRGB = false;
            lastCMwasARGB = true;
            lastCM = cm;
            return;
          }
          else if (quickRGB || ImageLoader.equals(d, RGBModel)) {
            // same thing, but no alpha:
            for (int n = y; n < y + h; n++) {
              int k = (n - y) * scanSize - x + offset;
              System.arraycopy(data, k, row, 0, w);
              // add alpha to every pixel:
              for (int a = 0; a < w; a++) {
                row[a] = (row[a] & 0xffffff) + 0xff000000;
              }
              dest.getRaster().setDataElements(x, n, w, 1, row);
            }
            lastCMwasRGB = true;
            lastCMwasARGB = false;
            lastCM = cm;
            return;
          }
        }
        int argb;
        // If you know what your ColorModel is,
        // it'd be good to make a special-case loop
        // to deal with that data (like the if/then above),
        // because a million calls to getRGB() is NOT cheap.
        for (int n = y; n < y + h; n++) {
          for (int m = x; m < x + w; m++) {
            argb = cm.getRGB(data[(n - y) * scanSize + (m - x) + offset]);
            row[m - x] = argb;
          }
          dest.getRaster().setDataElements(x, n, w, 1, row);
        }
        lastCMwasRGB = false;
        lastCMwasARGB = false;
        lastCM = cm;

        setProgress(x + w, y + h);
      }
      catch (RuntimeException | Error e) {
        LOGGER.debug("could not set pixels: {}", e.getMessage());
        throw e;
      }
    }

    public void setProperties(Hashtable<?, ?> p) {
      try {
        // TODO: one day integrate these
        // it wouldn't be hard to extend BufferedImage
        // and override the methods regarding
        // "getProperty" if you need to.

        if (properties == null)
          properties = new Properties();
        properties.putAll(p);
      }
      catch (RuntimeException | Error e) {
        LOGGER.warn("could not set properties: {}", e.getMessage());
        throw e;
      }
    }
  }

  private void setProgress(int x, int y) {
    progress = ((float) (y * size.width + x)) / ((float) (size.width * size.height));
    fireChangeListeners();
  }
}
