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
package org.tinymediamanager.core;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.bric.image.pixel.Scaling;

/**
 * @author manuel
 * 
 */
public class ImageCache {
  /** The static LOGGER. */
  private static final Logger LOGGER    = Logger.getLogger(ImageCache.class);

  /** The Constant CACHE_DIR. */
  public static final String  CACHE_DIR = "cache/image";

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

    return new ByteArrayInputStream(baos.toByteArray());
  }
}
