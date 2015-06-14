/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class IconRenderer.
 * 
 * @author Manuel Laggner
 */
public class IconRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 400599451709865596L;

  private String            tooltip;

  /**
   * Instantiates a new icon renderer.
   * 
   * @param tooltip
   *          the tooltip
   */
  public IconRenderer(String tooltip) {
    this.tooltip = tooltip;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof ImageIcon) {
      setIcon((ImageIcon) value);
    }
    else {
      setText((value == null) ? "" : value.toString());
      setIcon(null);
    }
    setHorizontalAlignment(JLabel.CENTER);

    // set a tooltip
    if (StringUtils.isNotBlank(tooltip)) {
      setToolTipText(tooltip);
    }

    return this;
  }
}
