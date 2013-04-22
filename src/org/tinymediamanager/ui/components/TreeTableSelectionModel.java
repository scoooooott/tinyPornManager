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
package org.tinymediamanager.ui.components;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;

/**
 * The Class TreeTableSelectionModel.
 * 
 * @author Manuel Laggner
 */
public class TreeTableSelectionModel extends DefaultTreeSelectionModel {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -678180965576521790L;

  /**
   * Instantiates a new tree table selection model.
   */
  public TreeTableSelectionModel() {
    super();

    getListSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {

      }
    });
  }

  /**
   * Gets the list selection model.
   * 
   * @return the list selection model
   */
  ListSelectionModel getListSelectionModel() {
    return listSelectionModel;
  }
}
