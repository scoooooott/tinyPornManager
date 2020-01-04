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

import javax.swing.ImageIcon;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.VariableHeightLayoutCache;

import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * a default table model for the TmmTreeTable
 *
 * @author Manuel Laggner
 */
public class TmmTreeTableModel implements ITmmTreeTableModel {
  private static String                NODES_COLUMN_LABEL = "Nodes";

  private TreeModel                    treeModel;
  private ConnectorTableModel          tableModel;
  private AbstractLayoutCache          layout;
  private TmmTreeTableEventBroadcaster eventBroadcaster;
  private TmmTreeTableTreePathSupport  treePathSupport;

  public TmmTreeTableModel(TreeModel treeModel, TmmTableFormat tableFormat) {
    this.treeModel = treeModel;
    this.tableModel = new ConnectorTableModel(tableFormat, this);

    this.layout = new VariableHeightLayoutCache();
    this.layout.setModel(this);
    this.layout.setRootVisible(true);
    this.treePathSupport = new TmmTreeTableTreePathSupport(layout);
    this.eventBroadcaster = new TmmTreeTableEventBroadcaster(this);
    this.treePathSupport.addTreeExpansionListener(eventBroadcaster);
    this.treePathSupport.addTreeWillExpandListener(eventBroadcaster);

    this.treeModel.addTreeModelListener(eventBroadcaster);
  }

  @Override
  public int getRowCount() {
    return layout.getRowCount();
  }

  @Override
  public int getColumnCount() {
    return tableModel.getColumnCount() + 1;
  }

  @Override
  public String getColumnName(int columnIndex) {
    if (columnIndex == 0) {
      return NODES_COLUMN_LABEL;
    }
    else {
      return tableModel.getColumnName(columnIndex - 1);
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == 0) {
      return Object.class;
    }
    else {
      return tableModel.getColumnClass(columnIndex - 1);
    }
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      return false;
    }
    else {
      return tableModel.isCellEditable(rowIndex, columnIndex - 1);
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object result;
    if (columnIndex == 0) {
      TreePath path = layout.getPathForRow(rowIndex);
      if (path != null) {
        result = path.getLastPathComponent();
      }
      else {
        result = null;
      }
    }
    else {
      result = (tableModel.getValueAt(rowIndex, columnIndex - 1));
    }
    return result;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex != 0) {
      tableModel.setValueAt(aValue, rowIndex, columnIndex - 1);
    }
    else {
      setTreeValueAt(aValue, rowIndex);
    }
  }

  @Override
  public void addTableModelListener(TableModelListener l) {
    eventBroadcaster.addTableModelListener(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l) {
    eventBroadcaster.removeTableModelListener(l);
  }

  @Override
  public Object getRoot() {
    return treeModel.getRoot();
  }

  @Override
  public Object getChild(Object parent, int index) {
    return treeModel.getChild(parent, index);
  }

  @Override
  public int getChildCount(Object parent) {
    return treeModel.getChildCount(parent);
  }

  @Override
  public boolean isLeaf(Object node) {
    return null != node && treeModel.isLeaf(node);
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // if the model is correctly implemented, this will trigger a change event
    treeModel.valueForPathChanged(path, newValue);
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    return treeModel.getIndexOfChild(parent, child);
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    eventBroadcaster.addTreeModelListener(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    eventBroadcaster.removeTreeModelListener(l);
  }

  protected void setTreeValueAt(Object aValue, int rowIndex) {
    // do nothing here
  }

  @Override
  public final TmmTreeTableTreePathSupport getTreePathSupport() {
    return treePathSupport;
  }

  @Override
  public final AbstractLayoutCache getLayout() {
    return layout;
  }

  @Override
  public TreeModel getTreeModel() {
    return treeModel;
  }

  @Override
  public ConnectorTableModel getTableModel() {
    return tableModel;
  }

  /**
   * Set up the column according to the table format
   *
   * @param column
   *          the column to be set up
   */
  public void setUpColumn(TableColumn column) {
    int columnIndex = column.getModelIndex() - 1;
    if (columnIndex < 0) {
      return;
    }

    TmmTableFormat tmmTableFormat = tableModel.getTableFormat();
    column.setIdentifier(tmmTableFormat.getColumnIdentifier(columnIndex));

    TableCellRenderer tableCellRenderer = tmmTableFormat.getCellRenderer(columnIndex);
    if (tableCellRenderer != null) {
      column.setCellRenderer(tableCellRenderer);
    }

    ImageIcon headerIcon = tmmTableFormat.getHeaderIcon(columnIndex);
    if (headerIcon != null) {
      column.setHeaderValue(headerIcon);
    }

    if (column.getHeaderRenderer() instanceof DefaultTableCellRenderer) {
      ((DefaultTableCellRenderer) column.getHeaderRenderer()).setToolTipText(tmmTableFormat.getColumnName(columnIndex));
    }

    column.setResizable(tmmTableFormat.getColumnResizeable(columnIndex));
    column.setMinWidth(tmmTableFormat.getMinWidth(columnIndex));
  }
}
