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

import java.awt.Color;

import javax.swing.Icon;

/**
 * This interface provides data for the tree portion of the TreeTable
 *
 * @author Manuel Laggner
 */
public interface TmmTreeTableRenderDataProvider {
  /**
   * Convert an object in the tree to the string that should be used to display its node
   * 
   * @param cellData
   *          the cell data to get the display name for
   */
  String getDisplayName(Object cellData);

  /**
   * Get the foreground color to be used for rendering this node. Return null if the standard table foreground or selected foreground should be used.
   * 
   * @param cellData
   *          the cell data to get the foreground color for
   */
  Color getForeground(Object cellData);

  /**
   * Get a tooltip text for this object
   * 
   * @param cellData
   *          the cell data to get the tooltip text for
   */
  String getTooltipText(Object cellData);

  /**
   * Get an icon to be used for this object. Return null if the look and feel's default tree folder/leaf icons should be used as appropriate.
   *
   * @param cellData
   *          the cell data to get the icon for
   */
  Icon getIcon(Object cellData);
}
