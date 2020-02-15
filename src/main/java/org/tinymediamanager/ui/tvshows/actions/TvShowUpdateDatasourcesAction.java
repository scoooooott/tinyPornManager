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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.actions.TmmAction;

/**
 * The Class TvShowUpdateDatasourcesAction. Update all data sources
 * 
 * @author Manuel Laggner
 */
public class TvShowUpdateDatasourcesAction extends TmmAction {
  private static final long           serialVersionUID = 5704371143505653741L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowUpdateDatasourcesAction() {
    putValue(NAME, BUNDLE.getString("update.datasource"));
    putValue(LARGE_ICON_KEY, IconManager.REFRESH);
    putValue(SMALL_ICON, IconManager.REFRESH);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    TmmThreadPool task = new TvShowUpdateDatasourceTask();
    TmmTaskManager.getInstance().addMainTask(task);
  }
}
