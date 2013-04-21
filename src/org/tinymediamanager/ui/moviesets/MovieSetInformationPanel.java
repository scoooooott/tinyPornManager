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
package org.tinymediamanager.ui.moviesets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.ResourceBundle;

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
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieSetInformationPanel extends JPanel {

  /** The Constant BUNDLE. */
  private static final ResourceBundle   BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long             serialVersionUID = -8166784589262658147L;

  /** The selection model. */
  private MovieSetSelectionModel        selectionModel;

  /** The lbl movie set name. */
  private JLabel                        lblMovieSetName;

  /** The table assigned movies. */
  private JTable                        tableAssignedMovies;

  /** The lbl movie set poster. */
  private ImageLabel                    lblMovieSetPoster;

  /** The panel. */
  private JPanel                        panel;

  /** The layered pane. */
  private JLayeredPane                  layeredPane;

  /** The lbl movie set fanart. */
  private ImageLabel                    lblMovieSetFanart;

  /** The panel south. */
  private JSplitPane                    panelSouth;

  /** The scroll pane overview. */
  private JScrollPane                   scrollPaneOverview;

  /** The tp overview. */
  private JTextPane                     tpOverview;

  /** The panel overview. */
  private JPanel                        panelOverview;

  /** The lbl overview. */
  private JLabel                        lblOverview;

  /** The media file event list. */
  private EventList<Movie>              movieEventList;

  /** The media file table model. */
  private DefaultEventTableModel<Movie> movieTableModel  = null;

  /**
   * Instantiates a new movie set information panel.
   * 
   * @param model
   *          the model
   */
  public MovieSetInformationPanel(MovieSetSelectionModel model) {
    this.selectionModel = model;
    movieEventList = new ObservableElementList<Movie>(GlazedLists.threadSafeList(new BasicEventList<Movie>()), GlazedLists.beanConnector(Movie.class));

    setLayout(new BorderLayout(0, 0));

    panel = new JPanel();
    add(panel, BorderLayout.CENTER);
    panel
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("180px:grow"), ColumnSpec.decode("1px"), },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("pref:grow"),
                RowSpec.decode("bottom:default"), }));

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
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("250px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("24px:grow"), }));

    lblOverview = new JLabel(BUNDLE.getString("movieinformation.overview")); //$NON-NLS-1$
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

    movieTableModel = new DefaultEventTableModel<Movie>(movieEventList, new MovieInMovieSetTableFormat());
    tableAssignedMovies = new JTable(movieTableModel);
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

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of media files
        if ((source.getClass() == MovieSetSelectionModel.class && "selectedMovieSet".equals(property))
            || (source.getClass() == MovieSet.class && "movies".equals(property))) {
          movieEventList.clear();
          movieEventList.addAll(selectionModel.getSelectedMovieSet().getMovies());
        }
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * The Class MediaTableFormat.
   * 
   * @author Manuel Laggner
   */
  private static class MovieInMovieSetTableFormat implements AdvancedTableFormat<Movie> {

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
     */
    @Override
    public int getColumnCount() {
      return 3;
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
          return BUNDLE.getString("movieset.parts"); //$NON-NLS-1$

        case 1:
          return BUNDLE.getString("metatag.year"); //$NON-NLS-1$

        case 2:
          return BUNDLE.getString("metatag.watched"); //$NON-NLS-1$
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object, int)
     */
    @Override
    public Object getColumnValue(Movie movie, int column) {
      switch (column) {
        case 0:
          return movie.getTitle();

        case 1:
          return movie.getYear();

        case 2:
          return movie.isWatched();
      }
      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
        case 1:
          return String.class;

        case 2:
          return Boolean.class;
      }
      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int arg0) {
      return null;
    }

  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty = BeanProperty.create("selectedMovieSet.title");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSetSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty, lblMovieSetName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty_4 = BeanProperty.create("selectedMovieSet.plot");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSetSelectionModel, String, JTextPane, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_4, tpOverview, jTextPaneBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovieSet.fanart");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty_1 = BeanProperty.create("imagePath");
    AutoBinding<MovieSetSelectionModel, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_1, lblMovieSetFanart, imageLabelBeanProperty_1);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovieSet.poster");
    AutoBinding<MovieSetSelectionModel, String, ImageLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty_2, lblMovieSetPoster, imageLabelBeanProperty_1);
    autoBinding_1.bind();
  }
}
