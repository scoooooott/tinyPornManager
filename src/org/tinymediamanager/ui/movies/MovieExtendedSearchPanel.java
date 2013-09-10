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
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
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
public class MovieExtendedSearchPanel extends CollapsiblePanel {
  private static final long           serialVersionUID = -4170930017190753789L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieList                   movieList        = MovieList.getInstance();
  private MovieSelectionModel         movieSelectionModel;

  /**
   * UI Elements
   */
  private JCheckBox                   cbFilterWatched;
  private JLabel                      lblGenre;
  private JComboBox                   cbGenre;
  private JComboBox                   cbSortColumn;
  private JComboBox                   cbSortOrder;
  private JLabel                      lblFilterBy;
  private JLabel                      lblWatchedFlag;
  private JComboBox                   cbWatched;
  private JCheckBox                   cbFilterGenre;
  private JLabel                      lblSortBy;
  private JCheckBox                   cbFilterCast;
  private JLabel                      lblCastMember;
  private JTextField                  tfCastMember;
  private JCheckBox                   cbFilterTag;
  private JLabel                      lblTag;
  private JComboBox                   cbTag;
  private JCheckBox                   cbFilterDuplicates;
  private JLabel                      lblShowDuplicates;
  private JCheckBox                   cbFilterMovieset;
  private JLabel                      lblMoviesInMovieset;
  private JComboBox                   cbMovieset;
  private JCheckBox                   cbFilterVideoFormat;
  private JLabel                      lblVideoFormat;
  private JComboBox                   cbVideoFormat;

  private final Action                actionSort       = new SortAction();
  private final Action                actionFilter     = new FilterAction();

  /**
   * Instantiates a new movie extended search panel.
   * 
   * @param model
   *          the model
   */
  public MovieExtendedSearchPanel(MovieSelectionModel model) {
    super(BUNDLE.getString("movieextendedsearch.options")); //$NON-NLS-1$

    this.movieSelectionModel = model;

    // JPanel panel = new JPanel();
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    panel.add(lblFilterBy, "2, 1, 3, 1");

    cbFilterDuplicates = new JCheckBox("");
    cbFilterDuplicates.setAction(actionFilter);
    panel.add(cbFilterDuplicates, "2, 3");

    lblShowDuplicates = new JLabel(BUNDLE.getString("movieextendedsearch.duplicates")); //$NON-NLS-1$
    panel.add(lblShowDuplicates, "4, 3, right, default");

    cbFilterWatched = new JCheckBox("");
    cbFilterWatched.setAction(actionFilter);
    panel.add(cbFilterWatched, "2, 4");

    lblWatchedFlag = new JLabel(BUNDLE.getString("movieextendedsearch.watched")); //$NON-NLS-1$
    panel.add(lblWatchedFlag, "4, 4, right, default");

    cbWatched = new JComboBox(WatchedFlag.values());
    cbWatched.setAction(actionFilter);
    panel.add(cbWatched, "6, 4, fill, default");

    cbFilterGenre = new JCheckBox("");
    cbFilterGenre.setAction(actionFilter);
    panel.add(cbFilterGenre, "2, 5");

    lblGenre = new JLabel(BUNDLE.getString("movieextendedsearch.genre")); //$NON-NLS-1$
    panel.add(lblGenre, "4, 5, right, default");

    cbGenre = new JComboBox(MediaGenres.values());
    cbGenre.setAction(actionFilter);
    panel.add(cbGenre, "6, 5, fill, default");

    cbFilterCast = new JCheckBox("");
    cbFilterCast.setAction(actionFilter);
    panel.add(cbFilterCast, "2, 6");

    lblCastMember = new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
    panel.add(lblCastMember, "4, 6, right, default");

    tfCastMember = new JTextField();
    panel.add(tfCastMember, "6, 6, fill, default");
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
    cbFilterTag.setAction(actionFilter);
    panel.add(cbFilterTag, "2, 7");

    lblTag = new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
    panel.add(lblTag, "4, 7, right, default");

    cbTag = new JComboBox();
    cbTag.setAction(actionFilter);

    panel.add(cbTag, "6, 7, fill, default");

    cbFilterMovieset = new JCheckBox("");
    cbFilterMovieset.setAction(actionFilter);
    panel.add(cbFilterMovieset, "2, 8");

    lblMoviesInMovieset = new JLabel(BUNDLE.getString("movieextendedsearch.movieset")); //$NON-NLS-1$
    panel.add(lblMoviesInMovieset, "4, 8, right, default");

    cbMovieset = new JComboBox(MovieInMovieSet.values());
    cbMovieset.setAction(actionFilter);
    panel.add(cbMovieset, "6, 8, fill, default");

    cbFilterVideoFormat = new JCheckBox("");
    cbFilterVideoFormat.setAction(actionFilter);
    panel.add(cbFilterVideoFormat, "2, 9");

    lblVideoFormat = new JLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
    panel.add(lblVideoFormat, "4, 9, right, default");

    cbVideoFormat = new JComboBox(getVideoFormats());
    cbVideoFormat.setAction(actionFilter);
    panel.add(cbVideoFormat, "6, 9, fill, default");

    lblSortBy = new JLabel(BUNDLE.getString("movieextendedsearch.sortby")); //$NON-NLS-1$
    panel.add(lblSortBy, "2, 11, 3, 1");

    cbSortColumn = new JComboBox(SortColumn.values());
    cbSortColumn.setAction(actionSort);
    panel.add(cbSortColumn, "4, 13, fill, default");

    cbSortOrder = new JComboBox(SortOrder.values());
    cbSortOrder.setAction(actionSort);
    panel.add(cbSortOrder, "6, 13, fill, default");

    add(panel);
    setCollapsed(true);

    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof MovieList && "tag".equals(evt.getPropertyName())) {
          buildAndInstallTagsArray();
        }
      }
    };
    movieList.addPropertyChangeListener(propertyChangeListener);
    buildAndInstallTagsArray();
  }

  private void buildAndInstallTagsArray() {
    cbTag.removeAllItems();
    List<String> tags = new ArrayList<String>(movieList.getTagsInMovies());
    Collections.sort(tags);
    for (String tag : tags) {
      cbTag.addItem(tag);
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
        searchOptions.put(SearchOptions.CAST, tfCastMember.getText());
      }

      // filter by tag
      if (cbFilterTag.isSelected()) {
        String tag = (String) cbTag.getSelectedItem();
        searchOptions.put(SearchOptions.TAG, tag);
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

      // apply the filter
      movieSelectionModel.filterMovies(searchOptions);
    }
  }

  private String[] getVideoFormats() {
    return new String[] { MediaFile.VIDEO_FORMAT_480P, MediaFile.VIDEO_FORMAT_540P, MediaFile.VIDEO_FORMAT_576P,
        MediaFile.VIDEO_FORMAT_720P, MediaFile.VIDEO_FORMAT_1080P, MediaFile.VIDEO_FORMAT_4K, MediaFile.VIDEO_FORMAT_8K,
        MediaFile.VIDEO_FORMAT_SD, MediaFile.VIDEO_FORMAT_HD };
  }
}