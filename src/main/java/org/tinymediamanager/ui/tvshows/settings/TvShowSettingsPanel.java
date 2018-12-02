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
package org.tinymediamanager.ui.tvshows.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.trakttv.ClearTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowSettingsPanel.
 * 
 * @author Manuel Laggner
 */
class TvShowSettingsPanel extends JPanel {
  private static final long            serialVersionUID = -675729644848101096L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings               settings         = TvShowModuleManager.SETTINGS;
  private JCheckBox                    chckbxImageCache;
  private JCheckBox                    chckbxTraktTv;
  private JButton                      btnClearTraktTvShows;
  private JCheckBox                    chckbxPersistUiFilters;
  private JCheckBox                    chckbxShowMissingEpisodes;
  private JButton                      btnPresetKodi;
  private JButton                      btnPresetXbmc;
  private JButton                      btnPresetMediaPortal1;
  private JButton                      btnPresetMediaPortal2;
  private JButton                      btnPresetPlex;
  private JCheckBox                    chckbxPersonalRatingFirst;
  private AutocompleteComboBox<String> cbRating;
  private JCheckBox                    chckbxRenameAfterScrape;
  private JCheckBox                    chckbxShowMissingSpecials;

  /**
   * Instantiates a new tv show settings panel.
   */
  TvShowSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // logic initializations
    btnClearTraktTvShows.addActionListener(e -> {
      int confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("Settings.trakt.cleartvshows.hint"),
          BUNDLE.getString("Settings.trakt.cleartvshows"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null); //$NON-NLS-1$
      if (confirm == JOptionPane.YES_OPTION) {
        TmmTask task = new ClearTraktTvTask(false, true);
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }
    });

    btnPresetXbmc.addActionListener(evt -> settings.setDefaultSettingsForXbmc());
    btnPresetKodi.addActionListener(evt -> settings.setDefaultSettingsForKodi());
    btnPresetMediaPortal1.addActionListener(evt -> settings.setDefaultSettingsForMediaPortal());
    btnPresetMediaPortal2.addActionListener(evt -> settings.setDefaultSettingsForMediaPortal());
    btnPresetPlex.addActionListener(evt -> settings.setDefaultSettingsForPlex());
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][15lp!][][15lp!][][15lp!][]"));
    {
      JPanel panelUiSettings = SettingsPanelFactory.createSettingsPanel();

      JLabel lblUiSettings = new TmmLabel(BUNDLE.getString("Settings.ui"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelUiSettings, lblUiSettings, true);
      add(collapsiblePanel, "cell 0 0,growx,wmin 0");
      {
        chckbxPersistUiFilters = new JCheckBox(BUNDLE.getString("Settings.movie.persistuifilter")); //$NON-NLS-1$
        panelUiSettings.add(chckbxPersistUiFilters, "cell 1 0 2 1");

        chckbxShowMissingEpisodes = new JCheckBox(BUNDLE.getString("Settings.tvshow.missingepisodes")); //$NON-NLS-1$
        panelUiSettings.add(chckbxShowMissingEpisodes, "cell 1 1 2 1");

        chckbxShowMissingSpecials = new JCheckBox(BUNDLE.getString("Settings.tvshow.missingespecials")); //$NON-NLS-1$
        panelUiSettings.add(chckbxShowMissingSpecials, "cell 2 2");

        JLabel lblRating = new JLabel(BUNDLE.getString("Settings.preferredrating")); //$NON-NLS-1$
        panelUiSettings.add(lblRating, "flowx,cell 1 3 2 1");

        cbRating = new AutocompleteComboBox(Arrays.asList("tvdb", "tmdb"));
        panelUiSettings.add(cbRating, "cell 1 3");

        chckbxPersonalRatingFirst = new JCheckBox(BUNDLE.getString("Settings.personalratingfirst")); //$NON-NLS-1$
        panelUiSettings.add(chckbxPersonalRatingFirst, "cell 2 4");
      }
    }
    {
      JPanel panelAutomaticTasks = SettingsPanelFactory.createSettingsPanel();

      JLabel lblAutomaticTasksT = new TmmLabel(BUNDLE.getString("Settings.automatictasks"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelAutomaticTasks, lblAutomaticTasksT, true);
      add(collapsiblePanel, "cell 0 2,growx,wmin 0");
      {
        chckbxRenameAfterScrape = new JCheckBox(BUNDLE.getString("Settings.tvshow.automaticrename")); //$NON-NLS-1$
        panelAutomaticTasks.add(chckbxRenameAfterScrape, "cell 1 0 2 1");

        JLabel lblAutomaticRenameHint = new JLabel(IconManager.HINT);
        lblAutomaticRenameHint.setToolTipText(BUNDLE.getString("Settings.tvshow.automaticrename.desc")); //$NON-NLS-1$
        panelAutomaticTasks.add(lblAutomaticRenameHint, "cell 1 0");

        chckbxTraktTv = new JCheckBox(BUNDLE.getString("Settings.trakt"));//$NON-NLS-1$
        panelAutomaticTasks.add(chckbxTraktTv, "cell 1 1 2 1");

        btnClearTraktTvShows = new JButton(BUNDLE.getString("Settings.trakt.cleartvshows"));//$NON-NLS-1$
        panelAutomaticTasks.add(btnClearTraktTvShows, "cell 1 1");
      }
    }
    {
      JPanel panelMisc = SettingsPanelFactory.createSettingsPanel();

      JLabel lblMiscT = new TmmLabel(BUNDLE.getString("Settings.misc"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMisc, lblMiscT, true);
      add(collapsiblePanel, "cell 0 4,growx,wmin 0");
      {
        chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecacheimport")); //$NON-NLS-1$
        panelMisc.add(chckbxImageCache, "cell 1 0 2 1");

        JLabel lblBuildImageCacheHint = new JLabel(IconManager.HINT);
        lblBuildImageCacheHint.setToolTipText(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
        panelMisc.add(lblBuildImageCacheHint, "cell 1 0");
      }
    }
    {
      JPanel panelPresets = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][15lp][][][grow]", "[]"));

      JLabel lblPresets = new TmmLabel(BUNDLE.getString("Settings.preset"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelPresets, lblPresets, true);
      add(collapsiblePanel, "cell 0 6,growx,wmin 0");
      {

        {
          JLabel lblPresetHintT = new JLabel(BUNDLE.getString("Settings.preset.desc")); //$NON-NLS-1$
          panelPresets.add(lblPresetHintT, "cell 1 0 3 1");
        }
        {
          btnPresetKodi = new JButton("Kodi v17+");
          panelPresets.add(btnPresetKodi, "cell 2 1,growx");

          btnPresetXbmc = new JButton("XBMC/Kodi <v17");
          panelPresets.add(btnPresetXbmc, "cell 3 1,growx");

          btnPresetMediaPortal1 = new JButton("MediaPortal 1.x");
          panelPresets.add(btnPresetMediaPortal1, "cell 2 2,growx");

          btnPresetMediaPortal2 = new JButton("MediaPortal 2.x");
          panelPresets.add(btnPresetMediaPortal2, "cell 3 2,growx");

          btnPresetPlex = new JButton("Plex");
          panelPresets.add(btnPresetPlex, "cell 2 3,growx");
        }
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty = BeanProperty.create("syncTrakt");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxTraktTv, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty = BeanProperty.create("buildImageCacheOnImport");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty, chckbxImageCache, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_1 = BeanProperty.create("storeUiFilters");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_1, chckbxPersistUiFilters, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_2 = BeanProperty.create("displayMissingEpisodes");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_2, chckbxShowMissingEpisodes, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSettings, String> tvShowSettingsBeanProperty_3 = BeanProperty.create("preferredRating");
    BeanProperty<AutocompleteComboBox, Object> autocompleteComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<TvShowSettings, String, AutocompleteComboBox, Object> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_3, cbRating, autocompleteComboBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_4 = BeanProperty.create("preferPersonalRating");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_4, chckbxPersonalRatingFirst, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_5 = BeanProperty.create("renameAfterScrape");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_5, chckbxRenameAfterScrape, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, chckbxShowMissingEpisodes,
        jCheckBoxBeanProperty, chckbxShowMissingSpecials, jCheckBoxBeanProperty_1);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_6 = BeanProperty.create("displayMissingSpecials");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_6, chckbxShowMissingSpecials, jCheckBoxBeanProperty);
    autoBinding_8.bind();
  }
}
