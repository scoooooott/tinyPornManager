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

import java.awt.Toolkit;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import org.tinymediamanager.core.UTF8Control;

/**
 * The Class TextFieldPopupMenu.
 */
public class TextFieldPopupMenu {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private static JMenuItem getCutMenuItem(final JPopupMenu menu) {
    final JMenuItem mntmCut = new JMenuItem(BUNDLE.getString("menuitem.cut"));
    mntmCut.addActionListener(e -> {
      if (menu.getInvoker() instanceof JTextComponent) {
        ((JTextComponent) menu.getInvoker()).cut();
      }
    });
    return mntmCut;
  }

  private static JMenuItem getCopyMenuItem(final JPopupMenu menu) {
    JMenuItem mntmCopy = new JMenuItem(BUNDLE.getString("menuitem.copy"));
    mntmCopy.addActionListener(e -> {
      if (menu.getInvoker() instanceof JTextComponent) {
        ((JTextComponent) menu.getInvoker()).copy();
      }
    });
    return mntmCopy;
  }

  private static JMenuItem getPasteMenuItem(final JPopupMenu menu) {
    JMenuItem mntmPaste = new JMenuItem(BUNDLE.getString("menuitem.paste"));
    mntmPaste.addActionListener(e -> {
      if (menu.getInvoker() instanceof JTextComponent) {
        ((JTextComponent) menu.getInvoker()).paste();
      }
    });
    return mntmPaste;
  }

  /**
   * Builds the popup menu including the common cut/copy/paste items.
   * 
   * @return the popup menu
   */
  public static JPopupMenu buildCutCopyPaste() {
    final JPopupMenu popupMenu = new JPopupMenu();
    final JMenuItem mntmCut = getCutMenuItem(popupMenu);
    final JMenuItem mntmCopy = getCopyMenuItem(popupMenu);
    final JMenuItem mntmPaste = getPasteMenuItem(popupMenu);
    popupMenu.add(mntmCut);
    popupMenu.add(mntmCopy);
    popupMenu.add(mntmPaste);

    popupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JTextComponent jtc = (JTextComponent) popupMenu.getInvoker();
        boolean enableCutCopy = (jtc.getSelectionEnd() - jtc.getSelectionStart()) > 0;
        boolean enablePaste = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null) != null;
        mntmCut.setEnabled(enableCutCopy);
        mntmCopy.setEnabled(enableCutCopy);
        mntmPaste.setEnabled(enablePaste);
      }
    });

    return popupMenu;
  }
}
