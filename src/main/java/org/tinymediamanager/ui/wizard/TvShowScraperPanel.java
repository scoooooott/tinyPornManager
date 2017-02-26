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
package org.tinymediamanager.ui.wizard;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.html.HTMLEditorKit;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.settings.TvShowScraperSettingsPanel.TvShowScraper;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

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
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());             //$NON-NLS-1$

  private final TvShowSettings        settings         = TvShowModuleManager.SETTINGS;
  private final List<TvShowScraper>   scrapers         = ObservableCollections.observableList(new ArrayList<TvShowScraper>());

  private JTable                      tableScraper;
  private JComboBox<MediaLanguages>   cbScraperLanguage;
  private JComboBox<CountryCode>      cbCertificationCountry;
  private JTextPane                   tpScraperDescription;
  private JLabel                      lblTvShowScraper;

  public TvShowScraperPanel() {
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
    tableScraper.getModel().addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        // click on the checkbox
        if (arg0.getColumn() == 0) {
          int row = arg0.getFirstRow();
          TvShowScraper changedScraper = scrapers.get(row);
          // if flag default scraper was changed, change all other flags
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

    // select default movie scraper
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  /*
   * init components
   */
  private void initComponents() {
    setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.LINE_GAP_ROWSPEC, }));
    JPanel panelTvShowScrapers = new JPanel();

    add(panelTvShowScrapers, "2, 2, fill, fill");
    panelTvShowScrapers.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("20dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("80dlu:grow"),
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LINE_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow(3)"), FormSpecs.LINE_GAP_ROWSPEC, }));

    lblTvShowScraper = new JLabel(BUNDLE.getString("wizard.tvshow.scraper")); //$NON-NLS-1$
    panelTvShowScrapers.add(lblTvShowScraper, "2, 2, 11, 1");

    JScrollPane scrollPaneScraper = new JScrollPane();
    panelTvShowScrapers.add(scrollPaneScraper, "2, 4, 5, 1, fill, fill");

    tableScraper = new JTable() {
      private static final long serialVersionUID = -144223066269069772L;

      @Override
      public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        String value = getModel().getValueAt(row, 2).toString();
        if (!Globals.isDonator() && value.startsWith("Kodi")) {
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

    JPanel panelScraperDetails = new JPanel();
    panelTvShowScrapers.add(panelScraperDetails, "8, 4, 5, 1, fill, fill");
    panelScraperDetails.setLayout(
        new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, }));

    tpScraperDescription = new JTextPane();
    tpScraperDescription.setOpaque(false);
    tpScraperDescription.setEditorKit(new HTMLEditorKit());
    panelScraperDetails.add(tpScraperDescription, "2, 2, default, top");

    panelTvShowScrapers.add(new JSeparator(), "2, 6, 11, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelTvShowScrapers.add(lblScraperLanguage, "2, 7, right, default");

    cbScraperLanguage = new JComboBox<>();
    cbScraperLanguage.setModel(new DefaultComboBoxModel<>(MediaLanguages.values()));
    panelTvShowScrapers.add(cbScraperLanguage, "4, 7");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelTvShowScrapers.add(lblCountry, "2, 9, right, default");

    cbCertificationCountry = new JComboBox<>();
    cbCertificationCountry.setModel(new DefaultComboBoxModel<>(CountryCode.values()));
    panelTvShowScrapers.add(cbCertificationCountry, "4, 9, fill, default");
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
