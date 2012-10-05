package org.tinymediamanager.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import org.tinymediamanager.scraper.util.CachedUrl;

import com.bric.image.pixel.Scaling;

public class ImageLabel extends JLabel {

  private BufferedImage originalImage;
  private String imageUrl;
  private String imagePath;

  public ImageLabel() {
    super("");
  }

  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String newValue) {
    String oldValue = this.imagePath;
    this.imagePath = newValue;
    firePropertyChange("imagePath", oldValue, newValue);

    if (newValue == null) {
      originalImage = null;
      this.repaint();
      return;
    }

    File file = new File(imagePath);
    if (file.exists()) {
      this.originalImage = com.bric.image.ImageLoader.createImage(file);// ImageIO.read(file);
    } else {
      originalImage = null;
    }

    this.repaint();
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String newValue) {
    String oldValue = this.imageUrl;
    this.imageUrl = newValue;
    firePropertyChange("imageUrl", oldValue, newValue);

    if (newValue == null) {
      originalImage = null;
      this.repaint();
      return;
    }

    try {
      CachedUrl cachedUrl = new CachedUrl(imageUrl);
      this.originalImage = com.bric.image.ImageLoader.createImage(cachedUrl.getUrl());// ImageIO.read(cachedUrl.getInputStream(null,
                                                                                      // true));

      this.repaint();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (originalImage != null) {
      int originalWidth = originalImage.getWidth(null);
      int originalHeight = originalImage.getHeight(null);

      // calculate new height/width
      Point size = calculateSize(this.getWidth() - 8, this.getHeight() - 8, originalWidth, originalHeight, true);
      int newWidth = size.x;
      int newHeight = size.y;

      g.setColor(Color.BLACK);
      g.drawRect(0, 0, size.x + 7, size.y + 7);
      g.setColor(Color.WHITE);
      g.fillRect(1, 1, size.x + 6, size.y + 6);
      g.drawImage(Scaling.scale(originalImage, newWidth, newHeight), 4, 4, newWidth, newHeight, this);
    }
  }

  public static Point calculateSize(int maxWidth, int maxHeight, int originalWidth, int originalHeight, boolean respectFactor) {
    Point size = new Point();
    if (respectFactor) {
      // calculate on available height
      size.y = maxHeight;
      size.x = size.y * originalWidth / originalHeight;

      if (size.x > maxWidth) {
        // calculate on available height
        size.x = maxWidth;
        size.y = size.x * originalHeight / originalWidth;
      }
    } else {
      size.x = maxWidth;
      size.y = maxHeight;
    }
    return size;
  }

}
