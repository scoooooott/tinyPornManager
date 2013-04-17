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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.CollapsiblePanel;
import org.tinymediamanager.ui.UTF8Control;
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

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = -4170930017190753789L;

  /** The cb search watched. */
  private JCheckBox                   cbFilterWatched;

  /** The action sort. */
  private final Action                actionSort       = new SortAction();

  /** The movie selection model. */
  private MovieSelectionModel         movieSelectionModel;

  /** The movie list. */
  private MovieList                   movieList        = MovieList.getInstance();

  /** The lbl genre. */
  private JLabel                      lblGenre;

  /** The cb genre. */
  private JComboBox                   cbGenre;

  /** The cb sort column. */
  private JComboBox                   cbSortColumn;

  /** The cb sort order. */
  private JComboBox                   cbSortOrder;

  /** The lbl filter by. */
  private JLabel                      lblFilterBy;

  /** The lbl watched flag. */
  private JLabel                      lblWatchedFlag;

  /** The cb watched. */
  private JComboBox                   cbWatched;

  /** The cb filter genre. */
  private JCheckBox                   cbFilterGenre;

  /** The lbl sort by. */
  private JLabel                      lblSortBy;

  /** The cb filter cast. */
  private JCheckBox                   cbFilterCast;

  /** The lbl cast member. */
  private JLabel                      lblCastMember;

  /** The tf cast member. */
  private JTextField                  tfCastMember;

  /** The action filter. */
  private final Action                actionFilter     = new FilterAction();

  /** The cb filter tag. */
  private JCheckBox                   cbFilterTag;

  /** The lbl tag. */
  private JLabel                      lblTag;

  /** The cb tag. */
  private JComboBox                   cbTag;

  /** The cb filter duplicates. */
  private JCheckBox                   cbFilterDuplicates;

  /** The lbl show duplicates. */
  private JLabel                      lblShowDuplicates;

  /** The cb filter movieset. */
  private JCheckBox                   cbFilterMovieset;

  /** The lbl movies in movieset. */
  private JLabel                      lblMoviesInMovieset;

  /** The cb movieset. */
  private JComboBox                   cbMovieset;

  /**
   * Instantiates a new movie extended search panel.
   * 
   * @param model
   *          the model
   */
  public MovieExtendedSearchPanel(MovieSelectionModel model) {
    super(BUNDLE.getString("movieextendedsearch.options")); //$NON-NLS-1$

    Font font = new Font("Dialog", Font.PLAIN, 10);

    this.movieSelectionModel = model;

    // JPanel panel = new JPanel();
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

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

    cbTag = new JComboBox(movieList.getTagsInMovies().toArray());
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

    lblSortBy = new JLabel(BUNDLE.getString("movieextendedsearch.sortby")); //$NON-NLS-1$
    panel.add(lblSortBy, "2, 10, 3, 1");

    cbSortColumn = new JComboBox(SortColumn.values());
    cbSortColumn.setAction(actionSort);
    panel.add(cbSortColumn, "4, 12, fill, default");

    cbSortOrder = new JComboBox(SortOrder.values());
    cbSortOrder.setAction(actionSort);
    panel.add(cbSortOrder, "6, 12, fill, default");

    add(panel);
    setCollapsed(true);
  }

  /**
   * The Class SortAction.
   * 
   * @author Manuel Laggner
   */
  private class SortAction extends AbstractAction {

    /**
     * Instantiates a new sort action.
     */
    public SortAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      SortColumn column = (SortColumn) cbSortColumn.getSelectedItem();
      SortOrder order = (SortOrder) cbSortOrder.getSelectedItem();
      boolean ascending = order == SortOrder.ASCENDING ? true : false;

      // sort
      movieSelectionModel.sortMovies(column, ascending);
    }
  }

  /**
   * The Class FilterAction.
   * 
   * @author Manuel Laggner
   */
  private class FilterAction extends AbstractAction {

    /**
     * Instantiates a new filter action.
     */
    public FilterAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
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

      // apply the filter
      movieSelectionModel.filterMovies(searchOptions);
    }
  }
}