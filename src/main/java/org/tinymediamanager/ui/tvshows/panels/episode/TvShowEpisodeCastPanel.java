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
package org.tinymediamanager.ui.tvshows.panels.episode;

import static org.tinymediamanager.core.Constants.ACTORS;

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
import org.tinymediamanager.ui.tvshows.TvShowEpisodeSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowEpisodeCastPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeCastPanel extends JPanel {
  private static final long                 serialVersionUID = 4712144916016763491L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle       BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private final TvShowEpisodeSelectionModel selectionModel;
  private EventList<Person>                 actorEventList   = null;

  /**
   * UI elements
   */
  private TmmTable                          tableActors;
  private ActorImageLabel                   lblActorImage;
  private JLabel                            lblDirector;
  private JLabel                            lblWriter;

  /**
   * Instantiates a new tv show episode cast panel.
   * 
   * @param model
   *          the selection model
   */
  public TvShowEpisodeCastPanel(TvShowEpisodeSelectionModel model) {
    this.selectionModel = model;
    actorEventList = GlazedLists.threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(Person.class)));

    initComponents();
    initDataBindings();

    lblActorImage.enableLightbox();
    lblActorImage.setCacheUrl(true);

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection/change of an episode

      if (source.getClass() != TvShowEpisodeSelectionModel.class) {
        return;
      }

      if ("selectedTvShowEpisode".equals(property) || ACTORS.equals(property)) {
        actorEventList.clear();
        actorEventList.addAll(selectionModel.getSelectedTvShowEpisode().getActors());
        if (!actorEventList.isEmpty()) {
          tableActors.getSelectionModel().setSelectionInterval(0, 0);
        }
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);

    // selectionlistener for the selected actor
    tableActors.getSelectionModel().addListSelectionListener(arg0 -> {
      if (!arg0.getValueIsAdjusting()) {
        int selectedRow = tableActors.convertRowIndexToModel(tableActors.getSelectedRow());
        if (selectedRow >= 0 && selectedRow < actorEventList.size()) {
          Person actor = actorEventList.get(selectedRow);
          lblActorImage.setActor(selectionModel.getSelectedTvShowEpisode().getTvShow(), actor);
        }
        else {
          lblActorImage.setImageUrl("");
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][400lp,grow][150lp,grow]", "[][][200lp,grow][grow]"));
    {
      JLabel lblDirectorT = new TmmLabel(BUNDLE.getString("metatag.director"));
      add(lblDirectorT, "cell 0 0");

      lblDirector = new JLabel("");
      add(lblDirector, "cell 1 0 2 1,growx,wmin 0");
    }
    {
      JLabel lblWriterT = new TmmLabel(BUNDLE.getString("metatag.writer"));
      add(lblWriterT, "cell 0 1");

      lblWriter = new JLabel("");
      add(lblWriter, "cell 1 1 2 1,growx,wmin 0");
    }
    {
      JLabel lblActorsT = new TmmLabel(BUNDLE.getString("metatag.actors"));
      add(lblActorsT, "cell 0 2,aligny top");

      tableActors = new PersonTable(actorEventList);
      JScrollPane scrollPaneActors = new JScrollPane(tableActors);
      tableActors.configureScrollPane(scrollPaneActors);
      add(scrollPaneActors, "cell 1 2 1 2,grow");
    }
    {
      lblActorImage = new ActorImageLabel();
      add(lblActorImage, "cell 2 2,grow");
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty = BeanProperty
        .create("selectedTvShowEpisode.directorsAsString");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty, lblDirector, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_1 = BeanProperty
        .create("selectedTvShowEpisode.writersAsString");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty_1, lblWriter, jLabelBeanProperty);
    autoBinding_1.bind();
  }
}
