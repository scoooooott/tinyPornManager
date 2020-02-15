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
import static org.tinymediamanager.ui.TmmFontHelper.L2;

import java.awt.Dimension;
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
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;

import com.sun.jna.Platform;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MiscSettingsPanel.
 * 
 * @author Manuel Laggner
 */
class SystemSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 500841588272296493L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
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
  private JSpinner                    spMaximumDownloadThreads;

  /**
   * Instantiates a new general settings panel.
   */
  SystemSettingsPanel() {

    initComponents();

    initDataBindings();

    initMemorySlider();

    // data init
    btnSearchMediaPlayer.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("chooseplayer.path");
      Path file = TmmUIHelper.selectFile(BUNDLE.getString("Button.chooseplayer"), path, null);
      if (file != null && Utils.isRegularFile(file) || Platform.isMac()) {
        tfMediaPlayer.setText(file.toAbsolutePath().toString());
        TmmProperties.getInstance().putProperty("chooseplayer.path", file.getParent().toString());
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][15lp!][][15lp!][][15lp!][]"));
    {
      JPanel panelMediaPlayer = SettingsPanelFactory.createSettingsPanel();

      JLabel lblLanguageT = new TmmLabel(BUNDLE.getString("Settings.mediaplayer"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMediaPlayer, lblLanguageT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        tfMediaPlayer = new JTextField();
        panelMediaPlayer.add(tfMediaPlayer, "cell 1 0 2 1");
        tfMediaPlayer.setColumns(35);

        btnSearchMediaPlayer = new JButton(BUNDLE.getString("Button.chooseplayer"));
        panelMediaPlayer.add(btnSearchMediaPlayer, "cell 1 0");

        JTextArea tpMediaPlayer = new ReadOnlyTextArea(BUNDLE.getString("Settings.mediaplayer.hint"));
        panelMediaPlayer.add(tpMediaPlayer, "cell 1 1 2 1,growx");
        TmmFontHelper.changeFont(tpMediaPlayer, L2);
      }
    }
    {
      JPanel panelMemory = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][][300lp][grow]", ""));

      JLabel lblMemoryT = new TmmLabel(BUNDLE.getString("Settings.memoryborder"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMemory, lblMemoryT, true);
      add(collapsiblePanel, "cell 0 2,growx,wmin 0");
      {
        lblMemoryT = new JLabel(BUNDLE.getString("Settings.memory"));
        panelMemory.add(lblMemoryT, "cell 1 0,aligny top");

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
        panelMemory.add(sliderMemory, "cell 2 0,growx,aligny top");

        lblMemory = new JLabel("512");
        panelMemory.add(lblMemory, "cell 3 0,aligny top");

        JLabel lblMb = new JLabel("MB");
        panelMemory.add(lblMb, "cell 3 0,aligny top");

        JTextArea tpMemoryHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.memory.hint"));
        panelMemory.add(tpMemoryHint, "cell 1 1 3 1,growx");
        TmmFontHelper.changeFont(tpMemoryHint, L2);
      }
    }
    {
      JPanel panelProxy = SettingsPanelFactory.createSettingsPanel();

      JLabel lblProxyT = new TmmLabel(BUNDLE.getString("Settings.proxy"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelProxy, lblProxyT, true);
      add(collapsiblePanel, "cell 0 4,growx,wmin 0");
      {
        JLabel lblProxyHostT = new JLabel(BUNDLE.getString("Settings.proxyhost"));
        panelProxy.add(lblProxyHostT, "cell 1 0,alignx right");

        tfProxyHost = new JTextField();
        panelProxy.add(tfProxyHost, "cell 2 0");
        tfProxyHost.setColumns(20);
        lblProxyHostT.setLabelFor(tfProxyHost);

        JLabel lblProxyPortT = new JLabel(BUNDLE.getString("Settings.proxyport"));
        panelProxy.add(lblProxyPortT, "cell 1 1,alignx right");
        lblProxyPortT.setLabelFor(tfProxyPort);

        tfProxyPort = new JTextField();
        panelProxy.add(tfProxyPort, "cell 2 1");
        tfProxyPort.setColumns(20);

        JLabel lblProxyUserT = new JLabel(BUNDLE.getString("Settings.proxyuser"));
        panelProxy.add(lblProxyUserT, "cell 1 2,alignx right");
        lblProxyUserT.setLabelFor(tfProxyUsername);

        tfProxyUsername = new JTextField();
        panelProxy.add(tfProxyUsername, "cell 2 2");
        tfProxyUsername.setColumns(20);

        JLabel lblProxyPasswordT = new JLabel(BUNDLE.getString("Settings.proxypass"));
        panelProxy.add(lblProxyPasswordT, "cell 1 3,alignx right");
        lblProxyPasswordT.setLabelFor(tfProxyPassword);

        tfProxyPassword = new JPasswordField();
        tfProxyPassword.setColumns(20);
        panelProxy.add(tfProxyPassword, "cell 2 3");
      }
    }
    {
      JPanel panelMisc = SettingsPanelFactory.createSettingsPanel();

      JLabel lblMiscT = new TmmLabel(BUNDLE.getString("Settings.misc"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMisc, lblMiscT, true);
      add(collapsiblePanel, "cell 0 6,growx,wmin 0");
      {
        JLabel lblParallelDownloadCountT = new JLabel(BUNDLE.getString("Settings.paralleldownload"));
        panelMisc.add(lblParallelDownloadCountT, "cell 1 0 2 1");

        spMaximumDownloadThreads = new JSpinner();
        spMaximumDownloadThreads.setMinimumSize(new Dimension(60, 20));
        panelMisc.add(spMaximumDownloadThreads, "cell 1 0 2 1");

        chckbxIgnoreSSLProblems = new JCheckBox(BUNDLE.getString("Settings.ignoressl"));
        panelMisc.add(chckbxIgnoreSSLProblems, "cell 1 1 2 1");
      }
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
    //
    BeanProperty<Settings, Integer> settingsBeanProperty_5 = BeanProperty.create("maximumDownloadThreads");
    BeanProperty<JSpinner, Object> jSpinnerBeanProperty = BeanProperty.create("value");
    AutoBinding<Settings, Integer, JSpinner, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, spMaximumDownloadThreads, jSpinnerBeanProperty);
    autoBinding_5.bind();
  }
}
