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
package org.tinymediamanager.ui.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.DateField;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class UiSettingsPanel is used to display some UI related settings
 * 
 * @author Manuel Laggner
 */
class UiSettingsPanel extends JPanel {
  private static final long           serialVersionUID   = 6409982195347794360L;

  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER             = LoggerFactory.getLogger(UiSettingsPanel.class);
  private static final Integer[]      DEFAULT_FONT_SIZES = { 12, 14, 16, 18, 20, 22, 24, 26, 28 };

  private Settings                    settings           = Settings.getInstance();
  private List<LocaleComboBox>        locales            = new ArrayList<>();

  private JComboBox                   cbLanguage;
  private JLabel                      lblFontChangeHint;
  private LinkLabel                   lblLinkTranslate;
  private JComboBox                   cbFontSize;
  private JComboBox                   cbFontFamily;
  private JLabel                      lblLanguageChangeHint;
  private JCheckBox                   chckbxStoreWindowPreferences;
  private JComboBox                   cbTheme;
  private JLabel                      lblThemeHint;
  private JCheckBox                   chckbxShowMemory;
  private JComboBox                   cbDatefield;

  UiSettingsPanel() {
    LocaleComboBox actualLocale = null;
    Locale settingsLang = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    for (Locale l : Utils.getLanguages()) {
      LocaleComboBox localeComboBox = new LocaleComboBox(l);
      locales.add(localeComboBox);
      if (l.equals(settingsLang)) {
        actualLocale = localeComboBox;
      }
    }
    Collections.sort(locales);

    // ui init
    initComponents();
    initDataBindings();

    // data init
    if (actualLocale != null) {
      cbLanguage.setSelectedItem(actualLocale);
    }

    cbFontFamily.setSelectedItem(Globals.settings.getFontFamily());
    int index = cbFontFamily.getSelectedIndex();
    if (index < 0) {
      cbFontFamily.setSelectedItem("Dialog");
      index = cbFontFamily.getSelectedIndex();
    }
    if (index < 0) {
      cbFontFamily.setSelectedIndex(0);
    }
    cbFontSize.setSelectedItem(Globals.settings.getFontSize());
    index = cbFontSize.getSelectedIndex();
    if (index < 0) {
      cbFontSize.setSelectedIndex(0);
    }
    cbTheme.setSelectedItem(Globals.settings.getTheme());
    index = cbTheme.getSelectedIndex();
    if (index < 0) {
      cbTheme.setSelectedIndex(0);
    }

    lblLinkTranslate.addActionListener(arg0 -> {
      try {
        TmmUIHelper.browseUrl(lblLinkTranslate.getText());
      }
      catch (Exception e) {
        LOGGER.error(e.getMessage());
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, lblLinkTranslate.getText(), "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));//$NON-NLS-2$
      }
    });

    ActionListener actionListener = e -> checkChanges();
    cbLanguage.addActionListener(actionListener);
    cbFontFamily.addActionListener(actionListener);
    cbFontSize.addActionListener(actionListener);
    cbTheme.addActionListener(actionListener);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void initComponents() {
    setLayout(new MigLayout("hidemode 1", "[grow]", "[][15lp!][][15lp!][][15lp!][]"));
    {
      JPanel panelLanguage = SettingsPanelFactory.createSettingsPanel();

      JLabel lblLanguageT = new TmmLabel(BUNDLE.getString("Settings.language"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelLanguage, lblLanguageT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        cbLanguage = new JComboBox(locales.toArray());
        panelLanguage.add(cbLanguage, "cell 1 0 2 1");
      }
      {
        final JLabel lblLanguageHint = new JLabel(BUNDLE.getString("tmm.helptranslate"));
        panelLanguage.add(lblLanguageHint, "cell 1 1 2 1");
      }
      {
        lblLinkTranslate = new LinkLabel("https://forum.kodi.tv/showthread.php?tid=174987");
        panelLanguage.add(lblLinkTranslate, "cell 1 2 2 1, grow, wmin 0");
      }
      {
        lblLanguageChangeHint = new JLabel("");
        TmmFontHelper.changeFont(lblLanguageChangeHint, Font.BOLD);
        panelLanguage.add(lblLanguageChangeHint, "cell 0 3 3 1");
      }
    }
    {
      JPanel panelTheme = SettingsPanelFactory.createSettingsPanel();

      JLabel lblThemeT = new TmmLabel(BUNDLE.getString("Settings.uitheme"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelTheme, lblThemeT, true);
      add(collapsiblePanel, "cell 0 2,growx,wmin 0");
      {
        cbTheme = new JComboBox(new String[] { "Light", "Dark" });
        panelTheme.add(cbTheme, "cell 1 0 2 1");
      }
      {
        lblThemeHint = new JLabel("");
        TmmFontHelper.changeFont(lblThemeHint, Font.BOLD);
        panelTheme.add(lblThemeHint, "cell 0 1 3 1");
      }
    }
    {
      JPanel panelFont = SettingsPanelFactory.createSettingsPanel();

      JLabel lblFontT = new TmmLabel(BUNDLE.getString("Settings.font"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelFont, lblFontT, true);
      add(collapsiblePanel, "cell 0 4,growx,wmin 0");
      {
        JLabel lblFontFamilyT = new JLabel(BUNDLE.getString("Settings.fontfamily"));
        panelFont.add(lblFontFamilyT, "cell 1 0");
      }
      {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        cbFontFamily = new JComboBox(env.getAvailableFontFamilyNames());
        panelFont.add(cbFontFamily, "cell 2 0");
      }
      {
        JLabel lblFontSizeT = new JLabel(BUNDLE.getString("Settings.fontsize"));
        panelFont.add(lblFontSizeT, "cell 1 1");
      }
      {
        cbFontSize = new JComboBox(DEFAULT_FONT_SIZES);
        panelFont.add(cbFontSize, "cell 2 1");
      }
      {
        JTextArea tpFontHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.fonts.hint"));
        panelFont.add(tpFontHint, "cell 1 2 2 1,growx");
      }
      {
        lblFontChangeHint = new JLabel("");
        TmmFontHelper.changeFont(lblFontChangeHint, Font.BOLD);
        panelFont.add(lblFontChangeHint, "cell 0 3 3 1");
      }
    }
    {
      JPanel panelMisc = SettingsPanelFactory.createSettingsPanel();

      JLabel lblMiscT = new TmmLabel(BUNDLE.getString("Settings.misc"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMisc, lblMiscT, true);
      add(collapsiblePanel, "cell 0 6,growx,wmin 0");
      {
        JLabel lblDatefield = new JLabel(BUNDLE.getString("Settings.datefield"));
        panelMisc.add(lblDatefield, "cell 1 0");

        cbDatefield = new JComboBox(DateField.values());
        panelMisc.add(cbDatefield, "cell 2 0");

        JLabel lblDatefieldHint = new JLabel(BUNDLE.getString("Settings.datefield.desc"));
        panelMisc.add(lblDatefieldHint, "cell 2 1");
      }
      {
        chckbxStoreWindowPreferences = new JCheckBox(BUNDLE.getString("Settings.storewindowpreferences"));
        panelMisc.add(chckbxStoreWindowPreferences, "cell 1 2 2 1");
      }
      {
        chckbxShowMemory = new JCheckBox(BUNDLE.getString("Settings.showmemory"));
        panelMisc.add(chckbxShowMemory, "cell 1 3 2 1");
      }
    }
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    LocaleComboBox loc = (LocaleComboBox) cbLanguage.getSelectedItem();
    if (loc != null) {
      Locale locale = loc.loc;
      Locale actualLocale = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
      if (!locale.equals(actualLocale)) {
        Globals.settings.setLanguage(locale.toString());
        lblLanguageChangeHint.setText(BUNDLE.getString("Settings.languagehint"));
      }
    }

    // theme
    String theme = (String) cbTheme.getSelectedItem();
    if (!theme.equals(Globals.settings.getTheme())) {
      Globals.settings.setTheme(theme);
      lblThemeHint.setText(BUNDLE.getString("Settings.uitheme.hint"));
    }

    // fonts
    Integer fontSize = (Integer) cbFontSize.getSelectedItem();
    if (fontSize != null && fontSize != Globals.settings.getFontSize()) {
      Globals.settings.setFontSize(fontSize);
      lblFontChangeHint.setText(BUNDLE.getString("Settings.fontchangehint"));
    }

    String fontFamily = (String) cbFontFamily.getSelectedItem();
    if (fontFamily != null && !fontFamily.equals(Globals.settings.getFontFamily())) {
      Globals.settings.setFontFamily(fontFamily);
      lblFontChangeHint.setText(BUNDLE.getString("Settings.fontchangehint"));
    }
  }

  /**
   * Helper class for customized toString() method, to get the Name in localized language.
   */
  private class LocaleComboBox implements Comparable<LocaleComboBox> {
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

    @Override
    public int compareTo(LocaleComboBox o) {
      return toString().toLowerCase(Locale.ROOT).compareTo(o.toString().toLowerCase(Locale.ROOT));
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("storeWindowPreferences");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxStoreWindowPreferences, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("showMemory");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxShowMemory, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, DateField> settingsBeanProperty_2 = BeanProperty.create("dateField");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, DateField, JComboBox, Object> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, cbDatefield, jComboBoxBeanProperty);
    autoBinding_2.bind();
  }
}
