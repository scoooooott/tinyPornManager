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
package org.tinymediamanager.ui.components;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.tinymediamanager.core.threading.TmmTaskHandle;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskState;
import org.tinymediamanager.core.threading.TmmTaskListener;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class TaskListPopupPane. To present active tasks in a popup pane
 *
 * @author Manuel Laggner
 */
public class TaskListPopup extends JPopupMenu implements TmmTaskListener {
  private static final long                           serialVersionUID = 27076046690061838L;
  /* @wbp.nls.resourceBundle messages */
  private static final ResourceBundle                 BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  // a map of all active tasks
  private final Map<TmmTaskHandle, TaskListComponent> taskMap          = new HashMap<>();
  // a list of all tasks to be displayed
  private final HashSet<TaskListComponent>            listComponents   = new HashSet<>();

  // UI components
  private final JScrollPane                           scrollPane;
  private final JPanel                                view;
  private final TaskListComponent                     noActiveTask;

  public TaskListPopup() {
    view = new JPanel();
    view.setBackground(UIManager.getColor("Menu.background"));

    noActiveTask = new TaskListComponent(BUNDLE.getString("task.nonerunning")); //$NON-NLS-1$
    noActiveTask.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    view.add(noActiveTask);

    GridLayout grid = new GridLayout(0, 1);
    grid.setHgap(0);
    grid.setVgap(5);
    view.setLayout(grid);
    view.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

    scrollPane = new JScrollPane() {
      @Override
      public Dimension getPreferredSize() {
        int count = view.getComponentCount();
        int height = count > 0 ? view.getComponent(0).getPreferredSize().height : 0;
        int offset = count > 5 ? height * 5 + 10 : (count * height) + 10;
        // 22 is the width of the additional scroll bar
        return new Dimension(count > 5 ? TaskListComponent.ITEM_WIDTH + getVerticalScrollBar().getWidth() : TaskListComponent.ITEM_WIDTH + 2, offset);
      }
    };
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setFocusable(true);
    scrollPane.setRequestFocusEnabled(true);
    scrollPane.setViewportView(view);
    add(scrollPane);

    TmmTaskManager.getInstance().addTaskListener(this);
  }

  @Override
  public void processTaskEvent(final TmmTaskHandle task) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (task.getState() == TaskState.CREATED || task.getState() == TaskState.QUEUED) {
          addListItem(task);
        }
        else if (task.getState() == TaskState.STARTED) {
          TaskListComponent comp = taskMap.get(task);
          if (comp == null) {
            addListItem(task);
            comp = taskMap.get(task);
          }
          comp.updateTaskInformation();
        }
        else if (task.getState() == TaskState.CANCELLED || task.getState() == TaskState.FINISHED) {
          removeListItem(task);
        }
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
    view.remove(noActiveTask);

    // ass the new component
    listComponents.add(comp);
    comp.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    view.add(comp);
    if (listComponents.size() > 5) {
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    else {
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    if (isShowing()) {
      scrollPane.invalidate();
      pack();
    }
  }

  /**
   * remove the given task from the task list
   *
   * @param task
   *          the task to be removed
   */
  private void removeListItem(TmmTaskHandle task) {
    taskMap.remove(task);

    Iterator<TaskListComponent> it = listComponents.iterator();
    while (it.hasNext()) {
      TaskListComponent comp = it.next();
      if (comp.getHandle() == task) {
        view.remove(comp);
        it.remove();
        break;
      }
    }
    if (listComponents.size() > 5) {
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    else {
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }

    if (listComponents.isEmpty()) {
      view.add(noActiveTask);
    }

    if (isShowing()) {
      scrollPane.invalidate();
      pack();
    }
  }
}
