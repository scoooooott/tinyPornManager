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
package org.tinymediamanager.ui.tvshows.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.tvshows.panels.TvShowScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link TvShowScraperSettingsPanel} shows scraper options for the meta data scraper.
 * 
 * @author Manuel Laggner
 */
class TvShowScraperOptionsSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 4999827736720726395L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JComboBox<MediaLanguages>   cbScraperLanguage;
  private JComboBox<CountryCode>      cbCertificationCountry;
  private JCheckBox                   chckbxCapitalizeWords;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  TvShowScraperOptionsSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow,shrink 0]", "[][]15lp![][15lp!][][15lp!][]"));
    {
      JPanel panelOptions = SettingsPanelFactory.createSettingsPanel();

      JLabel lblOptions = new TmmLabel(BUNDLE.getString("Settings.advancedoptions"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelOptions, lblOptions, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); // $NON-NLS-1$
        panelOptions.add(lblScraperLanguage, "cell 1 0 2 1");

        cbScraperLanguage = new JComboBox<>(MediaLanguages.valuesSorted());
        panelOptions.add(cbScraperLanguage, "cell 1 0");

        JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); // $NON-NLS-1$
        panelOptions.add(lblCountry, "cell 1 1 2 1");

        cbCertificationCountry = new JComboBox<>(CountryCode.values());
        panelOptions.add(cbCertificationCountry, "cell 1 1");

        chckbxCapitalizeWords = new JCheckBox(BUNDLE.getString("Settings.scraper.capitalizeWords"));
        panelOptions.add(chckbxCapitalizeWords, "cell 1 2");
      }
    }
    {
      JPanel panelDefaults = SettingsPanelFactory.createSettingsPanel();

      JLabel lblDefaultsT = new TmmLabel(BUNDLE.getString("scraper.metadata.defaults"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelDefaults, lblDefaultsT, true);
      add(collapsiblePanel, "cell 0 2,growx, wmin 0");
      {
        TvShowScraperMetadataPanel scraperMetadataPanel = new TvShowScraperMetadataPanel();
        panelDefaults.add(scraperMetadataPanel, "cell 1 0 2 1,grow");
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
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, MediaLanguages> settingsBeanProperty_8 = BeanProperty.create("scraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<TvShowSettings, MediaLanguages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSettings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("certificationCountry");
    AutoBinding<TvShowSettings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCertificationCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_10 = BeanProperty.create("capitalWordsInTitles");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, chckbxCapitalizeWords, jCheckBoxBeanProperty_1);
    autoBinding_9.bind();
  }
}
