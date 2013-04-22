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

import javax.swing.tree.TreeModel;

/**
 * The Interface TreeTableModel.
 * 
 * @author Manuel Laggner
 */
public interface TreeTableModel extends TreeModel {

  /**
   * Returns the number of available columns.
   * 
   * @return Number of Columns
   */
  public int getColumnCount();

  /**
   * Returns the column name.
   * 
   * @param column
   *          Column number
   * @return Column name
   */
  public String getColumnName(int column);

  /**
   * Returns the type (class) of a column.
   * 
   * @param column
   *          Column number
   * @return Class
   */
  public Class<?> getColumnClass(int column);

  /**
   * Returns the value of a node in a column.
   * 
   * @param node
   *          Node
   * @param column
   *          Column number
   * @return Value of the node in the column
   */
  public Object getValueAt(Object node, int column);

  /**
   * Check if a cell of a node in one column is editable.
   * 
   * @param node
   *          Node
   * @param column
   *          Column number
   * @return true/false
   */
  public boolean isCellEditable(Object node, int column);

  /**
   * Sets a value for a node in one column.
   * 
   * @param aValue
   *          New value
   * @param node
   *          Node
   * @param column
   *          Column number
   */
  public void setValueAt(Object aValue, Object node, int column);
}
