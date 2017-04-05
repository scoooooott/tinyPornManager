/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.tinymediamanager.ui.movies.actions.MovieEditAction;

/**
 * The class MovieTableMouseListener - to handle clicks in the movie table
 * 
 * @author Manuel Laggner
 */
@Deprecated
public class MovieTableMouseListener extends MouseAdapter {
  private JPopupMenu popup;
  private JTable     table;

  public MovieTableMouseListener(JPopupMenu popupMenu, JTable table) {
    this.popup = popupMenu;
    this.table = table;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() >= 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
      Action editAction = new MovieEditAction(false);
      editAction.actionPerformed(null);
    }
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
      int row = table.rowAtPoint(e.getPoint());
      int[] selectedRows = table.getSelectedRows();
      for (int selectedRow : selectedRows) {
        if (selectedRow == row) {
          selected = true;
        }
      }

      // if the row, which has been right clicked is not selected - select it
      if (!selected) {
        table.getSelectionModel().setSelectionInterval(row, row);
      }

      popup.show(e.getComponent(), e.getX(), e.getY());
    }
  }
}
