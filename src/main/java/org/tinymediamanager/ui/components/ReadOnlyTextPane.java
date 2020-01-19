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

import javax.swing.JTextPane;
import javax.swing.UIManager;

/**
 * A readonly variant of the JTextPane
 *
 * @author Manuel Laggner
 */
public class ReadOnlyTextPane extends JTextPane {
  public ReadOnlyTextPane() {
    this("");
  }

  public ReadOnlyTextPane(String text) {
    setOpaque(false);
    setEditable(false);
    setText(text);
    setFocusable(false);
    setForeground(UIManager.getColor("Label.foreground"));
  }
}
