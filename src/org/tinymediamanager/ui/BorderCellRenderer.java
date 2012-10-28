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
 * @author manuel
 * 
 */
public class BorderCellRenderer extends DefaultTableCellRenderer {

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
