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
package org.tinymediamanager.ui.components.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * The column model for the TmmTable
 *
 * @author Manuel Laggner
 */
public class TmmTableColumnModel extends DefaultTableColumnModel {

  /**
   * List<TableColumn>: holds list of columns that were hidden by the user. The columns contained here are not contained in the inherited tableColumns
   * list.
   */
  protected List<TableColumn> hiddenColumns         = new ArrayList<>();

  List<Integer>               hiddenColumnsPosition = new ArrayList<>();

  /**
   * Copy of addColumn(TableColumn) with an index specifying where to add the new column
   */
  private void addColumn(TableColumn aColumn, int index) {
    if (aColumn == null) {
      throw new IllegalArgumentException("Object is null");
    }

    tableColumns.insertElementAt(aColumn, index);
    aColumn.addPropertyChangeListener(this);
    totalColumnWidth = -1;

    // Post columnAdded event notification
    fireColumnAdded(new TableColumnModelEvent(this, 0, index));
  }

  @Override
  public void moveColumn(int ci1, int ci2) {
    super.moveColumn(ci1, ci2);
    // hidden positions between ci1 and ci2 need to be adjusted:
    int n = hiddenColumns.size();
    // Shift ci1 and ci2 by hidden columns:
    for (int i = 0; i < n; i++) {
      int index = hiddenColumnsPosition.get(i);
      if (ci1 >= index) {
        ci1++;
      }
      if (ci2 >= index) {
        ci2++;
      }
    }
    if (ci1 < ci2) {
      for (int i = 0; i < n; i++) {
        int index = hiddenColumnsPosition.get(i);
        if (ci1 < index && index <= ci2) {
          hiddenColumnsPosition.set(i, --index);
        }
      }
    }
    if (ci2 < ci1) {
      for (int i = 0; i < n; i++) {
        int index = hiddenColumnsPosition.get(i);
        if (ci2 <= index && index < ci1) {
          hiddenColumnsPosition.set(i, ++index);
        }
      }
    }
  }

  @Override
  public void removeColumn(TableColumn column) {
    int columnIndex = removeColumn(column, true);

    // Post columnAdded event notification. (JTable and JTableHeader
    // listens so they can adjust size and redraw)
    if (columnIndex != -1) {
      fireColumnRemoved(new TableColumnModelEvent(this, columnIndex, 0));
    }
  }

  private int removeColumn(TableColumn column, boolean doShift) {
    if (removeHiddenColumn(column, doShift) < 0) {
      int origColumnIndex = tableColumns.indexOf(column);
      int columnIndex = origColumnIndex;
      removeColumnOrig(column);

      if (doShift) {
        int n = hiddenColumnsPosition.size();
        for (Integer aHiddenColumnsPosition : hiddenColumnsPosition) {
          if (aHiddenColumnsPosition <= columnIndex) {
            columnIndex++;
          }
        }
        for (int i = 0; i < n; i++) {
          int index = hiddenColumnsPosition.get(i);
          if (index > columnIndex) {
            hiddenColumnsPosition.set(i, --index);
          }
        }
      }
      return origColumnIndex;
    }
    return -1;
  }

  public void removeColumnOrig(TableColumn column) {
    int columnIndex = tableColumns.indexOf(column);

    if (columnIndex != -1) {
      // Adjust for the selection
      if (selectionModel != null) {
        selectionModel.removeIndexInterval(columnIndex, columnIndex);
      }

      column.removePropertyChangeListener(this);
      tableColumns.removeElementAt(columnIndex);
      totalColumnWidth = -1;
    }
  }

  /**
   * @return the column position of the hidden column or -1 if the column is not hidden
   */
  private int removeHiddenColumn(TableColumn column, boolean doShift) {
    int hiddenIndex = -1;
    for (int i = 0; i < hiddenColumns.size(); i++) {
      if (column.equals(hiddenColumns.get(i))) {
        hiddenIndex = i;
        break;
      }
    }
    if (hiddenIndex >= 0) {
      hiddenColumns.remove(hiddenIndex);
      int hi = hiddenColumnsPosition.remove(hiddenIndex);
      if (doShift) {
        int n = hiddenColumnsPosition.size();
        for (int i = 0; i < n; i++) {
          int index = hiddenColumnsPosition.get(i);
          if (index > hi) {
            hiddenColumnsPosition.set(i, --index);
          }
        }
      }
      return hi;
    }
    else {
      return -1;
    }
  }

  /**
   * Makes the given column hidden or visible according to the parameter hidden.
   *
   * @param column
   *          The table column to change the visibility.
   * @param hidden
   *          <code>true</code> to make the column hidden, <code>false</code> to make it visible.
   */
  public void setColumnHidden(TableColumn column, boolean hidden) {
    if (hidden) {
      if (!hiddenColumns.contains(column)) {
        int columnIndex = tableColumns.indexOf(column);
        int index = columnIndex;

        if (index >= 0) {
          removeColumn(column, false);
          hiddenColumns.add(column);
          for (Integer pos : hiddenColumnsPosition) {
            if (pos <= index) {
              index++;
            }
          }
          while (hiddenColumnsPosition.contains(index)) {
            index++;
          }
          hiddenColumnsPosition.add(index);

          // Post columnAdded event notification. (JTable and JTableHeader
          // listens so they can adjust size and redraw)
          fireColumnRemoved(new TableColumnModelEvent(this, columnIndex, 0));
        }
      }
    }
    else {
      if (!tableColumns.contains(column)) {
        int index = removeHiddenColumn(column, false);
        if (index >= 0) {
          int i = index;
          for (Integer pos : hiddenColumnsPosition) {
            if (pos < index) {
              i--;
            }
          }
          index = Math.min(i, tableColumns.size());
          addColumn(column, index);
        }
      }
    }
  }

  /**
   * Get all columns, including hidden ones.
   */
  List<TableColumn> getAllColumns() {
    List<TableColumn> columns = Collections.list(getColumns());
    int n = hiddenColumns.size();
    for (int i = 0; i < n; i++) {
      int index = hiddenColumnsPosition.get(i);
      index = Math.min(index, columns.size());
      columns.add(index, hiddenColumns.get(i));
    }
    return columns;
  }

  /**
   * Test if the column is hidden or visible.
   *
   * @param tc
   *          The table column to test
   * @return <code>true</code> when the column is hidden, <code>false</code> when it's visible.
   */
  public boolean isColumnHidden(TableColumn tc) {
    return hiddenColumns.contains(tc);
  }

  public void setHiddenColumns(List<String> columnIdentifiers) {
    for (TableColumn col : getAllColumns()) {
      if (columnIdentifiers.contains(col.getIdentifier())) {
        setColumnHidden(col, true);
      }
    }
  }

  public List<TableColumn> getHiddenColumns() {
    return hiddenColumns;
  }

}
