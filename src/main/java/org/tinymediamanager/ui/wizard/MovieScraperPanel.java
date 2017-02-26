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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
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
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.settings.MovieScraperSettingsPanel.MovieScraper;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class MovieScraperPanel is used to maintain the movie scrapers in the wizard
 * 
 * @author Manuel Laggner
 */
class MovieScraperPanel extends JPanel {
  private static final long           serialVersionUID = 405588171648074608L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  private final MovieSettings         settings         = MovieModuleManager.MOVIE_SETTINGS;
  private final List<MovieScraper>    scrapers         = ObservableCollections.observableList(new ArrayList<MovieScraper>());

  private JTable                      tableScraper;
  private JComboBox<MediaLanguages>   cbScraperLanguage;
  private JComboBox<CountryCode>      cbCertificationCountry;
  private JTextPane                   tpScraperDescription;
  private JCheckBox                   chckbxScraperFallback;
  private JCheckBox                   cbMovieNfoFilename1;
  private JCheckBox                   cbMovieNfoFilename2;
  private JCheckBox                   cbMovieNfoFilename3;
  private JLabel                      lblMovieScraper;

  public MovieScraperPanel() {
    // data init before UI init
    MediaScraper defaultMediaScraper = MovieList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;
    int counter = 0;
    for (MediaScraper scraper : MovieList.getInstance().getAvailableMediaScrapers()) {
      MovieScraper movieScraper = new MovieScraper(scraper);
      if (scraper.equals(defaultMediaScraper)) {
        movieScraper.setDefaultScraper(true);
        selectedIndex = counter;
      }
      scrapers.add(movieScraper);
      counter++;
    }

    // UI init
    initComponents();
    initDataBindings();

    // init data after UI init
    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getMovieNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.DISC_NFO)) {
      cbMovieNfoFilename3.setSelected(true);
    }

    // item listener
    cbMovieNfoFilename1.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbMovieNfoFilename3.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbMovieNfoFilename2.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });

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
    JPanel panelMovieScrapers = new JPanel();

    add(panelMovieScrapers, "2, 2, fill, fill");
    panelMovieScrapers.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("80dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50dlu:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("120dlu:grow"),
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.LINE_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LINE_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LINE_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LINE_GAP_ROWSPEC, }));

    lblMovieScraper = new JLabel(BUNDLE.getString("wizard.movie.scraper")); //$NON-NLS-1$
    panelMovieScrapers.add(lblMovieScraper, "2, 2, 11, 1");

    JScrollPane scrollPaneScraper = new JScrollPane();
    panelMovieScrapers.add(scrollPaneScraper, "2, 4, 5, 1, fill, fill");

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
    panelMovieScrapers.add(panelScraperDetails, "8, 4, 5, 1, fill, fill");
    panelScraperDetails.setLayout(
        new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, }));

    tpScraperDescription = new JTextPane();
    tpScraperDescription.setOpaque(false);
    tpScraperDescription.setEditorKit(new HTMLEditorKit());
    panelScraperDetails.add(tpScraperDescription, "2, 2, default, top");

    panelMovieScrapers.add(new JSeparator(), "2, 6, 11, 1");

    JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
    panelMovieScrapers.add(lblScraperLanguage, "2, 7, right, default");

    cbScraperLanguage = new JComboBox<>();
    cbScraperLanguage.setModel(new DefaultComboBoxModel<>(MediaLanguages.values()));
    panelMovieScrapers.add(cbScraperLanguage, "4, 7");

    JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); //$NON-NLS-1$
    panelMovieScrapers.add(lblCountry, "8, 7, right, default");

    cbCertificationCountry = new JComboBox<>();
    cbCertificationCountry.setModel(new DefaultComboBoxModel<>(CountryCode.values()));
    panelMovieScrapers.add(cbCertificationCountry, "10, 7, fill, default");

    chckbxScraperFallback = new JCheckBox(BUNDLE.getString("Settings.scraperfallback")); //$NON-NLS-1$
    panelMovieScrapers.add(chckbxScraperFallback, "2, 9, 11, 1");
    panelMovieScrapers.add(new JSeparator(), "2, 11, 11, 1");

    JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming")); //$NON-NLS-1$
    panelMovieScrapers.add(lblNfoFileNaming, "2, 13");

    cbMovieNfoFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".nfo"); //$NON-NLS-1$
    panelMovieScrapers.add(cbMovieNfoFilename1, "4, 13, 3, 1");

    cbMovieNfoFilename3 = new JCheckBox(BUNDLE.getString("Settings.nfo.discstyle")); //$NON-NLS-1$
    panelMovieScrapers.add(cbMovieNfoFilename3, "8, 13, 3, 3, default, center");

    cbMovieNfoFilename2 = new JCheckBox("movie.nfo");
    panelMovieScrapers.add(cbMovieNfoFilename2, "4, 15, 3, 1");
  }

  /*
   * init data bindings
   */

  @SuppressWarnings("rawtypes")
  protected void initDataBindings() {
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
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("scraperFallback");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxScraperFallback, jCheckBoxBeanProperty);
    autoBinding_1.bind();
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

  private void checkChanges() {
    // set NFO filenames
    settings.clearMovieNfoFilenames();
    if (cbMovieNfoFilename1.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
    }
    if (cbMovieNfoFilename2.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.MOVIE_NFO);
    }
    if (cbMovieNfoFilename3.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.DISC_NFO);
    }
  }
}
