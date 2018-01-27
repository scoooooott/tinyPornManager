/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
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
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieSubtitleSettingsPanel. To maintain subtitle related settings
 * 
 * @author Manuel Laggner
 */
public class MovieSubtitleSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -1607146878528487625L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());               //$NON-NLS-1$ @wbp.nls.resourceBundle

  private MovieSettings               settings         = MovieModuleManager.SETTINGS;
  private List<SubtitleScraper>       scrapers         = ObservableCollections.observableList(new ArrayList<SubtitleScraper>());
  private TmmTable                    tableScraper;
  private JTextPane                   tpScraperDescription;
  private JPanel                      panelScraperOptions;
  private JComboBox                   cbScraperLanguage;
  private JComboBox<LanguageStyle>    cbSubtitleLanguageStyle;

  public MovieSubtitleSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();

    // data init
    List<String> enabledSubtitleProviders = settings.getSubtitleScrapers();
    int selectedIndex = -1;
    int counter = 0;
    for (MediaScraper scraper : MovieList.getInstance().getAvailableSubtitleScrapers()) {
      SubtitleScraper subtitleScraper = new SubtitleScraper(scraper);
      if (enabledSubtitleProviders.contains(subtitleScraper.getScraperId())) {
        subtitleScraper.active = true;
        if (selectedIndex < 0) {
          selectedIndex = counter;
        }
      }
      scrapers.add(subtitleScraper);
      counter++;
    }

    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    Color color = UIManager.getColor("Label.foreground");
    String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; color: rgb(" + color.getRed() + ","
        + color.getGreen() + "," + color.getBlue() + "); }";
    tpScraperDescription.setEditorKit(new HTMLEditorKit());
    ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);

    // adjust table columns
    // Checkbox and Logo shall have minimal width
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableScraper, 5);

    tableScraper.getModel().addTableModelListener(arg0 -> {
      // click on the checkbox
      if (arg0.getColumn() == 0) {
        int row = arg0.getFirstRow();
        SubtitleScraper changedScraper = scrapers.get(row);
        if (changedScraper.active) {
          settings.addMovieSubtitleScraper(changedScraper.getScraperId());
        }
        else {
          settings.removeMovieSubtitleScraper(changedScraper.getScraperId());
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

    // select default subtitle scraper
    if (selectedIndex < 0) {
      selectedIndex = 0;
    }
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][][grow]", "[][200lp][grow][20lp,shrink 0][][]"));
    {
      final JLabel lblScraperT = new JLabel(BUNDLE.getString("scraper.subtitle"));// $NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperT, 1.16667, Font.BOLD);
      add(lblScraperT, "cell 0 0 3 1");
    }
    {
      tableScraper = new TmmTable();
      tableScraper.setRowHeight(29);

      JScrollPane scrollPaneScraper = new JScrollPane(tableScraper);
      tableScraper.configureScrollPane(scrollPaneScraper);
      add(scrollPaneScraper, "cell 1 1 2 1,grow");
    }
    {
      JScrollPane scrollPaneScraperDetails = new JScrollPane();
      add(scrollPaneScraperDetails, "cell 1 2 2 1,grow");
      scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPaneScraperDetails.setBorder(null);

      JPanel panelScraperDetails = new ScrollablePanel();
      scrollPaneScraperDetails.setViewportView(panelScraperDetails);
      tpScraperDescription = new JTextPane();
      tpScraperDescription.setOpaque(false);

      panelScraperDetails.setLayout(new MigLayout("", "[grow]", "[][]"));
      panelScraperDetails.add(tpScraperDescription, "cell 0 0,growx,aligny top");
      panelScraperOptions = new ScrollablePanel();
      panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
      panelScraperDetails.add(panelScraperOptions, "cell 0 1,growx,aligny top");
    }
    {
      JSeparator separator = new JSeparator();
      add(separator, "cell 1 3 2 1,growx");
    }

    {
      JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); //$NON-NLS-1$
      add(lblScraperLanguage, "flowx,cell 1 4,alignx left");

      cbScraperLanguage = new JComboBox(MediaLanguages.values());
      add(cbScraperLanguage, "cell 1 4");
    }
    {
      JLabel lblSubtitleLanguageStyle = new JLabel(BUNDLE.getString("Settings.renamer.language")); //$NON-NLS-1$
      add(lblSubtitleLanguageStyle, "flowx,cell 1 5,alignx left");

      cbSubtitleLanguageStyle = new JComboBox(LanguageStyle.values());
      add(cbSubtitleLanguageStyle, "cell 1 5");
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public class SubtitleScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      active;

    public SubtitleScraper(MediaScraper scraper) {
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

      BufferedImage scaledImage = Scalr.resize(ImageUtils.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
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
    JTableBinding<MovieSubtitleSettingsPanel.SubtitleScraper, List<MovieSubtitleSettingsPanel.SubtitleScraper>, JTable> jTableBinding = SwingBindings
        .createJTableBinding(UpdateStrategy.READ_WRITE, scrapers, tableScraper);
    //
    BeanProperty<MovieSubtitleSettingsPanel.SubtitleScraper, Boolean> subtitleScraperBeanProperty = BeanProperty.create("active");
    jTableBinding.addColumnBinding(subtitleScraperBeanProperty).setColumnName("Active").setColumnClass(Boolean.class);
    //
    BeanProperty<MovieSubtitleSettingsPanel.SubtitleScraper, Icon> subtitleScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding.addColumnBinding(subtitleScraperBeanProperty_1).setColumnName("Logo").setEditable(false).setColumnClass(ImageIcon.class);
    //
    BeanProperty<MovieSubtitleSettingsPanel.SubtitleScraper, String> subtitleScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding.addColumnBinding(subtitleScraperBeanProperty_2).setColumnName("Name").setEditable(false).setColumnClass(String.class);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tableScraper, jTableBeanProperty,
        tpScraperDescription, jTextPaneBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, MediaLanguages> movieScraperBeanProperty = BeanProperty.create("subtitleScraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MediaLanguages, JComboBox, Object> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieScraperBeanProperty, cbScraperLanguage, jComboBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, LanguageStyle> movieScraperBeanProperty_1 = BeanProperty.create("subtitleLanguageStyle");
    AutoBinding<MovieSettings, LanguageStyle, JComboBox, Object> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieScraperBeanProperty_1, cbSubtitleLanguageStyle, jComboBoxBeanProperty);
    autoBinding_2.bind();
  }
}
