/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.Canvas;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.MovieTrailerQuality;
import org.tinymediamanager.core.movie.MovieTrailerSources;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieTrailerSettingsPanel. To maintain trailer related settings
 * 
 * @author Manuel Laggner
 */
public class MovieTrailerSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = -1607146878528487625L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$ @wbp.nls.resourceBundle

  private MovieSettings        settings = Settings.getInstance().getMovieSettings();
  private List<TrailerScraper> scrapers = ObservableCollections.observableList(new ArrayList<TrailerScraper>());
  private JTable               tableTrailerScraper;
  private JTextPane            tpScraperDescription;
  private JComboBox            cbTrailerSource;
  private JComboBox            cbTrailerQuality;
  private JCheckBox            checkBox;

  public MovieTrailerSettingsPanel() {
    // data init
    List<String> enabledTrailerProviders = settings.getMovieTrailerScrapers();
    int selectedIndex = -1;
    int counter = 0;
    for (MediaScraper scraper : MovieList.getInstance().getAvailableTrailerScrapers()) {
      TrailerScraper trailerScraper = new TrailerScraper(scraper);
      if (enabledTrailerProviders.contains(trailerScraper.getScraperId())) {
        trailerScraper.active = true;
        if (selectedIndex < 0) {
          selectedIndex = counter;
        }
      }
      scrapers.add(trailerScraper);
      counter++;
    }

    // UI init
    setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, }));

    JPanel panelTrailerScrapers = new JPanel();
    panelTrailerScrapers.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("scraper.trailer"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); // $NON-NLS-1$
    add(panelTrailerScrapers, "2, 2, fill, fill");
    panelTrailerScrapers.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("100dlu:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));

    JScrollPane scrollPaneScraper = new JScrollPane();
    panelTrailerScrapers.add(scrollPaneScraper, "2, 2, 3, 1, fill, fill");

    tableTrailerScraper = new JTable();
    tableTrailerScraper.setRowHeight(29);
    scrollPaneScraper.setViewportView(tableTrailerScraper);

    JSeparator separator = new JSeparator();
    panelTrailerScrapers.add(separator, "2, 4, 5, 1");

    checkBox = new JCheckBox(BUNDLE.getString("Settings.trailer.preferred")); //$NON-NLS-1$
    panelTrailerScrapers.add(checkBox, "2, 6, 5, 1");

    JLabel lblTrailerSource = new JLabel(BUNDLE.getString("Settings.trailer.source")); //$NON-NLS-1$
    panelTrailerScrapers.add(lblTrailerSource, "2, 8, right, default");

    cbTrailerSource = new JComboBox(MovieTrailerSources.values());
    panelTrailerScrapers.add(cbTrailerSource, "4, 8, fill, default");

    JLabel lblTrailerQuality = new JLabel(BUNDLE.getString("Settings.trailer.quality")); //$NON-NLS-1$
    panelTrailerScrapers.add(lblTrailerQuality, "2, 10, right, default");

    cbTrailerQuality = new JComboBox(MovieTrailerQuality.values());
    panelTrailerScrapers.add(cbTrailerQuality, "4, 10, fill, default");

    JPanel panelScraperDetails = new JPanel();
    panelTrailerScrapers.add(panelScraperDetails, "6, 2, fill, fill");
    panelScraperDetails.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    tpScraperDescription = new JTextPane();
    tpScraperDescription.setOpaque(false);
    tpScraperDescription.setEditorKit(new HTMLEditorKit());
    panelScraperDetails.add(tpScraperDescription, "2, 2, fill, fill");

    JPanel panelScraperOptions = new JPanel();
    panelScraperDetails.add(panelScraperOptions, "2, 4, fill, fill");
    initDataBindings();

    // adjust table columns
    // Checkbox and Logo shall have minimal width
    TableColumnResizer.setMaxWidthForColumn(tableTrailerScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableTrailerScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableTrailerScraper, 5);

    tableTrailerScraper.getModel().addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        // click on the checkbox
        if (arg0.getColumn() == 0) {
          int row = arg0.getFirstRow();
          TrailerScraper changedScraper = scrapers.get(row);
          if (changedScraper.active) {
            settings.addMovieTrailerScraper(changedScraper.getScraperId());
          }
          else {
            settings.removeMovieTrailerScraper(changedScraper.getScraperId());
          }
        }
      }
    });

    // select default movie scraper
    if (selectedIndex < 0) {
      selectedIndex = 0;
    }
    if (counter > 0) {
      tableTrailerScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public class TrailerScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      active;

    public TrailerScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
      }
    }

    private ImageIcon getScaledIcon(ImageIcon original) {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(getFont());

      int height = (int) (fm.getHeight() * 2f);
      int width = original.getIconWidth() / original.getIconHeight() * height;

      BufferedImage scaledImage = Scalr.resize(com.bric.image.ImageLoader.createImage(original.getImage()), Scalr.Method.QUALITY,
          Scalr.Mode.AUTOMATIC, width, height, Scalr.OP_ANTIALIAS);
      return new ImageIcon(scaledImage);
    }

    public String getScraperId() {
      return scraper.getId();
    }

    public String getScraperName() {
      return scraper.getName();
    }

    public String getScraperDescription() {
      // first try to get the localized version
      String description = null;
      try {
        description = BUNDLE.getString("scraper." + scraper.getId() + ".hint"); //$NON-NLS-1$
      }
      catch (Exception ignored) {
      }

      if (StringUtils.isBlank(description)) {
        // try to get a scraper text
        description = scraper.getDescription();
      }

      return description;
    }

    public Icon getScraperLogo() {
      return scraperLogo;
    }

    public Boolean getActive() {
      return active;
    }

    public void setActive(Boolean newValue) {
      Boolean oldValue = this.active;
      this.active = newValue;
      firePropertyChange("active", oldValue, newValue);
    }
  }

  protected void initDataBindings() {
    JTableBinding<TrailerScraper, List<TrailerScraper>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, scrapers,
        tableTrailerScraper);
    //
    BeanProperty<TrailerScraper, Boolean> trailerScraperBeanProperty = BeanProperty.create("active");
    jTableBinding.addColumnBinding(trailerScraperBeanProperty).setColumnName("Active").setColumnClass(Boolean.class);
    //
    BeanProperty<TrailerScraper, Icon> trailerScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding.addColumnBinding(trailerScraperBeanProperty_1).setColumnName("Logo").setEditable(false).setColumnClass(ImageIcon.class);
    //
    BeanProperty<TrailerScraper, String> trailerScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding.addColumnBinding(trailerScraperBeanProperty_2).setColumnName("Name").setEditable(false).setColumnClass(String.class);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tableTrailerScraper,
        jTableBeanProperty, tpScraperDescription, jTextPaneBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, MovieTrailerSources> movieSettingsBeanProperty = BeanProperty.create("trailerSource");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MovieTrailerSources, JComboBox, Object> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty, cbTrailerSource, jComboBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, MovieTrailerQuality> movieSettingsBeanProperty_1 = BeanProperty.create("trailerQuality");
    AutoBinding<MovieSettings, MovieTrailerQuality, JComboBox, Object> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_1, cbTrailerQuality, jComboBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_2 = BeanProperty.create("useTrailerPreference");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_2, checkBox, jCheckBoxBeanProperty);
    autoBinding_3.bind();
  }
}
