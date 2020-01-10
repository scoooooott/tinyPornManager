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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

class ToolbarButton extends JButton {
  private Icon             baseIcon;
  private Icon             hoverIcon;
  private final JPopupMenu popupMenu;

  ToolbarButton(Icon baseIcon, Icon hoverIcon) {
    this(baseIcon, hoverIcon, null);
  }

  ToolbarButton(Icon baseIcon, Icon hoverIcon, JPopupMenu popupMenu) {
    super(baseIcon);
    this.baseIcon = baseIcon;
    this.hoverIcon = hoverIcon;
    this.popupMenu = popupMenu;

    setVerticalTextPosition(SwingConstants.BOTTOM);
    setHorizontalTextPosition(SwingConstants.CENTER);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder());
    putClientProperty("flatButton", Boolean.TRUE);
    setHideActionText(true);
    updateUI();

    addMouseListener(new MouseListener() {
      @Override
      public void mouseReleased(MouseEvent arg0) {
      }

      @Override
      public void mousePressed(MouseEvent arg0) {
      }

      @Override
      public void mouseExited(MouseEvent arg0) {
        setIcon(ToolbarButton.this.baseIcon);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }

      @Override
      public void mouseEntered(MouseEvent arg0) {
        setIcon(ToolbarButton.this.hoverIcon);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseClicked(MouseEvent arg0) {
        if (ToolbarButton.this.popupMenu != null) {
          ToolbarButton.this.popupMenu.show(ToolbarButton.this,
              ToolbarButton.this.getWidth() - (int) ToolbarButton.this.popupMenu.getPreferredSize().getWidth(), ToolbarButton.this.getHeight());
        }
      }
    });
  }

  @Override
  public void setAction(Action a) {
    super.setAction(a);
    setEnabled(a != null);
  }

  @Override
  protected void configurePropertiesFromAction(Action a) {
    // only set tooltip from action
    setToolTipText(a != null ? (String) a.getValue(Action.SHORT_DESCRIPTION) : null);
  }

  void setIcons(Icon baseIcon, Icon hoverIcon) {
    setIcon(baseIcon);
    this.baseIcon = baseIcon;
    this.hoverIcon = hoverIcon;
  }
}
