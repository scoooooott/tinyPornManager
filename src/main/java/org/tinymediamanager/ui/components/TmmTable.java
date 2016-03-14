/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * The Class TmmTable. It's being used to draw the tables like our designer designed it ;)
 * 
 * @author Manuel Laggner
 */
public class TmmTable extends JTable {
  private static final long             serialVersionUID  = 6150939811851709115L;

  public static final Color             TABLE_GRID_COLOR  = new Color(211, 211, 211);
  private static final Color            TABLE_GRID_COLOR2 = new Color(248, 248, 248);

  private static final CellRendererPane CELL_RENDER_PANE  = new CellRendererPane();

  private ArrayList<TableColumn>        indexedColumns    = new ArrayList<TableColumn>();
  private Map<Object, TableColumn>      hiddenColumns     = new HashMap<Object, TableColumn>();

  public TmmTable() {
    super();
    init();
  }

  public TmmTable(TableModel dm) {
    setModel(dm);
    init();
  }

  private void init() {
    setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    setTableHeader(createTableHeader());
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setOpaque(false);
    setOpaque(false);
    setRowHeight(22);
    setGridColor(TABLE_GRID_COLOR);
    setIntercellSpacing(new Dimension(0, 0));
    // turn off grid painting as we'll handle this manually in order to paint grid lines over the entire viewport.
    setShowGrid(false);

    // disable grid in header
    for (int i = 0; i < getColumnCount(); i++) {
      getColumnModel().getColumn(i).setHeaderRenderer(new BottomBorderHeaderRenderer());
    }
  }

  private JTableHeader createTableHeader() {
    return new JTableHeader(getColumnModel()) {
      private static final long serialVersionUID = 1652463935117013248L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // if this JTableHEader is parented in a JViewport, then paint the
        // table header background to the right of the last column if neccessary.
        JViewport viewport = (JViewport) table.getParent();
        if (viewport != null && table.getWidth() < viewport.getWidth()) {
          int x = table.getWidth();
          int width = viewport.getWidth() - table.getWidth();
          paintHeader(g, getTable(), x, width);
        }
      }
    };
  }

  public void setNewFontSize(float size) {
    setFont(getFont().deriveFont(size));
    FontMetrics fm = getFontMetrics(getFont());
    setRowHeight(fm.getHeight() + 4);
  }

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

  private static void paintHeader(Graphics g, JTable table, int x, int width) {
    TableCellRenderer renderer = new BottomBorderHeaderRenderer();
    Component component = renderer.getTableCellRendererComponent(table, "", false, false, -1, 2);

    component.setBounds(0, 0, width, table.getTableHeader().getHeight());

    ((JComponent) component).setOpaque(false);
    CELL_RENDER_PANE.paintComponent(g, component, null, x, 0, width, table.getTableHeader().getHeight(), true);
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component component = super.prepareRenderer(renderer, row, column);
    // if the renderer is a JComponent and the given row isn't part of a
    // selection, make the renderer non-opaque so that striped rows show through.
    if (component instanceof JComponent) {
      ((JComponent) component).setOpaque(getSelectionModel().isSelectedIndex(row));
    }
    return component;
  }

  private static class TmmViewport extends JViewport {
    private static final long   serialVersionUID = 3786918873070899884L;

    private final JTable        fTable;
    private final List<Integer> colsWoRightGrid;

    public TmmViewport(JTable table, int[] cols) {
      fTable = table;
      colsWoRightGrid = new ArrayList<Integer>(cols.length);
      for (int i : cols) {
        colsWoRightGrid.add(new Integer(i));
      }
      fTable.putClientProperty("borderNotToDraw", colsWoRightGrid);
      setOpaque(false);
      initListeners();
    }

    private void initListeners() {
      // install a listener to cause the whole table to repaint when
      // a column is resized. we do this because the extended grid
      // lines may need to be repainted. this could be cleaned up,
      // but for now, it works fine.
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
      paintHorizontalGridLines(g);
      paintVerticalGridLines(g);
      super.paintComponent(g);
    }

    @Override
    public void setViewPosition(Point p) {
      super.setViewPosition(p);
      repaint();
    }

    private void paintVerticalGridLines(Graphics g) {
      int offset = getViewPosition().x;
      int x = -offset;
      for (int i = 0; i < fTable.getColumnCount(); i++) {
        TableColumn column = fTable.getColumnModel().getColumn(i);
        // increase the x position by the width of the current column.
        x += column.getWidth();

        if (colsWoRightGrid.contains(i)) {
          continue;
        }

        if (x >= 0) {
          g.setColor(TABLE_GRID_COLOR);
          // draw the grid line (not sure what the -1 is for, but BasicTableUI
          // also does it.
          g.drawLine(x - 1, g.getClipBounds().y, x - 1, getHeight());
        }
      }
    }

    private void paintHorizontalGridLines(Graphics g) {
      // get position
      Point viewPosition = getViewPosition();
      g.translate(0, -viewPosition.y);

      // get the row index at the top of the clip bounds (the first row
      // to paint).
      int rowAtPoint = fTable.rowAtPoint(g.getClipBounds().getLocation());
      // get the y coordinate of the first row to paint. if there are no
      // rows in the table, start painting at the top of the supplied
      // clipping bounds.
      int topY = rowAtPoint < 0 ? g.getClipBounds().y : fTable.getCellRect(rowAtPoint, 0, true).y;

      // create a counter variable to hold the current row. if there are no
      // rows in the table, start the counter at 0.
      int currentRow = rowAtPoint < 0 ? 0 : rowAtPoint;
      while (topY < g.getClipBounds().y + g.getClipBounds().height) {
        int bottomY = topY + fTable.getRowHeight(currentRow);// fTable.getRowHeight();
        // g.setColor(currentRow % 2 == 0 ? one : two);
        g.setColor(TABLE_GRID_COLOR);
        g.drawLine(g.getClipBounds().x + 5, bottomY - 1, g.getClipBounds().width, bottomY - 1);
        g.setColor(TABLE_GRID_COLOR2);
        g.drawLine(g.getClipBounds().x + 5, bottomY, g.getClipBounds().width, bottomY);
        topY = bottomY;
        currentRow++;
      }

      g.translate(0, viewPosition.y);
    }
  }

  public static JScrollPane createJScrollPane(JTable table, int[] columnsWithoutRightVerticalGrid) {
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setViewport(new TmmViewport(table, columnsWithoutRightVerticalGrid));
    scrollPane.getViewport().setView(table);
    scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_GRID_COLOR));
    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createCornerComponent(table));
    return scrollPane;
  }

  public static JScrollPane createJScrollPane(JTable table) {
    int[] columnsWithoutRightVerticalGrid = {};
    return createJScrollPane(table, columnsWithoutRightVerticalGrid);
  }

  private static JComponent createCornerComponent(final JTable table) {
    return new JComponent() {
      private static final long serialVersionUID = 3350437839386102803L;

      @Override
      protected void paintComponent(Graphics g) {
        paintHeader(g, table, 0, getWidth());
      }
    };
  }

  private static class BottomBorderHeaderRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 7963585655106103415L;

    public BottomBorderHeaderRenderer() {
      setHorizontalAlignment(SwingConstants.CENTER);
      setOpaque(true);

      // This call is needed because DefaultTableCellRenderer calls setBorder()
      // in its constructor, which is executed after updateUI()
      setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_GRID_COLOR));
    }

    @Override
    public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      JTableHeader h = table != null ? table.getTableHeader() : null;

      if (h != null) {
        setEnabled(h.isEnabled());
        setComponentOrientation(h.getComponentOrientation());

        setForeground(h.getForeground());
        setBackground(h.getBackground());
        setFont(h.getFont());
      }
      else {
        /* Use sensible values instead of random leftover values from the last call */
        setEnabled(true);
        setComponentOrientation(ComponentOrientation.UNKNOWN);

        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setFont(UIManager.getFont("TableHeader.font"));
      }

      if (value instanceof ImageIcon) {
        setIcon((ImageIcon) value);
        setText("");
      }
      else {
        setText((value == null) ? "" : value.toString());
        setIcon(null);
        setHorizontalAlignment(JLabel.CENTER);
      }

      return this;
    }
  }
}