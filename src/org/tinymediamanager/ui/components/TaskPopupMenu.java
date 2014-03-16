/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.tinymediamanager.TmmTaskManager;
import org.tinymediamanager.ui.TmmSwingWorker;

/**
 * The class TaskPopupMenu. To show active tasks in a popup as overview
 * 
 * @author Manuel Laggner
 */
public class TaskPopupMenu extends JPopupMenu {
  private static final long serialVersionUID = -4035891411658304261L;

  private Timer             refreshTimer;

  private JPanel            contentPanel;

  public TaskPopupMenu() {
    contentPanel = new JPanel();
    contentPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
    setLayout(new BorderLayout());
    add(contentPanel, BorderLayout.CENTER);
  }

  public void show(Component invoker) {
    buildPopup();
    int x = -contentPanel.getPreferredSize().width - 5;
    int y = -contentPanel.getPreferredSize().height - 5;
    show(invoker, x, y);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible) {
      refreshTimer = new Timer();
      refreshTimer.schedule(new RefreshTask(), 250, 250);
    }
    else {
      if (refreshTimer != null) {
        refreshTimer.cancel();
        refreshTimer = null;
      }
    }
  }

  private void buildPopup() {
    contentPanel.removeAll();
    Queue<TmmSwingWorker<?, ?>> activeTasks = TmmTaskManager.getActiveDownloadTasks();
    contentPanel.setLayout(new GridLayout(activeTasks.size(), 1));
    for (TmmSwingWorker<?, ?> task : activeTasks) {
      JPanel subPanel = new JPanel();

      subPanel.setLayout(new BorderLayout(5, 0));
      subPanel.add(task.getProgressActionLabel(), BorderLayout.NORTH);
      subPanel.add(task.getActionButton(), BorderLayout.EAST);
      subPanel.add(task.getProgressBar(), BorderLayout.CENTER);
      contentPanel.add(subPanel);
    }
  }

  private class RefreshTask extends TimerTask {
    @Override
    public void run() {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (!isShowing()) {
            return;
          }

          buildPopup();

          if (contentPanel.getComponentCount() == 0) {
            setVisible(false);
            return;
          }

          Point p = getLocationOnScreen();
          Point newPt = getInvoker().getLocationOnScreen();
          newPt.x -= (contentPanel.getPreferredSize().width + 5);
          newPt.y -= (contentPanel.getPreferredSize().height + 5);

          pack();
          invalidate();
          Component c = getParent();

          if (!newPt.equals(p)) {
            setLocation(newPt.x, newPt.y);
          }

          if (c != null) {
            c.validate();
          }
        }
      });
    }
  }
}
