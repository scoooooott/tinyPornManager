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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.japura.gui.CheckComboBox;
import org.japura.gui.model.ListCheckModel;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.components.MediaScraperComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieScrapeMetadataDialog. Rescrape metadata
 * 
 * @author Manuel Laggner
 */
public class MovieScrapeMetadataDialog extends TmmDialog {
  private static final long           serialVersionUID           = 3826984454317979241L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE                     = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSearchAndScrapeOptions movieSearchAndScrapeConfig = new MovieSearchAndScrapeOptions();
  private MediaScraperComboBox        cbMetadataScraper;
  private CheckComboBox               cbArtworkScraper;
  private CheckComboBox               cbTrailerScraper;
  private boolean                     startScrape                = false;

  /**
   * Instantiates a new movie scrape metadata.
   * 
   * @param title
   *          the title
   */
  public MovieScrapeMetadataDialog(String title) {
    super(title, "updateMetadata");
    setBounds(5, 5, 550, 280);
    setMinimumSize(new Dimension(getWidth(), getHeight()));

    // copy the values
    MovieScraperMetadataConfig settings = Globals.settings.getMovieScraperMetadataConfig();

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

    JPanel panelCenter = new JPanel();
    getContentPane().add(panelCenter, BorderLayout.CENTER);
    panelCenter.setLayout(
        new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, }));

    JPanel panelScraper = new JPanel();
    panelCenter.add(panelScraper, "2, 2, default, fill");
    panelScraper.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));

    JLabel lblMetadataScraperT = new JLabel(BUNDLE.getString("scraper.metadata")); //$NON-NLS-1$
    panelScraper.add(lblMetadataScraperT, "2, 2, right, default");

    cbMetadataScraper = new MediaScraperComboBox(MovieList.getInstance().getAvailableMediaScrapers());
    panelScraper.add(cbMetadataScraper, "4, 2, 5, 1");

    JLabel lblArtworkScraper = new JLabel(BUNDLE.getString("scraper.artwork")); //$NON-NLS-1$
    panelScraper.add(lblArtworkScraper, "2, 4, right, default");

    cbArtworkScraper = new MediaScraperCheckComboBox();
    cbArtworkScraper.setTextFor(CheckComboBox.NONE, BUNDLE.getString("scraper.selected.none")); //$NON-NLS-1$
    cbArtworkScraper.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("scraper.selected.multiple")); //$NON-NLS-1$
    cbArtworkScraper.setTextFor(CheckComboBox.ALL, BUNDLE.getString("scraper.selected.all")); //$NON-NLS-1$
    panelScraper.add(cbArtworkScraper, "4, 4, 5, 1");

    JLabel lblTrailerScraper = new JLabel(BUNDLE.getString("scraper.trailer")); //$NON-NLS-1$
    panelScraper.add(lblTrailerScraper, "2, 6, right, default");

    cbTrailerScraper = new MediaScraperCheckComboBox();
    cbTrailerScraper.setTextFor(CheckComboBox.NONE, BUNDLE.getString("scraper.selected.none")); //$NON-NLS-1$
    cbTrailerScraper.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("scraper.selected.multiple")); //$NON-NLS-1$
    cbTrailerScraper.setTextFor(CheckComboBox.ALL, BUNDLE.getString("scraper.selected.all")); //$NON-NLS-1$
    panelScraper.add(cbTrailerScraper, "4, 6, 5, 1");

    JPanel panelScraperMetadataSetting = new MovieScraperMetadataPanel(this.movieSearchAndScrapeConfig.getScraperMetadataConfig());
    panelScraperMetadataSetting.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), BUNDLE.getString("scraper.metadata.select"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); // $NON-NLS-1$,
    panelCenter.add(panelScraperMetadataSetting, "2, 4, default, fill");

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
    getContentPane().add(panelButtons, BorderLayout.SOUTH);

    JButton btnStart = new JButton(BUNDLE.getString("scraper.start")); //$NON-NLS-1$
    btnStart.setIcon(IconManager.APPLY);
    btnStart.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startScrape = true;
        setVisible(false);
      }
    });
    panelButtons.add(btnStart);

    JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    btnCancel.setIcon(IconManager.CANCEL);
    btnCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startScrape = false;
        setVisible(false);
      }
    });
    panelButtons.add(btnCancel);

    // set data

    // metadataprovider
    MediaScraper defaultScraper = MediaScraper.getMediaScraperById(MovieModuleManager.MOVIE_SETTINGS.getMovieScraper(), ScraperType.MOVIE);
    cbMetadataScraper.setSelectedItem(defaultScraper);

    ListCheckModel model = cbArtworkScraper.getModel();
    for (MediaScraper artworkScraper : MovieList.getInstance().getAvailableArtworkScrapers()) {
      model.addElement(artworkScraper);

      if (MovieModuleManager.MOVIE_SETTINGS.getMovieArtworkScrapers().contains(artworkScraper.getId())) {
        model.addCheck(artworkScraper);
      }
    }

    // trailer scraper
    model = cbTrailerScraper.getModel();
    for (MediaScraper trailerScraper : MovieList.getInstance().getAvailableTrailerScrapers()) {
      model.addElement(trailerScraper);

      if (MovieModuleManager.MOVIE_SETTINGS.getMovieTrailerScrapers().contains(trailerScraper.getId())) {
        model.addCheck(trailerScraper);
      }
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
    ListCheckModel model = cbArtworkScraper.getModel();
    for (Object checked : model.getCheckeds()) {
      if (checked != null && checked instanceof MediaScraper) {
        movieSearchAndScrapeConfig.addArtworkScraper((MediaScraper) checked);
      }
    }

    // tailer scraper
    model = cbTrailerScraper.getModel();
    for (Object checked : model.getCheckeds()) {
      if (checked != null && checked instanceof MediaScraper) {
        movieSearchAndScrapeConfig.addTrailerScraper((MediaScraper) checked);
      }
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
