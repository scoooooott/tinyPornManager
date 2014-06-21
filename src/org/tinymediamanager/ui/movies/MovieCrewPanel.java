/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ZebraJTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Panel to display the movie actors, writer and director
 * 
 * @author Manuel Laggner
 */
public class MovieCrewPanel extends JPanel {
  private static final long                     serialVersionUID   = 2972207353452870494L;
  private static final ResourceBundle           BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSelectionModel                   selectionModel;
  private EventList<MovieProducer>              producerEventList  = null;
  private DefaultEventTableModel<MovieProducer> ProducerTableModel = null;

  /**
   * UI elements
   */
  private JLabel                                lblDirectorT;
  private JLabel                                lblWriterT;
  private JLabel                                lblProducer;
  private JTable                                tableProducer;
  private JLabel                                lblDirector;
  private JLabel                                lblWriter;

  public MovieCrewPanel(MovieSelectionModel model) {
    selectionModel = model;
    producerEventList = GlazedLists.threadSafeList(new ObservableElementList<MovieProducer>(new BasicEventList<MovieProducer>(), GlazedLists
        .beanConnector(MovieProducer.class)));
    ProducerTableModel = new DefaultEventTableModel<MovieProducer>(GlazedListsSwing.swingThreadProxyList(producerEventList),
        new ProducerTableFormat());

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        RowSpec.decode("80px"), RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    lblDirectorT = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
    add(lblDirectorT, "2, 2");

    lblDirector = new JLabel("");
    add(lblDirector, "4, 2");

    lblWriterT = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
    add(lblWriterT, "2, 4");

    lblWriter = new JLabel("");
    add(lblWriter, "4, 4");

    lblProducer = new JLabel(BUNDLE.getString("metatag.producers")); //$NON-NLS-1$
    add(lblProducer, "2, 6, default, top");

    tableProducer = new ZebraJTable(ProducerTableModel);
    tableProducer.setTableHeader(null);
    JScrollPane scrollPaneMovieCast = ZebraJTable.createStripedJScrollPane(tableProducer);
    lblProducer.setLabelFor(scrollPaneMovieCast);
    add(scrollPaneMovieCast, "4, 6, 1, 2");
    scrollPaneMovieCast.setViewportView(tableProducer);

    initDataBindings();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a movei
        if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
            || (source.getClass() == Movie.class && PRODUCERS.equals(property))) {
          producerEventList.clear();
          producerEventList.addAll(selectionModel.getSelectedMovie().getProducers());
          if (producerEventList.size() > 0) {
            tableProducer.getSelectionModel().setSelectionInterval(0, 0);
          }
        }
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private static class ProducerTableFormat implements AdvancedTableFormat<MovieProducer> {
    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
        case 1:
          return "";
      }
      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MovieProducer producer, int column) {
      switch (column) {
        case 0:
          return producer.getName();

        case 1:
          return producer.getRole();
      }
      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
        case 1:
          return String.class;
      }
      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int column) {
      return null;
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.director");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSelectionModelBeanProperty, lblDirector, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.writer");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSelectionModelBeanProperty_1, lblWriter, jLabelBeanProperty);
    autoBinding_1.bind();
  }
}
