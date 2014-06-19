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
import java.util.ArrayList;
import java.util.List;

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
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class ImageCache - used to build a local image cache (scaled down versions & thumbnails - also for offline access).
 * 
 * @author Manuel Laggner
 */
public class ImageCache {
  private static final Logger LOGGER    = LoggerFactory.getLogger(ImageCache.class);
  public static final String  CACHE_DIR = "cache/image";

  public enum CacheType {
    FAST, SMOOTH
  }

  /**
   * Gets the cache dir. If it is not on the disk - it will also create it
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
   * Gets the file name of the cached file.
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
      // rate, especially for our limited use
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
   * @throws InterruptedException
   */
  public static InputStream scaleImage(String imageUrl, int width) throws IOException, InterruptedException {
    Url url = new Url(imageUrl);
    Image image = Toolkit.getDefaultToolkit().createImage(url.getBytes());
    BufferedImage originalImage = com.bric.image.ImageLoader.createImage(image);

    Point size = new Point();
    size.x = width;
    size.y = size.x * originalImage.getHeight() / originalImage.getWidth();

    // BufferedImage scaledImage = Scaling.scale(originalImage, size.x, size.y);
    BufferedImage scaledImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, size.x, size.y, Scalr.OP_ANTIALIAS);
    originalImage = null;

    ImageWriter imgWrtr = null;
    ImageWriteParam imgWrtrPrm = null;

    // here we have two different ways to create our thumb
    // a) a scaled down jpg/png (without transparency) which we have to modify since OpenJDK cannot call native jpg encoders
    // b) a scaled down png (with transparency) which we can store without any more modifying as png
    if (hasTransparentPixels(scaledImage)) {
      // transparent image -> png
      imgWrtr = ImageIO.getImageWritersByFormatName("png").next();
      imgWrtrPrm = imgWrtr.getDefaultWriteParam();

    }
    else {
      // non transparent image -> jpg
      // convert to rgb
      BufferedImage rgb = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
      ColorConvertOp xformOp = new ColorConvertOp(null);
      xformOp.filter(scaledImage, rgb);
      imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
      imgWrtrPrm = imgWrtr.getDefaultWriteParam();
      imgWrtrPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
      imgWrtrPrm.setCompressionQuality(0.80f);

      scaledImage = rgb;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageOutputStream output = ImageIO.createImageOutputStream(baos);
    imgWrtr.setOutput(output);
    IIOImage outputImage = new IIOImage(scaledImage, null, null);
    imgWrtr.write(null, outputImage, imgWrtrPrm);
    imgWrtr.dispose();
    scaledImage = null;

    byte[] bytes = baos.toByteArray();

    output.flush();
    output.close();
    baos.close();

    return new ByteArrayInputStream(bytes);
  }

  /**
   * Cache image.
   * 
   * @param mf
   *          the media file
   * @return the file the cached file
   * @throws Exception
   */
  public static File cacheImage(MediaFile mf) throws Exception {
    File originalFile = mf.getFile();

    if (FileUtils.sizeOf(originalFile) == 0) {
      throw new EmptyFileException(originalFile);
    }

    String cacheFilename = ImageCache.getCachedFileName(originalFile.getPath());
    File cachedFile = new File(ImageCache.getCacheDir(), cacheFilename + ".jpg");
    if (!cachedFile.exists()) {
      // check if the original file exists
      if (!originalFile.exists()) {
        throw new FileNotFoundException("unable to cache file: " + originalFile.getName() + "; file does not exist");
      }

      // recreate cache dir if needed
      // rescale & cache
      BufferedImage originalImage = null;
      try {
        originalImage = com.bric.image.ImageLoader.createImage(originalFile);
      }
      catch (Exception e) {
        throw new Exception("cannot create image - file seems not to be valid? " + originalFile);
      }

      // calculate width based on MF type
      int desiredWidth = originalImage.getWidth(); // initialize with fallback
      switch (mf.getType()) {
        case FANART:
          if (originalImage.getWidth() > 1000) {
            desiredWidth = 1000;
          }
          break;
        case POSTER:
          if (originalImage.getHeight() > 500) {
            desiredWidth = 350;
          }
          break;
        case EXTRAFANART:
        case THUMB:
        case BANNER:
        case GRAPHIC:
          desiredWidth = 300;

        default:
          break;
      }

      // special handling for movieset-fanart or movieset-poster
      if (mf.getFilename().startsWith("movieset-fanart") || mf.getFilename().startsWith("movieset-poster")) {
        if (originalImage.getWidth() > 1000) {
          desiredWidth = 1000;
        }
      }

      Point size = calculateSize(desiredWidth, (int) (originalImage.getHeight() / 1.5), originalImage.getWidth(), originalImage.getHeight(), true);
      BufferedImage scaledImage = null;

      if (Globals.settings.getImageCacheType() == CacheType.FAST) {
        // scale fast
        scaledImage = Scalr.resize(originalImage, Scalr.Method.BALANCED, Scalr.Mode.FIT_EXACT, size.x, size.y);
      }
      else {
        // scale with good quality
        scaledImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, size.x, size.y);
      }
      originalImage = null;

      ImageWriter imgWrtr = null;
      ImageWriteParam imgWrtrPrm = null;

      // here we have two different ways to create our thumb
      // a) a scaled down jpg/png (without transparency) which we have to modify since OpenJDK cannot call native jpg encoders
      // b) a scaled down png (with transparency) which we can store without any more modifying as png
      if (hasTransparentPixels(scaledImage)) {
        // transparent image -> png
        imgWrtr = ImageIO.getImageWritersByFormatName("png").next();
        imgWrtrPrm = imgWrtr.getDefaultWriteParam();

      }
      else {
        // non transparent image -> jpg
        // convert to rgb
        BufferedImage rgb = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        ColorConvertOp xformOp = new ColorConvertOp(null);
        xformOp.filter(scaledImage, rgb);
        imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
        imgWrtrPrm = imgWrtr.getDefaultWriteParam();
        imgWrtrPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        imgWrtrPrm.setCompressionQuality(0.80f);

        scaledImage = rgb;
      }

      FileImageOutputStream output = new FileImageOutputStream(cachedFile);
      imgWrtr.setOutput(output);
      IIOImage image = new IIOImage(scaledImage, null, null);
      imgWrtr.write(null, image, imgWrtrPrm);
      imgWrtr.dispose();
      output.flush();
      output.close();
      scaledImage = null;
    }

    if (!cachedFile.exists()) {
      throw new Exception("unable to cache file: " + originalFile.getName());
    }

    return cachedFile;
  }

  private static boolean hasTransparentPixels(BufferedImage image) {
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int pixel = image.getRGB(x, y);
        if ((pixel >> 24) == 0x00) {
          return true;
        }
      }
    }
    return false;
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
      MediaFile mf = new MediaFile(new File(path));
      return ImageCache.cacheImage(mf);
    }
    catch (EmptyFileException e) {
      LOGGER.warn("failed to cache file (file is empty): " + path);
    }
    catch (FileNotFoundException e) {
      LOGGER.warn(e.getMessage());
    }
    catch (Exception e) {
      LOGGER.warn("problem caching file: " + e.getMessage());
    }

    // fallback
    return new File(path);
  }

  /**
   * clear the image cache for all graphics within the given media entity
   * 
   * @param entity
   *          the media entity
   */
  public static void clearImageCacheForMediaEntity(MediaEntity entity) {
    List<MediaFile> mediaFiles = new ArrayList<MediaFile>(entity.getMediaFiles());
    for (MediaFile mediaFile : mediaFiles) {
      if (mediaFile.isGraphic()) {
        File file = ImageCache.getCachedFile(mediaFile.getFile().getPath());
        if (file.exists()) {
          FileUtils.deleteQuietly(file);
        }
      }
    }
  }

  public static Point calculateSize(int maxWidth, int maxHeight, int originalWidth, int originalHeight, boolean respectFactor) {
    Point size = new Point();
    if (respectFactor) {
      // calculate on available height
      size.y = maxHeight;
      size.x = (int) (size.y * (double) originalWidth / (double) originalHeight);

      if (size.x > maxWidth) {
        // calculate on available height
        size.x = maxWidth;
        size.y = (int) (size.x * (double) originalHeight / (double) originalWidth);
      }
    }
    else {
      size.x = maxWidth;
      size.y = maxHeight;
    }
    return size;
  }
}