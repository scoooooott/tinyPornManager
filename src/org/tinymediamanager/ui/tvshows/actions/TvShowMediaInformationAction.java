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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowReloadMediaInformationTask;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmSwingWorker;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowMediaInformationAction. To reload mediainfo from selected TV shows/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowMediaInformationAction extends AbstractAction {
  private static final long           serialVersionUID = -1274423130095036944L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowMediaInformationAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("movie.updatemediainfo")); //$NON-NLS-1$
    }
    putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/mediainfo.png")));
    putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/mediainfo.png")));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.updatemediainfo")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();
    List<TvShowEpisode> selectedEpisodes = new ArrayList<TvShowEpisode>();

    // add all episodes which are not part of a selected tv show
    for (Object obj : TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects()) {
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        if (!selectedTvShows.contains(episode.getTvShow())) {
          selectedEpisodes.add(episode);
        }
      }
    }

    // get data of all files within all selected movies
    if (selectedTvShows.size() > 0 || selectedEpisodes.size() > 0) {
      TmmSwingWorker<?, ?> task = new TvShowReloadMediaInformationTask(selectedTvShows, selectedEpisodes);
      if (!MainWindow.executeMainTask(task)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }
}