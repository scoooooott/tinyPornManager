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
import static org.tinymediamanager.ui.TmmFontHelper.L2;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ItemListener;
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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.TrailerQuality;
import org.tinymediamanager.core.TrailerSources;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.filenaming.TvShowTrailerNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

public class TvShowTrailerSettingsPanel extends JPanel {

  private static final ResourceBundle                     BUNDLE   = ResourceBundle.getBundle("messages", new UTF8Control());

  private TvShowSettings settings = TvShowModuleManager.SETTINGS;
  private List<TvShowTrailerSettingsPanel.TrailerScraper> scrapers = ObservableCollections.observableList(new ArrayList<>());
  private TmmTable tableTrailerScraper;
  private JTextPane tpScraperDescription;
  private JComboBox<TrailerSources> cbTrailerSource;
  private JComboBox<TrailerQuality> cbTrailerQuality;
  private JCheckBox checkBox;
  private JCheckBox chckbxAutomaticTrailerDownload;
  private JPanel panelScraperOptions;
  private JCheckBox cbTrailerFilename1;

  private ItemListener checkBoxListener;

  TvShowTrailerSettingsPanel() {
    checkBoxListener = e -> checkChanges();

    // UI init
    initComponents();
    initDataBindings();

    // implement checkBoxListener for preset events
    settings.addPropertyChangeListener(evt -> {
      if ("preset".equals(evt.getPropertyName())) {
        buildCheckBoxes();
      }
    });

    // data init
    List<String> enabledTrailerProviders = settings.getTrailerScrapers();
    int selectedIndex = -1;
    int counter = 0;
    for (MediaScraper scraper : TvShowList.getInstance().getAvailableTrailerScrapers()) {
      TvShowTrailerSettingsPanel.TrailerScraper trailerScraper = new TvShowTrailerSettingsPanel.TrailerScraper(scraper);
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
        TvShowTrailerSettingsPanel.TrailerScraper changedScraper = scrapers.get(row);
        if (changedScraper.active) {
          settings.addTvShowTrailerScraper(changedScraper.getScraperId());
        } else {
          settings.removeTvShowTrailerScraper(changedScraper.getScraperId());
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

    // select default scraper
    if (selectedIndex < 0) {
      selectedIndex = 0;
    }
    if (counter > 0) {
      tableTrailerScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }

    buildCheckBoxes();

  }

  private void buildCheckBoxes() {
    cbTrailerFilename1.removeItemListener(checkBoxListener);
    clearSelection(cbTrailerFilename1);

    // trailer filenames
    List<TvShowTrailerNaming> trailerFilenames = settings.getTrailerFilenames();
    if (trailerFilenames.contains(TvShowTrailerNaming.TVSHOW_TRAILER)) {
      cbTrailerFilename1.setSelected(true);
    }

    cbTrailerFilename1.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
    }
  }

  private void checkChanges() {
    // set trailer filenames
    settings.clearTrailerFilenames();
    if (cbTrailerFilename1.isSelected()) {
      settings.addTrailerFilename(TvShowTrailerNaming.TVSHOW_TRAILER);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("hidemode 0", "[400lp,grow]", "[][15lp!][]"));
    {
      JPanel panelScraper = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][grow]", "[][shrink 0][]"));

      JLabel lblScraper = new TmmLabel(BUNDLE.getString("scraper.trailer"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelScraper, lblScraper, true);
      add(collapsiblePanel, "cell 0 0,wmin 0,grow");
      {
        tableTrailerScraper = new TmmTable();
        tableTrailerScraper.setRowHeight(29);
        tableTrailerScraper.setShowGrid(true);
        panelScraper.add(tableTrailerScraper, "cell 1 0,grow");

        JSeparator separator = new JSeparator();
        panelScraper.add(separator, "cell 1 1,growx");

        JPanel panelScraperDetails = new ScrollablePanel();
        panelScraper.add(panelScraperDetails, "cell 1 2,grow");
        panelScraperDetails.setLayout(new MigLayout("insets 0", "[grow]", "[][grow]"));

        tpScraperDescription = new ReadOnlyTextPane();
        tpScraperDescription.setEditorKit(new HTMLEditorKit());
        panelScraperDetails.add(tpScraperDescription, "cell 0 0,grow");

        panelScraperOptions = new ScrollablePanel();
        panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelScraperDetails.add(panelScraperOptions, "cell 0 1,grow");
      }
    }
    {
      JPanel panelOptions = SettingsPanelFactory.createSettingsPanel();

      JLabel lblOptionsT = new TmmLabel(BUNDLE.getString("Settings.advancedoptions"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelOptions, lblOptionsT, true);
      add(collapsiblePanel, "cell 0 2,growx, wmin 0");
      {
        checkBox = new JCheckBox(BUNDLE.getString("Settings.trailer.preferred"));
        panelOptions.add(checkBox, "cell 1 0 2 1");

        JLabel lblTrailerSource = new JLabel(BUNDLE.getString("Settings.trailer.source"));
        panelOptions.add(lblTrailerSource, "cell 2 1");

        cbTrailerSource = new JComboBox();
        cbTrailerSource.setModel(new DefaultComboBoxModel<>(TrailerSources.values()));
        panelOptions.add(cbTrailerSource, "cell 2 1");

        JLabel lblTrailerQuality = new JLabel(BUNDLE.getString("Settings.trailer.quality"));
        panelOptions.add(lblTrailerQuality, "cell 2 2");

        cbTrailerQuality = new JComboBox();
        cbTrailerQuality.setModel(new DefaultComboBoxModel<>(TrailerQuality.values()));
        panelOptions.add(cbTrailerQuality, "cell 2 2");

        chckbxAutomaticTrailerDownload = new JCheckBox(BUNDLE.getString("Settings.trailer.automaticdownload"));
        panelOptions.add(chckbxAutomaticTrailerDownload, "cell 1 3 2 1");

        JLabel lblAutomaticTrailerDownloadHint = new JLabel(BUNDLE.getString("Settings.trailer.automaticdownload.hint"));
        panelOptions.add(lblAutomaticTrailerDownloadHint, "cell 2 4");
        TmmFontHelper.changeFont(lblAutomaticTrailerDownloadHint, L2);

        JPanel panelTrailerFilenames = new JPanel();
        panelOptions.add(panelTrailerFilenames, "cell 1 5 2 1");
        panelTrailerFilenames.setLayout(new MigLayout("insets 0", "[][]", "[][]"));

        JLabel lblTrailerFileNaming = new JLabel(BUNDLE.getString("Settings.trailerFileNaming"));
        panelTrailerFilenames.add(lblTrailerFileNaming, "cell 0 0");

        cbTrailerFilename1 = new JCheckBox("tvshow-trailer.ext");
        panelTrailerFilenames.add(cbTrailerFilename1, "cell 1 0");
      }
    }
  }

  protected void initDataBindings() {
    JTableBinding<TvShowTrailerSettingsPanel.TrailerScraper, List<TvShowTrailerSettingsPanel.TrailerScraper>, JTable> jTableBinding = SwingBindings
            .createJTableBinding(AutoBinding.UpdateStrategy.READ_WRITE, scrapers, tableTrailerScraper);
    //
    BeanProperty<TvShowTrailerSettingsPanel.TrailerScraper, Boolean> trailerScraperBeanProperty = BeanProperty.create("active");
    jTableBinding.addColumnBinding(trailerScraperBeanProperty).setColumnName("Active").setColumnClass(Boolean.class);
    //
    BeanProperty<TvShowTrailerSettingsPanel.TrailerScraper, Icon> trailerScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding.addColumnBinding(trailerScraperBeanProperty_1).setColumnName("Logo").setEditable(false).setColumnClass(ImageIcon.class);
    //
    BeanProperty<TvShowTrailerSettingsPanel.TrailerScraper, String> trailerScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding.addColumnBinding(trailerScraperBeanProperty_2).setColumnName("Name").setEditable(false).setColumnClass(String.class);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, tableTrailerScraper,
            jTableBeanProperty, tpScraperDescription, jTextPaneBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSettings, TrailerSources> tvShowSettingsBeanProperty = BeanProperty.create("trailerSource");
    BeanProperty<JComboBox<TrailerSources>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<TvShowSettings, TrailerSources, JComboBox<TrailerSources>, Object> autoBinding_1 = Bindings
            .createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, settings, tvShowSettingsBeanProperty, cbTrailerSource, jComboBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, TrailerQuality> tvShowSettingsBeanProperty_1 = BeanProperty.create("trailerQuality");
    BeanProperty<JComboBox<TrailerQuality>, Object> jComboBoxBeanProperty_1 = BeanProperty.create("selectedItem");
    AutoBinding<TvShowSettings, TrailerQuality, JComboBox<TrailerQuality>, Object> autoBinding_2 = Bindings
            .createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, settings, tvShowSettingsBeanProperty_1, cbTrailerQuality, jComboBoxBeanProperty_1);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_2 = BeanProperty.create("useTrailerPreference");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
            settings, tvShowSettingsBeanProperty_2, checkBox, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_3 = BeanProperty.create("automaticTrailerDownload");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
            settings, tvShowSettingsBeanProperty_3, chckbxAutomaticTrailerDownload, jCheckBoxBeanProperty);
    autoBinding_4.bind();
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public class TrailerScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon scraperLogo;
    private boolean active;

    public TrailerScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      } else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
      }
    }

    private ImageIcon getScaledIcon(ImageIcon original) {
      try {
        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(getFont());

        int height = (int) (fm.getHeight() * 2f);
        int width = original.getIconWidth() / original.getIconHeight() * height;

        BufferedImage scaledImage = Scalr.resize(ImageUtils.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width,
                height, Scalr.OP_ANTIALIAS);
        return new ImageIcon(scaledImage);
      } catch (Exception e) {
        return new ImageIcon();
      }
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
        description = BUNDLE.getString("scraper." + scraper.getId() + ".hint");
      } catch (Exception ignored) {
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
}
