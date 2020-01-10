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

package org.tinymediamanager.ui.components.toolbar;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ToolbarLabel extends JLabel {
  public static Color    COLOR       = Color.GRAY;
  public static Color    COLOR_HOVER = Color.WHITE;

  protected final String defaultText;
  private ActionListener action      = null;

  public ToolbarLabel(String text) {
    super(text, SwingConstants.CENTER);
    defaultText = text;

    setHorizontalTextPosition(SwingConstants.LEFT);
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setOpaque(false);
    setForeground(COLOR);

    setMouseListener();
  }

  public ToolbarLabel(String text, ActionListener action) {
    this(text);
    this.action = action;
  }

  protected void setMouseListener() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent arg0) {
        setForeground(COLOR);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }

      @Override
      public void mouseEntered(MouseEvent arg0) {
        setForeground(COLOR_HOVER);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseClicked(MouseEvent arg0) {
        if (action != null) {
          action.actionPerformed(new ActionEvent(ToolbarLabel.this, ActionEvent.ACTION_PERFORMED, ""));
        }
      }
    });
  }
}
