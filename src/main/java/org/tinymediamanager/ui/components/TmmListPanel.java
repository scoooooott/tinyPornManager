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

package org.tinymediamanager.ui.components;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * An abstraction of the JPanel to provide the method setPopupMenu
 * 
 * @author Manuel Laggner
 */
public abstract class TmmListPanel extends JPanel {
  /**
   * set an PopupMenu for the list in this panel
   * 
   * @param popupMenu
   *          the popupmenu to be set
   */
  public abstract void setPopupMenu(JPopupMenu popupMenu);
}
