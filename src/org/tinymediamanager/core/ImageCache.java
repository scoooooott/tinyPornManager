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
package org.tinymediamanager.core;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.ui.components.ImageLabel;

import com.bric.image.pixel.Scaling;

/**
 * The Class ImageCache.
 * 
 * @author Manuel Laggner
 */
public class ImageCache {
  /** The static LOGGER. */
  private static final Logger LOGGER    = LoggerFactory.getLogger(ImageCache.class);

  /** The Constant CACHE_DIR. */
  public static final String  CACHE_DIR = "cache/image";

  /**
   * The Enum CacheType.
   * 
   * @author Manuel Laggner
   */
  public enum CacheType {
    FAST, SMOOTH
  }

  /**
   * Gets the cache dir.
   * 
   * @return the cache dir
   */
  public static File getCacheDir() {
    File imageCacheDir = new File(CACHE_DIR);
    if (!imageCacheDir.exists()) {
      imageCacheDir.mkdirs();
    }
    return imageCacheDir;
  }

  /**
   * Gets the cached file name.
   * 
   * @param path
   *          the url
   * @return the cached file name
   */
  public static String getCachedFileName(String path) {
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
   * Scale image to fit in the given width.
   * 
   * @param imageUrl
   *          the image url
   * @param width
   *          the width
   * @return the input stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static InputStream scaleImage(String imageUrl, int width) throws IOException {
    CachedUrl cachedUrl = new CachedUrl(imageUrl);
    Image image = Toolkit.getDefaultToolkit().createImage(cachedUrl.getBytes());
    BufferedImage originalImage = com.bric.image.ImageLoader.createImage(image);

    Point size = new Point();
    size.x = width;
    size.y = size.x * originalImage.getHeight() / originalImage.getWidth();

    BufferedImage scaledImage = Scaling.scale(originalImage, size.x, size.y);

    // convert to rgb
    BufferedImage rgb = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);

    ColorConvertOp xformOp = new ColorConvertOp(null);
    xformOp.filter(scaledImage, rgb);

    ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
    ImageWriteParam jpgWrtPrm = imgWrtr.getDefaultWriteParam();
    jpgWrtPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
    jpgWrtPrm.setCompressionQuality(0.8f);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageOutputStream output = ImageIO.createImageOutputStream(baos);
    imgWrtr.setOutput(output);
    IIOImage outputImage = new IIOImage(rgb, null, null);
    imgWrtr.write(null, outputImage, jpgWrtPrm);
    imgWrtr.dispose();

    byte[] bytes = baos.toByteArray();

    output.close();
    baos.close();

    return new ByteArrayInputStream(bytes);
  }

  /**
   * Cache image.
   * 
   * @param originalFile
   *          the original file
   * @return the file
   * @throws Exception
   *           the exception
   */
  public static File cacheImage(File originalFile) throws Exception {
    String cacheFilename = ImageCache.getCachedFileName(originalFile.getPath());
    File cachedFile = new File(ImageCache.getCacheDir(), cacheFilename + ".jpg");
    if (!cachedFile.exists()) {
      // check if the original file exists
      if (!originalFile.exists()) {
        throw new FileNotFoundException("unable to cache file: " + originalFile.getName() + "; file does not exist");
      }

      // recreate cache dir if needed
      // rescale & cache
      BufferedImage originalImage = com.bric.image.ImageLoader.createImage(originalFile);
      // BufferedImage originalImage = ImageIO.read(originalFile);

      // rescale and reencode only, if its bigger than 1000x500
      if (originalImage.getWidth() > 1000 || originalImage.getHeight() > 500) {
        Point size = ImageLabel.calculateSize((int) (originalImage.getWidth() / 1.5), (int) (originalImage.getHeight() / 1.5),
            originalImage.getWidth(), originalImage.getHeight(), true);
        BufferedImage scaledImage = null;

        if (Globals.settings.getImageCacheType() == CacheType.FAST) {
          // scale fast
          scaledImage = Scaling.scale(originalImage, size.x, size.y);
        }
        else {
          // scale with good quality
          scaledImage = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_RGB);
          scaledImage.getGraphics().drawImage(originalImage.getScaledInstance(size.x, size.y, Image.SCALE_SMOOTH), 0, 0, null);
        }

        // convert to rgb
        // BufferedImage rgb = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        BufferedImage rgb = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_RGB);

        ColorConvertOp xformOp = new ColorConvertOp(null);
        xformOp.filter(scaledImage, rgb);

        ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWrtPrm = imgWrtr.getDefaultWriteParam();
        jpgWrtPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        jpgWrtPrm.setCompressionQuality(0.80f);

        FileImageOutputStream output = new FileImageOutputStream(cachedFile);
        imgWrtr.setOutput(output);
        IIOImage image = new IIOImage(rgb, null, null);
        imgWrtr.write(null, image, jpgWrtPrm);
        imgWrtr.dispose();
        output.close();
      }
      else {
        FileUtils.copyFile(originalFile, cachedFile);
      }
    }

    if (!cachedFile.exists()) {
      throw new Exception("unable to cache file: " + originalFile.getName());
    }

    return cachedFile;
  }

  /**
   * Invalidate cached image.
   * 
   * @param path
   *          the path
   */
  public static void invalidateCachedImage(String path) {
    File cachedFile = new File(ImageCache.getCacheDir(), ImageCache.getCachedFileName(path) + ".jpg");
    if (cachedFile.exists()) {
      cachedFile.delete();
    }
  }

  /**
   * Gets the cached file.
   * 
   * @param path
   *          the path
   * @return the cached file
   */
  public static File getCachedFile(String path) {
    if (StringUtils.isEmpty(path)) {
      return null;
    }

    // is the image cache activated?
    if (!Globals.settings.isImageCache()) {
      return new File(path);
    }

    // is the path in the cache dir?
    if (path.startsWith(ImageCache.CACHE_DIR)) {
      return new File(path);
    }

    try {
      File originalFile = new File(path);
      return ImageCache.cacheImage(originalFile);

    }
    catch (FileNotFoundException e) {
      LOGGER.warn(e.getMessage());
    }
    catch (Exception e) {
      LOGGER.warn("problem caching file: ", e);
    }

    // fallback
    return new File(path);
  }

}
