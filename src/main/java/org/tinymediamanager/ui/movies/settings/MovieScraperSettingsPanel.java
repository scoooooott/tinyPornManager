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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
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
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieScraperSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieScraperSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = -299825914193235308L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  private MovieSettings               settings         = MovieModuleManager.MOVIE_SETTINGS;

  private List<MovieScraper>          scrapers         = ObservableCollections.observableList(new ArrayList<MovieScraper>());

  /**
   * UI Elements
   */
  private JComboBox                   cbScraperLanguage;
  private JComboBox                   cbCertificationCountry;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JPanel                      panelScraperMetadata;
  private JPanel                      panelScraperMetadataContainer;
  private JTextPane                   lblScraperThresholdHint;
  private JPanel                      panelAutomaticScraper;
  private JSlider                     sliderThreshold;
  private JCheckBox                   chckbxScraperFallback;
  private JCheckBox                   chckbxImageLanguage;
  private JPanel                      panelScraperDetails;
  private JPanel                      panelScraperOptions;
  private JTextPane                   tpScraperDescription;
  private JScrollPane                 scrollPaneScraper;
  private JTable                      tableScraper;
  private JScrollPane                 scrollPane;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public MovieScraperSettingsPanel() {
    // data init
    MediaScraper defaultMediaScraper = MovieList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;
    int counter = 0;
    for (MediaScraper scraper : MovieList.getInstance().getAvailableMediaScrapers()) {
      MovieScraper movieScraper = new MovieScraper(scraper);
      if (scraper.equals(defaultMediaScraper)) {
        movieScraper.defaultScraper = true;
        selectedIndex = counter;
      }
      scrapers.add(movieScraper);
      counter++;
    }
    // UI init
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    JPanel panelMovieScrapers = new JPanel();
    panelMovieScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.metadata"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); // $NON-NLS-1$
    add(panelMovieScrapers, "2, 2, 3, 1, fill, fill");
    panelMovieScrapers.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50dlu:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("150dlu:grow"), FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    scrollPaneScraper = new JScrollPane();
    panelMovieScrapers.add(scrollPaneScraper, "2, 2, 3, 1, fill, fill");

    tableScraper = new JTable() {
      private static final long serialVersionUID = -144223066269069772L;

      @Override
      public java.awt.Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        java.awt.Component comp = super.prepareRenderer(renderer, row, col);
        String value = getModel().getValueAt(row, 2).toString();
        if (!Globals.isDonator() && value.startsWith("Kodi")) { // FIXME: use scraper.isEnabled() somehow?
          comp.setBackground(Color.lightGray);
          comp.setEnabled(false);
        }
        else {
          comp.setBackground(Color.white);
          comp.setEnabled(true);
        }
        return comp;
      }
    };
    tableScraper.setRowHeight(29);
    scrollPaneScraper.setViewportView(tableScraper);

    scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    panelMovieScrapers.add(scrollPane, "6, 1, 1, 7, fill, fill");

    panelScraperDetails = new ScrollablePanel();
    scrollPane.setViewportView(panelScraperDetails);
    panelScraperDetails.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.UNRELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));
    {
      // add a CSS rule to force body tags to use the default label font
      // instead of the value in javax.swing.text.html.default.csss
      Font font = UIManager.getFont("Label.font");
      String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";

      tpScraperDescription = new JTextPane();
      tpScraperDescription.setOpaque(false);
      tpScraperDescription.setEditorKit(new HTMLEditorKit());
      ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);
      panelScraperDetails.add(tpScraperDescription, "1, 1, default, top");
    }

    panelScraperOptions = new JPanel();
    panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
    panelScraperDetails.add(panelScraperOptions, "1, 3, fill, top");

    JSeparator separator = new JSeparator();
    panelMovieScrapers.add(separator, "2, 4, 3, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelMovieScrapers.add(lblScraperLanguage, "2, 5, right, default");

    cbScraperLanguage = new JComboBox(MediaLanguages.values());
    panelMovieScrapers.add(cbScraperLanguage, "4, 5");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelMovieScrapers.add(lblCountry, "2, 7, right, default");

    cbCertificationCountry = new JComboBox(CountryCode.values());
    panelMovieScrapers.add(cbCertificationCountry, "4, 7, fill, default");
    panelMovieScrapers.add(new JSeparator(), "2, 8, 5, 1");

    chckbxScraperFallback = new JCheckBox(BUNDLE.getString("Settings.scraperfallback")); //$NON-NLS-1$
    panelMovieScrapers.add(chckbxScraperFallback, "2, 9, 5, 1");

    panelScraperMetadataContainer = new JPanel();
    panelScraperMetadataContainer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
        BUNDLE.getString("scraper.metadata.defaults"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51))); //$NON-NLS-1$
    add(panelScraperMetadataContainer, "2, 4, fill, fill");
    panelScraperMetadataContainer.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("10dlu"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    panelScraperMetadata = new MovieScraperMetadataPanel(Settings.getInstance().getMovieScraperMetadataConfig());
    panelScraperMetadataContainer.add(panelScraperMetadata, "1, 1, 4, 1, fill, default");

    chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape")); //$NON-NLS-1$
    panelScraperMetadataContainer.add(chckbxAutomaticallyScrapeImages, "2, 3, 3, 1");

    chckbxImageLanguage = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape.language"));//$NON-NLS-1$
    panelScraperMetadataContainer.add(chckbxImageLanguage, "4, 5");

    panelAutomaticScraper = new JPanel();
    panelAutomaticScraper
        .setBorder(new TitledBorder(null, BUNDLE.getString("Settings.automaticscraper"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelAutomaticScraper, "4, 4, fill, fill");
    panelAutomaticScraper.setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblScraperTreshold = new JLabel(BUNDLE.getString("Settings.scraperTreshold")); //$NON-NLS-1$
    panelAutomaticScraper.add(lblScraperTreshold, "1, 2, default, top");

    sliderThreshold = new JSlider();
    sliderThreshold.setMinorTickSpacing(5);
    sliderThreshold.setMajorTickSpacing(10);
    sliderThreshold.setPaintTicks(true);
    sliderThreshold.setPaintLabels(true);
    sliderThreshold.setValue((int) (settings.getScraperThreshold() * 100));

    Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
    labelTable.put(100, new JLabel("1.0"));
    labelTable.put(75, new JLabel("0.75"));
    labelTable.put(50, new JLabel("0.50"));
    labelTable.put(25, new JLabel("0.25"));
    labelTable.put(0, new JLabel("0.0"));
    sliderThreshold.setLabelTable(labelTable);
    sliderThreshold.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent arg0) {
        settings.setScraperThreshold(sliderThreshold.getValue() / 100.0);
      }
    });
    panelAutomaticScraper.add(sliderThreshold, "3, 2");

    lblScraperThresholdHint = new JTextPane();
    panelAutomaticScraper.add(lblScraperThresholdHint, "1, 6, 3, 1");
    lblScraperThresholdHint.setOpaque(false);
    TmmFontHelper.changeFont(lblScraperThresholdHint, 0.833);
    lblScraperThresholdHint.setText(BUNDLE.getString("Settings.scraperTreshold.hint")); //$NON-NLS-1$

    initDataBindings();

    // adjust table columns
    // Checkbox and Logo shall have minimal width
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableScraper, 5);

    // implement listener to simulate button group
    tableScraper.getModel().addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        // click on the checkbox
        if (arg0.getColumn() == 0) {
          int row = arg0.getFirstRow();
          MovieScraper changedScraper = scrapers.get(row);
          // if flag default scraper was changed, change all other flags
          if (changedScraper.getDefaultScraper()) {
            settings.setMovieScraper(changedScraper.getScraperId());
            for (MovieScraper scraper : scrapers) {
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

    // select default movie scraper
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public static class MovieScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      defaultScraper;

    public MovieScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider() == null || scraper.getMediaProvider().getProviderInfo() == null
          || scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
      }
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

    private ImageIcon getScaledIcon(ImageIcon original) {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(new JPanel().getFont());

      int height = (int) (fm.getHeight() * 2f);
      int width = original.getIconWidth() / original.getIconHeight() * height;

      BufferedImage scaledImage;
      if (!scraper.isEnabled()) {
        scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
            Scalr.OP_GRAYSCALE);
      }
      else {
        scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
            Scalr.OP_ANTIALIAS);
      }
      return new ImageIcon(scaledImage);
    }

    public Icon getScraperLogo() {
      return scraperLogo;
    }

    public Boolean getDefaultScraper() {
      return defaultScraper;
    }

    public void setDefaultScraper(Boolean newValue) {
      if (scraper.isEnabled()) {
        Boolean oldValue = this.defaultScraper;
        this.defaultScraper = newValue;
        firePropertyChange("defaultScraper", oldValue, newValue);
      }
    }

    public IMediaProvider getMediaProvider() {
      return scraper.getMediaProvider();
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, MediaLanguages> settingsBeanProperty_8 = BeanProperty.create("scraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MediaLanguages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MovieSettings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("certificationCountry");
    AutoBinding<MovieSettings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCertificationCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("scraperFallback");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxScraperFallback, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ,
        chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty, chckbxImageLanguage, jCheckBoxBeanProperty_1);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_10 = BeanProperty.create("imageLanguagePriority");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, chckbxImageLanguage, jCheckBoxBeanProperty);
    autoBinding_11.bind();
    //
    JTableBinding<MovieScraper, List<MovieScraper>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, scrapers,
        tableScraper);
    //
    BeanProperty<MovieScraper, Boolean> movieScraperBeanProperty = BeanProperty.create("defaultScraper");
    jTableBinding.addColumnBinding(movieScraperBeanProperty).setColumnName("Default").setColumnClass(Boolean.class);
    //
    BeanProperty<MovieScraper, Icon> movieScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding.addColumnBinding(movieScraperBeanProperty_1).setColumnName("Logo").setColumnClass(Icon.class);
    //
    BeanProperty<MovieScraper, String> movieScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding.addColumnBinding(movieScraperBeanProperty_2).setColumnName("Name").setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ, tableScraper, jTableBeanProperty,
        tpScraperDescription, jTextPaneBeanProperty);
    autoBinding_12.bind();
  }
}
