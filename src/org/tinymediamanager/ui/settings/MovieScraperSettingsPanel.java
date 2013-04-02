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
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.imdb.ImdbSiteDefinition;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;
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

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The settings. */
  private Settings                    settings         = Settings.getInstance();

  /** The button group scraper. */
  private ButtonGroup                 buttonGroupScraper;

  /** The cb scraper tmdb language. */
  private JComboBox                   cbScraperTmdbLanguage;

  /** The cb country. */
  private JComboBox                   cbCountry;

  /** The cb imdb translateable content. */
  private JCheckBox                   cbImdbTranslateableContent;

  /** The cb scraper imdb. */
  private JCheckBox                   cbScraperImdb;

  /** The chckbx automatically scrape images. */
  private JCheckBox                   chckbxAutomaticallyScrapeImages;

  /** The cb imdb site. */
  private JComboBox                   cbImdbSite;

  /** The panel scraper metadata. */
  private JPanel                      panelScraperMetadata;

  /** The panel scraper metadata container. */
  private JPanel                      panelScraperMetadataContainer;

  /** The cb scraper ofdbde. */
  private JCheckBox                   cbScraperOfdbde;

  /** The cb scraper tmdb. */
  private JCheckBox                   cbScraperTmdb;

  /** The panel. */
  private JPanel                      panel;

  /** The cb the movie database. */
  private JCheckBox                   cbTheMovieDatabase;

  /** The cb hdtrailersnet. */
  private JCheckBox                   cbHdtrailersnet;

  /** The cb ofdbde. */
  private JCheckBox                   cbOfdbde;

  /** The cb scraper zelluloidde. */
  private JCheckBox                   cbScraperZelluloidde;

  /** The separator. */
  private JSeparator                  separator;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public MovieScraperSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    JPanel panelMovieScrapers = new JPanel();
    panelMovieScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.metadata.defaults"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMovieScrapers, "2, 2, fill, top");
    panelMovieScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    cbScraperTmdb = new JCheckBox("The Movie Database");
    buttonGroupScraper = new ButtonGroup();
    buttonGroupScraper.add(cbScraperTmdb);
    cbScraperTmdb.setSelected(true);
    panelMovieScrapers.add(cbScraperTmdb, "1, 2");

    cbScraperImdb = new JCheckBox("IMDB");
    buttonGroupScraper.add(cbScraperImdb);
    panelMovieScrapers.add(cbScraperImdb, "1, 4");

    cbImdbSite = new JComboBox(ImdbSiteDefinition.values());
    panelMovieScrapers.add(cbImdbSite, "3, 4, fill, default");

    cbImdbTranslateableContent = new JCheckBox(BUNDLE.getString("Settings.getfromTMDB")); //$NON-NLS-1$
    panelMovieScrapers.add(cbImdbTranslateableContent, "3, 5");

    cbScraperOfdbde = new JCheckBox("OFDb.de");
    buttonGroupScraper.add(cbScraperOfdbde);
    panelMovieScrapers.add(cbScraperOfdbde, "1, 7");

    cbScraperZelluloidde = new JCheckBox("Zelluloid.de");
    buttonGroupScraper.add(cbScraperZelluloidde);
    panelMovieScrapers.add(cbScraperZelluloidde, "1, 9");

    separator = new JSeparator();
    panelMovieScrapers.add(separator, "1, 11, 3, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelMovieScrapers.add(lblScraperLanguage, "1, 13, right, default");

    cbScraperTmdbLanguage = new JComboBox(TmdbMetadataProvider.Languages.values());
    panelMovieScrapers.add(cbScraperTmdbLanguage, "3, 13");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelMovieScrapers.add(lblCountry, "1, 15, right, default");

    cbCountry = new JComboBox(CountryCode.values());
    panelMovieScrapers.add(cbCountry, "3, 15, fill, default");

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
        cbScraperZelluloidde.setSelected(true);
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
    cbScraperZelluloidde.addItemListener(new ItemListener() {
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
    if (cbScraperZelluloidde.isSelected()) {
      settings.getMovieSettings().setMovieScraper(MovieScrapers.ZELLULOID);
    }
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<Settings, Languages> settingsBeanProperty_8 = BeanProperty.create("scraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, Languages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperTmdbLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("certificationCountry");
    AutoBinding<Settings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCountry, jComboBoxBeanProperty);
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
    BeanProperty<Settings, ImdbSiteDefinition> settingsBeanProperty_1 = BeanProperty.create("movieSettings.imdbSite");
    AutoBinding<Settings, ImdbSiteDefinition, JComboBox, Object> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, cbImdbSite, jComboBoxBeanProperty);
    autoBinding_1.bind();
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
