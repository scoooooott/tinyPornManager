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
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final long              serialVersionUID = -5344085698387374260L;
  private static final Logger            LOGGER           = LoggerFactory.getLogger(ImagePanel.class);
  private List<MediaFile>                mediaFiles       = null;
  public static final ThreadPoolExecutor imgpool          = new ThreadPoolExecutor(5, 5, // max threads
                                                              2, TimeUnit.SECONDS, // time to wait before closing idle workers
                                                              new LinkedBlockingQueue<Runnable>());   // our queue

  /**
   * UI components
   */

  private JPanel                         panelImages;
  private JScrollPane                    scrollPane;

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
    imgpool.getQueue().clear();
    panelImages.removeAll();
    panelImages.revalidate();
    scrollPane.repaint();

    // fetch image in separate worker -> performance
    for (MediaFile mf : new ArrayList<MediaFile>(mediaFiles)) {
      imgpool.submit(new ImageLoader(mf));
    }
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
  protected class ImageLoader extends SwingWorker<Void, BufferedImage> {
    private MediaFile mediaFile;

    private ImageLoader(MediaFile mediaFile) {
      this.mediaFile = mediaFile;
    }

    @Override
    protected Void doInBackground() throws Exception {
      if (isShowing()) {
        if (isCancelled()) {
          return null;
        }
        try {
          File file = ImageCache.getCachedFile(mediaFile.getPath() + File.separator + mediaFile.getFilename());
          LOGGER.debug("loading " + file);
          BufferedImage bufferedImage = com.bric.image.ImageLoader.createImage(file);
          Point size = ImageLabel.calculateSize(300, 100, bufferedImage.getWidth(), bufferedImage.getHeight(), true);
          publish(Scaling.scale(bufferedImage, size.x, size.y));
        }
        catch (Exception e) {
        }
      }
      return null;
    }

    @Override
    protected void process(List<BufferedImage> chunks) {
      try {
        JLabel lblImageJLabel = new JLabel(new ImageIcon(chunks.get(chunks.size() - 1))); // display last
        panelImages.add(lblImageJLabel);
        panelImages.revalidate();
        scrollPane.repaint();
      }
      catch (Exception e) {
      }
    }
  }
}
