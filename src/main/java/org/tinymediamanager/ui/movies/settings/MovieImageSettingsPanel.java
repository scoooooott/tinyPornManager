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
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.filenaming.MovieBannerNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearlogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieDiscartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieFanartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieLogoNaming;
import org.tinymediamanager.core.movie.filenaming.MoviePosterNaming;
import org.tinymediamanager.core.movie.filenaming.MovieThumbNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.entities.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTable;
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

  private MovieSettings               settings         = MovieModuleManager.SETTINGS;
  private List<ArtworkScraper>        scrapers         = ObservableCollections.observableList(new ArrayList<ArtworkScraper>());

  private JComboBox                   cbImagePosterSize;
  private JComboBox                   cbImageFanartSize;
  private JCheckBox                   chckbxMoviePosterFilename2;
  private JCheckBox                   chckbxMoviePosterFilename4;
  private JCheckBox                   chckbxMoviePosterFilename6;
  private JCheckBox                   chckbxMoviePosterFilename7;
  private JCheckBox                   chckbxMovieFanartFilename1;
  private JCheckBox                   chckbxMovieFanartFilename2;
  private JCheckBox                   chckbxMoviePosterFilename8;
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
  private JCheckBox                   chckbxMovieFanartFilename3;
  private JCheckBox                   chckbxMovieSetArtwork;
  private TmmTable                    tableScraper;
  private JTextPane                   tpScraperDescription;
  private JPanel                      panelScraperOptions;
  private JCheckBox                   chckbxBanner1;
  private JCheckBox                   chckbxBanner2;
  private JCheckBox                   chckbxClearart1;
  private JCheckBox                   chckbxClearart2;
  private JCheckBox                   chckbxThumb1;
  private JCheckBox                   chckbxThumb2;
  private JCheckBox                   chckbxThumb3;
  private JCheckBox                   chckbxThumb4;
  private JCheckBox                   chckbxLogo1;
  private JCheckBox                   chckbxLogo2;
  private JCheckBox                   chckbxClearlogo1;
  private JCheckBox                   chckbxClearlogo2;
  private JCheckBox                   chckbxDiscart1;
  private JCheckBox                   chckbxDiscart2;
  private ItemListener                checkBoxListener;

  /**
   * Instantiates a new movie image settings panel.
   */
  public MovieImageSettingsPanel() {
    checkBoxListener = e -> checkChanges();

    // UI init
    initComponents();
    initDataBindings();

    // data init
    List<String> enabledArtworkProviders = settings.getArtworkScrapers();
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

    // implement selection checkBoxListener to load settings
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

    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    Color color = UIManager.getColor("Label.foreground");
    String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: rgb(" + color.getRed() + ","
        + color.getGreen() + "," + color.getBlue() + "); }";
    tpScraperDescription.setEditorKit(new HTMLEditorKit());
    ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);

    btnSelectFolder.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("movieset.folderchooser.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.movieset.folderchooser"), path); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        tfMovieSetArtworkFolder.setText(file.toAbsolutePath().toString());
      }
    });

    // select default artwork scraper
    if (selectedIndex < 0) {
      selectedIndex = 0;
    }
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }

    // implement checkBoxListener for preset events
    settings.addPropertyChangeListener(evt -> {
      if ("preset".equals(evt.getPropertyName())) {
        buildCheckBoxes();
      }
    });

    buildCheckBoxes();
  }

  private void buildCheckBoxes() {
    // initialize
    chckbxMovieFanartFilename1.removeItemListener(checkBoxListener);
    chckbxMovieFanartFilename2.removeItemListener(checkBoxListener);
    chckbxMovieFanartFilename3.removeItemListener(checkBoxListener);
    clearSelection(chckbxMovieFanartFilename1, chckbxMovieFanartFilename2, chckbxMovieFanartFilename3);

    chckbxMoviePosterFilename2.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename4.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename7.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename8.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename6.removeItemListener(checkBoxListener);
    clearSelection(chckbxMoviePosterFilename2, chckbxMoviePosterFilename4, chckbxMoviePosterFilename6, chckbxMoviePosterFilename7,
        chckbxMoviePosterFilename8);

    chckbxBanner1.removeItemListener(checkBoxListener);
    chckbxBanner2.removeItemListener(checkBoxListener);
    clearSelection(chckbxBanner1, chckbxBanner2);

    chckbxClearart1.removeItemListener(checkBoxListener);
    chckbxClearart2.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearart1, chckbxClearart2);

    chckbxClearlogo1.removeItemListener(checkBoxListener);
    chckbxClearlogo2.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearlogo1, chckbxClearlogo2);

    chckbxLogo1.removeItemListener(checkBoxListener);
    chckbxLogo2.removeItemListener(checkBoxListener);
    clearSelection(chckbxLogo1, chckbxLogo2);

    chckbxThumb1.removeItemListener(checkBoxListener);
    chckbxThumb2.removeItemListener(checkBoxListener);
    chckbxThumb3.removeItemListener(checkBoxListener);
    chckbxThumb4.removeItemListener(checkBoxListener);
    clearSelection(chckbxThumb1, chckbxThumb2, chckbxThumb3, chckbxThumb4);

    chckbxDiscart1.removeItemListener(checkBoxListener);
    chckbxDiscart2.removeItemListener(checkBoxListener);
    clearSelection(chckbxDiscart1, chckbxDiscart2);

    // poster filenames
    for (MoviePosterNaming poster : settings.getPosterFilenames()) {
      switch (poster) {
        case FILENAME:
          chckbxMoviePosterFilename7.setSelected(true);
          break;
        case FILENAME_POSTER:
          chckbxMoviePosterFilename8.setSelected(true);
          break;
        case FOLDER:
          chckbxMoviePosterFilename6.setSelected(true);
          break;
        case MOVIE:
          chckbxMoviePosterFilename2.setSelected(true);
          break;
        case POSTER:
          chckbxMoviePosterFilename4.setSelected(true);
          break;
      }
    }

    // fanart filenames
    for (MovieFanartNaming fanart : settings.getFanartFilenames()) {
      switch (fanart) {
        case FANART:
          chckbxMovieFanartFilename2.setSelected(true);
          break;
        case FILENAME_FANART:
          chckbxMovieFanartFilename1.setSelected(true);
          break;
        case FILENAME_FANART2:
          chckbxMovieFanartFilename3.setSelected(true);
          break;
      }
    }

    // banner filenames
    for (MovieBannerNaming banner : settings.getBannerFilenames()) {
      switch (banner) {
        case BANNER:
          chckbxBanner2.setSelected(true);
          break;
        case FILENAME_BANNER:
          chckbxBanner1.setSelected(true);
          break;
      }
    }

    // clearart filenames
    for (MovieClearartNaming clearart : settings.getClearartFilenames()) {
      switch (clearart) {
        case CLEARART:
          chckbxClearart2.setSelected(true);
          break;
        case FILENAME_CLEARART:
          chckbxClearart1.setSelected(true);
          break;
      }
    }

    // thumb filenames
    for (MovieThumbNaming thumb : settings.getThumbFilenames()) {
      switch (thumb) {
        case THUMB:
          chckbxThumb2.setSelected(true);
          break;
        case FILENAME_THUMB:
          chckbxThumb1.setSelected(true);
          break;
        case LANDSCAPE:
          chckbxThumb4.setSelected(true);
          break;
        case FILENAME_LANDSCAPE:
          chckbxThumb3.setSelected(true);
          break;
      }
    }

    // logo filenames
    for (MovieLogoNaming logo : settings.getLogoFilenames()) {
      switch (logo) {
        case LOGO:
          chckbxLogo2.setSelected(true);
          break;
        case FILENAME_LOGO:
          chckbxLogo1.setSelected(true);
          break;
      }
    }

    // clearlogo filenames
    for (MovieClearlogoNaming clearlogo : settings.getClearlogoFilenames()) {
      switch (clearlogo) {
        case CLEARLOGO:
          chckbxClearlogo2.setSelected(true);
          break;
        case FILENAME_CLEARLOGO:
          chckbxClearlogo1.setSelected(true);
          break;
      }
    }

    // discart filenames
    for (MovieDiscartNaming discart : settings.getDiscartFilenames()) {
      switch (discart) {
        case DISC:
          chckbxDiscart2.setSelected(true);
          break;
        case FILENAME_DISC:
          chckbxDiscart1.setSelected(true);
          break;
      }
    }

    // listen to changes of the checkboxes
    chckbxMovieFanartFilename2.addItemListener(checkBoxListener);
    chckbxMovieFanartFilename3.addItemListener(checkBoxListener);

    chckbxMovieFanartFilename1.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename2.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename4.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename7.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename8.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename6.addItemListener(checkBoxListener);

    chckbxBanner1.addItemListener(checkBoxListener);
    chckbxBanner2.addItemListener(checkBoxListener);

    chckbxClearart1.addItemListener(checkBoxListener);
    chckbxClearart2.addItemListener(checkBoxListener);

    chckbxClearlogo1.addItemListener(checkBoxListener);
    chckbxClearlogo2.addItemListener(checkBoxListener);

    chckbxLogo1.addItemListener(checkBoxListener);
    chckbxLogo2.addItemListener(checkBoxListener);

    chckbxThumb1.addItemListener(checkBoxListener);
    chckbxThumb2.addItemListener(checkBoxListener);
    chckbxThumb3.addItemListener(checkBoxListener);
    chckbxThumb4.addItemListener(checkBoxListener);

    chckbxDiscart1.addItemListener(checkBoxListener);
    chckbxDiscart2.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][][][500lp,grow]", "[][200lp][][][20lp][][][20lp][][][]"));
    {
      final JLabel lblScraperT = new JLabel(BUNDLE.getString("scraper.artwork")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperT, 1.16667, Font.BOLD);
      add(lblScraperT, "cell 0 0 4 1");
    }
    {
      tableScraper = new TmmTable();
      tableScraper.setRowHeight(29);
      JScrollPane scrollPaneScraper = new JScrollPane(tableScraper);
      tableScraper.configureScrollPane(scrollPaneScraper);
      add(scrollPaneScraper, "cell 1 1 2 1,grow");
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
      panelFileNaming.setLayout(new MigLayout("insets 0", "[][][][]", "[][][][]"));

      JLabel lblPosterFilename = new JLabel(BUNDLE.getString("mediafiletype.poster"));//$NON-NLS-1$
      panelFileNaming.add(lblPosterFilename, "cell 0 0");

      chckbxMoviePosterFilename8 = new JCheckBox("<dynamic>-poster.ext");
      panelFileNaming.add(chckbxMoviePosterFilename8, "cell 2 0");

      chckbxMoviePosterFilename2 = new JCheckBox("movie.ext");
      panelFileNaming.add(chckbxMoviePosterFilename2, "cell 3 0");

      chckbxMoviePosterFilename7 = new JCheckBox("<dynamic>.ext");
      panelFileNaming.add(chckbxMoviePosterFilename7, "cell 2 1");

      chckbxMoviePosterFilename4 = new JCheckBox("poster.ext");
      panelFileNaming.add(chckbxMoviePosterFilename4, "cell 1 0");

      chckbxMoviePosterFilename6 = new JCheckBox("folder.ext");
      panelFileNaming.add(chckbxMoviePosterFilename6, "cell 1 1");

      JLabel lblFanartFileNaming = new JLabel(BUNDLE.getString("mediafiletype.fanart"));//$NON-NLS-1$
      panelFileNaming.add(lblFanartFileNaming, "cell 0 2");

      chckbxMovieFanartFilename1 = new JCheckBox("<dynamic>-fanart.ext");
      panelFileNaming.add(chckbxMovieFanartFilename1, "cell 2 2");

      chckbxMovieFanartFilename3 = new JCheckBox("<dynamic>.fanart.ext");
      panelFileNaming.add(chckbxMovieFanartFilename3, "cell 3 2");

      chckbxMovieFanartFilename2 = new JCheckBox("fanart.ext");
      panelFileNaming.add(chckbxMovieFanartFilename2, "cell 1 2");

      tpFileNamingHint = new JTextPane();
      panelFileNaming.add(tpFileNamingHint, "cell 0 3 4 1");
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
      JPanel panelExtraArtwork = new JPanel();
      add(panelExtraArtwork, "cell 1 9 3 1");
      panelExtraArtwork.setLayout(new MigLayout("insets 0", "[][][]", "[][][][][][][][]"));

      JLabel lblBannerNamingT = new JLabel(BUNDLE.getString("mediafiletype.banner"));//$NON-NLS-1$
      panelExtraArtwork.add(lblBannerNamingT, "cell 0 0");

      chckbxBanner2 = new JCheckBox("banner.ext");
      panelExtraArtwork.add(chckbxBanner2, "cell 1 0");

      chckbxBanner1 = new JCheckBox("<dynamic>-banner.ext");
      panelExtraArtwork.add(chckbxBanner1, "cell 2 0");

      JLabel lblClearartNamingT = new JLabel(BUNDLE.getString("mediafiletype.clearart"));//$NON-NLS-1$
      panelExtraArtwork.add(lblClearartNamingT, "cell 0 1");

      chckbxClearart2 = new JCheckBox("clearart.ext");
      panelExtraArtwork.add(chckbxClearart2, "cell 1 1");

      chckbxClearart1 = new JCheckBox("<dynamic>-clearart.ext");
      panelExtraArtwork.add(chckbxClearart1, "cell 2 1");

      JLabel lblThumbNamingT = new JLabel(BUNDLE.getString("mediafiletype.thumb"));//$NON-NLS-1$
      panelExtraArtwork.add(lblThumbNamingT, "cell 0 2");

      chckbxThumb2 = new JCheckBox("thumb.ext");
      panelExtraArtwork.add(chckbxThumb2, "cell 1 2");

      chckbxThumb1 = new JCheckBox("<dynamic>-thumb.ext");
      panelExtraArtwork.add(chckbxThumb1, "cell 2 2");

      chckbxThumb4 = new JCheckBox("landscape.ext");
      panelExtraArtwork.add(chckbxThumb4, "cell 1 3");

      chckbxThumb3 = new JCheckBox("<dynamic>-landscape.ext");
      panelExtraArtwork.add(chckbxThumb3, "cell 2 3");

      JLabel lblLogoNamingT = new JLabel(BUNDLE.getString("mediafiletype.logo"));//$NON-NLS-1$
      panelExtraArtwork.add(lblLogoNamingT, "cell 0 4");

      chckbxLogo2 = new JCheckBox("logo.ext");
      panelExtraArtwork.add(chckbxLogo2, "cell 1 4");

      chckbxLogo1 = new JCheckBox("<dynamic>-logo.ext");
      panelExtraArtwork.add(chckbxLogo1, "cell 2 4");

      JLabel lblClearlogoNamingT = new JLabel(BUNDLE.getString("mediafiletype.clearlogo"));//$NON-NLS-1$
      panelExtraArtwork.add(lblClearlogoNamingT, "cell 0 5");

      chckbxClearlogo2 = new JCheckBox("clearlogo.ext");
      panelExtraArtwork.add(chckbxClearlogo2, "cell 1 5");

      chckbxClearlogo1 = new JCheckBox("<dynamic>-clearlogo.ext");
      panelExtraArtwork.add(chckbxClearlogo1, "cell 2 5");

      JLabel lblDiscartNamingT = new JLabel(BUNDLE.getString("mediafiletype.discart"));//$NON-NLS-1$
      panelExtraArtwork.add(lblDiscartNamingT, "cell 0 6");

      chckbxDiscart2 = new JCheckBox("disc.ext");
      panelExtraArtwork.add(chckbxDiscart2, "cell 1 6");

      chckbxDiscart1 = new JCheckBox("<dynamic>-disc.ext");
      panelExtraArtwork.add(chckbxDiscart1, "cell 2 6");

      JTextPane tpFileNamingHint = new JTextPane();
      tpFileNamingHint.setText(BUNDLE.getString("Settings.naming.info")); //$NON-NLS-1$
      tpFileNamingHint.setOpaque(false);
      tpFileNamingHint.setEditable(false);
      TmmFontHelper.changeFont(tpFileNamingHint, 0.833);
      panelExtraArtwork.add(tpFileNamingHint, "cell 0 7 3 1");
    }
    {
      JPanel panelExtraArtwork = new JPanel();
      add(panelExtraArtwork, "cell 1 10 3 1");
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
    settings.clearPosterFilenames();

    if (chckbxMoviePosterFilename2.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.MOVIE);
    }
    if (chckbxMoviePosterFilename4.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.POSTER);
    }
    if (chckbxMoviePosterFilename6.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.FOLDER);
    }
    if (chckbxMoviePosterFilename7.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.FILENAME);
    }
    if (chckbxMoviePosterFilename8.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.FILENAME_POSTER);
    }

    // set fanart filenames
    settings.clearFanartFilenames();
    if (chckbxMovieFanartFilename1.isSelected()) {
      settings.addFanartFilename(MovieFanartNaming.FILENAME_FANART);
    }
    if (chckbxMovieFanartFilename2.isSelected()) {
      settings.addFanartFilename(MovieFanartNaming.FANART);
    }
    if (chckbxMovieFanartFilename3.isSelected()) {
      settings.addFanartFilename(MovieFanartNaming.FILENAME_FANART2);
    }

    // set banner filenames
    settings.clearBannerFilenames();
    if (chckbxBanner1.isSelected()) {
      settings.addBannerFilename(MovieBannerNaming.FILENAME_BANNER);
    }
    if (chckbxBanner2.isSelected()) {
      settings.addBannerFilename(MovieBannerNaming.BANNER);
    }

    // set clearart filenames
    settings.clearClearartFilenames();
    if (chckbxClearart1.isSelected()) {
      settings.addClearartFilename(MovieClearartNaming.FILENAME_CLEARART);
    }
    if (chckbxClearart2.isSelected()) {
      settings.addClearartFilename(MovieClearartNaming.CLEARART);
    }

    // set thumb filenames
    settings.clearThumbFilenames();
    if (chckbxThumb1.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.FILENAME_THUMB);
    }
    if (chckbxThumb2.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.THUMB);
    }
    if (chckbxThumb3.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.FILENAME_LANDSCAPE);
    }
    if (chckbxThumb4.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.LANDSCAPE);
    }

    // set logo filenames
    settings.clearLogoFilenames();
    if (chckbxLogo1.isSelected()) {
      settings.addLogoFilename(MovieLogoNaming.FILENAME_LOGO);
    }
    if (chckbxLogo2.isSelected()) {
      settings.addLogoFilename(MovieLogoNaming.LOGO);
    }

    // set clearlogo filenames
    settings.clearClearlogoFilenames();
    if (chckbxClearlogo1.isSelected()) {
      settings.addClearlogoFilename(MovieClearlogoNaming.FILENAME_CLEARLOGO);
    }
    if (chckbxClearlogo2.isSelected()) {
      settings.addClearlogoFilename(MovieClearlogoNaming.CLEARLOGO);
    }

    // set discart filenames
    settings.clearDiscartFilenames();
    if (chckbxDiscart1.isSelected()) {
      settings.addDiscartFilename(MovieDiscartNaming.FILENAME_DISC);
    }
    if (chckbxDiscart2.isSelected()) {
      settings.addDiscartFilename(MovieDiscartNaming.DISC);
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
