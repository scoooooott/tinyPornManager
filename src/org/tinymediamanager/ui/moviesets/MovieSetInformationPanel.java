/*
 * Copyright 2013 Manuel Laggner
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

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieSetInformationPanel extends JPanel {

  private MovieSetSelectionModel selectionModel;
  private JLabel                 lblMovieSetName;

  /**
   * Instantiates a new movie set information panel.
   * 
   * @param selectionModel
   *          the selection model
   */
  public MovieSetInformationPanel(MovieSetSelectionModel selectionModel) {
    this.selectionModel = selectionModel;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    lblMovieSetName = new JLabel("");
    add(lblMovieSetName, "2, 2");
    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<MovieSetSelectionModel, String> movieSetSelectionModelBeanProperty = BeanProperty.create("selectedMovieSet.name");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSetSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        movieSetSelectionModelBeanProperty, lblMovieSetName, jLabelBeanProperty);
    autoBinding.bind();
  }
}
