/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieArtworkScrapers;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.MovieTrailerScrapers;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieScrapeMetadataDialog. Rescrape metadata
 * 
 * @author Manuel Laggner
 */
public class MovieScrapeMetadataDialog extends TmmDialog {
  private static final long           serialVersionUID           = 3826984454317979241L;
  private static final ResourceBundle BUNDLE                     = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSearchAndScrapeOptions movieSearchAndScrapeConfig = new MovieSearchAndScrapeOptions();
  private JComboBox                   cbMetadataScraper;
  private JCheckBox                   chckbxTheMovieDb;
  private JCheckBox                   chckbxFanarttv;
  private JCheckBox                   chckbxTheMovieDb_1;
  private JCheckBox                   chckbxHdtrailernet;
  private JCheckBox                   chckbxOfdbde;
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

    movieSearchAndScrapeConfig.setScraperMetadataConfig(scraperMetadataConfig);

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new BorderLayout(0, 0));

    JPanel panelScraper = new JPanel();
    panelContent.add(panelScraper, BorderLayout.NORTH);
    panelScraper.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblMetadataScraperT = new JLabel(BUNDLE.getString("scraper.metadata")); //$NON-NLS-1$
    panelScraper.add(lblMetadataScraperT, "2, 2, right, default");

    cbMetadataScraper = new JComboBox(MovieScrapers.values());
    panelScraper.add(cbMetadataScraper, "4, 2, 3, 1, fill, default");

    JLabel lblArtworkScraper = new JLabel(BUNDLE.getString("scraper.artwork")); //$NON-NLS-1$
    panelScraper.add(lblArtworkScraper, "2, 4, right, default");

    chckbxTheMovieDb = new JCheckBox("The Movie DB");
    panelScraper.add(chckbxTheMovieDb, "4, 4");

    chckbxFanarttv = new JCheckBox("Fanart.tv");
    panelScraper.add(chckbxFanarttv, "6, 4");

    JLabel lblTrailerScraper = new JLabel(BUNDLE.getString("scraper.trailer")); //$NON-NLS-1$
    panelScraper.add(lblTrailerScraper, "2, 6, right, default");

    chckbxTheMovieDb_1 = new JCheckBox("The Movie DB");
    panelScraper.add(chckbxTheMovieDb_1, "4, 6");

    chckbxHdtrailernet = new JCheckBox("HD-Trailer.net");
    panelScraper.add(chckbxHdtrailernet, "6, 6");

    chckbxOfdbde = new JCheckBox("OFDb.de");
    panelScraper.add(chckbxOfdbde, "8, 6");

    {
      JPanel panelCenter = new JPanel();
      panelContent.add(panelCenter, BorderLayout.CENTER);
      panelCenter.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_ROWSPEC, }));

      JPanel panelScraperMetadataSetting = new MovieScraperMetadataPanel(this.movieSearchAndScrapeConfig.getScraperMetadataConfig());
      panelScraperMetadataSetting.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), BUNDLE.getString("scraper.metadata.select"),
          TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$,
      panelCenter.add(panelScraperMetadataSetting, "2, 2");
    }

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
    panelContent.add(panelButtons, BorderLayout.SOUTH);

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
    MovieScrapers defaultScraper = MovieModuleManager.MOVIE_SETTINGS.getMovieScraper();
    cbMetadataScraper.setSelectedItem(defaultScraper);

    // artwork provider
    if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperTmdb()) {
      chckbxTheMovieDb.setSelected(true);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isImageScraperFanartTv()) {
      chckbxFanarttv.setSelected(true);
    }

    // trailer provider
    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperTmdb()) {
      chckbxTheMovieDb_1.setSelected(true);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperHdTrailers()) {
      chckbxHdtrailernet.setSelected(true);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isTrailerScraperOfdb()) {
      chckbxOfdbde.setSelected(true);
    }
  }

  /**
   * Pass the movie search and scrape config to the caller.
   * 
   * @return the movie search and scrape config
   */
  public MovieSearchAndScrapeOptions getMovieSearchAndScrapeConfig() {
    // metadata provider
    movieSearchAndScrapeConfig.setMetadataScraper((MovieScrapers) cbMetadataScraper.getSelectedItem());

    // artwork provider
    if (chckbxTheMovieDb.isSelected()) {
      movieSearchAndScrapeConfig.addArtworkScraper(MovieArtworkScrapers.TMDB);
    }

    if (chckbxFanarttv.isSelected()) {
      movieSearchAndScrapeConfig.addArtworkScraper(MovieArtworkScrapers.FANART_TV);
    }

    // tailer provider
    if (chckbxTheMovieDb_1.isSelected()) {
      movieSearchAndScrapeConfig.addTrailerScraper(MovieTrailerScrapers.TMDB);
    }

    if (chckbxHdtrailernet.isSelected()) {
      movieSearchAndScrapeConfig.addTrailerScraper(MovieTrailerScrapers.HDTRAILERS);
    }

    if (chckbxOfdbde.isSelected()) {
      movieSearchAndScrapeConfig.addTrailerScraper(MovieTrailerScrapers.OFDB);
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
