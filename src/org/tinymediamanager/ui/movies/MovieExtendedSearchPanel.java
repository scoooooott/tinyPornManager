/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaSource;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.SmallCheckBoxUI;
import org.tinymediamanager.ui.SmallTextFieldBorder;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.RoundedPanel;
import org.tinymediamanager.ui.components.SmallComboBox;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.MovieInMovieSet;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortOrder;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.WatchedFlag;
import org.tinymediamanager.ui.movies.MoviesExtendedMatcher.SearchOptions;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.AbstractLookAndFeel;

/**
 * The Class MovieExtendedSearchPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieExtendedSearchPanel extends RoundedPanel {
  private static final long            serialVersionUID = -4170930017190753789L;
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$
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
  private JComboBox                    cbTag;
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
  private JComboBox                    cbDatasource;
  private JCheckBox                    cbFilterMissingMetadata;
  private JCheckBox                    cbFilterMissingArtwork;
  private JCheckBox                    cbFilterMissingSubtitles;

  private final Action                 actionSort       = new SortAction();
  private final Action                 actionFilter     = new FilterAction();
  private JCheckBox                    cbFilterNewMovies;
  private JLabel                       lblNewMovies;
  private JCheckBox                    cbFilterCertification;
  private JLabel                       lblCertification;
  private JComboBox                    cbCertification;
  private JCheckBox                    cbFilterMediaSource;
  private JComboBox                    cbMediaSource;

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

    // add a dummy mouse listener to prevent clicking through
    addMouseListener(new MouseAdapter() {
    });

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, }));

    JLabel lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    setComponentFont(lblFilterBy);
    add(lblFilterBy, "2, 2, 3, 1");

    cbFilterDuplicates = new JCheckBox("");
    cbFilterDuplicates.setUI(CHECKBOX_UI); // $hide$
    cbFilterDuplicates.setAction(actionFilter);
    add(cbFilterDuplicates, "2, 4");

    JLabel lblShowDuplicates = new JLabel(BUNDLE.getString("movieextendedsearch.duplicates")); //$NON-NLS-1$
    setComponentFont(lblShowDuplicates);
    add(lblShowDuplicates, "4, 4, right, default");

    cbFilterWatched = new JCheckBox("");
    cbFilterWatched.setUI(CHECKBOX_UI); // $hide$
    cbFilterWatched.setAction(actionFilter);
    add(cbFilterWatched, "2, 5");

    JLabel lblWatchedFlag = new JLabel(BUNDLE.getString("movieextendedsearch.watched")); //$NON-NLS-1$
    setComponentFont(lblWatchedFlag);
    add(lblWatchedFlag, "4, 5, right, default");

    cbWatched = new SmallComboBox(WatchedFlag.values());
    setComponentFont(cbWatched);
    cbWatched.setAction(actionFilter);
    add(cbWatched, "6, 5, fill, default");

    cbFilterGenre = new JCheckBox("");
    cbFilterGenre.setUI(CHECKBOX_UI); // $hide$
    cbFilterGenre.setAction(actionFilter);
    add(cbFilterGenre, "2, 6");

    JLabel lblGenre = new JLabel(BUNDLE.getString("movieextendedsearch.genre")); //$NON-NLS-1$
    setComponentFont(lblGenre);
    add(lblGenre, "4, 6, right, default");

    cbGenre = new SmallComboBox(MediaGenres.values());
    setComponentFont(cbGenre);
    cbGenre.setAction(actionFilter);
    add(cbGenre, "6, 6, fill, default");

    cbFilterCertification = new JCheckBox("");
    cbFilterCertification.setUI(CHECKBOX_UI); // $hide$
    cbFilterCertification.setAction(actionFilter);
    add(cbFilterCertification, "2, 7");

    lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    setComponentFont(lblCertification);
    add(lblCertification, "4, 7, right, default");

    cbCertification = new SmallComboBox();
    setComponentFont(cbCertification);
    cbCertification.setAction(actionFilter);
    add(cbCertification, "6, 7, fill, default");

    cbFilterCast = new JCheckBox("");
    cbFilterCast.setUI(CHECKBOX_UI); // $hide$
    cbFilterCast.setAction(actionFilter);
    add(cbFilterCast, "2, 8");

    JLabel lblCastMember = new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
    setComponentFont(lblCastMember);
    add(lblCastMember, "4, 8, right, default");

    tfCastMember = new JTextField();
    setComponentFont(tfCastMember);
    tfCastMember.setBorder(new SmallTextFieldBorder());
    add(tfCastMember, "6, 8, fill, default");
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
    cbFilterTag.setAction(actionFilter);
    add(cbFilterTag, "2, 9");

    JLabel lblTag = new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
    setComponentFont(lblTag);
    add(lblTag, "4, 9, right, default");

    cbTag = new SmallComboBox();
    setComponentFont(cbTag);
    cbTag.setAction(actionFilter);

    add(cbTag, "6, 9, fill, default");

    cbFilterMovieset = new JCheckBox("");
    cbFilterMovieset.setUI(CHECKBOX_UI); // $hide$
    cbFilterMovieset.setAction(actionFilter);
    add(cbFilterMovieset, "2, 10");

    JLabel lblMoviesInMovieset = new JLabel(BUNDLE.getString("movieextendedsearch.movieset")); //$NON-NLS-1$
    setComponentFont(lblMoviesInMovieset);
    add(lblMoviesInMovieset, "4, 10, right, default");

    cbMovieset = new SmallComboBox(MovieInMovieSet.values());
    setComponentFont(cbMovieset);
    cbMovieset.setAction(actionFilter);
    add(cbMovieset, "6, 10, fill, default");

    cbFilterVideoFormat = new JCheckBox("");
    cbFilterVideoFormat.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideoFormat.setAction(actionFilter);
    add(cbFilterVideoFormat, "2, 11");

    JLabel lblVideoFormat = new JLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
    setComponentFont(lblVideoFormat);
    add(lblVideoFormat, "4, 11, right, default");

    cbVideoFormat = new SmallComboBox(getVideoFormats());
    setComponentFont(cbVideoFormat);
    cbVideoFormat.setAction(actionFilter);
    add(cbVideoFormat, "6, 11, fill, default");

    cbFilterVideoCodec = new JCheckBox("");
    cbFilterVideoCodec.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideoCodec.setAction(actionFilter);
    add(cbFilterVideoCodec, "2, 12");

    JLabel lblVideoCodec = new JLabel(BUNDLE.getString("metatag.videocodec")); //$NON-NLS-1$
    setComponentFont(lblVideoCodec);
    add(lblVideoCodec, "4, 12, right, default");

    cbVideoCodec = new SmallComboBox();
    setComponentFont(cbVideoCodec);
    cbVideoCodec.setAction(actionFilter);
    add(cbVideoCodec, "6, 12, fill, default");

    cbFilterAudioCodec = new JCheckBox("");
    cbFilterAudioCodec.setUI(CHECKBOX_UI); // $hide$
    cbFilterAudioCodec.setAction(actionFilter);
    add(cbFilterAudioCodec, "2, 13");

    JLabel lblAudioCodec = new JLabel(BUNDLE.getString("metatag.audiocodec")); //$NON-NLS-1$
    setComponentFont(lblAudioCodec);
    add(lblAudioCodec, "4, 13, right, default");

    cbAudioCodec = new SmallComboBox();
    setComponentFont(cbAudioCodec);
    cbAudioCodec.setAction(actionFilter);
    add(cbAudioCodec, "6, 13, fill, default");

    cbFilterDatasource = new JCheckBox("");
    cbFilterDatasource.setUI(CHECKBOX_UI); // $hide$
    cbFilterDatasource.setAction(actionFilter);
    add(cbFilterDatasource, "2, 14");

    JLabel lblDatasource = new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
    setComponentFont(lblDatasource);
    add(lblDatasource, "4, 14, right, default");

    cbDatasource = new SmallComboBox();
    setComponentFont(cbDatasource);
    cbDatasource.setAction(actionFilter);
    add(cbDatasource, "6, 14, fill, default");

    cbFilterMediaSource = new JCheckBox("");
    cbFilterMediaSource.addActionListener(actionFilter);
    cbFilterMediaSource.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMediaSource, "2, 15");

    JLabel lblMediaSource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
    setComponentFont(lblMediaSource);
    add(lblMediaSource, "4, 15, right, default");

    cbMediaSource = new SmallComboBox(MovieMediaSource.values());
    setComponentFont(cbMediaSource);
    cbMediaSource.setAction(actionFilter);
    add(cbMediaSource, "6, 15, fill, default");

    cbFilterMissingMetadata = new JCheckBox("");
    cbFilterMissingMetadata.setAction(actionFilter);
    cbFilterMissingMetadata.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingMetadata, "2, 16");

    JLabel lblMissingMetadata = new JLabel(BUNDLE.getString("movieextendedsearch.missingmetadata")); //$NON-NLS-1$
    setComponentFont(lblMissingMetadata);
    add(lblMissingMetadata, "4, 16, right, default");

    cbFilterMissingArtwork = new JCheckBox("");
    cbFilterMissingArtwork.setAction(actionFilter);
    cbFilterMissingArtwork.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingArtwork, "2, 17");

    JLabel lblMissingArtwork = new JLabel(BUNDLE.getString("movieextendedsearch.missingartwork")); //$NON-NLS-1$
    setComponentFont(lblMissingArtwork);
    add(lblMissingArtwork, "4, 17, right, default");

    cbFilterMissingSubtitles = new JCheckBox("");
    cbFilterMissingSubtitles.setAction(actionFilter);
    cbFilterMissingSubtitles.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingSubtitles, "2, 18");

    JLabel lblMissingSubtitles = new JLabel(BUNDLE.getString("movieextendedsearch.missingsubtitles")); //$NON-NLS-1$
    setComponentFont(lblMissingSubtitles);
    add(lblMissingSubtitles, "4, 18, right, default");

    cbFilterNewMovies = new JCheckBox("");
    cbFilterNewMovies.setAction(actionFilter);
    cbFilterNewMovies.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterNewMovies, "2, 19");

    lblNewMovies = new JLabel(BUNDLE.getString("movieextendedsearch.newmovies")); //$NON-NLS-1$
    setComponentFont(lblNewMovies);
    add(lblNewMovies, "4, 19, right, default");

    JSeparator separator = new JSeparator();
    add(separator, "2, 21, 5, 1");

    JLabel lblSortBy = new JLabel(BUNDLE.getString("movieextendedsearch.sortby")); //$NON-NLS-1$
    setComponentFont(lblSortBy);
    add(lblSortBy, "2, 23, 3, 1");

    cbSortColumn = new SmallComboBox(SortColumn.values());
    setComponentFont(cbSortColumn);
    cbSortColumn.setAction(actionSort);
    add(cbSortColumn, "2, 25, 3, 1, fill, default");

    cbSortOrder = new SmallComboBox(SortOrder.values());
    setComponentFont(cbSortOrder);
    cbSortOrder.setAction(actionSort);
    add(cbSortOrder, "6, 25, fill, default");

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
    Settings.getInstance().getMovieSettings().addPropertyChangeListener(propertyChangeListener);
    buildAndInstallTagsArray();
    buildAndInstallCodecArray();
    buildAndInstallDatasourceArray();
    buildAndInstallCertificationArray();
  }

  private void buildAndInstallTagsArray() {
    cbTag.removeAllItems();
    List<String> tags = new ArrayList<String>(movieList.getTagsInMovies());
    Collections.sort(tags);
    for (String tag : tags) {
      cbTag.addItem(tag);
    }
  }

  private void buildAndInstallCodecArray() {
    cbVideoCodec.removeAllItems();
    List<String> codecs = new ArrayList<String>(movieList.getVideoCodecsInMovies());
    Collections.sort(codecs);
    for (String codec : codecs) {
      cbVideoCodec.addItem(codec);
    }

    cbAudioCodec.removeAllItems();
    codecs = new ArrayList<String>(movieList.getAudioCodecsInMovies());
    Collections.sort(codecs);
    for (String codec : codecs) {
      cbAudioCodec.addItem(codec);
    }
  }

  private void buildAndInstallDatasourceArray() {
    cbDatasource.removeAllItems();
    List<String> datasources = new ArrayList<String>(Settings.getInstance().getMovieSettings().getMovieDataSource());
    Collections.sort(datasources);
    for (String datasource : datasources) {
      cbDatasource.addItem(datasource);
    }
  }

  private void buildAndInstallCertificationArray() {
    cbCertification.removeAllItems();
    List<Certification> certifications = new ArrayList<Certification>(movieList.getCertificationsInMovies());
    Collections.sort(certifications);
    for (Certification cert : certifications) {
      cbCertification.addItem(cert);
    }
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
      HashMap<SearchOptions, Object> searchOptions = new HashMap<SearchOptions, Object>();

      // filter duplicates
      if (cbFilterDuplicates.isSelected()) {
        movieList.searchDuplicates();
        searchOptions.put(SearchOptions.DUPLICATES, null);
      }

      // filter for watched flag
      if (cbFilterWatched.isSelected()) {
        if (cbWatched.getSelectedItem() == WatchedFlag.WATCHED) {
          searchOptions.put(SearchOptions.WATCHED, true);
        }
        else {
          searchOptions.put(SearchOptions.WATCHED, false);
        }
      }

      // filter by genre
      if (cbFilterGenre.isSelected()) {
        MediaGenres genre = (MediaGenres) cbGenre.getSelectedItem();
        if (genre != null) {
          searchOptions.put(SearchOptions.GENRE, genre);
        }
      }

      // filter by certification
      if (cbFilterCertification.isSelected()) {
        Certification cert = (Certification) cbCertification.getSelectedItem();
        if (cert != null) {
          searchOptions.put(SearchOptions.CERTIFICATION, cert);
        }
      }

      // filter by cast
      if (cbFilterCast.isSelected()) {
        if (StringUtils.isNotBlank(tfCastMember.getText())) {
          searchOptions.put(SearchOptions.CAST, tfCastMember.getText());
        }
      }

      // filter by tag
      if (cbFilterTag.isSelected()) {
        String tag = (String) cbTag.getSelectedItem();
        if (StringUtils.isNotBlank(tag)) {
          searchOptions.put(SearchOptions.TAG, tag);
        }
      }

      // filter by movie in movieset
      if (cbFilterMovieset.isSelected()) {
        if (cbMovieset.getSelectedItem() == MovieInMovieSet.IN_MOVIESET) {
          searchOptions.put(SearchOptions.MOVIESET, true);
        }
        else {
          searchOptions.put(SearchOptions.MOVIESET, false);
        }
      }

      // filter by video format
      if (cbFilterVideoFormat.isSelected()) {
        String videoFormat = (String) cbVideoFormat.getSelectedItem();
        searchOptions.put(SearchOptions.VIDEO_FORMAT, videoFormat);
      }

      // filter by video codec
      if (cbFilterVideoCodec.isSelected()) {
        String videoCodec = (String) cbVideoCodec.getSelectedItem();
        if (StringUtils.isNotBlank(videoCodec)) {
          searchOptions.put(SearchOptions.VIDEO_CODEC, videoCodec);
        }
      }

      // filter by audio codec
      if (cbFilterAudioCodec.isSelected()) {
        String audioCodec = (String) cbAudioCodec.getSelectedItem();
        if (StringUtils.isNotBlank(audioCodec)) {
          searchOptions.put(SearchOptions.AUDIO_CODEC, audioCodec);
        }
      }

      // filter by datasource
      if (cbFilterDatasource.isSelected()) {
        String datasource = (String) cbDatasource.getSelectedItem();
        if (StringUtils.isNotBlank(datasource)) {
          searchOptions.put(SearchOptions.DATASOURCE, datasource);
        }
      }

      // filer by missing metadata
      if (cbFilterMissingMetadata.isSelected()) {
        searchOptions.put(SearchOptions.MISSING_METADATA, Boolean.TRUE);
      }

      // filer by missing artwork
      if (cbFilterMissingArtwork.isSelected()) {
        searchOptions.put(SearchOptions.MISSING_ARTWORK, Boolean.TRUE);
      }

      // filer by missing artwork
      if (cbFilterMissingSubtitles.isSelected()) {
        searchOptions.put(SearchOptions.MISSING_SUBTITLES, Boolean.TRUE);
      }

      // filter by new movies
      if (cbFilterNewMovies.isSelected()) {
        searchOptions.put(SearchOptions.NEW_MOVIES, Boolean.TRUE);
      }

      // filter by media source
      if (cbFilterMediaSource.isSelected()) {
        searchOptions.put(SearchOptions.MEDIA_SOURCE, cbMediaSource.getSelectedItem());
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