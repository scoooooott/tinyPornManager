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
package org.tinymediamanager;

import java.util.ResourceBundle;

import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.SettingsDialog;

/**
 * A task for preloading/instantiating classes
 * 
 * @author Myron Boyle
 */
public class PreloadTask extends TmmTask {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public PreloadTask() {
    super(BUNDLE.getString("task.othertasks"), 0, TaskType.BACKGROUND_TASK);
  }

  @Override
  protected void doInBackground() {
    // just initialize some slow loading static classes like:
    SettingsDialog.getInstance();
  }
}
