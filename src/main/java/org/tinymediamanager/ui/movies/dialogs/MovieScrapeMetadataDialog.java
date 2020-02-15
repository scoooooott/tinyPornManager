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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.components.combobox.ScraperMetadataConfigCheckComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieScrapeMetadataDialog. Rescrape metadata
 * 
 * @author Manuel Laggner
 */
public class MovieScrapeMetadataDialog extends TmmDialog {
  private static final long                                              serialVersionUID = 3826984454317979241L;

  private JComboBox<MediaLanguages>                                      cbLanguage;
  private MediaScraperComboBox                                           cbMetadataScraper;
  private MediaScraperCheckComboBox                                      cbArtworkScraper;
  private MediaScraperCheckComboBox                                      cbTrailerScraper;
  private boolean                                                        startScrape      = false;
  private ScraperMetadataConfigCheckComboBox<MovieScraperMetadataConfig> cbScraperConfig;

  /**
   * Instantiates a new movie scrape metadata.
   * 
   * @param title
   *          the title
   */
  public MovieScrapeMetadataDialog(String title) {
    super(title, "updateMetadata");

    // metadataprovider
    MediaScraper defaultScraper = MediaScraper.getMediaScraperById(MovieModuleManager.SETTINGS.getMovieScraper(), ScraperType.MOVIE);

    // artwork scraper
    List<MediaScraper> selectedArtworkScrapers = new ArrayList<>();
    for (MediaScraper artworkScraper : MovieList.getInstance().getAvailableArtworkScrapers()) {
      if (MovieModuleManager.SETTINGS.getArtworkScrapers().contains(artworkScraper.getId())) {
        selectedArtworkScrapers.add(artworkScraper);
      }
    }

    // trailer scraper
    List<MediaScraper> selectedTrailerScrapers = new ArrayList<>();
    for (MediaScraper trailerScraper : MovieList.getInstance().getAvailableTrailerScrapers()) {
      if (MovieModuleManager.SETTINGS.getTrailerScrapers().contains(trailerScraper.getId())) {
        selectedTrailerScrapers.add(trailerScraper);
      }
    }

    {
      JPanel panelCenter = new JPanel();
      getContentPane().add(panelCenter, BorderLayout.CENTER);
      panelCenter.setLayout(new MigLayout("", "[][600lp:800lp,grow]", "[][][][][shrink 0][150lp:n, grow]"));

      JLabel lblLanguageT = new TmmLabel(BUNDLE.getString("metatag.language"));
      panelCenter.add(lblLanguageT, "cell 0 0,alignx trailing");

      cbLanguage = new JComboBox(MediaLanguages.valuesSorted());
      cbLanguage.setSelectedItem(MovieModuleManager.SETTINGS.getScraperLanguage());
      panelCenter.add(cbLanguage, "cell 1 0,growx");

      JLabel lblMetadataScraperT = new TmmLabel(BUNDLE.getString("scraper.metadata"));
      panelCenter.add(lblMetadataScraperT, "cell 0 1,alignx right");

      cbMetadataScraper = new MediaScraperComboBox(MovieList.getInstance().getAvailableMediaScrapers());
      panelCenter.add(cbMetadataScraper, "cell 1 1,growx");
      cbMetadataScraper.setSelectedItem(defaultScraper);

      JLabel lblArtworkScraper = new TmmLabel(BUNDLE.getString("scraper.artwork"));
      panelCenter.add(lblArtworkScraper, "cell 0 2,alignx right");

      cbArtworkScraper = new MediaScraperCheckComboBox(MovieList.getInstance().getAvailableArtworkScrapers());
      panelCenter.add(cbArtworkScraper, "cell 1 2,growx");

      JLabel lblTrailerScraper = new TmmLabel(BUNDLE.getString("scraper.trailer"));
      panelCenter.add(lblTrailerScraper, "cell 0 3,alignx right");

      cbTrailerScraper = new MediaScraperCheckComboBox(MovieList.getInstance().getAvailableTrailerScrapers());
      panelCenter.add(cbTrailerScraper, "cell 1 3,growx");

      JSeparator separator = new JSeparator();
      panelCenter.add(separator, "cell 0 4 2 1,growx");

      JPanel panelScraperConfig = new JPanel();
      panelCenter.add(panelScraperConfig, "cell 0 5 2 1,grow");
      panelScraperConfig.setLayout(new MigLayout("", "[300lp:500lp,grow]", "[][]"));
      {
        JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("scraper.metadata.select"));
        panelScraperConfig.add(lblScrapeFollowingItems, "cell 0 0");

        cbScraperConfig = new ScraperMetadataConfigCheckComboBox(MovieScraperMetadataConfig.values());
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
    if (!selectedTrailerScrapers.isEmpty()) {
      cbTrailerScraper.setSelectedItems(selectedTrailerScrapers);
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

    // language
    movieSearchAndScrapeConfig.setLanguage((MediaLanguages) cbLanguage.getSelectedItem());

    // metadata provider
    movieSearchAndScrapeConfig.setMetadataScraper((MediaScraper) cbMetadataScraper.getSelectedItem());

    // artwork scrapers
    movieSearchAndScrapeConfig.setArtworkScraper(cbArtworkScraper.getSelectedItems());

    // tailer scraper
    movieSearchAndScrapeConfig.setTrailerScraper(cbTrailerScraper.getSelectedItems());

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
