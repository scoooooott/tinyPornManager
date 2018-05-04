/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.tvshows.panels.TvShowScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;


/**
 * The Class TvShowScrapeMetadataDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeMetadataDialog extends TmmDialog {
  private static final long            serialVersionUID            = 6120530120703772160L;

  private TvShowSearchAndScrapeOptions tvShowSearchAndScrapeConfig = new TvShowSearchAndScrapeOptions();
  private boolean                      startScrape                 = true;

  /** UI components */
  private MediaScraperComboBox         cbMetadataScraper;
  private MediaScraperCheckComboBox    cbArtworkScraper;

  public TvShowScrapeMetadataDialog(String title) {
    super(title, "updateMetadata");

    // copy the values
    TvShowScraperMetadataConfig settings = TvShowModuleManager.SETTINGS.getScraperMetadataConfig();

    TvShowScraperMetadataConfig scraperMetadataConfig = new TvShowScraperMetadataConfig();
    scraperMetadataConfig.setTitle(settings.isTitle());
    scraperMetadataConfig.setPlot(settings.isPlot());
    scraperMetadataConfig.setAired(settings.isAired());
    scraperMetadataConfig.setRating(settings.isRating());
    scraperMetadataConfig.setRuntime(settings.isRuntime());
    scraperMetadataConfig.setYear(settings.isYear());
    scraperMetadataConfig.setCertification(settings.isCertification());
    scraperMetadataConfig.setCast(settings.isCast());
    scraperMetadataConfig.setGenres(settings.isGenres());
    scraperMetadataConfig.setArtwork(settings.isArtwork());
    scraperMetadataConfig.setEpisodes(settings.isEpisodes());
    scraperMetadataConfig.setStatus(settings.isStatus());
    scraperMetadataConfig.setEpisodeList(TvShowModuleManager.SETTINGS.isDisplayMissingEpisodes());

    tvShowSearchAndScrapeConfig.setScraperMetadataConfig(scraperMetadataConfig);

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][300lp,grow]", "[][][20lp:n][][][]"));

      JLabel lblMetadataScraperT = new TmmLabel(BUNDLE.getString("scraper.metadata"));
      panelContent.add(lblMetadataScraperT, "cell 0 0,alignx right");

      cbMetadataScraper = new MediaScraperComboBox(TvShowList.getInstance().getAvailableMediaScrapers());
      panelContent.add(cbMetadataScraper, "cell 1 0,growx");

      JLabel lblArtworkScraper = new TmmLabel(BUNDLE.getString("scraper.artwork"));
      panelContent.add(lblArtworkScraper, "cell 0 1,alignx right");

      cbArtworkScraper = new MediaScraperCheckComboBox(TvShowList.getInstance().getAvailableArtworkScrapers());
      panelContent.add(cbArtworkScraper, "cell 1 1,growx");

      JSeparator separator = new JSeparator();
      panelContent.add(separator, "cell 0 3 2 1,growx");

      JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("scraper.metadata.select")); //$NON-NLS-1$
      panelContent.add(lblScrapeFollowingItems, "cell 0 4 2 1,growx");

      JPanel panelScraperMetadataSetting = new TvShowScraperMetadataPanel(this.tvShowSearchAndScrapeConfig.getScraperMetadataConfig());
      panelContent.add(panelScraperMetadataSetting, "cell 0 5 2 1,grow");
    }
    {
      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      btnCancel.setIcon(IconManager.CANCEL_INV);
      btnCancel.addActionListener(e -> {
        startScrape = false;
        setVisible(false);
      });
      addButton(btnCancel);

      JButton btnStart = new JButton(BUNDLE.getString("scraper.start")); //$NON-NLS-1$
      btnStart.setIcon(IconManager.APPLY_INV);
      btnStart.addActionListener(e -> {
        startScrape = true;
        setVisible(false);
      });
      addDefaultButton(btnStart);
    }
    // set data

    // metadataprovider
    MediaScraper defaultScraper = TvShowList.getInstance().getDefaultMediaScraper();
    cbMetadataScraper.setSelectedItem(defaultScraper);

    // artwork scraper
    List<MediaScraper> selectedArtworkScrapers = new ArrayList<>();
    for (MediaScraper artworkScraper : TvShowList.getInstance().getAvailableArtworkScrapers()) {
      if (TvShowModuleManager.SETTINGS.getArtworkScrapers().contains(artworkScraper.getId())) {
        selectedArtworkScrapers.add(artworkScraper);
      }
    }
    if (!selectedArtworkScrapers.isEmpty()) {
      cbArtworkScraper.setSelectedItems(selectedArtworkScrapers);
    }
  }

  /**
   * Pass the tv show search and scrape config to the caller.
   * 
   * @return the tv show search and scrape config
   */
  public TvShowSearchAndScrapeOptions getTvShowSearchAndScrapeConfig() {
    // metadata provider
    tvShowSearchAndScrapeConfig.setMetadataScraper((MediaScraper) cbMetadataScraper.getSelectedItem());

    // artwork scrapers
    for (MediaScraper scraper : cbArtworkScraper.getSelectedItems()) {
      tvShowSearchAndScrapeConfig.addArtworkScraper(scraper);
    }

    return tvShowSearchAndScrapeConfig;
  }

  /**
   * Should start scrape.
   * 
   * @return true, if successful
   */
  public boolean shouldStartScrape() {
    return startScrape;
  }
}
