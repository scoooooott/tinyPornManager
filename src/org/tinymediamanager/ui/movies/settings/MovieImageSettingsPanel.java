/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.scraper.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.MediaArtwork.PosterSizes;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieImageSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieImageSettingsPanel extends ScrollablePanel {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 7312645402037806284L;

  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$ @wbp.nls.resourceBundle

  /** The settings. */
  private Settings                    settings         = Settings.getInstance();

  /** The cb image tmdb poster size. */
  private JComboBox                   cbImagePosterSize;

  /** The cb image tmdb fanart size. */
  private JComboBox                   cbImageFanartSize;

  /** The cb movie poster filename2. */
  private JCheckBox                   cbMoviePosterFilename2;

  /** The cb movie poster filename4. */
  private JCheckBox                   cbMoviePosterFilename4;

  /** The cb movie poster filename6. */
  private JCheckBox                   cbMoviePosterFilename6;

  /** The cb movie poster filename7. */
  private JCheckBox                   cbMoviePosterFilename7;

  /** The cb movie fanart filename1. */
  private JCheckBox                   cbMovieFanartFilename1;

  /** The cb movie fanart filename2. */
  private JCheckBox                   cbMovieFanartFilename2;

  /** The cb movie poster filename8. */
  private JCheckBox                   cbMoviePosterFilename8;

  /** The chckbx fanarttv. */
  private JCheckBox                   chckbxFanarttv;

  /** The chckbx the movie database. */
  private JCheckBox                   chckbxTheMovieDatabase;

  /** The lbl attention fanart tv. */
  private JLabel                      lblAttentionFanartTv;

  /** The cb actor images. */
  private JCheckBox                   cbActorImages;

  /** The tp file naming hint. */
  private JTextPane                   tpFileNamingHint;

  /** The chckbx enable extrathumbs. */
  private JCheckBox                   chckbxEnableExtrathumbs;

  /** The chckbx enable extrafanart. */
  private JCheckBox                   chckbxEnableExtrafanart;

  /** The separator. */
  private JSeparator                  separator;

  /** The chckbx resize extrathumbs to. */
  private JCheckBox                   chckbxResizeExtrathumbsTo;

  /** The sp extrathumb width. */
  private JSpinner                    spExtrathumbWidth;

  /** The lbl download. */
  private JLabel                      lblDownload;

  /** The sp download count extrathumbs. */
  private JSpinner                    spDownloadCountExtrathumbs;

  /** The lbl download count. */
  private JLabel                      lblDownloadCount;

  /** The sp download count extrafanart. */
  private JSpinner                    spDownloadCountExtrafanart;

  /** The chckbx store movieset artwork. */
  private JCheckBox                   chckbxStoreMoviesetArtwork;

  /** The tf movie set artwork folder. */
  private JTextField                  tfMovieSetArtworkFolder;

  /** The lbl foldername. */
  private JLabel                      lblFoldername;

  /** The btn select folder. */
  private JButton                     btnSelectFolder;

  /** The separator_2. */
  private JSeparator                  separator_2;
  private JCheckBox                   cbMovieFanartFilename3;
  private JSeparator                  separator_3;
  private JCheckBox                   chckbxBanner;
  private JCheckBox                   chckbxLogo;
  private JCheckBox                   chckbxThumb;
  private JCheckBox                   chckbxDiscArt;
  private JCheckBox                   chckbxClearArt;
  private JPanel                      panelExtraArtwork;
  private JSeparator                  separator_1;
  private JSeparator                  separator_4;

  /**
   * Instantiates a new movie image settings panel.
   */
  public MovieImageSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel panelMovieImages = new JPanel();
    panelMovieImages.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.poster"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMovieImages, "2, 2, default, fill");
    panelMovieImages.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblSource = new JLabel(BUNDLE.getString("Settings.source")); //$NON-NLS-1$
    panelMovieImages.add(lblSource, "2, 2");

    chckbxTheMovieDatabase = new JCheckBox("The Movie Database");
    chckbxTheMovieDatabase.setSelected(true);
    panelMovieImages.add(chckbxTheMovieDatabase, "4, 2");

    chckbxFanarttv = new JCheckBox("Fanart.tv");
    chckbxFanarttv.setSelected(true);
    panelMovieImages.add(chckbxFanarttv, "4, 4");

    lblAttentionFanartTv = new JLabel(BUNDLE.getString("Settings.fanarttv.alert")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblAttentionFanartTv, 0.833);
    panelMovieImages.add(lblAttentionFanartTv, "4, 5, 3, 1");

    separator = new JSeparator();
    panelMovieImages.add(separator, "1, 7, 8, 1");

    JLabel lblImageTmdbPosterSize = new JLabel(BUNDLE.getString("image.poster.size"));
    panelMovieImages.add(lblImageTmdbPosterSize, "2, 9");

    cbImagePosterSize = new JComboBox(PosterSizes.values());
    panelMovieImages.add(cbImagePosterSize, "4, 9");

    JLabel lblImageTmdbFanartSize = new JLabel(BUNDLE.getString("image.fanart.size"));
    panelMovieImages.add(lblImageTmdbFanartSize, "2, 11");

    cbImageFanartSize = new JComboBox(FanartSizes.values());
    panelMovieImages.add(cbImageFanartSize, "4, 11");

    separator_2 = new JSeparator();
    panelMovieImages.add(separator_2, "2, 13, 7, 1");

    JLabel lblPosterFilename = new JLabel(BUNDLE.getString("image.poster.naming")); //$NON-NLS-1$
    panelMovieImages.add(lblPosterFilename, "2, 15");

    cbMoviePosterFilename7 = new JCheckBox("<dynamic>.ext"); //$NON-NLS-1$
    panelMovieImages.add(cbMoviePosterFilename7, "4, 15");

    cbMoviePosterFilename4 = new JCheckBox("poster.ext");
    panelMovieImages.add(cbMoviePosterFilename4, "6, 15");

    cbMoviePosterFilename2 = new JCheckBox("movie.ext");
    panelMovieImages.add(cbMoviePosterFilename2, "8, 15");

    cbMoviePosterFilename8 = new JCheckBox("<dynamic>-poster.ext"); //$NON-NLS-1$
    panelMovieImages.add(cbMoviePosterFilename8, "4, 16");

    cbMoviePosterFilename6 = new JCheckBox("folder.ext");
    panelMovieImages.add(cbMoviePosterFilename6, "6, 16");

    JLabel lblFanartFileNaming = new JLabel(BUNDLE.getString("image.fanart.naming")); //$NON-NLS-1$
    panelMovieImages.add(lblFanartFileNaming, "2, 17");

    cbMovieFanartFilename1 = new JCheckBox("<dynamic>-fanart.ext"); //$NON-NLS-1$
    panelMovieImages.add(cbMovieFanartFilename1, "4, 17");

    cbMovieFanartFilename3 = new JCheckBox("<dynamic>.fanart.ext");//$NON-NLS-1$
    panelMovieImages.add(cbMovieFanartFilename3, "6, 17");

    cbMovieFanartFilename2 = new JCheckBox("fanart.ext");
    panelMovieImages.add(cbMovieFanartFilename2, "8, 17");

    tpFileNamingHint = new JTextPane();
    tpFileNamingHint.setText(BUNDLE.getString("Settings.naming.info")); //$NON-NLS-1$
    tpFileNamingHint.setBackground(UIManager.getColor("Panel.background"));
    TmmFontHelper.changeFont(tpFileNamingHint, 0.833);
    panelMovieImages.add(tpFileNamingHint, "2, 18, 5, 1, fill, fill");

    panelExtraArtwork = new JPanel();
    panelExtraArtwork
        .setBorder(new TitledBorder(null, BUNDLE.getString("Settings.extraartwork"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelExtraArtwork, "2, 4, 3, 1, default, fill");
    panelExtraArtwork.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

    chckbxBanner = new JCheckBox(BUNDLE.getString("mediafiletype.banner"));
    panelExtraArtwork.add(chckbxBanner, "2, 2");

    chckbxClearArt = new JCheckBox(BUNDLE.getString("mediafiletype.clearart"));
    panelExtraArtwork.add(chckbxClearArt, "4, 2");

    chckbxThumb = new JCheckBox(BUNDLE.getString("mediafiletype.thumb"));
    panelExtraArtwork.add(chckbxThumb, "6, 2");

    chckbxLogo = new JCheckBox(BUNDLE.getString("mediafiletype.logo"));
    panelExtraArtwork.add(chckbxLogo, "8, 2");

    chckbxDiscArt = new JCheckBox(BUNDLE.getString("mediafiletype.discart"));
    panelExtraArtwork.add(chckbxDiscArt, "10, 2");

    separator_3 = new JSeparator();
    panelExtraArtwork.add(separator_3, "2, 4, 9, 1");

    chckbxEnableExtrathumbs = new JCheckBox(BUNDLE.getString("Settings.enable.extrathumbs"));
    panelExtraArtwork.add(chckbxEnableExtrathumbs, "2, 6, 3, 1");

    chckbxResizeExtrathumbsTo = new JCheckBox(BUNDLE.getString("Settings.resize.extrathumbs"));
    panelExtraArtwork.add(chckbxResizeExtrathumbsTo, "6, 6, 3, 1");

    spExtrathumbWidth = new JSpinner();
    panelExtraArtwork.add(spExtrathumbWidth, "10, 6");
    spExtrathumbWidth.setPreferredSize(new Dimension(49, 20));

    lblDownload = new JLabel(BUNDLE.getString("Settings.amount.autodownload"));
    panelExtraArtwork.add(lblDownload, "2, 8, 7, 1, right, default");

    spDownloadCountExtrathumbs = new JSpinner();
    panelExtraArtwork.add(spDownloadCountExtrathumbs, "10, 8");
    spDownloadCountExtrathumbs.setPreferredSize(new Dimension(49, 20));

    chckbxEnableExtrafanart = new JCheckBox(BUNDLE.getString("Settings.enable.extrafanart"));
    panelExtraArtwork.add(chckbxEnableExtrafanart, "2, 10, 9, 1");

    lblDownloadCount = new JLabel(BUNDLE.getString("Settings.amount.autodownload"));
    panelExtraArtwork.add(lblDownloadCount, "2, 12, 7, 1, right, default");

    spDownloadCountExtrafanart = new JSpinner();
    panelExtraArtwork.add(spDownloadCountExtrafanart, "10, 12");
    spDownloadCountExtrafanart.setPreferredSize(new Dimension(49, 20));

    separator_1 = new JSeparator();
    panelExtraArtwork.add(separator_1, "2, 14, 9, 1");

    cbActorImages = new JCheckBox(BUNDLE.getString("Settings.actor.download"));
    panelExtraArtwork.add(cbActorImages, "2, 16, 9, 1");

    separator_4 = new JSeparator();
    panelExtraArtwork.add(separator_4, "2, 18, 9, 1");

    chckbxStoreMoviesetArtwork = new JCheckBox(BUNDLE.getString("Settings.movieset.store"));
    panelExtraArtwork.add(chckbxStoreMoviesetArtwork, "2, 20, 9, 1");

    lblFoldername = new JLabel(BUNDLE.getString("Settings.movieset.foldername"));
    panelExtraArtwork.add(lblFoldername, "2, 22, 3, 1, right, default");

    tfMovieSetArtworkFolder = new JTextField();
    panelExtraArtwork.add(tfMovieSetArtworkFolder, "6, 22, 3, 1");
    tfMovieSetArtworkFolder.setColumns(10);

    btnSelectFolder = new JButton(BUNDLE.getString("Settings.movieset.buttonselect")); //$NON-NLS-1$
    panelExtraArtwork.add(btnSelectFolder, "10, 22");
    btnSelectFolder.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        File file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.movieset.folderchooser")); //$NON-NLS-1$
        if (file != null && file.exists() && file.isDirectory()) {
          tfMovieSetArtworkFolder.setText(file.getAbsolutePath());
        }
      }
    });

    initDataBindings();

    // poster filenames
    List<MoviePosterNaming> moviePosterFilenames = settings.getMovieSettings().getMoviePosterFilenames();
    if (moviePosterFilenames.contains(MoviePosterNaming.MOVIE_JPG)) {
      cbMoviePosterFilename2.setSelected(true);
    }
    if (moviePosterFilenames.contains(MoviePosterNaming.POSTER_JPG)) {
      cbMoviePosterFilename4.setSelected(true);
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
    List<MovieFanartNaming> movieFanartFilenames = settings.getMovieSettings().getMovieFanartFilenames();
    if (movieFanartFilenames.contains(MovieFanartNaming.FILENAME_FANART_JPG)) {
      cbMovieFanartFilename1.setSelected(true);
    }
    if (movieFanartFilenames.contains(MovieFanartNaming.FANART_JPG)) {
      cbMovieFanartFilename2.setSelected(true);
    }
    if (movieFanartFilenames.contains(MovieFanartNaming.FILENAME_FANART2_JPG)) {
      cbMovieFanartFilename3.setSelected(true);
    }

    // listen to changes of the checkboxes
    ItemListener listener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    };
    cbMoviePosterFilename4.addItemListener(listener);
    cbMoviePosterFilename6.addItemListener(listener);
    cbMoviePosterFilename7.addItemListener(listener);
    cbMoviePosterFilename8.addItemListener(listener);
    cbMoviePosterFilename2.addItemListener(listener);

    cbMovieFanartFilename1.addItemListener(listener);
    cbMovieFanartFilename3.addItemListener(listener);
    cbMovieFanartFilename2.addItemListener(listener);
  }

  /**
   * Check changes.
   */
  public void checkChanges() {
    // set poster filenames
    settings.getMovieSettings().clearMoviePosterFilenames();

    if (cbMoviePosterFilename2.isSelected()) {
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.MOVIE_JPG);
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.MOVIE_PNG);
    }
    if (cbMoviePosterFilename4.isSelected()) {
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.POSTER_PNG);
    }
    if (cbMoviePosterFilename6.isSelected()) {
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.FOLDER_JPG);
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.FOLDER_PNG);
    }
    if (cbMoviePosterFilename7.isSelected()) {
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.FILENAME_JPG);
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.FILENAME_PNG);
    }
    if (cbMoviePosterFilename8.isSelected()) {
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.FILENAME_POSTER_JPG);
      settings.getMovieSettings().addMoviePosterFilename(MoviePosterNaming.FILENAME_POSTER_PNG);
    }

    // set fanart filenames
    settings.getMovieSettings().clearMovieFanartFilenames();
    if (cbMovieFanartFilename1.isSelected()) {
      settings.getMovieSettings().addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART_JPG);
      settings.getMovieSettings().addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART_PNG);
    }
    if (cbMovieFanartFilename2.isSelected()) {
      settings.getMovieSettings().addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
      settings.getMovieSettings().addMovieFanartFilename(MovieFanartNaming.FANART_PNG);
    }
    if (cbMovieFanartFilename3.isSelected()) {
      settings.getMovieSettings().addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART2_JPG);
      settings.getMovieSettings().addMovieFanartFilename(MovieFanartNaming.FILENAME_FANART2_PNG);
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, PosterSizes> settingsBeanProperty_5 = BeanProperty.create("movieSettings.imagePosterSize");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, PosterSizes, JComboBox, Object> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, cbImagePosterSize, jComboBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, FanartSizes> settingsBeanProperty_6 = BeanProperty.create("movieSettings.imageFanartSize");
    AutoBinding<Settings, FanartSizes, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, cbImageFanartSize, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("movieSettings.imageScraperTmdb");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxTheMovieDatabase, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("movieSettings.imageScraperFanartTv");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxFanarttv, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_2 = BeanProperty.create("movieSettings.writeActorImages");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, cbActorImages, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_3 = BeanProperty.create("movieSettings.imageExtraFanart");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, chckbxEnableExtrafanart, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_4 = BeanProperty.create("movieSettings.imageExtraThumbs");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, chckbxEnableExtrathumbs, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, Integer> settingsBeanProperty_8 = BeanProperty.create("movieSettings.imageExtraThumbsSize");
    BeanProperty<JSpinner, Object> jSpinnerBeanProperty_1 = BeanProperty.create("value");
    AutoBinding<Settings, Integer, JSpinner, Object> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, spExtrathumbWidth, jSpinnerBeanProperty_1);
    autoBinding_10.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_9 = BeanProperty.create("movieSettings.imageExtraThumbsResize");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxResizeExtrathumbsTo, jCheckBoxBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<Settings, Integer> settingsBeanProperty_10 = BeanProperty.create("movieSettings.imageExtraThumbsCount");
    AutoBinding<Settings, Integer, JSpinner, Object> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, spDownloadCountExtrathumbs, jSpinnerBeanProperty_1);
    autoBinding_12.bind();
    //
    BeanProperty<Settings, Integer> settingsBeanProperty_11 = BeanProperty.create("movieSettings.imageExtraFanartCount");
    AutoBinding<Settings, Integer, JSpinner, Object> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, spDownloadCountExtrafanart, jSpinnerBeanProperty_1);
    autoBinding_13.bind();
    //
    BeanProperty<JSpinner, Boolean> jSpinnerBeanProperty = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        chckbxEnableExtrafanart, jCheckBoxBeanProperty, spDownloadCountExtrafanart, jSpinnerBeanProperty);
    autoBinding_14.bind();
    //
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        chckbxEnableExtrathumbs, jCheckBoxBeanProperty, spDownloadCountExtrathumbs, jSpinnerBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_12 = BeanProperty.create("movieSettings.movieSetArtworkFolder");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieSetArtworkFolder, jTextFieldBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_13 = BeanProperty.create("movieSettings.enableMovieSetArtworkFolder");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, chckbxStoreMoviesetArtwork, jCheckBoxBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        chckbxEnableExtrathumbs, jCheckBoxBeanProperty, chckbxResizeExtrathumbsTo, jCheckBoxBeanProperty_1);
    autoBinding_8.bind();
    //
    AutoBinding<JCheckBox, Boolean, JSpinner, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, chckbxEnableExtrathumbs,
        jCheckBoxBeanProperty, spExtrathumbWidth, jSpinnerBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_7 = BeanProperty.create("movieSettings.imageBanner");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, chckbxBanner, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_14 = BeanProperty.create("movieSettings.imageClearart");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_14, chckbxClearArt, jCheckBoxBeanProperty);
    autoBinding_18.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_15 = BeanProperty.create("movieSettings.imageThumb");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_15, chckbxThumb, jCheckBoxBeanProperty);
    autoBinding_19.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_16 = BeanProperty.create("movieSettings.imageLogo");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_16, chckbxLogo, jCheckBoxBeanProperty);
    autoBinding_20.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_17 = BeanProperty.create("movieSettings.imageDiscart");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_21 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_17, chckbxDiscArt, jCheckBoxBeanProperty);
    autoBinding_21.bind();
  }
}
