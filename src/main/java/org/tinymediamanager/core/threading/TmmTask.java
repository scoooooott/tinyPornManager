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
package org.tinymediamanager.core.threading;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The class TmmTask. The main class representing tasks in tmm
 * 
 * @author Manuel Laggner
 */
public abstract class TmmTask implements Runnable, TmmTaskHandle {
  private final Set<TmmTaskListener> listeners = new CopyOnWriteArraySet<>();
  private TaskType                   type;
  protected TaskState                state     = TaskState.CREATED;

  protected String                   taskName;
  protected String                   taskDescription;
  protected int                      workUnits;
  protected int                      progressDone;
  protected boolean                  cancel;
  private long                       uniqueId;
  protected Thread                   thread;

  protected TmmTask(String taskName, int workUnits, TaskType type) {
    this.taskName = taskName;
    this.workUnits = workUnits;
    this.taskDescription = "";
    this.progressDone = 0;
    this.type = type;
    uniqueId = TmmTaskManager.getInstance().GLOB_THRD_CNT.incrementAndGet();
    this.thread = null;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public final String getTaskName() {
    return taskName;
  }

  @Override
  public final int getWorkUnits() {
    return workUnits;
  }

  @Override
  public final int getProgressDone() {
    return progressDone;
  }

  @Override
  public final String getTaskDescription() {
    return taskDescription;
  }

  @Override
  public final TaskState getState() {
    return state;
  }

  protected void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  protected void setWorkUnits(int workUnits) {
    this.workUnits = workUnits;
  }

  protected void setProgressDone(int progressDone) {
    this.progressDone = progressDone;
  }

  protected void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
  }

  public final void addListener(final TmmTaskListener listener) {
    listeners.add(listener);
  }

  void setState(TaskState newState) {
    this.state = newState;
    informListeners();
  }

  public final void removeListener(final TmmTaskListener listener) {
    listeners.remove(listener);
  }

  protected void informListeners() {
    for (TmmTaskListener listener : listeners) {
      listener.processTaskEvent(this);
    }
  }

  @Override
  public final void run() {
    // the task has been cancelled before it is being executed
    if (cancel) {
      return;
    }
    Thread.currentThread().setName(Thread.currentThread().getName() + "-G" + uniqueId);

    start();
    try {
      doInBackground();
    }
    finally {
      finish();
    }
  }

  @Override
  public void cancel() {
    this.cancel = true;
    setState(TaskState.CANCELLED);
    thread = null;
  }

  protected void start() {
    thread = Thread.currentThread();
    setState(TaskState.STARTED);
  }

  protected void publishState(String taskDescription, int progress) {
    this.taskDescription = taskDescription;
    this.progressDone = progress;
    informListeners();
  }

  protected void publishState(int progress) {
    this.progressDone = progress;
    informListeners();
  }

  protected void publishState() {
    informListeners();
  }

  protected void finish() {
    setState(TaskState.FINISHED);
    thread = null;
  }

  @Override
  public final TaskType getType() {
    return type;
  }

  protected abstract void doInBackground();
}
