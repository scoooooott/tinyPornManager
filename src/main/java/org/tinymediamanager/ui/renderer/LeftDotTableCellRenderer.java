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

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The class LeftDotTableCellRenderer is used to draw a table column which left side gets truncated if the space for the whole string is too small
 * 
 * @author Manuel Laggner
 */
public class LeftDotTableCellRenderer extends DefaultTableCellRenderer {

  public LeftDotTableCellRenderer() {
    super();
    putClientProperty("clipPosition", SwingConstants.LEFT);
  }
}
