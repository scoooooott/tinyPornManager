/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JWindow;

/**
 * @author Manuel Laggner
 * 
 */
public class PopupWindow {
  public void makeUI(JComponent comp, JComponent parent) {
    JFrame frame = new JFrame();
    final JWindow window = new JWindow(frame);
    frame.setUndecorated(false);
    frame.setAlwaysOnTop(true);
    frame.setLocation(parent.getX(), parent.getY());

    final JButton button = new JButton("Close Popup");
    button.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        window.setVisible(false);
        button.setEnabled(false);
      }
    });
    button.setEnabled(false);
    button.setEnabled(false);
    frame.add(comp, BorderLayout.CENTER);
    frame.add(button, BorderLayout.SOUTH);

    final JComponent contentPane = (JComponent) frame.getContentPane();
    contentPane.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        maybeShowPopup(e);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
      }

      private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
          window.setLocation(e.getLocationOnScreen());
          window.setVisible(true);
          button.setEnabled(true);
        }
      }
    });

    window.add(new JTextArea(5, 30));
    window.pack();

    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    // frame.setSize(400, 400);
    frame.pack();
    // frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
