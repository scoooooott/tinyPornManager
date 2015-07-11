/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
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
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.imgscalr.Scalr;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.IconManager;

/**
 * The class MediaScraperComboBox provides a combobox with the scraper logo next to the text
 * 
 * @author Manuel Laggner
 *
 * @param <E>
 */
public class MediaScraperComboBox extends JComboBox<MediaScraper> {
  private static final long   serialVersionUID = 7845502706645523958L;
  private Map<URL, ImageIcon> imageCache;

  public MediaScraperComboBox() {
    super();
    init();
  }

  public MediaScraperComboBox(MediaScraper[] scrapers) {
    super(scrapers);
    init();
  }

  public MediaScraperComboBox(Vector scrapers) {
    super(scrapers);
    init();
  }

  public MediaScraperComboBox(List<MediaScraper> scrapers) {
    super(new Vector(scrapers));
    init();
  }

  @Override
  public Dimension getPreferredSize() {
    return getUI().getPreferredSize(this);
  }

  @Override
  public Dimension getMinimumSize() {
    return getUI().getPreferredSize(this);
  }

  @Override
  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  private void init() {
    setRenderer(new MediaScraperComboBoxRenderer());
    setEditable(true);
    setEditor(new MediaScraperComboBoxEditor());
  }

  private ImageIcon getIcon(URL url) {
    ImageIcon logo = imageCache.get(url);
    if (logo == null) {
      logo = getScaledIcon(IconManager.loadImageFromURL(url));
      imageCache.put(url, logo);
    }
    return logo;
  }

  private ImageIcon getScaledIcon(ImageIcon original) {
    Canvas c = new Canvas();
    FontMetrics fm = c.getFontMetrics(getFont());

    int height = (int) (fm.getHeight() * 2f);
    int width = original.getIconWidth() / original.getIconHeight() * height;

    BufferedImage scaledImage = Scalr.resize(com.bric.image.ImageLoader.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC,
        width, height, Scalr.OP_ANTIALIAS);
    return new ImageIcon(scaledImage);
  }

  class MediaScraperComboBoxEditor extends BasicComboBoxEditor {
    private JLabel       label = new JLabel("");
    private MediaScraper selectedItem;

    public MediaScraperComboBoxEditor() {
      label.setHorizontalAlignment(JLabel.LEFT);
      label.setVerticalAlignment(JLabel.CENTER);
      selectedItem = null;
    }

    @Override
    public Component getEditorComponent() {
      return label;
    }

    @Override
    public Object getItem() {
      return selectedItem;
    }

    @Override
    public void setItem(Object item) {
      selectedItem = (MediaScraper) item;

      if (selectedItem == null) {
        // label.setIcon(null);
        label.setText("");
      }
      else {
        // ImageIcon logo = getIcon(selectedItem.getLogoURL());
        // label.setIcon(logo);
        label.setText(selectedItem.getMediaProvider().getProviderInfo().getName());
      }
    }
  }

  class MediaScraperComboBoxRenderer extends JLabel implements ListCellRenderer<MediaScraper> {
    private static final long serialVersionUID = -4726883292397768525L;

    public MediaScraperComboBoxRenderer() {
      setOpaque(true);
      setHorizontalAlignment(LEFT);
      setVerticalAlignment(CENTER);
      setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
      imageCache = new HashMap<>();
    }

    /*
     * This method finds the image and text corresponding to the selected value and returns the label, set up to display the text and image.
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends MediaScraper> list, MediaScraper scraper, int index, boolean isSelected,
        boolean cellHasFocus) {

      // calculate the max width of the logo
      int maxWidth = 0;
      for (int i = 0; i < list.getModel().getSize(); i++) {
        MediaScraper ms = list.getModel().getElementAt(i);
        ImageIcon logo = MediaScraperComboBox.this.getIcon(ms.getLogoURL());
        maxWidth = Math.max(maxWidth, logo.getIconWidth());
      }

      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      }
      else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
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

      return this;
    }
  }
}
