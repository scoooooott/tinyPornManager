/*
 * Copyright 2012-2013 Manuel Laggner
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

import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

public class JNativeFileChooser extends JFileChooser {

  private static final String laf = "com.jtattoo.plaf.luna.LunaLookAndFeel";

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JFileChooser#updateUI()
   */
  public void updateUI() {
    LookAndFeel old = UIManager.getLookAndFeel();
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Throwable ex) {
      old = null;
    }

    super.updateUI();

    if (old != null) {
      try {
        UIManager.setLookAndFeel(old);
      }
      catch (Exception ignored) {
      } // shouldn't get here
    }
  }
}
