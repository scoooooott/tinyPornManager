/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.Color;
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
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieTrailerQuality;
import org.tinymediamanager.core.movie.MovieTrailerSources;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;

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
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  private List<MovieScraper>          scrapers         = ObservableCollections.observableList(new ArrayList<MovieScraper>());

  /**
   * UI Elements
   */
  private JComboBox                   cbScraperLanguage;
  private JComboBox                   cbCertificationCountry;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JPanel                      panelScraperMetadata;
  private JPanel                      panelScraperMetadataContainer;
  private JPanel                      panelTrailerScrapers;
  private JCheckBox                   cbTheMovieDatabase;
  private JCheckBox                   cbHdtrailersnet;
  private JCheckBox                   cbOfdbde;
  private JTextPane                   lblScraperThresholdHint;
  private JPanel                      panelAutomaticScraper;
  private JSlider                     sliderThreshold;
  private JCheckBox                   chckbxScraperFallback;
  private JCheckBox                   chckbxUseTrailerPreferences;
  private JLabel                      lblTrailerSource;
  private JLabel                      lblTrailerQuality;
  private JComboBox                   cbTrailerSource;
  private JComboBox                   cbTrailerQuality;
  private JSeparator                  separator_2;
  private JCheckBox                   chckbxImageLanguage;
  private JPanel                      panelScraperDetails;
  private JPanel                      panelScraperOptions;
  private JTextPane                   tpScraperDescription;
  private JScrollPane                 scrollPaneScraper;
  private JTable                      tableScraper;

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
    setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC,
        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    JPanel panelMovieScrapers = new JPanel();
    panelMovieScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.metadata"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMovieScrapers, "2, 2, 3, 1, fill, fill");
    panelMovieScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"), }, new RowSpec[] {
        FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("150dlu:grow"), FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
        FormSpecs.DEFAULT_ROWSPEC, }));

    scrollPaneScraper = new JScrollPane();
    panelMovieScrapers.add(scrollPaneScraper, "1, 2, 3, 1, fill, fill");

    tableScraper = new JTable();
    tableScraper.setRowHeight(29);
    scrollPaneScraper.setViewportView(tableScraper);

    panelScraperDetails = new JPanel();
    panelMovieScrapers.add(panelScraperDetails, "5, 2, fill, fill");
    panelScraperDetails.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("default:grow"), }));

    tpScraperDescription = new JTextPane();
    tpScraperDescription.setOpaque(false);
    tpScraperDescription.setEditorKit(new HTMLEditorKit());
    panelScraperDetails.add(tpScraperDescription, "2, 2");

    panelScraperOptions = new JPanel();
    panelScraperDetails.add(panelScraperOptions, "2, 4, fill, fill");

    JSeparator separator = new JSeparator();
    panelMovieScrapers.add(separator, "1, 4, 5, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelMovieScrapers.add(lblScraperLanguage, "1, 5, right, default");

    cbScraperLanguage = new JComboBox(MediaLanguages.values());
    panelMovieScrapers.add(cbScraperLanguage, "3, 5");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelMovieScrapers.add(lblCountry, "1, 7, right, default");

    cbCertificationCountry = new JComboBox(CountryCode.values());
    panelMovieScrapers.add(cbCertificationCountry, "3, 7, fill, default");
    panelMovieScrapers.add(new JSeparator(), "1, 8, 5, 1");

    chckbxScraperFallback = new JCheckBox(BUNDLE.getString("Settings.scraperfallback")); //$NON-NLS-1$
    panelMovieScrapers.add(chckbxScraperFallback, "1, 9, 5, 1");

    panelTrailerScrapers = new JPanel();
    panelTrailerScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.trailer"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelTrailerScrapers, "2, 4, 3, 1, fill, fill");
    panelTrailerScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    cbTheMovieDatabase = new JCheckBox("The Movie Database");
    panelTrailerScrapers.add(cbTheMovieDatabase, "2, 2");

    cbHdtrailersnet = new JCheckBox("HD-Trailers.net");
    panelTrailerScrapers.add(cbHdtrailersnet, "2, 4");

    cbOfdbde = new JCheckBox("OFDb.de");
    panelTrailerScrapers.add(cbOfdbde, "2, 6");

    separator_2 = new JSeparator();
    panelTrailerScrapers.add(separator_2, "2, 8, 5, 1");

    chckbxUseTrailerPreferences = new JCheckBox(BUNDLE.getString("Settings.trailer.preferred")); //$NON-NLS-1$ 
    panelTrailerScrapers.add(chckbxUseTrailerPreferences, "2, 10, 3, 1");

    lblTrailerSource = new JLabel(BUNDLE.getString("Settings.trailer.source")); //$NON-NLS-1$
    panelTrailerScrapers.add(lblTrailerSource, "2, 12, right, default");

    cbTrailerSource = new JComboBox(MovieTrailerSources.values());
    panelTrailerScrapers.add(cbTrailerSource, "4, 12, fill, default");

    lblTrailerQuality = new JLabel(BUNDLE.getString("Settings.trailer.quality")); //$NON-NLS-1$
    panelTrailerScrapers.add(lblTrailerQuality, "2, 14, right, default");

    cbTrailerQuality = new JComboBox(MovieTrailerQuality.values());
    panelTrailerScrapers.add(cbTrailerQuality, "4, 14, fill, default");

    panelScraperMetadataContainer = new JPanel();
    panelScraperMetadataContainer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE
        .getString("scraper.metadata.defaults"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51))); //$NON-NLS-1$
    add(panelScraperMetadataContainer, "2, 6, fill, fill");
    panelScraperMetadataContainer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("10dlu"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

    panelScraperMetadata = new MovieScraperMetadataPanel(settings.getMovieScraperMetadataConfig());
    panelScraperMetadataContainer.add(panelScraperMetadata, "1, 1, 4, 1, fill, default");

    chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape")); //$NON-NLS-1$
    panelScraperMetadataContainer.add(chckbxAutomaticallyScrapeImages, "2, 3, 3, 1");

    chckbxImageLanguage = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape.language"));//$NON-NLS-1$
    panelScraperMetadataContainer.add(chckbxImageLanguage, "4, 5");

    panelAutomaticScraper = new JPanel();
    panelAutomaticScraper.setBorder(new TitledBorder(null,
        BUNDLE.getString("Settings.automaticscraper"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelAutomaticScraper, "4, 6, fill, fill");
    panelAutomaticScraper.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblScraperTreshold = new JLabel(BUNDLE.getString("Settings.scraperTreshold")); //$NON-NLS-1$
    panelAutomaticScraper.add(lblScraperTreshold, "1, 2, default, top");

    sliderThreshold = new JSlider();
    sliderThreshold.setMinorTickSpacing(5);
    sliderThreshold.setMajorTickSpacing(10);
    sliderThreshold.setPaintTicks(true);
    sliderThreshold.setPaintLabels(true);
    sliderThreshold.setValue((int) (settings.getMovieSettings().getScraperThreshold() * 100));

    Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
    labelTable.put(Integer.valueOf(100), new JLabel("1.0"));
    labelTable.put(Integer.valueOf(75), new JLabel("0.75"));
    labelTable.put(Integer.valueOf(50), new JLabel("0.50"));
    labelTable.put(Integer.valueOf(25), new JLabel("0.25"));
    labelTable.put(Integer.valueOf(0), new JLabel("0.0"));
    sliderThreshold.setLabelTable(labelTable);
    sliderThreshold.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent arg0) {
        settings.getMovieSettings().setScraperThreshold(sliderThreshold.getValue() / 100.0);
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
          // if flag inNFO was changed, change all other trailers flags
          if (changedScraper.getDefaultScraper()) {
            settings.getMovieSettings().setMovieScraper(changedScraper.getScraperId());
            for (MovieScraper scraper : scrapers) {
              if (scraper != changedScraper) {
                scraper.setDefaultScraper(Boolean.FALSE);
              }
            }
          }
        }
      }
    });

    // select default movie scraper
    tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public class MovieScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      defaultScraper;

    public MovieScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo());
      }
    }

    public String getScraperId() {
      return scraper.getId();
    }

    public String getScraperName() {
      return scraper.getName();
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

  }

  protected void initDataBindings() {
    BeanProperty<Settings, MediaLanguages> settingsBeanProperty_8 = BeanProperty.create("movieSettings.scraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, MediaLanguages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("movieSettings.certificationCountry");
    AutoBinding<Settings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCertificationCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("movieSettings.scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_2 = BeanProperty.create("movieSettings.trailerScraperTmdb");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, cbTheMovieDatabase, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_3 = BeanProperty.create("movieSettings.trailerScraperHdTrailers");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, cbHdtrailersnet, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_4 = BeanProperty.create("movieSettings.trailerScraperOfdb");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, cbOfdbde, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("movieSettings.scraperFallback");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxScraperFallback, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_5 = BeanProperty.create("movieSettings.useTrailerPreference");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, chckbxUseTrailerPreferences, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, MovieTrailerSources> settingsBeanProperty_6 = BeanProperty.create("movieSettings.trailerSource");
    AutoBinding<Settings, MovieTrailerSources, JComboBox, Object> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, cbTrailerSource, jComboBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<Settings, MovieTrailerQuality> settingsBeanProperty_7 = BeanProperty.create("movieSettings.trailerQuality");
    AutoBinding<Settings, MovieTrailerQuality, JComboBox, Object> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, cbTrailerQuality, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ,
        chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty, chckbxImageLanguage, jCheckBoxBeanProperty_1);
    autoBinding_10.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_10 = BeanProperty.create("movieSettings.imageLanguagePriority");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
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
