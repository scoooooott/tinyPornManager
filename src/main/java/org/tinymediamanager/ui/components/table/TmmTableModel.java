package org.tinymediamanager.ui.components.table;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventTableModel;

/**
 * Created by manuel on 28.06.16.
 */
public class TmmTableModel<E> extends DefaultEventTableModel {
  private TmmTableFormat<? super E> tmmTableFormat;

  public TmmTableModel(EventList source, TmmTableFormat tableFormat) {
    super(source, tableFormat);
    tmmTableFormat = tableFormat;
  }

  public String getColumnIdentifier(int column) {
    return tmmTableFormat.getColumnIdentifier(column);
  }
}
