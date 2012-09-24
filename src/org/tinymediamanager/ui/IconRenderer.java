package org.tinymediamanager.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class IconRenderer extends DefaultTableCellRenderer {
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    // if (table != null) {
    // JTableHeader header = table.getTableHeader();
    // if (header != null) {
    // setForeground(header.getForeground());
    // setBackground(header.getBackground());
    // setFont(header.getFont());
    // }
    // }
    if (value instanceof ImageIcon) {
      setIcon((ImageIcon) value);
    }
    else {
      setText((value == null) ? "" : value.toString());
      setIcon(null);
    }
    // setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    setHorizontalAlignment(JLabel.CENTER);
    return this;
  }

}
