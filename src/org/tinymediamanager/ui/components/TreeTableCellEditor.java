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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;

/**
 * The Class TreeTableCellEditor.
 * 
 * @author Manuel Laggner
 */
public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2247338633359978973L;

  /** The tree. */
  private JTree             tree;

  /** The table. */
  private JTable            table;

  /**
   * My tree table cell editor.
   * 
   * @param tree
   *          the tree
   * @param table
   *          the table
   */
  public TreeTableCellEditor(JTree tree, JTable table) {
    this.tree = tree;
    this.table = table;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
   */
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
    return tree;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.AbstractCellEditor#isCellEditable(java.util.EventObject)
   */
  public boolean isCellEditable(EventObject e) {
    if (e instanceof MouseEvent) {
      // delegate mouse click to the tree
      MouseEvent me = (MouseEvent) e;
      tree.dispatchEvent(me);
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.CellEditor#getCellEditorValue()
   */
  @Override
  public Object getCellEditorValue() {
    return null;
  }

}
