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
package org.tinymediamanager.ui.components.tree;

import java.beans.PropertyChangeListener;

/**
 * The interface TmmTreeFilter is used for creating tree filters for the TmmTree
 * 
 * @author Manuel Laggner
 *
 * @param <E>
 */
public interface ITmmTreeFilter<E extends TmmTreeNode> {
  String TREE_FILTER_CHANGED = "treeFilterChanged";

  /**
   * is this filter active?
   *
   * @return true if this filter is active
   */
  boolean isActive();

  /**
   * Returns whether the specified object is accepted by this filter or not.
   *
   * @param object
   *          object to process
   * @return true if the specified object is accepted by this filter, false otherwise
   */
  boolean accept(E object);

  /**
   * Adds the property change listener.
   *
   * @param listener
   *          the listener
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Adds the property change listener.
   *
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Removes the property change listener.
   *
   * @param listener
   *          the listener
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes the property change listener.
   *
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
