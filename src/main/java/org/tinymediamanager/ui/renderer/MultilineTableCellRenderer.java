/*
 * Copyright 2012 - 2020 Manuel Laggner
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

package org.tinymediamanager.ui.renderer;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * the class {@link MultilineTableCellRenderer} is used to render table cells with multiple lines; ATTENTION multiple lines will always be rendered on
 * top and not centered
 * 
 * @author Manuel Laggner
 */
public class MultilineTableCellRenderer extends JTextArea implements TableCellRenderer {

  private static final Border SAFE_NO_FOCUS_BORDER    = new EmptyBorder(1, 1, 1, 1);
  private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
  protected static Border     noFocusBorder           = DEFAULT_NO_FOCUS_BORDER;

  public MultilineTableCellRenderer() {
    this(false);
  }

  public MultilineTableCellRenderer(boolean withWordWrap) {
    setForeground(null);
    setBackground(null);
    setOpaque(false);
    setBorder(getNoFocusBorder());
    setAlignmentY(CENTER_ALIGNMENT);
    if (withWordWrap) {
      setLineWrap(true);
      setWrapStyleWord(true);
    }
  }

  private Border getNoFocusBorder() {
    Border border = UIManager.getBorder("Table.cellNoFocusBorder");
    if (System.getSecurityManager() != null) {
      if (border != null)
        return border;
      return SAFE_NO_FOCUS_BORDER;
    }
    else if (border != null && (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
      return border;
    }
    return noFocusBorder;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    }
    else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setFont(table.getFont());
    if (hasFocus) {
      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
      if (table.isCellEditable(row, column)) {
        setForeground(UIManager.getColor("Table.focusCellForeground"));
        setBackground(UIManager.getColor("Table.focusCellBackground"));
      }
    }
    else {
      setBorder(new EmptyBorder(1, 2, 1, 2));
    }
    setText((value == null) ? "" : value.toString());

    return this;
  }
}
