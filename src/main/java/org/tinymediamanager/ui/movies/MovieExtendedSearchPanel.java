/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.japura.gui.CheckComboBox;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSearchOptions;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.ui.SmallCheckBoxUI;
import org.tinymediamanager.ui.SmallSpinnerUI;
import org.tinymediamanager.ui.SmallTextFieldBorder;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.SmallCheckComboBox;
import org.tinymediamanager.ui.components.SmallComboBox;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.MovieInMovieSet;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.OfflineMovie;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortOrder;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.WatchedFlag;
import org.tinymediamanager.ui.panels.RoundedPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.AbstractLookAndFeel;

/**
 * The Class MovieExtendedSearchPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieExtendedSearchPanel extends RoundedPanel {
  private static final long            serialVersionUID = -4170930017190753789L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());               //$NON-NLS-1$
  private static final float           FONT_SIZE        = Math.round(Globals.settings.getFontSize() * 0.916);
  private static final SmallCheckBoxUI CHECKBOX_UI      = AbstractLookAndFeel.getTheme() != null ? new SmallCheckBoxUI() : null; // hint for WBPro

  private MovieList                    movieList        = MovieList.getInstance();
  private MovieSelectionModel          movieSelectionModel;

  /**
   * UI Elements
   */
  private JCheckBox                    cbFilterWatched;
  private JComboBox                    cbGenre;
  private JComboBox                    cbSortColumn;
  private JComboBox                    cbSortOrder;
  private JComboBox                    cbWatched;
  private JCheckBox                    cbFilterGenre;
  private JCheckBox                    cbFilterCast;
  private JTextField                   tfCastMember;
  private JCheckBox                    cbFilterTag;
  private CheckComboBox                cbTag;
  private JCheckBox                    cbFilterDuplicates;
  private JCheckBox                    cbFilterMovieset;
  private JComboBox                    cbMovieset;
  private JCheckBox                    cbFilterVideoFormat;
  private JComboBox                    cbVideoFormat;
  private JCheckBox                    cbFilterVideoCodec;
  private JComboBox                    cbVideoCodec;
  private JCheckBox                    cbFilterAudioCodec;
  private JComboBox                    cbAudioCodec;
  private JCheckBox                    cbFilterDatasource;
  private CheckComboBox                cbDatasource;
  private JCheckBox                    cbFilterMissingMetadata;
  private JCheckBox                    cbFilterMissingArtwork;
  private JCheckBox                    cbFilterMissingSubtitles;
  private JCheckBox                    cbFilterVideo3D;

  private final Action                 actionSort       = new SortAction();
  private final Action                 actionFilter     = new FilterAction();
  private final ListCheckListener      listCheckListener;
  private JCheckBox                    cbFilterNewMovies;
  private JLabel                       lblNewMovies;
  private JCheckBox                    cbFilterCertification;
  private JLabel                       lblCertification;
  private JComboBox                    cbCertification;
  private JCheckBox                    cbFilterMediaSource;
  private JComboBox                    cbMediaSource;
  private JCheckBox                    cbFilterYear;
  private JLabel                       lblYear;
  private JSpinner                     spYear;
  private JCheckBox                    cbFilterOffline;
  private JLabel                       lblOffline;
  private JComboBox                    cbOffline;

  /**
   * Instantiates a new movie extended search
   * 
   * @param model
   *          the model
   */
  public MovieExtendedSearchPanel(MovieSelectionModel model) {
    super();
    setOpaque(false);
    shadowAlpha = 100;
    arcs = new Dimension(10, 10);

    this.movieSelectionModel = model;
    Map<MovieSearchOptions, Object> savedSearchOptions = MovieModuleManager.MOVIE_SETTINGS.getUiFilters();

    // add a dummy mouse listener to prevent clicking through
    addMouseListener(new MouseAdapter() {
    });

    listCheckListener = new ListCheckListener() {
      @Override
      public void removeCheck(ListEvent event) {
        actionFilter.actionPerformed(new ActionEvent(event.getSource(), 1, "checked"));
      }

      @Override
      public void addCheck(ListEvent event) {
        actionFilter.actionPerformed(new ActionEvent(event.getSource(), 1, "checked"));
      }
    };

    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.UNRELATED_GAP_ROWSPEC, }));

    JLabel lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    setComponentFont(lblFilterBy);
    add(lblFilterBy, "2, 2, 3, 1");

    cbFilterNewMovies = new JCheckBox("");
    cbFilterNewMovies.setUI(CHECKBOX_UI); // $hide$
    cbFilterNewMovies.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.NEW_MOVIES));
    cbFilterNewMovies.setAction(actionFilter);
    add(cbFilterNewMovies, "2, 4");

    lblNewMovies = new JLabel(BUNDLE.getString("movieextendedsearch.newmovies")); //$NON-NLS-1$
    setComponentFont(lblNewMovies);
    add(lblNewMovies, "4, 4, right, default");

    cbFilterDuplicates = new JCheckBox("");
    cbFilterDuplicates.setUI(CHECKBOX_UI); // $hide$
    cbFilterDuplicates.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.DUPLICATES));
    cbFilterDuplicates.setAction(actionFilter);
    add(cbFilterDuplicates, "2, 5");

    JLabel lblShowDuplicates = new JLabel(BUNDLE.getString("movieextendedsearch.duplicates")); //$NON-NLS-1$
    setComponentFont(lblShowDuplicates);
    add(lblShowDuplicates, "4, 5, right, default");

    cbFilterWatched = new JCheckBox("");
    cbFilterWatched.setUI(CHECKBOX_UI); // $hide$
    cbFilterWatched.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.WATCHED));
    cbFilterWatched.setAction(actionFilter);
    add(cbFilterWatched, "2, 6");

    JLabel lblWatchedFlag = new JLabel(BUNDLE.getString("movieextendedsearch.watched")); //$NON-NLS-1$
    setComponentFont(lblWatchedFlag);
    add(lblWatchedFlag, "4, 6, right, default");

    cbWatched = new SmallComboBox(WatchedFlag.values());
    setComponentFont(cbWatched);
    cbWatched.setAction(actionFilter);
    add(cbWatched, "6, 6, fill, default");

    cbFilterGenre = new JCheckBox("");
    cbFilterGenre.setUI(CHECKBOX_UI); // $hide$
    cbFilterGenre.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.GENRE));
    cbFilterGenre.setAction(actionFilter);
    add(cbFilterGenre, "2, 7");

    JLabel lblGenre = new JLabel(BUNDLE.getString("movieextendedsearch.genre")); //$NON-NLS-1$
    setComponentFont(lblGenre);
    add(lblGenre, "4, 7, right, default");

    cbGenre = new SmallComboBox(MediaGenres.values());
    setComponentFont(cbGenre);
    cbGenre.setAction(actionFilter);
    add(cbGenre, "6, 7, fill, default");

    cbFilterCertification = new JCheckBox("");
    cbFilterCertification.setUI(CHECKBOX_UI); // $hide$
    cbFilterCertification.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.CERTIFICATION));
    cbFilterCertification.setAction(actionFilter);
    add(cbFilterCertification, "2, 8");

    lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    setComponentFont(lblCertification);
    add(lblCertification, "4, 8, right, default");

    cbCertification = new SmallComboBox();
    setComponentFont(cbCertification);
    cbCertification.setAction(actionFilter);
    add(cbCertification, "6, 8, fill, default");

    cbFilterYear = new JCheckBox("");
    cbFilterYear.setUI(CHECKBOX_UI); // $hide$
    cbFilterYear.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.YEAR));
    cbFilterYear.setAction(actionFilter);
    add(cbFilterYear, "2, 9");

    lblYear = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    setComponentFont(lblYear);
    add(lblYear, "4, 9, right, default");

    int year = Calendar.getInstance().get(Calendar.YEAR);
    spYear = new JSpinner();
    setComponentFont(spYear);
    spYear.setUI(new SmallSpinnerUI());// $hide$
    spYear.setModel(new SpinnerNumberModel(year, 0, 3000, 1));
    spYear.setEditor(new JSpinner.NumberEditor(spYear, "#"));
    spYear.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        actionFilter.actionPerformed(null);
      }
    });
    add(spYear, "6, 9");

    cbFilterCast = new JCheckBox("");
    cbFilterCast.setUI(CHECKBOX_UI); // $hide$
    cbFilterCast.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.CAST));
    cbFilterCast.setAction(actionFilter);
    add(cbFilterCast, "2, 10");

    JLabel lblCastMember = new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
    setComponentFont(lblCastMember);
    add(lblCastMember, "4, 10, right, default");

    tfCastMember = new JTextField();
    setComponentFont(tfCastMember);
    tfCastMember.setBorder(new SmallTextFieldBorder());
    add(tfCastMember, "6, 10, fill, default");
    tfCastMember.setColumns(10);
    tfCastMember.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        actionFilter.actionPerformed(null);
      }

      public void insertUpdate(DocumentEvent e) {
        actionFilter.actionPerformed(null);
      }

      public void removeUpdate(DocumentEvent e) {
        actionFilter.actionPerformed(null);
      }
    });

    cbFilterTag = new JCheckBox("");
    cbFilterTag.setUI(CHECKBOX_UI); // $hide$
    cbFilterTag.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.TAG));
    cbFilterTag.setAction(actionFilter);
    add(cbFilterTag, "2, 11");

    JLabel lblTag = new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
    setComponentFont(lblTag);
    add(lblTag, "4, 11, right, default");

    cbTag = new SmallCheckComboBox();
    cbTag.setTextFor(CheckComboBox.NONE, BUNDLE.getString("movieextendedsearch.tags.selected.none")); //$NON-NLS-1$
    cbTag.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("movieextendedsearch.tags.selected.multiple")); //$NON-NLS-1$
    cbTag.setTextFor(CheckComboBox.ALL, BUNDLE.getString("movieextendedsearch.tags.selected.all")); //$NON-NLS-1$
    cbTag.getModel().addListCheckListener(listCheckListener);
    add(cbTag, "6, 11, fill, default");

    cbFilterMovieset = new JCheckBox("");
    cbFilterMovieset.setUI(CHECKBOX_UI); // $hide$
    cbFilterMovieset.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.MOVIESET));
    cbFilterMovieset.setAction(actionFilter);
    add(cbFilterMovieset, "2, 12");

    JLabel lblMoviesInMovieset = new JLabel(BUNDLE.getString("movieextendedsearch.movieset")); //$NON-NLS-1$
    setComponentFont(lblMoviesInMovieset);
    add(lblMoviesInMovieset, "4, 12, right, default");

    cbMovieset = new SmallComboBox(MovieInMovieSet.values());
    setComponentFont(cbMovieset);
    cbMovieset.setAction(actionFilter);
    add(cbMovieset, "6, 12, fill, default");

    cbFilterVideoFormat = new JCheckBox("");
    cbFilterVideoFormat.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideoFormat.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.VIDEO_FORMAT));
    cbFilterVideoFormat.setAction(actionFilter);
    add(cbFilterVideoFormat, "2, 13");

    JLabel lblVideoFormat = new JLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
    setComponentFont(lblVideoFormat);
    add(lblVideoFormat, "4, 13, right, default");

    cbVideoFormat = new SmallComboBox(getVideoFormats());
    setComponentFont(cbVideoFormat);
    cbVideoFormat.setAction(actionFilter);
    add(cbVideoFormat, "6, 13, fill, default");

    cbFilterVideoCodec = new JCheckBox("");
    cbFilterVideoCodec.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideoCodec.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.VIDEO_CODEC));
    cbFilterVideoCodec.setAction(actionFilter);
    add(cbFilterVideoCodec, "2, 14");

    JLabel lblVideoCodec = new JLabel(BUNDLE.getString("metatag.videocodec")); //$NON-NLS-1$
    setComponentFont(lblVideoCodec);
    add(lblVideoCodec, "4, 14, right, default");

    cbVideoCodec = new SmallComboBox();
    setComponentFont(cbVideoCodec);
    cbVideoCodec.setAction(actionFilter);
    add(cbVideoCodec, "6, 14, fill, default");

    cbFilterVideo3D = new JCheckBox("");
    cbFilterVideo3D.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideo3D.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.VIDEO_3D));
    cbFilterVideo3D.addActionListener(actionFilter);
    add(cbFilterVideo3D, "2, 15");

    JLabel lblVideo3D = new JLabel(BUNDLE.getString("metatag.3d")); //$NON-NLS-1$
    setComponentFont(lblVideo3D);
    add(lblVideo3D, "4, 15, right, default");

    cbFilterAudioCodec = new JCheckBox("");
    cbFilterAudioCodec.setUI(CHECKBOX_UI); // $hide$
    cbFilterAudioCodec.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.AUDIO_CODEC));
    cbFilterAudioCodec.setAction(actionFilter);
    add(cbFilterAudioCodec, "2, 16");

    JLabel lblAudioCodec = new JLabel(BUNDLE.getString("metatag.audiocodec")); //$NON-NLS-1$
    setComponentFont(lblAudioCodec);
    add(lblAudioCodec, "4, 16, right, default");

    cbAudioCodec = new SmallComboBox();
    setComponentFont(cbAudioCodec);
    cbAudioCodec.setAction(actionFilter);
    add(cbAudioCodec, "6, 16, fill, default");

    cbFilterDatasource = new JCheckBox("");
    cbFilterDatasource.setUI(CHECKBOX_UI); // $hide$
    cbFilterDatasource.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.DATASOURCE));
    cbFilterDatasource.setAction(actionFilter);
    add(cbFilterDatasource, "2, 17");

    JLabel lblDatasource = new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
    setComponentFont(lblDatasource);
    add(lblDatasource, "4, 17, right, default");

    cbDatasource = new SmallCheckComboBox();
    cbDatasource.setTextFor(CheckComboBox.NONE, BUNDLE.getString("checkcombobox.selected.none")); //$NON-NLS-1$
    cbDatasource.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("checkcombobox.selected.multiple")); //$NON-NLS-1$
    cbDatasource.setTextFor(CheckComboBox.ALL, BUNDLE.getString("checkcombobox.selected.all")); //$NON-NLS-1$
    cbDatasource.getModel().addListCheckListener(listCheckListener);
    add(cbDatasource, "6, 17, fill, default");

    cbFilterMediaSource = new JCheckBox("");
    cbFilterMediaSource.setUI(CHECKBOX_UI); // $hide$
    cbFilterMediaSource.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.MEDIA_SOURCE));
    cbFilterMediaSource.addActionListener(actionFilter);
    add(cbFilterMediaSource, "2, 18");

    JLabel lblMediaSource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
    setComponentFont(lblMediaSource);
    add(lblMediaSource, "4, 18, right, default");

    cbMediaSource = new SmallComboBox(MediaSource.values());
    setComponentFont(cbMediaSource);
    cbMediaSource.setAction(actionFilter);
    add(cbMediaSource, "6, 18, fill, default");

    cbFilterMissingMetadata = new JCheckBox("");
    cbFilterMissingMetadata.setUI(CHECKBOX_UI); // $hide$
    cbFilterMissingMetadata.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.MISSING_METADATA));
    cbFilterMissingMetadata.setAction(actionFilter);
    add(cbFilterMissingMetadata, "2, 19");

    JLabel lblMissingMetadata = new JLabel(BUNDLE.getString("movieextendedsearch.missingmetadata")); //$NON-NLS-1$
    setComponentFont(lblMissingMetadata);
    add(lblMissingMetadata, "4, 19, right, default");

    cbFilterMissingArtwork = new JCheckBox("");
    cbFilterMissingArtwork.setUI(CHECKBOX_UI); // $hide$
    cbFilterMissingArtwork.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.MISSING_ARTWORK));
    cbFilterMissingArtwork.setAction(actionFilter);
    add(cbFilterMissingArtwork, "2, 20");

    JLabel lblMissingArtwork = new JLabel(BUNDLE.getString("movieextendedsearch.missingartwork")); //$NON-NLS-1$
    setComponentFont(lblMissingArtwork);
    add(lblMissingArtwork, "4, 20, right, default");

    cbFilterMissingSubtitles = new JCheckBox("");
    cbFilterMissingSubtitles.setUI(CHECKBOX_UI); // $hide$
    cbFilterMissingSubtitles.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.MISSING_SUBTITLES));
    cbFilterMissingSubtitles.setAction(actionFilter);
    add(cbFilterMissingSubtitles, "2, 21");

    JLabel lblMissingSubtitles = new JLabel(BUNDLE.getString("movieextendedsearch.missingsubtitles")); //$NON-NLS-1$
    setComponentFont(lblMissingSubtitles);
    add(lblMissingSubtitles, "4, 21, right, default");

    cbFilterOffline = new JCheckBox("");
    cbFilterOffline.setUI(CHECKBOX_UI); // $hide$
    cbFilterOffline.setSelected(savedSearchOptions.containsKey(MovieSearchOptions.OFFLINE));
    cbFilterOffline.setAction(actionFilter);
    add(cbFilterOffline, "2, 22");

    lblOffline = new JLabel(BUNDLE.getString("movieextendedsearch.offline")); //$NON-NLS-1$
    setComponentFont(lblOffline);
    add(lblOffline, "4, 22, right, default");

    cbOffline = new SmallComboBox(OfflineMovie.values());
    cbOffline.setAction(actionFilter);
    add(cbOffline, "6, 22, fill, default");

    JSeparator separator = new JSeparator();
    add(separator, "2, 24, 5, 1");

    JLabel lblSortBy = new JLabel(BUNDLE.getString("movieextendedsearch.sortby")); //$NON-NLS-1$
    setComponentFont(lblSortBy);
    add(lblSortBy, "2, 26, 3, 1");

    cbSortColumn = new SmallComboBox(SortColumn.values());
    setComponentFont(cbSortColumn);
    cbSortColumn.setAction(actionSort);
    add(cbSortColumn, "2, 28, 3, 1, fill, default");

    cbSortOrder = new SmallComboBox(SortOrder.values());
    setComponentFont(cbSortOrder);
    cbSortOrder.setAction(actionSort);
    add(cbSortOrder, "6, 28, fill, default");

    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof MovieList && "tag".equals(evt.getPropertyName())) {
          buildAndInstallTagsArray();
        }
        if (evt.getSource() instanceof MovieList && "videoCodec".equals(evt.getPropertyName())) {
          buildAndInstallCodecArray();
        }
        if (evt.getSource() instanceof MovieList && "audioCodec".equals(evt.getPropertyName())) {
          buildAndInstallCodecArray();
        }
        if (evt.getSource() instanceof MovieSettings && "movieDataSource".equals(evt.getPropertyName())) {
          buildAndInstallDatasourceArray();
        }
        if (evt.getSource() instanceof MovieList && "certification".equals(evt.getPropertyName())) {
          buildAndInstallCertificationArray();
        }
      }
    };
    movieList.addPropertyChangeListener(propertyChangeListener);
    MovieModuleManager.MOVIE_SETTINGS.addPropertyChangeListener(propertyChangeListener);
    buildAndInstallTagsArray();
    buildAndInstallCodecArray();
    buildAndInstallDatasourceArray();
    buildAndInstallCertificationArray();
  }

  private void buildAndInstallTagsArray() {
    // remember old value and remove listener
    List<Object> oldValues = cbTag.getModel().getCheckeds();
    cbTag.getModel().removeListCheckListener(listCheckListener);

    // build up the new checkbox
    cbTag.getModel().clear();
    List<String> tags = new ArrayList<>(movieList.getTagsInMovies());
    Collections.sort(tags);
    for (String tag : tags) {
      cbTag.getModel().addElement(tag);
    }

    // re-set the value and readd action listener
    if (oldValues != null) {
      for (Object obj : oldValues) {
        cbTag.getModel().setCheck(obj);
      }
    }
    cbTag.getModel().addListCheckListener(listCheckListener);
  }

  private void buildAndInstallCodecArray() {
    // remember old value and remove listener
    Object oldValue = cbVideoCodec.getSelectedItem();
    cbVideoCodec.removeActionListener(actionFilter);

    // build up the new cb
    cbVideoCodec.removeAllItems();
    List<String> codecs = new ArrayList<>(movieList.getVideoCodecsInMovies());
    Collections.sort(codecs);
    for (String codec : codecs) {
      cbVideoCodec.addItem(codec);
    }

    // re-set the value and readd action listener
    if (oldValue != null) {
      cbVideoCodec.setSelectedItem(oldValue);
    }
    cbVideoCodec.addActionListener(actionFilter);

    // remember old value and remove listener
    oldValue = cbAudioCodec.getSelectedItem();
    cbAudioCodec.removeActionListener(actionFilter);

    // build up the new cb
    cbAudioCodec.removeAllItems();
    codecs = new ArrayList<>(movieList.getAudioCodecsInMovies());
    Collections.sort(codecs);
    for (String codec : codecs) {
      cbAudioCodec.addItem(codec);
    }

    // re-set the value and readd action listener
    if (oldValue != null) {
      cbAudioCodec.setSelectedItem(oldValue);
    }
    cbAudioCodec.addActionListener(actionFilter);
  }

  private void buildAndInstallDatasourceArray() {
    // remember old value and remove listener
    List<Object> oldValues = cbDatasource.getModel().getCheckeds();
    cbDatasource.getModel().removeListCheckListener(listCheckListener);

    // build up the new checkbox
    cbDatasource.getModel().clear();
    List<String> datasources = new ArrayList<>(MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource());
    Collections.sort(datasources);
    for (String datasource : datasources) {
      cbDatasource.getModel().addElement(datasource);
    }

    // re-set the value and readd action listener
    if (oldValues != null) {
      for (Object obj : oldValues) {
        cbDatasource.getModel().setCheck(obj);
      }
    }
    cbDatasource.getModel().addListCheckListener(listCheckListener);
  }

  private void buildAndInstallCertificationArray() {
    // remember old value and remove listener
    Object oldValue = cbCertification.getSelectedItem();
    cbCertification.removeActionListener(actionFilter);

    // build up the new cb
    cbCertification.removeAllItems();
    List<Certification> certifications = new ArrayList<>(movieList.getCertificationsInMovies());
    Collections.sort(certifications);
    for (Certification cert : certifications) {
      cbCertification.addItem(cert);
    }

    // re-set the value and readd action listener
    if (oldValue != null) {
      cbCertification.setSelectedItem(oldValue);
    }
    cbCertification.addActionListener(actionFilter);
  }

  private class SortAction extends AbstractAction {
    private static final long serialVersionUID = -4057379119252539003L;

    @Override
    public void actionPerformed(ActionEvent e) {
      SortColumn column = (SortColumn) cbSortColumn.getSelectedItem();
      SortOrder order = (SortOrder) cbSortOrder.getSelectedItem();
      boolean ascending = order == SortOrder.ASCENDING ? true : false;

      // sort
      movieSelectionModel.sortMovies(column, ascending);
    }
  }

  private class FilterAction extends AbstractAction {
    private static final long serialVersionUID = 7488733475791640009L;

    @Override
    public void actionPerformed(ActionEvent e) {
      HashMap<MovieSearchOptions, Object> searchOptions = new HashMap<>();

      // filter duplicates
      if (cbFilterDuplicates.isSelected()) {
        movieList.searchDuplicates();
        searchOptions.put(MovieSearchOptions.DUPLICATES, true);
      }

      // filter for watched flag
      if (cbFilterWatched.isSelected()) {
        if (cbWatched.getSelectedItem() == WatchedFlag.WATCHED) {
          searchOptions.put(MovieSearchOptions.WATCHED, true);
        }
        else {
          searchOptions.put(MovieSearchOptions.WATCHED, false);
        }
      }

      // filter by genre
      if (cbFilterGenre.isSelected()) {
        MediaGenres genre = (MediaGenres) cbGenre.getSelectedItem();
        if (genre != null) {
          searchOptions.put(MovieSearchOptions.GENRE, genre);
        }
      }

      // filter by certification
      if (cbFilterCertification.isSelected()) {
        Certification cert = (Certification) cbCertification.getSelectedItem();
        if (cert != null) {
          searchOptions.put(MovieSearchOptions.CERTIFICATION, cert);
        }
      }

      // filter by cast
      if (cbFilterCast.isSelected()) {
        if (StringUtils.isNotBlank(tfCastMember.getText())) {
          searchOptions.put(MovieSearchOptions.CAST, tfCastMember.getText());
        }
      }

      // filter by tag
      if (cbFilterTag.isSelected()) {
        List<Object> tags = cbTag.getModel().getCheckeds();
        searchOptions.put(MovieSearchOptions.TAG, tags);
      }

      // filter by movie in movieset
      if (cbFilterMovieset.isSelected()) {
        if (cbMovieset.getSelectedItem() == MovieInMovieSet.IN_MOVIESET) {
          searchOptions.put(MovieSearchOptions.MOVIESET, true);
        }
        else {
          searchOptions.put(MovieSearchOptions.MOVIESET, false);
        }
      }

      // filter by video format
      if (cbFilterVideoFormat.isSelected()) {
        String videoFormat = (String) cbVideoFormat.getSelectedItem();
        searchOptions.put(MovieSearchOptions.VIDEO_FORMAT, videoFormat);
      }

      // filter by video codec
      if (cbFilterVideoCodec.isSelected()) {
        String videoCodec = (String) cbVideoCodec.getSelectedItem();
        if (StringUtils.isNotBlank(videoCodec)) {
          searchOptions.put(MovieSearchOptions.VIDEO_CODEC, videoCodec);
        }
      }

      // filter by audio codec
      if (cbFilterAudioCodec.isSelected()) {
        String audioCodec = (String) cbAudioCodec.getSelectedItem();
        if (StringUtils.isNotBlank(audioCodec)) {
          searchOptions.put(MovieSearchOptions.AUDIO_CODEC, audioCodec);
        }
      }

      // filter by datasource
      if (cbFilterDatasource.isSelected()) {
        List<Object> datasources = cbDatasource.getModel().getCheckeds();
        searchOptions.put(MovieSearchOptions.DATASOURCE, datasources);
      }

      // filer by missing metadata
      if (cbFilterMissingMetadata.isSelected()) {
        searchOptions.put(MovieSearchOptions.MISSING_METADATA, Boolean.TRUE);
      }

      // filer by missing artwork
      if (cbFilterMissingArtwork.isSelected()) {
        searchOptions.put(MovieSearchOptions.MISSING_ARTWORK, Boolean.TRUE);
      }

      // filer by missing artwork
      if (cbFilterMissingSubtitles.isSelected()) {
        searchOptions.put(MovieSearchOptions.MISSING_SUBTITLES, Boolean.TRUE);
      }

      // filter by new movies
      if (cbFilterNewMovies.isSelected()) {
        searchOptions.put(MovieSearchOptions.NEW_MOVIES, Boolean.TRUE);
      }

      // filter by media source
      if (cbFilterMediaSource.isSelected()) {
        searchOptions.put(MovieSearchOptions.MEDIA_SOURCE, cbMediaSource.getSelectedItem());
      }

      // filter by year
      if (cbFilterYear.isSelected()) {
        searchOptions.put(MovieSearchOptions.YEAR, spYear.getValue());
      }

      // filter by 3D
      if (cbFilterVideo3D.isSelected()) {
        searchOptions.put(MovieSearchOptions.VIDEO_3D, Boolean.TRUE);
      }

      // filter by offline
      if (cbFilterOffline.isSelected()) {
        if (cbOffline.getSelectedItem() == OfflineMovie.OFFLINE) {
          searchOptions.put(MovieSearchOptions.OFFLINE, Boolean.TRUE);
        }
        else {
          searchOptions.put(MovieSearchOptions.OFFLINE, Boolean.FALSE);
        }
      }

      // apply the filter
      movieSelectionModel.filterMovies(searchOptions);
    }
  }

  private String[] getVideoFormats() {
    return new String[] { MediaFile.VIDEO_FORMAT_480P, MediaFile.VIDEO_FORMAT_540P, MediaFile.VIDEO_FORMAT_576P, MediaFile.VIDEO_FORMAT_720P,
        MediaFile.VIDEO_FORMAT_1080P, MediaFile.VIDEO_FORMAT_4K, MediaFile.VIDEO_FORMAT_SD, MediaFile.VIDEO_FORMAT_HD }; // MediaFile.VIDEO_FORMAT_8K,
  }

  private void setComponentFont(JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(FONT_SIZE));
  }
}
