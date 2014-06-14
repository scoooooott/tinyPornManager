/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageCache.CacheType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import ch.qos.logback.classic.Level;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class GeneralSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class GeneralSettingsPanel extends ScrollablePanel {

  private static final long           serialVersionUID   = 500841588272296493L;
  private static final ResourceBundle BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Integer[]      DEFAULT_FONT_SIZES = { 12, 14, 16, 18, 20, 22, 24, 26, 28 };

  private Settings                    settings           = Settings.getInstance();
  private List<LocaleComboBox>        locales            = new ArrayList<LocaleComboBox>();

  private JPanel                      panelProxySettings;
  private JTextField                  tfProxyHost;
  private JTextField                  tfProxyPort;
  private JTextField                  tfProxyUsername;
  private JPasswordField              tfProxyPassword;
  private JCheckBox                   chckbxClearCacheShutdown;
  private JLabel                      lblLoglevel;
  private JComboBox                   cbLogLevel;
  private JPanel                      panelCache;
  private JPanel                      panelLogger;
  private JLabel                      lblImageCacheQuality;
  private JComboBox                   cbImageCacheQuality;
  private JCheckBox                   chckbxBuildImageCache;
  private JCheckBox                   chckbxImageCache;
  private JPanel                      panelUI;
  private JLabel                      lblUiLanguage;
  private JComboBox                   cbLanguage;
  private JLabel                      lblLanguageHint;
  private JCheckBox                   chckbxShowNotifications;
  private JPanel                      panelMediaPlayer;
  private JTextField                  tfMediaPlayer;
  private JButton                     btnSearchMediaPlayer;
  private JTextPane                   tpMediaPlayer;
  private JSeparator                  separator;
  private JTextPane                   tpFontHint;
  private JLabel                      lblFontFamily;
  private JLabel                      lblFontSize;
  private JComboBox                   cbFontSize;
  private JComboBox                   cbFontFamily;
  private JLabel                      lblFontChangeHint;

  /**
   * Instantiates a new general settings panel.
   */
  public GeneralSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(200px;min):grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(200px;default):grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));

    panelUI = new JPanel();
    panelUI.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.ui"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelUI, "2, 2, 3, 1, fill, fill");
    panelUI.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    lblUiLanguage = new JLabel(BUNDLE.getString("Settings.language")); //$NON-NLS-1$
    panelUI.add(lblUiLanguage, "2, 2, right, default");

    // listen to changes of the combo box
    ItemListener listener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    };

    LocaleComboBox actualLocale = null;

    // cbLanguage = new JComboBox(Utils.getLanguages().toArray());
    Locale settingsLang = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    for (Locale l : Utils.getLanguages()) {
      LocaleComboBox localeComboBox = new LocaleComboBox();
      localeComboBox.loc = l;
      locales.add(localeComboBox);
      if (l.equals(settingsLang)) {
        actualLocale = localeComboBox;
      }
    }
    cbLanguage = new JComboBox(locales.toArray());
    if (actualLocale != null) {
      cbLanguage.setSelectedItem(actualLocale);
    }
    panelUI.add(cbLanguage, "4, 2, fill, default");

    lblLanguageHint = new JLabel("");
    TmmFontHelper.changeFont(lblLanguageHint, Font.BOLD);
    panelUI.add(lblLanguageHint, "2, 4, 5, 1");

    chckbxShowNotifications = new JCheckBox(BUNDLE.getString("Settings.shownotifications")); //$NON-NLS-1$
    panelUI.add(chckbxShowNotifications, "2, 6, 5, 1");

    separator = new JSeparator();
    panelUI.add(separator, "2, 8, 5, 1");

    tpFontHint = new JTextPane();
    tpFontHint.setOpaque(false);
    TmmFontHelper.changeFont(tpFontHint, 0.833);
    tpFontHint.setText(BUNDLE.getString("Settings.fonts.hint")); //$NON-NLS-1$
    panelUI.add(tpFontHint, "2, 10, 5, 1");

    lblFontFamily = new JLabel(BUNDLE.getString("Settings.fontfamily")); //$NON-NLS-1$
    panelUI.add(lblFontFamily, "2, 12, right, default");

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
    panelUI.add(cbFontFamily, "4, 12, fill, default");

    lblFontSize = new JLabel(BUNDLE.getString("Settings.fontsize")); //$NON-NLS-1$
    panelUI.add(lblFontSize, "2, 14, right, default");

    cbFontSize = new JComboBox(DEFAULT_FONT_SIZES);
    cbFontSize.setSelectedItem(Globals.settings.getFontSize());
    index = cbFontSize.getSelectedIndex();
    if (index < 0) {
      cbFontSize.setSelectedIndex(0);
    }
    panelUI.add(cbFontSize, "4, 14, fill, default");

    lblFontChangeHint = new JLabel("");
    TmmFontHelper.changeFont(lblFontChangeHint, Font.BOLD);
    panelUI.add(lblFontChangeHint, "2, 16, 5, 1");

    panelCache = new JPanel();
    panelCache.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.cache"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelCache, "2, 4, fill, fill");
    panelCache.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

    chckbxClearCacheShutdown = new JCheckBox(BUNDLE.getString("Settings.clearhttpcache"));//$NON-NLS-1$
    panelCache.add(chckbxClearCacheShutdown, "2, 2, 3, 1");

    chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecache"));//$NON-NLS-1$
    panelCache.add(chckbxImageCache, "2, 4, 3, 1");

    lblImageCacheQuality = new JLabel(BUNDLE.getString("Settings.imagecachetype"));//$NON-NLS-1$
    panelCache.add(lblImageCacheQuality, "2, 6, right, default");

    cbImageCacheQuality = new JComboBox(ImageCache.CacheType.values());
    panelCache.add(cbImageCacheQuality, "4, 6, fill, default");

    chckbxBuildImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecachebackground"));//$NON-NLS-1$
    chckbxBuildImageCache.setVisible(false);
    panelCache.add(chckbxBuildImageCache, "2, 8, 3, 1");

    panelProxySettings = new JPanel();
    panelProxySettings.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.proxy"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelProxySettings, "4, 4, fill, fill");
    panelProxySettings.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

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

    panelMediaPlayer = new JPanel();
    panelMediaPlayer.setBorder(new TitledBorder(null, "MediaPlayer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelMediaPlayer, "2, 6, 3, 1, fill, fill");
    panelMediaPlayer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

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
        File file = TmmUIHelper.selectFile(BUNDLE.getString("Button.chooseplayer")); //$NON-NLS-1$
        if (file != null && file.exists() && file.isFile()) {
          tfMediaPlayer.setText(file.getPath());
        }
      }
    });
    panelMediaPlayer.add(btnSearchMediaPlayer, "4, 4");

    panelLogger = new JPanel();
    add(panelLogger, "2, 8, fill, fill");
    panelLogger.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    lblLoglevel = new JLabel(BUNDLE.getString("Settings.loglevel"));//$NON-NLS-1$
    panelLogger.add(lblLoglevel, "2, 2");

    Level[] levels = new Level[] { Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
    Level actualLevel = Level.toLevel(Globals.settings.getLogLevel());
    cbLogLevel = new JComboBox(levels);
    panelLogger.add(cbLogLevel, "4, 2");
    cbLogLevel.setSelectedItem(actualLevel);

    initDataBindings();

    cbLanguage.addItemListener(listener);
    cbLogLevel.addItemListener(listener);
    cbFontFamily.addItemListener(listener);
    cbFontSize.addItemListener(listener);
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    Level level = (Level) cbLogLevel.getSelectedItem();
    int actualLevel = Globals.settings.getLogLevel();
    if (actualLevel != level.levelInt) {
      Globals.settings.setLogLevel(level.levelInt);
    }

    LocaleComboBox loc = (LocaleComboBox) cbLanguage.getSelectedItem();
    Locale locale = loc.loc;
    Locale actualLocale = Utils.getLocaleFromLanguage(Globals.settings.getLanguage());
    if (!locale.equals(actualLocale)) {
      Globals.settings.setLanguage(locale.getLanguage());
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
  private class LocaleComboBox {
    private Locale loc;

    @Override
    public String toString() {
      return loc.getDisplayLanguage(loc);
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
    BeanProperty<Settings, Boolean> settingsBeanProperty_4 = BeanProperty.create("clearCacheShutdown");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxClearCacheShutdown, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, CacheType> settingsBeanProperty_7 = BeanProperty.create("imageCacheType");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, CacheType, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, cbImageCacheQuality, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_8 = BeanProperty.create("imageCacheBackground");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, chckbxBuildImageCache, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_9 = BeanProperty.create("imageCache");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxImageCache, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_5 = BeanProperty.create("showNotifications");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, chckbxShowNotifications, jCheckBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_6 = BeanProperty.create("mediaPlayer");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, tfMediaPlayer, jTextFieldBeanProperty_3);
    autoBinding_9.bind();
  }
}
