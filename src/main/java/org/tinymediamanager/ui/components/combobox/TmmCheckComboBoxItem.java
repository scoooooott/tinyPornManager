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
package org.tinymediamanager.ui.components.combobox;

import javax.swing.JCheckBox;

/**
 * this class is a wrapper to hold an item for the TmmCheckComboBox
 * 
 * @author Manuel Laggner
 */
class TmmCheckComboBoxItem<E> extends JCheckBox {
  private static final long serialVersionUID = 6243546057910976652L;

  private final E           userObject;

  /**
   * create an item with the given user object
   * 
   * @param userObject
   *          the user object to assign to that item
   */
  public TmmCheckComboBoxItem(final E userObject) {
    super(userObject.toString());
    this.userObject = userObject;
  }

  /**
   * create an empty item with just the description string
   * 
   * @param description
   *          the description to be displayed
   */
  public TmmCheckComboBoxItem(final String description) {
    super(description);
    this.userObject = null;
  }

  /**
   * get the associated user object
   * 
   * @return the user object
   */
  public E getUserObject() {
    return userObject;
  }
}
