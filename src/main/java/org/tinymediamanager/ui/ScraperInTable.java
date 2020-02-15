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

package org.tinymediamanager.ui;

import java.awt.Canvas;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;

/**
 * The class {@link ScraperInTable} is used to display scrapers in a table
 */
public class ScraperInTable extends AbstractModelObject {
  /** @wbp.nls.resourceBundle messages */
  protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  protected MediaScraper                scraper;
  protected Icon                        scraperLogo;
  protected boolean                     active;

  public ScraperInTable(MediaScraper scraper) {
    this.scraper = scraper;
    if (scraper.getMediaProvider() == null || scraper.getMediaProvider().getProviderInfo() == null
        || scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
      scraperLogo = new ImageIcon();
    }
    else {
      scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
    }
  }

  protected ImageIcon getScaledIcon(ImageIcon original) {
    try {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(new JPanel().getFont());

      int height = (int) (fm.getHeight() * 2f);
      int width = original.getIconWidth() / original.getIconHeight() * height;

      BufferedImage scaledImage;
      if (!scraper.isEnabled()) {
        scaledImage = Scalr.resize(ImageUtils.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
            Scalr.OP_GRAYSCALE);
      }
      else {
        scaledImage = Scalr.resize(ImageUtils.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
            Scalr.OP_ANTIALIAS);
      }
      return new ImageIcon(scaledImage);
    }
    catch (Exception e) {
      return null;
    }
  }

  public String getScraperId() {
    return scraper.getId();
  }

  public String getScraperName() {
    if (StringUtils.isNotBlank(scraper.getVersion())) {
      return scraper.getName() + " - " + scraper.getVersion();
    }
    else {
      return scraper.getName();
    }
  }

  public String getScraperDescription() {
    // first try to get the localized version
    String description = null;
    try {
      description = BUNDLE.getString("scraper." + scraper.getId() + ".hint");
    }
    catch (Exception ignored) {
    }

    if (StringUtils.isBlank(description)) {
      // try to get a scraper text
      description = scraper.getDescription();
    }

    return description;
  }

  public Icon getScraperLogo() {
    return scraperLogo;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean newValue) {
    Boolean oldValue = this.active;
    this.active = newValue;
    firePropertyChange("active", oldValue, newValue);
  }

  public IMediaProvider getMediaProvider() {
    return scraper.getMediaProvider();
  }
}
