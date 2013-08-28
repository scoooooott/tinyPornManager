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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.PopupMenuEvent;
import javax.swing.tree.TreePath;

import org.tinymediamanager.ui.tvshows.PopupListenerData;

/**
 * The listener interface for receiving popup events. The class that is interested in processing a popup event implements this interface, and the
 * object created with that class is registered with a component using the component's <code>addPopupListener<code> method. When
 * the popup event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see PopupMenuEvent
 */
public class PopupListener extends MouseAdapter {
  private PopupListenerData data = new PopupListenerData();
  private JTree             tree;

  /**
   * Instantiates a new popup listener.
   * 
   * @param popupMenu
   *          the popup menu
   */
  public PopupListener(JPopupMenu popupMenu, JTree tree) {
    data.popup = popupMenu;
    this.tree = tree;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    maybeShowPopup(e);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    // if (table.getSelectedRow() != -1) {
    maybeShowPopup(e);
    // }
  }

  /**
   * Maybe show popup.
   * 
   * @param e
   *          the e
   */
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

      data.popup.show(e.getComponent(), e.getX(), e.getY());
    }
  }
}
