/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.ITmmTabItem;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TablePopupListener;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.EnhancedTextField;
import org.tinymediamanager.ui.components.TmmListPanel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.movies.MovieComparator;
import org.tinymediamanager.ui.movies.MovieFilterator;
import org.tinymediamanager.ui.movies.MovieMatcherEditor;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.movies.MovieTableFormat;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.movies.actions.MovieEditAction;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import net.miginfocom.swing.MigLayout;

/**
 * @author Manuel Laggner
 */
public class MovieListPanel extends TmmListPanel implements ITmmTabItem {
  private static final long           serialVersionUID = -1681460428331929420L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  MovieSelectionModel                 selectionModel;

  private MovieList                   movieList;

  private JTextField                  searchField;
  private TmmTable                    movieTable;
  private JLabel                      lblMovieCountFiltered;
  private JLabel                      lblMovieCountTotal;

  public MovieListPanel() {
    initComponents();
  }

  private void initComponents() {
    // putClientProperty("class", "roundedPanel");
    setOpaque(false);

    movieList = MovieList.getInstance();
    SortedList<Movie> sortedMovies = new SortedList<Movie>(GlazedListsSwing.swingThreadProxyList((ObservableElementList) movieList.getMovies()),
        new MovieComparator());
    sortedMovies.setMode(SortedList.AVOID_MOVING_ELEMENTS);

    setLayout(new MigLayout("insets n n 0 n", "[300lp:300lp,grow][fill]", "[][200lp:300lp,grow]0[][]"));

    searchField = new EnhancedTextField(BUNDLE.getString("tmm.searchfield"), IconManager.SEARCH_GREY); //$NON-NLS-1$
    add(searchField, "cell 0 0,growx");

    MatcherEditor<Movie> textMatcherEditor = new TextComponentMatcherEditor<>(searchField, new MovieFilterator());
    MovieMatcherEditor movieMatcherEditor = new MovieMatcherEditor();
    FilterList<Movie> extendedFilteredMovies = new FilterList<>(sortedMovies, movieMatcherEditor);
    FilterList<Movie> textFilteredMovies = new FilterList<>(extendedFilteredMovies, textMatcherEditor);
    selectionModel = new MovieSelectionModel(sortedMovies, textFilteredMovies, movieMatcherEditor);
    final DefaultEventTableModel<Movie> movieTableModel = new TmmTableModel<Movie>(textFilteredMovies, new MovieTableFormat());

    // build the table
    movieTable = new TmmTable(movieTableModel);

    // install and save the comparator on the Table
    selectionModel.setTableComparatorChooser(TableComparatorChooser.install(movieTable, sortedMovies, TableComparatorChooser.SINGLE_COLUMN));

    // restore hidden columns
    movieTable.readHiddenColumns(MovieModuleManager.SETTINGS.getMovieTableHiddenColumns());
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
          MovieModuleManager.SETTINGS.setMovieTableHiddenColumns(cols);
          MovieModuleManager.SETTINGS.saveSettings();
        });
      }
    });
    movieTable.adjustColumnPreferredWidths(3);

    movieTableModel.addTableModelListener(arg0 -> {
      lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
      // select first movie if nothing is selected
      ListSelectionModel selectionModel1 = movieTable.getSelectionModel();
      if (selectionModel1.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
        selectionModel1.setSelectionInterval(0, 0);
      }
    });

    JScrollPane scrollPane = new JScrollPane(movieTable);
    movieTable.configureScrollPane(scrollPane, new int[] { 0 });
    add(scrollPane, "cell 0 1 2 1,grow");

    final JToggleButton btnExtendedFilter = new JToggleButton("Filter");
    btnExtendedFilter.setToolTipText(BUNDLE.getString("movieextendedsearch.options")); //$NON-NLS-1$
    btnExtendedFilter.addActionListener(e -> MovieUIModule.getInstance().setFilterMenuVisible(btnExtendedFilter.isSelected()));
    add(btnExtendedFilter, "cell 1 0");

    JSeparator separator = new JSeparator();
    add(separator, "cell 0 2 2 1, growx");

    JLabel lblMovieCount = new JLabel(BUNDLE.getString("tmm.movies") + ":"); //$NON-NLS-1$
    add(lblMovieCount, "flowx,cell 0 3 2 1");

    lblMovieCountFiltered = new JLabel("");
    add(lblMovieCountFiltered, "cell 0 3 2 1");

    JLabel lblMovieCountOf = new JLabel(BUNDLE.getString("tmm.of")); //$NON-NLS-1$
    add(lblMovieCountOf, "cell 0 3 2 1");

    lblMovieCountTotal = new JLabel("");
    add(lblMovieCountTotal, "cell 0 3 2 1");

    initDataBindings();

    // initialize filteredCount
    lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
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
        int selectionIndex = movieTable.convertRowIndexToModel(0);
        selectionModel.setSelectionInterval(selectionIndex, selectionIndex);
      }
    }

    SwingUtilities.invokeLater(() -> movieTable.requestFocus());
  }

  @Override
  public ITmmUIModule getUIModule() {
    return MovieUIModule.getInstance();
  }

  @Override
  public void setPopupMenu(JPopupMenu popupMenu) {
    movieTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
          Action editAction = new MovieEditAction();
          editAction.actionPerformed(null);
        }
      }
    });
    movieTable.addMouseListener(new TablePopupListener(popupMenu, movieTable));
  }

  protected void initDataBindings() {
    BeanProperty<MovieList, Integer> movieListBeanProperty = BeanProperty.create("movieCount");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieList, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieList, movieListBeanProperty,
        lblMovieCountTotal, jLabelBeanProperty);
    autoBinding.bind();
  }
}
