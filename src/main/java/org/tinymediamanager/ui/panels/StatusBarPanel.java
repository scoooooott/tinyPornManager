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

package org.tinymediamanager.ui.panels;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskHandle;
import org.tinymediamanager.core.threading.TmmTaskListener;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIMessageCollector;
import org.tinymediamanager.ui.components.FlatButton;
import org.tinymediamanager.ui.dialogs.MessageHistoryDialog;
import org.tinymediamanager.ui.dialogs.TaskListDialog;

import net.miginfocom.swing.MigLayout;

/**
 * a status taskProgressBar indicating the memory amount, some information and the messages
 *
 * @author Manuel Laggner
 */
public class StatusBarPanel extends JPanel implements TmmTaskListener {
  private static final long           serialVersionUID = -6375900257553323558L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private Set<TmmTaskHandle>          taskSet;
  private TmmTaskHandle               activeTask;

  private JButton                     btnNotifications;
  private JLabel                      lblMemory;

  private JLabel                      taskLabel;
  private JProgressBar                taskProgressBar;
  private JButton                     taskStopButton;

  public StatusBarPanel() {
    initComponents();

    // further initializations
    btnNotifications.setVisible(false);
    taskLabel.setVisible(false);
    taskStopButton.setVisible(false);
    taskProgressBar.setVisible(false);

    // task management
    taskSet = new HashSet<>();
    taskLabel.setText("");
    TmmTaskManager.getInstance().addTaskListener(this);

    // memory indication
    final Settings settings = Settings.getInstance();
    final Timer m = new Timer(1000, null);
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

    // pre-load the dialog (to fetch all events)
    TaskListDialog.getInstance();
  }

  private void initComponents() {
    setLayout(new MigLayout("insets 0 n 0 n, hidemode 3", "[][50lp:n][grow][100lp][15lp:n][]", "[22lp:n]"));
    setOpaque(false);
    {
      lblMemory = new JLabel("");
      add(lblMemory, "cell 0 0");
    }

    {
      taskLabel = new JLabel("XYZ");
      add(taskLabel, "cell 2 0,alignx right, wmin 0");
    }
    {
      taskProgressBar = new JProgressBar();
      taskProgressBar.setBackground(UIManager.getColor("Panel.background"));
      taskProgressBar.addMouseListener(new MListener());
      add(taskProgressBar, "cell 3 0");
    }
    {
      taskStopButton = new FlatButton(IconManager.CANCEL);
      taskStopButton.addActionListener(e -> {
        if (activeTask instanceof TmmTask) {
          activeTask.cancel();
        }
      });
      add(taskStopButton, "cell 4 0");
    }
    {
      btnNotifications = new FlatButton(IconManager.WARN_INTENSIFIED);
      btnNotifications.setEnabled(false);
      btnNotifications.setForeground(Color.RED);
      btnNotifications.setToolTipText(BUNDLE.getString("notifications.new"));
      add(btnNotifications, "cell 5 0");
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

  @Override
  public synchronized void processTaskEvent(final TmmTaskHandle task) {
    SwingUtilities.invokeLater(() -> {

      if (task.getState() == TmmTaskHandle.TaskState.CREATED || task.getState() == TmmTaskHandle.TaskState.QUEUED) {
        taskSet.add(task);
      }
      else if (task.getState() == TmmTaskHandle.TaskState.STARTED) {
        taskSet.add(task);
      }
      else if (task.getState() == TmmTaskHandle.TaskState.CANCELLED || task.getState() == TmmTaskHandle.TaskState.FINISHED) {
        taskSet.remove(task);
      }

      // search for a new activetask to be displayed in the statusbar
      if (activeTask == null || activeTask.getState() == TmmTaskHandle.TaskState.FINISHED
          || activeTask.getState() == TmmTaskHandle.TaskState.CANCELLED) {
        activeTask = null;
        for (TmmTaskHandle handle : taskSet) {
          if (handle.getType() == TmmTaskHandle.TaskType.MAIN_TASK && handle.getState() == TmmTaskHandle.TaskState.STARTED) {
            activeTask = handle;
            break;
          }
        }

        // no active main task found; if there are any BG tasks, display a dummy char to indicate something is working
        if (activeTask == null) {
          for (TmmTaskHandle handle : taskSet) {
            if (handle.getState() == TmmTaskHandle.TaskState.STARTED) {
              activeTask = handle;
              break;
            }
          }
        }
      }

      // hide components if there is nothing to be displayed
      if (activeTask == null) {
        taskLabel.setVisible(false);
        taskStopButton.setVisible(false);
        taskProgressBar.setVisible(false);
      }
      else {
        // ensure everything is visible
        taskLabel.setVisible(true);
        taskProgressBar.setVisible(true);
        if (activeTask.getType() == TmmTaskHandle.TaskType.MAIN_TASK) {
          taskStopButton.setVisible(true);
        }
        else {
          taskStopButton.setVisible(false);
        }

        // and update content
        taskLabel.setText(activeTask.getTaskName());
        if (activeTask.getWorkUnits() > 0) {
          taskProgressBar.setIndeterminate(false);
          taskProgressBar.setMaximum(activeTask.getWorkUnits());
          taskProgressBar.setValue(activeTask.getProgressDone());
        }
        else {
          taskProgressBar.setIndeterminate(true);
        }
      }
    });
  }

  /****************************************************************************************
   * helper classes
   ****************************************************************************************/
  private class MListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      TaskListDialog.getInstance().setVisible(true);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }
}
