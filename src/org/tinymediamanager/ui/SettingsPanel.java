/*
 * Copyright 2012 Manuel Laggner
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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ObjectProperty;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieConnectors;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork.PosterSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class SettingsPanel.
 */
public class SettingsPanel extends JPanel {

  /** The settings. */
  private Settings          settings           = Settings.getInstance();

  /** The tf proxy host. */
  private JTextField        tfProxyHost;

  /** The tf proxy port. */
  private JTextField        tfProxyPort;

  /** The tf proxy username. */
  private JTextField        tfProxyUsername;

  /** The tf proxy password. */
  private JPasswordField    tfProxyPassword;

  /** The table movie sources. */
  private JTable            tableMovieSources;

  /** The cb image tmdb poster size. */
  private JComboBox         cbImageTmdbPosterSize;

  /** The cb image tmdb fanart size. */
  private JComboBox         cbImageTmdbFanartSize;

  /** The cb image tmdb language. */
  private JComboBox         cbImageTmdbLanguage;

  /** The cb scraper tmdb language. */
  private JComboBox         cbScraperTmdbLanguage;
  private JComboBox         cbCountry;
  private JComboBox         cbNfoFormat;
  private JTextField        tfMoviePath;
  private JTextField        tfMovieFilename;
  private final ButtonGroup buttonGroupScraper = new ButtonGroup();
  private JCheckBox         cbImdbTranslateableContent;

  /**
   * Create the panel.
   */
  public SettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default:grow"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    add(tabbedPane, "2, 2, fill, fill");

    JPanel tabMovieSettings = new JPanel();
    tabbedPane.addTab("Movies", null, tabMovieSettings, null);
    tabMovieSettings.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("422px:grow"), },
        new RowSpec[] { RowSpec.decode("fill:66px:grow"), }));

    JScrollPane scrollPaneMovieDetails = new JScrollPane();
    tabMovieSettings.add(scrollPaneMovieDetails, "1, 1, fill, fill");

    JPanel panelMovieSettings = new JPanel();
    scrollPaneMovieDetails.setViewportView(panelMovieSettings);
    panelMovieSettings.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(121dlu;default):grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JPanel panelMovieDataSources = new JPanel();
    panelMovieDataSources.setBorder(new TitledBorder(null, "Data Sources", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panelMovieSettings.add(panelMovieDataSources, "2, 2, fill, top");
    panelMovieDataSources.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("60px:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPane = new JScrollPane();
    panelMovieDataSources.add(scrollPane, "2, 2, 3, 1, fill, fill");

    tableMovieSources = new JTable();
    scrollPane.setViewportView(tableMovieSources);

    JPanel panelMovieSourcesButtons = new JPanel();
    panelMovieDataSources.add(panelMovieSourcesButtons, "6, 2");
    panelMovieSourcesButtons
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton("Add");
    btnAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
          settings.addMovieDataSources(fileChooser.getSelectedFile().getAbsolutePath());
        }
      }
    });

    panelMovieSourcesButtons.add(btnAdd, "2, 2, fill, top");

    JButton btnRemove = new JButton("Remove");
    btnRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int row = tableMovieSources.convertRowIndexToModel(tableMovieSources.getSelectedRow());
        String path = Globals.settings.getMovieDataSource().get(row);
        String[] choices = { "Continue", "Abort" };
        int decision = JOptionPane.showOptionDialog(null, "If you remove " + path
            + " from your data sources, all movies inside this path will also be removed. Continue?", "Remove datasource", JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, choices, "Abort");
        if (decision == 0) {
          Globals.settings.removeMovieDataSources(path);
        }
      }
    });
    panelMovieSourcesButtons.add(btnRemove, "2, 4, fill, top");

    JLabel lblNfoFormat = new JLabel("NFO format");
    panelMovieDataSources.add(lblNfoFormat, "2, 4, right, default");

    cbNfoFormat = new JComboBox(MovieConnectors.values());
    panelMovieDataSources.add(cbNfoFormat, "4, 4, fill, default");

    JLabel lblNfoFileNaming = new JLabel("NFO file naming");
    panelMovieDataSources.add(lblNfoFileNaming, "2, 6");

    final JCheckBox cbMovieNfoFilename1 = new JCheckBox("<filename>.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename1, "4, 6");

    final JCheckBox cbMovieNfoFilename2 = new JCheckBox("movie.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename2, "4, 7");

    JPanel panelMovieImages = new JPanel();
    panelMovieImages.setBorder(new TitledBorder(null, "Poster and Fanart", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panelMovieSettings.add(panelMovieImages, "4, 2, 1, 3, fill, top");
    panelMovieImages.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblSource = new JLabel("Source");
    panelMovieImages.add(lblSource, "2, 2");

    JCheckBox chckbxTheMovieDatabase = new JCheckBox("The Movie Database");
    chckbxTheMovieDatabase.setEnabled(false);
    chckbxTheMovieDatabase.setSelected(true);
    panelMovieImages.add(chckbxTheMovieDatabase, "4, 2");

    JPanel panelMovieImagesTmdb = new JPanel();
    panelMovieImagesTmdb.setBorder(new TitledBorder(null, "The Movie Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panelMovieImages.add(panelMovieImagesTmdb, "2, 4, 3, 1, fill, fill");
    panelMovieImagesTmdb.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblImageTmdbLanguage = new JLabel("Language");
    panelMovieImagesTmdb.add(lblImageTmdbLanguage, "2, 2, right, default");

    cbImageTmdbLanguage = new JComboBox(TmdbMetadataProvider.Languages.values());
    panelMovieImagesTmdb.add(cbImageTmdbLanguage, "4, 2, fill, default");

    JLabel lblImageTmdbPosterSize = new JLabel("Poster size");
    panelMovieImagesTmdb.add(lblImageTmdbPosterSize, "2, 4, right, default");

    cbImageTmdbPosterSize = new JComboBox(PosterSizes.values());
    panelMovieImagesTmdb.add(cbImageTmdbPosterSize, "4, 4, fill, default");

    JLabel lblImageTmdbFanartSize = new JLabel("Fanart size");
    panelMovieImagesTmdb.add(lblImageTmdbFanartSize, "2, 6, right, default");

    cbImageTmdbFanartSize = new JComboBox(FanartSizes.values());
    panelMovieImagesTmdb.add(cbImageTmdbFanartSize, "4, 6, fill, default");

    JLabel lblPosterFilename = new JLabel("Poster file naming");
    panelMovieImages.add(lblPosterFilename, "2, 6");

    final JCheckBox cbMoviePosterFilename1 = new JCheckBox("<movie filename>.tbn");
    panelMovieImages.add(cbMoviePosterFilename1, "4, 6");

    final JCheckBox cbMoviePosterFilename4 = new JCheckBox("poster.jpg");
    panelMovieImages.add(cbMoviePosterFilename4, "6, 6");

    final JCheckBox cbMoviePosterFilename7 = new JCheckBox("<movie filename>.jpg");
    panelMovieImages.add(cbMoviePosterFilename7, "4, 7");

    final JCheckBox cbMoviePosterFilename5 = new JCheckBox("poster.tbn");
    panelMovieImages.add(cbMoviePosterFilename5, "6, 7");

    final JCheckBox cbMoviePosterFilename2 = new JCheckBox("movie.jpg");
    panelMovieImages.add(cbMoviePosterFilename2, "4, 8");

    final JCheckBox cbMoviePosterFilename6 = new JCheckBox("folder.jpg");
    panelMovieImages.add(cbMoviePosterFilename6, "6, 8");

    final JCheckBox cbMoviePosterFilename3 = new JCheckBox("movie.tbn");
    panelMovieImages.add(cbMoviePosterFilename3, "4, 9");

    JLabel lblFanartFileNaming = new JLabel("Fanart file naming");
    panelMovieImages.add(lblFanartFileNaming, "2, 11");

    final JCheckBox cbMovieFanartFilename1 = new JCheckBox("<movie filename>-fanart.jpg");
    panelMovieImages.add(cbMovieFanartFilename1, "4, 11");

    final JCheckBox cbMovieFanartFilename2 = new JCheckBox("fanart.jpg");
    panelMovieImages.add(cbMovieFanartFilename2, "4, 12");

    JPanel panelMovieScrapers = new JPanel();
    panelMovieScrapers.setBorder(new TitledBorder(null, "Scrapers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panelMovieSettings.add(panelMovieScrapers, "2, 4, fill, top");
    panelMovieScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JCheckBox cbScraperTmdb = new JCheckBox("The Movie Database");
    buttonGroupScraper.add(cbScraperTmdb);
    cbScraperTmdb.setSelected(true);
    panelMovieScrapers.add(cbScraperTmdb, "1, 2");

    JLabel lblScraperTmdbLanguage = new JLabel("Language");
    panelMovieScrapers.add(lblScraperTmdbLanguage, "1, 4, right, default");

    cbScraperTmdbLanguage = new JComboBox(TmdbMetadataProvider.Languages.values());
    panelMovieScrapers.add(cbScraperTmdbLanguage, "3, 4");

    JLabel lblCountry = new JLabel("Certification country");
    panelMovieScrapers.add(lblCountry, "1, 6, right, default");

    cbCountry = new JComboBox(CountryCode.values());
    panelMovieScrapers.add(cbCountry, "3, 6, fill, default");

    final JCheckBox cbScraperImdb = new JCheckBox("IMDB");
    buttonGroupScraper.add(cbScraperImdb);
    panelMovieScrapers.add(cbScraperImdb, "1, 8");

    JLabel lblEperimental = new JLabel("experimental!");
    panelMovieScrapers.add(lblEperimental, "3, 8");

    cbImdbTranslateableContent = new JCheckBox("Plot/Title/Tagline from TMDB");
    panelMovieScrapers.add(cbImdbTranslateableContent, "3, 10");

    JPanel panel_1 = new JPanel();
    panel_1.setBorder(new TitledBorder(null, "Renamer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panelMovieSettings.add(panel_1, "2, 6, fill, fill");
    panel_1.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    JLabel lblMoviePath = new JLabel("Folder name");
    panel_1.add(lblMoviePath, "2, 2, right, default");

    tfMoviePath = new JTextField();
    panel_1.add(tfMoviePath, "4, 2, fill, default");
    tfMoviePath.setColumns(10);

    JTextPane txtpntTitle = new JTextPane();
    txtpntTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtpntTitle.setBackground(UIManager.getColor("Panel.background"));
    txtpntTitle.setText("Available pattern:\n$T = Title\n$O = OriginalTitle\n$1 = first letter of the title\n$Y = Year");
    txtpntTitle.setEditable(false);
    panel_1.add(txtpntTitle, "6, 2, 1, 5, fill, fill");

    JLabel lblMovieFilename = new JLabel("File name");
    panel_1.add(lblMovieFilename, "2, 4, right, fill");

    tfMovieFilename = new JTextField();
    lblMovieFilename.setLabelFor(tfMovieFilename);
    panel_1.add(tfMovieFilename, "4, 4, fill, default");
    tfMovieFilename.setColumns(10);

    JTextPane txtrChooseAFolder = new JTextPane();
    txtrChooseAFolder.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtrChooseAFolder
        .setText("Choose a folder and file renaming pattern.\nExample:\nDatasource = /media/movies\nFolder name = $1/$T [$Y]\nFile name = $T\nResult:\nFolder name = /media/movies/A/Aladdin [1992]/\nFile name = Aladdin.avi");
    txtrChooseAFolder.setBackground(UIManager.getColor("Panel.background"));
    panel_1.add(txtrChooseAFolder, "2, 6, 3, 1, fill, fill");

    JPanel tabGeneralSettings = new JPanel();
    tabbedPane.addTab("General", null, tabGeneralSettings, null);
    tabGeneralSettings.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JPanel panelProxySettings = new JPanel();
    panelProxySettings.setBorder(new TitledBorder(null, "Proxy Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    tabGeneralSettings.add(panelProxySettings, "2, 2, left, top");
    panelProxySettings.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblProxyHost = new JLabel("Host");
    panelProxySettings.add(lblProxyHost, "2, 2, right, default");

    tfProxyHost = new JTextField();
    lblProxyHost.setLabelFor(tfProxyHost);
    panelProxySettings.add(tfProxyHost, "4, 2, fill, default");
    tfProxyHost.setColumns(10);

    JLabel lblProxyPort = new JLabel("Port");
    panelProxySettings.add(lblProxyPort, "2, 4, right, default");

    tfProxyPort = new JTextField();
    lblProxyPort.setLabelFor(tfProxyPort);
    panelProxySettings.add(tfProxyPort, "4, 4, fill, default");
    tfProxyPort.setColumns(10);

    JLabel lblProxyUser = new JLabel("Username");
    panelProxySettings.add(lblProxyUser, "2, 6, right, default");

    tfProxyUsername = new JTextField();
    lblProxyUser.setLabelFor(tfProxyUsername);
    panelProxySettings.add(tfProxyUsername, "4, 6, fill, default");
    tfProxyUsername.setColumns(10);

    JLabel lblProxyPassword = new JLabel("Password");
    panelProxySettings.add(lblProxyPassword, "2, 8, right, default");

    tfProxyPassword = new JPasswordField();
    lblProxyPassword.setLabelFor(tfProxyPassword);
    panelProxySettings.add(tfProxyPassword, "4, 8, fill, default");

    JPanel panel = new JPanel();
    add(panel, "2, 4, fill, fill");
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));

    JButton btnSaveSettings = new JButton("Save");
    panel.add(btnSaveSettings, "4, 1");
    btnSaveSettings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // set NFO filenames
        settings.clearMovieNfoFilenames();
        if (cbMovieNfoFilename1.isSelected()) {
          settings.addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
        }
        if (cbMovieNfoFilename2.isSelected()) {
          settings.addMovieNfoFilename(MovieNfoNaming.MOVIE_NFO);
        }

        // set poster filenames
        settings.clearMoviePosterFilenames();
        if (cbMoviePosterFilename1.isSelected()) {
          settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_TBN);
        }
        if (cbMoviePosterFilename2.isSelected()) {
          settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_JPG);
        }
        if (cbMoviePosterFilename3.isSelected()) {
          settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_TBN);
        }
        if (cbMoviePosterFilename4.isSelected()) {
          settings.addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
        }
        if (cbMoviePosterFilename5.isSelected()) {
          settings.addMoviePosterFilename(MoviePosterNaming.POSTER_TBN);
        }
        if (cbMoviePosterFilename6.isSelected()) {
          settings.addMoviePosterFilename(MoviePosterNaming.FOLDER_JPG);
        }
        if (cbMoviePosterFilename7.isSelected()) {
          settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_JPG);
        }

        // set fanart filenames
        settings.clearMovieFanartFilenames();
        if (cbMovieFanartFilename1.isSelected()) {
          settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_JPG);
        }
        if (cbMovieFanartFilename2.isSelected()) {
          settings.addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
        }

        // save scraper
        if (cbScraperImdb.isSelected()) {
          settings.setMovieScraper(MovieScrapers.IMDB);
        }
        else {
          settings.setMovieScraper(MovieScrapers.TMDB);
        }

        // save settings
        settings.saveSettings();
      }
    });

    initDataBindings();

    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getMovieNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }

    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }

    // poster filenames
    List<MoviePosterNaming> moviePosterFilenames = settings.getMoviePosterFilenames();
    if (moviePosterFilenames.contains(MoviePosterNaming.FILENAME_TBN)) {
      cbMoviePosterFilename1.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.MOVIE_JPG)) {
      cbMoviePosterFilename2.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.MOVIE_TBN)) {
      cbMoviePosterFilename3.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.POSTER_JPG)) {
      cbMoviePosterFilename4.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.POSTER_TBN)) {
      cbMoviePosterFilename5.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.FOLDER_JPG)) {
      cbMoviePosterFilename6.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.FILENAME_JPG)) {
      cbMoviePosterFilename7.setSelected(true);
    }

    // fanart filenames
    List<MovieFanartNaming> movieFanartFilenames = settings.getMovieFanartFilenames();
    if (movieFanartFilenames.contains(MovieFanartNaming.FILENAME_JPG)) {
      cbMovieFanartFilename1.setSelected(true);
    }
    if (movieFanartFilenames.contains(MovieFanartNaming.FANART_JPG)) {
      cbMovieFanartFilename2.setSelected(true);
    }

    MovieScrapers movieScraper = settings.getMovieScraper();
    switch (movieScraper) {
      case IMDB:
        cbScraperImdb.setSelected(true);
        break;

      case TMDB:
      default:
        cbScraperTmdb.setSelected(true);
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, String> settingsBeanProperty = BeanProperty.create("proxyHost");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, tfProxyHost, jTextFieldBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_1 = BeanProperty.create("proxyPort");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, tfProxyPort, jTextFieldBeanProperty_1);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_2 = BeanProperty.create("proxyUsername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, tfProxyUsername, jTextFieldBeanProperty_2);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_3 = BeanProperty.create("proxyPassword");
    BeanProperty<JPasswordField, String> jPasswordFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JPasswordField, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, tfProxyPassword, jPasswordFieldBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieDataSource");
    JTableBinding<String, Settings, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, settings, settingsBeanProperty_4,
        tableMovieSources);
    //
    ObjectProperty<String> stringObjectProperty = ObjectProperty.create();
    jTableBinding.addColumnBinding(stringObjectProperty).setColumnName("Source");
    //
    jTableBinding.bind();
    //
    BeanProperty<Settings, PosterSizes> settingsBeanProperty_5 = BeanProperty.create("imageTmdbPosterSize");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, PosterSizes, JComboBox, Object> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, cbImageTmdbPosterSize, jComboBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, FanartSizes> settingsBeanProperty_6 = BeanProperty.create("imageTmdbFanartSize");
    AutoBinding<Settings, FanartSizes, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, cbImageTmdbFanartSize, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Languages> settingsBeanProperty_7 = BeanProperty.create("imageTmdbLangugage");
    AutoBinding<Settings, Languages, JComboBox, Object> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, cbImageTmdbLanguage, jComboBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<Settings, Languages> settingsBeanProperty_8 = BeanProperty.create("scraperTmdbLanguage");
    AutoBinding<Settings, Languages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperTmdbLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("certificationCountry");
    AutoBinding<Settings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<Settings, MovieConnectors> settingsBeanProperty_10 = BeanProperty.create("movieConnector");
    AutoBinding<Settings, MovieConnectors, JComboBox, Object> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_11 = BeanProperty.create("movieRenamerPathname");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, tfMoviePath, jTextFieldBeanProperty_3);
    autoBinding_10.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_12 = BeanProperty.create("movieRenamerFilename");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieFilename, jTextFieldBeanProperty_4);
    autoBinding_11.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_13 = BeanProperty.create("imdbScrapeForeignLanguage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, cbImdbTranslateableContent, jCheckBoxBeanProperty);
    autoBinding_12.bind();
  }
}
