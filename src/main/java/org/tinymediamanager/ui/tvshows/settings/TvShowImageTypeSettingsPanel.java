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

import java.awt.event.ItemListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.filenaming.TvShowBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowClearlogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowFanartNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowLogoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowThumbNaming;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link TvShowImageSettingsPanel} is used to display image file name settings.
 * 
 * @author Manuel Laggner
 */
class TvShowImageTypeSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 4999827736720726395L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private JCheckBox                   chckbxEpisodeThumb1;
  private JCheckBox                   chckbxEpisodeThumb2;
  private JCheckBox                   chckbxEpisodeThumb3;
  private JCheckBox                   chckbxEpisodeThumb4;

  private ItemListener                checkBoxListener;
  private JCheckBox                   chckbxPoster1;
  private JCheckBox                   chckbxPoster2;
  private JCheckBox                   chckbxFanart1;
  private JCheckBox                   chckbxBanner1;
  private JCheckBox                   chckbxClearart1;
  private JCheckBox                   chckbxThumb1;
  private JCheckBox                   chckbxThumb2;
  private JCheckBox                   chckbxLogo1;
  private JCheckBox                   chckbxClearlogo1;
  private JCheckBox                   chckbxSeasonPoster1;
  private JCheckBox                   chckbxSeasonPoster2;
  private JCheckBox                   chckbxSeasonBanner1;
  private JCheckBox                   chckbxSeasonBanner2;
  private JCheckBox                   chckbxSeasonThumb1;
  private JCheckBox                   chckbxSeasonThumb2;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  TvShowImageTypeSettingsPanel() {
    checkBoxListener = e -> checkChanges();

    // UI init
    initComponents();
    initDataBindings();

    // implement checkBoxListener for preset events
    settings.addPropertyChangeListener(evt -> {
      if ("preset".equals(evt.getPropertyName())) {
        buildCheckBoxes();
      }
    });

    buildCheckBoxes();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[]"));
    {
      JPanel panelFileNaming = new JPanel(new MigLayout("", "[25lp,shrink 0][][][500lp,grow]",
          "[][][10lp][][10lp][][10lp][][10lp][][10lp][][10lp][][10lp][][10lp][][10lp][][10lp][][][20lp][]"));

      JLabel lblFiletypes = new TmmLabel(BUNDLE.getString("Settings.artwork.naming"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelFileNaming, lblFiletypes, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JLabel lblPosterT = new JLabel(BUNDLE.getString("mediafiletype.poster"));
        panelFileNaming.add(lblPosterT, "cell 1 0");

        chckbxPoster1 = new JCheckBox("poster.ext");
        panelFileNaming.add(chckbxPoster1, "cell 2 0");

        chckbxPoster2 = new JCheckBox("folder.ext");
        panelFileNaming.add(chckbxPoster2, "cell 3 0");

        JLabel lblFanartT = new JLabel(BUNDLE.getString("mediafiletype.fanart"));
        panelFileNaming.add(lblFanartT, "cell 1 2");

        chckbxFanart1 = new JCheckBox("fanart.ext");
        panelFileNaming.add(chckbxFanart1, "cell 2 2");

        JLabel lblBannerT = new JLabel(BUNDLE.getString("mediafiletype.banner"));
        panelFileNaming.add(lblBannerT, "cell 1 4");

        chckbxBanner1 = new JCheckBox("banner.ext");
        panelFileNaming.add(chckbxBanner1, "cell 2 4");

        JLabel lblClearartT = new JLabel(BUNDLE.getString("mediafiletype.clearart"));
        panelFileNaming.add(lblClearartT, "cell 1 6");

        chckbxClearart1 = new JCheckBox("clearart.ext");
        panelFileNaming.add(chckbxClearart1, "cell 2 6");

        JLabel lblThumbT = new JLabel(BUNDLE.getString("mediafiletype.thumb"));
        panelFileNaming.add(lblThumbT, "cell 1 8");

        chckbxThumb1 = new JCheckBox("thumb.ext");
        panelFileNaming.add(chckbxThumb1, "cell 2 8");

        chckbxThumb2 = new JCheckBox("landscape.ext");
        panelFileNaming.add(chckbxThumb2, "cell 3 8");

        JLabel lblLogoT = new JLabel(BUNDLE.getString("mediafiletype.logo"));
        panelFileNaming.add(lblLogoT, "cell 1 10");

        chckbxLogo1 = new JCheckBox("logo.ext");
        panelFileNaming.add(chckbxLogo1, "cell 2 10");

        JLabel lblClearlogoT = new JLabel(BUNDLE.getString("mediafiletype.clearlogo"));
        panelFileNaming.add(lblClearlogoT, "cell 1 12");

        chckbxClearlogo1 = new JCheckBox("clearlogo.ext");
        panelFileNaming.add(chckbxClearlogo1, "cell 2 12");

        JLabel lblSeasonPosterT = new JLabel(BUNDLE.getString("mediafiletype.season_poster"));
        panelFileNaming.add(lblSeasonPosterT, "cell 1 14");

        chckbxSeasonPoster1 = new JCheckBox("seasonXX-poster.ext");
        panelFileNaming.add(chckbxSeasonPoster1, "cell 2 14");

        chckbxSeasonPoster2 = new JCheckBox("<season_folder>" + File.separator + "seasonXX.ext");
        panelFileNaming.add(chckbxSeasonPoster2, "cell 3 14");

        JLabel lblSeasonBannerT = new JLabel(BUNDLE.getString("mediafiletype.season_banner"));
        panelFileNaming.add(lblSeasonBannerT, "cell 1 16");

        chckbxSeasonBanner1 = new JCheckBox("seasonXX-banner.ext");
        panelFileNaming.add(chckbxSeasonBanner1, "cell 2 16");

        chckbxSeasonBanner2 = new JCheckBox("<season_folder>/seasonXX-banner.ext");
        panelFileNaming.add(chckbxSeasonBanner2, "cell 3 16");

        JLabel lblSeasonThumbT = new JLabel(BUNDLE.getString("mediafiletype.season_thumb"));
        panelFileNaming.add(lblSeasonThumbT, "cell 1 18");

        chckbxSeasonThumb1 = new JCheckBox("seasonXX-thumb.ext");
        panelFileNaming.add(chckbxSeasonThumb1, "cell 2 18");

        chckbxSeasonThumb2 = new JCheckBox("<season_folder>/seasonXX-thumb.ext");
        panelFileNaming.add(chckbxSeasonThumb2, "cell 3 18");

        JLabel lblThumbNaming = new JLabel(BUNDLE.getString("mediafiletype.episode_thumb"));
        panelFileNaming.add(lblThumbNaming, "cell 1 20");

        chckbxEpisodeThumb1 = new JCheckBox("<dynamic>-thumb.ext");
        panelFileNaming.add(chckbxEpisodeThumb1, "cell 2 20");

        chckbxEpisodeThumb2 = new JCheckBox("<dynamic>-landscape.ext");
        panelFileNaming.add(chckbxEpisodeThumb2, "cell 3 20");

        chckbxEpisodeThumb3 = new JCheckBox("<dynamic>.ext");
        panelFileNaming.add(chckbxEpisodeThumb3, "cell 2 21");

        chckbxEpisodeThumb4 = new JCheckBox("<dynamic>.tbn");
        panelFileNaming.add(chckbxEpisodeThumb4, "cell 3 21");

        JTextArea tpFileNamingHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.naming.info")); //$NON-NLS-1$
        panelFileNaming.add(tpFileNamingHint, "cell 1 23 3 1,growx");
        TmmFontHelper.changeFont(tpFileNamingHint, 0.833);
      }
    }
  }

  private void buildCheckBoxes() {
    chckbxPoster1.removeItemListener(checkBoxListener);
    chckbxPoster2.removeItemListener(checkBoxListener);
    clearSelection(chckbxPoster1, chckbxPoster2);

    chckbxFanart1.removeItemListener(checkBoxListener);
    clearSelection(chckbxFanart1);

    chckbxBanner1.removeItemListener(checkBoxListener);
    clearSelection(chckbxBanner1);

    chckbxClearart1.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearart1);

    chckbxThumb1.removeItemListener(checkBoxListener);
    chckbxThumb2.removeItemListener(checkBoxListener);
    clearSelection(chckbxThumb1, chckbxThumb2);

    chckbxLogo1.removeItemListener(checkBoxListener);
    clearSelection(chckbxLogo1);

    chckbxClearlogo1.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearlogo1);

    chckbxSeasonPoster1.removeItemListener(checkBoxListener);
    chckbxSeasonPoster2.removeItemListener(checkBoxListener);
    clearSelection(chckbxSeasonPoster1, chckbxSeasonPoster2);

    chckbxSeasonBanner1.removeItemListener(checkBoxListener);
    chckbxSeasonBanner2.removeItemListener(checkBoxListener);
    clearSelection(chckbxSeasonBanner1, chckbxSeasonBanner2);

    chckbxSeasonThumb1.removeItemListener(checkBoxListener);
    chckbxSeasonThumb2.removeItemListener(checkBoxListener);
    clearSelection(chckbxSeasonThumb1, chckbxSeasonThumb2);

    chckbxEpisodeThumb1.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb2.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb3.removeItemListener(checkBoxListener);
    chckbxEpisodeThumb4.removeItemListener(checkBoxListener);
    clearSelection(chckbxEpisodeThumb1, chckbxEpisodeThumb2, chckbxEpisodeThumb3, chckbxEpisodeThumb4);

    for (TvShowPosterNaming posterNaming : settings.getPosterFilenames()) {
      switch (posterNaming) {
        case POSTER:
          chckbxPoster1.setSelected(true);
          break;

        case FOLDER:
          chckbxPoster2.setSelected(true);
          break;
      }
    }

    for (TvShowFanartNaming fanartNaming : settings.getFanartFilenames()) {
      switch (fanartNaming) {
        case FANART:
          chckbxFanart1.setSelected(true);
          break;
      }
    }

    for (TvShowBannerNaming bannerNaming : settings.getBannerFilenames()) {
      switch (bannerNaming) {
        case BANNER:
          chckbxBanner1.setSelected(true);
          break;
      }
    }

    for (TvShowClearartNaming clearartNaming : settings.getClearartFilenames()) {
      switch (clearartNaming) {
        case CLEARART:
          chckbxClearart1.setSelected(true);
          break;
      }
    }

    for (TvShowThumbNaming thumbNaming : settings.getThumbFilenames()) {
      switch (thumbNaming) {
        case THUMB:
          chckbxThumb1.setSelected(true);
          break;

        case LANDSCAPE:
          chckbxThumb2.setSelected(true);
          break;
      }
    }

    for (TvShowLogoNaming logoNaming : settings.getLogoFilenames()) {
      switch (logoNaming) {
        case LOGO:
          chckbxLogo1.setSelected(true);
          break;
      }
    }

    for (TvShowClearlogoNaming clearlogoNaming : settings.getClearlogoFilenames()) {
      switch (clearlogoNaming) {
        case CLEARLOGO:
          chckbxClearlogo1.setSelected(true);
          break;
      }
    }

    for (TvShowSeasonPosterNaming seasonPosterNaming : settings.getSeasonPosterFilenames()) {
      switch (seasonPosterNaming) {
        case SEASON_POSTER:
          chckbxSeasonPoster1.setSelected(true);
          break;

        case SEASON_FOLDER:
          chckbxSeasonPoster2.setSelected(true);
          break;
      }
    }

    for (TvShowSeasonBannerNaming seasonBannerNaming : settings.getSeasonBannerFilenames()) {
      switch (seasonBannerNaming) {
        case SEASON_BANNER:
          chckbxSeasonBanner1.setSelected(true);
          break;

        case SEASON_FOLDER:
          chckbxSeasonBanner2.setSelected(true);
          break;
      }
    }

    for (TvShowSeasonThumbNaming seasonThumbNaming : settings.getSeasonThumbFilenames()) {
      switch (seasonThumbNaming) {
        case SEASON_THUMB:
          chckbxSeasonThumb1.setSelected(true);
          break;

        case SEASON_FOLDER:
          chckbxSeasonThumb2.setSelected(true);
          break;
      }
    }

    for (TvShowEpisodeThumbNaming thumbNaming : settings.getEpisodeThumbFilenames()) {
      switch (thumbNaming) {
        case FILENAME_THUMB:
          chckbxEpisodeThumb1.setSelected(true);
          break;

        case FILENAME_LANDSCAPE:
          chckbxEpisodeThumb2.setSelected(true);
          break;

        case FILENAME:
          chckbxEpisodeThumb3.setSelected(true);
          break;

        case FILENAME_TBN:
          chckbxEpisodeThumb4.setSelected(true);
          break;
      }
    }

    chckbxPoster1.addItemListener(checkBoxListener);
    chckbxPoster2.addItemListener(checkBoxListener);

    chckbxFanart1.addItemListener(checkBoxListener);

    chckbxBanner1.addItemListener(checkBoxListener);

    chckbxClearart1.addItemListener(checkBoxListener);

    chckbxThumb1.addItemListener(checkBoxListener);
    chckbxThumb2.addItemListener(checkBoxListener);

    chckbxLogo1.addItemListener(checkBoxListener);

    chckbxClearlogo1.addItemListener(checkBoxListener);

    chckbxSeasonPoster1.addItemListener(checkBoxListener);
    chckbxSeasonPoster2.addItemListener(checkBoxListener);

    chckbxSeasonBanner1.addItemListener(checkBoxListener);
    chckbxSeasonBanner2.addItemListener(checkBoxListener);

    chckbxSeasonThumb1.addItemListener(checkBoxListener);
    chckbxSeasonThumb2.addItemListener(checkBoxListener);

    chckbxEpisodeThumb1.addItemListener(checkBoxListener);
    chckbxEpisodeThumb2.addItemListener(checkBoxListener);
    chckbxEpisodeThumb3.addItemListener(checkBoxListener);
    chckbxEpisodeThumb4.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
    }
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    settings.clearPosterFilenames();
    if (chckbxPoster1.isSelected()) {
      settings.addPosterFilename(TvShowPosterNaming.POSTER);
    }
    if (chckbxPoster2.isSelected()) {
      settings.addPosterFilename(TvShowPosterNaming.FOLDER);
    }

    settings.clearFanartFilenames();
    if (chckbxFanart1.isSelected()) {
      settings.addFanartFilename(TvShowFanartNaming.FANART);
    }

    settings.clearBannerFilenames();
    if (chckbxBanner1.isSelected()) {
      settings.addBannerFilename(TvShowBannerNaming.BANNER);
    }

    settings.clearClearartFilenames();
    if (chckbxClearart1.isSelected()) {
      settings.addClearartFilename(TvShowClearartNaming.CLEARART);
    }

    settings.clearThumbFilenames();
    if (chckbxThumb1.isSelected()) {
      settings.addThumbFilename(TvShowThumbNaming.THUMB);
    }
    if (chckbxThumb2.isSelected()) {
      settings.addThumbFilename(TvShowThumbNaming.LANDSCAPE);
    }

    settings.clearLogoFilenames();
    if (chckbxLogo1.isSelected()) {
      settings.addLogoFilename(TvShowLogoNaming.LOGO);
    }

    settings.clearClearlogoFilenames();
    if (chckbxClearlogo1.isSelected()) {
      settings.addClearlogoFilename(TvShowClearlogoNaming.CLEARLOGO);
    }

    settings.clearSeasonPosterFilenames();
    if (chckbxSeasonPoster1.isSelected()) {
      settings.addSeasonPosterFilename(TvShowSeasonPosterNaming.SEASON_POSTER);
    }
    if (chckbxSeasonPoster2.isSelected()) {
      settings.addSeasonPosterFilename(TvShowSeasonPosterNaming.SEASON_FOLDER);
    }

    settings.clearSeasonBannerFilenames();
    if (chckbxSeasonBanner1.isSelected()) {
      settings.addSeasonBannerFilename(TvShowSeasonBannerNaming.SEASON_BANNER);
    }
    if (chckbxSeasonBanner2.isSelected()) {
      settings.addSeasonBannerFilename(TvShowSeasonBannerNaming.SEASON_FOLDER);
    }

    settings.clearSeasonThumbFilenames();
    if (chckbxSeasonThumb1.isSelected()) {
      settings.addSeasonThumbFilename(TvShowSeasonThumbNaming.SEASON_THUMB);
    }
    if (chckbxSeasonThumb2.isSelected()) {
      settings.addSeasonThumbFilename(TvShowSeasonThumbNaming.SEASON_FOLDER);
    }

    settings.clearEpisodeThumbFilenames();
    if (chckbxEpisodeThumb1.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_THUMB);
    }
    if (chckbxEpisodeThumb2.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_LANDSCAPE);
    }
    if (chckbxEpisodeThumb3.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME);
    }
    if (chckbxEpisodeThumb4.isSelected()) {
      settings.addEpisodeThumbFilename(TvShowEpisodeThumbNaming.FILENAME_TBN);
    }
  }

  protected void initDataBindings() {
  }
}
