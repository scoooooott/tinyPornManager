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

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.AbstractToolBarUI;

public class TmmToolBarUI extends AbstractToolBarUI {
  public static ComponentUI createUI(JComponent c) {
    return new TmmToolBarUI();
  }

  @Override
  public Border getRolloverBorder() {
    return TmmBorders.getRolloverToolButtonBorder();
  }

  @Override
  public Border getNonRolloverBorder() {
    return null;
  }

  @Override
  public boolean isButtonOpaque() {
    return false;
  }

  @Override
  public void paint(Graphics g, JComponent c) {
  }
}
