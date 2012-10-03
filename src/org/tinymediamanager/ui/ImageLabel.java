package org.tinymediamanager.ui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.tinymediamanager.scraper.util.CachedUrl;

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
		this.imagePath = newValue;

		if (newValue == null) {
			originalImage = null;
			this.repaint();
			return;
		}

		try {
			File file = new File(imagePath);
			if (file.exists()) {
				this.originalImage = ImageIO.read(file);
			} else {
				originalImage = null;
			}

			this.repaint();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String newValue) {
		this.imageUrl = newValue;

		if (newValue == null) {
			originalImage = null;
			this.repaint();
			return;
		}

		try {
			CachedUrl cachedUrl = new CachedUrl(imageUrl);
			this.originalImage = ImageIO.read(cachedUrl.getInputStream(null,
					true));

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
			Point size = calculateSize(this.getWidth(), this.getHeight(),
					originalWidth, originalHeight, true);
			int newWidth = size.x;
			int newHeight = size.y;

			// int newWidth, newHeight;
			// // calculate on available height
			// newHeight = this.getHeight();
			// newWidth = newHeight * originalWidth / originalHeight;
			//
			// if (newWidth > this.getWidth()) {
			// // calculate on available height
			// newWidth = this.getWidth();
			// newHeight = newWidth * originalHeight / originalWidth;
			// }

			g.drawImage(originalImage.getScaledInstance(newWidth, newHeight,
					Image.SCALE_SMOOTH), 0, 0, newWidth, newHeight, this);
		}
	}

	public static Point calculateSize(int maxWidth, int maxHeight,
			int originalWidth, int originalHeight, boolean respectFactor) {
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
