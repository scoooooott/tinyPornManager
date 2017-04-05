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
package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

import hu.kazocsaba.imageviewer.ImageViewer;

/**
 * The class ImagePreviewDialog. To display a preview of the image in the image chooser
 * 
 * @author Manuel Laggner
 */
public class ImagePreviewDialog extends TmmDialog {
  private static final long                serialVersionUID = -7479476493187235867L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle      BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger              LOGGER           = LoggerFactory.getLogger(ImagePreviewDialog.class);

  private String                           imageUrl;
  private SwingWorker<BufferedImage, Void> worker;
  private ImageViewer                      imgViewer        = new ImageViewer();

  private JLabel                           lblLoadingInfo;

  public ImagePreviewDialog(String urlToImage) {
    super("", "imagePreview");
    this.imageUrl = urlToImage;

    getContentPane().setLayout(new BorderLayout());
    lblLoadingInfo = new JLabel(BUNDLE.getString("image.download")); //$NON-NLS-1$
    lblLoadingInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
    TmmFontHelper.changeFont(lblLoadingInfo, 1.5f);

    getContentPane().add(lblLoadingInfo, BorderLayout.CENTER);

    worker = new ImageFetcher();
    worker.execute();
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      pack();
      setLocationRelativeTo(MainWindow.getActiveInstance());
      super.setVisible(true);
    }
    else {
      if (worker != null && !worker.isDone()) {
        worker.cancel(true);
      }
      super.setVisible(false);
      dispose();
    }
  }

  /***************************************************************************
   * helper classes
   **************************************************************************/
  protected class ImageFetcher extends SwingWorker<BufferedImage, Void> {
    @Override
    protected BufferedImage doInBackground() throws Exception {
      try {
        Url url = new Url(imageUrl);
        return ImageCache.createImage(url.getBytes());
      }
      catch (Exception e) {
        LOGGER.warn("fetch image: " + e.getMessage());
        return null;
      }
    }

    @Override
    protected void done() {
      try {
        BufferedImage image = get();
        if (image == null) {
          lblLoadingInfo.setText(BUNDLE.getString("image.download.failed")); //$NON-NLS-1$
          pack();
          return;
        }
        imgViewer.setImage(image);
        JComponent comp = imgViewer.getComponent();

        getContentPane().removeAll();
        getContentPane().add(comp, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(MainWindow.getActiveInstance());
      }
      catch (Exception e) {
        LOGGER.warn("fetch image: " + e.getMessage());
      }
    }
  }
}
