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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;

/**
 * The Class MovieTableModel.
 * 
 * @author Manuel Laggner
 */
public class MovieTableModel extends AbstractTableModel {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = -1850397154387184169L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  /** The Constant checkIcon. */
  private final static ImageIcon      checkIcon        = new ImageIcon(MainWindow.class.getResource("images/Checkmark.png"));

  /** The Constant crossIcon. */
  private final static ImageIcon      crossIcon        = new ImageIcon(MainWindow.class.getResource("images/Cross.png"));

  /** The movie list. */
  private MovieList                   movieList        = MovieList.getInstance();

  /** The movies. */
  private final List<Movie>           movies;

  /** The filtered movies. */
  private final List<Movie>           filteredMovies;

  /** The comparator. */
  private Comparator<Movie>           comparator;

  /**
   * Instantiates a new movie table model.
   */
  public MovieTableModel() {
    movies = movieList.getMovies();
    filteredMovies = new ArrayList<Movie>(movies);
    comparator = new MovieExtendedComparator(SortColumn.TITLE, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return 6;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getRowCount()
   */
  @Override
  public int getRowCount() {
    return filteredMovies.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return BUNDLE.getString("metatag.title"); //$NON-NLS-1$

      case 1:
        return BUNDLE.getString("metatag.year"); //$NON-NLS-1$

      case 2:
        return BUNDLE.getString("metatag.nfo"); //$NON-NLS-1$

      case 3:
        return BUNDLE.getString("metatag.images"); //$NON-NLS-1$

      case 4:
        return BUNDLE.getString("metatag.trailer"); //$NON-NLS-1$

      case 5:
        return BUNDLE.getString("metatag.subtitles"); //$NON-NLS-1$
    }

    throw new IllegalStateException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  @Override
  public Object getValueAt(int rowIndex, int column) {
    Movie movie = filteredMovies.get(rowIndex);
    switch (column) {
      case 0:
        return movie.getTitleSortable();

      case 1:
        return movie.getYear();

      case 2:
        if (movie.getHasNfoFile()) {
          return checkIcon;
        }
        return crossIcon;

      case 3:
        if (movie.getHasImages()) {
          return checkIcon;
        }
        return crossIcon;

      case 4:
        if (movie.getHasTrailer()) {
          return checkIcon;
        }
        return crossIcon;

      case 5:
        if (movie.hasSubtitles()) {
          return checkIcon;
        }
        return crossIcon;
    }

    throw new IllegalStateException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Class getColumnClass(int column) {
    switch (column) {
      case 0:
      case 1:
        return String.class;

      case 2:
      case 3:
      case 4:
      case 5:
        return ImageIcon.class;
    }

    throw new IllegalStateException();
  }

  /**
   * Sort movies.
   * 
   * @param column
   *          the column
   * @param ascending
   *          the ascending
   */
  public void sortMovies(MovieExtendedComparator.SortColumn column, boolean ascending) {
    comparator = new MovieExtendedComparator(column, ascending);
    sort();
    fireTableChanged(new TableModelEvent(this));
  }

  /**
   * Sort.
   */
  private void sort() {
    Collections.sort(filteredMovies, comparator);
  }

  /**
   * Gets the filtered movies.
   * 
   * @return the filtered movies
   */
  public List<Movie> getFilteredMovies() {
    return this.filteredMovies;
  }

  /**
   * Filter movies.
   * 
   * @param filter
   *          the filter
   */
  public void filterMovies(HashMap<MovieMatcher.SearchOptions, Object> filter) {
    MovieMatcher matcher = new MovieMatcher(filter);
    filteredMovies.clear();
    for (int i = 0; i < movies.size(); i++) {
      if (matcher.matches(movies.get(i))) {
        filteredMovies.add(movies.get(i));
      }
    }
    sort();
    fireTableChanged(new TableModelEvent(this));
  }

}
