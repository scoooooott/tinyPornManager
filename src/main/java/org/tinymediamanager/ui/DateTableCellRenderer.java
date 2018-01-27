/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This renderer is used to display Dates in a customizeable way
 * 
 * @author Manuel Laggner
 */
public class DateTableCellRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 2136302874452711571L;

  private DateFormat        dateFormat       = null;

  /**
   * Create a new DateTableCellRenderer that renders Dates as formatted Strings.
   *
   * @param DateFormat
   *          a String specifying how to format the date, in the format specified by {@link DateFormat}.
   */
  public DateTableCellRenderer(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }

  /**
   * Returns the component used for drawing the cell.
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
    if (value != null) {
      String prettyDate = dateFormat.format((Date) value);
      setText(prettyDate);
    }
    else {
      setText("");
    }
    return this;
  }
}
