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

import java.awt.Dimension;

import javax.swing.JTree;

/**
 * The Class TreeTable.
 * 
 * @author Manuel Laggner
 */
public class TreeTable extends ZebraJTable {

  /** The Constant serialVersionUID. */
  private static final long     serialVersionUID = 1020389681247739653L;

  /** The tree. */
  private TreeTableCellRenderer tree;

  /**
   * Instantiates a new tree table.
   * 
   * @param treeTableModel
   *          the tree table model
   */
  public TreeTable(AbstractTreeTableModel treeTableModel) {
    super();

    // create the JTree
    tree = new TreeTableCellRenderer(this, treeTableModel);

    // set model
    super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

    // select tree and table at the same time
    TreeTableSelectionModel selectionModel = new TreeTableSelectionModel();
    tree.setSelectionModel(selectionModel);
    setSelectionModel(selectionModel.getListSelectionModel());

    // tree renderer
    setDefaultRenderer(TreeTableModel.class, tree);
    // treetable editor
    setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor(tree, this));

    // supress grid
    setShowGrid(false);

    // no spacing
    setIntercellSpacing(new Dimension(0, 0));
  }

  public final JTree getTree() {
    return tree;
  }
}
