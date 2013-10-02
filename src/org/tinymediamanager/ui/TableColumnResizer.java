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
package org.tinymediamanager.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * The class TableColumnResizer used to resize table columns
 * 
 * @author Manuel Laggner
 */
public class TableColumnResizer {
  public static void adjustColumnPreferredWidths(JTable table) {
    adjustColumnPreferredWidths(table, 0);
  }

  public static void adjustColumnPreferredWidths(JTable table, int margin) {
    // strategy - get max width for cells in header and column and
    // make that the preferred width
    TableColumnModel columnModel = table.getColumnModel();
    for (int col = 0; col < table.getColumnCount(); col++) {

      int maxwidth = 0;
      // header
      TableCellRenderer rend = columnModel.getColumn(col).getHeaderRenderer();
      Object value = columnModel.getColumn(col).getHeaderValue();
      if (rend == null) {
        rend = table.getTableHeader().getDefaultRenderer();
      }
      Component comp = rend.getTableCellRendererComponent(table, value, false, false, -1, col);
      maxwidth = Math.max(comp.getPreferredSize().width + margin, maxwidth);

      // columns
      for (int row = 0; row < table.getRowCount(); row++) {
        rend = table.getCellRenderer(row, col);
        value = table.getValueAt(row, col);
        comp = rend.getTableCellRendererComponent(table, value, false, false, row, col);
        maxwidth = Math.max(comp.getPreferredSize().width + margin, maxwidth);
      }

      TableColumn column = columnModel.getColumn(col);
      column.setPreferredWidth(maxwidth);
    }
  }
}
