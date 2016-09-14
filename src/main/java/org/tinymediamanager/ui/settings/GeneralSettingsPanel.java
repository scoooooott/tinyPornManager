/*
 * Copyright 2012 - 2016 Manuel Laggner
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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.ImageCache.CacheType;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import com.sun.jna.Platform;

import net.miginfocom.swing.MigLayout;

/**
 * The Class GeneralSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class GeneralSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 500841588272296493L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Pattern        MEMORY_PATTERN   = Pattern.compile("-Xmx([0-9]*)(.)");

  private Settings                    settings         = Settings.getInstance();

  private JTextField                  tfProxyHost;
  private JTextField                  tfProxyPort;
  private JTextField                  tfProxyUsername;
  private JPasswordField              tfProxyPassword;
  private JComboBox                   cbImageCacheQuality;
  private JCheckBox                   chckbxImageCache;
  private JTextField                  tfMediaPlayer;
  private JButton                     btnSearchMediaPlayer;
  private JCheckBox                   chckbxDeleteTrash;
  private JSlider                     sliderMemory;
  private JCheckBox                   chckbxAnalytics;
  private JLabel                      lblMemory;

  /**
   * Instantiates a new general settings panel.
   */
  public GeneralSettingsPanel() {

    initComponents();

    initDataBindings();

    initMemorySlider();

    // data init
    btnSearchMediaPlayer.addActionListener(arg0 -> {
      Path file = TmmUIHelper.selectFile(BUNDLE.getString("Button.chooseplayer")); //$NON-NLS-1$
      if (file != null && Utils.isRegularFile(file) || Platform.isMac()) {
        tfMediaPlayer.setText(file.toAbsolutePath().toString());
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][][][][][grow]", "[][][][20lp][][][][20lp][][][][20lp][][][][][][][][]"));
    {
      final JLabel lblMediaPlayerT = new JLabel(BUNDLE.getString("Settings.mediaplayer")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMediaPlayerT, 1.16667, Font.BOLD);
      add(lblMediaPlayerT, "cell 0 0 3 1");
    }
    {
      tfMediaPlayer = new JTextField();
      add(tfMediaPlayer, "flowx,cell 1 1 4 1");
      tfMediaPlayer.setColumns(35);

      btnSearchMediaPlayer = new JButton(BUNDLE.getString("Button.chooseplayer")); //$NON-NLS-1$
      add(btnSearchMediaPlayer, "cell 1 1 4 1");

      JTextPane tpMediaPlayer = new JTextPane();
      add(tpMediaPlayer, "cell 1 2 5 1,growx");
      tpMediaPlayer.setOpaque(false);
      TmmFontHelper.changeFont(tpMediaPlayer, 0.833);
      tpMediaPlayer.setText(BUNDLE.getString("Settings.mediaplayer.hint"));
    }
    {
      final JLabel lblMemorySettingsT = new JLabel(BUNDLE.getString("Settings.memoryborder")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMemorySettingsT, 1.16667, Font.BOLD);
      add(lblMemorySettingsT, "cell 0 4 3 1");
    }
    {
      JLabel lblMemoryT = new JLabel(BUNDLE.getString("Settings.memory")); //$NON-NLS-1$
      add(lblMemoryT, "flowx,cell 1 5 4 1,aligny center");

      sliderMemory = new JSlider();
      add(sliderMemory, "cell 1 5 2 1,growx,aligny center");
      sliderMemory.setPaintLabels(true);
      sliderMemory.setPaintTicks(true);
      sliderMemory.setSnapToTicks(true);
      sliderMemory.setMajorTickSpacing(512);
      sliderMemory.setMinorTickSpacing(128);
      sliderMemory.setMinimum(256);
      sliderMemory.setMaximum(1536);
      sliderMemory.setValue(512);

      lblMemory = new JLabel("512");
      add(lblMemory, "cell 1 5 4 1,aligny center");

      JLabel lblMb = new JLabel("MB");
      add(lblMb, "cell 1 5 2 1,aligny center");

      JTextPane tpMemoryHint = new JTextPane();
      add(tpMemoryHint, "cell 1 6 5 1,growx");
      tpMemoryHint.setOpaque(false);
      tpMemoryHint.setText(BUNDLE.getString("Settings.memory.hint")); //$NON-NLS-1$
      TmmFontHelper.changeFont(tpMemoryHint, 0.833);
    }
    {
      final JLabel lblProxySettingsT = new JLabel(BUNDLE.getString("Settings.proxy")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblProxySettingsT, 1.16667, Font.BOLD);
      add(lblProxySettingsT, "cell 0 8 3 1");
    }
    {
      JLabel lblProxyHostT = new JLabel(BUNDLE.getString("Settings.proxyhost")); //$NON-NLS-1$
      add(lblProxyHostT, "cell 1 9,alignx right");

      tfProxyHost = new JTextField();
      add(tfProxyHost, "cell 2 9");
      tfProxyHost.setColumns(20);
      lblProxyHostT.setLabelFor(tfProxyHost);

      JLabel lblProxyPortT = new JLabel(BUNDLE.getString("Settings.proxyport")); //$NON-NLS-1$
      add(lblProxyPortT, "cell 3 9,alignx right");
      lblProxyPortT.setLabelFor(tfProxyPort);

      tfProxyPort = new JTextField();
      add(tfProxyPort, "cell 4 9");
      tfProxyPort.setColumns(20);

      JLabel lblProxyUserT = new JLabel(BUNDLE.getString("Settings.proxyuser")); //$NON-NLS-1$
      add(lblProxyUserT, "cell 1 10,alignx right");
      lblProxyUserT.setLabelFor(tfProxyUsername);

      tfProxyUsername = new JTextField();
      add(tfProxyUsername, "cell 2 10");
      tfProxyUsername.setColumns(20);

      JLabel lblProxyPasswordT = new JLabel(BUNDLE.getString("Settings.proxypass")); //$NON-NLS-1$
      add(lblProxyPasswordT, "cell 3 10,alignx right");
      lblProxyPasswordT.setLabelFor(tfProxyPassword);

      tfProxyPassword = new JPasswordField();
      tfProxyPassword.setColumns(20);
      add(tfProxyPassword, "cell 4 10");
    }
    {
      final JLabel lblMiscSettingsT = new JLabel(BUNDLE.getString("Settings.misc")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMiscSettingsT, 1.16667, Font.BOLD);
      add(lblMiscSettingsT, "cell 0 12 3 1");
    }
    {
      chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecache"));
      add(chckbxImageCache, "cell 1 13 2 1");

      JLabel lblImageCacheQuality = new JLabel(BUNDLE.getString("Settings.imagecachetype"));
      add(lblImageCacheQuality, "flowx,cell 2 14");

      cbImageCacheQuality = new JComboBox(ImageCache.CacheType.values());
      add(cbImageCacheQuality, "cell 2 14");

      chckbxDeleteTrash = new JCheckBox(BUNDLE.getString("Settings.deletetrash"));
      add(chckbxDeleteTrash, "cell 1 16 2 1");

      chckbxAnalytics = new JCheckBox(BUNDLE.getString("Settings.analytics"));
      add(chckbxAnalytics, "cell 1 18 2 1");

      JTextPane tpAnalyticsDescription = new JTextPane();
      add(tpAnalyticsDescription, "cell 1 19 5 1,growx");
      tpAnalyticsDescription.setText(BUNDLE.getString("Settings.analytics.desc"));//$NON-NLS-1$
      tpAnalyticsDescription.setOpaque(false);
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
    if (memoryAmount != 512 && Files.notExists(file)) {
      try {
        Utils.writeStringToFile(file, jvmArg);
      }
      catch (IOException ignored) {
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
      catch (Exception ignored) {
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
