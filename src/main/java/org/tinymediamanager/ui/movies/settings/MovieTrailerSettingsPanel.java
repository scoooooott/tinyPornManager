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
package org.tinymediamanager.ui.movies.settings;

import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.MovieTrailerQuality;
import org.tinymediamanager.core.movie.MovieTrailerSources;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

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
  private static final long              serialVersionUID = -1607146878528487625L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle    BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$ @wbp.nls.resourceBundle

  private MovieSettings                  settings         = MovieModuleManager.MOVIE_SETTINGS;
  private List<TrailerScraper>           scrapers         = ObservableCollections.observableList(new ArrayList<TrailerScraper>());
  private JTable                         tableTrailerScraper;
  private JTextPane                      tpScraperDescription;
  private JComboBox<MovieTrailerSources> cbTrailerSource;
  private JComboBox<MovieTrailerQuality> cbTrailerQuality;
  private JCheckBox                      checkBox;
  private JCheckBox                      chckbxAutomaticTrailerDownload;
  private JPanel                         panelScraperOptions;

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
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.UNRELATED_GAP_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("100dlu:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.UNRELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));

    final JScrollPane scrollPaneScraperDetails = new JScrollPane();
    scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPaneScraperDetails.setBorder(null);
    panelTrailerScrapers.add(scrollPaneScraperDetails, "8, 1, 1, 2, fill, fill");

    JPanel panelScraperDetails = new JPanel();
    scrollPaneScraperDetails.setViewportView(panelScraperDetails);
    panelScraperDetails.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), },
        new RowSpec[] { RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));
    {
      // add a CSS rule to force body tags to use the default label font
      // instead of the value in javax.swing.text.html.default.csss
      Font font = UIManager.getFont("Label.font");
      String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
      tpScraperDescription = new JTextPane();
      tpScraperDescription.setOpaque(false);
      tpScraperDescription.setEditorKit(new HTMLEditorKit());
      ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);
      panelScraperDetails.add(tpScraperDescription, "1, 1, fill, top");
    }
    panelScraperOptions = new ScrollablePanel();
    panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
    panelScraperDetails.add(panelScraperOptions, "1, 3, fill, top");

    JScrollPane scrollPaneScraper = new JScrollPane();
    panelTrailerScrapers.add(scrollPaneScraper, "2, 2, 5, 1, fill, fill");

    tableTrailerScraper = new JTable();
    tableTrailerScraper.setRowHeight(29);
    scrollPaneScraper.setViewportView(tableTrailerScraper);

    JSeparator separator = new JSeparator();
    panelTrailerScrapers.add(separator, "2, 4, 7, 1");

    checkBox = new JCheckBox(BUNDLE.getString("Settings.trailer.preferred")); //$NON-NLS-1$
    panelTrailerScrapers.add(checkBox, "2, 6, 7, 1");

    JLabel lblTrailerSource = new JLabel(BUNDLE.getString("Settings.trailer.source")); //$NON-NLS-1$
    panelTrailerScrapers.add(lblTrailerSource, "4, 8, right, default");

    cbTrailerSource = new JComboBox<>();
    cbTrailerSource.setModel(new DefaultComboBoxModel<>(MovieTrailerSources.values()));
    panelTrailerScrapers.add(cbTrailerSource, "6, 8, fill, default");

    JLabel lblTrailerQuality = new JLabel(BUNDLE.getString("Settings.trailer.quality")); //$NON-NLS-1$
    panelTrailerScrapers.add(lblTrailerQuality, "4, 10, right, default");

    cbTrailerQuality = new JComboBox<>();
    cbTrailerQuality.setModel(new DefaultComboBoxModel<>(MovieTrailerQuality.values()));
    panelTrailerScrapers.add(cbTrailerQuality, "6, 10, fill, default");

    chckbxAutomaticTrailerDownload = new JCheckBox(BUNDLE.getString("Settings.trailer.automaticdownload")); //$NON-NLS-1$
    panelTrailerScrapers.add(chckbxAutomaticTrailerDownload, "2, 12, 7, 1");

    JLabel lblAutomaticTrailerDownloadHint = new JLabel(BUNDLE.getString("Settings.trailer.automaticdownload.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblAutomaticTrailerDownloadHint, 0.833);
    panelTrailerScrapers.add(lblAutomaticTrailerDownloadHint, "4, 14, 5, 1");

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

    // implement selection listener to load settings
    tableTrailerScraper.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        int index = tableTrailerScraper.convertRowIndexToModel(tableTrailerScraper.getSelectedRow());
        if (index > -1) {
          panelScraperOptions.removeAll();
          if (scrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
            panelScraperOptions.add(new MediaScraperConfigurationPanel(scrapers.get(index).getMediaProvider()));
          }
          panelScraperOptions.revalidate();
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

      BufferedImage scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
          Scalr.OP_ANTIALIAS);
      return new ImageIcon(scaledImage);
    }

    public String getScraperId() {
      return scraper.getId();
    }

    public String getScraperName() {
      return scraper.getName() + " - " + scraper.getVersion();
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

    public IMediaProvider getMediaProvider() {
      return scraper.getMediaProvider();
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
    BeanProperty<JComboBox<MovieTrailerSources>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MovieTrailerSources, JComboBox<MovieTrailerSources>, Object> autoBinding_1 = Bindings
        .createAutoBinding(UpdateStrategy.READ_WRITE, settings, movieSettingsBeanProperty, cbTrailerSource, jComboBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, MovieTrailerQuality> movieSettingsBeanProperty_1 = BeanProperty.create("trailerQuality");
    BeanProperty<JComboBox<MovieTrailerQuality>, Object> jComboBoxBeanProperty_1 = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MovieTrailerQuality, JComboBox<MovieTrailerQuality>, Object> autoBinding_2 = Bindings
        .createAutoBinding(UpdateStrategy.READ_WRITE, settings, movieSettingsBeanProperty_1, cbTrailerQuality, jComboBoxBeanProperty_1);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_2 = BeanProperty.create("useTrailerPreference");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_2, checkBox, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_3 = BeanProperty.create("automaticTrailerDownload");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_3, chckbxAutomaticTrailerDownload, jCheckBoxBeanProperty);
    autoBinding_4.bind();
  }
}
