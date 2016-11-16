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
package org.tinymediamanager.ui.tvshows.settings;

import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.trakttv.ClearTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = -675729644848101096L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private JCheckBox                   chckbxImageCache;
  private JCheckBox                   chckbxTraktTv;
  private JButton                     btnClearTraktTvShows;
  private JCheckBox                   chckbxPersistUiFilters;
  private JCheckBox                   chckbxShowMissingEpisodes;

  /**
   * Instantiates a new tv show settings panel.
   */
  public TvShowSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // logic initializations
    if (!Globals.isDonator()) {
      chckbxTraktTv.setSelected(false);
      chckbxTraktTv.setEnabled(false);
      btnClearTraktTvShows.setEnabled(false);
    }
    btnClearTraktTvShows.addActionListener(e -> {
      int confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("Settings.trakt.cleartvshows.hint"),
          BUNDLE.getString("Settings.trakt.cleartvshows"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null); //$NON-NLS-1$
      if (confirm == JOptionPane.YES_OPTION) {
        TmmTask task = new ClearTraktTvTask(false, true);
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][]", "[][][][20lp][][][20lp][][][]"));
    {
      JLabel lblUiT = new JLabel(BUNDLE.getString("Settings.ui")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblUiT, 1.16667, Font.BOLD);
      add(lblUiT, "cell 0 0 2 1");
    }
    {
      chckbxPersistUiFilters = new JCheckBox(BUNDLE.getString("Settings.movie.persistuifilter")); //$NON-NLS-1$
      add(chckbxPersistUiFilters, "cell 1 1");
    }
    {
      chckbxShowMissingEpisodes = new JCheckBox(BUNDLE.getString("Settings.tvshow.missingepisodes")); //$NON-NLS-1$
      add(chckbxShowMissingEpisodes, "cell 1 2");
    }
    {
      JLabel lblAutomaticTasksT = new JLabel(BUNDLE.getString("Settings.automatictasks")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAutomaticTasksT, 1.16667, Font.BOLD);
      add(lblAutomaticTasksT, "cell 0 4 2 1");
    }

    {
      chckbxTraktTv = new JCheckBox(BUNDLE.getString("Settings.trakt"));//$NON-NLS-1$
      add(chckbxTraktTv, "flowx,cell 1 5");
      btnClearTraktTvShows = new JButton(BUNDLE.getString("Settings.trakt.cleartvshows"));//$NON-NLS-1$
      add(btnClearTraktTvShows, "cell 1 5");
    }

    {
      JLabel lblMiscT = new JLabel(BUNDLE.getString("Settings.misc")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMiscT, 1.16667, Font.BOLD);
      add(lblMiscT, "cell 0 7 2 1");
    }
    {
      chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecacheimport")); //$NON-NLS-1$
      add(chckbxImageCache, "flowx,cell 1 8");

      JLabel lblBuildImageCacheHint = new JLabel(IconManager.HINT);
      lblBuildImageCacheHint.setToolTipText(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
      add(lblBuildImageCacheHint, "cell 1 8");
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
  }
}
