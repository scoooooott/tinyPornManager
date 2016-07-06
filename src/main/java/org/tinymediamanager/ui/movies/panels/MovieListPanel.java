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
package org.tinymediamanager.ui.movies.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.ITmmTabItem;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.EnhancedTextField;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.movies.MovieComparator;
import org.tinymediamanager.ui.movies.MovieFilterator;
import org.tinymediamanager.ui.movies.MovieMatcherEditor;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.movies.MovieTableFormat;
import org.tinymediamanager.ui.movies.MovieTableFormat2;
import org.tinymediamanager.ui.movies.MovieTableMouseListener;
import org.tinymediamanager.ui.movies.MovieUIModule;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

/**
 * @author Manuel Laggner
 *
 */
public class MovieListPanel extends JPanel implements ITmmTabItem {
  private static final long           serialVersionUID = -1681460428331929420L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  MovieSelectionModel                 selectionModel;

  private JTextField                  searchField;
  private TmmTable                    movieTable;
  private JLabel                      lblMovieCountFiltered;

  public MovieListPanel() {
    // putClientProperty("class", "roundedPanel");
    setOpaque(false);
    setLayout(new FormLayout(
        new ColumnSpec[] { ColumnSpec.decode("10dlu"), ColumnSpec.decode("130dlu:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(150dlu;default):grow"),
            FormFactory.DEFAULT_ROWSPEC, }));

    buildTable();
    buildStatusPanel();
  }

  private void buildTable() {
    // build the list (wrap it with all necessary glazedlists types), build the tablemodel and the selectionmodel
    MovieList movieList = MovieList.getInstance();
    SortedList<Movie> sortedMovies = new SortedList<Movie>(GlazedListsSwing.swingThreadProxyList(movieList.getMovies()), new MovieComparator());
    sortedMovies.setMode(SortedList.AVOID_MOVING_ELEMENTS);

    searchField = EnhancedTextField.createSearchTextField();
    add(searchField, "2, 1, fill, fill");

    MatcherEditor<Movie> textMatcherEditor = new TextComponentMatcherEditor<>(searchField, new MovieFilterator());
    MovieMatcherEditor movieMatcherEditor = new MovieMatcherEditor();
    FilterList<Movie> extendedFilteredMovies = new FilterList<>(sortedMovies, movieMatcherEditor);
    FilterList<Movie> textFilteredMovies = new FilterList<>(extendedFilteredMovies, textMatcherEditor);
    selectionModel = new MovieSelectionModel(sortedMovies, textFilteredMovies, movieMatcherEditor);
    final DefaultEventTableModel<Movie> movieTableModel = new TmmTableModel<>(textFilteredMovies, new MovieTableFormat2());

    // build the table
    movieTable = new TmmTable(movieTableModel);

    movieTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
        // select first movie if nothing is selected
        ListSelectionModel selectionModel = movieTable.getSelectionModel();
        if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
          selectionModel.setSelectionInterval(0, 0);
        }
      }
    });

    // install and save the comparator on the Table
    selectionModel.setTableComparatorChooser(TableComparatorChooser.install(movieTable, sortedMovies, TableComparatorChooser.SINGLE_COLUMN));

    // configure columns
    MovieTableFormat.configureColumns(movieTable);

    // restore hidden columns
    movieTable.readHiddenColumns(MovieModuleManager.MOVIE_SETTINGS.getMovieTableHiddenColumns());
    movieTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
      @Override
      public void columnAdded(TableColumnModelEvent e) {
        writeSettings();
      }

      @Override
      public void columnRemoved(TableColumnModelEvent e) {
        writeSettings();
      }

      @Override
      public void columnMoved(TableColumnModelEvent e) {
      }

      @Override
      public void columnMarginChanged(ChangeEvent e) {

      }

      @Override
      public void columnSelectionChanged(ListSelectionEvent e) {
      }

      private void writeSettings() {
        movieTable.writeHiddenColumns(cols -> {
          MovieModuleManager.MOVIE_SETTINGS.setMovieTableHiddenColumns(cols);
          Globals.settings.saveSettings();
        });
      }
    });

    JScrollPane scrollPane = TmmTable.createJScrollPane(movieTable, new int[] { 0 });
    add(scrollPane, "1, 3, 5, 1, fill, fill");
  }

  private void buildStatusPanel() {
    final JToggleButton btnExtendedFilter = new JToggleButton("Filter");
    btnExtendedFilter.setToolTipText(BUNDLE.getString("movieextendedsearch.options")); //$NON-NLS-1$
    btnExtendedFilter.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        MovieUIModule.getInstance().setFilterMenuVisible(btnExtendedFilter.isSelected());
      }
    });
    add(btnExtendedFilter, "4, 1, fill, fill");
    JPanel panelStatus = new JPanel();
    add(panelStatus, "2, 4");
    panelStatus
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("1px"), ColumnSpec.decode("146px:grow"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    JPanel panelMovieCount = new JPanel();
    panelStatus.add(panelMovieCount, "3, 1, left, fill");

    JLabel lblMovieCount = new JLabel("Movies:");
    panelMovieCount.add(lblMovieCount);

    lblMovieCountFiltered = new JLabel("");
    panelMovieCount.add(lblMovieCountFiltered);

    JLabel lblMovieCountOf = new JLabel("of");
    panelMovieCount.add(lblMovieCountOf);

    JLabel lblMovieCountTotal = new JLabel("");
    panelMovieCount.add(lblMovieCountTotal);
  }

  public MovieSelectionModel getSelectionModel() {
    return selectionModel;
  }

  public void setInitialSelection() {
    movieTable.setSelectionModel(selectionModel.getSelectionModel());
    // selecting first movie at startup
    if (MovieList.getInstance().getMovies() != null && MovieList.getInstance().getMovies().size() > 0) {
      ListSelectionModel selectionModel = movieTable.getSelectionModel();
      if (selectionModel.isSelectionEmpty()) {
        selectionModel.setSelectionInterval(0, 0);
      }
    }
  }

  @Override
  public ITmmUIModule getUIModule() {
    return MovieUIModule.getInstance();
  }

  public void setPopupMenu(JPopupMenu popupMenu) {
    MouseListener mouseListener = new MovieTableMouseListener(popupMenu, movieTable);
    movieTable.addMouseListener(mouseListener);
  }
}
