package org.tinymediamanager.core.threading;

public interface TmmTaskHandle {
  enum TaskType {
    MAIN_TASK, BACKGROUND_TASK
  }

  enum TaskState {
    CREATED, QUEUED, STARTED, CANCELLED, FINISHED
  }

  String getTaskName();

  int getWorkUnits();

  int getProgressDone();

  String getTaskDescription();

  TaskState getState();

  TaskType getType();

  void cancel();
}
