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

import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.undo.UndoManager;

import org.tinymediamanager.ui.plaf.light.TmmLightBorderFactory;

import com.jtattoo.plaf.BaseTextPaneUI;
import com.jtattoo.plaf.JTattooUtilities;

public class TmmTextPaneUI extends BaseTextPaneUI {
  private FocusListener focusListener = null;

  private UndoListener  undoListener;
  private UndoManager   undoManager;
  private UndoAction    undoAction;
  private RedoAction    redoAction;

  public static ComponentUI createUI(JComponent c) {
    return new TmmTextPaneUI();
  }

  @Override
  public void installDefaults() {
    super.installDefaults();
    getComponent().setBorder(TmmLightBorderFactory.getInstance().getTextBorder());

    // install the undomanager
    undoManager = new UndoManager();
    undoAction = new UndoAction(getComponent(), undoManager);
    redoAction = new RedoAction(getComponent(), undoManager);
  }

  @Override
  protected void installKeyboardActions() {
    super.installKeyboardActions();

    // install keymap for the undo manager
    InputMap im = getComponent().getInputMap();
    ActionMap am = getComponent().getActionMap();
    am.put(UndoAction.UNDO, undoAction);
    am.put(RedoAction.REDO, redoAction);

    if (JTattooUtilities.isMac()) {
      int commandKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, commandKey), UndoAction.UNDO);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, commandKey), RedoAction.REDO);
    }
    else {
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK), UndoAction.UNDO);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK), RedoAction.REDO);
    }
  }

  @Override
  protected void installListeners() {
    super.installListeners();
    focusListener = new FocusListener() {

      public void focusGained(FocusEvent e) {
        if (getComponent() != null) {
          getComponent().invalidate();
          getComponent().repaint();
        }
      }

      public void focusLost(FocusEvent e) {
        if (getComponent() != null) {
          getComponent().invalidate();
          getComponent().repaint();
        }
      }
    };
    getComponent().addFocusListener(focusListener);
  }

  @Override
  protected void uninstallListeners() {
    getComponent().removeFocusListener(focusListener);
    focusListener = null;
    getComponent().getDocument().removeUndoableEditListener(undoListener);
    undoListener = null;
    super.uninstallListeners();
  }

  @Override
  protected void modelChanged() {
    super.modelChanged();

    // add the undo listener
    if (undoListener != null) {
      getComponent().getDocument().removeUndoableEditListener(undoListener);
    }
    undoListener = new UndoListener();
    getComponent().getDocument().addUndoableEditListener(undoListener);
  }

  private class UndoListener implements UndoableEditListener {
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
      if (getComponent().isEditable()) {
        undoManager.addEdit(e.getEdit());
      }
    }
  }
}
