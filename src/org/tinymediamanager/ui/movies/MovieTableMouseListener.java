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
public class MovieTableMouseListener extends MouseAdapter {
  private JPopupMenu popup;
  private JTable     table;

  MovieTableMouseListener(JPopupMenu popupMenu, JTable table) {
    this.popup = popupMenu;
    this.table = table;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() >= 2) {
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
