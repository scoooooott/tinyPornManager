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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;

import com.sun.jna.Platform;

import net.miginfocom.swing.MigLayout;

/**
 * The Class GeneralSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class SystemSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 500841588272296493L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Pattern        MEMORY_PATTERN   = Pattern.compile("-Xmx([0-9]*)(.)");

  private Settings                    settings         = Settings.getInstance();

  private JTextField                  tfProxyHost;
  private JTextField                  tfProxyPort;
  private JTextField                  tfProxyUsername;
  private JPasswordField              tfProxyPassword;
  private JTextField                  tfMediaPlayer;
  private JButton                     btnSearchMediaPlayer;
  private JSlider                     sliderMemory;
  private JLabel                      lblMemory;
  private JCheckBox                   chckbxIgnoreSSLProblems;

  /**
   * Instantiates a new general settings panel.
   */
  public SystemSettingsPanel() {

    initComponents();

    initDataBindings();

    initMemorySlider();

    // data init
    btnSearchMediaPlayer.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("chooseplayer.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectFile(BUNDLE.getString("Button.chooseplayer"), path); //$NON-NLS-1$
      if (file != null && Utils.isRegularFile(file) || Platform.isMac()) {
        tfMediaPlayer.setText(file.toAbsolutePath().toString());
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp:n][][][][][grow]", "[][][][20lp][][][][20lp][][][][20lp][][]"));
    {
      final JLabel lblMediaPlayerT = new JLabel(BUNDLE.getString("Settings.mediaplayer")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMediaPlayerT, 1.16667, Font.BOLD);
      add(lblMediaPlayerT, "cell 0 0 3 1");
    }
    {
      tfMediaPlayer = new JTextField();
      add(tfMediaPlayer, "flowx,cell 1 1 5 1");
      tfMediaPlayer.setColumns(35);

      btnSearchMediaPlayer = new JButton(BUNDLE.getString("Button.chooseplayer")); //$NON-NLS-1$
      add(btnSearchMediaPlayer, "cell 1 1 5 1");

      JTextArea tpMediaPlayer = new ReadOnlyTextArea(BUNDLE.getString("Settings.mediaplayer.hint")); //$NON-NLS-1$
      add(tpMediaPlayer, "cell 1 2 5 1,growx");
      TmmFontHelper.changeFont(tpMediaPlayer, 0.833);
    }
    {
      final JLabel lblMemorySettingsT = new JLabel(BUNDLE.getString("Settings.memoryborder")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMemorySettingsT, 1.16667, Font.BOLD);
      add(lblMemorySettingsT, "cell 0 4 3 1");
    }
    {
      JLabel lblMemoryT = new JLabel(BUNDLE.getString("Settings.memory")); //$NON-NLS-1$
      add(lblMemoryT, "flowx,cell 1 5 4 1,aligny top");

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
      add(sliderMemory, "cell 1 5 4 1,growx,aligny top");

      lblMemory = new JLabel("512");
      add(lblMemory, "cell 1 5 4 1,aligny top");

      JLabel lblMb = new JLabel("MB");
      add(lblMb, "cell 1 5 4 1,aligny top");

      JTextArea tpMemoryHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.memory.hint")); //$NON-NLS-1$
      add(tpMemoryHint, "cell 1 6 5 1,growx");
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
      final JLabel lblMiscT = new TmmLabel(BUNDLE.getString("Settings.misc"), 1.16667);
      add(lblMiscT, "cell 0 12 6 1");
    }
    {
      chckbxIgnoreSSLProblems = new JCheckBox(BUNDLE.getString("Settings.ignoressl"));
      add(chckbxIgnoreSSLProblems, "cell 1 13 5 1");
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
    BeanProperty<Settings, String> settingsBeanProperty_6 = BeanProperty.create("mediaPlayer");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, tfMediaPlayer, jTextFieldBeanProperty_3);
    autoBinding_9.bind();
    //
    BeanProperty<JSlider, Integer> jSliderBeanProperty = BeanProperty.create("value");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JSlider, Integer, JLabel, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ, sliderMemory, jSliderBeanProperty,
        lblMemory, jLabelBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_4 = BeanProperty.create("ignoreSSLProblems");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxIgnoreSSLProblems, jCheckBoxBeanProperty);
    autoBinding_4.bind();
  }
}
