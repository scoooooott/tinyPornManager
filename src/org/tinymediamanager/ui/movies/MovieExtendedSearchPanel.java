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
package org.tinymediamanager.ui.movies;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.CollapsiblePanel;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortOrder;
import org.tinymediamanager.ui.movies.MoviesExtendedMatcher.SearchOptions;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieExtendedSearchPanel.
 */
@SuppressWarnings("serial")
public class MovieExtendedSearchPanel extends CollapsiblePanel {

  /** The btn extended search. */
  private JButton             btnExtendedSearch;

  /** The cb search watched. */
  private JCheckBox           cbSearchWatched;

  /** The cb search not watched. */
  private JCheckBox           cbSearchNotWatched;

  /** The action search. */
  private final Action        actionSearch = new SearchAction();

  private final Action        actionSort   = new SortAction();

  /** The movie selection model. */
  private MovieSelectionModel movieSelectionModel;
  private JLabel              lblGenreT;
  private JComboBox           cbGenre;
  private JComboBox           cbSortColumn;
  private JComboBox           cbSortOrder;
  private JButton             btnSort;

  /**
   * Instantiates a new movie extended search panel.
   * 
   * @param model
   *          the model
   */
  public MovieExtendedSearchPanel(MovieSelectionModel model) {
    super("Extended filter and sort options");
    this.movieSelectionModel = model;

    JPanel panel = new JPanel();
    add(panel);
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    cbSearchWatched = new JCheckBox("Watched");
    cbSearchWatched.setPreferredSize(new Dimension(88, 16));
    panel.add(cbSearchWatched, "2, 1");

    cbSearchNotWatched = new JCheckBox("Not watched");
    cbSearchNotWatched.setPreferredSize(new Dimension(115, 16));
    panel.add(cbSearchNotWatched, "4, 1");

    lblGenreT = new JLabel("has Genre");
    panel.add(lblGenreT, "2, 3, right, default");

    cbGenre = new JComboBox(MediaGenres.values());
    cbGenre.setPreferredSize(new Dimension(133, 18));
    panel.add(cbGenre, "4, 3, fill, default");

    btnExtendedSearch = new JButton();
    btnExtendedSearch.setMinimumSize(new Dimension(34, 8));
    btnExtendedSearch.setMargin(new Insets(0, 14, 0, 14));
    btnExtendedSearch.setAction(actionSearch);
    panel.add(btnExtendedSearch, "4, 5, right, default");

    cbSortColumn = new JComboBox(SortColumn.values());
    cbSortColumn.setPreferredSize(new Dimension(32, 18));
    panel.add(cbSortColumn, "2, 7, fill, default");

    cbSortOrder = new JComboBox(SortOrder.values());
    cbSortOrder.setPreferredSize(new Dimension(32, 18));
    panel.add(cbSortOrder, "4, 7, fill, default");

    btnSort = new JButton("Sort");
    btnSort.setAction(actionSort);
    btnSort.setMargin(new Insets(0, 14, 0, 14));
    panel.add(btnSort, "4, 9, right, default");

    toggleVisibility(false);
  }

  /**
   * The Class SearchAction.
   */
  private class SearchAction extends AbstractAction {

    /**
     * Instantiates a new search action.
     */
    public SearchAction() {
      putValue(NAME, "Filter");
      putValue(SHORT_DESCRIPTION, "Search using the given options");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      HashMap<SearchOptions, Object> searchOptions = new HashMap<SearchOptions, Object>();
      // watched Flag
      if (cbSearchNotWatched.isSelected() ^ cbSearchWatched.isSelected()) {
        if (cbSearchNotWatched.isSelected()) {
          searchOptions.put(SearchOptions.WATCHED, false);
        }
        else {
          searchOptions.put(SearchOptions.WATCHED, true);
        }
      }

      // genre
      MediaGenres genre = (MediaGenres) cbGenre.getSelectedItem();
      if (genre != null && genre != MediaGenres.EMPTY) {
        searchOptions.put(SearchOptions.GENRE, genre);
      }

      // apply the filter
      movieSelectionModel.filterMovies(searchOptions);
    }
  }

  private class SortAction extends AbstractAction {
    public SortAction() {
      putValue(NAME, "Sort");
      putValue(SHORT_DESCRIPTION, "Sort movielist");
    }

    public void actionPerformed(ActionEvent e) {
      SortColumn column = (SortColumn) cbSortColumn.getSelectedItem();
      SortOrder order = (SortOrder) cbSortOrder.getSelectedItem();
      boolean ascending = order == SortOrder.ASCENDING ? true : false;

      // sort
      movieSelectionModel.sortMovies(column, ascending);
    }
  }
}
