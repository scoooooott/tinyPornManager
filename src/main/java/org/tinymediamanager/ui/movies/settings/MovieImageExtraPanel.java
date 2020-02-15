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
package org.tinymediamanager.ui.movies.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.awt.Dimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieImageSettingsPanel.
 * 
 * @author Manuel Laggner
 */
class MovieImageExtraPanel extends JPanel {
  private static final long           serialVersionUID = 7312645402037806284L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private MovieSettings               settings         = MovieModuleManager.SETTINGS;
  private JCheckBox                   cbActorImages;
  private JCheckBox                   chckbxEnableExtrathumbs;
  private JCheckBox                   chckbxEnableExtrafanart;
  private JCheckBox                   chckbxResizeExtrathumbsTo;
  private JSpinner                    spExtrathumbWidth;
  private JSpinner                    spDownloadCountExtrathumbs;
  private JSpinner                    spDownloadCountExtrafanart;
  private JCheckBox                   chckbxStoreMoviesetArtwork;
  private JTextField                  tfMovieSetArtworkFolder;
  private JCheckBox                   chckbxMovieSetArtwork;
  private JButton                     btnSelectFolder;
  private JCheckBox                   chckxKodiStyle;
  private JCheckBox                   chckxAutomatorStyle;

  /**
   * Instantiates a new movie image settings panel.
   */
  MovieImageExtraPanel() {

    // UI init
    initComponents();
    initDataBindings();

    // further initializations
    btnSelectFolder.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("movieset.folderchooser.path");
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.movieset.folderchooser"), path);
      if (file != null && Files.isDirectory(file)) {
        tfMovieSetArtworkFolder.setText(file.toAbsolutePath().toString());
        TmmProperties.getInstance().putProperty("movieset.folderchooser.path", file.toAbsolutePath().toString());
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][]"));
    {
      JPanel panelExtra = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][16lp!][grow]", ""));

      JLabel lblExtra = new TmmLabel(BUNDLE.getString("Settings.extraartwork"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelExtra, lblExtra, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        chckbxEnableExtrathumbs = new JCheckBox(BUNDLE.getString("Settings.enable.extrathumbs"));
        panelExtra.add(chckbxEnableExtrathumbs, "cell 1 0 2 1");

        chckbxResizeExtrathumbsTo = new JCheckBox(BUNDLE.getString("Settings.resize.extrathumbs"));
        panelExtra.add(chckbxResizeExtrathumbsTo, "cell 2 1");

        spExtrathumbWidth = new JSpinner();
        spExtrathumbWidth.setMinimumSize(new Dimension(60, 20));
        panelExtra.add(spExtrathumbWidth, "cell 2 1");

        JLabel lblDownload = new JLabel(BUNDLE.getString("Settings.amount.autodownload"));
        panelExtra.add(lblDownload, "cell 2 2");

        spDownloadCountExtrathumbs = new JSpinner();
        spDownloadCountExtrathumbs.setMinimumSize(new Dimension(60, 20));
        panelExtra.add(spDownloadCountExtrathumbs, "cell 2 2");

        chckbxEnableExtrafanart = new JCheckBox(BUNDLE.getString("Settings.enable.extrafanart"));
        panelExtra.add(chckbxEnableExtrafanart, "cell 1 3 2 1");

        JLabel lblDownloadCount = new JLabel(BUNDLE.getString("Settings.amount.autodownload"));
        panelExtra.add(lblDownloadCount, "cell 2 4");

        spDownloadCountExtrafanart = new JSpinner();
        spDownloadCountExtrafanart.setMinimumSize(new Dimension(60, 20));
        panelExtra.add(spDownloadCountExtrafanart, "cell 2 4");

        cbActorImages = new JCheckBox(BUNDLE.getString("Settings.actor.download"));
        panelExtra.add(cbActorImages, "cell 1 5 2 1");
      }
    }
    {
      JPanel panelMovieSet = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][16lp!][grow]", "[][][grow]"));

      JLabel lblTitle = new TmmLabel(BUNDLE.getString("Settings.movieset"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMovieSet, lblTitle, true);
      add(collapsiblePanel, "cell 0 1,growx, wmin 0");
      {
        chckbxMovieSetArtwork = new JCheckBox(BUNDLE.getString("Settings.movieset.store.movie"));
        panelMovieSet.add(chckbxMovieSetArtwork, "cell 1 0 2 1");

        chckbxStoreMoviesetArtwork = new JCheckBox(BUNDLE.getString("Settings.movieset.store"));
        panelMovieSet.add(chckbxStoreMoviesetArtwork, "cell 1 1 2 1");

        {

          JPanel panelFolderSettings = new JPanel();
          panelMovieSet.add(panelFolderSettings, "cell 2 2,grow");
          panelFolderSettings.setLayout(new MigLayout("insets 0", "[][grow]", "[][][]"));
          JLabel lblFoldername = new JLabel(BUNDLE.getString("Settings.movieset.foldername"));
          panelFolderSettings.add(lblFoldername, "cell 0 0,alignx right");

          tfMovieSetArtworkFolder = new JTextField();
          panelFolderSettings.add(tfMovieSetArtworkFolder, "flowx,cell 1 0");
          tfMovieSetArtworkFolder.setColumns(40);

          JLabel folderStyle = new JLabel(BUNDLE.getString("Settings.movieset.foldername.style"));
          panelFolderSettings.add(folderStyle, "cell 0 1,alignx right");

          chckxKodiStyle = new JCheckBox(BUNDLE.getString("Settings.movieset.foldername.kodi"));
          panelFolderSettings.add(chckxKodiStyle, "cell 1 1");

          chckxAutomatorStyle = new JCheckBox(BUNDLE.getString("Settings.movieset.foldername.automator"));
          panelFolderSettings.add(chckxAutomatorStyle, "cell 1 2");

          ButtonGroup buttonGroup = new ButtonGroup();
          buttonGroup.add(chckxKodiStyle);
          buttonGroup.add(chckxAutomatorStyle);

          btnSelectFolder = new JButton(BUNDLE.getString("Settings.movieset.buttonselect"));
          panelFolderSettings.add(btnSelectFolder, "cell 1 0");
        }
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_2 = BeanProperty.create("writeActorImages");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, cbActorImages, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_3 = BeanProperty.create("imageExtraFanart");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, chckbxEnableExtrafanart, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_4 = BeanProperty.create("imageExtraThumbs");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxEnableExtrathumbs, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MovieSettings, Integer> settingsBeanProperty_8 = BeanProperty.create("imageExtraThumbsSize");
    BeanProperty<JSpinner, Object> jSpinnerBeanProperty_1 = BeanProperty.create("value");
    AutoBinding<MovieSettings, Integer, JSpinner, Object> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, spExtrathumbWidth, jSpinnerBeanProperty_1);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_9 = BeanProperty.create("imageExtraThumbsResize");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxResizeExtrathumbsTo, jCheckBoxBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<MovieSettings, Integer> settingsBeanProperty_10 = BeanProperty.create("imageExtraThumbsCount");
    AutoBinding<MovieSettings, Integer, JSpinner, Object> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, spDownloadCountExtrathumbs, jSpinnerBeanProperty_1);
    autoBinding_12.bind();
    //
    BeanProperty<MovieSettings, Integer> settingsBeanProperty_11 = BeanProperty.create("imageExtraFanartCount");
    AutoBinding<MovieSettings, Integer, JSpinner, Object> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, spDownloadCountExtrafanart, jSpinnerBeanProperty_1);
    autoBinding_13.bind();
    //
    BeanProperty<JSpinner, Boolean> jSpinnerBeanProperty = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrafanart,
        jCheckBoxBeanProperty, spDownloadCountExtrafanart, jSpinnerBeanProperty);
    autoBinding_14.bind();
    //
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrathumbs,
        jCheckBoxBeanProperty, spDownloadCountExtrathumbs, jSpinnerBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<MovieSettings, String> settingsBeanProperty_12 = BeanProperty.create("movieSetArtworkFolder");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSettings, String, JTextField, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieSetArtworkFolder, jTextFieldBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_13 = BeanProperty.create("enableMovieSetArtworkFolder");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, chckbxStoreMoviesetArtwork, jCheckBoxBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrathumbs,
        jCheckBoxBeanProperty, chckbxResizeExtrathumbsTo, jCheckBoxBeanProperty_1);
    autoBinding_8.bind();
    //
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrathumbs,
        jCheckBoxBeanProperty, spExtrathumbWidth, jSpinnerBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_18 = BeanProperty.create("enableMovieSetArtworkMovieFolder");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_22 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_18, chckbxMovieSetArtwork, jCheckBoxBeanProperty);
    autoBinding_22.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty = BeanProperty.create("movieSetArtworkFolderStyleKodi");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty, chckxKodiStyle, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_1 = BeanProperty.create("movieSetArtworkFolderStyleAutomator");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_1, chckxAutomatorStyle, jCheckBoxBeanProperty);
    autoBinding_1.bind();
  }
}
