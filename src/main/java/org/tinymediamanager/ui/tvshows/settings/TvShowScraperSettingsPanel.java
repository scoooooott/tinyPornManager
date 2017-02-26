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

import static org.tinymediamanager.core.tvshow.TvShowEpisodeThumbNaming.FILENAME_THUMB_POSTFIX;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.TvShowEpisodeThumbNaming;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;
import org.tinymediamanager.ui.tvshows.TvShowScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowScraperSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowScraperSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 4999827736720726395L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private List<TvShowScraper>         scrapers         = ObservableCollections.observableList(new ArrayList<TvShowScraper>());
  private List<ArtworkScraper>        artworkScrapers  = ObservableCollections.observableList(new ArrayList<ArtworkScraper>());

  /** UI components */
  private JComboBox                   cbScraperTmdbLanguage;
  private JComboBox                   cbCountry;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JPanel                      panelScraperMetadata;
  private JPanel                      panelScraperMetadataContainer;
  private JPanel                      panelArtworkScrapers;
  private JRadioButton                rdbtnThumbWithPostfix;
  private JRadioButton                rdbtnThumbWoPostfix;
  private JLabel                      lblNewLabel;
  private ButtonGroup                 btnGroupThumbFilenaming;
  private JScrollPane                 scrollPaneScraper;
  private JTable                      tableScraper;
  private JPanel                      panelScraperDetails;
  private JTextPane                   tpScraperDescription;
  private JScrollPane                 scrollPaneArtworkScraper;
  private JTable                      tableArtworkScraper;
  private JPanel                      panelArtworkScraperDetails;
  private JTextPane                   tpArtworkScraperDescription;
  private JPanel                      panelScraperOptions;
  private JPanel                      panelArtworkScraperOptions;
  private JScrollPane                 scrollPaneScraperDetails;
  private JScrollPane                 scrollPaneArtworkScraperDetails;
  private JPanel                      panelImages;
  private JRadioButton                rdbtnThumbTbn;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public TvShowScraperSettingsPanel() {
    // data init
    MediaScraper defaultMediaScraper = TvShowList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;
    int counter = 0;
    for (MediaScraper scraper : TvShowList.getInstance().getAvailableMediaScrapers()) {
      TvShowScraper tvShowScraper = new TvShowScraper(scraper);
      if (scraper.equals(defaultMediaScraper)) {
        tvShowScraper.defaultScraper = true;
        selectedIndex = counter;
      }
      scrapers.add(tvShowScraper);
      counter++;
    }
    List<String> enabledArtworkProviders = settings.getTvShowArtworkScrapers();
    int artworkSelectedIndex = -1;
    int counterAW = 0;
    for (MediaScraper scraper : TvShowList.getInstance().getAvailableArtworkScrapers()) {
      ArtworkScraper artworkScraper = new ArtworkScraper(scraper);
      if (enabledArtworkProviders.contains(artworkScraper.getScraperId())) {
        artworkScraper.active = true;
        if (artworkSelectedIndex < 0) {
          artworkSelectedIndex = counterAW;
        }
      }
      artworkScrapers.add(artworkScraper);
      counterAW++;
    }
    // UI init
    setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));
    JPanel panelTvShowScrapers = new JPanel();
    panelTvShowScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.metadata.defaults"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); // $NON-NLS-1$
    add(panelTvShowScrapers, "2, 2, fill, top");
    panelTvShowScrapers.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("100dlu:grow"), FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, }));

    scrollPaneScraper = new JScrollPane();
    panelTvShowScrapers.add(scrollPaneScraper, "1, 2, 3, 1, fill, fill");

    tableScraper = new JTable();
    tableScraper.setRowHeight(29);
    scrollPaneScraper.setViewportView(tableScraper);

    scrollPaneScraperDetails = new JScrollPane();
    scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPaneScraperDetails.setBorder(null);
    panelTvShowScrapers.add(scrollPaneScraperDetails, "5, 2, fill, fill");

    panelScraperDetails = new ScrollablePanel();
    scrollPaneScraperDetails.setViewportView(panelScraperDetails);
    panelScraperDetails.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200dlu:grow"), },
        new RowSpec[] { RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
    tpScraperDescription = new JTextPane();
    tpScraperDescription.setOpaque(false);
    tpScraperDescription.setEditorKit(new HTMLEditorKit());
    ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);
    panelScraperDetails.add(tpScraperDescription, "1, 1, fill, top");
    panelScraperOptions = new JPanel();
    panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
    panelScraperDetails.add(panelScraperOptions, "1, 3, fill, top");

    JSeparator separator = new JSeparator();
    panelTvShowScrapers.add(separator, "1, 4, 5, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelTvShowScrapers.add(lblScraperLanguage, "1, 6, right, default");

    cbScraperTmdbLanguage = new JComboBox(MediaLanguages.values());
    panelTvShowScrapers.add(cbScraperTmdbLanguage, "3, 6");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelTvShowScrapers.add(lblCountry, "1, 8, right, default");

    cbCountry = new JComboBox(CountryCode.values());
    panelTvShowScrapers.add(cbCountry, "3, 8, fill, default");

    btnGroupThumbFilenaming = new ButtonGroup();

    panelArtworkScrapers = new JPanel();
    panelArtworkScrapers.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.images"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelArtworkScrapers, "2, 4, fill, fill");
    panelArtworkScrapers.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("80dlu:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, }));

    scrollPaneArtworkScraper = new JScrollPane();
    panelArtworkScrapers.add(scrollPaneArtworkScraper, "2, 2, fill, fill");

    tableArtworkScraper = new JTable();
    tableArtworkScraper.setRowHeight(29);
    scrollPaneArtworkScraper.setViewportView(tableArtworkScraper);

    scrollPaneArtworkScraperDetails = new JScrollPane();
    scrollPaneArtworkScraperDetails.setBorder(null);
    panelArtworkScrapers.add(scrollPaneArtworkScraperDetails, "4, 2, fill, fill");

    panelArtworkScraperDetails = new JPanel();
    scrollPaneArtworkScraperDetails.setViewportView(panelArtworkScraperDetails);
    panelArtworkScraperDetails.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"), },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    tpArtworkScraperDescription = new JTextPane();
    tpArtworkScraperDescription.setEditorKit(new HTMLEditorKit());
    ((HTMLDocument) tpArtworkScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);
    tpArtworkScraperDescription.setOpaque(false);
    panelArtworkScraperDetails.add(tpArtworkScraperDescription, "2, 2, fill, fill");

    panelArtworkScraperOptions = new JPanel();
    panelArtworkScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
    panelArtworkScraperDetails.add(panelArtworkScraperOptions, "2, 4, fill, fill");

    separator = new JSeparator();
    panelArtworkScrapers.add(separator, "2, 4, 3, 1");

    panelImages = new JPanel();
    panelArtworkScrapers.add(panelImages, "2, 6, 3, 1, fill, fill");
    panelImages.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
            FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { RowSpec.decode("23px"), }));

    lblNewLabel = new JLabel(BUNDLE.getString("image.thumb.naming"));
    panelImages.add(lblNewLabel, "1, 1, left, center");
    rdbtnThumbWithPostfix = new JRadioButton("<dynamic>-thumb.ext");
    panelImages.add(rdbtnThumbWithPostfix, "3, 1, fill, top");
    btnGroupThumbFilenaming.add(rdbtnThumbWithPostfix);
    rdbtnThumbTbn = new JRadioButton("<dynamic>.tbn");
    btnGroupThumbFilenaming.add(rdbtnThumbTbn);
    rdbtnThumbWoPostfix = new JRadioButton("<dynamic>.ext");
    panelImages.add(rdbtnThumbWoPostfix, "5, 1, fill, top");
    btnGroupThumbFilenaming.add(rdbtnThumbWoPostfix);
    panelImages.add(rdbtnThumbTbn, "7, 1");

    panelScraperMetadataContainer = new JPanel();
    panelScraperMetadataContainer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
        BUNDLE.getString("scraper.metadata.defaults"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51))); //$NON-NLS-1$
    add(panelScraperMetadataContainer, "2, 6, fill, top");
    panelScraperMetadataContainer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    panelScraperMetadata = new TvShowScraperMetadataPanel(Settings.getInstance().getTvShowScraperMetadataConfig());
    panelScraperMetadataContainer.add(panelScraperMetadata, "1, 1, 2, 1, fill, default");

    chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape")); //$NON-NLS-1$
    panelScraperMetadataContainer.add(chckbxAutomaticallyScrapeImages, "2, 3");

    initDataBindings();

    {
      // adjust table columns
      // Checkbox and Logo shall have minimal width
      TableColumnResizer.setMaxWidthForColumn(tableScraper, 0, 2);
      TableColumnResizer.setMaxWidthForColumn(tableScraper, 1, 2);
      TableColumnResizer.adjustColumnPreferredWidths(tableScraper, 5);

      TableColumnResizer.setMaxWidthForColumn(tableArtworkScraper, 0, 2);
      TableColumnResizer.setMaxWidthForColumn(tableArtworkScraper, 1, 2);
      TableColumnResizer.adjustColumnPreferredWidths(tableArtworkScraper, 5);

      // implement listener to simulate button group
      tableScraper.getModel().addTableModelListener(new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent arg0) {
          // click on the checkbox
          if (arg0.getColumn() == 0) {
            int row = arg0.getFirstRow();
            TvShowScraper changedScraper = scrapers.get(row);
            // if flag inNFO was changed, change all other trailers flags
            if (changedScraper.getDefaultScraper()) {
              settings.setTvShowScraper(changedScraper.getScraperId());
              for (TvShowScraper scraper : scrapers) {
                if (scraper != changedScraper) {
                  scraper.setDefaultScraper(Boolean.FALSE);
                }
              }
            }
          }
        }
      });

      // implement selection listener to load settings
      tableScraper.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          int index = tableScraper.convertRowIndexToModel(tableScraper.getSelectedRow());
          if (index > -1) {
            panelScraperOptions.removeAll();
            if (scrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
              panelScraperOptions.add(new MediaScraperConfigurationPanel(scrapers.get(index).getMediaProvider()));
            }
            panelScraperOptions.revalidate();
          }
        }
      });

      tableArtworkScraper.getModel().addTableModelListener(new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent arg0) {
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
        }
      });
      // implement selection listener to load settings
      tableArtworkScraper.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          int index = tableArtworkScraper.convertRowIndexToModel(tableArtworkScraper.getSelectedRow());
          if (index > -1) {
            panelArtworkScraperOptions.removeAll();
            if (artworkScrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
              panelArtworkScraperOptions.add(new MediaScraperConfigurationPanel(artworkScrapers.get(index).getMediaProvider()));
            }
            panelArtworkScraperOptions.revalidate();
          }
        }
      });

      // select default TV show scraper
      if (counter > 0) {
        tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
      }

      // select default artwork scraper
      if (counterAW > 0) {
        tableArtworkScraper.getSelectionModel().setSelectionInterval(artworkSelectedIndex, artworkSelectedIndex);
      }

      ItemListener itemListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          checkChanges();
        }
      };

      rdbtnThumbWoPostfix.addItemListener(itemListener);
      rdbtnThumbWithPostfix.addItemListener(itemListener);
      rdbtnThumbTbn.addItemListener(itemListener);

      switch (settings.getTvShowEpisodeThumbFilename()) {
        case FILENAME_THUMB_POSTFIX:
          rdbtnThumbWithPostfix.setSelected(true);
          break;

        case FILENAME_THUMB:
          rdbtnThumbWoPostfix.setSelected(true);
          break;

        case FILENAME_THUMB_TBN:
          rdbtnThumbTbn.setSelected(true);
          break;

        default:
          break;
      }
    }
  }

  /**
   * Check changes.
   */
  public void checkChanges() {
    if (rdbtnThumbWithPostfix.isSelected()) {
      settings.setTvShowEpisodeThumbFilename(FILENAME_THUMB_POSTFIX);
    }
    if (rdbtnThumbWoPostfix.isSelected()) {
      settings.setTvShowEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_THUMB);
    }
    if (rdbtnThumbTbn.isSelected()) {
      settings.setTvShowEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_THUMB_TBN);
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public static class TvShowScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      defaultScraper;

    public TvShowScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider() == null || scraper.getMediaProvider().getProviderInfo() == null
          || scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
      }
    }

    private ImageIcon getScaledIcon(ImageIcon original) {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(new JPanel().getFont());

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

    public Boolean getDefaultScraper() {
      return defaultScraper;
    }

    public void setDefaultScraper(Boolean newValue) {
      Boolean oldValue = this.defaultScraper;
      this.defaultScraper = newValue;
      firePropertyChange("defaultScraper", oldValue, newValue);
    }

    public IMediaProvider getMediaProvider() {
      return scraper.getMediaProvider();
    }
  }

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

  @SuppressWarnings("rawtypes")
  protected void initDataBindings() {
    BeanProperty<TvShowSettings, MediaLanguages> settingsBeanProperty_8 = BeanProperty.create("scraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<TvShowSettings, MediaLanguages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperTmdbLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSettings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("certificationCountry");
    AutoBinding<TvShowSettings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    JTableBinding<TvShowScraper, List<TvShowScraper>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, scrapers,
        tableScraper);
    //
    BeanProperty<TvShowScraper, Boolean> tvShowScraperBeanProperty = BeanProperty.create("defaultScraper");
    jTableBinding.addColumnBinding(tvShowScraperBeanProperty).setColumnName("Default").setColumnClass(Boolean.class);
    //
    BeanProperty<TvShowScraper, Icon> tvShowScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding.addColumnBinding(tvShowScraperBeanProperty_1).setColumnName("Logo").setColumnClass(Icon.class);
    //
    BeanProperty<TvShowScraper, String> tvShowScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding.addColumnBinding(tvShowScraperBeanProperty_2).setColumnName("Name").setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, tableScraper, jTableBeanProperty,
        tpScraperDescription, jTextPaneBeanProperty);
    autoBinding_3.bind();
    //
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
    BeanProperty<JTextPane, String> jTextPaneBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tableArtworkScraper,
        jTableBeanProperty, tpArtworkScraperDescription, jTextPaneBeanProperty_1);
    autoBinding_1.bind();
  }
}
