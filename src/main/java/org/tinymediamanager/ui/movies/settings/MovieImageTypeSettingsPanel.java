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

import java.awt.event.ItemListener;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.filenaming.MovieBannerNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearlogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieDiscartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieFanartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieKeyartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieLogoNaming;
import org.tinymediamanager.core.movie.filenaming.MoviePosterNaming;
import org.tinymediamanager.core.movie.filenaming.MovieThumbNaming;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link MovieImageSettingsPanel} is used to display image file name settings.
 * 
 * @author Manuel Laggner
 */
class MovieImageTypeSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 7312645402037806284L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private MovieSettings               settings         = MovieModuleManager.SETTINGS;
  private JCheckBox                   chckbxMoviePosterFilename2;
  private JCheckBox                   chckbxMoviePosterFilename4;
  private JCheckBox                   chckbxMoviePosterFilename6;
  private JCheckBox                   chckbxMoviePosterFilename7;
  private JCheckBox                   chckbxMovieFanartFilename1;
  private JCheckBox                   chckbxMovieFanartFilename2;
  private JCheckBox                   chckbxMoviePosterFilename8;
  private JCheckBox                   chckbxMovieFanartFilename3;
  private JCheckBox                   chckbxBanner1;
  private JCheckBox                   chckbxBanner2;
  private JCheckBox                   chckbxClearart1;
  private JCheckBox                   chckbxClearart2;
  private JCheckBox                   chckbxThumb1;
  private JCheckBox                   chckbxThumb2;
  private JCheckBox                   chckbxThumb3;
  private JCheckBox                   chckbxThumb4;
  private JCheckBox                   chckbxLogo1;
  private JCheckBox                   chckbxLogo2;
  private JCheckBox                   chckbxClearlogo1;
  private JCheckBox                   chckbxClearlogo2;
  private JCheckBox                   chckbxDiscart1;
  private JCheckBox                   chckbxDiscart2;
  private ItemListener                checkBoxListener;
  private JCheckBox                   chckbxDiscart4;
  private JCheckBox                   chckbxDiscart3;
  private JCheckBox                   chckbxKeyart1;
  private JCheckBox                   chckbxKeyart2;

  /**
   * Instantiates a new movie image settings panel.
   */
  MovieImageTypeSettingsPanel() {
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

  private void buildCheckBoxes() {
    // initialize
    chckbxMovieFanartFilename1.removeItemListener(checkBoxListener);
    chckbxMovieFanartFilename2.removeItemListener(checkBoxListener);
    chckbxMovieFanartFilename3.removeItemListener(checkBoxListener);
    clearSelection(chckbxMovieFanartFilename1, chckbxMovieFanartFilename2, chckbxMovieFanartFilename3);

    chckbxMoviePosterFilename2.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename4.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename7.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename8.removeItemListener(checkBoxListener);
    chckbxMoviePosterFilename6.removeItemListener(checkBoxListener);
    clearSelection(chckbxMoviePosterFilename2, chckbxMoviePosterFilename4, chckbxMoviePosterFilename6, chckbxMoviePosterFilename7,
        chckbxMoviePosterFilename8);

    chckbxBanner1.removeItemListener(checkBoxListener);
    chckbxBanner2.removeItemListener(checkBoxListener);
    clearSelection(chckbxBanner1, chckbxBanner2);

    chckbxClearart1.removeItemListener(checkBoxListener);
    chckbxClearart2.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearart1, chckbxClearart2);

    chckbxClearlogo1.removeItemListener(checkBoxListener);
    chckbxClearlogo2.removeItemListener(checkBoxListener);
    clearSelection(chckbxClearlogo1, chckbxClearlogo2);

    chckbxLogo1.removeItemListener(checkBoxListener);
    chckbxLogo2.removeItemListener(checkBoxListener);
    clearSelection(chckbxLogo1, chckbxLogo2);

    chckbxThumb1.removeItemListener(checkBoxListener);
    chckbxThumb2.removeItemListener(checkBoxListener);
    chckbxThumb3.removeItemListener(checkBoxListener);
    chckbxThumb4.removeItemListener(checkBoxListener);
    clearSelection(chckbxThumb1, chckbxThumb2, chckbxThumb3, chckbxThumb4);

    chckbxDiscart1.removeItemListener(checkBoxListener);
    chckbxDiscart2.removeItemListener(checkBoxListener);
    chckbxDiscart3.removeItemListener(checkBoxListener);
    chckbxDiscart4.removeItemListener(checkBoxListener);
    clearSelection(chckbxDiscart1, chckbxDiscart2, chckbxDiscart3, chckbxDiscart4);

    chckbxKeyart1.removeItemListener(checkBoxListener);
    chckbxKeyart2.removeItemListener(checkBoxListener);
    clearSelection(chckbxKeyart1, chckbxKeyart2);

    // poster filenames
    for (MoviePosterNaming poster : settings.getPosterFilenames()) {
      switch (poster) {
        case FILENAME:
          chckbxMoviePosterFilename7.setSelected(true);
          break;
        case FILENAME_POSTER:
          chckbxMoviePosterFilename8.setSelected(true);
          break;
        case FOLDER:
          chckbxMoviePosterFilename6.setSelected(true);
          break;
        case MOVIE:
          chckbxMoviePosterFilename2.setSelected(true);
          break;
        case POSTER:
          chckbxMoviePosterFilename4.setSelected(true);
          break;
      }
    }

    // fanart filenames
    for (MovieFanartNaming fanart : settings.getFanartFilenames()) {
      switch (fanart) {
        case FANART:
          chckbxMovieFanartFilename2.setSelected(true);
          break;
        case FILENAME_FANART:
          chckbxMovieFanartFilename1.setSelected(true);
          break;
        case FILENAME_FANART2:
          chckbxMovieFanartFilename3.setSelected(true);
          break;
      }
    }

    // banner filenames
    for (MovieBannerNaming banner : settings.getBannerFilenames()) {
      switch (banner) {
        case BANNER:
          chckbxBanner2.setSelected(true);
          break;
        case FILENAME_BANNER:
          chckbxBanner1.setSelected(true);
          break;
      }
    }

    // clearart filenames
    for (MovieClearartNaming clearart : settings.getClearartFilenames()) {
      switch (clearart) {
        case CLEARART:
          chckbxClearart2.setSelected(true);
          break;
        case FILENAME_CLEARART:
          chckbxClearart1.setSelected(true);
          break;
      }
    }

    // thumb filenames
    for (MovieThumbNaming thumb : settings.getThumbFilenames()) {
      switch (thumb) {
        case THUMB:
          chckbxThumb2.setSelected(true);
          break;
        case FILENAME_THUMB:
          chckbxThumb1.setSelected(true);
          break;
        case LANDSCAPE:
          chckbxThumb4.setSelected(true);
          break;
        case FILENAME_LANDSCAPE:
          chckbxThumb3.setSelected(true);
          break;
      }
    }

    // logo filenames
    for (MovieLogoNaming logo : settings.getLogoFilenames()) {
      switch (logo) {
        case LOGO:
          chckbxLogo2.setSelected(true);
          break;
        case FILENAME_LOGO:
          chckbxLogo1.setSelected(true);
          break;
      }
    }

    // clearlogo filenames
    for (MovieClearlogoNaming clearlogo : settings.getClearlogoFilenames()) {
      switch (clearlogo) {
        case CLEARLOGO:
          chckbxClearlogo2.setSelected(true);
          break;
        case FILENAME_CLEARLOGO:
          chckbxClearlogo1.setSelected(true);
          break;
      }
    }

    // discart filenames
    for (MovieDiscartNaming discart : settings.getDiscartFilenames()) {
      switch (discart) {
        case DISC:
          chckbxDiscart2.setSelected(true);
          break;
        case FILENAME_DISC:
          chckbxDiscart1.setSelected(true);
          break;
        case DISCART:
          chckbxDiscart4.setSelected(true);
          break;
        case FILENAME_DISCART:
          chckbxDiscart3.setSelected(true);
          break;
      }
    }

    // keyart filenames
    for (MovieKeyartNaming keyart : settings.getKeyartFilenames()) {
      switch (keyart) {
        case KEYART:
          chckbxKeyart1.setSelected(true);
          break;
        case FILENAME_KEYART:
          chckbxKeyart2.setSelected(true);
          break;
      }
    }

    // listen to changes of the checkboxes
    chckbxMovieFanartFilename2.addItemListener(checkBoxListener);
    chckbxMovieFanartFilename3.addItemListener(checkBoxListener);

    chckbxMovieFanartFilename1.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename2.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename4.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename7.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename8.addItemListener(checkBoxListener);
    chckbxMoviePosterFilename6.addItemListener(checkBoxListener);

    chckbxBanner1.addItemListener(checkBoxListener);
    chckbxBanner2.addItemListener(checkBoxListener);

    chckbxClearart1.addItemListener(checkBoxListener);
    chckbxClearart2.addItemListener(checkBoxListener);

    chckbxClearlogo1.addItemListener(checkBoxListener);
    chckbxClearlogo2.addItemListener(checkBoxListener);

    chckbxLogo1.addItemListener(checkBoxListener);
    chckbxLogo2.addItemListener(checkBoxListener);

    chckbxThumb1.addItemListener(checkBoxListener);
    chckbxThumb2.addItemListener(checkBoxListener);
    chckbxThumb3.addItemListener(checkBoxListener);
    chckbxThumb4.addItemListener(checkBoxListener);

    chckbxDiscart1.addItemListener(checkBoxListener);
    chckbxDiscart2.addItemListener(checkBoxListener);
    chckbxDiscart3.addItemListener(checkBoxListener);
    chckbxDiscart4.addItemListener(checkBoxListener);

    chckbxKeyart1.addItemListener(checkBoxListener);
    chckbxKeyart2.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[]"));
    {
      JPanel panelFileNaming = new JPanel(
          new MigLayout("insets 0", "[20lp!][][][][]", "[][][10lp!][][10lp!][][10lp!][][10lp!][][][10lp!][][10lp!][][10lp!][][][10lp!][][20lp][]"));

      JLabel lblFiletypes = new TmmLabel(BUNDLE.getString("Settings.artwork.naming"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelFileNaming, lblFiletypes, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JLabel lblPosterFilename = new TmmLabel(BUNDLE.getString("mediafiletype.poster"));
        panelFileNaming.add(lblPosterFilename, "cell 1 0");

        chckbxMoviePosterFilename8 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-poster.ext");
        panelFileNaming.add(chckbxMoviePosterFilename8, "cell 3 0");

        chckbxMoviePosterFilename2 = new JCheckBox("movie.ext");
        panelFileNaming.add(chckbxMoviePosterFilename2, "cell 4 0");

        chckbxMoviePosterFilename7 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".ext");
        panelFileNaming.add(chckbxMoviePosterFilename7, "cell 3 1");

        chckbxMoviePosterFilename4 = new JCheckBox("poster.ext");
        panelFileNaming.add(chckbxMoviePosterFilename4, "cell 2 0");

        chckbxMoviePosterFilename6 = new JCheckBox("folder.ext");
        panelFileNaming.add(chckbxMoviePosterFilename6, "cell 2 1");

        JLabel lblFanartFileNaming = new TmmLabel(BUNDLE.getString("mediafiletype.fanart"));
        panelFileNaming.add(lblFanartFileNaming, "cell 1 3");

        chckbxMovieFanartFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-fanart.ext");
        panelFileNaming.add(chckbxMovieFanartFilename1, "cell 3 3");

        chckbxMovieFanartFilename3 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".fanart.ext");
        panelFileNaming.add(chckbxMovieFanartFilename3, "cell 4 3");

        chckbxMovieFanartFilename2 = new JCheckBox("fanart.ext");
        panelFileNaming.add(chckbxMovieFanartFilename2, "cell 2 3");

        JLabel lblBannerNamingT = new TmmLabel(BUNDLE.getString("mediafiletype.banner"));
        panelFileNaming.add(lblBannerNamingT, "cell 1 5");

        chckbxBanner2 = new JCheckBox("banner.ext");
        panelFileNaming.add(chckbxBanner2, "cell 2 5");

        chckbxBanner1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-banner.ext");
        panelFileNaming.add(chckbxBanner1, "cell 3 5");

        JLabel lblClearartNamingT = new TmmLabel(BUNDLE.getString("mediafiletype.clearart"));
        panelFileNaming.add(lblClearartNamingT, "cell 1 7");

        chckbxClearart2 = new JCheckBox("clearart.ext");
        panelFileNaming.add(chckbxClearart2, "cell 2 7");

        chckbxClearart1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-clearart.ext");
        panelFileNaming.add(chckbxClearart1, "cell 3 7");

        JLabel lblThumbNamingT = new TmmLabel(BUNDLE.getString("mediafiletype.thumb"));
        panelFileNaming.add(lblThumbNamingT, "cell 1 9");

        chckbxThumb2 = new JCheckBox("thumb.ext");
        panelFileNaming.add(chckbxThumb2, "cell 2 9");

        chckbxThumb1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-thumb.ext");
        panelFileNaming.add(chckbxThumb1, "cell 3 9");

        chckbxThumb4 = new JCheckBox("landscape.ext");
        panelFileNaming.add(chckbxThumb4, "cell 2 10");

        chckbxThumb3 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-landscape.ext");
        panelFileNaming.add(chckbxThumb3, "cell 3 10");

        JLabel lblLogoNamingT = new TmmLabel(BUNDLE.getString("mediafiletype.logo"));
        panelFileNaming.add(lblLogoNamingT, "cell 1 12");

        chckbxLogo2 = new JCheckBox("logo.ext");
        panelFileNaming.add(chckbxLogo2, "cell 2 12");

        chckbxLogo1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-logo.ext");
        panelFileNaming.add(chckbxLogo1, "cell 3 12");

        JLabel lblClearlogoNamingT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo"));
        panelFileNaming.add(lblClearlogoNamingT, "cell 1 14");

        chckbxClearlogo2 = new JCheckBox("clearlogo.ext");
        panelFileNaming.add(chckbxClearlogo2, "cell 2 14");

        chckbxClearlogo1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-clearlogo.ext");
        panelFileNaming.add(chckbxClearlogo1, "cell 3 14");

        JLabel lblDiscartNamingT = new TmmLabel(BUNDLE.getString("mediafiletype.disc"));
        panelFileNaming.add(lblDiscartNamingT, "cell 1 16");

        chckbxDiscart2 = new JCheckBox("disc.ext");
        panelFileNaming.add(chckbxDiscart2, "cell 2 16");

        chckbxDiscart1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-disc.ext");
        panelFileNaming.add(chckbxDiscart1, "cell 3 16");

        chckbxDiscart4 = new JCheckBox("discart.ext");
        panelFileNaming.add(chckbxDiscart4, "cell 2 17");

        chckbxDiscart3 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-discart.ext");
        panelFileNaming.add(chckbxDiscart3, "cell 3 17");

        JLabel lblKeyartT = new TmmLabel(BUNDLE.getString("mediafiletype.keyart"));
        panelFileNaming.add(lblKeyartT, "cell 1 19");

        chckbxKeyart1 = new JCheckBox("keyart.ext");
        panelFileNaming.add(chckbxKeyart1, "cell 2 19");

        chckbxKeyart2 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + "-keyart.ext");
        panelFileNaming.add(chckbxKeyart2, "cell 3 19");

        JTextArea tpFileNamingHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.naming.info"));
        panelFileNaming.add(tpFileNamingHint, "cell 1 21 4 1,growx");
        TmmFontHelper.changeFont(tpFileNamingHint, 0.833);
      }
    }
  }

  /**
   * Check changes.
   */
  private void checkChanges() {
    // set poster filenames
    settings.clearPosterFilenames();

    if (chckbxMoviePosterFilename2.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.MOVIE);
    }
    if (chckbxMoviePosterFilename4.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.POSTER);
    }
    if (chckbxMoviePosterFilename6.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.FOLDER);
    }
    if (chckbxMoviePosterFilename7.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.FILENAME);
    }
    if (chckbxMoviePosterFilename8.isSelected()) {
      settings.addPosterFilename(MoviePosterNaming.FILENAME_POSTER);
    }

    // set fanart filenames
    settings.clearFanartFilenames();
    if (chckbxMovieFanartFilename1.isSelected()) {
      settings.addFanartFilename(MovieFanartNaming.FILENAME_FANART);
    }
    if (chckbxMovieFanartFilename2.isSelected()) {
      settings.addFanartFilename(MovieFanartNaming.FANART);
    }
    if (chckbxMovieFanartFilename3.isSelected()) {
      settings.addFanartFilename(MovieFanartNaming.FILENAME_FANART2);
    }

    // set banner filenames
    settings.clearBannerFilenames();
    if (chckbxBanner1.isSelected()) {
      settings.addBannerFilename(MovieBannerNaming.FILENAME_BANNER);
    }
    if (chckbxBanner2.isSelected()) {
      settings.addBannerFilename(MovieBannerNaming.BANNER);
    }

    // set clearart filenames
    settings.clearClearartFilenames();
    if (chckbxClearart1.isSelected()) {
      settings.addClearartFilename(MovieClearartNaming.FILENAME_CLEARART);
    }
    if (chckbxClearart2.isSelected()) {
      settings.addClearartFilename(MovieClearartNaming.CLEARART);
    }

    // set thumb filenames
    settings.clearThumbFilenames();
    if (chckbxThumb1.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.FILENAME_THUMB);
    }
    if (chckbxThumb2.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.THUMB);
    }
    if (chckbxThumb3.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.FILENAME_LANDSCAPE);
    }
    if (chckbxThumb4.isSelected()) {
      settings.addThumbFilename(MovieThumbNaming.LANDSCAPE);
    }

    // set logo filenames
    settings.clearLogoFilenames();
    if (chckbxLogo1.isSelected()) {
      settings.addLogoFilename(MovieLogoNaming.FILENAME_LOGO);
    }
    if (chckbxLogo2.isSelected()) {
      settings.addLogoFilename(MovieLogoNaming.LOGO);
    }

    // set clearlogo filenames
    settings.clearClearlogoFilenames();
    if (chckbxClearlogo1.isSelected()) {
      settings.addClearlogoFilename(MovieClearlogoNaming.FILENAME_CLEARLOGO);
    }
    if (chckbxClearlogo2.isSelected()) {
      settings.addClearlogoFilename(MovieClearlogoNaming.CLEARLOGO);
    }

    // set discart filenames
    settings.clearDiscartFilenames();
    if (chckbxDiscart1.isSelected()) {
      settings.addDiscartFilename(MovieDiscartNaming.FILENAME_DISC);
    }
    if (chckbxDiscart2.isSelected()) {
      settings.addDiscartFilename(MovieDiscartNaming.DISC);
    }
    if (chckbxDiscart3.isSelected()) {
      settings.addDiscartFilename(MovieDiscartNaming.FILENAME_DISCART);
    }
    if (chckbxDiscart4.isSelected()) {
      settings.addDiscartFilename(MovieDiscartNaming.DISCART);
    }

    // set keyart filenames
    settings.clearKeyartFilenames();
    if (chckbxKeyart1.isSelected()) {
      settings.addKeyartFilename(MovieKeyartNaming.KEYART);
    }
    if (chckbxKeyart2.isSelected()) {
      settings.addKeyartFilename(MovieKeyartNaming.FILENAME_KEYART);
    }
  }

  protected void initDataBindings() {
  }
}
