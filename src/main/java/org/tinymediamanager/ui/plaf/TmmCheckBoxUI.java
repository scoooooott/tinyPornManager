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

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class TmmCheckBoxUI extends TmmRadioButtonUI {
  private static TmmCheckBoxUI checkBoxUI = null;

  public static ComponentUI createUI(JComponent b) {
    if (checkBoxUI == null) {
      checkBoxUI = new TmmCheckBoxUI();
    }
    return checkBoxUI;
  }

  public void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    icon = UIManager.getIcon("CheckBox.icon");
  }
}
