package org.tinymediamanager.core.threading;

public interface TmmTaskHandle {
  public enum TaskType {
    MAIN_TASK, BACKGROUND_TASK
  }

  public enum TaskState {
    CREATED, QUEUED, STARTED, CANCELLED, FINISHED
  }

  public String getTaskName();

  public int getWorkUnits();

  public int getProgressDone();

  public String getTaskDescription();

  public TaskState getState();

  public TaskType getType();

  public void cancel();
}
