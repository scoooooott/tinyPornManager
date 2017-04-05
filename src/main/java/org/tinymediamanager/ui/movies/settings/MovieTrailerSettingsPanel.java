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
import java.awt.Color;
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
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
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
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieTrailerSettingsPanel. To maintain trailer related settings
 *
 * @author Manuel Laggner
 */
public class MovieTrailerSettingsPanel extends ScrollablePanel {
  private static final long              serialVersionUID = -1607146878528487625L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle    BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$

  private MovieSettings                  settings         = MovieModuleManager.SETTINGS;
  private List<TrailerScraper>           scrapers         = ObservableCollections.observableList(new ArrayList<TrailerScraper>());
  private TmmTable                       tableTrailerScraper;
  private JTextPane                      tpScraperDescription;
  private JComboBox<MovieTrailerSources> cbTrailerSource;
  private JComboBox<MovieTrailerQuality> cbTrailerQuality;
  private JCheckBox                      checkBox;
  private JCheckBox                      chckbxAutomaticTrailerDownload;
  private JPanel                         panelScraperOptions;

  public MovieTrailerSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();

    // data init
    List<String> enabledTrailerProviders = settings.getTrailerScrapers();
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

    // adjust table columns
    // Checkbox and Logo shall have minimal width
    TableColumnResizer.setMaxWidthForColumn(tableTrailerScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableTrailerScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableTrailerScraper, 5);

    tableTrailerScraper.getModel().addTableModelListener(arg0 -> {
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
    });

    // implement selection listener to load settings
    tableTrailerScraper.getSelectionModel().addListSelectionListener(e -> {
      int index = tableTrailerScraper.convertRowIndexToModel(tableTrailerScraper.getSelectedRow());
      if (index > -1) {
        panelScraperOptions.removeAll();
        if (scrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
          panelScraperOptions.add(new MediaScraperConfigurationPanel(scrapers.get(index).getMediaProvider()));
        }
        panelScraperOptions.revalidate();
      }
    });

    {
      // add a CSS rule to force body tags to use the default label font
      // instead of the value in javax.swing.text.html.default.csss
      Font font = UIManager.getFont("Label.font");
      Color color = UIManager.getColor("Label.foreground");
      String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; color: rgb(" + color.getRed() + ","
          + color.getGreen() + "," + color.getBlue() + "); }";
      tpScraperDescription.setEditorKit(new HTMLEditorKit());
      ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);
    }

    // select default movie scraper
    if (selectedIndex < 0) {
      selectedIndex = 0;
    }
    if (counter > 0) {
      tableTrailerScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][20lp][][][500lp,grow]", "[][200lp][][][][][]"));
    {
      JLabel lblScraperT = new JLabel(BUNDLE.getString("scraper.trailer")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperT, 1.16667, Font.BOLD);
      add(lblScraperT, "cell 0 0 5 1");
    }
    {
      tableTrailerScraper = new TmmTable();
      tableTrailerScraper.setRowHeight(29);

      JScrollPane scrollPaneScraper = new JScrollPane(tableTrailerScraper);
      tableTrailerScraper.configureScrollPane(scrollPaneScraper);
      add(scrollPaneScraper, "cell 1 1 3 1,growy");
    }
    {
      JScrollPane scrollPaneScraperDetails = new JScrollPane();
      add(scrollPaneScraperDetails, "cell 4 1 1 4,grow");
      scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPaneScraperDetails.setBorder(null);

      JPanel panelScraperDetails = new ScrollablePanel();
      scrollPaneScraperDetails.setViewportView(panelScraperDetails);
      panelScraperDetails.setLayout(new MigLayout("", "[grow]", "[][]"));
      tpScraperDescription = new JTextPane();
      tpScraperDescription.setOpaque(false);

      panelScraperDetails.add(tpScraperDescription, "cell 0 0,growx,aligny top");
      panelScraperOptions = new ScrollablePanel();
      panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
      panelScraperDetails.add(panelScraperOptions, "cell 0 1,growx,aligny top");
    }
    {
      checkBox = new JCheckBox(BUNDLE.getString("Settings.trailer.preferred")); //$NON-NLS-1$
      add(checkBox, "cell 1 2 3 1");

      JLabel lblTrailerSource = new JLabel(BUNDLE.getString("Settings.trailer.source")); //$NON-NLS-1$
      add(lblTrailerSource, "cell 2 3,alignx right");

      cbTrailerSource = new JComboBox<>();
      add(cbTrailerSource, "cell 3 3");
      cbTrailerSource.setModel(new DefaultComboBoxModel<>(MovieTrailerSources.values()));

      JLabel lblTrailerQuality = new JLabel(BUNDLE.getString("Settings.trailer.quality")); //$NON-NLS-1$
      add(lblTrailerQuality, "cell 2 4,alignx right");

      cbTrailerQuality = new JComboBox<>();
      add(cbTrailerQuality, "cell 3 4");
      cbTrailerQuality.setModel(new DefaultComboBoxModel<>(MovieTrailerQuality.values()));
    }
    {
      chckbxAutomaticTrailerDownload = new JCheckBox(BUNDLE.getString("Settings.trailer.automaticdownload")); //$NON-NLS-1$
      add(chckbxAutomaticTrailerDownload, "cell 1 5 3 1");

      JLabel lblAutomaticTrailerDownloadHint = new JLabel(BUNDLE.getString("Settings.trailer.automaticdownload.hint")); //$NON-NLS-1$
      add(lblAutomaticTrailerDownloadHint, "cell 2 6 2 1");
      TmmFontHelper.changeFont(lblAutomaticTrailerDownloadHint, 0.833);
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
