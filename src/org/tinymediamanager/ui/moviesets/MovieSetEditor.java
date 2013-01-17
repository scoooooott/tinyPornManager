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
package org.tinymediamanager.ui.moviesets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TableColumnAdjuster;
import org.tinymediamanager.ui.TmmWindowSaver;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetEditor.
 */
public class MovieSetEditor extends JDialog {

  /** The movie set to edit. */
  private MovieSet     movieSetToEdit;
  private JTextField   tfName;
  private JTable       tableMovies;

  private ImageLabel   lblPoster;

  private JTextPane    tpOverview;

  private List<Movie>  moviesInSet         = ObservableCollections.observableList(new ArrayList<Movie>());
  private List<Movie>  removedMovies       = new ArrayList<Movie>();
  private final Action actionRemoveMovie   = new RemoveMovieAction();
  private final Action actionMoveMovieUp   = new MoveUpAction();
  private final Action actionMoveMovieDown = new MoveDownAction();
  private final Action actionOk            = new OkAction();
  private final Action actionCancel        = new CancelAction();

  /**
   * Instantiates a new movie set editor.
   * 
   * @param movieSet
   *          the movie set
   */
  public MovieSetEditor(MovieSet movieSet) {
    setModal(true);
    setIconImage(Globals.logo);
    setTitle("Edit Movieset");
    setName("movieSetEditor");
    setBounds(5, 5, 700, 400);
    TmmWindowSaver.loadSettings(this);

    movieSetToEdit = movieSet;

    getContentPane().setLayout(new BorderLayout());

    JPanel panelContent = new JPanel();
    panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), }, new RowSpec[] {
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("75px:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), }));
    getContentPane().add(panelContent, BorderLayout.CENTER);

    JLabel lblName = new JLabel("Name");
    panelContent.add(lblName, "1, 1, right, default");

    tfName = new JTextField();
    panelContent.add(tfName, "3, 1, fill, default");
    tfName.setColumns(10);

    lblPoster = new ImageLabel();
    panelContent.add(lblPoster, "5, 1");

    JLabel lblOverview = new JLabel("Overview");
    panelContent.add(lblOverview, "1, 3, right, top");

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelContent.add(scrollPaneOverview, "3, 3, fill, fill");

    tpOverview = new JTextPane();
    scrollPaneOverview.setViewportView(tpOverview);

    JLabel lblMovies = new JLabel("Movies");
    panelContent.add(lblMovies, "1, 5, right, top");

    JScrollPane scrollPaneMovies = new JScrollPane();
    panelContent.add(scrollPaneMovies, "3, 5, 1, 9, fill, fill");

    tableMovies = new JTable();
    scrollPaneMovies.setViewportView(tableMovies);

    /**
     * Button pane
     */
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), ColumnSpec.decode("100px"),
          FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JButton btnOk = new JButton("OK");
        btnOk.setAction(actionOk);
        buttonPane.add(btnOk, "2, 2, fill, top");
        getRootPane().setDefaultButton(btnOk);
      }
      {
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setAction(actionCancel);
        buttonPane.add(btnCancel, "4, 2, fill, top");
      }
    }

    {
      tfName.setText(movieSetToEdit.getName());
      tpOverview.setText(movieSetToEdit.getOverview());
      lblPoster.setImageUrl(movieSetToEdit.getPosterUrl());

      JButton btnRemoveMovie = new JButton("");
      btnRemoveMovie.setAction(actionRemoveMovie);
      panelContent.add(btnRemoveMovie, "1, 7, right, top");

      JButton btnMoveMovieUp = new JButton("");
      btnMoveMovieUp.setAction(actionMoveMovieUp);
      panelContent.add(btnMoveMovieUp, "1, 9");

      JButton btnMoveMovieDown = new JButton("");
      btnMoveMovieDown.setAction(actionMoveMovieDown);
      panelContent.add(btnMoveMovieDown, "1, 11");
      moviesInSet.addAll(movieSetToEdit.getMovies());
    }

    initDataBindings();

    // adjust table columns
    TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(tableMovies);
    tableColumnAdjuster.setColumnDataIncluded(true);
    tableColumnAdjuster.setColumnHeaderIncluded(true);
    tableColumnAdjuster.adjustColumns();
  }

  protected void initDataBindings() {
    JTableBinding<Movie, List<Movie>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, moviesInSet, tableMovies);
    //
    BeanProperty<Movie, String> movieBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieBeanProperty).setColumnName("Name").setEditable(false);
    //
    BeanProperty<Movie, String> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setColumnName("Year");
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
  }

  private class RemoveMovieAction extends AbstractAction {
    public RemoveMovieAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Remove.png")));
      putValue(SHORT_DESCRIPTION, "Remove marked movie from movieset");
    }

    public void actionPerformed(ActionEvent e) {
      int row = tableMovies.getSelectedRow();
      Movie movie = moviesInSet.get(row);
      moviesInSet.remove(row);
      removedMovies.add(movie);
    }
  }

  private class MoveUpAction extends AbstractAction {
    public MoveUpAction() {
      putValue(NAME, "up");
      putValue(SHORT_DESCRIPTION, "Move marked movie up");
    }

    public void actionPerformed(ActionEvent e) {
      int row = tableMovies.getSelectedRow();
      if (row > 0) {
        Collections.rotate(moviesInSet.subList(row - 1, row + 1), 1);
        tableMovies.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveDownAction extends AbstractAction {
    public MoveDownAction() {
      putValue(NAME, "down");
      putValue(SHORT_DESCRIPTION, "Move marked movie down");
    }

    public void actionPerformed(ActionEvent e) {
      int row = tableMovies.getSelectedRow();
      if (row < moviesInSet.size() - 1) {
        Collections.rotate(moviesInSet.subList(row, row + 2), -1);
        tableMovies.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      putValue(NAME, "Save");
      putValue(SHORT_DESCRIPTION, "Save changes");
    }

    public void actionPerformed(ActionEvent e) {
      movieSetToEdit.setName(tfName.getText());
      movieSetToEdit.setOverview(tpOverview.getText());

      // sort movies in the right order (and rewrite their nfo)
      movieSetToEdit.removeAllMovies();
      for (Movie movie : moviesInSet) {
        movieSetToEdit.addMovie(movie);
        movie.writeNFO();
      }

      // remove removed movies
      for (Movie movie : removedMovies) {
        movie.removeFromMovieSet();
        movieSetToEdit.removeMovie(movie);
      }

      movieSetToEdit.saveToDb();

      setVisible(false);
      dispose();
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      putValue(NAME, "Cancel");
      putValue(SHORT_DESCRIPTION, "Discard changes");
    }

    public void actionPerformed(ActionEvent e) {
      setVisible(false);
      dispose();
    }
  }
}
