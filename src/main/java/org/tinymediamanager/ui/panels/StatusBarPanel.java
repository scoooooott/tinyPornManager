/*
 * Copyright 2012 - 2018 Manuel Laggner
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

package org.tinymediamanager.ui.panels;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIMessageCollector;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.FlatButton;
import org.tinymediamanager.ui.dialogs.MessageHistoryDialog;

import net.miginfocom.swing.MigLayout;

/**
 * a status bar indicating the memory amount, some information and the messages
 *
 * @author Manuel Laggner
 */
public class StatusBarPanel extends JPanel {
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private JButton                     btnNotifications;
  private JLabel                      lblMemory;

  public StatusBarPanel() {
    initComponents();

    // further initializations

    // memory indication
    final Settings settings = Settings.getInstance();
    final Timer m = new Timer(2000, null);
    m.addActionListener(evt -> lblMemory.setText(getMemory()));

    if (settings.isShowMemory()) {
      m.start();
    }
    // listener for settings change

    settings.addPropertyChangeListener(evt -> {
      if (settings.isShowMemory()) {
        m.start();
      }
      else {
        lblMemory.setText("");
        m.stop();
      }
    });

    // message notifications
    btnNotifications.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
    btnNotifications.addActionListener(e -> {
      MessageHistoryDialog dialog = MessageHistoryDialog.getInstance();
      dialog.setVisible(true);
    });

    // listener for messages change
    TmmUIMessageCollector.instance.addPropertyChangeListener(evt -> {
      if (Constants.MESSAGES.equals(evt.getPropertyName())) {
        if (TmmUIMessageCollector.instance.getNewMessagesCount() > 0) {
          btnNotifications.setVisible(true);
          btnNotifications.setEnabled(true);
          btnNotifications.setText("" + TmmUIMessageCollector.instance.getNewMessagesCount());
        }
        else {
          btnNotifications.setVisible(false);
          btnNotifications.setEnabled(false);
        }
        btnNotifications.repaint();
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("insets 0 n 0 n", "[][grow][]", "[]"));
    setOpaque(false);
    {
      lblMemory = new JLabel("");
      add(lblMemory, "cell 0 0");
    }
    {
      btnNotifications = new FlatButton(IconManager.WARN_INTENSIFIED);
      btnNotifications.setVisible(false);
      btnNotifications.setEnabled(false);
      btnNotifications.setForeground(Color.RED);
      btnNotifications.setToolTipText(BUNDLE.getString("notifications.new")); //$NON-NLS-1$
      add(btnNotifications, "cell 2 0");
    }
  }

  private String getMemory() {
    Runtime rt = Runtime.getRuntime();
    long totalMem = rt.totalMemory();
    long maxMem = rt.maxMemory(); // = Xmx
    long freeMem = rt.freeMemory();
    long megs = 1048576;

    // see http://stackoverflow.com/a/18375641
    long used = totalMem - freeMem;
    long free = maxMem - used;

    String phys = "";
    return BUNDLE.getString("tmm.memoryused") + " " + used / megs + " MiB  /  " + BUNDLE.getString("tmm.memoryfree") + " " + free / megs + " MiB  /  "
        + BUNDLE.getString("tmm.memorymax") + " " + maxMem / megs + " MiB" + phys;
  }
}
