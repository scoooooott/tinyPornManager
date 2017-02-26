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
package org.tinymediamanager.core;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.util.UrlUtil;
import org.tinymediamanager.thirdparty.ImageLoader;

/**
 * The Class ImageCache - used to build a local image cache (scaled down versions & thumbnails - also for offline access).
 * 
 * @author Manuel Laggner
 */
public class ImageCache {
  private static final Logger LOGGER    = LoggerFactory.getLogger(ImageCache.class);
  private static final Path   CACHE_DIR = Paths.get("cache/image");

  public enum CacheType {
    FAST,
    SMOOTH
  }

  /**
   * Gets the cache dir. If it is not on the disk - it will also create it
   * 
   * @return the cache dir
   */
  public static Path getCacheDir() {
    if (!Files.exists(CACHE_DIR)) {
      try {
        Files.createDirectories(CACHE_DIR);
      }
      catch (IOException e) {
        LOGGER.warn("Could not create cache dir " + CACHE_DIR + " - " + e.getMessage());
      }
    }
    return CACHE_DIR;
  }

  /**
   * Gets the file name (MD5 hash) of the cached file.
   * 
   * @param path
   *          the url
   * @return the cached file name
   */
  public static String getMD5(String path) {
    try {
      if (path == null) {
        return null;
      }
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

    BufferedImage originalImage = null;
    try {
      originalImage = createImage(url.getBytes());
    }
    catch (Exception e) {
      throw new IOException(e.getMessage());
    }

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
   * Scale image to fit in the given width.
   * 
   * @param file
   *          the original image file
   * @param width
   *          the width
   * @return the input stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws InterruptedException
   */
  public static InputStream scaleImage(Path file, int width) throws IOException, InterruptedException {
    BufferedImage originalImage = null;
    try {
      originalImage = createImage(file);
    }
    catch (Exception e) {
      throw new IOException(e.getMessage());
    }

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
  public static Path cacheImage(Path originalFile) throws Exception {
    MediaFile mf = new MediaFile(originalFile);
    Path cachedFile = ImageCache.getCacheDir().resolve(getMD5(originalFile.toString()) + "." + Utils.getExtension(originalFile));
    if (!Files.exists(cachedFile)) {
      // check if the original file exists && size > 0
      if (!Files.exists(originalFile)) {
        throw new FileNotFoundException("unable to cache file: " + originalFile + "; file does not exist");
      }
      if (Files.size(originalFile) == 0) {
        throw new EmptyFileException(originalFile);
      }

      // recreate cache dir if needed
      // rescale & cache
      BufferedImage originalImage = null;
      try {
        originalImage = createImage(originalFile);
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
          break;

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

      FileImageOutputStream output = new FileImageOutputStream(cachedFile.toFile());
      imgWrtr.setOutput(output);
      IIOImage image = new IIOImage(scaledImage, null, null);
      imgWrtr.write(null, image, imgWrtrPrm);
      imgWrtr.dispose();
      output.flush();
      output.close();
      scaledImage = null;
    }

    if (!Files.exists(cachedFile)) {
      throw new Exception("unable to cache file: " + originalFile);
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
  public static void invalidateCachedImage(Path path) {
    Path cachedFile = getCacheDir().resolve(ImageCache.getMD5(path.toAbsolutePath().toString()) + "." + Utils.getExtension(path));
    if (Files.exists(cachedFile)) {
      Utils.deleteFileSafely(cachedFile);
    }
  }

  /**
   * Gets the cached image for "string".<br>
   * If not found AND it is a valid url, download and cache first.<br>
   * 
   * @param url
   *          the url of image, or basically the unhashed string of cache file
   * @return the cached file or NULL
   */
  public static Path getCachedFile(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    String ext = UrlUtil.getExtension(url);
    if (ext.isEmpty()) {
      ext = "jpg"; // just assume
    }
    Path cachedFile = ImageCache.getCacheDir().resolve(getMD5(url) + "." + ext);
    if (Files.exists(cachedFile)) {
      LOGGER.trace("found cached url :) " + url);
      return cachedFile;
    }

    // is the image cache activated?
    if (!Globals.settings.isImageCache()) {
      return null;
    }

    try {
      Url u = new Url(url);
      boolean ok = u.download(cachedFile);
      if (ok) {
        LOGGER.trace("cached url successfully :) " + url);
        return cachedFile;
      }
    }
    catch (MalformedURLException e) {
      LOGGER.trace("Problem getting cached file for url " + e.getMessage());
    }

    LOGGER.trace("Problem getting cached file for url " + url);
    return null;
  }

  /**
   * Gets the cached file, if ImageCache is activated<br>
   * If not found, cache original first
   * 
   * @param path
   *          the path
   * @return the cached file
   */
  public static Path getCachedFile(Path path) {
    if (path == null) {
      return null;
    }
    path = path.toAbsolutePath();

    Path cachedFile = ImageCache.getCacheDir().resolve(getMD5(path.toString()) + "." + Utils.getExtension(path));
    if (Files.exists(cachedFile)) {
      LOGGER.trace("found cached file :) " + path);
      return cachedFile;
    }

    // TODO: when does this happen?!?!
    // is the path already inside the cache dir? serve direct
    if (path.startsWith(CACHE_DIR.toAbsolutePath())) {
      return path;
    }

    // is the image cache activated?
    if (!Globals.settings.isImageCache()) {
      LOGGER.trace("ImageCache not activated - return original file 1:1");
      return path;
    }

    try {
      Path p = ImageCache.cacheImage(path);
      LOGGER.trace("cached file successfully :) " + p);
      return p;
    }
    catch (EmptyFileException e) {
      LOGGER.warn("failed to cache file (file is empty): " + path);
    }
    catch (FileNotFoundException e) {
      LOGGER.trace(e.getMessage());
    }
    catch (Exception e) {
      LOGGER.warn("problem caching file: " + e.getMessage());
    }

    // fallback
    return path;
  }

  /**
   * Check whether the original image is in the image cache or not
   * 
   * @param path
   *          the path to the original image
   * @return true/false
   */
  public static boolean isImageCached(Path path) {
    if (!Globals.settings.isImageCache()) {
      return false;
    }

    Path cachedFile = CACHE_DIR.resolve(ImageCache.getMD5(path.toString()) + "." + Utils.getExtension(path));
    if (Files.exists(cachedFile)) {
      return true;
    }

    return false;
  }

  /**
   * clear the image cache for all graphics within the given media entity
   * 
   * @param entity
   *          the media entity
   */
  public static void clearImageCacheForMediaEntity(MediaEntity entity) {
    List<MediaFile> mediaFiles = new ArrayList<>(entity.getMediaFiles());
    for (MediaFile mediaFile : mediaFiles) {
      if (mediaFile.isGraphic()) {
        Path file = ImageCache.getCachedFile(mediaFile.getFileAsPath());
        if (Files.exists(file)) {
          Utils.deleteFileSafely(file);
        }
      }
    }
  }

  /**
   * calculate a new size which fits into maxWidth and maxHeight
   * 
   * @param maxWidth
   *          the maximum width of the result
   * @param maxHeight
   *          the maximum height of the result
   * @param originalWidth
   *          the width of the source
   * @param originalHeight
   *          the height of the source
   * @param respectFactor
   *          should we respect the aspect ratio?
   * @return the calculated new size
   */
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

  public static BufferedImage createImage(byte[] imageData) throws Exception {
    return createImage(Toolkit.getDefaultToolkit().createImage(imageData));
  }

  public static BufferedImage createImage(Path file) throws Exception {
    return createImage(Toolkit.getDefaultToolkit().createImage(file.toFile().getAbsolutePath()));
  }

  public static BufferedImage createImage(Image img) {
    return ImageLoader.createImage(img);
  }
}
