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
package org.tinymediamanager.ui.tvshows.settings;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.filenaming.TvShowBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearlogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowFanartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowLogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowThumbNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowImageSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowImageSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 4999827736720726395L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private List<ArtworkScraper>        artworkScrapers  = ObservableCollections.observableList(new ArrayList<ArtworkScraper>());

  private TmmTable                    tableArtworkScraper;
  private JTextPane                   tpArtworkScraperDescription;
  private JPanel                      panelArtworkScraperOptions;
  private JCheckBox                   chckbxEpisodeThumb1;
  private JCheckBox                   chckbxEpisodeThumb2;
  private JCheckBox                   chckbxEpisodeThumb3;
  private JCheckBox                   chckbxEpisodeThumb4;

  private ItemListener                checkBoxListener;
  private JCheckBox                   chckbxPoster1;
  private JCheckBox                   chckbxPoster2;
  private JCheckBox                   chckbxFanart1;
  private JCheckBox                   chckbxBanner1;
  private JCheckBox                   chckbxClearart1;
  private JCheckBox                   chckbxThumb1;
  private JCheckBox                   chckbxThumb2;
  private JCheckBox                   chckbxLogo1;
  private JCheckBox                   chckbxClearlogo1;
  private JCheckBox                   chckbxSeasonPoster1;
  private JCheckBox                   chckbxSeasonPoster2;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public TvShowImageSettingsPanel() {
    checkBoxListener = e -> checkChanges();

    // UI init
    initComponents();
    initDataBindings();

    // data init
    List<String> enabledArtworkProviders = settings.getArtworkScrapers();
    int selectedIndex = -1;
    int counter = 0;
    for (MediaScraper scraper : TvShowList.getInstance().getAvailableArtworkScrapers()) {
      ArtworkScraper artworkScraper = new ArtworkScraper(scraper);
      if (enabledArtworkProviders.contains(artworkScraper.getScraperId())) {
        artworkScraper.active = true;
        if (selectedIndex < 0) {
          selectedIndex = counter;
        }
      }
      artworkScrapers.add(artworkScraper);
      counter++;
    }

    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    Color color = UIManager.getColor("Label.foreground");
    String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: rgb(" + color.getRed() + ","
        + color.getGreen() + "," + color.getBlue() + "); }";
    tpArtworkScraperDescription.setEditorKit(new HTMLEditorKit());
    ((HTMLDocument) tpArtworkScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);

    TableColumnResizer.setMaxWidthForColumn(tableArtworkScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableArtworkScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableArtworkScraper, 5);

    tableArtworkScraper.getModel().addTableModelListener(arg0 -> {
      // click on the checkbox
      if (arg0.getColumn() == 0) {
        int row = arg0.getFirstRow();
        ArtworkScraper changedScraper = artworkScrapers.get(row);
        if (changedScraper.active) {
          settings.addTvShowArtworkScraper(changedScraper.getScraperId());
        }
        else {
          settings.removeTvShowArtworkScraper(changedScraper.getScraperId());
        }
      }
    });
    // implement selection listener to load settings
    tableArtworkScraper.getSelectionModel().addListSelectionListener(e -> {
      int index = tableArtworkScraper.convertRowIndexToModel(tableArtworkScraper.getSelectedRow());
      if (index > -1) {
        panelArtworkScraperOptions.removeAll();
        if (artworkScrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
          panelArtworkScraperOptions.add(new MediaScraperConfigurationPanel(artworkScrapers.get(index).getMediaProvider()));
        }
        panelArtworkScraperOptions.revalidate();
      }
    });

    // select default artwork scraper
    if (counter > 0) {
      tableArtworkScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }

    buildCheckBoxes();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][grow][][500lp,grow]", "[][200lp][20lp][][][][][]"));
    {
      final JLabel lblScraperT = new JLabel(BUNDLE.getString("scraper.artwork")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperT, 1.16667, Font.BOLD);
      add(lblScraperT, "cell 0 0 6 1");
    }
    {
      tableArtworkScraper = new TmmTable();
      tableArtworkScraper.setRowHeight(29);

      JScrollPane scrollPaneArtworkScraper = new JScrollPane(tableArtworkScraper);
      tableArtworkScraper.configureScrollPane(scrollPaneArtworkScraper);
      add(scrollPaneArtworkScraper, "cell 1 1 2 1,grow");
    }
    {
      JScrollPane scrollPaneScraperDetails = new JScrollPane();
      add(scrollPaneScraperDetails, "cell 3 1 1 3,grow");
      scrollPaneScraperDetails.setBorder(null);
      scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      JPanel panelScraperDetails = new ScrollablePanel();
      scrollPaneScraperDetails.setViewportView(panelScraperDetails);
      panelScraperDetails.setLayout(new MigLayout("", "[grow]", "[][]"));

      tpArtworkScraperDescription = new JTextPane();
      tpArtworkScraperDescription.setOpaque(false);
      tpArtworkScraperDescription.setEditable(false);
      panelScraperDetails.add(tpArtworkScraperDescription, "cell 0 0,growx");

      panelArtworkScraperOptions = new JPanel();
      panelArtworkScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
      panelScraperDetails.add(panelArtworkScraperOptions, "cell 0 1,growx");
    }
    {
      JLabel lblExtraArtworkT = new JLabel(BUNDLE.getString("Settings.artwork.naming")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblExtraArtworkT, 1.16667, Font.BOLD);
      add(lblExtraArtworkT, "cell 0 3 3 1");
    }
    {
      JPanel panel = new JPanel();
      add(panel, "cell 1 4 3 1,grow");
      panel.setLayout(new MigLayout("", "[][][grow]", "[][][][][][][][][][][grow]"));
      {
        JLabel lblPosterT = new JLabel(BUNDLE.getString("mediafiletype.poster"));//$NON-NLS-1$
        panel.add(lblPosterT, "cell 0 0");

        chckbxPoster1 = new JCheckBox("poster.ext");
        panel.add(chckbxPoster1, "cell 1 0");

        chckbxPoster2 = new JCheckBox("folder.ext");
        panel.add(chckbxPoster2, "cell 2 0");
      }
      {
        JLabel lblFanartT = new JLabel(BUNDLE.getString("mediafiletype.fanart"));//$NON-NLS-1$
        panel.add(lblFanartT, "cell 0 1");

        chckbxFanart1 = new JCheckBox("fanart.ext");
        panel.add(chckbxFanart1, "cell 1 1");
      }
      {
        JLabel lblBannerT = new JLabel(BUNDLE.getString("mediafiletype.banner"));//$NON-NLS-1$
        panel.add(lblBannerT, "cell 0 2");

        chckbxBanner1 = new JCheckBox("banner.ext");
        panel.add(chckbxBanner1, "cell 1 2");
      }
      {
        JLabel lblClearartT = new JLabel(BUNDLE.getString("mediafiletype.clearart"));//$NON-NLS-1$
        panel.add(lblClearartT, "cell 0 3");

        chckbxClearart1 = new JCheckBox("clearart.ext");
        panel.add(chckbxClearart1, "cell 1 3");
      }
      {
        JLabel lblThumbT = new JLabel(BUNDLE.getString("mediafiletype.thumb"));//$NON-NLS-1$
        panel.add(lblThumbT, "cell 0 4");

        chckbxThumb1 = new JCheckBox("thumb.ext");
        panel.add(chckbxThumb1, "cell 1 4");

        chckbxThumb2 = new JCheckBox("landscape.ext");
        panel.add(chckbxThumb2, "cell 2 4");
      }
      {
        JLabel lblLogoT = new JLabel(BUNDLE.getString("mediafiletype.logo"));//$NON-NLS-1$
        panel.add(lblLogoT, "cell 0 5");

        chckbxLogo1 = new JCheckBox("logo.ext");
        panel.add(chckbxLogo1, "cell 1 5");
      }
      {
        JLabel lblClearlogoT = new JLabel(BUNDLE.getString("mediafiletype.clearlogo"));//$NON-NLS-1$
        panel.add(lblClearlogoT, "cell 0 6");

        chckbxClearlogo1 = new JCheckBox("clearlogo.ext");
        panel.add(chckbxClearlogo1, "cell 1 6");
      }
      {
        JLabel lblSeasonPosterT = new JLabel(BUNDLE.getString("mediafiletype.season_poster"));//$NON-NLS-1$
        panel.add(lblSeasonPosterT, "cell 0 7");

        chckbxSeasonPoster1 = new JCheckBox("seasonXX-poster.ext");
        panel.add(chckbxSeasonPoster1, "cell 1 7");

        chckbxSeasonPoster2 = new JCheckBox("<season_folder>" + File.separator + "seasonXX.ext");
        panel.add(chckbxSeasonPoster2, "cell 2 7");
      }
      {
        JLabel lblThumbNaming = new JLabel(BUNDLE.getString("mediafiletype.episode_thumb"));
        panel.add(lblThumbNaming, "cell 0 8");

        chckbxEpisodeThumb1 = new JCheckBox("<dynamic>-thumb.ext");
        panel.add(chckbxEpisodeThumb1, "cell 1 8");

        chckbxEpisodeThumb2 = new JCheckBox("<dynamic>-landscape.ext");
        panel.add(chckbxEpisodeThumb2, "cell 2 8");

        chckbxEpisodeThumb3 = new JCheckBox("<dynamic>.ext");
        panel.add(chckbxEpisodeThumb3, "cell 1 9");

        chckbxEpisodeThumb4 = new JCheckBox("<dynamic>.tbn");
        panel.add(chckbxEpisodeThumb4, "cell 2 9");
      }
      {
        JTextPane tpFileNamingHint = new JTextPane();
        tpFileNamingHint.setText(BUNDLE.getString("Settings.naming.info")); //$NON-NLS-1$
        tpFileNamingHint.setOpaque(false);
        tpFileNamingHint.setEditable(false);
        TmmFontHelper.changeFont(tpFileNamingHint, 0.833);
        panel.add(tpFileNamingHint, "cell 0 10 3 1,grow");
      }
    }
  }

  private void buildCheckBoxes() {
    chckbxPoster1.removeItemListener(checkBoxListener);
    chckbxPoster2.removeItemListener(checkBoxListener);
    clearSelection(chckbxPoster1, chckbxPoster2);

    chckbxFanart1.removeItemListener(checkBoxListener);
    clearSelection(chckbxFanart1);

    chckbxBanner1.removeItemListener(checkBoxListener);
    clearSelection(chckbxBanner1);

    chckbxClearart1.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearart1);

    chckbxThumb1.removeItemListener(checkBoxListener);
    chckbxThumb2.removeItemListener(checkBoxListener);
    clearSelection(chckbxThumb1, chckbxThumb2);

    chckbxLogo1.removeItemListener(checkBoxListener);
    clearSelection(chckbxLogo1);

    chckbxClearlogo1.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearlogo1);

    chckbxSeasonPoster1.removeItemListener(checkBoxListener);
    chckbxSeasonPoster2.removeItemListener(checkBoxListener);
    clearSelection(chckbxSeasonPoster1, chckbxSeasonPoster2);

    chckbxEpisodeThumb1.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb2.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb3.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb4.removeItemListener(checkBoxListener);
    clearSelection(chckbxEpisodeThumb1, chckbxEpisodeThumb2, chckbxEpisodeThumb3, chckbxEpisodeThumb4);

    for (TvShowPosterNaming posterNaming : settings.getPosterFilenames()) {
      switch (posterNaming) {
        case POSTER:
          chckbxPoster1.setSelected(true);
          break;

        case FOLDER:
          chckbxPoster2.setSelected(true);
          break;
      }
    }

    for (TvShowFanartNaming fanartNaming : settings.getFanartFilenames()) {
      switch (fanartNaming) {
        case FANART:
          chckbxFanart1.setSelected(true);
          break;
      }
    }

    for (TvShowBannerNaming bannerNaming : settings.getBannerFilenames()) {
      switch (bannerNaming) {
        case BANNER:
          chckbxBanner1.setSelected(true);
          break;
      }
    }

    for (TvShowClearartNaming clearartNaming : settings.getClearartFilenames()) {
      switch (clearartNaming) {
        case CLEARART:
          chckbxClearart1.setSelected(true);
          break;
      }
    }

    for (TvShowThumbNaming thumbNaming : settings.getThumbFilenames()) {
      switch (thumbNaming) {
        case THUMB:
          chckbxThumb1.setSelected(true);
          break;

        case LANDSCAPE:
          chckbxThumb2.setSelected(true);
          break;
      }
    }

    for (TvShowLogoNaming logoNaming : settings.getLogoFilenames()) {
      switch (logoNaming) {
        case LOGO:
          chckbxLogo1.setSelected(true);
          break;
      }
    }

    for (TvShowClearlogoNaming clearlogoNaming : settings.getClearlogoFilenames()) {
      switch (clearlogoNaming) {
        case CLEARLOGO:
          chckbxClearlogo1.setSelected(true);
          break;
      }
    }

    for (TvShowSeasonPosterNaming seasonPosterNaming : settings.getSeasonPosterFilenames()) {
      switch (seasonPosterNaming) {
        case SEASON_POSTER:
          chckbxSeasonPoster1.setSelected(true);
          break;

        case SEASON_FOLDER:
          chckbxSeasonPoster2.setSelected(true);
          break;
      }
    }

    for (TvShowEpisodeThumbNaming thumbNaming : settings.getEpisodeThumbFilenames()) {
      switch (thumbNaming) {
        case FILENAME_THUMB:
          chckbxEpisodeThumb1.setSelected(true);
          break;

        case FILENAME_LANDSCAPE:
          chckbxEpisodeThumb2.setSelected(true);
          break;

        case FILENAME:
          chckbxEpisodeThumb3.setSelected(true);
          break;

        case FILENAME_TBN:
          chckbxEpisodeThumb4.setSelected(true);
          break;
      }
    }

    chckbxPoster1.addItemListener(checkBoxListener);
    chckbxPoster2.addItemListener(checkBoxListener);

    chckbxFanart1.addItemListener(checkBoxListener);

    chckbxBanner1.addItemListener(checkBoxListener);

    chckbxClearart1.addItemListener(checkBoxListener);

    chckbxThumb1.addItemListener(checkBoxListener);
    chckbxThumb2.addItemListener(checkBoxListener);

    chckbxLogo1.addItemListener(checkBoxListener);

    chckbxClearlogo1.addItemListener(checkBoxListener);

    chckbxSeasonPoster1.addItemListener(checkBoxListener);
    chckbxSeasonPoster2.addItemListener(checkBoxListener);

    chckbxEpisodeThumb1.addItemListener(checkBoxListener);
    chckbxEpisodeThumb2.addItemListener(checkBoxListener);
    chckbxEpisodeThumb3.addItemListener(checkBoxListener);
    chckbxEpisodeThumb4.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
    }
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    settings.clearPosterFilenames();
    if (chckbxPoster1.isSelected()) {
      settings.addPosterFilename(TvShowPosterNaming.POSTER);
    }
    if (chckbxPoster2.isSelected()) {
      settings.addPosterFilename(TvShowPosterNaming.FOLDER);
    }

    settings.clearFanartFilenames();
    if (chckbxFanart1.isSelected()) {
      settings.addFanartFilename(TvShowFanartNaming.FANART);
    }

    settings.clearBannerFilenames();
    if (chckbxBanner1.isSelected()) {
      settings.addBannerFilename(TvShowBannerNaming.BANNER);
    }

    settings.clearClearartFilenames();
    if (chckbxClearart1.isSelected()) {
      settings.addClearartFilename(TvShowClearartNaming.CLEARART);
    }

    settings.clearThumbFilenames();
    if (chckbxThumb1.isSelected()) {
      settings.addThumbFilename(TvShowThumbNaming.THUMB);
    }
    if (chckbxThumb2.isSelected()) {
      settings.addThumbFilename(TvShowThumbNaming.LANDSCAPE);
    }

    settings.clearLogoFilenames();
    if (chckbxLogo1.isSelected()) {
      settings.addLogoFilename(TvShowLogoNaming.LOGO);
    }

    settings.clearClearlogoFilenames();
    if (chckbxClearlogo1.isSelected()) {
      settings.addClearlogoFilename(TvShowClearlogoNaming.CLEARLOGO);
    }

    settings.clearSeasonPosterFilenames();
    if (chckbxSeasonPoster1.isSelected()) {
      settings.addSeasonPosterFilename(TvShowSeasonPosterNaming.SEASON_POSTER);
    }
    if (chckbxSeasonPoster2.isSelected()) {
      settings.addSeasonPosterFilename(TvShowSeasonPosterNaming.SEASON_FOLDER);
    }

    settings.clearEpisodeThumbFilenames();
    if (chckbxEpisodeThumb1.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_THUMB);
    }
    if (chckbxEpisodeThumb2.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_LANDSCAPE);
    }
    if (chckbxEpisodeThumb3.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME);
    }
    if (chckbxEpisodeThumb4.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_TBN);
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
    JTableBinding<ArtworkScraper, List<ArtworkScraper>, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE,
        artworkScrapers, tableArtworkScraper);
    //
    BeanProperty<ArtworkScraper, Boolean> artworkScraperBeanProperty = BeanProperty.create("active");
    jTableBinding_1.addColumnBinding(artworkScraperBeanProperty).setColumnName("Active").setColumnClass(Boolean.class);
    //
    BeanProperty<ArtworkScraper, Icon> artworkScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding_1.addColumnBinding(artworkScraperBeanProperty_1).setColumnName("Logo").setEditable(false).setColumnClass(ImageIcon.class);
    //
    BeanProperty<ArtworkScraper, String> artworkScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding_1.addColumnBinding(artworkScraperBeanProperty_2).setColumnName("Name").setEditable(false).setColumnClass(String.class);
    //
    jTableBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tableArtworkScraper,
        jTableBeanProperty, tpArtworkScraperDescription, jTextPaneBeanProperty_1);
    autoBinding_1.bind();
  }
}
