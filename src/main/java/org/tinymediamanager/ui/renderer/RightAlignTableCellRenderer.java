package org.tinymediamanager.ui.renderer;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * the class {@link RightAlignTableCellRenderer} is used to render table cells right aligned (especially numbers)
 *
 * @author Manuel Laggner
 */
public class RightAlignTableCellRenderer extends DefaultTableCellRenderer {

  public RightAlignTableCellRenderer() {
    setHorizontalAlignment(JLabel.RIGHT);
  }
}
