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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.PosterSizes;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieImageSettingsPanel.
 */
public class MovieImageSettingsPanel extends JPanel {

  /** The settings. */
  private Settings  settings = Settings.getInstance();

  /** The cb image tmdb poster size. */
  private JComboBox cbImageTmdbPosterSize;

  /** The cb image tmdb fanart size. */
  private JComboBox cbImageTmdbFanartSize;

  /** The cb image tmdb language. */
  private JComboBox cbImageTmdbLanguage;

  /** The cb movie poster filename1. */
  private JCheckBox cbMoviePosterFilename1;

  /** The cb movie poster filename2. */
  private JCheckBox cbMoviePosterFilename2;

  /** The cb movie poster filename3. */
  private JCheckBox cbMoviePosterFilename3;

  /** The cb movie poster filename4. */
  private JCheckBox cbMoviePosterFilename4;

  /** The cb movie poster filename5. */
  private JCheckBox cbMoviePosterFilename5;

  /** The cb movie poster filename6. */
  private JCheckBox cbMoviePosterFilename6;

  /** The cb movie poster filename7. */
  private JCheckBox cbMoviePosterFilename7;

  /** The cb movie fanart filename1. */
  private JCheckBox cbMovieFanartFilename1;

  /** The cb movie fanart filename2. */
  private JCheckBox cbMovieFanartFilename2;

  /** The cb movie poster filename8. */
  private JCheckBox cbMoviePosterFilename8;

  /** The chckbx fanarttv. */
  private JCheckBox chckbxFanarttv;

  /** The chckbx the movie database. */
  private JCheckBox chckbxTheMovieDatabase;

  /** The lbl attention fanart tv. */
  private JLabel    lblAttentionFanartTv;

  /** The lbl info1. */
  private JLabel    lblInfo1;

  /** The lbl info2. */
  private JLabel    lblInfo2;

  /** The lbl info3. */
  private JLabel    lblInfo3;

  /** The panel actor thumbs. */
  private JPanel    panelActorThumbs;

  /** The cb actor images. */
  private JCheckBox cbActorImages;
  private JTextPane tpFileNamingHint;

  /**
   * Instantiates a new movie image settings panel.
   */
  public MovieImageSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelMovieImages = new JPanel();
    panelMovieImages.setBorder(new TitledBorder(null, "Poster and Fanart", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelMovieImages, "2, 2, left, top");
    panelMovieImages.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblSource = new JLabel("Source");
    panelMovieImages.add(lblSource, "2, 2");

    chckbxTheMovieDatabase = new JCheckBox("The Movie Database");
    chckbxTheMovieDatabase.setSelected(true);
    panelMovieImages.add(chckbxTheMovieDatabase, "6, 2");

    chckbxFanarttv = new JCheckBox("Fanart.tv");
    chckbxFanarttv.setSelected(true);
    panelMovieImages.add(chckbxFanarttv, "6, 4");

    lblAttentionFanartTv = new JLabel("Attention: Fanart.tv does not provide poster");
    lblAttentionFanartTv.setFont(new Font("Dialog", Font.PLAIN, 10));
    panelMovieImages.add(lblAttentionFanartTv, "6, 5, 5, 1");

    JPanel panelMovieImagesTmdb = new JPanel();
    panelMovieImagesTmdb.setBorder(new TitledBorder(null, "The Movie Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panelMovieImages.add(panelMovieImagesTmdb, "2, 7, 5, 1, fill, fill");
    panelMovieImagesTmdb.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
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
    panelMovieImages.add(lblPosterFilename, "2, 9");

    cbMoviePosterFilename1 = new JCheckBox("<movie filename>.tbn");
    panelMovieImages.add(cbMoviePosterFilename1, "6, 9");

    lblInfo1 = new JLabel("");
    lblInfo1.setToolTipText(".tbn is deprecated and will be removed soon");
    lblInfo1.setIcon(new ImageIcon(MovieImageSettingsPanel.class.getResource("/org/tinymediamanager/ui/images/Info.png")));
    panelMovieImages.add(lblInfo1, "4, 9");

    cbMoviePosterFilename4 = new JCheckBox("poster.jpg");
    panelMovieImages.add(cbMoviePosterFilename4, "10, 9");

    cbMoviePosterFilename7 = new JCheckBox("<movie filename>.jpg");
    panelMovieImages.add(cbMoviePosterFilename7, "6, 10");

    lblInfo2 = new JLabel("");
    lblInfo2.setToolTipText(".tbn is deprecated and will be removed soon");
    lblInfo2.setIcon(new ImageIcon(MovieImageSettingsPanel.class.getResource("/org/tinymediamanager/ui/images/Info.png")));
    panelMovieImages.add(lblInfo2, "8, 10");

    cbMoviePosterFilename5 = new JCheckBox("poster.tbn");
    panelMovieImages.add(cbMoviePosterFilename5, "10, 10");

    cbMoviePosterFilename8 = new JCheckBox("<movie filename>-poster.jpg");
    panelMovieImages.add(cbMoviePosterFilename8, "6, 11");

    cbMoviePosterFilename6 = new JCheckBox("folder.jpg");
    panelMovieImages.add(cbMoviePosterFilename6, "10, 11");

    cbMoviePosterFilename2 = new JCheckBox("movie.jpg");
    panelMovieImages.add(cbMoviePosterFilename2, "6, 12");

    lblInfo3 = new JLabel("");
    lblInfo3.setToolTipText(".tbn is deprecated and will be removed soon");
    lblInfo3.setIcon(new ImageIcon(MovieImageSettingsPanel.class.getResource("/org/tinymediamanager/ui/images/Info.png")));
    panelMovieImages.add(lblInfo3, "8, 12");

    cbMoviePosterFilename3 = new JCheckBox("movie.tbn");
    panelMovieImages.add(cbMoviePosterFilename3, "10, 12");

    JLabel lblFanartFileNaming = new JLabel("Fanart file naming");
    panelMovieImages.add(lblFanartFileNaming, "2, 14");

    cbMovieFanartFilename1 = new JCheckBox("<movie filename>-fanart.jpg");
    panelMovieImages.add(cbMovieFanartFilename1, "6, 14");

    cbMovieFanartFilename2 = new JCheckBox("fanart.jpg");
    panelMovieImages.add(cbMovieFanartFilename2, "6, 15");

    tpFileNamingHint = new JTextPane();
    tpFileNamingHint
        .setText("Note: every file naming option ending with .jpg will alternatively store a png. It depends on the source of the image file");
    tpFileNamingHint.setBackground(UIManager.getColor("Panel.background"));
    tpFileNamingHint.setFont(new Font("Dialog", Font.PLAIN, 10));
    panelMovieImages.add(tpFileNamingHint, "2, 17, 9, 1, fill, fill");

    panelActorThumbs = new JPanel();
    panelActorThumbs.setBorder(new TitledBorder(new LineBorder(new Color(99, 130, 191)), "Actor Images", TitledBorder.LEADING, TitledBorder.TOP,
        null, null));
    add(panelActorThumbs, "2, 4");
    panelActorThumbs.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    cbActorImages = new JCheckBox("download actor images to .actors");
    panelActorThumbs.add(cbActorImages, "2, 2");

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
    if (moviePosterFilenames.contains(MoviePosterNaming.FILENAME_POSTER_JPG)) {
      cbMoviePosterFilename8.setSelected(true);
    }

    // fanart filenames
    List<MovieFanartNaming> movieFanartFilenames = settings.getMovieFanartFilenames();
    if (movieFanartFilenames.contains(MovieFanartNaming.FILENAME_FANART_JPG)) {
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
    cbMoviePosterFilename8.addItemListener(listener);

    cbMovieFanartFilename1.addItemListener(listener);
    cbMovieFanartFilename2.addItemListener(listener);
  }

  /**
   * Check changes.
   */
  public void checkChanges() {
    // set poster filenames
    settings.clearMoviePosterFilenames();
    if (cbMoviePosterFilename1.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_TBN);
    }
    if (cbMoviePosterFilename2.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_PNG);
    }
    if (cbMoviePosterFilename3.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.MOVIE_TBN);
    }
    if (cbMoviePosterFilename4.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.POSTER_PNG);
    }
    if (cbMoviePosterFilename5.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.POSTER_TBN);
    }
    if (cbMoviePosterFilename6.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FOLDER_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.FOLDER_PNG);
    }
    if (cbMoviePosterFilename7.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_PNG);
    }
    if (cbMoviePosterFilename8.isSelected()) {
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_POSTER_JPG);
      settings.addMoviePosterFilename(MoviePosterNaming.FILENAME_POSTER_PNG);
    }

    // set fanart filenames
    settings.clearMovieFanartFilenames();
    if (cbMovieFanartFilename1.isSelected()) {
      settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART_JPG);
      settings.addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART_PNG);
    }
    if (cbMovieFanartFilename2.isSelected()) {
      settings.addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
      settings.addMovieFanartFilename(MovieFanartNaming.FANART_PNG);
    }
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
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
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("imageScraperTmdb");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxTheMovieDatabase, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("imageScraperFanartTv");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxFanarttv, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_2 = BeanProperty.create("writeActorImages");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, cbActorImages, jCheckBoxBeanProperty);
    autoBinding_2.bind();
  }
}
