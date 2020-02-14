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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.IconManager;

/**
 * the class MediaScraperCheckComboBox is used to display a CheckCombBox with media scraper logos
 * 
 * @author Manuel Laggner
 */
public class MediaScraperCheckComboBox extends TmmCheckComboBox<MediaScraper> {
  private static final long   serialVersionUID = 8153649858409237947L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(MediaScraperCheckComboBox.class);

  public MediaScraperCheckComboBox(final List<MediaScraper> scrapers) {
    super(scrapers);
  }

  @Override
  protected void setRenderer() {
    setRenderer(new MediaScraperCheckBoxRenderer(checkBoxes));
  }

  private class MediaScraperCheckBoxRenderer extends CheckBoxRenderer {
    private JPanel              panel        = new JPanel();
    private JCheckBox           checkBox     = new JCheckBox();
    private JLabel              label        = new JLabel();

    private Map<URI, ImageIcon> imageCache;
    private int                 maxIconWidth = 0;

    private MediaScraperCheckBoxRenderer(final List<TmmCheckComboBoxItem<MediaScraper>> items) {
      super(items);
      panel.setLayout(new FlowLayout(FlowLayout.LEFT));
      panel.add(checkBox);
      panel.add(label);

      label.setOpaque(false);
      checkBox.setOpaque(false);
      imageCache = new HashMap<>();

      // calculate the max width of the logo
      for (TmmCheckComboBoxItem<MediaScraper> item : items) {
        if (item.getUserObject() != null) {
          ImageIcon logo = getIcon(item.getUserObject().getLogoURL());
          if (logo != null) {
            maxIconWidth = Math.max(maxIconWidth, logo.getIconWidth());
          }
        }
      }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends TmmCheckComboBoxItem<MediaScraper>> list, TmmCheckComboBoxItem<MediaScraper> value,
        int index, boolean isSelected, boolean cellHasFocus) {
      if (index > 0 && index <= checkBoxes.size()) {
        TmmCheckComboBoxItem<MediaScraper> cb = checkBoxes.get(index - 1);

        if (isSelected) {
          panel.setBackground(UIManager.getColor("ComboBox.selectionBackground"));
          panel.setForeground(UIManager.getColor("ComboBox.selectionForeground"));
        }
        else {
          panel.setBackground(UIManager.getColor("ComboBox.background"));
          panel.setForeground(UIManager.getColor("ComboBox.foreground"));
        }

        label.setText(cb.getText());
        checkBox.setSelected(cb.isSelected());

        MediaScraper scraper = cb.getUserObject();
        if (scraper != null) {
          int currentWidth = 0;
          ImageIcon logo = getIcon(scraper.getLogoURL());
          if (logo != null) {
            currentWidth = logo.getIconWidth();
          }

          label.setIcon(logo);
          label.setIconTextGap(maxIconWidth + 4 - currentWidth); // 4 = default iconTextGap
        }
        else {
          label.setIcon(null);
          label.setIconTextGap(4); // 4 = default iconTextGap
        }

        return panel;
      }

      String str;
      List<MediaScraper> objs = getSelectedItems();
      Vector<String> strs = new Vector<>();
      if (objs.isEmpty()) {
        str = BUNDLE.getString("ComboBox.select.mediascraper");
      }
      else {
        for (Object obj : objs) {
          strs.add(obj.toString());
        }
        str = strs.toString();
      }
      return defaultRenderer.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
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
      catch (Exception e) {
        LOGGER.debug("could not load scraper icon: {}", e.getMessage());
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
  }
}
