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
package org.tinymediamanager.ui;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tinymediamanager.ui.settings.TmmSettingsNode;

/**
 * Interface ITmmUIModule to access UI Modules easily
 * 
 * @author Manuel Laggner
 */
public interface ITmmUIModule {

  /**
   * get the module id
   * 
   * @return the module id
   */
  String getModuleId();

  /**
   * get the tab panel (left panel in the main view)
   * 
   * @return the tab panel
   */
  JPanel getTabPanel();

  /**
   * get the tab title
   * 
   * @return the tab title
   */
  String getTabTitle();

  /**
   * get the detail panel (right panel in the main view)
   * 
   * @return the detail panel
   */
  JPanel getDetailPanel();

  /**
   * get the search action (which will be triggered when pressing the search button in the toolbar)
   * 
   * @return the search action
   */
  Action getSearchAction();

  /**
   * get the icon to be used with the update button
   * 
   * @return the icon
   */
  Icon getSearchButtonIcon();

  /**
   * get the hover icon to be used with the update button
   * 
   * @return the hover icon
   */
  Icon getSearchButtonHoverIcon();

  /**
   * get the search popup menu (which will be shown when clicking on the search text)
   * 
   * @return the search popup menu
   */
  JPopupMenu getSearchMenu();

  /**
   * get the edit action (which will be triggered when pressing the edit button in the toolbar)
   * 
   * @return the edit action
   */
  Action getEditAction();

  /**
   * get the edit popup menu (which will be shown when clicking on the edit text)
   * 
   * @return the edit popup menu
   */
  JPopupMenu getEditMenu();

  /**
   * get the update action (which will be triggered when pressing the update button in the toolbar)
   * 
   * @return the update action
   */
  Action getUpdateAction();

  /**
   * get the update popup menu (which will be shown when clicking on the update text)
   * 
   * @return the update popup menu
   */
  JPopupMenu getUpdateMenu();

  /**
   * get the rename action (which will be triggered when pressing the rename button in the toolbar)
   * 
   * @return the rename action
   */
  Action getRenameAction();

  /**
   * get the rename popup menu (which will be shown when clicking on the rename text)
   * 
   * @return the rename popup menu
   */
  JPopupMenu getRenameMenu();

  /**
   * get the settings node for the settings dialog
   * 
   * @return the settings node
   */
  TmmSettingsNode getSettingsNode();
}
