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

package org.tinymediamanager.ui.plaf;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

/**
 * the class {@link UndoAction} is used for undoing any edit in a text field
 *
 * @author Manuel Laggner
 */
class UndoAction extends AbstractAction {
  static final String          UNDO = "undo";

  private final JTextComponent component;
  private final UndoManager    undoManager;

  UndoAction(JTextComponent component, UndoManager undoManager) {
    super();
    this.component = component;
    this.undoManager = undoManager;

    this.putValue(Action.NAME, undoManager.getUndoPresentationName());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (component.isEditable()) {
      try {
        undoManager.undo();
      }
      catch (Exception ingored) {
        // no need to do anything here
      }
    }
  }
}
