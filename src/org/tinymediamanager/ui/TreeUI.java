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

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.TreePath;

import com.jtattoo.plaf.BaseTreeUI;

/**
 * The Class TreeUI.
 * 
 * @author Manuel Laggner
 */
public class TreeUI extends BaseTreeUI {

  /** The sf. */
  private RowSelectionListener sf = new RowSelectionListener();

  // private Color backgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicTreeUI#paintRow(java.awt.Graphics, java.awt.Rectangle, java.awt.Insets, java.awt.Rectangle,
   * javax.swing.tree.TreePath, int, boolean, boolean, boolean)
   */
  protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded,
      boolean hasBeenExpanded, boolean isLeaf) {
    // Don't paint the renderer if editing this row.
    if (editingComponent != null && editingRow == row)
      return;

    // if (tree.isRowSelected(row)) {
    // g.setColor(backgroundSelectionColor);
    // g.fillRect(0, bounds.y, tree.getWidth(), bounds.height);
    // }

    super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicTreeUI#installListeners()
   */
  protected void installListeners() {
    super.installListeners();
    tree.addMouseListener(sf);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.plaf.basic.BasicTreeUI#uninstallListeners()
   */
  protected void uninstallListeners() {
    tree.removeMouseListener(sf);
    super.uninstallListeners();
  }

  /**
   * The listener interface for receiving rowSelection events. The class that is interested in processing a rowSelection event implements this
   * interface, and the object created with that class is registered with a component using the component's
   * <code>addRowSelectionListener<code> method. When
   * the rowSelection event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see RowSelectionEvent
   */
  private class RowSelectionListener extends MouseAdapter {

    /**
     * Listener for selecting the entire rows.
     * 
     * @param e
     *          the e
     * @author Kirill Grouchnikov
     */
    @Override
    public void mousePressed(MouseEvent e) {

      if (!tree.isEnabled())
        return;

      TreePath closestPath = tree.getClosestPathForLocation(e.getX(), e.getY());

      if (closestPath == null)
        return;

      Rectangle bounds = tree.getPathBounds(closestPath);
      // Process events outside the immediate bounds -
      // This properly handles Ctrl and Shift
      // selections on trees.
      if ((e.getY() >= bounds.y) && (e.getY() < (bounds.y + bounds.height)) && ((e.getX() < bounds.x) || (e.getX() > (bounds.x + bounds.width)))) {

        // fix - don't select a node if the click was on the
        // expand control
        if (isLocationInExpandControl(closestPath, e.getX(), e.getY())) {
          return;
        }

        selectPathForEvent(closestPath, e);
      }
    }
  }
}
