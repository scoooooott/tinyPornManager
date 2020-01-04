/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.ui.components.combobox;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.imgscalr.Scalr;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.IconManager;

/**
 * The class MediaScraperComboBox provides a combobox with the scraper logo next to the text
 * 
 * @author Manuel Laggner
 */
public class MediaScraperComboBox extends JComboBox<MediaScraper> {
  private static final long   serialVersionUID = 7845502706645523958L;
  private Map<URI, ImageIcon> imageCache;
  private boolean             layingOut        = false;
  private int                 listWidth        = 0;

  public MediaScraperComboBox() {
    super();
    init();
  }

  public MediaScraperComboBox(MediaScraper[] scrapers) {
    super(scrapers);
    init();
  }

  public MediaScraperComboBox(Vector<MediaScraper> scrapers) {
    super(scrapers);
    init();
  }

  public MediaScraperComboBox(List<MediaScraper> scrapers) {
    super(new Vector<>(scrapers));
    init();
  }

  @Override
  public void setSelectedItem(Object anObject) {
    MediaScraper ms = (MediaScraper) anObject;
    if (ms != null && ms.isEnabled()) {
      // only allow to choose scraper when active
      super.setSelectedItem(anObject);
    }
  }

  @Override
  public Dimension getSize() {
    Dimension dim = super.getSize();
    if (!layingOut) {
      dim.width = Math.max(dim.width, getPreferredPopupSize().width);
    }
    return dim;
  }

  /**
   * get the preferred popup size (width of the contents in the popup)
   * 
   * @return the preferred popup size
   */
  private Dimension getPreferredPopupSize() {
    Dimension dimension = getPreferredSize();
    if (listWidth > 0) {
      dimension.width = listWidth;
    }
    return dimension;
  }

  private void init() {
    setRenderer(new MediaScraperComboBoxRenderer());
    // setEditable(true);
    // setEditor(new MediaScraperComboBoxEditor());
    updateUI();
  }

  private ImageIcon getIcon(URL url) {
    try {
      URI uri = url.toURI();
      ImageIcon logo = imageCache.get(uri);
      if (logo == null) {
        logo = getScaledIcon(IconManager.loadImageFromURL(url));
        imageCache.put(uri, logo);
      }
      return logo;
    }
    catch (Exception ignored) {
    }
    return null;
  }

  private ImageIcon getScaledIcon(ImageIcon original) {
    Canvas c = new Canvas();
    FontMetrics fm = c.getFontMetrics(getFont());

    int height = (int) (fm.getHeight() * 2f);
    int width = original.getIconWidth() / original.getIconHeight() * height;

    BufferedImage scaledImage = Scalr.resize(ImageUtils.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
        Scalr.OP_ANTIALIAS);
    return new ImageIcon(scaledImage);
  }

  class MediaScraperComboBoxRenderer extends JLabel implements ListCellRenderer<MediaScraper> {
    protected final ListCellRenderer defaultRenderer;
    private static final long        serialVersionUID = -4726883292397768525L;

    public MediaScraperComboBoxRenderer() {
      setOpaque(true);
      setHorizontalAlignment(LEFT);
      setVerticalAlignment(CENTER);
      setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
      imageCache = new HashMap<>();

      // get the default renderer from a JComboBox
      JComboBox box = new JComboBox();
      defaultRenderer = box.getRenderer();
    }

    /*
     * This method finds the image and text corresponding to the selected value and returns the label, set up to display the text and image.
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends MediaScraper> list, MediaScraper scraper, int index, boolean isSelected,
        boolean cellHasFocus) {
      if (index > -1) {
        if (isSelected) {
          setBackground(list.getSelectionBackground());
          setForeground(list.getSelectionForeground());
        }
        else {
          setBackground(list.getBackground());
          setForeground(list.getForeground());
        }

        // calculate the max width of the logo
        int maxWidth = 0;
        for (int i = 0; i < list.getModel().getSize(); i++) {
          MediaScraper ms = list.getModel().getElementAt(i);
          ImageIcon logo = MediaScraperComboBox.this.getIcon(ms.getLogoURL());
          maxWidth = Math.max(maxWidth, logo == null ? 0 : logo.getIconWidth());
          if (!scraper.isEnabled()) {
            setEnabled(false);
            setBackground(Color.lightGray);
          }
          else {
            setEnabled(true);
          }
        }

        int currentWidth = 0;
        ImageIcon logo = MediaScraperComboBox.this.getIcon(scraper.getLogoURL());
        if (logo != null) {
          currentWidth = logo.getIconWidth();
        }

        setIcon(logo);
        setText(scraper.getMediaProvider().getProviderInfo().getName());
        setFont(list.getFont());
        setIconTextGap(maxWidth + 4 - currentWidth); // 4 = default iconTextGap

        Dimension preferredSize = getPreferredSize();
        if (listWidth < preferredSize.width) {
          listWidth = preferredSize.width;
        }

        return this;
      }

      MediaScraper ms = (MediaScraper) getSelectedItem();
      if (ms != null) {
        return defaultRenderer.getListCellRendererComponent(list, ms.getName(), index, isSelected, cellHasFocus);
      }
      return defaultRenderer.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
    }
  }
}
