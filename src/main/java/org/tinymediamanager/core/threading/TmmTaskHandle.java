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

public interface TmmTaskHandle {
  enum TaskType {
    MAIN_TASK,
    BACKGROUND_TASK
  }

  enum TaskState {
    CREATED,
    QUEUED,
    STARTED,
    CANCELLED,
    FINISHED
  }

  String getTaskName();

  int getWorkUnits();

  int getProgressDone();

  String getTaskDescription();

  TaskState getState();

  TaskType getType();

  void cancel();
}
