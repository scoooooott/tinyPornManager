/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * The Class ZebraTable. This JTable displays the content with alternating colors
 * 
 * @author Manuel Laggner
 */
public class ZebraJTable extends JTable {
  private static final long             serialVersionUID = -5461344983450088208L;
  private static final Color            EVEN_ROW_COLOR   = new Color(241, 245, 250);
  private static final Color            TABLE_GRID_COLOR = new Color(0xd9d9d9);
  private static final CellRendererPane CELL_RENDER_PANE = new CellRendererPane();

  private ArrayList<TableColumn>        indexedColumns   = new ArrayList<TableColumn>();
  private Map<Object, TableColumn>      hiddenColumns    = new HashMap<Object, TableColumn>();

  public ZebraJTable() {
    super();
    init();
  }

  public ZebraJTable(TableModel dm) {
    setModel(dm);
    init();
  }

  private void init() {
    setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    setTableHeader(createTableHeader());
    getTableHeader().setReorderingAllowed(false);
    setOpaque(false);

    setNewFontSize(getFont().getSize());
    setGridColor(TABLE_GRID_COLOR);
    setIntercellSpacing(new Dimension(0, 0));
    // turn off grid painting as we'll handle this manually in order to paint
    // grid lines over the entire viewport.
    setShowGrid(false);

    // store columns
    Enumeration<TableColumn> enumeration = columnModel.getColumns();
    while (enumeration.hasMoreElements()) {
      indexedColumns.add((TableColumn) enumeration.nextElement());
    }
  }

  public void setNewFontSize(float size) {
    setFont(getFont().deriveFont(size));
    FontMetrics fm = getFontMetrics(getFont());
    setRowHeight(fm.getHeight() + 4);
  }

  private JTableHeader createTableHeader() {
    return new JTableHeader(getColumnModel()) {
      private static final long serialVersionUID = -7676154270682107643L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // if this JTableHEader is parented in a JViewport, then paint the table header background to the right of the last column if neccessary.
        JViewport viewport = (JViewport) table.getParent();
        if (viewport != null && table.getWidth() < viewport.getWidth()) {
          int x = table.getWidth();
          int width = viewport.getWidth() - table.getWidth();
          paintHeader(g, getTable(), x, width);
        }
      }
    };
  }

  private static void paintHeader(Graphics g, JTable table, int x, int width) {
    TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
    Component component = renderer.getTableCellRendererComponent(table, "", false, false, -1, 2);

    component.setBounds(0, 0, width, table.getTableHeader().getHeight());

    ((JComponent) component).setOpaque(false);
    CELL_RENDER_PANE.paintComponent(g, component, null, x, 0, width, table.getTableHeader().getHeight(), true);
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component component = super.prepareRenderer(renderer, row, column);
    // if the rendere is a JComponent and the given row isn't part of a
    // selection, make the renderer non-opaque so that striped rows show
    // through.
    if (component instanceof JComponent) {
      ((JComponent) component).setOpaque(getSelectionModel().isSelectedIndex(row));
    }
    return component;
  }

  // Stripe painting Viewport. //////////////////////////////////////////////

  public void hideColumn(Object identifier) {
    int index = columnModel.getColumnIndex(identifier);
    TableColumn column = columnModel.getColumn(index);
    if (hiddenColumns.put(identifier, column) != null) {
      throw new IllegalArgumentException("Duplicate column name.");
    }
    columnModel.removeColumn(column);
  }

  public void showColumn(Object identifier) {
    TableColumn tableCloumn = hiddenColumns.remove(identifier);
    if (tableCloumn != null) {
      // find the new index to insert
      int originIndex = indexedColumns.indexOf(tableCloumn);
      int newIndex = 0;
      Enumeration<TableColumn> enumeration = columnModel.getColumns();
      while (enumeration.hasMoreElements()) {
        int index = indexedColumns.indexOf((TableColumn) enumeration.nextElement());
        if (index > originIndex) {
          break;
        }
        newIndex++;
      }

      columnModel.addColumn(tableCloumn);
      int lastColumn = columnModel.getColumnCount() - 1;
      if (newIndex < lastColumn) {
        columnModel.moveColumn(lastColumn, newIndex);
      }
    }
  }

  private static class StripedViewport extends JViewport {
    private static final long serialVersionUID = 7213871940348239879L;
    private final JTable      fTable;

    private StripedViewport(JTable table) {
      fTable = table;
      setOpaque(false);
      initListeners();
    }

    private void initListeners() {
      // install a listener to cause the whole table to repaint when a column is resized. we do this because the extended grid
      // lines may need to be repainted. this could be cleaned up, but for now, it works fine.
      PropertyChangeListener listener = createTableColumnWidthListener();
      for (int i = 0; i < fTable.getColumnModel().getColumnCount(); i++) {
        fTable.getColumnModel().getColumn(i).addPropertyChangeListener(listener);
      }
    }

    private PropertyChangeListener createTableColumnWidthListener() {
      return new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          repaint();
        }
      };
    }

    @Override
    protected void paintComponent(Graphics g) {
      paintStripedBackground(g);
      paintVerticalGridLines(g);
      super.paintComponent(g);
    }

    private void paintStripedBackground(Graphics g) {
      // get position
      Point viewPosition = getViewPosition();
      g.translate(0, -viewPosition.y);

      // get the row index at the top of the clip bounds (the first row to paint).
      int rowAtPoint = fTable.rowAtPoint(g.getClipBounds().getLocation());
      // get the y coordinate of the first row to paint. if there are no rows in the table, start painting at the top of the supplied clipping bounds.
      int topY = rowAtPoint < 0 ? g.getClipBounds().y : fTable.getCellRect(rowAtPoint, 0, true).y;

      // create a counter variable to hold the current row. if there are no rows in the table, start the counter at 0.
      int currentRow = rowAtPoint < 0 ? 0 : rowAtPoint;
      while (topY < g.getClipBounds().y + g.getClipBounds().height) {
        int bottomY = topY + fTable.getRowHeight(currentRow);// fTable.getRowHeight();
        // g.setColor(currentRow % 2 == 0 ? one : two);
        g.setColor(getRowColor(currentRow));
        g.fillRect(g.getClipBounds().x, topY, g.getClipBounds().width, bottomY);
        topY = bottomY;
        currentRow++;
      }

      g.translate(0, viewPosition.y);
    }

    @Override
    public void setViewPosition(Point p) {
      super.setViewPosition(p);
      repaint();
    }

    private Color getRowColor(int row) {
      // return row % 2 == 0 ? EVEN_ROW_COLOR : getBackground();
      return row % 2 == 0 ? EVEN_ROW_COLOR : Color.WHITE;
    }

    private void paintVerticalGridLines(Graphics g) {
      // paint the column grid dividers for the non-existent rows.
      int offset = getViewPosition().x;
      int x = -offset;
      for (int i = 0; i < fTable.getColumnCount(); i++) {
        TableColumn column = fTable.getColumnModel().getColumn(i);
        // increase the x position by the width of the current column.
        x += column.getWidth();

        if (x >= 0) {
          g.setColor(TABLE_GRID_COLOR);
          // draw the grid line (not sure what the -1 is for, but BasicTableUI
          // also does it.
          g.drawLine(x - 1, g.getClipBounds().y, x - 1, getHeight());
        }
      }
    }
  }

  public static JScrollPane createStripedJScrollPane(JTable table) {
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setViewport(new StripedViewport(table));
    scrollPane.getViewport().setView(table);
    // scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createCornerComponent(table));
    // scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    return scrollPane;
  }

  private static JComponent createCornerComponent(final JTable table) {
    return new JComponent() {
      private static final long serialVersionUID = -6612112068796852330L;

      @Override
      protected void paintComponent(Graphics g) {
        paintHeader(g, table, 0, getWidth());
      }
    };
  }
}