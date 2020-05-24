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

package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmTaskHandle;
import org.tinymediamanager.core.threading.TmmTaskListener;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.components.TaskListComponent;

import net.miginfocom.swing.MigLayout;

public class TaskListDialog extends TmmDialog implements TmmTaskListener {
  private static final long                           serialVersionUID = 4151412495928010232L;
  /** @wbp.nls.resourceBundle messages */
  protected static final ResourceBundle               BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private static TaskListDialog                       instance;

  // a map of all active tasks
  private final Map<TmmTaskHandle, TaskListComponent> taskMap          = new ConcurrentHashMap<>();
  private final TaskListComponent                     noActiveTask;

  private final JPanel                                panelContent;

  private TaskListDialog() {
    super(BUNDLE.getString("tasklist.title"), "taskList");
    setModalityType(ModalityType.MODELESS);

    {
      panelContent = new JPanel();
      panelContent.setOpaque(false);

      noActiveTask = new TaskListComponent(BUNDLE.getString("task.nonerunning"));
      noActiveTask.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
      panelContent.add(noActiveTask);

      panelContent.setLayout(new MigLayout("", "[grow]"));
      panelContent.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setViewportView(panelContent);

      JPanel rootPanel = new JPanel();
      rootPanel.setBackground(UIManager.getColor("Menu.background"));
      rootPanel.setLayout(new MigLayout("insets 0", "[]", "[100lp:300lp,grow]"));
      rootPanel.add(scrollPane, "cell 0 0, top, grow");

      getContentPane().add(rootPanel, BorderLayout.CENTER);

      JButton btnAbortAll = new JButton(BUNDLE.getString("Button.abortqueue"));
      btnAbortAll.addActionListener(e -> taskMap.forEach((task, component) -> {
        task.cancel();
        removeListItem(task);
      }));
      addButton(btnAbortAll);
    }
    TmmTaskManager.getInstance().addTaskListener(this);
  }

  @Override
  public void dispose() {
    // do not dispose (singleton), but save the size/position
    TmmWindowSaver.getInstance().saveSettings(this);
  }

  public static TaskListDialog getInstance() {
    if (instance == null) {
      instance = new TaskListDialog();
    }
    return instance;
  }

  @Override
  public void processTaskEvent(final TmmTaskHandle task) {
    SwingUtilities.invokeLater(() -> {
      if (task.getState() == TmmTaskHandle.TaskState.CREATED || task.getState() == TmmTaskHandle.TaskState.QUEUED) {
        addListItem(task);
      }
      else if (task.getState() == TmmTaskHandle.TaskState.STARTED) {
        TaskListComponent comp = taskMap.get(task);
        if (comp == null) {
          addListItem(task);
          comp = taskMap.get(task);
        }
        comp.updateTaskInformation();
      }
      else if (task.getState() == TmmTaskHandle.TaskState.CANCELLED || task.getState() == TmmTaskHandle.TaskState.FINISHED) {
        removeListItem(task);
      }
    });
  }

  /**
   * add a new task to the task list
   *
   * @param task
   *          the task to be added
   */
  private void addListItem(TmmTaskHandle task) {
    TaskListComponent comp;
    if (taskMap.containsKey(task)) {
      // happens when we click to display on popup and there is a
      // new handle waiting in the queue.
      comp = taskMap.get(task);
    }
    else {
      comp = new TaskListComponent(task);
      taskMap.put(task, comp);
    }

    // remove the no active task component (if available)
    panelContent.remove(noActiveTask);

    comp.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    panelContent.add(comp, "wrap, growx");
    bottomPanel.setVisible(true);

    if (isShowing()) {
      revalidate();
      pack();
      repaint();
    }
  }

  /**
   * remove the given task from the task list
   *
   * @param task
   *          the task to be removed
   */
  private void removeListItem(TmmTaskHandle task) {
    TaskListComponent comp = taskMap.remove(task);
    if (comp != null) {
      panelContent.remove(comp);
    }

    if (taskMap.isEmpty()) {
      panelContent.add(noActiveTask, "wrap, growx");
      bottomPanel.setVisible(false);
    }

    if (isShowing()) {
      revalidate();
      pack();
      repaint();
    }
  }
}
