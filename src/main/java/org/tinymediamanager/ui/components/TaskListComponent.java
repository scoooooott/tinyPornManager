/*
 * Copyright 2012 - 2017 Manuel Laggner
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.threading.TmmTaskHandle;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskState;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class TaskListComponent is used to show one task in the
 * {@link TaskListPopup}
 * 
 * @author Manuel Laggner
 */
public class TaskListComponent extends JPanel {
  private static final long           serialVersionUID = -6088880093610800005L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  static final int                    ITEM_WIDTH       = 400;

  private TmmTaskHandle               taskHandle;

  private JLabel                      mainLabel;
  private JLabel                      dynaLabel;
  private JProgressBar                bar;
  private JButton                     closeButton;

  private TaskListComponent() {
    setFocusable(true);
    setRequestFocusEnabled(true);
    setLayout(new BorderLayout(5, 0));
    setBorder(BorderFactory.createEmptyBorder());
    setOpaque(false);

    mainLabel = new JLabel();
    TmmFontHelper.changeFont(mainLabel, 1.167, Font.BOLD);
    dynaLabel = new JLabel();

    bar = new JProgressBar();

    closeButton = new JButton(new CancelAction());
    closeButton.setBorderPainted(false);
    closeButton.setBorder(BorderFactory.createEmptyBorder());
    closeButton.setOpaque(false);
    closeButton.setContentAreaFilled(false);
    closeButton.setFocusable(false);

    add(mainLabel, BorderLayout.NORTH);
    add(bar, BorderLayout.CENTER);
    add(closeButton, BorderLayout.EAST);
    add(dynaLabel, BorderLayout.SOUTH);
  }

  TaskListComponent(String staticText) {
    this();
    mainLabel.setText(staticText);
    bar.setVisible(false);
    closeButton.setVisible(false);
    dynaLabel.setVisible(false);
    taskHandle = null;
  }

  public TaskListComponent(TmmTaskHandle handle) {
    this();
    this.taskHandle = handle;
    updateTaskInformation();
  }

  void updateTaskInformation() {
    if (taskHandle == null) {
      return;
    }

    mainLabel.setText(taskHandle.getTaskName());

    switch (taskHandle.getState()) {
      case CREATED:
      case STARTED:
        if (StringUtils.isNotBlank(taskHandle.getTaskDescription())) {
          dynaLabel.setText(taskHandle.getTaskDescription());
        }
        else {
          dynaLabel.setText(BUNDLE.getString("task.running")); //$NON-NLS-1$
        }
        break;

      case QUEUED:
        dynaLabel.setText(BUNDLE.getString("task.queued")); //$NON-NLS-1$
        break;

      case CANCELLED:
        dynaLabel.setText(BUNDLE.getString("task.cancelled")); //$NON-NLS-1$
        break;

      case FINISHED:
        dynaLabel.setText(BUNDLE.getString("task.finished")); //$NON-NLS-1$
        break;
    }

    if (taskHandle.getWorkUnits() > 0) {
      bar.setValue(taskHandle.getProgressDone());
      bar.setMaximum(taskHandle.getWorkUnits());
      bar.setIndeterminate(false);
    }
    else if (taskHandle.getState() == TaskState.QUEUED) {
      bar.setIndeterminate(false);
      bar.setValue(0);
      bar.setMaximum(1);
    }
    else {
      bar.setIndeterminate(true);
    }
  }

  TmmTaskHandle getHandle() {
    return taskHandle;
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -2634569716059018131L;

    private CancelAction() {
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      taskHandle.cancel();
    }
  }
}
