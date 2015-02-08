/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.apache.commons.lang3.SystemUtils;

/**
 * The Class JNativeFileChooser.
 * 
 * @author Manuel Laggner
 */
public class JNativeFileChooser extends JFileChooser {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JFileChooser#updateUI()
   */
  @Override
  public void updateUI() {
    if (SystemUtils.IS_OS_WINDOWS) {
      // on windows set the native laf
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
    else {
      // on linux/mac as is
      super.updateUI();
    }
  }
}
