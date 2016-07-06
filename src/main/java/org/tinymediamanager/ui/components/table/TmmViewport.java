package org.tinymediamanager.ui.components.table;

import static org.tinymediamanager.ui.components.table.TmmTable.TABLE_GRID_COLOR;
import static org.tinymediamanager.ui.components.table.TmmTable.TABLE_GRID_COLOR2;

import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

/**
 * The Viewport which is used by the TmmTable
 * 
 * @author Manuel Laggner
 */
class TmmViewport extends JViewport {
  private static final long   serialVersionUID = 3786918873070899884L;

  private final JTable        fTable;
  private final List<Integer> colsWoRightGrid;

  public TmmViewport(JTable table, int[] cols) {
    fTable = table;
    colsWoRightGrid = new ArrayList<>(cols.length);
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
