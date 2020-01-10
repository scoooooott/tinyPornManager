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

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JViewport;

/**
 * The Viewport which is used by the TmmTable
 * 
 * @author Manuel Laggner
 */
class TmmViewport extends JViewport {
  private static final long   serialVersionUID = 3786918873070899884L;

  private final JTable        fTable;
  private final List<Integer> colsWoRightGrid;

  TmmViewport(JTable table, int[] cols) {
    fTable = table;
    colsWoRightGrid = new ArrayList<>(cols.length);
    for (int i : cols) {
      colsWoRightGrid.add(i);
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
    return evt -> repaint();
  }

  @Override
  public void setViewPosition(Point p) {
    super.setViewPosition(p);
    repaint();
  }
}
