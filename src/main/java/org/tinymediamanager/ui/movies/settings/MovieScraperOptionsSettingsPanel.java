/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import static org.tinymediamanager.ui.TmmFontHelper.H3;
import static org.tinymediamanager.ui.TmmFontHelper.L2;

import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.movies.panels.MovieScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link MovieScraperSettingsPanel} shows scraper options for the meta data scraper.
 *
 * @author Manuel Laggner
 */
class MovieScraperOptionsSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -299825914193235308L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private MovieSettings               settings         = MovieModuleManager.SETTINGS;
  private JSlider                     sliderThreshold;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JComboBox<MediaLanguages>   cbScraperLanguage;
  private JComboBox<CountryCode>      cbCertificationCountry;
  private JCheckBox                   chckbxScraperFallback;
  private JCheckBox                   chckbxCapitalizeWords;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  MovieScraperOptionsSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();

    // data init
    Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
    labelTable.put(100, new JLabel("1.0"));
    labelTable.put(75, new JLabel("0.75"));
    labelTable.put(50, new JLabel("0.50"));
    labelTable.put(25, new JLabel("0.25"));
    labelTable.put(0, new JLabel("0.0"));
    sliderThreshold.setLabelTable(labelTable);
    sliderThreshold.setValue((int) (settings.getScraperThreshold() * 100));
    sliderThreshold.addChangeListener(arg0 -> settings.setScraperThreshold(sliderThreshold.getValue() / 100.0));
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow,shrink 0]", "[][]15lp![][15lp!][][15lp!][]"));
    {
      JPanel panelOptions = SettingsPanelFactory.createSettingsPanel();

      JLabel lblOptions = new TmmLabel(BUNDLE.getString("Settings.advancedoptions"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelOptions, lblOptions, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage"));
        panelOptions.add(lblScraperLanguage, "cell 1 0 2 1");

        cbScraperLanguage = new JComboBox(MediaLanguages.valuesSorted());
        panelOptions.add(cbScraperLanguage, "cell 1 0");

        JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry"));
        panelOptions.add(lblCountry, "cell 1 1 2 1");

        cbCertificationCountry = new JComboBox(CountryCode.values());
        panelOptions.add(cbCertificationCountry, "cell 1 1");

        chckbxScraperFallback = new JCheckBox(BUNDLE.getString("Settings.scraperfallback"));
        panelOptions.add(chckbxScraperFallback, "cell 1 2 2 1");

        chckbxCapitalizeWords = new JCheckBox((BUNDLE.getString("Settings.scraper.capitalizeWords")));
        panelOptions.add(chckbxCapitalizeWords, "cell 1 3 2 1");
      }
    }
    {
      JPanel panelDefaults = SettingsPanelFactory.createSettingsPanel();

      JLabel lblDefaultsT = new TmmLabel(BUNDLE.getString("scraper.metadata.defaults"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelDefaults, lblDefaultsT, true);
      add(collapsiblePanel, "cell 0 2,growx, wmin 0");
      {
        MovieScraperMetadataPanel movieScraperMetadataPanel = new MovieScraperMetadataPanel();
        panelDefaults.add(movieScraperMetadataPanel, "cell 1 0 2 1");
      }
    }
    {
      JPanel panelImages = SettingsPanelFactory.createSettingsPanel();

      JLabel lblImagesT = new TmmLabel(BUNDLE.getString("Settings.images"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelImages, lblImagesT, true);
      add(collapsiblePanel, "cell 0 4,growx,wmin 0");
      {
        chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape"));
        panelImages.add(chckbxAutomaticallyScrapeImages, "cell 1 0 2 1");
      }
    }
    {
      JPanel panelAutomaticScrape = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][][300lp][grow]", ""));

      JLabel lblAutomaticScrapeT = new TmmLabel(BUNDLE.getString("Settings.automaticscraper"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelAutomaticScrape, lblAutomaticScrapeT, true);
      add(collapsiblePanel, "cell 0 6,growx,wmin 0");
      {
        JLabel lblScraperThreshold = new JLabel(BUNDLE.getString("Settings.scraperTreshold"));
        panelAutomaticScrape.add(lblScraperThreshold, "cell 1 0,aligny top");

        sliderThreshold = new JSlider();
        sliderThreshold.setMinorTickSpacing(5);
        sliderThreshold.setMajorTickSpacing(10);
        sliderThreshold.setPaintTicks(true);
        sliderThreshold.setPaintLabels(true);
        panelAutomaticScrape.add(sliderThreshold, "cell 2 0,growx,aligny top");

        JTextArea tpScraperThresholdHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.scraperTreshold.hint"));
        TmmFontHelper.changeFont(tpScraperThresholdHint, L2);
        panelAutomaticScrape.add(tpScraperThresholdHint, "cell 1 1 3 1, growx, wmin 0");
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
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
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("scraperFallback");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_2 = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxScraperFallback, jCheckBoxBeanProperty_2);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_2 = BeanProperty.create("capitalWordsInTitles");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_3 = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, chckbxCapitalizeWords, jCheckBoxBeanProperty_3);
    autoBinding_2.bind();
  }
}
