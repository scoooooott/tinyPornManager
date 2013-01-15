/*
 * Copyright 2013 Manuel Laggner
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
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.ui.ImageLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieSetInformationPanel extends JPanel {

  private MovieSetSelectionModel selectionModel;
  private JLabel                 lblMovieSetName;
  private JTable                 tableAssignedMovies;
  private ImageLabel             lblMovieSetPoster;
  private JPanel                 panel;

  /**
   * Instantiates a new movie set information panel.
   * 
   * @param selectionModel
   *          the selection model
   */
  public MovieSetInformationPanel(MovieSetSelectionModel selectionModel) {
    this.selectionModel = selectionModel;
    setLayout(new BorderLayout(0, 0));

    lblMovieSetName = new JLabel("");
    lblMovieSetName.setFont(new Font("Dialog", Font.BOLD, 16));
    add(lblMovieSetName, BorderLayout.NORTH);

    panel = new JPanel();
    add(panel, BorderLayout.CENTER);
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("180px"), ColumnSpec.decode("1px"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("270px"), }));

    lblMovieSetPoster = new ImageLabel();
    panel.add(lblMovieSetPoster, "2, 2, fill, fill");

    JScrollPane scrollPaneMovies = new JScrollPane();
    add(scrollPaneMovies, BorderLayout.SOUTH);

    tableAssignedMovies = new JTable();
    tableAssignedMovies.setPreferredScrollableViewportSize(new Dimension(450, 200));
    scrollPaneMovies.setViewportView(tableAssignedMovies);
    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty = BeanProperty.create("selectedMovieSet.name");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSetSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty, lblMovieSetName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSetSelectionModel, List<Movie>> movieSetSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovieSet.movies");
    JTableBinding<Movie, MovieSetSelectionModel, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_1, tableAssignedMovies);
    //
    BeanProperty<Movie, String> movieBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieBeanProperty).setColumnName("Name").setEditable(false);
    //
    BeanProperty<Movie, String> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setColumnName("Year").setEditable(false);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovieSet.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<MovieSetSelectionModel, String, ImageLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_2, lblMovieSetPoster, imageLabelBeanProperty);
    autoBinding_1.bind();
  }
}
