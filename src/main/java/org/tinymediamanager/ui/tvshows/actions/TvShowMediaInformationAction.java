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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowReloadMediaInformationTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowMediaInformationAction. To reload mediainfo from selected TV shows/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowMediaInformationAction extends TmmAction {
  private static final long           serialVersionUID = -1274423130095036944L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowMediaInformationAction() {
    putValue(NAME, BUNDLE.getString("tvshow.updatemediainfo")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.MEDIAINFO);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.updatemediainfo")); //$NON-NLS-1$
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();
    List<TvShowEpisode> selectedEpisodes = new ArrayList<>();

    // add all episodes which are not part of a selected tv show
    for (Object obj : TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects()) {
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        if (!selectedTvShows.contains(episode.getTvShow())) {
          selectedEpisodes.add(episode);
        }
      }
    }

    if (selectedEpisodes.isEmpty() && selectedTvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected")); //$NON-NLS-1$
      return;
    }

    // get data of all files within all selected movies
    TmmThreadPool task = new TvShowReloadMediaInformationTask(selectedTvShows, selectedEpisodes);
    if (TmmTaskManager.getInstance().addMainTask(task)) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
    }
  }
}
