package org.tinymediamanager.ui.movies;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * The class MovieTablePopupListener - to handle popups in the movie table
 * 
 * @author Manuel Laggner
 */
public class MovieTablePopupListener extends MouseAdapter {
  private JPopupMenu popup;
  private JTable     table;

  MovieTablePopupListener(JPopupMenu popupMenu, JTable table) {
    this.popup = popupMenu;
    this.table = table;
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
