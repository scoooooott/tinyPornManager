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
package org.tinymediamanager.ui.components.treetable;

import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;

/**
 * This model combines the TreeModel (the tree part) with the ConnectorTableModel (table part)
 * 
 * @author Manuel Laggner
 */
public interface ITmmTreeTableModel extends TreeModel, TableModel {

  /**
   * Get the <code>TreePathSupport</code> object this model uses to manage information about expanded nodes. <code>TreePathSupport</code> implements
   * logic for tracking expanded nodes, manages <code>TreeWillExpandListener</code>s, and is a repository for preserving expanded state information
   * about nodes whose parents are currently collapsed. JTree implements very similar logic internally to itself.
   *
   * @return the tree path support
   */
  TmmTreeTableTreePathSupport getTreePathSupport();

  /**
   * Get the layout cache which is used to track the visual state of nodes. This is typically one of the standard JDK layout cache classes, such as
   * <code>VariableHeightLayoutCache</code> or <code>FixedHeightLayoutCache</code>.
   * 
   * @return the abstract layout
   */
  AbstractLayoutCache getLayout();

  /**
   * get the associated tree model
   * 
   * @return the tree model
   */
  TreeModel getTreeModel();

  /**
   * get the associated table model
   * 
   * @return the table model
   */
  TableModel getTableModel();
}
