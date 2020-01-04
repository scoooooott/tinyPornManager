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
package org.tinymediamanager.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.PopupMenuEvent;
import javax.swing.tree.TreePath;

/**
 * The listener interface for receiving popup events. The class that is interested in processing a popup event implements this interface, and the
 * object created with that class is registered with a component using the component's <code>addPopupListener<code> method. When the popup event
 * occurs, that object's appropriate method is invoked.
 * 
 * @see PopupMenuEvent
 */
public class TreePopupListener extends MouseAdapter {
  private final JPopupMenu popup;
  private final JTree      tree;

  /**
   * Instantiates a new popup listener.
   * 
   * @param popupMenu
   *          the popup menu
   */
  public TreePopupListener(JPopupMenu popupMenu, JTree tree) {
    this.popup = popupMenu;
    this.tree = tree;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    maybeShowPopup(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    maybeShowPopup(e);
  }

  private void maybeShowPopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      boolean selected = false;
      // check the selected rows
      int row = tree.getClosestRowForLocation(e.getPoint().x, e.getPoint().y);

      TreePath[] paths = tree.getSelectionPaths();

      // filter out all objects from the selection
      if (paths != null) {
        for (TreePath path : paths) {
          if (path.getPathCount() > 1) {
            if (tree.getRowForPath(path) == row) {
              selected = true;
            }
          }
        }
      }

      // if the row, which has been right clicked is not selected - select it
      if (!selected) {
        tree.getSelectionModel().setSelectionPath(tree.getPathForRow(row));
      }

      popup.show(e.getComponent(), e.getX(), e.getY());
    }
  }
}
