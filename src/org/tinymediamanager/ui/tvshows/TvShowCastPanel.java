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
package org.tinymediamanager.ui.tvshows;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.tvshow.TvShowActor;
import org.tinymediamanager.ui.components.ImageLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowCastPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowCastPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long          serialVersionUID = 2374973082749248956L;

  /** The selection model. */
  private final TvShowSelectionModel selectionModel;

  /** The table actors. */
  private JTable                     tableActors;

  /** The lbl actor image. */
  private ImageLabel                 lblActorImage;

  /**
   * Instantiates a new tv show cast panel.
   * 
   * @param selectionModel
   *          the selection model
   */
  public TvShowCastPanel(TvShowSelectionModel selectionModel) {
    this.selectionModel = selectionModel;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("125px"), }, new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("fill:max(125px;default):grow"), }));

    lblActorImage = new ImageLabel();
    add(lblActorImage, "6, 2");

    JLabel lblActorsT = new JLabel("Actors");
    add(lblActorsT, "2, 2, left, top");

    JScrollPane scrollPaneActors = new JScrollPane();
    add(scrollPaneActors, "4, 2, fill, fill");

    tableActors = new JTable();
    scrollPaneActors.setViewportView(tableActors);
    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSelectionModel, List<TvShowActor>> tvShowSelectionModelBeanProperty_2 = BeanProperty.create("selectedTvShow.actors");
    JTableBinding<TvShowActor, TvShowSelectionModel, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_2, tableActors);
    //
    BeanProperty<TvShowActor, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty).setColumnName("Name").setEditable(false);
    //
    BeanProperty<TvShowActor, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1).setColumnName("Character").setEditable(false);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.thumb");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, tableActors, jTableBeanProperty,
        lblActorImage, imageLabelBeanProperty);
    autoBinding_2.bind();
  }

  /**
   * further initializations
   */
  void init() {
    if (tableActors.getModel().getRowCount() > 0) {
      tableActors.getSelectionModel().setSelectionInterval(0, 0);
    }
    else {
      lblActorImage.setImageUrl("");
    }

    // changes upon movie selection
    tableActors.getModel().addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        // change to the first actor on movie change
        if (tableActors.getModel().getRowCount() > 0) {
          tableActors.getSelectionModel().setSelectionInterval(0, 0);
        }
        else {
          lblActorImage.setImageUrl("");
        }
      }
    });
  }
}
