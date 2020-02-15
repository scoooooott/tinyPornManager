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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tasks.ImageCacheTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowRebuildImageCacheAction. To rebuild the image cache selected TV shows/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowRebuildImageCacheAction extends TmmAction {
  private static final long           serialVersionUID = 3452373237085274937L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowRebuildImageCacheAction() {
    putValue(NAME, BUNDLE.getString("tvshow.rebuildimagecache"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.rebuildimagecache"));

  }

  @Override
  protected void processAction(ActionEvent e) {
    if (!Globals.settings.isImageCache()) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.imagecache.notactivated"));
      return;
    }

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
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    List<MediaFile> imageFiles = new ArrayList<>();

    // get data of all files within all selected TV shows/episodes
    for (TvShow tvShow : selectedTvShows) {
      imageFiles.addAll(tvShow.getImagesToCache());
    }

    for (TvShowEpisode episode : selectedEpisodes) {
      imageFiles.addAll(episode.getImagesToCache());
    }

    ImageCacheTask task = new ImageCacheTask(imageFiles.stream().distinct().collect(Collectors.toList()));
    TmmTaskManager.getInstance().addUnnamedTask(task);
  }
}
