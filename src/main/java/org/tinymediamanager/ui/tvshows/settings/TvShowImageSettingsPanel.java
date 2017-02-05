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
package org.tinymediamanager.ui.tvshows.settings;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
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
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
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

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public TvShowImageSettingsPanel() {
    checkBoxListener = e -> checkChanges();

    // UI init
    initComponents();
    initDataBindings();

    // data init
    List<String> enabledArtworkProviders = settings.getTvShowArtworkScrapers();
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
      JLabel lblExtraArtworkT = new JLabel(BUNDLE.getString("Settings.extraartwork"));//$NON-NLS-1$
      TmmFontHelper.changeFont(lblExtraArtworkT, 1.16667, Font.BOLD);
      add(lblExtraArtworkT, "cell 0 3 3 1");
    }
    {
      JPanel panel = new JPanel();
      add(panel, "cell 1 4 3 1,grow");
      panel.setLayout(new MigLayout("", "[][][]", "[][]"));
      JLabel lblThumbNaming = new JLabel(BUNDLE.getString("image.episodethumb.naming"));
      panel.add(lblThumbNaming, "cell 0 0");

      chckbxEpisodeThumb1 = new JCheckBox("<dynamic>-thumb.ext");
      panel.add(chckbxEpisodeThumb1, "cell 1 0");

      chckbxEpisodeThumb2 = new JCheckBox("<dynamic>-landscape.ext");
      panel.add(chckbxEpisodeThumb2, "cell 2 0");

      chckbxEpisodeThumb3 = new JCheckBox("<dynamic>.ext");
      panel.add(chckbxEpisodeThumb3, "cell 1 1");

      chckbxEpisodeThumb4 = new JCheckBox("<dynamic>.tbn");
      panel.add(chckbxEpisodeThumb4, "cell 2 1");
    }
  }

  private void buildCheckBoxes() {
    chckbxEpisodeThumb1.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb2.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb3.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb4.removeItemListener(checkBoxListener);
    clearSelection(chckbxEpisodeThumb1, chckbxEpisodeThumb2, chckbxEpisodeThumb3, chckbxEpisodeThumb4);

    for (TvShowEpisodeThumbNaming thumbNaming : TvShowModuleManager.SETTINGS.getEpisodeThumbFilenames()) {
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
