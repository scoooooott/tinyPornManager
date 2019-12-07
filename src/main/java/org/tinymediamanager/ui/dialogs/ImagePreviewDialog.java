/*
 * Copyright 2012 - 2019 Manuel Laggner
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
package org.tinymediamanager.ui.dialogs;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.components.ImageLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class ImagePreviewDialog. To display a preview of the image in the image chooser
 * 
 * @author Manuel Laggner
 */
public class ImagePreviewDialog extends TmmDialog {
  private static final long serialVersionUID = -7479476493187235867L;

  private String            imageUrl;
  private String            imagePath;

  private ImageLabel        image;

  public ImagePreviewDialog(String urlToImage) {
    super(BUNDLE.getString("image.show"), "imagePreview");
    init();

    this.imageUrl = urlToImage;
  }

  public ImagePreviewDialog(Path pathToImage) {
    super("", "imagePreview");
    init();

    this.imagePath = pathToImage.toString();
  }

  private void init() {
    {
      JPanel imagePanel = new JPanel();
      imagePanel.setLayout(new MigLayout("", "[75lp,grow]", "[75lp,grow]"));
      getContentPane().add(imagePanel);

      image = new ImageLabel(true);
      image.setPreferCache(false);
      image.setIsLightbox(true);
      image.setPosition(ImageLabel.Position.CENTER);
      image.setCacheUrl(true);

      imagePanel.add(image, "cell 0 0,grow");
    }
    {
      JButton closeButton = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      closeButton.addActionListener(e -> setVisible(false));
      addDefaultButton(closeButton);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      // get max screen size on multi screen setups
      GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      int width = gd.getDisplayMode().getWidth();
      int height = gd.getDisplayMode().getHeight();
      setMaximumSize(new Dimension(width, height));

      // run later to avoid strange loading artefacts
      SwingUtilities.invokeLater(() -> {
        if (StringUtils.isNotBlank(imagePath)) {
          image.setImagePath(imagePath);
        }
        else if (StringUtils.isNotBlank(imageUrl)) {
          image.setImageUrl(imageUrl);
        }
      });

      pack();
      setLocationRelativeTo(MainWindow.getActiveInstance());
      super.setVisible(true);
    }
    else {
      super.setVisible(false);
      dispose();
    }
  }
}
