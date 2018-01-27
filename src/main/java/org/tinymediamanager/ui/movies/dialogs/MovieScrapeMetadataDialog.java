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
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.panels.MovieScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieScrapeMetadataDialog. Rescrape metadata
 * 
 * @author Manuel Laggner
 */
public class MovieScrapeMetadataDialog extends TmmDialog {
  private static final long           serialVersionUID           = 3826984454317979241L;

  private MovieSearchAndScrapeOptions movieSearchAndScrapeConfig = new MovieSearchAndScrapeOptions();
  private MediaScraperComboBox        cbMetadataScraper;
  private MediaScraperCheckComboBox   cbArtworkScraper;
  private MediaScraperCheckComboBox   cbTrailerScraper;
  private boolean                     startScrape                = false;

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

    // copy the values
    MovieScraperMetadataConfig settings = MovieModuleManager.SETTINGS.getMovieScraperMetadataConfig();

    MovieScraperMetadataConfig scraperMetadataConfig = new MovieScraperMetadataConfig();
    scraperMetadataConfig.setTitle(settings.isTitle());
    scraperMetadataConfig.setOriginalTitle(settings.isOriginalTitle());
    scraperMetadataConfig.setTagline(settings.isTagline());
    scraperMetadataConfig.setPlot(settings.isPlot());
    scraperMetadataConfig.setRating(settings.isRating());
    scraperMetadataConfig.setRuntime(settings.isRuntime());
    scraperMetadataConfig.setYear(settings.isYear());
    scraperMetadataConfig.setCertification(settings.isCertification());
    scraperMetadataConfig.setCast(settings.isCast());
    scraperMetadataConfig.setGenres(settings.isGenres());
    scraperMetadataConfig.setArtwork(settings.isArtwork());
    scraperMetadataConfig.setTrailer(settings.isTrailer());
    scraperMetadataConfig.setCollection(settings.isCollection());
    scraperMetadataConfig.setTags(settings.isTags());

    movieSearchAndScrapeConfig.setScraperMetadataConfig(scraperMetadataConfig);

    {
      JPanel panelCenter = new JPanel();
      getContentPane().add(panelCenter, BorderLayout.CENTER);
      panelCenter.setLayout(new MigLayout("", "[][300lp,grow]", "[][][][20lp:n][shrink 0][][]"));

      JLabel lblMetadataScraperT = new TmmLabel(BUNDLE.getString("scraper.metadata"));
      panelCenter.add(lblMetadataScraperT, "cell 0 0,alignx right");

      cbMetadataScraper = new MediaScraperComboBox(MovieList.getInstance().getAvailableMediaScrapers());
      panelCenter.add(cbMetadataScraper, "cell 1 0,growx");
      cbMetadataScraper.setSelectedItem(defaultScraper);

      JLabel lblArtworkScraper = new TmmLabel(BUNDLE.getString("scraper.artwork"));
      panelCenter.add(lblArtworkScraper, "cell 0 1,alignx right");

      cbArtworkScraper = new MediaScraperCheckComboBox(MovieList.getInstance().getAvailableArtworkScrapers());
      panelCenter.add(cbArtworkScraper, "cell 1 1,growx");

      JLabel lblTrailerScraper = new TmmLabel(BUNDLE.getString("scraper.trailer"));
      panelCenter.add(lblTrailerScraper, "cell 0 2,alignx right");

      cbTrailerScraper = new MediaScraperCheckComboBox(MovieList.getInstance().getAvailableTrailerScrapers());
      panelCenter.add(cbTrailerScraper, "cell 1 2,growx");

      JSeparator separator = new JSeparator();
      panelCenter.add(separator, "cell 0 4 2 1,growx");

      JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("scraper.metadata.select")); //$NON-NLS-1$
      panelCenter.add(lblScrapeFollowingItems, "cell 0 5 2 1,growx");

      JPanel panelScraperMetadataSetting = new MovieScraperMetadataPanel(this.movieSearchAndScrapeConfig.getScraperMetadataConfig());
      panelCenter.add(panelScraperMetadataSetting, "cell 0 6 2 1,grow");
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

    // default scrapers
    if (!selectedArtworkScrapers.isEmpty()) {
      cbArtworkScraper.setSelectedItems(selectedArtworkScrapers);
    }
    if (!selectedTrailerScrapers.isEmpty()) {
      cbTrailerScraper.setSelectedItems(selectedTrailerScrapers);
    }
  }

  /**
   * Pass the movie search and scrape config to the caller.
   * 
   * @return the movie search and scrape config
   */
  public MovieSearchAndScrapeOptions getMovieSearchAndScrapeConfig() {
    // metadata provider
    movieSearchAndScrapeConfig.setMetadataScraper((MediaScraper) cbMetadataScraper.getSelectedItem());

    // artwork scrapers
    for (MediaScraper scraper : cbArtworkScraper.getSelectedItems()) {
      movieSearchAndScrapeConfig.addArtworkScraper(scraper);
    }

    // tailer scraper
    for (MediaScraper scraper : cbTrailerScraper.getSelectedItems()) {
      movieSearchAndScrapeConfig.addTrailerScraper(scraper);
    }

    return movieSearchAndScrapeConfig;
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
