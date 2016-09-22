/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.ui.movies.settings;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieImageSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieImageSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 7312645402037806284L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$ @wbp.nls.resourceBundle

  private MovieSettings               settings         = MovieModuleManager.MOVIE_SETTINGS;
  private List<ArtworkScraper>        scrapers         = ObservableCollections.observableList(new ArrayList<ArtworkScraper>());

  private JComboBox                   cbImagePosterSize;
  private JComboBox                   cbImageFanartSize;
  private JCheckBox                   cbMoviePosterFilename2;
  private JCheckBox                   cbMoviePosterFilename4;
  private JCheckBox                   cbMoviePosterFilename6;
  private JCheckBox                   cbMoviePosterFilename7;
  private JCheckBox                   cbMovieFanartFilename1;
  private JCheckBox                   cbMovieFanartFilename2;
  private JCheckBox                   cbMoviePosterFilename8;
  private JCheckBox                   cbActorImages;
  private JTextPane                   tpFileNamingHint;
  private JCheckBox                   chckbxEnableExtrathumbs;
  private JCheckBox                   chckbxEnableExtrafanart;
  private JCheckBox                   chckbxResizeExtrathumbsTo;
  private JSpinner                    spExtrathumbWidth;
  private JLabel                      lblDownload;
  private JSpinner                    spDownloadCountExtrathumbs;
  private JLabel                      lblDownloadCount;
  private JSpinner                    spDownloadCountExtrafanart;
  private JCheckBox                   chckbxStoreMoviesetArtwork;
  private JTextField                  tfMovieSetArtworkFolder;
  private JLabel                      lblFoldername;
  private JButton                     btnSelectFolder;
  private JCheckBox                   cbMovieFanartFilename3;
  private JCheckBox                   chckbxBanner;
  private JCheckBox                   chckbxLogo;
  private JCheckBox                   chckbxThumb;
  private JCheckBox                   chckbxDiscArt;
  private JCheckBox                   chckbxClearArt;
  private JCheckBox                   chckbxMovieSetArtwork;
  private JTable                      tableScraper;
  private JTextPane                   tpScraperDescription;
  private JPanel                      panelScraperOptions;

  /**
   * Instantiates a new movie image settings panel.
   */
  public MovieImageSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();

    // data init
    List<String> enabledArtworkProviders = settings.getMovieArtworkScrapers();
    int selectedIndex = -1;
    int counter = 0;
    for (MediaScraper scraper : MovieList.getInstance().getAvailableArtworkScrapers()) {
      ArtworkScraper artworkScraper = new ArtworkScraper(scraper);
      if (enabledArtworkProviders.contains(artworkScraper.getScraperId())) {
        artworkScraper.active = true;
        if (selectedIndex < 0) {
          selectedIndex = counter;
        }
      }
      scrapers.add(artworkScraper);
      counter++;
    }

    // adjust table columns
    // Checkbox and Logo shall have minimal width
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableScraper, 5);

    tableScraper.getModel().addTableModelListener(arg0 -> {
      // click on the checkbox
      if (arg0.getColumn() == 0) {
        int row = arg0.getFirstRow();
        ArtworkScraper changedScraper = scrapers.get(row);
        if (changedScraper.active) {
          settings.addMovieArtworkScraper(changedScraper.getScraperId());
        }
        else {
          settings.removeMovieArtworkScraper(changedScraper.getScraperId());
        }
      }
    });

    // implement selection listener to load settings
    tableScraper.getSelectionModel().addListSelectionListener(e -> {
      int index = tableScraper.convertRowIndexToModel(tableScraper.getSelectedRow());
      if (index > -1) {
        panelScraperOptions.removeAll();
        if (scrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
          panelScraperOptions.add(new MediaScraperConfigurationPanel(scrapers.get(index).getMediaProvider()));
        }
        panelScraperOptions.revalidate();
      }
    });

    // listen to changes of the checkboxes
    ItemListener listener = e -> checkChanges();
    cbMovieFanartFilename2.addItemListener(listener);
    cbMovieFanartFilename3.addItemListener(listener);

    cbMovieFanartFilename1.addItemListener(listener);
    cbMoviePosterFilename2.addItemListener(listener);
    cbMoviePosterFilename4.addItemListener(listener);
    cbMoviePosterFilename7.addItemListener(listener);
    cbMoviePosterFilename8.addItemListener(listener);
    cbMoviePosterFilename6.addItemListener(listener);

    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    Color color = UIManager.getColor("Label.foreground");
    String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: rgb(" + color.getRed() + ","
        + color.getGreen() + "," + color.getBlue() + "); }";
    ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);

    btnSelectFolder.addActionListener(arg0 -> {
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.movieset.folderchooser")); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        tfMovieSetArtworkFolder.setText(file.toAbsolutePath().toString());
      }
    });

    // poster filenames
    List<MoviePosterNaming> moviePosterFilenames = settings.getMoviePosterFilenames();
    if (moviePosterFilenames.contains(MoviePosterNaming.MOVIE_JPG)) {
      cbMoviePosterFilename2.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.POSTER_JPG)) {
      cbMoviePosterFilename4.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.FOLDER_JPG)) {
      cbMoviePosterFilename6.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.FILENAME_JPG)) {
      cbMoviePosterFilename7.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.FILENAME_POSTER_JPG)) {
      cbMoviePosterFilename8.setSelected(true);
    }

    // fanart filenames
    List<MovieFanartNaming> movieFanartFilenames = settings.getMovieFanartFilenames();
    if (movieFanartFilenames.contains(MovieFanartNaming.FILENAME_FANART_JPG)) {
      cbMovieFanartFilename1.setSelected(true);
    }
    if (movieFanartFilenames.contains(MovieFanartNaming.FANART_JPG)) {
      cbMovieFanartFilename2.setSelected(true);
    }
    if (movieFanartFilenames.contains(MovieFanartNaming.FILENAME_FANART2_JPG)) {
      cbMovieFanartFilename3.setSelected(true);
    }

    // select default artwork scraper
    if (selectedIndex < 0) {
      selectedIndex = 0;
    }
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][][][grow]", "[][200lp][][][20lp][][][20lp][][][]"));
    {
      final JLabel lblScraperT = new JLabel(BUNDLE.getString("scraper.artwork")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperT, 1.16667, Font.BOLD);
      add(lblScraperT, "cell 0 0 4 1");
    }
    {
      JScrollPane scrollPaneScraper = new JScrollPane();
      add(scrollPaneScraper, "cell 1 1 2 1");

      tableScraper = new JTable();
      tableScraper.setRowHeight(29);
      scrollPaneScraper.setViewportView(tableScraper);
    }
    {
      JScrollPane scrollPaneScraperDetails = new JScrollPane();
      add(scrollPaneScraperDetails, "cell 3 1 1 3,grow");
      scrollPaneScraperDetails.setBorder(null);
      scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      JPanel panelScraperDetails = new ScrollablePanel();
      scrollPaneScraperDetails.setViewportView(panelScraperDetails);
      panelScraperDetails.setLayout(new MigLayout("", "[grow]", "[][]"));

      tpScraperDescription = new JTextPane();
      tpScraperDescription.setOpaque(false);
      tpScraperDescription.setEditable(false);
      panelScraperDetails.add(tpScraperDescription, "cell 0 0,growx");

      panelScraperOptions = new JPanel();
      panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
      panelScraperDetails.add(panelScraperOptions, "cell 0 1,growx");
    }
    {
      JLabel lblImageTmdbPosterSize = new JLabel(BUNDLE.getString("image.poster.size")); //$NON-NLS-1$
      add(lblImageTmdbPosterSize, "cell 1 2");

      cbImagePosterSize = new JComboBox(PosterSizes.values());
      add(cbImagePosterSize, "cell 2 2");

      JLabel lblImageTmdbFanartSize = new JLabel(BUNDLE.getString("image.fanart.size")); //$NON-NLS-1$
      add(lblImageTmdbFanartSize, "cell 1 3");

      cbImageFanartSize = new JComboBox(FanartSizes.values());
      add(cbImageFanartSize, "cell 2 3");
    }
    {
      final JLabel lblFileNamingT = new JLabel(BUNDLE.getString("Settings.artwork.naming")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblFileNamingT, 1.16667, Font.BOLD);
      add(lblFileNamingT, "cell 0 5 4 1");
    }
    {
      JPanel panelFileNaming = new JPanel();
      add(panelFileNaming, "cell 1 6 3 1");
      panelFileNaming.setLayout(new MigLayout("insets 0", "[][][][][][]", "[][][]"));

      JLabel lblPosterFilename = new JLabel(BUNDLE.getString("image.poster.naming"));//$NON-NLS-1$
      panelFileNaming.add(lblPosterFilename, "cell 0 0,growx,aligny center");

      cbMoviePosterFilename7 = new JCheckBox("<dynamic>.ext");
      panelFileNaming.add(cbMoviePosterFilename7, "cell 1 0,growx,aligny top");

      cbMoviePosterFilename4 = new JCheckBox("poster.ext");
      panelFileNaming.add(cbMoviePosterFilename4, "cell 2 0,growx,aligny top");

      cbMoviePosterFilename2 = new JCheckBox("movie.ext");
      panelFileNaming.add(cbMoviePosterFilename2, "cell 3 0,growx,aligny top");

      cbMoviePosterFilename8 = new JCheckBox("<dynamic>-poster.ext");
      panelFileNaming.add(cbMoviePosterFilename8, "cell 4 0,alignx left,aligny top");

      cbMoviePosterFilename6 = new JCheckBox("folder.ext");
      panelFileNaming.add(cbMoviePosterFilename6, "cell 5 0,alignx left,aligny top");

      JLabel lblFanartFileNaming = new JLabel(BUNDLE.getString("image.fanart.naming"));//$NON-NLS-1$
      panelFileNaming.add(lblFanartFileNaming, "cell 0 1,alignx left,aligny center");

      cbMovieFanartFilename1 = new JCheckBox("<dynamic>-fanart.ext");
      panelFileNaming.add(cbMovieFanartFilename1, "cell 1 1,alignx left,aligny top");

      cbMovieFanartFilename3 = new JCheckBox("<dynamic>.fanart.ext");
      panelFileNaming.add(cbMovieFanartFilename3, "cell 2 1,alignx left,aligny top");

      cbMovieFanartFilename2 = new JCheckBox("fanart.ext");
      panelFileNaming.add(cbMovieFanartFilename2, "cell 3 1,alignx left,aligny top");

      tpFileNamingHint = new JTextPane();
      panelFileNaming.add(tpFileNamingHint, "cell 0 2 6 1,grow");
      tpFileNamingHint.setText(BUNDLE.getString("Settings.naming.info")); //$NON-NLS-1$
      tpFileNamingHint.setOpaque(false);
      tpFileNamingHint.setEditable(false);
      TmmFontHelper.changeFont(tpFileNamingHint, 0.833);
    }
    {
      final JLabel lblExtraArtworkT = new JLabel(BUNDLE.getString("Settings.extraartwork"));//$NON-NLS-1$
      TmmFontHelper.changeFont(lblExtraArtworkT, 1.16667, Font.BOLD);
      add(lblExtraArtworkT, "cell 0 8 4 1");
    }
    {
      chckbxBanner = new JCheckBox(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
      add(chckbxBanner, "flowx,cell 1 9 3 1");

      chckbxClearArt = new JCheckBox(BUNDLE.getString("mediafiletype.clearart")); //$NON-NLS-1$
      add(chckbxClearArt, "cell 1 9");

      chckbxThumb = new JCheckBox(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
      add(chckbxThumb, "cell 1 9");

      chckbxLogo = new JCheckBox(BUNDLE.getString("mediafiletype.logo")); //$NON-NLS-1$
      add(chckbxLogo, "cell 1 9");

      chckbxDiscArt = new JCheckBox(BUNDLE.getString("mediafiletype.discart")); //$NON-NLS-1$
      add(chckbxDiscArt, "cell 1 9");
    }
    {
      JPanel panelExtraArtwork = new JPanel();
      add(panelExtraArtwork, "cell 1 10 3 1,grow");
      panelExtraArtwork.setLayout(new MigLayout("insets 0", "[][][][][]", "[][][][][15lp][][15lp][][][]"));

      chckbxEnableExtrathumbs = new JCheckBox(BUNDLE.getString("Settings.enable.extrathumbs"));//$NON-NLS-1$
      panelExtraArtwork.add(chckbxEnableExtrathumbs, "cell 0 0 2 1");

      chckbxResizeExtrathumbsTo = new JCheckBox(BUNDLE.getString("Settings.resize.extrathumbs"));//$NON-NLS-1$
      panelExtraArtwork.add(chckbxResizeExtrathumbsTo, "cell 2 0 2 1");

      spExtrathumbWidth = new JSpinner();
      panelExtraArtwork.add(spExtrathumbWidth, "cell 4 0,growx");

      lblDownload = new JLabel(BUNDLE.getString("Settings.amount.autodownload"));//$NON-NLS-1$
      panelExtraArtwork.add(lblDownload, "cell 2 1 2 1,alignx right");

      spDownloadCountExtrathumbs = new JSpinner();
      panelExtraArtwork.add(spDownloadCountExtrathumbs, "cell 4 1,growx");

      chckbxEnableExtrafanart = new JCheckBox(BUNDLE.getString("Settings.enable.extrafanart"));//$NON-NLS-1$
      panelExtraArtwork.add(chckbxEnableExtrafanart, "cell 0 2 5 1");

      lblDownloadCount = new JLabel(BUNDLE.getString("Settings.amount.autodownload"));//$NON-NLS-1$
      panelExtraArtwork.add(lblDownloadCount, "cell 2 3 2 1,alignx right");

      spDownloadCountExtrafanart = new JSpinner();
      panelExtraArtwork.add(spDownloadCountExtrafanart, "cell 4 3,growx");

      cbActorImages = new JCheckBox(BUNDLE.getString("Settings.actor.download"));//$NON-NLS-1$
      panelExtraArtwork.add(cbActorImages, "cell 0 5 5 1,growx");

      chckbxMovieSetArtwork = new JCheckBox(BUNDLE.getString("Settings.movieset.store.movie")); //$NON-NLS-1$
      panelExtraArtwork.add(chckbxMovieSetArtwork, "cell 0 7 5 1,growx");

      chckbxStoreMoviesetArtwork = new JCheckBox(BUNDLE.getString("Settings.movieset.store")); //$NON-NLS-1$
      panelExtraArtwork.add(chckbxStoreMoviesetArtwork, "cell 0 8 5 1,growx");

      lblFoldername = new JLabel(BUNDLE.getString("Settings.movieset.foldername")); //$NON-NLS-1$
      panelExtraArtwork.add(lblFoldername, "cell 1 9,alignx right");

      tfMovieSetArtworkFolder = new JTextField();
      panelExtraArtwork.add(tfMovieSetArtworkFolder, "cell 2 9 2 1,growx");
      tfMovieSetArtworkFolder.setColumns(10);

      btnSelectFolder = new JButton(BUNDLE.getString("Settings.movieset.buttonselect")); //$NON-NLS-1$
      panelExtraArtwork.add(btnSelectFolder, "cell 4 9");
    }
  }

  /**
   * Check changes.
   */
  public void checkChanges() {
    // set poster filenames
    settings.clearMoviePosterFilenames();

    if (cbMoviePosterFilename2.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_PNG);
    }
    if (cbMoviePosterFilename4.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.POSTER_PNG);
    }
    if (cbMoviePosterFilename6.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FOLDER_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.FOLDER_PNG);
    }
    if (cbMoviePosterFilename7.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_PNG);
    }
    if (cbMoviePosterFilename8.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_POSTER_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_POSTER_PNG);
    }

    // set fanart filenames
    settings.clearMovieFanartFilenames();
    if (cbMovieFanartFilename1.isSelected()) {
      settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART_JPG);
      settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART_PNG);
    }
    if (cbMovieFanartFilename2.isSelected()) {
      settings.addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
      settings.addMovieFanartFilename(MovieFanartNaming.FANART_PNG);
    }
    if (cbMovieFanartFilename3.isSelected()) {
      settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART2_JPG);
      settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART2_PNG);
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public class ArtworkScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      active;

    public ArtworkScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
      }
    }

    private ImageIcon getScaledIcon(ImageIcon original) {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(getFont());

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

  protected void initDataBindings() {
    BeanProperty<MovieSettings, PosterSizes> settingsBeanProperty_5 = BeanProperty.create("imagePosterSize");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, PosterSizes, JComboBox, Object> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, cbImagePosterSize, jComboBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSettings, FanartSizes> settingsBeanProperty_6 = BeanProperty.create("imageFanartSize");
    AutoBinding<MovieSettings, FanartSizes, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, cbImageFanartSize, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_2 = BeanProperty.create("writeActorImages");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, cbActorImages, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_3 = BeanProperty.create("imageExtraFanart");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, chckbxEnableExtrafanart, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_4 = BeanProperty.create("imageExtraThumbs");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxEnableExtrathumbs, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MovieSettings, Integer> settingsBeanProperty_8 = BeanProperty.create("imageExtraThumbsSize");
    BeanProperty<JSpinner, Object> jSpinnerBeanProperty_1 = BeanProperty.create("value");
    AutoBinding<MovieSettings, Integer, JSpinner, Object> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, spExtrathumbWidth, jSpinnerBeanProperty_1);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_9 = BeanProperty.create("imageExtraThumbsResize");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxResizeExtrathumbsTo, jCheckBoxBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<MovieSettings, Integer> settingsBeanProperty_10 = BeanProperty.create("imageExtraThumbsCount");
    AutoBinding<MovieSettings, Integer, JSpinner, Object> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, spDownloadCountExtrathumbs, jSpinnerBeanProperty_1);
    autoBinding_12.bind();
    //
    BeanProperty<MovieSettings, Integer> settingsBeanProperty_11 = BeanProperty.create("imageExtraFanartCount");
    AutoBinding<MovieSettings, Integer, JSpinner, Object> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, spDownloadCountExtrafanart, jSpinnerBeanProperty_1);
    autoBinding_13.bind();
    //
    BeanProperty<JSpinner, Boolean> jSpinnerBeanProperty = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrafanart,
        jCheckBoxBeanProperty, spDownloadCountExtrafanart, jSpinnerBeanProperty);
    autoBinding_14.bind();
    //
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrathumbs,
        jCheckBoxBeanProperty, spDownloadCountExtrathumbs, jSpinnerBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<MovieSettings, String> settingsBeanProperty_12 = BeanProperty.create("movieSetArtworkFolder");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSettings, String, JTextField, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieSetArtworkFolder, jTextFieldBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_13 = BeanProperty.create("enableMovieSetArtworkFolder");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, chckbxStoreMoviesetArtwork, jCheckBoxBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrathumbs,
        jCheckBoxBeanProperty, chckbxResizeExtrathumbsTo, jCheckBoxBeanProperty_1);
    autoBinding_8.bind();
    //
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrathumbs,
        jCheckBoxBeanProperty, spExtrathumbWidth, jSpinnerBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_7 = BeanProperty.create("imageBanner");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, chckbxBanner, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_14 = BeanProperty.create("imageClearart");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_14, chckbxClearArt, jCheckBoxBeanProperty);
    autoBinding_18.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_15 = BeanProperty.create("imageThumb");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_15, chckbxThumb, jCheckBoxBeanProperty);
    autoBinding_19.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_16 = BeanProperty.create("imageLogo");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_16, chckbxLogo, jCheckBoxBeanProperty);
    autoBinding_20.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_17 = BeanProperty.create("imageDiscart");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_21 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_17, chckbxDiscArt, jCheckBoxBeanProperty);
    autoBinding_21.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_18 = BeanProperty.create("enableMovieSetArtworkMovieFolder");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_22 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_18, chckbxMovieSetArtwork, jCheckBoxBeanProperty);
    autoBinding_22.bind();
    //
    JTableBinding<ArtworkScraper, List<ArtworkScraper>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, scrapers,
        tableScraper);
    //
    BeanProperty<ArtworkScraper, Boolean> artworkScraperBeanProperty = BeanProperty.create("active");
    jTableBinding.addColumnBinding(artworkScraperBeanProperty).setColumnName("Active").setColumnClass(Boolean.class);
    //
    BeanProperty<ArtworkScraper, Icon> artworkScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding.addColumnBinding(artworkScraperBeanProperty_1).setColumnName("Logo").setEditable(false).setColumnClass(ImageIcon.class);
    //
    BeanProperty<ArtworkScraper, String> artworkScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding.addColumnBinding(artworkScraperBeanProperty_2).setColumnName("Name").setEditable(false).setColumnClass(String.class);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_23 = Bindings.createAutoBinding(UpdateStrategy.READ, tableScraper, jTableBeanProperty,
        tpScraperDescription, jTextPaneBeanProperty);
    autoBinding_23.bind();
  }
}
