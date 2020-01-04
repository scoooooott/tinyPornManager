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

package org.tinymediamanager.ui.components.treetable;

import javax.swing.ImageIcon;

import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * The abstract TmmTableFormat is a convenience wrapper for the {@link TmmTableFormat} enhanced by the tri-state icon
 *
 * @author Manuel Laggner
 */
public abstract class TmmTreeTableFormat<E> extends TmmTableFormat<E> {
  protected enum TRI_STATE {
    ALL_OK,
    TOP_OK,
    BOTTOM_OK,
    NOTHING_OK;

    /**
     * returns the right state for the combination of the two given booleans
     * 
     * @param top
     *          the top value
     * @param bottom
     *          the bottom value
     * @return the right TRI_STATE
     */
    public static TRI_STATE getState(boolean top, boolean bottom) {
      if (top && bottom) {
        return ALL_OK;
      }
      if (top && !bottom) {
        return TOP_OK;
      }
      if (!top && bottom) {
        return BOTTOM_OK;
      }
      return NOTHING_OK;
    }
  }

  protected ImageIcon getTriStateIcon(TRI_STATE state) {
    switch (state) {
      case ALL_OK:
        return IconManager.TABLE_OK;

      case TOP_OK:
        return IconManager.TABLE_PROBLEM;

      case NOTHING_OK:
      case BOTTOM_OK:
        return IconManager.TABLE_NOT_OK;
    }
    return null;
  }
}
