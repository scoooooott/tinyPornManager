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
package org.tinymediamanager.ui.tvshows.panels.tvshow;

import static org.tinymediamanager.core.Constants.ACTORS;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.ActorImageLabel;
import org.tinymediamanager.ui.components.PersonTable;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.tvshows.TvShowSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowCastPanel, to display the cast for this tv show.
 * 
 * @author Manuel Laggner
 */
public class TvShowCastPanel extends JPanel {
  private static final long           serialVersionUID = 2374973082749248956L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private final TvShowSelectionModel  selectionModel;
  private EventList<Person>           actorEventList   = null;

  /**
   * UI elements
   */
  private TmmTable                    tableActors;
  private ActorImageLabel             lblActorImage;

  /**
   * Instantiates a new tv show cast panel.
   * 
   * @param model
   *          the selection model
   */
  public TvShowCastPanel(TvShowSelectionModel model) {
    selectionModel = model;
    actorEventList = GlazedLists.threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(Person.class)));

    initComponents();

    lblActorImage.enableLightbox();
    lblActorImage.setCacheUrl(true);

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection/change of a TV show
      if (source.getClass() != TvShowSelectionModel.class) {
        return;
      }

      if ("selectedTvShow".equals(property) || ACTORS.equals(property)) {
        actorEventList.clear();
        actorEventList.addAll(selectionModel.getSelectedTvShow().getActors());
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
          lblActorImage.setActor(selectionModel.getSelectedTvShow(), actor);
        }
        else {
          lblActorImage.setImageUrl("");
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][400lp,grow][150lp,grow]", "[200lp,grow][grow]"));
    {
      JLabel lblActorsT = new TmmLabel(BUNDLE.getString("metatag.actors"));
      TmmFontHelper.changeFont(lblActorsT, Font.BOLD);
      add(lblActorsT, "cell 0 0,aligny top");

      lblActorImage = new ActorImageLabel();
      add(lblActorImage, "cell 2 0,grow");

      tableActors = new PersonTable(actorEventList);
      JScrollPane scrollPaneActors = new JScrollPane(tableActors);
      tableActors.configureScrollPane(scrollPaneActors);
      scrollPaneActors.setViewportView(tableActors);
      add(scrollPaneActors, "cell 1 0 1 2,grow");
    }
  }
}
