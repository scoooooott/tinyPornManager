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
package org.tinymediamanager.ui.settings;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageCache.CacheType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.sun.jna.Platform;

/**
 * The Class GeneralSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class GeneralSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID   = 500841588272296493L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER             = LoggerFactory.getLogger(GeneralSettingsPanel.class);
  private static final Integer[]      DEFAULT_FONT_SIZES = { 12, 14, 16, 18, 20, 22, 24, 26, 28 };
  private static final Pattern        MEMORY_PATTERN     = Pattern.compile("-Xmx([0-9]*)(.)");

  private Settings                    settings           = Settings.getInstance();
  private List<LocaleComboBox>        locales            = new ArrayList<>();

  private JTextField                  tfProxyHost;
  private JTextField                  tfProxyPort;
  private JTextField                  tfProxyUsername;
  private JPasswordField              tfProxyPassword;
  private JComboBox                   cbImageCacheQuality;
  private JCheckBox                   chckbxImageCache;
  private JComboBox                   cbLanguage;
  private JTextField                  tfMediaPlayer;
  private JButton                     btnSearchMediaPlayer;
  private JTextPane                   tpMediaPlayer;
  private JTextPane                   tpFontHint;
  private JComboBox                   cbFontSize;
  private JComboBox                   cbFontFamily;
  private JCheckBox                   chckbxDeleteTrash;
  private JSlider                     sliderMemory;
  private JTextPane                   tpMemoryHint;
  private LinkLabel                   lblLinkTransifex;
  private JCheckBox                   chckbxAnalytics;
  private JLabel                      lblLanguageHint;
  private JLabel                      lblFontChangeHint;
  private JLabel                      lblMemory;

  /**
   * Instantiates a new general settings panel.
   */
  public GeneralSettingsPanel() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(200px;min):grow"), FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("max(200px;default):grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, }));

    JPanel panelUI = new JPanel();
    panelUI.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.ui"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelUI, "2, 2, 3, 1, fill, fill");
    panelUI.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("100dlu"),
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.UNRELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.UNRELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, }));
    LocaleComboBox actualLocale = null;
    // cbLanguage = new JComboBox(Utils.getLanguages().toArray());
    Locale settingsLang = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    for (Locale l : Utils.getLanguages()) {
      LocaleComboBox localeComboBox = new LocaleComboBox(l);
      locales.add(localeComboBox);
      if (l.equals(settingsLang)) {
        actualLocale = localeComboBox;
      }
    }

    JLabel lblUiLanguage = new JLabel(BUNDLE.getString("Settings.language"));
    panelUI.add(lblUiLanguage, "2, 2");
    cbLanguage = new JComboBox(locales.toArray());
    panelUI.add(cbLanguage, "4, 2");

    if (actualLocale != null) {
      cbLanguage.setSelectedItem(actualLocale);
    }

    JSeparator separator = new JSeparator();
    separator.setOrientation(SwingConstants.VERTICAL);
    panelUI.add(separator, "8, 2, 1, 7");

    JLabel lblFontFamily = new JLabel(BUNDLE.getString("Settings.fontfamily")); //$NON-NLS-1$
    panelUI.add(lblFontFamily, "10, 2, right, default");
    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    cbFontFamily = new JComboBox(env.getAvailableFontFamilyNames());
    cbFontFamily.setSelectedItem(Globals.settings.getFontFamily());
    int index = cbFontFamily.getSelectedIndex();
    if (index < 0) {
      cbFontFamily.setSelectedItem("Dialog");
      index = cbFontFamily.getSelectedIndex();
    }
    if (index < 0) {
      cbFontFamily.setSelectedIndex(0);
    }
    panelUI.add(cbFontFamily, "12, 2, fill, default");

    JLabel lblFontSize = new JLabel(BUNDLE.getString("Settings.fontsize")); //$NON-NLS-1$
    panelUI.add(lblFontSize, "10, 4, right, default");

    cbFontSize = new JComboBox(DEFAULT_FONT_SIZES);
    cbFontSize.setSelectedItem(Globals.settings.getFontSize());
    index = cbFontSize.getSelectedIndex();
    if (index < 0) {
      cbFontSize.setSelectedIndex(0);
    }

    panelUI.add(cbFontSize, "12, 4, fill, default");

    JPanel panel = new JPanel();
    panelUI.add(panel, "2, 6, 5, 1, fill, fill");
    panel.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("100dlu"), FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    JLabel lblMissingTranslation = new JLabel(BUNDLE.getString("tmm.helptranslate"));
    panel.add(lblMissingTranslation, "1, 1, 5, 1");

    lblLinkTransifex = new LinkLabel("https://www.transifex.com/projects/p/tinymediamanager/");
    panel.add(lblLinkTransifex, "1, 3, 5, 1");
    lblLinkTransifex.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          TmmUIHelper.browseUrl(lblLinkTransifex.getNormalText());
        }
        catch (Exception e) {
          LOGGER.error(e.getMessage());
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, lblLinkTransifex.getNormalText(), "message.erroropenurl",
              new String[] { ":", e.getLocalizedMessage() }));//$NON-NLS-1$
        }
      }
    });

    tpFontHint = new JTextPane();
    tpFontHint.setOpaque(false);
    TmmFontHelper.changeFont(tpFontHint, 0.833);
    tpFontHint.setText(BUNDLE.getString("Settings.fonts.hint")); //$NON-NLS-1$
    panelUI.add(tpFontHint, "10, 6, 5, 1");

    lblLanguageHint = new JLabel("");
    TmmFontHelper.changeFont(lblLanguageHint, Font.BOLD);
    panelUI.add(lblLanguageHint, "2, 8, 5, 1");

    lblFontChangeHint = new JLabel("");
    TmmFontHelper.changeFont(lblFontChangeHint, Font.BOLD);
    panelUI.add(lblFontChangeHint, "10, 8, 5, 1");

    JPanel panelMemory = new JPanel();
    panelMemory.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.memoryborder"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMemory, "2, 4, fill, fill");
    panelMemory.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("250px:grow(4)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(20dlu;default)"),
            ColumnSpec.decode("left:default:grow(5)"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblMemoryT = new JLabel(BUNDLE.getString("Settings.memory")); //$NON-NLS-1$
    panelMemory.add(lblMemoryT, "2, 1");

    sliderMemory = new JSlider();
    sliderMemory.setPaintLabels(true);
    sliderMemory.setPaintTicks(true);
    sliderMemory.setSnapToTicks(true);
    sliderMemory.setMajorTickSpacing(512);
    sliderMemory.setMinorTickSpacing(128);
    sliderMemory.setMinimum(256);
    if (Platform.is64Bit()) {
      sliderMemory.setMaximum(2560);
    }
    else {
      sliderMemory.setMaximum(1536);
    }
    sliderMemory.setValue(512);
    panelMemory.add(sliderMemory, "4, 1, fill, default");

    lblMemory = new JLabel("512"); //$NON-NLS-1$
    panelMemory.add(lblMemory, "6, 1, right, default");

    JLabel lblMb = new JLabel("MB");
    panelMemory.add(lblMb, "7, 1, left, default");

    tpMemoryHint = new JTextPane();
    tpMemoryHint.setOpaque(false);
    tpMemoryHint.setText(BUNDLE.getString("Settings.memory.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(tpMemoryHint, 0.833);
    panelMemory.add(tpMemoryHint, "2, 3, 6, 1, fill, fill");

    JPanel panelProxySettings = new JPanel();
    panelProxySettings.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.proxy"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelProxySettings, "4, 4, fill, fill");
    panelProxySettings.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblProxyHost = new JLabel(BUNDLE.getString("Settings.proxyhost")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyHost, "2, 2, right, default");

    tfProxyHost = new JTextField();
    lblProxyHost.setLabelFor(tfProxyHost);
    panelProxySettings.add(tfProxyHost, "4, 2, fill, default");
    tfProxyHost.setColumns(10);

    JLabel lblProxyPort = new JLabel(BUNDLE.getString("Settings.proxyport")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyPort, "2, 4, right, default");

    tfProxyPort = new JTextField();
    lblProxyPort.setLabelFor(tfProxyPort);
    panelProxySettings.add(tfProxyPort, "4, 4, fill, default");
    tfProxyPort.setColumns(10);

    JLabel lblProxyUser = new JLabel(BUNDLE.getString("Settings.proxyuser")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyUser, "2, 6, right, default");

    tfProxyUsername = new JTextField();
    lblProxyUser.setLabelFor(tfProxyUsername);
    panelProxySettings.add(tfProxyUsername, "4, 6, fill, default");
    tfProxyUsername.setColumns(10);

    JLabel lblProxyPassword = new JLabel(BUNDLE.getString("Settings.proxypass")); //$NON-NLS-1$
    panelProxySettings.add(lblProxyPassword, "2, 8, right, default");

    tfProxyPassword = new JPasswordField();
    lblProxyPassword.setLabelFor(tfProxyPassword);
    panelProxySettings.add(tfProxyPassword, "4, 8, fill, default");

    JPanel panelMediaPlayer = new JPanel();
    panelMediaPlayer.setBorder(new TitledBorder(null, "MediaPlayer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelMediaPlayer, "2, 6, fill, fill");
    panelMediaPlayer.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, }));

    tpMediaPlayer = new JTextPane();
    tpMediaPlayer.setOpaque(false);
    TmmFontHelper.changeFont(tpMediaPlayer, 0.833);
    tpMediaPlayer.setText(BUNDLE.getString("Settings.mediaplayer.hint")); //$NON-NLS-1$
    panelMediaPlayer.add(tpMediaPlayer, "2, 2, 3, 1, fill, fill");

    tfMediaPlayer = new JTextField();
    panelMediaPlayer.add(tfMediaPlayer, "2, 4, fill, default");
    tfMediaPlayer.setColumns(10);

    btnSearchMediaPlayer = new JButton(BUNDLE.getString("Button.chooseplayer")); //$NON-NLS-1$
    btnSearchMediaPlayer.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        String path = TmmProperties.getInstance().getProperty("chooseplayer.path");
        Path file = TmmUIHelper.selectFile(BUNDLE.getString("Button.chooseplayer"), path); //$NON-NLS-1$
        if (file != null && Utils.isRegularFile(file) || Platform.isMac()) {
          tfMediaPlayer.setText(file.toAbsolutePath().toString());
          TmmProperties.getInstance().putProperty("chooseplayer.path", file.toAbsolutePath().toString());
        }
      }
    });
    panelMediaPlayer.add(btnSearchMediaPlayer, "4, 4");

    JPanel panelCache = new JPanel();
    panelCache.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.cache"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelCache, "4, 6, fill, fill");
    panelCache.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, }));

    chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecache"));//$NON-NLS-1$
    panelCache.add(chckbxImageCache, "2, 2, 3, 1");

    JLabel lblImageCacheQuality = new JLabel(BUNDLE.getString("Settings.imagecachetype"));//$NON-NLS-1$
    panelCache.add(lblImageCacheQuality, "2, 4, right, default");

    cbImageCacheQuality = new JComboBox(ImageCache.CacheType.values());
    panelCache.add(cbImageCacheQuality, "4, 4, fill, default");

    JPanel panelAnalytics = new JPanel();
    panelAnalytics
        .setBorder(new TitledBorder(null, BUNDLE.getString("Settings.analytics.border"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelAnalytics, "2, 8, fill, fill");
    panelAnalytics.setLayout(
        new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.LINE_GAP_ROWSPEC, }));

    chckbxAnalytics = new JCheckBox(BUNDLE.getString("Settings.analytics"));//$NON-NLS-1$
    panelAnalytics.add(chckbxAnalytics, "2, 2");

    JTextPane tpAnalyticsDescription = new JTextPane();
    tpAnalyticsDescription.setText(BUNDLE.getString("Settings.analytics.desc"));//$NON-NLS-1$
    tpAnalyticsDescription.setOpaque(false);
    panelAnalytics.add(tpAnalyticsDescription, "2, 4, fill, fill");

    JPanel panelMisc = new JPanel();
    panelMisc.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.misc"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMisc, "4, 8, fill, fill");
    panelMisc.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    chckbxDeleteTrash = new JCheckBox(BUNDLE.getString("Settings.deletetrash"));
    panelMisc.add(chckbxDeleteTrash, "2, 2, 3, 1");

    initDataBindings();

    initMemorySlider();

    // listen to changes of the combo box
    ItemListener listener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    };

    cbLanguage.addItemListener(listener);
    cbFontSize.addItemListener(listener);
    cbFontFamily.addItemListener(listener);
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    LocaleComboBox loc = (LocaleComboBox) cbLanguage.getSelectedItem();
    Locale locale = loc.loc;
    Locale actualLocale = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    if (!locale.equals(actualLocale)) {
      Globals.settings.setLanguage(locale.toString());
      lblLanguageHint.setText(BUNDLE.getString("Settings.languagehint")); //$NON-NLS-1$
    }

    // fonts
    Integer fontSize = (Integer) cbFontSize.getSelectedItem();
    if (fontSize != Globals.settings.getFontSize()) {
      Globals.settings.setFontSize(fontSize);
      lblFontChangeHint.setText(BUNDLE.getString("Settings.fontchangehint")); //$NON-NLS-1$
    }

    String fontFamily = (String) cbFontFamily.getSelectedItem();
    if (!fontFamily.equals(Globals.settings.getFontFamily())) {
      Globals.settings.setFontFamily(fontFamily);
      lblFontChangeHint.setText(BUNDLE.getString("Settings.fontchangehint")); //$NON-NLS-1$
    }
  }

  /**
   * Helper class for customized toString() method, to get the Name in localized language.
   */
  public static class LocaleComboBox {
    private Locale       loc;
    private List<Locale> countries;

    public LocaleComboBox(Locale loc) {
      this.loc = loc;
      countries = LocaleUtils.countriesByLanguage(loc.getLanguage().toLowerCase(Locale.ROOT));
    }

    public Locale getLocale() {
      return loc;
    }

    @Override
    public String toString() {
      // display country name if needed
      // not needed when language == country
      if (loc.getLanguage().equalsIgnoreCase(loc.getCountry())) {
        return loc.getDisplayLanguage(loc);
      }

      // special exceptions (which do not have language == country)
      if (loc.toString().equals("en_US")) {
        return loc.getDisplayLanguage(loc);
      }

      // not needed, when this language is only in one country
      if (countries.size() == 1) {
        return loc.getDisplayLanguage(loc);
      }

      // output country if available
      if (StringUtils.isNotBlank(loc.getDisplayCountry(loc))) {
        return loc.getDisplayLanguage(loc) + " (" + loc.getDisplayCountry(loc) + ")";
      }

      return loc.getDisplayLanguage(loc);
    }
  }

  private void initMemorySlider() {
    Path file = Paths.get("extra.txt");
    int maxMemory = 512;
    if (Files.exists(file)) {
      // parse out memory option from extra.txt
      try {
        String extraTxt = Utils.readFileToString(file);
        Matcher matcher = MEMORY_PATTERN.matcher(extraTxt);
        if (matcher.find()) {
          maxMemory = Integer.parseInt(matcher.group(1));
          String dimension = matcher.group(2);
          if ("k".equalsIgnoreCase(dimension)) {
            maxMemory /= 1024;
          }
          if ("g".equalsIgnoreCase(dimension)) {
            maxMemory *= 1024;
          }
        }
      }
      catch (Exception e) {
        maxMemory = 512;
      }
    }

    sliderMemory.setValue(maxMemory);

    // add a listener to write the actual memory state to extra.txt
    addHierarchyListener(new HierarchyListener() {
      private boolean oldState = false;

      @Override
      public void hierarchyChanged(HierarchyEvent e) {
        if (oldState != isShowing()) {
          oldState = isShowing();
          if (!isShowing()) {
            writeMemorySettings();
          }
        }
      }
    });
  }

  private void writeMemorySettings() {
    int memoryAmount = sliderMemory.getValue();
    String jvmArg = "-Xmx" + memoryAmount + "m";

    // no need of putting the default value in the file
    if (memoryAmount == 512) {
      jvmArg = "";
    }

    Path file = Paths.get("extra.txt");
    // new file - do not write when 512MB is set
    if (memoryAmount != 512 && !Files.exists(file)) {
      try {
        Utils.writeStringToFile(file, jvmArg);
      }
      catch (IOException e) {
      }
    }
    else if (Files.exists(file)) {
      try {
        String extraTxt = Utils.readFileToString(file);
        Matcher matcher = MEMORY_PATTERN.matcher(extraTxt);
        if (matcher.find()) {
          extraTxt = extraTxt.replace(matcher.group(0), jvmArg);
        }
        else {
          extraTxt += "\r\n" + jvmArg;
        }
        // nothing in the file?
        if (StringUtils.isBlank(extraTxt)) {
          // yes -> delete it
          Utils.deleteFileSafely(file);
        }
        else {
          // no -> rewrite it
          Utils.writeStringToFile(file, extraTxt);
        }
      }
      catch (Exception e) {
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, String> settingsBeanProperty = BeanProperty.create("proxyHost");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, tfProxyHost, jTextFieldBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_1 = BeanProperty.create("proxyPort");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, tfProxyPort, jTextFieldBeanProperty_1);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_2 = BeanProperty.create("proxyUsername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, tfProxyUsername, jTextFieldBeanProperty_2);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_3 = BeanProperty.create("proxyPassword");
    BeanProperty<JPasswordField, String> jPasswordFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JPasswordField, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, tfProxyPassword, jPasswordFieldBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, CacheType> settingsBeanProperty_7 = BeanProperty.create("imageCacheType");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, CacheType, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, cbImageCacheQuality, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_9 = BeanProperty.create("imageCache");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxImageCache, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_6 = BeanProperty.create("mediaPlayer");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, tfMediaPlayer, jTextFieldBeanProperty_3);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_10 = BeanProperty.create("deleteTrashOnExit");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, chckbxDeleteTrash, jCheckBoxBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<JSlider, Integer> jSliderBeanProperty = BeanProperty.create("value");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JSlider, Integer, JLabel, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ, sliderMemory, jSliderBeanProperty,
        lblMemory, jLabelBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_4 = BeanProperty.create("enableAnalytics");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxAnalytics, jCheckBoxBeanProperty);
    autoBinding_4.bind();
  }
}
