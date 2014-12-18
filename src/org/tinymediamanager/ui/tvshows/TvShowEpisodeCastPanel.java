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
package org.tinymediamanager.ui.tvshows;

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
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
 * The Class TvShowEpisodeCastPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeCastPanel extends JPanel {
  private static final long                   serialVersionUID = 4712144916016763491L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle         BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final TvShowEpisodeSelectionModel   selectionModel;
  private EventList<TvShowActor>              actorEventList   = null;
  private DefaultEventTableModel<TvShowActor> actorTableModel  = null;

  /**
   * UI elements
   */
  private ZebraJTable                         tableActors;
  private ImageLabel                          lblActorImage;
  private JLabel                              lblDirector;
  private JLabel                              lblWriter;

  /**
   * Instantiates a new tv show episode cast panel.
   * 
   * @param model
   *          the selection model
   */
  public TvShowEpisodeCastPanel(TvShowEpisodeSelectionModel model) {
    this.selectionModel = model;
    actorEventList = GlazedLists.threadSafeList(new ObservableElementList<TvShowActor>(new BasicEventList<TvShowActor>(), GlazedLists
        .beanConnector(TvShowActor.class)));
    actorTableModel = new DefaultEventTableModel<TvShowActor>(GlazedListsSwing.swingThreadProxyList(actorEventList), new ActorTableFormat());

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("125px"), }, new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:max(125px;default):grow"), }));

    JLabel lblDirectorT = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
    add(lblDirectorT, "2, 2");

    lblDirector = new JLabel("");
    add(lblDirector, "4, 2");

    JLabel lblWriterT = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
    add(lblWriterT, "2, 4");

    lblWriter = new JLabel("");
    add(lblWriter, "4, 4");

    lblActorImage = new ImageLabel();
    lblActorImage.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
    add(lblActorImage, "6, 2, 1, 5");

    JLabel lblActorsT = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
    add(lblActorsT, "2, 6, left, top");

    tableActors = new ZebraJTable(actorTableModel);
    JScrollPane scrollPaneActors = ZebraJTable.createStripedJScrollPane(tableActors);
    scrollPaneActors.setViewportView(tableActors);
    add(scrollPaneActors, "4, 6, fill, fill");

    initDataBindings();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
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
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);

    // selectionlistener for the selected actor
    tableActors.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        if (!arg0.getValueIsAdjusting()) {
          int selectedRow = tableActors.convertRowIndexToModel(tableActors.getSelectedRow());
          if (selectedRow >= 0 && selectedRow < actorEventList.size()) {
            TvShowActor actor = actorEventList.get(selectedRow);
            lblActorImage.setImageUrl(actor.getThumb());
          }
        }
      }
    });
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty = BeanProperty.create("selectedTvShowEpisode.director");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty, lblDirector, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_1 = BeanProperty.create("selectedTvShowEpisode.writer");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty_1, lblWriter, jLabelBeanProperty);
    autoBinding_1.bind();
  }

  private static class ActorTableFormat implements AdvancedTableFormat<TvShowActor> {
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
    public Object getColumnValue(TvShowActor actor, int column) {
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
}
