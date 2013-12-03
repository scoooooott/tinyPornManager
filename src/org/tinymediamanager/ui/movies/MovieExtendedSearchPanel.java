/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.awt.event.ActionEvent;
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
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.SmallCheckBoxUI;
import org.tinymediamanager.ui.SmallTextFieldBorder;
import org.tinymediamanager.ui.UTF8Control;
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

/**
 * The Class MovieExtendedSearchPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieExtendedSearchPanel extends JPanel {
  private static final long            serialVersionUID = -4170930017190753789L;
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final float           FONT_SIZE        = 11f;
  private static final SmallCheckBoxUI CHECKBOX_UI      = new SmallCheckBoxUI();

  private MovieList                    movieList        = MovieList.getInstance();
  private MovieSelectionModel          movieSelectionModel;

  /**
   * UI Elements
   */
  private JCheckBox                    cbFilterWatched;
  private JLabel                       lblGenre;
  private JComboBox                    cbGenre;
  private JComboBox                    cbSortColumn;
  private JComboBox                    cbSortOrder;
  private JLabel                       lblWatchedFlag;
  private JComboBox                    cbWatched;
  private JCheckBox                    cbFilterGenre;
  private JLabel                       lblSortBy;
  private JCheckBox                    cbFilterCast;
  private JLabel                       lblCastMember;
  private JTextField                   tfCastMember;
  private JCheckBox                    cbFilterTag;
  private JLabel                       lblTag;
  private JComboBox                    cbTag;
  private JCheckBox                    cbFilterDuplicates;
  private JLabel                       lblShowDuplicates;
  private JCheckBox                    cbFilterMovieset;
  private JLabel                       lblMoviesInMovieset;
  private JComboBox                    cbMovieset;
  private JCheckBox                    cbFilterVideoFormat;
  private JLabel                       lblVideoFormat;
  private JComboBox                    cbVideoFormat;
  private JCheckBox                    cbFilterVideoCodec;
  private JLabel                       lblVideoCodec;
  private JComboBox                    cbVideoCodec;
  private JCheckBox                    cbFilterAudioCodec;
  private JLabel                       lblAudioCodec;
  private JComboBox                    cbAudioCodec;

  private final Action                 actionSort       = new SortAction();
  private final Action                 actionFilter     = new FilterAction();
  private JCheckBox                    cbFilterDatasource;
  private JLabel                       lblDatasource;
  private JComboBox                    cbDatasource;

  /**
   * Instantiates a new movie extended search
   * 
   * @param model
   *          the model
   */
  public MovieExtendedSearchPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    setBorder(new TitledBorder(BUNDLE.getString("movieextendedsearch.filterby"))); //$NON-NLS-1$

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    cbFilterDuplicates = new JCheckBox("");
    cbFilterDuplicates.setUI(CHECKBOX_UI); // $hide$
    cbFilterDuplicates.setAction(actionFilter);
    add(cbFilterDuplicates, "2, 3");

    lblShowDuplicates = new JLabel(BUNDLE.getString("movieextendedsearch.duplicates")); //$NON-NLS-1$
    setComponentFont(lblShowDuplicates);
    add(lblShowDuplicates, "4, 3, right, default");

    cbFilterWatched = new JCheckBox("");
    cbFilterWatched.setUI(CHECKBOX_UI); // $hide$
    cbFilterWatched.setAction(actionFilter);
    add(cbFilterWatched, "2, 4");

    lblWatchedFlag = new JLabel(BUNDLE.getString("movieextendedsearch.watched")); //$NON-NLS-1$
    setComponentFont(lblWatchedFlag);
    add(lblWatchedFlag, "4, 4, right, default");

    cbWatched = new SmallComboBox(WatchedFlag.values());
    setComponentFont(cbWatched);
    cbWatched.setAction(actionFilter);
    add(cbWatched, "6, 4, fill, default");

    cbFilterGenre = new JCheckBox("");
    cbFilterGenre.setUI(CHECKBOX_UI); // $hide$
    cbFilterGenre.setAction(actionFilter);
    add(cbFilterGenre, "2, 5");

    lblGenre = new JLabel(BUNDLE.getString("movieextendedsearch.genre")); //$NON-NLS-1$
    setComponentFont(lblGenre);
    add(lblGenre, "4, 5, right, default");

    cbGenre = new SmallComboBox(MediaGenres.values());
    setComponentFont(cbGenre);
    cbGenre.setAction(actionFilter);
    add(cbGenre, "6, 5, fill, default");

    cbFilterCast = new JCheckBox("");
    cbFilterCast.setUI(CHECKBOX_UI); // $hide$
    cbFilterCast.setAction(actionFilter);
    add(cbFilterCast, "2, 6");

    lblCastMember = new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
    setComponentFont(lblCastMember);
    add(lblCastMember, "4, 6, right, default");

    tfCastMember = new JTextField();
    setComponentFont(tfCastMember);
    tfCastMember.setBorder(new SmallTextFieldBorder());
    add(tfCastMember, "6, 6, fill, default");
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
    add(cbFilterTag, "2, 7");

    lblTag = new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
    setComponentFont(lblTag);
    add(lblTag, "4, 7, right, default");

    cbTag = new SmallComboBox();
    setComponentFont(cbTag);
    cbTag.setAction(actionFilter);

    add(cbTag, "6, 7, fill, default");

    cbFilterMovieset = new JCheckBox("");
    cbFilterMovieset.setUI(CHECKBOX_UI); // $hide$
    cbFilterMovieset.setAction(actionFilter);
    add(cbFilterMovieset, "2, 8");

    lblMoviesInMovieset = new JLabel(BUNDLE.getString("movieextendedsearch.movieset")); //$NON-NLS-1$
    setComponentFont(lblMoviesInMovieset);
    add(lblMoviesInMovieset, "4, 8, right, default");

    cbMovieset = new SmallComboBox(MovieInMovieSet.values());
    setComponentFont(cbMovieset);
    cbMovieset.setAction(actionFilter);
    add(cbMovieset, "6, 8, fill, default");

    cbFilterVideoFormat = new JCheckBox("");
    cbFilterVideoFormat.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideoFormat.setAction(actionFilter);
    add(cbFilterVideoFormat, "2, 9");

    lblVideoFormat = new JLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
    setComponentFont(lblVideoFormat);
    add(lblVideoFormat, "4, 9, right, default");

    cbVideoFormat = new SmallComboBox(getVideoFormats());
    setComponentFont(cbVideoFormat);
    cbVideoFormat.setAction(actionFilter);
    add(cbVideoFormat, "6, 9, fill, default");

    cbFilterVideoCodec = new JCheckBox("");
    cbFilterVideoCodec.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideoCodec.setAction(actionFilter);
    add(cbFilterVideoCodec, "2, 10");

    lblVideoCodec = new JLabel(BUNDLE.getString("metatag.videocodec")); //$NON-NLS-1$
    setComponentFont(lblVideoCodec);
    add(lblVideoCodec, "4, 10, right, default");

    cbVideoCodec = new SmallComboBox();
    setComponentFont(cbVideoCodec);
    cbVideoCodec.setAction(actionFilter);
    add(cbVideoCodec, "6, 10, fill, default");

    cbFilterAudioCodec = new JCheckBox("");
    cbFilterAudioCodec.setUI(CHECKBOX_UI); // $hide$
    cbFilterAudioCodec.setAction(actionFilter);
    add(cbFilterAudioCodec, "2, 11");

    lblAudioCodec = new JLabel(BUNDLE.getString("metatag.audiocodec")); //$NON-NLS-1$
    setComponentFont(lblAudioCodec);
    add(lblAudioCodec, "4, 11, right, default");

    cbAudioCodec = new SmallComboBox();
    setComponentFont(cbAudioCodec);
    cbAudioCodec.setAction(actionFilter);
    add(cbAudioCodec, "6, 11, fill, default");

    cbFilterDatasource = new JCheckBox("");
    cbFilterDatasource.setUI(CHECKBOX_UI); // $hide$
    cbFilterDatasource.setAction(actionFilter);
    add(cbFilterDatasource, "2, 12");

    lblDatasource = new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
    setComponentFont(lblDatasource);
    add(lblDatasource, "4, 12, right, default");

    cbDatasource = new SmallComboBox();
    setComponentFont(cbDatasource);
    cbDatasource.setAction(actionFilter);
    add(cbDatasource, "6, 12, fill, default");

    JSeparator separator = new JSeparator();
    add(separator, "2, 14, 5, 1");

    lblSortBy = new JLabel(BUNDLE.getString("movieextendedsearch.sortby")); //$NON-NLS-1$
    setComponentFont(lblSortBy);
    add(lblSortBy, "2, 16, 3, 1");

    cbSortColumn = new SmallComboBox(SortColumn.values());
    setComponentFont(cbSortColumn);
    cbSortColumn.setAction(actionSort);
    add(cbSortColumn, "2, 18, 3, 1, fill, default");

    cbSortOrder = new SmallComboBox(SortOrder.values());
    setComponentFont(cbSortOrder);
    cbSortOrder.setAction(actionSort);
    add(cbSortOrder, "6, 18, fill, default");

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
      }
    };
    movieList.addPropertyChangeListener(propertyChangeListener);
    Settings.getInstance().getMovieSettings().addPropertyChangeListener(propertyChangeListener);
    buildAndInstallTagsArray();
    buildAndInstallCodecArray();
    buildAndInstallDatasourceArray();
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

  private class SortAction extends AbstractAction {
    private static final long serialVersionUID = -4057379119252539003L;

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
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