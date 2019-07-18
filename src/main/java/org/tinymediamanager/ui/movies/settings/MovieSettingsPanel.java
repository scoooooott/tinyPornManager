/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import java.awt.event.ItemListener;
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
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.trakttv.ClearTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieSettingsPanel is used for displaying some movie related settings
 * 
 * @author Manuel Laggner
 */
public class MovieSettingsPanel extends JPanel {
  private static final long            serialVersionUID = -4173835431245178069L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final MovieSettings          settings         = MovieModuleManager.SETTINGS;

  private JButton                      btnClearTraktData;
  private JCheckBox                    chckbxTraktSync;
  private JCheckBox                    chckbxRenameAfterScrape;
  private JCheckBox                    chckbxPersistUiFilters;
  private JCheckBox                    chckbxBuildImageCache;
  private JCheckBox                    chckbxRuntimeFromMi;
  private JCheckBox                    chckbxPersistUiSorting;
  private JCheckBox                    chckbxShowLogos;
  private JButton                      btnPresetKodi;
  private JButton                      btnPresetXbmc;
  private JButton                      btnPresetMediaPortal1;
  private JButton                      btnPresetMediaPortal2;
  private JButton                      btnPresetPlex;
  private JCheckBox                    chckbxPersonalRatingFirst;
  private AutocompleteComboBox<String> cbRating;
  private JCheckBox                    chckbxIncludeExternalAudioStreams;

  private JCheckBox                    chckbxCheckPoster;
  private JCheckBox                    chckbxCheckFanart;
  private JCheckBox                    chckbxCheckBanner;
  private JCheckBox                    chckbxCheckClearart;
  private JCheckBox                    chckbxCheckThumb;
  private JCheckBox                    chckbxCheckLogo;
  private JCheckBox                    chckbxCheckClearlogo;
  private JCheckBox                    chckbxCheckDiscart;

  private ItemListener                 checkBoxListener;

  public MovieSettingsPanel() {

    checkBoxListener = e -> checkChanges();

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

    buildCheckBoxes();
  }

  private void checkChanges() {
    settings.clearCheckImagesMovie();
    if (chckbxCheckPoster.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.POSTER);
    }
    if (chckbxCheckFanart.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.BACKGROUND);
    }
    if (chckbxCheckBanner.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.BANNER);
    }
    if (chckbxCheckClearart.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.CLEARART);
    }
    if (chckbxCheckThumb.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.THUMB);
    }
    if (chckbxCheckLogo.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.LOGO);
    }
    if (chckbxCheckClearlogo.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.CLEARLOGO);
    }
    if (chckbxCheckDiscart.isSelected()) {
      settings.addCheckImagesMovie(MediaArtwork.MediaArtworkType.DISC);
    }
  }

  private void buildCheckBoxes() {
    chckbxCheckPoster.removeItemListener(checkBoxListener);
    chckbxCheckFanart.removeItemListener(checkBoxListener);
    chckbxCheckBanner.removeItemListener(checkBoxListener);
    chckbxCheckClearart.removeItemListener(checkBoxListener);
    chckbxCheckThumb.removeItemListener(checkBoxListener);
    chckbxCheckLogo.removeItemListener(checkBoxListener);
    chckbxCheckClearlogo.removeItemListener(checkBoxListener);
    chckbxCheckDiscart.removeItemListener(checkBoxListener);
    clearSelection(chckbxCheckPoster, chckbxCheckFanart, chckbxCheckBanner, chckbxCheckClearart, chckbxCheckThumb, chckbxCheckLogo,
        chckbxCheckClearlogo, chckbxCheckDiscart);

    for (MediaArtwork.MediaArtworkType type : settings.getCheckImagesMovie()) {
      switch (type) {
        case POSTER:
          chckbxCheckPoster.setSelected(true);
          break;
        case BACKGROUND:
          chckbxCheckFanart.setSelected(true);
          break;

        case BANNER:
          chckbxCheckBanner.setSelected(true);
          break;

        case CLEARART:
          chckbxCheckClearart.setSelected(true);
          break;

        case THUMB:
          chckbxCheckThumb.setSelected(true);
          break;

        case LOGO:
          chckbxCheckLogo.setSelected(true);
          break;

        case CLEARLOGO:
          chckbxCheckClearlogo.setSelected(true);
          break;

        case DISC:
          chckbxCheckDiscart.setSelected(true);
          break;

        default:
          break;
      }
    }

    chckbxCheckPoster.addItemListener(checkBoxListener);
    chckbxCheckFanart.addItemListener(checkBoxListener);
    chckbxCheckBanner.addItemListener(checkBoxListener);
    chckbxCheckClearart.addItemListener(checkBoxListener);
    chckbxCheckThumb.addItemListener(checkBoxListener);
    chckbxCheckLogo.addItemListener(checkBoxListener);
    chckbxCheckClearlogo.addItemListener(checkBoxListener);
    chckbxCheckDiscart.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkbox : checkBoxes) {
      checkbox.setSelected(false);
    }
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
      }
      {
        chckbxPersistUiSorting = new JCheckBox(BUNDLE.getString("Settings.movie.persistuisorting")); //$NON-NLS-1$
        panelUiSettings.add(chckbxPersistUiSorting, "cell 1 1 2 1");
      }
      {
        chckbxShowLogos = new JCheckBox(BUNDLE.getString("Settings.showlogos")); //$NON-NLS-1$
        panelUiSettings.add(chckbxShowLogos, "cell 1 2 2 1");
      }
      {
        JLabel lblRating = new JLabel(BUNDLE.getString("Settings.preferredrating")); //$NON-NLS-1$
        panelUiSettings.add(lblRating, "cell 1 3 2 1");

        cbRating = new AutocompleteComboBox<>(Arrays.asList("imdb", "tmdb", "rottenTomatoes"));
        panelUiSettings.add(cbRating, "cell 1 3");
      }
      {
        chckbxPersonalRatingFirst = new JCheckBox(BUNDLE.getString("Settings.personalratingfirst")); //$NON-NLS-1$
        panelUiSettings.add(chckbxPersonalRatingFirst, "cell 2 4 2 1");
      }
    }
    {
      JPanel panelAutomaticTasks = SettingsPanelFactory.createSettingsPanel();

      JLabel lblAutomaticTasksT = new TmmLabel(BUNDLE.getString("Settings.automatictasks"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelAutomaticTasks, lblAutomaticTasksT, true);
      add(collapsiblePanel, "cell 0 2,growx,wmin 0");
      {
        chckbxRenameAfterScrape = new JCheckBox(BUNDLE.getString("Settings.movie.automaticrename")); //$NON-NLS-1$
        panelAutomaticTasks.add(chckbxRenameAfterScrape, "cell 1 0 2 1");

        JLabel lblAutomaticRenameHint = new JLabel(IconManager.HINT);
        lblAutomaticRenameHint.setToolTipText(BUNDLE.getString("Settings.movie.automaticrename.desc")); //$NON-NLS-1$
        panelAutomaticTasks.add(lblAutomaticRenameHint, "cell 1 0 2 1");

        chckbxTraktSync = new JCheckBox(BUNDLE.getString("Settings.trakt")); //$NON-NLS-1$
        panelAutomaticTasks.add(chckbxTraktSync, "cell 1 1 2 1");

        btnClearTraktData = new JButton(BUNDLE.getString("Settings.trakt.clearmovies")); //$NON-NLS-1$
        panelAutomaticTasks.add(btnClearTraktData, "cell 1 1 2 1");
      }
    }
    {
      JPanel panelMisc = SettingsPanelFactory.createSettingsPanel();

      JLabel lblMiscT = new TmmLabel(BUNDLE.getString("Settings.misc"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMisc, lblMiscT, true);
      add(collapsiblePanel, "cell 0 4,growx,wmin 0");
      {
        chckbxBuildImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecacheimport")); //$NON-NLS-1$
        panelMisc.add(chckbxBuildImageCache, "cell 1 0 2 1");

        JLabel lblBuildImageCacheHint = new JLabel(IconManager.HINT);
        lblBuildImageCacheHint.setToolTipText(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
        panelMisc.add(lblBuildImageCacheHint, "cell 1 0 2 1");

        chckbxRuntimeFromMi = new JCheckBox(BUNDLE.getString("Settings.runtimefrommediafile")); //$NON-NLS-1$
        panelMisc.add(chckbxRuntimeFromMi, "cell 1 1 2 1");

        chckbxIncludeExternalAudioStreams = new JCheckBox(BUNDLE.getString("Settings.includeexternalstreamsinnfo")); //$NON-NLS-1$
        panelMisc.add(chckbxIncludeExternalAudioStreams, "cell 1 2 2 1");
      }
      JLabel lblCheckImages = new JLabel(BUNDLE.getString("Settings.checkimages"));
      panelMisc.add(lblCheckImages, "cell 1 3 2 1");

      {
        JPanel panelCheckImages = new JPanel();
        panelCheckImages.setLayout(new MigLayout("hidemode 1, insets 0", "", ""));
        panelMisc.add(panelCheckImages, "cell 2 4");

        chckbxCheckPoster = new JCheckBox(BUNDLE.getString("mediafiletype.poster"));
        panelCheckImages.add(chckbxCheckPoster, "cell 0 0");

        chckbxCheckFanart = new JCheckBox(BUNDLE.getString("mediafiletype.fanart"));
        panelCheckImages.add(chckbxCheckFanart, "cell 1 0");

        chckbxCheckBanner = new JCheckBox(BUNDLE.getString("mediafiletype.banner"));
        panelCheckImages.add(chckbxCheckBanner, "cell 2 0");

        chckbxCheckClearart = new JCheckBox(BUNDLE.getString("mediafiletype.clearart"));
        panelCheckImages.add(chckbxCheckClearart, "cell 3 0");

        chckbxCheckThumb = new JCheckBox(BUNDLE.getString("mediafiletype.thumb"));
        panelCheckImages.add(chckbxCheckThumb, "cell 4 0");

        chckbxCheckLogo = new JCheckBox(BUNDLE.getString("mediafiletype.logo"));
        panelCheckImages.add(chckbxCheckLogo, "cell 5 0");

        chckbxCheckClearlogo = new JCheckBox(BUNDLE.getString("mediafiletype.clearlogo"));
        panelCheckImages.add(chckbxCheckClearlogo, "cell 6 0");

        chckbxCheckDiscart = new JCheckBox(BUNDLE.getString("mediafiletype.disc"));
        panelCheckImages.add(chckbxCheckDiscart, "cell 7 0");
      }
    }
    {
      JPanel panelPresets = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][15lp][][][grow]", "[]"));

      JLabel lblPresets = new TmmLabel(BUNDLE.getString("Settings.preset"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelPresets, lblPresets, true);
      add(collapsiblePanel, "cell 0 6,growx,wmin 0");
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

  protected void initDataBindings() {
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty = BeanProperty.create("storeUiFilters");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty, chckbxPersistUiFilters, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_1 = BeanProperty.create("renameAfterScrape");
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
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_7 = BeanProperty.create("preferPersonalRating");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_7, chckbxPersonalRatingFirst, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MovieSettings, String> movieSettingsBeanProperty_8 = BeanProperty.create("preferredRating");
    BeanProperty<AutocompleteComboBox, Object> autocompleteComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, String, AutocompleteComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_8, cbRating, autocompleteComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_9 = BeanProperty.create("includeExternalAudioStreams");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_9, chckbxIncludeExternalAudioStreams, jCheckBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_10 = BeanProperty.create("showLogosPanel");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_10, chckbxShowLogos, jCheckBoxBeanProperty);
    autoBinding_10.bind();
  }
}
