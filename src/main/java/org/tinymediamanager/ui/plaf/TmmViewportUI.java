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
package org.tinymediamanager.ui.plaf;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ViewportUI;
import javax.swing.plaf.basic.BasicViewportUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.jtattoo.plaf.AbstractLookAndFeel;

public class TmmViewportUI extends BasicViewportUI {
  // Shared UI object
  private static ViewportUI viewportUI;

  public static ComponentUI createUI(JComponent c) {
    if (viewportUI == null) {
      viewportUI = new TmmViewportUI();
    }
    return viewportUI;
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    paintVerticalGridLines(g, c);
    paintHorizontalGridLines(g, c);
    super.paint(g, c);
  }

  private void paintVerticalGridLines(Graphics g, JComponent c) {
    JViewport viewport = (JViewport) c;
    int offset = viewport.getViewPosition().x;

    JTable fTable = null;

    // paint vertical grid lines for all tables except the TmmTable
    if (viewport.getView() instanceof JTable) {
      fTable = (JTable) ((JViewport) c).getView();
    }

    if (viewport.getView() instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) viewport.getView();
      fTable = header.getTable();
    }

    if (fTable != null) {
      int drawColumnCountOffset = 1;
      if ("RoundScrollPane".equals(viewport.getParent().getClass().getSimpleName())) {
        JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        if (scrollPane.getVerticalScrollBar().isShowing()) {
          drawColumnCountOffset = 0;
        }
      }

      ArrayList<Integer> colsWoRightGrid = new ArrayList<>();
      if (fTable.getClientProperty("borderNotToDraw") != null) {
        colsWoRightGrid = (ArrayList<Integer>) fTable.getClientProperty("borderNotToDraw");
      }

      int x = -offset;
      for (int i = 0; i < fTable.getColumnCount() - drawColumnCountOffset; i++) {
        TableColumn column = fTable.getColumnModel().getColumn(i);
        // increase the x position by the width of the current column.
        x += column.getWidth();

        if (colsWoRightGrid.contains(i)) {
          continue;
        }

        if (x >= 0) {
          g.setColor(AbstractLookAndFeel.getTheme().getGridColors()[0]);
          // draw the grid line (not sure what the -1 is for, but BasicTableUI also does it.
          g.drawLine(x - 1, g.getClipBounds().y, x - 1, viewport.getHeight());
        }
      }
    }
  }

  private void paintHorizontalGridLines(Graphics g, JComponent c) {
    JViewport viewport = (JViewport) c;
    int offset = viewport.getViewPosition().x;

    JTable fTable = null;

    // paint vertical grid lines for all tables except the TmmTable
    if (viewport.getView() instanceof JTable) {
      fTable = (JTable) ((JViewport) c).getView();
    }

    if (viewport.getView() instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) viewport.getView();
      fTable = header.getTable();
    }
    if (fTable != null) {
      // get position
      Point viewPosition = viewport.getViewPosition();
      g.translate(0, -viewPosition.y);

      // get the row index at the top of the clip bounds (the first row to paint).
      int rowAtPoint = fTable.rowAtPoint(g.getClipBounds().getLocation());
      // get the y coordinate of the first row to paint. if there are no
      // rows in the table, start painting at the top of the supplied clipping bounds.
      int topY = rowAtPoint < 0 ? g.getClipBounds().y : fTable.getCellRect(rowAtPoint, 0, true).y;

      // create a counter variable to hold the current row. if there are no
      // rows in the table, start the counter at 0.
      int currentRow = rowAtPoint < 0 ? 0 : rowAtPoint;
      while (topY < g.getClipBounds().y + g.getClipBounds().height) {
        int bottomY = topY + fTable.getRowHeight(currentRow);
        g.setColor(AbstractLookAndFeel.getTheme().getGridColors()[0]);
        g.drawLine(g.getClipBounds().x + 5, bottomY - 1, g.getClipBounds().width, bottomY - 1);
        g.setColor(AbstractLookAndFeel.getTheme().getGridColors()[1]);
        g.drawLine(g.getClipBounds().x + 5, bottomY, g.getClipBounds().width, bottomY);
        topY = bottomY;
        currentRow++;
      }

      g.translate(0, viewPosition.y);
    }
  }
}
