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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/*
 *  Class to manage the widths of colunmns in a table.
 *
 *  Various properties control how the width of the column is calculated.
 *  Another property controls whether column width calculation should be dynamic.
 *  Finally, various Actions will be added to the table to allow the user
 *  to customize the functionality.
 *
 *  This class was designed to be used with tables that use an auto resize mode
 *  of AUTO_RESIZE_OFF. With all other modes you are constrained as the width
 *  of the columns must fit inside the table. So if you increase one column, one
 *  or more of the other columns must decrease. Because of this the resize mode
 *  of RESIZE_ALL_COLUMNS will work the best.
 */
/**
 * The Class TableColumnAdjuster.
 * 
 * @author Manuel Laggner
 */
public class TableColumnAdjuster implements PropertyChangeListener, TableModelListener {

  /** The table. */
  private JTable                    table;

  /** The spacing. */
  private int                       spacing;

  /** The is column header included. */
  private boolean                   isColumnHeaderIncluded;

  /** The is column data included. */
  private boolean                   isColumnDataIncluded;

  /** The is only adjust larger. */
  private boolean                   isOnlyAdjustLarger;

  /** The is dynamic adjustment. */
  private boolean                   isDynamicAdjustment;

  /** The column sizes. */
  private Map<TableColumn, Integer> columnSizes = new HashMap<TableColumn, Integer>();

  /*
   * Specify the table and use default spacing
   */
  /**
   * Instantiates a new table column adjuster.
   * 
   * @param table
   *          the table
   */
  public TableColumnAdjuster(JTable table) {
    this(table, 6);
  }

  /*
   * Specify the table and spacing
   */
  /**
   * Instantiates a new table column adjuster.
   * 
   * @param table
   *          the table
   * @param spacing
   *          the spacing
   */
  public TableColumnAdjuster(JTable table, int spacing) {
    this.table = table;
    this.spacing = spacing;
    setColumnHeaderIncluded(true);
    setColumnDataIncluded(true);
    setOnlyAdjustLarger(true);
    setDynamicAdjustment(false);
    installActions();
  }

  /*
   * Adjust the widths of all the columns in the table
   */
  /**
   * Adjust columns.
   */
  public void adjustColumns() {
    TableColumnModel tcm = table.getColumnModel();

    for (int i = 0; i < tcm.getColumnCount(); i++) {
      adjustColumn(i);
    }
  }

  /*
   * Adjust the width of the specified column in the table
   */
  /**
   * Adjust column.
   * 
   * @param column
   *          the column
   */
  public void adjustColumn(final int column) {
    TableColumn tableColumn = table.getColumnModel().getColumn(column);

    if (!tableColumn.getResizable())
      return;

    int columnHeaderWidth = getColumnHeaderWidth(column);
    int columnDataWidth = getColumnDataWidth(column);
    int preferredWidth = Math.max(columnHeaderWidth, columnDataWidth);

    updateTableColumn(column, preferredWidth);
  }

  /*
   * Calculated the width based on the column name
   */
  /**
   * Gets the column header width.
   * 
   * @param column
   *          the column
   * @return the column header width
   */
  private int getColumnHeaderWidth(int column) {
    if (!isColumnHeaderIncluded)
      return 0;

    TableColumn tableColumn = table.getColumnModel().getColumn(column);
    Object value = tableColumn.getHeaderValue();
    TableCellRenderer renderer = tableColumn.getHeaderRenderer();

    if (renderer == null) {
      renderer = table.getTableHeader().getDefaultRenderer();
    }

    Component c = renderer.getTableCellRendererComponent(table, value, false, false, -1, column);
    return c.getPreferredSize().width;
  }

  /*
   * Calculate the width based on the widest cell renderer for the given column.
   */
  /**
   * Gets the column data width.
   * 
   * @param column
   *          the column
   * @return the column data width
   */
  private int getColumnDataWidth(int column) {
    if (!isColumnDataIncluded)
      return 0;

    int preferredWidth = 0;
    int maxWidth = table.getColumnModel().getColumn(column).getMaxWidth();

    for (int row = 0; row < table.getRowCount(); row++) {
      preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, column));

      // We've exceeded the maximum width, no need to check other rows

      if (preferredWidth >= maxWidth)
        break;
    }

    return preferredWidth;
  }

  /*
   * Get the preferred width for the specified cell
   */
  /**
   * Gets the cell data width.
   * 
   * @param row
   *          the row
   * @param column
   *          the column
   * @return the cell data width
   */
  private int getCellDataWidth(int row, int column) {
    // Inovke the renderer for the cell to calculate the preferred width

    TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
    Component c = table.prepareRenderer(cellRenderer, row, column);
    int width = c.getPreferredSize().width + table.getIntercellSpacing().width;

    return width;
  }

  /*
   * Update the TableColumn with the newly calculated width
   */
  /**
   * Update table column.
   * 
   * @param column
   *          the column
   * @param width
   *          the width
   */
  private void updateTableColumn(int column, int width) {
    final TableColumn tableColumn = table.getColumnModel().getColumn(column);

    if (!tableColumn.getResizable())
      return;

    width += spacing;

    // Don't shrink the column width

    if (isOnlyAdjustLarger) {
      width = Math.max(width, tableColumn.getPreferredWidth());
    }

    columnSizes.put(tableColumn, Integer.valueOf(tableColumn.getWidth()));
    table.getTableHeader().setResizingColumn(tableColumn);
    tableColumn.setWidth(width);
  }

  /*
   * Restore the widths of the columns in the table to its previous width
   */
  /**
   * Restore columns.
   */
  public void restoreColumns() {
    TableColumnModel tcm = table.getColumnModel();

    for (int i = 0; i < tcm.getColumnCount(); i++) {
      restoreColumn(i);
    }
  }

  /*
   * Restore the width of the specified column to its previous width
   */
  /**
   * Restore column.
   * 
   * @param column
   *          the column
   */
  private void restoreColumn(int column) {
    TableColumn tableColumn = table.getColumnModel().getColumn(column);
    Integer width = columnSizes.get(tableColumn);

    if (width != null) {
      table.getTableHeader().setResizingColumn(tableColumn);
      tableColumn.setWidth(width.intValue());
    }
  }

  /*
   * Indicates whether to include the header in the width calculation
   */
  /**
   * Sets the column header included.
   * 
   * @param isColumnHeaderIncluded
   *          the new column header included
   */
  public void setColumnHeaderIncluded(boolean isColumnHeaderIncluded) {
    this.isColumnHeaderIncluded = isColumnHeaderIncluded;
  }

  /*
   * Indicates whether to include the model data in the width calculation
   */
  /**
   * Sets the column data included.
   * 
   * @param isColumnDataIncluded
   *          the new column data included
   */
  public void setColumnDataIncluded(boolean isColumnDataIncluded) {
    this.isColumnDataIncluded = isColumnDataIncluded;
  }

  /*
   * Indicates whether columns can only be increased in size
   */
  /**
   * Sets the only adjust larger.
   * 
   * @param isOnlyAdjustLarger
   *          the new only adjust larger
   */
  public void setOnlyAdjustLarger(boolean isOnlyAdjustLarger) {
    this.isOnlyAdjustLarger = isOnlyAdjustLarger;
  }

  /*
   * Indicate whether changes to the model should cause the width to be dynamically recalculated.
   */
  /**
   * Sets the dynamic adjustment.
   * 
   * @param isDynamicAdjustment
   *          the new dynamic adjustment
   */
  public void setDynamicAdjustment(boolean isDynamicAdjustment) {
    // May need to add or remove the TableModelListener when changed

    if (this.isDynamicAdjustment != isDynamicAdjustment) {
      if (isDynamicAdjustment) {
        table.addPropertyChangeListener(this);
        table.getModel().addTableModelListener(this);
      }
      else {
        table.removePropertyChangeListener(this);
        table.getModel().removeTableModelListener(this);
      }
    }

    this.isDynamicAdjustment = isDynamicAdjustment;
  }

  //
  // Implement the PropertyChangeListener
  //
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent )
   */
  public void propertyChange(PropertyChangeEvent e) {
    // When the TableModel changes we need to update the listeners
    // and column widths

    if ("model".equals(e.getPropertyName())) {
      TableModel model = (TableModel) e.getOldValue();
      model.removeTableModelListener(this);

      model = (TableModel) e.getNewValue();
      model.addTableModelListener(this);
      adjustColumns();
    }
  }

  //
  // Implement the TableModelListener
  //
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event. TableModelEvent)
   */
  public void tableChanged(TableModelEvent e) {
    if (!isColumnDataIncluded)
      return;

    // A cell has been updated

    if (e.getType() == TableModelEvent.UPDATE) {
      int column = table.convertColumnIndexToView(e.getColumn());

      // Only need to worry about an increase in width for this cell

      if (isOnlyAdjustLarger) {
        int row = e.getFirstRow();
        TableColumn tableColumn = table.getColumnModel().getColumn(column);

        if (tableColumn.getResizable()) {
          int width = getCellDataWidth(row, column);
          updateTableColumn(column, width);
        }
      }

      // Could be an increase of decrease so check all rows

      else {
        adjustColumn(column);
      }
    }

    // The update affected more than one column so adjust all columns

    else {
      adjustColumns();
    }
  }

  /*
   * Install Actions to give user control of certain functionality.
   */
  /**
   * Install actions.
   */
  private void installActions() {
    installColumnAction(true, true, "adjustColumn", "control ADD");
    installColumnAction(false, true, "adjustColumns", "control shift ADD");
    installColumnAction(true, false, "restoreColumn", "control SUBTRACT");
    installColumnAction(false, false, "restoreColumns", "control shift SUBTRACT");

    installToggleAction(true, false, "toggleDynamic", "control MULTIPLY");
    installToggleAction(false, true, "toggleLarger", "control DIVIDE");
  }

  /*
   * Update the input and action maps with a new ColumnAction
   */
  /**
   * Install column action.
   * 
   * @param isSelectedColumn
   *          the is selected column
   * @param isAdjust
   *          the is adjust
   * @param key
   *          the key
   * @param keyStroke
   *          the key stroke
   */
  private void installColumnAction(boolean isSelectedColumn, boolean isAdjust, String key, String keyStroke) {
    Action action = new ColumnAction(isSelectedColumn, isAdjust);
    KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
    table.getInputMap().put(ks, key);
    table.getActionMap().put(key, action);
  }

  /*
   * Update the input and action maps with new ToggleAction
   */
  /**
   * Install toggle action.
   * 
   * @param isToggleDynamic
   *          the is toggle dynamic
   * @param isToggleLarger
   *          the is toggle larger
   * @param key
   *          the key
   * @param keyStroke
   *          the key stroke
   */
  private void installToggleAction(boolean isToggleDynamic, boolean isToggleLarger, String key, String keyStroke) {
    Action action = new ToggleAction(isToggleDynamic, isToggleLarger);
    KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
    table.getInputMap().put(ks, key);
    table.getActionMap().put(key, action);
  }

  /*
   * Action to adjust or restore the width of a single column or all columns
   */
  /**
   * The Class ColumnAction.
   * 
   * @author Manuel Laggner
   */
  class ColumnAction extends AbstractAction {

    /** The is selected column. */
    private boolean isSelectedColumn;

    /** The is adjust. */
    private boolean isAdjust;

    /**
     * Instantiates a new column action.
     * 
     * @param isSelectedColumn
     *          the is selected column
     * @param isAdjust
     *          the is adjust
     */
    public ColumnAction(boolean isSelectedColumn, boolean isAdjust) {
      this.isSelectedColumn = isSelectedColumn;
      this.isAdjust = isAdjust;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      // Handle selected column(s) width change actions

      if (isSelectedColumn) {
        int[] columns = table.getSelectedColumns();

        for (int i = 0; i < columns.length; i++) {
          if (isAdjust)
            adjustColumn(columns[i]);
          else
            restoreColumn(columns[i]);
        }
      }
      else {
        if (isAdjust)
          adjustColumns();
        else
          restoreColumns();
      }
    }
  }

  /*
   * Toggle properties of the TableColumnAdjuster so the user can customize the functionality to their preferences
   */
  /**
   * The Class ToggleAction.
   * 
   * @author Manuel Laggner
   */
  class ToggleAction extends AbstractAction {

    /** The is toggle dynamic. */
    private boolean isToggleDynamic;

    /** The is toggle larger. */
    private boolean isToggleLarger;

    /**
     * Instantiates a new toggle action.
     * 
     * @param isToggleDynamic
     *          the is toggle dynamic
     * @param isToggleLarger
     *          the is toggle larger
     */
    public ToggleAction(boolean isToggleDynamic, boolean isToggleLarger) {
      this.isToggleDynamic = isToggleDynamic;
      this.isToggleLarger = isToggleLarger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (isToggleDynamic) {
        setDynamicAdjustment(!isDynamicAdjustment);
        return;
      }

      if (isToggleLarger) {
        setOnlyAdjustLarger(!isOnlyAdjustLarger);
        return;
      }
    }
  }
}
