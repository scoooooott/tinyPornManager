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
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.TvShowScrapers;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowScraperSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowScraperSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 4999827736720726395L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  /** UI components */
  private ButtonGroup                 buttonGroupScraper;
  private JComboBox                   cbScraperTmdbLanguage;
  private JComboBox                   cbCountry;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JPanel                      panelScraperMetadata;
  private JPanel                      panelScraperMetadataContainer;
  private JCheckBox                   cbScraperTvdb;
  private JCheckBox                   chckbxAnidb;
  private JPanel                      panelArtworkScrapers;
  private JCheckBox                   chckbxImagesFanartTv;
  private JCheckBox                   chckbxImagesTvDB;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public TvShowScraperSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    JPanel panelTvShowScrapers = new JPanel();
    panelTvShowScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.metadata.defaults"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelTvShowScrapers, "2, 2, fill, top");
    panelTvShowScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    cbScraperTvdb = new JCheckBox("The TV Database");
    buttonGroupScraper = new ButtonGroup();
    buttonGroupScraper.add(cbScraperTvdb);
    panelTvShowScrapers.add(cbScraperTvdb, "1, 2");

    chckbxAnidb = new JCheckBox("AniDB");
    buttonGroupScraper.add(chckbxAnidb);
    panelTvShowScrapers.add(chckbxAnidb, "1, 4");

    JSeparator separator = new JSeparator();
    panelTvShowScrapers.add(separator, "1, 6, 3, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelTvShowScrapers.add(lblScraperLanguage, "1, 8, right, default");

    cbScraperTmdbLanguage = new JComboBox(MediaLanguages.values());
    panelTvShowScrapers.add(cbScraperTmdbLanguage, "3, 8");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelTvShowScrapers.add(lblCountry, "1, 10, right, default");

    cbCountry = new JComboBox(CountryCode.values());
    panelTvShowScrapers.add(cbCountry, "3, 10, fill, default");

    panelArtworkScrapers = new JPanel();
    panelArtworkScrapers.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.images"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelArtworkScrapers, "2, 4, fill, fill");
    panelArtworkScrapers.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    chckbxImagesTvDB = new JCheckBox("The TV Database");
    panelArtworkScrapers.add(chckbxImagesTvDB, "2, 2");

    chckbxImagesFanartTv = new JCheckBox("Fanart.tv");
    panelArtworkScrapers.add(chckbxImagesFanartTv, "2, 4");

    panelScraperMetadataContainer = new JPanel();
    panelScraperMetadataContainer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE
        .getString("scraper.metadata.defaults"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51))); //$NON-NLS-1$
    add(panelScraperMetadataContainer, "2, 6, fill, top");
    panelScraperMetadataContainer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    panelScraperMetadata = new TvShowScraperMetadataPanel(settings.getTvShowScraperMetadataConfig());
    panelScraperMetadataContainer.add(panelScraperMetadata, "1, 1, 2, 1, fill, default");

    chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape")); //$NON-NLS-1$
    panelScraperMetadataContainer.add(chckbxAutomaticallyScrapeImages, "2, 3");

    initDataBindings();

    // set movie Scrapers
    TvShowScrapers scraper = settings.getTvShowSettings().getTvShowScraper();
    switch (scraper) {
      case ANIDB:
        chckbxAnidb.setSelected(true);
        break;
      case TVDB:
      default:
        cbScraperTvdb.setSelected(true);
    }
    cbScraperTvdb.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    chckbxAnidb.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent arg0) {
        checkChanges();
      }
    });
  }

  /**
   * Check changes.
   */
  public void checkChanges() {
    // save scraper
    if (cbScraperTvdb.isSelected()) {
      settings.getTvShowSettings().setTvShowScraper(TvShowScrapers.TVDB);
    }
    if (chckbxAnidb.isSelected()) {
      settings.getTvShowSettings().setTvShowScraper(TvShowScrapers.ANIDB);
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, MediaLanguages> settingsBeanProperty_8 = BeanProperty.create("tvShowSettings.scraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, MediaLanguages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperTmdbLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("tvShowSettings.certificationCountry");
    AutoBinding<Settings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("tvShowSettings.scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("tvShowSettings.imageScraperTvdb");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxImagesTvDB, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_2 = BeanProperty.create("tvShowSettings.imageScraperFanartTv");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, chckbxImagesFanartTv, jCheckBoxBeanProperty);
    autoBinding_2.bind();
  }
}
