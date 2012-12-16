/*
 * Copyright 2012 Manuel Laggner
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork.PosterSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieImageSettingsPanel extends JPanel {

  /** The settings. */
  private Settings  settings = Settings.getInstance();

  /** The cb image tmdb poster size. */
  private JComboBox cbImageTmdbPosterSize;

  /** The cb image tmdb fanart size. */
  private JComboBox cbImageTmdbFanartSize;

  /** The cb image tmdb language. */
  private JComboBox cbImageTmdbLanguage;

  private JCheckBox cbMoviePosterFilename1;

  private JCheckBox cbMoviePosterFilename2;

  private JCheckBox cbMoviePosterFilename3;

  private JCheckBox cbMoviePosterFilename4;

  private JCheckBox cbMoviePosterFilename5;

  private JCheckBox cbMoviePosterFilename6;

  private JCheckBox cbMoviePosterFilename7;

  private JCheckBox cbMovieFanartFilename1;

  private JCheckBox cbMovieFanartFilename2;

  public MovieImageSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JPanel panelMovieImages = new JPanel();
    panelMovieImages.setBorder(new TitledBorder(null, "Poster and Fanart", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelMovieImages, "2, 2, left, top");
    panelMovieImages.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblSource = new JLabel("Source");
    panelMovieImages.add(lblSource, "2, 2");

    JCheckBox chckbxTheMovieDatabase = new JCheckBox("The Movie Database");
    chckbxTheMovieDatabase.setEnabled(false);
    chckbxTheMovieDatabase.setSelected(true);
    panelMovieImages.add(chckbxTheMovieDatabase, "4, 2");

    JPanel panelMovieImagesTmdb = new JPanel();
    panelMovieImagesTmdb.setBorder(new TitledBorder(null, "The Movie Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panelMovieImages.add(panelMovieImagesTmdb, "2, 4, 3, 1, fill, fill");
    panelMovieImagesTmdb.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblImageTmdbLanguage = new JLabel("Language");
    panelMovieImagesTmdb.add(lblImageTmdbLanguage, "2, 2, right, default");

    cbImageTmdbLanguage = new JComboBox(TmdbMetadataProvider.Languages.values());
    panelMovieImagesTmdb.add(cbImageTmdbLanguage, "4, 2, fill, default");

    JLabel lblImageTmdbPosterSize = new JLabel("Poster size");
    panelMovieImagesTmdb.add(lblImageTmdbPosterSize, "2, 4, right, default");

    cbImageTmdbPosterSize = new JComboBox(PosterSizes.values());
    panelMovieImagesTmdb.add(cbImageTmdbPosterSize, "4, 4, fill, default");

    JLabel lblImageTmdbFanartSize = new JLabel("Fanart size");
    panelMovieImagesTmdb.add(lblImageTmdbFanartSize, "2, 6, right, default");

    cbImageTmdbFanartSize = new JComboBox(FanartSizes.values());
    panelMovieImagesTmdb.add(cbImageTmdbFanartSize, "4, 6, fill, default");

    JLabel lblPosterFilename = new JLabel("Poster file naming");
    panelMovieImages.add(lblPosterFilename, "2, 6");

    cbMoviePosterFilename1 = new JCheckBox("<movie filename>.tbn");
    panelMovieImages.add(cbMoviePosterFilename1, "4, 6");

    cbMoviePosterFilename4 = new JCheckBox("poster.jpg");
    panelMovieImages.add(cbMoviePosterFilename4, "6, 6");

    cbMoviePosterFilename7 = new JCheckBox("<movie filename>.jpg");
    panelMovieImages.add(cbMoviePosterFilename7, "4, 7");

    cbMoviePosterFilename5 = new JCheckBox("poster.tbn");
    panelMovieImages.add(cbMoviePosterFilename5, "6, 7");

    cbMoviePosterFilename2 = new JCheckBox("movie.jpg");
    panelMovieImages.add(cbMoviePosterFilename2, "4, 8");

    cbMoviePosterFilename6 = new JCheckBox("folder.jpg");
    panelMovieImages.add(cbMoviePosterFilename6, "6, 8");

    cbMoviePosterFilename3 = new JCheckBox("movie.tbn");
    panelMovieImages.add(cbMoviePosterFilename3, "4, 9");

    JLabel lblFanartFileNaming = new JLabel("Fanart file naming");
    panelMovieImages.add(lblFanartFileNaming, "2, 11");

    cbMovieFanartFilename1 = new JCheckBox("<movie filename>-fanart.jpg");
    panelMovieImages.add(cbMovieFanartFilename1, "4, 11");

    cbMovieFanartFilename2 = new JCheckBox("fanart.jpg");
    panelMovieImages.add(cbMovieFanartFilename2, "4, 12");

    initDataBindings();

    // poster filenames
    List<MoviePosterNaming> moviePosterFilenames = settings.getMoviePosterFilenames();
    if (moviePosterFilenames.contains(MoviePosterNaming.FILENAME_TBN)) {
      cbMoviePosterFilename1.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.MOVIE_JPG)) {
      cbMoviePosterFilename2.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.MOVIE_TBN)) {
      cbMoviePosterFilename3.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.POSTER_JPG)) {
      cbMoviePosterFilename4.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.POSTER_TBN)) {
      cbMoviePosterFilename5.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.FOLDER_JPG)) {
      cbMoviePosterFilename6.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.FILENAME_JPG)) {
      cbMoviePosterFilename7.setSelected(true);
    }

    // fanart filenames
    List<MovieFanartNaming> movieFanartFilenames = settings.getMovieFanartFilenames();
    if (movieFanartFilenames.contains(MovieFanartNaming.FILENAME_JPG)) {
      cbMovieFanartFilename1.setSelected(true);
    }
    if (movieFanartFilenames.contains(MovieFanartNaming.FANART_JPG)) {
      cbMovieFanartFilename2.setSelected(true);
    }

    // listen to changes of the checkboxes
    ItemListener listener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    };

    cbMoviePosterFilename1.addItemListener(listener);
    cbMoviePosterFilename2.addItemListener(listener);
    cbMoviePosterFilename3.addItemListener(listener);
    cbMoviePosterFilename4.addItemListener(listener);
    cbMoviePosterFilename5.addItemListener(listener);
    cbMoviePosterFilename6.addItemListener(listener);
    cbMoviePosterFilename7.addItemListener(listener);

    cbMovieFanartFilename1.addItemListener(listener);
    cbMovieFanartFilename2.addItemListener(listener);
  }

  private void initDataBindings() {
    BeanProperty<Settings, PosterSizes> settingsBeanProperty_5 = BeanProperty.create("imageTmdbPosterSize");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, PosterSizes, JComboBox, Object> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, cbImageTmdbPosterSize, jComboBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, FanartSizes> settingsBeanProperty_6 = BeanProperty.create("imageTmdbFanartSize");
    AutoBinding<Settings, FanartSizes, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, cbImageTmdbFanartSize, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Languages> settingsBeanProperty_7 = BeanProperty.create("imageTmdbLangugage");
    AutoBinding<Settings, Languages, JComboBox, Object> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, cbImageTmdbLanguage, jComboBoxBeanProperty);
    autoBinding_6.bind();
    //
  }

  public void checkChanges() {
    // set poster filenames
    settings.clearMoviePosterFilenames();
    if (cbMoviePosterFilename1.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_TBN);
    }
    if (cbMoviePosterFilename2.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_JPG);
    }
    if (cbMoviePosterFilename3.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_TBN);
    }
    if (cbMoviePosterFilename4.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
    }
    if (cbMoviePosterFilename5.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.POSTER_TBN);
    }
    if (cbMoviePosterFilename6.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FOLDER_JPG);
    }
    if (cbMoviePosterFilename7.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_JPG);
    }

    // set fanart filenames
    settings.clearMovieFanartFilenames();
    if (cbMovieFanartFilename1.isSelected()) {
      settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_JPG);
    }
    if (cbMovieFanartFilename2.isSelected()) {
      settings.addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
    }
  }
}
