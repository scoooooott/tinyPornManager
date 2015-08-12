/*
 * Copyright 2012 - 2015 Manuel Laggner
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

/**
 * Dummy Unnamed Task container, to get simple Runables into our task manager
 * 
 * @author Myron Boyle
 */
public class UnnamedTask extends TmmTask {
  private Runnable task = null;

  public UnnamedTask(String name, Runnable thread, TaskType type) {
    super(name, 0, type);
    task = thread;
  }

  private UnnamedTask(String taskName, int workUnits, TaskType type) {
    super(taskName, workUnits, type);
  }

  @Override
  protected void doInBackground() {
    if (task != null) {
      task.run();
    }
  }
}
