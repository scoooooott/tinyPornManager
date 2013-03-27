/**
 * 
 */
package org.tinymediamanager.ui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The Class BorderCellRenderer.
 * 
 * @author Manuel Laggner
 */
public class BorderCellRenderer extends DefaultTableCellRenderer {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
   * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setForeground(table.getForeground());
    if (isSelected) {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    }
    else {
      setBackground(table.getBackground());

    }

    // left margin
    Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    Border defaultBorder = ((JComponent) comp).getBorder();
    defaultBorder = BorderFactory.createEmptyBorder(0, 2, 0, 0);
    this.setBorder(defaultBorder);

    setValue(value.toString());
    return this;
  }
}
