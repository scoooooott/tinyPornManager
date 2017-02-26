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
package org.tinymediamanager.ui.components;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.imgscalr.Scalr;
import org.japura.gui.CheckComboBox;
import org.japura.gui.CheckList;
import org.japura.gui.model.ListCheckModel;
import org.japura.gui.renderer.CheckListRenderer;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.IconManager;

public class MediaScraperCheckComboBox extends CheckComboBox {
  private static final long serialVersionUID = 1143686322977103280L;

  public MediaScraperCheckComboBox() {
    super();
    setRenderer(new IconCheckListRenderer());
  }

  class IconCheckListRenderer extends CheckListRenderer {
    private static final long   serialVersionUID = -5195029492716016109L;
    protected JPanel            panel            = new JPanel();
    protected JLabel            label            = new JLabel();
    private Map<URI, ImageIcon> imageCache;

    public IconCheckListRenderer() {
      super();
      panel.setLayout(new FlowLayout(FlowLayout.LEFT));
      panel.add(this);
      panel.add(label);
      label.setOpaque(false);
      setOpaque(false);
      imageCache = new HashMap<>();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (list instanceof CheckList) {
        // calculate the max width of the logo
        int maxWidth = 0;
        for (int i = 0; i < list.getModel().getSize(); i++) {
          Object obj = list.getModel().getElementAt(i);
          if (obj instanceof MediaScraper) {
            MediaScraper ms = (MediaScraper) obj;
            ImageIcon logo = getIcon(ms.getLogoURL());
            maxWidth = Math.max(maxWidth, logo.getIconWidth());
          }
        }

        CheckList cbl = (CheckList) list;
        ListCheckModel model = cbl.getModel();
        boolean checked = model.isChecked(value);
        boolean locked = model.isLocked(value);
        setSelected(checked);

        if (locked || cbl.isEnabled() == false) {
          setEnabled(false);
        }
        else {
          setEnabled(true);
        }

        if (getHighlight().equals(Highlight.MOUSE_OVER_AND_CHECKED_ITEMS) && (checked || isSelected)) {
          panel.setBackground(selectionBackground);
          panel.setForeground(selectionForeground);
        }
        else if (getHighlight().equals(Highlight.MOUSE_OVER) && isSelected) {
          panel.setBackground(selectionBackground);
          panel.setForeground(selectionForeground);
        }
        else if (getHighlight().equals(Highlight.CHECKED_ITEMS) && checked) {
          panel.setBackground(selectionBackground);
          panel.setForeground(selectionForeground);
        }
        else {
          panel.setBackground(background);
          panel.setForeground(foreground);
        }
        if (value instanceof MediaScraper) {
          MediaScraper scraper = (MediaScraper) value;
          int currentWidth = 0;
          ImageIcon logo = getIcon(scraper.getLogoURL());
          if (logo != null) {
            currentWidth = logo.getIconWidth();
          }

          label.setIcon(logo);
          label.setIconTextGap(maxWidth + 4 - currentWidth); // 4 = default iconTextGap
        }
        else {
          label.setIcon(null);
          label.setIconTextGap(4); // 4 = default iconTextGap
        }
      }
      label.setText(getText(value));
      return panel;
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

      BufferedImage scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
          Scalr.OP_ANTIALIAS);
      return new ImageIcon(scaledImage);
    }
  }
}
