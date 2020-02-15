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
package org.tinymediamanager.ui.wizard;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link UiSettingsPanel} is used to display generic UI settings
 * 
 * @author Manuel Laggner
 */
class UiSettingsPanel extends JPanel {
  private static final long           serialVersionUID   = -1241134514329815223L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER             = LoggerFactory.getLogger(UiSettingsPanel.class);

  private static final Integer[]      DEFAULT_FONT_SIZES = { 12, 14, 16, 18, 20, 22, 24, 26, 28 };

  private Settings                    settings           = Settings.getInstance();
  private List<LocaleComboBox>        locales            = new ArrayList<>();

  private JComboBox                   cbLanguage;
  private JRadioButton                rdbtnLight;
  private JRadioButton                rdbtnDark;
  private JComboBox                   cbFontSize;
  private JComboBox                   cbFontFamily;

  public UiSettingsPanel() {
    LocaleComboBox actualLocale = null;
    Locale settingsLang = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    for (Locale l : Utils.getLanguages()) {
      LocaleComboBox localeComboBox = new LocaleComboBox(l);
      locales.add(localeComboBox);
      if (l.equals(settingsLang)) {
        actualLocale = localeComboBox;
      }
    }

    initComponents();

    // data init
    if (actualLocale != null) {
      cbLanguage.setSelectedItem(actualLocale);
    }

    cbFontFamily.setSelectedItem(settings.getFontFamily());
    int index = cbFontFamily.getSelectedIndex();
    if (index < 0) {
      cbFontFamily.setSelectedItem("Dialog");
      index = cbFontFamily.getSelectedIndex();
    }
    if (index < 0) {
      cbFontFamily.setSelectedIndex(0);
    }
    cbFontSize.setSelectedItem(settings.getFontSize());
    index = cbFontSize.getSelectedIndex();
    if (index < 0) {
      cbFontSize.setSelectedIndex(0);
    }

    if ("Dark".equals(settings.getTheme())) {
      rdbtnDark.setSelected(true);
    }
    else {
      rdbtnLight.setSelected(true);
    }

    ActionListener actionListener = e -> checkChanges();
    cbLanguage.addActionListener(actionListener);
    cbFontFamily.addActionListener(actionListener);
    cbFontSize.addActionListener(actionListener);
    rdbtnLight.addActionListener(actionListener);
    rdbtnDark.addActionListener(actionListener);
  }

  /*
   * init UI components
   */
  private void initComponents() {
    setLayout(new MigLayout("", "[20lp!][200lp,grow][200lp,grow]", "[][][10lp!][][10lp!][][grow][][10lp!][][][]"));
    {
      JLabel lblUiSettings = new JLabel(BUNDLE.getString("wizard.ui"));
      TmmFontHelper.changeFont(lblUiSettings, 1.3333, Font.BOLD);
      add(lblUiSettings, "cell 0 0 3 1,growx");
    }

    JTextArea taSettingsHint = new ReadOnlyTextArea(BUNDLE.getString("wizard.ui.hint"));
    add(taSettingsHint, "cell 1 1 2 1,growx");

    JLabel lblLanguageT = new JLabel(BUNDLE.getString("Settings.language"));
    add(lblLanguageT, "flowx,cell 1 3 2 1");

    cbLanguage = new JComboBox(locales.toArray());
    add(cbLanguage, "cell 1 3 2 1");

    JLabel lblThemeT = new JLabel(BUNDLE.getString("Settings.uitheme"));
    add(lblThemeT, "cell 1 5");

    ImageLabel lblLight = new ImageLabel(false);
    try (InputStream is = UiSettingsPanel.class.getResourceAsStream("light.png")) {
      lblLight.setOriginalImage(IOUtils.toByteArray(is));
    }
    catch (Exception e) {
      LOGGER.error("could not load image: {}", e.getMessage());
    }
    add(lblLight, "cell 1 6, grow");

    ImageLabel lblDark = new ImageLabel(false);
    try (InputStream is = UiSettingsPanel.class.getResourceAsStream("dark.png")) {
      lblDark.setOriginalImage(IOUtils.toByteArray(is));
    }
    catch (Exception e) {
      LOGGER.error("could not load image: {}", e.getMessage());
    }
    add(lblDark, "cell 2 6, grow");

    ButtonGroup buttonGroup = new ButtonGroup();

    rdbtnLight = new JRadioButton("Light");
    buttonGroup.add(rdbtnLight);
    add(rdbtnLight, "cell 1 7,alignx center");

    rdbtnDark = new JRadioButton("Dark");
    buttonGroup.add(rdbtnDark);
    add(rdbtnDark, "cell 2 7,alignx center");

    JLabel lblFontT = new JLabel(BUNDLE.getString("Settings.font"));
    add(lblFontT, "flowx,cell 1 9 2 1");

    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    cbFontFamily = new JComboBox(env.getAvailableFontFamilyNames());
    add(cbFontFamily, "cell 1 9 2 1");

    JLabel lblSize = new JLabel(BUNDLE.getString("Settings.fontsize"));
    add(lblSize, "flowx,cell 1 10");

    cbFontSize = new JComboBox(DEFAULT_FONT_SIZES);
    add(cbFontSize, "cell 1 10");

    JTextArea taFontHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.fonts.hint"));
    add(taFontHint, "cell 1 11 2 1,grow");
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    LocaleComboBox loc = (LocaleComboBox) cbLanguage.getSelectedItem();
    if (loc != null) {
      Locale locale = loc.loc;
      Locale actualLocale = Utils.getLocaleFromLanguage(settings.getLanguage());
      if (!locale.equals(actualLocale)) {
        settings.setLanguage(locale.toString());
      }
    }

    // theme
    String theme;
    if (rdbtnDark.isSelected()) {
      theme = "Dark";
    }
    else {
      theme = "Light";
    }
    if (!theme.equals(settings.getTheme())) {
      settings.setTheme(theme);
    }

    // fonts
    Integer fontSize = (Integer) cbFontSize.getSelectedItem();
    if (fontSize != null && fontSize != settings.getFontSize()) {
      settings.setFontSize(fontSize);
    }

    String fontFamily = (String) cbFontFamily.getSelectedItem();
    if (fontFamily != null && !fontFamily.equals(settings.getFontFamily())) {
      settings.setFontFamily(fontFamily);
    }
  }

  /**
   * Helper class for customized toString() method, to get the Name in localized language.
   */
  private class LocaleComboBox {
    private Locale       loc;
    private List<Locale> countries;

    LocaleComboBox(Locale loc) {
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
}
