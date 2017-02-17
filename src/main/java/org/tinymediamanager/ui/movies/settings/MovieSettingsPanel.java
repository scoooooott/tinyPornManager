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
package org.tinymediamanager.ui.movies.settings;

import java.awt.Font;
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
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.trakttv.ClearTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieSettingsPanel is used for displaying some movie related settings
 * 
 * @author Manuel Laggner
 */
public class MovieSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -4173835431245178069L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final MovieSettings         settings         = MovieModuleManager.SETTINGS;

  private JButton                     btnClearTraktData;
  private JCheckBox                   chckbxTraktSync;
  private JCheckBox                   chckbxRenameAfterScrape;
  private JCheckBox                   chckbxPersistUiFilters;
  private JCheckBox                   chckbxBuildImageCache;
  private JCheckBox                   chckbxRuntimeFromMi;
  private JCheckBox                   chckbxPersistUiSorting;
  private JButton                     btnPresetKodi;
  private JButton                     btnPresetXbmc;
  private JButton                     btnPresetMediaPortal1;
  private JButton                     btnPresetMediaPortal2;
  private JButton                     btnPresetPlex;

  public MovieSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // logic initializations
    btnClearTraktData.addActionListener(e -> {
      int confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("Settings.trakt.clearmovies.hint"),
          BUNDLE.getString("Settings.trakt.clearmovies"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null); //$NON-NLS-1$
      if (confirm == JOptionPane.YES_OPTION) {
        TmmTask task = new ClearTraktTvTask(true, false);
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }
    });

    btnPresetXbmc.addActionListener(evt -> settings.setDefaultSettingsForXbmc());
    btnPresetKodi.addActionListener(evt -> settings.setDefaultSettingsForKodi());
    btnPresetMediaPortal1.addActionListener(evt -> settings.setDefaultSettingsForMediaPortal1());
    btnPresetMediaPortal2.addActionListener(evt -> settings.setDefaultSettingsForMediaPortal2());
    btnPresetPlex.addActionListener(evt -> settings.setDefaultSettingsForPlex());
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp:n][][][]", "[][][][20lp][][][][20lp][][][][20lp][][][][][]"));
    {
      JLabel lblUiT = new JLabel(BUNDLE.getString("Settings.ui")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblUiT, 1.16667, Font.BOLD);
      add(lblUiT, "cell 0 0 4 1");
    }
    {
      chckbxPersistUiFilters = new JCheckBox(BUNDLE.getString("Settings.movie.persistuifilter")); //$NON-NLS-1$
      add(chckbxPersistUiFilters, "cell 1 1 3 1");
    }
    {
      chckbxPersistUiSorting = new JCheckBox(BUNDLE.getString("Settings.movie.persistuisorting")); //$NON-NLS-1$
      add(chckbxPersistUiSorting, "cell 1 2 3 1");
    }
    {
      JLabel lblAutomaticTasksT = new JLabel(BUNDLE.getString("Settings.automatictasks")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAutomaticTasksT, 1.16667, Font.BOLD);
      add(lblAutomaticTasksT, "cell 0 4 4 1");
    }
    {
      chckbxRenameAfterScrape = new JCheckBox(BUNDLE.getString("Settings.movie.automaticrename")); //$NON-NLS-1$
      add(chckbxRenameAfterScrape, "flowx,cell 1 5 3 1");
    }
    {
      JLabel lblAutomaticRenameHint = new JLabel(IconManager.HINT);
      lblAutomaticRenameHint.setToolTipText(BUNDLE.getString("Settings.movie.automaticrename.desc")); //$NON-NLS-1$
      add(lblAutomaticRenameHint, "cell 1 5 3 1");
    }
    {
      chckbxTraktSync = new JCheckBox(BUNDLE.getString("Settings.trakt")); //$NON-NLS-1$
      add(chckbxTraktSync, "flowx,cell 1 6 3 1");
    }
    {
      btnClearTraktData = new JButton(BUNDLE.getString("Settings.trakt.clearmovies")); //$NON-NLS-1$
      add(btnClearTraktData, "cell 1 6 3 1");
    }
    {
      JLabel lblMiscT = new JLabel(BUNDLE.getString("Settings.misc")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMiscT, 1.16667, Font.BOLD);
      add(lblMiscT, "cell 0 8 4 1");
    }
    {
      chckbxBuildImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecacheimport")); //$NON-NLS-1$
      add(chckbxBuildImageCache, "flowx,cell 1 9 3 1");
    }
    {
      JLabel lblBuildImageCacheHint = new JLabel(IconManager.HINT);
      lblBuildImageCacheHint.setToolTipText(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
      add(lblBuildImageCacheHint, "cell 1 9 3 1");
    }
    {
      chckbxRuntimeFromMi = new JCheckBox(BUNDLE.getString("Settings.runtimefrommediafile")); //$NON-NLS-1$
      add(chckbxRuntimeFromMi, "cell 1 10 3 1");
    }
    {
      JLabel lblPresetT = new JLabel(BUNDLE.getString("Settings.preset")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblPresetT, 1.16667, Font.BOLD);
      add(lblPresetT, "cell 0 12 4 1");
    }
    {
      JLabel lblPresetHintT = new JLabel(BUNDLE.getString("Settings.preset.desc")); //$NON-NLS-1$
      add(lblPresetHintT, "cell 1 13 3 1");
    }
    {
      btnPresetKodi = new JButton("Kodi v17+");
      add(btnPresetKodi, "flowx,cell 1 14,growx");

      btnPresetXbmc = new JButton("XBMC/Kodi <v17");
      add(btnPresetXbmc, "cell 2 14,growx");
    }
    {
      btnPresetMediaPortal1 = new JButton("MediaPortal 1.x");
      add(btnPresetMediaPortal1, "flowx,cell 1 15,growx");

      btnPresetMediaPortal2 = new JButton("MediaPortal 2.x");
      add(btnPresetMediaPortal2, "cell 2 15,growx");
    }
    {
      btnPresetPlex = new JButton("Plex");
      add(btnPresetPlex, "cell 1 16,growx");
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty = BeanProperty.create("storeUiFilters");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty, chckbxPersistUiFilters, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_1 = BeanProperty.create("movieRenameAfterScrape");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_1, chckbxRenameAfterScrape, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_2 = BeanProperty.create("syncTrakt");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_2, chckbxTraktSync, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_3 = BeanProperty.create("buildImageCacheOnImport");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_3, chckbxBuildImageCache, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_4 = BeanProperty.create("runtimeFromMediaInfo");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_4, chckbxRuntimeFromMi, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_5 = BeanProperty.create("storeUiSorting");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_5, chckbxPersistUiSorting, jCheckBoxBeanProperty);
    autoBinding_5.bind();
  }
}
