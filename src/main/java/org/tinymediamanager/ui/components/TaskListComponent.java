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

import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmTaskHandle;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskState;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;

import net.miginfocom.swing.MigLayout;

/**
 * The class TaskListComponent is used to show one task in the {@link org.tinymediamanager.ui.dialogs.TaskListDialog}
 * 
 * @author Manuel Laggner
 */
public class TaskListComponent extends JPanel {
  private static final long           serialVersionUID = -6088880093610800005L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private TmmTaskHandle               taskHandle;

  private JLabel                      mainLabel;
  private JLabel                      dynaLabel;
  private JProgressBar                bar;
  private JButton                     closeButton;
  private JSeparator                  separator;

  public TaskListComponent() {
    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[100lp:300lp,grow][]", "[][][][]"));
    setOpaque(false);

    mainLabel = new JLabel();
    TmmFontHelper.changeFont(mainLabel, 1.167, Font.BOLD);
    dynaLabel = new JLabel();

    bar = new JProgressBar();

    closeButton = new FlatButton(IconManager.CANCEL);
    closeButton.addActionListener(e -> taskHandle.cancel());

    add(mainLabel, "cell 0 0,wmin 0");
    add(bar, "cell 0 1,growx");
    add(closeButton, "cell 1 1");
    add(dynaLabel, "cell 0 2,wmin 0");

    separator = new JSeparator();
    add(separator, "cell 0 3 2 1,growx");
  }

  public TaskListComponent(String staticText) {
    this();
    mainLabel.setText(staticText);
    mainLabel.setToolTipText(staticText);
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

  public void updateTaskInformation() {
    if (taskHandle == null) {
      return;
    }

    mainLabel.setText(taskHandle.getTaskName());
    mainLabel.setToolTipText(taskHandle.getTaskName());

    switch (taskHandle.getState()) {
      case CREATED:
      case STARTED:
        if (StringUtils.isNotBlank(taskHandle.getTaskDescription())) {
          dynaLabel.setText(taskHandle.getTaskDescription());
        }
        else {
          dynaLabel.setText(BUNDLE.getString("task.running"));
        }
        break;

      case QUEUED:
        dynaLabel.setText(BUNDLE.getString("task.queued"));
        break;

      case CANCELLED:
        dynaLabel.setText(BUNDLE.getString("task.cancelled"));
        break;

      case FINISHED:
        dynaLabel.setText(BUNDLE.getString("task.finished"));
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

  public TmmTaskHandle getHandle() {
    return taskHandle;
  }
}
