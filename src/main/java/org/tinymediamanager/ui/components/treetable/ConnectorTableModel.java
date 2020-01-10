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

import javax.swing.table.AbstractTableModel;

import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * This class represents the tableFormat in a model and provides the connector between the tree and the table
 * 
 * @author Manuel Laggner
 */
class ConnectorTableModel extends AbstractTableModel {
  private TmmTableFormat    tableFormat;
  private TmmTreeTableModel treeTableModel;

  public ConnectorTableModel(TmmTableFormat tableFormat, TmmTreeTableModel treeTableModel) {
    this.tableFormat = tableFormat;
    this.treeTableModel = treeTableModel;
  }

  TmmTableFormat getTableFormat() {
    return tableFormat;
  }

  @Override
  public int getRowCount() {
    // will never be called - the tree model handles this
    return -1;
  }

  @Override
  public int getColumnCount() {
    return tableFormat.getColumnCount();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object node = getNodeForRow(rowIndex);
    if (node == null) {
      assert false : "Some node should exist on row " + rowIndex + " and on column " + columnIndex + ", but was null.";
      return null;
    }
    return tableFormat.getColumnValue(node, columnIndex);
  }

  public String getTooltipAt(int rowIndex, int columnIndex) {
    Object node = getNodeForRow(rowIndex);
    if (node == null) {
      assert false : "Some node should exist on row " + rowIndex + " and on column " + columnIndex + ", but was null.";
      return null;
    }
    return tableFormat.getColumnTooltip(node, columnIndex);
  }

  @Override
  public String getColumnName(int column) {
    return tableFormat.getColumnName(column);
  }

  @Override
  public Class getColumnClass(int columnIndex) {
    return tableFormat.getColumnClass(columnIndex);
  }

  /**
   * Get the object that will be passed to the RowModel to fetch values for the given row.
   * 
   * @param row
   *          The row we need the tree node for
   */
  private Object getNodeForRow(int row) {
    return treeTableModel.getValueAt(row, 0);
  }
}
