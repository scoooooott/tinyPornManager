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

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * The class FlatButton is used to provide a button without the visual aspects of a button, but with a hover
 *
 * @author Manuel Laggner
 */
public class FlatButton extends JButton {

  public FlatButton(Icon icon) {
    this(null, icon);
  }

  public FlatButton(String text) {
    this(text, null);
  }

  public FlatButton(String text, Icon icon) {
    // Create the model
    setModel(new DefaultButtonModel());

    // initialize
    init(text, icon);
  }

  @Override
  protected void init(String text, Icon icon) {
    setOpaque(false);
    setContentAreaFilled(false);
    putClientProperty("flatButton", Boolean.TRUE);
    super.init(text, icon);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }
}
