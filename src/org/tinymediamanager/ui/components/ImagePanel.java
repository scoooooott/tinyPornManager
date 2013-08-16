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
package org.tinymediamanager.ui.components;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.ui.WrapLayout;

import com.bric.image.pixel.Scaling;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Image Panel is used to display all images for a MediaEntity
 * 
 * @author Manuel Laggner
 */
public class ImagePanel extends JPanel implements HierarchyListener {
  private static final long                serialVersionUID = -5344085698387374260L;

  private List<MediaFile>                  mediaFiles       = null;
  protected SwingWorker<List<Image>, Void> worker           = null;

  /**
   * UI components
   */

  private JPanel                           panelImages;
  private JScrollPane                      scrollPane;

  public ImagePanel(List<MediaFile> mediaFiles) {
    this.mediaFiles = mediaFiles;
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("100px:grow"), }, new RowSpec[] { RowSpec.decode("100px:grow"), }));

    scrollPane = new JScrollPane();
    add(scrollPane, "1, 1, fill, fill");

    panelImages = new JPanel();
    panelImages.setLayout(new WrapLayout(FlowLayout.LEFT));
    scrollPane.setViewportView(panelImages);
  }

  /**
   * Trigger to rebuild the panel
   */
  public void rebuildPanel() {
    panelImages.removeAll();
    panelImages.revalidate();
    scrollPane.repaint();

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    // fetch image in separate worker -> performance
    worker = new ImageLoader(new ArrayList<MediaFile>(mediaFiles));
    worker.execute();
  }

  @Override
  public void hierarchyChanged(HierarchyEvent arg0) {
    if (isShowing() && panelImages.getComponents().length == 0 && mediaFiles.size() > 0) {
      // rebuild the panel
      rebuildPanel();
    }
  }

  @Override
  public void addNotify() {
    super.addNotify();
    addHierarchyListener(this);
  }

  @Override
  public void removeNotify() {
    removeHierarchyListener(this);
    super.removeNotify();
  }

  /**
   * worker to load the images asynchrony
   */
  protected class ImageLoader extends SwingWorker<List<Image>, Void> {
    private List<MediaFile> mediaFiles;

    private ImageLoader(List<MediaFile> mediaFiles) {
      this.mediaFiles = mediaFiles;
    }

    @Override
    protected List<Image> doInBackground() throws Exception {
      List<Image> images = new ArrayList<Image>();
      if (isShowing()) {
        for (MediaFile mediaFile : mediaFiles) {
          if (isCancelled()) {
            return null;
          }
          try {
            File file = ImageCache.getCachedFile(mediaFile.getPath() + File.separator + mediaFile.getFilename());
            BufferedImage bufferedImage = com.bric.image.ImageLoader.createImage(file);
            Point size = ImageLabel.calculateSize(300, 100, bufferedImage.getWidth(), bufferedImage.getHeight(), true);
            images.add(Scaling.scale(bufferedImage, size.x, size.y));
          }
          catch (Exception e) {
          }
        }
      }
      return images;
    }

    @Override
    protected void done() {
      try {
        for (Image image : get()) {
          JLabel lblImageJLabel = new JLabel(new ImageIcon(image));
          panelImages.add(lblImageJLabel);
        }
        panelImages.revalidate();
        scrollPane.repaint();
      }
      catch (Exception e) {
      }
    }
  }
}
