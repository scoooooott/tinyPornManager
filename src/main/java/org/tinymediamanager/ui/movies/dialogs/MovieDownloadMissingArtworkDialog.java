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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.components.combobox.ScraperMetadataConfigCheckComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieDownloadMissingArtworkDialog is used to provide a UI for specifying which missing artwork should be downloaded
 * 
 * @author Manuel Laggner
 */
public class MovieDownloadMissingArtworkDialog extends TmmDialog {
  private static final long                                              serialVersionUID = -2126984610197979241L;

  private MediaScraperCheckComboBox                                      cbArtworkScraper;
  private boolean                                                        startScrape      = false;
  private ScraperMetadataConfigCheckComboBox<MovieScraperMetadataConfig> cbScraperConfig;

  public MovieDownloadMissingArtworkDialog() {
    super(BUNDLE.getString("movie.downloadmissingartwork"), "downloadMissingArtwork");

    // artwork scraper
    List<MediaScraper> selectedArtworkScrapers = new ArrayList<>();
    for (MediaScraper artworkScraper : MovieList.getInstance().getAvailableArtworkScrapers()) {
      if (MovieModuleManager.SETTINGS.getArtworkScrapers().contains(artworkScraper.getId())) {
        selectedArtworkScrapers.add(artworkScraper);
      }
    }

    {
      JPanel panelCenter = new JPanel();
      getContentPane().add(panelCenter, BorderLayout.CENTER);
      panelCenter.setLayout(new MigLayout("", "[][300lp:400lp,grow]", "[][][][][shrink 0][150lp:n, grow]"));

      JLabel lblArtworkScraper = new TmmLabel(BUNDLE.getString("scraper.artwork"));
      panelCenter.add(lblArtworkScraper, "cell 0 2,alignx right");

      cbArtworkScraper = new MediaScraperCheckComboBox(MovieList.getInstance().getAvailableArtworkScrapers());
      panelCenter.add(cbArtworkScraper, "cell 1 2,growx");

      JSeparator separator = new JSeparator();
      panelCenter.add(separator, "cell 0 4 2 1,growx");

      JPanel panelScraperConfig = new JPanel();
      panelCenter.add(panelScraperConfig, "cell 0 5 2 1,grow");
      panelScraperConfig.setLayout(new MigLayout("", "[300lp:400lp,grow]", "[][]"));
      {
        JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("scraper.metadata.select"));
        panelScraperConfig.add(lblScrapeFollowingItems, "cell 0 0");

        cbScraperConfig = new ScraperMetadataConfigCheckComboBox(MovieScraperMetadataConfig.getArtworkTypes());
        panelScraperConfig.add(cbScraperConfig, "cell 0 1 ,wmin 0,grow");
      }
    }
    {
      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
      btnCancel.setIcon(IconManager.CANCEL_INV);
      btnCancel.addActionListener(e -> {
        startScrape = false;
        setVisible(false);
      });
      addButton(btnCancel);

      JButton btnStart = new JButton(BUNDLE.getString("scraper.start"));
      btnStart.setIcon(IconManager.APPLY_INV);
      btnStart.addActionListener(e -> {
        startScrape = true;
        setVisible(false);
      });
      addDefaultButton(btnStart);
    }
    // set data

    // default scrapers
    if (!selectedArtworkScrapers.isEmpty()) {
      cbArtworkScraper.setSelectedItems(selectedArtworkScrapers);
    }

    // pre-set config
    cbScraperConfig.setSelectedItems(MovieModuleManager.SETTINGS.getScraperMetadataConfig());
  }

  /**
   * Pass the movie search and scrape config to the caller.
   * 
   * @return the movie search and scrape config
   */
  public MovieSearchAndScrapeOptions getMovieSearchAndScrapeOptions() {
    MovieSearchAndScrapeOptions movieSearchAndScrapeConfig = new MovieSearchAndScrapeOptions();

    // artwork scrapers
    movieSearchAndScrapeConfig.setArtworkScraper(cbArtworkScraper.getSelectedItems());

    return movieSearchAndScrapeConfig;
  }

  /**
   * Pass the movie meta data config to the caller
   * 
   * @return the movie meta data config
   */
  public List<MovieScraperMetadataConfig> getMovieScraperMetadataConfig() {
    return cbScraperConfig.getSelectedItems();
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
