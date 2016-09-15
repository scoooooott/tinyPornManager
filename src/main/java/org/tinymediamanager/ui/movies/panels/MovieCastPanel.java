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

import static org.tinymediamanager.core.Constants.ACTORS;
import static org.tinymediamanager.core.Constants.PRODUCERS;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.ui.BorderTableCellRenderer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ActorImageLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.movies.MovieSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * Panel to display the movie actors, writer and director
 * 
 * @author Manuel Laggner
 */
public class MovieCastPanel extends JPanel {
  private static final long                     serialVersionUID   = 2972207353452870494L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle           BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSelectionModel                   selectionModel;
  private EventList<MovieActor>                 actorEventList     = null;
  private DefaultEventTableModel<MovieActor>    actorTableModel    = null;
  private EventList<MovieProducer>              producerEventList  = null;
  private DefaultEventTableModel<MovieProducer> producerTableModel = null;

  /**
   * UI elements
   */
  private JLabel                                lblDirector;
  private JLabel                                lblWriter;
  private ActorImageLabel                       lblActorThumb;
  private JTable                                tableProducer;
  private JTable                                tableActors;

  public MovieCastPanel(MovieSelectionModel model) {
    selectionModel = model;
    producerEventList = GlazedLists
        .threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(MovieProducer.class)));
    producerTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(producerEventList), new ProducerTableFormat());

    actorEventList = GlazedLists.threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(MovieActor.class)));
    actorTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(actorEventList), new ActorTableFormat());

    initComponents();
    initDataBindings();

    tableProducer.getColumnModel().getColumn(0).setCellRenderer(new BorderTableCellRenderer());
    tableProducer.getColumnModel().getColumn(1).setCellRenderer(new BorderTableCellRenderer());
    tableActors.getColumnModel().getColumn(0).setCellRenderer(new BorderTableCellRenderer());
    tableActors.getColumnModel().getColumn(1).setCellRenderer(new BorderTableCellRenderer());

    // selectionlistener for the selected actor
    tableActors.getSelectionModel().addListSelectionListener(arg0 -> {
      if (!arg0.getValueIsAdjusting()) {
        int selectedRow = tableActors.convertRowIndexToModel(tableActors.getSelectedRow());
        if (selectedRow >= 0 && selectedRow < actorEventList.size()) {
          MovieActor actor = actorEventList.get(selectedRow);
          lblActorThumb.setActor(actor);
        }
        else {
          lblActorThumb.setImageUrl("");
        }
      }
    });

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection of a movie and change of a movie
      if ((source instanceof MovieSelectionModel && "selectedMovie".equals(property)) || (source instanceof Movie && ACTORS.equals(property))) {
        actorEventList.clear();
        actorEventList.addAll(selectionModel.getSelectedMovie().getActors());
        if (actorEventList.size() > 0) {
          tableActors.getSelectionModel().setSelectionInterval(0, 0);
        }
      }
      if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
          || (source.getClass() == Movie.class && PRODUCERS.equals(property))) {
        producerEventList.clear();
        producerEventList.addAll(selectionModel.getSelectedMovie().getProducers());
        if (producerEventList.size() > 0) {
          tableProducer.getSelectionModel().setSelectionInterval(0, 0);
        }
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][400lp,grow][150lp,grow]", "[][][150lp,grow][200lp,grow]"));
    {
      JLabel lblDirectorT = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblDirectorT, Font.BOLD);
      add(lblDirectorT, "cell 0 0,alignx right,aligny top");

      lblDirector = new JLabel("");
      lblDirectorT.setLabelFor(lblDirector);
      add(lblDirector, "cell 1 0 2 1");
    }
    {
      JLabel lblWriterT = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblWriterT, Font.BOLD);
      add(lblWriterT, "cell 0 1,alignx right,aligny top");

      lblWriter = new JLabel("");
      lblWriterT.setLabelFor(lblWriter);
      add(lblWriter, "cell 1 1 2 1,wmin 0");
    }
    {
      JLabel lblProducersT = new JLabel(BUNDLE.getString("metatag.producers")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblProducersT, Font.BOLD);
      add(lblProducersT, "cell 0 2,alignx right,aligny top");

      tableProducer = new TmmTable(producerTableModel);
      JScrollPane scrollPaneMovieProducer = new JScrollPane(tableProducer);
      add(scrollPaneMovieProducer, "cell 1 2,grow");
      scrollPaneMovieProducer.setViewportView(tableProducer);
    }
    {
      JLabel lblActorsT = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblActorsT, Font.BOLD);
      add(lblActorsT, "cell 0 3,alignx right,aligny top");

      tableActors = new TmmTable(actorTableModel);
      JScrollPane scrollPaneMovieActors = new JScrollPane(tableActors);
      add(scrollPaneMovieActors, "cell 1 3,grow");
      scrollPaneMovieActors.setViewportView(tableActors);
    }
    {
      lblActorThumb = new ActorImageLabel();
      add(lblActorThumb, "cell 2 3,grow");
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

  /**
   * inner class for representing the table
   */
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

  private static class ProducerTableFormat implements AdvancedTableFormat<MovieProducer> {
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
}
