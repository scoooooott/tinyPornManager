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

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.tinymediamanager.core.movie.MovieSet;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetEditor.
 */
public class MovieSetEditor extends JDialog {

  /** The movie set to edit. */
  private MovieSet   movieSetToEdit;
  private JTextField tfName;
  private JTable     tableMovies;

  /**
   * Instantiates a new movie set editor.
   * 
   * @param movieSet
   *          the movie set
   */
  public MovieSetEditor(MovieSet movieSet) {
    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JLabel lblName = new JLabel("Name");
    getContentPane().add(lblName, "1, 1, right, default");

    tfName = new JTextField();
    getContentPane().add(tfName, "3, 1, fill, default");
    tfName.setColumns(10);

    JLabel lblPoster = new JLabel("New label");
    getContentPane().add(lblPoster, "5, 1");

    JLabel lblOverview = new JLabel("Overview");
    getContentPane().add(lblOverview, "1, 3, right, top");

    JScrollPane scrollPaneOverview = new JScrollPane();
    getContentPane().add(scrollPaneOverview, "3, 3, fill, fill");

    JTextPane tpOverview = new JTextPane();
    scrollPaneOverview.setViewportView(tpOverview);

    JLabel lblMovies = new JLabel("Movies");
    getContentPane().add(lblMovies, "1, 5, right, top");

    JScrollPane scrollPaneMovies = new JScrollPane();
    getContentPane().add(scrollPaneMovies, "3, 5, fill, fill");

    tableMovies = new JTable();
    scrollPaneMovies.setViewportView(tableMovies);
    movieSetToEdit = movieSet;
  }
}
