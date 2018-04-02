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
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowRenameAction. To rename TV shows/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowRenameAction extends TmmAction {
  private static final long           serialVersionUID = -8988748633666277616L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowRenameAction() {
    putValue(NAME, BUNDLE.getString("tvshow.rename")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.rename")); //$NON-NLS-1$
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();
    Set<TvShowEpisode> selectedEpisodes = new HashSet<>();

    // add all episodes which are not part of a selected tv show
    for (Object obj : TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects()) {
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        if (!selectedTvShows.contains(episode.getTvShow())) {
          selectedEpisodes.add(episode);
        }
      }
      if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        for (TvShowEpisode episode : season.getEpisodes()) {
          selectedEpisodes.add(episode);
        }
      }
    }

    if (selectedEpisodes.isEmpty() && selectedTvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected")); //$NON-NLS-1$
      return;
    }

    // rename
    TmmThreadPool renameTask = new TvShowRenameTask(selectedTvShows, new ArrayList<>(selectedEpisodes), true);
    if (TmmTaskManager.getInstance().addMainTask(renameTask)) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
    }
  }
}
