/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;
import org.tinymediamanager.thirdparty.ImageLoader;

public class ImageUtils {
  /**
   * Scale image to fit in the given width.
   *
   * @param imageBytes
   *          the image bytes
   * @param width
   *          the width
   * @return the input stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static InputStream scaleImage(byte[] imageBytes, int width) throws IOException {
    BufferedImage originalImage;
    try {
      originalImage = createImage(imageBytes);
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
   */
  public static InputStream scaleImage(Path file, int width) throws IOException {
    BufferedImage originalImage;
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

  static boolean hasTransparentPixels(BufferedImage image) {
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

  public static BufferedImage createImage(byte[] imageData) {
    return createImage(Toolkit.getDefaultToolkit().createImage(imageData));
  }

  public static BufferedImage createImage(Path file) {
    return createImage(Toolkit.getDefaultToolkit().createImage(file.toFile().getAbsolutePath()));
  }

  public static BufferedImage createImage(Image img) {
    return ImageLoader.createImage(img);
  }
}
