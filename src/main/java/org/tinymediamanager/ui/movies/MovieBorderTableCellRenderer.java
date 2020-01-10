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

package org.tinymediamanager.ui.movies;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.renderer.BorderTableCellRenderer;

/**
 * create the movie CellRenderer with the default inset (2 px left)
 */
public class MovieBorderTableCellRenderer extends BorderTableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String text = "";
    if (value instanceof Movie) {
      Movie movie = (Movie) value;

      // get the column
      TableColumn tableColumn = table.getColumnModel().getColumn(column);
      if ("originalTitle".equals(tableColumn.getIdentifier())) {
        text = movie.getOriginalTitleSortable();
      }
      else {
        text = movie.getTitleSortable();
      }

      if (movie.isNewlyAdded()) {
        setHorizontalTextPosition(SwingConstants.LEADING);
        setIconTextGap(10);
        setIcon(IconManager.NEW);
      }
      else {
        setIcon(null);
      }

      return super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
    }
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}
