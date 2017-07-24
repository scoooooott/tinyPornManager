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
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
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
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieScraperSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieScraperSettingsPanel extends ScrollablePanel {
  private static final long                    serialVersionUID = -299825914193235308L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle          BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  private MovieSettings                        settings         = MovieModuleManager.SETTINGS;
  private List<MovieScraper>                   scrapers         = ObservableCollections.observableList(new ArrayList<MovieScraper>());

  /**
   * UI Elements
   */
  private JComboBox                            cbScraperLanguage;
  private JComboBox                            cbCertificationCountry;
  private JCheckBox                            chckbxScraperFallback;
  private JComboBox<MovieConnectors>           cbNfoFormat;
  private JCheckBox                            cbMovieNfoFilename1;
  private JCheckBox                            cbMovieNfoFilename2;
  private JCheckBox                            cbMovieNfoFilename3;
  private JComboBox<CertificationStyleWrapper> cbCertificationStyle;
  private JPanel                               panelScraperOptions;
  private JTextPane                            tpScraperDescription;
  private JSlider                              sliderThreshold;
  private JCheckBox                            chckbxAutomaticallyScrapeImages;
  private JCheckBox                            chckbxImageLanguage;
  private JCheckBox                            chckbxWriteCleanNfo;

  private TmmTable                             tableScraper;

  private ItemListener                         checkBoxListener;
  private ItemListener                         comboBoxListener;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public MovieScraperSettingsPanel() {
    checkBoxListener = e -> checkChanges();
    comboBoxListener = e -> checkChanges();

    // pre-init
    MediaScraper defaultMediaScraper = MovieList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;
    int counter = 0;
    for (MediaScraper scraper : MovieList.getInstance().getAvailableMediaScrapers()) {
      MovieScraper movieScraper = new MovieScraper(scraper);
      if (scraper.equals(defaultMediaScraper)) {
        movieScraper.defaultScraper = true;
        selectedIndex = counter;
      }
      scrapers.add(movieScraper);
      counter++;
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
        panelScraperOptions.revalidate();
      }
    });

    // select default movie scraper
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }

    Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
    labelTable.put(100, new JLabel("1.0"));
    labelTable.put(75, new JLabel("0.75"));
    labelTable.put(50, new JLabel("0.50"));
    labelTable.put(25, new JLabel("0.25"));
    labelTable.put(0, new JLabel("0.0"));
    sliderThreshold.setLabelTable(labelTable);
    sliderThreshold.setValue((int) (settings.getScraperThreshold() * 100));
    sliderThreshold.addChangeListener(arg0 -> settings.setScraperThreshold(sliderThreshold.getValue() / 100.0));

    // set default certification style when changing NFO style
    cbNfoFormat.addItemListener(e -> {
      if (cbNfoFormat.getSelectedItem() == MovieConnectors.MP) {
        for (int i = 0; i < cbCertificationStyle.getItemCount(); i++) {
          CertificationStyleWrapper wrapper = cbCertificationStyle.getItemAt(i);
          if (wrapper.style == CertificationStyle.TECHNICAL) {
            cbCertificationStyle.setSelectedItem(wrapper);
            break;
          }
        }
      }
      else if (cbNfoFormat.getSelectedItem() == MovieConnectors.XBMC) {
        for (int i = 0; i < cbCertificationStyle.getItemCount(); i++) {
          CertificationStyleWrapper wrapper = cbCertificationStyle.getItemAt(i);
          if (wrapper.style == CertificationStyle.LARGE) {
            cbCertificationStyle.setSelectedItem(wrapper);
            break;
          }
        }
      }
    });

    // implement checkBoxListener for preset events
    settings.addPropertyChangeListener(evt -> {
      if ("preset".equals(evt.getPropertyName())) {
        buildCheckBoxes();
        buildComboBoxes();
      }
    });

    buildCheckBoxes();
    buildComboBoxes();
  }

  private void buildCheckBoxes() {
    cbMovieNfoFilename1.removeItemListener(checkBoxListener);
    cbMovieNfoFilename2.removeItemListener(checkBoxListener);
    cbMovieNfoFilename3.removeItemListener(checkBoxListener);
    clearSelection(cbMovieNfoFilename1, cbMovieNfoFilename2, cbMovieNfoFilename3);

    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.DISC_NFO)) {
      cbMovieNfoFilename3.setSelected(true);
    }

    cbMovieNfoFilename1.addItemListener(checkBoxListener);
    cbMovieNfoFilename2.addItemListener(checkBoxListener);
    cbMovieNfoFilename3.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
    }
  }

  private void buildComboBoxes() {
    cbCertificationStyle.removeItemListener(comboBoxListener);
    cbCertificationStyle.removeAllItems();

    // certification examples
    for (CertificationStyle style : CertificationStyle.values()) {
      CertificationStyleWrapper wrapper = new CertificationStyleWrapper();
      wrapper.style = style;
      cbCertificationStyle.addItem(wrapper);
      if (style == settings.getCertificationStyle()) {
        cbCertificationStyle.setSelectedItem(wrapper);
      }
    }

    cbCertificationStyle.addItemListener(comboBoxListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][20lp][][][500lp,grow]", "[][200lp][20lp][][][][20lp][][][][][][20lp][][][20lp][][][][][][][]"));
    {
      JLabel lblMetadataScraper = new JLabel(BUNDLE.getString("scraper.metadata")); // $NON-NLS-1$
      TmmFontHelper.changeFont(lblMetadataScraper, 1.16667, Font.BOLD);
      add(lblMetadataScraper, "cell 0 0 5 1");
    }
    {
      tableScraper = new TmmTable();
      tableScraper.setRowHeight(29);
      JScrollPane scrollPaneScraper = new JScrollPane(tableScraper);
      tableScraper.configureScrollPane(scrollPaneScraper);
      add(scrollPaneScraper, "cell 1 1 3 1,grow");
    }
    {
      JScrollPane scrollPaneScraperDetails = new JScrollPane();
      add(scrollPaneScraperDetails, "cell 4 1 1 5,grow");
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
    {
      JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage"));
      add(lblScraperLanguage, "cell 1 3 2 1");

      cbScraperLanguage = new JComboBox(MediaLanguages.values());
      add(cbScraperLanguage, "cell 3 3");

      JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry"));
      add(lblCountry, "cell 1 4 2 1");

      cbCertificationCountry = new JComboBox(CountryCode.values());
      add(cbCertificationCountry, "cell 3 4");

      chckbxScraperFallback = new JCheckBox(BUNDLE.getString("Settings.scraperfallback"));
      add(chckbxScraperFallback, "cell 1 5 3 1");
    }
    {
      JLabel lblNfoSettingsT = new JLabel(BUNDLE.getString("Settings.nfo")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblNfoSettingsT, 1.16667, Font.BOLD);
      add(lblNfoSettingsT, "cell 0 7 5 1");
    }
    {

      JLabel lblNfoFormat = new JLabel(BUNDLE.getString("Settings.nfoFormat")); //$NON-NLS-1$
      add(lblNfoFormat, "flowx,cell 1 8 4 1");

      cbNfoFormat = new JComboBox(MovieConnectors.values());
      add(cbNfoFormat, "cell 1 8 4 1");

      {
        JPanel panelNfoFormat = new JPanel();
        add(panelNfoFormat, "cell 1 9 4 1,grow");
        panelNfoFormat.setLayout(new MigLayout("insets 0", "[][]", "[][][]"));

        JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming")); //$NON-NLS-1$
        panelNfoFormat.add(lblNfoFileNaming, "cell 0 0");

        cbMovieNfoFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".nfo"); //$NON-NLS-1$
        panelNfoFormat.add(cbMovieNfoFilename1, "cell 1 0");

        cbMovieNfoFilename2 = new JCheckBox("movie.nfo"); //$NON-NLS-1$
        panelNfoFormat.add(cbMovieNfoFilename2, "cell 1 1");

        cbMovieNfoFilename3 = new JCheckBox(BUNDLE.getString("Settings.nfo.discstyle")); //$NON-NLS-1$
        panelNfoFormat.add(cbMovieNfoFilename3, "cell 1 2");
      }
      {
        chckbxWriteCleanNfo = new JCheckBox(BUNDLE.getString("Settings.writecleannfo")); //$NON-NLS-1$
        add(chckbxWriteCleanNfo, "cell 1 10 4 1");
      }
      {
        JLabel lblCertificationStyle = new JLabel(BUNDLE.getString("Settings.certificationformat")); //$NON-NLS-1$
        add(lblCertificationStyle, "flowx,cell 1 11 4 1");

        cbCertificationStyle = new JComboBox();
        add(cbCertificationStyle, "cell 1 11 4 1");
      }
    }
    {
      JLabel lblScraperOptionsT = new JLabel(BUNDLE.getString("scraper.metadata.defaults")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperOptionsT, 1.16667, Font.BOLD);
      add(lblScraperOptionsT, "cell 0 13 5 1");
    }
    {
      MovieScraperMetadataPanel movieScraperMetadataPanel = new MovieScraperMetadataPanel(settings.getMovieScraperMetadataConfig());
      add(movieScraperMetadataPanel, "cell 1 14 4 1,grow");
    }
    {
      JLabel lblArtworkScrapeT = new JLabel(BUNDLE.getString("Settings.images")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblArtworkScrapeT, 1.16667, Font.BOLD);
      add(lblArtworkScrapeT, "cell 0 16 5 1");
    }
    {
      chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape"));
      add(chckbxAutomaticallyScrapeImages, "cell 1 17 4 1");
    }
    {
      chckbxImageLanguage = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape.language"));
      add(chckbxImageLanguage, "cell 2 18 3 1");
    }
    {
      JLabel lblAutomaticScrapeT = new JLabel(BUNDLE.getString("Settings.automaticscraper")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAutomaticScrapeT, 1.16667, Font.BOLD);
      add(lblAutomaticScrapeT, "cell 0 20 5 1");
    }
    {
      JLabel lblScraperThreshold = new JLabel(BUNDLE.getString("Settings.scraperTreshold")); //$NON-NLS-1$
      add(lblScraperThreshold, "flowx,cell 1 21 3 1,aligny top");

      sliderThreshold = new JSlider();
      sliderThreshold.setMinorTickSpacing(5);
      sliderThreshold.setMajorTickSpacing(10);
      sliderThreshold.setPaintTicks(true);
      sliderThreshold.setPaintLabels(true);
      add(sliderThreshold, "cell 1 21 3 1,growx,aligny top");

      JTextPane tpScraperThresholdHint = new JTextPane();
      tpScraperThresholdHint.setOpaque(false);
      TmmFontHelper.changeFont(tpScraperThresholdHint, 0.833);
      tpScraperThresholdHint.setText(BUNDLE.getString("Settings.scraperTreshold.hint")); //$NON-NLS-1$
      add(tpScraperThresholdHint, "cell 1 22 4 1");
    }
  }

  /**
   * check changes of checkboxes
   */
  private void checkChanges() {
    // set NFO filenames
    settings.clearNfoFilenames();
    if (cbMovieNfoFilename1.isSelected()) {
      settings.addNfoFilename(MovieNfoNaming.FILENAME_NFO);
    }
    if (cbMovieNfoFilename2.isSelected()) {
      settings.addNfoFilename(MovieNfoNaming.MOVIE_NFO);
    }
    if (cbMovieNfoFilename3.isSelected()) {
      settings.addNfoFilename(MovieNfoNaming.DISC_NFO);
    }

    CertificationStyleWrapper wrapper = (CertificationStyleWrapper) cbCertificationStyle.getSelectedItem();
    if (wrapper != null && settings.getCertificationStyle() != wrapper.style) {
      settings.setCertificationStyle(wrapper.style);
    }
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public static class MovieScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      defaultScraper;

    public MovieScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider() == null || scraper.getMediaProvider().getProviderInfo() == null
          || scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
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

    private ImageIcon getScaledIcon(ImageIcon original) {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(new JPanel().getFont());

      int height = (int) (fm.getHeight() * 2f);
      int width = original.getIconWidth() / original.getIconHeight() * height;

      BufferedImage scaledImage;
      if (!scraper.isEnabled()) {
        scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
            Scalr.OP_GRAYSCALE);
      }
      else {
        scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
            Scalr.OP_ANTIALIAS);
      }
      return new ImageIcon(scaledImage);
    }

    public Icon getScraperLogo() {
      return scraperLogo;
    }

    public Boolean getDefaultScraper() {
      return defaultScraper;
    }

    public void setDefaultScraper(Boolean newValue) {
      if (scraper.isEnabled()) {
        Boolean oldValue = this.defaultScraper;
        this.defaultScraper = newValue;
        firePropertyChange("defaultScraper", oldValue, newValue);
      }
    }

    public IMediaProvider getMediaProvider() {
      return scraper.getMediaProvider();
    }
  }

  /*
   * helper for displaying the combobox with an example
   */
  private class CertificationStyleWrapper {
    private CertificationStyle style;

    @Override
    public String toString() {
      String bundleTag = BUNDLE.getString("Settings.certification." + style.name().toLowerCase());
      return bundleTag.replace("{}", CertificationStyle.formatCertification(Certification.DE_FSK16, style));
    }
  }

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
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("scraperFallback");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
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
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ,
        chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty, chckbxImageLanguage, jCheckBoxBeanProperty_1);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_10 = BeanProperty.create("imageLanguagePriority");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, chckbxImageLanguage, jCheckBoxBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<MovieSettings, MovieConnectors> settingsBeanProperty_11 = BeanProperty.create("movieConnector");
    BeanProperty<JComboBox<MovieConnectors>, Object> jComboBoxBeanProperty_1 = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MovieConnectors, JComboBox<MovieConnectors>, Object> autoBinding_9 = Bindings
        .createAutoBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty_11, cbNfoFormat, jComboBoxBeanProperty_1);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty = BeanProperty.create("writeCleanNfo");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty, chckbxWriteCleanNfo, jCheckBoxBeanProperty);
    autoBinding_2.bind();
  }
}
