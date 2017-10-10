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
package org.tinymediamanager.ui.tvshows.settings;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowScraperSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowScraperOptionsSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 4999827736720726395L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public TvShowScraperOptionsSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][20lp,grow]", "[][][20lp,shrink 0][][]"));
    {
      final JLabel lblScraperOptionsT = new JLabel(BUNDLE.getString("scraper.metadata.defaults")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperOptionsT, 1.16667, Font.BOLD);
      add(lblScraperOptionsT, "cell 0 0 2 1");
    }
    {
      final TvShowScraperMetadataPanel scraperMetadataPanel = new TvShowScraperMetadataPanel(settings.getScraperMetadataConfig());
      add(scraperMetadataPanel, "cell 1 1,grow");
    }
    {
      final JLabel lblArtworkScrapeT = new JLabel(BUNDLE.getString("Settings.images")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblArtworkScrapeT, 1.16667, Font.BOLD);
      add(lblArtworkScrapeT, "cell 0 3 2 1");
    }
    {
      chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape")); //$NON-NLS-1$
      add(chckbxAutomaticallyScrapeImages, "cell 1 4");
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public static class TvShowScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      defaultScraper;

    public TvShowScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider() == null || scraper.getMediaProvider().getProviderInfo() == null
          || scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
      }
    }

    private ImageIcon getScaledIcon(ImageIcon original) {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(new JPanel().getFont());

      int height = (int) (fm.getHeight() * 2f);
      int width = original.getIconWidth() / original.getIconHeight() * height;

      BufferedImage scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
          Scalr.OP_ANTIALIAS);
      return new ImageIcon(scaledImage);
    }

    public String getScraperId() {
      return scraper.getId();
    }

    public String getScraperName() {
      return scraper.getName() + " - " + scraper.getVersion();
    }

    public String getScraperDescription() {
      // first try to get the localized version
      String description = null;
      try {
        description = BUNDLE.getString("scraper." + scraper.getId() + ".hint"); //$NON-NLS-1$
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

    public Boolean getDefaultScraper() {
      return defaultScraper;
    }

    public void setDefaultScraper(Boolean newValue) {
      Boolean oldValue = this.defaultScraper;
      this.defaultScraper = newValue;
      firePropertyChange("defaultScraper", oldValue, newValue);
    }

    public IMediaProvider getMediaProvider() {
      return scraper.getMediaProvider();
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
  }
}
