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
package org.tinymediamanager.ui.wizard;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.html.HTMLEditorKit;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;
import org.tinymediamanager.ui.tvshows.TvShowScraper;

import net.miginfocom.swing.MigLayout;

/**
 * The class TvShowScraperPanel is used to maintain the TV show scraper in the wizard
 * 
 * @author Manuel Laggner*
 */
class TvShowScraperPanel extends JPanel {
  private static final long           serialVersionUID = -2639391458779374972L;

  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private final TvShowSettings        settings         = TvShowModuleManager.SETTINGS;
  private final List<TvShowScraper>   scrapers         = ObservableCollections.observableList(new ArrayList<>());

  private JTable                      tableScraper;
  private JComboBox<MediaLanguages>   cbScraperLanguage;
  private JComboBox<CountryCode>      cbCertificationCountry;
  private JTextPane                   tpScraperDescription;
  private JPanel                      panelScraperOptions;

  TvShowScraperPanel() {
    // data init before UI init
    MediaScraper defaultMediaScraper = TvShowList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;
    int counter = 0;
    for (MediaScraper scraper : TvShowList.getInstance().getAvailableMediaScrapers()) {
      TvShowScraper tvShowScraper = new TvShowScraper(scraper);
      if (scraper.equals(defaultMediaScraper)) {
        tvShowScraper.setDefaultScraper(true);
        selectedIndex = counter;
      }
      scrapers.add(tvShowScraper);
      counter++;
    }

    // UI init
    initComponents();
    initDataBindings();

    // init data after UI init
    // adjust table columns
    // Checkbox and Logo shall have minimal width
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableScraper, 5);

    // implement listener to simulate button group
    tableScraper.getModel().addTableModelListener(arg0 -> {
      // click on the checkbox
      if (arg0.getColumn() == 0) {
        int row = arg0.getFirstRow();
        TvShowScraper changedScraper = scrapers.get(row);
        // if flag default scraper was changed, change all other flags
        if (changedScraper.getDefaultScraper()) {
          settings.setScraper(changedScraper.getScraperId());
          for (TvShowScraper scraper : scrapers) {
            if (scraper != changedScraper) {
              scraper.setDefaultScraper(Boolean.FALSE);
            }
          }
        }
      }
    });

    // implement selection listener to load settings
    tableScraper.getSelectionModel().addListSelectionListener(e -> {
      int index = tableScraper.convertRowIndexToModel(tableScraper.getSelectedRow());
      if (index > -1) {
        panelScraperOptions.removeAll();
        if (scrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
          panelScraperOptions.add(new MediaScraperConfigurationPanel(scrapers.get(index).getMediaProvider()));
        }
        panelScraperOptions.revalidate();
      }
    });

    // select default movie scraper
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  /*
   * init components
   */
  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][400lp,grow]"));

    JLabel lblTvShowScraper = new JLabel(BUNDLE.getString("wizard.tvshow.scraper"));
    add(lblTvShowScraper, "cell 0 0");
    JPanel panelTvShowScrapers = new JPanel();

    add(panelTvShowScrapers, "cell 0 1,grow");
    panelTvShowScrapers.setLayout(new MigLayout("", "[][][500lp,grow]", "[150lp:300lp][][]"));

    JScrollPane scrollPaneScraper = new JScrollPane();
    panelTvShowScrapers.add(scrollPaneScraper, "cell 0 0 2 1,grow");

    tableScraper = new TmmTable();
    tableScraper.setRowHeight(29);
    scrollPaneScraper.setViewportView(tableScraper);

    {
      JScrollPane scrollPaneScraperDetails = new JScrollPane();
      panelTvShowScrapers.add(scrollPaneScraperDetails, "cell 2 0 1 2,grow");
      scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPaneScraperDetails.setBorder(null);

      JPanel panelScraperDetails = new ScrollablePanel();
      scrollPaneScraperDetails.setViewportView(panelScraperDetails);
      panelScraperDetails.setLayout(new MigLayout("", "[grow]", "[][]"));

      tpScraperDescription = new JTextPane();
      tpScraperDescription.setOpaque(false);
      tpScraperDescription.setEditorKit(new HTMLEditorKit());
      panelScraperDetails.add(tpScraperDescription, "cell 0 0,growx,aligny top");

      panelScraperOptions = new JPanel();
      panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
      panelScraperDetails.add(panelScraperOptions, "cell 0 1,growx,aligny top");
    }

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage"));
    panelTvShowScrapers.add(lblScraperLanguage, "cell 0 1");

    cbScraperLanguage = new JComboBox<>();
    cbScraperLanguage.setModel(new DefaultComboBoxModel<>(MediaLanguages.valuesSorted()));
    panelTvShowScrapers.add(cbScraperLanguage, "cell 1 1");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry"));
    panelTvShowScrapers.add(lblCountry, "cell 0 2");

    cbCertificationCountry = new JComboBox<>();
    cbCertificationCountry.setModel(new DefaultComboBoxModel<>(CountryCode.values()));
    panelTvShowScrapers.add(cbCertificationCountry, "cell 1 2");
  }

  /*
   * init data bindings
   */

  @SuppressWarnings("rawtypes")
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
  }
}
