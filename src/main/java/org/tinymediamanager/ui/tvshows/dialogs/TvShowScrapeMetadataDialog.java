/*
 * Copyright 2012 - 2019 Manuel Laggner
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.tinymediamanager.core.tvshow.TvShowEpisodeScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.components.combobox.ScraperMetadataConfigCheckComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowScrapeMetadataDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeMetadataDialog extends TmmDialog {
  private static final long                                                      serialVersionUID = 6120530120703772160L;

  private boolean                                                                startScrape      = false;

  /** UI components */
  private JComboBox                                                              cbLanguage;
  private MediaScraperComboBox                                                   cbMetadataScraper;
  private MediaScraperCheckComboBox                                              cbArtworkScraper;
  private ScraperMetadataConfigCheckComboBox<TvShowScraperMetadataConfig>        cbTvShowScraperConfig;
  private ScraperMetadataConfigCheckComboBox<TvShowEpisodeScraperMetadataConfig> cbEpisodeScraperConfig;

  /**
   * create the dialog with all fields available
   *
   * @param title
   *          the title to display
   * @wbp.parser.constructor
   */
  public TvShowScrapeMetadataDialog(String title) {
    this(title, true, true, true, true);
  }

  /**
   * create the scraper dialog with displaying just set fields
   * 
   * @param title
   *          the title to display
   * @param metadata
   *          show the metadata scraper block?
   * @param artwork
   *          show the artwork scraper block?
   * @param tvShowMetadata
   *          show the TV show metadata config block?
   * @param episodeMetadata
   *          show the episode metadata config block?
   */
  public TvShowScrapeMetadataDialog(String title, boolean metadata, boolean artwork, boolean tvShowMetadata, boolean episodeMetadata) {
    super(title, "tvShowUpdateMetadata");

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new MigLayout("hidemode 3", "[][600lp:800lp,grow]", "[][][][shrink 0][200lp:n, grow]"));

    JLabel lblLanguageT = new TmmLabel(BUNDLE.getString("metatag.language"));
    panelContent.add(lblLanguageT, "cell 0 0,alignx trailing");

    cbLanguage = new JComboBox(MediaLanguages.valuesSorted());
    cbLanguage.setSelectedItem(TvShowModuleManager.SETTINGS.getScraperLanguage());
    panelContent.add(cbLanguage, "cell 1 0,growx");

    if (metadata) {
      JLabel lblMetadataScraperT = new TmmLabel(BUNDLE.getString("scraper.metadata"));
      panelContent.add(lblMetadataScraperT, "cell 0 1,alignx trailing");

      cbMetadataScraper = new MediaScraperComboBox(TvShowList.getInstance().getAvailableMediaScrapers());
      panelContent.add(cbMetadataScraper, "cell 1 1,growx");
    }
    if (artwork) {
      JLabel lblArtworkScraper = new TmmLabel(BUNDLE.getString("scraper.artwork"));
      panelContent.add(lblArtworkScraper, "cell 0 2,alignx trailing");

      cbArtworkScraper = new MediaScraperCheckComboBox(TvShowList.getInstance().getAvailableArtworkScrapers());
      panelContent.add(cbArtworkScraper, "cell 1 2,growx");
    }
    {
      JSeparator separator = new JSeparator();
      panelContent.add(separator, "cell 0 3 2 1,growx");
    }
    if (tvShowMetadata || episodeMetadata) {
      JPanel panelScraperConfig = new JPanel();
      panelContent.add(panelScraperConfig, "cell 0 4 2 1,grow");
      panelScraperConfig.setLayout(new MigLayout("", "[][300lp:500lp,grow]", "[][][]"));
      {
        JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("chooser.scrape"));
        panelScraperConfig.add(lblScrapeFollowingItems, "cell 0 0 2 1");
      }
      if (tvShowMetadata) {
        JLabel lblTvShowsT = new TmmLabel(BUNDLE.getString("metatag.tvshows"));
        panelScraperConfig.add(lblTvShowsT, "cell 0 1,alignx trailing");

        cbTvShowScraperConfig = new ScraperMetadataConfigCheckComboBox(TvShowScraperMetadataConfig.values());
        panelScraperConfig.add(cbTvShowScraperConfig, "cell 1 1,grow, wmin 0");
      }
      if (episodeMetadata) {

        JLabel lblEpisodesT = new TmmLabel(BUNDLE.getString("metatag.episodes"));
        panelScraperConfig.add(lblEpisodesT, "cell 0 2,alignx trailing");

        cbEpisodeScraperConfig = new ScraperMetadataConfigCheckComboBox(TvShowEpisodeScraperMetadataConfig.values());
        panelScraperConfig.add(cbEpisodeScraperConfig, "cell 1 2,grow, wmin 0");
      }
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

    // pre-set configs
    if (cbTvShowScraperConfig != null) {
      cbTvShowScraperConfig.setSelectedItems(TvShowModuleManager.SETTINGS.getTvShowScraperMetadataConfig());
    }
    if (cbEpisodeScraperConfig != null) {
      cbEpisodeScraperConfig.setSelectedItems(TvShowModuleManager.SETTINGS.getEpisodeScraperMetadataConfig());
    }
  }

  /**
   * Pass the tv show search and scrape config to the caller.
   * 
   * @return the tv show search and scrape config
   */
  public TvShowSearchAndScrapeOptions getTvShowSearchAndScrapeOptions() {
    TvShowSearchAndScrapeOptions tvShowSearchAndScrapeConfig = new TvShowSearchAndScrapeOptions();

    // language
    tvShowSearchAndScrapeConfig.setLanguage((MediaLanguages) cbLanguage.getSelectedItem());

    // metadata provider
    tvShowSearchAndScrapeConfig.setMetadataScraper((MediaScraper) cbMetadataScraper.getSelectedItem());

    // artwork scrapers
    tvShowSearchAndScrapeConfig.setArtworkScraper(cbArtworkScraper.getSelectedItems());

    return tvShowSearchAndScrapeConfig;
  }

  /**
   * Pass the episode search and scrape config to the caller.
   *
   * @return the episode search and scrape config
   */
  public TvShowEpisodeSearchAndScrapeOptions getTvShowEpisodeSearchAndScrapeOptions() {
    TvShowEpisodeSearchAndScrapeOptions episodeSearchAndScrapeOptions = new TvShowEpisodeSearchAndScrapeOptions();

    // language
    episodeSearchAndScrapeOptions.setLanguage((MediaLanguages) cbLanguage.getSelectedItem());

    // metadata provider
    episodeSearchAndScrapeOptions.setMetadataScraper((MediaScraper) cbMetadataScraper.getSelectedItem());

    // artwork scrapers
    episodeSearchAndScrapeOptions.setArtworkScraper(cbArtworkScraper.getSelectedItems());

    return episodeSearchAndScrapeOptions;
  }

  /**
   * pass the tv show meta data config to the caller
   * 
   * @return a list of meta data config
   */
  public List<TvShowScraperMetadataConfig> getTvShowScraperMetadataConfig() {
    return cbTvShowScraperConfig.getSelectedItems();
  }

  /**
   * pass the episode meta data config to the caller
   * 
   * @return a list of meta data config
   */
  public List<TvShowEpisodeScraperMetadataConfig> getTvShowEpisodeScraperMetadataConfig() {
    return cbEpisodeScraperConfig.getSelectedItems();
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
