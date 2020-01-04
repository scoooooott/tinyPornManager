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

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventTableModel;

/**
 * The class TmmTableModel is used as a template for our table models
 *
 * @author Manuel laggner
 */
public class TmmTableModel<E> extends DefaultEventTableModel<E> {
  private TmmTableFormat<? super E> tmmTableFormat;

  public TmmTableModel(EventList<E> source, TmmTableFormat<? super E> tableFormat) {
    super(source, tableFormat);
    tmmTableFormat = tableFormat;
  }

  /**
   * Set up the column according to the table format
   * 
   * @param column
   *          the column to be set up
   */
  public void setUpColumn(TableColumn column) {
    int columnIndex = column.getModelIndex();
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

    if (tmmTableFormat.getMaxWidth(columnIndex) > 0) {
      column.setMaxWidth(tmmTableFormat.getMaxWidth(columnIndex));
    }
  }

  public String getTooltipAt(int row, int column) {
    this.source.getReadWriteLock().readLock().lock();

    String tooltip = null;
    try {
      tooltip = tmmTableFormat.getColumnTooltip((E) this.source.get(row), column);
    }
    catch (Exception e) {
      // IOOB when -1; dunno how i managed this
      return "";
    }
    finally {
      this.source.getReadWriteLock().readLock().unlock();
    }

    return tooltip;
  }
}
