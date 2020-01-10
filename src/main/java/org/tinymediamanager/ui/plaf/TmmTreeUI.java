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
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.tree.TreePath;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseTreeUI;

/**
 * The class TmmTreeUI. Render the JTree nicely
 *
 * @author Manuel Laggner
 */
public class TmmTreeUI extends BaseTreeUI {

  private RowSelectionListener sf = new RowSelectionListener();

  public static ComponentUI createUI(JComponent c) {
    return new TmmTreeUI();
  }

  public TmmTreeUI() {
  }

  @Override
  public void paint(Graphics g, JComponent c) {

    final Insets insets = tree.getInsets();
    final int w = tree.getWidth() - insets.left - insets.right;
    final int h = tree.getHeight() - insets.top - insets.bottom;
    final int x = insets.left;
    int y = insets.top;

    // paint row background across the whole tree
    final int nItems = tree.getRowCount();
    int rowHeight = 17; // A default for empty trees
    for (int i = 0; i < nItems; i++, y += rowHeight) {
      Rectangle rect = tree.getRowBounds(i);
      rowHeight = rect != null ? rect.height : rowHeight;
      g.setColor(getSelectionModel().isRowSelected(i) ? AbstractLookAndFeel.getSelectionBackgroundColor() : AbstractLookAndFeel.getBackgroundColor());
      g.fillRect(x, y, w, rowHeight);
    }

    final int remainder = insets.top + h - y;
    if (remainder > 0) {
      g.setColor(AbstractLookAndFeel.getBackgroundColor());
      g.fillRect(x, y, w, remainder);
    }

    tree.setOpaque(false);
    super.paint(g, c);
    tree.setOpaque(true);
  }

  /*
   * expand the tree background to the whole tree width
   */
  @Override
  protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded,
      boolean hasBeenExpanded, boolean isLeaf) {
    if (editingComponent != null && editingRow == row) {
      return;
    }

    bounds.width = tree.getWidth() - bounds.x;
    super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
  }

  @Override
  protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
    // we do not need any tree lines
  }

  @Override
  protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
    // we do not need any tree lines
  }

  @Override
  protected void installListeners() {
    super.installListeners();
    tree.addMouseListener(sf);
  }

  @Override
  protected void uninstallListeners() {
    tree.removeMouseListener(sf);
    super.uninstallListeners();
  }

  /**
   * The listener interface for receiving rowSelection events. The class that is interested in processing a rowSelection event implements this
   * interface, and the object created with that class is registered with a component using the component's <code>addRowSelectionListener <code>
   * method. When the rowSelection event occurs, that object's appropriate method is invoked.
   */
  private class RowSelectionListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      if (!tree.isEnabled()) {
        return;
      }

      TreePath closestPath = tree.getClosestPathForLocation(e.getX(), e.getY());
      if (closestPath == null) {
        return;
      }

      Rectangle bounds = tree.getPathBounds(closestPath);
      // Process events outside the immediate bounds - This properly handles Ctrl and Shift
      // selections on trees.
      if ((e.getY() >= bounds.y) && (e.getY() < (bounds.y + bounds.height)) && ((e.getX() < bounds.x) || (e.getX() > (bounds.x + bounds.width)))) {
        // fix - don't select a node if the click was on the expand control
        if (isLocationInExpandControl(closestPath, e.getX(), e.getY())) {
          return;
        }

        selectPathForEvent(closestPath, e);
      }
    }
  }
}
