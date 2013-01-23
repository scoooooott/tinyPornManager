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
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;

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
  private JLayeredPane           layeredPane;
  private ImageLabel             lblMovieSetFanart;
  private JSplitPane             panelSouth;
  private JScrollPane            scrollPaneOverview;
  private JTextPane              tpOverview;
  private JPanel                 panelOverview;
  private JLabel lblOverview;

  /**
   * Instantiates a new movie set information panel.
   * 
   * @param selectionModel
   *          the selection model
   */
  public MovieSetInformationPanel(MovieSetSelectionModel selectionModel) {
    this.selectionModel = selectionModel;
    setLayout(new BorderLayout(0, 0));

    panel = new JPanel();
    add(panel, BorderLayout.CENTER);
    panel.setLayout(new FormLayout(new ColumnSpec[] {
    		FormFactory.RELATED_GAP_COLSPEC,
    		ColumnSpec.decode("180px:grow"),
    		ColumnSpec.decode("1px"),},
    	new RowSpec[] {
    		FormFactory.DEFAULT_ROWSPEC,
    		FormFactory.RELATED_GAP_ROWSPEC,
    		RowSpec.decode("pref:grow"),
    		RowSpec.decode("bottom:default"),}));

    lblMovieSetName = new JLabel("");
    lblMovieSetName.setFont(new Font("Dialog", Font.BOLD, 18));
    panel.add(lblMovieSetName, "2,1, fill, fill");

    layeredPane = new JLayeredPane();
    panel.add(layeredPane, "1, 3, 2, 1, fill, fill");
    layeredPane.setLayout(new FormLayout(
        new ColumnSpec[] { ColumnSpec.decode("10px"), ColumnSpec.decode("120px"), ColumnSpec.decode("default:grow"), }, new RowSpec[] {
            RowSpec.decode("10px"), RowSpec.decode("180px"), RowSpec.decode("default:grow"), }));

    lblMovieSetPoster = new ImageLabel();
    layeredPane.setLayer(lblMovieSetPoster, 1);
    layeredPane.add(lblMovieSetPoster, "2, 2, fill, fill");

    lblMovieSetFanart = new ImageLabel(false, true);
    layeredPane.add(lblMovieSetFanart, "1, 1, 3, 3, fill, fill");

    panelSouth = new JSplitPane();
    panelSouth.setContinuousLayout(true);
    panelSouth.setResizeWeight(0.5);
    add(panelSouth, BorderLayout.SOUTH);

    panelOverview = new JPanel();
    panelSouth.setLeftComponent(panelOverview);
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] {
    		ColumnSpec.decode("250px:grow"),},
    	new RowSpec[] {
    		FormFactory.LINE_GAP_ROWSPEC,
    		FormFactory.DEFAULT_ROWSPEC,
    		FormFactory.RELATED_GAP_ROWSPEC,
    		RowSpec.decode("24px:grow"),}));
    
    lblOverview = new JLabel("Overview");
    panelOverview.add(lblOverview, "1, 2");

    scrollPaneOverview = new JScrollPane();
    panelOverview.add(scrollPaneOverview, "1, 4, fill, fill");

    tpOverview = new JTextPane();
    scrollPaneOverview.setViewportView(tpOverview);

    JPanel panelMovies = new JPanel();
    panelSouth.setRightComponent(panelMovies);
    panelMovies.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("453px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("203px:grow"), }));

    JScrollPane scrollPaneMovies = new JScrollPane();
    panelMovies.add(scrollPaneMovies, "1, 2, fill, fill");
    tableAssignedMovies = new JTable();
    tableAssignedMovies.setPreferredScrollableViewportSize(new Dimension(450, 200));
    scrollPaneMovies.setViewportView(tableAssignedMovies);

    initDataBindings();

    // adjust table columns
    // year column
    tableAssignedMovies.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    tableAssignedMovies.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    tableAssignedMovies.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);

    // watched column
    tableAssignedMovies.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(70);
    tableAssignedMovies.getTableHeader().getColumnModel().getColumn(2).setMinWidth(70);
    tableAssignedMovies.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(85);
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
    jTableBinding.addColumnBinding(movieBeanProperty).setColumnName("Movieset parts").setEditable(false);
    //
    BeanProperty<Movie, String> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setColumnName("Year").setEditable(false);
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_2 = BeanProperty.create("watched");
    jTableBinding.addColumnBinding(movieBeanProperty_2).setColumnName("Watched").setEditable(false).setColumnClass(Boolean.class);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovieSet.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<MovieSetSelectionModel, String, ImageLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_2, lblMovieSetPoster, imageLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovieSet.fanartUrl");
    AutoBinding<MovieSetSelectionModel, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_3, lblMovieSetFanart, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty_4 = BeanProperty.create("selectedMovieSet.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSetSelectionModel, String, JTextPane, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_4, tpOverview, jTextPaneBeanProperty);
    autoBinding_3.bind();
  }
}
