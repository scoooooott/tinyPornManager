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
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.movies.MovieScraper;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link MovieScraperSettingsPanel} shows scrapers to be used for meta data scraping.
 * 
 * @author Manuel Laggner
 */
class MovieScraperSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -299825914193235308L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private MovieSettings               settings         = MovieModuleManager.SETTINGS;
  private List<MovieScraper>          scrapers         = ObservableCollections.observableList(new ArrayList<>());

  /**
   * UI Elements
   */
  private JPanel                      panelScraperOptions;
  private JScrollPane                 scrollPaneScraperDetails;
  private JTextPane                   tpScraperDescription;

  private TmmTable                    tableScraper;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  MovieScraperSettingsPanel() {
    // pre-init
    MediaScraper defaultMediaScraper = MovieList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;

    for (MediaScraper scraper : MovieList.getInstance().getAvailableMediaScrapers()) {
      MovieScraper movieScraper = new MovieScraper(scraper);
      scrapers.add(movieScraper);
    }

    Collections.sort(scrapers);

    for (int i = 0; i < scrapers.size(); i++) {
      MovieScraper scraper = scrapers.get(i);
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

    // select default movie scraper
    if (!scrapers.isEmpty()) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[400lp,grow]", "[400lp,grow]"));
    {
      JPanel panelScraper = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][grow]", "[][shrink 0][150lp:600lp,grow]"));

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

  protected void initDataBindings() {
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
