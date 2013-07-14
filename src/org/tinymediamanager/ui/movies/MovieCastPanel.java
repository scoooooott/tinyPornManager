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

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieActor;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ActorImageLabel;
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
public class MovieCastPanel extends JPanel {
  private static final long                  serialVersionUID = 2972207353452870494L;
  private static final ResourceBundle        BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSelectionModel                selectionModel;
  private EventList<MovieActor>              actorEventList   = null;
  private DefaultEventTableModel<MovieActor> actorTableModel  = null;

  /**
   * UI elements
   */
  private JLabel                             lblDirectorT;
  private JLabel                             lblDirector;
  private JLabel                             lblWriterT;
  private JLabel                             lblWriter;
  private JLabel                             lblActors;
  private ActorImageLabel                    lblActorThumb;
  private JTable                             tableCast;

  public MovieCastPanel(MovieSelectionModel model) {
    selectionModel = model;
    actorEventList = GlazedLists.threadSafeList(new ObservableElementList<MovieActor>(new BasicEventList<MovieActor>(), GlazedLists
        .beanConnector(MovieActor.class)));
    actorTableModel = new DefaultEventTableModel<MovieActor>(GlazedListsSwing.swingThreadProxyList(actorEventList), new ActorTableFormat());

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(39dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.MIN_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("125px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("80px"), RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    lblDirectorT = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
    add(lblDirectorT, "2, 2");

    lblDirector = new JLabel("");
    lblDirectorT.setLabelFor(lblDirector);
    add(lblDirector, "4, 2, 3, 1");

    lblWriterT = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
    add(lblWriterT, "2, 4");

    lblWriter = new JLabel("");
    lblWriterT.setLabelFor(lblWriter);
    add(lblWriter, "4, 4, 3, 1");

    lblActors = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
    add(lblActors, "2, 6, default, top");

    tableCast = new ZebraJTable(actorTableModel);
    JScrollPane scrollPaneMovieCast = ZebraJTable.createStripedJScrollPane(tableCast);
    lblActors.setLabelFor(scrollPaneMovieCast);
    add(scrollPaneMovieCast, "4, 6, 3, 2");
    scrollPaneMovieCast.setViewportView(tableCast);

    lblActorThumb = new ActorImageLabel();
    add(lblActorThumb, "8, 2, 1, 6, fill, fill");

    initDataBindings();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a movei
        if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
            || (source.getClass() == Movie.class && ACTORS.equals(property))) {
          actorEventList.clear();
          actorEventList.addAll(selectionModel.getSelectedMovie().getActors());
          if (actorEventList.size() > 0) {
            tableCast.getSelectionModel().setSelectionInterval(0, 0);
          }
        }
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);

    // selectionlistener for the selected actor
    tableCast.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        if (!arg0.getValueIsAdjusting()) {
          int selectedRow = tableCast.convertRowIndexToModel(tableCast.getSelectedRow());
          if (selectedRow >= 0 && selectedRow < actorEventList.size()) {
            MovieActor actor = actorEventList.get(selectedRow);
            lblActorThumb.setActor(actor);
          }
          else {
            lblActorThumb.setImageUrl("");
          }
        }
      }
    });
  }

  private static class ActorTableFormat implements AdvancedTableFormat<MovieActor> {
    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.name");//$NON-NLS-1$

        case 1:
          return BUNDLE.getString("metatag.role");//$NON-NLS-1$
      }
      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MovieActor actor, int column) {
      switch (column) {
        case 0:
          return actor.getName();

        case 1:
          return actor.getCharacter();
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
