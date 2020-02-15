/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.ui.components.ActorImageLabel;
import org.tinymediamanager.ui.components.PersonTable;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.movies.MovieSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import net.miginfocom.swing.MigLayout;

/**
 * Panel to display the movie actors, writer and director
 * 
 * @author Manuel Laggner
 */
public class MovieCastPanel extends JPanel {
  private static final long           serialVersionUID  = 2972207353452870494L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE            = ResourceBundle.getBundle("messages", new UTF8Control());

  private MovieSelectionModel         selectionModel;
  private EventList<Person>           actorEventList    = null;
  private EventList<Person>           producerEventList = null;

  /**
   * UI elements
   */
  private JLabel                      lblDirector;
  private JLabel                      lblWriter;
  private ActorImageLabel             lblActorThumb;
  private TmmTable                    tableProducer;
  private TmmTable                    tableActors;

  public MovieCastPanel(MovieSelectionModel model) {
    selectionModel = model;
    producerEventList = GlazedLists.threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(Person.class)));
    actorEventList = GlazedLists.threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(Person.class)));

    initComponents();
    initDataBindings();

    lblActorThumb.enableLightbox();
    lblActorThumb.setCacheUrl(true);

    // selectionlistener for the selected actor
    tableActors.getSelectionModel().addListSelectionListener(arg0 -> {
      if (!arg0.getValueIsAdjusting()) {
        int selectedRow = tableActors.convertRowIndexToModel(tableActors.getSelectedRow());
        if (selectedRow >= 0 && selectedRow < actorEventList.size()) {
          Person actor = actorEventList.get(selectedRow);
          lblActorThumb.setActor(selectionModel.getSelectedMovie(), actor);
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

      if (source.getClass() != MovieSelectionModel.class) {
        return;
      }

      // react on selection of a movie and change of a movie
      if ("selectedMovie".equals(property) || ACTORS.equals(property)) {
        actorEventList.clear();
        actorEventList.addAll(selectionModel.getSelectedMovie().getActors());
        if (!actorEventList.isEmpty()) {
          tableActors.getSelectionModel().setSelectionInterval(0, 0);
        }
      }
      if ("selectedMovie".equals(property) || PRODUCERS.equals(property)) {
        producerEventList.clear();
        producerEventList.addAll(selectionModel.getSelectedMovie().getProducers());
        if (!producerEventList.isEmpty()) {
          tableProducer.getSelectionModel().setSelectionInterval(0, 0);
        }
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][400lp,grow][150lp,grow]", "[][][100lp:150lp,grow][150lp:200lp,grow]"));
    {
      JLabel lblDirectorT = new TmmLabel(BUNDLE.getString("metatag.director"));
      add(lblDirectorT, "cell 0 0");

      lblDirector = new JLabel("");
      lblDirectorT.setLabelFor(lblDirector);
      add(lblDirector, "cell 1 0 2 1,growx,wmin 0");
    }
    {
      JLabel lblWriterT = new TmmLabel(BUNDLE.getString("metatag.writer"));
      add(lblWriterT, "cell 0 1");

      lblWriter = new JLabel("");
      lblWriterT.setLabelFor(lblWriter);
      add(lblWriter, "cell 1 1 2 1,growx,wmin 0");
    }
    {
      JLabel lblProducersT = new TmmLabel(BUNDLE.getString("metatag.producers"));
      add(lblProducersT, "cell 0 2,aligny top");

      tableProducer = new PersonTable(producerEventList);
      JScrollPane scrollPanePerson = new JScrollPane(tableProducer);
      tableProducer.configureScrollPane(scrollPanePerson);
      add(scrollPanePerson, "cell 1 2,grow");
    }
    {
      JLabel lblActorsT = new TmmLabel(BUNDLE.getString("metatag.actors"));
      add(lblActorsT, "cell 0 3,aligny top");

      tableActors = new PersonTable(actorEventList);
      JScrollPane scrollPanePersons = new JScrollPane(tableActors);
      tableActors.configureScrollPane(scrollPanePersons);
      add(scrollPanePersons, "cell 1 3,grow");
    }
    {
      lblActorThumb = new ActorImageLabel();
      add(lblActorThumb, "cell 2 3,grow");
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.directorsAsString");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSelectionModelBeanProperty, lblDirector, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.writersAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSelectionModelBeanProperty_1, lblWriter, jLabelBeanProperty);
    autoBinding_1.bind();
  }
}
