/*
 * Copyright 2012 Manuel Laggner
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
 * The Class MyTable.
 * 
 * @author Manuel Laggner
 */
public class MyTable extends JTable {

  /** The Constant serialVersionUID. */
  private static final long             serialVersionUID = 1L;

  /** The Constant EVEN_ROW_COLOR. */
  private static final Color            EVEN_ROW_COLOR   = new Color(241, 245, 250);

  /** The Constant TABLE_GRID_COLOR. */
  private static final Color            TABLE_GRID_COLOR = new Color(0xd9d9d9);

  /** The Constant CELL_RENDER_PANE. */
  private static final CellRendererPane CELL_RENDER_PANE = new CellRendererPane();

  /**
   * Instantiates a new my table.
   */
  public MyTable() {
    super();
    init();
  }

  /**
   * Instantiates a new my table.
   * 
   * @param dm
   *          the dm
   */
  public MyTable(TableModel dm) {
    super(dm);
    init();
  }

  /**
   * Inits the.
   */
  private void init() {
    // setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    setTableHeader(createTableHeader());
    getTableHeader().setReorderingAllowed(false);
    setOpaque(false);
    setRowHeight(16);
    setGridColor(TABLE_GRID_COLOR);
    setIntercellSpacing(new Dimension(0, 0));
    // turn off grid painting as we'll handle this manually in order to paint
    // grid lines over the entire viewport.
    setShowGrid(false);
    // setPopupUsedFromTheCorner(true);
  }

  /**
   * Creates a JTableHeader that paints the table header background to the right of the right-most column if neccesasry.
   * 
   * @return the j table header
   */
  private JTableHeader createTableHeader() {
    return new JTableHeader(getColumnModel()) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // if this JTableHEader is parented in a JViewport, then paint the
        // table header background to the right of the last column if
        // neccessary.
        JViewport viewport = (JViewport) table.getParent();
        if (viewport != null && table.getWidth() < viewport.getWidth()) {
          int x = table.getWidth();
          int width = viewport.getWidth() - table.getWidth();
          paintHeader(g, getTable(), x, width);
        }
      }
    };
  }

  /**
   * Paints the given JTable's table default header background at given x for the given width.
   * 
   * @param g
   *          the g
   * @param table
   *          the table
   * @param x
   *          the x
   * @param width
   *          the width
   */
  private static void paintHeader(Graphics g, JTable table, int x, int width) {
    TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
    Component component = renderer.getTableCellRendererComponent(table, "", false, false, -1, 2);

    component.setBounds(0, 0, width, table.getTableHeader().getHeight());

    ((JComponent) component).setOpaque(false);
    CELL_RENDER_PANE.paintComponent(g, component, null, x, 0, width, table.getTableHeader().getHeight(), true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JTable#prepareRenderer(javax.swing.table.TableCellRenderer, int, int)
   */
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

  /**
   * Creates a JViewport that draws a striped backgroud corresponding to the row positions of the given JTable.
   * 
   * @author Manuel Laggner
   */
  private static class StripedViewport extends JViewport {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The table. */
    private final JTable      fTable;

    /**
     * Instantiates a new striped viewport.
     * 
     * @param table
     *          the table
     */
    public StripedViewport(JTable table) {
      fTable = table;
      setOpaque(false);
      initListeners();
    }

    /**
     * Inits the listeners.
     */
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

    /**
     * Creates the table column width listener.
     * 
     * @return the property change listener
     */
    private PropertyChangeListener createTableColumnWidthListener() {
      return new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          repaint();
        }
      };
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
      paintStripedBackground(g);
      paintVerticalGridLines(g);
      super.paintComponent(g);
    }

    /**
     * Paint striped background.
     * 
     * @param g
     *          the g
     */
    private void paintStripedBackground(Graphics g) {
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
        int bottomY = topY + fTable.getRowHeight();
        // g.setColor(currentRow % 2 == 0 ? one : two);
        g.setColor(getRowColor(currentRow));
        g.fillRect(g.getClipBounds().x, topY, g.getClipBounds().width, bottomY);
        topY = bottomY;
        currentRow++;
      }

      g.translate(0, viewPosition.y);

      // // create a counter variable to hold the current row. if there are no
      // // rows in the table, start the counter at 0.
      // int currentRow = rowAtPoint < 0 ? 0 : rowAtPoint;
      // while (topY < g.getClipBounds().y + g.getClipBounds().height) {
      // int bottomY = topY + fTable.getRowHeight();
      // g.setColor(getRowColor(currentRow));
      // g.fillRect(g.getClipBounds().x, topY, g.getClipBounds().width,
      // bottomY);
      // topY = bottomY;
      // currentRow++;
      // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JViewport#setViewPosition(java.awt.Point)
     */
    @Override
    public void setViewPosition(Point p) {
      super.setViewPosition(p);
      repaint();
    }

    /**
     * Gets the row color.
     * 
     * @param row
     *          the row
     * @return the row color
     */
    private Color getRowColor(int row) {
      // return row % 2 == 0 ? EVEN_ROW_COLOR : getBackground();
      return row % 2 == 0 ? EVEN_ROW_COLOR : Color.WHITE;
    }

    /**
     * Paint vertical grid lines.
     * 
     * @param g
     *          the g
     */
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

  /**
   * Creates the striped j scroll pane.
   * 
   * @param table
   *          the table
   * @return the j scroll pane
   */
  public static JScrollPane createStripedJScrollPane(JTable table) {
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setViewport(new StripedViewport(table));
    scrollPane.getViewport().setView(table);
    // scrollPane.setBorder(BorderFactory.createEmptyBorder());
    // scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createCornerComponent(table));
    // scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    return scrollPane;
  }

  /**
   * Creates a component that paints the header background for use in a JScrollPane corner.
   * 
   * @param table
   *          the table
   * @return the j component
   */
  private static JComponent createCornerComponent(final JTable table) {
    return new JComponent() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        paintHeader(g, table, 0, getWidth());
      }
    };
  }
}