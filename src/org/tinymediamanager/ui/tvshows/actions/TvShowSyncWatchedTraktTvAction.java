/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class TvShowSyncWatchedTraktTvAction. To synchronize the watched state of your TV show library with trakt.tv
 * 
 * @author Manuel Laggner
 */
public class TvShowSyncWatchedTraktTvAction extends AbstractAction {
  private static final long           serialVersionUID = 6640292090443882545L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowSyncWatchedTraktTvAction() {
    putValue(NAME, BUNDLE.getString("tvshow.synctraktwatched")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.synctraktwatched.desc")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.SYNC);
    putValue(LARGE_ICON_KEY, IconManager.SYNC);

    if (!Globals.isDonator()) {
      setEnabled(false);
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tmm.donatorfunction.hint")); //$NON-NLS-1$
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TmmTask task = new SyncTraktTvTask(false, false, false, true);
    TmmTaskManager.getInstance().addUnnamedTask(task);
  }
}
