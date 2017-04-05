/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowEpisodeCastPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeCastPanel extends JPanel {
  private static final long                 serialVersionUID = 4712144916016763491L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle       BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final TvShowEpisodeSelectionModel selectionModel;
  private EventList<Person>                 actorEventList   = null;
  private DefaultEventTableModel<Person>    actorTableModel  = null;

  /**
   * UI elements
   */
  private TmmTable                          tableActors;
  private ImageLabel                        lblActorImage;
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
    actorTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(actorEventList), new ActorTableFormat());

    initComponents();
    initDataBindings();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection of a movie and change of an episode
      if ((source.getClass() == TvShowEpisodeSelectionModel.class && "selectedTvShowEpisode".equals(property))
          || (source.getClass() == TvShowEpisode.class && ACTORS.equals(property))) {
        actorEventList.clear();
        actorEventList.addAll(selectionModel.getSelectedTvShowEpisode().getActors());
        if (actorEventList.size() > 0) {
          tableActors.getSelectionModel().setSelectionInterval(0, 0);
        }
        else {
          lblActorImage.setImageUrl("");
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
          lblActorImage.setImageUrl(actor.getThumbUrl());
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][400lp,grow][150lp,grow]", "[][][200lp,grow][grow]"));
    {
      JLabel lblDirectorT = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblDirectorT, Font.BOLD);
      add(lblDirectorT, "cell 0 0,alignx right");

      lblDirector = new JLabel("");
      add(lblDirector, "cell 1 0 2 1");
    }
    {
      JLabel lblWriterT = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblWriterT, Font.BOLD);
      add(lblWriterT, "cell 0 1,alignx right");

      lblWriter = new JLabel("");
      add(lblWriter, "cell 1 1 2 1");
    }
    {
      JLabel lblActorsT = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblActorsT, Font.BOLD);
      add(lblActorsT, "cell 0 2,alignx right,aligny top");

      tableActors = new TmmTable(actorTableModel);
      JScrollPane scrollPaneActors = new JScrollPane(tableActors);
      tableActors.configureScrollPane(scrollPaneActors);
      scrollPaneActors.setViewportView(tableActors);
      add(scrollPaneActors, "cell 1 2 1 2,grow");
    }
    {
      lblActorImage = new ImageLabel();
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

  private static class ActorTableFormat implements AdvancedTableFormat<Person> {
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
    public Object getColumnValue(Person actor, int column) {
      switch (column) {
        case 0:
          return actor.getName();

        case 1:
          return actor.getRole();
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
