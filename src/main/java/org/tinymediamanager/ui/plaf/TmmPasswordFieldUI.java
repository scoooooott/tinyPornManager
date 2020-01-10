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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.BasePasswordFieldUI;

public class TmmPasswordFieldUI extends BasePasswordFieldUI {
  private FocusListener focusListener = null;

  public static ComponentUI createUI(JComponent c) {
    return new TmmPasswordFieldUI();
  }

  @Override
  protected void installListeners() {
    super.installListeners();

    focusListener = new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        if (getComponent() != null) {
          getComponent().invalidate();
          getComponent().repaint();
        }
      }

      @Override
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
    super.uninstallListeners();
  }
}
