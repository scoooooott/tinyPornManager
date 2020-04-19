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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
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
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;
import org.tinymediamanager.ui.tvshows.TvShowScraper;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link TvShowScraperSettingsPanel} shows scrapers to be used for meta data scraping.
 * 
 * @author Manuel Laggner
 */
class TvShowScraperSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 4999827736720726395L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private List<TvShowScraper>         scrapers         = ObservableCollections.observableList(new ArrayList<>());

  /** UI components */
  private TmmTable                    tableScraper;
  private JTextPane                   tpScraperDescription;
  private JScrollPane                 scrollPaneScraperDetails;
  private JPanel                      panelScraperOptions;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  TvShowScraperSettingsPanel() {

    // pre-init
    MediaScraper defaultMediaScraper = TvShowList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;
    for (MediaScraper scraper : TvShowList.getInstance().getAvailableMediaScrapers()) {
      TvShowScraper tvShowScraper = new TvShowScraper(scraper);
      scrapers.add(tvShowScraper);
    }

    Collections.sort(scrapers);

    for (int i = 0; i < scrapers.size(); i++) {
      TvShowScraper scraper = scrapers.get(i);
      if (scraper.getMediaScraper().equals(defaultMediaScraper)) {
        scraper.setDefaultScraper(true);
        selectedIndex = i;
        break;
      }
    }

    // UI init
    initComponents();
    initDataBindings();

    // data init

    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    Color color = UIManager.getColor("Label.foreground");
    String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: rgb(" + color.getRed() + ","
        + color.getGreen() + "," + color.getBlue() + "); }";
    ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);

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
        // if flag inNFO was changed, change all other trailers flags
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
        scrollPaneScraperDetails.revalidate();
        scrollPaneScraperDetails.repaint();
      }
    });

    // select default TV show scraper
    if (!scrapers.isEmpty()) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[400lp,grow]", "[400lp,grow]"));
    {
      JPanel panelScraper = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][grow]", "[][shrink 0][200lp:600lp,grow]"));

      JLabel lblScraper = new TmmLabel(BUNDLE.getString("scraper.metadata"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelScraper, lblScraper, true);
      add(collapsiblePanel, "cell 0 0,wmin 0,grow");
      {
        tableScraper = new TmmTable();
        tableScraper.setRowHeight(29);
        tableScraper.setShowGrid(true);
        panelScraper.add(tableScraper, "cell 1 0,grow");

        JSeparator separator = new JSeparator();
        panelScraper.add(separator, "cell 1 1,growx");

        scrollPaneScraperDetails = new JScrollPane();
        panelScraper.add(scrollPaneScraperDetails, "cell 1 2,grow");

        scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneScraperDetails.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneScraperDetails.setBorder(null);

        JPanel panelScraperDetails = new ScrollablePanel();
        scrollPaneScraperDetails.setViewportView(panelScraperDetails);
        panelScraperDetails.setLayout(new MigLayout("insets 0", "[grow]", "[][grow]"));

        tpScraperDescription = new ReadOnlyTextPane();
        tpScraperDescription.setEditorKit(new HTMLEditorKit());
        panelScraperDetails.add(tpScraperDescription, "cell 0 0,grow");

        panelScraperOptions = new JPanel();
        panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelScraperDetails.add(panelScraperOptions, "cell 0 1,grow");
      }
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  protected void initDataBindings() {
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
