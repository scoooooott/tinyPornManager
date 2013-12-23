/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui.settings;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieScraperSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieScraperSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -299825914193235308L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  /**
   * UI Elements
   */
  private ButtonGroup                 buttonGroupScraper;
  private JComboBox                   cbScraperLanguage;
  private JComboBox                   cbCertificationCountry;
  private JCheckBox                   cbImdbTranslateableContent;
  private JCheckBox                   cbScraperImdb;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JPanel                      panelScraperMetadata;
  private JPanel                      panelScraperMetadataContainer;
  private JCheckBox                   cbScraperOfdbde;
  private JCheckBox                   cbScraperTmdb;
  private JPanel                      panel;
  private JCheckBox                   cbTheMovieDatabase;
  private JCheckBox                   cbHdtrailersnet;
  private JCheckBox                   cbOfdbde;
  private JCheckBox                   cbZelluloidde;
  private JCheckBox                   cbMoviemeternl;
  private JTextPane                   lblScraperThresholdHint;
  private JPanel                      panelAutomaticScraper;
  private JSlider                     sliderThreshold;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public MovieScraperSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    JPanel panelMovieScrapers = new JPanel();
    panelMovieScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.metadata.defaults"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMovieScrapers, "2, 2, fill, top");
    panelMovieScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    cbScraperTmdb = new JCheckBox("The Movie Database");
    buttonGroupScraper = new ButtonGroup();
    buttonGroupScraper.add(cbScraperTmdb);
    cbScraperTmdb.setSelected(true);
    panelMovieScrapers.add(cbScraperTmdb, "1, 2");

    cbScraperImdb = new JCheckBox("IMDB");
    buttonGroupScraper.add(cbScraperImdb);
    panelMovieScrapers.add(cbScraperImdb, "1, 4");

    cbImdbTranslateableContent = new JCheckBox(BUNDLE.getString("Settings.getfromTMDB")); //$NON-NLS-1$
    panelMovieScrapers.add(cbImdbTranslateableContent, "3, 4");

    cbScraperOfdbde = new JCheckBox("OFDb.de");
    buttonGroupScraper.add(cbScraperOfdbde);
    panelMovieScrapers.add(cbScraperOfdbde, "1, 6");

    cbZelluloidde = new JCheckBox("Zelluloid.de");
    buttonGroupScraper.add(cbZelluloidde);
    panelMovieScrapers.add(cbZelluloidde, "1, 8");

    cbMoviemeternl = new JCheckBox("MovieMeter.nl");
    buttonGroupScraper.add(cbMoviemeternl);
    panelMovieScrapers.add(cbMoviemeternl, "1, 10");

    JSeparator separator = new JSeparator();
    panelMovieScrapers.add(separator, "1, 11, 3, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelMovieScrapers.add(lblScraperLanguage, "1, 12, right, default");

    cbScraperLanguage = new JComboBox(MediaLanguages.values());
    panelMovieScrapers.add(cbScraperLanguage, "3, 12");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelMovieScrapers.add(lblCountry, "1, 14, right, default");

    cbCertificationCountry = new JComboBox(CountryCode.values());
    panelMovieScrapers.add(cbCertificationCountry, "3, 14, fill, default");

    panelScraperMetadataContainer = new JPanel();
    panelScraperMetadataContainer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE
        .getString("scraper.metadata.defaults"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51))); //$NON-NLS-1$
    add(panelScraperMetadataContainer, "4, 2, fill, top");
    panelScraperMetadataContainer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    panelScraperMetadata = new MovieScraperMetadataPanel(settings.getMovieScraperMetadataConfig());
    panelScraperMetadataContainer.add(panelScraperMetadata, "1, 1, 2, 1, fill, default");

    chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape")); //$NON-NLS-1$
    panelScraperMetadataContainer.add(chckbxAutomaticallyScrapeImages, "2, 3");

    panel = new JPanel();
    panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.trailer"), TitledBorder.LEADING,
        TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panel, "2, 4, fill, fill");
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    cbTheMovieDatabase = new JCheckBox("The Movie Database");
    panel.add(cbTheMovieDatabase, "1, 2");

    cbHdtrailersnet = new JCheckBox("HD-Trailers.net");
    panel.add(cbHdtrailersnet, "1, 4");

    cbOfdbde = new JCheckBox("OFDb.de");
    panel.add(cbOfdbde, "1, 6");

    panelAutomaticScraper = new JPanel();
    panelAutomaticScraper.setBorder(new TitledBorder(null,
        BUNDLE.getString("Settings.automaticscraper"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelAutomaticScraper, "4, 4, fill, fill");
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
    java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
    labelTable.put(new Integer(100), new JLabel("1.0"));
    labelTable.put(new Integer(75), new JLabel("0.75"));
    labelTable.put(new Integer(50), new JLabel("0.50"));
    labelTable.put(new Integer(25), new JLabel("0.25"));
    labelTable.put(new Integer(0), new JLabel("0.0"));
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
    lblScraperThresholdHint.setFont(lblScraperThresholdHint.getFont().deriveFont(10f));
    lblScraperThresholdHint.setText(BUNDLE.getString("Settings.scraperTreshold.hint")); //$NON-NLS-1$

    initDataBindings();

    // set movie Scrapers
    MovieScrapers movieScraper = settings.getMovieSettings().getMovieScraper();
    switch (movieScraper) {
      case IMDB:
        cbScraperImdb.setSelected(true);
        break;

      case OFDB:
        cbScraperOfdbde.setSelected(true);
        break;

      case ZELLULOID:
        cbZelluloidde.setSelected(true);
        break;

      case MOVIEMETER:
        cbMoviemeternl.setSelected(true);
        break;

      case TMDB:
      default:
        cbScraperTmdb.setSelected(true);
    }

    cbScraperImdb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbScraperTmdb.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbScraperOfdbde.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbZelluloidde.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbMoviemeternl.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
  }

  /**
   * Check changes.
   */
  public void checkChanges() {
    // save scraper
    if (cbScraperImdb.isSelected()) {
      settings.getMovieSettings().setMovieScraper(MovieScrapers.IMDB);
    }
    if (cbScraperTmdb.isSelected()) {
      settings.getMovieSettings().setMovieScraper(MovieScrapers.TMDB);
    }
    if (cbScraperOfdbde.isSelected()) {
      settings.getMovieSettings().setMovieScraper(MovieScrapers.OFDB);
    }
    if (cbZelluloidde.isSelected()) {
      settings.getMovieSettings().setMovieScraper(MovieScrapers.ZELLULOID);
    }
    if (cbMoviemeternl.isSelected()) {
      settings.getMovieSettings().setMovieScraper(MovieScrapers.MOVIEMETER);
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
    BeanProperty<Settings, Boolean> settingsBeanProperty_13 = BeanProperty.create("movieSettings.imdbScrapeForeignLanguage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, cbImdbTranslateableContent, jCheckBoxBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("movieSettings.scrapeBestImage");
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
  }
}
