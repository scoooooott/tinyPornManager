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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

public abstract class AbstractTmmUIModule implements ITmmUIModule {
  protected final static ResourceBundle BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  protected final Map<Class, Action>    actionMap    = new HashMap<>();

  protected JPanel                      listPanel;
  protected JPanel                      detailPanel;

  protected Action                      searchAction = null;
  protected Action                      editAction   = null;
  protected Action                      updateAction = null;
  protected Action                      exportAction = null;
  protected Action                      renameAction = null;

  protected JPopupMenu                  popupMenu;
  protected JPopupMenu                  updatePopupMenu;
  protected JPopupMenu                  searchPopupMenu;
  protected JPopupMenu                  editPopupMenu;
  protected JPopupMenu                  renamePopupMenu;

  public AbstractTmmUIModule() {
  }

  /**
   * this factory creates the action and registers the hotkeys for accelerator management
   *
   * @param actionClass
   *          the class of the action
   * @return the constructed action
   */
  protected Action createAndRegisterAction(Class<? extends Action> actionClass) {
    Action action = actionMap.get(actionClass);
    if (action == null) {
      try {
        action = actionClass.newInstance();
        actionMap.put(actionClass, action);
        // KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
      }
      catch (Exception ignored) {
      }
    }
    return action;
  }

  /**
   * register accelerators
   */
  protected void registerAccelerators() {
    for (Map.Entry<Class, Action> entry : actionMap.entrySet()) {
      try {
        KeyStroke keyStroke = (KeyStroke) entry.getValue().getValue(Action.ACCELERATOR_KEY);
        if (keyStroke != null) {
          String actionMapKey = "action" + entry.getKey().getName();
          getTabPanel().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionMapKey);
          getTabPanel().getActionMap().put(actionMapKey, entry.getValue());
        }
      }
      catch (Exception ignored) {
      }
    }
  }

  @Override
  public JPanel getTabPanel() {
    return listPanel;
  }

  @Override
  public JPanel getDetailPanel() {
    return detailPanel;
  }

  @Override
  public Action getSearchAction() {
    return searchAction;
  }

  @Override
  public JPopupMenu getSearchMenu() {
    return searchPopupMenu;
  }

  @Override
  public Action getEditAction() {
    return editAction;
  }

  @Override
  public JPopupMenu getEditMenu() {
    return editPopupMenu;
  }

  @Override
  public Action getUpdateAction() {
    return updateAction;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    return updatePopupMenu;
  }

  @Override
  public Action getRenameAction() {
    return renameAction;
  }

  @Override
  public JPopupMenu getRenameMenu() {
    return renamePopupMenu;
  }

  @Override
  public Action getExportAction() {
    return exportAction;
  }

  @Override
  public Icon getSearchButtonIcon() {
    return IconManager.TOOLBAR_REFRESH;
  }

  @Override
  public Icon getSearchButtonHoverIcon() {
    return IconManager.TOOLBAR_REFRESH_HOVER;
  }
}
